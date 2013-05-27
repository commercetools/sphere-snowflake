package controllers;

import controllers.actions.Ajax;
import controllers.actions.Authorization;
import forms.customerForm.LogIn;
import forms.passwordForm.RecoverPassword;
import forms.passwordForm.ResetPassword;
import forms.customerForm.SignUp;
import io.sphere.client.SphereBackendException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerToken;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Email;
import views.html.login;
import views.html.mail.forgetPassword;
import views.html.mail.verifyAccount;
import views.html.helper.resetPassword;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.saveFlash;

public class Login extends ShopController {

    public static Result show() {
        return ok(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), ""));
    }

    public static Result showSignUp() {
        return show();
    }

    @With(Ajax.class)
    public static Result signUp() {
        Form<SignUp> form = form(SignUp.class).bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("sign-up", form);
            return badRequest(login.render(form(LogIn.class), form, form(RecoverPassword.class), ""));
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return redirect(routes.Customers.show());
        }
        // Case already registered user
        // TODO SDK: Deal with already registered user nicely
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch(SphereBackendException sbe) {
            signUp.displayAlreadyRegisteredError();
            return badRequest(login.render(form(LogIn.class), form, form(RecoverPassword.class), ""));
        }
        // Case valid sign up
        // TODO SDK: Allow email verification
        //CustomerToken token = sphere().currentCustomer().createEmailVerificationToken(24*60);
        //String url = routes.Login.verify(token.getValue()).absoluteURL(request());
        //Email email = new Email(signUp.email, "Activate account", verifyAccount.render(url).body());
        // TODO Enable send email and change result to OK with login view when email is working
        //email.send();
        signUp.displaySuccessMessage();
        //return redirect(url);
        return redirect(routes.Customers.show());
    }

    public static Result verify(String token) {
        if (sphere().isLoggedIn()) {
            sphere().currentCustomer().confirmEmail(token);
            flash("Great! Your account is now activated");
        }
        return redirect(session("returnUrl"));
    }

    @With(Ajax.class)
    public static Result logIn() {
        Form<LogIn> form = form(LogIn.class).bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("log-in", form);
            return badRequest(login.render(form, form(SignUp.class), form(RecoverPassword.class), ""));
        }
        // Case already logged in
        LogIn logIn = form.get();
        if (sphere().isLoggedIn()) {
            Customer customer = sphere().currentCustomer().fetch();
            logIn.displaySuccessMessage(customer.getName().getFirstName());
            return redirect(routes.Customers.show());
        }
        // Case invalid credentials
        if (!sphere().login(logIn.email, logIn.password)) {
            logIn.displayInvalidCredentialsError();
            return badRequest(login.render(form, form(SignUp.class), form(RecoverPassword.class), ""));
        }
        // Case valid log in
        Customer customer = sphere().currentCustomer().fetch();
        logIn.displaySuccessMessage(customer.getName().getFirstName());
        return redirect(routes.Customers.show());
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

    @With(Ajax.class)
    public static Result recoverPassword() {
        Form<RecoverPassword> form = form(RecoverPassword.class).bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("recover-password", form);
            return badRequest(login.render(form(LogIn.class), form(SignUp.class), form, ""));
        }
        // Case not registered email
        RecoverPassword recoverPassword = form.get();
        CustomerToken token;
        try {
            token = sphere().customers.createPasswordResetToken(recoverPassword.email);
        } catch (SphereBackendException sbe) {
            recoverPassword.displayInvalidEmailError();
            return badRequest(login.render(form(LogIn.class), form(SignUp.class), form, ""));
        }
        // Case valid recover password
        String url = routes.Login.showResetPassword(token.getValue()).absoluteURL(request());
        Email email = new Email(recoverPassword.email, "Password recovery", forgetPassword.render(url).body());
        // TODO Enable send email and change result to OK with login view when email is working
        //email.send();
        recoverPassword.displaySuccessMessage();
        return redirect(url);
    }

    public static Result showResetPassword(String token) {
        // Case invalid token
        Customer customer = sphere().customers.byToken(token).fetch().orNull();
        if (customer == null) {
            saveFlash("error", "Either you followed an invalid link or your request expired");
            badRequest(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), ""));
        }
        // Case success
        Form<ResetPassword> form = form(ResetPassword.class).fill(new ResetPassword(token));
        String resetPasswordHtml = resetPassword.render(form).body();
        return ok(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), resetPasswordHtml));
    }

    @With(Ajax.class)
    public static Result resetPassword() {
        Form<ResetPassword> form = form(ResetPassword.class).bindFromRequest();
        String resetPasswordHtml = resetPassword.render(form).body();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("reset-password", form);
            return badRequest(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), resetPasswordHtml));
        }
        // Case invalid token
        ResetPassword resetPassword = form.get();
        Customer customer = sphere().customers.byToken(resetPassword.token).fetch().orNull();
        if (customer == null) {
            resetPassword.displayInvalidTokenError();
            return badRequest(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), resetPasswordHtml));
        }
        // Case valid reset password
        sphere().customers.resetPassword(customer.getIdAndVersion(), resetPassword.token, resetPassword.newPassword);
        resetPassword.displaySuccessMessage();
        return ok(login.render(form(LogIn.class), form(SignUp.class), form(RecoverPassword.class), ""));
    }
}
