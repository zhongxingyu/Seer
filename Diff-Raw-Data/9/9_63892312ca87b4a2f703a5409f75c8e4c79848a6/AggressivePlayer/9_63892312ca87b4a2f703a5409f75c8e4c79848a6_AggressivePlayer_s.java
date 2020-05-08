 package game;
 
 import game.play.Board;
 import game.play.Card;
 import game.play.Player;
 import game.play.Suit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.strategy.CardsUtil;
 
 public class AggressivePlayer extends Player {
 
 	public AggressivePlayer(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public int bid(int tricksThisHand, int tricksRemaining, boolean restricted) {
 		if (restricted && tricksRemaining==tricksThisHand)
 		{
 			return tricksThisHand-1;
 		}
 		else
 		{
 			return tricksThisHand;
 		}
 	}
 
 	@Override
 	public Card playCard(Board board) {
 		Suit trump = board.getTrump();
 		Suit lead = board.getLead();
 		List<Card> legalPlays = CardsUtil.legalPlays(this.getHand(), lead);
 		List<Card> winningPlays = new ArrayList<Card>();
 		Card currentlyWinning = CardsUtil.whoWins(board.getCards(), lead, trump);
 		for ( Card card : legalPlays )
 		{
 			if ( card.beats(currentlyWinning, lead, trump) )
 			{
 				winningPlays.add(card);
 			}
 		}
 		Card choice;
 		if (winningPlays.size()==0)
 		{
 			choice = CardsUtil.lowest(legalPlays, trump);
 		}
		choice = CardsUtil.whoWins(winningPlays, lead, trump);
 		assert this.getHand().remove(choice);
 		return choice;
 	}
 
 }
