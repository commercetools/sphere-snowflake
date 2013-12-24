package forms.checkoutForm;

import forms.addressForm.SetAddress;
import forms.cartForm.ListCart;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;
import sphere.Sphere;

import static utils.ControllerHelper.*;

public class DoCheckout {

    @Constraints.Required(message = "Missing cart snapshot")
    public String cartSnapshot;

    @Constraints.Required(message = "Missing payment token")
    public String paymillToken;

    public DoCheckout() {

    }

    public static ObjectNode getJson() {
        ObjectNode json = Json.newObject();
        json.put("cartSnapshot", Sphere.getInstance().currentCart().createCartSnapshotId());
        json.put("cart", ListCart.getJson(getCurrentCart()));
        json.put("shippingAddress", SetAddress.getJson(getCurrentCart().getShippingAddress()));
        json.put("shippingMethod", SetShippingMethod.getJson(getShippingMethods()));
        return json;
    }

    public void displayCartChangedError() {
        String message = "Your cart has changed. Please review everything is still correct.";
        saveFlash("info", message);

        ObjectNode json = Json.newObject();
        json.put("info", message);
        saveJson(json);
    }

}
