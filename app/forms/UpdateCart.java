package forms;

import play.data.validation.Constraints;

public class UpdateCart {

    @Constraints.Required(message = "Line item required")
    public String lineItemId;

    @Constraints.Required(message = "Quantity required")
    @Constraints.Min(1)
    @Constraints.Max(10)
    public int quantity;


    public UpdateCart() {

    }

    public UpdateCart(String lineItemId, int quantity) {
        this.lineItemId = lineItemId;
        this.quantity = quantity;
    }
}
