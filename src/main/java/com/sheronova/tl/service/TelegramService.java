package com.sheronova.tl.service;

import com.sheronova.tl.handler.TrafficLawsQuestHandler;
import com.sheronova.tl.repository.UserRepository;
import lombok.NonNull;
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

    private UserService userService;

    private QuestService questService;

    private MarathonService marathonService;

    public TelegramService(UserService userService,
                           QuestService questService, MarathonService marathonService) {
        this.userService = userService;
        this.questService = questService;
        this.marathonService = marathonService;
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

            botapi.registerBot(new TrafficLawsQuestHandler(botOptions, TRAFFIC_LAW_USERNAME, TRAFFIC_LAW_TOKEN,
                    userService, questService, marathonService));
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
