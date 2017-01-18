package tk.dzrcc.webclient;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 17.01.2017.
 */
public class DzrWebClient {
    static WebClient webClient;

    public DzrWebClient(){
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    private static HtmlPage login(WebClient webClient, String login, String pass) throws IOException {
        HtmlPage page = null;
        // receiving a page
        page = webClient.getPage("http://classic.dzzzr.ru/vrn/");

        List<HtmlForm> forms = page.getForms();
        if (forms.size()>=3) {
            HtmlForm form = forms.get(2);


            HtmlInput loginInput = form.getInputsByName("login").get(0);
            loginInput.setValueAttribute(login);
            HtmlInput password = form.getInputsByName("password").get(0);
            System.out.println(password.toString());
            password.setValueAttribute(pass);
            HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

            page = submit.click();
            return page;
        } else
            return null;
    }

        static HtmlPage inputCode(WebClient webClient, String code) throws IOException {
            DefaultCredentialsProvider provider = new DefaultCredentialsProvider();
            provider.addCredentials("vrn_sorvi_golova", "447124");
            webClient.setCredentialsProvider(provider);
            HtmlPage page =  webClient.getPage("http://classic.dzzzr.ru/vrn/go");
            HtmlForm form = page.getForms().get(0);
            form.getInputByName("cod").setValueAttribute(code);
            HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

            page = submit.click();
            return page;
        }
}
