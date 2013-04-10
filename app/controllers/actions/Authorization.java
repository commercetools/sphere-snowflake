package controllers.actions;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Authorization extends Action.Simple {

    /*
    * Checks whether the current customer is authorized before performing the action
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        Http.Context.current.set(ctx);
        // TODO Customer account not implemented yet
        /*
        if (Sphere.getClient().currentCustomer() == null) {
            Form<LoginForm> loginForm = form(LoginForm.class);
            Form<SignUpForm> signUpForm = form(SignUpForm.class);
            return unauthorized(login.render(loginForm, signUpForm, ctx.session().get("returnUrl"), true));
        }
        */
        Result result = delegate.call(ctx);
        // After
        return result;
    }
}
