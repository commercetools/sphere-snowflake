package forms.passwordForm;

import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class RecoverPassword {

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid email address")
    public String email;

    public RecoverPassword() {

    }

    public void displaySuccessMessage() {
        String message = "Email has been sent to this email account with further instructions";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        saveJson(json);
    }

    public void displayInvalidEmailError() {
        String message = "Email not registered";
        saveFlash("recover-password-email-error", message);

        ObjectNode json = Json.newObject();
        json.put("email", message);
        saveJson(json);
    }

}
