package forms;

import play.data.validation.Constraints;

public class UpdatePassword {

    @Constraints.Required(message = "Password required")
    public String passwordOld;

    @Constraints.Required(message = "Password required")
    public String passwordNew;

    @Constraints.Required(message = "Password required")
    public String passwordRepeat;


    public UpdatePassword() {

    }

    public String validate() {
        if (!passwordNew.equals(passwordRepeat)) {
            return "New passwords do not match";
        }
        return null;
    }

}
