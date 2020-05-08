 package yuuki.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import yuuki.Options;
 import yuuki.action.Action;
 import yuuki.animation.engine.Animator;
 import yuuki.buff.Buff;
 import yuuki.entity.Character;
 import yuuki.entity.Stat;
 import yuuki.graphic.ImageFactory;
 import yuuki.sound.SoundEngine;
 import yuuki.ui.menu.FileMenu;
 import yuuki.ui.menu.MenuBar;
 import yuuki.ui.menu.MenuBarListener;
 import yuuki.ui.screen.BattleScreen;
 import yuuki.ui.screen.CharacterCreationScreen;
 import yuuki.ui.screen.CharacterCreationScreenListener;
 import yuuki.ui.screen.IntroScreen;
 import yuuki.ui.screen.IntroScreenListener;
 import yuuki.ui.screen.OptionsScreen;
 import yuuki.ui.screen.OptionsScreenListener;
 import yuuki.ui.screen.OverworldMovementListener;
 import yuuki.ui.screen.OverworldScreen;
 import yuuki.ui.screen.OverworldScreenListener;
 import yuuki.ui.screen.Screen;
 import yuuki.util.Grid;
 import yuuki.world.Locatable;
 import yuuki.world.Tile;
 import yuuki.world.WalkGraph;
 
 /**
  * A graphical user interface that uses the Swing framework.
  */
 public class GraphicalInterface implements Interactable, IntroScreenListener,
 CharacterCreationScreenListener, OverworldScreenListener,
 OptionsScreenListener, MenuBarListener {
 	
 	/**
 	 * The speed of game animation.
 	 */
 	public static final int ANIMATION_FPS = 30;
 	
 	/**
 	 * The height of the message box within the window.
 	 */
 	public static final int MESSAGE_BOX_HEIGHT = 100;
 	
 	/**
 	 * The amount of time to show a message on the screen for.
 	 */
 	public static final int MESSAGE_DISPLAY_TIME = 5000;
 	
 	/**
 	 * The delay, in milliseconds, between each letter printed on the message
 	 * box.
 	 */
 	public static final int MESSAGE_LETTER_DELAY = 100;
 	
 	/**
 	 * The height of the game window.
 	 */
 	public static final int WINDOW_HEIGHT = 600;
 	
 	/**
 	 * The width of the game window.
 	 */
 	public static final int WINDOW_WIDTH = 800;
 	
 	/**
 	 * Invokes the given Runnable later if not on the EDT. If currently on the
 	 * EDT, executes the Runnable immediately on the current thread.
 	 * 
 	 * @param doRun The Runnable to be executed.
 	 */
 	private static void invokeLaterIfNeeded(Runnable doRun) {
 		if (SwingUtilities.isEventDispatchThread()) {
 			doRun.run();
 		} else {
 			SwingUtilities.invokeLater(doRun);
 		}
 	}
 	/**
 	 * The animation engine.
 	 */
 	private Animator animationEngine;
 	
 	/**
 	 * The battle screen.
 	 */
 	private BattleScreen battleScreen;
 	
 	/**
 	 * The screen where character creation is done.
 	 */
 	private CharacterCreationScreen charCreationScreen;
 	
 	/**
 	 * The screen that the interface is currently on.
 	 */
 	private Screen<?> currentScreen;
 	
 	/**
 	 * The ending screen.
 	 */
 	private Screen<?> endingScreen;
 	
 	/**
 	 * The screen that the interface was previously on.
 	 */
 	private Screen<?> formerScreen;
 	
 	/**
 	 * Loads image files.
 	 */
 	private ImageFactory imageEngine;
 	
 	/**
 	 * The intro screen.
 	 */
 	private IntroScreen introScreen;
 	
 	/**
 	 * The object performing the actual work.
 	 */
 	private UiExecutor mainProgram;
 	
 	/**
 	 * The main window of the program.
 	 */
 	private JFrame mainWindow;
 	
 	/**
 	 * The game's menu bar.
 	 */
 	private MenuBar menuBar;
 	
 	/**
 	 * The message box for the game.
 	 */
 	private MessageBox messageBox;
 	
 	/**
 	 * The options of the game.
 	 */
 	private Options options;
 	
 	/**
 	 * The options screen.
 	 */
 	private OptionsScreen optionsScreen;
 	
 	/**
 	 * The overworld screen.
 	 */
 	private OverworldScreen overworldScreen;
 	
 	/**
 	 * The pause screen.
 	 */
 	private Screen<?> pauseScreen;
 	
 	/**
 	 * Plays all sounds.
 	 */
 	private SoundEngine soundEngine;
 	
 	/**
 	 * Allocates a new GraphicalInterface. Its components are created.
 	 * 
 	 * @param mainProgram The class that executes requests made by the GUI.
 	 * @param options The options of the program.
 	 */
 	public GraphicalInterface(UiExecutor mainProgram, Options options) {
 		this.options = options;
 		this.mainProgram = mainProgram;
 		currentScreen = null;
 		formerScreen = null;
 		this.animationEngine = new Animator(ANIMATION_FPS);
 		this.soundEngine = new SoundEngine();
 		this.imageEngine = new ImageFactory();
 	}
 	
 	@Override
 	public void addWorldLocatables(ArrayList<Locatable> l) {
 		class Runner implements Runnable {
 			private ArrayList<Locatable> l;
 			public Runner(ArrayList<Locatable> l) {
 				this.l = l;
 			}
 			@Override
 			public void run() {
 				overworldScreen.addWorldLocatables(l);
 			}
 		}
 		Runner r = new Runner(l);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void applyOptions(Options options) {
 		soundEngine.setEffectVolume(options.sfxVolume);
 		soundEngine.setMusicVolume(options.bgmVolume);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void bgmVolumeChanged(int volume) {
 		options.bgmVolume = volume;
 		mainProgram.requestOptionApplication();
 	}
 	
 	@Override
 	public void clearWorldLocatables() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				overworldScreen.clearWorldLocatables();
 			}
 		});
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean confirm(String prompt, String yes, String no) {
 		String[] ops = {yes, no};
 		String choice = (String) getChoice(prompt, ops);
 		return (choice.equals(yes));
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createCharacterClicked() {
 		String name = charCreationScreen.getEnteredName();
 		int level = charCreationScreen.getEnteredLevel();
 		if (!name.equals("")) {
 			mainProgram.requestCharacterCreation(name, level);
 		} else {
 			alert("You must enter a name!");
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void destroy() {
 		mainWindow.dispose();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void display(Character speaker, String message, boolean animated) {
 		long letterTime = (animated) ? MESSAGE_LETTER_DELAY : 0;
 		messageBox.display(speaker, message, letterTime, MESSAGE_DISPLAY_TIME);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void exitClicked() {
 		mainProgram.requestQuit();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getChoice(Object[] options) {
 		return getChoice("Select an option", options);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object getChoice(String prompt, Object[] options) {
 		class Runner implements Runnable, MessageBoxInputListener {
 			public Object value = null;
 			private Object[] options;
 			private String prompt;
 			public Runner(String prompt, Object[] options) {
 				this.options = options;
 				this.prompt = prompt;
 			}
 			@Override
 			public void enterClicked(String s) {}
 			@Override
 			public void optionClicked(Object option) {
 				value = option;
 				messageBox.removeListener(this);
 			}
 			@Override
 			public void run() {
 				messageBox.addListener(this);
 				messageBox.getChoice(prompt, options);
 			}
 		};
 		Runner r = new Runner(prompt, options);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.value == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			messageBox.removeListener(r);
 			messageBox.exitPrompt();
 			Thread.currentThread().interrupt();
 		}
 		return r.value;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public double getDouble() {
 		return getDouble("Enter an integer");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public double getDouble(double min, double max) {
 		return getDouble("Enter an integer", min, max);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public double getDouble(String prompt) {
 		Double answer = null;
 		while (answer == null) {
 			answer = Double.parseDouble(getString(prompt));
 		}
 		return answer.doubleValue();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public double getDouble(String prompt, double min, double max) {
 		double answer = 0;
 		boolean answerIsGood = false;
 		while (!answerIsGood) {
 			answer = getDouble(prompt);
 			if (answer >= min && answer <= max) {
 				answerIsGood = true;
 			}
 		}
 		return answer;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getInt() {
 		return getInt("Enter an integer");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getInt(int min, int max) {
 		return getInt("Enter an integer", min, max);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getInt(String prompt) {
 		Integer answer = null;
 		while (answer == null) {
 			answer = Integer.parseInt(getString(prompt));
 		}
 		return answer.intValue();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int getInt(String prompt, int min, int max) {
 		int answer = 0;
 		boolean answerIsGood = false;
 		while (!answerIsGood) {
 			answer = getInt(prompt);
 			if (answer >= min && answer <= max) {
 				answerIsGood = true;
 			}
 		}
 		return answer;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getString() {
 		return getString("Enter a value");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getString(String prompt) {
 		class Runner implements MessageBoxInputListener, Runnable {
 			public String prompt;
 			public String value = null;
 			public Runner(String prompt) {
 				this.prompt = prompt;
 			}
 			@Override
 			public void enterClicked(String input) {
 				value = input;
 				messageBox.removeListener(this);
 			}
 			@Override
 			public void optionClicked(Object option) {}
 			@Override
 			public void run() {
 				messageBox.addListener(this);
 				messageBox.getString(prompt);
 			}
 		}
 		Runner r = new Runner(prompt);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.value == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			messageBox.removeListener(r);
 			messageBox.exitPrompt();
 			Thread.currentThread().interrupt();
 		}
 		return r.value;
 	}
 	
 	/**
 	 * Creates the components of the main JFrame and draws the main window on
 	 * screen. In order to prevent asynchronous access of child components
 	 * before they have finished constructing, this method blocks until
 	 * construction is complete.
 	 */
 	@Override
 	public void initialize() {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				createComponents();
 				refreshWindow();
 			}
 		};
 		try {
 			SwingUtilities.invokeAndWait(r);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Thread.currentThread().interrupt();
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void loadGameClicked() {
 		mainProgram.requestLoadGame();
 	}
 	
 	@Override
 	public void menuItemTriggered(int menuId, int itemId) {
 		switch (menuId) {
 			case MenuBar.FILE_MENU_ID:
 				switch (itemId) {
 					case FileMenu.NEW_ITEM_ID:
 						mainProgram.requestNewGame();
 						break;
 						
 					case FileMenu.LOAD_ITEM_ID:
 						mainProgram.requestLoadGame();
 						break;
 						
 					case FileMenu.SAVE_ITEM_ID:
 						mainProgram.requestSaveGame();
 						break;
 						
 					case FileMenu.CLOSE_ITEM_ID:
 						mainProgram.requestCloseGame();
 						break;
 						
 					case FileMenu.OPTIONS_ITEM_ID:
 						mainProgram.requestOptionsScreen();
 						break;
 						
 					case FileMenu.EXIT_ITEM_ID:
 						mainProgram.requestQuit();
 						break;
 				}
 				break;
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void newGameClicked() {
 		mainProgram.requestNewGame();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void optionsClicked() {
 		mainProgram.requestOptionsScreen();
 	}
 	
 	@Override
 	public void optionsSubmitted() {
 		mainProgram.requestOptionsSubmission();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void playSound(String effectIndex) {
 		soundEngine.playEffect(effectIndex);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Action selectAction(Action[] actions) {
 		return (Action) getChoice("Select an action", actions);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Point selectMove(WalkGraph graph) {
 		class Runner implements Runnable, OverworldMovementListener {
 			public Point move = null;
 			private WalkGraph graph;
 			public Runner(WalkGraph graph) {
 				this.graph = graph;
 			}
 			@Override
 			public void movementButtonClicked(Point moveLocation) {
				overworldScreen.setWalkGraph(null);
 				overworldScreen.removeMovementListener(this);
 				this.move = moveLocation;
 			}
 			@Override
 			public void run() {
 				overworldScreen.setWalkGraph(graph);
 				overworldScreen.addMovementListener(this);
 			}
 		};
 		Runner r = new Runner(graph);
 		SwingUtilities.invokeLater(r);
 		try {
 			while (r.move == null) {
 				Thread.sleep(50);
 			}
 		} catch (InterruptedException e) {
 			overworldScreen.setWalkGraph(null);
 			overworldScreen.removeMovementListener(r);
 			Thread.currentThread().interrupt();
 		}
 		return r.move;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Character selectTarget(ArrayList<ArrayList<Character>> fighters) {
 		ArrayList<Character> chars = new ArrayList<Character>();
 		for (int i = 0; i < fighters.size(); i++) {
 			ArrayList<Character> team = fighters.get(i);
 			for (int j = 0; j < team.size(); j++) {
 				chars.add(team.get(j));
 			}
 		}
 		Character[] charsArr = chars.toArray(new Character[0]);
 		return (Character) getChoice("Select a target", charsArr);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setWorldView(Grid<Tile> view) {
 		class Runner implements Runnable {
 			private Grid<Tile> view;
 			public Runner(Grid<Tile> tg) {
 				view = tg;
 			}
 			@Override
 			public void run() {
 				overworldScreen.setWorldView(view);
 			}
 		}
 		Runner r = new Runner(view);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	@Override
 	public void sfxTestClicked() {
 		soundEngine.playEffect("SFX_TEST");
 	}
 	
 	@Override
 	public void sfxVolumeChanged(int volume) {
 		options.sfxVolume = volume;
 		mainProgram.requestOptionApplication();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showActionFailure(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionFailure(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showActionPreperation(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionPreparation(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showActionUse(Action action) {
 		class Runner implements Runnable {
 			public Action action;
 			@Override
 			public void run() {
 				battleScreen.showActionUse(action);
 			}
 		}
 		Runner r = new Runner();
 		r.action = action;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showBuffActivation(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffActivation(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showBuffApplication(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffApplication(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showBuffDeactivation(Buff buff) {
 		class Runner implements Runnable {
 			public Buff buff;
 			@Override
 			public void run() {
 				battleScreen.showBuffDeactivation(buff);
 			}
 		}
 		Runner r = new Runner();
 		r.buff = buff;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showCharacterRemoval(Character c) {
 		class Runner implements Runnable {
 			public Character c;
 			@Override
 			public void run() {
 				battleScreen.showCharacterRemoval(c);
 			}
 		}
 		Runner r = new Runner();
 		r.c = c;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showCharacterVictory(Character[] cs) {
 		class Runner implements Runnable {
 			public Character[] cs;
 			@Override
 			public void run() {
 				battleScreen.showCharacterVictory(cs);
 			}
 		}
 		Runner r = new Runner();
 		r.cs = cs;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showDamage(Character fighter, Stat stat, double damage) {
 		if (stat.getName().equalsIgnoreCase("health")) {
 			soundEngine.playEffect("HIT");
 		}
 		battleScreen.showDamage(fighter, stat, damage);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showDamage(Character fighter, Stat stat, int damage) {
 		if (stat.getName().equalsIgnoreCase("health")) {
 			soundEngine.playEffect("HIT");
 		}
 		battleScreen.showDamage(fighter, stat, damage);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showRecovery(Character fighter, Stat stat, double amount) {
 		battleScreen.showRecovery(fighter, stat, amount);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showRecovery(Character fighter, Stat stat, int amount) {
 		battleScreen.showRecovery(fighter, stat, amount);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void showStatUpdate(Character fighter) {
 		class Runner implements Runnable {
 			public Character fighter;
 			@Override
 			public void run() {
 				battleScreen.showStatUpdate(fighter);
 			}
 		}
 		Runner r = new Runner();
 		r.fighter = fighter;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * Shows that a feature has not yet been implemented.
 	 */
 	public void showUnimpMsg() {
 		display(null, "That feature has not yet been implemented.", true);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void startBattleClicked() {
 		mainProgram.requestBattle(true);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToBattleScreen(Character[][] fighters) {
 		class Runner implements Runnable {
 			public Character[][] fighters;
 			@Override
 			public void run() {
 				battleScreen.initBattle(fighters);
 				switchWindow(battleScreen);
 				battleScreen.showStart();
 				mainProgram.requestBattleStart();
 			}
 		}
 		Runner r = new Runner();
 		r.fighters = fighters;
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToCharacterCreationScreen() {
 		charCreationScreen.addListener(this);
 		switchWindow(charCreationScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToEndingScreen() {
 		switchWindow(endingScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToIntroScreen() {
 		introScreen.addListener(this);
 		switchWindow(introScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToLastScreen() {
 		switchWindow(formerScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToOptionsScreen() {
 		optionsScreen.addListener(this);
 		optionsScreen.setValues(options);
 		switchWindow(optionsScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToOverworldScreen() {
 		overworldScreen.addListener(this);
 		switchWindow(overworldScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void switchToPauseScreen() {
 		switchWindow(pauseScreen);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateWorldView(Point center) {
 		class Runner implements Runnable {
 			private Point p;
 			public Runner(Point p) {
 				this.p = p;
 			}
 			@Override
 			public void run() {
 				overworldScreen.updateWorldView(p);
 			}
 		}
 		Runner r = new Runner(center);
 		SwingUtilities.invokeLater(r);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void waitForDisplay() {
 		messageBox.waitForClean();
 	}
 	
 	/**
 	 * Shows an alert dialog box.
 	 * 
 	 * @param msg The message to alert the user with.
 	 */
 	private void alert(String msg) {
 		JOptionPane.showMessageDialog(null, msg);
 	}
 	
 	/**
 	 * Clears the main window of all components.
 	 */
 	private void clearWindow() {
 		mainWindow.getContentPane().removeAll();
 	}
 	
 	/**
 	 * Creates the battle screen.
 	 */
 	private void createBattleScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		battleScreen = new BattleScreen(WINDOW_WIDTH, height, animationEngine);
 		battleScreen.setBackgroundMusic("BGM_BATTLE");
 	}
 	
 	/**
 	 * Creates the screens used in this GUI.
 	 */
 	private void createComponents() {
 		createMainWindow();
 		createMessageBox();
 		createMenuBar();
 		createIntroScreen();
 		createOptionsScreen();
 		createBattleScreen();
 		createOverworldScreen();
 		createPauseScreen();
 		createEndingScreen();
 		createPlayerCreationScreen();
 	}
 	
 	/**
 	 * Creates the ending screen.
 	 */
 	private void createEndingScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		endingScreen = Screen.getInstance(WINDOW_WIDTH, height);
 		endingScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the intro screen.
 	 */
 	private void createIntroScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		Image screenImage = imageEngine.getImage("BG_INTRO_SCREEN");
 		introScreen = new IntroScreen(WINDOW_WIDTH, height, soundEngine);
 		introScreen.setBackgroundMusic("BGM_MAIN_MENU");
 		introScreen.setBackgroundImage(screenImage);
 	}
 	
 	/**
 	 * Creates the primary window.
 	 */
 	private void createMainWindow() {
 		WindowListener l = new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				mainProgram.requestQuit();
 			}
 		};
 		mainWindow = new JFrame("Yuuki - A JRPG");
 		mainWindow.setDefaultCloseOperation(
 				WindowConstants.DO_NOTHING_ON_CLOSE);
 		mainWindow.setResizable(false);
 		mainWindow.addWindowListener(l);
 	}
 	
 	/**
 	 * Creates the menu bar.
 	 */
 	private void createMenuBar() {
 		menuBar = new MenuBar();
 		menuBar.addListener(this);
 	}
 	
 	/**
 	 * Creates the message box.
 	 */
 	private void createMessageBox() {
 		Animator a = animationEngine;
 		messageBox = new MessageBox(a, WINDOW_WIDTH, MESSAGE_BOX_HEIGHT);
 	}
 	
 	/**
 	 * Creates the options screen.
 	 */
 	private void createOptionsScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		optionsScreen = new OptionsScreen(WINDOW_WIDTH, height);
 		optionsScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the overworld screen.
 	 */
 	private void createOverworldScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		overworldScreen = new OverworldScreen(WINDOW_WIDTH, height);
 		overworldScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the pause screen.
 	 */
 	private void createPauseScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		pauseScreen = Screen.getInstance(WINDOW_WIDTH, height);
 		pauseScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Creates the player creation screen.
 	 */
 	private void createPlayerCreationScreen() {
 		int height = WINDOW_HEIGHT - MESSAGE_BOX_HEIGHT;
 		charCreationScreen = new CharacterCreationScreen(WINDOW_WIDTH, height);
 		charCreationScreen.setBackgroundMusic("BGM_MAIN_MENU");
 	}
 	
 	/**
 	 * Packs, revalidates, and repaints the main window.
 	 */
 	private void refreshWindow() {
 		mainWindow.pack();
 		mainWindow.revalidate();
 		mainWindow.repaint();
 	}
 	
 	/**
 	 * Sets the screen of this window. The current screen is moved to the
 	 * former screen to make room.
 	 * 
 	 * @param screen The screen to switch to.
 	 */
 	private void setScreen(Screen<?> screen) {
 		formerScreen = currentScreen;
 		currentScreen = screen;
 	}
 	
 	/**
 	 * Switches the window to display the specified screen.
 	 * 
 	 * @param screen The screen to switch to.
 	 */
 	private void switchWindow(Screen<?> screen) {
 		setScreen(screen);
 		class Runner implements Runnable {
 			private Screen<?> screen;
 			public Runner(Screen<?> screen) {
 				this.screen = screen;
 			}
 			@Override
 			public void run() {
 				clearWindow();
 				mainWindow.add(menuBar, BorderLayout.NORTH);
 				mainWindow.add(screen, BorderLayout.CENTER);
 				mainWindow.add(messageBox.getComponent(), BorderLayout.SOUTH);
 				refreshWindow();
 				if (!mainWindow.isVisible()) {
 					mainWindow.setVisible(true);
 				}
 				screen.setInitialProperties();
 			}
 		}
 		GraphicalInterface.invokeLaterIfNeeded(new Runner(screen));
 		soundEngine.playMusic(screen.getBackgroundMusic(), false);
 	}
 	
 }
