package forms.cartForm;

import io.sphere.client.shop.model.Attribute;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.LineItem;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import play.data.validation.Constraints;
import play.libs.Json;

public class AddToCart {

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

    public JsonNode getJson(Cart cart) {
        ObjectNode json = Json.newObject();
        json.put("cart-totalPrice", cart.getTotalPrice().toString());
        for (LineItem item : cart.getLineItems()) {
            String itemId = "-" + item.getId();
            json.put(itemId, item.getId());
            json.put("lineItem-productId" + itemId, item.getProductId());
            json.put("lineItem-productName" + itemId, item.getProductName());
            for (Attribute attr : item.getVariant().getAttributes()) {
                json.put("lineItem-attribute-" + attr.getName() + itemId, attr.getValue().toString());
            }
            json.put("lineItem-quantity" + itemId, item.getQuantity());
            json.put("lineItem-price" + itemId, item.getPrice().getValue().toString());
            json.put("lineItem-totalPrice" + itemId, item.getTotalPrice().toString());
        }
        return json;
    }
}
