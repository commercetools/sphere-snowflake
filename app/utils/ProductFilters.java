package utils;

import io.sphere.client.facets.Facet;
import io.sphere.client.facets.Facets;
import io.sphere.client.filters.Filter;
import io.sphere.client.filters.Filters;

import java.util.Arrays;
import java.util.List;

public class ProductFilters {

    /* FILTERS */

    // Fulltext search
    public static final Filters.Fulltext fulltextSearch =
            new Filters.Fulltext().setQueryParam("q");

    // Filter price
    public static final Filters.Price.DynamicRange filterPrice =
            new Filters.Price.DynamicRange().setQueryParam("price");

    // A special collection for filters that checks parameter name conflict
    public static final List<Filter> filters = Arrays.<Filter>asList(
            filterPrice,
            fulltextSearch
    );


    /* FACETS */

    // Facet color
    public static final Facets.StringAttribute.Terms facetColor =
            new Facets.StringAttribute.Terms("variants.attributes.color").setQueryParam("color");

    // A special collection for facets that does parameter name conflict checking
    public static final List<Facet> facets = Arrays.<Facet>asList(
            facetColor
    );
}
