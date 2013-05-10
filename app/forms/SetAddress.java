package forms;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Customer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class SetAddress {

    public String company;

    @Constraints.Required(message = "First name required")
    public String firstName;

    @Constraints.Required(message = "Last name required")
    public String lastName;

    @Constraints.Email(message = "Invalid value for email")
    public String email;

    @Constraints.Required(message = "Phone required")
    public String phone;

    public String mobile;

    @Constraints.Required(message = "Street address required")
    public String street;

    public String street2;

    @Constraints.Required(message = "Postal code required")
    public String postalCode;

    @Constraints.Required(message = "City required")
    public String city;

    @Constraints.Required(message = "Country required")
    @Constraints.Pattern(value = "DE|AT", message = "Invalid value for country")
    public String country;

    public SetAddress() {

    }

    public SetAddress(Address address) {
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
        }
    }

    public SetAddress(Customer customer) {
        if (customer != null) {
            this.firstName = customer.getFirstName();
            this.lastName = customer.getLastName();
            this.email = customer.getEmail();
        }
    }

    public JsonNode getJson(Address address) {
        ObjectNode json = Json.newObject();
        json.put("address-company", address.getCompany());
        json.put("address-firstName", address.getFirstName());
        json.put("address-lastName", address.getLastName());
        json.put("address-email", address.getEmail());
        json.put("address-phone", address.getPhone());
        json.put("address-mobile", address.getMobile());
        json.put("address-street", address.getStreetName());
        json.put("address-street2", address.getStreetNumber());
        json.put("address-postalCode", address.getPostalCode());
        json.put("address-city", address.getCity());
        json.put("address-country", address.getCountry().getAlpha2());
        return json;
    }

    public Address getAddress() {
        Address address = new Address(getCountryCode());
        address.setCompany(company);
        address.setFirstName(firstName);
        address.setLastName(lastName);
        address.setEmail(email);
        address.setPhone(phone);
        address.setMobile(mobile);
        address.setStreetName(street);
        address.setStreetNumber(street2);
        address.setPostalCode(postalCode);
        address.setCity(city);
        return address;
    }

    public CountryCode getCountryCode() {
        return CountryCode.getByCode(this.country);
    }

    public static List<CountryCode> getCountryCodes() {
        List<CountryCode> countries = new ArrayList<CountryCode>();
        countries.add(CountryCode.DE);
        countries.add(CountryCode.AT);
        return countries;
    }
}
