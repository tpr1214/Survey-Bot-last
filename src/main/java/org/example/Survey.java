package org.example;

import java.util.*;

public class Survey {
    private final long creatorId;
    private final List<Question> questions;
    private final Map<Long, int[]> submissions = new HashMap<>();

    public Survey(long creatorId, List<Question> questions) {
        this.creatorId = creatorId;
        this.questions = questions;
    }

    public long getCreatorId() { return creatorId; }
    public List<Question> getQuestions() { return questions; }

    public synchronized boolean submit(long userId, int[] choices) {
        if (submissions.containsKey(userId)) return false;
        if (choices.length != questions.size()) return false;
        submissions.put(userId, choices);
        return true;
    }

    public synchronized int getSubmissionCount() {
        return submissions.size();
    }

    public synchronized Map<Integer, int[]> resultsMatrix() {
        Map<Integer, int[]> matrix = new HashMap<>();
        for (int qi = 0; qi < questions.size(); qi++) {
            matrix.put(qi, new int[questions.get(qi).getOptions().size()]);
        }
        for (int[] ans : submissions.values()) {
            for (int qi = 0; qi < ans.length; qi++) {
                matrix.get(qi)[ans[qi]]++;
            }
        }
        return matrix;
    }
}
