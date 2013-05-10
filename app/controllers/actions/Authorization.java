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
        if (Sphere.getClient().currentCustomer() == null) {
            return Login.show();
        }
        Result result = delegate.call(ctx);
        // After
        return result;
    }
}
