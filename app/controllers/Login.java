package controllers;

import forms.LogIn;
import forms.SignUp;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;

import static play.data.Form.form;
import static utils.ControllerHelper.generateErrorMessages;

public class Login extends ShopController {

    public static Result show() {
        return ok(views.html.login.render(form(LogIn.class), form(SignUp.class)));
    }

    public static Result signUp() {
        Form<SignUp> form = form(SignUp.class).bindFromRequest();
        if (form.hasErrors()) {
            generateErrorMessages(form, "sign-up");
            return badRequest(views.html.login.render(form(LogIn.class), form));
        }
        SignUp signUp = form.get();
        sphere().signup(signUp.email, signUp.password, signUp.getCustomerName());
        return ok();
    }

    public static Result logIn() {
        Form<LogIn> form = form(LogIn.class).bindFromRequest();
        if (form.hasErrors()) {
            generateErrorMessages(form, "log-in");
            return badRequest(views.html.login.render(form, form(SignUp.class)));
        }
        LogIn logIn = form.get();
        if (!sphere().login(logIn.email, logIn.password)) {
            flash("log-in-error", "Invalid credentials");
            return badRequest(views.html.login.render(form, form(SignUp.class)));
        }
        return ok();
    }

}
