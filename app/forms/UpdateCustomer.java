package forms;

import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerName;
import play.data.validation.Constraints;

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
}
