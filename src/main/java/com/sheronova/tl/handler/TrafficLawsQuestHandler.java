package com.sheronova.tl.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.sheronova.tl.model.Quest;
import com.sheronova.tl.model.TariffType;
import com.sheronova.tl.model.User;
import com.sheronova.tl.service.MarathonService;
import com.sheronova.tl.service.QuestService;
import com.sheronova.tl.service.UserService;
import com.sheronova.tl.utils.JacksonUtils;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TrafficLawsQuestHandler extends TelegramLongPollingBot {

    private String botUsername;
    private String botToken;

    private static final String BEGIN_MARATHON = "begin_marathon";
    private static final String BEGIN_MARATHON_TEXT = "Участвовать в марафоне 800ПДД";
    private static final String FINISH_MARATHON = "finish_marathon";
    private static final String TRAIN_MISTAKES = "train_mistakes";
    private static final String TRAIN_MISTAKES_TEXT = "Тренировать свои наиболее частые ошибки";
    private static final String ANSWER = ":answer:";
    private static final String START_TEXT = "Здравствуй, %s!\nЯ, ПДД чат-бот помогу тебе подготовиться к теоретическому экзамену ПДД.\n" +
            "Выбери тренировку:";
    private static final String CONTINUE_MARATHON_TEXT = "Продолжить марафон";
    private static final String CONTINUE_MARATHON = "continue_marathon";
    private static final String FINISH_MARATHON_TEXT = "Закончить марафон";
    private static final String START_COMMAND = "/start";

    private UserService userService;

    private QuestService questService;

    private MarathonService marathonService;


    public TrafficLawsQuestHandler(DefaultBotOptions botOptions, String botUsername,
                                   String botToken, UserService userService,
                                   QuestService questService, MarathonService marathonService) {
        super(botOptions);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.userService = userService;
        this.questService = questService;
        this.marathonService = marathonService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                String input = message.getText();
                if (input.equals(START_COMMAND)) {
                    startBotUsing(message);
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            String data = callbackquery.getData();
            if (data.contains(BEGIN_MARATHON)) {
                editMessageToClearAnswers(callbackquery);
                beginMarathon(callbackquery);
            }
            if (data.contains(ANSWER)) {
                checkQuestAnswer(callbackquery);
            }
            if (data.contains(CONTINUE_MARATHON)) {
                editMessageToClearAnswers(callbackquery);
                sendQuestMessage(callbackquery);
            }
            if (data.contains(FINISH_MARATHON)) {
                editMessageToClearAnswers(callbackquery);
                sendFinishMessage(callbackquery);
            }
        }
    }



    private void startBotUsing(Message message) {
        Integer userId = message.getFrom().getId();
        if (userService.getUserById(userId) == null) {
            userService.saveUser(new User(userId, LocalDateTime.now(), null, TariffType.GENERAL));
            marathonService.insertNewData(userId);
        }
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId().toString());
        sendMessageRequest.setText(String.format(START_TEXT, message.getFrom().getFirstName()));
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setReplyMarkup(this.getStartMarkup());
        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getStartMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(BEGIN_MARATHON_TEXT).setCallbackData(BEGIN_MARATHON))));
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(TRAIN_MISTAKES_TEXT).setCallbackData(TRAIN_MISTAKES))));
        markup.setKeyboard(rows);
        return markup;
    }


    private void beginMarathon(CallbackQuery callbackQuery) {
        marathonService.beginMarathon(callbackQuery.getFrom().getId());
        sendQuestMessage(callbackQuery);
    }

    private void sendQuestMessage(CallbackQuery callbackQuery) {
        Quest quest = marathonService.getQuest(callbackQuery.getFrom().getId());
        if (quest == null) {
            sendFinishMessage(callbackQuery);
            return;
        }
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(callbackQuery.getMessage().getChatId().toString());
        sendMessageRequest.setText(questText(quest));
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setReplyMarkup(this.getQuestMarkaup(quest.getImageFile(), quest.getId(), quest.getAnswers()));
        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String questText(Quest quest) {
        StringBuilder builder = new StringBuilder(quest.getQuest() + "\n\n");
        Map<Integer, String> answers = JacksonUtils.toMap(quest.getAnswers());
        for (Map.Entry<Integer, String> answer: answers.entrySet()) {
            builder.append(answer.getKey()).append(": ").append(answer.getValue()).append("\n\n");
        }
        return builder.toString();
    }

    private InlineKeyboardMarkup getQuestMarkaup(String imageFile, Integer questId, JsonNode answers) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        Map<Integer, String> mapAnswers = JacksonUtils.toMap(answers);
        for (Map.Entry<Integer, String> answer: mapAnswers.entrySet()) {
            rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(String.valueOf(answer.getKey())).setCallbackData(questId + ANSWER + answer.getKey()))));
        }
        markup.setKeyboard(rows);
        return markup;
    }

    private void checkQuestAnswer(CallbackQuery callbackquery) {
        String[] data = callbackquery.getData().split(":");
        Integer questId = Integer.valueOf(data[0]);
        Integer answer = Integer.valueOf(data[2]);
        Quest quest = questService.getQuestById(questId);
        Map<Integer, String> answers = JacksonUtils.toMap(quest.getAnswers());
        String generalText = quest.getQuest() + "\n\n" + "Правильный ответ: " + quest.getCorrect() + " " + answers.get(quest.getCorrect()) +
                "\n\n" + "Ваш ответ: " + answer + " " + answers.get(answer)+ "\n\n";
        if (quest.getCorrect().equals(answer)) {
            marathonService.setQuestResult(questId, callbackquery.getFrom().getId(), 1);
            sendQuestResultAnswer(callbackquery, generalText + "Вы ответили правильно!");
        } else {
            marathonService.setQuestResult(questId, callbackquery.getFrom().getId(), -1);
            sendQuestResultAnswer(callbackquery, generalText + "К сожалению, вы ошиблись:(" + "\n\n"
                    + "Подсказка: " + quest.getHelp());
        }
    }

    private void sendQuestResultAnswer(CallbackQuery callbackquery, String text) {
        EditMessageText editMarkup = new EditMessageText();
        editMarkup.setChatId(callbackquery.getMessage().getChatId().toString());
        editMarkup.setInlineMessageId(callbackquery.getInlineMessageId());
        editMarkup.setText(text);
        editMarkup.enableMarkdown(true);
        editMarkup.setMessageId(callbackquery.getMessage().getMessageId());
        editMarkup.setReplyMarkup(getQuestAnswerMarkup());
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            System.out.println(text );
            e.printStackTrace();
        }
    }

    private void editMessageToClearAnswers(CallbackQuery callbackQuery) {
        EditMessageText editMarkup = new EditMessageText();
        editMarkup.setChatId(callbackQuery.getMessage().getChatId().toString());
        editMarkup.setInlineMessageId(callbackQuery.getInlineMessageId());
        editMarkup.setText(callbackQuery.getMessage().getText());
        editMarkup.enableMarkdown(false);
        editMarkup.setMessageId(callbackQuery.getMessage().getMessageId());
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getQuestAnswerMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(CONTINUE_MARATHON_TEXT).setCallbackData(CONTINUE_MARATHON))));
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(FINISH_MARATHON_TEXT).setCallbackData(FINISH_MARATHON))));
        markup.setKeyboard(rows);
        return markup;
    }

    private void sendFinishMessage(CallbackQuery callbackQuery) {
        Pair<Integer, Integer> correctAndWrongCount =  marathonService.finishMarathon(callbackQuery.getFrom().getId());
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(callbackQuery.getMessage().getChatId().toString());
        sendMessageRequest.setText("Марафон завершен \n\n" + "Количество правильный ответов: " + correctAndWrongCount.getFirst() +
                "\n\n" + "Количество ошибок: " + correctAndWrongCount.getSecond());
        try {
            execute(sendMessageRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }




    @Override
    public String getBotToken() {
        return botToken;
    }
}
