package forms.checkoutForm;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sphere.client.model.Money;
import io.sphere.client.model.ReferenceId;
import io.sphere.client.shop.model.*;
import play.data.validation.Constraints;
import play.libs.Json;

import java.math.BigDecimal;
import java.util.List;

import static utils.ControllerHelper.*;
import static utils.ViewHelper.printPrice;
import static utils.ViewHelper.printPriceAmount;
import static utils.ViewHelper.printPriceCurrency;

public class SetShippingMethod {

    @Constraints.Required(message = "Missing shipping method")
    public String method;
    public String name;
    public String price;


    public SetShippingMethod() {

    }

    public SetShippingMethod(ShippingInfo shippingInfo) {
        if (shippingInfo != null) {
            this.method = shippingInfo.getShippingMethod().getId();
            this.name = shippingInfo.getShippingMethodName();
            this.price = printPrice(shippingInfo.getPrice());
        }
    }

    public ReferenceId<ShippingMethod> getShippingMethod() {
        return ShippingMethod.reference(method);
    }

    public void displaySuccessMessage() {
        String message = "Shipping data set!";
        saveFlash("success", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(DoCheckout.getJson());

        saveJson(json);
    }

    public static ObjectNode getJson(List<ShippingMethod> shippingMethods) {
        ObjectNode json = Json.newObject();
        if (getCurrentCart().getShippingAddress() == null) return json;
        ArrayNode list = json.putArray("method");
        for (ShippingMethod shippingMethod : shippingMethods) {
            ObjectNode jsonShipping = getJson(shippingMethod);
            if (jsonShipping != null) list.add(jsonShipping);
        }
        return json;
    }

    public static ObjectNode getJson(ShippingMethod shippingMethod) {
        Cart cart = getCurrentCart();
        if (shippingMethod == null) return null;
        if (cart.getShippingAddress() == null) return null;
        boolean select = false;
        if (cart.getShippingInfo() != null) {
            select = cart.getShippingInfo().getShippingMethod().getId().equals(shippingMethod.getId());
        }
        Location location = Location.of(cart.getShippingAddress());
        ShippingRate rate = shippingMethod.shippingRateForLocation(location, cart.getCurrency());
        if (rate == null) return null;
        Money price = rate.getPrice();
        if (rate.getFreeAbove() != null) {
            if (cart.getTotalPrice().getAmount().compareTo(rate.getFreeAbove().getAmount()) > 0) {
                price = new Money(BigDecimal.ZERO, cart.getCurrency().getCurrencyCode());
            }
        }
        ObjectNode json = Json.newObject();
        json.put("id", shippingMethod.getId());
        json.put("name", shippingMethod.getName());
        json.put("description", shippingMethod.getDescription());
        json.put("price", printPriceAmount(price));
        json.put("currency", printPriceCurrency(cart.getCurrency().getCurrencyCode()));
        json.put("select", select);
        System.out.println("not empty " + json.toString());
        return json;
    }

}