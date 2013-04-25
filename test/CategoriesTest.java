import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelper.*;

import controllers.routes;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import play.mvc.Result;
import sphere.Sphere;
import utils.MockHelper;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Sphere.class)
@PowerMockIgnore("javax.crypto.*")
public class CategoriesTest {

    private static int NUM_PRODUCT = 4;

    @Before
    public void mockSphere() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                MockHelper.mockSphereClient("cat", 3, "prod", NUM_PRODUCT);
            }
        });
    }

    @Ignore
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
                assertUrlNotNull(GET, "/cat");
                assertUrlNotNull(GET, "/cat1/cat");
                assertUrlNotNull(GET, "/cat2-cat1/cat");
            }
        });
	}

    @Test
    public void checkProductPagingUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat", "?page=1");
                assertUrlNotNull(GET, "/cat1/cat", "?page=1");
                assertUrlNotNull(GET, "/cat2-cat1/cat", "?page=1");
            }
        });
    }

	@Test
	public void checkProductFilterByPriceUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat", "?price=10_20");
                assertUrlNotNull(GET, "/cat1/cat", "?price=10_20");
                assertUrlNotNull(GET, "/cat2-cat1/cat", "?price=10_20");
            }
        });
	}

	@Test
	public void checkProductFilterByColorUrl() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                assertUrlNotNull(GET, "/cat", "?color=black");
                assertUrlNotNull(GET, "/cat1/cat", "?color=black");
                assertUrlNotNull(GET, "/cat2-cat1/cat", "?color=black");
            }
        });
	}

    @Ignore
    @Test
    public void showHome() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.home(1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
            }
        });
    }

    @Ignore
	@Test
	public void selectCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("cat1", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
            }
        });
	}

    @Ignore
	@Test
	public void showSubcategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("cat1-cat2/cat3", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
            }
        });
	}

    @Test
	public void showInvalidCategory() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("non-existing-category", 1));
                assertNotFound(result);
            }
        });
	}

    @Ignore
	@Test
	public void showCategoryWithInvalidParent() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = callAction(routes.ref.Categories.select("non-existing-category/cat3", 1));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
            }
        });
	}

	@Ignore
	@Test
	public void pagingProducts() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                // TODO Impossible to specify page now with current mocking
                Result result = callAction(routes.ref.Categories.select("cat1-cat2/cat3", 2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
            }
        });
	}

	@Ignore
	@Test
	public void pagingProductsAboveRange() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                // TODO Impossible to specify page now with current mocking
                Result result = callAction(routes.ref.Categories.select("cat1/cat", 100));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(0);
                assertThat(body.select("#messages .alert-info").hasText()).isTrue();
            }
        });
	}

	@Ignore
	@Test
	public void pagingProductsInvalid() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                // TODO Impossible to specify page now with current mocking
                Result result = callAction(routes.ref.Categories.select("cat1/cat", -2));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
                assertThat(body.select("#num-offset-product").val()).isEqualTo("0");
                assertThat(body.select("#num-current-product").val()).isEqualTo("15");
                assertThat(body.select("#num-total-product").val()).isEqualTo("107");
                assertThat(body.select("#num-current-page").val()).isEqualTo("0");
                assertThat(body.select("#num-total-page").val()).isEqualTo("8");
            }
        });
	}

	@Ignore
	@Test
	public void filterProductsByPrice() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                // TODO Impossible to specify price now with current mocking
                Result result = callAction(routes.ref.Categories.select("cat1/cat", 1), fakeRequest("GET", "?price=10_20"));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(NUM_PRODUCT);
                assertThat(body.select("#num-total-product").val()).isEqualTo("26");
            }
        });
	}

	@Ignore
	@Test
	public void filterProductsByColor() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                // TODO Impossible to specify color now with current mocking
                Result result = callAction(routes.ref.Categories.select("cat1/cat", 1), fakeRequest("GET", "?color=schwarz"));
                assertOK(result);
                Document body = contentAsDocument(result);
                assertThat(body.select("#product-list .product-item").size()).isEqualTo(12);
                assertThat(body.select("#num-total-product").val()).isEqualTo("12");
                // TODO SDK: Use title or id instead of text
                // assertThat(body.select("#filter-color > ul > li > a.selected").text()).isEqualTo("schwarz");
            }
        });
	}
}
