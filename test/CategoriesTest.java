import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.filters.expressions.FilterExpressions;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Result;
import utils.SphereTestable;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;
import static utils.SphereTestable.mockCategory;
import static utils.SphereTestable.mockProduct;
import static utils.TestHelper.*;
import static utils.TestHelper.contentAsDocument;


public class CategoriesTest {

    private SphereTestable sphereTestable;

    @Before
    public void mockSphere() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                sphereTestable = new SphereTestable();
            }
        });
    }

    private void mockCategoryRequest(int level) {
        List<Category> categories = mockCategory("cat", level);
        sphereTestable.mockCategoryTree(categories);
    }

    private void mockProductRequest(int numProducts, int page, int pageSize) {
        List<Product> products = new ArrayList<Product>();
        for (int i = 0 ; i < numProducts; i++) {
            products.add(mockProduct("prod" + i + 1, 1, 1, 1));
        }
        sphereTestable.mockProductService(products, page, pageSize);
    }

    @Test
    public void checkHomeUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/");
            }
        });
    }

    @Test
    public void checkSelectCategoryUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1Slug");
            }
        });
    }

    @Test
    public void checkProductPagingUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1Slug", "?page=1");
            }
        });
    }

    @Test
    public void checkProductFilterByPriceUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1Slug", "?price=10_20");
            }
        });
    }

    @Test
    public void checkProductFilterByColorUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1Slug", "?color=black");
            }
        });
    }

    @Test
    public void showHome() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(5);
                Result result = callAction(routes.ref.Categories.home("", 1));
                assertOK(result, HTML_CONTENT);
                Document body = contentAsDocument(result);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
            }
        });
    }

    @Test
    public void selectCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(2);
                Result result = callAction(routes.ref.Categories.select("cat1Slug", "", 1));
                assertOK(result, HTML_CONTENT);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 1);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
            }
        });
    }

    @Test
    public void showSubcategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(2);
                Result result = callAction(routes.ref.Categories.select("cat2Slug", "", 1));
                assertOK(result, HTML_CONTENT);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 2);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products from category are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
            }
        });
    }

    @Test
    public void showInvalidCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("non-existing-category", "", 1));
                assertNotFound(result);
            }
        });
    }

    @Test
    public void pagingProducts() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                List<Category> categories = mockCategory("cat", 1);
                Category cat = categories.get(0);
                mockProductRequest(15, 1, 10);
                Result result = callAction(
                        routes.ref.Categories.listProducts(cat.getSlug(), "", 2));
                assertOK(result, JSON_CONTENT);
                JsonNode data = contentAsJson(result);
                assertThat(data.get("product").size()).isEqualTo(5);
                FilterExpression expression = new FilterExpressions
                        .CategoriesOrSubcategories(categories);
                //verify(sphereTestable.searchRequest).filter(expression);
                verify(sphereTestable.searchRequest).page(1);
            }
        });
    }

    @Test
    public void pagingProductsAboveRange() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockProductRequest(15, 99, 10);
                Result result = callAction(routes.ref.Categories.listProducts("catSlug", "", 100));
                assertOK(result, JSON_CONTENT);
                JsonNode data = contentAsJson(result);
                // Check products list is empty
                assertThat(data.get("product").size()).isEqualTo(0);
                // Check informative message is displayed
                // Check search is requesting correct page
                verify(sphereTestable.searchRequest).page(99);
            }
        });
    }

    @Test
    public void pagingProductsInvalid() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockProductRequest(15, -3, 10);
                Result result = callAction(routes.ref.Categories.listProducts("catSlug", "", -2));
                assertOK(result, JSON_CONTENT);
                JsonNode data = contentAsJson(result);
                // Check products from page are listed
                assertThat(data.get("product").size()).isEqualTo(10);
                // Check search is requesting correct page
                verify(sphereTestable.searchRequest).page(0);
            }
        });
    }

    @Test
    public void filterProductsByPrice() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                String[] queryString = { "10_20" };
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.listProducts("catSlug", "", 1),
                        fakeRequest("GET", "?price=" + queryString[0]));
                assertOK(result, JSON_CONTENT);
                JsonNode data = contentAsJson(result);
                // Check products from page are listed
                assertThat(data.get("product").size()).isEqualTo(10);
                // TODO Check price range is correctly displayed
                // TODO Implement equals methods in Filters to verify search request
                //List<FilterExpression> filters = new ArrayList<FilterExpression>();
                //filters.add(new Filters.Price.DynamicRange().parse(Collections.singletonMap("price", queryString)));
                //filters.add(new Filters.Fulltext().parse(Collections.singletonMap("q", queryString)));
                //verify(sphereTestable.searchRequest).filter(filters);
            }
        });
    }

    @Test
    public void filterProductsByColor() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                String[] queryString = { "1" };
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.listProducts("catSlug", "", 1),
                        fakeRequest("GET", "?codeFilterColor=" + queryString[0]));
                assertOK(result, JSON_CONTENT);
                JsonNode data = contentAsJson(result);
                // Check products from page are listed
                assertThat(data.get("product").size()).isEqualTo(10);
                // TODO Check color filter is correctly displayed
                //assertThat(body.select(".color-filter > .facet > .item a.selected")).isNotEmpty();
                // TODO Implement equals methods in Filters to verify search request
                //List<FilterExpression> filters = new ArrayList<FilterExpression>();
                //filters.add(new Filters.Price.DynamicRange().parse(Collections.singletonMap("codeFilterColor", queryString)));
                //filters.add(new Filters.Fulltext().parse(Collections.singletonMap("q", queryString)));
                //verify(sphereTestable.searchRequest).filter(filters);
            }
        });
    }
}