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

/**
 * Created by Maksim on 17.01.2017.
 */
public class DozorBot extends TelegramLongPollingBot {
    private Game game;
    private Long chatId;

    private static final Long ADMIN_CHAT_ID = 1L;
    private static final String TOKEN = "";
    private static final String HELP_TEXT = "На данный момент у бота две команды: /1234 и /status.\nПервая команда для вбивания кодов. То есть слэш + код, который надо вбить. В ответ на сообщение с кодом бот отправит всю необходимую информацию по нему. Если код принят, то будет указан сектор, код сложности (1,1+,2 и т.д.), порядковый номер в секторе (помогает, если организаторы на локе вписывают коды по порядку), кол-во взятых кодов. Если код не принят, тоже будет прислано соответствующее сообщение. Если бот ничего не ответил, значит код вряд ли был вбит в движок и с ботом что-то случилось. Прожолжать долбить его тем же сообщением не стоит.\nКоманда /status выводит полную информацию по взятым кодам на текущем задании.\n\nБот принимает коды только из одного группового чата!";

    public DozorBot(){
        game = new Game(
                "",
                "",
                "",
                "",
                ""
        );
    }

    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            /*if (message.getChatId().equals(ADMIN_CHAT_ID)) {
                handleAdminMessage(message);
            } else {
                if(message.getChat().isGroupChat()){
                    if (chatId == null) {
                        chatId = message.getChatId();
                    } else if (!chatId.equals(message.getChatId())) {
                        return;
                    }
                    if (message.hasText()) {
                        handleMessage(
                                message.getText(),
                                message.getChatId(),
                                message.getFrom(),
                                message.getMessageId()
                        );
                    }
                } else {
                    forbidden(message.getChatId());
                }
            }*/
            //System.out.println(message.getChatId());

            // TODO: 27.01.2017
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

    private void handleAdminMessage(Message message) {
        if (message.hasText()) {
            try {
                sendMessage(new SendMessage()
                        .setChatId(chatId)
                        .setText(message.getText()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (message.getSticker() != null) {
            try {
                sendSticker(new SendSticker()
                        .setChatId(chatId)
                        .setSticker(message.getSticker().getFileId()));
            } catch (TelegramApiException e) {
                e.printStackTrace();
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
        //System.out.println(command);
        SendMessage sendMessage = new SendMessage()
                .setChatId(chatId);

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

            sendMessage
                    .setText(codeResponse.toString())
                    .setReplyToMessageId(messageId);
        }

        if (command.equals("restart")){
            sendMessage.setText(game.init());
        }

        if (command.equals("status")){
            System.out.println(sender.getFirstName()+" "+sender.getLastName()+" запросил статус игры");

            sendMessage.setText(game.getGameStatus());
        }

        if (command.equals("help")){
            sendMessage.setText(HELP_TEXT);
        }

        if (command.equals("time")){
            sendMessage.setText(game.getTime());
        }

        if (command.toUpperCase().replace(" ","").contains("МИШАОБОСРАЛСЯ")){
            sendMessage.setText("Внатуре!?\n\n Код принят.");
        }

        try {
            if (sendMessage.getText() != null || sendMessage.getText() == "")
                sendMessage(sendMessage); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
