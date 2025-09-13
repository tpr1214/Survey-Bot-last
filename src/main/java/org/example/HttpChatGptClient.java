// src/main/java/org/example/HttpChatGptClient.java
package org.example;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HttpChatGptClient implements ChatGptClient {
    private static final OkHttpClient HTTP = new OkHttpClient();

    @Override
    public List<Question> generate(String topic, int questionsCount) {
        int n = Math.max(1, Math.min(3, questionsCount));

        String apiKey = AppConfig.OPENAI_API_KEY(); // <-- פונקציה, לא קבוע
        System.out.println("OPENAI key present? " + (apiKey != null && !apiKey.isBlank())); // דיבאג
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY missing → using FakeChatGptClient");
            return new FakeChatGptClient().generate(topic, n);
        }

        try {
            JSONObject payload = new JSONObject()
                    .put("model", AppConfig.OPENAI_MODEL)
                    .put("temperature", 0.6)
                    .put("messages", new JSONArray()
                            .put(new JSONObject().put("role", "system").put(
                                    "content", "You write short multiple-choice survey questions. Respond with STRICT JSON only."))
                            .put(new JSONObject().put("role", "user").put(
                                    "content", "Create " + n + " survey question(s) about \"" + topic + "\". " +
                                            "Each must have 2-4 concise options. " +
                                            "Return JSON exactly as: " +
                                            "{\"questions\":[{\"text\":\"...\",\"options\":[\"...\",\"...\"]}]}"))
                    );

            Request req = new Request.Builder()
                    .url(AppConfig.OPENAI_API_BASE)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response res = HTTP.newCall(req).execute()) {
                if (!res.isSuccessful()) {
                    String body = res.body() != null ? res.body().string() : "";
                    System.err.println("OpenAI error " + res.code() + ": " + body);
                    throw new RuntimeException("HTTP " + res.code());
                }
                String body = res.body() != null ? res.body().string() : "";
                JSONObject root = new JSONObject(body);

                String content = root.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                JSONObject json = new JSONObject(content);
                JSONArray arr = json.getJSONArray("questions");

                List<Question> out = new ArrayList<>();
                for (int i = 0; i < Math.min(arr.length(), 3); i++) {
                    JSONObject q = arr.getJSONObject(i);
                    String text = q.getString("text").trim();
                    JSONArray opts = q.getJSONArray("options");
                    List<String> options = new ArrayList<>();
                    for (int j = 0; j < opts.length(); j++) {
                        String o = opts.getString(j).trim();
                        if (!o.isEmpty()) options.add(o);
                    }
                    if (options.size() >= 2) {
                        if (options.size() > 4) options = options.subList(0, 4);
                        out.add(new Question(text, options));
                    }
                }
                if (out.isEmpty()) return new FakeChatGptClient().generate(topic, n);
                if (out.size() > n) return out.subList(0, n);
                return out;
            }
        } catch (Exception e) {
            System.err.println("HttpChatGptClient error → fallback to Fake: " + e.getMessage());
            return new FakeChatGptClient().generate(topic, n);
        }
    }
}
