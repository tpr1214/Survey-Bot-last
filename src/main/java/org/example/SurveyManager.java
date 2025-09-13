package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SurveyManager {
    private static final SurveyManager INSTANCE = new SurveyManager();
    public static SurveyManager get() { return INSTANCE; }

    // >>> סף מינימלי לגודל קהילה כדי לאפשר שליחת סקר (שני את המספר כאן בלבד)
    public static final int MIN_COMMUNITY_FOR_SURVEY = 1;


    // קהילה
    private final Set<Long> community = new HashSet<>();

    // סקר פעיל
    private volatile Survey active;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // חיבור לבוט לצורך שידור הודעות
    private volatile MyBot bot;

    // callback ל-UI (Swing) כשסקר נסגר
    private volatile Consumer<Survey> onSurveyClosedUiCallback;

    private SurveyManager() {}

    /* ===== קהילה ===== */
    public synchronized boolean joinCommunity(long userId, String displayNameForBroadcast) {
        boolean added = community.add(userId);
        if (added) {
            // שדר לשאר החברים על מצטרף חדש (כולל גודל קהילה)
            broadcastToCommunity("חבר/ה חדש/ה הצטרף/ה: " + displayNameForBroadcast +
                    "\nגודל הקהילה: " + community.size());
        }
        return added;
    }

    public synchronized int communitySize() { return community.size(); }

    public synchronized Set<Long> snapshotCommunity() {
        return Collections.unmodifiableSet(new HashSet<>(community));
    }

    /* ===== בוט / UI ===== */
    public void attachBot(MyBot bot) { this.bot = bot; }

    public void setOnSurveyClosedUiCallback(Consumer<Survey> cb) {
        this.onSurveyClosedUiCallback = cb;
    }

    private void broadcastToCommunity(String msg) {
        MyBot b = this.bot;
        if (b != null) {
            b.broadcast(snapshotCommunity(), msg);
        }
    }

    /* ===== סקר ===== */
    public synchronized boolean hasActive() { return active != null; }
    public synchronized Survey getActive() { return active; }

    /**
     * יוצר סקר ומתזמן את השליחה שלו בעוד delayMinutes דקות.
     * נשלחת הודעת התחלה עם הוראות מענה: vote 1,2,1 ...
     * לאחר השליחה, מתוזמנת סגירה אוטומטית כעבור 5 דקות.
     */
    public boolean createAndScheduleSend(long creatorId, List<Question> questions, int delayMinutes) {
        synchronized (this) {
            if (active != null) return false;
            if (community.size() < MIN_COMMUNITY_FOR_SURVEY) return false;
            active = new Survey(creatorId, questions);
        }

        // תזמון שליחה
        scheduler.schedule(() -> {
            // שליחת שאלות + הנחיות מענה
            Survey curr = active; // שמירה ל־NPE safety
            if (curr == null) return;

            StringBuilder sb = new StringBuilder();
            sb.append("סקר חדש התחיל!\n");
            List<Question> qs = curr.getQuestions();
            for (int i = 0; i < qs.size(); i++) {
                Question q = qs.get(i);
                sb.append("\nשאלה ").append(i + 1).append(": ").append(q.getText()).append("\n");
                List<String> opts = q.getOptions();
                for (int j = 0; j < opts.size(); j++) {
                    sb.append("  ").append(j + 1).append(") ").append(opts.get(j)).append("\n");
                }
            }
            sb.append("\nענו בהודעה:  vote a,b,c,d  (לדוגמה: vote 2,1,3)\n")
                    .append("אפשר לענות פעם אחת בלבד. הסקר נסגר בעוד 5 דקות או כשכולם ענו.");

            broadcastToCommunity(sb.toString());

            // תזמון סגירה אוטומטית אחרי 5 דקות (לתאם עם הטקסט למעלה)
            scheduler.schedule(this::closeIfActiveAndNotify, 5, TimeUnit.MINUTES);
        }, Math.max(0, delayMinutes), TimeUnit.MINUTES);

        return true;
    }

    /**
     * מענה משתמש. מצופה קלט באורך מספר השאלות, כשכל ערך הוא אינדקס תשובה (0-based).
     * אם כולם ענו – נסגור מיד.
     */
    public boolean submitAnswers(long userId, int[] zeroBasedChoices) {
        Survey curr;
        int communityCount;
        synchronized (this) {
            curr = active;
            if (curr == null) return false;
            boolean ok = curr.submit(userId, zeroBasedChoices);
            if (!ok) return false;
            communityCount = community.size();
        }
        // סגירה מיידית אם כולם ענו
        if (curr.getSubmissionCount() >= communityCount) {
            closeIfActiveAndNotify();
        }
        return true;
    }

    private void closeIfActiveAndNotify() {
        Survey toClose = null;
        synchronized (this) {
            if (active != null) {
                toClose = active;
                active = null;
            }
        }
        if (toClose != null) {
            broadcastToCommunity("הסקר נסגר. תודה שהשתתפתם!");
            // עדכון ה-UI (תוצאות)
            if (onSurveyClosedUiCallback != null) {
                onSurveyClosedUiCallback.accept(toClose);
            }
        }
    }
}
