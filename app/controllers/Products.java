package controllers;

import controllers.actions.SaveContext;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import io.sphere.client.shop.model.Variant;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

import static utils.ControllerHelper.getDefaultCategory;

@With(SaveContext.class)
public class Products extends ShopController {

    public static Result select(String productSlug, String variantId) {
        Product product = sphere().products.bySlug(productSlug).fetch().orNull();
        if (product == null) {
            return notFound("Product not found: " + productSlug);
        }
        Variant variant = product.getVariants().byId(variantId).or(product.getMasterVariant());
        Category category = getDefaultCategory(product);
        return ok(views.html.products.render(product, variant, category));
    }
}
