$ ->
    updateCustomerForm = $('#form-update-customer')
    updatePasswordForm = $('#form-update-password')

    # Validate form and mark incorrect fields as invalid
    validateForm = (form, allRequired) ->
        all = form.find(':input')
        required = all.not(':disabled')
        required = required.filter('[required]') if not allRequired

        # Start with all fields marked as valid
        all.attr("aria-invalid", "false")

        # Mark incorrect fields as invalid
        invalid = required.filter -> return not $(this).val()
        return true unless invalid.length > 0
        invalid.attr("aria-invalid", "true")
        displayMessage form, "error", "Highlighted fields are required"
        return false

    # Validate field values are equal
    validateEqualFields = (form, fields) ->
        # Start with all fields marked as valid
        fields.attr("aria-invalid", "false")
        value = fields.first().val()
        console.debug value

        # Mark incorrect fields as invalid
        invalid = fields.filter -> return $(this).val() isnt value
        return true unless invalid.length > 0
        fields.attr("aria-invalid", "true")
        displayMessage form, "error", "Highlighted fields must be equal"
        return false

    # Create alert message
    displayMessage = (place, level, message) ->
        icon = ''
        if level is "info" then icon = '<i class="icon-pencil"></i>'
        if level is "error" then icon = '<i class="icon-exclamation-sign"></i>'
        if level is "success" then icon = '<i class="icon-ok"></i>'
        place.append('<div class="alert alert-' + level + '">' + icon + ' ' + message + '</div>')

    # Remove all alert messages
    removeMessages = (place, speed, removeAll) ->
        alerts = if removeAll then place.find('.alert') else place.find('.alert.alert-success')
        alerts.stop true, true
        alerts.fadeOut(speed, -> $(this).remove())

    # Update customer data
    updateCustomer = (btn, url, method, data) ->
        btn.button('loading')
        $.ajax url,
            type: method
            data: data
            dataType: 'html'
        .always ->
            btn.button('reset')
        .done ->
            displayMessage btn.parent(), "success", "Saved!"
        .fail ->
            displayMessage btn.parent(), "error", "Something went wrong..."
            btn.text(btn.data("failed-text"))

    # Update password data
    updatePassword = (btn, url, method, data) ->
        btn.button('loading')
        $.ajax url,
            type: method
            data: data
            dataType: 'html'
        .always ->
            btn.button('reset')
        .done ->
            displayMessage btn.parent(), "success", "Saved!"
        .fail ->
            displayMessage btn.parent(), "error", "Something went wrong..."
            btn.text(btn.data("failed-text"))

    # Bind customer update 'save' submit event to 'update customer' functionality
    updateCustomerForm.find('[type=submit]').click( ->
        # Remove alert messages
        removeMessages updateCustomerForm, 0, true
        $(this).text $(this).data("default-text")

        # Validate form client side
        return false unless validateForm updateCustomerForm, false

        # Send new data to server
        url = updateCustomerForm.attr("action")
        method = updateCustomerForm.attr("method")
        data = updateCustomerForm.serialize()
        updateCustomer $(this), url, method, data

        # Disable form submit
        return false
    )

    # Bind password update 'save' submit event to 'update password' functionality
    updatePasswordForm.find('[type=submit]').click( ->
        # Remove alert messages
        removeMessages updatePasswordForm, 0, true
        $(this).text $(this).data("default-text")

        # Validate form client side
        return false unless validateForm updatePasswordForm, false

        # Validate repeat password match
        repeatPasswords = updatePasswordForm.find('[name=newPassword], [name=repeatPassword]')
        return false unless validateEqualFields updatePasswordForm, repeatPasswords

        # Send new data to server
        url = updatePasswordForm.attr("action")
        method = updatePasswordForm.attr("method")
        data = updatePasswordForm.serialize()
        updatePassword $(this), url, method, data

        # Disable form submit
        return false
    )

    # Bind change tab with remove messages functionality
    $('.tabbable .nav [data-toggle=tab]').click( ->
        removeMessages updateCustomerForm, 0, false
        removeMessages updatePasswordForm, 0, false
    )

    # Bind change input value with remove messages functionality
    updateCustomerForm.find(':input').change( ->
        removeMessages updateCustomerForm, 0, true
        displayMessage updateCustomerForm, "info", "You have unsaved changes"
    )

    # Bind change input value with remove messages functionality
    updatePasswordForm.find(':input').change( ->
        removeMessages updatePasswordForm, 0, true
        displayMessage updateCustomerForm, "info", "You have unsaved changes"
    )