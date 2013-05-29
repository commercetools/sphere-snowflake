package controllers;

import controllers.actions.Ajax;
import controllers.actions.Authorization;
import forms.addressForm.SetAddress;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
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
public class Customers extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
    final static Form<SetAddress> setAddressForm = form(SetAddress.class);


    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<UpdateCustomer> formCustomer = updateCustomerForm.fill(new UpdateCustomer(customer));
        return ok(customers.render(customer, formCustomer, updatePasswordForm, setAddressForm));
    }

    @With(Ajax.class)
    public static Result update() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<UpdateCustomer> form = updateCustomerForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-customer", form);
            return badRequest(customers.render(customer, form, updatePasswordForm, setAddressForm));
        }
        // Case valid customer update
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate()
                .setName(updateCustomer.getCustomerName())
                .setEmail(updateCustomer.email);
        customer = sphere().currentCustomer().update(update);
        updateCustomer.displaySuccessMessage(customer);
        return ok(customers.render(customer, form, updatePasswordForm, setAddressForm));
    }

    @With(Ajax.class)
    public static Result updatePassword() {
        Customer customer = sphere().currentCustomer().fetch();
        Form<UpdatePassword> form = updatePasswordForm.bindFromRequest();
        Form<UpdateCustomer> formCustomer = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-password", form);
            return badRequest(customers.render(customer, formCustomer, form, setAddressForm));
        }
        // Case invalid old password
        UpdatePassword updatePassword = form.get();
        try {
            sphere().currentCustomer().changePassword(updatePassword.oldPassword, updatePassword.newPassword);
        } catch (InvalidPasswordException e) {
            updatePassword.displayInvalidPasswordError();
            return badRequest(customers.render(customer, formCustomer, form, setAddressForm));
        }
        // Case valid password update
        updatePassword.displaySuccessMessage();
        return ok(customers.render(customer, formCustomer, form, setAddressForm));
    }
}
