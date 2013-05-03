package forms;

import controllers.routes;
import io.sphere.client.shop.model.CustomerName;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;

public class SignUp {

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid value for email")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;


    public SignUp() {

    }

    public CustomerName getCustomerName() {
        return new CustomerName(this.firstName, this.lastName);
    }

    public JsonNode getJson(Call call) {
        ObjectNode json = Json.newObject();
        json.put("redirectUrl", call.absoluteURL(Http.Context.current().request()));
        return json;
    }
}
