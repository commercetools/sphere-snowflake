define ["jquery", "masonry.pkgd.min", "imagesloaded.pkgd.min", "handlebars"], ($, Masonry, imagesLoaded) ->
    calling = true
    distance = 1000

    productList = $("#product-list")
    productPager = $("#product-pager")
    jumpToTop = $('#jump-to-top')

    template = {
        item: Handlebars.compile $.trim($("#product-item-template").html())
        pager: Handlebars.compile $.trim($("#product-pager-template").html())
    }

    masonry = new Masonry(productList.get(0), {itemSelector: ".product-item"})
    imagesLoaded productList, ->
        productList.fadeTo("fast", 1)
        calling = false
        masonry.layout()

    # Load products from a URL
    loadProducts = (url) ->
        return if calling or not url
        calling = true
        productList.find('.loading-ajax').show()
        xhr = $.getJSON url
        xhr.done (res) ->
            appendProducts res
        xhr.always ->
            productList.find('.loading-ajax').hide()
            calling = false

    # Append products to the product list
    appendProducts = (data) ->
        return unless template.item? and template.pager? and data.product?
        # Append products
        page = ($(template.item product).fadeTo(0, 0) for product in data.product)
        productList.append page
        # Replace previous pager
        pagerHtml = template.pager data
        productPager.empty().append(pagerHtml).fadeTo(0, 0)
        imagesLoaded productList, ->
            masonry.appended product.get() for product in page
            productPager.fadeTo("slow", 1)
            # Avoid so many loops somehow!
            product.fadeTo(0, 1) for product in page
            masonry.layout()

    # Load and append more products to the product list
    loadMore = ->
        url = productPager.find("#load-more").data('url')
        loadProducts url

    # Bind click on 'Jump to top' tag to top scrolling
    $("a[href='#top']").click (e) ->
        $("html, body").animate { scrollTop: 0 }, "slow"
        return false

    # Bind click on 'Load  more' button to load more products functionality
    productPager.on "click", "#load-more", ->
        loadMore()
        return false

    # Check scroll position to fire 'jump to top' and 'load products' actions
    checkScrollingPosition = ->
        # Show the jump to top tag if it is not in the page top
        isNearTop = $(window).scrollTop() < 100
        jumpToTop.fadeOut() if isNearTop and jumpToTop.is(":visible")
        jumpToTop.fadeIn() if not isNearTop and jumpToTop.is(":hidden")

        # Load more products if it is near the page bottom
        limit = $(document).height() - $(window).height() - distance
        isNearBottom = $(window).scrollTop() >= limit
        loadMore() if isNearBottom

    setInterval checkScrollingPosition, 400