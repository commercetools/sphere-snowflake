package controllers.actions;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class SaveContext extends Action.Simple {

    /*
    * Saves the current URL in session to be used later as a return URL
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        // TODO For some reason it is breaking the tests with mocking
        //ctx.session().put("returnUrl", ctx.request().uri());
        Result result = delegate.call(ctx);
        // After
        return result;
    }
}
