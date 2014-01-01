package controllers.actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import sphere.Sphere;

public class Authorization extends Action.Simple {

    /*
    * Checks whether the current customer is authorized before performing the action
    * */
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        // Before
        Http.Context.current.set(ctx);
        if (!Sphere.getInstance().isLoggedIn()) {
            ctx.flash().put("error", "You need to log in to view this section");
            return F.Promise.pure(redirect(controllers.routes.Login.show()));
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
