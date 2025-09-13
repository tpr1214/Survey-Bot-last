// src/main/java/org/example/MyBot.java
package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Set;

public class MyBot extends TelegramLongPollingBot {
    private final String username;
    private final String token;
    public static final String BOT_TOKEN    = "8141374623:AAGr9b8FqzCr2n36qaxEZbx7pgh-XylTIyc";

    // בנאי שמושך ישירות מ-AppConfig ומחבר את הבוט ל-SurveyManager
    public MyBot() {
        this.username = AppConfig.BOT_USERNAME;
        this.token = AppConfig.BOT_TOKEN;
        SurveyManager.get().attachBot(this);
        System.out.println("MyBot init ✅ username=" + username +
                ", tokenLen=" + (token != null ? token.length() : 0));
    }

    @Override public String getBotUsername() { return username; }
    @Override public String getBotToken()    { return token;    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update != null && update.hasMessage()) {
                handleMessage(update.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg) throws TelegramApiException {
        if (msg == null || !msg.hasText()) return;

        String text = msg.getText().trim();
        long chatId = msg.getChatId();
        long userId = msg.getFrom().getId();
        String displayName =
                (msg.getFrom().getFirstName() == null ? "" : msg.getFrom().getFirstName()) +
                        (msg.getFrom().getLastName()  == null ? "" : " " + msg.getFrom().getLastName());

        // הצטרפות לקהילה
        if ("/start".equalsIgnoreCase(text) || "היי".equalsIgnoreCase(text)
                || "hi".equalsIgnoreCase(text) || "Hi".equalsIgnoreCase(text)) {

            boolean joined = SurveyManager.get()
                    .joinCommunity(userId, displayName.isBlank() ? "משתמש/ת" : displayName);

            reply(chatId, joined
                    ? "הצטרפת לקהילה ✅ (" + SurveyManager.get().communitySize() + " חברים)"
                    : "כבר הצטרפת קודם 🙂");
            return;
        }

        // סטטוס
        if ("/status".equalsIgnoreCase(text)) {
            reply(chatId, "חברי קהילה: " + SurveyManager.get().communitySize()
                    + (SurveyManager.get().hasActive() ? "\nיש סקר פעיל." : "\nאין סקר פעיל."));
            return;
        }

        // הצבעה בטקסט: "vote 2,1,3"
        if (text.toLowerCase().startsWith("vote")) {
            Survey active = SurveyManager.get().getActive();
            if (active == null) { reply(chatId, "אין סקר פעיל כרגע."); return; }

            List<Question> qs = active.getQuestions();
            String args = text.substring(4).trim();
            if (args.isEmpty()) { reply(chatId, "דוגמה: vote 2,1,3"); return; }

            String[] parts = args.replace(" ", "").split("[,;]");
            if (parts.length != qs.size()) {
                reply(chatId, "צריך לבחור בדיוק " + qs.size() + " תשובות. דוגמה: vote 2,1,3");
                return;
            }

            int[] zeroBased = new int[parts.length];
            try {
                for (int i = 0; i < parts.length; i++) {
                    int idx = Integer.parseInt(parts[i]) - 1;
                    int max = qs.get(i).getOptions().size();
                    if (idx < 0 || idx >= max) {
                        reply(chatId, "לשאלה " + (i + 1) + " בחר/י מספר בין 1 ל-" + max);
                        return;
                    }
                    zeroBased[i] = idx;
                }
            } catch (NumberFormatException ex) {
                reply(chatId, "ערכים חייבים להיות מספרים. דוגמה: vote 2,1,3");
                return;
            }

            boolean ok = SurveyManager.get().submitAnswers(userId, zeroBased);
            reply(chatId, ok ? "התשובה נקלטה ✅" : "כבר ענית או שהתשובה לא תקינה.");
        }
    }

    public void broadcast(Set<Long> chatIds, String text) {
        for (Long id : chatIds) {
            try {
                execute(new SendMessage(String.valueOf(id), text));
            } catch (TelegramApiException e) {
                System.err.println("Failed to send to " + id + ": " + e.getMessage());
            }
        }
    }

    private void reply(long chatId, String text) throws TelegramApiException {
        execute(new SendMessage(String.valueOf(chatId), text));
    }
}
