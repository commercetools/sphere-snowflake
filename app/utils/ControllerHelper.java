package utils;

import static play.mvc.Controller.flash;
import java.util.List;

import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import play.data.Form;
import play.data.validation.ValidationError;

public class ControllerHelper {

	public static <T> void generateErrorMessages(Form<T> form, String prefix) {
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

    public static Category getDefaultCategory(Product product) {
        if (product.getCategories().isEmpty()) return null;
        return product.getCategories().get(0);
    }
}
