package controllers;

import forms.LogIn;
import forms.ResetPassword;
import forms.SignUp;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerToken;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;

import static play.data.Form.form;

public class Login extends ShopController {

    public static Result show() {
        CustomerToken tokenResetPassword = sphere().currentCustomer().createEmailVerificationToken(60*24);
        return ok(views.html.login.render(tokenResetPassword, form(LogIn.class), form(SignUp.class)));
    }

    public static Result signUp() {
        Form<SignUp> form = form(SignUp.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        SignUp signUp = form.get();
        sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        return ok(signUp.getJson(routes.Customers.show()));
    }

    public static Result logIn() {
        Form<LogIn> form = form(LogIn.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        LogIn logIn = form.get();
        if (!sphere().login(logIn.email, logIn.password)) {
            return badRequest(logIn.getJsonCredentialsMatchError());
        }
        return ok(logIn.getJson(routes.Customers.show()));
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

    public static Result showResetPassword(String token) {
        return ok();
    }

    public static Result resetPassword(String token) {
        Form<ResetPassword> form = form(ResetPassword.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        ResetPassword resetPassword = form.get();
        Customer customer = sphere().currentCustomer().resetPassword(token, resetPassword.newPassword);
        return ok();
    }
}
