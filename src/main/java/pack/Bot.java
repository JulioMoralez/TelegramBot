package pack;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import pack.dao.AgentDao;
import pack.dao.AgentDaoMemory;
import pack.model.Agent;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static AgentDao agentDao;

    static AgentDao getAgentDao() {
        return agentDao;
    }

    Bot(){
        System.getProperties().put( "proxySet", "true" );
        System.getProperties().put( "socksProxyHost", "127.0.0.1" );
        System.getProperties().put( "socksProxyPort", "9150" );

        agentDao = new AgentDaoMemory();
        System.loadLibrary("opencv_java411");
        System.out.println("dll OK");
    }

    public static void main(String[] args) {

        System.out.println("START");

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        Bot bot = new Bot();
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //отправка простого сообщения
    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //отправка статистики игрока
    private void sendStat(Message message, String nick) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        String text="";
        if (!nick.equals("")){
            Agent agent = agentDao.findByField("nick",nick);
            if (agent==null) {
                text="Агент в базе не найден";
            }
            else {
                text="Ник: " + agent.getNick() +
                        "\nУровень: " + agent.getLevel() +
                        "\nAP: " + agent.getAp();
                if (agent.getFraction()==1){
                    text=text+"\nФракция: Resistance";
                }
                else if (agent.getFraction()==2){
                    text=text+"\nФракция: Enlightened";
                }
                if (agent.getBuilder()!=null){
                    text=text+"\nBuilder: " + agent.getBuilder();
                }
                if (agent.getPurifier()!=null){
                    text=text+"\nPurifier: " + agent.getPurifier();
                }
                if (agent.getTrekker()!=null){
                    text=text+"\nTrekker: " + agent.getTrekker();
                }
                if (agent.getRecharger()!=null){
                    text=text+"\nRecharger: " + agent.getRecharger();
                }
            }
        }
        else {
            text="Картинка не распознана";
        }
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public synchronized void setButtons(SendMessage sendMessage) {
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("/fraction Enl"));
        KeyboardButton keyboardButton = new KeyboardButton();

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("/fraction Res"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    //слушаем запросы
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            System.out.println(message.getText());
            //sendMsg(message, "Привет");
            sendStat(message,message.getText());
        }
        if ((message!=null) && (message.hasPhoto())){
            List<PhotoSize> photos = message.getPhoto();
            for (PhotoSize photo : photos) {
                if (photo.getWidth() < 400) {
                    if (photo.getHeight() > 1200) {
                        sendMsg(message, "Скрин слишком длинный");
                    }
                    continue;
                } else {
                    if (photo.getHeight() < 900) {
                        continue;
                    }
                }
                new Thread(new PhotoThread(photo,message)).start();
            }
        }
    }

    //получаем фото и отправляем на обработку
    class PhotoThread implements Runnable {

        private PhotoSize photo;
        private Message message;

        public PhotoThread(PhotoSize photo, Message message) {
            this.photo = photo;
            this.message = message;
        }

        @Override
        public void run() {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(photo.getFileId());
            File file = null;
            try {
                file = getFile(getFileRequest);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            System.out.println(file.getFilePath());
            try {
                java.io.File fileFromSystem = downloadFile(file.getFilePath());
                Parser parser = new Parser(fileFromSystem.getName() + ".jpg");

                java.io.File newName = new java.io.File(parser.getImageIn());
                fileFromSystem.renameTo(newName);
                System.out.println(fileFromSystem.getName());
                parser.parsIt();
                Agent agent = parser.getAgent();
                if (!agent.getNick().equals("")) {
                    sendStat(message,agent.getNick());
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public String getBotUsername() {
        return "JulioMoralezBot";
    }

    public String getBotToken() {
        return "749210775:AAFGRWdjgtymB3Vwj9N9shLKXw7VtrT60nk";
    }
}
