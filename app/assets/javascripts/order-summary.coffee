$ ->
    class @OrderSummary
        constructor: (@content) ->
            html = $("#order-summary-template").html()
            @template = Handlebars.compile html.trim() if html?

        load: ->
            @content.find('.loading-ajax').show()
            $.getJSON(@content.data("url"), (data) =>
                @replace data
                @content.find('.loading-ajax').hide()
            )

        # Replace the whole order summary
        replace: (cart) ->
            return unless @template? and cart?
            @content.empty()
            @content.append(@template cart)

    window.orderSummary = new @OrderSummary $('.order-summary')

    # Load order summary on page loaded
    orderSummary.load()



