package main.java.com.ui;

import main.java.com.model.*;
import javax.swing.table.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EventTableModel extends AbstractTableModel {
    private final String[] COLUMNS = {"Title", "Date", "Priority", "Category", "Description"};
    private List<Event> events;

    public EventTableModel(List<Event> events) {
        this.events = new ArrayList<>(events);
    }

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        fireTableDataChanged();
    }

    public Event getEventAt(int row) {
        return events.get(row);
    }

    @Override public int getRowCount() { return events.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 2 ? Priority.class : String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Event event = events.get(rowIndex);
        switch (columnIndex) {
            case 0: return event.getTitle();
            case 1: return event.getEventTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
            case 2: return event.getPriority();
            case 3: return event.getCategory();
            case 4: return event.getDescription();
            default: return null;
        }
    }
}