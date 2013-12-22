package controllers;

import controllers.actions.FormHandler;
import forms.customerForm.LogIn;
import forms.passwordForm.RecoverPassword;
import forms.passwordForm.ResetPassword;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import io.sphere.client.exceptions.InvalidPasswordException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerToken;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Email;
import views.html.login;
import views.html.mail.forgetPassword;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.saveFlash;

public class Login extends ShopController {

    final static Form<LogIn> logInForm = form(LogIn.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);
    final static Form<RecoverPassword> recoverPasswordForm = form(RecoverPassword.class);
    final static Form<ResetPassword> resetPasswordForm = form(ResetPassword.class);


    public static Result show() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        return ok(login.render(false, logInForm, signUpForm, recoverPasswordForm, resetPasswordForm));
    }

    public static Result showSignUp() {
        return show();
    }

    @With(FormHandler.class)
    public static Result signUp() {
        Form<SignUp> form = signUpForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("sign-up", form);
            return badRequest(login.render(false, logInForm, form, recoverPasswordForm, resetPasswordForm));
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return redirect(routes.Customers.show());
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            signUp.displayAlreadyRegisteredError();
            return badRequest(login.render(false, logInForm, form, recoverPasswordForm, resetPasswordForm));
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

    @With(FormHandler.class)
    public static Result logIn() {
        Form<LogIn> form = logInForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("log-in", form);
            return badRequest(login.render(false, form, signUpForm, recoverPasswordForm, resetPasswordForm));
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
            return badRequest(login.render(false, form, signUpForm, recoverPasswordForm, resetPasswordForm));
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

    @With(FormHandler.class)
    public static Result recoverPassword() {
        Form<RecoverPassword> form = recoverPasswordForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("recover-password", form);
            return badRequest(login.render(false, logInForm, signUpForm, form, resetPasswordForm));
        }
        // Case not registered email
        RecoverPassword recoverPassword = form.get();
        CustomerToken token;
        try {
            token = sphere().customers().createPasswordResetToken(recoverPassword.email);
        } catch (InvalidPasswordException e) {
            recoverPassword.displayInvalidEmailError();
            return badRequest(login.render(false, logInForm, signUpForm, form, resetPasswordForm));
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
        Customer customer = sphere().customers().byToken(token).fetch().orNull();
        if (customer == null) {
            saveFlash("error", "Either you followed an invalid link or your request expired");
            badRequest(login.render(false, logInForm, signUpForm, recoverPasswordForm, resetPasswordForm));
        }
        // Case success
        Form<ResetPassword> form = resetPasswordForm.fill(new ResetPassword(token));
        return ok(login.render(true, logInForm, signUpForm, recoverPasswordForm, form));
    }

    @With(FormHandler.class)
    public static Result resetPassword() {
        Form<ResetPassword> form = resetPasswordForm.bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("reset-password", form);
            return badRequest(login.render(true, logInForm, signUpForm, recoverPasswordForm, form));
        }
        // Case invalid token
        ResetPassword resetPassword = form.get();
        Customer customer = sphere().customers().byToken(resetPassword.token).fetch().orNull();
        if (customer == null) {
            resetPassword.displayInvalidTokenError();
            return badRequest(login.render(true, logInForm, signUpForm, recoverPasswordForm, form));
        }
        // Case valid reset password
        sphere().customers().resetPassword(customer.getIdAndVersion(), resetPassword.token, resetPassword.newPassword);
        resetPassword.displaySuccessMessage();
        return ok(login.render(false, logInForm, signUpForm, recoverPasswordForm, resetPasswordForm));
    }
}
