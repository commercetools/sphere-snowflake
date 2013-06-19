package utils;

import com.google.common.base.Optional;
import com.neovisionaries.i18n.CountryCode;
import io.sphere.client.model.VersionedId;
import org.joda.time.DateTime;
import sphere.*;
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

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

public class SphereTestable {

    private Sphere sphere;
    public String currency = "EUR";
    public CountryCode country = CountryCode.DE;

    public SphereTestable() {
        sphere = mock(Sphere.class);

        mockCategoryTree(Collections.<Category>emptyList());
        mockProductService(Collections.<Product>emptyList(), 0, 100);
        mockCurrentCart(Collections.<LineItem>emptyList());

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
        when(currentCart.addLineItem(Mockito.anyString(), Mockito.anyInt())).thenReturn(cart);
        when(currentCart.addLineItem(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(cart);
        // Mock remove item
        when(currentCart.removeLineItem(Mockito.anyString())).thenReturn(cart);
        // Mock update cart
        when(currentCart.update(Mockito.any(CartUpdate.class))).thenReturn(cart);

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
            if (c.isRoot()) roots.add(c);
        }
        // Mock get roots
        when(categoryTree.getRoots()).thenReturn(roots);

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
        when(productService.filter(Mockito.any(FilterExpression.class))).thenReturn(searchRequest);
        when(productService.filter(Mockito.any(Iterable.class))).thenReturn(searchRequest);
        // Mock get by id/slug
        //when(productService.byId(Mockito.anyString())).thenReturn(mockFetchRequest(null));
        //when(productService.bySlug(Mockito.anyString())).thenReturn(mockFetchRequest(null));
        for (Product p : products) {
            FetchRequest<Product> fetchRequest = mockFetchRequest(p);
            when(productService.byId(p.getId())).thenReturn(fetchRequest);
            when(productService.bySlug(p.getSlug())).thenReturn(fetchRequest);
        }

        when(sphere.products()).thenReturn(productService);
        setSphereInstance(sphere);
    }

    public List<Category> mockCategory(String name, int level) {
        List<Category> categories = new ArrayList<Category>();
        List<Category> tree = new ArrayList<Category>();
        for (int i = 0; i < level - 1; ++i) {
            Category ancestor = mockCategoryNode(name, tree);
            categories.add(ancestor);
            tree.add(ancestor);
        }
        categories.add(mockCategoryNode(name, tree));
        return categories;
    }

    public Product mockProduct(String name, int numVariants, int numAttributes, int numImages) {
        Product product = mock(Product.class);

        // Mock id/name/slug
        when(product.getId()).thenReturn(name);
        when(product.getIdAndVersion()).thenReturn(VersionedId.create(name, 1));
        when(product.getSlug()).thenReturn(name);
        when(product.getName()).thenReturn(name);
        // Mock price
        when(product.getPrice()).thenReturn(mockPrice(10));
        // Mock image
        Image image = mockImage();
        when(product.getFeaturedImage()).thenReturn(image);
        when(product.getImages()).thenReturn(Collections.nCopies(numImages, image));
        // Mock attribute
        Attribute attribute = mockAttribute("attrName", "attrValue");
        when(product.get(Mockito.anyString())).thenReturn(attribute);
        when(product.getString(Mockito.anyString())).thenReturn("attrValue");
        when(product.getInt(Mockito.anyString())).thenReturn(5);
        when(product.getMoney(Mockito.anyString())).thenReturn(mockMoney(5));
        when(product.getDouble(Mockito.anyString())).thenReturn(5.0);
        when(product.getAttribute(Mockito.anyString())).thenReturn(attribute);
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

    public Variant mockVariant(int id) {
        Variant variant = mock(Variant.class);

        // Mock get id
        when(variant.getId()).thenReturn(id);
        // Mock get attributes
        when(variant.get(Mockito.anyString())).thenReturn("attrValue");
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
        int total = products.size();
        int offset = page * pageSize;
        int count = Math.min(total - offset, pageSize);
        List<Product> result = new ArrayList<Product>();
        if (count > 0) result = products.subList(0, count);
        Map<String, FacetResult> facetResult = Collections.<String, FacetResult>emptyMap();
        SearchResult<Product> searchResult = new SearchResult<Product>(offset, count, total, result, facetResult, pageSize);
        when(request.fetch()).thenReturn(searchResult);

        return request;
    }

    @SuppressWarnings("unchecked")
    private FetchRequest<Product> mockFetchRequest(Product product) {
        FetchRequest<Product> request = mock(FetchRequest.class);

        // Mock request expand
        when(request.expand((String)anyVararg())).thenReturn(request);
        // Mock request fetch
        when(request.fetch()).thenReturn(Optional.of(product));

        return request;
    }

    private Image mockImage() {
        return new Image("image.png", "imageLabel", new Dimensions(1000, 1000));
    }

    private Attribute mockAttribute(String name, String value) {
        return new Attribute(name, value);
    }

    private Money mockMoney(double amount) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    private Price mockPrice(double amount) {
        Price price = mock(Price.class);
        /*
        // Mock value/string
        Money money = mockMoney(amount);
        // TODO For some reason it is failing
        //when(price.getValue()).thenReturn(money);
        when(price.toString()).thenReturn(String.valueOf(amount));
        // Mock country
        when(price.getCountry()).thenReturn(country);
        when(price.getCountryString()).thenReturn(country.getName());
         */
        return price;
    }

    private Category mockCategoryNode(String name, List<Category> ancestors) {
        Category category = mock(Category.class);

        int level = ancestors.size() + 1;
        // Mock id/name/slug
        when(category.getId()).thenReturn(name + level);
        when(category.getName()).thenReturn(name + level);
        when(category.getSlug()).thenReturn(name + level);
        // Mock description
        when(category.getDescription()).thenReturn("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        // Mock parent
        Category parent = null;
        if (level > 1) {
            parent = ancestors.get(ancestors.size() - 1);
        }
        when(category.getParent()).thenReturn(parent);
        // Mock root
        when(category.isRoot()).thenReturn(level < 2);
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
