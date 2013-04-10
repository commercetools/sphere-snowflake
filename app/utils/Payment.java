package utils;

import com.ning.http.client.Realm;
import controllers.routes;
import io.sphere.client.shop.model.Attribute;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.LineItem;
import io.sphere.client.shop.model.PaymentState;
import forms.PaymentNetwork;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import play.Play;
import play.libs.F;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Http;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Payment {

    public static String AUTH_TOKEN = Play.application().configuration().getString("optile.token");
    public static String AUTH_USERNAME = Play.application().configuration().getString("optile.username");
    public static String AUTH_PASSWORD = Play.application().configuration().getString("optile.password");

    public static String HOSTED_URL = Play.application().configuration().getString("optile.hostedUrl");
    public static String NATIVE_URL = Play.application().configuration().getString("optile.nativeUrl");

    public static Document requestHostedList(Cart cart, String checkoutId) {
        return request(HOSTED_URL, cart.getId(),
                getListXmlHeader() +
                        getOriginXml(cart) +
                        getCustomerXml(cart) +
                        getCallbackXml(checkoutId) +
                        getPaymentXml(cart) +
                        getProductsXml(cart) +
                        getStyleXml() +
                        getListXmlFooter());
    }

    public static Document requestHostedList(Cart cart, String checkoutId, String networkGroup) {
        return request(HOSTED_URL, cart.getId(),
                getListXmlHeader() +
                        getOriginXml(cart) +
                        getCustomerXml(cart) +
                        getCallbackXml(checkoutId) +
                        getPaymentXml(cart) +
                        getPreselectionXml(networkGroup) +
                        getProductsXml(cart) +
                        getStyleXml() +
                        getListXmlFooter());
    }

    public static Document requestNativeList(Cart cart, String checkoutId) {
        return request(NATIVE_URL, cart.getId(),
                getListXmlHeader() +
                        getOriginXml(cart) +
                        getCustomerXml(cart) +
                        getCallbackXml(checkoutId) +
                        getPaymentXml(cart) +
                        getProductsXml(cart) +
                        getStyleXml() +
                        getListXmlFooter());
    }

    public static Document requestCharge(Cart cart, String referredId) {
        return request(NATIVE_URL, cart.getId(),
                getChargeXmlHeader() +
                        getOriginXml(cart) +
                        getReferredIdXml(referredId) +
                        getAccountXml() +
                        getListXmlFooter());
    }

    public static String getRedirectUrl(Document response) {
        return XPath.selectText("//results/result/redirect[1]/url", response);
    }

    public static String getReferredId(Document response) {
        return XPath.selectText("//results/result/identification/longId", response);
    }

    public static List<PaymentNetwork> getApplicableNetworks(Document response) {
        List<PaymentNetwork> paymentNetworkList = new ArrayList<PaymentNetwork>();
        NodeList nodes = XPath.selectNodes("//results/result[1]/networks/applicableNetworks/applicableNetwork", response);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            paymentNetworkList.add(new PaymentNetwork(
                    XPath.selectText("networkCode", node),
                    XPath.selectText("description", node),
                    XPath.selectText("grouping", node),
                    XPath.selectText("asynchronous", node).equals("true"),
                    Integer.parseInt(XPath.selectText("sortOrder", node)),
                    XPath.selectText("formsUrl", node),
                    XPath.selectText("formsResources/logoUrl", node),
                    XPath.selectText("formsResources/languageUrl", node),
                    XPath.selectText("formsResources/localizedFormUrl", node),
                    XPath.selectText("nextOperation", node),
                    XPath.selectText("nextOperationUrl", node),
                    XPath.selectText("registration", node),
                    XPath.selectText("recurrence", node)));
        }
        return paymentNetworkList;
    }

    public static PaymentState getPaymentState(String entity, String statusCode, String reasonCode) {
        // TODO Complete it
        if (statusCode.equals("charged")) return PaymentState.Paid;
        if (statusCode.equals("paid_out")) return PaymentState.Paid;
        if (statusCode.equals("pending")) return PaymentState.Pending;
        if (statusCode.equals("failed")) return PaymentState.Failed;
        return PaymentState.Failed;
    }

    private static Document request(String url, String transactionId, String requestXml) {
        F.Promise<WS.Response> promise = WS.url(url)
                .setHeader("Content-Type","application/x-www-form-urlencoded")
                .setAuth(AUTH_USERNAME, AUTH_PASSWORD, Realm.AuthScheme.BASIC)
                .post("command=" + requestXml);
        Document response = null;
        try {
            response = parseXml(promise.get().getBody());
            if (!XPath.selectText("//results/result[1]/identification/transactionId", response).equals(transactionId)) {
                return null;
            }
            printMessages(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private static String getListXmlHeader() {
        return  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<pt:Request xmlns:pt=\"http://dev.btelligent.net/optile/xsd/paymentTransaction/v1.120\">\n" +
                    "\t<authentication>\n" +
                        "\t\t<token>"+ AUTH_TOKEN +"</token>\n" +
                    "\t</authentication>\n" +
                    "\t<transactions>\n" +
                        "\t\t<transaction>\n" +
                            "\t\t\t<operation>LIST</operation>\n";
    }

    private static String getChargeXmlHeader() {
        return  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<pt:Request xmlns:pt=\"http://dev.btelligent.net/optile/xsd/paymentTransaction/v1.120\">\n" +
                    "\t<authentication>\n" +
                        "\t\t<token>"+ AUTH_TOKEN +"</token>\n" +
                    "\t</authentication>\n" +
                    "\t<transactions>\n" +
                        "\t\t<transaction>\n" +
                            "\t\t\t<operation>CHARGE</operation>\n";
    }

    private static String getListXmlFooter() {
        return          "\t\t</transaction>\n" +
                    "\t</transactions>\n" +
                "</pt:Request>";
    }

    private static String getOriginXml(Cart cart) {
        return  "<origin>\n" +
                    "\t<merchant>COMMERCETOOLS</merchant>\n" +
                    "\t<transactionId>"+ cart.getId() +"</transactionId>\n" +
                    "\t<channel>WEB_ORDER</channel>\n" +
                    "\t<country>DE</country>\n" +
                "</origin>\n";
    }

    private static String getReferredIdXml(String referredId) {
        return "<referredId>"+ referredId +"</referredId>\n";
    }

    private static String getCustomerXml(Cart cart) {
        if (cart.getCustomerId() == null) return "";
        return  "<customer>\n" +
                    "\t<number>"+ cart.getCustomerId() +"</number>\n" +
                "</customer>\n";
    }

    private static String getCallbackXml(String checkoutId) {
        return  "<callback>\n" +
                    "\t<cancelUrl>"+ routes.Checkouts.failure().absoluteURL(Http.Context.current().request()) +"</cancelUrl>\n" +
                    "\t<returnUrl>"+ routes.Checkouts.success().absoluteURL(Http.Context.current().request()) +"</returnUrl>\n" +
                    "\t<notificationUrl>"+ routes.Checkouts.notification(checkoutId).absoluteURL(Http.Context.current().request()) +"</notificationUrl>\n" +
                "</callback>\n";
    }

    private static String getSessionIdXml() {
        return  "<sessionId>...</sessionId>";
    }

    private static String getCustomerRegistrationXml() {
        return  "<customerRegistrationId>customerRegistrationId</customerRegistrationId>\n" +
                "<customerRegistrationPassword>...</customerRegistrationPassword>\n";
    }

    private static String getPaymentXml(Cart cart) {
        return  "<payment>\n" +
                    "\t<currency>"+ cart.getTotalPrice().getCurrencyCode() +"</currency>\n" +
                    "\t<maskedAmount>\n" +
                        "\t\t<amount>"+ cart.getTotalPrice().getAmount().setScale(2) +"</amount>\n" +
                        "\t\t<mask>##########0.00</mask>\n" +
                    "\t</maskedAmount>\n" +
                    "\t<shortReference>Sphere</shortReference>\n" +
                    "\t<longReference>\n" +
                        "\t\t<essential>Sphere.io by commercetools.de</essential>\n" +
                    "\t</longReference>\n" +
                "</payment>\n";
    }

    private static String getPreselectionXml(String networkGroup) {
        return  "<preselection>\n" +
                    "\t<method>"+ networkGroup +"</method>\n" +
                "</preselection>\n";
    }

    private static String getProductsXml(Cart cart) {
        if (cart.getLineItems().size() < 1) return "";
        String productsXml = "<products>\n";
        for (LineItem item : cart.getLineItems()) {
            productsXml += "<product>\n" +
                            "\t<code>"+ item.getVariant().getSKU() +"</code>\n" +
                            "\t<name>"+ item.getProductName() +"</name>\n" +
                            "\t<quantity>"+ item.getQuantity() +"</quantity>\n" +
                            "\t<unit>item</unit>\n" +
                            "\t<totalAmount>"+ item.getTotalPrice().getAmount().setScale(2) +"</totalAmount>\n" +
                            "\t<currency>"+ item.getTotalPrice().getCurrencyCode() +"</currency>\n" +
                            "\t<attributes>\n";
            for (Attribute attr : item.getVariant().getAttributes()) {
                productsXml +=  "\t\t<attribute>\n" +
                                    "\t\t\t<name>"+ attr.getName() +"</name>\n" +
                                    "\t\t\t<value>"+ attr.getValue().toString() +"</value>\n" +
                                    "\t\t\t<type>string</type>\n" +
                                    "\t\t\t<pattern>*</pattern>\n" +
                                "\t\t</attribute>\n";
            }
            productsXml +=      "\t</attributes>\n" +
                            "</product>\n";
        }
        productsXml += "</products>\n";
        return productsXml;
    }

    private static String getAccountXml() {
        return "<account>\n" +
                    "\t<holderName>James Bond</holderName>\n" +
                    "\t<number>4111111111111111</number>\n" +
                    "\t<expiryMonth>12</expiryMonth>\n" +
                    "\t<expiryYear>2012</expiryYear>\n" +
                    "\t<verificationCode>123</verificationCode>\n" +
                "</account>\n";
    }

    private static String getStyleXml() {
        return  "<style>\n" +
                    "\t<language>en_US</language >\n" +
                    "\t<theme>light</theme>\n" +
                    "\t<cssOverride>https://sandbox.oscato.com/shop/css/override.css</cssOverride>\n" +
                "</style>\n";
    }

    private static Document parseXml(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static void printMessages(Document response) {
        NodeList messages = XPath.selectNodes("//results/result/messages/message", response);
        for (int i = 0; i < messages.getLength(); i++) {
            Node message = messages.item(i);
            String level = XPath.selectNode("level", message).getTextContent();
            String text = XPath.selectNode("text", message).getTextContent();
            System.err.println(level + ": " + text);
        }
    }
}
