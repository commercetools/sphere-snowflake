package forms.passwordForm;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

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

    public void displaySuccessMessage() {
        String message = "Password successfully changed!";
        saveFlash("update-password-success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        saveJson(json);
    }

    public void displayInvalidPasswordError() {
        String message = "Current password does not match our records";
        saveFlash("update-password-oldPassword-error", message);

        ObjectNode json = Json.newObject();
        json.put("oldPassword", message);
        saveJson(json);
    }

}
