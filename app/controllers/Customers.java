package controllers;

import controllers.actions.Authorization;
import forms.UpdateCustomer;
import forms.UpdatePassword;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

import static play.data.Form.form;
import static utils.ControllerHelper.generateErrorMessages;

@With(Authorization.class)
public class Customers extends ShopController {

    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        UpdateCustomer updateCustomer = new UpdateCustomer(customer);
        Form<UpdateCustomer> form = form(UpdateCustomer.class).fill(updateCustomer);
        return ok(views.html.customers.render(customer, form, form(UpdatePassword.class)));
    }

    public static Result update() {
        Form<UpdateCustomer> form = form(UpdateCustomer.class).bindFromRequest();
        if (form.hasErrors()) {
            generateErrorMessages(form, "update-customer");
            return badRequest();
        }
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate();
        update.setName(updateCustomer.getCustomerName());
        update.setEmail(updateCustomer.email);
        sphere().currentCustomer().updateCustomer(update);
        return ok();
    }

    public static Result updatePassword() {
        Form<UpdatePassword> form = form(UpdatePassword.class).bindFromRequest();
        if (form.hasErrors()) {
            generateErrorMessages(form, "update-password");
            return badRequest();
        }
        UpdatePassword updatePassword = form.get();
        sphere().currentCustomer().changePassword(updatePassword.passwordOld, updatePassword.passwordNew);
        return ok();
    }

}
