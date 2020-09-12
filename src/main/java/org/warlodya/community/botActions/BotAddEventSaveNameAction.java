package org.warlodya.community.botActions;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.warlodya.community.entities.BotUser;
import org.warlodya.community.interfaces.BotInSessionAction;
import org.warlodya.community.interfaces.TelegramBotApi;
import org.warlodya.community.session.Session;
import org.warlodya.community.session.SessionCrudRepository;
import org.warlodya.community.session.SessionType;
import org.warlodya.community.util.UpdateUtils;

import java.util.Locale;

public class BotAddEventSaveNameAction implements BotInSessionAction {
    private SessionCrudRepository sessionCrudRepository;
    private TelegramBotApi telegramBotApi;
    private MessageSource messageSource;

    @Autowired
    public BotAddEventSaveNameAction(
            TelegramBotApi telegramBotApi,
            SessionCrudRepository sessionCrudRepository,
            MessageSource messageSource
    ) {
        this.telegramBotApi = telegramBotApi;
        this.sessionCrudRepository = sessionCrudRepository;
        this.messageSource = messageSource;
    }

    @Override
    public void execute(Update update, BotUser botUser, Session session) {
        changeSessionState(update, session);
        sendMessageToUser(update);
    }

    private void changeSessionState(Update update, Session session) {
        var state = session.getState();
        state.flags.put("addName", false);
        state.flags.put("addDescription", true);
        state.data.put("name", update.getMessage().getText());
        sessionCrudRepository.save(session);
    }

    private void sendMessageToUser(Update update) {
        String languageCode = UpdateUtils.getLocale(update);
        String message = messageSource.getMessage("events.save.addDescription", null, new Locale(languageCode));
        telegramBotApi.sendMessage(message, UpdateUtils.getChatId(update));
    }

    @Override
    public boolean isAllowed(Update update, Session session) {
        boolean shouldAddName = session.getState().flags.getOrDefault("addName", false);
        return UpdateUtils.hasText(update) && session.getSessionType() == SessionType.ADD_EVENT && shouldAddName;
    }
}