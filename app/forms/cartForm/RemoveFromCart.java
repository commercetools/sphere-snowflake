package forms.cartForm;

import play.data.validation.Constraints;

public class RemoveFromCart {

    @Constraints.Required(message = "Line item required")
    public String lineItemId;


    public RemoveFromCart() {

    }

}
