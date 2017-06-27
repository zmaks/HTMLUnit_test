package tk.dzrcc.game;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import tk.dzrcc.CodeHandler;
import tk.dzrcc.entities.Code;
import tk.dzrcc.exception.DozorBotException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tk.dzrcc.TextConstants.*;

/**
 * Created by Maksim on 18.01.2017.
 */
public class HtmlParser {
    private static Pattern sectorsPattern = Pattern.compile("(основныекоды|бонусныекоды):[<spanstyle=\"color:red\">1-3nul<\\/span>,+]+<br");
    //private Pattern sectorsPattern = Pattern.compile("основныекоды:((<spanstyle=\"color:red\">)?([1-3]|null)\\+?(<\\/span>)?,?)+<br");
    private static Pattern taskNumPattern = Pattern.compile("Задание\\s\\d{1,2}");
    private static Pattern timePattern = Pattern.compile("Время на уровне:.{9}");

    public static String parseTaskNumber(HtmlPage page){
        HtmlElement task = page.getBody();//.getFirstChild().getFirstByXPath("//div[@class='title']");
        if (task == null) return null;
        Matcher matcher = taskNumPattern.matcher(task.asText());
        String taskNum = "";
        if (matcher.find()) taskNum = matcher.group();
        return taskNum.replace("Задание ", "");
    }

    public static String parseTime(HtmlPage page) throws DozorBotException {
        HtmlElement task = page.getBody();
        if (task == null) throw new DozorBotException(CANNOT_READ_ENGINE_INFO);
        Matcher matcher = timePattern.matcher(task.asText());
        String time = "";
        if (matcher.find()) time = matcher.group();
        return time;
    }

    public static String parseSysMessage(HtmlPage page) throws DozorBotException {
        HtmlDivision task = (HtmlDivision)page.getBody().getFirstByXPath("//div[@class='sysmsg']");
        if (task == null) throw new DozorBotException(CANNOT_READ_SYSMESSAGE);
        return task.getTextContent();
    }

    public static List<ArrayList<Code>> parseCodePage(HtmlPage page) throws DozorBotException {
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
            codesStr = codesStr.replace("основныекоды:", "").replace("бонусныекоды:", "").replace("<br","");
            System.out.println(codesStr);
            ArrayList<String> codes = new ArrayList<String>(Arrays.asList(codesStr.split(",")));
            ArrayList<Code> sector = new ArrayList<Code>();

            if (!codes.isEmpty()) {
                for (int i = 0; i < codes.size(); i++) {
                    /*if (!parsedSectors.isEmpty()) {
                        sector = parsedSectors.get(parsedSectors.size() - 1);
                    }*/
                    Code code = CodeHandler.createCode(codes.get(i), parsedSectors.size()+1, sector.size()+1);
                    sector.add(code);
                }
            }
            parsedSectors.add(sector);
        }

        return parsedSectors;
    }

}
