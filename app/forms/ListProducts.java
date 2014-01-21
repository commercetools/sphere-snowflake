package forms;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.*;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;

import static utils.ControllerHelper.saveFlash;
import static utils.ControllerHelper.saveJson;
import static utils.ViewHelper.*;

public class ListProducts {

    public ListProducts() {

    }

    public static void displaySuccessMessage(SearchResult<Product> search, Category category, String sort) {
        String message = search.getTotal() + " products found";
        saveFlash("total-products-found", message);

        ObjectNode json = Json.newObject();
        json.put("success", message);
        json.putAll(getJson(search, category, sort));

        saveJson(json);
    }

    public static ObjectNode getJson(SearchResult<Product> search, Category category, String sort) {
        ObjectNode json = Json.newObject();
        // Pager attributes
        json.put("offsetProducts", search.getOffset());
        json.put("countProducts", search.getCount());
        json.put("totalProducts", search.getTotal());
        json.put("currentPage", search.getCurrentPage());
        json.put("totalPages", search.getTotalPages());
        // Next page URL
        Call url = getProductListUrl(search, sort, category);
        if (url != null) {
            json.put("nextPageUrl", url.absoluteURL(Http.Context.current().request()));
        }
        // Product list
        ArrayNode products = json.putArray("product");
        int i = 0;
        for (Product product : search.getResults()) {
            products.add(getJson(product, category, i));
            i++;
        }
        return json;
    }


    public static ObjectNode getJson(Product product, Category category, int position) {
        Variant masterVariant = product.getMasterVariant();
        ObjectNode json = Json.newObject();
        json.put("id", product.getId());
        json.put("name", capitalizeInitials(product.getName()));
        json.put("slug", product.getSlug());
        json.put("description", product.getDescription());
        json.put("isFeatured", position > 5 && Math.random() > 0.9);
        json.put("variant", getJson(product, masterVariant, category));
        json.put("hasMoreColors", hasMoreColors(product));
        json.put("hasMoreSizes", hasMoreSizes(product));
        // Matching variants
        if (product.getVariants().size() > 1) {
            ArrayNode matchVariants = json.putArray("matchVariant");
            ObjectNode matchVariant;
            for (Variant match : getPossibleVariants(product, masterVariant, "color")) {
                matchVariant = getJson(product, match, category);
                matchVariant.put("isActive", masterVariant.getId() == match.getId());
                matchVariants.add(matchVariant);
            }
        }
        return json;
    }

    public static ObjectNode getJson(Product product, Variant variant, Category category) {
        if (variant.getPrice() == null) return null;
        ObjectNode json = Json.newObject();
        json.put("id", variant.getId());
        json.put("productId", product.getId());
        json.put("price", printPrice(getPrice(variant)));
        json.put("url", getProductUrl(product, variant, category).absoluteURL(Http.Context.current().request()));
        json.put("addCartUrl", controllers.routes.Carts.add().absoluteURL(Http.Context.current().request()));
        // Images
        ObjectNode images = Json.newObject();
        images.put("thumbnail", getJson(variant.getFeaturedImage(), ImageSize.THUMBNAIL));
        images.put("small", getJson(variant.getFeaturedImage(), ImageSize.SMALL));
        images.put("medium", getJson(variant.getFeaturedImage(), ImageSize.MEDIUM));
        images.put("large", getJson(variant.getFeaturedImage(), ImageSize.LARGE));
        images.put("original", getJson(variant.getFeaturedImage(), ImageSize.ORIGINAL));
        json.put("image", images);
        // Matching sizes
        ArrayNode sizes = json.putArray("size");
        for (Variant match : getPossibleVariants(product, variant, "size")) {
            sizes.add(match.getString("size"));
        }
        return json;
    }

    public static ObjectNode getJson(Image image, ImageSize size) {
        ObjectNode images = Json.newObject();
        if (!image.isSizeAvailable(size)) return null;
        ScaledImage scale = image.getSize(size);
        images.put("url", scale.getUrl());
        images.put("width", scale.getWidth());
        images.put("height", scale.getHeight());
        return images;
    }
}
