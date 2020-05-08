 import java.io.IOException;
 
 import javax.microedition.lcdui.Canvas;
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.Command;
 import javax.microedition.media.*;
 
 import javax.microedition.media.control.*;
 
 /**
  * Game board, statView and everything used to play go.
  *
  * This is main controlling class. It has its own thread
  * to control game, networking etc.
  */
 public class Goban extends Canvas
 	implements Runnable, UIElement.Parent, Arbiter.Parent {
 
 	/**
 	 * Command event when application close was requested.
 	 */
 	public Command exitCmd = new Command("Exit", Command.BACK, 99);
 
 	/**
 	 * Stop playing music command.
 	 */
 	public Command stopMusicCmd = new Command("Stop music", Command.BACK, 41);
 
 	/**
 	 * Start playing music command.
 	 */
 	public Command startMusicCmd = new Command("Start music", Command.BACK, 41);
 
 	/**
 	 * Command event when skip intro was requested.
 	 */
 	public Command skipIntroCmd = new Command("Skip", Command.SCREEN, 99);
 
 	/**
 	 * Element of UI for board display.
 	 */
 	public BoardView boardView = new BoardView(this);
 
 	/**
 	 * Request
 	 */
 	public Command beClientCmd = new Command(
 			"Connect to remote",
 			Command.SCREEN, 12
 			);
 
 	/**
 	 * Request hosting a local server command.
 	 */
 	public Command beServerCmd = new Command("Host game", Command.SCREEN, 11);
 
 	/**
 	 * Request that user want to play white stones (possibly: too) command.
 	 */
 	public Command playWhiteCmd = new Command("Play white", Command.SCREEN, 21);
 
 	/**
 	 * Request that user want to play black stones (possibly: too) command.
 	 */
 	public Command playBlackCmd = new Command("Play black", Command.SCREEN, 22);
 
 	/**
 	 * Help request command.
 	 */
 	public Command printHelpCmd = new Command("Print help", Command.SCREEN, 1);
 
 	/**
 	 * Music player.
 	 */
 	Player player;
 
 	/**
 	 * Element of UI for statView display and such.
 	 */
 	public StatView statView = new StatView(this);
 
 	/**
 	 * Element of UI for chat and logs.
 	 */
 	public LogView logView = new LogView(this);
 
 	/**
 	 * Board representation.
 	 */
 	public Board board = null;
 
 	/**
 	 * Is game thread in state of being stopped.
 	 */
 	private boolean stopped = false;
 
 	/**
 	 * Is game paused.
 	 */
 	private boolean paused = false;
 
 	/**
 	 * Intro.
 	 *
 	 * Nil if none.
 	 */
 	private Intro demo;
 
 
 	/**
 	 * Is timer panel hidden?
 	 */
 	public boolean timerToggled = true;
 
 	/**
 	 * Is chat panel hidden?
 	 */
 	public boolean chatToggled = false;
 
 	/**
 	 * Local game controller.
 	 */
 	public LocalGameController gameController;
 	public Arbiter arbiter;
 	/**
 	 * Ctor.
 	 */
 	public Goban() {
 		logView.appendString("Welcome in DPC Goban!");
 		logView.appendString("Connect to or host a game to start.");
 		logView.appendString("---");
 
 		addCommand(skipIntroCmd);
 
 		try {
 			demo = new Cube(this);
 		} catch (Exception e) {
 			/* fails on problems with textures etc. */
 			skipIntro();
 			logView.appendString("Couldn't start intro. Sorry.");
 		}
 		addCommand(stopMusicCmd);
 		startMusic();
 
 		board = new Board(boardView);
 	}
 
 	/**
 	 * Create local go server (arbiter) and
 	 * play game locally, listening for
 	 * incoming players.
 	 */
 	public void beServer() {
 		shutdown();
 		try {
 			LocalArbiter arbiter = new LocalArbiter(this);
 			gameController = new LocalGameController(board, logView);
 			arbiter.connected(gameController);
 			this.arbiter = arbiter;
 		} catch (LocalArbiter.CreationError e) {
 			logView.appendString(
 				"Couldn't create server. Reason: `"
 				+ e.getMessage() + "'"
 				);
 		}
 	}
 
 	public void handleArbiterInitFinished() {
 		repaintUI();
 	}
 
 	/**
 	 * Try connecting to some remote game
 	 * and play game there.
 	 */
 	public void beClient() {
 		shutdown();
 		try {
 			RemoteArbiter arbiter = new RemoteArbiter(
 					this,
 					RemoteArbiterTransport.BLUETOOTH
 					);
 			gameController = new LocalGameController(board, logView);
 			arbiter.connected(gameController);
 			this.arbiter = arbiter;
 		} catch (RemoteArbiter.CreationError e) {
 			logView.appendString(
 				"Couldn't connect to server. Reason: `"
 				+ e.getMessage() + "'"
 				);
 		}
 	}
 
 	public void shutdown() {
 		if (arbiter != null) {
 			arbiter.shutdown();
 			arbiter = null;
 		}
 		if (gameController != null) {
 			gameController.shutdown();
 			gameController = null;
 		}
 	}
 
 	/**
 	 * Request from current server giving user control over the black stones.
 	 */
 	public void playBlack() {
 		if (gameController != null) {
 			gameController.handleColor(Board.COLOR_BLACK);
 		}
 	}
 
 	/**
 	 * Request from current server giving user control over white stones.
 	 */
 	public void playWhite() {
 		if (gameController != null) {
 			gameController.handleColor(Board.COLOR_WHITE);
 		}
 	}
 
 	/**
 	 * Print basic user help.
 	 */
 	public void printUIHelp() {
 		logView.appendString("1, 3 - zoom in/out");
 		logView.appendString("7, 9 - toggle side, log bars");
 		logView.appendString("arrows - move; action - place stone");
 	}
 
 	/**
 	 * Main paint() function for this midlet.
 	 */
 	public void paint(Graphics g) {
 		long time = System.currentTimeMillis();
 		if (demo != null) {
 			demo.paint(g, time);
 			if (demo.done()) {
 				skipIntro();
 			}
 			return;
 		}
 
 		statView.paint(g);
 		boardView.paint(g);
 		logView.paint(g);
 	}
 
 	/**
 	 * Thread runs here.
 	 */
 	public void run() {
 		while (!stopped) {
 			if (!paused) {
 				repaint();
 			}
 
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	/**
 	 * Skip intro mode.
 	 */
 	public void skipIntro() {
 		removeCommand(skipIntroCmd);
 		addCommand(exitCmd);
 		addCommand(playWhiteCmd);
 		addCommand(playBlackCmd);
 		addCommand(beServerCmd);
 		addCommand(beClientCmd);
 		addCommand(printHelpCmd);
 		demo = null;
 		repaintUI();
 	}
 
 	/**
 	 * Stop game.
 	 */
 	public void stop() {
 		stopped = true;
 	}
 
 	/**
 	 * Start playing music.
 	 */
 	public void startMusic() {
 		removeCommand(startMusicCmd);
 		addCommand(stopMusicCmd);
 
 		// TODO: below makes OutOfMemoryException
 		try
 		{
 			player = Manager.createPlayer(
 				getClass().getResourceAsStream("/intro.midi"), "audio/midi"
 				);
			// TODO: XXX: FIXME: setting volume makes player silent
			//VolumeControl vc = (VolumeControl) player.getControl("VolumeControl");
			//vc.setLevel(100);
 			player.start();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		} catch (OutOfMemoryError e) {
 			System.out.println(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Stop playing music.
 	 */
 	public void stopMusic() {
 		removeCommand(stopMusicCmd);
 		addCommand(startMusicCmd);
 		try
 		{
 			player.stop();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 	}
 
 	/**
 	 * Handle key presses.
 	 */
 	public void keyPressed(int keycode) {
 		if (board == null) {
 			return;
 		}
 
 		switch (getGameAction(keycode)) {
 			case UP:
 				board.moveCrosshair(Board.MOVE_UP);
 				break;
 			case DOWN:
 				board.moveCrosshair(Board.MOVE_DOWN);
 				break;
 			case LEFT:
 				board.moveCrosshair(Board.MOVE_LEFT);
 				break;
 			case RIGHT:
 				board.moveCrosshair(Board.MOVE_RIGHT);
 				break;
 			case GAME_A:
 				board.zoomView(Board.ZOOM_IN);
 				break;
 			case GAME_B:
 				board.zoomView(Board.ZOOM_OUT);
 				break;
 			case GAME_C:
 				timerToggled = !timerToggled;
 				repaintUI();
 				break;
 			case GAME_D:
 				chatToggled = !chatToggled;
 				repaintUI();
 				break;
 			case FIRE:
 				if (gameController != null) {
 					gameController.moveRequest();
 				}
 				break;
 
 		}
 	}
 
 	/**
 	 * Is game currently active.
 	 */
 	public boolean isActive() {
 		if (gameController == null) {
 			return false;
 		}
 
 		if (!gameController.isActive()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Mark whole UI to be repainted.
 	 */
 	public void repaintUI() {
 		statView.markDirty();
 		boardView.markDirty();
 		logView.markDirty();
 	}
 
 	/**
 	 * Current screen division point x-coordinated.
 	 */
 	public int getXDiv() {
 		if (timerToggled) {
 			return getWidth();
 		}
 		return getWidth() * 3 / 4;
 	}
 
 	/**
 	 * Current screen division point y-coordinated.
 	 */
 	public int getYDiv() {
 		if (chatToggled) {
 			return getHeight();
 		}
 		return getHeight() * 5 / 6;
 	}
 
 	/**
 	 * Screen X size.
 	 */
 	public int getXSize() {
 		return getWidth();
 	}
 
 	/**
 	 * Screen Y size.
 	 */
 	public int getYSize() {
 		return getHeight();
 	}
 
 	/**
 	 * Handle msg requests from RemoteArbiter.
 	 */
 	public void handleArbiterMsg(String s) {
 		logView.appendString(s);
 	}
 }
