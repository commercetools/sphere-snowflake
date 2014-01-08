define ["jquery", "handlebars", "demo/lib-form.min", "demo/paymill.min"], ($) ->
    template = {
        address: Handlebars.compile $.trim($("#shipping-address-template").html())
        shipping: Handlebars.compile $.trim($("#shipping-method-template").html())
    }

    marginTop = 78
    checkout = $('#form-checkout')
    checkoutCart = $('#checkout-cart.step')
    checkoutAddress = $('#checkout-address.step')
    checkoutShipping = $('#checkout-shipping.step')
    checkoutBilling = $('#checkout-billing.step')
    sections = $('#checkout .step')
    shippingAddressForm = $("#shipping-address-form")
    shippingMethodForm = $("#shipping-method-form")

    shippingAddress = new Form $('#form-shipping-address')
    shippingMethod = new Form $('#form-shipping-method')
    billingMethod = new Form $('#form-billing-method')
    paymill = new Paymill billingMethod, $('#form-checkout')

    # Toggle payment form between credit card and direct debit
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
    updateCheckout = (res) ->
        # Update price details
        orderSummary.replace res.data.cart
        # Update payment form
        paymill.updatePrice res.data.cart.totalPrice, res.data.cart.currencyCode
        # Update cart snapshot
        checkout.find("input[name=cartSnapshot]").val res.data.cartSnapshot


    # Load shipping address form
    loadShippingAddress = ->
        url = shippingAddressForm.data("url")
        return unless url?
        shippingAddressForm.find('.loading-ajax').show()
        $.getJSON url, (data) ->
            replaceShippingAddress data
            shippingAddressForm.find('.loading-ajax').hide()

    # Load shipping method form
    loadShippingMethod = ->
        url = shippingMethodForm.data("url")
        return unless url?
        shippingMethodForm.find('.loading-ajax').show()
        $.getJSON url, (data) ->
            replaceShippingMethod data
            shippingMethodForm.find('.loading-ajax').hide()


    # Load and replace shipping address form with new data
    replaceShippingAddress = (data) ->
        shippingAddressForm.empty().append(template.address data)

    # Load and replace shipping method form with new data
    replaceShippingMethod = (data) ->
        shippingMethodForm.empty().append(template.shipping data)


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


    # "Edit step data" functionality bound to click event
    $('#checkout .btn-edit').click ->
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


    # "Go to next step" functionality bound to click event
    checkoutCart.find('.btn-next').click ->
        nextStep(checkoutCart)


    # "Set shipping address" functionality handler bound to submit event
    shippingAddress.form.submit ->
        shippingAddress.reload()
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
            # Update checkout content
            replaceShippingMethod res.data.shippingMethod
            updateCheckout res
            # Fill summary form data
            fillSummary $('#shipping-address-form'), $('#shipping-address-summary')
            # Go to next section
            nextStep(checkoutAddress)
        xhr.fail (res) -> shippingAddress.failSubmit res
        xhr.always -> shippingAddress.stopSubmit()
        return shippingAddress.allowSubmit

    # "Set shipping method" functionality handler bound to submit event
    shippingMethod.form.submit ->
        shippingMethod.reload()
        # Remove alert messages
        shippingMethod.removeAllMessages()
        # Validate form client side
        return false unless shippingMethod.validateRequired()
        # Send new data to server
        shippingMethod.startSubmit()
        url = shippingMethod.form.attr("action")
        method = shippingMethod.form.attr("method")
        data = shippingMethod.form.serialize()
        xhr = shippingMethod.submit(url, method, data)
        xhr.done (res) ->
            # Update checkout content
            updateCheckout res
            # Fill summary form data
            summary = $('#shipping-method-summary')
            method = shippingMethod.inputs.filter("[name=method]:checked")
            summary.find("[data-form=name]").text method.data("name")
            summary.find("[data-form=price]").text method.data("price")
            summary.find("[data-form=description]").text method.data("description")
            # Go to next section
            nextStep(checkoutShipping)
        xhr.fail (res) -> shippingMethod.failSubmit res
        xhr.always -> shippingMethod.stopSubmit()
        return shippingMethod.allowSubmit

    # "Set shipping method" functionality handler bound to change event
    shippingMethodForm.on "change", ":input", ->
        shippingMethod.reload()
        # Remove alert messages
        shippingMethod.removeAllMessages()
        # Validate form client side
        return false unless shippingMethod.validateRequired()
        # Send new data to server
        shippingMethod.startSubmit()
        url = shippingMethod.form.attr("action")
        method = shippingMethod.form.attr("method")
        data = shippingMethod.form.serialize()
        xhr = shippingMethod.submit(url, method, data)
        xhr.done (res) ->
            # Update checkout content
            updateCheckout res
        xhr.fail (res) -> shippingMethod.failSubmit res
        xhr.always -> shippingMethod.stopSubmit()


    # "Set payment data" functionality handler bound to submit event
    billingMethod.form.submit ->
        # Remove alert messages
        billingMethod.removeAllMessages()
        billingMethod.reload()
        # Validate form client side
        return false unless paymill.validate()
        # Submit payment data to Paymill
        billingMethod.startSubmit()
        paymill.submit (error, result) ->
            billingMethod.stopSubmit()
            return billingMethod.displayErrorMessage(error.apierror) if error
            # Append token to checkout form
            checkout.find("input[name=paymillToken]").val result.token
            # Fill form summary data
            fillSummary($('#billing-method-form'), $('#billing-method-summary'))
            # Go to next section
            nextStep(checkoutBilling)
        return billingMethod.allowSubmit


    # Replace shipping address with address from address book
    $("#shipping-address-list .address-item").click ->
        url = $(this).data("url")
        return unless url?
        $(this).find('.loading-ajax').show()
        $.getJSON url, (data) ->
            replaceShippingAddress data
            $(this).find('.loading-ajax').hide()


    loadShippingAddress()
    loadShippingMethod()
