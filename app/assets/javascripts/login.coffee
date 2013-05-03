$ ->
    logIn = new @Form $('#form-log-in')
    signUp = new @Form $('#form-sign-up')

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