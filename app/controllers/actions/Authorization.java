package controllers.actions;

import forms.LogIn;
import forms.SignUp;
import play.data.Form;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import sphere.Sphere;

import static play.data.Form.form;

public class Authorization extends Action.Simple {

    /*
    * Checks whether the current customer is authorized before performing the action
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        Http.Context.current.set(ctx);
        // TODO Customer account not implemented yet

        if (Sphere.getClient().currentCustomer() == null) {
            return unauthorized(views.html.login.render(form(LogIn.class), form(SignUp.class)));
        }
        Result result = delegate.call(ctx);
        // After
        return result;
    }
}
