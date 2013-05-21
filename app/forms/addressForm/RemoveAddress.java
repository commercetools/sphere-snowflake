package forms.addressForm;

import play.data.validation.Constraints;

public class RemoveAddress extends ListAddress {

    @Constraints.Required(message = "Invalid address")
    public String addressId;

    public RemoveAddress() {

    }

}
