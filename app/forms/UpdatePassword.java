package forms;

import io.sphere.client.shop.model.Customer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

public class UpdatePassword {

    @Constraints.Required(message = "Old password required")
    public String oldPassword;

    @Constraints.Required(message = "New password required")
    public String newPassword;

    @Constraints.Required(message = "Repeat password required")
    public String repeatPassword;


    public UpdatePassword() {

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

    public JsonNode getJsonPasswordMatchError() {
        ObjectNode json = Json.newObject();
        json.put("oldPassword", "Current password does not match our records");
        return json;
    }
}
