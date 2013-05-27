package controllers;

import controllers.actions.Ajax;
import forms.cartForm.ListCart;
import io.sphere.client.shop.model.*;
import forms.cartForm.AddToCart;
import forms.cartForm.RemoveFromCart;
import forms.cartForm.UpdateCart;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.carts;
import views.html.products;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;
import static utils.ControllerHelper.getDefaultCategory;

public class Carts extends ShopController {

    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        return ok(carts.render(cart));
    }

    public static Result get() {
        Cart cart = sphere().currentCart().fetch();
        return ok(ListCart.getJson(cart));
    }

    @With(Ajax.class)
    public static Result add() {
        Form<AddToCart> form = form(AddToCart.class).bindFromRequest();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("add-to-cart", form);
            return badRequest(); // TODO Decide where to return to
        }
        // Case invalid product
        AddToCart addToCart = form.get();
        Product product = sphere().products.byId(addToCart.productId).fetch().orNull();
        if (product == null) {
            return notFound("Product not found");
        }
        // Case invalid variant
        Variant variant = product.getVariants().byId(addToCart.variantId).orNull();
        if (variant == null) {
            return notFound("Product variant not found");
        }
        // Case valid product to add to cart
        int variantId = getMatchedSizeVariant(product, variant, addToCart.size);
        Cart cart = sphere().currentCart().addLineItem(addToCart.productId, variantId, addToCart.quantity);
        addToCart.displaySuccessMessage(cart);
        return ok(products.render(product, variant, getDefaultCategory(product)));
    }

    @With(Ajax.class)
    public static Result update() {
        Form<UpdateCart> form = form(UpdateCart.class).bindFromRequest();
        Cart cart = sphere().currentCart().fetch();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-cart", form);
            return badRequest(carts.render(cart));
        }
        // Case valid cart update
        UpdateCart updateCart = form.get();
        CartUpdate cartUpdate = new CartUpdate()
                .setLineItemQuantity(updateCart.lineItemId, updateCart.quantity);
        cart = sphere().currentCart().update(cartUpdate);
        updateCart.displaySuccessMessage(cart);
        return ok(carts.render(cart));
    }

    @With(Ajax.class)
    public static Result remove() {
        Form<RemoveFromCart> form = form(RemoveFromCart.class).bindFromRequest();
        Cart cart = sphere().currentCart().fetch();
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("remove-from-cart", form);
            return badRequest(carts.render(cart));
        }
        // Case valid cart update
        RemoveFromCart removeFromCart = form.get();
        cart = sphere().currentCart().removeLineItem(removeFromCart.lineItemId);
        removeFromCart.displaySuccessMessage(cart);
        return ok(carts.render(cart));
    }

    protected static int getMatchedSizeVariant(Product product, Variant variant, String size) {
        // When size not defined return selected variant ID
        if (size == null) return variant.getId();
        // Otherwise fetch all variants
        VariantList variants = product.getVariants();
        // Filter them by selected color, if any
        if (variant.hasAttribute("color")) {
            variants = variants.byAttributes(variant.getAttribute("color"));
        }
        // And filter them by selected size, return matching variant ID
        Attribute sizeAttr = new Attribute("size", size);
        return variants.byAttributes(sizeAttr).first().or(variant).getId();
    }

}
