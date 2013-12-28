class window.Paymill
    constructor: (@form) ->
        @paymentType = @form.inputs.filter('.paymenttype.disabled').val() ? 'cc'
        @error = false

        @cardNumber = @form.inputs.filter('.card-number')
        @cardCvc = @form.inputs.filter('.card-cvc')
        @cardMonth = @form.inputs.filter('.card-expiry-month')
        @cardYear = @form.inputs.filter('.card-expiry-year')
        @cardHolder = @form.inputs.filter('.card-holdername')
        @cardAmount = @form.inputs.filter('.card-amount')
        @cardCurrency = @form.inputs.filter('.card-currency')

        @elvNumber = @form.inputs.filter('.elv-account')
        @elvBank = @form.inputs.filter('.elv-bankcode')
        @elvHolder = @form.inputs.filter('.elv-holdername')

    # Method to update amount and currency for 3DS credit card
    updatePrice: (amount, currency) ->
        @cardAmount.val parseFloat(amount) * 100
        @cardCurrency.val currency

    # General method to validate payment data
    validate: ->
        switch @paymentType
            when "cc" then @validateCc()
            when "elv" then @validateElv()
        return not @error

    # Method to validate credit card data
    validateCc: ->
        if not paymill.validateCardNumber @cardNumber.val()
            @form.displayErrorMessage(translation["error"]["invalid-card-number"], @cardNumber)
            @error = true
        if not paymill.validateExpiry @cardMonth.val(), @cardYear.val()
            @form.displayErrorMessage(translation["error"]["invalid-card-expiry-date"], @cardMonth)
            @error = true
        if not paymill.validateCvc @cardCvc.val(), @cardNumber.val()
            @form.displayErrorMessage("Invalid verification code", @cardCvc)
            @error = true
        if not @cardHolder.val()?
            @form.displayErrorMessage(translation["error"]["invalid-card-holdername"], @cardHolder)
            @error = true

    # Method to validate debit bank data
    validateElv: ->
        if not paymill.validateAccountNumber @elvAccount.val()
            @form.displayErrorMessage(translation["error"]["invalid-elv-accountnumber"], @elvAccount)
            @error = true
        if not paymill.validateBankCode @elvBank.val()
            @form.displayErrorMessage(translation["error"]["invalid-elv-bankcode"], @elvBank)
            @error = true
        if not @elvHolder.val()?
            @form.displayErrorMessage(translation["error"]["invalid-elv-holdername"], @elvHolder)
            @error = true

    # Method to handle form submission
    submit: (responseHandler) ->
        return false if @error
        switch @paymentType
            when "ELV" then params = {
                    number: @elvNumber.val()
                    bank: @elvBank.val()
                    accountholder: @elvHolder.val()
                }
            else params = {
                    number: @cardNumber.val()
                    exp_month: @cardMonth.val()
                    exp_year: @cardYear.val()
                    cvc: @cardCvc.val()
                    cardholder: @cardHolder.val()
                    amount: @cardAmount.val() * 100
                    currency: @cardCurrency.val()
                }
        paymill.createToken(params, responseHandler)