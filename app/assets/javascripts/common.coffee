CDNJS = "https://cdnjs.cloudflare.com/ajax/libs/"

require.config
    baseUrl: "/assets/javascripts/"
    paths :
        # Using https because then the CDNs switch to SPDY
        "jquery"            : [CDNJS + "jquery/1.10.2/jquery.min"]
        "handlebars"        : [CDNJS + "handlebars.js/1.3.0/handlebars.min"]
        "bootstrap"         : [CDNJS + "twitter-bootstrap/2.3.2/js/bootstrap.min"]
        # The query param is needed in order to prevent requireJs to append the extension '.js'
        "paymill-bridge"    : ["https://bridge.paymill.com/?noext"]
        # Dependencies of masonry and images loaded
    shim :
        "bootstrap"         : ["jquery"]

require [
    "handlebars"
    "demo/main.min"
    "demo/mini-cart.min"
    "demo/order-summary.min"
]