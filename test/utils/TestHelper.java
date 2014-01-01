package utils;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.ViewHelper.printPrice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import controllers.routes;
import io.sphere.client.shop.model.Category;
import io.sphere.client.shop.model.LineItem;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;
import play.i18n.Lang;
import play.libs.Json;
import play.mvc.Content;
import play.mvc.Http;
import play.api.mvc.RequestHeader;
import play.mvc.Result;

public class TestHelper {

    public static final String HTML_CONTENT = "text/html";
    public static final String JSON_CONTENT = "application/json";
    public static final String CHARSET = "utf-8";

    public static void setContext() {
        Http.Request request = mock(Http.Request.class);
        when(request.acceptLanguages()).thenReturn(Arrays.asList(Lang.forCode("en")));

        Http.Context.current.set(new Http.Context(
                Long.MIN_VALUE,
                mock(RequestHeader.class),
                request,
                new HashMap<String, String>(),
                new HashMap<String, String>(),
                new HashMap<String, Object>()
        ));
    }

    public static Document contentAsDocument(Content content) {
        return Jsoup.parse(contentAsString(content), CHARSET);
    }

    public static Document contentAsDocument(Result result) {
        return Jsoup.parse(contentAsString(result), CHARSET);
    }

    public static JsonNode contentAsJson(Result result) {
        return Json.parse(contentAsString(result));
    }

    public static void assertContentTypeAndCharset(Result result, String contentType) {
        assertThat(contentType(result)).isEqualToIgnoringCase(contentType);
        assertThat(charset(result)).isEqualToIgnoringCase(CHARSET);
    }

    public static void assertOK(Result result, String contentType) {
        assertThat(status(result)).isEqualTo(OK);
        assertContentTypeAndCharset(result, contentType);
    }

    public static void assertBadRequest(Result result, String contentType) {
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        assertContentTypeAndCharset(result, contentType);
    }

    public static void assertUnauthorized(Result result, String contentType) {
        assertThat(status(result)).isEqualTo(UNAUTHORIZED);
        assertContentTypeAndCharset(result, contentType);
    }

