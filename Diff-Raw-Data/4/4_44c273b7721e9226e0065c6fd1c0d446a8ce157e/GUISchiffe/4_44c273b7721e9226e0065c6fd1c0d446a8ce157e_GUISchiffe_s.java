 package testpackage.interfaces;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.text.FieldPosition;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Random;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import javax.swing.ButtonGroup;
 
import com.ibm.icu.text.SimpleDateFormat;

 //import testpackage.highscore.pasteName;
 import testpackage.highscore.pasteName;
 import testpackage.shared.ship.AI;
 import testpackage.shared.ship.BadAI;
 import testpackage.shared.ship.IntelligentAI;
 import testpackage.shared.ship.GoodAI;
 import testpackage.shared.ship.Engine;
 import testpackage.shared.ship.Level;
 import testpackage.shared.ship.Rules;
 import testpackage.shared.ship.State;
 import testpackage.shared.ship.SoundHandler;
 import testpackage.shared.ship.LevelGenerator;
 import testpackage.shared.ship.exceptions.InvalidInstruction;
 import testpackage.shared.ship.exceptions.InvalidLevelException;
 import testpackage.shared.ship.TemplateImages;
 
 import translator.TranslatableGUIElement;
 import translator.Translator;
 
 /**
  * class implementing Swing UI
  */
 public class GUISchiffe extends SoundHandler implements ActionListener, BoardUser, KeyListener {
 	private JFrame frame;
 	/**
 	 * this is the engine used in gui game. initialized multiple places
 	 */
 	Engine engine;
 	private AI ai;
 	private BoardPanel[] panels;
 	private boolean speerfeuer;
 	int speerfeuertime;
 	private CountdownTimerPanel clock;
 	private final Translator translator;
 	private JLabel statusLabel;
 	private JLabel hitsLabel;
 	private int[] keyboardSelected = {1,0,0}; // player 0, coord 0,0
 	private Map<Sound,SoundStreamPlayer>[] soundPlayerMaps;
 	private JCheckBoxMenuItem soundcb;
 	private String currentSkin = "defaultskin";
 	private Map<JMenuItem, String> skinButtonToSkinName;
 	private long starttime;
 	
 	
 	private static String getResourceAsString(String path) {
 		return new Scanner(GUISchiffe.class.getResourceAsStream(path)).useDelimiter("\\A").next();
 	}
 
 	/**
 	 * initialize 3 soundstreamplayers for each sound
 	 */
 	private void initSound() {
 		soundPlayerMaps = new Map[3];
 		for (int p : new int[] {0, 1, 2}) { // 2 ist stereo
 			Map<Sound,SoundStreamPlayer> thisMap = soundPlayerMaps[p] = new HashMap<Sound,SoundStreamPlayer>();
 			for (Sound sound : SoundHandler.Sound.values()) {
 				if (p == 2) {
 					thisMap.put(sound, new SoundStreamPlayer(sound, 0));
 				} else {
 					thisMap.put(sound, new SoundStreamPlayer(sound, (p == 0 ? -0.8f : 0.8f)));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * kill players from initSound
 	 */
 	private void uninitSound() {
 		for (int p : new int[] {0, 1, 2}) {
 			for (SoundStreamPlayer ssp : soundPlayerMaps[p].values()) {
 				ssp.kill();
 			}
 		}
 	}
 	
 	private static class GameoverKeyHandler implements KeyListener {
 
 		@Override
 		public void keyPressed(KeyEvent arg0) {
 			//System.err.println(arg0.toString());
 			if (arg0.getKeyCode() == KeyEvent.VK_SPACE) arg0.consume(); // to prevent people from accidentally closing the gameover window when they use keyboard controls
 			
 		}
 
 		@Override
 		public void keyReleased(KeyEvent arg0) {
 		}
 
 		@Override
 		public void keyTyped(KeyEvent arg0) {
 		}
 		
 	}
 	
 	/**
 	 * call this when game is over to notify user of winning player and shut down
 	 */
 	public void gameOver() {
 		if (speerfeuer) clock.pause();
 		
 		panels[0].refresh();
 		panels[1].refresh();
 		
 		int winner = engine.checkWin().playernr;
 		
 		String winnerstr;
 
 		if (winner == -1) {
 			winnerstr = translator.translateMessage("gameOverDraw");
 		} else {
 			String format = "dd.MM.yyyy   HH:mm";
 		    SimpleDateFormat sdf = new SimpleDateFormat(format); 
 		    FieldPosition pos = new FieldPosition(0);
 		    StringBuffer buf = new StringBuffer();
 		    StringBuffer date = sdf.format(new Date(), buf, pos);
 		    String datestring = buf.toString();
 			pasteName.maybeAddHighScore(frame, datestring, (int) Math.round((System.currentTimeMillis() - starttime) / 1000) , engine.getState().getLevel());
 			winnerstr = translator.translateMessage("gameOverWinner", String.valueOf(winner+1));
 		}
 		
 		final JDialog d = new JDialog(frame, translator.translateMessage("gameOverWindowTitle"), Dialog.ModalityType.DOCUMENT_MODAL);
 		//d.setSize(500, 100);
 		FlowLayout layout = new FlowLayout();
 		d.setLayout(layout);
 		d.add(new JLabel(translator.translateMessage("gameOverReasonLabel", winnerstr, engine.checkWin().reason)));
 		
 		JButton but = new JButton();
 		but.addKeyListener(new GameoverKeyHandler());
 		but.setText(translator.translateMessage("gameOverQuit"));
 		but.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				d.dispose();
 				shutdown();
 			}
 		});
 		d.add(but);
 		
 		JButton butnew = new JButton();
 		butnew.addKeyListener(new GameoverKeyHandler());
 		butnew.setText(translator.translateMessage("gameOverStartOver"));
 		butnew.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				d.dispose();
 				frame.dispose();
 				initDefaultGame();
 				showFrame();
 			}
 		});
 		d.add(butnew);
 		
 		d.pack();
 		d.setVisible(true);
 		
 		
 /*		JOptionPane.showMessageDialog(frame,
 			    "Game over. " + winnerstr + " Reason: " + engine.checkWin().reason, 
 			    "Game over", 0);*/
 	}
 	
 	/**
 	 * entry point
 	 * @param args not used
 	 */
 	
 	private GUISchiffe(Locale targetLocale) {
 		initSound();
 		
 		initDefaultGame();
 		
 		translator = new Translator("Battleship", targetLocale);
 		showFrame();
 		
 	}
 	
 	/**
 	 * this is the game initialized on startup
 	 */
 	private void initDefaultGame() {
 		try {
 			initNewEngineAndAI(Rules.defaultWidth, Rules.defaultHeight, Rules.enableShotsPerShip, false, Rules.shotsPerShipPart, Rules.standardSpeerfeuerTime, BadAI.class, Rules.defaultAllowMultipleShotsPerTurn, Rules.defaultReichweiteVonSchiffenEnabled);
 		} catch (InvalidLevelException ex) {
 			throw new RuntimeException("Default level probably impossible", ex);
 		}
 	}
 	
 	/**
 	 * ask for settings and then shows the placeOwnShipsDialog
 	 * @return true if both steps complete
 	 */
 	private boolean placeOwnShipsWizard() {
 		SettingsChooser s = new SettingsChooser(translator);
 		s.askForSettings(frame);
 		if (!s.finished) return false;
 
 		Engine newengine = new Engine(new LevelGenerator(s.w, s.h).getLevel());
 
 		speerfeuer = s.speerfeuerenabled;
 		if (s.ammoenabled) {
 			newengine.enableShotsPerShip(s.ammospinnervalue);
 			if (s.rangeenabled)
 				newengine.enableRange();
 		}
 		newengine.setMoreShots(s.moreshotsenabled);
 
 		//System.err.println(newengine.isShotsPerShipEnabled());
 		placeOwnShipsDialog shipchooser = new placeOwnShipsDialog(frame, translator, newengine, this);
 		//System.err.println(newengine.isShotsPerShipEnabled());
 
 		shipchooser.setVisible(true); // blocks. i.e. next line is only executed after ship chooser is closed.
 		//System.err.println(newengine.isShotsPerShipEnabled());
 
 		if (shipchooser.finished ) {
 			engine = newengine;
 			
 			instantiateAndSetAI(s.chosenAI);
 			
 			frame.dispose();
 			showFrame();
 			
 			if (engine.isFinished()) {
 				gameOver();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private void instantiateAndSetAI(Class<?> chosenAI) {
 		try {
 			ai = (AI) chosenAI.getConstructor().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		ai.setEngine(engine);
 	}
 
 	/**
 	 * for making all the gui actions type safe and type static. actioncommands can only be strings.
 	 */
 	private static class actions {
 		static enum actionnames {
 			quicknewgame, save, load, newplaceownships, newgenerated, about, skins, soundcb, quit
 		}
 		
 		public static String quicknewgame = actionnames.quicknewgame.name();
 		public static String save = actionnames.save.name();
 		public static String load = actionnames.load.name();
 		public static String newplaceownships = actionnames.newplaceownships.name();
 		public static String newgenerated = actionnames.newgenerated.name();
 		public static String about = actionnames.about.name();
 		public static String skins = actionnames.skins.name();
 		public static String quit = actionnames.quit.name();
 		public static String soundcb = actionnames.soundcb.name();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent actionEvent) {
 		//System.err.println(actionEvent);
 		String a = actionEvent.getActionCommand();
 		if (speerfeuer) clock.pause();
 		if (a.equals(actions.quicknewgame)) { // would be nice with the Java 7 string switch
 			quickNewGame();
 			if (speerfeuer) clock.resetCountdown();
 		} else if (a.equals(actions.save)) {
 			save();
 		} else if (a.equals(actions.load)) {
 			load();
 			if (speerfeuer) clock.resetCountdown();
 		} else if (a.equals(actions.about)) {
 			about();
 		} else if (a.equals(actions.newplaceownships)) {
 			if (placeOwnShipsWizard())
 				if (speerfeuer) clock.resetCountdown();
 		} else if (a.equals(actions.newgenerated)) {
 			boolean startedNewGame = generatedNewGame();
 			if (speerfeuer && startedNewGame) clock.resetCountdown();
 		} else if (a.equals(actions.quit)) {
 			shutdown();
 		} else if (a.equals(actions.soundcb)) {
 			toggleSound();
 		} else if (a.equals(actions.skins)) {
 			changeSkin(actionEvent);
 		} else {
 			throw new RuntimeException("Unknown action!");
 		}
 		if (speerfeuer) clock.resume();
 	}
 
 	private void toggleSound() {
 		if (soundcb.isSelected()) {
 			initSound();
 		} else {
 			uninitSound();
 		}
 	}
 	
 	private void setStatusBarMessage(String message) {
 		setStatusBarMessage(false, message);
 	}
 
 	/**
 	 * show statusbarmessage
 	 * @param isError if true an error icon will be shown
 	 * @param message text to display. must be html if line-wrapping is needed
 	 */
 	private void setStatusBarMessage(Boolean isError, String message) {
 		if (isError)	statusLabel.setIcon(GUISchiffe.getIcon("Error16"));
 		else		statusLabel.setIcon(GUISchiffe.getIcon("Information16"));
 		statusLabel.setText(message);
 	}
 	
 	/**
 	 * initialize status panel. used below
 	 */
 	private JPanel makeStatusPanel() {
 		JPanel statusPanel = new JPanel();
 		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
 
 		statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 32));
 		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
 		statusLabel = new JLabel();
 		if (engine.isShotsPerShipEnabled()) {
 			setStatusBarMessage(translator.translateMessage("constructorShooterSelect"));
 		} else {
 			setStatusBarMessage(translator.translateMessage("constructorJustAttack"));
 		}
 		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
 		statusPanel.add(statusLabel);
 		
 		statusPanel.add(Box.createHorizontalGlue());
 		
 		hitsLabel = new JLabel(translator.translateMessage("hitCounter", String.valueOf(0), String.valueOf(0)));
 		hitsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 		statusPanel.add(hitsLabel);
 		
 		return statusPanel;
 	}
 	
 	/**
 	 * initialize and show main game window
 	 */
 	private void showFrame() {	
 		TranslatableGUIElement guiBuilder = translator.getGenerator();
 		
 		frame = guiBuilder.generateJFrame("guiFrame");
 		
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(new WindowListener() {
 
 			public void windowActivated(WindowEvent arg0) {}
 			public void windowClosed(WindowEvent arg0) {}
 			public void windowClosing(WindowEvent e) {
 				shutdown();
 			}
 			public void windowDeactivated(WindowEvent e) {}
 			public void windowDeiconified(WindowEvent e) {}
 			public void windowIconified(WindowEvent e) {}
 			public void windowOpened(WindowEvent e) {}
 		});
 		
 		//frame.setResizable(false);
 		frame.setLayout(new BorderLayout());
 		
 		frame.add(makeStatusPanel(), BorderLayout.SOUTH);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		panel.add(setUpBoardPanels());
 		
 		panels[keyboardSelected[0]].addSelection(0, 0);
 		
 		if (speerfeuer) {
 			clock = new CountdownTimerPanel(this);
 			panel.add(clock);
 		}
 		
 		frame.getContentPane().add(panel);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 
 		fillMenuBar(guiBuilder, menuBar);
 		
 		frame.pack();
 		frame.setVisible(true);
 		if (speerfeuer) {
 			clock.go();
 		}
 	}
 
 	/**
 	 * fill menu bar with items
 	 * @param guiBuilder
 	 * @param menuBar
 	 */
 	private void fillMenuBar(TranslatableGUIElement guiBuilder, JMenuBar menuBar) {
 		ActionListener menuListener = this;
 		
 		// File Menu
 	    JMenu fileMenu = guiBuilder.generateJMenu("fileMenu");
 	    fileMenu.setMnemonic(KeyEvent.VK_F);
 	    menuBar.add(fileMenu);
 	    
 	    // Options
 	    JMenu Options = guiBuilder.generateJMenu("Options");
 	    menuBar.add(Options);
 	    
 	    //New
 	    JMenu newMenu = guiBuilder.generateJMenu("New");
 	    newMenu.setIcon(GUISchiffe.getIcon("New16"));
 	    fileMenu.add(newMenu);
 	    
 	    //About
 	    JMenuItem About = guiBuilder.generateJMenuItem("About");
 	    About.setIcon(GUISchiffe.getIcon("About16"));
 	    About.addActionListener(menuListener);
 	    About.setActionCommand(actions.about);
 	    Options.add(About);
 	    
 	    //QuickNew
 	    JMenuItem quickNewItem = guiBuilder.generateJMenuItem("QuickNew");
 	    quickNewItem.addActionListener(menuListener);
 	    quickNewItem.setAccelerator(KeyStroke.getKeyStroke("N"));
 	    quickNewItem.setActionCommand(actions.quicknewgame);
 	    newMenu.add(quickNewItem);
 	    
 	    //PlaceShips
 	    JMenuItem place = guiBuilder.generateJMenuItem("PlaceShips");
 	    place.addActionListener(menuListener);
 	    place.setAccelerator(KeyStroke.getKeyStroke("G"));
 	    place.setActionCommand(actions.newplaceownships);
 	    newMenu.add(place);
 	    
 	    //DefaultNewGame
 	    JMenuItem generate = guiBuilder.generateJMenuItem("DefaultNewGame");
 	    generate.addActionListener(menuListener);
 	    generate.setActionCommand(actions.newgenerated);
 	    newMenu.add(generate);
 	    
 	    //SaveGame
 	    JMenuItem saveItem = guiBuilder.generateJMenuItem("saveGame");
 	    saveItem.setIcon(GUISchiffe.getIcon("Save16"));
 	    saveItem.addActionListener(menuListener);
 	    saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
 	    saveItem.setActionCommand(actions.save);
 	    fileMenu.add(saveItem);
 	    
 	    //loadGame
 	    JMenuItem loadItem = guiBuilder.generateJMenuItem("loadGame");
 	    loadItem.setIcon(GUISchiffe.getIcon("Load16"));
 	    loadItem.addActionListener(menuListener);
 	    loadItem.setAccelerator(KeyStroke.getKeyStroke("control L"));
 	    loadItem.setActionCommand(actions.load);
 	    fileMenu.add(loadItem);
 	    
 	    JMenuItem quitItem = guiBuilder.generateJMenuItem("quit");
 	    quitItem.addActionListener(this);
 	    quitItem.setActionCommand(actions.quit);
 	    fileMenu.add(quitItem);
 	    
 	    //Language Menu
 	    JMenu languageMenu = guiBuilder.generateJMenu("languageMenu");
 	    languageMenu.setIcon(GUISchiffe.getIcon("Language16"));
 		
 	    JMenuItem germanItem = guiBuilder.generateJMenuItem("German");
 		germanItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				translator.setTranslatorLocale(Locale.GERMANY);
 			}
 		});
 		JMenuItem englishItem = guiBuilder.generateJMenuItem("English");
 		englishItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				translator.setTranslatorLocale(Locale.US);
 			}
 		});
 		JMenuItem russianItem = guiBuilder.generateJMenuItem("Russian");
 		russianItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				translator.setTranslatorLocale(new Locale("ru"));
 			}
 		});
 		JMenuItem greekItem = guiBuilder.generateJMenuItem("Greek");
 		greekItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				translator.setTranslatorLocale(new Locale("el"));
 			}
 		});
 		JMenuItem danishItem = guiBuilder.generateJMenuItem("Danish");
 		danishItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				translator.setTranslatorLocale(new Locale("da"));
 			}
 		});
 		
 	    	Options.add(languageMenu);
 		languageMenu.add(englishItem);
 		languageMenu.add(germanItem);
 		languageMenu.add(russianItem);
 		languageMenu.add(greekItem);
 		languageMenu.add(danishItem);
 		// End Language Menu
 
 		soundcb = (JCheckBoxMenuItem) guiBuilder.generateToggleableJMenuItem("enableSounds", new Object[] {} , true, true);
 	    soundcb.setIcon(GUISchiffe.getIcon("Sound16"));
 		soundcb.setActionCommand(actions.soundcb);
 		soundcb.addActionListener(this);
 		
 		Options.add(soundcb);
 
 		JMenu skinMenu = guiBuilder.generateJMenu("skinMenu");
 		ButtonGroup group = new ButtonGroup();
 		skinButtonToSkinName = new HashMap<JMenuItem, String>();
 		for (String skinname : TemplateImages.allSkins) {
 			JMenuItem radioitem = guiBuilder.generateToggleableJMenuItem("skinMenuChoose", new String[] {skinname}, false, currentSkin.equals(skinname));
 			radioitem.setIcon(new javax.swing.ImageIcon(this.getClass().getResource(TemplateImages.imagesdir + skinname + "/" + TemplateImages.fieldToIcon('t') + ".png")));
 			radioitem.setActionCommand(actions.skins);
 			radioitem.addActionListener(this);
 			group.add(radioitem);
 			skinMenu.add(radioitem);
 
 			skinButtonToSkinName.put(radioitem, skinname);
 		}
 		Options.add(skinMenu);
 
 	}
 
 	private JPanel setUpBoardPanels() {
 		JPanel boardpanel = new JPanel();
 		
 		BoardPanel panel1 = new BoardPanel(this, engine, 0, false);
 		boardpanel.add(panel1);
 		BoardPanel panel2 = new BoardPanel(this, engine, 1, false);
 		panels = new BoardPanel[] {panel1, panel2};
 		boardpanel.add(panel2);
 		
 		panel1.setOtherBoard(panel2);
 		panel2.setOtherBoard(panel1);
 		
 		return boardpanel;
 	}
 
 	/**
 	 * procedure for clean shutdown so the threads dont just keep running
 	 */
 	private void shutdown() {
 		//System.err.println("Shutting down");
 		frame.dispose();
 		uninitSound();
 		//System.err.println("Done");
 		System.exit(0);
 	}
 
 	public static void main(String[] args) {
 		new GUISchiffe(Locale.GERMANY);
 	}
 
 	/**
 	 * show error message to user
 	 * @param message message to show
 	 */
 	void userError(String message) {
 		JOptionPane.showMessageDialog(frame,
 			    message, 
 			    translator.translateMessage("userErrorWindowTitle"), 0);
 	}
 	
 	/**
 	 * randomly loads level from built-in collection. this functionality is required by minimal assignment level
 	 */
 	private void quickNewGame() {
 		String[] levels = {"01.lvl", "02.lvl", "03.lvl", "04.lvl", "05.lvl", "06.lvl", "07.lvl", "08.lvl", "09.lvl", "10.lvl", "11.lvl", "12.lvl", "13.lvl", "14.lvl", "15.lvl", "16.lvl", "17.lvl", "18.lvl", "19.lvl"};
 		
 		Random gen = new Random();
 		String chosenLvl = levels[gen.nextInt(levels.length)];
 		
 		Level level;
 		
 		try {
 			//level = new Level(readFileAsString(levelDir + children[chosenLvl]));
 			level = new Level(getResourceAsString(TemplateImages.levelspath + chosenLvl));
 		
 		} catch (InvalidLevelException e) {
 			throw new RuntimeException(String.format("Template level corrupted: %s",chosenLvl),e);
 		}
 		
 		initNewEngineAndAIWithLevel(level, false, false, Rules.shotsPerShipPart, speerfeuertime, ai.getClass(), false, false);
 		
 		frame.dispose();
 		showFrame();
 	}
 
 	private void initNewEngineAndAIWithLevel(Level level, boolean enableshotspership, boolean speerfeuer, int ammocount, int time, Class<? extends AI> chosenAI, boolean moreshots, boolean rangeenabled) {
 		engine = new Engine(level);
 		engine.setSoundHandler(this);
 		
 		this.speerfeuer = speerfeuer;
 		this.speerfeuertime = time;
 		if (enableshotspership) { engine.enableShotsPerShip(ammocount); if (rangeenabled) engine.enableRange(); }
 		engine.setMoreShots(moreshots);
 		instantiateAndSetAI(chosenAI);
 		
 		if (engine.isFinished()) {
 			gameOver();
 		}
 	}
 	
 	private void initNewEngineAndAI(int w, int h, boolean enableshotspership, boolean speerfeuer, int ammocount, int time, Class<? extends AI> chosenAI, boolean moreshots, boolean rangeenabled) throws InvalidLevelException {
 		Level l;
 		try {
 			l = new LevelGenerator(w, h).getLevel();
 		} catch (InvalidLevelException ex) {
 			throw ex;
 		}
 		initNewEngineAndAIWithLevel(	l,
 						enableshotspership,
 						speerfeuer,
 						ammocount,
 						time,
 						chosenAI, moreshots, rangeenabled);
 	}
 	
 	private boolean generatedNewGame() {
 		SettingsChooser s = new SettingsChooser(translator);
 		s.askForSettings(frame);
 		if (!s.finished) return false;
 		try {
 			initNewEngineAndAI(s.w, s.h, s.ammoenabled, s.speerfeuerenabled, s.ammospinnervalue, s.speerfeuerspinnervalue, s.chosenAI, s.moreshotsenabled, s.rangeenabled);
 		} catch (InvalidLevelException ex) {
 			userError(ex.getMessage());
 			return false;
 		}
 		
 		starttime = System.currentTimeMillis();
 		
 		frame.dispose();
 		showFrame();
 		return true;
 	}
 	
 	private void changeSkin(ActionEvent evt){
 		JRadioButtonMenuItem source = (JRadioButtonMenuItem) evt.getSource();
 		currentSkin = skinButtonToSkinName.get(source);
 		panels[0].refresh();
 		panels[1].refresh();
 	}
 	
 	private void about(){
 		JOptionPane.showMessageDialog(frame,
 			    translator.translateMessage("aboutContent"), 
 			    translator.translateMessage("aboutWindowTitle"), JOptionPane.PLAIN_MESSAGE);
 	}
 	
 	/**
 	 * asks for name and saves. blocks
 	 */
 	private void save() {
 		String saveGameName;
 		
 	    JFileChooser chooser = new JFileChooser();
 
 	    int returnVal = chooser.showSaveDialog(frame);
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	saveGameName = chooser.getSelectedFile().getPath();
 	    } else {
 	    	return;
 	    }
 		
 		try {
 			FileOutputStream fileOut = new FileOutputStream(saveGameName);
 			ObjectOutputStream out = new ObjectOutputStream(fileOut);
 			out.writeObject(engine.getState());
 			out.close();
 			fileOut.close();
 		} catch (IOException i) {
 			userError(i.getMessage());
 		}
 	}
 	
 	/**
 	 * asks for name and loads. blocks
 	 */
 	private void load() {
 		String saveGameName;
 		
 	    JFileChooser chooser = new JFileChooser();
 
 	    int returnVal = chooser.showOpenDialog(frame);
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	saveGameName = chooser.getSelectedFile().getPath();
 	    } else {
 	    	return;
 	    }
 		
 		State e = null;
 		try {
 			FileInputStream fileIn = new FileInputStream(saveGameName);
 			ObjectInputStream in = new ObjectInputStream(fileIn);
 			e = (State) in.readObject();
 			in.close();
 			fileIn.close();
 		} catch (IOException i) {
 			userError(i.getMessage());
 			return;
 		} catch (ClassNotFoundException c) {
 			userError("State class not found");
 			c.printStackTrace();
 			return;
 		}
 		engine.setState(e);
 		
 		frame.dispose();
 		showFrame();
 	}
 
 	@Override
 	public void bomb(int player, int x, int y) {
 			if (player == 0) {
 				if (engine.isShotsPerShipEnabled()) {
 					try {
 						int shots = engine.chooseFiringXY(player, y, x);
 						setStatusBarMessage(translator.translateMessage("bombSelectShooter", String.valueOf(x),String.valueOf(y),String.valueOf(shots)));
 						
 						if (engine.isRangeEnabled()) {
 							panels[1].refresh();
 							if (engine.remainingShotsFor(0) > 0) panels[1].markTargets(engine.getTargets(0, y, x));
 						}
 					} catch (InvalidInstruction e) {
 						userError(localizeException(e));
 					}
 				}
 				panels[0].refresh();
 				return;
 			}
 			
 			try {
 				engine.attack(Engine.otherPlayer(player), y, x);
 			} catch (InvalidInstruction e) {
 				if (speerfeuer) clock.pause();
 				if (e.getReason() != testpackage.shared.ship.exceptions.InvalidInstruction.Reason.NOMOREAMMO &&
 					e.getReason() != testpackage.shared.ship.exceptions.InvalidInstruction.Reason.NOTSHOOTABLE)
 					userError(localizeException(e));
 				else
 					setStatusBarMessage(true,localizeException(e));
 				if (speerfeuer) clock.resume();
 				return;
 			}
 			//System.err.println(engine.getState().isPlayerTurn());
 			
 			panels[player].refresh();
 			
 			if (engine.isShotsPerShipEnabled() && player == 1) {
 				try {
 					setStatusBarMessage(translator.translateMessage("bombRemainingShots", String.valueOf(engine.remainingShotsFor(0))));
 					if (engine.isRangeEnabled() && engine.remainingShotsFor(0) > 0)
 						panels[1].markTargets(engine.getTargets(0, engine.getState().getFiringX(0), engine.getState().getFiringY(0)));
 				} catch (InvalidInstruction e) {
 				}
 			}
 			
 			if (engine.isFinished()) { gameOver(); return; }
 
 			aiAttackAs(player);
 			
 			updateHitCounter();
 	}
 
 	private void updateHitCounter() {
 		hitsLabel.setText(translator.translateMessage("hitCounter", String.valueOf(engine.getState().getHits()[0]), String.valueOf(engine.getState().getHits()[1])));
 	}
 	
 	/**
 	 * localize most common user errors (relating to board handling). engine errors aren't very user friendly, the localized versions feature suggestions.
 	 * @param e exception to localize
 	 * @return localized message
 	 */
 	private String localizeException(InvalidInstruction e) {
 		switch (e.getReason()) {
 		case NOMOREAMMO:
 			return translator.translateMessage("RNoMoreAmmo"); 
 		case NOSHOOTERDESIGNATED:
 			return translator.translateMessage("RNoShooterDesignated");
 		case NOTSHOOTABLE:
 			return translator.translateMessage("RNotShootable");
 		case NOTYOURTURN:
 			return translator.translateMessage("RNotYourTurn");
 		default:
 			return e.getMessage();
 		}
 	}
 
 	/**
 	 * called when 5 sec speerfeuer timer expires
 	 * @param player the player number the AI plays as
 	 */
 	void aiAttackAs(int player) {		
 		int i = 0;
 		while (!engine.getState().isPlayerTurn() && !engine.isFinished()) {
 			//System.err.println(i++ + " AI plays as " + player); // debug output to detect runaway AI
 			System.err.flush();
 			ai.playAs(player);
 			engine.checkWin();
 			if (i >= 500) throw new RuntimeException("AI broken");
 		}
 
 		panels[Engine.otherPlayer(player)].refresh();
 		
 		if (engine.isFinished()) { gameOver(); return; }
 
 		updateHitCounter();
 		
 		if (speerfeuer) clock.resetCountdown();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int p = keyboardSelected[0];
 		int x = keyboardSelected[1];
 		int y = keyboardSelected[2];
 		
 		switch (e.getKeyCode()) {
 		case KeyEvent.VK_LEFT:
 			x--;
 			break;
 		case KeyEvent.VK_RIGHT:
 			x++;
 			break;
 		case KeyEvent.VK_UP:
 			y--;
 			break;
 		case KeyEvent.VK_DOWN:
 			y++;
 			break;
 		case KeyEvent.VK_SPACE:
 			bomb(p,x,y);
 			break;
 		default:
 			return;
 		}
 		
 		if (x < 0 && p == 1) {
 			x = engine.getyWidth()-1;
 			p = Engine.otherPlayer(p);
 		}
 		if (x < 0) {
 			x = 0;
 		}
 		if (x > engine.getyWidth()-1 && p == 0) {
 			x = 0;
 			p = Engine.otherPlayer(p);
 		}
 		if (x > engine.getyWidth()-1) {
 			x = engine.getyWidth()-1;
 		}
 		
 		if (y < 0) {
 			y = 0;
 		}
 		
 		if (y > engine.getxWidth()-1) {
 			y = engine.getxWidth()-1;
 		}
 		
 		keyboardSelected = new int[] {p, x, y};
 		
 		//System.err.println(Arrays.toString(keyboardSelected));
 		
 		panels[0].removeSelection();
 		panels[1].removeSelection();
 		panels[p].addSelection(y, x);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 
 		
 	}
 
 	@Override 
 	public void playSound(Sound sound) {
 		soundPlayerMaps[2].get(sound).restart();
 	}
 
 	@Override 
 	public void playSound(Sound sound, int player) {
 		soundPlayerMaps[player].get(sound).restart();
 	}
 
 	public String getSelectedSkin() {
 		return currentSkin;
 	}
 
 	public static javax.swing.ImageIcon getIcon(String iconName) {
 		return new javax.swing.ImageIcon(TemplateImages.class.getResource(TemplateImages.iconsdir + iconName + ".png"));
 	}
 }
