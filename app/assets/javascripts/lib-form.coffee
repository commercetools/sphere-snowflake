$ ->
    class @Form
        constructor: (@form, @saved = true) ->
            @labels = @form.find('label, fieldset')
            @inputs = @form.find(':input')
            @buttons = @inputs.filter('[type=submit]')

        # Mark fields as valid
        markValid: (fields) ->
            fields.attr("aria-invalid", "false")
            fields.parentsUntil('.control-group').parent().removeClass("error")

        # Mark fields as invalid
        markInvalid: (fields) ->
            fields.attr("aria-invalid", "true")
            fields.parentsUntil('.control-group').parent().addClass("error")

        # Validate form and mark incorrect fields as invalid
        validateRequired: ->
            @validateRequired(false)

        validateRequired: (allRequired) ->
            required = @inputs.not(':disabled')
            required = required.filter('[required]') if not allRequired

            # Start with all fields marked as valid
            @markValid @inputs

            # Check incorrect fields
            invalid = required.filter -> return not $(this).val()
            return true unless invalid.length > 0

            # Mark incorrect fields as invalid
            @markInvalid invalid

            # Display error message
            @displayErrorMessage("Highlighted fields are required")
            return false

        # Validate field values are equal
        validateEqualFields: (fields) ->
            # Start with all fields marked as valid
            @markValid fields
            value = fields.first().val()

            # Check incorrect fields
            invalid = fields.filter -> return $(this).val() isnt value
            return true unless invalid.length > 0

            # Mark incorrect fields as invalid
            @markInvalid fields

            # Display error message in its label container
            place = fields.first()
            @displayErrorMessage("These fields must contain the same value", place)
            return false

        # Create success message
        displaySuccessMessage: (message, speed = 0) ->
            text = '<i class="icon-ok"></i> ' + message
            @displayMessage("success", text, false, speed)

        # Create error message
        displayErrorMessage: (message, place = false, speed = 0) ->
            text = '<i class="icon-exclamation-sign"></i> ' + message
            @displayMessage("error", text, place, speed)

        # Create info message
        displayInfoMessage: (message, speed = 0) ->
            text = '<i class="icon-pencil"></i> ' + message
            @displayMessage("info", text, false, speed)

        # Create alert message
        displayMessage: (level, text, place, speed) ->
            # Choose inline help or form alert
            if not place or place.length < 1
                alert = $('<div class="alert alert-' + level + '">' + text + '</div>')
                place = @buttons
            else
                alert = $('<span class="help-inline span">' + text + '</span>')

            # Display alert
            alert.hide()
            place.after(alert)
            alert.fadeIn(speed)

        # Remove success alert messages
        removeSuccessMessages: (speed = 0) ->
            @removeMessages(true, speed)

        # Remove all alert messages
        removeAllMessages: (speed = 0) ->
            @removeMessages(false, speed)

        # Remove alert messages
        removeMessages: (removeSuccessOnly, speed) ->
            # Select alert and inline help messages
            alerts = if removeSuccessOnly then @form.find('.alert.alert-success, .control-group.success .help-inline')
            else @form.find('.alert, .help-inline')

            # Remove alert and inline help messages
            if alerts.length > 0
                alerts.stop true, true
                alerts.fadeOut(speed, -> $(this).remove())

            # Remove inline help notation
            helps = @form.find('.control-group')
            helps.removeClass("success")
            helps.removeClass("error", "info") if not removeSuccessOnly

            # Remove mark from invalid fields
            @markValid @inputs if not removeSuccessOnly

        # Submit form data
        submit: (url, method, data) ->
            @buttons.button('loading')
            $.ajax url,
                type: method
                data: data
                dataType: 'json'

            .always =>
                @buttons.button('reset')

            .done (data) =>
                # Display success message
                @saved = true
                @displaySuccessMessage("Saved!")

                # Update page data
                $.each data, (key, value) ->
                    elem = $('span[data-form-update=' + key + ']')
                    elem.text(value)

            .fail (xhr) =>
                # When dealing with any other error display default message
                if xhr.status isnt 400
                    @displayErrorMessage("Something went wrong...")

                # When dealing with bad request display errors
                else
                    try
                        data = $.parseJSON xhr.responseText
                        $.each data, (key, msg) =>
                            # Mark field as invalid
                            field = @inputs.filter('[name=' + key + ']')
                            @markInvalid field

                            # Display error message in each label
                            @displayErrorMessage(msg, field)
                    catch error
                        @displayErrorMessage("Something went wrong...")
