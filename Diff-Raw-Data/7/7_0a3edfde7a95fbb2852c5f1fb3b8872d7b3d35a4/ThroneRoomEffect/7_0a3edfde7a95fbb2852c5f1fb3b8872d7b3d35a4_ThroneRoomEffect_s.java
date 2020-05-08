 package org.jdominion.effects.base;
 
 import org.jdominion.Card;
 import org.jdominion.Player;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
import org.jdominion.Card.Type;
 import org.jdominion.decisions.ChooseActionCardToPlay;
 import org.jdominion.effects.CardEffectAction;
 
 public class ThroneRoomEffect extends CardEffectAction {
 
 	private int numberOfTimesToPlayTheCard;
 	private boolean cancelable;
 
 	public ThroneRoomEffect() {
 		this(2, false);
 	}
 
 	public ThroneRoomEffect(int numberOfTimesToPlayTheCard, boolean cancelable) {
 		super();
 		this.numberOfTimesToPlayTheCard = numberOfTimesToPlayTheCard;
 		this.cancelable = cancelable;
 	}
 
 	@Override
 	public boolean execute(Player activePlayer, Turn currentTurn, Supply supply) {
 		if (!activePlayer.hasActionCardInHand()) {
 			return false;
 		}
 
 		ChooseActionCardToPlay decision = new ChooseActionCardToPlay(activePlayer.getHand(), this.cancelable);
 		activePlayer.decide(decision, this);
 		assert decision.getAnswer().size() == 1;
 		Card choosenCard = decision.getAnswer().getFirst();
 		assert choosenCard.isOfType(Type.ACTION);
 		for (int i = 0; i < numberOfTimesToPlayTheCard; i++) {
 			currentTurn.playCard(activePlayer, supply, choosenCard);
 		}
 
 		return true;
 	}
 
 }
