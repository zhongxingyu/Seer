 package bots.demobots;
 
 import java.util.ArrayList;
 
import util.Utils;

 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.Card;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.Hand;
 import com.biotools.meerkat.Holdem;
 import com.biotools.meerkat.Player;
 import com.biotools.meerkat.util.Preferences;
 import common.handeval.klaatu.PartialStageFastEval;
 
 /** 
  * A Simple example bot based on the 'SimpleBot' embedded with the Meerkat-API.<br>
  * This bot doesn't make use of the Meerkat-HandEvaluator-class is this one doesn't work within the Testbet 
  * (Randomizer-class is missing in the meerkat.jar)<br>
  * Furthermore some changes were added to also place proper No-Limit-Bets and not
  * to fall into preflop reraise wars with other bots.
  * <br>
  * Some tests show that SimpleBot beats default XenBot from Poker-Academy with 40bb/100 but loses agains
  * the  Sklansky2-Bot by about 50bb/100. This is interesting as XenBot wins angainst Sklansky-Bot<br>
  * So every bot can exploit some behaviour of another - and it shows how important it is
  * to test against different types of bots, to find one most robust against all other strategies.  
  * (This doesn't necessarily imply SimpleBot is 'better' (against real humans), as this bot
  * is very predictable and both other bots have much more variation in their plays.
  *    
  */
 public class SimpleBot implements Player {
 	private int ourSeat; // our seat for the current hand
 	private Card c1, c2; // our hole cards
 	private GameInfo gi; // general game information
 	private Preferences prefs; // the configuration options for this bot
 
 	public SimpleBot() {
 	}
 
 	/**
 	 * An event called to tell us our hole cards and seat number
 	 * @param c1 your first hole card
 	 * @param c2 your second hole card
 	 * @param seat your seat number at the table
 	 */
 	public void holeCards(Card c1, Card c2, int seat) {
 		this.c1 = c1;
 		this.c2 = c2;
 		this.ourSeat = seat;
 	}
 
 	/**
 	 * Requests an Action from the player
 	 * Called when it is the Player's turn to act.
 	 */
 	public Action getAction() {
 		if (gi.isPreFlop()) {
 			return preFlopAction();
 		} else {
 			return postFlopAction();
 		}
 	}
 
 	/**
 	 * Get the current settings for this bot.
 	 */
 	public Preferences getPreferences() {
 		return prefs;
 	}
 
 	/**
 	 * Load the current settings for this bot.
 	 */
 	public void init(Preferences playerPrefs) {
 		this.prefs = playerPrefs;
 	}
 
 	/**
 	 * @return true if debug mode is on.
 	 */
 	public boolean getDebug() {
 		return prefs.getBooleanPreference("DEBUG", false);
 	}
 
 	/**
 	 * print a debug statement.
 	 */
 	public void debug(String str) {
 		if (getDebug()) {
 			System.out.println(str);
 		}
 	}
 
 	/**
 	 * print a debug statement with no end of line character
 	 */
 	public void debugb(String str) {
 		if (getDebug()) {
 			System.out.print(str);
 		}
 	}
 
 	/**
 	 * A new betting round has started.
 	 */
 	public void stageEvent(int stage) {
 	}
 
 	/**
 	 * A showdown has occurred.
 	 * @param pos the position of the player showing
 	 * @param c1 the first hole card shown
 	 * @param c2 the second hole card shown
 	 */
 	public void showdownEvent(int seat, Card c1, Card c2) {
 	}
 
 	/**
 	 * A new game has been started.
 	 * @param gi the game stat information
 	 */
 	public void gameStartEvent(GameInfo gInfo) {
 		this.gi = gInfo;
 	}
 
 	/**
 	 * An event sent when all players are being dealt their hole cards
 	 */
 	public void dealHoleCardsEvent() {
 	}
 
 	/**
 	 * An action has been observed. 
 	 */
 	public void actionEvent(int pos, Action act) {
 	}
 
 	/**
 	 * The game info state has been updated
 	 * Called after an action event has been fully processed
 	 */
 	public void gameStateChanged() {
 	}
 
 	/**
 	 * The hand is now over. 
 	 */
 	public void gameOverEvent() {
 	}
 
 	/**
 	 * A player at pos has won amount with the hand handName
 	 */
 	public void winEvent(int pos, double amount, String handName) {
 	}
 
 	/**
 	 * Decide what to do for a pre-flop action
 	 *
 	 * Uses a really simple hand selection, as a silly example.
 	 */
 	private Action preFlopAction() {
 		debug(gi.getPlayerName(ourSeat) + " Hand: [" + c1.toString() + "-" + c2.toString() + "] ");
 		double toCall = gi.getAmountToCall(ourSeat);
 		// play all pocket-pairs      
 		if (c1.getRank() == c2.getRank()) {
 			if ((c1.getRank() >= Card.TEN || c1.getRank() == Card.TWO) && gi.getNumRaises() < 2) {
 				return Action.raiseAction(gi);
 			}
 			if (gi.getNumRaises() < 3) {
 				return Action.callAction(toCall);
 			}
 		}
 
 		// play all cards where both cards are bigger than Tens
 		// and raise if they are suited
 		if (c1.getRank() >= Card.TEN && c2.getRank() >= Card.TEN) {
 			if (c1.getSuit() == c2.getSuit() && gi.getNumRaises() < 2) {
 				return Action.raiseAction(gi);
 			}
 			if (gi.getNumRaises() < 3) {
 				return Action.callAction(toCall);
 			}
 		}
 
 		// play all suited connectors
 		if ((c1.getSuit() == c2.getSuit())) {
 			if (Math.abs(c1.getRank() - c2.getRank()) == 1) {
 				return Action.callAction(toCall);
 			}
 			// raise A2 suited
 			if ((c1.getRank() == Card.ACE && c2.getRank() == Card.TWO) || (c2.getRank() == Card.ACE && c1.getRank() == Card.TWO)) {
 				if (gi.getNumRaises() == 0) {
 					return Action.raiseAction(gi);
 				}
 				if (gi.getNumRaises() < 1) {
 					return Action.callAction(gi);
 				}
 			}
 			// call any suited ace
 			if ((c1.getRank() == Card.ACE || c2.getRank() == Card.ACE)) {
 				return Action.callAction(toCall);
 			}
 		}
 
 		// play anything 5% of the time
 		if (gi.getAmountToCall(ourSeat) <= gi.getBigBlindSize()) {
 			if (Math.random() < 0.05) {
 				return Action.callAction(toCall);
 			}
 		}
 
 		// check or fold
 		return Action.checkOrFoldAction(toCall);
 	}
 
 	/**
 	 * Decide what to do for a post-flop action
 	 */
 	private Action postFlopAction() {
 		// number of players left in the hand (including us)
 		int np = gi.getNumActivePlayers();
 
 		// amount to call
 		double toCall = gi.getAmountToCall(ourSeat);
 
 		// immediate pot odds
 		double PO = toCall / (double) (gi.getEligiblePot(ourSeat) + toCall);
 
 		EnumerateResult result = enumerateHands(c1, c2, gi.getBoard());
 		// compute our current hand rank
 		double HRN = Math.pow(result.HR, np - 1);
 
 		// compute a fast approximation of our hand potential
 		double PPOT = 0.0;
 		if (gi.getStage() < Holdem.RIVER) {
 			PPOT = result.PPot;
 		}
 
 		debug(gi.getBoard() + " | HRn = " + Math.round(HRN * 10) / 10.0 + " PPot = " + Math.round(PPOT * 10) / 10.0 + " PotOdds = " + Math.round(PO * 10)
 				/ 10.0);
 
 		if (HRN == 1.0) {
 			// dah nuts -- raise the roof!
 			return betOrRaisePot();
 		}
 
 		// consider checking or betting:
 		if (toCall == 0) {
 			if (Math.random() < HRN * HRN) {
 				debug(gi.getPlayerName(ourSeat) + ":valuebet-bet");
 				return betOrRaisePot(); // bet a hand in proportion to it's strength
 
 			}
 			if (Math.random() < PPOT) {
 				debug(gi.getPlayerName(ourSeat) + ":semibluff-bet");
 				return betOrRaisePot(); // semi-bluff
 			}
 			// just check
 			debug(gi.getPlayerName(ourSeat) + ": check");
 			return Action.checkAction();
 		} else {
 			// consider folding, calling or raising:        
 			if (Math.random() < Math.pow(HRN, 1 + gi.getNumRaises())) {
 				// raise in proportion to the strength of our hand
 				debug(gi.getPlayerName(ourSeat) + ": valuebet-raise");
 				return betOrRaisePot();
 			}
 
 			if (HRN * HRN * gi.getEligiblePot(ourSeat) > toCall || PPOT > PO) {
 				// if we have draw odds or a strong enough hand to call
 				debug(gi.getPlayerName(ourSeat) + ":potodds-call");
 				return Action.callAction(toCall);
 			}
 
 			debug(gi.getPlayerName(ourSeat) + ": checkOrFold");
 			return Action.checkOrFoldAction(toCall);
 		}
 	}
 
 	/**
 	 * if fixed-limit: just bets or raises<br>
 	 * in no-limit:<br>
 	 * - bets 2/3 pot<br>
 	 * - or raises a 2/3*(pot + toCall) if someone bet before<br>
 	 * thus always giving 1:2.5 pot odds to the villain.<br>
 	 * <br>
 	 * if stack is lower than to call, just bets the stack
 	 * if stack remaining after the raise is lower than the bet/raise goes
 	 * all-in
 	 * @return
 	 */
 	private Action betOrRaisePot() {
 		if (gi.isFixedLimit()) {
 			return Action.raiseAction(gi);
 		} else {
 			if (gi.getAmountToCall(ourSeat) > 0) {
 				if (gi.getBankRoll(ourSeat) > gi.getAmountToCall(ourSeat)) {
					double wantedRaiseAmount = Utils.roundToCents((gi.getMainPotSize() + gi.getAmountToCall(ourSeat)) / 3 * 2);
					double maxPossibleRaise = Utils.roundToCents(gi.getBankRoll(ourSeat) - gi.getAmountToCall(ourSeat));
					if (maxPossibleRaise < wantedRaiseAmount) {
						wantedRaiseAmount = maxPossibleRaise;
 					}
					return Action.raiseAction(gi, wantedRaiseAmount);
 				} else {
 					return Action.callAction(gi);
 				}
 			} else {
				double betAmount = Utils.roundToCents(gi.getMainPotSize() / 3 * 2);
 				if (gi.getBankRoll(ourSeat) - betAmount < betAmount) {
 					betAmount = gi.getBankRoll(ourSeat);
 				}
 
 				return Action.betAction(betAmount);
 			}
 		}
 	}
 
 	/**
 	 * Calculate the raw (unweighted) PPot1 and NPot1 of a hand. (Papp 1998, 5.3)
 	 * Does a one-card look ahead.
 	 * 
 	 * @param c1 the first hole card
 	 * @param c2 the second hole card
 	 * @param bd the board cards
 	 * @return 
 	 */
 	public EnumerateResult enumerateHands(Card c1, Card c2, Hand bd) {
 		double[][] HP = new double[3][3];
 		double[] HPTotal = new double[3];
 		int ourrank7, opprank;
 		int index;
 		int[] boardIndexes = new int[bd.size()];
 		int[] boardIndexes2 = new int[bd.size() + 1];
 
 		int c1Index;
 		int c2Index;
 
 		ArrayList<Integer> deck = new ArrayList<Integer>();
 		for (int i = 0; i < 52; i++) {
 			deck.add(Integer.valueOf(i));
 		}
 		for (int i = 0; i < bd.size(); i++) {
 			Card card = bd.getCard(i + 1);
 			boardIndexes[i] = PartialStageFastEval.encode(card.getRank(), card.getSuit());
 			boardIndexes2[i] = PartialStageFastEval.encode(card.getRank(), card.getSuit());
 			deck.remove(Integer.valueOf(boardIndexes[i]));
 		}
 		c1Index = PartialStageFastEval.encode(c1.getRank(), c1.getSuit());
 		c2Index = PartialStageFastEval.encode(c2.getRank(), c2.getSuit());
 		deck.remove(Integer.valueOf(c1Index));
 		deck.remove(Integer.valueOf(c2Index));
 
 		int ourrank5 = eval(boardIndexes, c1Index, c2Index);
 
 		// pick first opponent card
 		for (int i = 0; i < deck.size(); i++) {
 			int o1Card = deck.get(i);
 			// pick second opponent card
 			for (int j = i + 1; j < deck.size(); j++) {
 				int o2Card = deck.get(j);
 				opprank = eval(boardIndexes, o1Card, o2Card);
 				if (ourrank5 > opprank)
 					index = AHEAD;
 				else if (ourrank5 == opprank)
 					index = TIED;
 				else
 					index = BEHIND;
 				HPTotal[index]++;
 				if (bd.size() < 5) {
 
 					// tally all possiblities for next board card
 					for (int k = 0; k < deck.size(); k++) {
 						if (i == k || j == k)
 							continue;
 						boardIndexes2[boardIndexes2.length - 1] = deck.get(k);
 						ourrank7 = eval(boardIndexes2, c1Index, c2Index);
 						opprank = eval(boardIndexes2, o1Card, o2Card);
 						if (ourrank7 > opprank)
 							HP[index][AHEAD]++;
 						else if (ourrank7 == opprank)
 							HP[index][TIED]++;
 						else
 							HP[index][BEHIND]++;
 					}
 				}
 			}
 		} /* end of possible opponent hands */
 
 		double den1 = (45 * (HPTotal[BEHIND] + (HPTotal[TIED] / 2.0)));
 		double den2 = (45 * (HPTotal[AHEAD] + (HPTotal[TIED] / 2.0)));
 		EnumerateResult result = new EnumerateResult();
 		if (den1 > 0) {
 			result.PPot = (HP[BEHIND][AHEAD] + (HP[BEHIND][TIED] / 2.0) + (HP[TIED][AHEAD] / 2.0)) / (double) den1;
 		}
 		if (den2 > 0) {
 			result.NPot = (HP[AHEAD][BEHIND] + (HP[AHEAD][TIED] / 2.0) + (HP[TIED][BEHIND] / 2.0)) / (double) den2;
 		}
 		result.HR = (HPTotal[AHEAD] + (HPTotal[TIED] / 2)) / (HPTotal[AHEAD] + HPTotal[TIED] + HPTotal[BEHIND]);
 
 		return result;
 	}
 
 	private int eval(int[] boardIndexes, int c1Index, int c2Index) {
 		if (boardIndexes.length == 5) {
 			return PartialStageFastEval.eval7(boardIndexes[0], boardIndexes[1], boardIndexes[2], boardIndexes[3], boardIndexes[4], c1Index, c2Index);
 		} else if (boardIndexes.length == 4) {
 			return PartialStageFastEval.eval6(boardIndexes[0], boardIndexes[1], boardIndexes[2], boardIndexes[3], c1Index, c2Index);
 		} else {
 			return PartialStageFastEval.eval5(boardIndexes[0], boardIndexes[1], boardIndexes[2], c1Index, c2Index);
 		}
 	}
 
 	// constants used in above method:
 	private final static int AHEAD = 0;
 	private final static int TIED = 1;
 	private final static int BEHIND = 2;
 
 	class EnumerateResult {
 		double HR;
 		double PPot;
 		double NPot;
 	}
 }
