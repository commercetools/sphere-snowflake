package forms.cartForm;

import io.sphere.client.shop.model.*;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import play.libs.Json;

import java.math.BigDecimal;

public class ListCart {

    public ListCart() {

    }

    public static ObjectNode getJson(Cart cart) {
        ObjectNode json = Json.newObject();
        if (cart.getShippingAddress() != null) {
            json.put("totalPrice", cart.getTaxedPrice().getTotalGross().toString());
            json.put("totalNetPrice", cart.getTaxedPrice().getTotalNet().toString());
            // TODO Use SDK shipping logic
            json.put("shippingPrice", "10 EUR");
            ArrayNode taxPortions = json.putArray("taxPortion");
            for (TaxPortion tax: cart.getTaxedPrice().getTaxPortions()) {
                ObjectNode taxPortion = Json.newObject();
                taxPortion.put("rate", String.valueOf(BigDecimal.valueOf(tax.getRate() * 100).stripTrailingZeros()));
                taxPortion.put("amount", tax.getAmount().toString());
                taxPortions.add(taxPortion);
            }
        } else {
            json.put("totalPrice", cart.getTotalPrice().toString());
        }
        ArrayNode list = json.putArray("item");
        for (LineItem item : cart.getLineItems()) {
            list.add(getJson(item));
        }
        return json;
    }

    public static ObjectNode getJson(LineItem item) {
        ObjectNode json = Json.newObject();
        json.put("itemId", item.getId());
        json.put("productId", item.getProductId());
        json.put("productName", item.getProductName());
        json.put("quantity", item.getQuantity());
        json.put("price", item.getPrice().getValue().toString());
        json.put("totalPrice", item.getTotalPrice().toString());

        ArrayNode attributes = json.putArray("attribute");
        for (Attribute attr : item.getVariant().getAttributes()) {
            ObjectNode attribute = Json.newObject();
            attribute.put("name", attr.getName());
            attribute.put("value", attr.getValue().toString());
            attributes.add(attribute);
        }

        ObjectNode images = Json.newObject();
        images.put("thumbnail", item.getVariant().getFeaturedImage().getSize(ImageSize.THUMBNAIL).getUrl());
        images.put("small", item.getVariant().getFeaturedImage().getSize(ImageSize.SMALL).getUrl());
        images.put("medium", item.getVariant().getFeaturedImage().getSize(ImageSize.MEDIUM).getUrl());
        images.put("large", item.getVariant().getFeaturedImage().getSize(ImageSize.LARGE).getUrl());
        images.put("original", item.getVariant().getFeaturedImage().getSize(ImageSize.ORIGINAL).getUrl());
        json.put("image", images);

        return json;
    }
}
