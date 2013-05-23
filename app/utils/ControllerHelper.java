package utils;

import static play.mvc.Controller.flash;

import java.util.List;

import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.codehaus.jackson.JsonNode;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Http;

public class ControllerHelper {

    public static <T> void displayErrors(String prefix, Form<T>form) {
        saveFlash(prefix, form);
        saveJson(form.errorsAsJson());
    }

    public static void saveFlash(String key, String message) {
        flash(key, message);
    }

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

    public static void saveJson(JsonNode json) {
        Http.Context.current().args.put("json", json);
    }

    public static Category getDefaultCategory(Product product) {
        if (product.getCategories().isEmpty()) return null;
        return product.getCategories().get(0);
    }
}
