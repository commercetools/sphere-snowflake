package forms.customerForm;

import io.sphere.client.shop.model.CustomerName;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class SignUp {

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid email address")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;


    public SignUp() {

    }

    public CustomerName getCustomerName() {
        return new CustomerName(this.firstName, this.lastName);
    }

    public void displaySuccessMessage() {
        String message = "We are glad you joined us "+ this.firstName +"!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        saveJson(json);
    }

    public void displayAlreadyRegisteredError() {
        String message = "Email already in use";
        saveFlash("sign-up-email-error", message);

        ObjectNode json = Json.newObject();
        json.put("email", message);
        saveJson(json);
    }

}
