package controllers;

import controllers.actions.Authorization;
import forms.addressForm.ListAddress;
import forms.addressForm.RemoveAddress;
import forms.addressForm.SetAddress;
import forms.addressForm.UpdateAddress;
import io.sphere.client.shop.model.Address;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;

import java.util.List;

import static play.data.Form.form;

@With(Authorization.class)
public class Addresses extends ShopController {

    public static Result show() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Address> addresses = customer.getAddresses();
        return ok(SetAddress.getJson(addresses));
    }

    public static Result add() {
        Form<SetAddress> form = form(SetAddress.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        SetAddress setAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().addAddress(setAddress.getAddress());
        Customer customer = sphere().currentCustomer().update(update);
        return ok(SetAddress.getJson(customer.getAddresses()));
    }

    public static Result update() {
        Form<UpdateAddress> form = form(UpdateAddress.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        UpdateAddress updateAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().changeAddress(updateAddress.addressId, updateAddress.getAddress());
        Customer customer = sphere().currentCustomer().update(update);
        return ok(UpdateAddress.getJson(customer.getAddresses()));
    }

    public static Result remove() {
        Form<RemoveAddress> form = form(RemoveAddress.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        RemoveAddress removeAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().removeAddress(removeAddress.addressId);
        Customer customer = sphere().currentCustomer().update(update);
        return ok(RemoveAddress.getJson(customer.getAddresses()));
    }
}
