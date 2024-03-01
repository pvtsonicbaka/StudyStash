import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudyStash3 extends JFrame {
    private List<String> academicCalendars = new ArrayList<>();
    private JTextArea textArea;
    private UndoManager undoManager;
    private boolean isFileSaved = true;
    private List<String> savedNotes = new ArrayList<>();
    private static final String SAVED_NOTES_FILE = "saved_notes.ser";
    private static final String ACADEMIC_CALENDARS_DIR = "academic_calendars/";

    public StudyStash3() {
        setTitle("StudyStash");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.GRAY);

        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem showNotesMenuItem = new JMenuItem("Show Saved Notes");
        JMenuItem addCalendarMenuItem = new JMenuItem("Add Academic Calendar");
        JMenuItem viewCalendarMenuItem = new JMenuItem("View Academic Calendar");
        fileMenu.add(newMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(showNotesMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(addCalendarMenuItem);
        fileMenu.add(viewCalendarMenuItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem findMenuItem = new JMenuItem("Find");
        JMenuItem replaceMenuItem = new JMenuItem("Replace");
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(undoMenuItem);
        editMenu.add(findMenuItem);
        editMenu.add(replaceMenuItem);
        menuBar.add(editMenu);

        JMenu preferencesMenu = new JMenu("Preferences");
        JMenuItem textColorMenuItem = new JMenuItem("Change Text Color");
        JMenuItem backgroundColorMenuItem = new JMenuItem("Change Background Color");
        JMenuItem fontSizeMenuItem = new JMenuItem("Change Font Size");
        preferencesMenu.add(textColorMenuItem);
        preferencesMenu.add(backgroundColorMenuItem);
        preferencesMenu.add(fontSizeMenuItem);
        menuBar.add(preferencesMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));

        newMenuItem.addActionListener(e -> newFile());
        saveMenuItem.addActionListener(e -> saveFile());
        openMenuItem.addActionListener(e -> openFile());
        copyMenuItem.addActionListener(e -> textArea.copy());
        pasteMenuItem.addActionListener(e -> textArea.paste());
        undoMenuItem.addActionListener(e -> undo());
        textColorMenuItem.addActionListener(e -> changeTextColor());
        backgroundColorMenuItem.addActionListener(e -> changeBackgroundColor());
        fontSizeMenuItem.addActionListener(e -> changeFontSize());
        aboutMenuItem.addActionListener(e -> displayAboutMessage());
        showNotesMenuItem.addActionListener(e -> showSavedNotes());
        addCalendarMenuItem.addActionListener(e -> addAcademicCalendar());
        viewCalendarMenuItem.addActionListener(e -> viewAcademicCalendar());
        findMenuItem.addActionListener(e -> showFindDialog());
        replaceMenuItem.addActionListener(e -> showReplaceDialog());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                saveNotesToFile();
                dispose();
            }
        });

        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                isFileSaved = false;
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                isFileSaved = false;
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                isFileSaved = false;
            }
        });

        loadNotesFromFile();
        loadAcademicCalendars();
    }

    private void newFile() {
        if (!isFileSaved) {
            int option = JOptionPane.showConfirmDialog(this, "Do you want to save the changes before creating a new file?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                saveFile();
                clearTextArea();
            } else if (option == JOptionPane.NO_OPTION) {
                clearTextArea();
            }
        } else {
            clearTextArea();
        }
    }

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textArea.getText());
                isFileSaved = true;
                savedNotes.add(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openFile() {
        if (!isFileSaved) {
            int option = JOptionPane.showConfirmDialog(this, "Do you want to save the changes before opening a new file?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                saveFile();
                chooseFileToOpen();
            } else if (option == JOptionPane.NO_OPTION) {
                chooseFileToOpen();
            }
        } else {
            chooseFileToOpen();
        }
    }

    private void chooseFileToOpen() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                textArea.setText(sb.toString());
                isFileSaved = true;
                savedNotes.add(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSavedNotes() {
        JFrame notesFrame = new JFrame("Saved Notes");
        JPanel panel = new JPanel(new GridLayout(savedNotes.size(), 2));

        for (String note : savedNotes) {
            JButton button = new JButton(note);
            JButton deleteButton = new JButton("Delete");
            button.addActionListener(e -> displaySelectedNoteContent(note));
            deleteButton.addActionListener(e -> deleteNoteButtonClicked(note));
            panel.add(button);
            panel.add(deleteButton);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        notesFrame.add(scrollPane);
        notesFrame.setSize(300, 400);
        notesFrame.setLocationRelativeTo(this);
        notesFrame.setVisible(true);
    }

    private void deleteNoteButtonClicked(String notePath) {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this note?", "Delete Note", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            savedNotes.remove(notePath);
            showSavedNotes(); // Refresh the list after deletion
        }
    }

    private void displaySelectedNoteContent(String notePath) {
        JFrame noteContentFrame = new JFrame("Note Content");
        JTextArea noteContentArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(noteContentArea);
        noteContentFrame.add(scrollPane);

        try (BufferedReader reader = new BufferedReader(new FileReader(notePath))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            noteContentArea.setText(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        noteContentFrame.setSize(400, 300);
        noteContentFrame.setLocationRelativeTo(null);
        noteContentFrame.setVisible(true);
    }

    private void undo() {
        if (undoManager.canUndo()) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void changeTextColor() {
        Color color = JColorChooser.showDialog(this, "Choose Text Color", textArea.getForeground());
        if (color != null) {
            textArea.setForeground(color);
        }
    }

    private void changeBackgroundColor() {
        Color color = JColorChooser.showDialog(this, "Choose Background Color", textArea.getBackground());
        if (color != null) {
            textArea.setBackground(color);
        }
    }

    private void changeFontSize() {
        String input = JOptionPane.showInputDialog(this, "Enter Font Size:");
        try {
            int size = Integer.parseInt(input);
            Font font = textArea.getFont().deriveFont((float) size);
            textArea.setFont(font);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayAboutMessage() {
        JOptionPane.showMessageDialog(this, "Made by Sajjad", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearTextArea() {
        textArea.setText("");
        isFileSaved = true;
    }

    private void saveNotesToFile() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(SAVED_NOTES_FILE))) {
            outputStream.writeObject(savedNotes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadNotesFromFile() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(SAVED_NOTES_FILE))) {
            Object obj = inputStream.readObject();
            if (obj instanceof List) {
                savedNotes = (List<String>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addAcademicCalendar() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
               
                academicCalendars.add(file.getName());
                JOptionPane.showMessageDialog(this, "Academic calendar added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding academic calendar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    

    private void viewAcademicCalendar() {
        JFrame calendarViewer = new JFrame("Academic Calendar Viewer");
        JPanel panel = new JPanel(new GridLayout(0, 1));
    
        File directory = new File(ACADEMIC_CALENDARS_DIR);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isImageFile(file)) {
                        try {
                            BufferedImage image = loadImage(file);
                            if (image != null) {
                                JLabel label = new JLabel(new ImageIcon(image));
                                panel.add(label);
                                academicCalendars.add(file.getAbsolutePath()); // Store full file path
                            }
                        } catch (Exception e) {
                            System.out.println("Error loading image: " + e.getMessage());
                        }
                    }
                }
            } else {
                System.out.println("No files found in directory: " + ACADEMIC_CALENDARS_DIR);
            }
        } else {
            System.out.println("Academic calendar directory not found: " + ACADEMIC_CALENDARS_DIR);
        }
    
        JScrollPane scrollPane = new JScrollPane(panel);
        calendarViewer.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
        calendarViewer.setSize(800, 600);
        calendarViewer.setLocationRelativeTo(null);
        calendarViewer.setVisible(true);
    }
    

    private void loadAcademicCalendars() {
        File directory = new File(ACADEMIC_CALENDARS_DIR);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                academicCalendars.add(file.getName());
            }
        }
    }

    private void showFindDialog() {
        String searchText = JOptionPane.showInputDialog(this, "Enter text to find:");
        if (searchText != null && !searchText.isEmpty()) {
            String text = textArea.getText();
            int index = text.indexOf(searchText);
            if (index != -1) {
                textArea.setCaretPosition(index);
                textArea.setSelectionStart(index);
                textArea.setSelectionEnd(index + searchText.length());
            } else {
                JOptionPane.showMessageDialog(this, "Text not found: " + searchText, "Find", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showReplaceDialog() {
        String searchText = JOptionPane.showInputDialog(this, "Enter text to find:");
        if (searchText != null && !searchText.isEmpty()) {
            String replaceText = JOptionPane.showInputDialog(this, "Enter text to replace:");
            if (replaceText != null) {
                String text = textArea.getText();
                text = text.replace(searchText, replaceText);
                textArea.setText(text);
            }
        }
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }

    private BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StudyStash3 notepad = new StudyStash3();
                notepad.setVisible(true);
            }
        });
    }
}
