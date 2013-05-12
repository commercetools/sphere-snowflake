package forms;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

public class RecoverPassword {

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid email address")
    public String email;

    public RecoverPassword() {

    }

    public JsonNode getJson(String url) {
        ObjectNode json = Json.newObject();
        json.put("redirectUrl", url);
        return json;
    }

}
