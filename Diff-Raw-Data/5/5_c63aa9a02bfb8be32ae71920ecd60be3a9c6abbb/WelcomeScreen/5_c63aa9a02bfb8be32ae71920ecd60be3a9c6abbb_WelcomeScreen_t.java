 package gui;
 
 import java.awt.BorderLayout;
import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextPane;
 
 import gui.WelcomeScreenLine.Type;
 import machine.Machine.MachineType;
 
 /** This class represents the Welcome Screen.
  * @author David Wille
  */
 public class WelcomeScreen extends JPanel {
 	
 	private static final long serialVersionUID = 882824632406556671L;
 	private JTabbedPane tabbedPane;
 	private JScrollPane turingPane;
 	private JScrollPane brainfuckPane;
 	private JPanel turingContainer;
 	private JPanel brainfuckContainer;
 	private JPanel header;
 	private JPanel headerRight;
 	private JLabel logo;
 	private JLabel headerTitle;
 	private JTextPane headerText;
 	private JPanel turingOpen;
 	private JPanel turingCreate;
 	private JPanel turingExamples;
 	private JPanel brainfuckOpen;
 	private JPanel brainfuckCreate;
 	private JPanel brainfuckExamples;
 	private Editor editor;
 	
 	/**
 	 * Constructs the welcome screen.
 	 * @param editor The current editor window
 	 */
 	public WelcomeScreen(Editor editor) {
 		this.editor = editor;
 		initWelcomeScreen();
 		this.setLayout(new BorderLayout());
 		
 		// header
 		header.setLayout(new BorderLayout());
 
 		header.add(logo, BorderLayout.LINE_START);
 		this.logo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 		header.add(headerRight, BorderLayout.CENTER);
 		
 		this.headerTitle.setFont(this.headerTitle.getFont().deriveFont(24f));
 		this.headerText.setEditable(false);
 		this.headerText.setBackground(this.getBackground());
		this.headerText.setOpaque(false);
		this.headerText.setBackground(new Color(0,0,0,0));
 		this.headerText.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
 
 		
 		this.headerRight.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
 		headerRight.setLayout(new BorderLayout());
 		headerRight.add(this.headerTitle, BorderLayout.NORTH);
 		headerRight.add(this.headerText, BorderLayout.CENTER);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		// turingPane
 		turingOpen = new WelcomeScreenGroup(this.editor, "Open", Type.OPEN, MachineType.TuringMachine);
 		turingCreate = new WelcomeScreenGroup(this.editor, "Create", Type.CREATE, MachineType.TuringMachine);
 		turingExamples = new WelcomeScreenGroup(this.editor, "Examples", Type.FILE, MachineType.TuringMachine);
 		turingContainer.setLayout(new GridBagLayout());
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		turingContainer.add(turingOpen, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		turingContainer.add(turingCreate, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		turingContainer.add(turingExamples, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.weightx = 1.0;
 		c.weighty = 1.0;
 		c.insets = new Insets(0,0,0,0);
 		turingContainer.add(Box.createHorizontalGlue(), c);
 		
 		// brainfuckPane
 		brainfuckOpen = new WelcomeScreenGroup(this.editor, "Open", Type.OPEN, MachineType.BrainfuckMachine);
 		brainfuckCreate = new WelcomeScreenGroup(this.editor, "Create", Type.CREATE, MachineType.BrainfuckMachine);
 		brainfuckExamples = new WelcomeScreenGroup(this.editor, "Examples", Type.FILE, MachineType.BrainfuckMachine);
 		brainfuckContainer.setLayout(new GridBagLayout());
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 0;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		brainfuckContainer.add(brainfuckOpen, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		brainfuckContainer.add(brainfuckCreate, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 1.0;
 		c.insets = new Insets(5,5,5,5);
 		brainfuckContainer.add(brainfuckExamples, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.weightx = 1.0;
 		c.weighty = 1.0;
 		c.insets = new Insets(0,0,0,0);
 		brainfuckContainer.add(Box.createHorizontalGlue(), c);
 		
 		turingPane = new JScrollPane(turingContainer);
 		turingPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		turingPane.getVerticalScrollBar().setUnitIncrement(14);
 		
 		brainfuckPane = new JScrollPane(brainfuckContainer);
 		brainfuckPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		brainfuckPane.getVerticalScrollBar().setUnitIncrement(14);
 
 
 		// tabs
 		tabbedPane.addTab("Turing Machine", turingPane);
 		tabbedPane.addTab("Brainfuck", brainfuckPane);
 		
 		this.add(header, BorderLayout.PAGE_START);
 		this.add(tabbedPane, BorderLayout.CENTER);
 	}
 	
 	private void initWelcomeScreen() {
 		tabbedPane = new JTabbedPane();
 		turingContainer = new JPanel();
 		brainfuckContainer = new JPanel();
 		logo = new JLabel("", new ImageIcon(this.getClass().getResource("images" + File.separator + "logo.png")), JLabel.CENTER);
 		header = new JPanel();
 		headerRight = new JPanel();
 		headerTitle = new JLabel("Welcome to " + AppData.APP_NAME + "!");
 		headerText = new JTextPane();
 		headerText.setText(AppData.APP_NAME + " lets you create and simulate your own Turing machines and Brainfuck programs. "
 				+ "To do so it provides an easy to use graphical Turing machine editor and a simple code editor for your Brainfuck programs. "
 				+ "To get started, you might want to have a look on the example machines below.");
 	}
 }
