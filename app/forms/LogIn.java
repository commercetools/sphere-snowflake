package forms;

import play.data.validation.Constraints;

public class LogIn {

    @Constraints.Required(message = "Email required")
    @Constraints.Email(message = "Invalid value for email")
    public String email;

    @Constraints.Required(message = "Password required")
    public String password;


    public LogIn() {

    }

}
