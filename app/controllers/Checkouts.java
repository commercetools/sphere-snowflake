package controllers;

import com.google.common.primitives.Ints;
import controllers.actions.FormHandler;
import controllers.actions.CartNotEmpty;
import de.paymill.Paymill;
import de.paymill.PaymillException;
import de.paymill.model.Payment;
import de.paymill.model.Transaction;
import de.paymill.net.ApiException;
import de.paymill.service.PaymentService;
import de.paymill.service.TransactionService;
import forms.addressForm.ListAddress;
import forms.addressForm.SetAddress;
import forms.checkoutForm.DoCheckout;
import forms.checkoutForm.SetShippingMethod;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import play.Play;
import play.data.Form;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.checkouts;
import views.html.orders;

import static play.data.Form.form;
import static utils.ControllerHelper.*;
import static utils.ViewHelper.getCurrentCart;
import static utils.ViewHelper.getPrice;

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
        try {
            // Get payment object from token
            Paymill.setApiKey(paymillKey);
            PaymentService paymentSrv = Paymill.getService(PaymentService.class);
            Payment payment = paymentSrv.create(doCheckout.paymillToken);
            // Set transaction details
            TransactionService transactionSrv = Paymill.getService(TransactionService.class);
            Transaction transaction = new Transaction();
            Money money = getPrice(getCurrentCart());
            transaction.setPayment(payment);
            transaction.setAmount(Ints.checkedCast(money.getCentAmount()));
            transaction.setCurrency(money.getCurrencyCode());
            // Execute charge transaction
            play.Logger.debug("Cart " + doCheckout.cartSnapshot + " - Executing payment " + payment.getId()
                    + " of " + transaction.getAmount() + " (cents) " + transaction.getCurrency()
                    + " with token " + doCheckout.paymillToken);
            transactionSrv.create(transaction);
        } catch (PaymillException pe) {
            play.Logger.error(pe.getMessage());
            flash("error", "Payment failed unexpectedly, please try again");
            return internalServerError(showPage(4));
        }
        // Case success purchase
        Order order = sphere().currentCart().createOrder(doCheckout.cartSnapshot, PaymentState.Paid);
        play.Logger.debug("Cart " + doCheckout.cartSnapshot + " - Order created");
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
