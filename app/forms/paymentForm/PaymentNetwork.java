package forms.paymentForm;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentNetwork {

    public String networkCode;
    public String description;
    public String grouping;
    public boolean asynchronous;
    public int sortOrder;
    public String formsUrl;
    public String logoUrl;
    public String languageUrl;
    public String localizedFormUrl;
    public String nextOperation;
    public String nextOperationUrl;
    public String registration;
    public String recurrence;

    public PaymentNetwork() {
    }

    public PaymentNetwork(String networkCode, String description, String grouping, boolean asynchronous,
                          int sortOrder, String formsUrl, String logoUrl, String languageUrl,
                          String localizedFormUrl, String nextOperation, String nextOperationUrl,
                          String registration, String recurrence) {
        this.networkCode = networkCode;
        this.description = description;
        this.grouping = grouping;
        this.asynchronous = asynchronous;
        this.sortOrder = sortOrder;
        this.formsUrl = formsUrl;
        this.logoUrl = logoUrl;
        this.languageUrl = languageUrl;
        this.localizedFormUrl = localizedFormUrl;
        this.nextOperation = nextOperation;
        this.nextOperationUrl = nextOperationUrl;
        this.registration = registration;
        this.recurrence = recurrence;
    }

    public static ObjectNode getJson(List<PaymentNetwork> networks, String referredId) {
        ObjectNode json = Json.newObject();
        json.put("referredId", referredId);
        ArrayNode groupsArray = json.putArray("group");
        for (Map.Entry<String, List<PaymentNetwork>> grouping : group(networks).entrySet()) {
            ObjectNode group = Json.newObject();
            group.put("name", grouping.getKey());
            ArrayNode groupArray = group.putArray("network");
            for (PaymentNetwork network : grouping.getValue()) {
                groupArray.add(getJson(network));
            }
            groupsArray.add(group);
        }
        return json;
    }

    public static ObjectNode getJson(PaymentNetwork network) {
        ObjectNode json = Json.newObject();
        json.put("networkCode", network.networkCode);
        json.put("description", network.description);
        json.put("asynchronous", network.asynchronous);
        json.put("sortOrder", network.sortOrder);
        json.put("registration", network.registration);
        json.put("logoUrl", network.logoUrl);
        json.put("formHtml", network.getFormHtml());
        return json;
    }

    public static Map<String, List<PaymentNetwork>> group(List<PaymentNetwork> networks) {
        Map<String, List<PaymentNetwork>> groups = new HashMap<String, List<PaymentNetwork>>();
        for (PaymentNetwork network : networks) {
            if (groups.containsKey(network.grouping)) {
                groups.get(network.grouping).add(network);
            } else {
                List<PaymentNetwork> group = new ArrayList<PaymentNetwork>();
                group.add(network);
                groups.put(network.grouping, group);
            }
        }
        return groups;
    }

    public String getFormHtml() {
        try {
            Document doc = Jsoup.connect(localizedFormUrl).get();
            // Convert all popups into regular links
            Elements onclickLinks = doc.select("a[onclick]");
            if (!onclickLinks.isEmpty()) {
                Elements scripts = doc.select("script");
                String urlVar = "var url\\s*=\\s*\"(.+?)\"";
                String funcName;
                Pattern p;
                Matcher m;
                for (Element link : onclickLinks) {
                    p = Pattern.compile("^(?:javascript:)?(.+?)\\(.*\\)$");
                    m = p.matcher(link.attr("onclick"));
                    if (!m.matches()) continue;
                    funcName = Pattern.quote(m.group(1));
                    p = Pattern.compile("(?is)function\\s+"+ funcName +"\\s*\\(.*?\\)\\s*\\{.*?"+ urlVar +".*?\\}");
                    m = p.matcher(scripts.html());
                    if (!m.matches()) continue;
                    link.attr("href", m.group(1));
                    link.attr("target", "_blank");
                }
            }
            // Append some text inside label element where a hint message is found
            Elements hints = doc.select(".hint");
            if (!hints.isEmpty()) {
                String id;
                for (Element hint : hints) {
                    id = hint.attr("id").replaceAll("-hint$", "");
                    doc.select("label[for=" + id + "]")
                            .append("<span class=\"hint-message\"><i class=\"icon-question-sign\"></span>");
                }
            }

            // Clean up code to avoid XSS
            Whitelist whitelist = Whitelist.relaxed()
                .addAttributes(":all", "class")
                .addAttributes(":all", "id")
                .addAttributes(":all", "style")
                .addTags("fieldset", "span", "textarea")
                .addAttributes("a", "target")
                .addAttributes("input", "type", "name", "value", "max", "min", "checked", "disabled", "autocomplete")
                .addAttributes("label", "for")
                .addAttributes("option", "value", "selected")
                .addAttributes("select", "name");
            String cleanForm = Jsoup.clean(doc.html(), whitelist);
            // Replace custom variables from template
            return cleanForm.replaceAll(Pattern.quote("${formId}"), "payment-network-"+ networkCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
