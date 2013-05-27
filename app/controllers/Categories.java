package controllers;

import controllers.actions.SaveContext;
import io.sphere.client.facets.Facet;
import io.sphere.client.facets.Facets;
import io.sphere.client.facets.expressions.FacetExpression;
import io.sphere.client.filters.Filter;
import io.sphere.client.filters.Filters;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.filters.expressions.FilterExpressions;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import play.mvc.Result;
import play.mvc.With;
import sphere.ShopController;
import sphere.SearchRequest;
import views.html.categories;
import views.html.home;

import java.util.ArrayList;
import java.util.List;

public class Categories extends ShopController {

    public static int PAGE_SIZE = 50;

    @With(SaveContext.class)
    public static Result home(int page) {
        SearchRequest<Product> searchRequest = sphere().products.all();
        searchRequest = filterBy(searchRequest);
        searchRequest = sortBy(searchRequest);
        searchRequest = paging(searchRequest, page);
        SearchResult<Product> searchResult = searchRequest.fetch();
        return ok(home.render(searchResult));
    }

    @With(SaveContext.class)
    public static Result select(String categoryPath, int page) {
        String[] categorySlugs = categoryPath.split("/");
        String categorySlug = categorySlugs[categorySlugs.length - 1];
        Category category = sphere().categories.getBySlug(categorySlug);
        if (category == null) {
            return notFound("Category not found: " + categorySlug);
        }
        FilterExpression categoryFilter = new FilterExpressions.CategoriesOrSubcategories(category);
        SearchRequest <Product> searchRequest = sphere().products.filter(categoryFilter);
        searchRequest = filterBy(searchRequest);
        searchRequest = sortBy(searchRequest);
        searchRequest = paging(searchRequest, page);
        SearchResult<Product> searchResult = searchRequest.fetch();
        if (searchResult.getCount() < 1) {
            flash("info", "No products found");
        }
        return ok(categories.render(searchResult, category));
    }

    public static Result listProducts(String categorySlug, int page) {
        Category category = null;
        SearchRequest<Product> searchRequest;
        if (categorySlug.isEmpty()) {
            searchRequest = sphere().products.all();
        } else {
            category = sphere().categories.getBySlug(categorySlug);
            if (category == null) {
                return notFound("Category not found: " + categorySlug);
            }
            FilterExpression categoryFilter = new FilterExpressions.CategoriesOrSubcategories(category);
            searchRequest = sphere().products.filter(categoryFilter);
        }
        searchRequest = filterBy(searchRequest);
        searchRequest = sortBy(searchRequest);
        searchRequest = paging(searchRequest, page);
        SearchResult<Product> searchResult = searchRequest.fetch();
        return ok(views.html.ajax.listProducts.render(searchResult, category));
    }

    protected static SearchRequest<Product> filterBy(SearchRequest<Product> searchRequest) {
        // Filters
        List<Filter> filterList = new ArrayList<Filter>();
        // By price
        Filters.Price.DynamicRange filterPrice = new Filters.Price.DynamicRange().setQueryParam("price");
        filterList.add(filterPrice);
        // Build request
        List<FilterExpression> filterExp = bindFiltersFromRequest(filterList);
        searchRequest = searchRequest.filter(filterExp);

        // Facets
        List<Facet> facetList = new ArrayList<Facet>();
        // By color
        Facets.StringAttribute.Terms facetColor = new Facets.StringAttribute.Terms("variants.attributes.color").setQueryParam("color");
        facetList.add(facetColor);
        // Build request
        List<FacetExpression> facetExp = bindFacetsFromRequest(facetList);
        searchRequest = searchRequest.facet(facetExp);

        return searchRequest;
    }

    protected static SearchRequest<Product> sortBy(SearchRequest<Product> searchRequest) {
        return searchRequest;
    }

    protected static SearchRequest<Product> paging(SearchRequest<Product> searchRequest, int currentPage) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        // Convert page from 1..N to 0..N-1
        currentPage--;
        return searchRequest.page(currentPage).pageSize(PAGE_SIZE);
    }


}
