package controllers;

import controllers.actions.Authorization;
import forms.addressForm.SetAddress;
import forms.paymentForm.PaymentNetwork;
import forms.paymentForm.PaymentNotification;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.PaymentState;
import play.Play;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Payment;
import views.html.checkouts;

import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;

public class Checkouts extends ShopController {

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<PaymentNotification> paymentNotificationForm = form(PaymentNotification.class);

    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        String checkoutId = sphere().currentCart().createCheckoutSnapshotId();
        String submitUrl = Play.application().configuration().getString("optile.chargeUrl");
        Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
        return ok(checkouts.render(cart, checkoutId, submitUrl, addressForm));
    }

    public static Result submitShippingAddress() {
        Cart cart = sphere().currentCart().fetch();
        String checkoutId = sphere().currentCart().createCheckoutSnapshotId();
        String submitUrl = Play.application().configuration().getString("optile.chargeUrl");
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            return badRequest(checkouts.render(cart, checkoutId, submitUrl, form));
        }
        SetAddress setAddress = form.get();
        sphere().currentCart().setShippingAddress(setAddress.getAddress());
        cart = sphere().currentCart().setCountry(setAddress.getCountryCode());
        setAddress.displaySuccessMessage(cart.getShippingAddress());
        return ok(SetAddress.getJson(cart.getShippingAddress()));
    }

    public static Result notification(String checkoutId) {
        Form<PaymentNotification> form = paymentNotificationForm.bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        System.err.println("OK from " + request().remoteAddress());
        PaymentNotification paymentNotification = form.get();
        System.err.println("Notification " + paymentNotification.transactionId);
        System.err.println(paymentNotification.entity + " - " + paymentNotification.statusCode + " - " + paymentNotification.reasonCode);
        System.err.println(paymentNotification.resultCode + ": " + paymentNotification.resultInfo);
        PaymentState state = paymentNotification.getPaymentState();
        if (state.equals(PaymentState.Paid)) {
            sphere().currentCart().createOrder(checkoutId, state);
        }
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
        String checkoutId = sphere().currentCart().createCheckoutSnapshotId();
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
