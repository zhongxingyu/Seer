 package stereomatching.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * Class that defines a GUI for handling user input.
  * Two files are chosen, one for the left image of a
  * stereo pair and one for the right image. The images
  * are displayed (if possible to do this) as image icons
  * in the GUI. When the match button is pressed the
  * GUI passes control to the StereoMatchingController
  * class, which then passes control to a bash script
  * which runs the Vector Pascal matching code.
  * 
  * Possible additions if time:
  *     > A combo box to select matching metric (if I can
  *     code up the VP in time)
  *     > A number of other things to work on single images
  * 
  * @author Adam Murray
  * @version 0.1
  *
  */
 @SuppressWarnings("serial")
 public class StereoMatchingGUI extends JFrame
 {
 	private JMenuBar menubar;
 	private JPanel north, center, south;
 	private JLabel leftImageFileNameLabel, rightImageFileNameLabel;
 	private JLabel timeForLastMatchLabel, averageMatchTimeLabel;
 	private JButton runButton, openLeftImageButton, openRightImageButton;
 	private JButton clearButton, saveButton, saveAsButton, exitButton;
 	private JTextArea outputTextArea, infoTextArea, notesTextArea;
 	private JFileChooser leftImageChooser, rightImageChooser;
 
 	private ImageIcon attentionIcon = new ImageIcon("./gui_icons/attention.png");
 	private ImageIcon clearIcon = new ImageIcon("./gui_icons/eraser.png");
 	private ImageIcon errorIcon = new ImageIcon("./gui_icons/close_delete.png");
 	private ImageIcon exitIcon = new ImageIcon("./gui_icons/delete_2.png");
 	private ImageIcon infoIcon = new ImageIcon("./gui_icons/information.png");
 	private ImageIcon openLeftIcon = new ImageIcon("./gui_icons/arrow_left.png");
 	private ImageIcon openRightIcon = new ImageIcon("./gui_icons/arrow_right.png");
 	private ImageIcon runIcon = new ImageIcon("./gui_icons/play.png");
 	private ImageIcon saveIcon = new ImageIcon("./gui_icons/save_diskette_floppy_disk.png");
 	private ImageIcon saveAsIcon = new ImageIcon("./gui_icons/save_as.png");
 
 	private String leftImageFileName, rightImageFileName;
 	private String leftImageFilePath, rightImageFilePath;
 	private String outputFileName;
 	private StereoMatchingController controller;
 
 	private FileNameExtensionFilter fileNameFilter = new FileNameExtensionFilter(".bmp Images", "bmp");
 
 	private long matchStartTime, matchEndTime, matchTotalTime;
 	private long totalMatchTime;
 	private double averageMatchTime;
 	private int totalMatches = 0;
 
 	private final int GUI_WIDTH = 850;
 	private final int GUI_HEIGHT = 650;
 
 	public StereoMatchingGUI()
 	{
 		initialiseUI();
 		layoutComponents();
 		showWelcomeScreen();
 	}
 
 	private void initialiseUI()
 	{
 		setTitle("Stereo Image Matcher v0.1");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setLocationRelativeTo(null);
 		setLocationByPlatform(true);
 		setSize(GUI_WIDTH, GUI_HEIGHT);
 		setLocation(500, 100);
 		setResizable(true);
 		setVisible(true);
 	}
 
 	private void layoutComponents()
 	{
 		addMenuBar();
 		addComponentsToNorth();
 		addComponentsToCenter();
 		addComponentsToSouth();
 	}
 
 	private void addMenuBar()
 	{
 		menubar = new JMenuBar();
 
 		JMenu file = new JMenu("File");
 		file.setMnemonic(KeyEvent.VK_F);
 
 		JMenuItem openLeftImageMenuItem = new JMenuItem("Open left image...");
 		openLeftImageMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processLeftImageSelect();
 			}
 		});
 
 		file.add(openLeftImageMenuItem);
 
 		JMenuItem openRightImageMenuItem = new JMenuItem("Open right image...");
 		openRightImageMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processRightImageSelect();
 			}
 		});
 
 		file.add(openRightImageMenuItem);
 
 		file.addSeparator();
 		JMenuItem saveMenuItem = new JMenuItem("Save");
 		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
 		saveMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processSave();
 			}
 		});
 
 		file.add(saveMenuItem);
 
 		JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
 		saveAsMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processSaveAs();
 			}
 		});
 
 		file.add(saveAsMenuItem);
 
 		file.addSeparator();
 		JMenuItem exitMenuItem = new JMenuItem("Exit");
 		exitMenuItem.setMnemonic(KeyEvent.VK_E);
 		exitMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processExitProgram();
 			}
 		});
 
 		file.add(exitMenuItem);
 		menubar.add(file);
 
 		JMenu edit = new JMenu("Edit");
 
 		JMenuItem processClearInfoItem = new JMenuItem("Clear Info");
 		processClearInfoItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processClearInfo();
 			}
 		});
 
 		edit.add(processClearInfoItem);
 
 		JMenuItem processClearOutputItem = new JMenuItem("Clear Output");
 		processClearOutputItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processClearOutput();
 			}
 		});
 
 		edit.add(processClearOutputItem);
 		menubar.add(edit);
 
 		JMenu view = new JMenu("View");
 
 		JMenuItem showStatusBarItem = new JMenuItem("Show Status Bar");
 		processClearOutputItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processShowStatusBar();
 			}
 		});
 
 		view.add(showStatusBarItem);
 		menubar.add(view);
 
 		JMenu run = new JMenu("Run");
 
 		JMenuItem runMatchingItem = new JMenuItem("Run");
 		runMatchingItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processMatchImages();
 			}
 		});
 
 		run.add(runMatchingItem);
 		menubar.add(run);
 
 		JMenu help = new JMenu("Help");
 
 		JMenuItem helpPagesMenuItem = new JMenuItem("Help Pages");
 		helpPagesMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processShowHelpPages();
 			}
 		});
 
 		help.add(helpPagesMenuItem);
 		menubar.add(help);
 		
 		help.addSeparator();
 		JMenuItem aboutMenuItem = new JMenuItem("About");
 		aboutMenuItem.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{
 				processShowAboutDialog();
 			}
 		});
 
 		help.add(aboutMenuItem);
 		menubar.add(help);
 
 		setJMenuBar(menubar);
 	}
 
 	private void addComponentsToNorth()
 	{
 		north = new JPanel();
 		north.setLayout(new FlowLayout((int) LEFT_ALIGNMENT));
 
 
 		runButton = new JButton(runIcon);
 		runButton.setBorder(BorderFactory.createEmptyBorder());
 		runButton.setContentAreaFilled(false);
 		runButton.setToolTipText("Run Stereo Matching");
 		runButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processMatchImages();
 			}
 		});
 		north.add(runButton);
 
 
 		openLeftImageButton = new JButton(openLeftIcon);
 		openLeftImageButton.setBorder(BorderFactory.createEmptyBorder());
 		openLeftImageButton.setContentAreaFilled(false);
 		openLeftImageButton.setToolTipText("Open Left Image");
 		openLeftImageButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processLeftImageSelect();
 			}
 		});
 		north.add(openLeftImageButton);
 
 
 		openRightImageButton = new JButton(openRightIcon);
 		openRightImageButton.setBorder(BorderFactory.createEmptyBorder());
 		openRightImageButton.setContentAreaFilled(false);
 		openRightImageButton.setToolTipText("Open Right Image");
 		openRightImageButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processRightImageSelect();
 			}
 		});
 		north.add(openRightImageButton);
 
 
 		clearButton = new JButton(clearIcon);
 		clearButton.setBorder(BorderFactory.createEmptyBorder());
 		clearButton.setContentAreaFilled(false);
 		clearButton.setToolTipText("Clear All Windows");
 		clearButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processClearOutput();
 				processClearInfo();
 			}
 		});
 		north.add(clearButton);
 
 
 		saveButton = new JButton(saveIcon);
 		saveButton.setBorder(BorderFactory.createEmptyBorder());
 		saveButton.setContentAreaFilled(false);
 		saveButton.setToolTipText("Save (Ctrl+S)");
 		saveButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processSaveAs();
 			}
 		});
 		north.add(saveButton);
 
 
 		saveAsButton = new JButton(saveAsIcon);
 		saveAsButton.setBorder(BorderFactory.createEmptyBorder());
 		saveAsButton.setContentAreaFilled(false);
 		saveAsButton.setToolTipText("Save As...");
 		saveAsButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processSaveAs();
 			}
 		});
 		north.add(saveAsButton);
 
 		exitButton = new JButton(exitIcon);
 		exitButton.setBorder(BorderFactory.createEmptyBorder());
 		exitButton.setContentAreaFilled(false);
 		exitButton.setToolTipText("Exit");
 		exitButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent event)
 			{				
 				processExitProgram();
 			}
 		});
 		north.add(exitButton);
 
 		this.add(north, BorderLayout.NORTH);
 	}
 
 	private void addComponentsToCenter()
 	{
 		center = new JPanel();
 		center.setLayout(new GridLayout(1, 2));
 
 		JPanel centerLeft = new JPanel();
 		centerLeft.setLayout(new BorderLayout());
 		center.add(centerLeft);
 
 		JPanel outputPanel = new JPanel();
 		outputPanel.setBorder(new EtchedBorder());
 		centerLeft.add(outputPanel, BorderLayout.NORTH);
 		JLabel outputLabel = new JLabel("Matching Program Output");
 		outputPanel.add(outputLabel);
 
 		outputTextArea = new JTextArea();
 		outputTextArea.setBackground(Color.BLACK);
 		outputTextArea.setForeground(Color.ORANGE);
 		outputTextArea.setEditable(false);
 		outputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 		outputTextArea.setWrapStyleWord(true);
 		outputTextArea.setLineWrap(true);
 
 		JScrollPane outputTextAreaScrollPane = new JScrollPane();
 		outputTextAreaScrollPane.setViewportView(outputTextArea);
 		centerLeft.add(outputTextAreaScrollPane, BorderLayout.CENTER);
 
 		JPanel centerRight = new JPanel();		
 		centerRight.setLayout(new GridLayout(2,1));
 		center.add(centerRight);
 
 		JPanel centerRightTop = new JPanel();
 		centerRightTop.setLayout(new BorderLayout());
 		centerRight.add(centerRightTop);
 
 		JPanel notesPanel = new JPanel();
 		notesPanel.setBorder(new EtchedBorder());
 		centerRightTop.add(notesPanel, BorderLayout.NORTH);
 		JLabel notesLabel = new JLabel("Notes");
 		notesPanel.add(notesLabel);
 
 		notesTextArea = new JTextArea();
 		notesTextArea.setBackground(Color.WHITE);
 		notesTextArea.setForeground(Color.BLACK);
 		notesTextArea.setEditable(true);
 		notesTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 		notesTextArea.setWrapStyleWord(true);
 		notesTextArea.setLineWrap(true);
 
 		JScrollPane notesTextAreaScrollPane = new JScrollPane();
 		notesTextAreaScrollPane.setViewportView(notesTextArea);
 		centerRightTop.add(notesTextAreaScrollPane, BorderLayout.CENTER);
 
 		JPanel centerRightBottom = new JPanel();
 		centerRightBottom.setLayout(new BorderLayout());
 		centerRight.add(centerRightBottom);		
 
 		JPanel infoPanel = new JPanel();
 		infoPanel.setBorder(new EtchedBorder());
 		centerRightBottom.add(infoPanel, BorderLayout.NORTH);
 		JLabel infoLabel = new JLabel("Program Information");
 		infoPanel.add(infoLabel);
 
 		infoTextArea = new JTextArea();
 		infoTextArea.setBackground(Color.BLACK);
 		infoTextArea.setForeground(Color.ORANGE);
 		infoTextArea.setEditable(false);
 		infoTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
 		infoTextArea.setWrapStyleWord(true);
 		infoTextArea.setLineWrap(true);
 
 		JScrollPane infoTextAreaScrollPane = new JScrollPane();
 		infoTextAreaScrollPane.setViewportView(infoTextArea);
 		centerRightBottom.add(infoTextAreaScrollPane, BorderLayout.CENTER);
 
 		this.add(center, BorderLayout.CENTER);
 	}
 
 	private void addComponentsToSouth()
 	{
 		south = new JPanel();
 		south.setLayout(new GridLayout(2, 2));
 		south.setBorder(new TitledBorder(new EtchedBorder()));
 
 		leftImageFileNameLabel = new JLabel("Left image file selected: ----");
 		leftImageFileNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
 		south.add(leftImageFileNameLabel);
 
 		timeForLastMatchLabel = new JLabel("Time taken for last match: ----");
 		timeForLastMatchLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
 		south.add(timeForLastMatchLabel);
 
 		rightImageFileNameLabel = new JLabel("Right image file selected: ----");
 		rightImageFileNameLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
 		south.add(rightImageFileNameLabel);
 
 		averageMatchTimeLabel = new JLabel("Average match time: ----");
 		averageMatchTimeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
 		south.add(averageMatchTimeLabel);
 
 		this.add(south, BorderLayout.SOUTH);
 	}
 
 	private void showWelcomeScreen()
 	{
 		String aboutDialog = "Welcome to the Stereo Image Matcher.\n\n" +
 				"Stereo Image Matcher provides an interface" +
 				" for matching a pair of stereo images." +
 				"\nImages must be in .bmp format and can be selected via the" +
 				"\n'File' menu or the left and right arrow icons in the button pane." +
 				"\n\nFor further information, please consult the 'Help' menu.";
 
 		JOptionPane.showMessageDialog(this, aboutDialog, "Welcome", JOptionPane.INFORMATION_MESSAGE,
 				infoIcon);
 	}
 
 	private void processMatchImages()
 	{
 		try
 		{
 			++totalMatches;
 
 			if (leftImageFileName.equals(null) || rightImageFileName.equals(null))
 				throw new NullPointerException();
 
 			controller = new StereoMatchingController(
					leftImageFilePath,
					rightImageFilePath);
 
 			matchStartTime = System.currentTimeMillis();
 			controller.runVectorPascalCode();
 			matchEndTime = System.currentTimeMillis();
 
 			matchTotalTime = matchEndTime - matchStartTime;
 			totalMatchTime += matchTotalTime;
 			averageMatchTime = (totalMatchTime * 1.0) / totalMatches;
 
 			averageMatchTimeLabel.setText(String.format("%s %.2fms", "Average match time: ", averageMatchTime));
 			timeForLastMatchLabel.setText(String.format("%s %dms", "Time taken for last match: ",
 					matchTotalTime));
 
 			infoTextArea.append("Match number: " + totalMatches + "\n\n");
 
 			infoTextArea.append("Matching the following images: " +
 					"\n\tLeft image: " + leftImageFileName +
 					"\n\tRight image: " + rightImageFileName);
 			infoTextArea.append("\n\nMatching started: " + getCurrentDate() + "\n");
 			infoTextArea.append("Time to complete match: " + matchTotalTime + "ms\n");
 
 			String outputDelimiter = "_________________________";
 			infoTextArea.append(outputDelimiter + outputDelimiter + "\n\n");
 
 			outputTextArea.append("Match number: " + totalMatches + "\n\n");
 
 			for (String errorLine : controller.getErrorLines())
 				outputTextArea.append(errorLine + "\n");
 
 			for (String outputLine : controller.getOutputLines())
 				outputTextArea.append(outputLine + "\n");
 
 			outputTextArea.append(outputDelimiter + outputDelimiter + "\n\n");
 		}
 		catch (NullPointerException npx)
 		{			
 			JOptionPane.showMessageDialog(this, "You must specify two images to be matched",
 					"Error", JOptionPane.ERROR_MESSAGE,
 					errorIcon);
 		}
 	}
 
 	private void processLeftImageSelect()
 	{
 		leftImageChooser = new JFileChooser();
 		leftImageChooser.setCurrentDirectory(new File("./"));
 		leftImageChooser.setFileFilter(fileNameFilter);
 		leftImageChooser.setDialogTitle("Select Left Stereo Image");
 		int returnValLeft = leftImageChooser.showOpenDialog(this);
 
 		if (returnValLeft == JFileChooser.APPROVE_OPTION)
 		{
 			leftImageFilePath = leftImageChooser.getSelectedFile().getAbsolutePath();
 			leftImageFileName = leftImageChooser.getSelectedFile().getName();
 			leftImageFileNameLabel.setText("Left image file selected: " + leftImageFileName);
 		}
 	}
 
 	private void processRightImageSelect()
 	{
 		rightImageChooser = new JFileChooser();
 		rightImageChooser.setCurrentDirectory(new File("./"));
 		rightImageChooser.setFileFilter(fileNameFilter);
 		rightImageChooser.setDialogTitle("Select Right Stereo Image");
 		int returnValRight = rightImageChooser.showOpenDialog(this);
 
 		if (returnValRight == JFileChooser.APPROVE_OPTION)
 		{
 			rightImageFilePath = rightImageChooser.getSelectedFile().getAbsolutePath();
 			rightImageFileName = rightImageChooser.getSelectedFile().getName();
 			rightImageFileNameLabel.setText("Right image file selected: " + rightImageFileName);
 		}
 	}
 
 	private void processSave()
 	{
 		FileWriter outputFileWriter = null;
 
 		try
 		{
 			try
 			{
 				if (outputFileName == null)
 				{
 					processSaveAs();
 				}
 				else
 				{
 					if (outputTextArea.getText().equals("") &&
 							infoTextArea.getText().equals("") &&
 							notesTextArea.getText().equals(""))
 						throw new IllegalArgumentException();
 
 					outputFileWriter = new FileWriter(outputFileName);
 
 					outputFileWriter.write(createReport());
 				}
 			}
 			finally
 			{
 				if (outputFileWriter != null) outputFileWriter.close();
 			}
 		}
 		catch (IllegalArgumentException iax)
 		{
 			JOptionPane.showMessageDialog(this, "There is nothing to save",
 					"No Output", JOptionPane.INFORMATION_MESSAGE,
 					attentionIcon);
 		}
 		catch (IOException iox)
 		{
 			JOptionPane.showMessageDialog(this, "File could not be written to",
 					"Error", JOptionPane.ERROR_MESSAGE,
 					errorIcon);
 		}
 	}
 
 	private void processSaveAs()
 	{
 		outputFileName = null;
 		FileWriter outputFileWriter = null;
 
 		try
 		{
 			try
 			{
 				if (outputTextArea.getText().equals("") &&
 						infoTextArea.getText().equals("") &&
 						notesTextArea.getText().equals(""))
 					throw new IllegalArgumentException();
 
 				JFileChooser fileSaveChooser = new JFileChooser();
 				fileSaveChooser.setCurrentDirectory(new File("./"));
 				int returnValFileSave = fileSaveChooser.showSaveDialog(this);
 
 				if (returnValFileSave == JFileChooser.APPROVE_OPTION)
 				{
 					outputFileName = fileSaveChooser.getSelectedFile().getName();
 					File saveFile = fileSaveChooser.getSelectedFile();
 					outputFileWriter = new FileWriter(saveFile);
 					outputFileWriter.write(createReport());
 				}
 			}
 			finally
 			{
 				if (outputFileWriter != null) outputFileWriter.close();
 			}
 		}
 		catch (IllegalArgumentException iax)
 		{
 			JOptionPane.showMessageDialog(this, "There is nothing to save",
 					"No Output", JOptionPane.INFORMATION_MESSAGE,
 					attentionIcon);
 		}
 		catch (IOException iox)
 		{
 			JOptionPane.showMessageDialog(this, "File could not be written to",
 					"Error", JOptionPane.ERROR_MESSAGE,
 					errorIcon);
 		}
 		catch (NullPointerException npx)
 		{
 			JOptionPane.showMessageDialog(this, "You must enter an output file name",
 					"Error", JOptionPane.ERROR_MESSAGE,
 					errorIcon);
 		}
 	}
 
 	private String createReport()
 	{
 		String report;
 
 		String reportHeadingTop = "/**********************************";
 		String reportHeadingMiddle = "\n\n\tStereo Matcher Report\n\n";
 		String reportHeadingCreationTime = "\tCreated: " + getCurrentDate() + "\n\n";
 		String reportHeadingBottom = "***********************************/";
 
 		String reportNotes = notesTextArea.getText();
 		String reportProgramInfo = infoTextArea.getText();
 		String reportProgramOutput = outputTextArea.getText();
 
 		report = reportHeadingTop + reportHeadingMiddle +
 				reportHeadingCreationTime + reportHeadingBottom +
 				"\n\n" + "Notes\n" + "----------------\n\n" +
 				reportNotes +
 				"\n\n\n" + "Program Information\n" + "----------------\n\n" +
 				reportProgramInfo +
 				"\n\n\n" + "Program Output\n" + "----------------\n\n" +
 				reportProgramOutput;
 		return report;
 	}
 
 	private void processShowAboutDialog()
 	{
 		String aboutDialog = "Stereo Image Matcher\n" +
 				"\nVersion: 0.1" +
 				"\nAuthor: Adam Murray" + 
 				"\nCopyright (c) 2013 Adam Murray. All rights reserved." +
 				"\n\nThis program is a user interface for use in matching a pair of stereo images.";
 		JOptionPane.showMessageDialog(this, aboutDialog, "About", JOptionPane.INFORMATION_MESSAGE,
 				infoIcon);
 	}
 
 	private void processShowStatusBar()
 	{
 		//TODO complete processShowStatusBar
 	}
 
 	private void processShowHelpPages()
 	{
 		//TODO complete help pages
 	}
 	
 	private void processExitProgram()
 	{
 		if (JOptionPane.showConfirmDialog(this,
 				"Are you sure you want to exit?",
 				"Confirm Exit",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.INFORMATION_MESSAGE,
 				attentionIcon) == JOptionPane.YES_OPTION)
 		{
 			System.exit(0);
 		}
 	}
 
 	private void processClearOutput()
 	{
 		if (JOptionPane.showConfirmDialog(this,
 				"Are you sure you want to clear the output?",
 				"Confirm Clear Output",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.INFORMATION_MESSAGE,
 				attentionIcon) == JOptionPane.YES_OPTION)
 		{
 			outputTextArea.setText("");
 		}
 	}
 
 	private void processClearInfo()
 	{
 		if (JOptionPane.showConfirmDialog(this,
 				"Are you sure you want to clear the information output?",
 				"Confirm Clear Info",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.INFORMATION_MESSAGE,
 				attentionIcon) == JOptionPane.YES_OPTION)
 		{
 			infoTextArea.setText("");
 		}
 	}
 
 	private String getCurrentDate()
 	{
 		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 		Date date = new Date();
 		String dateString = dateFormat.format(date).toString();
 		return dateString;
 	}
 
 	/**
 	 * Main method.
 	 * Causes the EDT to be invoked.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				StereoMatchingGUI gui = new StereoMatchingGUI();
 				gui.setVisible(true);
 			}
 		});
 	}
 }
