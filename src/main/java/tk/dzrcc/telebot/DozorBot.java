package tk.dzrcc.telebot;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendSticker;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import tk.dzrcc.analyzer.CodeResponse;
import tk.dzrcc.analyzer.Game;
import tk.dzrcc.webclient.DzrWebClient;

/**
 * Created by Maksim on 17.01.2017.
 */
public class DozorBot extends TelegramLongPollingBot {
    private Game game;
    private Long chatId;

    private static final String TOKEN = "182854264:AAGIoBFz0VfwAIlWJwHPplZ2nH-JKHDzfNQ";

    public DozorBot(){
        game = new Game(
                "http://classic.dzzzr.ru/vrn/",
                "Zubrrr",
                "121212",
                "vrn_sorvi_golova",
                "973784"
        );
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            /*if(message.getChat().isGroupChat()){
                if (chatId == null) {
                    chatId = message.getChatId();
                } else if (!chatId.equals(message.getChatId())) {
                    return;
                }
            } else {
                forbidden(message.getChatId());
                return;
            }*/

            if (message.hasText()) {
                handleMessage(
                        message.getText(),
                        message.getChatId(),
                        message.getFrom(),
                        message.getMessageId()
                );
            }
        }
    }

    private void forbidden(Long chatId) {
        try {
            sendSticker(new SendSticker()
                    .setChatId(chatId)
                    .setSticker("BQADAgAD4wADN20QAvwxTMfig1etAg"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return "ISKRA_DZR";
    }

    public String getBotToken() {
        return TOKEN;
    }

    private void handleMessage(String message, Long chatId, User sender, Integer messageId){
        String command = message.replace("/", "");
        System.out.println(command);
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId)
                .setReplyToMessageId(messageId);
        if (StringUtils.isNumeric(command)){
            CodeResponse codeResponse = game.performCode(command, sender.getFirstName()+" "+sender.getLastName());
            sendMessage.setText(codeResponse.toString());
        }

        if (command.equals("restart")){
            sendMessage.setText(game.init());
        }

        try {
            sendMessage(sendMessage); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
