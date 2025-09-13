package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class ResultsDialog extends JDialog {
    public ResultsDialog(Frame owner, Survey s) {
        super(owner, "תוצאות הסקר", true);
        setSize(640, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JTextArea area = new JTextArea();
        area.setEditable(false);

        StringBuilder sb = new StringBuilder();
        Map<Integer, int[]> matrix = s.resultsMatrix();
        for (int qi = 0; qi < s.getQuestions().size(); qi++) {
            Question q = s.getQuestions().get(qi);
            sb.append("שאלה ").append(qi + 1).append(": ").append(q.getText()).append("\n");

            int[] counts = matrix.get(qi);
            int total = Arrays.stream(counts).sum();

            // לבנות זוגות (אינדקס תשובה, ספירה), למיין בירידה, ואז להדפיס
            List<Entry<Integer,Integer>> list = new ArrayList<>();
            for (int oi = 0; oi < q.getOptions().size(); oi++) {
                list.add(new AbstractMap.SimpleEntry<>(oi, counts[oi]));
            }
            list.sort((a,b) -> Integer.compare(b.getValue(), a.getValue()));

            for (Entry<Integer,Integer> e : list) {
                int oi = e.getKey();
                String opt = q.getOptions().get(oi);
                int c = e.getValue();
                int pct = total == 0 ? 0 : (int) Math.round(100.0 * c / total);
                sb.append("  ").append(opt).append(" — ").append(pct).append("% (").append(c).append(")\n");
            }
            sb.append("\n");
        }
        area.setText(sb.toString());
        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton close = new JButton("סגירה");
        close.addActionListener(e -> dispose());
        add(close, BorderLayout.SOUTH);
    }
}
