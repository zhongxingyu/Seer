 package edu.fmi.ai.reversi;
 
 import java.util.Collection;
 
 import edu.fmi.ai.reversi.model.Board;
 import edu.fmi.ai.reversi.model.Player;
 
 public class GameSolver {
 
 	/**
 	 * {@value}
 	 */
 	private static final int MAX_LEVEL_SEARCH_DEPTH = 3;
 
 	public GameMoveHelper getOptimalMove(final Board state) {
 		final GameMoveHelper result = getOptimalMinMove(new GameSolverParameter(state,
 				Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, 0));
 		result.move = result.diff(state);
 		return result;
 	}
 
 	public GameMoveHelper getOptimalMinMove(final GameSolverParameter parameter) {
 		if (parameter.level == MAX_LEVEL_SEARCH_DEPTH) {
 			return new GameMoveHelper(parameter, Player.WHITE);
 		}
 
 		final Collection<Board> gameStates = parameter.getNextBoards(Player.WHITE);
 		GameMoveHelper result = new GameMoveHelper(Float.POSITIVE_INFINITY, parameter.board);
 
 		for (final Board nextState : gameStates) {
 			final GameSolverParameter nextParameter = GameSolverParameter.increasedLevel(nextState,
 					parameter);
 			final GameMoveHelper optimalMove = getOptimalMaxMove(nextParameter);
 			tryUpdateMinResult(parameter, result, nextState, optimalMove);
 
 			if (parameter.beta <= parameter.alpha) {
 				return result;
 			}
 		}
 
 		return result;
 	}
 
 	public GameMoveHelper getOptimalMaxMove(final GameSolverParameter parameter) {
 		if (parameter.level == MAX_LEVEL_SEARCH_DEPTH) {
 			return new GameMoveHelper(parameter, Player.BLACK);
 		}
 
 		final Collection<Board> gameStates = parameter.getNextBoards(Player.BLACK);
 		GameMoveHelper result = new GameMoveHelper(Float.NEGATIVE_INFINITY, parameter.board);
 
 		for (final Board nextState : gameStates) {
 			final GameSolverParameter nextParameter = GameSolverParameter.increasedLevel(nextState,
 					parameter);
 
 			final GameMoveHelper optimalMove = getOptimalMinMove(nextParameter);
 
 			tryUpdateMaxResult(parameter, result, nextState, optimalMove);
 			if (parameter.beta <= parameter.alpha) {
 				return result;
 			}
 		}
 		return result;
 	}
 
 	private void tryUpdateMaxResult(final GameSolverParameter parameter, GameMoveHelper result,
 			final Board nextState, final GameMoveHelper next) {
		if (result.value <= next.value) {
 			result.value = next.value;
 			result.state = nextState;
 			parameter.alpha = next.value;
 		}
 	}
 
 	private void tryUpdateMinResult(final GameSolverParameter parameter, GameMoveHelper result,
 			final Board nextState, final GameMoveHelper next) {
		if (result.value >= next.value) {
 			result.value = next.value;
 			result.state = nextState;
 			parameter.beta = next.value;
 		}
 	}
 
 }
