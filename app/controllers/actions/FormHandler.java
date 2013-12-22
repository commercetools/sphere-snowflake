package controllers.actions;

import org.codehaus.jackson.node.ObjectNode;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class FormHandler extends Action.Simple {

    /*
    * Checks whether the current request was made with Ajax or regular form submit
    * */

    public Result call(Http.Context ctx) throws Throwable {
        // Before, detect submission type
        boolean isAjax = Boolean.valueOf(ctx.request().getQueryString("ajax"));
        // Call action, always returns HTML file or URL redirection
        Result result = delegate.call(ctx);
        // After, return:
        // - JSON data when AJAX and no redirection
        // - JSON with URL when AJAX and redirection
        // - Result from action otherwise
        if (isAjax) {
            // Extract saved JSON data
            ObjectNode json = Json.newObject();
            json.put("data", Json.toJson(ctx.args.get("json")));
            // Extract URL location header
            String url = JavaResultExtractor.getHeaders(result).get("Location");
            if (url != null) {
                // AJAX and redirection
                json.put("redirect", url);
                result = ok(json);
            } else {
                // AJAX and no redirection
                result = status(JavaResultExtractor.getStatus(result), json);
                ctx.flash().clear();
            }
        }
        ctx.args.remove("json");
        return result;
    }
}
