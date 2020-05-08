 package yotris.logic;
 import yotris.logic.GameState;
 import yotris.ui.UserInterface;
 import yotris.util.Settings;
 
 public class GameLogic {
 	private UserInterface ui;
 	private Grid grid;
 	private Settings settings;
 
 	public GameLogic(UserInterface ui, Settings settings)	 {
 		this.ui = ui;
		this.reset();
 	}
 
	private void reset() {
 		this.grid = new Grid(settings.getGridWidth(), settings.getGridHeight());	
 	}
 
 	public GameState update() {
 		GameState state = new GameState(true);	
 		state.running = false;
 
 		return state;
 	}
 }
