$ ->
    Handlebars.registerHelper('ifEq', (v1, v2, options) ->
        if v1 is v2 then options.fn(this) else options.inverse(this)
    )
    template = {
        payment: Handlebars.compile $("#payment-method-template").html().trim()
        address: Handlebars.compile $("#shipping-address-template").html().trim()
    }

    marginTop = 78
    checkoutCart = $('#checkout-cart.step')
    checkoutShipping = $('#checkout-shipping.step')
    checkoutBilling = $('#checkout-billing.step')
    sections = $('#checkout .step')
    shippingAddressForm = $("#shipping-address-form")

    shippingAddress = new Form $('#form-shipping-address')
    billingMethod = new Form $('#form-billing-method')

    # Method to be called each time a change has been triggered
    updateCheckout = ->
        loadPaymentMethod $('#payment-networks')
        orderSummary.load()

    # Load and replace shipping address form with new data
    replaceShippingAddress = (data) ->
        shippingAddressForm.empty().append(template.address data)

    # Load shipping address form
    loadShippingAddress = ->
        $.getJSON(shippingAddressForm.data("url"), (data) ->
            replaceShippingAddress data
        )

    # Load payment method list
    loadPaymentMethod = (listElement) ->
        listElement.find('.loading-ajax').show()

        xhr = $.getJSON(listElement.data("url"))
        xhr.done (data) ->
            return window.location.href = data.redirect if data.redirect?
            return unless template? and data?
            listElement.empty()
            listElement.append(template.payment data)

            # Disable all form elements until selected
            listElement.find('.payment-network-form :input').attr('disabled', 'disabled')

            # Add events on change selected payment method
            listElement.find('.payment-network').has('a[data-toggle=tab]').click( ->
                active = listElement.find($(this).find('a[data-toggle=tab]').attr("href"))
                inactive = active.siblings()

                # Disable form elements of inactive networks
                inactive.find(':input').attr('disabled', 'disabled')

                # Enable form elements of selected network
                active.find(':input').removeAttr('disabled')
            )

            # Show tooltip on hovering input with hint
            listElement.find('.hint-message').hover( ->
                id = $(this).parent().attr("for") + "-hint"
                $('#' + id + '.hint').show()
            , ->
                id = $(this).parent().attr("for") + "-hint"
                $('#' + id + '.hint').hide()
            )
            listElement.find('.loading-ajax').hide()

    # Fill form summary with form data
    fillSummary = (form, summaryList) ->
        form.find(':input').not(':disabled').each ->
            value = if $(this).is('select') then $(this).find(':selected').text() else $(this).val()
            place = summaryList.find('[data-form=' + $(this).attr("name") + ']')

            if place.length > 0
                # If there is a list element for this value, set here the data
                place.text(value)
            else
                # Otherwise append a new list element
                summaryList.append("<li>" + value + "</li>")

    # Jump to the next section form
    nextStep = (focused) ->
        next = $('#checkout .step.disabled').filter(':first')

        # Set focused section as visited
        focused.removeClass("disabled current").addClass("visited")

        if next.length > 0
            # Set first disabled section as current
            next.removeClass("visited disabled").addClass("current")

            # Set scroll position to next section
            $('html, body').animate scrollTop: next.offset().top - marginTop, 'slow'
        else
            # Set submit button visible
            $('#checkout-footer button[type=submit]').not(':visible').fadeIn()


    # Bind 'change' button click event to allow editing a section form
    $('#checkout .btn-edit').click( ->
        selected = $(this).parentsUntil('.step').parent()
        focused = sections.not('.disabled').not('.visited')

        # Set focused section as disabled unless it is also current section
        if focused.hasClass("current")
            focused.addClass("disabled")
        else
            focused.addClass("visited")

        # Set selected section as current
        selected.removeClass("visited disabled")

        # Set scroll position to selected section
        $('html, body').animate scrollTop: selected.offset().top - marginTop, 'slow'
    )

    # Bind cart 'next step' click event to 'next step' functionality
    checkoutCart.find('.btn-next').click( ->
        updateCheckout()
        nextStep(checkoutCart)
    )

    # Bind shipping address submit event to 'set address' and 'next step' functionality
    shippingAddress.form.submit( ->
        # Remove alert messages
        shippingAddress.removeAllMessages()

        # Validate form client side
        return false unless shippingAddress.validateRequired()

        # Send new data to server
        shippingAddress.startSubmit()
        url = shippingAddress.form.attr("action")
        method = shippingAddress.form.attr("method")
        data = shippingAddress.form.serialize()
        xhr = shippingAddress.submit(url, method, data)
        xhr.done (res) ->
            shippingAddress.doneSubmit(res)
            updateCheckout()
            fillSummary($('#shipping-address-form'), $('#shipping-address-summary'))
            nextStep(checkoutShipping)
        xhr.fail (res) -> shippingAddress.failSubmit(res)
        xhr.always -> shippingAddress.stopSubmit()

        return shippingAddress.allowSubmit
    )

    # Bind billing 'next step' click event to 'validate form' and 'next step' functionality
    billingMethod.inputs.filter('.btn-next').click( ->
        # Remove alert messages
        billingMethod.removeAllMessages()
        billingMethod.reload()

        # Validate form client side
        return false unless billingMethod.validateRequired(true)

        # Fill form summary data
        fillSummary($('#billing-method-form'), $('#billing-method-summary'))

        # Go to next section
        nextStep(checkoutBilling)
    )

    $("#shipping-address-list .address-item").click( ->
        $.getJSON($(this).data("url"), (data) ->
            replaceShippingAddress data
        )
    )

    loadShippingAddress()
