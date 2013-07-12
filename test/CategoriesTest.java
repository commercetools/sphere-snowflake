import controllers.routes;
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
                assertUrlNotNull(GET, "/cat1/cat2");
                assertUrlNotNull(GET, "/cat1-cat2/cat3");
            }
        });
	}

    @Test
    public void checkProductPagingUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?page=1");
                assertUrlNotNull(GET, "/cat1/cat2", "?page=1");
                assertUrlNotNull(GET, "/cat1-cat2/cat3", "?page=1");
            }
        });
    }

	@Test
	public void checkProductFilterByPriceUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?price=10_20");
                assertUrlNotNull(GET, "/cat1/cat2", "?price=10_20");
                assertUrlNotNull(GET, "/cat1-cat2/cat3", "?price=10_20");
            }
        });
	}

	@Test
	public void checkProductFilterByColorUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat1", "?color=black");
                assertUrlNotNull(GET, "/cat1/cat2", "?color=black");
                assertUrlNotNull(GET, "/cat1-cat2/cat3", "?color=black");
            }
        });
	}

    @Test
    public void showHome() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.home(1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
    }

	@Test
	public void selectCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(1);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("", "cat1", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
	}

	@Test
	public void showSubcategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat1-cat2/", "cat3", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
	}

    @Test
	public void showInvalidCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("", "non-existing-category", 1));
                assertNotFound(result);
            }
        });
	}

	@Test
	public void showCategoryWithInvalidParent() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("non-existing-category/" , "cat3", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
            }
        });
	}

	@Test
	public void pagingProducts() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 1, 10);
                Result result = callAction(routes.ref.Categories.select("cat1-cat2/", "cat3", 2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(5);
                verify(sphereTestable.searchRequest).page(1);
            }
        });
	}

	@Test
	public void pagingProductsAboveRange() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 99, 10);
                Result result = callAction(routes.ref.Categories.select("cat1/", "cat2", 100));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
                assertThat(body.select("#messages .alert-info").hasText()).isTrue();
                verify(sphereTestable.searchRequest).page(99);
            }
        });
	}

	@Test
	public void pagingProductsInvalid() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, -3, 10);
                Result result = callAction(routes.ref.Categories.select("cat1/", "cat2", -2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
                verify(sphereTestable.searchRequest).page(0);
            }
        });
	}

	@Test
	public void filterProductsByPrice() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                String[] queryString = { "10_20" };
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat1/", "cat2", 1),
                        fakeRequest("GET", "?price=" + queryString[0]));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
                //verify(sphereTestable.searchRequest).filter(Collections.singletonList(
                 //       new Filters.Price.DynamicRange().parse(Collections.singletonMap("price", queryString))
                //));
            }
        });
	}

	@Test
	public void filterProductsByColor() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
                Result result = callAction(routes.ref.Categories.select("cat1/", "cat2", 1), fakeRequest("GET", "?color=schwarz"));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(10);
                // TODO SDK: Use title or id instead of text
                // assertThat(body.select("#filter-color > ul > li > a.selected").text()).isEqualTo("schwarz");
            }
        });
	}
}
