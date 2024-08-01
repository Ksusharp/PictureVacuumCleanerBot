package com.PictureVacuumCleanerBot.service;

import com.PictureVacuumCleanerBot.config.BotConfig;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//проходит ли проверка на валидацию когда в пуле ничего нет. потому что я как не вертела всегда один ответ
//поменять название метода checkIfValid
//SendChatAction(upload_photo); для того чтоб телаграм выводил что бот что то пишет или скидывает // работает, но сбрасывается с первым отправленным сообщением (так надо)
//parseJsonStringIfArray parseJsonStringIfObject можно ли сложить в 1? // нет?
//имена выводятся как BUN_BASH_|_Chapter_4_[by_peculiart] или Latex_Doll_TFTG_(by_KaeAskavi) или Zootopia_comic_by_そらふき или 食戟なアレ２ // сделала
//тайм ауты к обращениям поставить
//если планирую тысячу обращений в секунду там про обращения к https://e621.net/db_export/ было

//https://e621.net/pools.json?search[id]=337
//https://e621.net/posts/65605.json

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private CurrentState currentState = CurrentState.DEFAULT;
    static final String HELP_TEXT = "This the bot was created to parse e621.\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /lucky to get a random pool\n\n" +
            "Type /enterPool to get a specific pool\n\n" +
            "Type /help to see this message again";
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        processMessage(update.getMessage());
    }

    public void processMessage(Message message) {
        Long chatId = message.getChatId();

        if (message.hasText() && message.getText() != null) {
            if (currentState == CurrentState.POOL_ID_SELECT) {
                if (message.hasText()) {
                        String poolNumber = message.getText();
                        sendText(chatId, "gogogo");
                        //getPool(Integer.valueOf(poolNumber), chatId);
                    }
                    else {sendText(chatId,"I don't get it...");}
                    currentState = CurrentState.DEFAULT;
                    return;
            }
            switch (message.getText()) {
                case "/start": {
                    startCommandReceived(chatId);
                    break;
                }
                case "/lucky": {

                    getPool(chatId);

                    break;
                }
                case "/enterPool": {
                    sendText(chatId,"Enter a number between 1 and 40000");
                    currentState = CurrentState.POOL_ID_SELECT;

                    break;
                }
                case "/help": {
                    sendText(chatId, HELP_TEXT);
                    break;
                }

                default: sendText(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void getPool(Long chatId) { //, String poolNumber

        sendChatAction(chatId, "upload_photo");

        String jsonPoolString = getRandomPoolJson();

        //String jsonPoolString = getPoolJson(chatId, poolNumber);

        var jsonPool = parseJsonStringAsArray(jsonPoolString);
        var jsonPoolObject = (JSONObject) jsonPool.getFirst();
        //sendText(chatId, String.valueOf(jsonPoolObject));

        var name = (String) jsonPoolObject.get("name");
        name = name.charAt(0) + name.replaceAll("_", " ").substring(1, name.length()).toLowerCase();
        sendText(chatId, name);

        var id = (long) jsonPoolObject.get("id");
        sendText(chatId, String.valueOf(id));

        JSONArray postIds = (JSONArray) jsonPoolObject.get("post_ids");

        //var postCount = (long) jsonPoolObject.get("post_count");
        //sendText(chatId, String.valueOf(postCount));

        sendChatAction(chatId, "upload_photo");

        ArrayList<InputMedia> photos = new ArrayList<>();

        for (var postId : postIds) {
            String postUrl = "https://e621.net/posts/" + postId + ".json";
            var jsonPostString =  checkIfValid(postUrl);
            var jsonPost = parseJsonStringAsObject(jsonPostString);
            //sendText(chatId, String.valueOf(jsonPost));
            JSONObject JsonPostPost = (JSONObject) jsonPost.get("post");
            JSONObject JsonPostFile = (JSONObject) JsonPostPost.get("file");
            String JsonPostUrl = (String) JsonPostFile.get("url");
            //sendText(chatId, String.valueOf(JsonPostUrl));
            //sendText(chatId, "new iteration");

            if (JsonPostUrl != null) {
                InputMedia inputMedia = new InputMediaPhoto(JsonPostUrl);
                photos.add(inputMedia);
            }
        }

        if (!photos.isEmpty())
            sendPool(chatId, photos);
        else {
            sendText(chatId, "Empty :(((");
        }
    }

    private String getPoolJson(Long chatId,String number){

        String poolUrl = "https://e621.net/pools.json?search[id]=" + number;
        String jsonString = checkIfValid(poolUrl);

        if (jsonString.equals("[]")){
            sendText(chatId,"The pool is empty :(");
        }
        return jsonString;
    }

    private String getRandomPoolJson(){

        Random random = new Random();
        int luckyNumber;
        String jsonString = "[]";

        while (jsonString.equals("[]")){
            luckyNumber = random.nextInt(1,40000);
            String poolUrl = "https://e621.net/pools.json?search[id]=" + luckyNumber;
            jsonString = checkIfValid(poolUrl);
        }

        return jsonString;
    }

    private String checkIfValid(String OpenUrl) {
        URL url;
        try {
            url = new URL(OpenUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        String jsonString = openConnectionAndGetJson(url);

        return jsonString;
    }

    private String openConnectionAndGetJson(URL url) {

        int response;
        String result;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "my test app");
            connection.connect();
            response = connection.getResponseCode();
            result = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (response != 200) {
            throw new RuntimeException("HttpResponseCode: " + response);
        } else {
            return result;
        }
    }

    private JSONArray parseJsonStringAsArray(String jsonString) {
        JSONParser parse = new JSONParser();
        JSONArray dataObject;
        try {
            dataObject = (JSONArray) parse.parse(jsonString);
        } catch (ParseException e) {

            throw new RuntimeException(e);
        }
        return dataObject;
    }

    private JSONObject parseJsonStringAsObject(String jsonString) {
        JSONParser parser = new JSONParser();
        JSONObject dataObject;
        try {
            dataObject = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {

            throw new RuntimeException(e);
        }
        return dataObject;
    }

    private void startCommandReceived (long chatId) {

        String text = "Hi! Nice to meet you!";

        sendText(chatId, text);
    }

    private void sendPool(Long chatId, ArrayList<InputMedia> photos) {

        int chunkSize = 10;
        if (photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException("The list must have at least 1 element");
        }
        AtomicInteger counter = new AtomicInteger();
        List<ArrayList<InputMedia>> result = new ArrayList<>();

        for (InputMedia item : photos) {
            if (counter.getAndIncrement() % chunkSize == 0) {
                result.add(new ArrayList<>());
            }
            result.getLast().add(item);
        }
        for (ArrayList<InputMedia> item : result) {
            sendAlbum(chatId, item);
        }
    }

    private void sendChatAction(long chatId, String action) { //Use this method when you need to tell the user that something is happening on the bot's side
        SendChatAction sendChatAction = SendChatAction.builder()
                .chatId(String.valueOf(chatId))
                .action(action)
                .build();
        try {
            execute(sendChatAction);
        }
        catch (TelegramApiException e) {throw new RuntimeException();
        }
    }

    private void sendText(long chatId, String text) { //Use this method to send text
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(message);
        }
        catch (TelegramApiException e) {throw new RuntimeException();
        }
    }

    private void sendAlbum(long chatId, ArrayList<InputMedia> media) { //Use this method to send a group of photos or videos as an album
        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(String.valueOf(chatId))
                .medias(media)
                .build();
        try {
            execute(sendMediaGroup);
        }
        catch (TelegramApiException e) {throw new RuntimeException();
        }
    }

    private void sendPhoto(long chatId, String url) { //Use this method to send photos
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(String.valueOf(chatId))
                .photo(new InputFile(url))
                .build();
        try {
            execute(sendPhoto);
        }
        catch (TelegramApiException e) {throw new RuntimeException();
        }
    }
}