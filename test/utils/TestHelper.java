package utils;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import play.mvc.Result;

public class TestHelper {

	public static final String CONTENT_TYPE = "text/html";
	public static final String CHARSET = "utf-8";

	public static Document contentAsDocument(Result result) {
		return Jsoup.parse(contentAsString(result), "UTF-8");
	}

	public static void assertContentTypeAndCharset(Result result) {
		assertThat(contentType(result)).isEqualToIgnoringCase(CONTENT_TYPE);
		assertThat(charset(result)).isEqualToIgnoringCase(CHARSET);
	}

	public static void assertOK(Result result) {
		assertThat(status(result)).isEqualTo(OK);
		assertContentTypeAndCharset(result);
	}

	public static void assertBadRequest(Result result) {
		assertThat(status(result)).isEqualTo(BAD_REQUEST);
		assertContentTypeAndCharset(result);
	}

	public static void assertUnauthorized(Result result) {
		assertThat(status(result)).isEqualTo(UNAUTHORIZED);
		assertContentTypeAndCharset(result);
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
