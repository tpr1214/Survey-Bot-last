package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionEditor extends JPanel {
    private final JTextField qText = new JTextField();
    private final List<JTextField> opts = new ArrayList<>();

    public QuestionEditor() {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; add(new JLabel("שאלה:"), c);
        c.gridx = 1; c.weightx = 1; add(qText, c);

        for (int i = 0; i < 4; i++) {
            JTextField tf = new JTextField();
            opts.add(tf);
            c.gridy++;
            c.gridx = 0; c.weightx = 0; add(new JLabel("תשובה " + (i+1) + ":"), c);
            c.gridx = 1; c.weightx = 1; add(tf, c);
        }
    }

    public Question build() {
        String t = qText.getText().trim();
        if (t.isEmpty()) throw new IllegalArgumentException("טקסט השאלה ריק");
        List<String> answers = new ArrayList<>();
        for (JTextField tf : opts) {
            String s = tf.getText().trim();
            if (!s.isEmpty()) answers.add(s);
        }
        if (answers.size() < 2) throw new IllegalArgumentException("חובה לפחות 2 תשובות");
        if (answers.size() > 4) answers = answers.subList(0, 4);
        return new Question(t, answers);
    }
}
