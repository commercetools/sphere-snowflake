package controllers;

import controllers.actions.Ajax;
import controllers.actions.Authorization;
import forms.addressForm.ListAddress;
import forms.addressForm.RemoveAddress;
import forms.addressForm.SetAddress;
import forms.addressForm.UpdateAddress;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.customers;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;

@With(Authorization.class)
public class Addresses extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
    final static Form<SetAddress> setAddressForm = form(SetAddress.class);
    final static Form<UpdateAddress> updateAddressForm = form(UpdateAddress.class);
    final static Form<RemoveAddress> removeAddressForm = form(RemoveAddress.class);


    public static Result get() {
        Customer customer = sphere().currentCustomer().fetch();
        return ok(ListAddress.getJson(customer.getAddresses()));
    }

    @With(Ajax.class)
    public static Result add() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<SetAddress> form = setAddressForm.bindFromRequest();
        Form<UpdateCustomer> formCustomer = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            return badRequest(customers.render(customer, formCustomer, updatePasswordForm, form));
        }
        // Case valid add address
        SetAddress setAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().addAddress(setAddress.getAddress());
        customer = sphere().currentCustomer().update(update);
        setAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
    }

    @With(Ajax.class)
    public static Result update() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<UpdateAddress> form = updateAddressForm.bindFromRequest();
        Form<UpdateCustomer> formCustomer = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-address", form);
            return badRequest(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
        }
        // Case valid update address
        UpdateAddress updateAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().changeAddress(updateAddress.addressId, updateAddress.getAddress());
        customer = sphere().currentCustomer().update(update);
        updateAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
    }

    @With(Ajax.class)
    public static Result remove() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<RemoveAddress> form = removeAddressForm.bindFromRequest();
        Form<UpdateCustomer> formCustomer = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("remove-address", form);
            return badRequest(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
        }
        // Case valid remove address
        RemoveAddress removeAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().removeAddress(removeAddress.addressId);
        customer = sphere().currentCustomer().update(update);
        removeAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
    }
}
