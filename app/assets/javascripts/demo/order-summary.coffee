define ["jquery", "handlebars"], ($) ->
    class @OrderSummary
        constructor: (@content) ->
            html = $("#order-summary-template").html()
            @template = Handlebars.compile $.trim(html) if html?

        load: ->
            url = @content.data("url")
            if url?
                @content.find('.loading-ajax').show()
                $.getJSON(url, (data) =>
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



