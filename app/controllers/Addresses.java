package controllers;

import controllers.actions.FormHandler;
import controllers.actions.Authorization;
import forms.addressForm.*;
import forms.customerForm.UpdateCustomer;
import forms.passwordForm.UpdatePassword;
import io.sphere.client.shop.model.Customer;
import io.sphere.client.shop.model.CustomerUpdate;
import io.sphere.client.shop.model.Order;
import play.data.Form;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import views.html.customers;

import java.util.List;

import static play.data.Form.form;
import static utils.ControllerHelper.displayErrors;

@With(Authorization.class)
public class Addresses extends ShopController {

    final static Form<UpdateCustomer> updateCustomerForm = form(UpdateCustomer.class);
    final static Form<UpdatePassword> updatePasswordForm = form(UpdatePassword.class);
    final static Form<AddAddress> addAddressForm = form(AddAddress.class);
    final static Form<UpdateAddress> updateAddressForm = form(UpdateAddress.class);
    final static Form<RemoveAddress> removeAddressForm = form(RemoveAddress.class);


    public static Result get(String id) {
        Customer customer = sphere().currentCustomer().fetch();
        return ok(ListAddress.getJson(customer.getAddressById(id)));
    }

    public static Result getList() {
        Customer customer = sphere().currentCustomer().fetch();
        return ok(ListAddress.getJson(customer.getAddresses()));
    }

    @With(FormHandler.class)
    public static Result add() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<AddAddress> form = addAddressForm.bindFromRequest();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("set-address", form);
            return badRequest(customers.render(customer, orders, customerForm, updatePasswordForm));
        }
        // Case valid add address
        AddAddress addAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().addAddress(addAddress.getAddress());
        customer = sphere().currentCustomer().update(update);
        addAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, orders, customerForm, updatePasswordForm));
    }

    @With(FormHandler.class)
    public static Result update() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<UpdateAddress> form = updateAddressForm.bindFromRequest();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("update-address", form);
            return badRequest(customers.render(customer, orders, customerForm, updatePasswordForm));
        }
        // Case valid update address
        UpdateAddress updateAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().changeAddress(updateAddress.addressId, updateAddress.getAddress());
        customer = sphere().currentCustomer().update(update);
        updateAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, orders, customerForm, updatePasswordForm));
    }

    @With(FormHandler.class)
    public static Result remove() {
        Customer customer = sphere().currentCustomer().fetch();
        List<Order> orders = sphere().currentCustomer().orders().fetch().getResults();
        Form<RemoveAddress> form = removeAddressForm.bindFromRequest();
        Form<UpdateCustomer> customerForm = updateCustomerForm.fill(new UpdateCustomer(customer));
        // Case missing or invalid form data
        if (form.hasErrors()) {
            displayErrors("remove-address", form);
            return badRequest(customers.render(customer, orders, customerForm, updatePasswordForm));
        }
        // Case valid remove address
        RemoveAddress removeAddress = form.get();
        CustomerUpdate update = new CustomerUpdate().removeAddress(removeAddress.addressId);
        customer = sphere().currentCustomer().update(update);
        removeAddress.displaySuccessMessage(customer.getAddresses());
        return ok(customers.render(customer, orders, customerForm, updatePasswordForm));
    }
}
