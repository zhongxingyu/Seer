 package org.ggp.base.player.gamer.statemachine.sample;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections4.map.AbstractReferenceMap;
 import org.apache.commons.collections4.map.ReferenceMap;
 import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
 import org.ggp.base.util.statemachine.MachineState;
 import org.ggp.base.util.statemachine.Move;
 import org.ggp.base.util.statemachine.Role;
 import org.ggp.base.util.statemachine.StateMachine;
 import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
 import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
 
 /**
  * Fabulous Multiplayer
  * 
  * @author Nera, Nicolai
  *
  */
 final class FabulousMultiPlayer2 extends SampleGamer {
 	
 	private static final int MAX_SCORE = 100;
 	
 	private static final int MIN_SCORE = 0;
 	
 	private static final ReferenceStrength soft = AbstractReferenceMap.ReferenceStrength.SOFT;
 	
 	/**
 	 * Minimax internal node return value.
 	 * Holds a score and information about completeness of exploration.
 	 */
 	private class Tuple {
 		protected int score;
 		protected boolean complete;
 
 		protected Tuple (int score, boolean complete){
 			this.complete = complete;
 			this.score = score;
 		}
 	}
 	
 	/**
 	 * Thrown if a timeout occurs during search.
 	 */
 	private class TimeoutException extends RuntimeException {
 		private static final long serialVersionUID = 7485356568086889532L;
 		
 		public TimeoutException(){
 			super();
 		}
 	}
 	
 	private ReferenceMap<MachineState, Tuple> transposition;
 	
 	private Role role;
 	
 	private StateMachine theMachine;
 	
 	private MachineState currentState;
 	
 	@Override
 	public void setState(MachineState state){
 		currentState = state;
 	}
 	
 	@Override
 	public void stateMachineMetaGame(long timeout){
 		timeout -= 500;
 		theMachine = getStateMachine();
 		role = getRole();
 		transposition = new ReferenceMap<MachineState, Tuple>(soft, soft);
 		minimax(currentState, timeout, true);
 	}
 	
 	@Override
 	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
 		timeout -= 500;
 		Move move = minimax(currentState, timeout, false);
 		if(move != null){
 			return move;
 		}
 		System.out.println("Playing random move.");
 		return theMachine.getRandomMove(currentState, role);
 	}
 	
 	/**
 	 * Performs exhaustive minimax search in a state.
 	 * 
 	 * @param state Game state
 	 * @param timeout Time limit
 	 * @param optimal If true, null is returned unless an optimal move is found
 	 * @return Ideal move
 	 */
 	private Move minimax(MachineState state, long timeout, boolean optimal){
 		List<Move> moves;
 		try {
 			moves = theMachine.getLegalMoves(state, role);
 		} catch (MoveDefinitionException e) {
 			System.err.println("No legal moves!");
 			return null;
 		}
 		boolean notDone = true;
 		int depth = 1;	//Change to something high for testing with output!
 		int bestScore = MIN_SCORE - 1;
 		Move bestMove =  null;
 		Search:
 		while(notDone){
 			if(optimal){
 				bestMove =  null;
 				bestScore = MIN_SCORE - 1;
 			}
 			if(System.currentTimeMillis() > timeout){
 				System.out.println("Ran out of time!");
 				break;
 			}
 			depth++;
 			int alpha = MIN_SCORE - 1;
 			int beta = MAX_SCORE + 1;
 			notDone = false;
 			for (Move move: moves){
 				Tuple tempScore = new Tuple(bestScore, false);
 				try {
 					tempScore = minPlayer (state, move, depth, timeout, alpha, beta);
 				} catch (TimeoutException e){
 					System.out.println("Ran out of time!");
 					if(optimal){
 						bestMove = null;
 					}
 					break Search;
 				}
 				if (!tempScore.complete){
 					notDone = true;
 				}
 				if (tempScore.complete && tempScore.score > bestScore){
 					bestScore = tempScore.score;
 					bestMove = move;
 				}			
 			}		
 		}
 		//System.out.println("Done. Depth: " + depth);
 		return bestMove;
 	}
 	
 	/**
 	 * Recursively performs minimax search (max-player move)
 	 * 
 	 * @param state Game state
 	 * @param depth Depth limit
 	 * @param timeout Time limit
 	 * @param alpha Alpha value
 	 * @param beta Beta value
 	 * @return Best score
 	 * @throws TimeoutException Time limit exceeded
 	 */
 	private Tuple maxPlayer(MachineState state, int depth, long timeout, int alpha, int beta) throws TimeoutException{
 		if(System.currentTimeMillis() > timeout){
 			throw new TimeoutException();
 		}
 		if(theMachine.isTerminal(state)){
 			Tuple ret;
 			try {
 				ret = new Tuple(theMachine.getGoal(state, role), true);
 			} catch (GoalDefinitionException e) {
 				System.err.println("Bad goal description!");
 				ret = new Tuple(Integer.MIN_VALUE, false);
 			}
 			//System.out.println("Found a goal of value " + ret.score);
 			transposition.put(state, ret);
 			return ret;
 		}
 		if (transposition.containsKey(state) && transposition.get(state).complete){
 			return transposition.get(state);
 		}
 		if(depth == 0){
 			return new Tuple (Integer.MIN_VALUE, false);
 		}
 		
 		int bestScore = MIN_SCORE - 1;
 		//Move bestMove = null;
 		//boolean pruned = false;
 		boolean complete = true;
 		boolean foundOne = false;
 		List<Move> moves;
 		try {
 			moves = theMachine.getLegalMoves(state, role);
 		} catch (MoveDefinitionException e) {
 			System.err.println("No legal moves!");
 			return new Tuple(Integer.MIN_VALUE, false);
 		}
 		for(Move move : moves){
 			Tuple s = minPlayer(state, move, depth, timeout, alpha, beta);
 			if(!s.complete){
 				complete = false;
 				//return new Tuple (Integer.MIN_VALUE, false);
 			}	
 			if(s.score != Integer.MIN_VALUE){
 				foundOne = true;
 				if(s.score > bestScore){
 					bestScore = s.score;
 					//bestMove = move;
 				}
 			}
 		}
 		
 		if(! foundOne){
 			bestScore = Integer.MIN_VALUE;
 		}
 		Tuple ret = new Tuple(bestScore, complete);
 		if(complete){
 			transposition.put(state, ret);
 		}
 		return ret;
 	}
 	
 	/**
 	 * Recursively performs minimax search (min-player move)
 	 * 
 	 * @param state Game state
 	 * @param move Max-player's move
 	 * @param depth Depth limit
 	 * @param timeout Time limit
 	 * @param alpha Alpha value
 	 * @param beta Beta value
 	 * @return Worst score
 	 * @throws TimeoutException Time limit exceeded
 	 */
 	private Tuple minPlayer(MachineState state, Move move, int depth, long timeout, int alpha, int beta) throws TimeoutException{
 		if( System.currentTimeMillis() > timeout){
 			throw new TimeoutException();
 		}
 		
 		List<List<Move>> options = new ArrayList<List<Move>>();
 		List<Role> roles = theMachine.getRoles();
 		int fabulous = 0;
 		for(int i = 0; i < roles.size(); i++){
 			Role player = roles.get(i);
 			if(player.equals(role)){
 				fabulous = i;
 				continue;
 			}
 			List<Move> moves;
 			try {
 				moves = theMachine.getLegalMoves(state, player);
 			} catch (MoveDefinitionException e) {
 				System.err.println("No legal moves!");
 				moves = new ArrayList<Move>();
 			}
 			options.add(moves);
 		}
 		Set<List<Move>> next = combinations(options);
 		
 		MachineState nextState;
 		int worstScore = MAX_SCORE + 1;
 		boolean complete = true;
 		boolean foundOne = false;
 		for(List<Move> moves : next){
 			//System.out.println("Expanding " + moves.get(0).toString());
 			moves.add(fabulous, move);
 			try {
 				nextState = theMachine.getNextState(state, moves);
 			} catch (TransitionDefinitionException e) {
 				System.err.println("Attempted bad moves!");
 				complete = false;
 				continue;
 			}
 			Tuple s = maxPlayer(nextState, depth - 1, timeout, alpha, beta);
 			if(!s.complete){
 				complete = false;
 				//return new Tuple (Integer.MIN_VALUE, false);
 			}
 			if (s.score != Integer.MIN_VALUE){
 				foundOne = true;
 				if(s.score < worstScore){
 					worstScore = s.score;
 				}
 			}
 		}
 		
 		if (!foundOne){
 			worstScore = Integer.MIN_VALUE;
 		}
 		return new Tuple (worstScore, complete);
 	}
 	
 	/**
 	 * Creates all combinations of moves for opposing players.
 	 * 
 	 * @param moves List of all possible moves for all opposing player (in order)
 	 * @return Set of combinations of one move per opposing player (maintains order)
 	 */
 	private Set<List<Move>> combinations(List<List<Move>> moves){
 		Set<List<Move>> ret = new HashSet<List<Move>>();
 		if(moves.size() == 0){
 			return ret;
 		}
 		int num = 1;
 		for(List<Move> l : moves){
 			num *= l.size();
 		}
 		for(int i = 0; i < num; i++){
 			int tmp = i;
 			List<Move> combination = new ArrayList<Move>();
 			for(int r = 0; r < moves.size(); r++){
 				List<Move> l = moves.get(r);
 				combination.add(l.get(tmp % l.size()));
 				tmp /= l.size();
 			}
 			ret.add(combination);
 		}
 		return ret;
 	}
 	
 }
 
 
 
