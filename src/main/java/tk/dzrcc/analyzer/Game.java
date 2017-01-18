package tk.dzrcc.analyzer;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maksim on 18.01.2017.
 */
public class Game {

    private WebClient webClient;

    private String url;
    private String login;
    private String pass;

    private String gameUrl;
    private String gameLogin;
    private String gamePass;

    private List<ArrayList<ArrayList<Code>>> sectors;
    private List<GottenCode> gottenCodes;
    private String currentTaskNumber = "0";

    private Pattern sectorsPattern = Pattern.compile("основные.+<br");
    private Pattern codePattern = Pattern.compile("\\d\\+?");
    private Pattern taskNumPattern = Pattern.compile("Задание\\s\\d{1,2}");

    private static String CANNOT_INPUT_CODE = "Ошибка. Не получается вбить код :(";
    private static String CANNOT_LOAD_PAGE = "Что-то не получется загрузить страницу движка...";
    private static String CANNOT_FIND_TASK_NUMBER = "Что-то пошло не так. Не могу найти номер задания на странице движка.";
    private static String STRANGE_SITUATION = "Странно. Код вроде принят, но почему-то не могу определить какой из. Нужна помощь человеческого разума. Зайдите на движок и чекните последние события игры.";

    public Game(String url, String login, String pass, String gameLogin, String gamePass) {
        this.url = url;
        this.login = login;
        this.pass = pass;
        this.gameUrl = url+"/go";
        this.gameLogin = gameLogin;
        this.gamePass = gamePass;
        init();
    }

    public String init(){
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

        sectors = new ArrayList<ArrayList<ArrayList<Code>>>();
        gottenCodes = new ArrayList<GottenCode>();
        try {
            HtmlPage page = login(url, login, pass);
            System.out.println(page.getBody().asText());
        } catch (IOException e) {
            e.printStackTrace();
            return "Не удалось залогиниться :(";
        }
        return "Авторизация прошла успешно";

    }

    public synchronized CodeResponse performCode(String code, String player) {
        HtmlPage gamePage;
        try {
            gamePage = getGamePage();
        } catch (IOException e) {
            e.printStackTrace();
            return new CodeResponse(CANNOT_LOAD_PAGE, null);
        }
        System.out.println(gamePage.getBody().asText());
        String taskNum = parseTaskNumber(gamePage);
        if (taskNum == null)
            return new CodeResponse(CANNOT_FIND_TASK_NUMBER, null);

        if (!taskNum.equals(currentTaskNumber)){
            currentTaskNumber = taskNum;
            sectors = parseCodePage(gamePage);
            gottenCodes.clear();
        }

        HtmlPage codePage;
        try {
            codePage = inputCode(code);
        } catch (IOException e) {
            e.printStackTrace();
            return new CodeResponse(CANNOT_INPUT_CODE, null);
        }

        String sysMessage = parseSysMessage(codePage);
        if (sysMessage.contains("Код принят")){
            Code inputCode = analyzeCodes(parseCodePage(codePage));

            if (inputCode == null)
                return new CodeResponse(STRANGE_SITUATION, null);
            gottenCodes.add(new GottenCode(code, player, new Date()));
            CodeResponse codeResponse = new CodeResponse(sysMessage, inputCode);
            codeResponse.setLevelStat(getLevelStatistic(sectors, inputCode.getSector(), inputCode.getLevel()));
            codeResponse.setSectorStat(getSectorStatistic(sectors, inputCode.getSector()));
            return codeResponse;
        } else {
            GottenCode gottenCode = gottenCodes.stream().filter(x -> x.getCode().equals(code)).findFirst().orElse(null);
            return new CodeResponse(sysMessage, null, gottenCode);
        }
    }

    private String getLevelStatistic(List<ArrayList<ArrayList<Code>>> sectors, Integer sector, String level) {
        if (sectors.get(sector-1).size()==1) return null;
        ArrayList<Code> levelList = sectors.get(sector-1)
                .stream()
                .filter(x -> x.get(0).getLevel().equals(level))
                .findFirst()
                .orElse(null);

        Long countFound = levelList
                .stream()
                .filter(x -> x.getGotten())
                .count();
        return countFound+"/"+levelList.size();
    }

    private String getSectorStatistic(List<ArrayList<ArrayList<Code>>> sectors, Integer sector){
        Long countGottenInSector = 0L;
        Long countInSector = 0L;
        for (int i = 0; i < sectors.get(sector-1).size(); i++) {
            countInSector += sectors.get(sector-1).get(i).size();
            countGottenInSector += sectors.get(sector-1).get(i)
                    .stream()
                    .filter(x -> x.getGotten())
                    .count();
        }
        return countGottenInSector+"/"+countInSector;
    }

