// src/main/java/org/example/SecretsLoader.java
package org.example;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class SecretsLoader {
    private SecretsLoader() {}

    public static void load() {
        // אם כבר יש ב-ENV לא צריך קובץ
        String env = System.getenv("OPENAI_API_KEY");
        if (env != null && !env.isBlank()) {
            System.out.println("DEBUG OPENAI present? true (from ENV)");
            return;
        }

        try {
            Path p = Paths.get(System.getProperty("user.home"), ".homeworkgpt", "secrets.properties"); // שים לב: הכל באותיות קטנות
            if (!Files.exists(p)) {
                System.out.println("Secrets file NOT found at: " + p);
                return;
            }
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(p.toFile())) {
                props.load(in);
            }
            String key = props.getProperty("OPENAI_API_KEY", "").trim();
            if (!key.isEmpty()) {
                System.setProperty("OPENAI_API_KEY", key);
                System.out.println("Loaded OPENAI key from: " + p);
            } else {
                System.out.println("secrets.properties found, but OPENAI_API_KEY is empty");
            }
        } catch (Exception e) {
            System.out.println("SecretsLoader error: " + e.getMessage());
        }
    }
}
