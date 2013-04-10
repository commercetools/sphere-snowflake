$ ->
    marginTop = 78
    checkoutCart = $('#checkout-cart.step')
    checkoutShipping = $('#checkout-shipping.step')
    checkoutBilling = $('#checkout-billing.step')

    # Validate form and mark incorrect fields as invalid
    validateForm = (form) ->
        # Start with all fields marked as valid
        required = form.find('[required=true]').filter(':visible')
        required.attr("aria-invalid", "false")

        # Mark incorrect fields as invalid
        invalid = required.filter -> return not $(this).val()
        return true unless invalid.length > 0
        invalid.attr("aria-invalid", "true")
        return false

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
                deselected = $(this).siblings()
                active = listElement.find($(this).find('a[data-toggle=tab]').attr("href"))
                inactive = active.siblings()

                # Display inactive all deselected logos
                deselected.find('img.network-img').attr("src", -> $(this).data("inactive"))

                # Disable form elements of inactive networks
                inactive.find(':input').attr('disabled', 'disabled')

                # Highlight images of the selected group
                $(this).find('img.network-img').attr("src", -> $(this).data("active"))

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

    # Bind cart 'next step' button click event to 'next step' functionality
    checkoutCart.find('.btn-next').click( ->
        nextStep(checkoutCart)
    )

    # Bind shipping 'next step' button click event to 'submit form' and 'next step' functionality
    checkoutShipping.find('.btn-next').click( ->
        # Validate form client side
        return unless validateForm(checkoutShipping.find('#form-shipping-address'))

        # Send address to server
        url = "/checkout/submit/shipping"
        $.ajax url,
            type: 'POST'
            data: {
                company: $('#shipping-address-company').val(),
                firstName: $('#shipping-address-firstName').val(),
                lastName: $('#shipping-address-lastName').val(),
                email: $('#shipping-address-email').val(),
                phone: $('#shipping-address-phone').val(),
                mobile: $('#shipping-address-mobile').val(),
                street: $('#shipping-address-street').val(),
                street2: $('#shipping-address-street2').val(),
                postalCode: $('#shipping-address-postalCode').val(),
                city: $('#shipping-address-city').val(),
                country: $('#shipping-address-country').val()
            }
            dataType: 'html'
        .done( ->
            # Load payment networks once we have shipping information
            loadPaymentNetworks($('#payment-networks'))

            # Go to next section
            nextStep(checkoutShipping)
        )
    )

    checkoutBilling.find('.btn-next').click( ->
        form = $('#form-billing-method')

        # Validate form client side
        return unless validateForm(form)

        # Go to next section
        nextStep(checkoutBilling)
    )
