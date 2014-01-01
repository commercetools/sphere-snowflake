package controllers.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;
import scala.Option;

public class FormHandler extends Action.Simple {

    /*
    * Checks whether the current request was made with Ajax or regular form submit
    * */

    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        // Before, detect submission type
        boolean isAjax = Boolean.valueOf(ctx.request().getQueryString("ajax"));
        // Call action, always returns HTML file or URL redirection
        F.Promise<SimpleResult> result = delegate.call(ctx);
        // After, return:
        // - JSON data when AJAX and no redirection
        // - JSON with URL when AJAX and redirection
        // - Result from action otherwise
        if (isAjax) {
            return result.map( new F.Function<SimpleResult, SimpleResult>() {
                public SimpleResult apply(SimpleResult result) {
                    // Extract saved JSON data
                    ObjectNode json = Json.newObject();
                    json.put("data", Json.toJson(Http.Context.current().args.get("json")));
                    // Extract URL location header
                    Option<String> url = result.getWrappedSimpleResult().header().headers().get("Location");
                    if (url.isEmpty()) {
                        // AJAX and no redirection
                        Http.Context.current().flash().clear();
                        return status(result.getWrappedSimpleResult().header().status(), json);
                    } else {
                        // AJAX and redirection
                        json.put("redirect", url.get());
                        return ok(json);
                    }
                }
            });
        }
        ctx.args.remove("json");
        return result;
    }
}
