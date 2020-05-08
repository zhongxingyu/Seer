 package game;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import search.DepthLimitedSearch;
 import search.Node;
 
 import agent.Action;
 import agent.Actions;
 import agent.Percept;
 import mixmeta4.Board;
 import mixmeta4.Move;
 import mixmeta4.Moves;
 import mixmeta4.Piece;
 import mixmeta4.PieceSet;
 import mixmeta4.Player;
 
 /*
  * A one-step look-ahead agent will try each of the possible moves using,
  * a model of the environment - in this case the Board that has been provided,
  * to you as the percept - and evaluate the resulting positions to select the,
  * most promising one. 
  * This evaluation function is an example of a utility function,
  * (hence this is a simple utility-based agent).
  * 
  * For example, one simple strategy would be to move to a position that,
  * minimises the number of options available to your opponent. 
  * In this case the utility function might be the negation of the number,
  * of moves available to the opponent (so that fewer moves gives a higher value).
  * 
  * Implement an agent with this evaluation function. You can do this by,
  * using the Board as your implementation of State - cloning a new copy,
  * of the Board for each available move (so you don't change the actual game), 
  * and trying the move out by passing it the cloned board's update function. 
  * The resulting position can be examined in an implementation of NodeInfo.
  * 
  * Try the new agent out against your reflex agent to see if it is more successful. 
  * The extra competency may be more apparent with some board sizes, configurations, 
  * and number of pieces than others, so you may wish to try a few.
  * 
  * What other one-step strategies can you think of? 
  * Note that you have access to the current pieces through board.getRedPieces(),
  * and board.getBlackPieces() methods. 
  * Try to develop the optimal one-step strategy/evalutation function. 
  * Try your proposals out by building a player and playing off against, 
  * the players provided and other student's players and see if they appear,
  * successful.
  *   
  */
 
 /*
  * function Minimax-Decision(game) returns an operator
    for each op in Operators[game] do
         Value[op] ← Minimax-Value(Apply(op, game), game)
    end
    return the op with the highest Value[op]
 function Minimax-Value(state, game) returns a utility value
    if Terminal-Test[game](state) then
         return Utility[game](state)
    else if max is to move in state then
         return the highest Minimax-Value of Successors(state)
    else
         return the lowest Minimax-Value of Successors(state)
 
  */
 
 public class EricSUA extends Player implements agent.Agent {
 
 	CheXNodeInfo nodeInfo;
 	Random random;
 	int maxPly;
 	
 	public EricSUA(boolean isRed) {
 		super(isRed, "Eric - Simple Utility Agent");
 		this.nodeInfo = new CheXNodeInfo(isRed, 2, null);
 		this.random = new Random();
 		maxPly = 3;
 		System.out.println();
 		if(this.isRed) System.out.println(this.name + " is red");
 		if(!this.isRed) System.out.println(this.name + "is blue");
 	}
 
 	@Override
 	public Action getAction(Percept cP) {
 		 Board board = (Board) cP;
 		 Moves fullMoveList = (Moves)board.getActions();
 		 
 		 PieceSet currentPieceSet = board.getRedPieces();
 		 if(this.isRed) {
 		 } else {
 			 currentPieceSet = board.getBlackPieces();
 		 }
 			 
 		 // Get list of all my current pieces.
 		 
 	     ArrayList<MoveValuePair> bestMoveValuePairs = new ArrayList<MoveValuePair>();
 		 for(Iterator<Piece> piece  = currentPieceSet.iterator(); piece.hasNext(); ) {
 			 
 			 Piece currentPiece = piece.next();
 			 for(Iterator<Move> moves = ((Moves)(board.getActions(currentPiece))).iterator(); moves.hasNext(); ) {
 				 Move move = moves.next();
 				 Board clone = (Board)board.clone();
 				 clone.update(move);
 				 bestMoveValuePairs.add(new MoveValuePair(move, optimumMoveValueForPiece(board, clone)));
 			 }
 		 }
 		 
 		 double best = Double.NEGATIVE_INFINITY;
 		 Move bestMove = (Move)fullMoveList.get(random.nextInt(fullMoveList.size()));
 		 
 		 for(Iterator<MoveValuePair> findBest = bestMoveValuePairs.iterator(); findBest.hasNext(); ) {
 			 MoveValuePair currentPair = findBest.next();
 			 if(currentPair.getValue() > best) {
				 best = currentPair.getValue();
 				 bestMove = currentPair.getMove();
 			 }
 		 }
 	     return bestMove;
 	}
 	
 	// Must return the max value for me for the piece that was moved for this board.
 	public double optimumMoveValueForPiece(Board original, Board withMove) {
 		int nPly = 0;
 		double result = 0;
 		
 		result = minForO(original, withMove, nPly);
 		return result;
 	}
 	
 	public double maxForP(Board original, Board withMove, int nPly) {
 		double result = 0;
 		double maxValue = Double.NEGATIVE_INFINITY;
 		ArrayList<MoveValuePair> bestMoveValuePairs = new ArrayList<MoveValuePair>();
 		
		nPly++;
		if(nPly > maxPly) {
			return 0;
		}
		
 		for(Iterator<Move> pMove = withMove.getActions().iterator(); pMove.hasNext(); ) {
 			Move currentMove = pMove.next();
 			Board clone = (Board)withMove.clone();
 			clone.update(currentMove);
 			bestMoveValuePairs.add(new MoveValuePair(currentMove, nodeInfo.utility(new Node(clone, new Actions()))));
 		}
 		
 		Move bestMove = (Move)withMove.getActions().get(random.nextInt(withMove.getActions().size()));
 		for(Iterator<MoveValuePair> findBest = bestMoveValuePairs.iterator(); findBest.hasNext(); ) {
 			 MoveValuePair currentPair = findBest.next();
 			 if(currentPair.getValue() > maxValue) {
 				 maxValue = currentPair.getValue();
 				 bestMove = currentPair.getMove();
 			 }
 		 }
 		 withMove.update(bestMove);
		 result += minForO(original, withMove, nPly);
 		 return result;
 	}
 	
 	public double minForO(Board original, Board withMove, int nPly) {
		double result = 0;
		double minValue = Double.POSITIVE_INFINITY;
		ArrayList<MoveValuePair> worstMoveValuePairs = new ArrayList<MoveValuePair>();
 		
		nPly++;
 		if(nPly > maxPly) {
 			return 0;
 		}
 		
 		for(Iterator<Move> oMove = withMove.getActions().iterator(); oMove.hasNext(); ) {
 			Move currentMove = oMove.next();
 			Board clone = (Board)withMove.clone();
 			clone.update(currentMove);
 			worstMoveValuePairs.add(new MoveValuePair(currentMove, nodeInfo.utility(new Node(clone, new Actions()))));
 		}
 		
 		Move worstMove = (Move)withMove.getActions().get(random.nextInt(withMove.getActions().size()));
 		for(Iterator<MoveValuePair> findWorst = worstMoveValuePairs.iterator(); findWorst.hasNext(); ) {
 			 MoveValuePair currentPair = findWorst.next();
 			 if(currentPair.getValue() < minValue) {
 				 minValue = currentPair.getValue();
 				 worstMove = currentPair.getMove();
 			 }
 		 }
 		 withMove.update(worstMove);
		 result += maxForP(original, withMove, nPly);
 		 return result;
 	}
 	
 	public class MoveValuePair {
 		
 		Move move;
 		double value;
 		
 		public MoveValuePair(Move theMove, double theValue) {
 			this.move = theMove;
 			this.value = theValue;
 		}
 		
 		public double getValue() {
 			return this.value;
 		}
 		
 		public Move getMove() {
 			return this.move;
 		}
 	}
 
 }
