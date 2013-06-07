$ ->
    cartContent = $('#cart-content')
    html = $("#cart-item-template").html()
    template = Handlebars.compile html.trim() if html?

    # Load address list on page loaded
    $.getJSON(cartContent.data("url"), (data) ->
        replaceCart data
    )

    # Replace the whole cart
    replaceCart = (cart) ->
        return unless template? or cart?
        cartContent.empty()
        cartContent.append(template item) for item in cart.item

    # Bind 'add to cart' button in product detail with 'add to cart' functionality
    $('#product-detail #form-add-to-cart').submit( ->
        addToCart = new Form $(this)

        # Send new data to server
        addToCart.startSubmit()
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()
        xhr = addToCart.submit(url, method, data)
        xhr.done (res) ->
            addToCart.doneSubmit res
            orderSummary.replace res.data
            miniCart.replace res.data
            miniCart.open('slow')
            miniCart.addCloseDelay(3000, 'slow')
        xhr.fail (res) -> addToCart.failSubmit res
        xhr.always -> addToCart.stopSubmit()

        return addToCart.allowSubmit
    )

    # Bind 'add to cart' button in product list with 'add to cart' functionality
    $('#product-list').on('submit', 'form.form-add-to-cart', ->
        addToCart = new Form $(this)

        # Send new data to server
        addToCart.startSubmit()
        url = addToCart.form.attr("action")
        method = addToCart.form.attr("method")
        data = addToCart.form.serialize()
        xhr = addToCart.submit(url, method, data)
        xhr.done (res) ->
            addToCart.doneSubmit res
            orderSummary.replace res.data
            miniCart.replace res.data
            miniCart.open('slow')
            miniCart.addCloseDelay(3000, 'slow')
        xhr.fail (res) -> addToCart.failSubmit res
        xhr.always -> addToCart.stopSubmit()

        return addToCart.allowSubmit
    )

    # Bind 'update item quantity' input with 'update cart' functionality
    updateDelay = {}
    $("#cart").on("change", "form.form-update-cart input[name=quantity]", ->
        updateCart = new Form $(this).closest('form')
        lineItemId = updateCart.inputs.filter('[name=lineItemId]').val()

        # Send new data to server
        updateCart.startSubmit()
        url = updateCart.form.attr("action")
        method = updateCart.form.attr("method")
        data = updateCart.form.serialize()

        clearTimeout updateDelay.timeout if lineItemId is updateDelay.item and updateDelay.timeout?
        updateDelay.item = lineItemId
        updateDelay.timeout = setTimeout ( ->
            xhr = updateCart.submit(url, method, data)
            xhr.done (res) ->
                updateCart.doneSubmit res
                orderSummary.replace res.data
                miniCart.replace res.data
                replaceCart res.data
            xhr.fail (res) -> updateCart.failSubmit res
            xhr.always -> updateCart.stopSubmit()
        ), 800

        return updateCart.allowSubmit
    )

    # Bind 'remove item' button with 'remove from cart' functionality
    $("#cart").on("submit", "form.form-remove-from-cart", ->
        removeFromCart = new Form $(this)
        lineItemId = removeFromCart.inputs.filter('[name=lineItemId]').val()

        # Send new data to server
        removeFromCart.startSubmit()
        url = removeFromCart.form.attr("action")
        method = removeFromCart.form.attr("method")
        data = removeFromCart.form.serialize()

        xhr = removeFromCart.submit(url, method, data)
        xhr.done (res) ->
            removeFromCart.doneSubmit res
            orderSummary.replace res.data
            miniCart.replace res.data
            # Remove line item
            $('#item-line-'+ lineItemId).fadeOut 500, ( -> $(this).remove() )
        xhr.fail (res) ->
            removeFromCart.failSubmit res
            removeFromCart.stopSubmit()

        return removeFromCart.allowSubmit
    )

