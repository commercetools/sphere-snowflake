import controllers.routes;
import io.sphere.client.filters.Filters;
import io.sphere.client.filters.expressions.FilterExpression;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import org.mockito.verification.VerificationMode;
import play.mvc.Result;
import utils.SphereTestable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;
import static utils.TestHelper.*;


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
        List<Category> categories = sphereTestable.mockCategory("cat", level);
        sphereTestable.mockCategoryTree(categories);
    }

    private void mockProductRequest(int numProducts, int page, int pageSize) {
        List<Product> products = new ArrayList<Product>();
        for (int i = 0 ; i < numProducts; i++) {
            products.add(sphereTestable.mockProduct("prod" + i+1, 1, 1, 1));
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
                assertUrlNotNull(GET, "/cat1");
            }
        });
    }

    @Test
    public void checkProductPagingUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?page=1");
            }
        });
    }

    @Test
    public void checkProductFilterByPriceUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?price=10_20");
            }
        });
    }

    @Test
    public void checkProductFilterByColorUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?color=black");
            }
        });
    }

    @Test
    public void showHome() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(5);
                Result result = callAction(routes.ref.Categories.home("", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
            }
        });
    }

    @Test
    public void selectCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(2);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat1", "", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 1);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check subcategories of category are listed
                // Check products from category are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
    }

    @Test
    public void showSubcategory() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                mockCategoryRequest(2);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat2", "", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 2);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products from category are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
    }

    @Test
    public void showInvalidCategory() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("non-existing-category", "", 1));
                assertNotFound(result);
            }
        });
    }

    @Test
    public void pagingProducts() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 1, 10);
                Result result = callAction(routes.ref.Categories.select("cat3", "", 2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 3);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products from page are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(5);
                // Check search is requesting correct page
                verify(sphereTestable.searchRequest).page(1);
            }
        });
    }

    @Test
    public void pagingProductsAboveRange() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 99, 10);
                Result result = callAction(routes.ref.Categories.select("cat3", "", 100));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 3);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products list is empty
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
                // Check informative message is displayed
                assertThat(body.select("#messages .alert-info").hasText()).isTrue();
                // Check search is requesting correct page
                verify(sphereTestable.searchRequest).page(99);
            }
        });
    }

    @Test
    public void pagingProductsInvalid() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, -3, 10);
                Result result = callAction(routes.ref.Categories.select("cat3", "", -2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 3);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products from page are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
                // Check search is requesting correct page
                verify(sphereTestable.searchRequest).page(0);
            }
        });
    }

    @Test
    public void filterProductsByPrice() {
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                String[] queryString = { "10_20" };
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat3", "", 1),
                        fakeRequest("GET", "?price=" + queryString[0]));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertValidBreadcrumb(body, 3);
                assertValidNavigationMenu(body, sphereTestable);
                assertValidMiniCart(body, sphereTestable);
                // Check products from page are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
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
        running(fakeApplication(noAuthConfig()), new Runnable() {
            public void run() {
                String[] queryString = { "1" };
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat3", "", 1),
                        fakeRequest("GET", "?codeFilterColor=" + queryString[0]));
                assertOK(result);
                Document body = contentAsDocument(result);
                // Check products from page are listed
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
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