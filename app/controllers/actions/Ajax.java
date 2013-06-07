package controllers.actions;

import org.codehaus.jackson.node.ObjectNode;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Ajax extends Action.Simple {

    /*
    * Checks whether the current request was made with Ajax or regular form submit
    * */
    public Result call(Http.Context ctx) throws Throwable {
        // Before
        boolean isAjax = Boolean.valueOf(ctx.request().getQueryString("ajax"));

        // Call
        Result result = delegate.call(ctx);

        // After
        if (isAjax) {
            ObjectNode json = Json.newObject();
            json.put("data", Json.toJson(ctx.args.get("json")));

            String url = JavaResultExtractor.getHeaders(result).get("Location");
            if (url != null) {
                json.put("redirect", url);
                result = ok(json);
            } else {
                result = status(JavaResultExtractor.getStatus(result), json);
                ctx.flash().clear();
            }
        }
        ctx.args.remove("json");
        return result;
    }
}
