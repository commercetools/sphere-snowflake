package forms;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

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

    public String getFormHtml() {
        try {
            Document doc = Jsoup.connect(localizedFormUrl).get();
            // Convert all popups into common links
            Elements onclickLinks = doc.select("a[onclick]");
            if (!onclickLinks.isEmpty()) {
                Elements scripts = doc.select("script");
                String urlVar = "var url = \"(.+?)\"";
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
