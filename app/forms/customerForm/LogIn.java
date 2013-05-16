package forms.customerForm;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;

public class LogIn {

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid value for email")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;


    public LogIn() {

    }

    public JsonNode getJson(Call call) {
        ObjectNode json = Json.newObject();
        json.put("redirectUrl", call.absoluteURL(Http.Context.current().request()));
        return json;
    }

    public JsonNode getJsonCredentialsMatchError() {
        ObjectNode json = Json.newObject();
        json.put("global", "Invalid credentials");
        json.put("email", "");
        json.put("password", "");
        return json;
    }
}
