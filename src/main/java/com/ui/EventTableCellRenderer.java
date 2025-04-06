package main.java.com.ui;

import main.java.com.model.*;
import main.java.com.model.Event;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.util.*;

public class EventTableCellRenderer extends DefaultTableCellRenderer {
    private final Map<Priority, Color> PRIORITY_COLORS = Map.of(
            Priority.HIGH, new Color(255, 200, 200),
            Priority.MEDIUM, new Color(255, 235, 200),
            Priority.LOW, new Color(200, 255, 200)
    );

    private final Map<String, Color> CATEGORY_COLORS = Map.of(
            "Work", new Color(220, 220, 255),
            "Personal", new Color(255, 220, 255),
            "Family", new Color(220, 255, 220),
            "Health", new Color(255, 220, 220)
    );

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        EventTableModel model = (EventTableModel) table.getModel();
        Event event = model.getEventAt(row);
        JComponent jc = (JComponent) c;

        if (column == 2) {
            c.setBackground(PRIORITY_COLORS.get(event.getPriority()));
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        } else if (column == 3) {
            c.setBackground(CATEGORY_COLORS.getOrDefault(event.getCategory(), Color.WHITE));
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            c.setBackground(Color.WHITE);
            ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
        }

        if (event.getEventTime().toLocalDate().equals(LocalDate.now())) {
            jc.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.BLUE));
        } else {
            jc.setBorder(null);
        }

        return c;
    }
}