 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ConcurrentHashMultiset;
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multiset;
 import com.google.common.collect.Table;
 
 public class GameState {
	private static final ExecutorService executor = Executors.newFixedThreadPool(8);
 	
 	// bit mask for the full deck, used for checking that a state is valid
 	private static final long FULL_DECK_MASK;
 
 	private static Map<String, File> CACHE_FILES;
 	
 	private static final LoadingCache<GameState, Map<String, Double>> CACHE =
 			CacheBuilder.newBuilder().concurrencyLevel(1)
 				.maximumSize(1500000L)
 				.recordStats()
 				.build(new CacheLoader<GameState, Map<String, Double>>() {
 					public Map<String, Double> load(GameState state) {
 						return state.calculateValue(Scorers.getScorers(), true);
 					}
 				});
 	
 	// instrumentation
 	private static final AtomicLong statesSolved;
 	private static final AtomicLong counter;
 	private static final Multiset<Double> statesSolvedByStreet;
 	private static final long startTime = System.currentTimeMillis();
 	private static long timestamp = 0L;
 	
 	static {
 		System.out.println(Runtime.getRuntime().maxMemory());
 		
 		statesSolved = new AtomicLong(0L);
 		counter = new AtomicLong(0L);
 		statesSolvedByStreet = ConcurrentHashMultiset.create();
 		
 		
 		FULL_DECK_MASK = createFullDeckMask();
 		CACHE_FILES = createCacheFiles();
 
 		long timestamp = System.currentTimeMillis();
 
 		BufferedReader reader = null;
 /*
 		try {
 			for (String cacheFileName : CACHE_FILES.keySet()) {
 				reader = new BufferedReader(new FileReader(cacheFileName));
 				String s = reader.readLine();
 				while (s != null) {
 					if (!s.trim().isEmpty()) {
 						GameState state = fromFileString(s);
 						Map<String, Double> cachedScores = CACHE.getIfPresent(state);
 						if (cachedScores == null) {
 							cachedScores = Maps.newHashMap();
 							cachedScores.put(cacheFileName, getValueFromFileString(s));
 							CACHE.put(state, cachedScores);
 						} else {
 							if (!cachedScores.containsKey(cacheFileName)) {
 								cachedScores.put(cacheFileName, getValueFromFileString(s));
 								CACHE.put(state, cachedScores);
 							}
 						}
 						updateInstrumentation(state.getStreet(), 1);
 					}
 					s = reader.readLine();
 				}
 				reader.close();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			System.out.println(CACHE.stats());
 			try {
 				reader.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("load from file took " + (System.currentTimeMillis() - timestamp));
 */
 	}
 	
 	private static final long createFullDeckMask() {
 		long fullDeck = 0L;
 		for (int i = 0; i < 52; i++) {
 			fullDeck <<= 1;
 			fullDeck |= 1L;
 		}
 		return fullDeck;
 	}
 	
 	private static final Map<String, File> createCacheFiles() {
 		Map<String, File> tempCacheMap = Maps.newHashMap();
 		for (Scorers.Scorer scorer : Scorers.getScorers()) {
 			File file = new File(scorer.getCacheFile());
 			System.out.println(file.getAbsolutePath());
 			if (!file.exists()) {
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 			tempCacheMap.put(scorer.getKey(), file);
 		}
 		return ImmutableMap.copyOf(tempCacheMap);
 	}
 	
 	// The current player
 	OfcHand player1;
 	OfcHand player2;
 	OfcDeck deck;
 	
 	public GameState(OfcHand player1, OfcHand player2, OfcDeck deck) {
 		this.player1 = player1;
 		this.player2 = player2;
 		this.deck = deck;
 		if (player1.getStreet() != player2.getStreet() &&
 			player1.getStreet() != player2.getStreet() - 1) {
 			throw new IllegalStateException("Player 1 must be on same or previous street as player 2");
 		}
 		
 		// Don't permit any dead cards.  We'd like game states to be normalized so that any 2 of {player1, player2, deck}
 		// define the state.
 		if (FULL_DECK_MASK != (player1.getBackMask() ^ player1.getMiddleMask() ^ player1.getFrontMask() ^
 				player2.getBackMask() ^ player2.getMiddleMask() ^ player2.getFrontMask() ^ deck.getMask())) {
 			throw new IllegalStateException("Hands + deck != full deck");
 		}
 	}
 	
 	private String getInstrumentation() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(statesSolved.longValue() + "\n");
 		long newTime = System.currentTimeMillis();
 		sb.append("total time: " + (newTime - startTime) / 1000 + " seconds \n");
 		sb.append("since last time: " + (newTime - timestamp) + " ms\n");
 		timestamp = newTime;
 		Double[] streets = statesSolvedByStreet.elementSet().toArray(new Double[0]);
 		Arrays.sort(streets);
 		for (Double street : streets) {
 			sb.append(street + ": " + statesSolvedByStreet.count(street) + "\n");
 		}
 		sb.append(CACHE.stats() + "\n");
 		return sb.toString();
 	}
 	
 	private static void updateInstrumentation(double street, int numStates) {
 		statesSolved.addAndGet(numStates);
 		counter.addAndGet(numStates);
 		statesSolvedByStreet.add(street, numStates);
 	}
 	
 	/**
 	 * Generate the value of each best state reachable from this one, i.e., optimal setting for each card. // lies
 	 * If a state is the best setting for any scorer, it will be returned.
 	 */
 	private Table<String, GameState, Double> generateBestStateValues(Scorers.Scorer[] scorers, boolean concurrent) {
 		if (player1.isComplete()) {
 			throw new IllegalStateException("Shouldn't generate new states for complete hand");
 		}
 
 		Table<String, GameState, Double> bestScores = HashBasedTable.create();
 		
 		for (OfcCard card : CardSetUtils.asCards(deck.getMask())) {
 
 			Table<String, GameState, Double> allScoresForHand = HashBasedTable.create();
 			
 			for (OfcHand hand : player1.generateHands(card)) {
 				GameState candidateState = new GameState(player2, hand, new OfcDeck(deck.withoutCard(card)));
 				Map<String, Double> scoresForHand = candidateState.getValue(scorers, concurrent);
 				for (Scorers.Scorer scorer : scorers) {
 					// Score is from opponent's perspective, so we flip it
 					allScoresForHand.put(scorer.getKey(), candidateState, scoresForHand.get(scorer.getKey()) * -1);
 				}
 				// it would be nice to filter out hopeless settings here
 			}
 
 			// Now we have the score for each setting of this card, filter out the unusable ones
 			boolean instrumented = false;
 			for (Scorers.Scorer scorer : scorers) {
 				// TODO: Double.MIN_VALUE doesn't compare properly.
 				GameState bestSet = null;
 				double bestValue = -1000000000000.0;
 				Map<GameState, Double> scoresForScorer = allScoresForHand.row(scorer.getKey());
 				for (Map.Entry<GameState, Double> entry : scoresForScorer.entrySet()) {
 					if (entry.getValue() > bestValue) {
 						bestSet = entry.getKey();
 						bestValue = entry.getValue();
 					}
 				}
 				bestScores.put(scorer.getKey(), bestSet, bestValue);
 				if (!instrumented) {
 					instrumented = true;
 					updateInstrumentation(bestSet.getStreet(), 1);
 				}
 			}
 			
 		}
 		
 		if (counter.longValue() > 100000) {
 			counter.set(0L);
 			System.out.println(getInstrumentation());
 		}
 
 		// sanity check
 		if (bestScores.columnKeySet().size() > (52 * 3 * scorers.length)) {
 			throw new IllegalStateException("shouldn't happen");
 		}
 
 		return bestScores;
 	}
 	
 	public boolean isComplete() {
 		return player1.isComplete() && player2.isComplete();
 	}
 	
 	/**
 	 * Get the value of this state for the first player.  If the game is complete, this is the score.
 	 * Otherwise, it is the average of all states generated by dealing the opponent a card.
 	 */
 	public Map<String, Double> getValue(Scorers.Scorer[] scorers, boolean concurrent) {
 		if (getStreet() > 13.0) {
 			throw new IllegalStateException("never happen");
 		}
 		Map<String, Double> values = CACHE.getUnchecked(this);
 		
 		for (Scorers.Scorer scorer : scorers) {
 			if (!values.containsKey(scorer.getKey())) {
 				// if we're missing any value, recalculate them all
 				return calculateValue(scorers, concurrent);
 			}
 		}
 		
 		return values;
 	}
 	
 	/**
 	 * Calculate the value when it's not available in the cache
 	 */
 	private Map<String, Double> calculateValue(Scorers.Scorer[] scorers, boolean concurrent) {
 		// 13th street, no more choices		
 		if (getStreet() == 13.0) {
 			return calculate13thStreet(scorers);
 		}
 
 		Map<String, Double> values;
 		if (getStreet() > 11.5 && concurrent) {
 			values = getValueConcurrent(scorers);
 		} else {
 			values = getValueSequential(scorers);
 		}
 		
 		if (getStreet() < 12.0) {
 			System.out.println("\n\nSolved");
 			System.out.println(this);
 			System.out.println(values);
 			System.out.println(getInstrumentation());
 
 			try {
 				for (Scorers.Scorer scorer : scorers) {
 					FileWriter fw = new FileWriter(CACHE_FILES.get(scorer.getCacheFile()).getAbsoluteFile(), true);
 					BufferedWriter bw = new BufferedWriter(fw);
 					bw.write(toFileString() + " " + values.get(scorer.getKey()) + "\n");
 					bw.flush();
 					bw.close();
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		// TODO: be smarter about cache policy
 		CACHE.put(this, values);
 		return values;
 	}
 
 	private Map<String, Double> calculate13thStreet(Scorers.Scorer[] scorers) {
 		int count = 0;
 		int[] sums = new int[scorers.length];
 		OfcCard[] cards =  CardSetUtils.asCards(deck.getMask());
 		CompleteOfcHand[] p2Hands = new CompleteOfcHand[cards.length];
 		for (int i = cards.length - 1; i >= 0; i--) {
 			p2Hands[i] = player2.generateOnlyHand(cards[i]);
 		}
 		for (int i = cards.length - 1; i >= 0; i--) {
 			OfcCard p1Card = cards[i];
 			CompleteOfcHand p1Hand = player1.generateOnlyHand(p1Card);
 			for (int j = cards.length - 1; j >= 0; j--) {
 				if (j != i) {
 					CompleteOfcHand p2Hand = p2Hands[j];
 					count++;
 					for (int k = 0; k < scorers.length; k++) {
 						sums[k] += scorers[k].score(p1Hand, p2Hand);
 					}
 				}
 			}
 		}
 		
 		int index = 0;
 		Map<String, Double> scores = Maps.newHashMap();
 		for (Scorers.Scorer scorer : scorers) {
 			scores.put(scorer.getCacheFile(), (double) sums[index] / count);
 			index++;
 		}
 
 		return scores;
 	}
 
 	public Map<String, Double> getValueSequential(Scorers.Scorer[] scorers) {
 		Table<String, GameState, Double> bestStates = generateBestStateValues(scorers, false);
 		
 		Map<String, Double> values = Maps.newHashMap();
 
 		for (String key : bestStates.rowKeySet()) {
 			int count = 0;
 			double sum = 0.0;
 			for (GameState state : bestStates.row(key).keySet()) {
 				count++;
 				sum += bestStates.get(key, state);
 			}
 			values.put(key, sum / count);
 		}
 		
 		return values;
 	}
 
 	public Map<String, Double> getValueConcurrent(final Scorers.Scorer[] scorers) {
 		List<Future<Map<String, Double>>> results = Lists.newArrayList();
 		Table<String, GameState, Double> bestStates = generateBestStateValues(scorers, false);
 		for (String key : bestStates.rowKeySet()) {
 			for (final GameState nextState : bestStates.row(key).keySet()) {
 				results.add(executor.submit(new Callable<Map<String, Double>>() {
 					@Override
 					public Map<String, Double> call() throws Exception {
 						return nextState.getValue(scorers, false);
 					}					
 				}));				
 			}
 		}
 				
 		Map<String, Double> values = Maps.newHashMap();
 		for (Future<Map<String, Double>> result : results) {
 			try {
 				Map<String, Double> score = result.get();
 				for (String scoreKey : score.keySet()) {
 					if (values.containsKey(scoreKey)) {
 						double value = values.get(scoreKey) + score.get(scoreKey);
 						values.put(scoreKey, value);
 					} else {
 						values.put(scoreKey, score.get(scoreKey));
 					}
 				}
 			} catch (Exception e) {
 				throw new RuntimeException("oh, this is bad");
 			}
 		}
 
 		for (String resultKey : values.keySet()) {
 			// Current player's value is the negative of the opponent's value.
 			// making an assumption here that every result has a result for each scorer
 			double returnValue = values.get(resultKey) / results.size() * -1;
 			values.put(resultKey, returnValue);
 		}
 		return values;
 	}
 		
 	private String toFileString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getStreet());
 		sb.append(" ");
 		sb.append(player1.toKeyString());
 		sb.append(" ");
 		sb.append(player2.toKeyString());
 		
 		return sb.toString();
 	}
 	
 	@VisibleForTesting
 	static GameState fromFileString(String fileString) {
 		String[] splitString = fileString.split(" ");
 
 		OfcHand p1 = LongOfcHand.fromKeyString(splitString[1]);
 		OfcHand p2 = LongOfcHand.fromKeyString(splitString[2]);
 		
 		return new GameState(p1, p2, new OfcDeck(deriveDeck(p1, p2)));
 	}
 	
 	@VisibleForTesting
 	static double getValueFromFileString(String fileString) {
 		String[] splitString = fileString.split(" ");
 		return Double.parseDouble(splitString[3]);
 	}
 	
 	@VisibleForTesting
 	static long deriveDeck(OfcHand p1, OfcHand p2) {
 		return ~(~FULL_DECK_MASK | p1.getFrontMask() | p1.getMiddleMask() | p1.getBackMask()
 					| p2.getFrontMask() | p2.getMiddleMask() | p2.getBackMask());
 	}
 	
 	public double getStreet() {
 		return (player1.getStreet() + player2.getStreet()) / 2.0;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((deck == null) ? 0 : deck.hashCode());
 		result = prime * result + ((player1 == null) ? 0 : player1.hashCode());
 		result = prime * result + ((player2 == null) ? 0 : player2.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		GameState other = (GameState) obj;
 		if (deck == null) {
 			if (other.deck != null)
 				return false;
 		} else if (!deck.equals(other.deck))
 			return false;
 		if (player1 == null) {
 			if (other.player1 != null)
 				return false;
 		} else if (!player1.equals(other.player1))
 			return false;
 		if (player2 == null) {
 			if (other.player2 != null)
 				return false;
 		} else if (!player2.equals(other.player2))
 			return false;
 		return true;
 	}
 
 	public static void shutdown() {
 		executor.shutdown();
 	}
 	
 	@Override
 	public String toString() {		
 		StringBuilder sb = new StringBuilder();
 		sb.append("Game state: \n");
 		sb.append("Street: " + getStreet() + "\n");
 		sb.append("Cards in deck: " + CardSetUtils.asCards(deck.getMask()).length + "\n");
 		sb.append(player1);
 		sb.append("\n");
 		sb.append(player2);
 		sb.append("\n");
 		if (isComplete()) {
 			sb.append(getValue(Scorers.getScorers(), true) + " points\n");
 		}
 		sb.append(deck);
 		return sb.toString();
 	}
 }
