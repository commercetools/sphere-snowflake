define ["jquery", "demo/lib-form.min"], ($) ->
    updateCustomer = new Form $('#form-update-customer')
    updatePassword = new Form $('#form-update-password')

    # "Update customer" functionality handler bound to submit event
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
        return updateCustomer.allowSubmit
    )

    # "Update password" functionality handler bound to submit event
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
        return updatePassword.allowSubmit
    )

    # Remove success messages from all forms when changing tab
    $('.tabbable .nav [data-toggle=tab]').click( ->
        updateCustomer.removeSuccessMessages()
        updatePassword.removeSuccessMessages()
    )

    # Remove all messages and show "unsaved changes" message when user modifies the customer form
    updateCustomer.inputs.change( ->
        if updateCustomer.saved is true
            updateCustomer.removeAllMessages()
            updateCustomer.displayInfoMessage("You have unsaved changes", 700)
            updateCustomer.saved = false
    )

    # Remove all messages and show "unsaved changes" message when user modifies the password form
    updatePassword.inputs.change( ->
        if updatePassword.saved is true
            updatePassword.removeAllMessages()
            updatePassword.displayInfoMessage("You have unsaved changes", 700)
            updatePassword.saved = false
    )
