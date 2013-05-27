package forms.cartForm;

import io.sphere.client.shop.model.Cart;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class UpdateCart extends ListCart {

    @Constraints.Required(message = "Line item required")
    public String lineItemId;

    @Constraints.Required(message = "Quantity required")
    @Constraints.Min(1)
    @Constraints.Max(10)
    public int quantity;


    public UpdateCart() {

    }

    public void displaySuccessMessage(Cart cart) {
        String message = "Item updated!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(cart));

        saveJson(json);
    }

}
