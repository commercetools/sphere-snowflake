$ ->
    paymillForm = new Form $('#form-paymill')
    cardNumber = paymillForm.inputs.filter('.card-number')
    cardCvc = paymillForm.inputs.filter('.card-cvc')
    cardHolder = paymillForm.inputs.filter('.card-holdername')
    cardMonth = paymillForm.inputs.filter('.card-expiry-month')
    cardYear = paymillForm.inputs.filter('.card-expiry-year')
    cardAmount = paymillForm.inputs.filter('.card-amount-int')
    cardCurrency = paymillForm.inputs.filter('.card-currency')

    validatePaymill = (form) ->
        if paymill.validateCardNumber cardNumber.val()
            paymillForm.displayErrorMessage("Invalid card number", cardNumber)
        if paymill.validateExpiry cardMonth.val() cardYear.val()
            paymillForm.displayErrorMessage("Invalid expire date", cardMonth)
        if paymill.validateCvc cardCvc.val() cardNumber.val()
            paymillForm.displayErrorMessage("Invalid verification code", cardCvc)

    paymillForm.form.submit ->
        # Deactivate submit button to avoid further clicks
        paymillForm.find('.submit-button').attr("disabled", "disabled")

        paymill.createToken({
            number: cardNumber.val()
            exp_month: cardMonth.val()
            exp_year: cardYear.val()
            cvc: cardCvc.val()
            amount_int: cardAmount.val()
            currency: cardCurrency.val()
            cardholder: cardHolder.val()
        }, PaymillResponseHandler)

        return false