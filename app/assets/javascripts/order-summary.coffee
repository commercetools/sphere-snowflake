$ ->
    class @OrderSummary
        constructor: (@content) ->
            html = $("#order-summary-template").html()
            @template = Handlebars.compile html.trim() if html?

        # Replace the whole order summary
        replace: (cart) ->
            return unless @template?
            @content.empty()
            @content.append(@template cart)

    window.orderSummary = new @OrderSummary $('.order-summary')

    # Load order summary on page loaded
    $.getJSON(orderSummary.content.data("url"), (data) ->
        orderSummary.replace data
    )


