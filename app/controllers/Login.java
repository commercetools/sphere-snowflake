package controllers;

import forms.customerForm.LogIn;
import forms.passwordForm.RecoverPassword;
import forms.passwordForm.ResetPassword;
import forms.customerForm.SignUp;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerToken;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;
import utils.Email;

import static play.data.Form.form;

public class Login extends ShopController {

    public static Result show() {
        return ok(views.html.login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), ""));
    }

    public static Result signUp() {
        Form<SignUp> form = form(SignUp.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        SignUp signUp = form.get();
        // TODO SDK: Deal with already registered user
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

    public static Result recoverPassword() {
        Form<RecoverPassword> form = form(RecoverPassword.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        RecoverPassword recoverPassword = form.get();
        CustomerToken token = sphere().customers.createPasswordResetToken(recoverPassword.email).execute();
        System.out.println(token.getCustomerId());
        System.out.println(token.getValue());
        String url = routes.Login.showResetPassword(token.getValue()).absoluteURL(request());
        String body = views.html.mail.forgetPassword.render(url).body();
        Email email = new Email(recoverPassword.email, "Password recovery", body);
        //email.send();
        return ok(recoverPassword.getJson(url));
    }

    public static Result showResetPassword(String token) {
        Customer customer = sphere().customers.byToken(token).fetch().orNull();
        if (customer == null) {
            flash("error", "Either you followed an invalid link or your request expired");
            badRequest(views.html.login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), ""));
        }
        ResetPassword resetPassword = new ResetPassword(token);
        Form<ResetPassword> form = form(ResetPassword.class).fill(resetPassword);
        String resetPasswordHtml = views.html.helper.resetPassword.render(form).body();
        return ok(views.html.login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), resetPasswordHtml));
    }

    public static Result resetPassword() {
        Form<ResetPassword> form = form(ResetPassword.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        ResetPassword resetPassword = form.get();
        //sphere().currentCustomer().resetPassword(resetPassword.token, resetPassword.newPassword);
        return ok(resetPassword.getJson());
    }
}
