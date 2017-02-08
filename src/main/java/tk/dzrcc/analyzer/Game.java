package tk.dzrcc.analyzer;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import tk.dzrcc.exception.DozorBotException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private List<ArrayList<Code>> sectors;
    private List<GottenCode> gottenCodes;
    private String currentTaskNumber = "0";

    private Pattern sectorsPattern = Pattern.compile("основныекоды:[<spanstyle=\"color:red\">1-3<\\/span>,+]+<br");
    private Pattern codePattern = Pattern.compile("\\d\\+?");
    private Pattern taskNumPattern = Pattern.compile("Задание\\s\\d{1,2}");
    private Pattern timePattern = Pattern.compile("Время на уровне:.{9}");

    private static final String CANNOT_INPUT_CODE = "Ошибка. Не получается вбить код :(";
    private static final String CANNOT_LOAD_PAGE = "Что-то не получется загрузить страницу движка...";
    private static final String CANNOT_FIND_TASK_NUMBER = "Что-то пошло не так. Не могу найти номер задания на странице движка.";
    private static final String STRANGE_SITUATION = "Код принят, но почему-то не могу определить какой из. Зайдите на движок и чекните последние события игры.";
    private static final String CANNOT_LOAD_LOGIN_PAGE = "Ошибка. Не получается загрузить страницу для ввода логина и пароля пользователя. Возможно, указан неправильный адрес.";
    private static final String CANNOT_READ_CODE_PAGE = "Ошибка. Не получается прочитать страницу движка. Возможно, указан неправильный адрес.";
    private static final String CANNOT_READ_SYSMESSAGE = "Ошибочка... Не получается прочитать сообщение от движка. Возможно, указан неправильный адрес.";
    private static final String CANNOT_READ_ENGINE_INFO = "Ошибка. Не получается прочитать информацию на движке. Возможно, указан неправильный адрес.";

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

        sectors = new ArrayList<ArrayList<Code>>();
        gottenCodes = new ArrayList<GottenCode>();
        try {
            login(url, login, pass);
            firstGameLoad();
        } catch (IOException | DozorBotException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "Авторизация прошла успешно.\n";

    }

    private void firstGameLoad() throws DozorBotException {
        HtmlPage gamePage;
        try {
            gamePage = getGamePage();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DozorBotException(CANNOT_LOAD_PAGE);
        }
        String taskNum = parseTaskNumber(gamePage);
        if (taskNum == null)
            throw new DozorBotException(CANNOT_FIND_TASK_NUMBER);
        System.out.print("Номер задания определен: "+taskNum);

        if (!taskNum.equals(currentTaskNumber)){
            System.out.println(". Это новое задание!");
            currentTaskNumber = taskNum;
            sectors = parseCodePage(gamePage);
            gottenCodes.clear();
        }

        if (sectors.isEmpty())
            sectors = parseCodePage(gamePage);
    }

    public synchronized CodeResponse performCode(String code, String player) {
        try {
            firstGameLoad();
        } catch (DozorBotException e) {
            return new CodeResponse(e.getMessage(), null);
        }
        HtmlPage codePage;
        try {
            codePage = inputCode(code);
        } catch (IOException | DozorBotException e) {
            e.printStackTrace();
            return new CodeResponse(e.getMessage(), null);
        }

        String sysMessage;
        try {
            sysMessage = parseSysMessage(codePage);
        } catch (DozorBotException e){
            return new CodeResponse(e.getMessage(), null);
        }
        System.out.println(sysMessage);
        Code inputCode;
        try {
            inputCode = analyzeCodes(parseCodePage(codePage), code);
        } catch (DozorBotException e){
            return new CodeResponse(e.getMessage(), null);
        }

        if (inputCode == null) {
            GottenCode gottenCode = gottenCodes.stream().filter(x -> x.getCode().equals(code)).findFirst().orElse(null);
            return new CodeResponse(sysMessage, null, gottenCode);
        } else {
            gottenCodes.add(new GottenCode(code, player, new Date()));
            CodeResponse codeResponse = new CodeResponse(sysMessage, inputCode);
            codeResponse.setLevelStat(getLevelStatistic(sectors, inputCode.getSector(), inputCode.getLevel()));
            codeResponse.setSectorStat(getSectorStatistic(sectors, inputCode.getSector()));
            return codeResponse;
        }
    }

    private String getLevelStatistic(List<ArrayList<Code>> sectors, Integer sector, String level) {
        if (sectors.get(sector-1).size()==1) return null;
        List<Code> levelList = sectors.get(sector-1)
                .stream()
                .filter(x -> x.getLevel().equals(level))
                .collect(Collectors.toList());

        Long countFound = levelList
                .stream()
                .filter(x -> x.getGotten())
                .count();
        return countFound+"/"+levelList.size();
    }

    private String getSectorStatistic(List<ArrayList<Code>> sectors, Integer sector){
        Long countGottenInSector = 0L;
        countGottenInSector += sectors.get(sector-1)
                .stream()
                .filter(x -> x.getGotten())
                .count();
        return countGottenInSector+"/"+sectors.get(sector-1).size();
    }

    public String getTime(){
        HtmlPage page = null;
        try {
            page = getGamePage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String response;
        try {
            response = parseTime(page);
        } catch (DozorBotException e) {
            return e.getMessage();
        }
        return response;
    }

    public String getGameStatus(){
        firstGameLoad();
        StringBuilder response = new StringBuilder();
        response.append("Инфа по заданию №");
        response.append(currentTaskNumber);

        for (int i = 0; i < sectors.size(); i++) {
            if (sectors.size() != 1) {
                response.append("\n\n");
                response.append("Сектор ");
                response.append(i+1);
                response.append(":\n");
            }
            for (int j = 0; j < sectors.get(i).size(); j++) {
                ArrayList<Code> codes = sectors.get(i);
                response.append((j+1)+") ");
                Code curCode = codes.get(j);
                if (curCode.getGotten())
                    response.append(curCode.getCode());
                else
                    response.append("не взят");
                response.append(" (");
                response.append(curCode.getLevel());
                response.append(")\n");
            }
            List<String> list = sectors.get(i)
                    .stream()
                    .distinct()
                    .map(x -> x.getLevel())
                    .collect(Collectors.toList());
            if (list.size()>1) {
                response.append("По уровням сложности:\n");
                for (int j = 0; j < list.size(); j++) {
                    response.append("(");
                    response.append(list.get(j));
                    response.append(") - ");
                    response.append(getLevelStatistic(sectors,i+1, list.get(j)));
                    if (list.size()-1 != j) response.append(", ");
                }
            }
            response.append("\nВзято");
            if (sectors.size() > 1) response.append(" в секторе");
            response.append(":");
            response.append(getSectorStatistic(sectors,i+1));
        }
        return response.toString();
    }

    private String parseTaskNumber(HtmlPage page){
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='title']");
        if (task == null) return null;
        Matcher matcher = taskNumPattern.matcher(task.asText());
        String taskNum = "";
        if (matcher.find()) taskNum = matcher.group();
        return taskNum.replace("Задание ", "");
    }

    private String parseTime(HtmlPage page) throws DozorBotException{
        HtmlElement task = page.getBody();
        if (task == null) throw new DozorBotException(CANNOT_READ_ENGINE_INFO);
        Matcher matcher = timePattern.matcher(task.asText());
        String time = "";
        if (matcher.find()) time = matcher.group();
        return time;
    }

    private String parseSysMessage(HtmlPage page){
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='sysmsg']");
        if (task == null) throw new DozorBotException(CANNOT_READ_SYSMESSAGE);
        return task.getTextContent();
    }

    private HtmlPage inputCode(String code) throws IOException, DozorBotException {
        HtmlPage page = getGamePage();
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
        // TODO: 27.01.2017 change
        //return page;
        return getGamePage();
    }

    private HtmlPage getGamePage() throws IOException {
        /*DefaultCredentialsProvider provider = new DefaultCredentialsProvider();
        provider.addCredentials(gameLogin, gamePass);
        webClient.setCredentialsProvider(provider);
        return webClient.getPage(gameUrl);*/
        // TODO: 27.01.2017 change
        return webClient.getPage("http://dzrcc.tk/");
    }

    private Code analyzeCodes(List<ArrayList<Code>> parsedSectors, String codeVal){
        ArrayList<Code> curSector;
        Code currentCode = null;
        Code parsedCode;
        boolean found = false;

        if (!sectors.isEmpty()){
            for(int sector = 0; sector < parsedSectors.size(); sector++){
                curSector = parsedSectors.get(sector);
                for(int code = 0; code < curSector.size(); code++){
                    currentCode = sectors.get(sector).get(code);
                    parsedCode = parsedSectors.get(sector).get(code);
                    if (parsedCode.getValue().length()>4 && !currentCode.getGotten()){
                        currentCode.setGotten(true);
                        currentCode.setCode(codeVal);
                        found = true;
                    }
                    if (found) break;
                }
                if (found) break;
            }
        }

        return found ? currentCode : null;
    }

    private List<ArrayList<Code>> parseCodePage(HtmlPage page){
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='zad']");
        if (task == null) throw new DozorBotException(CANNOT_READ_CODE_PAGE);
        //System.out.println(task.asXml());
        String s = task.asXml().replace("\n","").replace("\r", "").replace(" ", "");
        System.out.println(s);
        Matcher matcher = sectorsPattern.matcher(s);
        List<ArrayList<Code>> parsedSectors = new ArrayList<ArrayList<Code>>();

        while (matcher.find()) {
            String codesStr = matcher.group();
            System.out.println(codesStr+"\n\n");
            codesStr = codesStr.replace("основныекоды:", "").replace("<br","");
            System.out.println(codesStr);
            ArrayList<String> codes = new ArrayList<String>(Arrays.asList(codesStr.split(",")));
            ArrayList<Code> sector = new ArrayList<Code>();

            if (!codes.isEmpty()) {
                for (int i = 0; i < codes.size(); i++) {
                    /*if (!parsedSectors.isEmpty()) {
                        sector = parsedSectors.get(parsedSectors.size() - 1);
                    }*/
                    Code code = createCode(codes.get(i), parsedSectors.size()+1, sector.size()+1);
                    sector.add(code);
                }
            }
            parsedSectors.add(sector);
        }

        return parsedSectors;
    }

    public HtmlPage login(String url, String login, String pass) throws IOException, DozorBotException {
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
