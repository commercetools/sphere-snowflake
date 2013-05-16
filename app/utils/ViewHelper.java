package utils;

import java.math.BigDecimal;
import java.util.*;

import com.neovisionaries.i18n.CountryCode;
import controllers.routes;
import io.sphere.client.model.Money;
import io.sphere.client.model.SearchResult;
import forms.paymentForm.PaymentNetwork;
import io.sphere.client.shop.model.*;
import org.apache.commons.lang3.text.WordUtils;
import play.mvc.Call;
import play.mvc.Http;
import sphere.Sphere;

public class ViewHelper {

	/**
	 * Returns the current Cart in session.
	 */
	public static Cart getCurrentCart() {
		return Sphere.getClient().currentCart().fetch();
	}

    public static Customer getCurrentCustomer() {
        Customer customer = null;
        if (Sphere.getClient().isLoggedIn()) {
            customer = Sphere.getClient().currentCustomer().fetch();
        }
        return customer;
    }

    public static boolean isLoggedIn() {
        return Sphere.getClient().isLoggedIn();
    }

	/**
	 * Returns the list of root categories
	 */
	public static List<Category> getRootCategories() {
        return Sphere.getClient().categories.getRoots();
	}

    public static String getCategoryPath(Category category) {
        String path = "";
        int level = category.getPathInTree().size();
        if (level > 1) {
            // Add first 2 oldest ancestors separated by '-'
            List<Category> ancestors = category.getPathInTree().subList(0, level - 1);
            String ancestorsPath = ancestors.get(0).getSlug();
            if (ancestors.size() > 1) {
                ancestorsPath += "-" + ancestors.get(1).getSlug();
            }
            path += ancestorsPath + "/";
        }
        // Add current category
        path += category.getPathInTree().get(level - 1).getSlug();
        return path;
    }

    public static String getReturnUrl() {
        return Http.Context.current().session().get("returnUrl");
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

    public static BigDecimal getPercentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }

	/**
	 * Compares the categories and returns the 'active' class if are the same.
	 * 
	 * @param category
     * @param currentCategory
	 * @return 'active' if categories are the same, otherwise an empty string.
	 */
	public static String getActiveClass(Category category, Category currentCategory) {
        String active = "";
        if (currentCategory != null && currentCategory.getPathInTree().contains(category)) {
            active = "active";
        }
		return active;
	}

    public static boolean isCustomerAddressSet(Cart cart) {
        return cart.getShippingAddress() != null;
    }

	/**
	 * Check whether the given product has more than one attribute value
	 * 
	 * @param product
     * @param attributeName
	 * @return true if the product has more than one attribute value, false otherwise
	 */
	public static boolean hasMoreAttributeValues(Product product, String attributeName) {
        return product.getVariants().getAvailableAttributes(attributeName).size() > 1;
    }

    /**
     * Check whether the given Product has more than one 'color' attribute
     *
     * @param product
     * @return true if the product has more than one color, false otherwise
     */
    public static boolean hasMoreColors(Product product) {
        return hasMoreAttributeValues(product, "color");
    }

    /**
     * Check whether the given Product has more than one 'size' attribute
     *
     * @param product
     * @return true if the product has more than one size, false otherwise
     */
    public static boolean hasMoreSizes(Product product) {
        return hasMoreAttributeValues(product, "size");
    }

    public static Money getShippingCost() {
        // TODO Implement correct shipping cost
        return new Money(BigDecimal.valueOf(10), "EUR");
    }

    /**
     * Returns
     *
     */
    public static Call getListProductsUrl(SearchResult<Product> search, Category category) {
        if (search.getCurrentPage() >= search.getTotalPages() - 1) {
            return null;
        }
        // Convert from 0..N-1 to 1..N
        int nextPage = search.getCurrentPage() + 2;
        String categorySlug = "";
        if (category != null) {
            categorySlug = category.getSlug();
        }
        return routes.Categories.listProducts(categorySlug, nextPage);
    }

    public static Call getCategoryUrl(Category category) {
        return getCategoryUrl(category, 1);
    }

    public static Call getCategoryUrl(Category category, int page) {
        return routes.Categories.select(getCategoryPath(category), page);
    }

    public static Call getProductUrl(Product product, Variant variant, Category category) {
        return routes.Products.select(product.getSlug(), String.valueOf(variant.getId()));
    }

    public static List<String> getPossibleSizes(Product product, Variant variant) {
        List<Variant> variants = getPossibleVariants(product, variant, "size");
        List<String> sizes = new ArrayList<String>();
        for (Variant matchedVariant : variants) {
            sizes.add(matchedVariant.getString("size"));
        }
        return sizes;
    }

    public static List<Variant> getPossibleVariants(Product product, Variant variant, String selectedAttribute) {
        List<Variant> matchingVariantList = new ArrayList<Variant>();
        List<Attribute> desiredAttributes = new ArrayList<Attribute>();
        for (Attribute attribute : variant.getAttributes()) {
            if (!selectedAttribute.equals(attribute.getName()) && hasMoreAttributeValues(product, attribute.getName())) {
                desiredAttributes.add(attribute);
            }
        }
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

	public static Map<String, List<PaymentNetwork>> groupPaymentNetworks(List<PaymentNetwork> networks) {
		Map<String, List<PaymentNetwork>> groups = new HashMap<String, List<PaymentNetwork>>();
		for (PaymentNetwork network : networks) {
			if (groups.containsKey(network.grouping)) {
				groups.get(network.grouping).add(network);
			} else {
				List<PaymentNetwork> group = new ArrayList<PaymentNetwork>();
				group.add(network);
				groups.put(network.grouping, group);
			}
		}
		return groups;
	}
}
