package controllers;

import controllers.actions.FormHandler;
import controllers.actions.Authorization;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.exceptions.InvalidPasswordException;
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
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.getCurrentCustomer;
import static utils.ControllerHelper.setCurrentCustomer;

@With(Authorization.class)
public class Customers extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);


    public static Result show() {
        return ok(showPage());
    }

    @With(FormHandler.class)
    public static Result update() {
        // Case missing or invalid form data
        Form<UpdateCustomer> form = updateCustomerForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("update-customer", form);
            return badRequest(showPageCustomer(form));
        }
        // Case valid customer update
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate()
                .setName(updateCustomer.getCustomerName())
                .setEmail(updateCustomer.email);
        setCurrentCustomer(sphere().currentCustomer().update(update));
        updateCustomer.displaySuccessMessage();
        return ok(showPageCustomer(form));
    }

    @With(FormHandler.class)
    public static Result updatePassword() {
        // Case missing or invalid form data
        Form<UpdatePassword> form = updatePasswordForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("update-password", form);
            return badRequest(showPagePassword(form));
        }
        // Case invalid old password
        UpdatePassword updatePassword = form.get();
        try {
            sphere().currentCustomer().changePassword(updatePassword.oldPassword, updatePassword.newPassword);
            setCurrentCustomer(sphere().currentCustomer().fetch());
        } catch (InvalidPasswordException e) {
            updatePassword.displayInvalidPasswordError();
            return badRequest(showPagePassword(form));
        }
        // Case valid password update
        updatePassword.displaySuccessMessage();
        return ok(showPagePassword(form));
    }


    protected static Content showPage() {
        return showPage(null, updatePasswordForm);
    }

    protected static Content showPageCustomer(Form<UpdateCustomer> updateCustomer) {
        return showPage(updateCustomer, updatePasswordForm);
    }

    protected static Content showPagePassword(Form<UpdatePassword> updatePassword) {
        return showPage(null, updatePassword);
    }

    protected static Content showPage(Form<UpdateCustomer> updateCustomer, Form<UpdatePassword> updatePassword) {
        if (updateCustomer == null) {
            updateCustomer = updateCustomerForm.fill(new UpdateCustomer(getCurrentCustomer()));
        }
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        return customers.render(getCurrentCustomer(), orders, updateCustomer, updatePassword);
    }
}
