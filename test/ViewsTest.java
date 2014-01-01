import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.Product;
import org.jsoup.nodes.Document;
import org.junit.Test;
import play.mvc.Content;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentType;
import static utils.SphereTestable.mockCategory;
import static utils.SphereTestable.mockProduct;
import static utils.TestHelper.contentAsDocument;

public class ViewsTest {

    @Test
    public void checkBreadcrumbHomePage() {
        Content html = views.html.helper.breadcrumb.render(null, null);
        assertThat(contentType(html)).isEqualTo("text/html");
        Document body = contentAsDocument(html);
        assertThat(body.select(".breadcrumb > *").size()).isEqualTo(1);
        assertThat(body.select(".breadcrumb > .step").size()).isEqualTo(0);
        assertThat(body.select(".breadcrumb > .active").size()).isEqualTo(1);
        assertThat(body.select(".breadcrumb > .active").text()).isEqualTo("Home");
    }

    @Test
    public void checkBreadcrumbCategoryPage() {
        Category category = mockCategory("cat", 2).get(1);
        Content html = views.html.helper.breadcrumb.render(category, null);
        assertThat(contentType(html)).isEqualTo("text/html");
        Document body = contentAsDocument(html);
        assertThat(body.select(".breadcrumb > *").size()).isEqualTo(3);
        assertThat(body.select(".breadcrumb > .step").size()).isEqualTo(2);
        assertThat(body.select(".breadcrumb > .step:eq(0) a").text()).isEqualTo("Home");
        assertThat(body.select(".breadcrumb > .step:eq(1) a").text()).isEqualTo(category.getParent().getName());
        assertThat(body.select(".breadcrumb > .active").size()).isEqualTo(1);
        assertThat(body.select(".breadcrumb > .active").text()).isEqualTo(category.getName());
    }

    @Test
    public void checkBreadcrumbProductPage() {
        Category cat = mockCategory("cat", 2).get(1);
        Category par = cat.getParent();
        Product prod = mockProduct("prod", 1, 0, 0);
        Content html = views.html.helper.breadcrumb.render(cat, prod);
        assertThat(contentType(html)).isEqualTo("text/html");
        Document d = contentAsDocument(html);
        assertThat(d.select(".breadcrumb > *").size()).isEqualTo(4);
        assertThat(d.select(".breadcrumb >.step").size()).isEqualTo(3);
        assertThat(d.select(".breadcrumb >.step:eq(0) a").text()).isEqualTo("Home");
        assertThat(d.select(".breadcrumb >.step:eq(1) a").text()).isEqualTo(par.getName());
        assertThat(d.select(".breadcrumb >.step:eq(2) a").text()).isEqualTo(cat.getName());
        assertThat(d.select(".breadcrumb >.active").size()).isEqualTo(1);
        assertThat(d.select(".breadcrumb >.active").text()).isEqualTo(prod.getName());
    }


}
