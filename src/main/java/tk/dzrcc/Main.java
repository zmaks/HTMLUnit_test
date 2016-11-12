package tk.dzrcc;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mazh0416 on 11/12/2016.
 */
public class Main {
    static WebClient webClient;

    public static void main(String[] args) {

        initWebClient();
        try {
            System.out.print(login(webClient, "Zubrrr", "121212").asText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initWebClient() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    private static HtmlPage login(WebClient webClient, String login, String pass) throws IOException {
        HtmlPage page = null;

        // receiving a page
        page = webClient.getPage("http://classic.dzzzr.ru/vrn/");

        /*String res = page.getTextContent();
        System.out.println(res);*/

        HtmlForm form = page.getForms().get(2);
        Iterable<DomNode> children = form.getChildren();
        List<HtmlDivision> divs = new ArrayList<HtmlDivision>();
        for (DomNode child : children) {
            if (child instanceof HtmlDivision) {
                divs.add((HtmlDivision) child);
            }
        }


        HtmlInput loginInput = form.getInputsByName("login").get(0);
        loginInput.setValueAttribute(login);
        HtmlInput password = form.getInputsByName("password").get(0);
        System.out.println(password.toString());
        password.setValueAttribute(pass);
        HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

        page = submit.click();

        return page;
    }
}
