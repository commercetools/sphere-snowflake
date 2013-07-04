package controllers;

import controllers.actions.Ajax;
import controllers.actions.CartNotEmpty;
import de.paymill.Paymill;
import de.paymill.model.Payment;
import de.paymill.net.ApiException;
import de.paymill.service.PaymentService;
import forms.addressForm.ListAddress;
import forms.addressForm.SetAddress;
import forms.paymentForm.DoCheckout;
import forms.paymentForm.PaymentNotification;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.Order;
import io.sphere.client.shop.model.PaymentState;
import play.Play;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkouts;
import views.html.orders;

import java.util.Collections;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.getAddressBook;

public class Checkouts extends ShopController {

    final static String paymillKey = Play.application().configuration().getString("paymill.apiKey");

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<DoCheckout> doCheckoutForm = form(DoCheckout.class);


    @With(CartNotEmpty.class)
    public static Result show() {
        return ok(showPage(1));
    }

    @With(CartNotEmpty.class)
    public static Result showShippingAddress() {
        return ok(showPage(2));
    }

    @With(CartNotEmpty.class)
    public static Result showPaymentMethod() {
        return ok(showPage(3));
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
        setAddress.displaySuccessMessage(cart, sphere().currentCart().createCartSnapshotId());
        return ok(checkouts.render(cart, getAddressBook(), form, 2));
    }

    public static Result submit() {
        Form<DoCheckout> form = doCheckoutForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("", form);
            return badRequest(showPage(3));
        }
        // Case cart changed
        DoCheckout doCheckout = form.get();
        if (!sphere().currentCart().isSafeToCreateOrder(doCheckout.cartSnapshot)) {
            flash("info", "Your cart items have changed, please review everything is correct");
            redirect(routes.Checkouts.show());
        }
        // Case payment failure
        Payment payment;
        try {
            Paymill.setApiKey(paymillKey);
            PaymentService paymentService = Paymill.getService(PaymentService.class);
            System.out.println(doCheckout.paymillToken);
            payment = paymentService.create(doCheckout.paymillToken);
        } catch (ApiException ae) {
            if (ae.getCode().equals("token_not_found")) {
                flash("error", "Invalid payment token");
                return badRequest(showPage(3));
            }
            flash("error", "Payment failed unexpectedly, please try again");
            return internalServerError(showPage(3));
        }
        // Case success purchase
        System.out.println(payment.getCode());
        Order order = sphere().currentCart().createOrder(doCheckout.cartSnapshot, PaymentState.Paid);
        flash("success", "Congratulations, you finished your order!");
        return ok(orders.render(order));
    }

    protected static Html showPage(int page) {
        Cart cart = sphere().currentCart().fetch();
        Form<SetAddress> addressForm;
        if (sphere().isLoggedIn()) {
            Customer customer = sphere().currentCustomer().fetch();
            addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress(), customer));
        } else {
            addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
        }
        return checkouts.render(cart, getAddressBook(), addressForm, page);
    }
}
