package controllers;

import controllers.actions.Authorization;
import forms.addressForm.SetAddress;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

import static play.data.Form.form;

@With(Authorization.class)
public class Customers extends ShopController {

    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        UpdateCustomer updateCustomer = new UpdateCustomer(customer);
        Form<UpdateCustomer> form = form(UpdateCustomer.class).fill(updateCustomer);
        return ok(views.html.customers.render(customer, form, form(UpdatePassword.class), form(SetAddress.class)));
    }

    public static Result update() {
        Form<UpdateCustomer> form = form(UpdateCustomer.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        UpdateCustomer updateCustomer = form.get();
        CustomerUpdate update = new CustomerUpdate()
                .setName(updateCustomer.getCustomerName())
                .setEmail(updateCustomer.email);
        Customer customer = sphere().currentCustomer().updateCustomer(update);
        return ok(updateCustomer.getJson(customer));
    }

    public static Result updatePassword() {
        Form<UpdatePassword> form = form(UpdatePassword.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        UpdatePassword updatePassword = form.get();
        if (!sphere().currentCustomer().changePassword(updatePassword.oldPassword, updatePassword.newPassword)) {
            return badRequest(updatePassword.getJsonPasswordMatchError());
        }
        return ok(updatePassword.getJson());
    }
}
