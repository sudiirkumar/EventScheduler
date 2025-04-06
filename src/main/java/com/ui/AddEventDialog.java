package main.java.com.ui;
import main.java.com.eventscheduler.EventScheduler;
import main.java.com.manager.*;
import main.java.com.model.*;
import main.java.com.model.Event;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.time.ZoneId;
import java.util.Date;

public class AddEventDialog extends JDialog {
    public AddEventDialog(JCalendarPanel calendarPanel, EventManager eventManager, LocalDate selectedDate) {
        super(SwingUtilities.getWindowAncestor(calendarPanel), "Add Event", ModalityType.APPLICATION_MODAL);
        setSize(400, 550);
        setLocationRelativeTo(calendarPanel);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField();
        JTextArea descriptionArea = new JTextArea(2, 10);
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JComboBox<Priority> priorityCombo = new JComboBox<>(Priority.values());
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Work", "Personal", "Family", "Health"});
        JComboBox<ReminderType> reminderTypeCombo = new JComboBox<>(ReminderType.values());

        JCheckBox rem15min = new JCheckBox("15 Minutes Before");
        JCheckBox rem1hr = new JCheckBox("1 Hour Before");
        JCheckBox rem1day = new JCheckBox("1 Day Before");
        JPanel reminderPanel = new JPanel(new GridLayout(0, 1));
        reminderPanel.add(rem15min);
        reminderPanel.add(rem1hr);
        reminderPanel.add(rem1day);

        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));

        panel.add(new JLabel("Event Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));
        panel.add(new JLabel("Time:"));
        panel.add(timeSpinner);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Reminder Type:"));
        panel.add(reminderTypeCombo);
        panel.add(new JLabel("Reminders:"));
        panel.add(reminderPanel);

        JButton submitButton = new JButton("Create Event");
        submitButton.addActionListener(e -> {
            try {
                LocalDateTime eventDateTime = LocalDateTime.of(
                        selectedDate,
                        ((Date) timeSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                );

                Event newEvent = new Event(
                        titleField.getText(),
                        descriptionArea.getText(),
                        eventDateTime,
                        (Priority) priorityCombo.getSelectedItem(),
                        (String) categoryCombo.getSelectedItem()
                );

                Set<Integer> reminders = new HashSet<>();
                if (rem15min.isSelected()) reminders.add(15);
                if (rem1hr.isSelected()) reminders.add(60);
                if (rem1day.isSelected()) reminders.add(1440);
                newEvent.setReminderMinutes(reminders);
                newEvent.setReminderType((ReminderType) reminderTypeCombo.getSelectedItem());

                eventManager.addEvent(newEvent);
                calendarPanel.refreshCalendar(); // Refresh calendar
                JOptionPane.showMessageDialog(this, "Event created successfully!");
                EventScheduler.searchButton.doClick();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating event: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(submitButton);
        add(panel);
    }
}