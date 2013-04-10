package controllers;

import io.sphere.client.shop.model.Cart;
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
        Cart cart = sphere().currentCart().addLineItem(addToCart.productId, addToCart.variantId, addToCart.quantity);
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
