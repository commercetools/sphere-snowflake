$ ->
    template = {
        update: Handlebars.compile $.trim($("#update-address-template").html())
        add: Handlebars.compile $.trim($("#add-address-template").html())
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
        url = addressList.data("url")
        return unless url?
        addressList.find('.loading-ajax').show()
        $.getJSON url, (data) ->
            replaceAddressList data
            addressList.find('.loading-ajax').hide()

    # Replace the whole address list
    replaceAddressList = (list) ->
        return unless template.update? and list?
        addressList.empty()
        addressList.append(template.update address) for address in list.address

    # Update address list with proper animation
    updateAddressList = (list) ->
        # Clean addresses form
        removeActiveAddresses()
        addAddress.clean()
        # Find all new addresses for later
        newList = (address for address in list.address when addressList.find("#address-#{address.addressId}").length < 1)
        # Replace all addresses
        replaceAddressList(list)
        # Fade in new addresses
        return unless newList?
        addressList.find("#address-#{address.addressId}").hide().fadeIn 500 for address in newList

    # Remove all active addresses
    removeActiveAddresses = ->
        addressList.find('.address-item').removeClass("active")

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

    # "Add address" functionality handler bound to submit event
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

    # "Update address" functionality handler bound to submit event
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

    # "Remove address" functionality handler bound to submit event
    addressList.on('submit', 'form.form-remove-address', ->
        removeAddress = new Form $(this)
        addressId = removeAddress.inputs.filter("[name=addressId]").val()
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
            removed = $("#address-#{addressId}").fadeOut 500, ->
                $(this).remove()
                updateAddressList(res.data)
        xhr.fail (res) -> removeAddress.failSubmit(res)
        xhr.always -> removeAddress.stopSubmit()
        return removeAddress.allowSubmit
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

    # Clean address form when user clicks on "new address"
    $('.open-add-address').click( ->
        addAddress.clean()
        removeActiveAddresses()
    )

    # Show selected address in the form when user clicks on "edit address"
    addressList.on('click', '.open-update-address, .address-item .address', ->
        removeActiveAddresses()
        # Active current address
        $(this).parent('.address-item').addClass("active")
    )

    loadAddressForm()
    loadAddressList()