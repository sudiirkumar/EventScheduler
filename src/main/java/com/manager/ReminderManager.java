package main.java.com.manager;
import main.java.com.model.*;
import main.java.com.eventscheduler.EventScheduler;
import main.java.com.model.Event;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.util.concurrent.*;

public class ReminderManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final EventManager eventManager;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledReminders = new ConcurrentHashMap<>();

    public ReminderManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkAndScheduleReminders, 0, 30, TimeUnit.SECONDS);
    }

    private void checkAndScheduleReminders() {
        eventManager.getEvents().forEach(event -> {
            event.getReminderMinutes().forEach(reminderMinutes -> {
                String reminderKey = event.getId() + "-" + reminderMinutes;
                if (!scheduledReminders.containsKey(reminderKey)) {
                    scheduleReminder(event, reminderMinutes, reminderKey);
                }
            });
        });
    }

    private void scheduleReminder(Event event, int reminderMinutes, String reminderKey) {
        LocalDateTime reminderTime = event.getEventTime().minusMinutes(reminderMinutes);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(reminderTime)) return;

        long initialDelay = Duration.between(now, reminderTime).toMillis();
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            showNotification(event, reminderMinutes);
            scheduledReminders.remove(reminderKey);
        }, initialDelay, TimeUnit.MILLISECONDS);

        scheduledReminders.put(reminderKey, future);
    }

    public void cancelReminder(String eventId, int minutes) {
        String key = eventId + "-" + minutes;
        ScheduledFuture<?> future = scheduledReminders.get(key);
        if (future != null) {
            future.cancel(true);
            scheduledReminders.remove(key);
        }
    }

    private void showNotification(Event event, int minutesBefore) {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            if (tray.getTrayIcons().length == 0) return;

            TrayIcon trayIcon = tray.getTrayIcons()[0];
            String timeText = formatTimeText(minutesBefore);
            String message = buildNotificationMessage(event, timeText);

            if (event.getReminderType() == ReminderType.MUSIC) {
                EventScheduler.playReminderSound();
            }

            SwingUtilities.invokeLater(() -> {
                trayIcon.displayMessage(
                        "Upcoming Event: " + event.getTitle(),
                        message,
                        TrayIcon.MessageType.INFO
                );
            });
        } catch (Exception e) {
            System.err.println("Error showing notification: " + e.getMessage());
        }
    }

    private String formatTimeText(int minutesBefore) {
        if (minutesBefore >= 1440) return (minutesBefore / 1440) + " days";
        if (minutesBefore >= 60) return (minutesBefore / 60) + " hours";
        return minutesBefore + " minutes";
    }

    private String buildNotificationMessage(Event event, String timeText) {
        return String.format(
                "Event starting in %s:\n%s\n%s",
                timeText,
                event.getEventTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                event.getDescription()
        );
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}