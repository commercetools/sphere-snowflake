$ ->
    class @MiniCart
        constructor: (@buttonCart, @popoverCart) ->

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

    window.miniCart = new @MiniCart $('#mini-cart'), $('#mini-cart-popover')

    # Bind 'mouse over mini cart' to 'open mini cart' functionality unless showing cart page
    miniCart.buttonCart.hover( ->
        miniCart.open 'fast' unless $('#cart').length > 0
    , ->
        miniCart.addCloseDelay(500, '')
    )