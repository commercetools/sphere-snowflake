package controllers;

import controllers.actions.SaveContext;
import forms.ListProducts;
import io.sphere.client.ProductSort;
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
import java.util.Collections;
import java.util.List;

public class Categories extends ShopController {

    public static int PAGE_SIZE = 15;

    @With(SaveContext.class)
    public static Result home(String sort, int page) {
        return ok(home.render(page, sort));
    }

    @With(SaveContext.class)
    public static Result select(String categorySlug, String sort, int page) {
        Category category = sphere().categories().getBySlug(categorySlug);
        if (category == null) {
            flash("error", "Category not found");
            return notFound();
        }
        return ok(categories.render(page, sort, category));
    }

    public static Result listProducts(String categorySlug, String sort, int page) {
        Category category = null;
        SearchRequest<Product> searchRequest;
        if (categorySlug.isEmpty()) {
            searchRequest = sphere().products().all();
        } else {
            category = sphere().categories().getBySlug(categorySlug);
            if (category == null) {
                return notFound("Category not found: " + categorySlug);
            }
            FilterExpression categoryFilter =
                    new FilterExpressions.CategoriesOrSubcategories(Collections.singletonList(category));
            searchRequest = sphere().products().filter(categoryFilter);
        }
        searchRequest = filterBy(searchRequest);
        searchRequest = sortBy(searchRequest, sort);
        searchRequest = paging(searchRequest, page);
        SearchResult<Product> searchResult = searchRequest.fetch();
        return ok(ListProducts.getJson(searchResult, category, sort));
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

    protected static SearchRequest<Product> sortBy(SearchRequest<Product> searchRequest, String sort) {
        if (sort.equals("price_asc")) {
            searchRequest = searchRequest.sort(ProductSort.price.asc);
        } else if (sort.equals("price_desc")) {
            searchRequest = searchRequest.sort(ProductSort.price.desc);
        } else if (sort.equals("name_asc")) {
            searchRequest = searchRequest.sort(ProductSort.name.asc);
        } else if (sort.equals("name_desc")) {
            searchRequest = searchRequest.sort(ProductSort.name.desc);
        }
        return searchRequest;
    }

    protected static SearchRequest<Product> paging(SearchRequest<Product> searchRequest, int currentPage) {
        if (currentPage < 1) currentPage = 1;
        // Convert page from 1..N to 0..N-1
        currentPage--;
        return searchRequest.page(currentPage).pageSize(PAGE_SIZE);
    }


}
