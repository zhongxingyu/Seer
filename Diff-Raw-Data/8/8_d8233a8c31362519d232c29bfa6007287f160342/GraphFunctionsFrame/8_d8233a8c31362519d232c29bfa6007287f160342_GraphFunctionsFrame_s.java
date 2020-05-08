 package lorian.graph;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JColorChooser;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.filechooser.FileFilter;
 
 import lorian.graph.fileio.GraphFileReader;
 import lorian.graph.fileio.GraphFileWriter;
 import lorian.graph.fileio.JFileChooserWithConfirmation;
 import lorian.graph.function.Function;
 import lorian.graph.function.Function2Var;
 import lorian.graph.function.MathChars;
 import lorian.graph.function.PointXYZ;
 import lorian.graph.function.Util;
 import lorian.graph.fileio.ExtensionFileFilter;
 import lorian.graph.lang.Language;
 import lorian.graph.opengl.Graph3D;
 import lorian.graph.opengl.Point3D;
 
 
 public class GraphFunctionsFrame extends JFrame implements ActionListener, KeyListener, MouseListener, WindowListener {
 	private static final long serialVersionUID = -1090268654275240501L;
 	
 	public static final String version = "1.5 Beta";
 	
 	// Only for GraphFileReader and GraphFileWriter
 	public static final short major_version = 0x0001;
 	public static final short minor_version = 0x0005;
 	
 	public static Language language;
 	public static Language lang_en;
 	
 	public static int MaxFunctions = 20;
 	private final Dimension WindowSize = new Dimension(800, 800);
 	private final Dimension WindowSizeSmall = new Dimension(640, 640);
 	public static List<Function> functions;
 	public static List<Function2Var> functions2var;
 	
 	private List<JTextField> textfields;
 	private List<JLabel> labels;
 	private List<JCheckBox> checkboxes;
 	private List<ParseResultIcon> parseresults;
 	
 	//private final String[] buttons = { "Draw", "Special characters"}; 
 	//private String[] calcMenuStrings = { "Value", "Zero", "Minimum", "Maximum", "Intersect", "dy/dx", MathChars.Integral.getCode() + "f(x)dx" };
 	private final String[] MenuStrings = {"menu.file", "menu.calculate", "menu.mode", "menu.help" };
 	private final String[] FileMenuStrings = {"file.new", "file.open", "file.save", "file.saveas", "file.settings", "file.exit" };
 	private final String[] calcMenuStrings = { "calc.value", "calc.zero", "calc.min", "calc.max", "calc.intersect", "calc.deriv",  MathChars.Integral.getCode() + "f(x)dx"};
 	private final String[] HelpMenuStrings = { "help.about" };
 	private final String[] buttons = { "buttons.draw", "buttons.specialchars"}; 
 		
 	private String[] old2DTextFieldValues = new String[MaxFunctions];
 	private String[] old3DTextFieldValues = new String[MaxFunctions];
 	private final Color[] defaultColors = { new Color(37, 119, 255), new Color(224,0,0).brighter(), new Color(211,0,224).brighter(), new Color(0,158,224).brighter(), new Color(0,255,90), new Color(221,224,0).brighter(), new Color(224,84,0).brighter() };  
 	
 	public static boolean FileSaved = false;
 	public static boolean FilePathPresent = false;
 	public static String FileName;
 	public static String FilePath;
 	public static final String FileExt = "lgf";
 	
 	private boolean empty = true;
 	
 	private static GraphFunctionsFrame funcframe;
 	public static GraphFrame gframe; 
 	public static Graph3D g3d;
 	private JPanel G3DContainer;
 	private CalculateFrame calcframe;
 	private static SettingsFrame settingsframe;
 	private static SpecialCharsFrame charframe;
 	public static WindowSettings settings;
 
 	public JMenuBar menuBar;
 	public static boolean applet = false;
 	private JPanel functionsInputPanel;
 	public JPanel MainPanel;
 	
 	public JProgressBar progressbar;
 	private JPanel progressPanel;
 	private boolean doProgressBar = false;
 	
 	private boolean enable3d = false;
 	
 	public GraphFunctionsFrame(boolean applet)
 	{
 		this(applet, false);
 	}
 	public GraphFunctionsFrame(boolean applet, boolean forceSmall)
 	{
 		super();
 		initVars(applet);
 		initLanguages();
 		initUI(forceSmall);
 		FileName = Translate("files.untitled");
 		if(!applet)
 		{
 			setTitle(FileName + " * - " + "Graph v" + version);
 			this.setVisible(true);
 		}
 		
 		Render();
 
 	}
 	private void initVars(boolean applet)
 	{
 		textfields = new ArrayList<JTextField>();
 		labels = new ArrayList<JLabel>();
 		checkboxes = new ArrayList<JCheckBox>();
 		parseresults = new ArrayList<ParseResultIcon>();
 		functions = new ArrayList<Function>();
 		functions2var = new ArrayList<Function2Var>();
 		settings = new WindowSettings();
 		GraphFunctionsFrame.applet = applet;
 	}
 	
 	private void initLanguages()
 	{
 		language = new Language();
 		lang_en = new Language();
 		System.out.println("Reading language files...");
 		try {
 			language.readDefault();
 		} catch (IOException e) {
 			System.out.println("Could not read local language file: " + e.getMessage());
 			System.out.println("Switching to English.");
 			
 		}
 		
 		try {
 			lang_en.read("en");
 		} catch (IOException e) {
 			System.out.println("Could not read English language file: " + e.getMessage());
 			System.exit(-1);
 		} 
 		translateOptionPane();
 		translateFileChooser();
 		translateOtherStrings();
 		System.out.println("Done");
 
 	}
 	private void translateFileChooser()
 	{
 		  UIManager.put("FileChooser.lookInLabelText", Translate("filechooser.lookin"));
 		  UIManager.put("FileChooser.saveInLabelText", Translate("filechooser.savein"));
 		  UIManager.put("FileChooser.saveDialogTitleText", Translate("filechooser.save"));
 		  UIManager.put("FileChooser.openDialogTitleText", Translate("filechooser.open"));
 		  UIManager.put("FileChooser.filesOfTypeLabelText", Translate("filechooser.filesoftype"));
 		  UIManager.put("FileChooser.upFolderToolTipText", Translate("filechooser.upfolder"));
 		  UIManager.put("FileChooser.fileNameLabelText",  Translate("filechooser.filename"));
 		  UIManager.put("FileChooser.newFolderToolTipText", Translate("filechooser.newfolder"));
 		  UIManager.put("FileChooser.viewMenuLabelText", Translate("filechooser.view"));
 		  UIManager.put("FileChooser.saveButtonText", Translate("filechooser.save"));
 		  UIManager.put("FileChooser.openButtonText", Translate("filechooser.open"));
 		  UIManager.put("FileChooser.cancelButtonText", Translate("filechooser.cancel"));
 		  UIManager.put("FileChooser.updateButtonText", Translate("filechooser.update"));
 		  UIManager.put("FileChooser.refreshActionLabelText", Translate("filechooser.refresh"));
 		  UIManager.put("FileChooser.newFolderActionLabelText", Translate("filechooser.newfolder"));
 		  UIManager.put("FileChooser.listViewActionLabelText", Translate("filechooser.list"));
 		  UIManager.put("FileChooser.detailsViewActionLabelText", Translate("filechooser.details"));
 		  UIManager.put("FileChooser.helpButtonText", Translate("filechooser.help"));
 		  UIManager.put("FileChooser.saveButtonToolTipText", Translate("filechooser.save"));
 		  UIManager.put("FileChooser.openButtonToolTipText", Translate("filechooser.open"));
 		  UIManager.put("FileChooser.cancelButtonToolTipText", Translate("filechooser.cancel"));
 		  UIManager.put("FileChooser.updateButtonToolTipText", Translate("filechooser.update"));
 		  UIManager.put("FileChooser.helpButtonToolTipText", Translate("filechooser.help"));
 		  UIManager.put("FileChooser.win32.newFolder", Translate("filechooser.newfolder"));
 		  UIManager.put("FileChooser.win32.newFolder.subsequent", Translate("filechooser.newfolder") + " ({0})");
 		  UIManager.put("FileChooser.other.newFolder", Translate("filechooser.newfolder"));
 		  UIManager.put("FileChooser.other.newFolder.subsequent", Translate("filechooser.newfolder") + " ({0})"); 
 		  UIManager.put("FileChooser.listViewButtonToolTipText", Translate("filechooser.list"));
 		  UIManager.put("FileChooser.detailsViewButtonToolTipText", Translate("filechooser.details"));
 		  UIManager.put("FileChooser.viewMenuButtonToolTipText", Translate("filechooser.viewmenu")); 
 		  UIManager.put("FileChooser.acceptAllFileFilterText", Translate("files.allfiles")); 
 	}
 	private void translateOptionPane()
 	{
 		 UIManager.put("OptionPane.cancelButtonText", Translate("optionpane.cancel"));
 		 UIManager.put("OptionPane.noButtonText", Translate("optionpane.no"));
 		 UIManager.put("OptionPane.okButtonText", Translate("optionpane.ok"));
 		 UIManager.put("OptionPane.yesButtonText", Translate("optionpane.yes"));
 	}
 	private void translateOtherStrings()
 	{
 	
 		for(int i=0;i<MenuStrings.length;i++)
 		{
 			MenuStrings[i] = Translate(MenuStrings[i]);
 		}
 		
 		
 		for(int i=0;i<FileMenuStrings.length;i++)
 		{
 			FileMenuStrings[i] = Translate(FileMenuStrings[i]);
 		}
 		
 		// Translate all except integral
 		for(int i=0;i<calcMenuStrings.length-1;i++)
 		{
 			calcMenuStrings[i] = Translate(calcMenuStrings[i]);
 		}
 		
 		for(int i=0;i<HelpMenuStrings.length;i++)
 		{
 			HelpMenuStrings[i] = Translate(HelpMenuStrings[i]);
 		}
 		
 		for(int i=0;i<buttons.length;i++)
 		{
 			buttons[i] = Translate(buttons[i]);
 		}
 		
 		
 	}
 	private Color ChooseColor(Component component, String title, Color initialColor)
 	{
 		return JColorChooser.showDialog(component, title, initialColor);
 	}
 	
 	public static String Translate(String key)
 	{
 		if(language.isAvailable(key))
 		{
 			return language.getValue(key);
 		}
 		else if(lang_en.isAvailable(key))
 		{
 			return lang_en.getValue(key);
 		}
 		else
 			return key;
 	}
 	private static void UpdateGUIStyle()
 	{
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(funcframe);
 		}
 		catch (NullPointerException e) { 
 			//System.out.println("Error changing GUI Style of function frame");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(gframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of gframe");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(settingsframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of settings frame");
 		}
 		try
 		{
 			SwingUtilities.updateComponentTreeUI(charframe);
 		}
 		catch (NullPointerException e) {
 			//System.out.println("Error changing GUI Style of char frame");
 		}
 		
 	}
 	public static void SetSystemLookAndFeel()
 	{		
 		try {
 			UIManager.setLookAndFeel(//"com.sun.java.swing.plaf.windows.WindowsLookAndFeel"
 					UIManager.getSystemLookAndFeelClassName()
 					);
 		} //catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		} 		
 		UpdateGUIStyle();
 	}
 	public static void SetJavaLookAndFeel()
 	{
 		try
 		{
 			UIManager.setLookAndFeel(
 					UIManager.getCrossPlatformLookAndFeelClassName()
 					);
 		} //catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return;
 		} 	
 		UpdateGUIStyle();
 	}
 	public static void UpdateWindowSettings()
 	{		
 		gframe.UpdateWindowSettings(settings);
 		if(gframe.windowerror)
 		{
 			JOptionPane.showMessageDialog(null, Translate("message.windowsettingserror"), Translate("message.error"), JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	public static void UpdateTitle()
 	{
 		if(!applet)
 			//GraphFunctionsFrame.funcframe.setTitle("Graph v" + version + " - " + FileName + (FileSaved ? "" : " *"));
 			GraphFunctionsFrame.funcframe.setTitle(FileName + (FileSaved ? "" : " *") + " - Graph v" + version );
 			
 	}
 	private void InitMenu()
 	{
 		JMenu fileMenu, calcMenu, modeMenu, helpMenu;
 		JMenuItem NewFileItem,OpenFileItem, SaveFileItem, SaveFileAsItem, settingsItem, exitItem;
 		JMenuItem aboutItem;
 		
 		menuBar = new JMenuBar();
 		fileMenu = new JMenu(MenuStrings[0]);
 		calcMenu = new JMenu(MenuStrings[1]);
 		modeMenu = new JMenu(MenuStrings[2]);
 		helpMenu = new JMenu(MenuStrings[3]);
 		menuBar.add(fileMenu);
 		menuBar.add(calcMenu);
 		menuBar.add(modeMenu);
 		menuBar.add(helpMenu);
 		
 		JRadioButtonMenuItem toggle2dItem = new JRadioButtonMenuItem("y=f(x)");
 		JRadioButtonMenuItem toggle3dItem = new JRadioButtonMenuItem("z=f(x,y) (ALPHA)");
 		toggle2dItem.setSelected(true);
 		toggle2dItem.setName("toggle2d");
 		toggle3dItem.setName("toggle3d"); 
 		toggle3dItem.addActionListener(this);
 		toggle2dItem.addActionListener(this);
 		
 		ButtonGroup modeGroup = new ButtonGroup();
 		modeGroup.add(toggle2dItem);
 		modeGroup.add(toggle3dItem);
 		
 		modeMenu.add(toggle2dItem);
 		modeMenu.add(toggle3dItem);
 		
 		NewFileItem = new JMenuItem(FileMenuStrings[0]);
 		NewFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)); 
 		NewFileItem.addActionListener(this);
 		
 		OpenFileItem = new JMenuItem(FileMenuStrings[1]);
 		OpenFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)); 
 		OpenFileItem.addActionListener(this);
 		
 		SaveFileItem = new JMenuItem(FileMenuStrings[2]);
 		SaveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)); 
 		SaveFileItem.addActionListener(this);
 		
 		SaveFileAsItem = new JMenuItem(FileMenuStrings[3]);
 		SaveFileAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)); 
 		SaveFileAsItem.addActionListener(this);
 		
 		settingsItem = new JMenuItem(FileMenuStrings[4]);
 		settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK)); 
 		settingsItem.addActionListener(this);
 		
 		
 		fileMenu.add(NewFileItem);
 		fileMenu.add(OpenFileItem);
 		fileMenu.add(SaveFileItem);
 		fileMenu.add(SaveFileAsItem);
 		fileMenu.addSeparator();
 		fileMenu.add(settingsItem); 
 		
 		
 		if(!applet)
 		{
 			exitItem = new JMenuItem(FileMenuStrings[5]);
 			exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
 			exitItem.addActionListener(this);
 			fileMenu.addSeparator();
 			fileMenu.add(exitItem);
 		}
 		
 		for(int i=0;i<calcMenuStrings.length; i++)
 		{
 			JMenuItem item = new JMenuItem(language.getValue(calcMenuStrings[i]));
 			item.addActionListener(this);
 			calcMenu.add(item);
 		}
 		
 		
 		aboutItem = new JMenuItem(HelpMenuStrings[0]);
 		aboutItem.addActionListener(this);
 		helpMenu.add(aboutItem);
 		if(!applet)
 			this.setJMenuBar(menuBar);
 	}
 	private void initUI(boolean forceSmall)
 	{
 		MainPanel = new JPanel();
 		
 		boolean small;
 		if(!applet && !forceSmall)
 		{
 			if(Toolkit.getDefaultToolkit().getScreenSize().getHeight() <= 900) 
 				small = true;
 			else 
 				small = false;
 		}	
 		else
 		{
 			System.out.println("Forcing small mode");
 			small = forceSmall;
 		}
 	
 		
 		SetSystemLookAndFeel();
 		if(small)
 		{
 			System.out.println("Setting window to smaller size");
 			gframe = new GraphFrame(functions, settings, WindowSizeSmall);
 			GraphFunctionsFrame.MaxFunctions = 18;
 		}
 		else
 			gframe = new GraphFrame(functions, settings, WindowSize);
 		
 		JPanel panel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 		
 		int height = 0;
 		
 		for(int i=0;i<MaxFunctions; i++)
 		{
 			JPanel funcpanel = new JPanel();
 			SpringLayout funcpanellayout = new SpringLayout();
 			funcpanel.setLayout(funcpanellayout);
 			
 			JCheckBox checkBox = new JCheckBox();
 			checkBox.setSelected(true);
 			checkBox.setFocusable(false);
 			checkBox.addActionListener(this);
 			checkBox.setName("c" + i);
 			
 			JLabel label = new JLabel("Y" + (i+1) + " = ");
 			label.setFont(label.getFont().deriveFont(13.0f));
 			label.setOpaque(true);
 			label.setBackground(defaultColors[i % defaultColors.length]);
 			label.setForeground(Color.BLACK);
 			label.addMouseListener(this);
 			
 			
 			JTextField textField = new JTextField();
 				
 			textField.setPreferredSize(new Dimension(350, (int) textField.getPreferredSize().getHeight() + 5)); 
 			textField.setFont(textField.getFont().deriveFont(15.0f));
 			textField.setForeground(Color.BLACK);
 			textField.addKeyListener(this);
 			
 			//JTextPane textField = new JTextPane();
 			//textField.setContentType("text/html");
 			//textField.setText("<HTML>x<sup>2</sup></HTML> ");
 			//textField.setPreferredSize(new Dimension( 55 * (int) textField.getPreferredSize().getWidth(), (int) textField.getPreferredSize().getHeight()));
 			ParseResultIcon icon = new ParseResultIcon();
 			icon.setState(ParseResultIcon.State.EMPTY);
 			
 			
 			height = ((int) textField.getPreferredSize().getHeight() + 5);
 			funcpanel.setPreferredSize(new Dimension(450, height));
 			
 			funcpanel.add(checkBox);
 			funcpanel.add(label);
 			funcpanel.add(textField);
 			funcpanel.add(icon);
 			
 			SpringLayout.Constraints labelCons = funcpanellayout.getConstraints(label);
 			labelCons.setX(Spring.constant(25));
 			labelCons.setY(Spring.constant(3));
 			SpringLayout.Constraints textFieldCons = funcpanellayout.getConstraints(textField);
 			textFieldCons.setX(Spring.constant(70));
 			SpringLayout.Constraints iconCons = funcpanellayout.getConstraints(icon);
 			iconCons.setX(Spring.constant(423));
 			
 			panel.add(funcpanel);
 			SpringLayout.Constraints funcPanelCons = layout.getConstraints(funcpanel);
 			funcPanelCons.setX(Spring.constant(0));
 			funcPanelCons.setY(Spring.constant(8 + height * i));
 			
 			/*
 			panel.add(label);
 			panel.add(textField);
 			
 			SpringLayout.Constraints labelCons = layout.getConstraints(label);
 			labelCons.setX(Spring.constant(5));
 			labelCons.setY(Spring.constant(8 + height * i));
 			SpringLayout.Constraints textFieldCons = layout
 				        .getConstraints(textField);
 			textFieldCons.setX(Spring.constant(50));
 			textFieldCons.setY(Spring.constant(5 + height  * i));
 			*/
 			checkboxes.add(checkBox);
 			textfields.add(textField);
 			labels.add(label);
 			parseresults.add(icon);
 			
 			//funcpanel.requestFocusInWindow();
 
 			
 			
 		}
 		
 		
 		JPanel buttonpanel = new JPanel();
 		for(int i=0;i< buttons.length ;i++)
 		{
 			JButton button = new JButton(buttons[i]);
 			button.setFont(button.getFont().deriveFont(13.0f));
 			button.addActionListener(this);
 			buttonpanel.add(button);
 		}
 		SpringLayout.Constraints buttonPanelCons = layout.getConstraints(buttonpanel);
 		buttonPanelCons.setX(Spring.constant(50));
 		buttonPanelCons.setY(Spring.constant(10 + height *MaxFunctions));
 		panel.add(buttonpanel);
 		
 		progressPanel = new JPanel();
 		//progressPanel.setLayout(new GridLayout(2, 1)); 
 		progressbar = new JProgressBar(0, 100);
 		progressbar.setValue(0);
 		progressbar.setPreferredSize(new Dimension(300, 20));
 		progressbar.setStringPainted(true);
 		//progressbar.setVisible(false);
 		
 		progressPanel.add(progressbar);
 		progressPanel.setVisible(false);
 		
 		panel.add(progressPanel);
 		
 		SpringLayout.Constraints progressCons = layout.getConstraints(progressPanel);
 		progressCons.setX(Spring.constant(50));
 		if(small)
 			progressCons.setY(Spring.constant(height * MaxFunctions + 50));
 		else
 			progressCons.setY(Spring.constant(745));
 			
 		//if(applet)
 			MainPanel.setLayout(new BorderLayout());
 		//else this.setLayout(new BorderLayout());
 		
 		panel.setPreferredSize(new Dimension(450, 50 + height * (MaxFunctions+1)));
 		
 		this.functionsInputPanel = panel;
 		
 		MainPanel.add(functionsInputPanel, BorderLayout.WEST);
 		MainPanel.add(new JSeparator(JSeparator.VERTICAL));		
 		MainPanel.add((JPanel) gframe, BorderLayout.EAST);
 		
 		/*
 		else
 		{
 			this.add(panel, BorderLayout.WEST);
 			this.add(new JSeparator(JSeparator.VERTICAL));		
 			this.add((JPanel) gframe, BorderLayout.EAST);
 		}*/
 		
 		
 		InitMenu();
 		
 
 		if(!applet)
 		{
 			this.add(MainPanel);
 			//this.pack();
 		}	
 		
 		textfields.get(0).requestFocusInWindow();
 		this.setBackground(Color.WHITE);
 		if(applet)
 		{
 			if(small)
 				MainPanel.setSize(450 + (int) WindowSizeSmall.getWidth() + 10, (int) WindowSizeSmall.getHeight() + 50);
 			else
 				MainPanel.setSize(450 + (int) WindowSize.getWidth() + 10, (int) WindowSize.getHeight() + 50);
 		}
 		else
 		{
 			if(small)
 				this.setSize(450 + (int) WindowSizeSmall.getWidth() + 10, (int) WindowSizeSmall.getHeight() + 50);
 			else
 				this.setSize(450 + (int) WindowSize.getWidth() + 10, (int) WindowSize.getHeight() + 50);
 			this.setResizable(false);
 			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 			this.addWindowListener(this);
 			this.setLocationRelativeTo(null);
 		}
 		
 	}
 	private void Toggle3D()
 	{	
 		MainPanel.setVisible(false);
 		int i=0;
 		
 		
 		if(enable3d)
 		{
 			if(g3d == null || G3DContainer == null)
 			{
 				g3d = new Graph3D(800, 800);
 			
 				G3DContainer = new JPanel();
 				G3DContainer.setPreferredSize(g3d.getSize());
 				G3DContainer.setSize(g3d.getSize());
 				G3DContainer.add(g3d);
 			}
 			
 			i=0;
 			for(JLabel l: labels)
 			{
 				l.setText("Z" + (i+1) + " = ");
 				i++;
 			}
 			
 			i=0;
 			for(JTextField txt: textfields)
 			{
 				old2DTextFieldValues[i] = txt.getText();
 				txt.setText(old3DTextFieldValues[i]);
 				i++;
 			}
 			
 			MainPanel.removeAll();
 			MainPanel.add(functionsInputPanel, BorderLayout.WEST);
 			MainPanel.add(new JSeparator(JSeparator.VERTICAL));		
 			MainPanel.add(G3DContainer, BorderLayout.EAST);
 		}
 		else
 		{
 			i=0;
 			for(JLabel l: labels)
 			{
 				l.setText("Y" + (i+1) + " = ");
 				i++;
 			}
 			i=0;
 			for(JTextField txt: textfields)
 			{
 				old3DTextFieldValues[i] = txt.getText();
 				txt.setText(old2DTextFieldValues[i]);
 				i++;
 			}
 			MainPanel.removeAll();
 			MainPanel.add(functionsInputPanel, BorderLayout.WEST);
 			MainPanel.add(new JSeparator(JSeparator.VERTICAL));		
 			MainPanel.add(gframe, BorderLayout.EAST);
 		}
 		MainPanel.setVisible(true);
 		this.repaint();
 		this.paintAll(this.getGraphics());
 		
 		Render();
 	}
 	
 	private boolean SaveFile() 
 	{
 		Render();
 		System.out.println("Saving to " + FilePath);
 		GraphFileWriter fw = new GraphFileWriter(FilePath);
 		fw.setWindowSettings(settings);
 		
 		int i=0;
 		for(Function f: functions)
 		{
 			if(!f.isEmpty())
 			{
 				fw.addFunction(f, i);
 			}
 			i++;
 		}
 		try
 		{
 			boolean result =  fw.write();
 			System.out.println("Done");
 			FileSaved = result;
 			empty = false;
 			return result;
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	private boolean OpenFile(String filePath)
 	{
 		System.out.println("Opening " + filePath);
 		progressbar.setValue(0);
 		progressbar.setString(String.format(Translate("message.progressbar.opening"), (new File(filePath)).getName(), progressbar.getValue()));
 		progressPanel.setVisible(true);
 		progressPanel.paintAll(progressPanel.getGraphics());
 		GraphFileReader fr = new GraphFileReader(filePath);
 		try
 		{
 			if(!fr.read())
 			{
 				progressPanel.setVisible(false);
 				JOptionPane.showMessageDialog(this, String.format(Translate("message.fileopenerror"), (new File(filePath)).getName()), Translate("message.error"), JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 			
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(this, String.format(Translate("message.fileopenerror"), (new File(filePath)).getName()), Translate("message.error"), JOptionPane.ERROR_MESSAGE);
 			progressPanel.setVisible(false);
 			return false;
 		}
 		doProgressBar = false;
 		ClearAll();
 		doProgressBar = true;
 		progressbar.setValue(20);
 		progressbar.setString(String.format(Translate("message.progressbar.opening"), (new File(filePath)).getName(), progressbar.getValue()));
 		progressPanel.paintAll(progressPanel.getGraphics());
 
 		String[] reconstructedfunctions = fr.getFunctionStrings();
 		short[] indexes = fr.getFunctionIndexes();
 		Function[] functions = fr.getFunctions();
 		
 		settings = fr.getWindowSettings();
 		for(int i=0;i<indexes.length; i++)
 		{
 			if(reconstructedfunctions[i].trim().equalsIgnoreCase("")) continue;
 			
 			int j = indexes[i];
 			JCheckBox checkbox = this.checkboxes.get(j);
 			JLabel label = this.labels.get(j);
 			JTextField textfield = this.textfields.get(j);
 			checkbox.setSelected(functions[i].drawOn());
 			label.setBackground(functions[i].getColor());
 			textfield.setText(reconstructedfunctions[i]);
 			this.checkboxes.set(j, checkbox);
 			this.labels.set(j, label);
 			this.textfields.set(j, textfield);
 		}
 		progressbar.setValue(50);
 		progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 		progressPanel.paintAll(progressPanel.getGraphics());
 		UpdateWindowSettings();
 		Render();
 
 		
 		progressPanel.setVisible(false);
 		
 		FilePath = filePath;
 		FilePathPresent = true;
 		FileName = (new File(filePath)).getName();
 		FileSaved = true;
 		return true;
 	}
 	
 	private boolean ShowOpenFileDialog()
 	{
 		JFileChooserWithConfirmation openFile;
 		 if(!FilePathPresent)
 			 openFile = new JFileChooserWithConfirmation(System.getProperty("user.dir"));
 		 else
 			 openFile = new JFileChooserWithConfirmation(new File(FilePath));
 		 
 		 FileFilter openFilter = new ExtensionFileFilter(Translate("files.graphfiles"), new String[] { FileExt });
 		 openFile.setFileFilter(openFilter);
 		 
 		 int openOption = openFile.showOpenDialog(this);
 		 if(openOption == JFileChooser.APPROVE_OPTION)
 		 {
 			String filePath = openFile.getSelectedFile().getAbsolutePath();
 			if(!filePath.endsWith("." + FileExt))
 			{
 				filePath += "." + FileExt;
 			}
 			return OpenFile(filePath);
 		 }
 		 else return false;
 	}
 	private boolean SaveFileAs()
 	{
 		 JFileChooserWithConfirmation saveFile;
 		 if(!FilePathPresent)
 			 saveFile = new JFileChooserWithConfirmation(System.getProperty("user.dir"));
 		 else
 			 saveFile = new JFileChooserWithConfirmation(new File(FilePath));
 		 
 	
 		 FileFilter saveFilter = new ExtensionFileFilter(Translate("files.graphfiles"), new String[] { FileExt });
 		 saveFile.setFileFilter(saveFilter);
 		 
 		 int saveOption = saveFile.showSaveDialog(this);
 		 if(saveOption == JFileChooser.APPROVE_OPTION)
 		 {
 			FilePath = saveFile.getSelectedFile().getAbsolutePath();
 			if(!FilePath.endsWith("." + FileExt))
 			{
 				FilePath += "." + FileExt;
 			}
 			FilePathPresent = true;
 			FileName = (new File(FilePath)).getName();
 			return SaveFile();
 					
 		 }
 		 else return false;
 	}
 	private boolean ConfirmFileChanges()
 	{
		
		int n= JOptionPane.showConfirmDialog (this, String.format(Translate("message.confirmchanges"), FileName), "Graph", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 		
 		if(n == JOptionPane.YES_OPTION)
 		{
 			if(FilePathPresent)
 				return SaveFile();
 			else
 				return SaveFileAs();
 		}
 		else if(n == JOptionPane.NO_OPTION)
 		{
 			return true;
 		}
 		else if(n == JOptionPane.CANCEL_OPTION)
 		{
 			return false;
 		}
 		return false;
 	}
 	
 	private void ClearAll()
 	{
 		for(JTextField txt: textfields)
 		{
 			txt.setText("");
 		}
 		for(JCheckBox check : checkboxes)
 		{
 			check.setSelected(true);
 		}
 		for(int i=0;i<MaxFunctions;i++)
 		{
 			JLabel label = this.labels.get(i);
 			label.setBackground(defaultColors[i % defaultColors.length]);
 		}
 		
 		settings = new WindowSettings();
 		FilePath = "";
 		FileSaved = false;
 		FilePathPresent = false;
 		FileName = Translate("files.untitled"); 
 		empty = true;
 		UpdateWindowSettings();
 		if(settingsframe != null)
 			settingsframe.ResetWindowSettings();
 		Render();
 	}
 
 	private void Render()
 	{
 		if(enable3d)
 			Render3D();
 		else 
 			Render2D();
 	}
 	private void Render3D()
 	{
 		System.out.println("Parsing functions... ");
 		
 		int progstart = progressbar.getValue();
 		functions2var.clear();
 		for(int i=0;i<GraphFunctionsFrame.MaxFunctions;i++)
 		{
 			String text = Util.removeWhiteSpace(textfields.get(i).getText().trim());
 			Function2Var f = new Function2Var();
 			ParseResultIcon parseresult = parseresults.get(i);
 			if(text.toLowerCase().contains("z" + (i+1))) 
 			{
 				System.out.println("Error: Unable to parse function Z" + (i+1));
 				f.clear();
 				f.setDraw(false);
 				functions2var.add(f);
 				parseresult.setState(ParseResultIcon.State.ERROR);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 			}
 			if(text.isEmpty()) {
 				f.setDraw(false);
 				functions2var.add(f);
 				//System.out.println("Z" + (i+1) + " is empty. Skipping.");
 				parseresult.setState(ParseResultIcon.State.EMPTY);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 			}
 			empty = false;
 			if(!f.Parse(text))
 			{
 				System.out.println("Error: Unable to parse function Z" + (i+1));
 				f.clear();
 				f.setDraw(false);
 				functions2var.add(f);
 				parseresult.setState(ParseResultIcon.State.ERROR);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 				
 			}
 			Color c = labels.get(i).getBackground();
 			f.setColor(c);
 			f.setDraw(checkboxes.get(i).isSelected());
 			functions2var.add(f);
 			parseresult.setState(ParseResultIcon.State.OK);
 			parseresults.set(i, parseresult);
 			System.out.println("Added function Z" + (i+1) + " with color " + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
 			if(doProgressBar)
 			{
 				progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 				progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 				progressPanel.paintAll(progressPanel.getGraphics());
 			}
 		}
 		progressbar.setValue(100);
 		progressbar.setString(String.format(Translate("message.progressbar.done"), progressbar.getValue()));
 		progressPanel.paintAll(progressPanel.getGraphics());
 		
 		
 		g3d.Update(functions2var);
 		
 		this.repaint();
 		
 		/*
 		if(calcframe != null)
 		{
 			calcframe.Update();
 		}
 		*/
 		System.out.println("Done");
 	}
 	private void Render2D()
 	{
 		System.out.println("Parsing functions...");
 		int progstart = progressbar.getValue();
 		functions.clear();
 		for(int i=0;i<GraphFunctionsFrame.MaxFunctions;i++)
 		{
 			String text = Util.removeWhiteSpace(textfields.get(i).getText().trim());
 			Function f = new Function();
 			ParseResultIcon parseresult = parseresults.get(i);
 			if(text.toLowerCase().contains("y" + (i+1))) 
 			{
 				System.out.println("Error: Unable to parse function Y" + (i+1));
 				f.clear();
 				f.setDraw(false);
 				functions.add(f);
 				parseresult.setState(ParseResultIcon.State.ERROR);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 			}
 			if(text.isEmpty()) {
 				f.setDraw(false);
 				functions.add(f);
 				//System.out.println("Y" + (i+1) + " is empty. Skipping.");
 				parseresult.setState(ParseResultIcon.State.EMPTY);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 			}
 			empty = false;
 			if(!f.Parse(text))
 			{
 				System.out.println("Error: Unable to parse function Y" + (i+1));
 				f.clear();
 				f.setDraw(false);
 				functions.add(f);
 				parseresult.setState(ParseResultIcon.State.ERROR);
 				parseresults.set(i, parseresult);
 				if(doProgressBar)
 				{
 					progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 					progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 					progressPanel.paintAll(progressPanel.getGraphics());
 				}
 				continue;
 				
 			}
 			Color c = labels.get(i).getBackground();
 			f.setColor(c);
 			f.setDraw(checkboxes.get(i).isSelected());
 			functions.add(f);
 			parseresult.setState(ParseResultIcon.State.OK);
 			parseresults.set(i, parseresult);
 			System.out.println("Added function Y" + (i+1) + " with color " + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
 			if(doProgressBar)
 			{
 				progressbar.setValue(progstart + (int) (((double)(i+1)/(double)MaxFunctions) * (100-progstart)));
 				progressbar.setString(String.format(Translate("message.progressbar.drawingfunctions"), progressbar.getValue()));
 				progressPanel.paintAll(progressPanel.getGraphics());
 			}
 		}
 		progressbar.setValue(100);
 		progressbar.setString(String.format(Translate("message.progressbar.done"), progressbar.getValue()));
 		progressPanel.paintAll(progressPanel.getGraphics());
 		gframe.Update(functions);
 		this.repaint();
 		if(calcframe != null)
 		{
 			calcframe.Update();
 		}
 		System.out.println("Done");
 	}
 
 	
 	public static void main(String[] args)
 	{
 		System.out.println("Graph v" + GraphFunctionsFrame.version);
 		//System.out.println((new PointXYZ(1, 2, 3)).toString());
 		GraphFunctionsFrame.funcframe = new GraphFunctionsFrame(false);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 	
 		if(e.getSource() instanceof JButton || e.getSource() instanceof JMenuItem)
 		{
 			
 			String buttonname = e.getActionCommand();
 			if(buttonname.equalsIgnoreCase(buttons[0])) //render
 			{
 				Render();
 			}
 			else if(buttonname.equalsIgnoreCase(buttons[1])) //Special chars
 			{
 				if(charframe == null)
 					charframe = new SpecialCharsFrame(this.getLocation());
 				else 
 					charframe.Restore();
 			}
 			
 			else if(buttonname.equalsIgnoreCase(FileMenuStrings[0])) //new
 			{
 				System.out.println("Creating new file...");
 				if(!FileSaved && !empty)
 				{
 					if(!ConfirmFileChanges())
 					{
 						System.out.println("New file canceled by user");
 						return;
 					}
 				}
 				ClearAll();
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase(FileMenuStrings[1]))
 			{
 				System.out.println("Opening file...");
 				if(!FileSaved && !empty)
 				{
 					if(!ConfirmFileChanges())
 					{
 						System.out.println("Canceled by user");
 						return;
 					}
 				}
 				ShowOpenFileDialog();
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase(FileMenuStrings[2]))
 			{
 				if(FilePathPresent)
 				{
 					SaveFile();
 				}
 				else
 				{
 					System.out.println("Saving file");
 					SaveFileAs();
 				}
 				UpdateTitle();
 			}
 			else if(buttonname.equalsIgnoreCase(FileMenuStrings[3]))
 			{
 				System.out.println("Saving file");
 				SaveFileAs();
 				
 				UpdateTitle();
 			}
 			
 			else if (buttonname.equalsIgnoreCase(FileMenuStrings[4])) 
 			{
 				if(settingsframe == null)
 				{
 					settingsframe = new SettingsFrame(this.getLocation());
 				}
 				else
 					settingsframe.setVisible(true);
 			}
 			
 			else if (buttonname.equalsIgnoreCase(FileMenuStrings[5])) 
 			{
 				windowClosing(null);
 			}	
 			//else if(Util.StringArrayGetIndex(calcMenuStrings, language.getName(buttonname).substring(0, 1).toUpperCase() + language.getName(buttonname).substring(1)) != -1)
 			else if(Util.StringArrayGetIndex(calcMenuStrings, buttonname) != -1)
 			{
 
 				//switch(Util.StringArrayGetIndex(calcMenuStrings, language.getName(buttonname).substring(0, 1).toUpperCase() + language.getName(buttonname).substring(1)))
 				switch(Util.StringArrayGetIndex(calcMenuStrings, buttonname))
 				{
 					case 0: // value
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.VALUE);
 						break;
 					}
 					case 1: // zero
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.ZERO);
 						break;
 					}
 					case 2: // minimum
 						{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.MINIMUM);
 						break;
 					}
 					case 3: // maximum
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.MAXIMUM);
 						break;
 					}
 					case 4: // intersect
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.INTERSECT);
 						break;
 					}
 					case 5: //  dy/dx
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.DYDX);
 						break;
 					}
 					case 6: // integral
 					{
 						calcframe = new CalculateFrame(CalculateFrame.Calculation.INTEGRAL);
 						break;
 					}
 					default:
 					{
 						return;
 					}
 				}
 	
 				gframe.setCalcPanel(calcframe);
 				gframe.setCalcPanelVisible(true);
 			}
 			else if(buttonname.equalsIgnoreCase("y=f(x)"))
 			{
 				System.out.println("Toggling 3D to OFF");
 				enable3d = false;
 				Toggle3D();
 			}
 			else if(buttonname.startsWith("z=f(x,y)"))
 			{
 				/*
 				JRadioButtonMenuItem cbItem = (JRadioButtonMenuItem) e.getSource();
 				if(cbItem.isSelected())
 				{
 					System.out.println("Toggling 3D to ON");
 					enable3d = true;
 				}
 				else
 				{
 					System.out.println("Toggling 3D to OFF");
 					enable3d = false;
 				}
 				Toggle3D();
 				*/
 				System.out.println("Toggling 3D to ON");
 				enable3d = true;
 				Toggle3D();
 			}
 		}
 		else if(e.getSource() instanceof JCheckBox)
 		{
 			JCheckBox source = (JCheckBox) e.getSource();
 			int index = Integer.parseInt(source.getName().substring(1));
 			if(!textfields.get(index).getText().trim().isEmpty()) 
 				Render();
 		}
 		
 		
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyChar() == KeyEvent.VK_ENTER)
 		{
 			Render();
 		}
 		
 	}
 
 	@Override
 	 public void mouseReleased(MouseEvent e)  
 	    {  
 	        JLabel label = ((JLabel)e.getSource());
 	        //label.setBackground(JColorChooser.showDialog(null, "Choose function color", label.getBackground()));
 	        label.setBackground(ChooseColor(null, Translate("color.choosecolor"), label.getBackground()));
 	        int funcindex;
 	        String functext = label.getText().substring(1, label.getText().length()-3);
 	        funcindex = Integer.parseInt(functext.trim()) - 1;
 	        Color c = label.getBackground();
 			Function f = functions.get(funcindex);
 			f.setColor(c);
 			functions.set(funcindex, f);
 	        gframe.Update(funcindex, functions.get(funcindex));
 	    }  
 	
 	@Override
 	public void keyTyped(KeyEvent e) 
 	{
 		if((e.getKeyChar() >= 'a' && e.getKeyChar() <= 'z') ||(e.getKeyChar() >= 'A' && e.getKeyChar() <= 'Z') || (e.getKeyChar() >= '0' && e.getKeyChar() <= '9') || e.getKeyChar() == '(' || e.getKeyChar() == ')' || e.getKeyChar() == '^' || e.getKeyChar() == ' ' || e.getKeyChar() == 0x08)
 		{
 			FileSaved = false;
 			empty = false;
 			UpdateTitle();
 		}
 	}	
 	
 	@Override
 	public void windowClosing(WindowEvent e) {
 		if(!FileSaved && !empty)
 		{
 			if(ConfirmFileChanges()) System.exit(0);
 		}
 		else
 			System.exit(0);
 	}
 	
 	
 	//Unused
 	@Override
 	public void keyReleased(KeyEvent e) {}
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 	@Override
 	public void mousePressed(MouseEvent arg0) {}
 	@Override
 	public void windowActivated(WindowEvent arg0) {}
 	@Override
 	public void windowClosed(WindowEvent arg0){}
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {}
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {}
 	@Override
 	public void windowIconified(WindowEvent arg0) {}
 	@Override
 	public void windowOpened(WindowEvent arg0) {}
 	
 }
