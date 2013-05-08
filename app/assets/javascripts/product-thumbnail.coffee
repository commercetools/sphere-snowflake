$ ->
    # Add class 'active' to hovered product item
    $('#product-list').on('mouseenter', '.product-item', ->
        $(this).addClass("active")
    )

    # Remove class 'active' to unhovered product item
    $('#product-list').on('mouseleave', '.product-item', ->
        $(this).removeClass("active")
    )

    # Update product item with hovered product variant
    $('#product-list').on('mouseenter', '.product-info-variants a', ->
        productItem = $(this).parentsUntil('.product-item').parent()

        # Update product picture
        variantImg = $(this).data('image')
        productItem.find('.product-image img').attr('src', variantImg)

        # Update product price
        variantPrice = $(this).data('price')
        productItem.find('.product-info-price').text(variantPrice)

        # Update all product detail links
        productLink = productItem.find('.product-info-name a').attr('href')
        variantLink = $(this).attr('href')
        productItem.find("a:not(.product-info-variants a)[href='" + productLink + "']").attr('href', variantLink)

        # Update all add to cart links
        variantId = $(this).data('variant')
        productItem.find("form.form-add-to-cart [name=variantId]").val(variantId)

        # Update active variant
        $(this).parentsUntil('ul').parent().find('.active').removeClass("active")
        $(this).parentsUntil('ul').addClass("active")
    )