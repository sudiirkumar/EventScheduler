package main.java.com.storage;

import main.java.com.model.*;
import java.io.*;
import java.util.*;

public class FileEventStorage implements EventStorage {
    private static final String FILE_NAME = "events.dat";

    public void saveEvents(List<Event> events) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(events);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Event> loadEvents() throws IOException, ClassNotFoundException {
        File file = new File(FILE_NAME);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Event>) ois.readObject();
        }
    }
}