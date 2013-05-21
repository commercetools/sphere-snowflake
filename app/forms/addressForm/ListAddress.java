package forms.addressForm;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.shop.model.Address;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class ListAddress {

    public ListAddress() {

    }

    public static ObjectNode getJson(List<Address> addresses) {
        ObjectNode json = Json.newObject();
        ArrayNode list = json.putArray("address");
        for (Address address : addresses) {
            list.add(getJson(address));
        }
        return json;
    }

    public static ObjectNode getJson(Address address) {
        ObjectNode json = Json.newObject();
        json.put("addressId", address.getId());
        json.put("company", address.getCompany());
        json.put("firstName", address.getFirstName());
        json.put("lastName", address.getLastName());
        json.put("email", address.getEmail());
        json.put("phone", address.getPhone());
        json.put("mobile", address.getMobile());
        json.put("street", address.getStreetName());
        json.put("street2", address.getStreetNumber());
        json.put("postalCode", address.getPostalCode());
        json.put("city", address.getCity());
        json.put("country", address.getCountry().getAlpha2());
        json.put("countryName", address.getCountry().getName());
        return json;
    }

    public static List<CountryCode> getCountryCodes() {
        List<CountryCode> countries = new ArrayList<CountryCode>();
        countries.add(CountryCode.DE);
        countries.add(CountryCode.AT);
        return countries;
    }
}
