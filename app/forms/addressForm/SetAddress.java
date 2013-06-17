package forms.addressForm;

import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Customer;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class SetAddress extends AddAddress {

    public SetAddress() {

    }

    public SetAddress(Address address) {
        this(address, null);
    }

    public SetAddress(Address address, Customer customer) {
        if (address != null) {
            this.company = address.getCompany();
            this.firstName = address.getFirstName();
            this.lastName = address.getLastName();
            this.email = address.getEmail();
            this.phone = address.getPhone();
            this.mobile = address.getMobile();
            this.street = address.getStreetName();
            this.street2 = address.getStreetNumber();
            this.postalCode = address.getPostalCode();
            this.city = address.getCity();
            this.country = address.getCountry().getAlpha2();
        } else if (customer != null) {
            this.firstName = customer.getName().getFirstName();
            this.lastName = customer.getName().getLastName();
            this.email = customer.getEmail();
        }
    }

    public void displaySuccessMessage(Address address) {
        String message = "Address set!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(address));

        saveJson(json);
    }

}
