 
 package skittles.g2;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import skittles.sim.Offer;
 
 /**
  * Keeps track of what people have guessed and tries to analyze what people are
  * interested in getting.
  */
 public class KnowledgeBase {
 	// TODO - see what trades people skipped
 
 	private Inventory inventory;
 	private ArrayList<PreferenceHistory> playerHistories;
 	private ArrayList<Offer> successfulOffers;
 	private ArrayList<Offer> unsuccessfulOffers;
 
 	private double[][] estimatedCount;
 	private int turn;
 	private enum STAGE {DISCOVERY, HOARD, END}
 	private STAGE[] playerStage;
 
 	private PreferenceHistory marketHistory;
 
 	/**
 	 * Each relative want: rows are the things that they would gain.
 	 * Columns are the things they are giving up.
 	 */
 	private ArrayList<double[][]> relativeWants;
 	
 	private int playerCount;
 	
 	private final static double WANTS_NEW_WEIGHT = 0.5;
 		
 	/**
 	 * Index of ourselves in the playerTrades ArrayList.
 	 */
 	private int selfIndex;
 
 	public KnowledgeBase(Inventory inventory, int playerCount, int selfIndex) {
 		this.playerCount = playerCount;
 		playerStage = new STAGE[playerCount];
 		for (STAGE s: playerStage) {
 			s = STAGE.DISCOVERY;
 		}
 		this.inventory = inventory;
 		this.successfulOffers = new ArrayList<Offer>();
 		this.unsuccessfulOffers = new ArrayList<Offer>();
 		this.selfIndex = selfIndex;
 		this.playerCount = playerCount;
 		playerHistories = new ArrayList<PreferenceHistory>();
 		for (int i = 0; i < playerCount; i++) {
 			playerHistories.add(new PreferenceHistory(inventory.getNumColors()));
 		}
 
 		relativeWants = getRelativeWants();
 		marketHistory = new PreferenceHistory(inventory.getNumColors());
 
 		// For counting the players
 		double avgCount = inventory.getStartingSkittles() / (inventory.size() * 1.0);
 		estimatedCount = new double[playerCount][inventory.size()];
 		for (int j = 0; j < playerCount; j++) {
 			this.estimatedCount[j] = new double[inventory.size()];
 			for (int i = 0; i < inventory.size(); i++) {
 				estimatedCount[j][i] = Math.max(avgCount * (getCoeffecient(inventory.size())), 0); //Empirically proven to give conservative estimate
 			}
 		}
 		this.turn = 0;
 	}
 	
 	private int getEstimatedPlayerCount(int color, int player) {
 		return (int) estimatedCount[player][color];
 	}
 	
 	public double getCoeffecient(int x) {
         // coefficients
         double a = 1.1023231527810862E+00;
         double b = 4.5624279968285669E-01;
         double c = -2.2103379207855689E-01;
         return a * Math.pow((x - b), c);
 	}
 
 	public void storeUnselectedTrade(Offer offer) {
 		unsuccessfulOffers.add(offer);
 		
 		int proposer = offer.getOfferedByIndex();
 		playerHistories.get(proposer).addUnsuccessfulTrade(
 				offer.getOffer(), offer.getDesire());
 		if (proposer != selfIndex) {
 			marketHistory.addUnsuccessfulTrade(offer.getOffer(),
 					offer.getDesire());
 		}
 	}
 	
 	public double getOtherHappiness(Offer o) {
 		double h = 0;
 		for (int i = 0; i < playerCount; i++) {
 			double newHapp = getOtherHappiness(o, i);
 			if (newHapp > h) {
 				h = newHapp;
 			}
 		}
 		return h;
 	}
 	
 	private double getOtherHappiness(Offer o, int i) {
 		return getOtherHappiness(o.getDesire(), o.getOffer(), i);
 	}
 	
 	// giving is what they are giving
 	// taking is what they are taking
 	public double getOtherHappiness(int[] giving, int[] taking, 
 			int playerIndex) {
 		double[][] desires = relativeWants.get(playerIndex);
 		double[] takingRatios = getRatios(taking);
 		double value = 0;
 
 		for (int i = 0; i < inventory.getNumColors(); i++) {
 			if (giving[i] == 0) {
 				continue;
 			}
 			for (int j = 0; j < inventory.getNumColors(); j++) {
 				if (takingRatios[j] == 0) {
 					continue;
 				}
 				value += giving[i] * takingRatios[j] * desires[i][j];
 			}
 		}
 		return value;
 	}
 	
 	private ArrayList<double[][]> getRelativeWants() {
 		ArrayList<double[][]> relativeWants =
 				new ArrayList<double[][]>(playerCount); 
 		for (int i = 0; i < playerCount; i++) {
 			int skittleCount = inventory.getNumColors();
 			double[][] arr = new double[skittleCount][skittleCount];
 			for (int j = 0; j < arr.length; j++) {
 				for (int k = 0; k < arr.length; k++) {
 					arr[j][k] = 0.0001;
 				}
 			}
 			relativeWants.add(arr);
 		}
 		return relativeWants;
 	}
 	
 	public Offer getBestOfferPerPlayer(ArrayList<Integer> want,
 			ArrayList<Integer> giveUp, int p, int playerIndex) {
 		int skittleCount = inventory.getNumColors();
 		Offer o = new Offer(playerIndex, skittleCount);
 		int[] toGive = new int[skittleCount];
 		int[] toRequest = new int[skittleCount];
 		
 		ArrayList<RelativeScore> goodTrades = new ArrayList<RelativeScore>();
 		HashMap<Integer, Integer> colorToCount = new HashMap<Integer, Integer>();
 		
 		for (int i = 0; i < skittleCount; i++) {
 			for (int j = 0; j < i; j++) {
 				// i = what they are giving, j = what they are taking;
 				// positive score means they like it
 				double theirVal = relativeWants.get(p)[i][j];
 				double ourVal = inventory.getSkittle(i).getHoardingValue() -
 						inventory.getSkittle(j).getHoardingValue();
 				if (theirVal > 0 && want.contains(i) &&
 						giveUp.contains(j)) {
 					
 					/*
 					System.out.println(inventory.getSkittle(i).getHoardingValue());
 					System.out.println(inventory.getSkittle(j).getHoardingValue());
 					System.out.println(ourVal);
 					System.out.println("we get " + i + " for " + j);
 					System.out.println("");
 					*/
 					
 					goodTrades.add(new RelativeScore(theirVal * ourVal, j, i));
 					colorToCount.put(i, Math.min(
 							getEstimatedPlayerCount(i, playerIndex),
 							inventory.getSkittle(i).getCount()));
 					colorToCount.put(j, Math.min(
 							getEstimatedPlayerCount(j, playerIndex),
 							inventory.getSkittle(j).getCount()));
 				}
 			}
 		}
 		
 		Collections.sort(goodTrades);
 		Collections.reverse(goodTrades);
 		
 //		System.out.println(goodTrades);
 		
 		for (RelativeScore s : goodTrades) {
 			int count = Math.max( 
 				Math.min(colorToCount.get(s.toGive), colorToCount.get(s.toTake)),
 				0);
 			colorToCount.put(s.toGive, colorToCount.get(s.toGive) - count);
 			colorToCount.put(s.toTake, colorToCount.get(s.toTake) - count);
 			toGive[s.toGive] += count;
 			toRequest[s.toTake] += count;
 		}
 		
 //		System.out.print("giving " + Arrays.toString(toGive));
 //		System.out.print(" and taking " + Arrays.toString(toRequest));
 //		System.out.println(" to player " + p);
 		
 		o.setOffer(toGive, toRequest);
 		return o;
 	}
 	
 	private class RelativeScore implements Comparable<RelativeScore> {
 		private double score;
 		private int toGive;
 		private int toTake;
 		
 		// to give to them, to take from them
 		public RelativeScore(double score, int toGive, int toTake) {
 			this.score = score;
 			this.toGive = toGive;
 			this.toTake = toTake;
 		}
 
 		@Override
 		public int compareTo(RelativeScore other) {
 			// 1000 avoids small numbers being rounded to 0
 			return (int) (1000 * (this.score - other.score));
 		}
 		
 		public String toString() {
 			return toGive + " for " + toTake + " ; " + score;
 		}
 	}
 	
 	public void updateRelativeWants(ArrayList<SuperOffer> offers) {
 		ArrayList<double[][]> tempRelativeWants = getRelativeWants();
 		for (SuperOffer so : offers) {
 			Offer o = so.getOffer();
 			// Skip ignored trades for now.
 			int proposer = o.getOfferedByIndex();
 			int selector = o.getPickedByIndex();
 			addRelativeWants(tempRelativeWants, proposer, o.getDesire(),
 					o.getOffer(), false);
 			if (selector == -1) {
 				handleSkippedSuperOffer(so, tempRelativeWants);
 			} else {
 				addRelativeWants(tempRelativeWants, selector, o.getOffer(),
 						o.getDesire(), false);
 			}
 		}
 		mergeWants(tempRelativeWants);
 		
 		/*
 		for (Offer o : offers) {
 			if (o.getPickedByIndex() != -1) {
 				for (int i : o.getDesire()) {
 					if (i > 0) {
 						getOtherHappiness(o.getDesire(), o.getOffer(),
 								o.getPickedByIndex());
 						break;
 					}
 				}
 			}
 		}
 		*/
 	}
 	
 	private void handleSkippedSuperOffer(SuperOffer so,
 			ArrayList<double[][]> tempRelativeWants) {
 		for (int p : so.getSkippers()) {
 			Offer o = so.getOffer();
 			addRelativeWants(tempRelativeWants, p, o.getOffer(), o.getDesire(),
 					true);
 		}
 	}
 
 	private void mergeWants(ArrayList<double[][]> tempRelativeWants) {
 		for (int i = 0; i < tempRelativeWants.size(); i++) {
 			double[][] tempArray = tempRelativeWants.get(i);
 			double[][] oldArray = relativeWants.get(i);
 			for (int j = 0; j < tempArray.length; j++) {
 				for (int k = 0; k < tempArray[j].length; k++) {
 					oldArray[j][k] = oldArray[j][k] * (1- WANTS_NEW_WEIGHT) +
 							tempArray[j][k] * WANTS_NEW_WEIGHT;
 				}
 			}
 		}
 	}
 
 	private double[] getRatios(int[] counts) {
 		double[] givenRatios = new double[counts.length];
 		double sum = 0;
 		for (int i : counts) {
 			sum += i;
 		}
 		if (sum == 0) {
 			return null;
 		}
 		for (int i = 0; i < givenRatios.length; i++) {
 			givenRatios[i] = counts[i] / sum;
 		}
 		return givenRatios;
 	}
 	private void addRelativeWants(ArrayList<double[][]> tempRelativeWants,
 			int affectedPlayerIndex, int[] gained, int[] given, boolean dislike) {
 		double[][] playerWants = tempRelativeWants.get(affectedPlayerIndex);
 		
 		double[] givenRatios = getRatios(given);
 		double[] gainedRatios = getRatios(gained);
 		
 		if (givenRatios == null || gainedRatios == null) {
 			return;
 		}
 
 		for (int i = 0; i < givenRatios.length; i++) {
 			for (int j = 0; j < gainedRatios.length; j++) {
 				if (gainedRatios[i] != 0 && givenRatios[j] != 0) {
 					double jiChange = gainedRatios[i] / givenRatios[j];
 					double ijChange = gainedRatios[i] / givenRatios[j];
 					if (dislike) {
						jiChange *= 0.25;
						ijChange *= 0.25;
 					}
 					playerWants[j][i] += jiChange;
 					playerWants[i][j] -= ijChange;
 				}
 			}
 		}
 	}
 
 	public void storeSelectedTrade(Offer offer) {
 		successfulOffers.add(offer);
 		
 		int proposer = offer.getOfferedByIndex();
 		int selector = offer.getPickedByIndex();
 
 		playerHistories.get(proposer).addUnsuccessfulTrade(offer.getOffer(),
 				offer.getDesire());
 		playerHistories.get(selector).addUnsuccessfulTrade(offer.getDesire(),
 				offer.getOffer());
 		// If neither condition is true, the trades will cancel out, so don't
 		// bother putting them in.
 		if (proposer == selfIndex || selector == selfIndex) {
 			if (proposer != selfIndex) {
 				marketHistory.addSuccessfulTrade(offer.getOffer(), offer.getDesire());
 			}
 			if (selector != selfIndex) {
 				marketHistory.addSuccessfulTrade(offer.getDesire(), offer.getOffer());
 			}
 		}
 	}
 
 	public double[] getMarketPreferences() {
 		return marketHistory.getPreferences();
 	}
 
 	public double[] getPlayerPreferences(int playerId) {
 		return playerHistories.get(playerId).getPreferences();
 	}
 
 	/**
 	 * @param tastedSkittles
 	 * @return
 	 */
 	public Skittle getHighestMarketValueColorFrom(int start, ArrayList<Skittle> tastedSkittles) {
 		Skittle unwantedColor = null;
 		double[] marketPrefs = this.getMarketPreferences();
 		double currentMarketValue = Double.NEGATIVE_INFINITY;
 		double newMarketValue = 0.0;
 
 		for (int i = start; i < tastedSkittles.size(); i++) {
 			newMarketValue = marketPrefs[tastedSkittles.get(i).getColor()];
 			if (newMarketValue > currentMarketValue) {
 				unwantedColor = tastedSkittles.get(i);
 				currentMarketValue = newMarketValue;
 			}
 		}
 		return unwantedColor;
 	}
 
 	public double tradeUtility(Offer o, boolean taking) {
 		double valueOriginal = 0.0;
 		double valueLater = 0.0;
 
 		// what we receive is what they are offering
 		int[] in, out;
 		if (taking) {
 			in = o.getOffer();
 			// what we send is what they want
 			out = o.getDesire();
 		} else {
 			in = o.getDesire();
 			out = o.getOffer();
 		}
 
 		double[] colorValues = inventory.getColorValues();
 		
 		for (int i = 0; i < in.length; i++) {
 			int inCount = inventory.getSkittle(i).getCount();	
 			double value = colorValues[i] == -2 ? 0.001 : colorValues[i];
 			valueLater += value * Math.pow(inCount + in[i], 2);
 			valueOriginal += colorValues[i] * Math.pow(inCount, 2);
 		}
 
 		for (int j = 0; j < out.length; j++) {
 			int outCount = inventory.getSkittle(j).getCount();
 			double value = colorValues[j] == -2 ? 0.001 : colorValues[j];
 			valueLater += value * (Math.pow(outCount - out[j], 2));
 			valueOriginal += colorValues[j] * (Math.pow(outCount, 2));
 		}
 		
 		double netValue = 0;
 		if (valueOriginal < 0) {
 			netValue += valueOriginal;
 		} else {
 			netValue -= valueOriginal;
 		}
 		netValue += valueLater;
 		
 		/*
 		System.out.println("***********");
 		System.out.println("taking " + Arrays.toString(in));
 		System.out.println("giving " + Arrays.toString(out));
 		System.out.println(Arrays.toString(colorValues));
 		System.out.println("from " + valueOriginal + " to " + valueLater);
 		System.out.println(netValue);
 		*/
 		
 		return netValue;
 
 	}
 
 	// TODO: calculate the probability that a trade will be accepted
 	public double tradeAcceptanceProbability(Offer o) {
 		// Sid's model
 
 		// ???, profit
 		return 0.0;
 	}
 	
 	public double tradeCountProbability(Offer offer) {
 		double probability = 0;
 		for (int i = 0; i < playerCount; i++) {
 			if (i == this.selfIndex) {
 				continue;
 			}
 			probability = Math.max(tradeCountProbabilityPerPlayer(offer, i), probability);
 		}
 		return probability;
 	}
 	
 	private double tradeCountProbabilityPerPlayer(Offer offer, int player) {
 		int[] whatWeWant = offer.getDesire();
 		double probability = 1;
 		for (int i = 0; i < whatWeWant.length; i++) {
 			probability *= countProbability(whatWeWant[i], i, player);
 		}
 		return probability;
 	}
 	
 	private double countProbability(int count, int color, int player) {
 		double ourEstimate = estimatedCount[player][color];
 		return Math.max((1 - (count / (ourEstimate  + 1))), 0);
 	}
 	
 	public double scoreOurOffer(Offer o) {
 		double score = 0;
 		for (int i = 0; i < playerCount; i++) {
 			double newscore = scoreOurOffer(o, i);
 			if (newscore > score) {
 				score = newscore;
 			}
 		}
 		return score;
 	}
 	
 	public double scoreOurOffer(Offer o, int player) {
 		return this.tradeUtility(o, false) * getOtherHappiness(o, player) *
 				tradeCountProbabilityPerPlayer(o, player);
 	}
 	
 	//Is not called yet
 	public void triggerEndStage(int player) {
 		playerStage[player] = STAGE.END;
 	}
 
 	public void updateCountByTurn() {
 		turn++;
 		if (turn == 1) {
 			for (int j = 0; j < playerCount; j++) {
 				for (int i = 0; i < inventory.size(); i++) {
 					if (estimatedCount[j][i] > 1) {
 						estimatedCount[j][i] -= 1;
 					}
 				}
 			}
 			return;
 		}
 		if (turn > inventory.size()) {
 			for (STAGE s: playerStage) {
 				if (s == STAGE.DISCOVERY) {
 					s = STAGE.HOARD;
 				}
 			}
 		}	
 		for (int j = 0; j < playerCount; j++) {
 			if (playerStage[j] != STAGE.DISCOVERY) {
 				hoardEstimate(j);
 			}
 		}
 	}
 
 	private void hoardEstimate(int j) {
 		int zeroCount = 0;
 		for (int i = 0; i < inventory.size(); i++) {
 			if (estimatedCount[j][i] <= 0) {
 				zeroCount++;
 			}
 		}
 		for (int i = 0; i < inventory.size(); i++) {
 			if ((estimatedCount[j][i] - (1.0 / (inventory.size() - zeroCount)) > 1)) {
 				estimatedCount[j][i] -= 1.0 / (inventory.size() - zeroCount);
 			} else {
 				estimatedCount[j][i] = 0;
 			}
 		}
 	}
 
 	public void updateCountByOffer(Offer o) {
 		int proposer = o.getOfferedByIndex();
 		int selector = o.getPickedByIndex();
 		for (int i = 0; i < inventory.size(); i++) {
 			estimatedCount[selector][i] += o.getOffer()[i];
 			if (estimatedCount[proposer][i] - o.getOffer()[i] > 0) {
 				estimatedCount[proposer][i] -= o.getOffer()[i];
 			} else {
 				estimatedCount[proposer][i] = 0;
 			}
 			
 			if (estimatedCount[selector][i] - o.getDesire()[i] > 0) {
 				estimatedCount[selector][i] -= o.getDesire()[i];
 			} else {
 				estimatedCount[selector][i] = 0;
 			}
 			estimatedCount[proposer][i] += o.getDesire()[i];
 
 		}
 	}
 	
 	public void printEstimateCount() {
 		for (int i = 0; i < estimatedCount.length; i++) {
 			System.out.println("Player " + i + " estimate: " + aToS(estimatedCount[i]));
 		}
 	}
 	
 	public String aToS(double[] a) {
 		String ret = "[  ";
 		for (double d: a) {
 			ret+= d + "  ";
 		}
 		ret += "]";
 		return ret;
 	}
 	
 	public boolean isActive(int player) {
 		return playerStage[player] != STAGE.END;
 	}
 }
