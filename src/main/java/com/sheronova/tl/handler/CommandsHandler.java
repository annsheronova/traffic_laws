package com.sheronova.tl.handler;

import com.sheronova.tl.command.HelpCommand;
import com.sheronova.tl.command.StartCommand;
import com.sheronova.tl.command.StopCommand;
import com.sheronova.tl.repository.UserRepository;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandsHandler extends TelegramLongPollingCommandBot {

    public static final String LOGTAG = "COMMANDSHANDLER";

    private static final String BEGIN_MARATHON = "begin_marathon";
    private static final String BEGIN_MARATHON_TEXT = "Участвовать в марафоне 800ПДД";
    private static final String FINISH_MARATHON = "finish_marathon";
    private static final String TRAIN_MISTAKES = "train_mistakes";
    private static final String TRAIN_MISTAKES_TEXT = "Тренировать свои наиболее частые ошибки";
    private static final String ANSWER = "answer:";
    private static final String START_TEXT = "Здравствуй %s!\nЯ, ПДД чат-бот помогу тебе подготовиться к теоретическому экзамену ПДД.\n" +
            "Выбери, пожалуйста, тренировку:";


    private String botToken;
    private UserRepository userRepository;

    public CommandsHandler(DefaultBotOptions botOptions, String botUsername, String botToken, UserRepository userRepository) {
        super(botOptions, botUsername);
        this.botToken = botToken;
        this.userRepository = userRepository;

        register(new HelpCommand(this, userRepository));
        register(new StartCommand(userRepository));
        register(new StopCommand(userRepository));
        HelpCommand helpCommand = new HelpCommand(this, userRepository);

        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId());
            commandUnknownMessage.setText("The command '" + message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[]{});
        });
    }

    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                String input = message.getText();
                if (input.equals("/start")) {
                    SendMessage sendMessageRequest = new SendMessage();
                    sendMessageRequest.setChatId(message.getChatId().toString());
                    sendMessageRequest.setText(START_TEXT);
                    sendMessageRequest.enableMarkdown(true);
                    sendMessageRequest.setReplyMarkup(this.getStartMarkup());
                    try {
                        execute(sendMessageRequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackquery = update.getCallbackQuery();
            EditMessageText editMarkup = new EditMessageText();
            editMarkup.setChatId(callbackquery.getMessage().getChatId().toString());
            editMarkup.setInlineMessageId(callbackquery.getInlineMessageId());
            editMarkup.setText("marathon begin");
            editMarkup.enableMarkdown(true);
            editMarkup.setMessageId(callbackquery.getMessage().getMessageId());
            try {
                execute(editMarkup);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }


    private void sendAnswerCallbackQuery(String text, boolean alert, CallbackQuery callbackquery) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackquery.getId());
        answerCallbackQuery.setShowAlert(alert);
        answerCallbackQuery.setText(text);
        execute(answerCallbackQuery);
    }

    private InlineKeyboardMarkup getStartMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(BEGIN_MARATHON_TEXT).setCallbackData(BEGIN_MARATHON))));
        rows.add(new ArrayList<>(Arrays.asList(new InlineKeyboardButton().setText(TRAIN_MISTAKES_TEXT).setCallbackData(TRAIN_MISTAKES))));
        markup.setKeyboard(rows);
        return markup;
    }

    public String getBotToken() {
        return botToken;
    }
}
