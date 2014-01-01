package forms.passwordForm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class ResetPassword {

    @Constraints.Required(message = "Missing information")
    public String token;

    @Constraints.Required(message = "New password required")
    public String newPassword;

    @Constraints.Required(message = "Repeat password required")
    public String repeatPassword;


    public ResetPassword() {

    }

    public ResetPassword(String token) {
        this.token = token;
    }

    public String validate() {
        if (!newPassword.equals(repeatPassword)) {
            return "New passwords do not match";
        }
        return null;
    }

    public void displaySuccessMessage() {
        String message = "Password successfully reset, please try to log in again";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        saveJson(json);
    }

    public void displayInvalidTokenError() {
        String message = "Either you followed an invalid link or your request expired";
        saveFlash("error", message);

        ObjectNode json = Json.newObject();
        json.put("error", message);
        saveJson(json);
    }

}