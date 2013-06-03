$ ->
    class @MiniCart
        constructor: (@buttonCart, @popoverCart, @content) ->
            html = $("#mini-cart-template").html()
            @template = Handlebars.compile html.trim() if html?

        # Load data and replace mini cart
        load: ->
            @content.find('.loading-ajax').show()
            $.getJSON(@content.data("url"), (data) =>
                @replace data
                @content.find('.loading-ajax').hide()
            )

        # Replace the whole mini cart
        replace: (cart) ->
            return unless @template? and cart?
            @content.empty()
            @content.append(@template cart)

        # Fade mini cart in
        open: (speed) ->
            @removeCloseDelay()
            @popoverCart.stop true, true
            @popoverCart.fadeIn speed, =>
                $(document).unbind 'mouseup'
                $(document).mouseup( (e) =>
                    @close 'fast' if @popoverCart.has(e.target).length is 0
                )

        # Fade mini cart out
        close: (speed) ->
            $(document).unbind 'mouseup'
            @popoverCart.stop true, true
            @popoverCart.fadeOut speed

        # Set timeout to close mini cart after a while
        addCloseDelay: (time, speed) ->
            @removeCloseDelay()
            @closeDelay = setTimeout ( =>
                @close speed
            ), time

        # Remove timeout to close mini cart
        removeCloseDelay: ->
            clearTimeout @closeDelay if @closeDelay?

    window.miniCart = new @MiniCart $('#mini-cart'), $('#mini-cart-popover'), $('#mini-cart-content')

    # Bind 'mouse over mini cart' to 'open mini cart' functionality unless showing cart page
    miniCart.buttonCart.hover( ->
        miniCart.open 'fast' unless $('#cart').length > 0
    , ->
        miniCart.addCloseDelay(500, '')
    )

    # Load mini cart on page loaded
    miniCart.load()


