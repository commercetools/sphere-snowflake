package utils;

import static io.sphere.internal.filters.DynamicFilterHelpers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.sphere.client.ProductSort;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.model.Money;
import io.sphere.client.model.SearchResult;
import io.sphere.client.model.VersionedId;
import io.sphere.client.model.facets.FacetResult;
import io.sphere.client.model.facets.RangeFacetItem;
import io.sphere.client.model.facets.RangeFacetResultRaw;
import io.sphere.client.shop.CategoryTree;
import io.sphere.client.shop.model.Attribute;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.CartUpdate;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Dimensions;
import io.sphere.client.shop.model.Image;
import io.sphere.client.shop.model.LineItem;
import io.sphere.client.shop.model.Price;
import io.sphere.client.shop.model.Product;
import io.sphere.client.shop.model.Variant;
import io.sphere.client.shop.model.VariantList;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import org.joda.time.DateTime;

import sphere.CurrentCart;
import sphere.FetchRequest;
import sphere.ProductService;
import sphere.SearchRequest;
import sphere.Sphere;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;

public class SphereTestable {

    private final Sphere sphere;
    public SearchRequest searchRequest;
    public CategoryTree categoryTree;
    public CurrentCart currentCart;

    public static String currency = "EUR";
    public static CountryCode country = CountryCode.DE;
    public static Locale locale = Locale.ENGLISH;

    public SphereTestable() {
        sphere = mock(Sphere.class);

        mockCategoryTree(Collections.<Category> emptyList());
        mockProductService(Collections.<Product> emptyList(), 0, 100);
        mockCurrentCart(Collections.<LineItem> emptyList());

        setSphereInstance(sphere);
    }

    public void mockCurrentCart(List<LineItem> items) {
        CurrentCart currentCart = mock(CurrentCart.class);

        // Mock fetch
        Cart cart = mockCart(items);
        when(currentCart.fetch()).thenReturn(cart);
        // Mock quantity
        when(currentCart.getQuantity()).thenReturn(items.size());
        // Mock add item
        when(currentCart.addLineItem(anyString(), anyInt())).thenReturn(cart);
        when(currentCart.addLineItem(anyString(), anyInt(), anyInt())).thenReturn(cart);
        // Mock remove item
        when(currentCart.removeLineItem(anyString())).thenReturn(cart);
        // Mock update cart
        when(currentCart.update(any(CartUpdate.class))).thenReturn(cart);
        this.currentCart = currentCart;

        when(sphere.currentCart()).thenReturn(currentCart);
        setSphereInstance(sphere);
    }

    public void mockCategoryTree(List<Category> categories) {
        CategoryTree categoryTree = mock(CategoryTree.class);

        // Mock get list
        when(categoryTree.getAsFlatList()).thenReturn(categories);
        // Mock get by id/slug
        List<Category> roots = new ArrayList<Category>();
        for (Category c : categories) {
            when(categoryTree.getBySlug(c.getSlug())).thenReturn(c);
            when(categoryTree.getById(c.getId())).thenReturn(c);
            if (c.isRoot())
                roots.add(c);
        }
        // Mock get roots
        when(categoryTree.getRoots()).thenReturn(roots);
        this.categoryTree = categoryTree;

        when(sphere.categories()).thenReturn(categoryTree);
        setSphereInstance(sphere);
    }

    @SuppressWarnings("unchecked")
    public void mockProductService(List<Product> products, int page, int pageSize) {
        ProductService productService = mock(ProductService.class);
        // Mock get all
        SearchRequest<Product> searchRequest = mockSearchRequest(products, page, pageSize);
        when(productService.all()).thenReturn(searchRequest);
        // Mock filter
        when(productService.filter(any(FilterExpression.class))).thenReturn(searchRequest);
        when(productService.filter(any(Iterable.class))).thenReturn(searchRequest);
        // Mock get by id/slug
        // when(productService.byId(anyString())).thenReturn(mockFetchRequest(null));
        // when(productService.bySlug(anyString())).thenReturn(mockFetchRequest(null));
        for (Product p : products) {
            FetchRequest<Product> fetchRequest = mockFetchRequest(p);
            when(productService.byId(p.getId())).thenReturn(fetchRequest);
            when(productService.bySlug(p.getSlug())).thenReturn(fetchRequest);
        }
        when(sphere.products()).thenReturn(productService);
        setSphereInstance(sphere);
    }

