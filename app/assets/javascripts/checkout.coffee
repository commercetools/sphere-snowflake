$ ->
    marginTop = 78
    checkoutCart = $('#checkout-cart.step')
    checkoutShipping = $('#checkout-shipping.step')
    checkoutBilling = $('#checkout-billing.step')

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

    # Get form data
    getFormData = (form) ->
        data = {}
        form.find(':input').not(':disabled').each ->
            data[$(this).attr("name")] = $(this).val()
        return data

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

    # Load the payment method list
    loadPaymentNetworks = (listElement) ->
        listElement.load("checkout/payment/network", ->

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
        )

    # Bind 'change' button click event to allow editing a section form
    $('#checkout .btn-edit').click( ->
        selected = $(this).parentsUntil('.step').parent()
        focused = selected.siblings('.step:not(.disabled):not(.visited)')

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
        nextStep(checkoutCart)
    )

    # Bind shipping 'next step' click event to 'submit form' and 'next step' functionality
    checkoutShipping.find('.btn-next').click( ->
        form = $('#form-shipping-address')

        # Validate form client side
        return unless validateForm(form, false)

        # Send address to server
        url = "/checkout/submit/shipping"
        $.ajax url,
            type: 'POST'
            data: getFormData(form)
            dataType: 'html'
        .done( ->
            # Load payment networks once we have shipping information
            loadPaymentNetworks($('#payment-networks'))

            # Fill form summary data
            fillSummary(form, $('#shipping-address-summary'))

            # Go to next section
            nextStep(checkoutShipping)
        )
    )

    # Bind billing 'next step' click event to 'validate form' and 'next step' functionality
    checkoutBilling.find('.btn-next').click( ->
        form = $('#form-billing-method')

        # Validate form client side
        return unless validateForm(form, true)

        # Fill form summary data
        fillSummary(form, $('#billing-method-summary'))

        # Go to next section
        nextStep(checkoutBilling)
    )
