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

public class StudyStash extends JFrame {
    private JTextArea textArea;
    private UndoManager undoManager;
    private boolean isFileSaved = true;
    private List<String> savedNotes = new ArrayList<>();
    private List<String> academicCalendars = new ArrayList<>();
    private static final String SAVED_NOTES_FILE = "saved_notes.ser";
    private static final String ACADEMIC_CALENDARS_DIR = "academic_calendars/";

    public StudyStash() {
        setTitle("StudyStash");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.GRAY); // Set background color of menu bar

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
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(undoMenuItem);
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

        // Add shortcut keys
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));

        // Add action listeners
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
        JPanel panel = new JPanel(new GridLayout(savedNotes.size(), 1));

        for (String note : savedNotes) {
            JButton button = new JButton(note);
            button.addActionListener(e -> displaySelectedNoteContent(note));
            panel.add(button);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        notesFrame.add(scrollPane);
        notesFrame.setSize(300, 400);
        notesFrame.setLocationRelativeTo(this);
        notesFrame.setVisible(true);
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
                convertPDFToJPEG(file);
                academicCalendars.add(file.getName());
                JOptionPane.showMessageDialog(this, "Academic calendar added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding academic calendar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void convertPDFToJPEG(File pdfFile) throws IOException {
        // Implement PDF to JPEG conversion here
        // For simplicity, we are not implementing this in this example
    }

    private void viewAcademicCalendar() {
        // Print out the list of files in the academic calendar directory
        System.out.println("Files in academic calendar directory:");
        File directory = new File(ACADEMIC_CALENDARS_DIR);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                System.out.println(file.getAbsolutePath());
            }
        } else {
            System.out.println("Academic calendar directory not found.");
        }
        
        // Create the JFrame and JPanel for displaying the academic calendars
        JFrame calendarFrame = new JFrame("Academic Calendar");
        JPanel panel = new JPanel(new GridLayout(academicCalendars.size(), 1));
    
        // Iterate through each academic calendar file and attempt to load it
        for (String calendar : academicCalendars) {
            try {
                File calendarFile = new File(ACADEMIC_CALENDARS_DIR + calendar);
                if (!calendarFile.exists()) {
                    System.err.println("File not found: " + calendarFile.getAbsolutePath());
                    continue;
                }
                BufferedImage image = ImageIO.read(calendarFile);
                if (image == null) {
                    System.err.println("Failed to read image file: " + calendarFile.getAbsolutePath());
                    continue;
                }
                JLabel label = new JLabel(new ImageIcon(image));
                panel.add(label);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // Create and display the scrollable panel containing the academic calendars
        JScrollPane scrollPane = new JScrollPane(panel);
        calendarFrame.add(scrollPane);
        calendarFrame.setSize(800, 600); // Adjusted size for better viewing
        calendarFrame.setLocationRelativeTo(this);
        calendarFrame.setVisible(true);
    }
    
    
    
    

    private void loadAcademicCalendars() {
        File directory = new File(ACADEMIC_CALENDARS_DIR);
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                academicCalendars.add(file.getName());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StudyStash notepad = new StudyStash();
                notepad.setVisible(true);
            }
        });
    }
}
