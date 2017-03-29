package tk.dzrcc.telebot;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendSticker;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import tk.dzrcc.entities.CodeResponse;
import tk.dzrcc.exception.DozorBotException;
import tk.dzrcc.game.Game;

import static tk.dzrcc.TextConstants.HELP_TEXT;
import static tk.dzrcc.TextConstants.NO_CONNECTION;

/**
 * Created by Maksim on 17.01.2017.
 */
public class DozorBot extends TelegramLongPollingBot {
    private Game game;
    private Long chatId;

    private static final Long ADMIN_CHAT_ID = 183375382L;
    private static final String TOKEN = "182854264:AAGIoBFz0VfwAIlWJwHPplZ2nH-JKHDzfNQ";

    public DozorBot(){

    }

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
                //System.out.println(message.getChatId());

            /*if (message.hasText()) {
                handleMessage(
                        message.getText(),
                        message.getChatId(),
                        message.getFrom(),
                        message.getMessageId()
                );
            }*/

            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAdminMessage(Message message) throws TelegramApiException {
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

    private String connect() throws TelegramApiException {
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
        String command = messageText.replace("/", "");
        //System.out.println(command);
        /*SendMessage sendMessage = new SendMessage()
                .setChatId(chatId);*/

        if (sender.getId().longValue()==ADMIN_CHAT_ID&&command.contains("connect")) {
            String[] params = command.split(" ");
            if (params.length == 3) {
                game = new Game(
                        "http://classic.dzzzr.ru/vrn/",
                        "Zubr2",
                        "121212",
                        params[1],
                        params[2]
                );
                connect();
                return;
            }
        }

        if (StringUtils.isNumeric(command)){
            System.out.println(sender.getFirstName()+" "+sender.getLastName()+" отправил код "+command);

            String player = "";
            if (sender.getFirstName() != null){
                player = sender.getFirstName();
            } else if (sender.getLastName() != null){
                player +=" "+sender.getLastName();
            } else
                player = "кем-то";
            CodeResponse codeResponse = game.performCode(command, player);
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(codeResponse.toString())
                    .setReplyToMessageId(messageId));

            return;
        }

        if (command.equals("help")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(HELP_TEXT));
            return;
        }

        if (command.equals("reconnect")){
            if (game != null)
                connect();
            else
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(NO_CONNECTION));
            return;
        }

        if (command.equals("status")){
            System.out.println(sender.getFirstName()+" "+sender.getLastName()+" запросил статус игры");
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(game == null ? NO_CONNECTION : game.getGameStatus()));
            return;
        }

        if (command.equals("time")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText(game == null ? NO_CONNECTION : game.getTime()));
            return;
        }

        if (command.toUpperCase().replace(" ","").contains("МИШАОБОСРАЛСЯ")){
            sendMessage(new SendMessage()
                    .setChatId(chatId)
                    .setText("Внатуре!?\n\n Код принят."));
        }
    }

}
