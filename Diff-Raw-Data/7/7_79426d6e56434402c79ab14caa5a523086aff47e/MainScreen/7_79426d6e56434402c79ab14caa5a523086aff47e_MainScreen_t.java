 import java.awt.event.KeyEvent;
 
 public class MainScreen extends ConsoleKeyListener implements IDrawable {
 	private Gotchi gotchi = null;
 	private GameState state = GameState.GotchiScreen;
 	private IScreen activeScreen = null;
 
 	public void start() {
 		gotchi = new Cat("Murmur");
 		activeScreen = new GotchiScreen(gotchi);
 		Draw();
 	}
 
 	@Override
 	public void Draw() {
 		while (!Javagotchi.Exit) {
 			clearScreen();
 
 			// print the Header
 			System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
 			System.out.println("║ Javagotchi " + Javagotchi.VERSION + "                                                             ║");
 			System.out.println("╟──────────────────────────────────────────────────────────────────────────────╢");
 
 			switch (state) {
 			case CreateGotchi:
 				System.out.println("create");
 				break;
 			case GotchiScreen:
 				activeScreen.Draw();
 				break;
 			case ChooseAction:
 				System.out.println("action");
 				break;
 			}
 
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Clears the console Window
 	 */
 	private void clearScreen() {
 		// just move content out of view
 		for (int i = 0; i < 50; ++i) {
 			System.out.println("\n");
 		}
 	}
 
 	public void keyReleased(KeyEvent e) {
 		if (e.getKeyCode() == 27)
 		{
 			System.exit(0);
 		}
 		else
 		{
 			activeScreen.keyReleased(e);
 			//System.out.println(e.getKeyCode());
 		}
 	}
 	
 	public enum GameState {
 		GotchiScreen, CreateGotchi, ChooseAction
 	}
 }
