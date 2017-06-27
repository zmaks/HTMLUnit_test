package tk.dzrcc.game;

import com.gargoylesoftware.htmlunit.html.*;
import tk.dzrcc.TextConstants;
import tk.dzrcc.entities.Code;
import tk.dzrcc.entities.CodeResponse;
import tk.dzrcc.entities.GottenCode;
import tk.dzrcc.exception.DozorBotException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static tk.dzrcc.TextConstants.*;
import static tk.dzrcc.game.CodesAnalyzer.analyzeCodes;
import static tk.dzrcc.game.CodesAnalyzer.getLevelStatistic;
import static tk.dzrcc.game.CodesAnalyzer.getSectorStatistic;
import static tk.dzrcc.game.HtmlParser.*;

/**
 * Created by Maksim on 18.01.2017.
 */
public class Game {

    private WebService webService;

    private List<ArrayList<Code>> sectors;
    private List<GottenCode> gottenCodes;
    private String currentTaskNumber = "0";


    public Game(String url, String login, String pass, String gameLogin, String gamePass) {
        webService = new WebService(url, login, pass, url+"/go", gameLogin, gamePass);

        //init();
    }

    public String init() throws DozorBotException {
        sectors = new ArrayList<ArrayList<Code>>();
        gottenCodes = new ArrayList<GottenCode>();
        String message = "";
        try {
            webService.login();
            message+= TextConstants.SUCCESS_AUTH;
            gameLoad();
            message+= TextConstants.SUCCESS_LOADED;
        } catch (IOException | DozorBotException e) {
            e.printStackTrace();
            throw new DozorBotException(message +e.getMessage());
        }

        return message;

    }

    private void gameLoad() throws DozorBotException {
        HtmlPage gamePage;
        try {
            gamePage = webService.getGamePage();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DozorBotException(CANNOT_LOAD_PAGE);
        }
        //System.out.println(gamePage.getBody().asXml());
        String taskNum = parseTaskNumber(gamePage);
        if (taskNum == null || taskNum.isEmpty())
            throw new DozorBotException(CANNOT_FIND_TASK_NUMBER);
        System.out.println("Номер задания определен: "+taskNum);

        List<ArrayList<Code>> parsedCodes = parseCodePage(gamePage);
        if (!taskNum.equals(currentTaskNumber)){
            System.out.println(". Это новое задание!");
            currentTaskNumber = taskNum;
            sectors = parsedCodes;
            gottenCodes.clear();
        }

        if (sectors.isEmpty())
            sectors = parsedCodes;
        analyzeCodes(parsedCodes, sectors,null);
    }

    public synchronized CodeResponse performCode(String code, String player) {
        Code inputCode;
        String sysMessage;
        try {
            gameLoad();

            HtmlPage codePage;
            codePage = webService.inputCode(code);

            sysMessage = parseSysMessage(codePage);
            System.out.println(sysMessage);

            inputCode = analyzeCodes(parseCodePage(codePage), sectors, code);
        } catch (DozorBotException | IOException e) {
            return new CodeResponse(e.getMessage(), null);
        }

        String key = sysMessage.contains("Код не принят") ? NOT_TAKEN_CODE : TAKEN_CODE;
        if (inputCode == null) {
            GottenCode gottenCode = gottenCodes.stream()
                    .filter(x -> x.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
            return new CodeResponse(key+"\n"+sysMessage, null, gottenCode);
        } else {
            gottenCodes.add(new GottenCode(code, player, new Date()));
            CodeResponse codeResponse = new CodeResponse(key+"\n"+sysMessage, inputCode);
            codeResponse.setLevelStat(getLevelStatistic(sectors, inputCode.getSector(), inputCode.getLevel()));
            codeResponse.setSectorStat(getSectorStatistic(sectors, inputCode.getSector()));
            return codeResponse;
        }
    }

    public String getTime(){
        HtmlPage page = null;
        try {
            page = webService.getGamePage();
        } catch (IOException e) {
            e.printStackTrace();
            return CANNOT_LOAD_PAGE;
        }
        String response;
        try {
            response = "⏱"+parseTime(page);
        } catch (DozorBotException e) {
            return e.getMessage();
        }
        return response;
    }

    public String getGameStatus(){
        //analyzeCodes(parseCodePage(gameLoad()), null);
        try {
            gameLoad();
        } catch (DozorBotException e) {
            return e.getMessage();
        }
        StringBuilder response = new StringBuilder();
        response.append("ℹ Задание №");
        response.append(currentTaskNumber);
        response.append(" ℹ\n");

        for (int i = 0; i < sectors.size(); i++) {
            if (sectors.size() != 1) {
                response.append("\n");
                response.append("Сектор ");
                response.append(i+1);
                response.append(":\n");
            }
            //response.append("\n");
            for (int j = 0; j < sectors.get(i).size(); j++) {
                ArrayList<Code> codes = sectors.get(i);
                response.append((j+1));
                response.append(". ");
                Code curCode = codes.get(j);
                if (curCode.getGotten())
                    response.append(TAKEN_CODE);
                else
                    response.append(NOT_TAKEN_CODE);
                response.append(" (");
                response.append(curCode.getLevel());
                response.append(")");
                if (curCode.getCode() != null){
                    response.append(" - ");
                    response.append(curCode.getCode());
                }
                response.append("\n");
            }
            List<String> list = sectors.get(i)
                    .stream()
                    .distinct()
                    .map(Code::getLevel)
                    .collect(Collectors.toList());
            if (list.size()>1) {
                response.append("По уровням опасности:\n");
                for (int j = 0; j < list.size(); j++) {
                    response.append("(");
                    response.append(list.get(j));
                    response.append(") - ");
                    response.append(getLevelStatistic(sectors,i+1, list.get(j)));
                    if (list.size()-1 != j) response.append(", ");
                }
                response.append("\n");
            }
            response.append("Взято");
            if (sectors.size() > 1) response.append(" в секторе");
            response.append(":");
            response.append(getSectorStatistic(sectors,i+1));
            response.append("\n");
        }
        return response.toString();
    }

}
