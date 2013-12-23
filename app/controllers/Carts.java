package controllers;

import controllers.actions.FormHandler;
import controllers.actions.CartNotEmpty;
import forms.cartForm.ListCart;
import io.sphere.client.shop.model.*;
import forms.cartForm.AddToCart;
import forms.cartForm.RemoveFromCart;
import forms.cartForm.UpdateCart;
import play.data.Form;
import play.mvc.Content;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.carts;
import views.html.products;

import static play.data.Form.form;
import static utils.ControllerHelper.*;

public class Carts extends ShopController {

    final static Form<AddToCart> addToCartForm = form(AddToCart.class);
    final static Form<UpdateCart> updateCartForm = form(UpdateCart.class);
    final static Form<RemoveFromCart> removeFromCartForm = form(RemoveFromCart.class);

    public static Result get() {
        return ok(ListCart.getJson(getCurrentCart()));
    }

    @With(CartNotEmpty.class)
    public static Result show() {
        return ok(carts.render(getCurrentCart()));
    }

    @With(FormHandler.class)
    public static Result add() {
        // Case missing or invalid form data, display errors
        Form<AddToCart> form = addToCartForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("add-to-cart", form);
            return redirect(routes.Categories.home(1));
        }
        // Case invalid product, return not found
        AddToCart addToCart = form.get();
        Product product = sphere().products().byId(addToCart.productId).fetch().orNull();
        if (product == null) {
            addToCart.displayInvalidProductError();
            return notFound();
        }
        // Case invalid variant, return not found
        Variant variant = product.getVariants().byId(addToCart.variantId).orNull();
        if (variant == null) {
            addToCart.displayInvalidProductError();
            return notFound();
        }
        // Case valid, add product to cart
        int variantId = getMatchedSizeVariant(product, variant, addToCart.size);
        setCurrentCart(sphere().currentCart().addLineItem(addToCart.productId, variantId, addToCart.quantity));
        addToCart.displaySuccessMessage();
        return ok(products.render(product, variant, getDefaultCategory(product)));
    }

    @With(FormHandler.class)
    public static Result update() {
        // Case missing or invalid form data, display errors
        Form<UpdateCart> form = updateCartForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("update-cart", form);
            return badRequest(showPage());
        }
        // Case valid, update quantity
        UpdateCart updateCart = form.get();
        CartUpdate cartUpdate = new CartUpdate()
                .setLineItemQuantity(updateCart.lineItemId, updateCart.quantity);
        setCurrentCart(sphere().currentCart().update(cartUpdate));
        updateCart.displaySuccessMessage();
        return ok(showPage());
    }

    @With(FormHandler.class)
    public static Result remove() {
        // Case missing or invalid form data, display errors
        Form<RemoveFromCart> form = removeFromCartForm.bindFromRequest();
        if (form.hasErrors()) {
            displayErrors("remove-from-cart", form);
            return badRequest(showPage());
        }
        // Case valid, remove item
        RemoveFromCart removeFromCart = form.get();
        setCurrentCart(sphere().currentCart().removeLineItem(removeFromCart.lineItemId));
        removeFromCart.displaySuccessMessage();
        return ok(showPage());
    }

    protected static Content showPage() {
        return carts.render(getCurrentCart());
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