    public static List<Category> mockCategory(String name, int level) {
        List<Category> categories = new ArrayList<Category>();
        List<Category> tree = new ArrayList<Category>();
        for (int i = 0; i < level; ++i) {
            Category category = mockCategoryNode(name, tree);
            categories.add(category);
            if (!tree.isEmpty()) {
                when(tree.get(tree.size() - 1).getChildren()).thenReturn(Collections.singletonList(category));
            }
            tree.add(category);
        }
        return categories;
    }

    public static Product mockProduct(String name, int numVariants, int numAttributes, int numImages) {
        Product product = mock(Product.class);

        // Mock id/name/slug
        when(product.getId()).thenReturn(name+"Id");
        when(product.getIdAndVersion()).thenReturn(VersionedId.create(name, 1));
        when(product.getSlug()).thenReturn(name+"Slug");
        when(product.getName()).thenReturn(name);
        // Mock price
        when(product.getPrice()).thenReturn(mockPrice(10));
        // Mock image
        Image image = mockImage();
        when(product.getFeaturedImage()).thenReturn(image);
        when(product.getImages()).thenReturn(Collections.nCopies(numImages, image));
        // Mock attribute
        Attribute attribute = mockAttribute("attrName", "attrValue");
        when(product.get(anyString())).thenReturn(attribute);
        when(product.getString(anyString())).thenReturn("attrValue");
        when(product.getInt(anyString())).thenReturn(5);
        when(product.getMoney(anyString())).thenReturn(mockMoney(5));
        when(product.getDouble(anyString())).thenReturn(5.0);
        when(product.getAttribute(anyString())).thenReturn(attribute);
        when(product.getAttributes()).thenReturn(Collections.nCopies(numAttributes, attribute));
        // Mock variants
        List<Variant> variantList = new ArrayList<Variant>();
        for (int i = 0; i < numVariants + 1; ++i) {
            variantList.add(mockVariant(i));
        }
        when(product.getVariants()).thenReturn(new VariantList(variantList));
        // Mock master variant
        when(product.getMasterVariant()).thenReturn(variantList.get(0));

        return product;
    }

    public static Variant mockVariant(int id) {
        Variant variant = mock(Variant.class);

        // Mock get id
        when(variant.getId()).thenReturn(id);
        // Mock get attributes
        when(variant.get(anyString())).thenReturn("attrValue");
        // Mock get price
        when(variant.getPrice()).thenReturn(mockPrice(10));
        // Mock get images
        Image image = mockImage();
        when(variant.getFeaturedImage()).thenReturn(image);
        when(variant.getImages()).thenReturn(Collections.singletonList(image));

        return variant;
    }

    public Cart mockCart(List<LineItem> items) {
        Cart cart = mock(Cart.class);

        // Mock id/version
        String id = UUID.randomUUID().toString();
        when(cart.getId()).thenReturn(id);
        when(cart.getIdAndVersion()).thenReturn(VersionedId.create(id, 1));
        // Mock state/inventory
        when(cart.getCartState()).thenReturn(Cart.CartState.Active);
        when(cart.getInventoryMode()).thenReturn(Cart.InventoryMode.None);
        // Mock country/currency
        when(cart.getCountry()).thenReturn(country);
        when(cart.getCurrency()).thenReturn(Currency.getInstance(currency));
        // Mock line items
        when(cart.getLineItems()).thenReturn(items);
        // Mock total price
        double amount = 0;
        for (LineItem item : items) {
            amount += item.getTotalPrice().getAmount().doubleValue();
        }
        when(cart.getTotalPrice()).thenReturn(mockMoney(amount));
        // Mock quantity
        when(cart.getTotalQuantity()).thenReturn(items.size());
        // Mock time
        when(cart.getLastModifiedAt()).thenReturn(DateTime.now());
        when(cart.getCreatedAt()).thenReturn(DateTime.now());

        return cart;
    }

