package utils;

import static play.mvc.Controller.flash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.client.SphereClientException;
import io.sphere.client.model.Money;
import io.sphere.client.shop.model.*;
import play.Logger;
import play.Play;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Http;
import sphere.Sphere;

public class ControllerHelper {

    /* Method to keep a local version of the current cart throughout the current request.
     * Avoids repeated unnecessary fetching to the Sphere backend */
    public static Cart getCurrentCart() {
        Cart cart;
        try {
            Http.Context ctx = Http.Context.current();
            if (ctx.args.containsKey("currentCart")) {
                cart = (Cart)ctx.args.get("currentCart");
                if (cart != null) return cart;
            }
            cart = Sphere.getInstance().currentCart().fetch();
        } catch (SphereClientException sce) {
            Logger.error("Invalid current cart. Probably project data changed.");
            Currency currency = Currency.getInstance(
                    Play.application().configuration().getString("sphere.cart.currency"));
            cart = Sphere.getInstance().client().carts().createCart(currency).execute();
        }
        setCurrentCart(cart);
        return cart;
    }

    /* Saves a cart as the current local cart.
     * Must be called every time the cart changes, otherwise local cart will be outdated! */
    public static void setCurrentCart(Cart cart) {
        Http.Context.current().args.put("currentCart", cart);
    }

    /* Method to keep a local version of the current customer throughout the current request.
     * Avoids repeated unnecessary fetching to the Sphere backend */
    public static Customer getCurrentCustomer() {
        Customer customer = null;
        try {
            if (Sphere.getInstance().isLoggedIn()) {
                Http.Context ctx = Http.Context.current();
                if (ctx.args.containsKey("currentCustomer")) {
                    customer = (Customer)ctx.args.get("currentCustomer");
                    if (customer != null) return customer;
                }
                customer = Sphere.getInstance().currentCustomer().fetch();
            }
        } catch (SphereClientException sce) {
            customer = null;
        }
        setCurrentCustomer(customer);
        return customer;
    }

    /* Saves a customer as the current local customer.
     * Must be called every time the customer changes, otherwise local customer will be outdated! */
    public static void setCurrentCustomer(Customer customer) {
        if (customer != null && !customer.getCustomerGroup().isExpanded()) {
            customer = Sphere.getInstance().currentCustomer().fetch();
        }
        Http.Context.current().args.put("currentCustomer", customer);
    }

    /* Method in charge of displaying errors of a form, both in Flash scope and as JSON data. */
    public static <T> void displayErrors(String prefix, Form<T>form) {
        saveFlash(prefix, form);
        saveJson(form.errorsAsJson());
    }

    /* Saves a message in Flash scope. */
    public static void saveFlash(String key, String message) {
        flash(key, message);
    }

    /* Saves a any related form error in Flash scope. */
	public static <T> void saveFlash(String prefix, Form<T>form) {
		String flashName;
		for (List<ValidationError> errorList : form.errors().values()) {
			for (ValidationError error : errorList) {
				flashName = prefix + "-" + error.key() + "-error";
				if (flash().containsKey(flashName)) {
					flash(flashName, flash(flashName).concat(", " + error.message()));
				} else {
					flash(flashName, error.message());
				}
			}
		}
	}

    /* Saves some JSON data in the current request scope. */
    public static void saveJson(JsonNode json) {
        Http.Context.current().args.put("json", json);
    }

    /* Returns net price */
    public static Money getNetPrice(Money price, TaxRate taxRate) {
        if (taxRate == null) return price;
        if (!taxRate.isIncludedInPrice()) return price;
        return price.multiply(1 / (1 + taxRate.getAmount()));
    }

    /* Returns gross price */
    public static Money getGrossPrice(Money price, TaxRate taxRate) {
        if (taxRate == null) return price;
        if (taxRate.isIncludedInPrice()) return price;
        return price.plus(price.multiply(taxRate.getAmount()));
    }

    /* Returns the default category of a product. */
    public static Category getDefaultCategory(Product product) {
        if (product.getCategories().isEmpty()) return null;
        return product.getCategories().get(0);
    }

    /* Returns the address book of the current customer. */
    public static List<Address> getAddressBook() {
        if (Sphere.getInstance().isLoggedIn()) {
            return getCurrentCustomer().getAddresses();
        }
        return Collections.emptyList();
    }

    public static List<ShippingMethod> getShippingMethods() {
        List<ShippingMethod> shippingMethods = new ArrayList<ShippingMethod>();
        if (getCurrentCart().getShippingAddress() != null) {
            shippingMethods = Sphere.getInstance().shippingMethods().query().fetch().getResults();
        }
        return shippingMethods;
    }

    public static ShippingMethod getDefaultShippingMethod(List<ShippingMethod> shippingMethods) {
        // Case no shipping methods - return null
        if (shippingMethods.isEmpty()) return null;
        // Case default shipping method - return default
        for (ShippingMethod shippingMethod : shippingMethods) {
            if (shippingMethod.isDefault()) return shippingMethod;
        }
        // Case no default shipping method - return first
        return shippingMethods.get(0);
    }
}
