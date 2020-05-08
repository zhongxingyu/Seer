 package game.play;
 
 import game.score.HandRecord;
 import game.score.ScoreCard;
 
 import java.util.List;
 
 public class GameManager {
 
 	private ScoreCard scoreCard;
 	private Deck deck;
 	private int handNumber;
 	private HandRecord thisHand;
 	private List<Player> players;
 	private Player inLead;
 	private Board board;
 	
 	public GameManager(int numPlayers)
 	{
 		scoreCard = new ScoreCard(numPlayers);
 	
 		for (handNumber = 1; handNumber<=13; handNumber++)
 		{
 			playHand();
 		}
 		
 		inLead = players.get(0);
 		
 	}
 	
 	private void playHand() {
 		board.clear();
 		dealCards();
 		bidRound();
 		for(int i=0;i<cardsThisHand();i++)
 		{
 			inLead = playTrick();
 		}
 		thisHand.assessScores();
 		scoreCard.recordHand(thisHand);
 	}
 
 	/*
 	 * returns the player that won the trick
 	 */
 	private Player playTrick() {
 		int leadOffset = players.indexOf(inLead);
		board.add(inLead.playCard(null));
 		for(int turnIndex = 1; turnIndex<players.size(); turnIndex++)
 		{
			board.add(players.get(turnIndex+leadOffset).playCard(board.get(0).getSuit()));
 		}
 		
		return determineWinner(board);
 		
 	}
 
 	private void bidRound() {
 		int tricksRemaining = cardsThisHand();
 		for (Player player : players)
 		{
 			tricksRemaining -= player.bid(tricksRemaining,false);
 		}
 	}
 
 	private void dealCards() {
 		thisHand = new HandRecord(players);
 		for (Player player : players)
 		{
			player.setHand(new Hand(deck.drawCards(cardsThisHand())));
 		}
 		Card trump = deck.drawCard();
 		thisHand.setTrump(trump.getSuit());
 	}
 
 	/*
 	 * reads the handNumberField and returns the number of cards in the given hand.
 	 * perhaps allow this to be overriden for different variations on the rules. (along with scoring)
 	 */
 	private int cardsThisHand()
 	{
 		return Math.abs(7-handNumber)+1;
 	}
 	
 }
