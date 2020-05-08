 package game;
 
 import com.google.gson.Gson;
 
 /**
  * Local test driver
  * 
  * @author Jacob Charles
  */
 public class TestDriver {
 
 	/**
 	 * Local game testing
 	 * 
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		//initialize
 		Wardrobe.init();
 		SoundBank.init();
 		Controller c = new Controller();
 		RetroView v = new RetroView();
 		//MenuState ms = new MenuState();
 		StopWatch t = new StopWatch(20);
 		v.attachController(c);
 
 		//menu test loop
 		int prog = 0;
 		/*while (prog == 0 && v.isVisible()) {
 			t.loopStart();
 			c.update();
 			prog = ms.update(c);
 			v.reDraw(ms);
 			t.loopRest();
 		}*/
 
 		//menu based actions
 		if (prog == MenuState.HOST); //TODO: Host, in theory
 		if (prog == MenuState.JOIN); //TODO: Join, in theory
 		if (prog == MenuState.QUIT) v.setVisible(false);
 
 		//new game starting
 		ServerGameState gs = new ServerGameState();
 
 		//test players
 		gs.addPlayer();
 		//gs.addPlayer();
 		//gs.addPlayer();
 		//gs.addPlayer();
 
 		//main loop
		while (v.isVisible()) {
 			t.loopStart();
 			c.update();
 			for (int i = 0; i < gs.getNumberOfPlayers(); i++){
 				gs.readControls(gs.getPlayer(i), c); //using a hackish version to not create a Participant
 			}
 			gs.update();
 			v.reDraw(gs.convert());
 			t.loopRest();
 		}
 		System.exit(0);
 	}
 }
