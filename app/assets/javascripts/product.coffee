$ ->
    # Bind click on 'size variant' to 'select size' functionality
    $('.product-info-variant-size a').click( ->

        # Update 'add to cart' button with new variant
        variantId = $(this).data("variant")
        $('button[name=addToCart-product]').data("variant", variantId)

        # Disable 'active' on previously selected size
        $('.product-info-variant-size li.active').removeClass("active")

        # Enable 'active' on selected size
        $(this).parent().addClass("active")

        # Disable link
        return false
    )
