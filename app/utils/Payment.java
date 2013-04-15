package utils;

import com.ning.http.client.Realm;
import controllers.routes;
import io.sphere.client.shop.model.Attribute;
import io.sphere.client.shop.model.Cart;
import io.sphere.client.shop.model.LineItem;
import forms.PaymentNetwork;
import org.specs2.internal.scalaz.std.string;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import play.Play;
import play.libs.F;
import play.libs.WS;
import play.libs.XPath;
import play.mvc.Http;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Payment {

    private static String AUTH_TOKEN = Play.application().configuration().getString("optile.token");
    private static String AUTH_USERNAME = Play.application().configuration().getString("optile.username");
    private static String AUTH_PASSWORD = Play.application().configuration().getString("optile.password");

    public static String HOSTED_URL = Play.application().configuration().getString("optile.hostedUrl");
    public static String NATIVE_URL = Play.application().configuration().getString("optile.nativeUrl");

    private Document req;
    private Document res;

    public Cart cart;
    public String checkoutId;
    public String networkGroup;

    public enum Operation {
        LIST, CHARGE
    }

    public Payment(Cart cart, String checkoutId) {
        this.cart = cart;
        this.checkoutId = checkoutId;
        try {
            req = createXml();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreselectedNetwork(String networkGroup) {
        this.networkGroup = networkGroup;
    }

    public String doRequest(String url, Operation operation) {
        String transactionId = cart.getId();
        try {
            // Build request
            Element root = req.createElementNS("http://dev.btelligent.net/optile/xsd/paymentTransaction/v1.120", "pt:Request");
            req.appendChild(root);

            Element authentication = req.createElement("authentication");
            authentication.appendChild(createNode("token", AUTH_TOKEN));
            root.appendChild(authentication);

            Element transactions = req.createElement("transactions");
            Transaction transaction = new Transaction(operation, transactionId);
            transactions.appendChild(transaction.get());
            transaction.addOrigin()
                    .addCustomer()
                    .addCallback()
                    .addPayment()
                    .addProducts()
                    .addPreselection()
                    .addStyle();
            root.appendChild(transactions);
            // Send request

            F.Promise<WS.Response> promise = WS.url(url)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setAuth(AUTH_USERNAME, AUTH_PASSWORD, Realm.AuthScheme.BASIC)
                    .post("command=" + transformXml(req));
            // Read request
            res = parseXml(promise.get().getBody());
            printMessages(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactionId;
    }

    public boolean isValidResponse(String transactionId) {
        if (res == null) return false;
        if (!getTransactionId().equals(transactionId)) return false;
        return true;
    }

    public String getRedirectUrl() {
        return XPath.selectText("//results/result[1]/redirect[1]/url", res);
    }

    public String getReferredId() {
        return XPath.selectText("//results/result[1]/identification/longId", res);
    }

    public String getTransactionId() {
        return XPath.selectText("//results/result[1]/identification/transactionId", res);
    }

    public List<PaymentNetwork> getApplicableNetworks() {
        List<PaymentNetwork> paymentNetworkList = new ArrayList<PaymentNetwork>();
        NodeList nodes = XPath.selectNodes("//results/result[1]/networks/applicableNetworks/applicableNetwork", res);
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

    protected Node createNode(String name, String value) {
        Element element =  req.createElement(name);
        element.appendChild(req.createTextNode(value));
        return element;
    }

    protected static Document createXml() throws Exception{
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    protected static String transformXml(Document doc) throws Exception {
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(writer));
        System.out.println(writer.getBuffer().toString());
        return writer.getBuffer().toString();
    }

    protected static Document parseXml(String xml) throws Exception {
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


    public class Transaction {

        public String id;
        public Node transaction;

        public Transaction(Operation operation, String id) {
            this.id = id;
            this.transaction = req.createElement("transaction");
            this.transaction.appendChild(createNode("operation", operation.name()));
        }

        public Node get() {
            return transaction;
        }

        public Transaction addOrigin() {
            Node merchant       = createNode("merchant", "COMMERCETOOLS");
            Node transactionId  = createNode("transactionId", id);
            Node channel        = createNode("channel", "WEB_ORDER");
            Node country        = createNode("country", (cart.getCountry() != null)? cart.getCountry().getAlpha2() : "DE");

            Element element = req.createElement("origin");
            element.appendChild(merchant);
            element.appendChild(transactionId);
            element.appendChild(channel);
            element.appendChild(country);
            transaction.appendChild(element);
            return this;
        }

        public Transaction addCustomer() {
            String number = cart.getCustomerId();
            if (number != null) {
                Node customer = createNode("number", number);

                Element element = req.createElement("customer");
                element.appendChild(customer);
                transaction.appendChild(element);
            }
            return this;
        }

        public Transaction addCallback() {
            Node cancelUrl          = createNode("cancelUrl",
                    routes.Checkouts.failure().absoluteURL(Http.Context.current().request()));
            Node returnUrl          = createNode("returnUrl",
                    routes.Checkouts.success().absoluteURL(Http.Context.current().request()));
            Node notificationUrl    = createNode("notificationUrl",
                    routes.Checkouts.notification(checkoutId).absoluteURL(Http.Context.current().request()));

            Element element = req.createElement("callback");
            element.appendChild(cancelUrl);
            element.appendChild(returnUrl);
            element.appendChild(notificationUrl);
            transaction.appendChild(element);
            return this;
        }

        public Transaction addPayment() {
            Node currency       = createNode("currency", cart.getCurrency().getCurrencyCode());
            Node amount         = createNode("amount", cart.getTotalPrice().getAmount().setScale(2).toString());
            Node mask           = createNode("mask", "##########0.00");
            Node shortReference = createNode("shortReference", "Sphere");
            Node essential      = createNode("essential", "Sphere.io by commercetools.de");

            Element element = req.createElement("payment");
            element.appendChild(currency);

            Element maskedAmount = req.createElement("maskedAmount");
            maskedAmount.appendChild(amount);
            maskedAmount.appendChild(mask);
            element.appendChild(maskedAmount);

            element.appendChild(shortReference);

            Element longReference = req.createElement("longReference");
            longReference.appendChild(essential);
            element.appendChild(longReference);

            transaction.appendChild(element);
            return this;
        }

        public Transaction addPreselection() {
            if (networkGroup != null) {
                Element element = req.createElement("preselection");
                element.appendChild(createNode("method", networkGroup));
                transaction.appendChild(element);
            }
            return this;
        }

        public Transaction addProducts() {
            if (cart.getLineItems().size() > 0) {
                Element products = req.createElement("products");
                for (LineItem item : cart.getLineItems()) {
                    Node code       = createNode("code", item.getVariant().getSKU());
                    Node name       = createNode("name", item.getProductName());
                    Node quantity   = createNode("quantity", String.valueOf(item.getQuantity()));
                    Node unit       = createNode("unit", "item");
                    Node amount     = createNode("totalAmount", item.getTotalPrice().getAmount().setScale(2).toString());
                    Node currency   = createNode("currency", item.getTotalPrice().getCurrencyCode());

                    Element product = req.createElement("product");
                    product.appendChild(code);
                    product.appendChild(name);
                    product.appendChild(quantity);
                    product.appendChild(unit);
                    product.appendChild(amount);
                    product.appendChild(currency);

                    if (item.getVariant().getAttributes().size() > 0) {
                        Element attributes = req.createElement("attributes");
                        for (Attribute attr : item.getVariant().getAttributes()) {
                            Node attrName       = createNode("name", attr.getName());
                            Node attrValue      = createNode("value", attr.getString());
                            Node attrType       = createNode("type", "string");
                            Node attrPattern    = createNode("pattern", "*");

                            Element attribute = req.createElement("attribute");
                            attribute.appendChild(attrName);
                            attribute.appendChild(attrValue);
                            attribute.appendChild(attrType);
                            attribute.appendChild(attrPattern);
                            attributes.appendChild(attribute);
                        }
                        product.appendChild(attributes);
                    }
                    products.appendChild(product);
                }
                transaction.appendChild(products);
            }
            return this;
        }

        public Transaction addStyle() {
            Node language   = createNode("language", "en_US");
            Node theme      = createNode("theme", "light");
            Node css        = createNode("cssOverride", "https://sandbox.oscato.com/shop/css/override.css");

            Element element = req.createElement("style");
            element.appendChild(language);
            element.appendChild(theme);
            element.appendChild(css);
            transaction.appendChild(element);
            return this;
        }

    }
}