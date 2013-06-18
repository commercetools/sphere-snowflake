package controllers;

import controllers.actions.Ajax;
import controllers.actions.CartNotEmpty;
import forms.addressForm.ListAddress;
import forms.addressForm.SetAddress;
import forms.paymentForm.PaymentNetwork;
import forms.paymentForm.PaymentNotification;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.Customer;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Payment;
import views.html.checkouts;

import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.getAddressBook;

public class Checkouts extends ShopController {

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<PaymentNotification> paymentNotificationForm = form(PaymentNotification.class);


    @With(CartNotEmpty.class)
    public static Result show() {
        return showPage(1);
    }

    @With(CartNotEmpty.class)
    public static Result showShippingAddress() {
        return showPage(2);
    }

    @With(CartNotEmpty.class)
    public static Result showPaymentMethod() {
        return showPage(3);
    }

    protected static Result showPage(int page) {
        Cart cart = sphere().currentCart().fetch();
        Form<SetAddress> addressForm;
        if (sphere().isLoggedIn()) {
            Customer customer = sphere().currentCustomer().fetch();
            addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress(), customer));
        } else {
            addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
        }
        return ok(checkouts.render(cart, getAddressBook(), addressForm, page));
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

    @With(Ajax.class)
    public static Result setShippingAddress() {
        Cart cart;
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            cart = sphere().currentCart().fetch();
            return badRequest(checkouts.render(cart, getAddressBook(), form, 2));
        }
        // Case valid shipping address
        SetAddress setAddress = form.get();
        if (setAddress.email != null) {
            sphere().currentCart().setCustomerEmail(setAddress.email);
        }
        cart = sphere().currentCart().setShippingAddress(setAddress.getAddress());
        setAddress.displaySuccessMessage(cart.getShippingAddress());
        System.out.println("set shipping " + sphere().currentCart().createCartSnapshotId());
        return ok(checkouts.render(cart, getAddressBook(), form, 2));
    }

    public static Result getPaymentMethod() {
        Cart cart = sphere().currentCart().fetch();
        String cartSnapshot = sphere().currentCart().createCartSnapshotId();
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
        System.out.println("Sending payment information " + cartSnapshot);
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
        if (!sphere().currentCart().isSafeToCreateOrder(cartSnapshot)) {
            System.out.println("Cart changed! " + cartSnapshot + " - " + sphere().currentCart().createCartSnapshotId());
            System.out.println("Cart changed! New cart: " + cart.getIdAndVersion());
            return badRequest();
        }
        // Case valid order
        System.out.println("Closing payment");
        Payment payment = new Payment(cart, cartSnapshot, paymentNotification.longId);
        if (payment.doRequest(Payment.NATIVE_URL, Payment.Operation.CHARGE)) {
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
