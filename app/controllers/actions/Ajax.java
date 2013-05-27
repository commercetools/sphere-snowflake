package controllers.actions;

import org.codehaus.jackson.JsonNode;
import play.api.mvc.PlainResult;
import play.api.mvc.ResponseHeader;
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
            if (result.getWrappedResult() instanceof PlainResult) {
                JsonNode json = Json.toJson(ctx.args.get("json"));
                if (json == null) {
                    json = Json.newObject();
                }
                ResponseHeader header = ((PlainResult)result.getWrappedResult()).header();
                result = status(header.status(), json);
                ctx.flash().clear();
            } else {
                // Manually send temporarily redirection otherwise jQuery may detect it as a non-JSON 200 response
                result = status(303);
            }
        }
        ctx.args.remove("json");
        return result;
    }
}
