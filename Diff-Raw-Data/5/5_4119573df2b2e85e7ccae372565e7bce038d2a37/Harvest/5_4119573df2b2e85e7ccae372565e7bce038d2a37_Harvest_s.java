 package bohnanza.core.shared.actions;
 import java.util.ArrayList;
 import bohnanza.core.Action;
 import bohnanza.core.Card;
 import bohnanza.core.Field;
 import bohnanza.core.GameBase;
 import bohnanza.core.IllegalActionException;
 import bohnanza.core.Player;
 
 public class Harvest extends Action<GameBase> {
 
	private final Field<Card> field;
 
	public Harvest(GameBase game, Player initiator, Field<Card> field) {
 		super(game, initiator);
 		this.field = field;
 	}
 
 	@Override
 	/**Harvest specified field from a Player
 	 * @throws IllegalActionException if harvesting of specified field is not allowed */
 	protected void innerHandle() throws IllegalActionException {
 		ArrayList<Card> discard = new ArrayList<Card>();
 		discard = initiator.harvestField(field);
 		for(Card card : discard) {
 			game.addCardToDiscardPile(card);
 		}
 	}
 
 }
