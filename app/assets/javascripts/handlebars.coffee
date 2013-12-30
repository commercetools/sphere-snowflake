$ ->
    Handlebars.registerHelper 'ifNot', (v1, options) ->
        if not v1 then options.fn(this) else options.inverse(this)

    Handlebars.registerHelper 'ifEq', (v1, v2, options) ->
        if v1 is v2 then options.fn(this) else options.inverse(this)

    Handlebars.registerHelper 'ifGr', (v1, v2, options) ->
        if parseInt(v1, 10) > parseInt(v2, 10) then options.fn(this) else options.inverse(this)

    Handlebars.registerHelper 'image', (variant, isFeatured, options) ->
        image = if isFeatured then variant.image.medium else variant.image.small
        options.fn image

    Handlebars.registerHelper 'sizes', (variant, options) ->
        if variant.size? then variant.size.join()
