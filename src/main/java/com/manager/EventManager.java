package main.java.com.manager;

import main.java.com.model.*;
import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import main.java.com.storage.*;

public class EventManager {
    private List<Event> events = new ArrayList<>();
    private EventStorage storage = new FileEventStorage();

    public void addEvent(Event event) {
        events.add(event);
        saveEvents();
    }

    public void removeEvent(Event event) {
        events.removeIf(e -> e.getId().equals(event.getId()));
        saveEvents();
    }

    public void removePastEvents() {
        LocalDateTime now = LocalDateTime.now();
        events.removeIf(e -> e.getEventTime().isBefore(now));
        saveEvents();
    }

    public List<Event> getEvents() { return new ArrayList<>(events); }

    public List<Event> getUpcomingEvents() {
        return events.stream()
                .filter(e -> e.getEventTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Event::getEventTime))
                .collect(Collectors.toList());
    }

    public List<Event> searchEvents(String query) {
        String lcQuery = query.toLowerCase();
        return events.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lcQuery) ||
                        e.getDescription().toLowerCase().contains(lcQuery))
                .collect(Collectors.toList());
    }

    public void loadEvents() {
        try {
            events = storage.loadEvents();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error loading events: " + e.getMessage());
        }
    }

    public void saveEvents() {
        try {
            storage.saveEvents(events);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving events: " + e.getMessage());
        }
    }
}