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
        return false

    # Validate field values are equal
    validateEqualFields = (fields) ->
        # Start with all fields marked as valid
        fields.attr("aria-invalid", "false")
        value = fields.first().val()
        console.debug value

        # Mark incorrect fields as invalid
        invalid = fields.filter -> return $(this).val() isnt value
        return true unless invalid.length > 0
        fields.attr("aria-invalid", "true")
        return false

    # Get form data
    getFormData = (form) ->
        data = {}
        form.find(':input').not(':disabled').each ->
            data[$(this).attr("name")] = $(this).val()
        return data

    # Update customer data
    updateCustomer = (url, method, data) ->
        $.ajax url,
            type: method
            data: data
            dataType: 'html'
        .done ->
            console.debug "OK"
        .fail ->
            console.debug "KO"

    # Update password data
    updatePassword = (url, method, data) ->
        $.ajax url,
            type: method
            data: data
            dataType: 'html'
        .done ->
            console.debug "OK"
        .fail ->
            console.debug "KO"

    # Bind customer update 'save' submit event to 'update customer' functionality
    updateCustomerForm.find('[type=submit]').click( ->
        # Validate form client side
        return false unless validateForm($(this), false)

        # Send new data to server
        url = updateCustomerForm.attr("action")
        method = updateCustomerForm.attr("method")
        data = getFormData(updateCustomerForm)
        updateCustomer url, method, data

        # Disable form submit
        return false
    )

    # Bind password update 'save' submit event to 'update password' functionality
    updatePasswordForm.find('[type=submit]').click( ->
        # Validate form client side
        return false unless validateForm updatePasswordForm, false

        # Validate repeat password match
        repeatPasswords = updatePasswordForm.find('[name=newPassword], [name=repeatPassword]')
        return false unless validateEqualFields repeatPasswords

        # Send new data to server
        url = updatePasswordForm.attr("action")
        method = updatePasswordForm.attr("method")
        data = getFormData(updatePasswordForm)
        updatePassword url, method, data

        # Disable form submit
        return false
    )