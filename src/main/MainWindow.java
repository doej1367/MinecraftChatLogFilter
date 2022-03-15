package main;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import util.LogRecords;
import util.MCLogFile;
import util.MCLogLine;
import util.OSFileSystem;

/**
 *
 * @author doej1367
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static MainWindow mainWindow;
	private FolderWindow folderWindow;

	private JPanel contentPane;
	private JTextArea inputTextField;
	private JTextArea outputTextField;
	private JScrollPane scrollPaneBottom;
	private JTextArea statusTextField;
	private Button defaultButton;
	private Button addFoldersButton;
	private JPanel panel_north;
	private Component horizontalGlue;
	private Component horizontalGlue_1;
	private Component horizontalGlue_2;

	private TreeSet<String> additionalFolderPaths = new TreeSet<>();
	private LogRecords logRecords;

	private int tmpStauslength = 0;
	private JPanel panel_south;

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setFont(new Font("Consolas", Font.PLAIN, 14));
		// create window
		setTitle("Minecraft Chat Log Filter");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 540);
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 5));
		setContentPane(contentPane);

		// add buttons
		panel_north = new JPanel();
		contentPane.add(panel_north, BorderLayout.NORTH);
		panel_north.setLayout(new BoxLayout(panel_north, BoxLayout.X_AXIS));

		horizontalGlue = Box.createHorizontalGlue();
		panel_north.add(horizontalGlue);

		defaultButton = new Button("Start analyzing currently known .minecraft folders");
		defaultButton.setBackground(new Color(240, 248, 255));
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startAnalysis();
			}
		});
		panel_north.add(defaultButton);
		defaultButton.setForeground(Color.BLUE);
		defaultButton.setFont(new Font("Consolas", Font.PLAIN, 14));

		horizontalGlue_2 = Box.createHorizontalGlue();
		panel_north.add(horizontalGlue_2);

		addFoldersButton = new Button("Add custom .minecraft folder locations");
		addFoldersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							if (folderWindow == null)
								folderWindow = new FolderWindow(mainWindow);
							folderWindow.setVisible(true);
						} catch (Exception ignored) {
						}
					}
				});
			}
		});
		addFoldersButton.setBackground(new Color(255, 240, 245));
		panel_north.add(addFoldersButton);
		addFoldersButton.setForeground(new Color(153, 0, 102));
		addFoldersButton.setFont(new Font("Consolas", Font.PLAIN, 14));

		horizontalGlue_1 = Box.createHorizontalGlue();
		panel_north.add(horizontalGlue_1);

		panel_south = new JPanel();
		contentPane.add(panel_south, BorderLayout.CENTER);
		panel_south.setLayout(new BorderLayout(0, 0));

		// add a scrollable text field with multiple lines of text for output
		JScrollPane scrollPaneTop = new JScrollPane();
		panel_south.add(scrollPaneTop, BorderLayout.NORTH);
		inputTextField = new JTextArea();
		inputTextField.setLineWrap(true);
		inputTextField.setWrapStyleWord(true);
		inputTextField.setRows(4);
		inputTextField.setFont(new Font("Consolas", Font.PLAIN, 14));
		inputTextField.setText("(You bought Kismet Feather!.*)|(You claimed Kismet Feather from .* auction!)|(The Catacombs - Floor VII)->(Team Score: [0-9]+ \\(S\\+\\).*)");
		inputTextField.setEditable(true);
		scrollPaneTop.setViewportView(inputTextField);

		// add a scrollable text field with multiple lines of text for output
		JScrollPane scrollPaneMiddle = new JScrollPane();
		panel_south.add(scrollPaneMiddle, BorderLayout.CENTER);
		outputTextField = new JTextArea();
		outputTextField.setFont(new Font("Consolas", Font.PLAIN, 14));
		outputTextField.setText("");
		outputTextField.setEditable(false);
		scrollPaneMiddle.setViewportView(outputTextField);

		// add a scrollable text field with multiple lines of text for debug output
		scrollPaneBottom = new JScrollPane();
		panel_south.add(scrollPaneBottom, BorderLayout.SOUTH);
		statusTextField = new JTextArea();
		statusTextField.setRows(5);
		statusTextField.setFont(new Font("Consolas", Font.PLAIN, 14));
		statusTextField.setText("");
		statusTextField.setEditable(false);
		scrollPaneBottom.setViewportView(statusTextField);
	}

	private synchronized void analyze() {
		try {
			OSFileSystem fileSystem = new OSFileSystem(mainWindow);
			ArrayList<File> minecraftLogFolders = fileSystem.lookForMinecraftLogFolders();

			// look for log files
			ArrayList<File> allFiles = new ArrayList<>();
			for (File minecraftLogFolder : minecraftLogFolders) {
				addStatus("INFO: Gathering log files from " + minecraftLogFolder.getAbsolutePath());
				for (File logFile : minecraftLogFolder.listFiles())
					if (logFile.getName().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}-[0-9]+\\.log\\.gz|latest\\.log"))
						allFiles.add(logFile);
			}

			// analyze all found log files
			addStatus("INFO: Loading log files (this might take a minute)");

			int counter = 0;
			int fileCount = allFiles.size();
			MCLogFile minecraftLogFile = null;
			TreeMap<String, Integer> playerNames = new TreeMap<>();
			String lastLoginName = null;
			String lineFilter = null;

			String logLineSpecialFilterRegex = inputTextField.getText();

			List<MCLogLine> relevantLogLines = new ArrayList<>();
			logRecords = new LogRecords(relevantLogLines, logLineSpecialFilterRegex);

			Collections.sort(allFiles, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					long tmp = getLastModifiedTime(f1) - getLastModifiedTime(f2);
					return tmp < 0 ? -1 : tmp > 0 ? 1 : 0;
				}
			});
			for (File logFile : allFiles) {
				if (counter++ % 50 == 0)
					addStatusTemporaryly(
							"INFO: Loading " + fileCount + " files - " + (counter * 100 / fileCount) + "%");
				try {
					minecraftLogFile = new MCLogFile(logFile, getPreviousFileInFolder(counter, allFiles));
					if (minecraftLogFile.getPlayerName() != null) {
						lastLoginName = minecraftLogFile.getPlayerName();
						playerNames.put(lastLoginName, playerNames.getOrDefault(lastLoginName, 0) + 1);
					}
					lineFilter = logRecords.getLineFilter();
					relevantLogLines.addAll(minecraftLogFile.filterLines(lineFilter, lastLoginName));
				} catch (FileNotFoundException ignored) {
				} catch (IOException ignored) {
				}
			}
			Collections.sort(relevantLogLines);

			String name = playerNames.entrySet().stream().max((a, b) -> a.getValue() - b.getValue()).get().getKey();
			for (MCLogLine l : relevantLogLines) {
				if (l.getPlayerName() == null)
					l.setPlayerName(name);
				else
					break;
			}
			// addStatus("INFO: Found most logins from " + name);

			for (int i = 0; i < relevantLogLines.size(); i++)
				logRecords.add(i);

			// send data to google form
			StringBuilder sb = new StringBuilder();
			for (MCLogLine entry : logRecords) {
				sb.append(entry.getCreationTime());
				sb.append(":");
				sb.append(entry.getText());
				sb.append("\n");
			}
			setOutput(sb.toString());
			addStatus("INFO: Found " + logRecords.size() + " results!");
			defaultButton.setEnabled(true);
			addFoldersButton.setEnabled(true);
		} catch (Exception e) {
			addStatus("ERROR: " + e.toString());
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement elem : e.getStackTrace()) {
				sb.append("        ");
				sb.append(elem.toString());
				sb.append("\n");
			}
			addStatus(sb.toString());
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					mainWindow = new MainWindow();
					mainWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void startAnalysis() {
		defaultButton.setEnabled(false);
		addFoldersButton.setEnabled(false);
		Thread t0 = new Thread() {
			@Override
			public void run() {
				mainWindow.analyze();
			}
		};
		t0.start();
	}

	/**
	 * Adds a new line of text to the outputTextField
	 *
	 * @param s - text to add
	 */
	public void setOutput(String s) {
		outputTextField.setText(s);
	}

	/**
	 * Adds a new line of text to the statusTextField
	 *
	 * @param s - text to add
	 */
	public void addStatus(String s) {
		String oldText = statusTextField.getText();
		statusTextField.setText(oldText.substring(0, oldText.length() - tmpStauslength) + s + "\n");
		tmpStauslength = 0;
		JScrollBar vertical = scrollPaneBottom.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	public void addStatusTemporaryly(String s) {
		if (tmpStauslength == 0)
			scrollPaneBottom.getVerticalScrollBar().setValue(scrollPaneBottom.getVerticalScrollBar().getMaximum());
		String oldText = statusTextField.getText();
		statusTextField.setText(oldText.substring(0, oldText.length() - tmpStauslength) + s);
		tmpStauslength = s.length();
	}

	public TreeSet<String> getAdditionalFolderPaths() {
		return additionalFolderPaths;
	}

	private long getLastModifiedTime(File file) {
		try {
			return Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
		} catch (IOException ignored) {
		}
		return 0;
	}

	private File getPreviousFileInFolder(int counter, ArrayList<File> allFiles) {
		File current = allFiles.get(counter - 1);
		File previous = null;
		for (int i = counter - 1; i > 0; i--) {
			previous = allFiles.get(i - 1);
			if (previous.getParentFile().equals(current.getParentFile()) && previous.getName().endsWith(".gz"))
				return previous;
		}
		return null;
	}
}
