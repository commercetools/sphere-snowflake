package forms.cartForm;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import play.libs.Json;

import java.math.BigDecimal;

import static utils.ViewHelper.*;

public class ListCart {

    public ListCart() {

    }

    public static ObjectNode getJson(String snapshot) {
        ObjectNode json = Json.newObject();
        json.put("snapshot", snapshot);
        return json;
    }

    public static ObjectNode getJson(Cart cart) {
        ObjectNode json = Json.newObject();
        if (cart.getTotalQuantity() < 1) return json;
        // Total price
        json.put("totalPrice", printPriceAmount(getPrice(cart)));
        json.put("currency", printPriceCurrency(cart.getCurrency().getCurrencyCode()));
        json.put("currencyCode", cart.getCurrency().getCurrencyCode());
        if (cart.getShippingAddress() != null) {
            // Shipping price
            if (cart.getShippingInfo() != null) {
                ShippingInfo shipping = cart.getShippingInfo();
                json.put("shippingPrice", printPriceAmount(getPrice(shipping)));
            }
            // Tax portions
            ArrayNode taxPortions = json.putArray("taxPortion");
            for (TaxPortion tax: cart.getTaxedPrice().getTaxPortions()) {
                ObjectNode taxPortion = Json.newObject();
                taxPortion.put("included", true);
                taxPortion.put("rate", String.valueOf(getPercentage(tax.getRate())));
                taxPortion.put("amount", printPriceAmount(tax.getAmount()));
                taxPortion.put("currency", printPriceCurrency(tax.getAmount().getCurrencyCode()));
                taxPortions.add(taxPortion);
            }
        }
        // Total items price
        ArrayNode list = json.putArray("item");
        Money totalItemPrice = new Money(BigDecimal.ZERO, cart.getCurrency().getCurrencyCode());
        for (LineItem item : cart.getLineItems()) {
            list.add(getJson(item));
            totalItemPrice = totalItemPrice.plus(getPrice(item));
        }
        // Total
        json.put("totalItemPrice", printPriceAmount(totalItemPrice));
        // Custom line items
        if(cart.getCustomLineItems().size() > 0) {
            ArrayNode customLineItemList = json.putArray("customLineItems");
            for (CustomLineItem item : cart.getCustomLineItems()) {
                customLineItemList.add(getJson(item));
            }
        }
        return json;
    }

    public static ObjectNode getJson(LineItem item) {
        ObjectNode json = Json.newObject();
        json.put("itemId", item.getId());
        json.put("productId", item.getProductId());
        json.put("productName", item.getProductName());
        json.put("variantId", item.getVariant().getId());
        json.put("quantity", item.getQuantity());
        json.put("currency", printPriceCurrency(item.getTotalPrice().getCurrencyCode()));
        json.put("price", printPriceAmount(getPrice(item.getPrice().getValue(), item.getTaxRate())));
        json.put("totalPrice", printPriceAmount(getPrice(item)));
        // Attributes
        ArrayNode attributes = json.putArray("attribute");
        for (Attribute attr : item.getVariant().getAttributes()) {
            ObjectNode attribute = Json.newObject();
            attribute.put("name", attr.getName());
            attribute.put("value", attr.getValue().toString());
            attributes.add(attribute);
        }
        // Images
        ObjectNode images = Json.newObject();
        images.put("thumbnail", item.getVariant().getFeaturedImage().getSize(ImageSize.THUMBNAIL).getUrl());
        images.put("small", item.getVariant().getFeaturedImage().getSize(ImageSize.SMALL).getUrl());
        images.put("medium", item.getVariant().getFeaturedImage().getSize(ImageSize.MEDIUM).getUrl());
        images.put("large", item.getVariant().getFeaturedImage().getSize(ImageSize.LARGE).getUrl());
        images.put("original", item.getVariant().getFeaturedImage().getSize(ImageSize.ORIGINAL).getUrl());
        json.put("image", images);
        return json;
    }

    public static ObjectNode getJson(CustomLineItem item) {
        ObjectNode json = Json.newObject();
        json.put("itemId", item.getId());
        json.put("name", item.getName().get());
        json.put("money", printPriceAmount(getPrice(item)));
        json.put("currency", printPriceCurrency(item.getMoney().getCurrencyCode()));
        json.put("slug", item.getSlug());
        json.put("quantity", item.getQuantity());
        json.put("taxCategory", item.getTaxCategory().getTypeId());
        return json;
    }
}
