 package states;
 
 import input.Controls;
 import input.Controls.Action;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import main.MainGame;
 import map.Map;
 
 import org.jbox2d.callbacks.DebugDraw;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.FontUtils;
 
 import time.Time;
 import time.Timer;
 import ui.Cursor;
 import ui.DebugInfo;
 import ui.PauseScreen;
 import ui.TimeBar;
 import ui.Transitions;
 import util.Box2DDebugDraw;
 import camera.Camera;
 
 import combat.CombatContact;
 
 import config.Biome;
 import config.Config;
 import config.Level;
 import config.Section;
 import entities.Player;
 import entities.Salmon;
 import entities.StaticObstacle;
 import entities.Steam;
 import entities.enemies.Enemy;
 
 public class LevelState extends BasicGameState{
 	private Section section;
 	private Map map;
 	private Player player;
 	private ArrayList<Enemy> enemies;
 	private ArrayList<StaticObstacle> staticObjects;
 	private ArrayList<Steam> steams;
 	private static Box2DDebugDraw debugdraw;
 	private boolean viewDebug = false;
 	public static boolean godMode = false;
 	public static boolean slowMode = false;
 	public static boolean replayMode = false;
 	private boolean timerGo = false;
 	private static Camera camera;
 	private Cursor cursor;
 	private Vec2 goalLoc;
 	private Image background;
 	private Timer timer;
 	private Biome biome;
 	private static TimeBar timerBar;
 	private static DebugInfo info;
 	private static PauseScreen pauseScrn;
 	
 	private UnicodeFont pauseFont;
 	private float pauseCounter;
 	
 	private static Music forestLoop;
 	private static UnicodeFont plainFont;
 	private static UnicodeFont boldFont;
 	
 	static {
 		debugdraw = new Box2DDebugDraw();
 		debugdraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit | DebugDraw.e_centerOfMassBit);
 		info = new DebugInfo(Config.RESOLUTION_WIDTH - 500, 100);
 		pauseScrn = new PauseScreen();
 		try {
 			forestLoop = new Music("assets/sounds/Song1.wav");
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		plainFont = Config.PLAIN_FONT;
         
         boldFont = Config.BOLD_FONT;
         
 	}
 	
 	public LevelState(Section section) {
 		super();
 		this.section = section;
 		biome = section.getBiome();
 		if (Config.times.containsKey(section)) timer = Config.times.get(section);
 		else {
 			timer = new Timer();
 			Config.times.put(section, timer);
 		}
 	}
 		
 	@Override
 	public void init(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		timerBar = new TimeBar(gc, plainFont, boldFont);
 		Salmon.timerBar = timerBar;
 		forestLoop.loop();
 		forestLoop.setVolume(0f);
 		if (Config.soundOn) {
 			forestLoop.fade(2000, 1f, false);
 		} else {
 			forestLoop.setVolume(Config.gameVolume);
 			forestLoop.pause();
 		}
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics graphics)
 			throws SlickException {
 		camera.translateGraphics(gc);
 		drawBackground(graphics, gc, game);
 		camera.untranslateGraphics(gc);
 		camera.drawMap();
 		timerBar.render(gc, graphics, timer, timerGo);
 		camera.translateGraphics(gc);
 		
 		if (viewDebug) {
 			debugdraw.setGraphics(graphics);
 			map.getWorld().drawDebugData();
 			camera.untranslateGraphics(gc);
 			info.render(graphics);
 			camera.translateGraphics(gc);
 		} else {
 			for (Enemy e : enemies) {
 				e.render(graphics, biome);
 			}
 			for (StaticObstacle s : staticObjects) {
 				s.render(graphics);
 			}
 			for (Steam s : steams) {
 				s.render(graphics);
 			}
 			player.render(graphics);
 		}
 		cursor.render(graphics);
 		
 		// so that transitions render correctly
 		camera.untranslateGraphics(gc);
 		if (replayMode) {
 			plainFont.drawString(0, Config.RESOLUTION_HEIGHT- plainFont.getHeight("Replay On"), "Replay On");
 		}
 		String mapName = section.getDisplayName();
 		plainFont.drawString(Config.RESOLUTION_WIDTH/2 - plainFont.getWidth(mapName)/2, 0, mapName);
 
 		if (gc.isPaused()) pauseScrn.render(gc, graphics);
 		else renderPauseNotice(gc, graphics);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta)
 			throws SlickException {
 		Controls.update(gc);
 		
 		// check toggles for special commands
 		// TODO: remove
 		if (Controls.isKeyPressed(Action.DEBUG)) viewDebug = !viewDebug;
 		if (Controls.isKeyPressed(Action.GOD_MODE)) godMode = !godMode;
 		if (Controls.isKeyPressed(Action.SLOW_DOWN)) slowMode = !slowMode;
 		if (Controls.isKeyPressed(Action.REPLAY)) replayMode = !replayMode;
 
 		// check for these even if game is paused
 		if (Controls.isKeyPressed(Action.MUTE)) {
 			if (Config.soundOn) {
 				forestLoop.pause();
 				Config.soundOn = false;
 			} else {
 				forestLoop.resume();
 				Config.soundOn = true;
 			}
 		}
 		if (Controls.isKeyPressed(Action.FULLSCREEN)) MainGame.setFullscreen((AppGameContainer) gc, !gc.isFullscreen());
		if (Controls.isKeyPressed(Action.RESET)) { reset(game); }
 		if (Controls.isKeyPressed(Action.SKIP)) { nextLevel(game); }
 		
 		// show pause screen if paused
 		if (gc.isPaused()) { pauseScrn.update(gc, game, delta); return; }
 		
 		// should go after pause screen update
 		if (Controls.isKeyPressed(Action.PAUSE) || !gc.hasFocus()) pause(gc);
 		
 		// slooooow dooooown
 		if (slowMode) delta /= 10;
 		
 		if (Controls.moveKeyPressed()) {
 			timerGo = true;
 			pauseCounter = -Config.PAUSE_BLINK;
 		}
 		
 		pauseCounter += delta;
 		if (pauseCounter > 0) pauseCounter = pauseCounter % Config.PAUSE_BLINK;
 		
 		// if the goal is reached
 		if (closeToGoal()) {
 			timer.updateRecords();
 			Config.saveTimes();
 			if (!replayMode) {
 				nextLevel(game);
 			}
 			else {
 				this.reset(game);
 			}
 		}
 		
 		// update world
 		map.getWorld().step(delta/1000f, Config.VELOCITY_ITERATIONS, Config.POSITION_ITERATIONS);
 		
 		// update player
 		player.update(gc, delta);
 		
 		// update enemies
 		for (Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
 			Enemy e = it.next();
 			if (e.getHp() > 0) {
 				e.update(gc, delta, player);
 			} else {
 				it.remove();
 				e.kill();
 			}
 		}
 		
 		for (Steam s : map.getSteams()) {
 			s.update(gc, delta);
 		}
 		
 		for (StaticObstacle s : staticObjects) {
 			s.update(gc, delta);
 		}
 
 		camera.centerOn(player.getX(),player.getY());
 		cursor.update(gc, delta);
 		if (timerGo) {
 			timer.update(delta);
 		}
 		timerBar.update(gc, game, delta);
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame game)
 			throws SlickException {
 		super.enter(gc, game);
 		timerGo = false;
 		map = new Map(section.getMapPath(), new Vec2(0, Config.GRAVITY));
 		map.parseMapObjects();
 		map.getWorld().setContactListener(new CombatContact());
 		map.getWorld().setDebugDraw(debugdraw);
 		
 		background = new Image(section.getBackgroundPath());
 		background = background.getScaledCopy((float) Math.max(map.getHeight(), Config.RESOLUTION_HEIGHT) / background.getHeight());
 
 		player = MainGame.player;
 		player.addToWorld(map.getWorld(), map.getPlayerLoc().x, map.getPlayerLoc().y 
 				+ (Config.TILE_HEIGHT / 2) - (Config.PLAYER_HEIGHT / 2)); // move up to avoid getting stuck in the ground
 		player.reset();
 		
 		this.timer.setGoal(new Time(section.getGoalTime()));
 
 		goalLoc = map.getGoalLoc();
 		camera = new Camera(gc, map.getTiledMap());
 		cursor = new Cursor(player);
 
 		timer.reset();
 		timerBar.enter(gc, game, timer);
 
 		pauseFont = Config.MENU_FONT;
 		pauseCounter = -Config.PAUSE_BLINK;
 		
 		enemies = map.getEnemies();
 		staticObjects = map.getStaticObjects();
 		steams = map.getSteams();
 		World world = map.getWorld();
 		for (Enemy e : enemies) {
 			e.addToWorld(world, e.getX(), e.getY());
 		}
 		for (StaticObstacle s : staticObjects) {
 			s.addToWorld(world, s.getX(), s.getY(), timer.getCurrentTime());
 		}
 	}
 
 	private void pause(GameContainer gc) {
 		gc.setPaused(true);
 		gc.setTargetFrameRate(Config.INACTIVE_FRAME_RATE);
 	}
 
 	private boolean closeToGoal() {
 		return Math.abs(player.getCenterX()-goalLoc.x) < 30 && Math.abs(player.getCenterY() - goalLoc.y) < 30;
 	}
 	
 	private void nextLevel(StateBasedGame game) {
 		ResultsState.recordResult(section);
 		if (Level.isQueueEmpty()) game.enterState(ResultsState.ID, Transitions.fadeOut(), Transitions.fadeIn());
 		else game.enterState(Level.getNextSection().getID(), Transitions.fadeOut(), Transitions.fadeIn());
 	}
 	
 	private void reset(StateBasedGame game) {
 		game.enterState(getID(), Transitions.fadeOut(), Transitions.fadeIn());
 	}
 	
 	// kinda janky, remove when paralaxing set up
 	private void drawBackground(Graphics graphics, GameContainer gc, StateBasedGame game) {
 		int backgroundX = -Config.RESOLUTION_WIDTH;
 		while (backgroundX < map.getWidth()){
 			graphics.drawImage(background,  backgroundX,  map.getHeight() > Config.RESOLUTION_HEIGHT ? 0 : map.getHeight() - Config.RESOLUTION_HEIGHT);
 			backgroundX += background.getWidth();
 		}
 	}
 	
 	private void renderPauseNotice(GameContainer gc, Graphics graphics) {
 		if (pauseCounter > 0 && pauseCounter < Config.PAUSE_BLINK / 2) {
 			FontUtils.drawRight(pauseFont, "Press 'Escape' to pause  ", 0,
 					Config.RESOLUTION_HEIGHT - Config.MENU_FONT.getLineHeight(), Config.RESOLUTION_WIDTH);
 		}
 	}
 	
 	@Override
 	public int getID() {
 		return section.getID();
 	}
 	
 	public static Camera getCamera() {
 		return camera;
 	}
 }
