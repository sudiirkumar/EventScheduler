package main.java.com.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class Event implements Serializable {
    private String id = UUID.randomUUID().toString();
    private String title;
    private String description;
    private LocalDateTime eventTime;
    private Priority priority;
    private String category;
    private RecurrencePattern recurrencePattern;
    private Set<Integer> reminderMinutes = new HashSet<>();
    private ReminderType reminderType = ReminderType.TEXT;

    public Event(String title, String description, LocalDateTime eventTime,
                 Priority priority, String category) {
        this.title = title;
        this.description = description;
        this.eventTime = eventTime;
        this.priority = priority;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    public Set<Integer> getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(Set<Integer> reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }
    public ReminderType getReminderType() { return reminderType; }
    public void setReminderType(ReminderType reminderType) { this.reminderType = reminderType; }
}