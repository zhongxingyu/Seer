 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.event.TreeModelEvent;
 import javax.swing.event.TreeModelListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.DefaultEditorKit;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Document;
 import javax.swing.text.Highlighter;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.StyleContext;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.UndoManager;
 
 import say.swing.JFontChooser;
 
 import com.sun.speech.*;
 import com.sun.speech.freetts.Voice;
 import com.sun.speech.freetts.VoiceManager;
 
 /**
  * 
  * @author ericzhu
  * 
  */
 @SuppressWarnings("unused")
 public class TXE1 extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	public static JTextArea TXEAREA = new JTextArea();
 
 	public JEditorPane saveField = new JEditorPane();
 
 	private JFileChooser dialog = new JFileChooser(
 			System.getProperty("home.dir"));
 
 	public static String currentFile = "Untitled Document";
 
 	private boolean changed = false;
 
 	public static Color color = (Color.WHITE);
 
 	public String changeLog = ("TXE 1.6.5 change log 1. New color buttons in the easy access bar  2.New about option 3.Colors are added  4.Menu Seperators are added  5.The scroll bars  auto hide  6. Added the Settings Tab 7. Minor bug fixes ");
 
 	public String DefualtText = ("Welcome To TXE. The  new innovative Text Editor. Type what ever you want. Updates coming soon! \r\n\r\n*Note* this is TXE 1.8!\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\nCredits: Eric Zhu of Great Ark Studios and Turk4n of CodeCall.net Icons from http://www.visualpharm.com/");
 
 	public String currentText = TXEAREA.getText();
 
 	public String saveText = saveField.getText();
 
 	public String caseText;
 
 	public int fsizeString;
 
 	public String text;
 
 	public JFrame panel;
 
 	public JTextField findText = new JTextField();
 
 	public JScrollPane scroll = new JScrollPane(TXEAREA,
 			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 	FileSystemModel fsm = new FileSystemModel();
 
 	JTree tree = new JTree(fsm);
 
 	JScrollPane scrollTree = new JScrollPane(tree);
 	public Color CoL = Color.YELLOW;
 	public Color Colors = CoL;
 	Highlighter.HighlightPainter HighLight = new highLight(Colors);
 
 	// DO NOT CHANGE
 	JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 			new JScrollPane(tree), scroll);
 
 	UndoManager undoManager = null;
 
 	DefaultStyledDocument document;
 
 	StyleContext styleContext;
 
 	JMenuItem undo = new JMenuItem("Undo");
 
 	JMenuItem redo = new JMenuItem("Redo");
 
 	JMenuItem undoP = new JMenuItem("Undo");
 
 	JMenuItem redoP = new JMenuItem("Redo");
 
 	JColorChooser CC = new JColorChooser();
 
 	public String colS;
 
 	public TXE1() {
 
 		undoManager = new UndoManager();
 
 		styleContext = new StyleContext();
 
 		document = new DefaultStyledDocument(styleContext);
 
 		TXEAREA.setDocument(document);
 		TXEAREA.requestFocus(true);
 
 		this.setSize(1000, 1000);
 		TXEAREA.setText(DefualtText);
 		this.setLocationRelativeTo(null);
 		TXEAREA.setFont(new Font("Times New Roman", Font.PLAIN, 12));
 
 		findText.setToolTipText("Type word or phrase to be found");
 		findText.setSize(50, 200);
 		findText.setText("Type word or phrase to be found here.");
 
 		this.getContentPane().add(splitpane);
 
 		ImageIcon ImgIc = new ImageIcon(getClass().getResource(
 				"images/normal.gif"));
 		this.setIconImage(ImgIc.getImage());
 
 		splitpane.setResizeWeight(0.5);
 		splitpane.setOneTouchExpandable(true);
 		Dimension minimumSize = new Dimension(0, 0);
 		TXEAREA.setMinimumSize(minimumSize);
 		tree.setMinimumSize(minimumSize);
 		tree.setVisible(true);
 		tree.setSize(100, 1000);
 
 		JMenuItem About = new JMenuItem("About");
 		JMenu format = new JMenu("Format");
 		JMenu TXESettings = new JMenu("Tools");
 		TXESettings.setToolTipText("Extra apps and settings.");
 		JMenuItem settingsFrame = new JMenuItem("Settings");
 		JMenuItem addwebsite = new JMenuItem("Add Website Signature");
 		JMenuItem addname = new JMenuItem("Add Name Signature");
 		JMenuItem addcompany = new JMenuItem("Add Company Name");
 		JMenuItem bold = new JMenuItem("Bold Document");
 		JMenuItem italics = new JMenuItem("Italicize Document");
 		italics.setToolTipText("Italicize, using Times New Roman 12 pt font.");
 		JMenuItem plain = new JMenuItem("Normal Style");
 		plain.setToolTipText("Normal style and Times New Roman 12 pt font.");
 		JMenuItem bI = new JMenuItem("Bold and Italicize Document");
 		bI.setToolTipText("Bold and Italicize, using Times New Roman 12 pt font.");
 		JMenuItem Fr = new JMenuItem("Font");
 		Fr.setToolTipText("Choose a font");
 		JMenuItem bL = new JMenuItem("Blue");
 		bL.setToolTipText("Make text blue");
 		JMenuItem rD = new JMenuItem("Red");
 		rD.setToolTipText("Make text red");
 		JMenuItem gR = new JMenuItem("Green");
 		gR.setToolTipText("Make text green");
 		JMenuItem bLa = new JMenuItem("Normal");
 		bLa.setToolTipText("Make text black");
 		JMenuItem rsA = new JMenuItem("Text Align Right");
 		rsA.setToolTipText("Align text right");
 		JMenuItem lsA = new JMenuItem("Text Align Left");
 		lsA.setToolTipText("Align text left");
 		JMenuItem csA = new JMenuItem("Text Align Center");
 		csA.setToolTipText("Align text middle");
 		JMenuItem cL = new JMenuItem("Change Log");
 		JMenuItem caL = new JMenuItem("Calculator");
 		caL.setToolTipText("Do some math");
 		JMenuItem enC = new JMenuItem("Encryption");
 		enC.setToolTipText("Encrypt a message");
 		JMenuItem coL = new JMenuItem("Color Chooser");
 		coL.setToolTipText("Choose a color");
 		JMenuItem coLP = new JMenuItem("Color Chooser");
 		coLP.setToolTipText("Choose a color");
 		JMenuItem HcoL = new JMenuItem("Color Chooser");
 		HcoL.setToolTipText("Choose a highlighter color");
 		JMenuItem srenSht = new JMenuItem("Screenshot");
 		srenSht.setToolTipText("Take Screenshot");
 		JMenuItem nimbus = new JMenuItem("Nimbus Style");
 		JMenuItem metal = new JMenuItem("Metal Style");
 		JMenuItem natives = new JMenuItem("Native Style");
 		JMenuItem print = new JMenuItem("Print");
 		print.setToolTipText("Print current document");
 		JMenuItem printP = new JMenuItem("Print");
 		printP.setToolTipText("Print current document");
 		JMenuItem date = new JMenuItem("Insert Date and Time");
 		JMenuItem dateP = new JMenuItem("Insert Date and Time");
 		JMenuItem sA = new JMenuItem("Select All");
 		sA.setToolTipText("Select All Text In Document");
 		JMenuItem sAP = new JMenuItem("Select All");
 		sAP.setToolTipText("Select All Text In Document");
 		JMenuItem pT = new JMenuItem("Programmer's Text Pad");
 		pT.setToolTipText("Programmer's Text Pad");
 		JMenuItem sL = new JMenuItem("Text Size Larger");
 		sL.setToolTipText("Text Size Larger");
 		sL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,
 				InputEvent.CTRL_DOWN_MASK));
 		JMenuItem sS = new JMenuItem("Text Size Smaller");
 		sS.setToolTipText("Text Size Smaller");
 		sS.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
 				InputEvent.CTRL_DOWN_MASK));
 		JMenuItem sLP = new JMenuItem("Text Size Larger");
 		sLP.setToolTipText("Text Size Larger");
 		sLP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,
 				InputEvent.CTRL_DOWN_MASK));
 		JMenuItem sSP = new JMenuItem("Text Size Smaller");
 		sSP.setToolTipText("Text Size Smaller");
 		sSP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
 				InputEvent.CTRL_DOWN_MASK));
 		JMenuItem cP = new JMenuItem("Captilize Text");
 		cP.setToolTipText("Captilize Text");
 		JMenuItem dC = new JMenuItem("Decaptilize Text");
 		dC.setToolTipText("Decaptilize Text");
 		JMenuItem cPP = new JMenuItem("Captilize Text");
 		cPP.setToolTipText("Captilize Text");
 		JMenuItem dCP = new JMenuItem("Decaptilize Text");
 		dCP.setToolTipText("Decaptilize Text");
 		JMenuItem nor = new JMenuItem("Original Text Style");
 		nor.setToolTipText("Original Text Style");
 		JMenuItem dS = new JMenuItem("Document Stats");
 		dS.setToolTipText("Document Stats");
 		JMenuItem pe = new JMenuItem("No Editing");
 		JMenuItem ae = new JMenuItem("Allow Editing");
 		JMenuItem peP = new JMenuItem("No Editing");
 		JMenuItem aeP = new JMenuItem("Allow Editing");
 		JMenuItem boldP = new JMenuItem("Bold Document");
 		JMenuItem italicsP = new JMenuItem("Italicize Document");
 		italicsP.setToolTipText("Italicize, using Times New Roman 12 pt font.");
 		JMenuItem plainP = new JMenuItem("Normal Style");
 		plain.setToolTipText("Normal style and Times New Roman 12 pt font.");
 		JMenuItem bIP = new JMenuItem("Bold and Italicize Document");
 		bIP.setToolTipText("Bold and Italicize, using Times New Roman 12 pt font.");
 		JMenuItem FrP = new JMenuItem("Font");
 		FrP.setToolTipText("Choose a font");
 		JMenuItem bLP = new JMenuItem("Blue");
 		bLP.setToolTipText("Make text blue");
 		JMenuItem rDP = new JMenuItem("Red");
 		rDP.setToolTipText("Make text red");
 		JMenuItem gRP = new JMenuItem("Green");
 		gRP.setToolTipText("Make text green");
 		JMenuItem bLaP = new JMenuItem("Normal");
 		bLaP.setToolTipText("Make text black");
 		JMenuItem rsAP = new JMenuItem("Text Align Right");
 		rsAP.setToolTipText("Align text right");
 		JMenuItem lsAP = new JMenuItem("Text Align Left");
 		lsAP.setToolTipText("Align text left");
 		JMenuItem csAP = new JMenuItem("Text Align Center");
 		csAP.setToolTipText("Align text middle");
 		JMenuItem tts = new JMenuItem("Text To Speech");
 		tts.setToolTipText("Text To Speech");
 
 		JButton findButton = new JButton("Find");
 
 		document.addUndoableEditListener(new UndoableEditListener() {
 			@Override
 			public void undoableEditHappened(UndoableEditEvent e) {
 				undoManager.addEdit(e.getEdit());
 				updateUndoRedoMenu();
 			}
 
 		});
 
 		undo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.undo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Undo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		undo.setIcon(new ImageIcon(getClass().getResource(
 				"images/Undo_16x16.png")));
 		undo.setAccelerator(KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_Z,
 				java.awt.event.InputEvent.CTRL_MASK));
 
 		redo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.redo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Redo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		redo.setIcon(new ImageIcon(getClass().getResource(
 				"images/Redo_16x16.png")));
 		redo.setAccelerator(KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_X,
 				java.awt.event.InputEvent.CTRL_MASK));
 		undoP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.undo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Undo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		undoP.setIcon(new ImageIcon(getClass().getResource(
 				"images/Undo_16x16.png")));
 		undoP.setAccelerator(KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_Z,
 				java.awt.event.InputEvent.CTRL_MASK));
 
 		redoP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					undoManager.redo();
 				} catch (CannotRedoException ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Exception: " + ex.getLocalizedMessage(),
 							"Redo Exception", JOptionPane.ERROR_MESSAGE);
 				}
 				updateUndoRedoMenu();
 			}
 		});
 		redoP.setIcon(new ImageIcon(getClass().getResource(
 				"images/Redo_16x16.png")));
 		redoP.setAccelerator(KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_X,
 				java.awt.event.InputEvent.CTRL_MASK));
 		pT.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEProgrammer TP = new TXEProgrammer();
 
 			}
 		});
 
 		HcoL.addActionListener(new ActionListener() {
 			@Override
 			@SuppressWarnings("static-access")
 			public void actionPerformed(ActionEvent e) {
 				JColorChooser.showDialog(null, "Pick Highlighter Color", CoL);
 				Colors = CoL;
 				System.out.println(CoL);
 				System.out.println(Colors);
 			}
 		});
 
 		findButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				try {
 
 					if (findText.getText().length() == 0) {
 						JOptionPane.showMessageDialog(rootPane,
 								"Please type something to be found", "WARNING",
 								JOptionPane.INFORMATION_MESSAGE);
 					}
 					highlight(TXEAREA, findText.getText());
 				} catch (Exception ex) {
 					JOptionPane.showMessageDialog(rootPane,
 							"Please type something to be found", "WARNING",
 							JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		});
 
 		sA.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.selectAll();
 
 			}
 		});
 		sAP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.selectAll();
 
 			}
 		});
 		date.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Date date = new Date();
 				SimpleDateFormat sdt = new SimpleDateFormat(
 						"E MM.dd.yyyy 'at' hh:mm:ss a zzz");
 				TXEAREA.insert(sdt.format(date), TXEAREA.getCaretPosition());
 
 			}
 		});
 		dateP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Date date = new Date();
 				SimpleDateFormat sdt = new SimpleDateFormat(
 						"E MM.dd.yyyy 'at' hh:mm:ss a zzz");
 				TXEAREA.insert(sdt.format(date), TXEAREA.getCaretPosition());
 
 			}
 		});
 
 		print.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					boolean print = TXEAREA.print();
 					if (print) {
 						JOptionPane
 								.showMessageDialog(null, "Printing is Done!");
 					} else {
 
 					}
 
 				} catch (Exception exc) {
 
 				}
 			}
 
 		});
 		printP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					boolean print = TXEAREA.print();
 					if (print) {
 						JOptionPane
 								.showMessageDialog(null, "Printing is Done!");
 					} else {
 
 					}
 
 				} catch (Exception exc) {
 
 				}
 			}
 
 		});
 
 		pe.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				TXEAREA.setEditable(false);
 			}
 
 		});
 		ae.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				TXEAREA.setEditable(true);
 			}
 
 		});
 		peP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				TXEAREA.setEditable(false);
 			}
 
 		});
 		aeP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				TXEAREA.setEditable(true);
 			}
 
 		});
 
 		natives.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nativeActionPerformed(e);
 			}
 
 		});
 
 		nimbus.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				nimbusActionPerformed(e);
 			}
 		});
 		srenSht.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				/**
 				 * TXESHOT txesh = new TXESHOT(); txesh.setVisible(true);
 				 **/
 				Date date = new Date();
 				SimpleDateFormat sdt = new SimpleDateFormat(
 						"E MM.dd.yyyy 'at' hh:mm:ss a zzz");
 				try {
 
 					Rectangle screenshotRect = new Rectangle(Toolkit
 							.getDefaultToolkit().getScreenSize());
 					BufferedImage Capture = new Robot()
 							.createScreenCapture(screenshotRect);
 					// JFrame frame = new JFrame();
 					// frame.setVisible(true);
 					// frame.setResizable(false);
 					// frame.setSize(500,50);
 					// frame.add(saveField);
 					// saveField.setText("Screenshot name.png");
 					ImageIO.write(Capture, "png", new File("TXE Screenshot "
 							+ sdt.format(date) + ".png"));
 					JOptionPane
 							.showMessageDialog(
 									getParent(),
 									"Your file has been saved, please change the file name to prevent overwriting. It was saved under the name TXE Screenshot.png.");
 				} catch (Exception ex) {
 
 					JOptionPane.showMessageDialog(getParent(),
 							"Error processing image...");
 
 				}
 			}
 
 		});
 		coL.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				color = JColorChooser
 						.showDialog(null, "Pick Text Color", color);
 				TXEAREA.setForeground(color);
 
 			}
 
 		});
 		coLP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				color = JColorChooser
 						.showDialog(null, "Pick Text Color", color);
 				TXEAREA.setForeground(color);
 
 			}
 
 		});
 		enC.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Encrypt enc = new Encrypt();
 				enc.setVisible(true);
 			}
 		});
 		lsA.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 			}
 		});
 		rsA.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 
 			}
 		});
 		settingsFrame.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Settings txesettings = new Settings();
 				txesettings.setVisible(true);
 			}
 		});
 		addwebsite.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 
 				TXEAREA.setText(currentText + settingsWeb.webText1);
 			}
 		});
 		addname.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 				;
 				TXEAREA.setText(currentText + settingsWeb.nameText);
 			}
 		});
 		addwebsite.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Settings settingsWeb = new Settings();
 
 				TXEAREA.setText(currentText + settingsWeb.companyText1);
 			}
 		});
 		cL.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					JOptionPane.showOptionDialog(null, null,
 							"Would you like to save your document",
 							JOptionPane.YES_NO_OPTION,
 							JOptionPane.QUESTION_MESSAGE, null, null, null);
 					TXEAREA.setText(changeLog);
 				}
 			}
 
 		});
 		sL.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fsizeString = TXEAREA.getFont().getSize();
 				System.out.println(fsizeString);
 				fsizeString++;
 				text = TXEAREA.getText();
 				TXEAREA.setFont(new Font(TXEAREA.getFont().getFontName(),
 						TXEAREA.getFont().getStyle(), fsizeString));
 			}
 		});
 		sS.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fsizeString = TXEAREA.getFont().getSize();
 				System.out.println(fsizeString);
 				fsizeString--;
 				text = TXEAREA.getText();
 				TXEAREA.setFont(new Font(TXEAREA.getFont().getFontName(),
 						TXEAREA.getFont().getStyle(), fsizeString));
 			}
 		});
 		sLP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fsizeString = TXEAREA.getFont().getSize();
 				System.out.println(fsizeString);
 				fsizeString++;
 				text = TXEAREA.getText();
 				TXEAREA.setFont(new Font(TXEAREA.getFont().getFontName(),
 						TXEAREA.getFont().getStyle(), fsizeString));
 			}
 		});
 		sSP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				fsizeString = TXEAREA.getFont().getSize();
 				System.out.println(fsizeString);
 				fsizeString--;
 				text = TXEAREA.getText();
 				TXEAREA.setFont(new Font(TXEAREA.getFont().getFontName(),
 						TXEAREA.getFont().getStyle(), fsizeString));
 			}
 		});
 		cP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				currentText = TXEAREA.getText().toString();
 				caseText = TXEAREA.getText().toUpperCase().toString();
 				TXEAREA.setText(caseText);
 			}
 
 		});
 		dC.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				currentText = TXEAREA.getText().toString();
 				caseText = TXEAREA.getText().toLowerCase().toString();
 				TXEAREA.setText(caseText);
 			}
 
 		});
 		cPP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				currentText = TXEAREA.getText().toString();
 				caseText = TXEAREA.getText().toUpperCase().toString();
 				TXEAREA.setText(caseText);
 			}
 
 		});
 		dCP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				currentText = TXEAREA.getText().toString();
 				caseText = TXEAREA.getText().toLowerCase().toString();
 				TXEAREA.setText(caseText);
 			}
 
 		});
 		nor.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				TXEAREA.setText(currentText);
 			}
 
 		});
 		dS.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new InfoFrame().setVisible(true);
 			}
 
 		});
 		final String vName = "kevin16";
 		tts.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Voice voice;
 				VoiceManager vm = VoiceManager.getInstance();
 				voice = vm.getVoice(vName);
 				voice.allocate();
 				try {
 					voice.speak(TXEAREA.getText());
 				} catch (Exception ex) {
 
 				}
 
 			}
 
 		});
 		// add stuff to format
 		format.add(plain);
 		format.add(bold);
 		format.add(italics);
 		format.add(bI);
 		format.addSeparator();
 		format.add(Fr);
 		format.addSeparator();
 		format.add(coL);
 		format.add(bLa);
 		format.add(rD);
 		format.add(gR);
 		format.add(bL);
 		format.addSeparator();
 		format.add(rsA);
 		format.add(lsA);
 		format.addSeparator();
 		format.add(pe);
 		format.add(ae);
 
 		italics.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.ITALIC, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		italicsP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.ITALIC, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		plain.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.PLAIN, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		plainP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.PLAIN, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		bold.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.BOLD, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		boldP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.BOLD, TXEAREA
 							.getFont().getSize()));
 				}
 			}
 
 		});
 		bI.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.BOLD + Font.ITALIC,
 							TXEAREA.getFont().getSize()));
 				}
 			}
 
 		});
 		bIP.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				{
 					String txtFont = TXEAREA.getFont().getFontName();
 					TXEAREA.setFont(new Font(txtFont, Font.BOLD + Font.ITALIC,
 							TXEAREA.getFont().getSize()));
 				}
 			}
 
 		});
 		Fr.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JFontChooser fontChooser = new JFontChooser();
 				fontChooser.setSelectedFont(TXEAREA.getFont());
 				int option = fontChooser.showDialog(TXEAREA);
 				if (option == JFontChooser.OK_OPTION) {
					//fontChooser.setSelectedFont(TXEAREA.getFont());
 					Font font = fontChooser.getSelectedFont();
 					TXEAREA.setFont(font);
 					System.out.println("Selected Font : " + font);
 				}
 
 			}
 		});
 		FrP.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JFontChooser fontChooser = new JFontChooser();	
 				fontChooser.setSelectedFont(TXEAREA.getFont());
 				int option = fontChooser.showDialog(TXEAREA);
 				if (option == JFontChooser.OK_OPTION) {
					//fontChooser.setSelectedFont(TXEAREA.getFont());
 					Font font = fontChooser.getSelectedFont();
 					TXEAREA.setFont(font);
 					System.out.println("Selected Font : " + font);
 				}
 
 			}
 		});
 		gR.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.green);
 				color = Color.green;
 			}
 		});
 		bL.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.blue);
 				color = Color.blue;
 			}
 		});
 		rD.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.red);
 				color = Color.red;
 			}
 		});
 		bLa.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				TXEAREA.setForeground(Color.black);
 				color = Color.black;
 			}
 		});
 		About.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				About about = new About();
 				about.setVisible(true);
 
 			}
 		});
 		caL.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				Calculator calculator = new Calculator();
 				calculator.setVisible(true);
 
 			}
 		});
 
 		JMenu ScrollSettings = new JMenu("Settings");
 		JMenuItem vsbA = new JMenuItem("Vertical Scroll Bar Always");
 		JMenuItem hsbA = new JMenuItem("Horizontal Scroll Bar Always");
 		JMenuItem vhsbA = new JMenuItem(
 				"Vertical And Horizontal Scroll Bar Always");
 		vsbA.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollv = new JScrollPane(TXEAREA,
 						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 			}
 		});
 		vsbA.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollh = new JScrollPane(TXEAREA,
 						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 			}
 		});
 		vhsbA.addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				JScrollPane scrollvh = new JScrollPane(TXEAREA,
 						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
 						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 			}
 		});
 		// Popupmenu
 		// Note the naming of these components for the popup menu is the normal
 		// component and with a p for Popupmenu
 		final JPopupMenu popup = new JPopupMenu();
 
 		popup.add(undoP);
 		popup.add(redoP);
 		popup.addSeparator();
 		popup.add(aeP);
 		popup.add(peP);
 		popup.addSeparator();
 		popup.add(sAP);
 		popup.addSeparator();
 		popup.add(boldP);
 		popup.add(italicsP);
 		popup.add(plainP);
 		popup.add(bIP);
 		popup.addSeparator();
 		popup.add(FrP);
 		popup.addSeparator();
 		popup.add(coLP);
 		popup.addSeparator();
 		popup.add(printP);
 		popup.addSeparator();
 		popup.add(dateP);
 		popup.addSeparator();
 		popup.add(dCP);
 		popup.add(cPP);
 		popup.addSeparator();
 		popup.add(sLP);
 		popup.add(sSP);
 
 		// add mouse listener
 		TXEAREA.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				showPopup(e);
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				showPopup(e);
 			}
 
 			private void showPopup(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		});
 		// New project menu item
 		// MouseListener PopUpShow = new popupshow();
 		// this.addMouseListener(PopUpShow);
 		// TXEAREA.addMouseListener(PopUpShow);
 		// splitpane.addMouseListener(PopUpShow);
 		// scroll.addMouseListener(PopUpShow);
 
 		ScrollSettings.add(vsbA);
 		ScrollSettings.add(hsbA);
 		ScrollSettings.add(vhsbA);
 		JMenuBar JMB = new JMenuBar();
 
 		setJMenuBar(JMB);
 		// make the file and edit
 		JMenu file = new JMenu("File");
 
 		JMenu edit = new JMenu("Edit");
 
 		// add menus
 		JMB.add(file);
 		JMB.add(edit);
 		JMB.add(format);
 		JMB.add(TXESettings);
 
 		// file menu items
 		file.add(About);
 		file.addSeparator();
 		// file.add(cL);
 		file.add(New);
 		file.addSeparator();
 		file.add(Open);
 		file.addSeparator();
 		file.add(Save);
 		file.add(SaveAs);
 		file.addSeparator();
 		file.add(print);
 		file.addSeparator();
 		file.add(Quit);
 		// tools menu items
 		TXESettings.add(settingsFrame);
 		TXESettings.addSeparator();
 		TXESettings.add(srenSht);
 		TXESettings.addSeparator();
 		TXESettings.add(addwebsite);
 		TXESettings.add(addname);
 		TXESettings.add(addcompany);
 		TXESettings.addSeparator();
 		TXESettings.add(caL);
 		TXESettings.add(enC);
 		TXESettings.addSeparator();
 		TXESettings.add(nimbus);
 		TXESettings.add(natives);
 		TXESettings.addSeparator();
 		TXESettings.add(HcoL);
 		TXESettings.addSeparator();
 		TXESettings.add(pT);
 		TXESettings.addSeparator();
 		TXESettings.add(dS);
 		for (int i = 0; i < 1; i++)
 
 			file.getItem(i).setIcon(null);
 
 		// file.getItem(1).setText("New");
 
 		// edit.addSeparator();
 		edit.add(Cut);
 		edit.add(Copy);
 		edit.add(Paste);
 		edit.addSeparator();
 		edit.add(undo);
 		edit.add(redo);
 		edit.addSeparator();
 		edit.add(date);
 		edit.addSeparator();
 		edit.add(sA);
 		edit.addSeparator();
 		edit.add(sL);
 		edit.add(sS);
 		edit.addSeparator();
 		edit.add(cP);
 		edit.add(dC);
 		edit.add(nor);
 		edit.addSeparator();
 		edit.add(tts);
 
 		edit.getItem(0).setText("Cut		");
 		edit.getItem(0).setIcon(
 				new ImageIcon(getClass().getResource("images/cut.gif")));
 
 		edit.getItem(1).setText("Copy			");
 		edit.getItem(1).setIcon(
 				new ImageIcon(getClass().getResource("images/copy.gif")));
 
 		edit.getItem(2).setText("Paste			");
 		edit.getItem(2).setIcon(
 				new ImageIcon(getClass().getResource("images/paste.gif")));
 
 		for (int n = 0; n < 0; n++)
 
 			edit.getItem(n).setIcon(null);
 
 		JToolBar TXEBAR = new JToolBar();
 
 		this.add(TXEBAR, BorderLayout.SOUTH);
 
 		TXEBAR.add(New);
 		TXEBAR.add(Open);
 		TXEBAR.add(Save);
 
 		TXEBAR.addSeparator();
 
 		JButton cut = TXEBAR.add(Cut), cop = TXEBAR.add(Copy), pas = TXEBAR
 				.add(Paste);
 
 		cut.setText(null);
 		cut.setIcon(new ImageIcon(getClass().getResource("images/cut.gif")));
 
 		cop.setText(null);
 		cop.setIcon(new ImageIcon(getClass().getResource("images/copy.gif")));
 
 		pas.setText(null);
 		pas.setIcon(new ImageIcon(getClass().getResource("images/paste.gif")));
 
 		TXEBAR.addSeparator();
 		TXEBAR.add(Normal);
 		TXEBAR.add(Bold);
 		TXEBAR.add(Italics);
 		TXEBAR.add(BI);
 		TXEBAR.addSeparator();
 		TXEBAR.add(bLaB);
 		TXEBAR.add(rDb);
 		TXEBAR.add(gRb);
 		TXEBAR.add(bLb);
 		TXEBAR.addSeparator();
 		TXEBAR.add(findButton);
 		TXEBAR.add(findText);
 
 		Save.setEnabled(false);
 
 		SaveAs.setEnabled(false);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		this.pack();
 
 		TXEAREA.addKeyListener(k1);
 
 		this.setTitle("TXE 1.8  " + currentFile);
 
 		this.setVisible(true);
 
 	}
 
 	public void removeHighlight(JTextComponent comp) {
 		Highlighter highlighte = comp.getHighlighter();
 		Highlighter.Highlight[] higlite = highlighte.getHighlights();
 		for (int i = 0; i < higlite.length; i++) {
 			if (higlite[i].getPainter() instanceof highLight) {
 				highlighte.removeHighlight(higlite[i]);
 			}
 		}
 	}
 
 	public void highlight(JTextComponent comp, String pattern) {
 
 		removeHighlight(comp);
 
 		try {
 
 			Highlighter highlighte = comp.getHighlighter();
 			Document doc = comp.getDocument();
 			String text = doc.getText(0, doc.getLength());
 			int pos = 0;
 			while ((pos = text.toUpperCase()
 					.indexOf(pattern.toUpperCase(), pos)) >= 0) {
 				highlighte.addHighlight(pos, pos + pattern.length(), HighLight);
 				pos += pattern.length();
 			}
 		} catch (Exception ex) {
 
 		}
 	}
 
 	public void nimbusActionPerformed(ActionEvent e) {
 		try {
 			for (LookAndFeelInfo feel : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(feel.getName())) {
 					UIManager.setLookAndFeel(feel.getClassName());
 					SwingUtilities.updateComponentTreeUI(this);
 					break;
 				}
 			}
 		} catch (Exception exc) {
 
 		}
 
 	}
 
 	public void nativeActionPerformed(java.awt.event.ActionEvent e) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
 		} catch (Exception exc) {
 		}
 	}
 
 	public void updateUndoRedoMenu() {
 
 	}
 
 	private void setIcon() {
 		setIconImage(Toolkit.getDefaultToolkit().getImage(
 				getClass().getResource("Neptune.png")));
 
 	}
 
 	private KeyListener k1 = new KeyAdapter() {
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 
 			changed = true;
 
 			Save.setEnabled(true);
 
 			SaveAs.setEnabled(true);
 		}
 
 	};
 	Action gRb = new AbstractAction("Green", new ImageIcon(getClass()
 			.getResource("images/green.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.GREEN);
 				color = Color.green;
 			}
 		}
 
 	};
 	Action bLaB = new AbstractAction("Normal", new ImageIcon(getClass()
 			.getResource("images/black.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.black);
 				color = Color.black;
 			}
 		}
 
 	};
 	Action rDb = new AbstractAction("Red", new ImageIcon(getClass()
 			.getResource("images/red.gif"))) {
 
 		/**
  * 
  */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.red);
 				color = Color.red;
 			}
 		}
 
 	};
 	Action bLb = new AbstractAction("Red", new ImageIcon(getClass()
 			.getResource("images/blue.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setForeground(Color.blue);
 				color = Color.blue;
 			}
 		}
 
 	};
 
 	Action BI = new AbstractAction("Bold and Italics", new ImageIcon(getClass()
 			.getResource("images/Bold and Italic.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.BOLD
 						+ Font.ITALIC, 12));
 			}
 		}
 
 	};
 
 	Action Normal = new AbstractAction("Normal", new ImageIcon(getClass()
 			.getResource("images/normal.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.PLAIN, 12));
 			}
 		}
 
 	};
 
 	Action Bold = new AbstractAction("Bold", new ImageIcon(getClass()
 			.getResource("images/Bold.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.BOLD, 12));
 			}
 		}
 
 	};
 
 	Action Italics = new AbstractAction("Italics", new ImageIcon(getClass()
 			.getResource("images/Italic.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			{
 				TXEAREA.setFont(new Font("Times New Roman", Font.ITALIC, 12));
 			}
 		}
 
 	};
 
 	Action Open = new AbstractAction("Open", new ImageIcon(getClass()
 			.getResource("images/open.gif"))) {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 
 				readInFile(dialog.getSelectedFile().getAbsolutePath());
 
 			}
 
 			SaveAs.setEnabled(true);
 
 		}
 
 	};
 	Action New = new AbstractAction("New", new ImageIcon(getClass()
 			.getResource("images/new.gif"))) {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			TXEAREA.setText("Type here");
 
 			currentFile = "Untitled Document";
 
 			setTitle("TXE 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 			Save.setEnabled(false);
 
 			SaveAs.setEnabled(false);
 
 		}
 
 	};
 
 	Action Save = new AbstractAction("Save", new ImageIcon(getClass()
 			.getResource("images/save.gif"))) {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			if (!currentFile.equals(currentFile))
 
 				saveFile(currentFile);
 
 			else
 
 				saveFileAs();
 
 		}
 
 	};
 
 	Action SaveAs = new AbstractAction("Save as") {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			saveFileAs();
 
 		}
 
 	};
 
 	Action Quit = new AbstractAction("Quit") {
 
 		/**
 	 * 
 	 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			saveOld();
 
 			System.exit(0);
 
 		}
 
 	};
 
 	ActionMap m = TXEAREA.getActionMap();
 
 	Action Cut = m.get(DefaultEditorKit.cutAction);
 
 	Action Copy = m.get(DefaultEditorKit.copyAction);
 
 	Action Paste = m.get(DefaultEditorKit.pasteAction);
 
 	private void saveOld() {
 
 		if (changed) {
 
 			if (JOptionPane.showConfirmDialog(this, "Would you like to save "
 					+ currentFile + " ?", "Save", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
 
 				saveFile(currentFile);
 
 		}
 
 	}
 
 	public void readInFile(String fileName) {
 
 		try {
 
 			FileReader fr = new FileReader(fileName);
 
 			TXEAREA.read(fr, null);
 
 			fr.close();
 
 			currentFile = fileName;
 
 			setTitle("TXE 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 		}
 
 		catch (IOException e) {
 
 			JOptionPane.showMessageDialog(this, "TXE can not find the file: "
 					+ fileName);
 
 		}
 
 	}
 
 	private void saveFile(String fileName) {
 
 		try {
 
 			FileWriter fw = new FileWriter(fileName);
 
 			TXEAREA.write(fw);
 
 			fw.close();
 
 			currentFile = fileName;
 			// important
 			setTitle("Txe 1.7.1 Beta - " + currentFile);
 
 			changed = false;
 
 			Save.setEnabled(false);
 
 		}
 
 		catch (IOException e) {
 
 		}
 
 	}
 
 	private void saveFileAs() {
 
 		if (dialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
 
 			saveFile(dialog.getSelectedFile().getAbsolutePath());
 
 	}
 
 	/**
 	 * 
 	 * @author Eric Zhu of Great Ark Studios and http://www.java2s.com/
 	 * 
 	 */
 
 	static class FileSystemModel implements TreeModel, ActionListener {
 		private String root; // The root identifier
 
 		private Vector listeners; // Declare the listeners vector
 
 		public FileSystemModel() {
 
 			root = System.getProperty("user.dir");
 			File tempFile = new File(root);
 			root = tempFile.getParent();
 
 			listeners = new Vector();
 		}
 
 		@Override
 		public Object getRoot() {
 			return (new File(root));
 		}
 
 		@Override
 		public Object getChild(Object parent, int index) {
 			File directory = (File) parent;
 			String[] directoryMembers = directory.list();
 			return (new File(directory, directoryMembers[index]));
 		}
 
 		@Override
 		public int getChildCount(Object parent) {
 			File fileSystemMember = (File) parent;
 			if (fileSystemMember.isDirectory()) {
 				String[] directoryMembers = fileSystemMember.list();
 				return directoryMembers.length;
 			}
 
 			else {
 
 				return 0;
 			}
 		}
 
 		@Override
 		public int getIndexOfChild(Object parent, Object child) {
 			File directory = (File) parent;
 			File directoryMember = (File) child;
 			String[] directoryMemberNames = directory.list();
 			int result = -1;
 
 			for (int i = 0; i < directoryMemberNames.length; ++i) {
 				if (directoryMember.getName().equals(directoryMemberNames[i])) {
 					result = i;
 					break;
 				}
 			}
 
 			return result;
 		}
 
 		@Override
 		public boolean isLeaf(Object node) {
 			return ((File) node).isFile();
 		}
 
 		@Override
 		public void addTreeModelListener(TreeModelListener l) {
 			if (l != null && !listeners.contains(l)) {
 				listeners.addElement(l);
 			}
 		}
 
 		@Override
 		public void removeTreeModelListener(TreeModelListener l) {
 			if (l != null) {
 				listeners.removeElement(l);
 			}
 		}
 
 		@Override
 		public void valueForPathChanged(TreePath path, Object newValue) {
 			// Does Nothing!
 		}
 
 		public void fireTreeNodesInserted(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesInserted(e);
 			}
 		}
 
 		public void fireTreeNodesRemoved(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesRemoved(e);
 			}
 
 		}
 
 		public void fireTreeNodesChanged(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeNodesChanged(e);
 			}
 
 		}
 
 		public void fireTreeStructureChanged(TreeModelEvent e) {
 			Enumeration listenerCount = listeners.elements();
 			while (listenerCount.hasMoreElements()) {
 				TreeModelListener listener = (TreeModelListener) listenerCount
 						.nextElement();
 				listener.treeStructureChanged(e);
 			}
 
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 	/**
 	 * 
 	 * @author ericzhu, and ProgrammingKnowledge
 	 * 
 	 */
 	static class highLight extends DefaultHighlighter.DefaultHighlightPainter {
 		public highLight(Color color) {
 			super(color);
 		}
 
 	}
 
 	public static void main(String[] args) {
 		TXE1 txe1 = new TXE1();
 
 	}
 
 }
