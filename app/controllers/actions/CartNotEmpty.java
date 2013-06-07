package controllers.actions;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import sphere.Sphere;

public class CartNotEmpty extends Action.Simple {

    /*
    * Checks whether the current cart is empty
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        if (Sphere.getInstance().currentCart().getQuantity() < 1) {
            return redirect(ctx.session().get("returnUrl"));
        }

        // Call
        return delegate.call(ctx);
    }
}
