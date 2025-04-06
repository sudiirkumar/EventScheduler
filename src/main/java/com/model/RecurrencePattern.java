package main.java.com.model;

import java.io.Serializable;
import java.time.LocalDate;

public class RecurrencePattern implements Serializable {
    private RecurrenceType type;
    private LocalDate endDate;

    public RecurrencePattern(RecurrenceType type, LocalDate endDate) {
        this.type = type;
        this.endDate = endDate;
    }

    public RecurrenceType getType() { return type; }
    public void setType(RecurrenceType type) { this.type = type; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}