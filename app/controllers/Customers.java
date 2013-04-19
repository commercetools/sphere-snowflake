package controllers;

import controllers.actions.Authorization;
import io.sphere.client.shop.model.Customer;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

@With(Authorization.class)
public class Customers extends ShopController {

    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        return ok(views.html.customers.render(customer));
    }

}
