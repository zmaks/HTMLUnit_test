package tk.dzrcc;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import tk.dzrcc.analyzer.Code;
import tk.dzrcc.telebot.DozorBot;
import tk.dzrcc.webclient.DzrWebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mazh0416 on 11/12/2016.
 */
public class Main {

    public static void main(String[] args) {
        initBot();
    }

    private static  void  initBot(){
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new DozorBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

/*
    static HtmlPage inputCode(String code) throws IOException {
        WebClient webClient;
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);



        List<ArrayList<ArrayList<Code>>> sectors = new ArrayList<ArrayList<ArrayList<Code>>>();
        HtmlPage page =  webClient.getPage("http://dzrcc.tk/test/4.html");

        HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='sysmsg']");
        Pattern taskNumPattern = Pattern.compile("Задание\\s\\d{1,2}");
        Matcher matcher = taskNumPattern.matcher(task.asText());
        System.out.println(task.getTextContent());
        //HtmlDivision task = (HtmlDivision)page.getBody().getFirstChild().getFirstByXPath("//div[@class='zad']");
        String s = task.asXml().replace("\n","").replace("\r", "").replace(" ", "");
        System.out.println(s);
        //System.out.println((int)s.toCharArray()[18]);
        Pattern pattern = Pattern.compile("основные.+<br");
        //Matcher matcher = pattern.matcher(s);
        List<ArrayList<ArrayList<Code>>> parsedSectors = new ArrayList<ArrayList<ArrayList<Code>>>();

        while (matcher.find()) {
            String codesStr = matcher.group();
            codesStr = codesStr.replace("основныекоды:", "").replace("<br","");
            System.out.println(codesStr);

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

        ArrayList<ArrayList<Code>> curSector;
        ArrayList<Code> curLevel;
        Code currentCode;
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

        //System.out.print(task.asXml());
       *//* HtmlForm form = page.getForms().get(0);
        form.getInputByName("cod").setValueAttribute(code);
        HtmlElement submit = form.getElementsByAttribute("input", "type", "submit").get(0);

        page = submit.click();*//*
        return page;
    }

    private static Code createCode(String value, Integer sector, Integer num){
        Pattern codePattern = Pattern.compile("\\d\\+?");
        Matcher curCodeMatcher = codePattern.matcher(value);
        String level = null;
        if (curCodeMatcher.find()) level = curCodeMatcher.group();
        return new Code(value, level, sector, num);
    }*/

}
