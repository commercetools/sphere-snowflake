package controllers;

import controllers.actions.Ajax;
import controllers.actions.Authorization;
import forms.addressForm.SetAddress;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import io.sphere.client.shop.model.Order;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.customers;

import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;

@With(Authorization.class)
public class Customers extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);


    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        return ok(customers.render(customer, orders, customerForm, updatePasswordForm));
    }

    @With(Ajax.class)
    public static Result update() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdateCustomer> form = updateCustomerForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-customer", form);
            return badRequest(customers.render(customer, orders, form, updatePasswordForm));
        }
        // Case valid customer update
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate()
                .setName(updateCustomer.getCustomerName())
                .setEmail(updateCustomer.email);
        customer = sphere().currentCustomer().update(update);
        updateCustomer.displaySuccessMessage(customer);
        return ok(customers.render(customer, orders, form, updatePasswordForm));
    }

    @With(Ajax.class)
    public static Result updatePassword() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdatePassword> form = updatePasswordForm.bindFromRequest();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-password", form);
            return badRequest(customers.render(customer, orders, customerForm, form));
        }
        // Case invalid old password
        UpdatePassword updatePassword = form.get();
        try {
            sphere().currentCustomer().changePassword(updatePassword.oldPassword, updatePassword.newPassword);
        } catch (InvalidPasswordException e) {
            updatePassword.displayInvalidPasswordError();
            return badRequest(customers.render(customer, orders, customerForm, form));
        }
        // Case valid password update
        updatePassword.displaySuccessMessage();
        return ok(customers.render(customer, orders, customerForm, form));
    }
}
