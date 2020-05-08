 /**
  * 
  */
 package edu.txstate.hearts.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.awt.EventQueue;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 
 import edu.txstate.hearts.gui.ConfigurationUI;
 import edu.txstate.hearts.gui.HeartsUI;
 import edu.txstate.hearts.gui.HeartsUI.CardAction;
 import edu.txstate.hearts.gui.HeartsUI.Position;
 import edu.txstate.hearts.gui.RulesWindow;
 import edu.txstate.hearts.model.*;
 import edu.txstate.hearts.model.Card.Face;
 import edu.txstate.hearts.model.Card.Suit;
 import edu.txstate.hearts.utils.RiskThresholds;
 
 /**
  * @author Neil Stickels, I Gede Sutapa
  * 
  */
 public class Hearts implements ActionListener
 {
 	public enum CardAction
 	{
 		Passing, Playing, Idle
 	}
 	
 	public enum Passing
 	{
 		Left, Right, Front, Stay
 	}
 	
 	private Position currentPlayingPosition;
 	private int numCardsSelectedToPass;
 	private boolean cardsReadyToPass;
 	private CardAction currentCardAction;
 	private HeartsUI heartsUI;
 	private ConfigurationUI configurationUI;
 	private Deck deck;
 	private List<Player> players;
 	private List<Card> cardsSelectedToPass;
 	private List<JButton> buttonCardsSelectedToPass;
 	private Card cardSelectedToPlay;
 	private JButton buttonCardSelectedToPlay;
 	private boolean cardChosenToPlay;
 	private boolean cardReadyToPlay;
 	private int turnsPlayed;
 	private boolean heartsBroken; // flag when hearts broken to allow a heart as
 									// first card played
 	private boolean notifyHeartsBroken; // implement notification
 										// "Hearts has been broken"
 	private Passing passing;
 	private int endScore;
 	public final static boolean silent = false;
 	private final static int GAMES_TO_RUN = 1;
 	private Set setofusers;
 	private final static boolean runUI = true;
 	private boolean showOpponentCards = true;
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Set getSetofusers()
 	{
 		return setofusers;
 	}
 	
 	/**
 	 * @param args
 	 */
 	/*
 	 * public static void main(String[] args) { final Hearts game = new
 	 * Hearts();
 	 * 
 	 * if(runUI) { EventQueue.invokeLater(new Runnable() { public void run() {
 	 * try { ConfigurationUI window = new ConfigurationUI(game);
 	 * window.getFrmConfigurationWindow().setVisible(true); } catch (Exception
 	 * e) { e.printStackTrace(); } } }); } else { game.oldInitialize();
 	 * game.runGame(); }
 	 * 
 	 * //game.initialize(); //game.runGame(); }
 	 */
 	
 	public Hearts()
 	{
 		
 	}
 	
 	public void addUI(HeartsUI heartsUI)
 	{
 		this.heartsUI = heartsUI;
 	}
 	
 	public void run()
 	{
 		this.configurationUI = new ConfigurationUI(this);
 		configurationUI.showDialog();
 //		String playerName = "Mr.Awesome";
 //		int endScore = 100;
 //		String levelOfDifficulty = "Master";
 //		
 //		initialize(playerName, endScore, levelOfDifficulty);
 //		runGame();
 	}
 	
 	public void initialize(String playerName, int endScore,
 			String levelOfDifficulty)
 	{
 		this.passing = Passing.Left; // initial
 		this.endScore = endScore; // default
 		this.cardsSelectedToPass = new ArrayList<Card>(3);
 		this.buttonCardsSelectedToPass = new ArrayList<JButton>(3);
 		
 		this.players = new ArrayList<Player>(4);
 		
 		Player player1 = new User(playerName, 0);
 		Player player2;
 		Player player3;
 		Player player4;
 		
 		if(levelOfDifficulty.equalsIgnoreCase("Easy"))
 		{
 			player2 = new AgentGoofy("Neil", 1);
 			player3 = new AgentGoofy("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 		else if(levelOfDifficulty.equalsIgnoreCase("Medium"))
 		{
 			player2 = new AgentGoofy("Neil", 1);
 			player3 = new AgentAggressive("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 		else
 		{
 			player2 = new AgentAggressive("Neil", 1);
 			player3 = new AgentAggressive("Jonathan", 2);
 			player4 = new AgentDetermined("Maria", 3);
 		}
 		
 		players.add(player1);
 		players.add(player2);
 		players.add(player3);
 		players.add(player4);
 	}
 	
 	private void shuffleCards()
 	{
 		deck.shuffleCards();
 		
 		if(!silent)
 		{
 			deck.printCards();
 			System.out.println("");
 		}
 	}
 	
 	private void dealCards()
 	{
 		// deal cards
 		for (int i = 0; i < 13; i++)
 		{
 			for (int j = 0; j < players.size(); j++)
 			{
 				players.get(j).addCard(deck.dealCard());
 			}
 		}
 		
 		// print each player cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.sortCards();
 			if(!silent)
 			{
 				System.out.println(player.getName());
 				player.printHand();
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 	}
 	
 	private void passingCards()
 	{
 		if(this.passing == Passing.Stay)
 			return;
 		
 		List<Card> p0CardsToPass = this.cardsSelectedToPass;
 		for (int i = 0; i < p0CardsToPass.size(); i++)
 		{
 			this.players.get(0).getHand().remove(p0CardsToPass.get(i));
 		}
 		
 		List<Card> p1CardsToPass = this.players.get(1).getCardsToPass(
 				this.passing);
 		List<Card> p2CardsToPass = this.players.get(2).getCardsToPass(
 				this.passing);
 		List<Card> p3CardsToPass = this.players.get(3).getCardsToPass(
 				this.passing);
 		
 		if(this.passing == Passing.Left)
 		{
 			this.players.get(0).addCards(p3CardsToPass);
 			this.players.get(1).addCards(p0CardsToPass);
 			this.players.get(2).addCards(p1CardsToPass);
 			this.players.get(3).addCards(p2CardsToPass);
 		}
 		else if(this.passing == Passing.Right)
 		{
 			this.players.get(0).addCards(p1CardsToPass);
 			this.players.get(1).addCards(p2CardsToPass);
 			this.players.get(2).addCards(p3CardsToPass);
 			this.players.get(3).addCards(p0CardsToPass);
 		}
 		else if(this.passing == Passing.Front)
 		{
 			this.players.get(0).addCards(p2CardsToPass);
 			this.players.get(1).addCards(p3CardsToPass);
 			this.players.get(2).addCards(p0CardsToPass);
 			this.players.get(3).addCards(p1CardsToPass);
 		}
 		if(!silent)
 			System.out.println("=====After passing cards to the "
 					+ this.passing.toString() + "=====");
 		// print each player cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.sortCards();
 			if(!silent)
 			{
 				System.out.println(player.getName());
 				player.printHand();
 				System.out.println("");
 				System.out.println("");
 			}
 		}
 	}
 	
 	private void runGameOld()
 	{
 		this.heartsUI.setPlayers(this.players);
 		this.heartsUI.setUI(this.showOpponentCards);
 		this.heartsUI.showDialog();
 		
 		long start = System.nanoTime();
 		int myLossCount = 0;
 		int myWinCount = 0;
 		for (int n = 0; n < GAMES_TO_RUN; n++)
 		{
 			// while we still playing the game
 			for (int i = 0; i < players.size(); i++)
 			{
 				Player p = players.get(i);
 				int score = (-1) * p.getScore();
 				p.addScore(score);
 			}
 			while (true)
 			{
 				for (int i = 0; i < players.size(); i++)
 				{
 					Player p = players.get(i);
 					// clear each player cards
 					p.clearCards();
 					this.cardsSelectedToPass.clear();
 					this.buttonCardsSelectedToPass.clear();
 					
 					this.cardsReadyToPass = true;
 				}
 				
 				// initialize deck for the game
 				deck = new Deck();
 				shuffleCards();
 				dealCards();
 				
 				this.currentCardAction = CardAction.Idle;
 				this.numCardsSelectedToPass = 0;
 				this.heartsUI.displayCards();
 				
 				if(this.passing != Passing.Stay)
 				{
 					this.currentCardAction = CardAction.Passing;
 					this.heartsUI.setPassButtonVisible(true);
 					this.cardsReadyToPass = false;
 					
 					while (!cardsReadyToPass)
 					{
 						
 					}
 					
 					passingCards();
 					this.heartsUI.redrawCards();
 					
 					for (int k = 0; k < this.buttonCardsSelectedToPass.size(); k++)
 						this.heartsUI
 								.setCardUnselected(this.buttonCardsSelectedToPass
 										.get(k));
 				}
 				
 				// initialize game properties
 				this.currentCardAction = CardAction.Playing;
 				turnsPlayed = 0;
 				heartsBroken = false;
 				notifyHeartsBroken = false;
 				
 				// new round, find player to start round
 				int playerToStartRound = findPlayerToStart();
 				if(!silent)
 					System.out.println("Player "
 							+ players.get(playerToStartRound).getName()
 							+ " goes first");
 				
 				// while we still have cards
 				while (turnsPlayed < 13)
 				{
 					// clear player's in play cards
 					for (int i = 0; i < players.size(); i++)
 						players.get(i).clearInPlayCards();
 					
 					playerToStartRound = runTurnOld(playerToStartRound);
 					if(!silent)
 						System.out.println("=============== trick "
 								+ (turnsPlayed + 1) + " done ================");
 					// every 4 cards played is a "trick", a round is when all
 					// cards have been exhausted.
 					// changed "round <number> done" to "trick <number> done"
 					turnsPlayed++;
 				}
 				
 				// add scores to each player
 				assignScoresToPlayers();
 				
 				// check for highest point
 				Player playerWithHighestTotalPoints = null;
 				Player playerWithLowestTotalPoints = null;
 				for (int i = 0; i < players.size(); i++)
 				{
 					Player p = players.get(i);
 					if(playerWithHighestTotalPoints == null)
 					{
 						playerWithHighestTotalPoints = p;
 						playerWithLowestTotalPoints = p;
 					}
 					else if(playerWithHighestTotalPoints.getScore() < p
 							.getScore())
 						playerWithHighestTotalPoints = p;
 					else if(playerWithLowestTotalPoints.getScore() > p
 							.getScore())
 						playerWithLowestTotalPoints = p;
 				}
 				
 				// determine if the game ends
 				if(playerWithHighestTotalPoints.getScore() >= this.endScore)
 				{
 					if(playerWithHighestTotalPoints instanceof AgentAggressive)
 						myLossCount++;
 					if(playerWithLowestTotalPoints instanceof AgentAggressive)
 						myWinCount++;
 					if(!silent)
 						System.out.println("Player "
 								+ playerWithHighestTotalPoints.getName()
 								+ " loses with total score of "
 								+ playerWithHighestTotalPoints.getScore());
 					if(!silent)
 						System.out.println("Player "
 								+ playerWithLowestTotalPoints.getName()
 								+ " wins with total score of "
 								+ playerWithLowestTotalPoints.getScore());
 					break;
 				}
 				
 				// figure out next passing
 				if(this.passing == Passing.Left)
 					this.passing = Passing.Right;
 				else if(this.passing == Passing.Right)
 					this.passing = Passing.Front;
 				else if(this.passing == Passing.Front)
 					this.passing = Passing.Stay;
 				else if(this.passing == Passing.Stay)
 					this.passing = Passing.Left;
 			}
 		}
 		long done = System.nanoTime();
 		System.out.println("time to run was " + (done - start) / 1000000);
 		System.out.println("My loss count was " + myLossCount);
 		System.out.println("My win count was " + myWinCount);
 		
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player p = players.get(i);
 			if(p instanceof AgentAggressive)
 			{
 				AgentAggressive aa = (AgentAggressive) p;
 				System.out.println("expected wins " + aa.getExpectedWins());
 				System.out.println("actual wins " + aa.getActualWins());
 				aa.serializeThresholds();
 			}
 		}
 	}
 	
 	private void runGame()
 	{
 		this.heartsUI.setUI(this.showOpponentCards);
 		this.heartsUI.setPlayers(this.players);
 		this.heartsUI.showDialog();
 		
 		this.runNextGame();
 	}
 	
 	private void runNextGame()
 	{
 		this.deck = new Deck();
 		shuffleCards();
 		dealCards();
 		
 		this.cardsSelectedToPass.clear();
 		this.buttonCardsSelectedToPass.clear();
 		
 		this.currentCardAction = CardAction.Idle;
 		this.numCardsSelectedToPass = 0;
 		this.heartsUI.displayCards();
 		this.heartsUI.setPassButtonVisible(false);
 		
 		if(this.passing != Passing.Stay)
 		{
 			this.currentCardAction = CardAction.Passing;
 			this.heartsUI.setPassButtonVisible(true);
 			this.cardsReadyToPass = false;
 		}
 		else
 		{
 			this.currentCardAction = CardAction.Playing;
 			this.heartsUI.setPassButtonVisible(false);
 			this.initializeFirstTurn();
 		}
 	}
 	
 	private void initializeFirstTurn()
 	{
 		// clear player's in play cards
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).clearInPlayCards();
 
 		this.CURRENT_TURN = 0;
 		this.CURRENT_PLAYER_THIS_TURN = this.findPlayerToStart();
 		this.initializeTurn();
 
 		// user move
 		if (this.CURRENT_PLAYER_THIS_TURN == 0)
 		{
 			// do nothing, wait for user move
 		}
 		else
 		{
 			this.runAITurns();
 		}
 	}
 	
 	private void runAITurns()
 	{
 		// while the next player to move is AI and this turn is not
 		// finished yet
 		while (this.CURRENT_PLAYER_THIS_TURN != 0
 				&& this.CURRENT_CARDS_PLAYED.size() != this.MAX_CARDS_PER_TURN)
 		{
 			// AI move
 			this.runTurn(this.CURRENT_PLAYER_THIS_TURN);
 		}
 
 		if (this.CURRENT_CARDS_PLAYED.size() == this.MAX_CARDS_PER_TURN)
 		{
 			this.summarizeTurn();
 			this.CURRENT_TURN++;
 			this.initializeTurn();
 			this.nextTurn();
 		}
 	}
 	
 	private final int MAX_TURNS_PLAYED = 13;
 	private final int MAX_CARDS_PER_TURN = 4;
 	private int CURRENT_PLAYER_THIS_TURN;
 	private int CURRENT_TURN;
 	private List<Card> CURRENT_CARDS_PLAYED;
 	private boolean CURRENT_TURN_FIRST;
 	private Card CURRENT_TURN_FIRST_PLAYED_CARD;
 	private Card CURRENT_TURN_HIGHEST_VALUE_CARD;
 	private Player CURRENT_TURN_PLAYER_HIGHEST_VALUE;
 	private boolean HEARTS_BROKEN;
 	
 	private void finalizePlayerTurn(Card card, Position position, Player player)
 	{
 		JButton cardButton = this.heartsUI.findButton(position, card);
 		this.heartsUI.removeButton(cardButton, position);
 		this.heartsUI.showPlayedCardButton(position, card);
 		
 		try
 		{
 			Thread.sleep(500);
 		}
 		catch (InterruptedException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.CURRENT_CARDS_PLAYED.add(card);
 		if(this.CURRENT_TURN_FIRST)
 		{
 			this.CURRENT_TURN_FIRST_PLAYED_CARD = card;
 			this.CURRENT_TURN_HIGHEST_VALUE_CARD = card;
 			this.CURRENT_TURN_PLAYER_HIGHEST_VALUE = player;
 		}
 		else
 		{
 			// compare with highest value
 			if(card.getSuit() == this.CURRENT_TURN_FIRST_PLAYED_CARD.getSuit())
 			{
 				if(card.getFace().ordinal() > this.CURRENT_TURN_HIGHEST_VALUE_CARD
 						.getFace().ordinal())
 				{
 					this.CURRENT_TURN_HIGHEST_VALUE_CARD = card;
 					this.CURRENT_TURN_PLAYER_HIGHEST_VALUE = player;
 				}
 			}
 		}
 		
 		this.CURRENT_TURN_FIRST = false;
 		if(!silent)
 			System.out
 					.println("Player " + player.getName() + " played " + card);
 		
 		// notify that hearts has been broken (once per round)
 		notifyHeartsBroken(this.CURRENT_CARDS_PLAYED, player);
 //		this.heartsUI.ShowBalloonTip("DEBUG MESSAGE: A card has been played");
 		
 		// add that card to each player's in play cards
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).addInPlayCards(card);
 		
 		this.CURRENT_PLAYER_THIS_TURN++;
 		this.CURRENT_PLAYER_THIS_TURN %= 4;
 	}
 	
 	private int runTurnOld(int num) 
 	{
 		this.cardReadyToPlay = false;
 		this.cardChosenToPlay = false;
 		this.cardSelectedToPlay = null;
 		
 		List<Card> cardsPlayed = new ArrayList<Card>(4);
 		boolean first = true;
 		Card firstPlayedCard = null;
 		Card cardWithHighestValue = null;
 		Player playerWithHighestValue = null;
 		
 		Card c = null;
 		
 		while(cardsPlayed.size() < 4)
 		{
 		  Player p = players.get(num);
 		  Position playerPosition = this.getPlayerPosition(num);
 		  
 		  //if this is user's turn
 		  if(p.getClass().equals(User.class))
 		  {
 			  User user = (User)p;
 			  this.currentCardAction = CardAction.Playing;
 			  
 			  while(!this.cardReadyToPlay)
 			  {
 				  try
 				  {
 					  while(!this.cardChosenToPlay)
 					  {
 						  
 					  }
 					  user.TryPlayCard(this.cardSelectedToPlay, cardsPlayed, heartsBroken, (first && turnsPlayed == 0));
 					  this.cardReadyToPlay = true;
 					  this.heartsUI.removeButton(this.buttonCardSelectedToPlay, playerPosition);
 					  
 					  c = this.cardSelectedToPlay;
 				  }
 				  catch(Exception ex)
 				  {
 					  this.cardChosenToPlay = false;
 					  //TODO show why this is illegal move
 				  }
 			  }
 		  }
 		  else
 		  {
 			  c = p.playCard(cardsPlayed, heartsBroken, (first && turnsPlayed == 0));
 			  JButton cardButton = this.heartsUI.findButton(playerPosition, c);
 			  this.heartsUI.removeButton(cardButton, playerPosition);
 		  }
 		  
 		  this.heartsUI.showPlayedCardButton(playerPosition, c);
 		  
 	  	  cardsPlayed.add(c);
 		  if(first)
 		  {
 			  firstPlayedCard = c;
 			  cardWithHighestValue = c;
 			  playerWithHighestValue = p;
 		  }
 		  else
 		  {
 			  //compare with highest value
 			  if(c.getSuit() == firstPlayedCard.getSuit())
 			  {
 				  if(c.getFace().ordinal() > cardWithHighestValue.getFace().ordinal())
 				  {
 					  cardWithHighestValue = c;
 					  playerWithHighestValue = p;
 				  }
 			  }
 		  }
 		  
 		  first = false;
 		  if(!silent)
 			  System.out.println("Player "+p.getName()+" played "+c);
 		
 		  //notify that hearts has been broken (once per round)
 		  notifyHeartsBroken(cardsPlayed, p);
 		  
 		  //add that card to each player's in play cards
 		  for(int i = 0; i < players.size(); i++)
 			  players.get(i).addInPlayCards(c);
 		  
 		  num++;
 		  num%=4;
 	  }
 		
 		//give a little time
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.heartsUI.hideAllPlayedCardButtons();
 		
 		
 		//also add to the played cards
 		//add that card to each player's list of played cards
 		for(int i = 0; i < players.size(); i++)
 		{
 			boolean tookCards = players.get(i).equals(playerWithHighestValue);
 			players.get(i).addPlayedCards(cardsPlayed, tookCards, num);
 			if(tookCards)
 			{
 				//add cards to the player with highest value
 				for(int j = 0; j < cardsPlayed.size(); j++)
 				{
 					playerWithHighestValue.addTakenCard(cardsPlayed.get(j), !silent);
 					
 					//figure out if hearts already broken
 					if(!this.heartsBroken && cardsPlayed.get(j).getSuit() == Suit.Hearts)
 						this.heartsBroken = true;
 						
 				}				
 			}
 		}
 		  
 		return this.players.indexOf(playerWithHighestValue);
 	}
 	
 	private void runTurn(int playerNum)
 	{
 		Card card = null;
 		
 		Player player = players.get(playerNum);
 		Position position = this.getPlayerPosition(playerNum);
 		
 		card = player.playCard(this.CURRENT_CARDS_PLAYED, this.HEARTS_BROKEN,
 				(this.CURRENT_TURN_FIRST && this.CURRENT_TURN == 0));
 		
 		this.finalizePlayerTurn(card, position, player);
 	}
 	
 	private void summarizeTurn()
 	{
 		this.heartsUI.hideAllPlayedCardButtons();
 		
 		// also add to the played cards
 		// add that card to each player's list of played cards
 		for (int i = 0; i < players.size(); i++)
 		{
 			boolean tookCards = players.get(i).equals(
 					this.CURRENT_TURN_PLAYER_HIGHEST_VALUE);
 			players.get(i).addPlayedCards(this.CURRENT_CARDS_PLAYED, tookCards,
 					this.CURRENT_TURN);
 			
 			if(tookCards)
 			{
 				// add cards to the player with highest value
 				for (int j = 0; j < this.CURRENT_CARDS_PLAYED.size(); j++)
 				{
 					this.CURRENT_TURN_PLAYER_HIGHEST_VALUE.addTakenCard(
 							this.CURRENT_CARDS_PLAYED.get(j), !silent);
 					
 					// figure out if hearts already broken
 					if(!this.HEARTS_BROKEN
 							&& this.CURRENT_CARDS_PLAYED.get(j).getSuit() == Suit.Hearts)
 						this.HEARTS_BROKEN = true;
 				}
 			}
 		}
 		
 		// find player to start next
 		this.CURRENT_PLAYER_THIS_TURN = this.players
 				.indexOf(this.CURRENT_TURN_PLAYER_HIGHEST_VALUE);
 	}
 	
 	private void initializeTurn()
 	{
 		this.CURRENT_CARDS_PLAYED = new ArrayList<Card>(4);
 		
 		for (int i = 0; i < players.size(); i++)
 			players.get(i).clearInPlayCards();
 		
 		this.CURRENT_TURN_FIRST = true;
 		this.CURRENT_TURN_FIRST_PLAYED_CARD = null;
 		this.CURRENT_TURN_HIGHEST_VALUE_CARD = null;
 		this.CURRENT_TURN_PLAYER_HIGHEST_VALUE = null;
 	}
 	
 	private void nextTurn()
 	{
 		if(this.CURRENT_TURN == this.MAX_TURNS_PLAYED)
 		{
 			this.assignScoresToPlayers();
 			
 			// check for highest point
 			Player playerWithHighestTotalPoints = null;
 			Player playerWithLowestTotalPoints = null;
 			
 			for (int i = 0; i < players.size(); i++)
 			{
 				Player player = players.get(i);
 				if(playerWithHighestTotalPoints == null)
 				{
 					playerWithHighestTotalPoints = player;
 					playerWithLowestTotalPoints = player;
 				}
 				else if(playerWithHighestTotalPoints.getScore() < player
 						.getScore())
 					playerWithHighestTotalPoints = player;
 				else if(playerWithLowestTotalPoints.getScore() > player
 						.getScore())
 					playerWithLowestTotalPoints = player;
 			}
 			
 			// determine if the game ends
 			if(playerWithHighestTotalPoints.getScore() >= this.endScore)
 			{
 				if(!silent)
 					System.out.println("Player "
 							+ playerWithHighestTotalPoints.getName()
 							+ " loses with total score of "
 							+ playerWithHighestTotalPoints.getScore());
 				if(!silent)
 					System.out.println("Player "
 							+ playerWithLowestTotalPoints.getName()
 							+ " wins with total score of "
 							+ playerWithLowestTotalPoints.getScore());
 				return;
 			}
 			
 			// figure out next passing
 			if(this.passing == Passing.Left)
 				this.passing = Passing.Right;
 			else if(this.passing == Passing.Right)
 				this.passing = Passing.Front;
 			else if(this.passing == Passing.Front)
 				this.passing = Passing.Stay;
 			else if(this.passing == Passing.Stay)
 				this.passing = Passing.Left;
 			
 			this.runNextGame();
 		}
 		else
 		{
 			while (this.CURRENT_PLAYER_THIS_TURN != 0
 					&& this.CURRENT_CARDS_PLAYED.size() != 4)
 			{
 				// AI move
 				this.runTurn(this.CURRENT_PLAYER_THIS_TURN);
 			}
 		}
 	}
 	
 	/**
 	 * This displays an instant notification when hearts have been broken during
 	 * a round. The notification also includes the name of the player who broke
 	 * hearts.
 	 * 
 	 * @param cardsPlayed
 	 *            To monitor the cards on the table
 	 * @param p
 	 *            Player object. Enables passing in of player name.
 	 * @author Jonathan Shelton
 	 */
 	private void notifyHeartsBroken(List<Card> cardsPlayed, Player p)
 	{
 		if(notifyHeartsBroken == false)
 		{
 			for (int i = 0; i < cardsPlayed.size(); i++)
 			{
 				if(cardsPlayed.get(i).getSuit() == Suit.Hearts)
 				{
 					this.heartsUI.ShowBalloonTip("Hearts have been broken by "
 								+ p.getName());
 					if(!silent)
 						System.out.println("*****Hearts have been broken by "
 								+ p.getName() + "*****");
 					notifyHeartsBroken = true;
 				}
 			}
 		}
 	}
 	
 	private int findPlayerToStart()
 	{
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player p = players.get(i);
 			if(p.hasTwoOfClubs())
 				return i;
 		}
 		throw new RuntimeException("Couldn't find the 2 of clubs");
 	}
 	
 	private void assignScoresToPlayers()
 	{
 		// see if a player collects all hearts and queen of spade
 		int numHearts;
 		int numQueenSpade;
 		boolean playerShootingTheMoon = false;
 		
 		List<Integer> scores = Arrays.asList(0, 0, 0, 0);
 		for (int i = 0; i < players.size(); i++)
 		{
 			numHearts = 0;
 			numQueenSpade = 0;
 			
 			Player player = players.get(i);
 			List<Card> takenCards = player.getTakenCards();
 			
 			for (int j = 0; j < takenCards.size(); j++)
 			{
 				Card takenCard = takenCards.get(j);
 				if(takenCard.getSuit() == Suit.Hearts)
 					numHearts++;
 				else if(takenCard.getSuit() == Suit.Spades
 						&& takenCard.getFace() == Face.Queen)
 					numQueenSpade++;
 			}
 			
 			// if this player gets all the hearts & queen of spade
 			if(numHearts == 13 && numQueenSpade == 1)
 			{
 				playerShootingTheMoon = true;
 				for (int j = 0; j < players.size(); j++)
 				{
 					if(i != j)
 						scores.set(j, 26);
 					else
 						scores.set(j, 0);
 				}
 				break;
 			}
 			else
 			{
 				scores.set(i, numHearts + (numQueenSpade * 13));
 			}
 		}
 		
 		for (int i = 0; i < players.size(); i++)
 		{
 			Player player = players.get(i);
 			player.addScore(scores.get(i));
 			if(!silent)
 			{
 				System.out.println("This round, " + player.getName()
 						+ " collected " + scores.get(i) + " points ");
 				System.out.println("Total points for " + player.getName()
 						+ " is " + player.getScore() + " points ");
 				System.out.println("=======================================");
 			}
 		}
 	}
 	
 	public Position getPlayerPosition(int num)
 	{
 		switch (num)
 		{
 			case 0:
 				return Position.South;
 			case 1:
 				return Position.West;
 			case 2:
 				return Position.North;
 			case 3:
 				return Position.East;
 			default:
 				return Position.South;
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent actionEvent)
 	{
 		Object source = actionEvent.getSource();
 
 		if (source.getClass() == JButton.class) {
 			JButton jButton = (JButton) source;
 			String buttonType = (String) jButton
 					.getClientProperty("ButtonType");
 
			if (buttonType.equals("ConfigurationOK")) 
			{
 				String playerName = this.configurationUI.getPlayerName();
 				if(playerName == null || playerName.trim().equals(""))
				{
 					if(!configurationUI.isUserErrorShown())
 					{
 						configurationUI.displayErrorDialog("Enter a user name");
 						configurationUI.setUserErrorShown(true);
 					}
				}
 				else
 				{
 					String levelOfDifficulty = this.configurationUI
 						.getLevelofDifficulty();
 					int endScore = this.configurationUI.getEndScore();
 
 					if (!silent) {
 						System.out.println("Player: " + playerName + ", Level: "
 							+ levelOfDifficulty + ", End Score: " + endScore);
 					}
 					this.configurationUI.getFrmConfigurationWindow().dispose();
 
 					initialize(playerName, endScore, levelOfDifficulty);
 					runGame();
 				}
 			} else if (buttonType.equals("ConfigurationCancel")) {
 				System.exit(0);
 			} else if (buttonType.equals("PassButton")) {
 				if (this.numCardsSelectedToPass != 3) {
 					this.heartsUI.ShowBalloonTip("Pick 3 cards to pass!");
 					return;
 				}
 
 				passingCards();
 				this.heartsUI.redrawCards();
 
 				// set selected cards to normal mode
 				for (int k = 0; k < this.buttonCardsSelectedToPass.size(); k++)
 					this.heartsUI
 							.setCardUnselected(this.buttonCardsSelectedToPass
 									.get(k));
 
 				this.heartsUI.setPassButtonVisible(false);
 				this.currentCardAction = CardAction.Playing;
 				this.initializeFirstTurn();
 			} else if (buttonType.equals("CardButton")) {
 				Card card = (Card) jButton.getClientProperty("Card");
 				if (this.currentCardAction == CardAction.Passing) {
 					boolean isSelected = (boolean) jButton
 							.getClientProperty("Selected");
 
 					if (isSelected) {
 						this.heartsUI.setCardUnselected(jButton);
 						this.buttonCardsSelectedToPass.remove(jButton);
 						this.cardsSelectedToPass.remove(card);
 						this.numCardsSelectedToPass--;
 					} else {
 						if (this.numCardsSelectedToPass != 3) {
 							this.heartsUI.setCardSelected(jButton);
 							this.buttonCardsSelectedToPass.add(jButton);
 							this.cardsSelectedToPass.add(card);
 							this.numCardsSelectedToPass++;
 						}
 					}
 				} else if (this.currentCardAction == CardAction.Playing) {
 					// user turn
 					if (this.CURRENT_PLAYER_THIS_TURN == 0) {
 						try {
 							User user = (User) this.players.get(0);
 							user.TryPlayCard(
 									card,
 									this.CURRENT_CARDS_PLAYED,
 									this.HEARTS_BROKEN,
 									(this.CURRENT_TURN_FIRST && this.CURRENT_TURN == 0));
 
 							this.finalizePlayerTurn(card, Position.South,
 									this.players.get(0));
 
 							this.runAITurns();
 						} catch (Exception ex) {
 							// TODO show why this is illegal move
 							ex.printStackTrace();
 							this.heartsUI.ShowBalloonTip(ex.getMessage());
 						}
 					}
 				}
 			}
 		}
 		else if(source.getClass() == JMenuItem.class)
 		{
 			JMenuItem jMenuItem = (JMenuItem)source;
 			String menuItemType = (String)jMenuItem.getClientProperty("MenuItemType");
 			
 			if(menuItemType.equalsIgnoreCase("Rules"))
 			{
 				RulesWindow rulesWindow = new RulesWindow();
 			}
 			else if(menuItemType.equalsIgnoreCase("Achievements"))
 			{
 				//TODO put achievements windows
 			}
 			else if(menuItemType.equalsIgnoreCase("Exit"))
 			{
 				System.exit(0);
 			}
 		}
 	}
 }
