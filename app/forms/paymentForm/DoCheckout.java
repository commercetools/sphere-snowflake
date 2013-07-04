package forms.paymentForm;

import play.data.validation.Constraints;

public class DoCheckout {

    @Constraints.Required(message = "Missing cart snapshot")
    public String cartSnapshot;

    @Constraints.Required(message = "Missing payment token")
    public String paymillToken;

    public DoCheckout() {

    }

}
