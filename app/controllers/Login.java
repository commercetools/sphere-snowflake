package controllers;

import controllers.actions.FormHandler;
import forms.customerForm.LogIn;
import forms.passwordForm.RecoverPassword;
import forms.passwordForm.ResetPassword;
import forms.customerForm.SignUp;
import io.sphere.client.exceptions.EmailAlreadyInUseException;
import io.sphere.client.exceptions.SphereBackendException;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerToken;
import play.data.Form;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import utils.Email;
import views.html.login;
import views.html.mail.forgetPassword;


import static play.data.Form.form;
import static utils.ControllerHelper.*;

public class Login extends ShopController {

    final static Form<LogIn> logInForm = form(LogIn.class);
    final static Form<SignUp> signUpForm = form(SignUp.class);
    final static Form<RecoverPassword> recoverPasswordForm = form(RecoverPassword.class);
    final static Form<ResetPassword> resetPasswordForm = form(ResetPassword.class);


    public static Result show() {
        if (sphere().isLoggedIn()) {
            sphere().logout();
        }
        return ok(showPage());
    }

    public static Result showSignUp() {
        return show();
    }

    @With(FormHandler.class)
    public static Result signUp() {
        // Case missing or invalid form data
        Form<SignUp> form = signUpForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("sign-up", form);
            return badRequest(showPageSignUp(form));
        }
        // Case already signed up
        SignUp signUp = form.get();
        if (sphere().login(signUp.email, signUp.password)) {
            return redirect(controllers.routes.Customers.show());
        }
        // Case already registered email
        try {
            sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        } catch (EmailAlreadyInUseException e) {
            signUp.displayAlreadyRegisteredError();
            return badRequest(showPageSignUp(form));
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
        return redirect(controllers.routes.Customers.show());
    }

    public static Result verify(String token) {
        if (sphere().isLoggedIn()) {
            setCurrentCustomer(sphere().currentCustomer().confirmEmail(token));
            flash("success", "Great! Your account is now activated");
        }
        return redirect(session("returnUrl"));
    }

    @With(FormHandler.class)
    public static Result logIn() {
        // Case missing or invalid form data
        Form<LogIn> form = logInForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("log-in", form);
            return badRequest(showPageLogin(form));
        }
        // Case already logged in
        LogIn logIn = form.get();
        if (sphere().isLoggedIn()) {
            logIn.displaySuccessMessage();
            return redirect(controllers.routes.Customers.show());
        }
        // Case invalid credentials
        if (!sphere().login(logIn.email, logIn.password)) {
            logIn.displayInvalidCredentialsError();
            return badRequest(showPageLogin(form));
        }
        // Case valid log in
        logIn.displaySuccessMessage();
        return redirect(controllers.routes.Customers.show());
    }

    public static Result logOut() {
        sphere().logout();
        return redirect(session("returnUrl"));
    }

    @With(FormHandler.class)
    public static Result recoverPassword() {
        // Case missing or invalid form data
        Form<RecoverPassword> form = recoverPasswordForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("recover-password", form);
            return badRequest(showPageRecover(form));
        }
        // Case not registered email
        RecoverPassword recoverPassword = form.get();
        CustomerToken token;
        try {
            token = sphere().customers().createPasswordResetToken(recoverPassword.email);
        } catch (SphereBackendException sbe) {
            recoverPassword.displayInvalidEmailError();
            return badRequest(showPageRecover(form));
        }
        // Case valid recover password
        String url = controllers.routes.Login.showResetPassword(token.getValue()).absoluteURL(request());
        Email.send(recoverPassword.email, "Password recovery", forgetPassword.render(url).body());
        recoverPassword.displaySuccessMessage();
        return ok();
    }

    public static Result showResetPassword(String token) {
        // Case invalid token
        Customer customer = sphere().customers().byToken(token).fetch().orNull();
        if (customer == null) {
            saveFlash("error", "Either you followed an invalid link or your request expired");
            badRequest(showPage());
        }
        // Case success
        Form<ResetPassword> form = resetPasswordForm.fill(new ResetPassword(token));
        return ok(showPageReset(form));
    }

    @With(FormHandler.class)
    public static Result resetPassword() {
        // Case missing or invalid form data
        Form<ResetPassword> form = resetPasswordForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("reset-password", form);
            return badRequest(showPageReset(form));
        }
        // Case invalid token
        ResetPassword resetPassword = form.get();
        Customer customer = sphere().customers().byToken(resetPassword.token).fetch().orNull();
        if (customer == null) {
            resetPassword.displayInvalidTokenError();
            return badRequest(showPageReset(form));
        }
        // Case valid reset password
        sphere().customers().resetPassword(customer.getIdAndVersion(), resetPassword.token, resetPassword.newPassword);
        resetPassword.displaySuccessMessage();
        return ok(showPage());
    }

    protected static Content showPage() {
        return showPage(false, logInForm, signUpForm, recoverPasswordForm, resetPasswordForm);
    }

    protected static Content showPageLogin(Form<LogIn> logIn) {
        return showPage(false, logIn, signUpForm, recoverPasswordForm, resetPasswordForm);
    }

    protected static Content showPageSignUp(Form<SignUp> signUp) {
        return showPage(false, logInForm, signUp, recoverPasswordForm, resetPasswordForm);
    }

    protected static Content showPageRecover(Form<RecoverPassword> recoverPassword) {
        return showPage(false, logInForm, signUpForm, recoverPassword, resetPasswordForm);
    }

    protected static Content showPageReset(Form<ResetPassword> resetPassword) {
        return showPage(true, logInForm, signUpForm, recoverPasswordForm, resetPassword);
    }

    protected static Content showPage(boolean isReset, Form<LogIn> logIn, Form<SignUp> signUp,
                                      Form<RecoverPassword> recoverPassword, Form<ResetPassword> resetPassword) {
        return login.render(isReset, logIn, signUp, recoverPassword, resetPassword);
    }
}
