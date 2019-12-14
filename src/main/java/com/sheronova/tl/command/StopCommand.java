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

/**
 * This command stops the conversation with the bot.
 * Bot won't respond to user until he sends a start command
 *
 * @author Timo Schulz (Mit0x2)
 */
public class StopCommand extends BotCommand {

    public static final String LOGTAG = "STOPCOMMAND";

    private UserRepository userRepository;
    /**
     * Construct
     */
    public StopCommand(UserRepository userRepository) {
        super("stop", "With this command you can stop the Bot");
        this.userRepository = userRepository;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        if (userRepository.findById(user.getId()).isPresent()) {
            String userName = user.getFirstName() + " " + user.getLastName();

            SendMessage answer = new SendMessage();
            answer.setChatId(chat.getId().toString());
            answer.setText("Good bye " + userName + "\n" + "Hope to see you soon!");

            try {
                absSender.execute(answer);
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }
        }
    }
}
