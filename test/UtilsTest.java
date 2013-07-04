import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.junit.Before;
import org.junit.Test;
import utils.SphereTestable;
import utils.ViewHelper;

import java.util.List;

public class UtilsTest {

    private SphereTestable sphere;

    @Before
    public void mockSphere() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                sphere = new SphereTestable();
            }
        });
    }

    private Category createCategory(int level) {
        List<Category> categories = sphere.mockCategory("cat", level);
        return categories.get(categories.size()-1);
    }

    private Product createProduct() {
        return sphere.mockProduct("prod", 1, 1, 1);
    }

	@Test
	public void UrlWithOneCategory() {
        Category category = createCategory(1);
		String path = ViewHelper.getCategoryUrl(category).url();
		assertThat(path).isEqualTo("/cat1");
	}

	@Test
	public void UrlWithTwoCategories() {
        Category category = createCategory(2);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1/cat2");
	}

    @Test
    public void UrlWithThreeCategories() {
        Category category = createCategory(3);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1-cat2/cat3");
    }

    @Test
    public void UrlWithMoreThanThreeCategories() {
        Category category = createCategory(4);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1-cat2/cat4");
    }

    @Test
    public void UrlWithProduct() {
        Category category = createCategory(1);
        Product product = createProduct();
        String path = ViewHelper.getProductUrl(product, product.getMasterVariant(), category).url();
        assertThat(path).isEqualTo("/prod-0.html");
    }

}
