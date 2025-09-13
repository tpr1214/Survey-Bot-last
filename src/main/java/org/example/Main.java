// src/main/java/org/example/Main.java
package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t,e)->e.printStackTrace());

        // אין SecretsLoader – מסירים.
        String k = AppConfig.OPENAI_API_KEY(); // או AppConfig.OPENAI_API_KEY
        System.out.println("OPENAI key present? " + (k != null && !k.isBlank()));

        SwingUtilities.invokeLater(() -> {
            SurveyFrame frame = new SurveyFrame();
            frame.setVisible(true);
            System.out.println("Swing UI UP ✅");
        });

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            MyBot bot = new MyBot();
            api.registerBot(bot);
            System.out.println("BOT UP ✅  using @" + AppConfig.BOT_USERNAME);
        } catch (Exception e) {
            System.err.println("שגיאה בהרמת הבוט:");
            e.printStackTrace();
        }
    }
}
