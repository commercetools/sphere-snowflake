package forms;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

public class ResetPassword {

    @Constraints.Required(message = "New password required")
    public String newPassword;

    @Constraints.Required(message = "Repeat password required")
    public String repeatPassword;


    public ResetPassword() {

    }

    public String validate() {
        if (!newPassword.equals(repeatPassword)) {
            return "New passwords do not match";
        }
        return null;
    }

    public JsonNode getJson() {
        ObjectNode json = Json.newObject();
        return json;
    }

}
