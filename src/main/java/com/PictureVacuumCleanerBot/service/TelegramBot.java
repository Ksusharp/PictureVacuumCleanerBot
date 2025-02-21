package com.PictureVacuumCleanerBot.service;

import com.PictureVacuumCleanerBot.config.BotConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.PictureVacuumCleanerBot.service.HelpText.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private CurrentState currentState = CurrentState.DEFAULT;

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

        switch (currentState) {
            case DEFAULT -> handleCommand(chatId, message);
            case POOL_ID_SELECT -> {
                if (message.hasText()) {
                    String poolNumber = message.getText();
                    String jsonString = getPoolJson(chatId, poolNumber);
                    getPool(chatId, jsonString);
                } else {
                    sendText(chatId, "Huh, man, it's too much...");
                }
                currentState = CurrentState.DEFAULT;
            }
            case POST_ID_SELECT -> {
                if (message.hasText()) {
                    String postTags = message.getText();
                    String postsByTagsJson = PageWithPostsByTagsJson(postTags);
                    getPosts(chatId, postsByTagsJson);
                } else {
                    sendText(chatId, "Huh, man, it's too much...");
                }
                currentState = CurrentState.DEFAULT;
            }
        }
    }

    private void handleCommand(Long chatId, Message message) {
        switch (message.getText()) {
            case "/start" -> startCommandReceived(chatId);
            case "/help" -> sendText(chatId, HELP_TEXT);
            case "/tagGuide" -> {
                sendText(chatId, TAG_GUIDE_How_To_Request);
                sendText(chatId, TAG_GUIDE_Basics);
                sendText(chatId, TAG_GUIDE_Sexually_Explicit);
                sendText(chatId, TAG_GUIDE_Pose_Activity_Appearance);
                sendText(chatId, TAG_Information_And_Requests);
                sendText(chatId, TAG_GUIDE_Do_Not_Tag);
            }
            case "/luckyPool" -> {
                String jsonString = getRandomPoolJson();
                getPool(chatId, jsonString);
            }
            case "/enterPool" -> {
                sendText(chatId, "Enter a number between 1 and 40000");
                currentState = CurrentState.POOL_ID_SELECT;
            }
            case "/luckyPost" -> {
                String luckyPostsJson = RandomPageWithPostsJson();
                getPosts(chatId, luckyPostsJson);
            }
            case "/enterPost" -> {
                sendText(chatId, "Enter tags");
                currentState = CurrentState.POST_ID_SELECT;
            }
            default -> sendText(chatId, "What?...");
        }
    }

    private void getPool (Long chatId, String jsonString) {

        var jsonPool = parseJsonStringAsArray(jsonString);
        var jsonPoolObject = (JSONObject) jsonPool.getFirst();

        var name = (String) jsonPoolObject.get("name");
        name = name.charAt(0) + name.replaceAll("_", " ").substring(1, name.length()).toLowerCase();
        sendText(chatId, name);

        var id = (long) jsonPoolObject.get("id");
        sendText(chatId, String.valueOf(id));

        JSONArray postIds = (JSONArray) jsonPoolObject.get("post_ids");

        sendChatAction(chatId, "upload_photo");

        ArrayList<InputMedia> photoUrls = getInputMediaWithPhotoUrls(postIds, chatId);

        if (!photoUrls.isEmpty())
            sendPool(chatId, photoUrls);
        else {
            sendText(chatId, "Empty :(((");
        }
    }

    private void getPosts(Long chatId, String jsonString) {

        var jsonPostsObject = parseJsonStringAsObject(jsonString);

        var jsonPosts = (JSONArray) jsonPostsObject.get("posts");

        Random random = new Random();
        int luckyIndex = random.nextInt(0, 310);
        JSONArray postIds = new JSONArray();

        for (int i = 0; i < 10; i++) {
            var postId = (JSONObject) jsonPosts.get(luckyIndex);
            postIds.add(postId.get("id"));
            luckyIndex++;
        }

        ArrayList<InputMedia> photoUrls = getInputMediaWithPhotoUrls(postIds, chatId);
        sendAlbum(chatId, photoUrls);
    }

    public ArrayList<InputMedia> getInputMediaWithPhotoUrls(JSONArray postIds, long chatId){
        ArrayList<InputMedia> photoUrls = new ArrayList<>();

        Set<String> validTypes = new HashSet<>(Arrays.asList("jpg", "png"));

        for (var postId : postIds) {
            String postUrl = "https://e621.net/posts/" + postId + ".json?";
            var jsonPostString = openConnectionAndGetJson(postUrl);

            var jsonPost = parseJsonStringAsObject(jsonPostString);
            JSONObject JsonPostPost = (JSONObject) jsonPost.get("post");
            JSONObject JsonPostFile = (JSONObject) JsonPostPost.get("file");

            var width = (long) JsonPostFile.get("width");
            var height = (long) JsonPostFile.get("height");
            var size = (long) JsonPostFile.get("size");
            String type = (String) JsonPostFile.get("ext");

            if (width > 4000 || height > 6000 || size > 3000000 || !validTypes.contains(type))
                continue;

            String JsonPostFileUrl = (String) JsonPostFile.get("url");

            if (JsonPostFileUrl != null) {
                InputMedia inputMedia = new InputMediaPhoto(JsonPostFileUrl);
                photoUrls.add(inputMedia);
            }
        }
        return photoUrls;
    }

    private String getPoolJson (Long chatId, String number){

        String poolUrl = "https://e621.net/pools.json?search[id]=" + number;
        String jsonString = openConnectionAndGetJson(poolUrl);
        if (jsonString.equals("[]")) {
            sendText(chatId, "The pool is empty :(");
        }
        return jsonString;
    }

    private String getRandomPoolJson () {

        Random random = new Random();
        int luckyNumber;
        String jsonString = "[]";

        while (jsonString.equals("[]")) {
            luckyNumber = random.nextInt(1, 40000);
            String poolUrl = "https://e621.net/pools.json?search[id]=" + luckyNumber;
            jsonString = openConnectionAndGetJson(poolUrl);
        }
        return jsonString;
    }

    private String RandomPageWithPostsJson(){

        Random random = new Random();
        int luckyPage =  random.nextInt(0, 751);

        String pageUrl = "https://e621.net/posts.json?limit=320&page=" + luckyPage;
        String jsonString = openConnectionAndGetJson(pageUrl);
        return jsonString;
    }

    private String PageWithPostsByTagsJson(String postTags) {

        int page = 1;
        String pageUrl = "https://e621.net/posts.json?limit=320&page=" + page +"&tags=" + postTags;
        String jsonString = openConnectionAndGetJson(pageUrl);

        return jsonString;
    }
    private String openConnectionAndGetJson (String UrlForOpen){

        URL url;
        try {
            url = new URL(UrlForOpen);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

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

    private JSONArray parseJsonStringAsArray (String jsonString){
        JSONParser parse = new JSONParser();
        JSONArray dataObject;
        try {
            dataObject = (JSONArray) parse.parse(jsonString);
        } catch (ParseException e) {

            throw new RuntimeException(e);
        }
        return dataObject;
    }

    private JSONObject parseJsonStringAsObject (String jsonString){
        JSONParser parser = new JSONParser();
        JSONObject dataObject;
        try {
            dataObject = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {

            throw new RuntimeException(e);
        }
        return dataObject;
    }

    private void startCommandReceived (long chatId){

        String text = "Hi! Nice to meet you!";

        sendText(chatId, text);
    }

    private void sendPool (Long chatId, ArrayList < InputMedia > photos){

        sendChatAction(chatId, "upload_photo");

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

    private void sendChatAction ( long chatId, String action) { //Use this method when you need to tell the user that something is happening on the bot's side

        SendChatAction sendChatAction = SendChatAction.builder()
                .chatId(String.valueOf(chatId))
                .action(action)
                .build();
        try {
            execute(sendChatAction);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }

    private void sendText ( long chatId, String text) { //Use this method to send text

        sendChatAction(chatId, "typing");

        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }

    private void sendAlbum ( long chatId, ArrayList<InputMedia> media) { //Use this method to send a group of photos or videos as an album

        sendChatAction(chatId, "upload_photo");

        SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                .chatId(String.valueOf(chatId))
                .medias(media)
                .build();
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }

    private void sendPhoto ( long chatId, String url) { //Use this method to send photos

        sendChatAction(chatId, "upload_photo");

        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(String.valueOf(chatId))
                .photo(new InputFile(url))
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException();
        }
    }
}