$ ->
    summaryOrder = '#order-summary'

    createForm = (form) =>
        return new @Form form, true, 'html'

    getMiniCart = =>
        return @miniCart

    # Bind 'add to cart' button in product detail with 'add to cart' functionality
    $('#product-detail #form-add-to-cart').submit( ->
        addToCart = createForm $(this)

        # Send new data to server
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()

        addToCart.submit(url, method, data, (res) ->
            response = $("<div>").html(res)

            # Update mini cart
            getMiniCart().popoverCart.empty().append(response.find('#mini-cart-popover').contents())

            # Open mini cart
            getMiniCart().open('slow')
            getMiniCart().addCloseDelay(3000, 'slow')
        )

        return false
    )

    # Bind 'add to cart' button in product list with 'add to cart' functionality
    $('#product-list').on('submit', 'form.form-add-to-cart', ->
        addToCart = createForm $(this)

        # Send new data to server
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()

        addToCart.submit(url, method, data, (res) ->
            response = $("<div>").html(res)

            # Update mini cart
            getMiniCart().popoverCart.empty().append(response.find('#mini-cart-popover').contents())

            # Open mini cart
            getMiniCart().open('slow')
            getMiniCart().addCloseDelay(3000, 'slow')
        )

        return false
    )

    # Bind 'update item quantity' input with 'update cart' functionality
    updateDelay = {}
    $("#cart form.form-update-cart input[name=quantity]").change( ->
        updateCart = createForm $(this).closest('form')

        # Send new data to server
        url = updateCart.form.attr("action")
        method = updateCart.form.attr("method")
        data = updateCart.form.serialize()

        lineItemId = updateCart.inputs.filter('[name=lineItemId]').val()

        clearTimeout updateDelay.timeout if lineItemId is updateDelay.item and updateDelay.timeout?
        updateDelay.item = lineItemId
        updateDelay.timeout = setTimeout ( ->
            updateCart.submit(url, method, data, (res) ->
                response = $("<div>").html(res)

                # Update item total price
                $('#item-total-price-'+ lineItemId).text(response.find('#item-total-price-'+ lineItemId).text())

                # Update order summary total price
                $(summaryOrder).empty().append(response.find(summaryOrder).contents())

                # Update mini cart
                getMiniCart().popoverCart.empty().append(response.find('#mini-cart-popover').contents())
            )
        ), 800

        return false
    )

    # Bind 'remove item' button with 'remove from cart' functionality
    $('#cart form.form-remove-from-cart').submit( ->
        removeFromCart = createForm $(this)

        # Send new data to server
        url = removeFromCart.form.attr("action")
        method = removeFromCart.form.attr("method")
        data = removeFromCart.form.serialize()

        lineItemId = removeFromCart.inputs.filter('[name=lineItemId]').val()

        removeFromCart.submit(url, method, data, (res) ->
            response = $("<div>").html(res)

            # Remove line item
            $('#item-line-'+ lineItemId).fadeOut(500, ->
                $(this).remove()
            )

            # Update order summary total price
            $(summaryOrder).empty().append(response.find(summaryOrder).contents())

            # Update mini cart
            getMiniCart().popoverCart.empty().append(response.find('#mini-cart-popover').contents())
        )

        return false
    )

