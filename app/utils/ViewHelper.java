package utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.io.*;
import com.neovisionaries.i18n.CountryCode;
import controllers.routes;
import io.sphere.client.model.Money;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.*;
import org.apache.commons.lang3.text.WordUtils;
import play.api.Play;
import play.mvc.Call;
import play.mvc.Http;
import sphere.Sphere;

import static utils.ControllerHelper.getGrossPrice;

public class ViewHelper {

	/**
	 * Returns the current Cart in session.
	 */
	public static Cart getCurrentCart() {
		return Sphere.getInstance().currentCart().fetch();
	}

    public static Customer getCurrentCustomer() {
        Customer customer = null;
        if (Sphere.getInstance().isLoggedIn()) {
            customer = Sphere.getInstance().currentCustomer().fetch();
        }
        return customer;
    }

    public static boolean isLoggedIn() {
        return Sphere.getInstance().isLoggedIn();
    }

	/**
	 * Returns the list of root categories
	 */
	public static List<Category> getRootCategories() {
        return Sphere.getInstance().categories().getRoots();
	}

    public static String capitalizeInitials(String text) {
        return WordUtils.capitalizeFully(text);
    }

    public static String getCountryName(String code) {
        try {
            return CountryCode.getByCode(code).getName();
        } catch (Exception e) {
            return "";
        }
    }

	/**
	 * Compares the categories and returns the 'active' class if are the same.
	 * @return 'active' if categories are the same, otherwise an empty string.
	 */
	public static String getActiveClass(Category category, Category currentCategory) {
        String active = "";
        if (currentCategory != null && currentCategory.getPathInTree().contains(category)) {
            active = "active";
        }
		return active;
	}

