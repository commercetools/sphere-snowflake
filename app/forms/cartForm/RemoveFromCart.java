package forms.cartForm;

import io.sphere.client.shop.model.Cart;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.getCurrentCart;
import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class RemoveFromCart extends ListCart {

    @Constraints.Required(message = "Line item required")
    public String lineItemId;


    public RemoveFromCart() {

    }

    public void displaySuccessMessage() {
        String message = "Item removed from cart!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(getCurrentCart()));

        saveJson(json);
    }

}