    private String parseTaskNumber(HtmlPage page){
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='title']");
        if (task == null) return null;
        Matcher matcher = taskNumPattern.matcher(task.asText());
        String taskNum = "";
        if (matcher.find()) taskNum = matcher.group();
        return taskNum.replace("Задание ", "");
    }

    private String parseSysMessage(HtmlPage page){
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='sysmsg']");
        return task.getTextContent();
    }

    private HtmlPage inputCode(String code) throws IOException {
        HtmlPage page = getGamePage();
        HtmlForm form = page.getForms().get(page.getForms().size()-1);
        form.getInputByName("cod").setValueAttribute(code);
        HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

        page = submit.click();
        return page;
    }

    private HtmlPage getGamePage() throws IOException {
        DefaultCredentialsProvider provider = new DefaultCredentialsProvider();
        provider.addCredentials(gameLogin, gamePass);
        webClient.setCredentialsProvider(provider);
        return webClient.getPage(gameUrl);
    }

    private Code analyzeCodes(List<ArrayList<ArrayList<Code>>> parsedSectors){
        ArrayList<ArrayList<Code>> curSector;
        ArrayList<Code> curLevel;
        Code currentCode = null;
        Code parsedCode;
        boolean found = false;
        //------------------
        sectors = parsedSectors;
        //-----------

        if (!sectors.isEmpty()){
            for(int sector = 0; sector < parsedSectors.size(); sector++){
                curSector = parsedSectors.get(sector);
                for(int level = 0; level < curSector.size(); level++){
                    curLevel = curSector.get(level);
                    for(int codeInd = 0; codeInd < curLevel.size(); codeInd++){
                        currentCode = sectors.get(sector).get(level).get(codeInd);
                        parsedCode = parsedSectors.get(sector).get(level).get(codeInd);
                        if (parsedCode.getValue().length()>4 && !currentCode.getGotten()){
                            currentCode.setGotten(true);
                            found = true;
                        }
                        if (found) break;
                        String res = "Сектор: "+(sector+1)+"; Уровень: " + curLevel.get(codeInd) + "; Номер: " + (codeInd+1);
                        System.out.println(res);
                    }
                    if (found) break;
                }
                if (found) break;
            }
        }

        return found ? currentCode : null;
    }

    private List<ArrayList<ArrayList<Code>>> parseCodePage(HtmlPage page){
        //HtmlPage page =  webClient.getPage("http://dzrcc.tk/test/6.html");
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='zad']");
        String s = task.asXml().replace("\n","").replace("\r", "").replace(" ", "");
        //System.out.println(s);
        //System.out.println((int)s.toCharArray()[18]);

        Matcher matcher = sectorsPattern.matcher(s);
        List<ArrayList<ArrayList<Code>>> parsedSectors = new ArrayList<ArrayList<ArrayList<Code>>>();

        while (matcher.find()) {
            String codesStr = matcher.group();
            codesStr = codesStr.replace("основныекоды:", "").replace("<br","");
            //System.out.println(codesStr);
            ArrayList<String> codes = new ArrayList<String>(Arrays.asList(codesStr.split(",")));
            ArrayList<ArrayList<Code>> levels = new ArrayList<ArrayList<Code>>();
            ArrayList<Code> level = new ArrayList<Code>();
            level.add(createCode(codes.get(0), parsedSectors.size(), 1));
            levels.add(level);

            if (codes.size() > 1) {
                for (int i = 1; i < codes.size(); i++) {
                    level = levels.get(levels.size()-1);
                    Code curCode = createCode(codes.get(i), parsedSectors.size()+1, level.size()+1);
                    Code prevCode = level.get(level.size()-1);
                    if (curCode.getLevel().equals(prevCode.getLevel())){
                        level.add(curCode);
                    } else {
                        ArrayList<Code> newLevel = new ArrayList<Code>();
                        curCode.setNumberInLevel(1);
                        newLevel.add(curCode);
                        levels.add(newLevel);
                    }
                }
            }
            parsedSectors.add(levels);
        }

        return parsedSectors;
    }

    public HtmlPage login(String url, String login, String pass) throws IOException {
        HtmlPage page = null;
        // receiving a page
        page = webClient.getPage(url);

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
        }
        return page;
    }

    private Code createCode(String value, Integer sector, Integer num){
        Matcher curCodeMatcher = codePattern.matcher(value);
        String level = null;
        if (curCodeMatcher.find()) level = curCodeMatcher.group();
        return new Code(value, level, sector, num);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public String getGameLogin() {
        return gameLogin;
    }

    public void setGameLogin(String gameLogin) {
        this.gameLogin = gameLogin;
    }

    public String getGamePass() {
        return gamePass;
    }

    public void setGamePass(String gamePass) {
        this.gamePass = gamePass;
    }
}
