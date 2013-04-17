$ ->
    # Use masonry to dynamically arrange product items
    $('#product-list').imagesLoaded( ->
        $('#product-list').masonry {
            itemSelector: '.product-item',
        }
    )

    # Load and append more products to the product list
    calling = false
    loadMore = ->
        url = $('#load-more').data('url')
        return if calling or not url
        calling = true
        $.ajax url,
            type: 'GET'
            dataType: 'html'
            success: (data, textStatus, jqXHR) ->
                response = $("<div>").html(data)

                # Update load more pager
                loader = response.find('#product-list-pager .more')
                $('#product-list-pager .more').addClass(loader.attr('class'))
                $('#product-list-pager .more a').data('url', loader.find('a').data('url'))

                # Append loaded products with masonry
                products = response.find('#product-list').children()
                products.imagesLoaded( ->
                    $('#product-list').append(products).masonry('appended', products)
                )
            complete: ->
                calling = false;

    # Bind click on 'Jump to top' tag to top scrolling
    $("a[href='#top']").click( (e) ->
        $("html, body").animate({ scrollTop: 0 }, "slow")
        return false
    )

    # Bind click on 'Load  more' button to load more products functionality
    $('#load-more').click( (e) ->
        loadMore()
        return false
    )

    # Check scroll position to fire 'jump to top' and 'load products' functionality
    setInterval ( ->
        # Show the jump to top tag if it is not in the page top
        isNearTop = $(window).scrollTop() < 100
        $('#jump-to-top').fadeOut() if isNearTop and $('#jump-to-top:visible')
        $('#jump-to-top').fadeIn() if not isNearTop and $('#jump-to-top:hidden')

        # Load more products if it is near the page bottom
        distance = 1000
        limit = $(document).height() - $(window).height() - distance
        isNearBottom = $(window).scrollTop() >= limit
        loadMore() if isNearBottom
    ), 300