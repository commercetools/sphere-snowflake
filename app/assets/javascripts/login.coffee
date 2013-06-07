$ ->
    logIn = new Form $('#form-log-in')
    signUp = new Form $('#form-sign-up')
    recoverPassword = new Form $('#form-recover-password')
    resetPassword = new Form $('#form-reset-password')

    # Bind 'log in' submit event to 'log in' functionality
    logIn.form.submit( ->
        return if logIn.allowSubmit

        # Remove alert messages
        logIn.removeAllMessages()

        # Validate form client side
        return false unless logIn.validateRequired()

        # Send new data to server
        logIn.startSubmit()
        url = logIn.form.attr("action")
        method = logIn.form.attr("method")
        data = logIn.form.serialize()
        xhr = logIn.submit(url, method, data)
        xhr.done (res) ->
            logIn.allowSubmit = true
            logIn.form.submit()
        xhr.fail (res) -> logIn.stopSubmit() if logIn.failSubmit(res)

        return logIn.allowSubmit
    )

    # Bind 'sign up' submit event to 'sign up' functionality
    signUp.form.submit( ->
        # Remove alert messages
        signUp.removeAllMessages()

        # Validate form client side
        return false unless signUp.validateRequired()

        # Send new data to server
        signUp.startSubmit()
        url = signUp.form.attr("action")
        method = signUp.form.attr("method")
        data = signUp.form.serialize()
        xhr = signUp.submit(url, method, data)
        xhr.done (res) -> signUp.doneSubmit(res)
        xhr.fail (res) -> signUp.stopSubmit() if signUp.failSubmit(res)

        return signUp.allowSubmit
    )

    # Bind 'recover password' submit event to 'recover password' functionality
    recoverPassword.form.submit( ->
        # Remove alert messages
        recoverPassword.removeAllMessages()

        # Validate form client side
        return false unless recoverPassword.validateRequired()

        # Send new data to server
        recoverPassword.startSubmit()
        url = recoverPassword.form.attr("action")
        method = recoverPassword.form.attr("method")
        data = recoverPassword.form.serialize()
        xhr = recoverPassword.submit(url, method, data)
        xhr.done (res) ->
            recoverPassword.doneSubmit(res)
            # Close form
            $('#recover-password-modal').modal('hide')
        xhr.fail (res) -> recoverPassword.failSubmit(res)
        xhr.always -> recoverPassword.stopSubmit()

        return recoverPassword.allowSubmit
    )

    # Bind 'recover password' submit event to 'recover password' functionality
    resetPassword.form.submit( ->
        # Remove alert messages
        resetPassword.removeAllMessages()

        # Validate form client side
        return false unless resetPassword.validateRequired()

        # Send new data to server
        resetPassword.startSubmit()
        url = resetPassword.form.attr("action")
        method = resetPassword.form.attr("method")
        data = resetPassword.form.serialize()
        xhr = resetPassword.submit(url, method, data)
        xhr.done (res) ->
            resetPassword.doneSubmit(res)
            # Close form
            setTimeout ( -> $('#reset-password-modal').modal('hide')), 3000
        xhr.fail (res) ->
            resetPassword.stopSubmit() if resetPassword.failSubmit(res)

        return resetPassword.allowSubmit
    )

    # Automatically open reset password modal when found
    $('#reset-password-modal').modal("show")