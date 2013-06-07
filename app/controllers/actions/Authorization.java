package controllers.actions;

import controllers.Login;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import sphere.Sphere;

public class Authorization extends Action.Simple {

    /*
    * Checks whether the current customer is authorized before performing the action
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        Http.Context.current.set(ctx);
        if (!Sphere.getInstance().isLoggedIn()) {
            ctx.flash().put("error", "You need to log in to view this section");
            return Login.show();
        }
/*        if (!Sphere.getInstance().currentCustomer().fetch().isEmailVerified()) {
            ctx.flash().put("error", "Your account is not activated");
            return Login.show();
        }
  */
        // Call
        return delegate.call(ctx);
    }
}
