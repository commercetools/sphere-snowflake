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
import utils.ProductFilters;
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
        List<Category> categories = new ArrayList<Category>();
        SearchRequest<Product> searchRequest = sphere().products().all();
        if (!categorySlug.isEmpty()) {
            category = sphere().categories().getBySlug(categorySlug);
            categories.add(category);
        }
        searchRequest = filterBy(searchRequest, categories);
        searchRequest = sortBy(searchRequest, sort);
        searchRequest = paging(searchRequest, page);
        SearchResult<Product> searchResult = searchRequest.fetch();
        return ok(ListProducts.getJson(searchResult, category, sort));
    }

    protected static SearchRequest<Product> filterBy(SearchRequest<Product> searchRequest, List<Category> categories) {
        // Filter by category
        searchRequest = searchRequest.filter(
                new FilterExpressions.CategoriesOrSubcategories(categories));
        // Filter by request parameters
        searchRequest = searchRequest.filter(bindFiltersFromRequest(ProductFilters.filters));
        // Facet by request parameters
        searchRequest = searchRequest.facet(bindFacetsFromRequest(ProductFilters.facets));
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
