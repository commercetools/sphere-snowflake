import static org.fest.assertions.Assertions.assertThat;

import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.MockHelper;
import utils.ViewHelper;

@RunWith(PowerMockRunner.class)
public class UtilsTest {

	@Test
	public void UrlWithOneCategory() {
        Category category = MockHelper.mockCategory("cat", 1);
		String path = ViewHelper.getCategoryUrl(category).url();
		assertThat(path).isEqualTo("/cat1");
	}

	@Test
	public void UrlWithTwoCategories() {
        Category category = MockHelper.mockCategory("cat", 2);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1/cat2");
	}

    @Test
    public void UrlWithThreeCategories() {
        Category category = MockHelper.mockCategory("cat", 3);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1-cat2/cat3");
    }

    @Test
    public void UrlWithMoreThanThreeCategories() {
        Category category = MockHelper.mockCategory("cat", 4);
        String path = ViewHelper.getCategoryUrl(category).url();
        assertThat(path).isEqualTo("/cat1-cat2/cat4");
    }

    @Test
    public void UrlWithProduct() {
        Product product = MockHelper.mockProduct("prod", 1, 1, 1);
        Category category = MockHelper.mockCategory("cat", 1);
        String path = ViewHelper.getProductUrl(product, product.getMasterVariant(), category).url();
        assertThat(path).isEqualTo("/prod-0.html");
    }

}
