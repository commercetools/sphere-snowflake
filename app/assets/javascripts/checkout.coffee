$ ->
    Handlebars.registerHelper('ifEq', (v1, v2, options) ->
        if v1 is v2 then options.fn(this) else options.inverse(this)
    )
    template = {
        address: Handlebars.compile $.trim($("#shipping-address-template").html())
    }

    marginTop = 78
    checkout = $('#form-checkout')
    checkoutCart = $('#checkout-cart.step')
    checkoutShipping = $('#checkout-shipping.step')
    checkoutBilling = $('#checkout-billing.step')
    sections = $('#checkout .step')
    shippingAddressForm = $("#shipping-address-form")

    shippingAddress = new Form $('#form-shipping-address')
    billingMethod = new Form $('#form-billing-method')
    paymill = new Paymill billingMethod, $('#form-checkout')

    # Toggle payment form
    billingMethod.inputs.filter('.paymenttype').click ->
        $(this).addClass('btn-primary disabled')
        if $(this).val() is 'ELV'
            $('#payment-form-elv').show()
            $('#payment-form-cc').hide()
            $('#btn-paymenttype-cc').removeClass('btn-primary disabled')
        else
            $('#payment-form-elv').hide()
            $('#payment-form-cc').show()
            $('#btn-paymenttype-elv').removeClass('btn-primary disabled')

    # Method to be called each time a change has been triggered
    updateCheckout = ->
        orderSummary.load()

    # Load and replace shipping address form with new data
    replaceShippingAddress = (data) ->
        shippingAddressForm.empty().append(template.address data)

    # Load shipping address form
    loadShippingAddress = ->
        $.getJSON(shippingAddressForm.data("url"), (data) ->
            replaceShippingAddress data
        )

    # Fill form summary with form data
    fillSummary = (form, summaryList) ->
        summaryList.find("span").empty()
        form.find(':input').not(':disabled').not(':hidden').each ->
            value = if $(this).is('select') then $(this).find(':selected').text() else $(this).val()
            name = $(this).attr("name") ? $(this).attr('class')
            place = summaryList.find("span[data-form=#{name}]")

            if place.length > 0
                # If there is a list element for this value, set here the data
                place.text(value)
            else
                # Otherwise append a new list element
                summaryList.append("<li><span>" + value + "</span></li>")

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
            paymill.updatePrice res.data.cart.totalPrice, res.data.cart.currency
            # Append cart snapshot to checkout form
            checkout.append "<input type='hidden' name='cartSnapshot' value='#{res.data.cartSnapshot}'/>"
            # Fill summary form data
            fillSummary $('#shipping-address-form'), $('#shipping-address-summary')
            # Go to next section
            nextStep(checkoutShipping)
        xhr.fail (res) -> shippingAddress.failSubmit(res)
        xhr.always -> shippingAddress.stopSubmit()

        return shippingAddress.allowSubmit
    )

    # Bind billing 'next step' click event to 'validate form' and 'next step' functionality
    billingMethod.form.submit( ->
        # Remove alert messages
        billingMethod.removeAllMessages()
        billingMethod.reload()

        # Validate form client side
        return false unless paymill.validate()

        # Submit payment data to Paymill
        billingMethod.startSubmit()
        paymill.submit( (error, result) ->
            billingMethod.stopSubmit()
            return billingMethod.displayErrorMessage(error.apierror) if error
            # Append token to checkout form
            checkout.append "<input type='hidden' name='paymillToken' value='#{result.token}'/>"
            # Fill form summary data
            fillSummary($('#billing-method-form'), $('#billing-method-summary'))
            # Go to next section
            nextStep(checkoutBilling)
        )

        return billingMethod.allowSubmit
    )

    $("#shipping-address-list .address-item").click( ->
        $.getJSON($(this).data("url"), (data) ->
            replaceShippingAddress data
        )
    )

    loadShippingAddress()
