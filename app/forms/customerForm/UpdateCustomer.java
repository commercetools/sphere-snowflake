package forms.customerForm;

import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerName;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.getCurrentCustomer;
import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

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
        this.firstName = customer.getName().getFirstName();
        this.lastName = customer.getName().getLastName();
        this.email = customer.getEmail();
    }

    public CustomerName getCustomerName() {
        return new CustomerName(this.firstName, this.lastName);
    }

    public void displaySuccessMessage() {
        String message = "Your information is updated!";
        saveFlash("update-customer-success", message);

        Customer customer = getCurrentCustomer();
        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.put("customer-firstName", customer.getName().getFirstName());
        json.put("customer-lastName", customer.getName().getLastName());
        json.put("customer-email", customer.getEmail());
        saveJson(json);
    }

}
