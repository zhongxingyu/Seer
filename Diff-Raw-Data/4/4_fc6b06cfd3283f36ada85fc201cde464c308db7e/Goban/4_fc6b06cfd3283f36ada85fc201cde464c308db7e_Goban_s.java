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
 	public Command stopMusicCmd = new Command(
 			"Stop music", Command.SCREEN, 41
 			);
 
 	/**
 	 * Start playing music command.
 	 */
 	public Command startMusicCmd = new Command(
 			"Start music", Command.SCREEN, 41
 			);
 
 	/**
 	 * Command event when skip intro was requested.
 	 */
 	public Command skipIntroCmd = new Command("Skip", Command.BACK, 99);
 
 	/**
 	 * Command event when skip intro was requested.
 	 */
 	public Command passReqCmd = new Command("Pass move", Command.SCREEN, 2);
 
 	/**
 	 * Element of UI for board display.
 	 */
 	public BoardView boardView = new BoardView(this);
 
 	/**
 	 * Request
 	 */
 	public Command beClientCmd = new Command(
 			"Join game",
 			Command.SCREEN, 12
 			);
 
 	/**
 	 * Request hosting a local server command.
 	 */
 	public Command beServerCmd = new Command("Start game", Command.SCREEN, 11);
 
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
 	public Command printHelpCmd = new Command("Print help", Command.SCREEN, 2);
 
 	/**
 	 * Help request command.
 	 */
 	public Command moveReqCmd = new Command("Move", Command.SCREEN, 1);
 
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
 		logView.appendString(
 				"Connect to or host a game to start. "
 				+ "Use help to see list of available shortcuts."
 				);
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
 		logView.appendString("To start/join a game use menu commands.");
 		logView.appendString("When in active game you can:");
 		logView.appendString(" - move (arrows)");
 		logView.appendString(" - place stone (action button)");
 		logView.appendString(" - toggle log visibility (model dependent)");
 		logView.appendString(" - zoom view in/out (model dependent)");
 		logView.appendString("more help: http://dpc.wikidot.com/lab:dpcgoban");
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
 		addCommand(moveReqCmd);
 		addCommand(passReqCmd);
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
 			player.realize();
 			VolumeControl vc = (VolumeControl) player.getControl("VolumeControl");
 			vc.setLevel(10);
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
 				board.zoomView(Board.ZOOM_OUT);
 				break;
 			case GAME_B:
 				board.zoomView(Board.ZOOM_IN);
 				break;
 			case GAME_C:
 				chatToggled = !chatToggled;
 				repaintUI();
 				break;
 			case GAME_D:
 				//timerToggled = !timerToggled;
 				//repaintUI();
 				break;
 			case FIRE:
 				moveReq();
 				break;
 		}
 	}
 
 	public void moveReq() {
 		if (gameController != null) {
 			gameController.moveRequest();
 		}
 	}
 
 	public void passReq() {
		gameController.passRequest();
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
 		return getYDivOn();
 	}
 
 	public int getYDivOn() {
 		return getHeight() * 2 / 3;
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
