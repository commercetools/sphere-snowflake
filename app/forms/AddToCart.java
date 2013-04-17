package forms;

import play.data.validation.Constraints;

public class AddToCart {

    @Constraints.Required(message = "Product required")
    public String productId;

    @Constraints.Required(message = "Variant required")
    public String variantId;

    @Constraints.Required(message = "Quantity required")
    @Constraints.Min(1)
    @Constraints.Max(10)
    public int quantity;

    public String size;


    public AddToCart() {

    }

    public AddToCart(String productId, String variantId, int quantity) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public AddToCart(String productId, String variantId, int quantity, String size) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.size = size;
    }
}
