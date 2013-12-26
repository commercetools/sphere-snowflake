import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import play.libs.F;
import play.test.TestBrowser;
import utils.SphereTestable;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

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
                // TODO Find out why htmlunit has trouble loading masonry and why it only fails on home page
                browser = testBrowser(new HtmlUnitDriver(false));
                goToHome(browser);
                assertThat(browser.$("body.home").isEmpty()).isFalse();
            }
        });
    }

    @Test
    public void showCategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser = testBrowser(new HtmlUnitDriver(false));
                goToCategory(browser);
                assertThat(browser.$("body.category").isEmpty()).isFalse();
                assertThat(browser.title()).isEqualTo("cat1");
            }
        });
    }

    @Test
    public void showSubcategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser = testBrowser(new HtmlUnitDriver(false));
                goToSubcategory(browser);
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
        browser.$("#link-category-cat1").click();
    }

    public void goToSubcategory(TestBrowser browser) {
        goToCategory(browser);
        browser.$("#link-category-cat2").click();
    }

}
