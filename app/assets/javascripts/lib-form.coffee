$ ->
    class @Form
        constructor: (@form, @saved = true) ->
            @labels = @form.find('label, fieldset')
            @inputs = @form.find(':input')
            @buttons = @inputs.filter('[type=submit]')

        # Mark fields as valid
        markValid: (fields) ->
            fields.attr("aria-invalid", "false")

        # Mark fields as invalid
        markInvalid: (fields) ->
            fields.attr("aria-invalid", "true")

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

            # Display error message in each label
            for field in invalid
                place = @labels.filter('[for=' + $(field).attr("id") + ']')
                @displayErrorMessage("Highlighted fields are required", place)
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
            place =  @labels.has($(fields))
            @displayErrorMessage("Highlighted fields must be equal", place)
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
            # Select form element if no element is specified
            place = @form if not place or place.length < 1

            # Display alert
            alert = $('<div class="alert alert-' + level + '">' + text + '</div>').hide()
            place.append(alert)
            alert.fadeIn(speed)

        # Remove success alert messages
        removeSuccessMessages: (speed = 0) ->
            @removeMessages(true, speed)

        # Remove all alert messages
        removeAllMessages: (speed = 0) ->
            @removeMessages(false, speed)

        # Remove alert messages
        removeMessages: (removeSuccessOnly, speed) ->
            # Remove alert messages
            alerts = if removeSuccessOnly then @form.find('.alert.alert-success') else @form.find('.alert')
            if alerts.length > 0
                alerts.stop true, true
                alerts.fadeOut(speed, -> $(this).remove())

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
                $.each data, (key, msg) =>
                    $('[data-form-update=' + key + ']')

                    # Display error message in each label
                    place = @labels.filter('[for=' + field.attr("id") + ']')
                    @displayErrorMessage(msg, place)

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
                            place = @labels.filter('[for=' + field.attr("id") + ']')
                            @displayErrorMessage(msg, place)
                    catch error
                        @displayErrorMessage("Something went wrong...")
