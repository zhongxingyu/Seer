 package de.thm.ateam.memory.game;
 
 import java.util.ArrayList;
 import de.thm.ateam.memory.engine.type.Player;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class GameActivity extends Activity{
 	
 	Memory mem;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.test); // i hate xml files, so fuck them.
 		
 		ArrayList<Player> players = PlayerList.getInstance().session; // just a reference
 		
 		Collections.shuffle(PlayerList.getInstance().session); // we want a different order each time
 		
 		/*
 		 * this is where the gameactivity initializes its specific game, e.g. a descendant from "Game.java",
 		 * the method assembleLayout() creates a grid view holding the specific cards.
 		 * 
 		 * This is also the spot where the Game should be merged into the rest of the Application.
 		 * assembleLayout() does not need any kind of XML File, which makes it very versatile in its use.
 		 * 
 		 */
 		mem = new Memory(this,new MemoryAttributes(players, 2, 4));
 		setContentView(mem.assembleLayout());
 	}
 	
 	/**
 	 * 
 	 * Function to clear the Pictures
 	 * 
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mem.onDestroy();
 	}
 	/*
 	 * if we decide to do some eventhandling for network usage (messages to be more specific) we should do that here.
 	 * A descendant of a Game will have to handle that specific request. 
 	 */
 }
