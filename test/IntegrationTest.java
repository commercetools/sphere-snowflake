import org.junit.Ignore;
import org.junit.Test;
import play.libs.F;
import play.test.TestBrowser;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

@Ignore
public class IntegrationTest {

    @Test
    public void showHome() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                System.out.println("A");
                goToHome(browser);
                System.out.println("A2");
                assertThat(browser.$("#num-total-product").first().getText()).isEqualTo("1283");
            }
        });
    }

    @Test
    public void showCategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                System.out.println("B");
                goToCategory(browser);
                System.out.println("B2");
                assertThat(browser.$("#num-total-product").first().getText()).isEqualTo("669");
            }
        });
    }

    @Test
    public void showSubcategory() {
        running(testServer(3333, fakeApplication()), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                System.out.println("C");
                goToSubcategory(browser);
                System.out.println("C2");
                assertThat(browser.$("#num-total-product").first().getText()).isEqualTo("107");
            }
        });
    }

    public void goToHome(TestBrowser browser) {
        System.out.println("A0");
        browser.goTo("http://localhost:3333");
        System.out.println("A1");
    }

    public void goToCategory(TestBrowser browser) {
        System.out.println("B0");
        goToHome(browser);
        browser.$("#header-menu-kuche").click();
        System.out.println("B1");
    }

    public void goToSubcategory(TestBrowser browser) {
        System.out.println("C0");
        goToCategory(browser);
        browser.$("#header-submenu-gedeckter-tisch").click();
        System.out.println("C1");
    }

}
