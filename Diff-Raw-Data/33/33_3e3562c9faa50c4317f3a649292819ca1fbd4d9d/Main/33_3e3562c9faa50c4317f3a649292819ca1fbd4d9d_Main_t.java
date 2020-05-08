 package internal.main;
 
 import api.GameLogic;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.hli.StaticScreenGame;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.vpe.VanillaAARectangle;
 
 import internal.util.StateManager;
 import internal.view.BoardDisplay;
 import internal.view.View;
 import api.struct.Board;
 import internal.util.UnitConversion;
 import jig.engine.GameClock.Alarm;
 
 /**
  *
  * @author Andrew Blaine
  */
 public class Main extends StaticScreenGame {
 
 	private enum GameState { INIT, PLAYING, GAMEOVER }
 	private final StateManager<GameState> stateManager = new StateManager<GameState>(GameState.INIT);
 
 	public static final String RSC_PATH = "internal/resources/";
 	public static final String SPRITE_SHEET = RSC_PATH + "spritesheet.gif";
 
 	private AbstractBodyLayer<VanillaAARectangle> tileLayer;
 
 	private View view;
 	private Match match;
 
 	public Main() {
 		super(View.WORLD_WIDTH, View.WORLD_HEIGHT, false);
 		ResourceFactory.getFactory().loadResources(RSC_PATH, "resources.xml");
 		gameframe.setTitle("Othello");
 
 		tileLayer = new AbstractBodyLayer.IterativeUpdate<VanillaAARectangle>();
 
 		gameObjectLayers.add(tileLayer);
 
 		Board board = BoardFactory.createDefaultOthelloBoard();
 		BoardDisplay boardDisplay = new BoardDisplay(board, tileLayer);
 		view = new View(boardDisplay);
		int seconds = 10;
 
 		Alarm alarm = theClock.setAlarm(UnitConversion.secondToNanosecond(seconds));
 		Player dark = new Player("RandomPlayer");
 		Player light = new Player("SlowRandomPlayer");
 		GameLogic._init(alarm);
 		match = new Match(alarm, board, dark, light);
 	}
 
 	@Override
 	public void update(final long deltaMs) {
 		super.update(deltaMs);
 		switch(stateManager.getCurState()) {
 			case INIT:
 				match.update(deltaMs);
 				break;
 			case PLAYING:
 				break;
 			case GAMEOVER:
 				break;
 		}
 	}
 
 	@Override
 	public void render(final RenderingContext rc) {
 		super.render(rc);
 	}
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 		Main main = new Main();
 		main.run();
 	}
 }
