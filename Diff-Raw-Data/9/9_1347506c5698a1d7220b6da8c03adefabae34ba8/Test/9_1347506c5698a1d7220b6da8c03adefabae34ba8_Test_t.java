 package org.ojim;
 
 import org.ojim.client.gui.GUIClient;
 import org.ojim.logic.rules.ActionMoveToField;
 import org.ojim.logic.rules.Card;
 import org.ojim.logic.state.Field;
 import org.ojim.logic.state.GameState;
 
 /**
  * NUR EINE BEISPIELKLASSE!
  * @author xZise
  */
 public class Test {
 	
 	private Card[] cards;
 	
 	private static final int foo = 8;
 	
 	private Test() {
 		
 		GUIClient gc = new GUIClient();
 		
 		GameState state = new GameState(foo);
 		
 		this.cards = new Card[10];
 		for (int i = 0; i < this.cards.length; i++) {  /* Hier kann eine andere Aktion hin, oder mehrere hintereinander */
			this.cards[i] = new Card("Beispielkarte #" + i, state, false, new ActionMoveToField(state, false, new Field(state)));
 		}
 	}
 }
