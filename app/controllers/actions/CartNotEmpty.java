package controllers.actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;
import sphere.Sphere;

public class CartNotEmpty extends Action.Simple {

    /*
    * Checks whether the current cart is empty
    * */
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        // Before
        if (Sphere.getInstance().currentCart().getQuantity() < 1) {
            return F.Promise.pure(redirect(ctx.session().get("returnUrl")));
        }

        // Call
        return delegate.call(ctx);
    }
}
