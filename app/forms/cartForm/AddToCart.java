package forms.cartForm;

import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

import static utils.ControllerHelper.getCurrentCart;
import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;

public class AddToCart extends ListCart {

    @Constraints.Required(message = "Product required")
    public String productId;

    @Constraints.Required(message = "Variant required")
    public int variantId;

    @Constraints.Required(message = "Quantity required")
    @Constraints.Min(1)
    @Constraints.Max(10)
    public int quantity;

    public String size;


    public AddToCart() {

    }

    public void displaySuccessMessage() {
        String message = "Item added to cart!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(getCurrentCart()));

        saveJson(json);
    }

    public void displayInvalidProductError() {
        String message = "Product not found";
        saveFlash("error", message);

        ObjectNode json = Json.newObject();
        json.put("error", message);
        saveJson(json);
    }

}
