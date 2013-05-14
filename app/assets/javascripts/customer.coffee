$ ->
    updateCustomer = new Form $('#form-update-customer')
    updatePassword = new Form $('#form-update-password')

    # Bind customer update 'save' submit event to 'update customer' functionality
    updateCustomer.form.submit( ->
        # Remove alert messages
        updateCustomer.removeAllMessages()

        # Validate form client side
        return false unless updateCustomer.validateRequired()

        # Send new data to server
        updateCustomer.startSubmit()
        url = updateCustomer.form.attr("action")
        method = updateCustomer.form.attr("method")
        data = updateCustomer.form.serialize()
        xhr = updateCustomer.submit(url, method, data)
        xhr.done (res) -> updateCustomer.doneSubmit(res)
        xhr.fail (res) -> updateCustomer.failSubmit(res)
        xhr.always -> updateCustomer.stopSubmit()

        false
    )

    # Bind password update 'save' submit event to 'update password' functionality
    updatePassword.form.submit( ->
        # Remove alert messages
        updatePassword.removeAllMessages()

        # Validate form client side
        return false unless updatePassword.validateRequired()

        # Validate repeat password match
        repeatPasswords = updatePassword.inputs.filter('[name=newPassword], [name=repeatPassword]')
        return false unless updatePassword.validateEqualFields(repeatPasswords)

        # Send new data to server
        updatePassword.startSubmit()
        url = updatePassword.form.attr("action")
        method = updatePassword.form.attr("method")
        data = updatePassword.form.serialize()
        xhr = updatePassword.submit(url, method, data)
        xhr.done (res) -> updatePassword.doneSubmit(res)
        xhr.fail (res) -> updatePassword.failSubmit(res)
        xhr.always -> updatePassword.stopSubmit()

        false
    )

    # Bind change tab with remove success messages functionality
    $('.tabbable .nav [data-toggle=tab]').click( ->
        updateCustomer.removeSuccessMessages()
        updatePassword.removeSuccessMessages()
    )

    # Bind change input value with remove all messages functionality
    updateCustomer.inputs.change( ->
        if updateCustomer.saved is true
            updateCustomer.removeAllMessages()
            updateCustomer.displayInfoMessage("You have unsaved changes", 700)
            updateCustomer.saved = false
    )

    # Bind change input value with remove all messages functionality
    updatePassword.inputs.change( ->
        if updatePassword.saved is true
            updatePassword.removeAllMessages()
            updatePassword.displayInfoMessage("You have unsaved changes", 700)
            updatePassword.saved = false
    )