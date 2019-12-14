package com.sheronova.tl.command;

import com.sheronova.tl.model.TariffType;
import com.sheronova.tl.repository.UserRepository;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.time.LocalDateTime;


public class StartCommand extends BotCommand {

    private static final String LOGTAG = "STARTCOMMAND";
    private UserRepository userRepository;

    public StartCommand(UserRepository userRepository) {
        super("start", "With this command you can start the Bot");
        this.userRepository = userRepository;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        StringBuilder messageBuilder = new StringBuilder();

        String userName = user.getFirstName() + " " + user.getLastName();

        if (userRepository.findById(user.getId()).isPresent()) {
            messageBuilder.append("Hi ").append(userName).append("\n");
            messageBuilder.append("i think we know each other already!");
        } else {
            userRepository.save(new com.sheronova.tl.model.User(user.getId(), LocalDateTime.now(), null, TariffType.GENERAL));
            messageBuilder.append("Welcome ").append(userName).append("\n");
            messageBuilder.append("this bot will demonstrate you the command feature of the Java TelegramBots API!");
        }

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText(messageBuilder.toString());

        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
