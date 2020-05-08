 package gui;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * A game state representing the main menu
  */
 public class GUISimulationMenu extends StateBasedGame {
 	public GUISimulationMenu(final String title) {
 		super(title);
 	}
 
 	@Override
 	public void initStatesList(final GameContainer container)
 			throws SlickException {
 		addState(new GameStateMainMenu()); //0
		addState(new GameStateResolution()); // 1
 		addState(new GameStateChangeMap()); // 2
 		addState(new GameStateSimRun()); // 3
 	}
 
 	/**
 	 * Start the simulation, show menu
 	 * @param args
 	 */
 	public static void main(final String[] args) {
 		try {
 			AppGameContainer container = new AppGameContainer(
 					new GUISimulationMenu("traffic simulation "));
 			container.setDisplayMode(container.getScreenWidth(),
 					container.getScreenHeight(), false);
 
 			container.setTargetFrameRate(30);
 			container.setMinimumLogicUpdateInterval(1);
 			container.setMinimumLogicUpdateInterval(2);
 			container.start();
 
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }
