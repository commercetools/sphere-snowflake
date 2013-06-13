package controllers;

import controllers.actions.Ajax;
import controllers.actions.CartNotEmpty;
import forms.addressForm.ListAddress;
import forms.addressForm.SetAddress;
import forms.paymentForm.PaymentNetwork;
import forms.paymentForm.PaymentNotification;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.Order;
import org.codehaus.jackson.node.ObjectNode;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Payment;
import views.html.checkouts;
import views.html.helper.order;
import views.html.orders;

import java.util.Collections;
import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.getAddressBook;

public class Checkouts extends ShopController {

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<PaymentNotification> paymentNotificationForm = form(PaymentNotification.class);


    @With(CartNotEmpty.class)
    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        Customer customer = sphere().currentCustomer().fetch();
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress(), customer));
        return ok(checkouts.render(cart, cartSnapshot, getAddressBook(), addressForm, 1));
    }

    public static Result showSummary(String orderId) {
        //while (!sphere().orders().byId(orderId).fetch().isPresent()) {
            // Waiting for the order to be created by the notification call

        //}
        //Order order = sphere().orders().byId(orderId).fetch().orNull();
        //return ok(orders.render(Collections.singletonList(order)));
        return ok("Order created!");
    }

    public static Result getShippingAddress() {
        Cart cart = sphere().currentCart().fetch();
        return ok(ListAddress.getJson(cart.getShippingAddress()));
    }

    @With(CartNotEmpty.class)
    public static Result showShippingAddress() {
        Cart cart = sphere().currentCart().fetch();
        Customer customer = sphere().currentCustomer().fetch();
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress(), customer));
        return ok(checkouts.render(cart, cartSnapshot, getAddressBook(), addressForm, 2));
    }

    @With(CartNotEmpty.class)
    public static Result showPaymentMethod() {
        Cart cart = sphere().currentCart().fetch();
        Customer customer = sphere().currentCustomer().fetch();
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        Form<SetAddress> addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress(), customer));
        return ok(checkouts.render(cart, cartSnapshot, getAddressBook(), addressForm, 3));
    }

    @With(Ajax.class)
    public static Result setShippingAddress() {
        Cart cart;
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            cart = sphere().currentCart().fetch();
            return badRequest(checkouts.render(cart, cartSnapshot, getAddressBook(), form, 2));
        }
        // Case valid shipping address
        SetAddress setAddress = form.get();
        if (setAddress.email != null) {
            sphere().currentCart().setCustomerEmail(setAddress.email);
        }
        //sphere().currentCart().setCountry(setAddress.getCountryCode());
        cart = sphere().currentCart().setShippingAddress(setAddress.getAddress());
        setAddress.displaySuccessMessage(cart.getShippingAddress());
        return ok(checkouts.render(cart, cartSnapshot, getAddressBook(), form, 2));
    }

    public static Result getPaymentMethod(String cartSnapshot) {
        Cart cart = sphere().currentCart().fetch();
        // Case not synchronized cart with the one displayed
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            ObjectNode json = Json.newObject();
            json.put("redirect", routes.Checkouts.show().url());
            // TODO SDK: Consider a safe cart when it contains the same items or items price (no shipping or taxes?)
            //return ok(json);
        }
        // Case no shipping address
        if (cart.getShippingAddress() == null) {
            return noContent();
        }
        // Case no payment needed
        if (cart.getTotalPrice().getAmount().doubleValue() <= 0) {
            return noContent();
        }
        // Case failed request
        Payment payment = new Payment(cart, cartSnapshot);
        if (!payment.doRequest(Payment.NATIVE_URL, Payment.Operation.LIST)) {
            return internalServerError();
        }
        // Case success request
        List<PaymentNetwork> paymentNetworks = payment.getApplicableNetworks();
        String referredId = payment.getReferredId();
        return ok(PaymentNetwork.getJson(paymentNetworks, referredId));
    }

    public static Result notification(String cartSnapshot) {
        // Case missing or invalid data
        Form<PaymentNotification> form = paymentNotificationForm.bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        // Case not safe order
        PaymentNotification paymentNotification = form.get();
        System.err.println("Notification " + paymentNotification.transactionId);
        System.err.println(paymentNotification.entity + " - " + paymentNotification.statusCode + " - " + paymentNotification.reasonCode);
        System.err.println(paymentNotification.resultCode + ": " + paymentNotification.resultInfo);
        Cart cart = sphere().currentCart().fetch();
        Payment payment = new Payment(cart, cartSnapshot, paymentNotification.longId);
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            System.out.println("Canceling payment");
            payment.doRequest(Payment.NATIVE_URL, Payment.Operation.CANCELATION);
            return badRequest();
        }
        // Case valid order
        System.out.println("Closing payment");
        if (payment.doRequest(Payment.NATIVE_URL, Payment.Operation.CLOSING)) {
            System.out.println("Creating order");
            sphere().currentCart().createOrder(cartSnapshot, paymentNotification.getPaymentState());
        }
        return ok();
    }

    public static Result success(String orderId) {
        flash("success", "Your payment has been processed, thank you for shopping with us!");
        return redirect(routes.Checkouts.showSummary(orderId));
    }

    public static Result failure() {
        flash("error", "Payment process aborted, please start over with another payment method.");
        return redirect(routes.Checkouts.showPaymentMethod());
    }
}
