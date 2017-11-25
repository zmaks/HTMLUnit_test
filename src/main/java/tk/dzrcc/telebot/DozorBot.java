package tk.dzrcc.telebot;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendSticker;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import tk.dzrcc.TextConstants;
import tk.dzrcc.entities.CodeResponse;
import tk.dzrcc.exception.DozorBotException;
import tk.dzrcc.game.Game;

import static tk.dzrcc.TextConstants.HELP_TEXT;
import static tk.dzrcc.TextConstants.INIT_CONNECTION;
import static tk.dzrcc.TextConstants.NO_CONNECTION;

/**
 * Created by Maksim on 17.01.2017.
 */
public class DozorBot extends TelegramLongPollingBot {
    private Game game;
    private Long chatId;

    private static final Long ADMIN_CHAT_ID = 183375382L;
    private static final String TOKEN = "182854264:AAGIoBFz0VfwAIlWJwHPplZ2nH-JKHDzfNQ";
    private boolean pause = false;


    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();

                if (message.getChatId().equals(ADMIN_CHAT_ID)) {
                    handleAdminMessage(message);
                } else {
                    if (message.getChat().isGroupChat()) {
                        if (chatId == null) {
                            chatId = message.getChatId();
                        } else if (!chatId.equals(message.getChatId())) {
                            return;
                        }
                        if (message.hasText()) {
                            handleMessage(message);
                        }
                    } else {
                        forbidden(message.getChatId());
                    }
                }
                /*chatId = ADMIN_CHAT_ID;
                handleMessage(message);*/

            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminMessage(Message message) throws TelegramApiException {
        if (chatId == null) {
            sendMessage(new SendMessage()
                    .setChatId(ADMIN_CHAT_ID)
                    .setText("Ok"));
            return;
        }
        if (message.hasText()) {

            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(message.getText()));
            return;
        }
        if (message.getSticker() != null) {
            sendSticker(new SendSticker()
                    .setChatId(chatId)
                    .setSticker(message.getSticker().getFileId()));
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

    private String connect(String login, String pass) throws TelegramApiException {
        sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(INIT_CONNECTION));
        game = new Game(
                "http://classic.dzzzr.ru/vrn/",
                //"http://classic.dzzzr.ru/mega-vlg/",
                "Zubr2",
                "121212",
                login,
                pass
        );
        String m = "";
        try {
            m = game.init();
        } catch (DozorBotException e) {
            m = e.getMessage();
            game = null;
        }
        sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(m));
        return m;
    }

    public String getBotUsername() {
        return "ISKRA_DZR";
    }

    public String getBotToken() {
        return TOKEN;
    }

    private void handleMessage(Message message) throws TelegramApiException {
        String messageText = message.getText();
        Long chatId = message.getChatId();
        User sender = message.getFrom();
        Integer messageId = message.getMessageId();
        String command = messageText;
        //System.out.println(command);
        /*SendMessage sendMessage = new SendMessage()
                .setChatId(chatId);*/


        if (command.contains("/pause")) {
            pause = true;
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(TextConstants.PAUSE_MESSAGE));
            return;
        }

        if (command.contains("/resume")) {
            pause = false;
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(TextConstants.RESUME_MESSAGE));
            return;
        }

        if (sender.getId().longValue()==ADMIN_CHAT_ID&&command.contains("/connect")) {
            String[] params = command.split(" ");
            if (params.length == 3) {

                connect(params[1], params[2]);
                return;
            }
        }

        if (StringUtils.isNumeric(command.replace("/","")) && game != null){
            System.out.println(sender.getFirstName()+" "+sender.getLastName()+" отправил код "+command);
            if (pause) {
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(TextConstants.GAME_IN_PAUSE)
                );
            } else {
                CodeResponse codeResponse = game.performCode(command, prettyPrintUserName(sender));
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(codeResponse.toString())
                        .setReplyToMessageId(messageId));
            }

            return;
        }

        if (command.toUpperCase().replace(" ","").contains("МИШАОБОСРАЛСЯ")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Код принят"));
        }

        if (command.contains("/вбей")&&game != null){
            String[] params = command.split(" ");
            if (pause) {
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(TextConstants.GAME_IN_PAUSE)
                );
            } else {
                if (params.length == 2) {
                    CodeResponse codeResponse = game.performCode(command.replace("/вбей ", ""), prettyPrintUserName(sender));
                    sendMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText(codeResponse.toString())
                            .setReplyToMessageId(messageId));

                }
            }
            return;
        }



        if (command.contains("/help")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(HELP_TEXT));
            return;
        }

        /*if (command.equals("reconnect")){
            if (game != null)
                connect();
            else
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(NO_CONNECTION));
            return;
        }*/

        if (command.contains("/status")){
            System.out.println(sender.getFirstName()+" "+sender.getLastName()+" запросил статус игры");
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(game == null ? NO_CONNECTION : game.getGameStatus()));
            return;
        }

        if (command.contains("/time")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(game == null ? NO_CONNECTION : game.getTime()));
            return;
        }

        if (command.contains("start") || command.contains("stop")||command.contains("screen")){
            return;
        }

        if (command.contains("/")&&game != null){
            String[] params = command.split(" ");
            if (pause) {
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(TextConstants.GAME_IN_PAUSE)
                );
            } else {

                    CodeResponse codeResponse = game.performCode(command.replace("/", ""), prettyPrintUserName(sender));
                    sendMessage(new SendMessage()
                            .setChatId(chatId)
                            .setText(codeResponse.toString())
                            .setReplyToMessageId(messageId));


            }
            return;
        }

    }

    private String prettyPrintUserName(User sender) {
        String player = "кем-то";
        if (sender.getFirstName() != null){
            player = sender.getFirstName();
            if (sender.getLastName() != null) {
                player += " " + sender.getLastName();
            }
        } else
            player = sender.getUserName();
        return player;
    }

}
