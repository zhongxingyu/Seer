 package fr.lo02.model.card.remedyCards;
 
 import fr.lo02.model.Match;
 import fr.lo02.model.Player;
 import fr.lo02.model.card.Card;
 import fr.lo02.model.card.HazardCards.SpeedLimit;
 import fr.lo02.model.card.HazardCards.Stop;
 import fr.lo02.model.exception.NotValidCardOnBattleException;
 
 public class EndOfLimit extends Card {
 
 	public EndOfLimit(){
 		this.setFileName("Fin_limite.jpg");
 		this.setRemedyCard(true);
 	}
 	
 	public Player checkValidMove(Player activePlayer, Player targetPlayer) throws NotValidCardOnBattleException {
 		Player p = null;
 		 if (activePlayer.getLastCardFromSpeed() instanceof SpeedLimit) {
 			 p = activePlayer;
 		 }
 		 else {
 			 throw new NotValidCardOnBattleException("Vous n'est pas sous limitation de vitesse.");
 		 }
 		return p;
 	}
 
 	@Override
 	public Card playThisCard(Player activePlayer, Player targetedPlayer) {
 		targetedPlayer.addToSpeed(this);
 		super.playThisCard(activePlayer, targetedPlayer);
 
 		for (int i = 0; i < 2; i++) {
			Match.getInstance().addToDiscardStack(activePlayer.getLastCardFromSpeed());
//			activePlayer.deleteLastCardFromBattle();
 		}
 		return null;
 	}
 
 }
