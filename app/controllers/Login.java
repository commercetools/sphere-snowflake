package controllers;

import forms.LogIn;
import forms.SignUp;
import org.codehaus.jackson.node.ObjectNode;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http;
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

}
