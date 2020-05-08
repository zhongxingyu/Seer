 package com.tictactoe.search;
 
 import com.tictactoe.move.*;
 import com.tictactoe.player.*;
 import com.tictactoe.table.*;
 
 /**
  * The <code>Search</code> class is a class made useful for making
  * searches in a known and generated search tree. It is not yet
  * fully implemented as envisioned but will suffice for now.
  * Adding a search object to the game is necessary.
  * (As of now, the thought that players, and not games, should have
  * search objects, have been discarded, and only objects of the
  * <code>Game</code> class have objects of this class. This may be
  * alternate approach may be looked into in the future.)
  * @author Kenshin Himura
  *
  */
 public class Search
 {
 	/**
 	 * Holds the current best move found in the search.
 	 */
 	private Move bestMove;
 	/**
 	 * Used to hold the score of the best state found so far in the search.
 	 */
 	private double bestScore;
 	/**
 	 * The current best state of the search, may be updated as and when
 	 * search gets deeper or shallower, with respect to the best move
 	 * found.
 	 */
 	private Table bestState;
 	/**
 	 * Holds the current player for whom it is searching, as this is
 	 * necessary for the search, especially if algorithms like NegaMax
 	 * are used.
 	 */
 	private Player currentPlayer;
 	/**
 	 * Used as a current-score keeper. Was easier to have it global,
 	 * so made it as a data member of the search class.
 	 */
 	private double currentScore;
 	/**
 	 * Holds the current search-state of the table used in the search.
 	 * Keeps changing as the search progresses.
 	 */
 	private Table currentSearchState;
 	/**
 	 * Holds the current state of the table used in the game. Passed
 	 * as argument during construction and MUST remain unchanged, as
 	 * Java passes arguments as reference by default, and any change
 	 * will be reflected back.
 	 */
 	private Table currentState;
 	/**
 	 * This holds the maximum number of possible legal moves for the given
 	 * state and so by default acts as the sizeOf() or length member of the
 	 * moveStack array.
 	 */
 	private int maxMoves;
 	/**
 	 * The moveStack is an array of objects of type <code>Move</code>
 	 * and is produced by adding all legal moves on the current state
 	 * and pushing them to the moveStack. This is performed by the moveGen()
 	 * method.
 	 */
 	private Move moveStack[];
 	/**
 	 * Holds the required search depth for the current instance of class
 	 * <code>Search</code>.
 	 */
 	private int searchDepth;
 	/**
 	 * Unused as of now, but may be used to enforce a time constraint on
 	 * the search. Will most likely be specified in milliseconds.
 	 */
 	private float time;
 	/**
 	 * Default constructor of <code>Search</code> class. No use in default
 	 * construction of a search object, so an empty constructor is made, to enable
 	 * definition of parameterized constructors.
 	 */
 	public Search()
 	{
 		
 	}
 	/**
 	 * Since the <code>Search</code> class is independant of other classes, viz.
 	 * Player, Game and Table, the current player and the current state of the
 	 * game must be passed on to the constructor to properly initialize an
 	 * instantiation of this class.
 	 * @param currentState Current State of the Game (Type <code>Table</code>)
 	 * @param currentPlayer Current Player of the Game (Type <code>Player</code>)
 	 */
 	public Search(Table currentState,Player currentPlayer)
 	{
 		setCurrentState(currentState);
 		setCurrentPlayer(currentPlayer);
		setSearchDepth(this.currentState.getNoOfDs());
 		setCurrentSearchState(getCurrentState());
 	}
 	/**
 	 * Mini-max with alpha beta pruning to determine best score and move
 	 * @param nodeTable Current node of search tree
 	 * @param depth Depth of search
 	 * @param alpha Initial alpha cut-off value
 	 * @param beta Initial beta cut-off value
 	 * @param current Current player (for sign change and move generation)
 	 * @return The best score in negaMax fashion
 	 */
 	private double alphaBeta(Table nodeTable, int depth, double alpha, double beta, Player current)
 	{
 		Table node=nodeTable.clone();
		if((depth==1)||(node.isComplete()!=-1))
 			return node.getScore();
 		Player tempPlayer;
 		if(current.getPlayerType()!="User")
 			tempPlayer=new Player("Temp");
 		else
 			tempPlayer=new Player();
 		Search tempSearch=new Search(node, tempPlayer);
 		tempSearch.moveGen();
 		if(tempPlayer.getPlayerType()=="User")
 		{
 			for(int i=0;i<tempSearch.getMaxMoves();i++)
 			{
 				alpha=Math.max(alpha,alphaBeta(node.makeMove(tempSearch.getMove(i)),depth-1,alpha,beta,tempPlayer));
 				if(beta<=alpha)
 					break;
 			}
 			return alpha;
 		}
 		else
 		{
 			for(int i=0;i<tempSearch.getMaxMoves();i++)
 			{
 				beta=Math.min(beta,alphaBeta(node.makeMove(tempSearch.getMove(i)),depth-1,alpha,beta,tempPlayer));
 				if(alpha>=beta)
 					break;
 			}
 			return beta;
 		}
 	}
 	/**
 	 * The generic getter for the bestMove variable has been modified such
 	 * that it generates moves using the moveGen() function, and reads
 	 * every move from the moveStack, evaluates the resulting states (table
 	 * positions) and then picks the best move out of the moveStack and
 	 * sets this as the best move, before returning it.
 	 * @return Best Move found in the current Search
 	 */
 	public Move getBestMove()
 	{
 		double infinity=Math.pow((currentState.getSizeOfTable()+1),(currentState.getNoOfRows()+1));
 		bestScore=-infinity;
 		currentScore=bestScore;
 		moveGen();
 		for(int i=0;i<maxMoves;i++)
 		{
 			currentSearchState=currentState.makeMove(moveStack[i]);
 			currentScore=alphaBeta(currentSearchState,searchDepth,-infinity,infinity,currentPlayer);
 			if(currentScore>bestScore)
 			{
 				setBestScore(currentScore);
 				setBestMove(moveStack[i]);
 			}
 		}
 		return bestMove;
 	}
 	/**
 	 * Generic getter method to get the current best score of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Current Best Score of the Search
 	 */
 	public double getBestScore()
 	{
 		return bestScore;
 	}
 	/**
 	 * Generic getter method to get the current best state of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Current Best State of the Search
 	 */
 	public Table getBestState()
 	{
 		return bestState;
 	}
 	/**
 	 * Generic getter method to get the current player of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Current player of the Search
 	 */
 	public Player getCurrentPlayer()
 	{
 		return currentPlayer;
 	}
 	/**
 	 * Generic getter method to access the currentScore of the search.
 	 * @return The current score of the <code>Search</code>
 	 */
 	public double getCurrentScore()
 	{
 		return currentScore;
 	}
 	/**
 	 * Generic getter method of the currentSearchState data member
 	 * @return The current search state, as an object of type <code>Table</code>.
 	 */
 	public Table getCurrentSearchState()
 	{
 		return currentSearchState;
 	}
 	/**
 	 * Generic getter method to get the current state of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Current State of the Search
 	 */
 	public Table getCurrentState()
 	{
 		return currentState;
 	}
 	/**
 	 * Generic getter method to access the maxMoves in a search.
 	 * Used by the miniMax or negaMax (or other future) search functions.
 	 * @return The maximum number of generated moves
 	 */
 	public int getMaxMoves()
 	{
 		return maxMoves;
 	}
 	/**
 	 * This function is used to get a move of the move stack generated.
 	 * It is useful in generating and obtaining all moves to be used in the
 	 * negaMax function.
 	 * @param moveNo The index of the move in the move stack.
 	 * @return The move found in the specified index of the moveStack.
 	 */
 	public Move getMove(int moveNo)
 	{
 		return moveStack[moveNo];
 	}
 	/**
 	 * Generic getter method to access the moveStack generated in a search.
 	 * Used by the miniMax or negaMax (or other future) search functions.
 	 * @return The generated stack of moves
 	 */
 	public Move[] getMoveStack()
 	{
 		return moveStack;
 	}
 	/**
 	 * Generic getter method to get the depth of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Depth of the Search
 	 */
 	public int getSearchDepth()
 	{
 		return searchDepth;
 	}
 	/**
 	 * Generic getter method to get the time limit of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @return Time Limit of the Search in milliseconds
 	 */
 	public float getTime()
 	{
 		return time;
 	}
 	/**
 	 * This method generates the legal moves for the current Search object
 	 * and pushes the moves to the moveStack.
 	 */
 	private void moveGen()
 	{
 			maxMoves=currentSearchState.getNoOfDs();
 			moveStack=new Move[maxMoves];
 			int j=-1;
 			for(int i=0;i<maxMoves;i++)
 			{
 				for(;j<8;)
 				{
 					j++;
 					if(currentSearchState.isEmpty(j))
 					{
 						Table nextState=currentSearchState.clone();
 						nextState.updateTable(j, currentPlayer.getPlayerSign());
 						moveStack[i]=new Move(currentSearchState,nextState);
 						break;
 					}
 				}
 			}
 	}
 	/**
 	 * Generic setter method to set the best move of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param bestMove Best Move of the Search
 	 */
 	public void setBestMove(Move bestMove)
 	{
 		this.bestMove = bestMove;
 	}
 	/**
 	 * Generic setter method to set the best score of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param currentScore2 Best Score of the Search
 	 */
 	public void setBestScore(double currentScore2)
 	{
 		this.bestScore = currentScore2;
 	}
 	/**
 	 * Generic setter method to set the current best state of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param bestState Current Best State of the Search
 	 */
 	public void setBestState(Table bestState)
 	{
 		this.bestState = bestState;
 	}
 	/**
 	 * Generic setter method to set the current player of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param currentPlayer Current player of the Search
 	 */
 	public void setCurrentPlayer(Player currentPlayer)
 	{
 		this.currentPlayer = currentPlayer;
 	}
 	/**
 	 * Generic setter method to set the currentScore of the search.
 	 * @param currentScore The current score of the <code>Search</code>
 	 */
 	public void setCurrentScore(float currentScore)
 	{
 		this.currentScore = currentScore;
 	}
 	/**
 	 * Generic setter method of the currentSearchState data member
 	 * @param currentSearchState current search state, as an object of type <code>Table</code>.
 	 */
 	public void setCurrentSearchState(Table currentSearchState)
 	{
 		this.currentSearchState = currentSearchState;
 	}
 	/**
 	 * Generic setter method to set the current state of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param currentState Current State of the Search
 	 */
 	public void setCurrentState(Table currentState)
 	{
 		this.currentState = currentState;
 	}
 	/**
 	 * Generic setter method to set the maximum number of moves in a
 	 * search
 	 * @param maxMoves Maximum number of legal moves
 	 */
 	public void setMaxMoves(int maxMoves)
 	{
 		this.maxMoves = maxMoves;
 	}
 	/**
 	 * Generic setter method to set the moveStack in a search.
 	 * Not used by me. Simply declared as a good programming practice.
 	 * @param moveStack The stack of moves to be set
 	 */
 	public void setMoveStack(Move[] moveStack)
 	{
 		this.moveStack = moveStack;
 	}
 	/**
 	 * Generic setter method to set the depth of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param searchDepth Depth of the Search
 	 */
 	public void setSearchDepth(int searchDepth)
 	{
 		this.searchDepth = searchDepth;
 	}
 	/**
 	 * Generic setter method to set the time limit of the Search.
 	 * It, being a private variable, can only be accessed using public
 	 * getter and setter methods.
 	 * @param time Time Limit of the Search in milliseconds
 	 */
 	public void setTime(float time)
 	{
 		this.time = time;
 	}
 }