    public static void assertNotFound(Result result) {
        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    public static void assertSeeOther(Result result) {
        assertThat(status(result)).isEqualTo(SEE_OTHER);
    }

    /**
     * Assert that the given URL is not null with the given HTTP method.
     *
     * @param method an HTTP method (GET, POST, PUT, DELETE)
     * @param baseUrl the URL to test
     */
    public static void assertUrlNotNull(String method, String baseUrl) {
        assertUrlNotNull(method, baseUrl, "");
    }

    /**
     * Assert that the given URL (with some query parameters) is not null with
     * the given HTTP method.
     *
     * @param method an HTTP method (GET, POST, PUT, DELETE)
     * @param baseUrl the URL to test
     * @param queryString some query parameters as queryString (e.g.: ?param1=value1&param2=value2)
     */
    public static void assertUrlNotNull(String method, String baseUrl, String queryString) {
        assertThat(route(fakeRequest(method, baseUrl + queryString))).isNotNull();
        if (!baseUrl.equals("/")) {
            assertThat(route(fakeRequest(method, baseUrl + "/" + queryString))).isNotNull();
        }
    }

    public static String generateUniqueId() {
        DateFormat df = new SimpleDateFormat("yyyyMMddhhmmssSSS");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    public static String getValueOrEmpty(String value) {
        if (value == null)
            return "";
        return value;
    }

    public static Map<String, String> createValidUser() {
        String username = generateUniqueId() + "@example.com";
        Map<String, String> data = new HashMap<String, String>();
        data.put("title", "frau");
        data.put("firstName", "firstName");
        data.put("lastName", "lastName");
        data.put("email", username);
        data.put("password", "secret");
        data.put("confirm", "secret");
        return data;
    }

    public static void assertValidBreadcrumb(Document body, int elements) {
        // Check breadcrumb is well formed
        assertThat(body.select(".breadcrumb > *").size()).isEqualTo(elements + 1);
        assertThat(body.select(".breadcrumb > .step").size()).isEqualTo(elements);
        assertThat(body.select(".breadcrumb > .active").size()).isEqualTo(1);
        assertThat(body.select(".breadcrumb > *").last().hasClass("active")).isTrue();
    }

    public static void assertValidNavigationMenu(Document body, SphereTestable sphere) {
        Elements header = body.select("#main-navigation .parent-menu");
        // Check each menu categories listed manually are there
        assertThat(header).isNotEmpty();
        // Check categories are correctly displayed in menu 1
        int numSubCategories = 0;
        for (Category category : sphere.categoryTree.getRoots()) {
            assertThat(header.select("#link-category-" + category.getSlug())).isNotEmpty();
            for (Category sub : category.getChildren()) {
                assertThat(header.select("#link-category-" + sub.getSlug())).isNotEmpty();
                numSubCategories++;
            }
        }
        // Check correct number of categories in menu 1
        int numCategories = sphere.categoryTree.getRoots().size();
        assertThat(header.select(".category-menu").size()).isEqualTo(numCategories);
        assertThat(header.select(".subcategory-menu").size()).isEqualTo(numSubCategories);
    }

    public static void assertValidMiniCart(Document body, SphereTestable sphere) {
        int quantity = sphere.currentCart.getQuantity();
        // Check correct quantity of items is shown in mini cart
        if (quantity > 0) {
            assertThat(body.select("#btn-mini-cart-quantity").text()).isEqualTo(": " + quantity);
        }
        // Check correct list of items is shown in mini cart
        Elements miniCart = body.select("#mini-cart-content");
        int numItems = sphere.currentCart.fetch().getLineItems().size();
        assertThat(body.select("#mini-cart-content .item-line").size()).isEqualTo(numItems);
        for (LineItem item : sphere.currentCart.fetch().getLineItems()) {
            assertThat(miniCart.select(".item-product-name").text()).isEqualTo(item.getProductName());
            assertThat(miniCart.select(".item-quantity").text()).isEqualTo(Integer.toString(item.getQuantity()));
            assertThat(miniCart.select(".item-total-price").text()).isEqualTo(printPrice(item.getTotalPrice()));
        }
    }

    public static void assertSignUpFormFilled(Document body, Map<String, String> data) {
        assertThat(body.select("#signUp-form")).isNotEmpty();
        if (data.containsKey("title")) {
            assertThat(body.select("#signUp-title-" + data.get("title")).attr("checked")).isEqualTo("checked");
        } else {
            assertThat(body.select("#signUp-title-herr").attr("checked")).isEmpty();
            assertThat(body.select("#signUp-title-frau").attr("checked")).isEmpty();
        }
        assertThat(body.select("#signUp-firstName").val()).isEqualTo(getValueOrEmpty(data.get("firstName")));
        assertThat(body.select("#signUp-lastName").val()).isEqualTo(getValueOrEmpty(data.get("lastName")));
        assertThat(body.select("#signUp-email").val()).isEqualTo(getValueOrEmpty(data.get("email")));
        assertThat(body.select("#signUp-password").val()).isEmpty();
        assertThat(body.select("#signUp-confirm").val()).isEmpty();
    }

    public static void assertUserLoggedIn(Document body, Map<String, String> data) {
        assertThat(body.select("#user-first-name").text()).isEqualTo(getValueOrEmpty(data.get("firstName")));
        assertThat(body.select("#user-last-name").text()).isEqualTo(getValueOrEmpty(data.get("lastName")));
        assertThat(body.select("#user-profile")).isNotEmpty();
    }

    public static void assertLoginFormFilled(Document body, Map<String, String> data) {
        assertThat(body.select("#login-form")).isNotEmpty();
        assertThat(body.select("#login-username").val()).isEqualTo(getValueOrEmpty(data.get("username")));
        assertThat(body.select("#login-password").val()).isEmpty();
    }

    public static void assertEditUserFormFilled(Document body, Map<String, String> data) {
        assertThat(body.select("#user-edit-form")).isNotEmpty();
        if (data.containsKey("title")) {
            assertThat(body.select("#user-title-" + data.get("title")).attr("checked")).isEqualTo("checked");
        } else {
            assertThat(body.select("#user-title-herr").attr("checked")).isEmpty();
            assertThat(body.select("#user-title-frau").attr("checked")).isEmpty();
        }
        assertThat(body.select("#user-firstName").val()).isEqualTo(getValueOrEmpty(data.get("firstName")));
        assertThat(body.select("#user-lastName").val()).isEqualTo(getValueOrEmpty(data.get("lastName")));
        assertThat(body.select("#user-email").val()).isEqualTo(getValueOrEmpty(data.get("email")));
    }

    public static void assertEditPasswordFormEmpty(Document body) {
        assertThat(body.select("#password-edit-form")).isNotEmpty();
        assertThat(body.select("#password-old").val()).isEmpty();
        assertThat(body.select("#password-password").val()).isEmpty();
        assertThat(body.select("#password-confirm").val()).isEmpty();
    }
}