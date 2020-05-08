 package edu.neumont.learningChess.engine;
 
 import java.util.ArrayList;
 import java.util.Iterator;
import java.util.List;
 import java.util.Stack;
 
 import edu.neumont.learningChess.api.ChessGameState;
import edu.neumont.learningChess.api.ExtendedMove;
 import edu.neumont.learningChess.api.MoveHistory;
 import edu.neumont.learningChess.controller.GameController;
import edu.neumont.learningChess.controller.GameController.PlayerType;
 import edu.neumont.learningChess.controller.HistoryAnalyzer;
 import edu.neumont.learningChess.engine.persistence.PersistentGameStateCache;
 import edu.neumont.learningChess.model.ChessBoard;
 import edu.neumont.learningChess.model.Move;
import edu.neumont.learningChess.model.ProxyPlayer;
 import edu.neumont.learningChess.model.SingletonRandom;
 public class LearningEngine implements HistoryAnalyzer {
 
 	private PersistentGameStateCache persistence;
 
 	private LearningEngine(PersistentGameStateCache cache) {
 		persistence = cache;
 	}
 
 	public static void create(String fileName, long maxListSize) {
 		final long recordSize = SerializedChessGameState.getRecordSize();
 		PersistentGameStateCache.create(fileName, recordSize, recordSize, maxListSize);
 	}
 
 	public static void delete(String fileName) {
 		PersistentGameStateCache.delete(fileName);
 	}
 
 	public static LearningEngine open(String fileName) {
 		LearningEngine ai = new LearningEngine(PersistentGameStateCache.open(fileName));
 		return ai;
 	}
 
 	public void close() {
 		persistence.close();
 	}
 
 	public Move getMove(ChessGameState gameState) {
 		GameController control = new GameController(gameState, this);
 		SearchResult result = findBestMove(control);
 		return result.getMove();
 	}
 	
 	public GameStateInfo getGameStateInfo(ChessGameState gameState) {
 		return persistence.get(gameState);
 	}
 
 	public void analyzeGameHistory(MoveHistory history) {
 		GameController controller = new GameController(this, history);
 		controller.play();
 	}
 	@Override
 	public void analyzeStack(Stack<ChessGameState> historyStack) {
 		int denominator = (int) Math.floor(historyStack.size() / 2.0);
 		boolean winner = false;
 		int count = denominator;
 		while (historyStack.size() > 1) {
 			ChessGameState current = historyStack.pop();
 			float value = count / denominator * (winner ? 1 : -1);
 			GameStateInfo toUpdate = persistence.get(current);
 			if (toUpdate == null)
 				toUpdate = new GameStateInfo(SerializedChessGameState.serialize(current));
 			toUpdate.addObservation(value);
 			persistence.put(current, toUpdate);
 			if (winner)
 				count--;
 			winner = !winner;
 		}
 	}
 	
 	@Override
 	public void analyzeStaleStack(Stack<ChessGameState> historyStack) {
 		while (historyStack.size() > 1) {
 			ChessGameState current = historyStack.pop();
 			GameStateInfo toUpdate = persistence.get(current);
 			toUpdate.addObservation(0);
 			persistence.put(current, toUpdate);
 		}
 	}
 
 	private SearchResult findBestMove(GameController gc) {
 		ArrayList<SearchResult> results = null;
 		float bestValue = 0;
 		ChessBoard board = gc.getBoard();
 		for (Iterator<Move> i = gc.getCurrentTeam().getMoves(board); i.hasNext();) {
 			Move move = i.next();
 			board.tryMove(move);
 			float moveValue = getBoardValue(gc);
 			board.undoTriedMove();
 			if ((results == null) || (moveValue > bestValue)) {
 				SearchResult result = new SearchResult(move, moveValue);
 
 				results = new ArrayList<SearchResult>();
 
 				results.add(result);
 				bestValue = moveValue;
 			} else if (moveValue == bestValue) {
 				SearchResult result = new SearchResult(move, moveValue);
 				results.add(result);
 			}
 		}
 		return ((results == null) || (results.size() == 0)) ? null : results.get(SingletonRandom.nextInt(results.size()));
 	}
 
 	private float getBoardValue(GameController gc) {
 		ChessGameState gameState = gc.getCurrentGameState();
 		GameStateInfo info = persistence.get(gameState);
 		return info != null ? info.getAverage() : 0;
 	}
 
 	private class SearchResult {
 		private Move move;
 		public SearchResult(Move move, float value) {
 			this.move = move;
 		}
 		public Move getMove() {
 			return move;
 		}
 	}
 
 }
