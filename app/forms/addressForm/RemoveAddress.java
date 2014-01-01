package forms.addressForm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.getAddressBook;
import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class RemoveAddress extends ListAddress {

    @Constraints.Required(message = "Invalid address")
    public String addressId;

    public RemoveAddress() {

    }

    public void displaySuccessMessage() {
        String message = "Address removed!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(getAddressBook()));

        saveJson(json);
    }

}
