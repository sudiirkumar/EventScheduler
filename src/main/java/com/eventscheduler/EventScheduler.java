package main.java.com.eventscheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.*;
import javax.swing.border.EmptyBorder;

import main.java.com.manager.*;
import main.java.com.model.*;
import main.java.com.model.Event;
import main.java.com.ui.*;

public class EventScheduler extends JFrame {
    public static JButton searchButton;

    private final EventManager eventManager = new EventManager();
    private final ReminderManager reminderManager = new ReminderManager(eventManager);
    private JTable eventTable;
    private EventTableModel tableModel;
    private JCalendarPanel calendarPanel;
    private static Clip reminderClip;

    public EventScheduler() {
        initializeUI();
        loadEvents();
        initializeAudio();
        reminderManager.start();
        startAutoCleanup();
    }

    private void initializeUI() {
        setTitle("Event Scheduler Pro");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        calendarPanel = new JCalendarPanel(eventManager);
        tabbedPane.addTab("Calendar", new JScrollPane(calendarPanel));
        tabbedPane.addTab("Events", createListPanel());
        add(tabbedPane); // Removed the "Add Event" tab

        setJMenuBar(createMenuBar());
        setupSystemTray();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeAudio() {
        try {
            // Load from file system instead of resources
            File soundFile = new File("alarm.wav");
            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                reminderClip = AudioSystem.getClip();
                reminderClip.open(audioIn);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Alarm sound file not found! Please create a WAV file named 'alarm.wav' " +
                                "in the application directory.", "Audio Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading sound: " + e.getMessage(), "Audio Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void playReminderSound() {
        if (reminderClip != null) {
            new Thread(() -> {
                try {
                    reminderClip.stop();
                    reminderClip.setFramePosition(0);
                    reminderClip.start();
                    // Loop continuously until stopped
                    reminderClip.loop(Clip.LOOP_CONTINUOUSLY);
                } catch (Exception e) {
                    System.err.println("Error playing sound: " + e.getMessage());
                }
            }).start();
        }
    }


    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        tableModel = new EventTableModel(eventManager.getEvents());
        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(30);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setDefaultRenderer(Object.class, new EventTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(eventTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchEvents(searchField.getText()));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedEvent());
        JButton manageRemindersButton = new JButton("Manage Reminders");
        manageRemindersButton.addActionListener(e -> manageReminders());
        controlPanel.add(deleteButton);
        controlPanel.add(manageRemindersButton);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void manageReminders() {
        int row = eventTable.getSelectedRow();
        if (row >= 0) {
            Event selectedEvent = tableModel.getEventAt(row);
            new ManageRemindersDialog(this, selectedEvent).setVisible(true);
            refreshUI();
        }
    }

    class ManageRemindersDialog extends JDialog {
        public ManageRemindersDialog(JFrame parent, Event event) {
            super(parent, "Manage Reminders for: " + event.getTitle(), true);
            setSize(300, 200);

            JPanel panel = new JPanel(new BorderLayout());
            DefaultListModel<Integer> listModel = new DefaultListModel<>();
            event.getReminderMinutes().forEach(listModel::addElement);

            JList<Integer> reminderList = new JList<>(listModel);
            panel.add(new JScrollPane(reminderList), BorderLayout.CENTER);

            JButton deleteButton = new JButton("Delete Selected");
            deleteButton.addActionListener(e -> {
                int selectedIndex = reminderList.getSelectedIndex();
                if (selectedIndex != -1) {
                    int minute = listModel.get(selectedIndex);
                    event.getReminderMinutes().remove(minute);
                    eventManager.saveEvents();
                    reminderManager.cancelReminder(event.getId(), minute);
                    listModel.remove(selectedIndex);
                }

            });
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (reminderClip != null && reminderClip.isRunning()) {
                        reminderClip.stop();
                    }
                }
            });

            panel.add(deleteButton, BorderLayout.SOUTH);
            add(panel);

        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            TrayIcon trayIcon = new TrayIcon(image, "Event Scheduler");
            PopupMenu popup = new PopupMenu();
            MenuItem openItem = new MenuItem("Open");
            openItem.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
        } catch (AWTException ex) {
            System.err.println("System tray initialization failed: " + ex.getMessage());
        }
    }

    public void startAutoCleanup() {
        ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        cleanupScheduler.scheduleAtFixedRate(() -> {
            eventManager.removePastEvents();
            refreshUI();
        }, 0, 1, TimeUnit.HOURS);
    }

    public void refreshUI() {
        tableModel.setEvents(eventManager.getEvents());
        calendarPanel.refreshCalendar();
    }

    private void loadEvents() {
        eventManager.loadEvents();
    }

    private void searchEvents(String query) {
        tableModel.setEvents(eventManager.searchEvents(query));
    }

    private void deleteSelectedEvent() {
        int row = eventTable.getSelectedRow();
        if (row >= 0) {
            eventManager.removeEvent(tableModel.getEventAt(row));
            refreshUI();
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Event Scheduler Pro\nVersion 3.0\n© 2023 Event Scheduler Team",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                EventScheduler app = new EventScheduler();
                app.setVisible(true);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    app.reminderManager.shutdown();
                    if (reminderClip != null) {
                        reminderClip.close();
                    }
                }));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Application initialization failed:\n" + e.getMessage(),
                        "Fatal Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
