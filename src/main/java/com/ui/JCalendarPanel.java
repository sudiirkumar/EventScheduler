package main.java.com.ui;

import main.java.com.manager.*;
import main.java.com.model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.util.List;
import java.util.stream.Collectors;

public class JCalendarPanel extends JPanel {
    private final EventManager eventManager;
    private LocalDate currentDate;
    private JLabel monthLabel;
    private JPanel daysPanel;

    public JCalendarPanel(EventManager eventManager) {
        this.eventManager = eventManager;
        this.currentDate = LocalDate.now();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel navPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("←");
        JButton nextButton = new JButton("→");
        monthLabel = new JLabel("", SwingConstants.CENTER);

        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            updateCalendar();
        });

        navPanel.add(prevButton, BorderLayout.WEST);
        navPanel.add(nextButton, BorderLayout.EAST);
        navPanel.add(monthLabel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            headerPanel.add(new JLabel(day, SwingConstants.CENTER));
        }

        daysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        updateCalendar();

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(daysPanel, BorderLayout.CENTER);

        add(navPanel, BorderLayout.NORTH);
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    public void refreshCalendar() {
        updateCalendar();
    }

    private void updateCalendar() {
        daysPanel.removeAll();
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < dayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }

        LocalDate date = firstOfMonth;
        while (date.getMonth() == currentDate.getMonth()) {
            daysPanel.add(createDayButton(date));
            date = date.plusDays(1);
        }

        revalidate();
        repaint();
    }

    private JButton createDayButton(LocalDate date) {
        JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
        //increase button font size
        button.setFont(button.getFont().deriveFont(30f));
        button.setFocusPainted(false);

        if (date.equals(LocalDate.now())) {
            button.setBackground(new Color(220, 240, 255));
        }

        long eventCount = eventManager.getEvents().stream()
                .filter(e -> e.getEventTime().toLocalDate().equals(date))
                .count();

        if (eventCount > 0) {
            button.setToolTipText(eventCount + " events");
            button.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        }

        button.addActionListener(e -> showDayEvents(date));
        return button;
    }

    private void showDayEvents(LocalDate date) {
        List<Event> events = eventManager.getEvents().stream()
                .filter(e -> e.getEventTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        String message = events.stream()
                .map(e -> "• " + e.getTitle() + " (" + e.getEventTime().format(DateTimeFormatter.ofPattern("HH:mm")) + ")")
                .collect(Collectors.joining("\n"));

        JPanel panel = new JPanel(new BorderLayout());
        JTextArea eventListArea = new JTextArea(message.isEmpty() ? "No events this day" : message);
        eventListArea.setEditable(false);
        panel.add(new JScrollPane(eventListArea), BorderLayout.CENTER);

        JButton addEventButton = new JButton("Add Event");
        addEventButton.addActionListener(e -> new AddEventDialog(this, eventManager, date).setVisible(true));
        panel.add(addEventButton, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "Events on " + date.format(DateTimeFormatter.ISO_DATE), JOptionPane.INFORMATION_MESSAGE);
    }
}