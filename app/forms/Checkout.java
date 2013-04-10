package forms;

import io.sphere.client.shop.model.Cart;
import play.data.validation.Constraints;

public class Checkout {

    //@Constraints.Required(message = "Billing address required")
    @Constraints.Min(value = 0, message = "Invalid value for billing address")
    public Integer billingAddress;

    //@Constraints.Required(message = "Shipping address required")
    @Constraints.Min(value = 0, message = "Invalid value for shipping address")
    public Integer shippingAddress;

    //@Constraints.Required(message = "Shipping method required")
    @Constraints.Pattern(value = "dhl", message = "Invalid value for shipping method")
    public String shippingMethod;

    @Constraints.Required(message = "Payment method required")
    public String paymentMethod;

    @Constraints.Required(message = "Missing cart version")
    public String checkoutId;

    public Checkout() {
    }

    public Checkout(Integer billingAddress, Integer shippingAddress, String paymentMethod, String shippingMethod,
                    String checkoutId) {
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.shippingMethod = shippingMethod;
        this.checkoutId = checkoutId;
    }

    public Checkout(Cart cart) {
        this.billingAddress = 0;
        this.shippingAddress = 0;
        this.paymentMethod = "";
        this.shippingMethod = "";
    }

}
