package forms.addressForm;

import forms.cartForm.ListCart;
import forms.checkoutForm.DoCheckout;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.ShippingMethod;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;
import sphere.Sphere;

import static utils.ControllerHelper.*;

public class SetAddress extends AddAddress {

    public SetAddress() {

    }

    public SetAddress(Address address) {
        Customer customer = getCurrentCustomer();
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

    public void displaySuccessMessage() {
        String message = "Address set!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(DoCheckout.getJson());

        saveJson(json);
    }

}
