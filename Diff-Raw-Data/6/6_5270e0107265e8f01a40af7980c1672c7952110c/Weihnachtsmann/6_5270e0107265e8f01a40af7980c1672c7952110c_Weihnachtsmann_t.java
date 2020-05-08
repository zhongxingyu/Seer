 package de.nordakademie.nakjava.gamelogic.cards.zauberlabor;
 
 import java.util.Map;
 
 import de.nordakademie.nakjava.gamelogic.cards.impl.AbstractCard;
 import de.nordakademie.nakjava.gamelogic.cards.impl.ArtifactEffect;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Card;
 import de.nordakademie.nakjava.gamelogic.cards.impl.CardLibrary;
 import de.nordakademie.nakjava.gamelogic.cards.impl.Target;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Kristalle;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Monster;
 import de.nordakademie.nakjava.gamelogic.shared.artifacts.ressources.Ziegel;
 import de.nordakademie.nakjava.gamelogic.shared.cards.CardType;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.CardSet;
 import de.nordakademie.nakjava.gamelogic.shared.playerstate.PlayerState;
 import de.nordakademie.nakjava.server.internal.model.InGameSpecificModel;
 
 @Card(name = "Weihnachtsmann",
 		type = CardType.ZAUBERLABOR,
 		artifactEffects = { @ArtifactEffect(artifact = Ziegel.class,
 				count = 5,
 				target = Target.SELF),
 				@ArtifactEffect(artifact = Monster.class,
 						count = 5,
 						target = Target.SELF),
 				@ArtifactEffect(artifact = Kristalle.class,
 						count = 5,
 						target = Target.SELF) },
 		additionalDescription = "Zufällige Karte mit Kosten>14 vom Vorratsstapel oder Kartenfriedhof auf die Hand.")
 public class Weihnachtsmann extends AbstractCard {
 
 	@Override
 	protected void performAction(Map<Target, PlayerState> states) {
 		super.performAction(states);
 		CardSet selfCards = ((InGameSpecificModel) states.get(Target.SELF)
 				.getStateSpecificModel()).getCards();
 		int passedCards = 0;
 		String drawnCard;
		boolean cardFound = false;
		while (!cardFound && passedCards < selfCards.getCardSetSize()) {
 			drawnCard = selfCards.drawCardFromDeck();
 			passedCards++;
 			if (CardLibrary.get().getCardForName(drawnCard).getTotalCosts() < 15) {
 				selfCards.discardCardFromHand(drawnCard);
			} else {
				cardFound = true;
 			}
 		}
 	}
 
 }
