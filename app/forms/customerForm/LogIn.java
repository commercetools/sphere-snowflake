package forms.customerForm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.getCurrentCustomer;
import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class LogIn {

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid value for email")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;

    public LogIn() {

    }

    public void displaySuccessMessage() {
        String message = "Welcome back "+ getCurrentCustomer().getName().getFirstName() +"!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        saveJson(json);
    }

    public void displayInvalidCredentialsError() {
        String message = "Invalid credentials";
        saveFlash("log-in-error", message);

        ObjectNode json = Json.newObject();
        json.put("error", message);
        json.put("email", "");
        json.put("password", "");
        saveJson(json);
    }

}
