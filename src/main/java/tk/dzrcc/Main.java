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

}
