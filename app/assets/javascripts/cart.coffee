$ ->
    miniCart = '#mini-cart'
    popoverCart = '#mini-cart-popover'
    linkCart = '#link-cart'
    summaryOrder = '#order-summary'
    closeDelay = {}

    # Fade mini cart in
    openMiniCart = (speed) ->
        removeCloseDelay()
        p = $(popoverCart)
        p.stop true, true
        p.fadeIn speed, ->
            $(document).unbind 'mouseup'
            $(document).mouseup( (e) ->
                closeMiniCart 'fast' if p.has(e.target).length is 0
            )

    # Fade mini cart out
    closeMiniCart = (speed) ->
        $(document).unbind 'mouseup'
        p = $(popoverCart)
        p.stop true, true
        p.fadeOut speed

    # Set timeout to close mini cart after a while
    addCloseDelay = (time, speed) ->
        removeCloseDelay()
        closeDelay.timeout = setTimeout ( ->
            closeMiniCart speed
        ), time

    # Remove timeout to close mini cart
    removeCloseDelay = ->
        clearTimeout closeDelay.timeout if closeDelay.timeout?

    # Bind 'mouse over mini cart' to 'open mini cart' functionality unless showing cart page
    $(miniCart).hover( ->
        openMiniCart 'fast' unless $('#cart').length > 0
    , ->
        addCloseDelay(500, '')
    )

    # Add selected product and quantity to cart
    addToCart = (productId, variantId, quantity) ->
        url = "/cart/add"
        $.ajax url,
            type: 'POST'
            data: {
                productId: productId,
                variantId: variantId,
                quantity: quantity
            }
            dataType: 'html'
            success: (data, textStatus, jqXHR) ->
                response = $("<div>").html(data)

                # Update mini cart
                $(popoverCart).empty().append(response.find(popoverCart).contents())

                # Open mini cart
                openMiniCart('slow')
                addCloseDelay(3000, 'slow')

    # Update line item in cart with selected quantity
    updateCart = (lineItemId, quantity) ->
        url = "/cart/update"
        $.ajax url,
            type: 'POST'
            data: {
                lineItemId: lineItemId,
                quantity: quantity
            }
            dataType: 'html'
            success: (data, textStatus, jqXHR) ->
                response = $("<div>").html(data)

                # Update item total price
                $('#item-total-price-'+ lineItemId).text(response.find('#item-total-price-'+ lineItemId).text())

                # Update order summary total price
                $(summaryOrder).empty().append(response.find(summaryOrder).contents())

                # Update mini cart
                $(popoverCart).empty().append(response.find(popoverCart).contents())

    # Remove selected line item from cart
    removeFromCart = (lineItemId) ->
        url = "/cart/remove"
        $.ajax url,
            type: 'POST'
            data: {
                lineItemId: lineItemId,
            }
            dataType: 'html'
            success: (data, textStatus, jqXHR) ->
                response = $("<div>").html(data)

                # Remove line item
                $('#item-line-'+ lineItemId).fadeOut(500, ->
                    $(this).remove()
                )

                # Update order summary total price
                $(summaryOrder).empty().append(response.find(summaryOrder).contents())

                # Update mini cart
                $(popoverCart).empty().append(response.find(popoverCart).contents())


    # Bind 'add to cart' button in product list with 'add to cart' functionality
    $('#product-list').on('click', 'button[name=addToCart-product]', ->
        productId = $(this).data("product")
        variantId = $(this).data("variant")
        quantity = 1
        addToCart productId, variantId, quantity
    )

    # Bind 'add to cart' button in product detail with 'add to cart' functionality
    $("#product-detail button[name=addToCart-product]").click( ->
        productId = $(this).data("product")
        variantId = $(this).data("variant")
        quantity = $('#addToCart-quantity').val() ? 1
        addToCart productId, variantId, quantity
    )

    # Bind 'update item quantity' input with 'update cart' functionality
    updateDelay = {}
    $("#cart-cart input[name=quantity]").change( ->
        lineItemId = $(this).data("item")
        quantity = $(this).val()
        clearTimeout updateDelay.timeout if lineItemId is updateDelay.item and updateDelay.timeout?
        updateDelay.item = lineItemId
        updateDelay.timeout = setTimeout ( ->
            updateCart lineItemId, quantity
        ), 800
    )

    # Bind 'remove item' button with 'remove from cart' functionality
    $("#cart-cart button[name=remove]").click( ->
        lineItemId = $(this).data("item")
        removeFromCart lineItemId
    )

