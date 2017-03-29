package tk.dzrcc;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import tk.dzrcc.telebot.DozorBot;

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
