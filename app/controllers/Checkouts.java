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
        Cart cart = sphere().currentCart().fetch();
        Document response = Payment.requestHostedList(cart, checkout.checkoutId, checkout.paymentMethod);
        String url = Payment.getRedirectUrl(response);
        // TODO Do not jump over payment platform
        notification(checkout.checkoutId);
        return success();
        //return redirect(url);
    }

    public static Result notification(String checkoutId) {
        // TODO Check it's really coming from the payment system
        Form<PaymentNotification> form = form(PaymentNotification.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        PaymentNotification payment = form.get();
        PaymentState state = Payment.getPaymentState(payment.entity, payment.statusCode, payment.reasonCode);
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
        Document response = Payment.requestNativeList(cart, checkoutId);
        if (response == null) {
            return noContent();
        }
        List<PaymentNetwork> paymentNetworks = Payment.getApplicableNetworks(response);
        String referredId = Payment.getReferredId(response);
        return ok(views.html.ajax.listPaymentNetworks.render(paymentNetworks, referredId, selected));
    }

}
