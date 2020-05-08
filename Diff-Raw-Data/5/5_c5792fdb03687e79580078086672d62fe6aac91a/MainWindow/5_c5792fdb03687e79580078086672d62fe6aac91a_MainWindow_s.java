 import java.awt.AlphaComposite;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import layout.TableLayout;
 import Images.Misc.CharacterMenu;
 import Images.Misc.Controls;
 import Images.Misc.Icon;
 import Sound.MP3;
 
 
 public class MainWindow extends JFrame{
 
 	private static final long serialVersionUID = 304199376093554707L;
 	final JFrame parent = this;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.gameinit();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void gameinit()
 	{
 		
 		JFrame window = new JFrame("Zauber");
 		window.setLayout(new BorderLayout());
 		window.add(new GamePanel(window), BorderLayout.CENTER);
 		window.setSize(800, 600);
 		window.setResizable(false);
 		window.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		window.setIconImage(new Icon().getImage());
 		
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		int w = window.getSize().width;
 		int h = window.getSize().height;
 		int x = (dim.width-w)/2;
 		int y = (dim.height-h)/2;
 		window.setLocation(x, y);
 		
 		//window.setUndecorated(true);
 		
 		window.setVisible(true);
 	}
 
 }
 
 
 
 
 @SuppressWarnings("serial")
 class GamePanel extends JPanel implements KeyListener{
 	private final JTextField textField = new JTextField();;
 	public synchronized JTextField getTextField() {
 		return textField;
 	}
 
 	private final GameText gametext = new GameText(this);
 	private final AnimationPanel animpanel = new AnimationPanel();
 	private final GameData gamedata = new GameData(gametext, animpanel, this);
 	private final ScreenPanel screen = new ScreenPanel(gamedata, gametext,
 			this, textField);
 	private final JPanel screenPanel = new JPanel();
 	JFrame window;
 	private final GamePanel gamepanel = this;
 
 	final JTextPane textArea = new JTextPane();
 	MessageConsole console = new MessageConsole(textArea, true);
 
 	public String state = "MainMenu";
 
 	/**
 	 * Create the panel.
 	 */
 	public GamePanel(final JFrame window) {
 		this.window = window;
 
 		this.addKeyListener(this);
 
 		mainMenu();
 	}
 
 	public void characterPanel() {
 
 		state = "Character";
 
 		this.setFocusable(true);
 		this.requestFocusInWindow();
 
 		textField.setEnabled(false);
 		screenPanel.removeAll();
 		screenPanel.setLayout(new BorderLayout(0, 0));
 
 		screenPanel.add(animpanel, BorderLayout.SOUTH);
 		CharacterPanel menu = new CharacterPanel(this, gamedata);
 		screenPanel.add(menu);
 		screenPanel.revalidate();
 		screenPanel.repaint();
 		
 		gamedata.setDialogue(true);
 
 	}
 
 	final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
 	final int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(10);
 
 	public void formPanel() {
 
 
 		ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
 		ToolTipManager.sharedInstance().setInitialDelay(0);
 
 		state = "Form";
 
 		this.setFocusable(true);
 		this.requestFocusInWindow();
 
 		textField.setEnabled(false);
 		screenPanel.removeAll();
 		screenPanel.setLayout(new BorderLayout(0, 0));
 
 		screenPanel.add(animpanel, BorderLayout.SOUTH);
 		FormPanel menu = new FormPanel(this, gamedata);
 		screenPanel.add(menu);
 		screenPanel.revalidate();
 		screenPanel.repaint();
 		
 		gamedata.setDialogue(true);
 	}
 
 	public void notesPanel() {
 
 		state = "Notes";
 
 		this.setFocusable(true);
 		this.requestFocusInWindow();
 
 		textField.setEnabled(false);
 		screenPanel.removeAll();
 		screenPanel.setLayout(new BorderLayout(0, 0));
 
 		screenPanel.add(animpanel, BorderLayout.SOUTH);
 		NotesPanel notes = new NotesPanel();
 		screenPanel.add(notes);
 		screenPanel.revalidate();
 		screenPanel.repaint();
 
 		gamedata.setDialogue(true);
 	}
 
 	public void gameOver() {
 		gamedata.setDialogue(true);
 		screenPanel.removeAll();
 
 		JPanel panel = new JPanel();
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 70, 325, 58, 0 };
 		gridBagLayout.rowHeights = new int[] { 63, 173, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		panel.setLayout(gridBagLayout);
 
 		JLabel lblYouDied = new JLabel("You Died");
 		lblYouDied.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 40));
 		GridBagConstraints gbc_lblYouDied = new GridBagConstraints();
 		gbc_lblYouDied.insets = new Insets(0, 0, 5, 5);
 		gbc_lblYouDied.gridx = 1;
 		gbc_lblYouDied.gridy = 1;
 		panel.add(lblYouDied, gbc_lblYouDied);
 
 		JButton btnMainMenu = new JButton("Main Menu");
 		btnMainMenu.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 			}
 		});
 		GridBagConstraints gbc_btnMainMenu = new GridBagConstraints();
 		gbc_btnMainMenu.insets = new Insets(0, 0, 0, 5);
 		gbc_btnMainMenu.gridx = 1;
 		gbc_btnMainMenu.gridy = 2;
 		panel.add(btnMainMenu, gbc_btnMainMenu);
 
 		screenPanel.add(panel);
 		screenPanel.revalidate();
 		screenPanel.repaint();
 	}
 
 	public void newGame() {
 		this.removeAll();
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 109, 238, 107, 0 };
 		gridBagLayout.rowHeights = new int[] { 153, 28, 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0,
 				Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0,
 				Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		JLabel lblPleaseChooseA = new JLabel(
 				"Please Choose a Name for this Game Session:");
 		GridBagConstraints gbc_lblPleaseChooseA = new GridBagConstraints();
 		gbc_lblPleaseChooseA.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPleaseChooseA.gridx = 1;
 		gbc_lblPleaseChooseA.gridy = 1;
 		add(lblPleaseChooseA, gbc_lblPleaseChooseA);
 
 		final JTextField textField1 = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 5, 5);
 		gbc_textField.fill = GridBagConstraints.BOTH;
 		gbc_textField.gridx = 1;
 		gbc_textField.gridy = 2;
 		add(textField1, gbc_textField);
 		textField1.setColumns(10);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 0, 5);
 		gbc_panel.fill = GridBagConstraints.VERTICAL;
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 3;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 100, 100, 0 };
 		gbl_panel.rowHeights = new int[] { 0, 0 };
 		gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
 		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
 		panel.setLayout(gbl_panel);
 
 		JButton btnAccept = new JButton("Accept");
 		btnAccept.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if ((textField1.getText() != null)
 						&& (textField1.getText().trim() != "") && (textField1.getText().trim().length() != 0)) {
 					if (new File("GameData/Saves/" + textField1.getText()
 							+ ".sav").exists()) {
 						int n = JOptionPane
 								.showConfirmDialog(
 										window,
 										"The session already exists. Would you like to overwrite it?",
 										"", JOptionPane.YES_NO_OPTION);
 						if (n == JOptionPane.YES_OPTION) {
 							new File("GameData/Saves/" + textField1.getText()
 									+ ".sav").delete();
 							GameData.sessionName = "GameData/Saves/"
 									+ textField1.getText();
 							QuestList.startGame();
 							GLOBAL.resetAll();
 							gamepanel.createGame();
 						} else {
 							return;
 						}
 					}
 					GameData.sessionName = "GameData/Saves/"
 							+ textField1.getText();
 					QuestList.startGame();
 					GLOBAL.resetAll();
 					gamepanel.createGame();
 				}
 				else
 				{
 
 				}
 			}
 		});
 		GridBagConstraints gbc_btnAccept = new GridBagConstraints();
 		gbc_btnAccept.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnAccept.insets = new Insets(0, 0, 0, 5);
 		gbc_btnAccept.anchor = GridBagConstraints.NORTH;
 		gbc_btnAccept.gridx = 0;
 		gbc_btnAccept.gridy = 0;
 		panel.add(btnAccept, gbc_btnAccept);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 
 			}
 
 		});
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnCancel.gridx = 1;
 		gbc_btnCancel.gridy = 0;
 		panel.add(btnCancel, gbc_btnCancel);
 
 		revalidate();
 		repaint();
 	}
 
 	public void mainMenu() {
 		
 		state = "MainMenu";
 		
 		gamedata.getPlayer().setCurrentLevel("default");
 		gamedata.loadLevel();
 
 		this.removeAll();
 		GridBagLayout gridBagLayout_1 = new GridBagLayout();
 		gridBagLayout_1.columnWidths = new int[]{447, 0};
 		gridBagLayout_1.rowHeights = new int[]{298, 0};
 		gridBagLayout_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout_1);
 
 		JPanel panel = new JPanel();
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 127, 210, 110, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0,
 				Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0, 0.0, Double.MIN_VALUE };
 		panel.setLayout(gridBagLayout);
 
 		JLabel lblZauber = new JLabel("Zauber");
 		lblZauber.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 99));
 		GridBagConstraints gbc_lblZauber = new GridBagConstraints();
 		gbc_lblZauber.fill = GridBagConstraints.VERTICAL;
 		gbc_lblZauber.gridwidth = 3;
 		gbc_lblZauber.insets = new Insets(0, 0, 5, 0);
 		gbc_lblZauber.gridx = 0;
 		gbc_lblZauber.gridy = 0;
 		panel.add(lblZauber, gbc_lblZauber);
 
 		JButton btnNewGame = new JButton("New Game");
 		btnNewGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				newGame();
 
 			}
 
 		});
 		GridBagConstraints gbc_btnNewGame = new GridBagConstraints();
 		gbc_btnNewGame.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnNewGame.insets = new Insets(0, 0, 5, 5);
 		gbc_btnNewGame.gridx = 1;
 		gbc_btnNewGame.gridy = 2;
 		panel.add(btnNewGame, gbc_btnNewGame);
 
 		JButton btnLoadGame = new JButton("Load Game");
 		btnLoadGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loadGame();
 
 			}
 
 		});
 		GridBagConstraints gbc_btnLoadGame = new GridBagConstraints();
 		gbc_btnLoadGame.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnLoadGame.insets = new Insets(0, 0, 5, 5);
 		gbc_btnLoadGame.gridx = 1;
 		gbc_btnLoadGame.gridy = 3;
 		panel.add(btnLoadGame, gbc_btnLoadGame);
 
 		JButton btnOptions = new JButton("Options");
 		btnOptions.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addOptions();
 			}
 
 		});
 		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
 		gbc_btnOptions.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnOptions.insets = new Insets(0, 0, 5, 5);
 		gbc_btnOptions.gridx = 1;
 		gbc_btnOptions.gridy = 4;
 		panel.add(btnOptions, gbc_btnOptions);
 
 		JButton btnTutorial = new JButton("Tutorial");
 		btnTutorial.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 				createTutorial();
 			}
 		});
 		GridBagConstraints gbc_btnTutorial = new GridBagConstraints();
 		gbc_btnTutorial.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnTutorial.insets = new Insets(0, 0, 5, 5);
 		gbc_btnTutorial.gridx = 1;
 		gbc_btnTutorial.gridy = 5;
 		panel.add(btnTutorial, gbc_btnTutorial);
 
 		JButton btnCredits = new JButton("Credits");
 		btnCredits.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Credits();
 			}
 			
 		});
 		GridBagConstraints gbc_btnCredits = new GridBagConstraints();
 		gbc_btnCredits.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnCredits.insets = new Insets(0, 0, 5, 5);
 		gbc_btnCredits.gridx = 1;
 		gbc_btnCredits.gridy = 6;
 		panel.add(btnCredits, gbc_btnCredits);
 
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		GridBagConstraints gbc_btnQuit = new GridBagConstraints();
 		gbc_btnQuit.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnQuit.insets = new Insets(0, 0, 0, 5);
 		gbc_btnQuit.gridx = 1;
 		gbc_btnQuit.gridy = 7;
 		panel.add(btnQuit, gbc_btnQuit);
 
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		this.add(panel, gbc_panel);
 
 		JLabel version = new JLabel("Version 0.2a - PreAlpha");
 		gbc_panel.gridy = 1;
 		gbc_panel.weightx = 1;
 		gbc_panel.anchor = GridBagConstraints.LINE_START;
 		gbc_panel.insets = new Insets(5,5,5,5);
 		
 		this.add(version, gbc_panel);
 		
 		revalidate();
 		repaint();
 	}
 	
 	public void createTutorial()
 	{
 		this.removeAll();
 
 		console.clear();
 
 		animpanel.bindgame(gamedata, this);
 		gametext.bindGameData(gamedata);
 		gamedata.bindScreen(screen);
 
 		setForeground(Color.BLACK);
 		setBackground(Color.BLACK);
 
 		double[][] size = {{0.8, 0.2},{TableLayout.FILL}};
 		setLayout(new TableLayout(size));
 		
 		screenPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
 				Color.WHITE, Color.BLACK));
 
 		add(screenPanel, "0,0");
 		addScreen();
 
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.BLACK);
 
 		add(panel, "1,0");
 
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 86, 86, 0 };
 		gbl_panel.rowHeights = new int[] { 22, 20, 0, 0 };
 		gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
 		gbl_panel.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
 		panel.setLayout(gbl_panel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridwidth = 2;
 		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 1;
 		panel.add(scrollPane, gbc_scrollPane);
 
 		console.redirectOut();
 		console.setMessageLines(250);
 		textArea.setEditable(false);
 		textArea.setForeground(Color.WHITE);
 		textArea.setBackground(Color.DARK_GRAY);
 		scrollPane.setViewportView(textArea);
 		textArea.setFocusable(false);
 
 		textField.setForeground(Color.WHITE);
 		textField.setBackground(Color.DARK_GRAY);
 		textField.setColumns(10);
 		textField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				gamepanel.activateTextField();
 			}
 		});
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.gridwidth = 2;
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.anchor = GridBagConstraints.NORTH;
 		gbc_textField.gridx = 0;
 		gbc_textField.gridy = 2;
 		gbc_textField.insets = new Insets(5, 5, 5, 5);
 		panel.add(textField, gbc_textField);
 
 		revalidate();
 		repaint();
 		
 		gamedata.setPlayer(new Player("@", Color.GREEN, gamedata, gametext, "You", new int[]{0,0}, 100, 100, 1, 1));
 
 		GameData.sessionName = "Tutorial@Me-65gd5duoiyd6d";
 		gamedata.getPlayer().setCurrentLevel("Tutorial");
 		QuestList.getQuestList().clear();
 		QuestList.getQuestList().put("TutorialQuest", new TutorialQuest());
 		
 		gamedata.getSeenMaps().clear();
 		gamedata.loadLevel();
 	}
 
 	public void loadGame() {
 		this.removeAll();
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 258, 0 };
 		gridBagLayout.rowHeights = new int[] { 266, 0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0,
 				Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.anchor = GridBagConstraints.EAST;
 		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 1;
 		add(panel_1, gbc_panel_1);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.insets = new Insets(5, 5, 5, 0);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 0;
 		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
 		add(scrollPane, gbc_scrollPane);
 		gbc_scrollPane.gridy = 0;
 
 		JPanel p = new JPanel();
 		p.setLayout(new GridLayout(0, 1, 0, 0));
 
 		JPanel panel = new JPanel();
 
 		p.add(panel);
 
 		scrollPane.setViewportView(p);
 		panel.setLayout(new GridBagLayout());
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets(5,5,5,5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.weightx = 1;
 		c.weighty = 1;
 
 
 		String path = "GameData/Saves/";
 
 		String files;
 		File folder = new File(path);
 		final File[] listOfFiles = folder.listFiles();
 		
 		Arrays.sort(listOfFiles, new Comparator<File>(){
 		    public int compare(File f1, File f2)
 		    {
 		        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
 		    } });
 
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 
 		DefaultListModel list = new DefaultListModel();
 
 		for (int i = 0; i < listOfFiles.length; i++) {
 
 			if (listOfFiles[i].isFile()) {
 				files = listOfFiles[i].getName();
 				final String file = files.substring(0, files.length() - 4);
 				list.addElement("<html><table width=\"700\"><tr><td>" + file + "</td><td align = \"right\">" + " Last Saved at " + sdf.format(listOfFiles[i].lastModified()) + "</td></tr></table></html>");
 			}
 		}
 
 		final JList jlist = new JList(list);
 
 		DefaultListCellRenderer renderer =  
 				(DefaultListCellRenderer)jlist.getCellRenderer();  
 		renderer.setHorizontalAlignment(JLabel.LEFT); 
 
 		panel.add(jlist, c);
 
 		JButton btnLoad = new JButton("Load");
 		btnLoad.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = jlist.getSelectedIndex();
 				if (index > -1)
 				{
 					String files = listOfFiles[index].getName();
 					final String file = files.substring(0, files.length() - 4);
 
 					GameData.sessionName = "GameData/Saves/" + file;
 					createGame();
 				}
 			}	
 		});
 		panel_1.add(btnLoad);
 
 		JButton btnDelete = new JButton("Delete");
 		btnDelete.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = jlist.getSelectedIndex();
 				if (index > -1)
 				{
 					int n = JOptionPane.showConfirmDialog(
 							window,
 							"Are you sure you want to delete this save?",
 							"",
 							JOptionPane.YES_NO_OPTION);
 					if (n == JOptionPane.YES_OPTION)
 					{
 						listOfFiles[index].delete();
 						loadGame();
 					}
 				}
 			}	
 		});
 		panel_1.add(btnDelete);
 
 		JButton cancl = new JButton("Cancel");
 		panel_1.add(cancl);
 		cancl.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 
 			}
 
 		});
 
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void addOptions()
 	{
 		this.removeAll();
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{279, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 69, 0};
 		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 0;
 		panel.add(panel_1, gbc_panel_1);
 
 		JLabel lblScreenResolution = new JLabel("Screen Resolution");
 		panel_1.add(lblScreenResolution);
 
 		final JComboBox comboBox = new JComboBox();
 		panel_1.add(comboBox);
 		comboBox.setName("Screen Resolution");
 		comboBox.setToolTipText("Choose Screen Resolution");
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"300x300", "400x400", "500x500", "600x600", "700x700", "800x800"}));
 		comboBox.setSelectedIndex((gamedata.getResolution()/100)-3);
 
 		final JCheckBox tglbtnFullscreen = new JCheckBox("Fullscreen");
 		if (window.getExtendedState() == JFrame.MAXIMIZED_BOTH)
 		{
 			tglbtnFullscreen.setSelected(true);
 		}
 		GridBagConstraints gbc_tglbtnFullsreen = new GridBagConstraints();
 		gbc_tglbtnFullsreen.insets = new Insets(0, 0, 5, 0);
 		gbc_tglbtnFullsreen.gridx = 0;
 		gbc_tglbtnFullsreen.gridy = 1;
 		panel.add(tglbtnFullscreen, gbc_tglbtnFullsreen);
 		
 		final JCheckBox btnHealth = new JCheckBox("Health Numbers");
 		if (gamedata.getAnimPanel().healthDes)
 		{
 			btnHealth.setSelected(true);
 		}
 		GridBagConstraints gbc_btnHealth = new GridBagConstraints();
 		gbc_btnHealth.insets = new Insets(0, 0, 5, 0);
 		gbc_btnHealth.gridx = 0;
 		gbc_btnHealth.gridy = 2;
 		panel.add(btnHealth, gbc_btnHealth);
 		
 		final JCheckBox btnMana= new JCheckBox("Throat Numbers");
 		if (gamedata.getAnimPanel().manaDes)
 		{
 			btnMana.setSelected(true);
 		}
 		GridBagConstraints gbc_btnMana = new GridBagConstraints();
 		gbc_btnMana.insets = new Insets(0, 0, 5, 0);
 		gbc_btnMana.gridx = 0;
 		gbc_btnMana.gridy = 3;
 		panel.add(btnMana, gbc_btnMana);
 
 		JPanel panel_2 = new JPanel();
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.gridx = 0;
 		gbc_panel_2.gridy = 4;
 		panel.add(panel_2, gbc_panel_2);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[]{121, 65, 136, 83, 0};
 		gbl_panel_2.rowHeights = new int[]{23, 0};
 		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_2.setLayout(gbl_panel_2);
 
 		JButton btnDefaults = new JButton("Defaults");
 		btnDefaults.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				comboBox.setSelectedIndex(3);
 				tglbtnFullscreen.setSelected(false);
 				btnHealth.setSelected(false);
 				btnMana.setSelected(false);
 				revalidate();
 			}
 		});
 		GridBagConstraints gbc_btnDefaults = new GridBagConstraints();
 		gbc_btnDefaults.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnDefaults.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDefaults.gridx = 0;
 		gbc_btnDefaults.gridy = 0;
 		panel_2.add(btnDefaults, gbc_btnDefaults);
 
 		JButton btnApply = new JButton("Apply");
 		btnApply.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				gamedata.setResolution((comboBox.getSelectedIndex()+3)*100);
 				if (tglbtnFullscreen.isSelected())
 				{
 					window.setExtendedState(JFrame.MAXIMIZED_BOTH);
 				}
 				else
 				{
 					window.setExtendedState(JFrame.NORMAL);
 				}
 				gamedata.getAnimPanel().healthDes = btnHealth.isSelected();
 				gamedata.getAnimPanel().manaDes = btnMana.isSelected();
 				mainMenu();
 			}
 		});
 		GridBagConstraints gbc_btnApply = new GridBagConstraints();
 		gbc_btnApply.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
 		gbc_btnApply.gridx = 2;
 		gbc_btnApply.gridy = 0;
 		panel_2.add(btnApply, gbc_btnApply);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 			}
 		});
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnCancel.gridx = 3;
 		gbc_btnCancel.gridy = 0;
 		panel_2.add(btnCancel, gbc_btnCancel);
 
 		gamepanel.requestFocusInWindow();
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void createGame() {
 		this.removeAll();
 
 		console.clear();
 
 		animpanel.bindgame(gamedata, this);
 		gametext.bindGameData(gamedata);
 		gamedata.bindScreen(screen);
 
 		setForeground(Color.BLACK);
 		setBackground(Color.BLACK);
 
 		double[][] size = {{0.8, 0.2},{TableLayout.FILL}};
 		setLayout(new TableLayout(size));
 		
 		screenPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
 				Color.WHITE, Color.BLACK));
 
 		add(screenPanel, "0,0");
 		addScreen();
 
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.BLACK);
 
 		add(panel, "1,0");
 		
 		
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[] { 86, 86, 0 };
 		gbl_panel.rowHeights = new int[] { 22, 20, 0, 0 };
 		gbl_panel.columnWeights = new double[] { 1.0};
 		gbl_panel.rowWeights = new double[] {0.0, 1.0};
 		panel.setLayout(gbl_panel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridwidth = 2;
 		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 1;
 		panel.add(scrollPane, gbc_scrollPane);
 
 		console.redirectOut();
 		console.setMessageLines(250);
 		textArea.setEditable(false);
 		textArea.setForeground(Color.WHITE);
 		textArea.setBackground(Color.DARK_GRAY);
 		scrollPane.setViewportView(textArea);
 		textArea.setFocusable(false);
 
 		textField.setForeground(Color.WHITE);
 		textField.setBackground(Color.DARK_GRAY);
 		textField.setColumns(10);
 		textField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				gamepanel.activateTextField();
 			}
 		});
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.gridwidth = 2;
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.anchor = GridBagConstraints.NORTH;
 		gbc_textField.gridx = 0;
 		gbc_textField.gridy = 2;
 		gbc_textField.insets = new Insets(5, 5, 5, 5);
 		panel.add(textField, gbc_textField);
 
 		revalidate();
 		repaint();
 
 		gamedata.getSeenMaps().clear();
 		gamedata.loadGame();
 	}
 
 	public void activateTextField() {
 		String text = textField.getText();
 		if ((text == null) || (text.trim().length() == 0)) {
 
 		} else {
 			gametext.playerSpeak(text);
 			textField.setText("");
 		}
 		screen.requestFocus();
 	}
 	
 	public void Credits() {
 		this.removeAll();
 		
 		GridBagLayout gridBagLayout = new GridBagLayout();
 //		gridBagLayout.columnWidths = new int[]{0, 0};
 //		gridBagLayout.rowHeights = new int[]{128, 121, 60, 0};
 //		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 //		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel lblSpriteBaseModel = new JLabel("Sprite Base Model: Showkaizer");
 		GridBagConstraints gbc_lblSpriteBaseModel = new GridBagConstraints();
 		gbc_lblSpriteBaseModel.insets = new Insets(0, 0, 5, 0);
 		gbc_lblSpriteBaseModel.gridx = 0;
 		gbc_lblSpriteBaseModel.gridy = 0;
 		add(lblSpriteBaseModel, gbc_lblSpriteBaseModel);
 		
 		JLabel lblLF = new JLabel("Excellent Java L&F: Hans Bickel - Site: www.muntjak.de");
 		GridBagConstraints gbc_lblLF = new GridBagConstraints();
 		gbc_lblLF.insets = new Insets(0, 0, 5, 0);
 		gbc_lblLF.gridx = 0;
 		gbc_lblLF.gridy = 1;
 		add(lblLF, gbc_lblLF);
 		
 		JLabel lblSC = new JLabel("Shadow Casting Algorithm Logic: Eric Lippert - Site: http://blogs.msdn.com/b/ericlippert/archive/2011/12/12/shadowcasting-in-c-part-one.aspx");
 		GridBagConstraints gbc_lblSC = new GridBagConstraints();
 		gbc_lblSC.insets = new Insets(0, 0, 5, 0);
 		gbc_lblSC.gridx = 0;
 		gbc_lblSC.gridy = 2;
 		add(lblSC, gbc_lblSC);
 		
 		JLabel lblS = new JLabel("Amazing File to Java converter: Stephen G. Ware - sgware@gmail.com");
 		GridBagConstraints gbc_lblS = new GridBagConstraints();
 		gbc_lblS.insets = new Insets(0, 0, 5, 0);
 		gbc_lblS.gridx = 0;
 		gbc_lblS.gridy = 3;
 		add(lblS, gbc_lblS);
 		
 		JLabel lblSs = new JLabel("Great MP3 library - JLayer - http://www.javazoom.net/javalayer/javalayer.html");
 		GridBagConstraints gbc_lblSs = new GridBagConstraints();
 		gbc_lblSs.insets = new Insets(0, 0, 5, 0);
 		gbc_lblSs.gridx = 0;
 		gbc_lblSs.gridy = 4;
 		add(lblSs, gbc_lblSs);
 		
 		JLabel lblEverythingElseMe = new JLabel("Everything Else: Me (Infinity8)");
 		GridBagConstraints gbc_lblEverythingElseMe = new GridBagConstraints();
 		gbc_lblEverythingElseMe.insets = new Insets(0, 0, 5, 0);
 		gbc_lblEverythingElseMe.gridx = 0;
 		gbc_lblEverythingElseMe.gridy = 5;
 		add(lblEverythingElseMe, gbc_lblEverythingElseMe);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 6;
 		add(panel, gbc_panel);
 		
 		JButton btnBackToMain = new JButton("Back to Main Menu");
 		btnBackToMain.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				mainMenu();
 			}
 			
 		});
 		panel.add(btnBackToMain);
 
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void addScreen() {
 
 		ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
 
 		state = "Screen";
 
 		textField.setEnabled(true);
 		screenPanel.removeAll();
 		screenPanel.setLayout(new BorderLayout(0, 0));
 
 		screenPanel.add(screen);
 
 		screenPanel.add(animpanel, BorderLayout.SOUTH);
 
 		screenPanel.revalidate();
 		screenPanel.repaint();
 		screen.requestFocusInWindow();
 		
 		gamedata.setDialogue(false);
 	}
 
 	public void addMenu() {
 
 		ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
 
 		this.setFocusable(true);
 		this.requestFocusInWindow();
 
 		state = "GameMenu";
 
 		textField.setEnabled(false);
 		screenPanel.removeAll();
 		screenPanel.setLayout(new BorderLayout(0, 0));
 
 		screenPanel.add(animpanel, BorderLayout.SOUTH);
 		MenuPanel menu = new MenuPanel(this, gamedata);
 		menu.bindWindow(window);
 		screenPanel.add(menu);
 		screenPanel.revalidate();
 		screenPanel.repaint();
 		
 		gamedata.setDialogue(true);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		ToolTipManager.sharedInstance().setEnabled(false);
 		ToolTipManager.sharedInstance().setEnabled(true);
 		if ((e.getKeyCode() == KeyEvent.VK_ESCAPE) && (!screen.escKey))
 		{
 			if (state.equals("GameMenu"))
 			{
 				addScreen();
 			}
 			else
 			{
 				addMenu();
 			}
 			screen.escKey = true;
 		}
 		else if ((e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) && (!screen.graveKey))
 		{
 			if (state.equals("Character"))
 			{
 				addScreen();
 			}
 			else
 			{
 				characterPanel();
 			}
 			screen.graveKey = true;
 		}
 		else if ((e.getKeyCode() == KeyEvent.VK_SHIFT) && (!screen.shiftKey))
 		{
 			if (state.equals("Notes"))
 			{
 				addScreen();
 			}
 			else
 			{
 				notesPanel();
 			}
 			screen.shiftKey = true;
 		}
 		else if ((e.getKeyCode() == KeyEvent.VK_CONTROL) && (!screen.ctrlKey))
 		{
 			if (state.equals("Form"))
 			{
 				addScreen();
 			}
 			else
 			{
 				formPanel();
 			}
 			screen.ctrlKey = true;
 		}
 
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		screen.escKey = false;
 		screen.graveKey = false;
 		screen.shiftKey = false;
 		screen.ctrlKey = false;
 	}
 }
 
 
 
 
 class AnimationPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8808776660359780858L;
 	private int animSpeed = 150;
 	@SuppressWarnings("unused")
 	private final AnimationThread thread = new AnimationThread(this);
 	private int direction = 0;
 	private int animstage = 0;
 	private boolean playAnim = false;
 	private GameData gamedata;
 	private ImageIcon singleImage;
 	private int drawMode = 0;
 	private GamePanel gamepanel;
 	boolean healthDes = true;
 	boolean manaDes = true;
 	
 	JButton whisper;
 	JButton shout;
 	JButton roar;
 	JButton incant;
 	JButton profane;
 	
 
 	public AnimationPanel()
 	{
 		this.setPreferredSize(new Dimension(600, 125));
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.LINE_START;
 		c.insets = new Insets(5, 5, 5, 5);
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(6, 3, 3, 3));
 		buttonPanel.setOpaque(false);
 
 		buttonPanel.add(new JLabel("Menus"));
 		buttonPanel.add(new JLabel("Actions"));
 		buttonPanel.add(new JLabel("Shouts"));
 		
 		JButton menu = new JButton("Menu");
 		menu.setFocusable(false);
 		menu.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (gamepanel.state.equals("GameMenu"))
 				{
 					gamepanel.addScreen();
 				}
 				else if (!(gamedata.isDialogue()))
 				{
 					setPlayAnim(false);
 					gamepanel.addMenu();
 				}
 				else if (!gamepanel.state.equals("Screen"))
 				{
 					setPlayAnim(false);
 					gamepanel.addMenu();
 				}
 			}
 		});
 		buttonPanel.add(menu);
 
 		JButton up = new JButton("\u02C4");
 		up.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.getPlayer().setDirection(3);
 			}
 			
 		});
 		buttonPanel.add(up);
 		
 
 		whisper = new JButton("W");
 		
 		whisper.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.getScreen().selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamedata.getGametext().whisper();
 				}
 			}
 			
 		});
 		buttonPanel.add(whisper);
 		
 		
 		JButton character = new JButton("Char");
 		character.setFocusable(false);
 		character.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (gamepanel.state.equals("Character"))
 				{
 					gamepanel.addScreen();
 				}
 				else if (!gamedata.isDialogue())
 				{
 					setPlayAnim(false);
 					gamepanel.characterPanel();
 				}
 				else if (!gamepanel.state.equals("Screen"))
 				{
 					setPlayAnim(false);
 					gamepanel.characterPanel();
 				}
 			}
 
 		});
 		buttonPanel.add(character);
 		
 
 		JButton left = new JButton("\u02C2");
 		left.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.getPlayer().setDirection(1);
 			}
 			
 		});
 		buttonPanel.add(left);
 
 		shout = new JButton("S");
 		shout.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.getScreen().selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamedata.getGametext().shout();
 				}
 			}
 			
 		});
 		buttonPanel.add(shout);
 		
 		
 		JButton forms = new JButton("Form");
 		forms.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (gamepanel.state.equals("Form"))
 				{
 					gamepanel.addScreen();
 				}
 				else if(!gamedata.isDialogue())
 				{
 					setPlayAnim(false);
 					gamepanel.formPanel();
 				}
 				else if (!gamepanel.state.equals("Screen"))
 				{
 					setPlayAnim(false);
 					gamepanel.formPanel();
 				}
 			}
 
 		});
 		forms.setFocusable(false);
 		buttonPanel.add(forms);
 
 		JButton wait = new JButton("Wait");
 		wait.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.startTurn(1);
 				}
 			}
 			
 		});
 		buttonPanel.add(wait);
 
 
 		roar = new JButton("R");
 		roar.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.getScreen().selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamedata.getGametext().roar();
 				}
 			}
 			
 		});
 		buttonPanel.add(roar);
 		
 		
 		JButton notes = new JButton("Note");
 		notes.setFocusable(false);
 		notes.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (gamepanel.state.equals("Notes"))
 				{
 					gamepanel.addScreen();
 				}
 				else if(!gamedata.isDialogue())
 				{
 					setPlayAnim(false);
 					gamepanel.notesPanel();
 				}
 				else if (!gamepanel.state.equals("Screen"))
 				{
 					setPlayAnim(false);
 					gamepanel.notesPanel();
 				}
 			}
 
 		});
 		buttonPanel.add(notes);
 		
 		JButton right = new JButton("\u02C3");
 		right.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.getPlayer().setDirection(2);
 			}
 			
 		});
 		buttonPanel.add(right);
 		
 		incant = new JButton("I");
 		incant.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.getScreen().selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamedata.getGametext().incant();
 				}
 			}
 			
 		});
 		buttonPanel.add(incant);
 		
 		
 		buttonPanel.add(new JLabel(""));
 		this.add(buttonPanel, c);
 
 		JButton down = new JButton("\u02C5");
 		down.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.getPlayer().setDirection(0);
 			}
 			
 		});
 		buttonPanel.add(down);
 
 
 		profane = new JButton("P");
 		profane.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!gamedata.isDialogue()) {
 					gamedata.getScreen().selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamedata.getGametext().profane();
 				}
 			}
 			
 		});
 		buttonPanel.add(profane);
 
 	}
 
 	public void bindgame(GameData gamedata, GamePanel gamepanel) {
 		this.gamedata = gamedata;
 		this.gamepanel = gamepanel;
 		
 		makeTooltips();
 	}
 	
 	public void makeTooltips()
 	{
 		whisper.setToolTipText("<html>" + gamedata.getPlayer().getCurrentForm().whisper().getName() + 
 				"<br>" + gamedata.getPlayer().getCurrentForm().whisper().getDescription() + 
 				"<br> Cost: " + gamedata.getPlayer().getCurrentForm().whisper().getManaCost() + "</html>");
 		shout.setToolTipText("<html>" + gamedata.getPlayer().getCurrentForm().shout().getName() + 
 				"<br>" + gamedata.getPlayer().getCurrentForm().shout().getDescription() + 
 				"<br> Cost: " + gamedata.getPlayer().getCurrentForm().shout().getManaCost() + "</html>");
 		roar.setToolTipText("<html>" + gamedata.getPlayer().getCurrentForm().roar().getName() + 
 				"<br>" + gamedata.getPlayer().getCurrentForm().roar().getDescription()  + 
 				"<br> Cost: " + gamedata.getPlayer().getCurrentForm().roar().getManaCost() + "</html>");
 		incant.setToolTipText("<html>" + gamedata.getPlayer().getCurrentForm().incant().getName() + 
 				"<br>" + gamedata.getPlayer().getCurrentForm().incant().getDescription()  + 
 				"<br> Cost: " + gamedata.getPlayer().getCurrentForm().incant().getManaCost() + "</html>");
 		profane.setToolTipText("<html>" + gamedata.getPlayer().getCurrentForm().profane().getName() + 
 				"<br>" + gamedata.getPlayer().getCurrentForm().profane().getDescription()  + 
 				"<br> Cost: " + gamedata.getPlayer().getCurrentForm().profane().getManaCost() + "</html>");
 	}
 
 	public BufferedImage getAnimationImage(BufferedImage spritesheet)
 	{
 		BufferedImage sprite = new BufferedImage(spritesheet.getWidth()/4,
 				spritesheet.getHeight()/4, BufferedImage.TYPE_INT_ARGB);
 
 		Graphics2D i2d = sprite.createGraphics();
 
 		i2d.drawImage(spritesheet, 0, 0, sprite.getWidth(), sprite.getHeight(),
 				(getAnimstage()*(spritesheet.getWidth())/4),
 				(spritesheet.getHeight()/4)*getDirection(),
 				(getAnimstage()+1)*(spritesheet.getWidth()/4),
 				(spritesheet.getHeight()/4)*(getDirection()+1), null);
 
 		return sprite;
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 
 		Graphics2D g2d = (Graphics2D)g;
 
 		g2d.setColor(Color.GREEN);
 		//g2d.drawOval((this.getWidth()/2)-125, 5, 250, this.getHeight()-10);
 
 		if (drawMode == 0)
 		{
 			g2d.drawImage(getAnimationImage(gamedata.getGroundImage().getImage()),
 					(this.getWidth()/2)-25, 20, null);
 			g2d.drawImage(getAnimationImage(gamedata.getPlayer().getSpritesheet().getImage()),
 					(this.getWidth()/2)-25, 10, null);
 			g2d.drawImage(getAnimationImage(gamedata.getPlayer().getCurrentArmour().getSpritesheet().getImage()),
 					(this.getWidth()/2)-25, 10, null);
 			g2d.drawImage(getAnimationImage(gamedata.getPlayer().getCurrentWeapon().getSpritesheet().getImage()),
 					(this.getWidth()/2)-25, 10, null);
 
 			g2d.setColor(Color.GREEN);
 			g2d.drawString("Health:", this.getWidth()-160, 25);
 			
 			if (healthDes)
 				g2d.drawString(gamedata.getPlayer().getHealthDescription(), this.getWidth()-160, 45);
 			else
 				g2d.drawString(gamedata.getPlayer().getHealth() + " / " + gamedata.getPlayer().getMaxHealth(), this.getWidth()-160, 45);
 
 			g2d.drawString("Throat Condition:", this.getWidth()-160, 85);
 			if (manaDes)
 				g2d.drawString(gamedata.getPlayer().getManaDescription(), this.getWidth()-160, 105);
 			else
 				g2d.drawString(gamedata.getPlayer().getMana() + " / " + gamedata.getPlayer().getMaxMana(), this.getWidth()-160, 105);
 			
 		}
 		else if (drawMode == 1)
 		{
 			g2d.drawImage(getAnimationImage(getSingleImage().getImage()),
 					(this.getWidth()/2) -50, 0, null);
 		}
 	}
 
 	private Image getAnimationImage(Image spritesheet) {
 		Image sprite = new BufferedImage(spritesheet.getWidth(null)/4,
 				spritesheet.getHeight(null)/4, BufferedImage.TYPE_INT_ARGB);
 
 		Graphics2D i2d = (Graphics2D) sprite.getGraphics();
 
 		i2d.drawImage(spritesheet, 0, 0, sprite.getWidth(null), sprite.getHeight(null),
 				(getAnimstage()*(spritesheet.getWidth(null))/4),
 				(spritesheet.getHeight(null)/4)*getDirection(),
 				(getAnimstage()+1)*(spritesheet.getWidth(null)/4),
 				(spritesheet.getHeight(null)/4)*(getDirection()+1), null);
 
 		return sprite;
 	}
 
 	public void animate()
 	{
 		setAnimstage(getAnimstage()+1);
 		if (getAnimstage() == 4)
 		{
 			setAnimstage(0);
 		}
 	}
 
 	/**
 	 * @return the playAnim
 	 */
 	public synchronized boolean isPlayAnim() {
 		return playAnim;
 	}
 
 	/**
 	 * @param playAnim the playAnim to set
 	 */
 	public synchronized void setPlayAnim(boolean playAnim) {
 		if (playAnim)
 		{
 			this.playAnim = true;
 		}
 		else
 		{
 			this.playAnim = false;
 			this.animstage = 0;
 			this.repaint();
 		}
 	}
 
 	/**
 	 * @return the animSpeed
 	 */
 	public synchronized int getAnimSpeed() {
 		return animSpeed;
 	}
 
 	/**
 	 * @param animSpeed the animSpeed to set
 	 */
 	public synchronized void setAnimSpeed(int animSpeed) {
 		this.animSpeed = animSpeed;
 	}
 
 	/**
 	 * @return the direction
 	 */
 	public synchronized int getDirection() {
 		return direction;
 	}
 
 	/**
 	 * @param direction the direction to set
 	 */
 	public synchronized void setDirection(int direction) {
 		this.direction = direction;
 		this.repaint();
 	}
 
 	/**
 	 * @return the animstage
 	 */
 	public synchronized int getAnimstage() {
 		return animstage;
 	}
 
 	/**
 	 * @param animstage the animstage to set
 	 */
 	public synchronized void setAnimstage(int animstage) {
 		this.animstage = animstage;
 	}
 
 	public ImageIcon getSingleImage() {
 		return singleImage;
 	}
 
 	public void setSingleImage(ImageIcon file) {
 		this.singleImage = file;
 	}
 
 	public int getDrawMode() {
 		return drawMode;
 	}
 
 	public void setDrawMode(int drawMode) {
 		this.drawMode = drawMode;
 	}
 
 	public void playSpecificAnimation(ImageIcon file, int dir) {
 
 		this.setSingleImage(file);
 		this.setDrawMode(1);
 		gamedata.getPlayer().setDirection(dir);
 	}
 
 
 }
 
 class AnimationThread extends Thread
 {
 	AnimationPanel panel;
 
 	public AnimationThread(AnimationPanel panel)
 	{
 		this.panel = panel;
 		this.setDaemon(true);
 		this.start();
 	}
 
 	public void run()
 	{
 		int i = 0;
 		while(true)
 		{
 			if ((panel.isPlayAnim()) || (panel.getDrawMode() == 1))
 			{
 				for (i = 0; i < 2; i++)
 				{
 					panel.setAnimstage(i);
 					panel.repaint();
 
 					try {
 						Thread.sleep(panel.getAnimSpeed());
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			if ((panel.isPlayAnim()) || (panel.getDrawMode() == 1))
 			{
 				for (i = 2; i < 4; i++)
 				{
 					panel.setAnimstage(i);
 					panel.repaint();
 
 					try {
 						Thread.sleep(panel.getAnimSpeed());
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
			panel.setAnimstage(0);
			if ((panel.isPlayAnim()) || (panel.getDrawMode() == 1))
 			{
 				panel.repaint();
 			}
 		}
 	}
 }
 
 class CharacterPanel extends JPanel {
 
 	private static final long serialVersionUID = 5402095780124871980L;
 	//private GamePanel gamepanel;
 	private GameData gamedata;
 	final CharacterPanel parent = this;
 
 	public CharacterPanel(GamePanel gamepanel, GameData gamedata) {
 		//this.gamepanel = gamepanel;
 		this.gamedata = gamedata;
 
 		createPanel(1);
 	}
 
 	public void createPanel(int tab)
 	{
 		this.removeAll();
 
 		setLayout(new GridLayout(0, 2, 0, 0));
 
 		CharacterDraw c = new CharacterDraw(gamedata);
 		add(c);
 
 		JPanel panel_1 = new JPanel();
 		add(panel_1);
 		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		panel_1.add(tabbedPane);
 
 		JPanel panel_2 = new JPanel();
 		panel_2 = populateWeapons(panel_2);
 		JScrollPane s2 = new JScrollPane(panel_2);
 		tabbedPane.addTab("Weapons", null, s2, null);
 
 		JPanel panel_3 = new JPanel();
 		panel_3 = populateArmours(panel_3);
 		JScrollPane s3 = new JScrollPane(panel_3);
 		tabbedPane.addTab("Armour", null, s3, null);
 
 		JPanel panel_5 = new JPanel();
 		panel_5 = populateAccessories(panel_5);
 		JScrollPane s5 = new JScrollPane(panel_5);
 		tabbedPane.addTab("Accessories", null, s5, null);
 
 		JPanel panel_4 = new JPanel();
 		panel_4 = populateItems(panel_4);
 		JScrollPane s4 = new JScrollPane(panel_4);
 		tabbedPane.addTab("Items", null, s4, null);
 
 		if (tab == 1)
 		{
 			tabbedPane.setSelectedComponent(s2);
 		}
 		else if (tab == 2)
 		{
 			tabbedPane.setSelectedComponent(s3);
 		}
 		else if (tab == 3)
 		{
 			tabbedPane.setSelectedComponent(s5);
 		}
 		else if (tab == 4)
 		{
 			tabbedPane.setSelectedComponent(s4);
 		}
 
 		this.revalidate();
 		this.repaint();
 		gamedata.getPlayer().reloadImages();
 		gamedata.getAnimPanel().repaint();
 	}
 
 	public JPanel populateWeapons(JPanel panel)
 	{
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.insets = new Insets(5, 10, 5, 5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		for (int i = 0; i < Inventory.getWeapons().size(); i++)
 		{
 			inventoryButton l = null;
 			if (i == gamedata.getPlayer().weapon)
 			{
 				l = new inventoryButton(i, "Weapon", Inventory.getWeapons().get(i).getName() + " : EQUIPPED");
 			}
 			else if (Inventory.getWeapons().get(i).getDamage() > gamedata.getPlayer().getCurrentWeapon().getDamage())
 			{
 				l = new inventoryButton(i, "Weapon", Inventory.getWeapons().get(i).getName() + " : BETTER");
 			}
 			else
 			{
 				l = new inventoryButton(i, "Weapon", Inventory.getWeapons().get(i).getName());
 			}
 			l.setHorizontalAlignment(SwingConstants.LEFT);
 			panel.add(l, c);
 			c.gridy = c.gridy + 1;
 		}
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(panel, BorderLayout.NORTH);
 		return p;
 	}
 
 	public JPanel populateArmours(JPanel panel)
 	{
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.insets = new Insets(5, 10, 5, 5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		for (int i = 0; i < Inventory.getArmours().size(); i++)
 		{
 			inventoryButton l = null;
 			if (i == gamedata.getPlayer().armour)
 			{
 				l = new inventoryButton(i, "Armour", Inventory.getArmours().get(i).getName() + " : EQUIPPED");
 			}
 			else if (Inventory.getArmours().get(i).getProtection() > gamedata.getPlayer().getCurrentArmour().getProtection())
 			{
 				l = new inventoryButton(i, "Armour", Inventory.getArmours().get(i).getName() + " : BETTER");
 			}
 			else
 			{
 				l = new inventoryButton(i, "Armour", Inventory.getArmours().get(i).getName());
 			}
 			l.setHorizontalAlignment(SwingConstants.LEFT);
 			panel.add(l, c);
 			c.gridy = c.gridy + 1;
 		}
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(panel, BorderLayout.NORTH);
 		return p;
 	}
 
 	public JPanel populateAccessories(JPanel panel)
 	{
 		ArrayList<inventoryButton> amulets = new ArrayList<inventoryButton>();
 		ArrayList<inventoryButton> offhands = new ArrayList<inventoryButton>();
 		ArrayList<inventoryButton> rings = new ArrayList<inventoryButton>();
 
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.insets = new Insets(5, 10, 5, 5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		for (int i = 0; i < Inventory.getAccessories().size(); i++)
 		{
 			Accessory a = Inventory.getAccessories().get(i);
 			if (a instanceof Amulet)
 			{
 				inventoryButton l = null;
 				if (i == gamedata.getPlayer().amulet)
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName() + " : EQUIPPED");
 				}
 				else
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName());
 				}
 				l.setHorizontalAlignment(SwingConstants.LEFT);
 				amulets.add(l);
 			}
 			else if (a instanceof OffHand)
 			{
 				inventoryButton l = null;
 				if (i == gamedata.getPlayer().offhand)
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName() + " : EQUIPPED");
 				}
 				else
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName());
 				}
 				l.setHorizontalAlignment(SwingConstants.LEFT);
 				offhands.add(l);
 			}
 			else if (a instanceof Ring)
 			{
 				inventoryButton l = null;
 				if ((i == gamedata.getPlayer().rings[0]) || (i == gamedata.getPlayer().rings[1])
 						|| (i == gamedata.getPlayer().rings[2]) || (i == gamedata.getPlayer().rings[3]))
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName() + " : EQUIPPED");
 				}
 				else
 				{
 					l = new inventoryButton(i, "Accessory", Inventory.getAccessories().get(i).getName());
 				}
 				l.setHorizontalAlignment(SwingConstants.LEFT);
 				rings.add(l);
 			}
 		}
 
 		panel.add(new JLabel("Amulets"), c);
 		c.gridy = c.gridy + 1;
 
 		for (inventoryButton l : amulets)
 		{
 			panel.add(l, c);
 			c.gridy++;
 		}
 
 		panel.add(new JLabel("OffHands"), c);
 		c.gridy = c.gridy + 1;
 
 		for (inventoryButton l : offhands)
 		{
 			panel.add(l, c);
 			c.gridy++;
 		}
 
 		panel.add(new JLabel("Rings"), c);
 		c.gridy = c.gridy + 1;
 
 		for (inventoryButton l : rings)
 		{
 			panel.add(l, c);
 			c.gridy++;
 		}
 
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(panel, BorderLayout.NORTH);
 		return p;
 	}
 
 	public JPanel populateItems(JPanel panel)
 	{
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.insets = new Insets(5, 10, 5, 5);
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		for (int i = 0; i < Inventory.getItems().size(); i++)
 		{
 			inventoryButton l = new inventoryButton(i, "Item", Inventory.getItems().get(i).getName());
 			l.setHorizontalAlignment(SwingConstants.LEFT);
 			panel.add(l, c);
 			c.gridy++;
 		}
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(panel, BorderLayout.NORTH);
 		return p;
 	}
 
 	class inventoryButton extends JButton implements ActionListener
 	{
 		private static final long serialVersionUID = -6951987640826006041L;
 		int index;
 		String type;
 
 		public inventoryButton(int index, String type, String name)
 		{
 			this.index = index;
 			this.type = type;
 			this.setFocusable(false);
 			this.setText(name);
 			this.addActionListener(this);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (type.equals("Weapon"))
 			{
 				JPopupMenu m = new JPopupMenu();
 				if (gamedata.getPlayer().weapon == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().weapon = -1;
 							parent.createPanel(1);
 						}
 
 					});
 					m.add(equip);
 				}
 				else
 				{
 					JMenuItem equip = new JMenuItem("Equip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().weapon = index;
 							parent.createPanel(1);
 						}
 
 					});
 					m.add(equip);
 				}
 				JMenuItem inspect = new JMenuItem("Inspect");
 				inspect.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						gamedata.getGametext().infoMessage(Inventory.getWeapons().get(index).getDescription()
 								+ " Damage: " + Inventory.getWeapons().get(index).getDamage());
 					}
 				});
 				m.add(inspect);
 				JMenuItem drop = new JMenuItem("Drop");
 				drop.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						int val = gamedata.getPlayer().weapon;
 						if (val < index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								Inventory.getWeapons().remove(index);
 								parent.createPanel(1);
 							}
 						}
 						else if (val > index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								gamedata.getPlayer().weapon--;
 								Inventory.getWeapons().remove(index);
 								parent.createPanel(1);
 							}
 						}
 						else if (val == index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								gamedata.getPlayer().weapon = -1;
 								Inventory.getWeapons().remove(index);
 								parent.createPanel(1);
 							}
 						}
 					}
 				});
 				m.add(drop);
 
 				m.show(getParent(), this.getX(), this.getY());
 			}
 			else if (type.equals("Armour"))
 			{
 				JPopupMenu m = new JPopupMenu();
 				if (gamedata.getPlayer().armour == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().armour = -1;
 							parent.createPanel(2);
 						}
 
 					});
 					m.add(equip);
 				}
 				else
 				{
 					JMenuItem equip = new JMenuItem("Equip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().armour = index;
 							parent.createPanel(2);
 						}
 
 					});
 					m.add(equip);
 				}
 				JMenuItem inspect = new JMenuItem("Inspect");
 				inspect.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						gamedata.getGametext().infoMessage(Inventory.getArmours().get(index).getDescription() + 
 								" Defense: " + Inventory.getArmours().get(index).getProtection());
 					}
 				});
 				m.add(inspect);
 				JMenuItem drop = new JMenuItem("Drop");
 				drop.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						int val = gamedata.getPlayer().armour;
 						if (val < index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								Inventory.getArmours().remove(index);
 								parent.createPanel(2);
 							}
 						}
 						else if (val > index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								gamedata.getPlayer().armour--;
 								Inventory.getArmours().remove(index);
 								parent.createPanel(2);
 							}
 						}
 						else if (val == index)
 						{
 							int n = JOptionPane.showConfirmDialog(
 									getParent(),
 									"Destroy Item?",
 									"",
 									JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.YES_OPTION)
 							{
 								gamedata.getPlayer().armour = -1;
 								Inventory.getArmours().remove(index);
 								parent.createPanel(2);
 							}
 						}
 					}
 				});
 				m.add(drop);
 
 				m.show(getParent(), this.getX(), this.getY());
 			}
 			else if (type.equals("Accessory"))
 			{
 				final JPopupMenu m = new JPopupMenu();
 				if (gamedata.getPlayer().amulet == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().amulet = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else if (gamedata.getPlayer().offhand == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().offhand = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else if (gamedata.getPlayer().rings[0] == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().rings[0] = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else if (gamedata.getPlayer().rings[1] == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().rings[1] = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else if (gamedata.getPlayer().rings[2] == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().rings[2] = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else if (gamedata.getPlayer().rings[3] == index)
 				{
 					JMenuItem equip = new JMenuItem("Unequip");
 					equip.addActionListener(new ActionListener(){
 
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 							sound.playOnce();
 							gamedata.getPlayer().rings[3] = -1;
 							parent.createPanel(3);
 						}
 
 					});
 					m.add(equip);
 				}
 				else
 				{
 					if (Inventory.getAccessories().get(index) instanceof Ring)
 					{
 						final JMenu equip = new JMenu("Equip");
 
 
 						JMenuItem ring1 = new JMenuItem("Ring 1");
 						ring1.addActionListener(new ActionListener(){
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 								sound.playOnce();
 								gamedata.getPlayer().rings[0] = index;
 								parent.createPanel(3);
 							}
 						});
 						equip.add(ring1);
 						JMenuItem ring2 = new JMenuItem("Ring 2");
 						ring2.addActionListener(new ActionListener(){
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 								sound.playOnce();
 								gamedata.getPlayer().rings[1] = index;
 								parent.createPanel(3);
 							}
 						});
 						equip.add(ring2);
 						JMenuItem ring3 = new JMenuItem("Ring 3");
 						ring3.addActionListener(new ActionListener(){
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 								sound.playOnce();
 								gamedata.getPlayer().rings[2] = index;
 								parent.createPanel(3);
 							}
 						});
 						equip.add(ring3);
 						JMenuItem ring4 = new JMenuItem("Ring 4");
 						ring4.addActionListener(new ActionListener(){
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 								sound.playOnce();
 								gamedata.getPlayer().rings[3] = index;
 								parent.createPanel(3);
 							}
 						});
 						equip.add(ring4);
 
 						m.add(equip);
 					}
 					else
 					{
 						final JMenuItem equip = new JMenuItem("Equip");
 
 						equip.addActionListener(new ActionListener(){
 
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								MP3 sound = new MP3("GameData/Resources/Sound/SE/Equip.mp3");
 								sound.playOnce();
 								Accessory a = Inventory.getAccessories().get(index);
 								if (a instanceof Amulet)
 								{
 									gamedata.getPlayer().amulet = index;
 									parent.createPanel(3);
 								}
 								else if (a instanceof OffHand)
 								{
 									gamedata.getPlayer().offhand = index;
 									parent.createPanel(3);
 								}
 							}
 
 						});
 						m.add(equip);
 					}
 
 
 				}
 				JMenuItem inspect = new JMenuItem("Inspect");
 				inspect.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						Accessory a = Inventory.getAccessories().get(index);
 						if (a instanceof OffHand)
 						{
 							gamedata.getGametext().infoMessage(a.getDescription() + 
 									" Spell Multiplier: " + ((Amulet) a).getPower());
 						}
 						else if (a instanceof Ring)
 						{
 							gamedata.getGametext().infoMessage(a.getDescription() + 
 									" Ring Power: " + ((Ring) a).getValue());
 						}
 						else if (a instanceof Amulet)
 						{
 							gamedata.getGametext().infoMessage(a.getDescription() + 
 									" Amulet Power: " + ((Amulet) a).getPower());
 						}
 						
 					}
 				});
 				m.add(inspect);
 				JMenuItem drop = new JMenuItem("Drop");
 				drop.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						int n = JOptionPane.showConfirmDialog(
 								getParent(),
 								"Destroy Item?",
 								"",
 								JOptionPane.YES_NO_OPTION);
 						if (n == JOptionPane.YES_OPTION)
 						{
 							Inventory.getAccessories().remove(index);
 
 							int amulet = gamedata.getPlayer().amulet;
 							int offhand = gamedata.getPlayer().offhand;
 							int ring1 = gamedata.getPlayer().rings[0];
 							int ring2 = gamedata.getPlayer().rings[1];
 							int ring3 = gamedata.getPlayer().rings[2];
 							int ring4 = gamedata.getPlayer().rings[3];
 
 							if (amulet == index)
 							{
 								gamedata.getPlayer().amulet = -1;
 							}
 							else if (offhand == index)
 							{
 								gamedata.getPlayer().offhand = -1;
 							}
 							else if (ring1 == index)
 							{
 								gamedata.getPlayer().rings[0] = -1;
 							}
 							else if (ring2 == index)
 							{
 								gamedata.getPlayer().rings[1] = -1;
 							}
 							else if (ring3 == index)
 							{
 								gamedata.getPlayer().rings[2] = -1;
 							}
 							else if (ring4 == index)
 							{
 								gamedata.getPlayer().rings[3] = -1;
 							}
 
 							if (amulet > index)
 							{
 								gamedata.getPlayer().amulet--;
 							}
 							else if (offhand > index)
 							{
 								gamedata.getPlayer().offhand--;
 							}
 							else if (ring1 > index)
 							{
 								gamedata.getPlayer().rings[0]--;
 							}
 							else if (ring2 > index)
 							{
 								gamedata.getPlayer().rings[1]--;
 							}
 							else if (ring3 > index)
 							{
 								gamedata.getPlayer().rings[2]--;
 							}
 							else if (ring4 > index)
 							{
 								gamedata.getPlayer().rings[3]--;
 							}
 
 							parent.createPanel(3);
 						}
 					}
 				});
 				m.add(drop);
 
 				m.show(getParent(), this.getX(), this.getY());
 			}
 			else if (type.equals("Item"))
 			{
 				JPopupMenu m = new JPopupMenu();
 
 				JMenuItem inspect = new JMenuItem("Inspect");
 				inspect.addActionListener(new ActionListener(){
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						gamedata.getGametext().infoMessage(Inventory.getItems().get(index).getDescription());
 					}
 				});
 				m.add(inspect);
 
 				m.show(getParent(), this.getX(), this.getY());
 			}
 		}
 	}
 }
 
 
 class CharacterDraw extends JPanel implements MouseListener, MouseMotionListener
 {
 	private GameData gamedata;
 
 	public CharacterDraw(GameData gamedata)
 	{
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.gamedata = gamedata;
 	}
 
 	private static final long serialVersionUID = -5485625033412149229L;
 
 	public void paintComponent(Graphics g)
 	{
 		final int halfWidth = this.getWidth()/2;
 		final int quarterHeight = this.getHeight()/4;
 
 		final int width100 = this.getWidth()/100;
 		final int height100 = this.getHeight()/100;
 
 		final Font startFont = g.getFont();
 		Font smallFont = null;
 		if (gamedata.getGamepanel().window.getExtendedState() == JFrame.MAXIMIZED_BOTH)
 		{
 			smallFont = new Font(startFont.getFontName(), Font.ITALIC, startFont.getSize());
 		}
 		else
 		{
 			smallFont = new Font(startFont.getFontName(), Font.ITALIC, startFont.getSize()-3);
 		}
 
 		g.drawImage(new CharacterMenu().getImage(), 0, 0, this.getWidth(), this.getHeight(), null);
 
 		g.setColor(Color.GREEN);
 
 		g.drawString("Main Hand", 4*width100, 15*height100 - 6);
 		g.drawRect(4*width100, 15*height100, 8*height100, height100*8);
 		if (gamedata.getPlayer().getWeapon() == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", 5*width100, 20*height100);
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", 5*width100, 20*height100);
 		}
 
 		g.setFont(startFont);
 		g.drawString("Off Hand", this.getWidth() - (10*width100) - 30, 18*height100 - 6);
 		g.drawRect(this.getWidth() - (17*width100), 18*height100, 8*height100, height100*8);
 		if (gamedata.getPlayer().getOffhand() == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", this.getWidth() - (16*width100), 23*height100);
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", this.getWidth() - (16*width100), 23*height100);
 		}
 
 		g.setFont(startFont);
 
 		g.drawString("Body", halfWidth - (16), quarterHeight + (height100*10) - 6);
 		g.drawRect(halfWidth - (4*height100), quarterHeight + (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getArmour() == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", halfWidth - (3*height100), quarterHeight + (height100*15));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", halfWidth - (3*height100), quarterHeight + (height100*15));
 		}
 
 		g.setFont(startFont);
 
 		g.drawRect(width100 * 10, this.getHeight() - (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getRings()[0] == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", width100 * 11, this.getHeight() - (height100*5));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", width100 * 11, this.getHeight() - (height100*5));
 		}
 
 		g.setFont(startFont);
 
 		g.drawString("Rings", width100 * 33, this.getHeight() - (height100*10) - 6);
 		g.drawRect(width100 * 25, this.getHeight() - (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getRings()[1] == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", width100 * 27, this.getHeight() - (height100*5));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", width100 * 27, this.getHeight() - (height100*5));
 		}
 
 		g.setFont(startFont);
 
 		g.drawRect(width100 * 40, this.getHeight() - (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getRings()[2] == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", width100 * 42, this.getHeight() - (height100*5));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", width100 * 42, this.getHeight() - (height100*5));
 		}
 
 		g.setFont(startFont);
 
 		g.drawRect(width100 * 55, this.getHeight() - (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getRings()[3] == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", width100 * 57, this.getHeight() - (height100*5));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", width100 * 57, this.getHeight() - (height100*5));
 		}
 
 		g.setFont(startFont);
 
 		g.drawString("Amulet", width100 * 90, this.getHeight() - (height100*10) - 6);
 		g.drawRect(width100 * 90, this.getHeight() - (height100*10), 8*height100, height100*8);
 		if (gamedata.getPlayer().getAmulet() == -1)
 		{
 			g.setFont(smallFont);
 			g.drawString("Empty", width100 * 92, this.getHeight() - (height100*5));
 		}
 		else
 		{
 			g.setFont(smallFont);
 			g.drawString("Filled", width100 * 92, this.getHeight() - (height100*5));
 		}
 
 		g.setFont(startFont);
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
 	final int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(10); // 10 minutes
 	@Override
 	public void mouseEntered(MouseEvent me) {
 		ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
 		ToolTipManager.sharedInstance().setInitialDelay(0);
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) {
 		ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 
 		final int halfWidth = this.getWidth()/2;
 		final int quarterHeight = this.getHeight()/4;
 
 		final int width100 = this.getWidth()/100;
 		final int height100 = this.getHeight()/100;
 
 		int x = e.getX();
 		int y = e.getY();
 
 		//width100 * 10, this.getHeight() - (height100*10), 8*height100, height100*8
 
 		if ((x > (4*width100)) && (x < (4*width100 + (8*height100)))
 				&& (y > 15*height100) && (y < 23*height100))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getCurrentWeapon().getName() + "<br><br>" +
 					gamedata.getPlayer().getCurrentWeapon().getDescription() + 
 					"<br> Damage: " + gamedata.getPlayer().getCurrentWeapon().getDamage() + "</HTML>");
 		}
 		else if ((x > (halfWidth - (4*height100))) && (x < (halfWidth + (4*height100)))
 				&& (y > (quarterHeight + (height100*10))) && (y < (quarterHeight + (height100*18))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getCurrentArmour().getName() + "<br><br>" +
 					gamedata.getPlayer().getCurrentArmour().getDescription() + 
 					"<br> Defense: " + gamedata.getPlayer().getCurrentArmour().getProtection() + "</HTML>");
 		}
 		else if ((x > (this.getWidth() - (17*width100))) && (x < (this.getWidth() - (17*width100) + (8*height100)))
 				&& (y > (18*height100)) && (y < (26*height100)))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getCurrentOffhand().getName() + "<br><br>" +
 					gamedata.getPlayer().getCurrentOffhand().getDescription() + 
 					"<br> Spell Mulitplier: " + gamedata.getPlayer().getCurrentOffhand().getPower() + "</HTML>");
 		}
 		else if ((x > (width100 * 10)) && (x < (width100 * 10 + (8*height100)))
 				&& (y > (this.getHeight() - (height100*10))) && (y < (this.getHeight() - (height100*2))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getRing(0).getName() + "<br><br>" +
 					gamedata.getPlayer().getRing(0).getDescription() + 
 					"<br> Ring Power: " + gamedata.getPlayer().getRing(0).getValue() + "</HTML>");
 		}
 		else if ((x > (width100 * 25)) && (x < (width100 * 25 + (8*height100)))
 				&& (y > (this.getHeight() - (height100*10))) && (y < (this.getHeight() - (height100*2))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getRing(1).getName() + "<br><br>" +
 					gamedata.getPlayer().getRing(1).getDescription() + 
 					"<br> Ring Power: " + gamedata.getPlayer().getRing(1).getValue() + "</HTML>");
 		}
 		else if ((x > (width100 * 40)) && (x < (width100 * 40 + (8*height100)))
 				&& (y > (this.getHeight() - (height100*10))) && (y < (this.getHeight() - (height100*2))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getRing(2).getName() + "<br><br>" +
 					gamedata.getPlayer().getRing(2).getDescription() + 
 					"<br> Ring Power: " + gamedata.getPlayer().getRing(2).getValue() + "</HTML>");
 		}
 		else if ((x > (width100 * 55)) && (x < (width100 * 55 + (8*height100)))
 				&& (y > (this.getHeight() - (height100*10))) && (y < (this.getHeight() - (height100*2))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getRing(3).getName() + "<br><br>" +
 					gamedata.getPlayer().getRing(3).getDescription() + 
 					"<br> Ring Power: " + gamedata.getPlayer().getRing(3).getValue() +  "</HTML>");
 		}
 		else if ((x > (width100 * 90)) && (x < (width100 * 90 + (8*height100)))
 				&& (y > (this.getHeight() - (height100*10))) && (y < (this.getHeight() - (height100*2))))
 		{
 			setToolTipText("<HTML>" + gamedata.getPlayer().getCurrentAmulet().getName() + "<br><br>" +
 					gamedata.getPlayer().getCurrentAmulet().getDescription() + 
 					"<br> Amulet Power: " + gamedata.getPlayer().getCurrentAmulet().getPower() + "</HTML>");
 		}
 		else
 		{
 			setToolTipText(null);
 		}
 
 	}
 }
 
 class NotesPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5572750865468150264L;
 
 	/**
 	 * Create the panel.
 	 */
 	public NotesPanel() {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 
 		JLabel lblMyNotes = new JLabel("My Notes");
 		GridBagConstraints gbc_lblMyNotes = new GridBagConstraints();
 		gbc_lblMyNotes.insets = new Insets(5, 5, 5, 5);
 		gbc_lblMyNotes.gridx = 0;
 		gbc_lblMyNotes.gridy = 0;
 		add(lblMyNotes, gbc_lblMyNotes);
 
 		JPanel panel = new JPanel();
 		panel = fill(panel);
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 1;
 		add(panel, gbc_panel);
 
 	}
 
 	public JPanel fill(JPanel panel)
 	{
 		JPanel p = new JPanel();
 		//JPanel p = panel;
 		p.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.PAGE_START;
 		
 		for (Quest q : QuestList.getQuestList().values())
 		{
 			if (q.getStage() > 0)
 			{
 				JLabel l = new JLabel("<html><table width=\"470\"><td>" + q.getStageMessage()[q.getStage()] + "</td></table></html>");
 				l.setHorizontalAlignment(SwingConstants.LEFT);
 				p.add(l, c);
 				c.gridy = c.gridy + 1;
 			}
 		}
 
 		panel.add(p);
 
 		return panel;
 	}
 
 }
 
 class FormPanel extends JPanel {
 
 	private static final long serialVersionUID = 7739746377562691419L;
 	GamePanel gamepanel;
 	GameData gamedata;
 	ArrayList<innerformpanel> forms;
 	int selectedForm;
 	JPanel panel = new JPanel();
 	
 	public FormPanel(GamePanel gamepanel, GameData gamedata) {
 		this.gamepanel = gamepanel;
 		this.gamedata = gamedata;
 		setLayout(new BorderLayout(0, 0));
 		
 		JButton btnLeft = new JButton("\u02C2");
 		btnLeft.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectedForm--;
 				if (selectedForm < 0)
 				{
 					selectedForm = forms.size()-1;
 				}
 				changeForm();
 			}
 		});
 		add(btnLeft, BorderLayout.WEST);
 		
 		JButton btnRight = new JButton("\u02C3");
 		btnRight.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectedForm++;
 				if (selectedForm == forms.size())
 				{
 					selectedForm = 0;
 				}
 				changeForm();
 			}
 		});
 		add(btnRight, BorderLayout.EAST);
 		
 		forms = new ArrayList<innerformpanel>();
 		int v = 0;
 		for (Form f : gamedata.getPlayer().getForms().values())
 		{
 			forms.add(new innerformpanel(f, gamedata, gamepanel));
 			if (f.getSummon().equals(gamedata.getPlayer().getFormKey()))
 			{
 				selectedForm = v;
 			}
 			v++;
 		}
 		add(panel, BorderLayout.CENTER);
 		
 		changeForm();
 	}
 	
 	public void changeForm()
 	{
 		gamepanel.requestFocusInWindow();
 		panel.removeAll();
 		panel.add(forms.get(selectedForm));
 		panel.revalidate();
 		panel.repaint();
 	}
 
 }
 
 class innerformpanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8029465210732328945L;
 
 	/**
 	 * Create the panel.
 	 */
 	public innerformpanel(final Form form, final GameData gamedata, final GamePanel gamepanel) {
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 68, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel lblFormname = null;
 		if (gamedata.getPlayer().getFormKey().equals(form.getSummon()))
 			lblFormname = new JLabel(form.getName() + " - CURRENT FORM");
 		else
 			lblFormname = new JLabel(form.getName());
 		GridBagConstraints gbc_lblFormname = new GridBagConstraints();
 		gbc_lblFormname.insets = new Insets(15, 5, 5, 5);
 		gbc_lblFormname.gridx = 1;
 		gbc_lblFormname.gridy = 0;
 		add(lblFormname, gbc_lblFormname);
 		
 		JLabel lblPhrase = new JLabel("Shapeshift KeyWord: " + form.getSummon());
 		GridBagConstraints gbc_lblPhrase = new GridBagConstraints();
 		gbc_lblPhrase.insets = new Insets(5, 5, 5, 5);
 		gbc_lblPhrase.gridx = 1;
 		gbc_lblPhrase.gridy = 1;
 		add(lblPhrase, gbc_lblPhrase);
 		
 		JLabel lblDescription = new JLabel("<html><table width=\"370\"><td>" + form.getDescription() + "</td></table></html>");
 		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
 		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescription.gridx = 1;
 		gbc_lblDescription.gridy = 2;
 		add(lblDescription, gbc_lblDescription);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.insets = new Insets(0, 0, 0, 5);
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 3;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{199, 179, 0};
 		gbl_panel.rowHeights = new int[]{63, 56, 64, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JLabel lblWhisper = new JLabel(form.whisper().getName());
 		lblWhisper.setBorder(new TitledBorder(null, "Whisper", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		lblWhisper.setToolTipText("<html>" + form.whisper().getName() + 
 				"<br>" + form.whisper().getDescription()  + 
 				"<br> Cost: " + form.whisper().getManaCost() + "</html>");
 		GridBagConstraints gbc_lblWhisper = new GridBagConstraints();
 		gbc_lblWhisper.fill = GridBagConstraints.BOTH;
 		gbc_lblWhisper.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWhisper.gridx = 0;
 		gbc_lblWhisper.gridy = 0;
 		panel.add(lblWhisper, gbc_lblWhisper);
 		
 		JLabel lblIncant = new JLabel(form.incant().getName());
 		lblIncant.setBorder(new TitledBorder(null, "Incant", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		lblIncant.setToolTipText("<html>" + form.incant().getName() + 
 				"<br>" + form.incant().getDescription()  + 
 				"<br> Cost: " + form.incant().getManaCost() + "</html>");
 		GridBagConstraints gbc_lblIncant = new GridBagConstraints();
 		gbc_lblIncant.fill = GridBagConstraints.BOTH;
 		gbc_lblIncant.insets = new Insets(0, 0, 5, 0);
 		gbc_lblIncant.gridx = 1;
 		gbc_lblIncant.gridy = 0;
 		panel.add(lblIncant, gbc_lblIncant);
 		
 		JLabel lblShout = new JLabel(form.shout().getName());
 		lblShout.setBorder(new TitledBorder(null, "Shout", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		lblShout.setToolTipText("<html>" + form.shout().getName() + 
 				"<br>" + form.shout().getDescription()  + 
 				"<br> Cost: " + form.shout().getManaCost() + "</html>");
 		GridBagConstraints gbc_lblShout = new GridBagConstraints();
 		gbc_lblShout.fill = GridBagConstraints.BOTH;
 		gbc_lblShout.insets = new Insets(0, 0, 5, 5);
 		gbc_lblShout.gridx = 0;
 		gbc_lblShout.gridy = 1;
 		panel.add(lblShout, gbc_lblShout);
 		
 		JLabel lblRoar = new JLabel(form.roar().getName());
 		lblRoar.setBorder(new TitledBorder(null, "Roar", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		lblRoar.setToolTipText("<html>" + form.roar().getName() + 
 				"<br>" + form.roar().getDescription()  + 
 				"<br> Cost: " + form.roar().getManaCost() + "</html>");
 		GridBagConstraints gbc_lblRoar = new GridBagConstraints();
 		gbc_lblRoar.fill = GridBagConstraints.BOTH;
 		gbc_lblRoar.insets = new Insets(0, 0, 0, 5);
 		gbc_lblRoar.gridx = 0;
 		gbc_lblRoar.gridy = 2;
 		panel.add(lblRoar, gbc_lblRoar);
 		
 		JLabel lblProfane = new JLabel(form.profane().getName());
 		lblProfane.setBorder(new TitledBorder(null, "Profane", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		lblProfane.setToolTipText("<html>" + form.profane().getName() + 
 				"<br>" + form.profane().getDescription()  + 
 				"<br> Cost: " + form.profane().getManaCost() + "</html>");
 		GridBagConstraints gbc_lblProfane = new GridBagConstraints();
 		gbc_lblProfane.fill = GridBagConstraints.BOTH;
 		gbc_lblProfane.gridx = 1;
 		gbc_lblProfane.gridy = 2;
 		panel.add(lblProfane, gbc_lblProfane);
 		
 		JButton change = new JButton("Take On This Form");
 		change.setMargin(new Insets(5,5,5,5));
 		change.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.getPlayer().setFormKey(form.getSummon());
 				gamepanel.addScreen();
 				gamedata.getScreen().repaint();
 				
 			}
 			
 		});
 		
 		GridBagConstraints gbc_change = new GridBagConstraints();
 		gbc_change.fill = GridBagConstraints.BOTH;
 		gbc_change.insets = new Insets(40, 0, 0, 5);
 		gbc_change.gridx = 1;
 		gbc_change.gridy = 4;
 		
 		add(change, gbc_change);
 	}
 }
 
 
 
 @SuppressWarnings("serial")
 class ScreenPanel extends JPanel implements MouseListener{
 
 	@SuppressWarnings("unused")
 	private MovementThread thread;
 	
 	public boolean upKey;
 	public boolean downKey;
 	public boolean leftKey;
 	public boolean rightKey;
 	public boolean questionKey;
 	public boolean escKey;
 	public boolean graveKey;
 	public boolean shiftKey;
 	public boolean ctrlKey;
 	
 	private boolean ControlsImage;
 	
 	public long startTime;
 	
 	BufferedImage layer;
 	GameData gamedata;
 	final ScreenPanel thisPanel = this;
 	private int drawMode = 0;
 	public int fadeStage = 0;
 	private int[] selectionpos = {0, 0};
 	boolean selectionbox = false;
 	/**
 	 * @return the selectionbox
 	 */
 	public boolean isSelectionbox() {
 		return selectionbox;
 	}
 
 	/**
 	 * @param selectionbox the selectionbox to set
 	 */
 	public void setSelectionbox(boolean selectionbox) {
 		this.selectionbox = selectionbox;
 	}
 
 	private int[] screenpos = {0, 0};
 	GameText gametext;
 	Color boxcolour = Color.BLUE;
 	GamePanel gamepanel;
 	private BufferedImage fov = null;
 	private JTextField textField;
 
 	public ScreenPanel(GameData gamedata, GameText gametext, GamePanel gamepanel, JTextField textField)
 	{
 		this.textField = textField;
 		this.gamepanel = gamepanel;
 		this.gametext = gametext;
 		this.gamedata = gamedata;
 		
 		this.thread = new MovementThread(this, gamedata);
 		
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 1;
 		c.anchor = GridBagConstraints.FIRST_LINE_END;
 		c.insets = new Insets(5,5,5,5);
 
 		this.setFocusable(true);
 		this.requestFocusInWindow();
 		this.addKeyListener();
 		this.addMouseListener(this);
 
 		calculateFOV();
 	}
 	public void paintNOW()
 	{
 		//paintComponent(this.getGraphics());
 		//this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
 		repaint();
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 
 		Graphics2D g2d = (Graphics2D)g;
 
 		g2d.setColor(Color.BLACK);
 		g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
 
 		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
 				RenderingHints.VALUE_RENDER_QUALITY);
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		BufferedImage screen = null;
 		if (drawMode == 0)
 		{
 			screen = drawScreen();
 		}
 		else if (drawMode == 1)
 		{
 			screen = fade();
 		}
 			
 		int bottomx = (gamedata.getPlayer().getPos()[0]*gamedata.getPixelSize()) - gamedata.getResolution()/2;
 		int bottomy = (gamedata.getPlayer().getPos()[1]*gamedata.getPixelSize()) - gamedata.getResolution()/2;
 
 		int topx = (gamedata.getPlayer().getPos()[0]*gamedata.getPixelSize()) + gamedata.getResolution()/2;
 		int topy = (gamedata.getPlayer().getPos()[1]*gamedata.getPixelSize()) + gamedata.getResolution()/2;
 
 		int x1 = 0;
 		int y1 = 0;
 
 		if (bottomx < 0)
 		{
 			x1 = 0;
 		}
 
 		else if  (topx > gamedata.getSize()[0])
 		{
 			x1 = screen.getWidth()-gamedata.getResolution();
 		}
 
 		else if (!(bottomx < 0) && !(topx > gamedata.getSize()[0]))
 		{
 			x1 = bottomx;
 		}
 
 		if (bottomy < 0)
 		{
 			y1 = 0;
 		}
 
 		else if (topy > gamedata.getSize()[1])
 		{
 			y1 = screen.getHeight()-gamedata.getResolution();
 		}
 
 		else if (!(bottomy < 0) && !(topy > gamedata.getSize()[1]))
 		{
 			y1 = bottomy;
 		}
 
 
 		if (gamedata.getResolution() > gamedata.getSize()[0])
 		{
 			x1 = 0;
 		}
 		if (gamedata.getResolution() > gamedata.getSize()[1])
 		{
 			y1 = 0;
 		}
 
 		screenpos[0] = x1;
 		screenpos[1] = y1;
 
 		g2d.drawImage(screen, 0, 0, this.getWidth(), this.getHeight(), screenpos[0], screenpos[1], screenpos[0]+gamedata.getResolution(), screenpos[1]+gamedata.getResolution(), null);
 
 	}
 
 	public BufferedImage drawScreen()
 	{
 		calculateFOV();
 
 		BufferedImage image = new BufferedImage(gamedata.getSize()[0], gamedata.getSize()[1], BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g2d = image.createGraphics();
 
 		g2d.setColor(Color.BLACK);
 		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
 
 		g2d.setColor(Color.LIGHT_GRAY);
 		String[] map = gamedata.getMap();
 
 		for (int y = 0; y < map.length; y++)
 		{
 			String line = map[y];
 			for (int x = 0; x < line.length(); x++)
 			{
 				if ((!gamedata.getLit()[x][y]) && (gamedata.getSeen()[x][y]))
 				{
 					g2d.setColor(gamedata.leveldata.getDefaultColor());
 					g2d.drawString(Character.toString(','), x*gamedata.getPixelSize(), y*gamedata.getPixelSize());
 				}
 				else if (!gamedata.getLit()[x][y])
 				{
 					continue;
 				}
 				else if ((line.charAt(x) == '.') || (line.charAt(x) == '@') 
 						|| (line.charAt(x) == 'N') || (line.charAt(x) == 'E')
 						|| (line.charAt(x) == 'S') || (line.charAt(x) == 'A'))
 				{
 					g2d.setColor(gamedata.leveldata.getColor("."));
 					g2d.drawString("\u2235", x*gamedata.getPixelSize(), y*gamedata.getPixelSize());
 				}
 				else
 				{
 					g2d.setColor(gamedata.leveldata.getColor(Character.toString(line.charAt(x))));
 					g2d.drawString(Character.toString(line.charAt(x)), x*gamedata.getPixelSize(), y*gamedata.getPixelSize());
 				}
 			}
 		}
 		Player p = gamedata.getPlayer();
 		g2d.setColor(p.getCharColour());
 		g2d.drawString(p.getChar(), p.getPos()[0]*gamedata.getPixelSize(), p.getPos()[1]*gamedata.getPixelSize());
 
 		for (Actor actor : gamedata.getActors())
 		{
 			int[] pos = actor.getPos();
 			if (gamedata.getLit()[pos[0]][pos[1]])
 			{
 				g2d.setColor(actor.getCharColour());
 				g2d.drawString(actor.getChar(), pos[0]*gamedata.getPixelSize(), pos[1]*gamedata.getPixelSize());
 			}
 		}
 
 		for (Activator a : gamedata.getActivators())
 		{
 			int[] pos = a.getPosition();
 			if (gamedata.getLit()[pos[0]][pos[1]])
 			{
 				g2d.setColor(a.getCharColor());
 				g2d.drawString(a.getCharImage(), pos[0]*gamedata.getPixelSize(), pos[1]*gamedata.getPixelSize());
 			}
 		}
 
 		for (Spell spell : gamedata.getSpells())
 		{
 			if (spell instanceof PhysicalSpell)
 			{
 				g2d.setColor(((PhysicalSpell) spell).getColour());
 				for (int[] pos : ((PhysicalSpell) spell).getAffectedTiles())
 				{
 					g2d.drawString(((PhysicalSpell) spell).getCharImage(), pos[0]*gamedata.getPixelSize(), pos[1]*gamedata.getPixelSize());
 				}
 			}
 		}
 		g2d.drawImage(fov, 0, 0, null);
 
 		if (selectionbox) {
 			g2d.setColor(boxcolour);
 
 			g2d.drawOval(selectionpos[0]-1, selectionpos[1]+3,
 					gamedata.getPixelSize(), gamedata.getPixelSize());
 		}
 
 		return image;
 	}
 	
 	public BufferedImage fade()
 	{
 		final BufferedImage background = drawScreen();
 		
 		AlphaComposite ac =
 				  AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (0.25f * getFadeStage()));
 		
 		Graphics2D g2d = background.createGraphics();
 		
 		g2d.setComposite(ac);
 		g2d.setColor(Color.BLACK);
 		g2d.fillRect(0, 0, background.getWidth(), background.getHeight());
 		
 		return background;
 	}
 
 	public void calculateFOV()
 	{
 		Shadow s = new Shadow(gamedata);
 		s.ComputeFieldOfViewWithShadowCasting(gamedata.getPlayer().getPos()[0], gamedata.getPlayer().getPos()[1], gamedata.getLit(), gamedata.getSeen());
 	}
 
 	public void addKeyListener()
 	{
 		TimedKeyListener kl = new TimedKeyListener() {
 			
 		      public void keyPressed(KeyEvent e) {
 		        // Must be called prior to any other action!
 		        super.keyPressed(e);
 		       
 		        int direction = -1;
 
 				if ((e.getKeyCode() == KeyEvent.VK_ESCAPE) && (!gamedata.isDialogue()) && (!escKey))
 				{
 					selectionbox = false;
 					TaskManager.clearTasks();
 					for (Actor actor : gamedata.getActors())
 					{
 						actor.clearActions();
 					}
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamepanel.addMenu();
 					escKey = true;
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) && (!gamedata.isDialogue()) && (!graveKey))
 				{
 					selectionbox = false;
 					TaskManager.clearTasks();
 					for (Actor actor : gamedata.getActors())
 					{
 						actor.clearActions();
 					}
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamepanel.characterPanel();
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e1) {
 						e1.printStackTrace();
 					}
 					graveKey = true;
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_SHIFT) && (!gamedata.isDialogue()) && (!shiftKey))
 				{
 					selectionbox = false;
 					TaskManager.clearTasks();
 					for (Actor actor : gamedata.getActors())
 					{
 						actor.clearActions();
 					}
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamepanel.notesPanel();
 					shiftKey = true;
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_CONTROL) && (!gamedata.isDialogue()) && (!ctrlKey))
 				{
 					selectionbox = false;
 					TaskManager.clearTasks();
 					for (Actor actor : gamedata.getActors())
 					{
 						actor.clearActions();
 					}
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gamepanel.formPanel();
 					ctrlKey = true;
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_1) && (!gamedata.isDialogue()))
 				{
 					selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gametext.whisper();
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_2) && (!gamedata.isDialogue()))
 				{
 					selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gametext.shout();
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_3) && (!gamedata.isDialogue()))
 				{
 					selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gametext.roar();
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_4) && (!gamedata.isDialogue()))
 				{
 					selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gametext.incant();
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_5) && (!gamedata.isDialogue()))
 				{
 					selectionbox = false;
 					gamedata.stopAll();
 					gamedata.getAnimPanel().setPlayAnim(false);
 					gametext.profane();
 				}
 				else if ((e.getKeyCode() == KeyEvent.VK_SLASH) && (!gamedata.isDialogue()) && (!questionKey))
 				{
 					if (ControlsImage)
 					{
 						repaint();
 						ControlsImage = false;
 					}
 					else
 					{
 						gamedata.setTurn(true);
 						
 						Graphics g = getGraphics();
 						
 						g.drawImage(new Controls().getImage(), 0, 0, gamedata.getScreen().getWidth(), gamedata.getScreen().getHeight(), null);
 						ControlsImage = true;
 					}
 					questionKey = true;
 					
 				}
 				else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 					direction = 2;
 					rightKey = true;
 				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 					direction = 1;
 					leftKey = true;
 				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
 					direction = 3;
 					upKey = true;
 				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 					direction = 0;
 					downKey = true;
 				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (textField.getText().length() == 0)
 					{
 						gamedata.activate();
 					}
 					else
 					{
 						gamepanel.activateTextField();
 					}
 				} else if ((e.getKeyCode() == KeyEvent.VK_SPACE)
 						&& (!gamedata.isTurn())) {
 					gamedata.startTurn(1);
 				}
 				else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
 				{
 					if (textField.getText().length() > 0)
 					{
 						textField.setText(textField.getText().substring(0, textField.getText().length()-1));
 						textField.revalidate();
 					}
 				}
 				else if (e.getKeyCode() == KeyEvent.VK_EQUALS)
 				{
 					
 				}
 				else
 				{
 					textField.setText(textField.getText()+e.getKeyChar());
 					textField.revalidate();
 				}
 				
 				if ((direction > -1) && (startTime == 0))
 				{
 					startTime = System.currentTimeMillis();
 				}
 
 				if ((direction > -1) && (!gamedata.isDialogue())){
 					TaskManager.clearTasks();
 					gamedata.getPlayer().clearActions();
 					selectionbox = false;
 					final int finaldirection = direction;
 					gamedata.getPlayer().setDirection(finaldirection);
 
 					repaint();
 				}
 		      }
 		      
 		      public void keyReleased(KeyEvent evt) {
 		        // Must be called prior to any other action!
 		        super.keyReleased(evt);
 		        // Do we have a real final key release?
 		        if (getReleased()) {
 		        	upKey = false;
 		    		downKey = false;
 		    		leftKey = false;
 		    		rightKey = false;
 		    		escKey = false;
 		    		graveKey = false;
 		    		shiftKey = false;
 		    		ctrlKey = false;
 		    		questionKey = false;
 		    		startTime = 0;
 		    		
 		    		if (!gamedata.isDialogue())
 		    			gamedata.getAnimPanel().setPlayAnim(false);
 		         
 		        }
 		      }
 		    };
 		 
 		    this.addKeyListener(kl);
 	}
 //	
 //	@Override
 //	public void keyTyped(KeyEvent e) {
 //
 //	}
 
 	long start = 0;
 //	public void keyPressed(KeyEvent e) {
 //		int direction = -1;
 //
 //		if ((e.getKeyCode() == KeyEvent.VK_ESCAPE) && (!gamedata.isDialogue()) && (!escKey))
 //		{
 //			selectionbox = false;
 //			TaskManager.clearTasks();
 //			for (Actor actor : gamedata.getActors())
 //			{
 //				actor.clearActions();
 //			}
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gamepanel.addMenu();
 //			escKey = true;
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_BACK_QUOTE) && (!gamedata.isDialogue()) && (!graveKey))
 //		{
 //			selectionbox = false;
 //			TaskManager.clearTasks();
 //			for (Actor actor : gamedata.getActors())
 //			{
 //				actor.clearActions();
 //			}
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gamepanel.characterPanel();
 //			try {
 //				Thread.sleep(100);
 //			} catch (InterruptedException e1) {
 //				e1.printStackTrace();
 //			}
 //			graveKey = true;
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_SHIFT) && (!gamedata.isDialogue()) && (!shiftKey))
 //		{
 //			selectionbox = false;
 //			TaskManager.clearTasks();
 //			for (Actor actor : gamedata.getActors())
 //			{
 //				actor.clearActions();
 //			}
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gamepanel.notesPanel();
 //			shiftKey = true;
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_CONTROL) && (!gamedata.isDialogue()) && (!ctrlKey))
 //		{
 //			selectionbox = false;
 //			TaskManager.clearTasks();
 //			for (Actor actor : gamedata.getActors())
 //			{
 //				actor.clearActions();
 //			}
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gamepanel.formPanel();
 //			ctrlKey = true;
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_1) && (!gamedata.isDialogue()))
 //		{
 //			selectionbox = false;
 //			gamedata.stopAll();
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gametext.whisper();
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_2) && (!gamedata.isDialogue()))
 //		{
 //			selectionbox = false;
 //			gamedata.stopAll();
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gametext.shout();
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_3) && (!gamedata.isDialogue()))
 //		{
 //			selectionbox = false;
 //			gamedata.stopAll();
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gametext.roar();
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_4) && (!gamedata.isDialogue()))
 //		{
 //			selectionbox = false;
 //			gamedata.stopAll();
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gametext.incant();
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_5) && (!gamedata.isDialogue()))
 //		{
 //			selectionbox = false;
 //			gamedata.stopAll();
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //			gametext.profane();
 //		}
 //		else if ((e.getKeyCode() == KeyEvent.VK_SLASH) && (!gamedata.isDialogue()) && (!questionKey))
 //		{
 //			if (ControlsImage)
 //			{
 //				this.repaint();
 //				ControlsImage = false;
 //			}
 //			else
 //			{
 //				gamedata.setTurn(true);
 //				File f = new File("GameData/Resources/Controls.png");
 //				BufferedImage i = null;
 //				try {
 //					i = ImageIO.read(f);
 //				} catch (IOException argh) {
 //					argh.printStackTrace();
 //				}
 //				
 //				Graphics g = this.getGraphics();
 //				
 //				g.drawImage(i, 0, 0, gamedata.getScreen().getWidth(), gamedata.getScreen().getHeight(), null);
 //				ControlsImage = true;
 //			}
 //			questionKey = true;
 //			
 //		}
 //		else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 //			direction = 2;
 //			rightKey = true;
 //		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 //			direction = 1;
 //			leftKey = true;
 //		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
 //			direction = 3;
 //			upKey = true;
 //		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 //			direction = 0;
 //			downKey = true;
 //		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 //			System.err.println("pressed");
 //			if (textField.getText().length() == 0)
 //			{
 //				System.err.println("activate normal");
 //				gamedata.activate();
 //			}
 //			else
 //			{
 //				System.err.println("activate text field");
 //				gamepanel.activateTextField();
 //			}
 //		} else if ((e.getKeyCode() == KeyEvent.VK_SPACE)
 //				&& (!gamedata.isTurn())) {
 //			gamedata.startTurn(1);
 //		}
 //		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
 //		{
 //			if (textField.getText().length() > 0)
 //			{
 //				textField.setText(textField.getText().substring(0, textField.getText().length()-1));
 //				textField.revalidate();
 //			}
 //		}
 //		else if (e.getKeyCode() == KeyEvent.VK_EQUALS)
 //		{
 //			gamedata.getAnimPanel().playSpecificAnimation("GameData/Resources/Animations/TentacleSex2.png", 0);
 //			gamedata.setDialogue(true);
 //		}
 //		else
 //		{
 //			textField.setText(textField.getText()+e.getKeyChar());
 //			textField.revalidate();
 //		}
 //		
 //		if ((direction > -1) && (startTime == 0))
 //		{
 //			startTime = System.currentTimeMillis();
 //		}
 //
 //		if ((direction > -1) && (!gamedata.isDialogue())){
 //			TaskManager.clearTasks();
 //			gamedata.getPlayer().clearActions();
 //			selectionbox = false;
 //			final int finaldirection = direction;
 //			gamedata.getPlayer().setDirection(finaldirection);
 //
 //			this.repaint();
 //		}
 //
 //	}
 
 //	@Override
 //	public void keyReleased(KeyEvent e) {
 //		upKey = false;
 //		downKey = false;
 //		leftKey = false;
 //		rightKey = false;
 //		escKey = false;
 //		graveKey = false;
 //		shiftKey = false;
 //		ctrlKey = false;
 //		questionKey = false;
 //		startTime = 0;
 //		
 //		if (!gamedata.isDialogue())
 //			gamedata.getAnimPanel().setPlayAnim(false);
 //	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 		this.requestFocusInWindow();
 		if (!gamedata.isDialogue()) {
 
 			if (e.getButton() == MouseEvent.BUTTON1) {
 
 				selectionbox = true;
 				double xspacing = (double) this.getWidth()
 						/ gamedata.getResolution();
 				double yspacing = (double) this.getHeight()
 						/ gamedata.getResolution();
 				int x = (int) (e.getX() / xspacing);
 				int y = (int) (e.getY() / yspacing);
 				x /= gamedata.getPixelSize();
 				y /= gamedata.getPixelSize();
 				x *= gamedata.getPixelSize();
 				y *= gamedata.getPixelSize();
 				x += screenpos[0];
 				y += screenpos[1];
 
 				selectionpos[0] = x;
 				selectionpos[1] = y;
 
 				x /= gamedata.getPixelSize();
 				y /= gamedata.getPixelSize();
 
 				y += 1;
 
 				int[] destination = { x, y };
 				gamedata.stopAll();
 
 				int size = gamedata.getPlayer().pathFind(destination);
 
 				if (size == 0) {
 					int xdiff = gamedata.getPlayer().getPos()[0] - x;
 					int ydiff = gamedata.getPlayer().getPos()[1] - y;
 
 					if (Math.abs(xdiff) + Math.abs(ydiff) == 1) {
 						int direction = -1;
 						if (Math.abs(xdiff) > Math.abs(ydiff)) {
 							if (xdiff > 0) {
 								direction = 1;
 							} else {
 								direction = 2;
 							}
 						} else {
 							if (ydiff > 0) {
 								direction = 3;
 							} else {
 								direction = 0;
 							}
 						}
 						gamedata.getPlayer().setDirection(direction);
 						this.repaint();
 						gamedata.activate();
 					} else {
 						boxcolour = Color.RED;
 						this.repaint();
 					}
 				} else {
 					boxcolour = Color.BLUE;
 					gamedata.startTurn(size);
 				}
 			}
 			else if (e.getButton() == MouseEvent.BUTTON3)
 			{
 				double xspacing = (double) this.getWidth()
 						/ gamedata.getResolution();
 				double yspacing = (double) this.getHeight()
 						/ gamedata.getResolution();
 				int x = (int) (e.getX() / xspacing);
 				int y = (int) (e.getY() / yspacing);
 				x /= gamedata.getPixelSize();
 				y /= gamedata.getPixelSize();
 				x *= gamedata.getPixelSize();
 				y *= gamedata.getPixelSize();
 				x += screenpos[0];
 				y += screenpos[1];
 
 				selectionpos[0] = x;
 				selectionpos[1] = y;
 
 				x /= gamedata.getPixelSize();
 				y /= gamedata.getPixelSize();
 
 				y += 1;
 
 				if (!gamedata.getLit()[x][y])
 				{
 					gametext.infoMessage("You cant see there." );
 					return;
 				}
 
 				if ((gamedata.getPlayer().getPos()[0] == x) && (gamedata.getPlayer().getPos()[1] == y))
 				{
 					gametext.infoMessage(gamedata.getPlayer().description());
 					return;
 				}
 
 				for (Actor actor : gamedata.getActors())
 				{
 					if ((actor.getPos()[0] == x) && (actor.getPos()[1] == y))
 					{
 						gametext.infoMessage(actor.description());
 						return;
 					}
 				}
 
 				for (Activator a : gamedata.getActivators())
 				{
 					if ((a.getPosition()[0] == x) && (a.getPosition()[1] == y))
 					{
 						gametext.infoMessage(a.getDescription());
 						return;
 					}
 				}
 
 				gametext.infoMessage(gamedata.leveldata.getDescription(
 						Character.toString(gamedata.getMap()[y].charAt(x))));
 
 			}
 		}
 		else
 		{
 			gamepanel.activateTextField();
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public int getDrawMode() {
 		return drawMode;
 	}
 
 	public void setDrawMode(int drawMode) {
 		this.drawMode = drawMode;
 	}
 
 	public synchronized int getFadeStage() {
 		return fadeStage;
 	}
 
 	public synchronized void setFadeStage(int fadeStage) {
 		this.fadeStage = fadeStage;
 	}
 
 }
 
 
 
 class MenuPanel extends JPanel{
 
 	private static final long serialVersionUID = 2034471973364962621L;
 	GamePanel gamepanel;
 	GameData gamedata;
 	JFrame window;
 	final MenuPanel menupanel = this;
 	
 	public MenuPanel(final GamePanel gamepanel, final GameData gamedata) {
 		this.gamepanel = gamepanel;
 		this.gamedata = gamedata;
 		
 		addMenu();
 	}
 	
 	public void bindWindow(JFrame window)
 	{
 		this.window = window;
 	}
 	
 	public void addOptions()
 	{
 //		this.removeAll();
 //		
 //		GridBagLayout gridBagLayout = new GridBagLayout();
 //		gridBagLayout.columnWidths = new int[]{0, 0};
 //		gridBagLayout.rowHeights = new int[]{0, 0};
 //		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 //		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 //		setLayout(gridBagLayout);
 //		
 //		JPanel panel = new JPanel();
 //		GridBagConstraints gbc_panel = new GridBagConstraints();
 //		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 //		gbc_panel.gridx = 0;
 //		gbc_panel.gridy = 0;
 //		add(panel, gbc_panel);
 //		GridBagLayout gbl_panel = new GridBagLayout();
 //		gbl_panel.columnWidths = new int[]{279, 0};
 //		gbl_panel.rowHeights = new int[]{0, 0, 69, 0};
 //		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 //		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 //		panel.setLayout(gbl_panel);
 //		
 //		JPanel panel_1 = new JPanel();
 //		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 //		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
 //		gbc_panel_1.fill = GridBagConstraints.BOTH;
 //		gbc_panel_1.gridx = 0;
 //		gbc_panel_1.gridy = 0;
 //		panel.add(panel_1, gbc_panel_1);
 //		
 //		JLabel lblScreenResolution = new JLabel("Screen Resolution");
 //		panel_1.add(lblScreenResolution);
 //		
 //		final JComboBox comboBox = new JComboBox();
 //		panel_1.add(comboBox);
 //		comboBox.setName("Screen Resolution");
 //		comboBox.setToolTipText("Choose Screen Resolution");
 //		comboBox.setModel(new DefaultComboBoxModel(new String[] {"300x300", "400x400", "500x500", "600x600", "700x700", "800x800"}));
 //		comboBox.setSelectedIndex((gamedata.getResolution()/100)-3);
 //		
 //		final JCheckBox tglbtnFullscreen = new JCheckBox("Fullscreen");
 //		if (window.getExtendedState() == JFrame.MAXIMIZED_BOTH)
 //		{
 //			tglbtnFullscreen.setSelected(true);
 //		}
 //		tglbtnFullscreen.setActionCommand("Fullscreen");
 //		GridBagConstraints gbc_tglbtnFullsreen = new GridBagConstraints();
 //		gbc_tglbtnFullsreen.insets = new Insets(0, 0, 5, 0);
 //		gbc_tglbtnFullsreen.gridx = 0;
 //		gbc_tglbtnFullsreen.gridy = 1;
 //		panel.add(tglbtnFullscreen, gbc_tglbtnFullsreen);
 //		
 //		JPanel panel_2 = new JPanel();
 //		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 //		gbc_panel_2.gridx = 0;
 //		gbc_panel_2.gridy = 2;
 //		panel.add(panel_2, gbc_panel_2);
 //		GridBagLayout gbl_panel_2 = new GridBagLayout();
 //		gbl_panel_2.columnWidths = new int[]{121, 65, 136, 83, 0};
 //		gbl_panel_2.rowHeights = new int[]{23, 0};
 //		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 //		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 //		panel_2.setLayout(gbl_panel_2);
 //		
 //		JButton btnDefaults = new JButton("Defaults");
 //		btnDefaults.addActionListener(new ActionListener(){
 //			public void actionPerformed(ActionEvent e) {
 //				comboBox.setSelectedIndex(3);
 //				tglbtnFullscreen.setSelected(false);
 //				revalidate();
 //			}
 //		});
 //		GridBagConstraints gbc_btnDefaults = new GridBagConstraints();
 //		gbc_btnDefaults.anchor = GridBagConstraints.NORTHEAST;
 //		gbc_btnDefaults.insets = new Insets(0, 0, 0, 5);
 //		gbc_btnDefaults.gridx = 0;
 //		gbc_btnDefaults.gridy = 0;
 //		panel_2.add(btnDefaults, gbc_btnDefaults);
 //		
 //		JButton btnApply = new JButton("Apply");
 //		btnApply.addActionListener(new ActionListener(){
 //			public void actionPerformed(ActionEvent e) {
 //				gamedata.setResolution((comboBox.getSelectedIndex()+3)*100);
 //				if (tglbtnFullscreen.isSelected())
 //				{
 //					window.setExtendedState(JFrame.MAXIMIZED_BOTH);
 //				}
 //				else
 //				{
 //					window.setExtendedState(JFrame.NORMAL);
 //				}
 //				gamepanel.addScreen();
 //				addMenu();
 //			}
 //		});
 //		GridBagConstraints gbc_btnApply = new GridBagConstraints();
 //		gbc_btnApply.anchor = GridBagConstraints.NORTHEAST;
 //		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
 //		gbc_btnApply.gridx = 2;
 //		gbc_btnApply.gridy = 0;
 //		panel_2.add(btnApply, gbc_btnApply);
 //		
 //		JButton btnCancel = new JButton("Cancel");
 //		btnCancel.addActionListener(new ActionListener(){
 //			public void actionPerformed(ActionEvent e) {
 //				addMenu();
 //			}
 //		});
 //		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 //		gbc_btnCancel.anchor = GridBagConstraints.NORTHEAST;
 //		gbc_btnCancel.gridx = 3;
 //		gbc_btnCancel.gridy = 0;
 //		panel_2.add(btnCancel, gbc_btnCancel);
 //		
 //		gamepanel.requestFocusInWindow();
 //		this.revalidate();
 //		this.repaint();
 		
 		this.removeAll();
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{279, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 69, 0};
 		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 0;
 		panel.add(panel_1, gbc_panel_1);
 
 		JLabel lblScreenResolution = new JLabel("Screen Resolution");
 		panel_1.add(lblScreenResolution);
 
 		final JComboBox comboBox = new JComboBox();
 		panel_1.add(comboBox);
 		comboBox.setName("Screen Resolution");
 		comboBox.setToolTipText("Choose Screen Resolution");
 		comboBox.setModel(new DefaultComboBoxModel(new String[] {"300x300", "400x400", "500x500", "600x600", "700x700", "800x800"}));
 		comboBox.setSelectedIndex((gamedata.getResolution()/100)-3);
 
 		final JCheckBox tglbtnFullscreen = new JCheckBox("Fullscreen");
 		if (window.getExtendedState() == JFrame.MAXIMIZED_BOTH)
 		{
 			tglbtnFullscreen.setSelected(true);
 		}
 		GridBagConstraints gbc_tglbtnFullsreen = new GridBagConstraints();
 		gbc_tglbtnFullsreen.insets = new Insets(0, 0, 5, 0);
 		gbc_tglbtnFullsreen.gridx = 0;
 		gbc_tglbtnFullsreen.gridy = 1;
 		panel.add(tglbtnFullscreen, gbc_tglbtnFullsreen);
 		
 		final JCheckBox btnHealth = new JCheckBox("Health Numbers");
 		if (gamedata.getAnimPanel().healthDes)
 		{
 			btnHealth.setSelected(true);
 		}
 		GridBagConstraints gbc_btnHealth = new GridBagConstraints();
 		gbc_btnHealth.insets = new Insets(0, 0, 5, 0);
 		gbc_btnHealth.gridx = 0;
 		gbc_btnHealth.gridy = 2;
 		panel.add(btnHealth, gbc_btnHealth);
 		
 		final JCheckBox btnMana= new JCheckBox("Throat Numbers");
 		if (gamedata.getAnimPanel().manaDes)
 		{
 			btnMana.setSelected(true);
 		}
 		GridBagConstraints gbc_btnMana = new GridBagConstraints();
 		gbc_btnMana.insets = new Insets(0, 0, 5, 0);
 		gbc_btnMana.gridx = 0;
 		gbc_btnMana.gridy = 3;
 		panel.add(btnMana, gbc_btnMana);
 
 		JPanel panel_2 = new JPanel();
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.gridx = 0;
 		gbc_panel_2.gridy = 4;
 		panel.add(panel_2, gbc_panel_2);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[]{121, 65, 136, 83, 0};
 		gbl_panel_2.rowHeights = new int[]{23, 0};
 		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
 		panel_2.setLayout(gbl_panel_2);
 
 		JButton btnDefaults = new JButton("Defaults");
 		btnDefaults.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				comboBox.setSelectedIndex(3);
 				tglbtnFullscreen.setSelected(false);
 				btnHealth.setSelected(false);
 				btnMana.setSelected(false);
 				revalidate();
 			}
 		});
 		GridBagConstraints gbc_btnDefaults = new GridBagConstraints();
 		gbc_btnDefaults.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnDefaults.insets = new Insets(0, 0, 0, 5);
 		gbc_btnDefaults.gridx = 0;
 		gbc_btnDefaults.gridy = 0;
 		panel_2.add(btnDefaults, gbc_btnDefaults);
 
 		JButton btnApply = new JButton("Apply");
 		btnApply.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				gamedata.setResolution((comboBox.getSelectedIndex()+3)*100);
 				if (tglbtnFullscreen.isSelected())
 				{
 					window.setExtendedState(JFrame.MAXIMIZED_BOTH);
 				}
 				else
 				{
 					window.setExtendedState(JFrame.NORMAL);
 				}
 				gamedata.getAnimPanel().healthDes = btnHealth.isSelected();
 				gamedata.getAnimPanel().manaDes = btnMana.isSelected();
 				addMenu();
 			}
 		});
 		GridBagConstraints gbc_btnApply = new GridBagConstraints();
 		gbc_btnApply.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
 		gbc_btnApply.gridx = 2;
 		gbc_btnApply.gridy = 0;
 		panel_2.add(btnApply, gbc_btnApply);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				addMenu();
 			}
 		});
 		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
 		gbc_btnCancel.anchor = GridBagConstraints.NORTHEAST;
 		gbc_btnCancel.gridx = 3;
 		gbc_btnCancel.gridy = 0;
 		panel_2.add(btnCancel, gbc_btnCancel);
 
 		gamepanel.requestFocusInWindow();
 		this.revalidate();
 		this.repaint();
 	}
 	
 	public void addMenu()
 	{
 		this.removeAll();
 		
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{82, 276, -90, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 0, 5);
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{50, 0};
 		gbl_panel.rowHeights = new int[]{23, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JButton btnReturn = new JButton("Return");
 		btnReturn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamepanel.addScreen();
 			}
 		});
 		GridBagConstraints gbc_btnReturn = new GridBagConstraints();
 		gbc_btnReturn.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnReturn.anchor = GridBagConstraints.NORTH;
 		gbc_btnReturn.insets = new Insets(0, 0, 5, 0);
 		gbc_btnReturn.gridx = 0;
 		gbc_btnReturn.gridy = 0;
 		panel.add(btnReturn, gbc_btnReturn);
 		
 		JButton btnSave = new JButton("Save");
 		btnSave.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gamedata.saveGame();
 				gamepanel.addScreen();
 			}	
 		});
 		GridBagConstraints gbc_btnSave = new GridBagConstraints();
 		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnSave.anchor = GridBagConstraints.NORTH;
 		gbc_btnSave.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSave.gridx = 0;
 		gbc_btnSave.gridy = 1;
 		panel.add(btnSave, gbc_btnSave);
 		
 		if (gamedata.getPlayer().getCurrentLevel().equals("Tutorial"))
 		{
 			btnSave.setEnabled(false);
 		}
 		
 //		JButton btnLoad = new JButton("Load");
 //		btnLoad.addActionListener(new ActionListener(){
 //			@Override
 //			public void actionPerformed(ActionEvent e) {
 //				gamedata.loadGame();
 //				gamepanel.addScreen();
 //				gamepanel.console.clear();
 //			}	
 //		});
 //		GridBagConstraints gbc_btnLoad = new GridBagConstraints();
 //		gbc_btnLoad.fill = GridBagConstraints.HORIZONTAL;
 //		gbc_btnLoad.insets = new Insets(0, 0, 5, 0);
 //		gbc_btnLoad.anchor = GridBagConstraints.NORTH;
 //		gbc_btnLoad.gridx = 0;
 //		gbc_btnLoad.gridy = 2;
 //		panel.add(btnLoad, gbc_btnLoad);
 		
 		JButton btnOptions = new JButton("Options");
 		btnOptions.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				menupanel.addOptions();
 				
 			}
 			
 		});
 		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
 		gbc_btnOptions.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnOptions.anchor = GridBagConstraints.NORTH;
 		gbc_btnOptions.insets = new Insets(0, 0, 5, 0);
 		gbc_btnOptions.gridx = 0;
 		gbc_btnOptions.gridy = 3;
 		panel.add(btnOptions, gbc_btnOptions);
 		
 		JButton btnQuit = new JButton("Quit to Main Menu");
 		btnQuit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int n = JOptionPane.showConfirmDialog(
 					    window,
 					    "Are you sure you want to quit? All unsaved data will be lost.",
 					    "",
 					    JOptionPane.YES_NO_OPTION);
 				if (n == JOptionPane.YES_OPTION)
 				{
 					gamepanel.mainMenu();
 				}
 				else
 				{
 					return;
 				}
 
 			}
 		});
 		GridBagConstraints gbc_btnQuit = new GridBagConstraints();
 		gbc_btnQuit.fill = GridBagConstraints.HORIZONTAL;
 		gbc_btnQuit.anchor = GridBagConstraints.NORTH;
 		gbc_btnQuit.gridx = 0;
 		gbc_btnQuit.gridy = 4;
 		panel.add(btnQuit, gbc_btnQuit);
 
 		gamepanel.requestFocusInWindow();
 		this.revalidate();
 		this.repaint();
 	}
 
 }
 
 class MovementThread extends Thread
 {
 	ScreenPanel panel;
 	GameData gamedata;
 	private boolean move;
 
 	public MovementThread(ScreenPanel panel, GameData gamedata)
 	{
 		this.gamedata = gamedata;
 		this.panel = panel;
 		this.setDaemon(true);
 		this.start();
 	}
 
 	public void run()
 	{
 		long sT = 0;
 		int direction = -1;
 		long sleepTime = 0;
 		while (true) {
 			direction = -1;
 			if (panel.upKey) {
 				direction = 3;
 			} else if (panel.downKey) {
 				direction = 0;
 			} else if (panel.leftKey) {
 				direction = 1;
 			} else if (panel.rightKey) {
 				direction = 2;
 			}
 			final int finaldirection = direction;
 			
 			if ((direction > -1) && (panel.startTime != 0))
 			{
 				move = (System.currentTimeMillis() - panel.startTime) > 100;
 			}
 			else
 			{
 				move = false;
 			}
 			
 			if ((direction > -1) && (move)) {
 				gamedata.getPlayer().setDirection(finaldirection);
 				gamedata.getPlayer().moveActor(finaldirection);
 				if ((gamedata.getPlayer().nextpos[0] != gamedata.getPlayer().getPos()[0]) || (gamedata.getPlayer().nextpos[1] != gamedata.getPlayer().getPos()[1]))
 					gamedata.startTurn(1);
 				else
 					gamedata.getScreen().selectionbox = false;
 			}
 			
 			sleepTime = 120 - (System.currentTimeMillis() - sT);
 			
 			if (sleepTime > 0) {
 				try {
 					Thread.sleep(sleepTime);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			sT = System.currentTimeMillis();
 		}
 	}
 }
 
 class TimedKeyListener
   implements KeyListener, ActionListener {
   private final Timer timer;
   
   private boolean released = false;
   private KeyEvent releaseEvent;
   
   public TimedKeyListener() {
     /* Just a millisecond is necessary
      * to detect a final key release.
      */
     timer = new Timer(1, this);
   }
   
   public void keyPressed(KeyEvent e) {
     released = false;
     /* This key pressed event indicates
      * that the recent key release event
      * was no real, final key release,
      * so we have to stop the timer.
      */
     timer.stop();
   }
   
   public void keyReleased(KeyEvent e) {
     if (!released) {
       /* Store the current key release
        * event, as it is sent finally
        * by our timer.
        */
       releaseEvent = e;
       timer.restart();
     }
   }
   
   public void keyTyped(KeyEvent e) {
   }
   
   public void actionPerformed(ActionEvent e) {
     /* When the timer sends its action event
      * we know that no key press event has
      * followed the last key release event,
      * so we resend the recently stored
      * key release event; but now we set
      * released to true to indicate that
      * we've got the final key release.
      */
     released = true;
     timer.stop();
     keyReleased(releaseEvent);
   }
 
   /**
    * Do we have a real final key release?
    * @return True, if the KeyEvent obtained
    * via keyReleased() is a real final key
    * release.
    */
   protected boolean getReleased() {
     return released;
   }
 }
