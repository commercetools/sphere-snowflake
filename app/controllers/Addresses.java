package controllers;

import controllers.actions.FormHandler;
import controllers.actions.Authorization;
import forms.addressForm.*;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.shop.model.CustomerUpdate;
import io.sphere.client.shop.model.Order;
import play.data.Form;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.customers;

import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.*;

@With(Authorization.class)
public class Addresses extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
    final static Form<AddAddress> addAddressForm = form(AddAddress.class);
    final static Form<UpdateAddress> updateAddressForm = form(UpdateAddress.class);
    final static Form<RemoveAddress> removeAddressForm = form(RemoveAddress.class);


    public static Result get(String id) {
        return ok(ListAddress.getJson(getCurrentCustomer().getAddressById(id)));
    }

    public static Result getList() {
        return ok(ListAddress.getJson(getCurrentCustomer().getAddresses()));
    }

    @With(FormHandler.class)
    public static Result add() {
        // Case missing or invalid form data
        Form<AddAddress> form = addAddressForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            return badRequest(showPage());
        }
        // Case valid add address
        AddAddress addAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().addAddress(addAddress.getAddress());
        setCurrentCustomer(sphere().currentCustomer().update(update));
        addAddress.displaySuccessMessage(getAddressBook());
        return ok(showPage());
    }

    @With(FormHandler.class)
    public static Result update() {
        // Case missing or invalid form data
        Form<UpdateAddress> form = updateAddressForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("update-address", form);
            return badRequest(showPage());
        }
        // Case valid update address
        UpdateAddress updateAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().changeAddress(updateAddress.addressId, updateAddress.getAddress());
        setCurrentCustomer(sphere().currentCustomer().update(update));
        updateAddress.displaySuccessMessage(getAddressBook());
        return ok(showPage());
    }

    @With(FormHandler.class)
    public static Result remove() {
        // Case missing or invalid form data
        Form<RemoveAddress> form = removeAddressForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("remove-address", form);
            return badRequest(showPage());
        }
        // Case valid remove address
        RemoveAddress removeAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().removeAddress(removeAddress.addressId);
        setCurrentCustomer(sphere().currentCustomer().update(update));
        removeAddress.displaySuccessMessage(getAddressBook());
        return ok(showPage());
    }

    protected static Content showPage() {
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(getCurrentCustomer()));
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        return customers.render(getCurrentCustomer(), orders, customerForm, updatePasswordForm);
    }
}
