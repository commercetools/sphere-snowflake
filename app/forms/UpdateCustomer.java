package forms;

import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerName;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

public class UpdateCustomer {

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid value for email")
    public String email;


    public UpdateCustomer() {

    }

    public UpdateCustomer(Customer customer) {
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
    }

    public CustomerName getCustomerName() {
        return new CustomerName(this.firstName, this.lastName);
    }

    public JsonNode getJson(Customer customer) {
        ObjectNode json = Json.newObject();
        json.put("firstName", customer.getFirstName());
        json.put("lastName", customer.getLastName());
        json.put("email", customer.getEmail());
        return json;
    }
}
