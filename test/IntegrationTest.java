import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.libs.F;
import play.test.TestBrowser;
import utils.SphereTestable;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;
import static utils.TestHelper.contentAsDocument;

public class IntegrationTest {

    private SphereTestable sphereTestable;

    @Before
    public void mockSphere() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                sphereTestable = new SphereTestable();
                mockCategoryRequest(3);
                mockProductRequest(15, 0, 10);
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
    public void showHome() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                goToHome(browser);
                assertThat(browser.$("body.home").isEmpty()).isFalse();
            }
        });
    }

    @Test
    public void showCategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                goToCategory(browser);
                assertThat(browser.$("body.category").isEmpty()).isFalse();
                assertThat(browser.title()).isEqualTo("cat1");
            }
        });
    }

    @Ignore
    @Test
    public void showSubcategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                goToSubcategory(browser);
                System.out.println(browser.pageSource());
                assertThat(browser.$("body.category").isEmpty()).isFalse();
                assertThat(browser.title()).isEqualTo("cat2");
            }
        });
    }

    public void goToHome(TestBrowser browser) {
        browser.goTo("http://localhost:3333");
    }

    public void goToCategory(TestBrowser browser) {
        goToHome(browser);
        browser.$("#link-cat1").click();
    }

    public void goToSubcategory(TestBrowser browser) {
        goToCategory(browser);
        browser.$("#link-cat2").click();
    }

}
