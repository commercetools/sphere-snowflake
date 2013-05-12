$ ->
    logIn = new @Form $('#form-log-in')
    signUp = new @Form $('#form-sign-up')
    recoverPassword = new @Form $('#form-recover-password')
    resetPassword = new @Form $('#form-reset-password')

    # Bind 'log in' submit event to 'log in' functionality
    logIn.form.submit( ->
        # Remove alert messages
        logIn.removeAllMessages()

        # Validate form client side
        return false unless logIn.validateRequired()

        # Send new data to server
        url = logIn.form.attr("action")
        method = logIn.form.attr("method")
        data = logIn.form.serialize()
        logIn.submit(url, method, data)

        # Disable form submit
        return false
    )

    # Bind 'sign up' submit event to 'sign up' functionality
    signUp.form.submit( ->
        # Remove alert messages
        signUp.removeAllMessages()

        # Validate form client side
        return false unless signUp.validateRequired()

        # Send new data to server
        url = signUp.form.attr("action")
        method = signUp.form.attr("method")
        data = signUp.form.serialize()
        signUp.submit(url, method, data)

        # Disable form submit
        return false
    )

    # Bind 'recover password' submit event to 'recover password' functionality
    recoverPassword.form.submit( ->
        # Remove alert messages
        recoverPassword.removeAllMessages()

        # Validate form client side
        return false unless recoverPassword.validateRequired()

        # Send new data to server
        url = recoverPassword.form.attr("action")
        method = recoverPassword.form.attr("method")
        data = recoverPassword.form.serialize()
        recoverPassword.submit(url, method, data, ->
            $('#recover-password-modal').modal('hide')
        )

        # Disable form submit
        return false
    )

    # Bind 'recover password' submit event to 'recover password' functionality
    resetPassword.form.submit( ->
        # Remove alert messages
        resetPassword.removeAllMessages()

        # Validate form client side
        return false unless resetPassword.validateRequired()

        # Send new data to server
        url = resetPassword.form.attr("action")
        method = resetPassword.form.attr("method")
        data = resetPassword.form.serialize()
        resetPassword.submit(url, method, data, ->
            setTimeout ( -> $('#reset-password-modal').modal('hide')), 3000
        )

        # Disable form submit
        return false
    )

    # Automatically open reset password modal when found
    $('#reset-password-modal').modal("show")