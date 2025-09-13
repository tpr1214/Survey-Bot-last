// src/main/java/org/example/FakeChatGptClient.java
package org.example;

import java.util.ArrayList;
import java.util.List;

/** מחולל סקר "חכם" ללא OpenAI: יוצר 1–3 שאלות בעברית לפי הנושא. */
public class FakeChatGptClient implements ChatGptClient {

    @Override
    public List<Question> generate(String topic, int questionsCount) {
        int n = Math.max(1, Math.min(3, questionsCount));

        // ניקוי בסיסי לנושא
        String t = (topic == null ? "" : topic.trim()).replaceAll("[\\p{Cntrl}]+", "");
        if (t.isEmpty()) t = "הנושא";

        // זיהוי עברית (לתצורת "בגלישה") לעומת לא-עברית ("בתחום Surfing")
        int first = t.codePointCount(0, t.length()) > 0 ? t.codePointAt(0) : 0;
        boolean isHeb = (first >= 0x0590 && first <= 0x05FF);
        String bTopic = isHeb ? t : ("תחום " + t); // ישמש אחרי האות 'ב'

        List<Question> qs = new ArrayList<>(n);

        // שאלה 1 – עניין מרכזי
        if (qs.size() < n) {
            qs.add(new Question(
                    "מה הכי מעניין אותך בתחום " + t + "?",
                    List.of("ללמוד טכניקות חדשות", "ציוד ואביזרים", "קהילה ואירועים", "בטיחות ומדיניות")
            ));
        }

        // שאלה 2 – רמת ניסיון
        if (qs.size() < n) {
            qs.add(new Question(
                    "מה רמת הניסיון שלך ב" + bTopic + "?",
                    List.of("מתחיל/ה", "ביניים", "מתקדם/ת")
            ));
        }

        // שאלה 3 – תדירות עיסוק
        if (qs.size() < n) {
            qs.add(new Question(
                    "באיזו תדירות את/ה עוסק/ת ב" + bTopic + "?",
                    List.of("כמעט מדי יום", "פעמיים–שלוש בשבוע", "פעם בחודש או פחות")
            ));
        }

        return qs;
    }


    private static String sanitize(String s) {
        if (s == null) return "הנושא";
        String out = s.trim();
        if (out.isEmpty()) return "הנושא";
        // מנקה תווים מוזרים
        return out.replaceAll("\\p{Cntrl}+", "");
    }

    private static String prefix(String topic) {
        // מכניס מקף מקשר כשצריך (למשל "בגלישה" / "בצילום")
        char first = topic.charAt(0);
        boolean heb = (first >= 0x0590 && first <= 0x05FF);
        return heb ? topic : ("תחום " + topic);
    }
}
