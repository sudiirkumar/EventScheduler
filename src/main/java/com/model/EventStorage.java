package main.java.com.model;

import java.io.*;
import java.util.List;

public interface EventStorage {
    void saveEvents(List<Event> events) throws IOException;
    List<Event> loadEvents() throws IOException, ClassNotFoundException;
}