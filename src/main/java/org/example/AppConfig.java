// src/main/java/org/example/AppConfig.java
package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class AppConfig {
    private AppConfig() {}

    // ---- Telegram ----
    public static final String BOT_USERNAME = "HomeWork33Bot";
    public static final String BOT_TOKEN    = "8141374623:AAGr9b8FqzCr2n36qaxEZbx7pgh-XylTIyc";

    // ---- OpenAI ----
    public static final String OPENAI_API_BASE = "https://api.openai.com/v1/chat/completions";
    public static final String OPENAI_MODEL    = "gpt-4o-mini";
    public static final String OPENAI_API_KEY; // נטען בסטטיק בלוק

    static {
        String key = System.getenv("OPENAI_API_KEY"); // קודם מה-ENV
        String loadedFrom = null;



        if (key == null || key.isBlank()) {
            // גם ~/.homeworkgpt וגם ~/.homeWorkGpt
            Path p1 = Paths.get(System.getProperty("user.home"), ".homeworkgpt", "secrets.properties");
            Path p2 = Paths.get(System.getProperty("user.home"), ".homeWorkGpt", "secrets.properties");
            for (Path p : new Path[]{p1, p2}) {
                if (Files.isRegularFile(p)) {
                    try (InputStream in = Files.newInputStream(p)) {
                        Properties props = new Properties();
                        props.load(in);
                        String v = props.getProperty("OPENAI_API_KEY");
                        if (v != null && !v.isBlank()) {
                            key = v.trim();
                            loadedFrom = p.toString();
                            break;
                        }
                    } catch (IOException ignored) {}
                }
            }
        }

        if (loadedFrom != null) {
            System.out.println("Loaded OPENAI key from: " + loadedFrom);
        }
        System.out.println("OPENAI key present? " + (key != null && !key.isBlank()));
        OPENAI_API_KEY = key;
    }

    // ---- Seker (אם משתמשים בו ב-SurveyFrame) ----
    public static final String SEKER_BASE_URL = "https://app.seker.live/fm1/create-poll";
    public static final String SEKER_OWNER_ID = "314943028";

    // עטיפה נוחה—אפשר להמשיך לקרוא כמו מתודה אם יש מקומות שכבר כתבת ככה
    public static String OPENAI_API_KEY() {
        return OPENAI_API_KEY;
    }
}
