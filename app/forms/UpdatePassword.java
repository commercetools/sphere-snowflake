package forms;

import play.data.validation.Constraints;

public class UpdatePassword {

    @Constraints.Required(message = "Old password required")
    public String oldPassword;

    @Constraints.Required(message = "New password required")
    public String newPassword;

    @Constraints.Required(message = "Repeat password required")
    public String repeatPassword;


    public UpdatePassword() {

    }

    public String validate() {
        if (!newPassword.equals(repeatPassword)) {
            return "New passwords do not match";
        }
        return null;
    }

}
