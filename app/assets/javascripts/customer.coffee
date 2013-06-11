$ ->
    Handlebars.registerHelper('ifEq', (v1, v2, options) ->
        if v1 is v2 then options.fn(this) else options.inverse(this)
    )
    template = {
        update: Handlebars.compile $("#update-address-template").html().trim()
        add: Handlebars.compile $("#add-address-template").html().trim()
    }

    updateCustomer = new Form $('#form-update-customer')
    updatePassword = new Form $('#form-update-password')
    addAddress = new Form $('#form-add-address')

    addressList = $("#address-list")

    # Load new address form on page loaded
    loadAddressForm = ->
        return unless template.add?
        addAddress.form.empty().append(template.add {})

    # Load address list on page loaded
    loadAddressList = ->
        $.getJSON(addressList.data("url"), (data) ->
            replaceAddressList data
        )

    # Replace the whole address list
    replaceAddressList = (list) ->
        return unless template.update? or list?
        addressList.empty()
        addressList.append(template.update address) for address in list.address

    # Update address list with proper animation
    updateAddressList = (list) ->
        removeActiveAddresses()
        updatedIds = ("address-#{address.addressId}" for address in list.address)

        # Remove addresses that no longer exist
        removed = $("#address-list .address-item").filter -> return $(this).attr("id") not in updatedIds
        removed.each -> $(this).fadeOut 500

        # Add new addresses
        return unless template.update? or list?
        removed.promise().done( ->
            $(this).remove()
            for address in list.address when addressList.find("#address-#{address.addressId}").length < 1
                element = $(template.update address).hide()
                addressList.append(element)
                element.fadeIn 500
        )

    # Remove all active addresses
    removeActiveAddresses = ->
        addressList.find('.address-item').removeClass("active")

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

        return updateCustomer.allowSubmit
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

        return updatePassword.allowSubmit
    )

    # Bind new address 'save' submit event to 'add address' functionality
    addAddress.form.submit( ->
        # Remove alert messages
        addAddress.removeAllMessages()

        # Validate form client side
        return false unless addAddress.validateRequired()

        # Send new data to server
        addAddress.startSubmit()
        url = addAddress.form.attr("action")
        method = addAddress.form.attr("method")
        data = addAddress.form.serialize()
        xhr = addAddress.submit(url, method, data)
        xhr.done (res) ->
            addAddress.doneSubmit(res)
            updateAddressList(res.data)
        xhr.fail (res) -> addAddress.failSubmit(res)
        xhr.always -> addAddress.stopSubmit()

        return addAddress.allowSubmit
    )

    # Bind update address 'save' submit event to 'update address' functionality
    addressList.on('submit', 'form.form-update-address', ->
        updateAddress = new Form $(this)

        # Remove alert messages
        updateAddress.removeAllMessages()

        # Validate form client side
        return false unless updateAddress.validateRequired()

        # Send new data to server
        updateAddress.startSubmit()
        url = updateAddress.form.attr("action")
        method = updateAddress.form.attr("method")
        data = updateAddress.form.serialize()
        xhr = updateAddress.submit(url, method, data)
        xhr.done (res) ->
            updateAddress.doneSubmit(res)
            updateAddressList(res.data)
        xhr.fail (res) -> updateAddress.failSubmit(res)
        xhr.always -> updateAddress.stopSubmit()

        return updateAddress.allowSubmit
    )

    # Bind 'remove address' submit event to 'remove address' functionality
    addressList.on('submit', 'form.form-remove-address', ->
        removeAddress = new Form $(this)

        # Remove alert messages
        removeAddress.removeAllMessages()

        # Validate form client side
        return false unless removeAddress.validateRequired()

        # Send new data to server
        removeAddress.startSubmit()
        url = removeAddress.form.attr("action")
        method = removeAddress.form.attr("method")
        data = removeAddress.form.serialize()
        xhr = removeAddress.submit(url, method, data)
        xhr.done (res) ->
            removeAddress.doneSubmit(res)
            updateAddressList(res.data)
        xhr.fail (res) -> removeAddress.failSubmit(res)
        xhr.always -> removeAddress.stopSubmit()

        return removeAddress.allowSubmit
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

    # Bind click on 'add new address' to show add address form
    $('.open-add-address').click( ->
        addAddress.clean()
        removeActiveAddresses()
    )

    # Bind click on 'edit address' to show update address form
    addressList.on('click', '.open-update-address, .address-item .address', ->
        removeActiveAddresses()

        # Active current address
        $(this).parent('.address-item').addClass("active")
    )

    loadAddressForm()
    loadAddressList()