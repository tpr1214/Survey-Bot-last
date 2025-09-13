// src/main/java/org/example/CommunityService.java
package org.example;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CommunityService {
    // שמור מזהי צ'אטים (לא userId)
    private final Set<Long> chatIds = new HashSet<>();
    private MyBot bot;

    public synchronized void attachBot(MyBot bot) { this.bot = bot; }

    // מצטרף לפי chatId
    public synchronized boolean tryJoin(long chatId, String displayName) {
        if (chatIds.add(chatId)) {
            if (bot != null) {
                String msg = "חבר/ה חדש/ה הצטרף/ה: " + displayName + "\nגודל הקהילה: " + chatIds.size();
                bot.broadcast(chatIds, msg);
            }
            return true;
        }
        return false;
    }

    public synchronized int size() { return chatIds.size(); }

    public synchronized Set<Long> snapshotMembers() {
        return Collections.unmodifiableSet(new HashSet<>(chatIds));
    }
}
