$ ->
    # Bind click on 'size variant' to 'select size' functionality
    $('.product-info-variant-size a').click( ->

        # Update 'add to cart' button with new variant
        variantId = $(this).data("variant")
        $('#form-add-to-cart [name=variantId]').val(variantId)

        # Disable 'active' on previously selected size
        $('.product-info-variant-size li.active').removeClass("active")

        # Enable 'active' on selected size
        $(this).parent().addClass("active")

        # Disable link
        return false
    )
