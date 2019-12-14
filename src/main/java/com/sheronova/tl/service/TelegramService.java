package com.sheronova.tl.service;

import com.sheronova.tl.handler.CommandsHandler;
import com.sheronova.tl.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Service
public class TelegramService {
    private static final String LOGTAG = "MAIN";

    private static final String TRAFFIC_LAW_USERNAME = "ASH416HELLOBot";
    private static final String TRAFFIC_LAW_TOKEN = "1021455160:AAHLcvTjAmKckQ29OqVg2LSNF4BO6Mzcv4k";

    @NonNull
    private UserRepository userRepository;

    public TelegramService(UserRepository userRepository) {
        this.userRepository = userRepository;
        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("244765011", "zjdnaJG3".toCharArray());
                }
            });
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyPort(999);
            botOptions.setProxyHost("grsst.s5.opennetwork.cc");
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            botapi.registerBot(new CommandsHandler(botOptions, TRAFFIC_LAW_USERNAME, TRAFFIC_LAW_TOKEN, userRepository));
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
