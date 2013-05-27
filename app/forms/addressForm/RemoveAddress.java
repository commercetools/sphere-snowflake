package forms.addressForm;

import io.sphere.client.shop.model.Address;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.List;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class RemoveAddress extends ListAddress {

    @Constraints.Required(message = "Invalid address")
    public String addressId;

    public RemoveAddress() {

    }

    public void displaySuccessMessage(List<Address> addresses) {
        String message = "Address removed!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(addresses));

        saveJson(json);
    }

}
