package controllers;

import controllers.actions.Authorization;
import forms.*;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.PaymentState;
import org.w3c.dom.Document;
import play.Play;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Payment;

import java.util.List;

import static play.data.Form.form;

public class Checkouts extends ShopController {

    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        String checkoutId = sphere().currentCart().createCheckoutSummaryId();
        AddAddress draftAddress = new AddAddress(cart.getShippingAddress());
        Form<AddAddress> addressForm = form(AddAddress.class).fill(draftAddress);
        String submitUrl = Play.application().configuration().getString("optile.chargeUrl");
        return ok(views.html.checkouts.render(cart, checkoutId, submitUrl, addressForm));
    }

    public static Result submitShippingAddress() {
        Form<AddAddress> form = form(AddAddress.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        AddAddress addAddress = form.get();
        sphere().currentCart().setShippingAddress(addAddress.getAddress());
        return ok();
    }

    public static Result submit() {
        Form<Checkout> form = form(Checkout.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        Checkout checkout = form.get();
        if (sphere().currentCart().isSafeToCreateOrder(checkout.checkoutId)) {
            sphere().currentCart().createOrder(checkout.checkoutId, PaymentState.Pending);
            return success();
        }
        return failure();
    }

    public static Result notification(String checkoutId) {
        Form<PaymentNotification> form = form(PaymentNotification.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        PaymentNotification paymentNotification = form.get();
        PaymentState state = paymentNotification.getPaymentState();
        sphere().currentCart().createOrder(checkoutId, state);
        return ok();
    }

    @With(Authorization.class)
    public static Result success() {
        flash("success", "Your payment has been processed, thank you for shopping with us!");
        // TODO Redirect to somewhere
        return Categories.home(1);
    }

    @With(Authorization.class)
    public static Result failure() {
        flash("error", "Payment process aborted, please start over with another payment method.");
        // TODO Redirect to somewhere
        return badRequest("Failed...");
    }

    public static Result listPaymentNetworks(String selected) {
        Cart cart = sphere().currentCart().fetch();
        if (cart.getTotalPrice().getAmount().doubleValue() <= 0) {
            return noContent();
        }
        String checkoutId = sphere().currentCart().createCheckoutSummaryId();
        Payment payment = new Payment(cart, checkoutId);
        String transactionId = payment.doRequest(Payment.NATIVE_URL, Payment.Operation.LIST);
        if (!payment.isValidResponse(transactionId)) {
            return noContent();
        }
        List<PaymentNetwork> paymentNetworks = payment.getApplicableNetworks();
        String referredId = payment.getReferredId();
        return ok(views.html.ajax.listPaymentNetworks.render(paymentNetworks, referredId, selected));
    }

}
