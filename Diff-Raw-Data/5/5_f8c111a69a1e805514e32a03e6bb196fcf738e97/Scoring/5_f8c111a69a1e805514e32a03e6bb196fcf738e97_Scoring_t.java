 package util;
 
 import bot.Direction;
 import bot.Map;
 import bot.Position;
 import bot.Square;
 import bot.SquareType;
 
 public class Scoring {
 	
 	private final static double MOVE_COST = 8;
 	private final static double MINIMAL_CONSERVATION = 1;
 	private final static double PERCENT_CONSERVATION = 0.45;
 	
 	public final static double LEVEL_SCORE = 2000;
 	
 	public final static double VISIT_SCORE = 1;
 	public final static double UNKNOWN_SCORE = 2;
 	public final static double PASSAGE_SCORE = 20;
 	public final static double OPEN_SCORE = 50;
 	public final static double OPEN_FADING = 0.8;
 	public final static double FORCE_SCORE = 50;
 	public final static double FORCE_FADING = 0.8;
 	public final static double FOUND_SCORE = 250;
 	
 	private final static int TRIES_PER_SEARCH = 5;
 	
 	public final static double DEAD_END_SEARCH_RATIO = 2;
 	
 	
 	/**
 	 * SEARCH_K is used to smooth the probability of founding something
 	 */
 	private final static double SEARCH_K = 1;
 	
 	/**
 	 * OPEN_K is used to smooth the probability of opening something
 	 */
 	private final static double OPEN_K = 1;
 	
 	public static double smoothedProbability(int nbSuccess, int nbTries, double k){
 		return (nbSuccess + k / 2) / (nbTries + k);
 	}
 	
 	/**
 	 * Return the value of a single Square when searched, abstracting from
 	 * all environment.
 	 * @param s
 	 * @return A number between 0 and 1
 	 */
 	public static double localSearchScore(Map m, Square s){
 		if (!s.getType().searchable())
 			return 0;
 		double searchedValue = s.getNbSearch() /
				(hiddenProbability(m,s) * TRIES_PER_SEARCH * (m.getNbCompleteSearches() + 1));
 		if (searchedValue > 1)
 			return 0;
 		return (1 - searchedValue);
 	}
 	
 	/**
 	 * Return the probability that something is hidden in the specified
 	 * square according to the neighborhood
 	 * @param s
 	 */
 	public static double hiddenProbability(Map m, Square s){
 		int nbPassages = 0;
 		int nbEmptyOrDoors = 0;
 		// Counting neighbors passage
 		for (Square neighbor : s.getNeighbors()){
 			if (neighbor.getType() == SquareType.PASSAGE)
 				nbPassages++;
 			if (neighbor.getType() == SquareType.EMPTY ||
 				neighbor.getType() == SquareType.CLOSED_DOOR)
 				nbEmptyOrDoors++;
 			if (neighbor.isRoomUnknownExit(m))
 				return DEAD_END_SEARCH_RATIO;
 		}
 		if (nbPassages == 1 && nbEmptyOrDoors == 0)
 			return DEAD_END_SEARCH_RATIO;
 		return 1;
 	}
 	
 	/**
 	 * Compute the score of a search, taking into account the environment.
 	 * If the square is not a reachable type, return 0.
 	 * This function assume that isReachable returns an up to date value for
 	 * the specified Position. Every Square of the neighborhood must have it's
 	 * localSearchScore up to date.
 	 * @param m The map on which the search
 	 * @param p The position where the search would be performed
 	 * @return A number between 0 and 1
 	 */
 	public static double environmentalSearchScore(Map m, Position p){
 		Square src = m.getSquare(p);
 		if (!src.isReachable())
 			return 0;
 		double score = 0;
 		for (Direction d : Direction.values()){
 			Square neighbor = m.getSquare(Position.add(p, d));
 			if (neighbor != null)
 				score += neighbor.getLocalSearchScore();
 		}
 		return score / 8;
 		
 	}
 	
 	public static double visitScore(Map m, Position p){
 		Square s = m.getSquare(p);
 		
 		if (s == null || !s.isReachable() || s.getNbVisits() > 0)
 			return 0;
 		double score = 0;
 		for (Square n : s.getNeighbors())
 			if (n.getType() == SquareType.UNKNOWN)
 				score += UNKNOWN_SCORE;
 		if (s.getType() == SquareType.PASSAGE)
 			return PASSAGE_SCORE + score;
 		return 1 + score;
 	}
 	
 	public static double openScore(Square s){
 		if (s.getType() != SquareType.CLOSED_DOOR) return 0;
 		return Math.pow(OPEN_FADING, s.getNbOpenTries());
 		//return smoothedProbability(0, s.getNbOpenTries(), OPEN_K);
 	}
 	
 	public static double forceScore(Square s){
 		if (s.getType() != SquareType.CLOSED_DOOR) return 0;
 		return Math.pow(FORCE_FADING, s.getNbForceTries());
 		//return smoothedProbability(0, s.getNbOpenTries(), OPEN_K);
 	}
 	
 	public static double afterMoveScore(double initialScore){
 		double newScore = initialScore - Scoring.MOVE_COST;
 		if (newScore < Scoring.MINIMAL_CONSERVATION)
 			newScore = initialScore * Scoring.PERCENT_CONSERVATION;//Avoiding that all squares get the same score
 		return newScore;
 	}
 
 	public static double levelChangeScore(Map m, Square s) {
 		if (s.getType() == SquareType.WAY_DOWN &&
 		    s.getStairsDest() == null)
 			return 1;
 		return 0;
 	}
 }
