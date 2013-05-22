package utils;

import io.sphere.client.shop.SphereClient;
import sphere.Sphere;
import sphere.CurrentCart;
import sphere.SearchRequest;
import sphere.ProductService;
import io.sphere.client.ProductSort;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.model.Money;
import io.sphere.client.model.SearchResult;
import io.sphere.client.model.facets.FacetResult;
import io.sphere.client.shop.CategoryTree;
import io.sphere.client.shop.model.*;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockHelper {

    public static int PAGE = 0;
    public static int PAGE_SIZE = 100;

    public static Sphere mockSphere(String categoryName, int categoryLevel, String productName, int numProducts) {
        CategoryTree categories = mockCategories(categoryName, categoryLevel);
        ProductService products = mockProducts(productName, numProducts, PAGE, PAGE_SIZE);
        // Mock sphere client
        Sphere sphere = mock(Sphere.class);
        CurrentCart cart = mockCurrentCart();
        when(sphere.currentCart()).thenReturn(cart);
        // TODO Change when categories and products have corresponding getters
        //when(sphere.categories).thenReturn(categories);
        //when(sphere.products).thenReturn(products);
        when(Sphere.getInstance()).thenReturn(sphere);
        return sphere;
    }

    public static Category mockCategory(String name, int level) {
        // Mock get category tree
        List<Category> categoryTree = new ArrayList<Category>();
        for (int i = 0; i < level - 1; ++i) {
            Category ancestor = _mockCategory(name, categoryTree);
            categoryTree.add(ancestor);
        }
        return _mockCategory(name, categoryTree);
    }

    private static Category _mockCategory(String name, List<Category> ancestors) {
        Category category = mock(Category.class);
        int level = ancestors.size() + 1;
        // Mock get slug
        when(category.getSlug()).thenReturn(name + level);
        when(category.getId()).thenReturn(name + level);
        // Mock get parent
        Category parent = null;
        if (level > 1) {
            parent = ancestors.get(ancestors.size() - 1);
        }
        when(category.getParent()).thenReturn(parent);
        // Mock get root
        when(category.isRoot()).thenReturn(level < 2);
        // Mock get category tree
        List<Category> categoryTree = new ArrayList<Category>(ancestors);
        when(category.getPathInTree()).thenReturn(categoryTree);
        categoryTree.add(category);
        return category;
    }

    public static Product mockProduct(String name, int numVariants, int numAttributes, int numImages) {
        Product product = mock(Product.class);
        // Mock get slug
        when(product.getSlug()).thenReturn(name);
        // Mock get name
        when(product.getName()).thenReturn(name);
        // Mock get price
        Price price = mockPrice(10);
        when(product.getPrice()).thenReturn(price);
        // Mock get image
        Image image = mockImage();
        when(product.getFeaturedImage()).thenReturn(image);
        when(product.getImages()).thenReturn(Collections.nCopies(numImages, image));
        // Mock get attribute
        Attribute attribute = mockAttribute("attrName", "attrValue");
        when(product.get(Mockito.anyString())).thenReturn(attribute);
        when(product.getAttribute(Mockito.anyString())).thenReturn(attribute);
        when(product.getAttributes()).thenReturn(Collections.nCopies(numAttributes, attribute));
        // Mock get variants
        List<Variant> variantList = new ArrayList<Variant>();
        for (int i = 0; i < numVariants + 1; ++i) {
            variantList.add(mockVariant(i));
        }
        when(product.getVariants()).thenReturn(new VariantList(variantList));
        // Mock get master variant
        when(product.getMasterVariant()).thenReturn(variantList.get(0));
        return product;
    }

    public static Variant mockVariant(int id) {
        Variant variant = mock(Variant.class);
        // Mock get id
        when(variant.getId()).thenReturn(id);
        // Mock get attributes
        when(variant.get(Mockito.anyString())).thenReturn("attr");
        // Mock get price
        Price price = mockPrice(10);
        when(variant.getPrice()).thenReturn(price);
        // Mock get images
        Image image = mockImage();
        when(variant.getFeaturedImage()).thenReturn(image);
        when(variant.getImages()).thenReturn(Collections.singletonList(image));
        return variant;
    }

    private static Image mockImage() {
        return new Image("image.png", "imageLabel", new Dimensions(10, 10));
    }

    private static Attribute mockAttribute(String name, String value) {
        return new Attribute(name, value);
    }

    private static Price mockPrice(double amount) {
        Price price = mock(Price.class);
        when(price.getValue()).thenReturn(new Money(BigDecimal.valueOf(amount), "EUR"));
        return price;
    }

    private static CategoryTree mockCategories(String categoryName, int categoryLevel) {
        CategoryTree categories = mock(CategoryTree.class);
        // Mock get by slug
        Category leaf = mockCategory(categoryName, categoryLevel);
        for (Category category : leaf.getPathInTree()) {
            when(categories.getBySlug(category.getSlug())).thenReturn(category);
        }
        return categories;
    }

    @SuppressWarnings("unchecked")
    private static ProductService mockProducts(String productName, int numProducts, int page, int pageSize) {
        ProductService products = mock(ProductService.class);
        SearchRequest<Product> request = mock(SearchRequest.class);
        // Mock request filter
        when(request.filter(Mockito.any(FilterExpression.class))).thenReturn(request);
        when(request.filter(Mockito.any(Iterable.class))).thenReturn(request);
        // Mock request facet
        when(request.facet(Mockito.any(FacetExpression.class))).thenReturn(request);
        when(request.facet(Mockito.any(Collection.class))).thenReturn(request);
        // Mock request sort
        when(request.sort(Mockito.any(ProductSort.class))).thenReturn(request);
        // Mock request paging
        when(request.page(Mockito.anyInt())).thenReturn(request);
        when(request.pageSize(Mockito.anyInt())).thenReturn(request);
        // Mock request fetch
        List<Product> productList = new ArrayList<Product>();
        for (int i = 0; i < numProducts; ++i) {
            productList.add(mockProduct(productName + i, 1, 1, 1));
        }
        int offset = page * pageSize;
        int count = Math.min((numProducts - offset) % pageSize, pageSize);
        SearchResult<Product> result = new SearchResult<Product>(offset, count, numProducts, productList, Collections.<String, FacetResult>emptyMap(), pageSize);
        when(request.fetch()).thenReturn(result);
        // Mock filter
        when(products.filter(Mockito.any(FilterExpression.class))).thenReturn(request);
        when(products.filter(Mockito.any(Iterable.class))).thenReturn(request);
        return products;
    }

    private static CurrentCart mockCurrentCart() {
        CurrentCart currentCart = mock(CurrentCart.class);
        Cart cart = mock(Cart.class);
        when(cart.getCartState()).thenReturn(Cart.CartState.Active);
        when(cart.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(currentCart.fetch()).thenReturn(cart);
        return currentCart;
    }
}
