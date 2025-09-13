package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SurveyFrame extends JFrame {
    private final ChatGptClient gpt = new HttpChatGptClient();


    private final JTextField topicField = new JTextField();
    private final JSpinner qCountSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 3, 1));
    private final JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));

    private final JPanel manualPanel = new JPanel(new GridLayout(0, 1, 8, 8));
    private final List<QuestionEditor> questionEditors = new ArrayList<>();

    private final JLabel statusLabel = new JLabel(" ");

    // מחליפים את ה-FakeChatGptClient למימוש שלנו
    //private final ChatGptClient gpt = new HttpChatGptClient();

    public SurveyFrame() {
        super("Survey Builder (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 640);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("יצירה ידנית", buildManualTab());
        tabs.addTab("יצירה עם GPT", buildGptTab());

        add(tabs, BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        SurveyManager.get().setOnSurveyClosedUiCallback(this::showResultsDialog);
    }

    private JPanel buildManualTab() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addQ = new JButton("הוסף שאלה");
        addQ.addActionListener(e -> addQuestionEditor());
        JButton clear = new JButton("נקה");
        clear.addActionListener(e -> resetManual());
        header.add(addQ); header.add(clear);
        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(manualPanel), BorderLayout.CENTER);
        addQuestionEditor();
        return root;
    }

    private JPanel buildGptTab() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; root.add(new JLabel("נושא כללי:"), c);
        c.gridx = 1; c.weightx = 1; root.add(topicField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; root.add(new JLabel("מס' שאלות:"), c);
        c.gridx = 1; root.add(qCountSpinner, c);

        c.gridx = 0; c.gridy = 2; root.add(new JLabel("עיכוב בדקות:"), c);
        c.gridx = 1; root.add(delaySpinner, c);

        JButton gen = new JButton("צור ושלח סקר");
        gen.addActionListener(e -> sendGptSurvey());
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2; root.add(gen, c);

        return root;
    }

    private JPanel buildBottomBar() {
        JPanel p = new JPanel(new BorderLayout());
        JButton sendManual = new JButton("שלח סקר ידני");
        sendManual.addActionListener(e -> sendManualSurvey());
        p.add(sendManual, BorderLayout.EAST);
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    private void addQuestionEditor() {
        if (questionEditors.size() >= 3) return;
        QuestionEditor qe = new QuestionEditor();
        questionEditors.add(qe);
        manualPanel.add(qe);
        manualPanel.revalidate();
        manualPanel.repaint();
    }

    private void resetManual() {
        questionEditors.clear();
        manualPanel.removeAll();
        addQuestionEditor();
    }

    private void sendManualSurvey() {
        try {
            List<Question> qs = new ArrayList<>();
            for (QuestionEditor qe : questionEditors) {
                Question q = qe.build();
                if (q != null) qs.add(q);
            }
            if (qs.isEmpty()) throw new IllegalArgumentException("חובה לפחות שאלה אחת");
            int delay = (int) ((SpinnerNumberModel) delaySpinner.getModel()).getNumber();
            boolean ok = SurveyManager.get().createAndScheduleSend(currentCreatorId(), qs, delay);
            setStatus(ok ? "נשלח/תוזמן ✅"
                    : "נכשל: ייתכן שכבר יש סקר פעיל או שיש פחות מ-"
                    + SurveyManager.MIN_COMMUNITY_FOR_SURVEY + " בקהילה");
        } catch (Exception ex) {
            setStatus("שגיאה: " + ex.getMessage());
        }
    }

    private void sendGptSurvey() {
        try {
            String topic = topicField.getText().trim();
            if (topic.isEmpty()) throw new IllegalArgumentException("נושא ריק");
            int n = (int) ((SpinnerNumberModel) qCountSpinner.getModel()).getNumber();
            List<Question> qs = gpt.generate(topic, n);
            int delay = (int) ((SpinnerNumberModel) delaySpinner.getModel()).getNumber();
            boolean ok = SurveyManager.get().createAndScheduleSend(currentCreatorId(), qs, delay);

            if (ok) {
                setStatus("נשלח/תוזמן ✅");
                // פתחי את עמוד יצירת הסקר באתר שלך (URL ב-AppConfig)
                try {
                    String url = AppConfig.SEKER_BASE_URL + "?owner=" + AppConfig.SEKER_OWNER_ID;
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new java.net.URI(url));
                    }
                } catch (Exception ex2) {
                    System.err.println("שגיאה בפתיחת URL: " + ex2.getMessage());
                }
            } else {
                setStatus("נכשל: ייתכן שכבר יש סקר פעיל או שיש פחות מ-"
                        + SurveyManager.MIN_COMMUNITY_FOR_SURVEY + " בקהילה");
            }
        } catch (Exception ex) {
            setStatus("שגיאה: " + ex.getMessage());
        }
    }

    private void showResultsDialog(Survey s) {
        SwingUtilities.invokeLater(() -> new ResultsDialog(this, s).setVisible(true));
    }

    private long currentCreatorId() {
        return 123456789L; // דוגמה
    }

    private void setStatus(String t) { statusLabel.setText(t); }
}
