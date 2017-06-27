package tk.dzrcc.game;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import tk.dzrcc.exception.DozorBotException;

import java.io.IOException;
import java.util.List;

import static tk.dzrcc.TextConstants.CANNOT_INPUT_CODE;
import static tk.dzrcc.TextConstants.CANNOT_LOAD_LOGIN_PAGE;

/**
 * Created by Maksim on 29.03.2017.
 */
public class WebService {
    private WebClient webClient;

    private String url;
    private String login;
    private String pass;

    private String gameUrl;
    private String gameLogin;
    private String gamePass;

    public WebService(String url, String login, String pass, String gameUrl, String gameLogin, String gamePass) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.gameUrl = gameUrl;
        this.gameLogin = gameLogin;
        this.gamePass = gamePass;

    }

    public HtmlPage getGamePage() throws IOException {
        // TODO: 6/27/2017
        /*DefaultCredentialsProvider provider = new DefaultCredentialsProvider();
        provider.addCredentials(gameLogin, gamePass);
        webClient.setCredentialsProvider(provider);
        return webClient.getPage(gameUrl);*/
        return webClient.getPage("http://dzrcc.tk/test/q/2.html");
    }

    public HtmlPage login() throws IOException, DozorBotException {

        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = null;
        // receiving a page
        page = webClient.getPage(url);

        List<HtmlForm> forms = page.getForms();
        if (forms.size()>=3) {
            HtmlForm form = forms.get(2);
            HtmlInput loginInput = form.getInputsByName("login").get(0);
            loginInput.setValueAttribute(login);
            HtmlInput password = form.getInputsByName("password").get(0);
            password.setValueAttribute(pass);
            HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

            page = submit.click();

        } else
            throw new DozorBotException(CANNOT_LOAD_LOGIN_PAGE);
        return page;
    }

    public HtmlPage inputCode(String code) throws IOException, DozorBotException {
        HtmlPage page = getGamePage();
        //HtmlPage page = webClient.getPage("http://dzrcc.tk/test/2222.html");
        HtmlElement submit = null;
        List<HtmlForm> forms = page.getForms();
        int i = 0;
        while (i < forms.size()) {
            try {
                forms.get(i).getInputByName("cod").setValueAttribute(code);
                submit = forms.get(i).getElementsByAttribute("input", "type", "submit").get(0);
            } catch (ElementNotFoundException e) {
                i++;
                continue;
            }
            break;
        }
        if (submit!= null) {
            page = submit.click();
        } else {
            throw new DozorBotException(CANNOT_INPUT_CODE);
        }
        return page;
        //return getGamePage();
    }
}
