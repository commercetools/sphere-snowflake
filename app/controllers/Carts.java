package controllers;

import io.sphere.client.shop.model.*;
import forms.AddToCart;
import forms.RemoveFromCart;
import forms.UpdateCart;
import play.data.Form;
import play.mvc.Result;
import sphere.ShopController;

import static play.data.Form.form;

public class Carts extends ShopController {

    public static Result show() {
        Cart cart = sphere().currentCart().fetch();
        return ok(views.html.carts.render(cart));
    }

    public static Result add() {
        Form<AddToCart> form = form(AddToCart.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        AddToCart addToCart = form.get();
        String variantId = addToCart.variantId;
        // If size is selected we have to find the correct variant
        if (addToCart.size != null) {
            // Fetch selected product variant
            Product product = sphere().products.byId(addToCart.productId).fetch().orNull();
            if (product == null) {
                return badRequest();
            }
            Variant variant = product.getVariants().byId(addToCart.variantId).orNull();
            if (variant == null) {
                return badRequest();
            }
            // Fetch all variants
            VariantList variants = product.getVariants();
            // Filter them by selected color, if any
            if (variant.hasAttribute("color")) {
                variants = variants.byAttributes(variant.getAttribute("color"));
            }
            // Filter them by selected size
            Attribute size = new Attribute("size", addToCart.size);
            variantId = variants.byAttributes(size).first().or(variant).getId();
        }
        Cart cart = sphere().currentCart().addLineItem(addToCart.productId, variantId, addToCart.quantity);
        return ok(views.html.ajax.updateCart.render(cart));
    }

    public static Result update() {
        Form<UpdateCart> form = form(UpdateCart.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        UpdateCart updateCart = form.get();
        //Cart cart = sphere().currentCart().updateLineItemQuantity(updateCart.lineItemId, updateCart.quantity);
        //return ok(views.html.ajax.updateCart.render(cart));
        return ok();
    }

    public static Result remove() {
        Form<RemoveFromCart> form = form(RemoveFromCart.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest();
        }
        RemoveFromCart updateCart = form.get();
        Cart cart = sphere().currentCart().removeLineItem(updateCart.lineItemId);
        return ok(views.html.ajax.updateCart.render(cart));
    }
}