    public LineItem mockLineItem(String name, int quantity) {
        LineItem item = mock(LineItem.class);

        // Mock id/name
        when(item.getId()).thenReturn(name);
        when(item.getProductId()).thenReturn(name);
        when(item.getProductName()).thenReturn(name);
        // Mock variant
        when(item.getVariant()).thenReturn(mockVariant(1));
        // Mock quantity
        when(item.getQuantity()).thenReturn(quantity);
        // Mock price
        double amount = 10;
        when(item.getPrice()).thenReturn(mockPrice(amount));
        when(item.getTotalPrice()).thenReturn(mockMoney(amount * quantity));

        return item;
    }

    @SuppressWarnings("unchecked")
    private SearchRequest<Product> mockSearchRequest(List<Product> products, int page, int pageSize) {
        SearchRequest<Product> request = mock(SearchRequest.class);

        // Mock request filter
        when(request.filter(any(FilterExpression.class))).thenReturn(request);
        when(request.filter(any(Iterable.class))).thenReturn(request);
        // Mock request facet
        when(request.facet(any(FacetExpression.class))).thenReturn(request);
        when(request.facet(any(Collection.class))).thenReturn(request);
        // Mock request sort
        when(request.sort(any(ProductSort.class))).thenReturn(request);
        // Mock request paging
        when(request.page(anyInt())).thenReturn(request);
        when(request.pageSize(anyInt())).thenReturn(request);
        // Mock request fetch
        int total = products.size();
        int offset = page * pageSize;
        int count = Math.min(total - offset, pageSize);
        List<Product> result = new ArrayList<Product>();
        if (count > 0)
            result = products.subList(0, count);
        // Mock facet results
        Map<String, FacetResult> facetResult = new HashMap<String, FacetResult>();
        facetResult.put(PriceRangeFilterExpression.helperFacetAlias, mockFacetRangeResult(300, 9000));
        SearchResult<Product> searchResult = new SearchResult<Product>(offset, count, total, result, facetResult, pageSize);
        when(request.fetch()).thenReturn(searchResult);

        this.searchRequest = request;
        return request;
    }

    private FacetResult mockFacetRangeResult(double min, double max) {
        RangeFacetItem rangeItem = mock(RangeFacetItem.class);
        when(rangeItem.getMin()).thenReturn(min);
        when(rangeItem.getMax()).thenReturn(max);
        return new RangeFacetResultRaw(Collections.singletonList(rangeItem));
    }

    @SuppressWarnings("unchecked")
    private FetchRequest<Product> mockFetchRequest(Product product) {
        FetchRequest<Product> request = mock(FetchRequest.class);

        // Mock request expand
        when(request.expand((String) anyVararg())).thenReturn(request);
        // Mock request fetch
        when(request.fetch()).thenReturn(Optional.of(product));

        return request;
    }

    private static Image mockImage() {
        return new Image("image.png", "imageLabel", new Dimensions(1000, 1000));
    }

    private static Attribute mockAttribute(String name, String value) {
        return new Attribute(name, value);
    }

    private static Money mockMoney(double amount) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    private static Price mockPrice(double amount) {
        return new Price(mockMoney(amount), country, null);
    }

    private static Category mockCategoryNode(String prefix, List<Category> ancestors) {
        Category category = mock(Category.class);

        int level = ancestors.size() + 1;
        when(category.getId()).thenReturn(prefix+level+"Id");
        when(category.getName()).thenReturn(prefix+level+"Name");
        when(category.getSlug()).thenReturn(prefix+level+"Slug");
        when(category.getDescription()).thenReturn(prefix+level+"Description");
        // Mock root
        when(category.isRoot()).thenReturn(level < 2);
        // Mock parent
        Category parent = null;
        if (level > 1) {
            parent = ancestors.get(ancestors.size() - 1);
        }
        when(category.getParent()).thenReturn(parent);
        // Mock category tree
        List<Category> categoryTree = new ArrayList<Category>(ancestors);
        when(category.getPathInTree()).thenReturn(categoryTree);
        categoryTree.add(category);

        return category;
    }

    private static void setSphereInstance(Sphere sphere) {
        try {
            Field field = Sphere.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, sphere);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}