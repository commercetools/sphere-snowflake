$ ->
    Handlebars.registerHelper('ifNot', (v1, options) ->
        if not v1 then options.fn(this) else options.inverse(this)
    )

    Handlebars.registerHelper('ifEq', (v1, v2, options) ->
        if v1 is v2 then options.fn(this) else options.inverse(this)
    )

    Handlebars.registerHelper('ifGr', (v1, v2, options) ->
        if parseInt(v1, 10) > parseInt(v2, 10) then options.fn(this) else options.inverse(this)
    )

    Handlebars.registerHelper('image', (variant, isFeatured, options) ->
        if isFeatured then options.fn(variant.image.medium) else options.fn(variant.image.small)
    )