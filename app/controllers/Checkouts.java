package controllers;

import controllers.actions.FormHandler;
import controllers.actions.CartNotEmpty;
import de.paymill.Paymill;
import de.paymill.model.Payment;
import de.paymill.net.ApiException;
import de.paymill.service.PaymentService;
import forms.addressForm.ListAddress;
import forms.addressForm.SetAddress;
import forms.checkoutForm.DoCheckout;
import forms.checkoutForm.SetShippingMethod;
import io.sphere.client.shop.model.*;
import play.Play;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkouts;
import views.html.form.setAddress;
import views.html.orders;

import static play.data.Form.form;
import static utils.ControllerHelper.*;

public class Checkouts extends ShopController {

    final static String paymillKey = Play.application().configuration().getString("paymill.apiKey");

    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<SetShippingMethod> setShippingForm = form(SetShippingMethod.class);
    final static Form<DoCheckout> doCheckoutForm = form(DoCheckout.class);


    public static Result getShippingAddress() {
        return ok(ListAddress.getJson(getCurrentCart().getShippingAddress()));
    }

    public static Result getShippingMethod() {
        return ok(SetShippingMethod.getJson(getShippingMethods()));
    }

    @With(CartNotEmpty.class)
    public static Result show() {
        return ok(showPage(1));
    }

    @With(CartNotEmpty.class)
    public static Result showShippingAddress() {
        return ok(showPage(2));
    }

    @With(CartNotEmpty.class)
    public static Result showShippingMethod() {
        return ok(showPage(3));
    }

    @With(CartNotEmpty.class)
    public static Result showPaymentMethod() {
        return ok(showPage(4));
    }

    @With(FormHandler.class)
    public static Result setShippingAddress() {
        // Case missing or invalid form data
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            return badRequest(showPage(2, form, null));
        }
        // Case valid shipping address
        SetAddress setAddress = form.get();
        if (setAddress.email != null) {
            setCurrentCart(sphere().currentCart().setCustomerEmail(setAddress.email));
        }
        setCurrentCart(sphere().currentCart().setShippingAddress(setAddress.getAddress()));
        setAddress.displaySuccessMessage();
        return ok(showPage(3));
    }

    @With(FormHandler.class)
    public static Result setShippingMethod() {
        // Case missing or invalid form data
        Form<SetShippingMethod> form = setShippingForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("set-shipping", form);
            return badRequest(showPage(3, null, form));
        }
        // Case valid shipping method
        SetShippingMethod setShipping = form.get();
        setCurrentCart(sphere().currentCart().setShippingMethod(setShipping.getShippingMethod()));
        setShipping.displaySuccessMessage();
        return ok(showPage(4));
    }

    public static Result submit() {
        // Case missing or invalid form data
        Form<DoCheckout> form = doCheckoutForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("", form);
            return badRequest(showPage(4));
        }
        // Case cart changed
        DoCheckout doCheckout = form.get();
        if (!sphere().currentCart().isSafeToCreateOrder(doCheckout.cartSnapshot)) {
            doCheckout.displayCartChangedError();
            redirect(routes.Checkouts.show());
        }
        // Case payment failure
        Payment payment;
        try {
            Paymill.setApiKey(paymillKey);
            PaymentService paymentService = Paymill.getService(PaymentService.class);
            play.Logger.debug("Payment token received: " + doCheckout.paymillToken);
            payment = paymentService.create(doCheckout.paymillToken);
        } catch (ApiException ae) {
            if (ae.getCode().equals("token_not_found")) {
                flash("error", "Invalid payment token");
                return badRequest(showPage(4));
            }
            flash("error", "Payment failed unexpectedly, please try again");
            return internalServerError(showPage(4));
        }
        // Case success purchase
        play.Logger.debug("Payment executed with code " + payment.getCode());
        Order order = sphere().currentCart().createOrder(doCheckout.cartSnapshot, PaymentState.Paid);
        play.Logger.debug("Order created");
        flash("success", "Congratulations, you finished your order!");
        return ok(orders.render(order));
    }

    protected static Content showPage(int page) {
        return showPage(page, null, null);
    }

    protected static Content showPage(int page, Form<SetAddress> addressForm, Form<SetShippingMethod> shippingForm) {
        Cart cart = getCurrentCart();
        if (addressForm == null) {
            addressForm = setAddressForm.fill(new SetAddress(cart.getShippingAddress()));
        }
        if (shippingForm == null) {
            shippingForm = setShippingForm.fill(new SetShippingMethod(cart.getShippingInfo()));
        }
        // Pre-select a shipping method
        if (cart.getShippingAddress() != null && cart.getShippingInfo() == null) {
            String shippingMethodId = getDefaultShippingMethod(getShippingMethods()).getId();
            setCurrentCart(sphere().currentCart().setShippingMethod(ShippingMethod.reference(shippingMethodId)));
        }
        return checkouts.render(getCurrentCart(), getAddressBook(), addressForm, shippingForm, page);
    }
}