    public static BigDecimal getPercentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }

    public static boolean isSet(Object object) {
        return object != null;
    }


    /**
     *  TEMPLATE UTIL METHODS
     */

    public static Handlebars initializeHandlebars() throws IOException{
        File directory = Play.current().getFile("app/views/templates");
        if (!directory.exists()) throw new IOException("Not found template directory");
        TemplateLoader loader = new FileTemplateLoader(directory);
        return new Handlebars(loader);
    }

    public static Template getTemplate(String templateName) {
        try {
            return initializeHandlebars().compile(templateName);
        } catch (IOException ioe) {
            play.Logger.error(ioe.getMessage());
        }
        return null;
    }

    public static String getJavaScriptTemplate(String templateName) {
        try {
            return initializeHandlebars().compileInline("{{precompile \"" + templateName + "\"}}").apply("");
        } catch (IOException ioe) {
            play.Logger.error(ioe.getMessage());
        }
        return "";
    }

    public static String renderTemplate(Template template, JsonNode model) {
        if (template == null) return "";
        try {
            String html = template.apply(Context.newBuilder(model).resolver(JsonNodeValueResolver.INSTANCE).build());
            // TODO Remove when Handlebars java fixes this bug that adds quotes to arrays
            // https://github.com/jknack/handlebars.java/issues/260
            return html.replace("&quot;", "");
        } catch(IOException ioe) {
            play.Logger.error("Could not render item with template");
        }
        return "";
    }

    public static String renderTemplateProductList(Template template, ObjectNode json) {
        String html = "";
        for (JsonNode model : json.path("product")) {
            html += renderTemplate(template, model);
        }
        return html;
    }



    /**
     *  PRICE UTIL METHODS
     */

    public static Money getShippingCost() {
        // TODO Implement correct shipping cost
        return new Money(BigDecimal.valueOf(10), "EUR");
    }

    /* Methods for printing prices */
    public static String printPrice(Money money) {
        if (money == null) return "";
        return printPriceAmount(money) + " " + printPriceCurrency(money.getCurrencyCode());
    }

    public static String printPriceAmount(Money money) {
        if (money == null) return "";
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2);
        return format.format(money.getAmount());
    }

    public static String printPriceCurrency(String currencyCode) {
        return Currency.getInstance(currencyCode).getSymbol(Locale.GERMANY);
    }

    /* Calculate total price for all line items */
    public static Money getLineItemsTotalPrice(Order order) {
        Money itemsTotalPrice = new Money(BigDecimal.ZERO, order.getCurrency().getCurrencyCode());
        for (LineItem item : order.getLineItems()) {
            itemsTotalPrice = itemsTotalPrice.plus(getPrice(item));
        }
        return itemsTotalPrice;
    }

    /* Set of methods to get correct price */
    public static Money getPrice(Cart cart) {
        if (cart == null) return null;
        if (cart.getTaxedPrice() == null) return cart.getTotalPrice();
        return cart.getTaxedPrice().getTotalGross();
    }

    public static Money getPrice(Order order) {
        if (order == null) return null;
        if (order.getTaxedPrice() == null) return order.getTotalPrice();
        return order.getTaxedPrice().getTotalGross();
    }

    public static Money getPrice(ShippingInfo shippingInfo) {
        if (shippingInfo == null) return null;
        return getPrice(shippingInfo.getPrice(), shippingInfo.getTaxRate());
    }

    public static Money getPrice(LineItem item) {
        if (item == null) return null;
        return getPrice(item.getTotalPrice(), item.getTaxRate());
    }

    public static Money getPrice(CustomLineItem item) {
        if (item == null) return null;
        return getPrice(item.getMoney().multiply(item.getQuantity()), item.getTaxRate());
    }

    public static Money getPrice(Variant variant) {
        if (variant == null || variant.getPrice() == null) return null;
        return variant.getPrice().getValue();
    }

    public static Money getPrice(Money price, TaxRate rate) {
        return getGrossPrice(price, rate);
    }



    /**
     * URLS UTIL METHODS
     */
    public static Call getProductListUrl(SearchResult<Product> search, String sort, Category category) {
        if (search.getCurrentPage() >= search.getTotalPages() - 1) {
            return null;
        }
        // Convert from 0..N-1 to 1..N
        int nextPage = search.getCurrentPage() + 2;
        return getProductListUrl(nextPage, sort, category);
    }

    public static Call getProductListUrl(int page, String sort, Category category) {
        String categorySlug = "";
        if (category != null) {
            categorySlug = category.getSlug();
        }
        return routes.Categories.listProducts(categorySlug, sort, page);
    }

    public static Call getCategoryUrl(Category category) {
        return getCategoryUrl(category, 1);
    }

    public static Call getCategoryUrl(Category category, int page) {
        return routes.Categories.select(category.getSlug(), "", page);
    }

    public static Call getCategoryUrl(Category category, String sort, int page) {
        return routes.Categories.select(category.getSlug(), sort, page);
    }

    public static Call getProductUrl(Product product, Variant variant, Category category) {
        return routes.Products.select(product.getSlug(), variant.getId());
    }

    public static String getReturnUrl() {
        return Http.Context.current().session().get("returnUrl");
    }



    /**
     *  PRODUCTS UTIL METHODS
     */

    /**
     * Check whether the given product has more than one attribute value
     * @return true if the product has more than one attribute value, false otherwise
     */
    public static boolean hasMoreAttributeValues(Product product, String attributeName) {
        return product.getVariants().getAvailableAttributes(attributeName).size() > 1;
    }

    /**
     * Check whether the given Product has more than one 'color' attribute
     * @return true if the product has more than one color, false otherwise
     */
    public static boolean hasMoreColors(Product product) {
        return hasMoreAttributeValues(product, "color");
    }

    /**
     * Check whether the given Product has more than one 'size' attribute
     * @return true if the product has more than one size, false otherwise
     */
    public static boolean hasMoreSizes(Product product) {
        return hasMoreAttributeValues(product, "size");
    }

    /* Get possible variant sizes for a particular variant */
    public static List<String> getPossibleSizes(Product product, Variant variant) {
        List<Variant> variants = getPossibleVariants(product, variant, "size");
        List<String> sizes = new ArrayList<String>();
        for (Variant matchedVariant : variants) {
            sizes.add(matchedVariant.getString("size"));
        }
        return sizes;
    }

    /* Get variants with matching attributes but with different selected attribute
    * This method can be simplified if fixed and variable attributes are known beforehand */
    public static List<Variant> getPossibleVariants(Product product, Variant variant, String selectedAttribute) {
        List<Variant> matchingVariantList = new ArrayList<Variant>();
        List<Attribute> desiredAttributes = new ArrayList<Attribute>();
        // Get all other attributes with more than one different value
        for (Attribute attribute : variant.getAttributes()) {
            if (!selectedAttribute.equals(attribute.getName()) && hasMoreAttributeValues(product, attribute.getName())) {
                desiredAttributes.add(attribute);
            }
        }
        // Get variants matching all these other attributes but different selected attribute
        VariantList variantList = product.getVariants().byAttributes(desiredAttributes);
        for (Attribute attr : product.getVariants().getAvailableAttributes(selectedAttribute)) {
            if (variantList.byAttributes(attr).size() < 1) {
                matchingVariantList.add((product.getVariants().byAttributes(attr).first()).orNull());
            } else {
                matchingVariantList.add((variantList.byAttributes(attr).first()).orNull());
            }
        }
        matchingVariantList.removeAll(Collections.singleton(null));
        return matchingVariantList;
    }
}
