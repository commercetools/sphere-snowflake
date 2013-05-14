$ ->
    summaryOrder = '#order-summary'

    # Bind 'add to cart' button in product detail with 'add to cart' functionality
    $('#product-detail #form-add-to-cart').submit( ->
        addToCart = new Form $(this), 'html'

        # Send new data to server
        addToCart.startSubmit()
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()
        xhr = addToCart.submit(url, method, data)
        xhr.done (res) ->
            addToCart.doneSubmit(res)
            response = $("<div>").html(res)

            # Update mini cart
            miniCart.popoverCart.empty().append(response.find('#mini-cart-popover').contents())

            # Open mini cart
            miniCart.open('slow')
            miniCart.addCloseDelay(3000, 'slow')
        xhr.fail (res) -> addToCart.failSubmit(res)
        xhr.always -> addToCart.stopSubmit()

        false
    )

    # Bind 'add to cart' button in product list with 'add to cart' functionality
    $('#product-list').on('submit', 'form.form-add-to-cart', ->
        addToCart = new Form $(this), 'html'

        # Send new data to server
        addToCart.startSubmit()
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()
        xhr = addToCart.submit(url, method, data)
        xhr.done (res) ->
            addToCart.doneSubmit(res)
            response = $("<div>").html(res)

            # Update mini cart
            miniCart.popoverCart.empty().append(response.find('#mini-cart-popover').contents())

            # Open mini cart
            miniCart.open('slow')
            miniCart.addCloseDelay(3000, 'slow')
        xhr.fail (res) -> addToCart.failSubmit(res)
        xhr.always -> addToCart.stopSubmit()

        false
    )

    # Bind 'update item quantity' input with 'update cart' functionality
    updateDelay = {}
    $("#cart form.form-update-cart input[name=quantity]").change( ->
        updateCart = new Form $(this).closest('form'), 'html'

        # Send new data to server
        updateCart.startSubmit()
        url = updateCart.form.attr("action")
        method = updateCart.form.attr("method")
        data = updateCart.form.serialize()

        lineItemId = updateCart.inputs.filter('[name=lineItemId]').val()

        clearTimeout updateDelay.timeout if lineItemId is updateDelay.item and updateDelay.timeout?
        updateDelay.item = lineItemId
        updateDelay.timeout = setTimeout ( ->
            xhr = updateCart.submit(url, method, data)
            xhr.done (res) ->
                updateCart.doneSubmit(res)
                response = $("<div>").html(res)

                # Update item total price
                $('#item-total-price-'+ lineItemId).text(response.find('#item-total-price-'+ lineItemId).text())

                # Update order summary total price
                $(summaryOrder).empty().append(response.find(summaryOrder).contents())

                # Update mini cart
                miniCart.popoverCart.empty().append(response.find('#mini-cart-popover').contents())
            xhr.fail (res) -> updateCart.failSubmit(res)
            xhr.always -> updateCart.stopSubmit()
        ), 800

        false
    )

    # Bind 'remove item' button with 'remove from cart' functionality
    $('#cart form.form-remove-from-cart').submit( ->
        removeFromCart = new Form $(this), 'html'

        # Send new data to server
        removeFromCart.startSubmit()
        url = removeFromCart.form.attr("action")
        method = removeFromCart.form.attr("method")
        data = removeFromCart.form.serialize()

        xhr = removeFromCart.submit(url, method, data)
        xhr.done (res) ->
            removeFromCart.doneSubmit(data)
            response = $("<div>").html(data)

            # Remove line item
            lineItemId = removeFromCart.inputs.filter('[name=lineItemId]').val()
            $('#item-line-'+ lineItemId).fadeOut(500, ->
                $(this).remove()
            )

            # Update order summary total price
            $(summaryOrder).empty().append(response.find(summaryOrder).contents())

            # Update mini cart
            miniCart.popoverCart.empty().append(response.find('#mini-cart-popover').contents())
        xhr.fail (res) ->
            removeFromCart.failSubmit(res)
            removeFromCart.stopSubmit()

        false
    )

