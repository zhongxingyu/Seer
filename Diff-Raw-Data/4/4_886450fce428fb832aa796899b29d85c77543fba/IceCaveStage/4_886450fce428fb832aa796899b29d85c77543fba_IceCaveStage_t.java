 package com.android.icecave.mapLogic;
 
 import java.util.Random;
 
 import android.graphics.Point;
 
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.general.GeneralServiceProvider;
 import com.android.icecave.mapLogic.collision.ICollisionable;
 import com.android.icecave.mapLogic.tiles.BoulderTile;
 import com.android.icecave.mapLogic.tiles.EmptyTile;
 import com.android.icecave.mapLogic.tiles.FlagTile;
 import com.android.icecave.mapLogic.tiles.IBlockingTile;
 import com.android.icecave.mapLogic.tiles.ITile;
 import com.android.icecave.mapLogic.tiles.WallTile;
 import com.android.icecave.mapLogic.tiles.validators.TileValidatorFactory;
 
 public class IceCaveStage
 {
 	private ITile[][] mTiles;
 	private int mMoves;
 	private boolean[][] mVisitedTiles;
 	
 	/**
 	 * Validate a point on the board.
 	 * @param toCheck - Point to validate.
 	 * @return true if point is valid.
 	 */
 	public boolean ValidatePoint(Point toCheck)
 	{
 		return ((toCheck.x > 0 			    || 
 				 toCheck.x < mTiles[0].length) && 
 				(toCheck.y > 0 			|| 
 				 toCheck.y < mTiles.length));
 	}
 	
 	/**
 	 * Move the player one tile in a direction.
 	 * @param playerLocation - Current player location.
 	 * @param direction - Direction to move the player in.
 	 */
 	public void movePlayerOneTile(Point playerLocation, EDirection direction) {
 		// Get the next index on the board for the player.
 		Point nextPoint = new Point(playerLocation);
 		nextPoint.offset(direction.getDirection().x,direction.getDirection().y);
 		
 		// Call the tile that the player will meet.
 		MapLogicServiceProvider.getInstance().
 								getCollisionManager().
 								handleCollision((ICollisionable) mTiles[nextPoint.x]
 																	   [nextPoint.y]);
 		
 	}
 	
 	/**
 	 * Get the number of minimal moves for this stage.
 	 * @return Minimal number of moves.
 	 */
 	public int getMoves() {
 		return mMoves;
 	}
 	
 	/**
 	 * Get the board of the stage.
 	 * @return The current stage's board.
 	 */
 	public ITile[][] getBoard() {
 		return mTiles;
 	}
 	
 	/**
 	 * Initialize the board.
 	 * @param rowLen - Number of rows in board.
 	 * @param colLen - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 */
 	private void initializeBoard(int rowLen, int colLen, int wallWidth)
 	{
 		createEmptyBoard(rowLen, colLen);
 
 		fillWithEmptyTles(rowLen, colLen, wallWidth);
 	}
 
 	/**
 	 * Fill the initialized board with empty tiles.
 	 * 
 	 * @param rowLen - Number of rows in board.
 	 * @param colLen - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 */
 	private void fillWithEmptyTles(int rowLen, int colLen, int wallWidth) {
 		for (int i = wallWidth; i < rowLen - wallWidth; i++)
 		{
 			for (int j = wallWidth; j < colLen - wallWidth; j++)
 			{
 				// Initializing board.
 				mTiles[i][j] = new EmptyTile(i,j);
 			}
 		}
 	}
 
 	/**
 	 * Creates an empty board, all tiles are walls.
 	 * @param rowLen - Number of rows in board.
 	 * @param colLen - Number of columns in board.
 	 */
 	private void createEmptyBoard(int rowLen, int colLen) {
 		// Initializing walls
 		for (int i = 0; i < rowLen; i++)
 		{
 			for (int j = 0; j < colLen; j++)
 			{
 				mTiles[i][j] = new WallTile(i,j);
 			}
 		}
 	}
 
 	/**
 	 * Generating a possible to beat map
 	 * 
 	 * @param difficulty - Difficulty for the stage.
 	 * @param rowLen - Number of rows in board.
 	 * @param colLen - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 * @param playerLoc - Starting location for the player.
 	 * @param boulderNum - Number of boulders in the board.
 	 * @param startingMove - First move of the player to do (while building the board).
 	 */
 	public void buildBoard(EDifficulty difficulty, 
 						   int 		   rowLen, 
 						   int 		   colLen, 
 						   int 		   wallWidth, 
 						   Point 	   playerLoc, 
 						   int 	       boulderNum,
 						   EDirection  startingMove)
 	{
 		// Initialize members.
 		mTiles = new ITile[colLen][rowLen];
 		mVisitedTiles = new boolean[colLen][rowLen];
 		
 		// Place tiles in the board.
 		placeTiles(rowLen, colLen, wallWidth, playerLoc, boulderNum);			
 	
 		// Validating the matrix path
 		// If the validate turns false
 		while (!Validate(startingMove, playerLoc, difficulty))
 		{
 			// Re-initializing map
 			placeTiles(rowLen, colLen, wallWidth, playerLoc, boulderNum);			
 
 		}
 	}
 
 	/**
 	 * Place all tiles on the board.
 	 * 
 	 * @param rowLen - Board row length in tiles. 
 	 * @param colLen - Board column length in tiles.
 	 * @param wallWidth - Width of the wall in tiles.
 	 * @param playerLoc - Player location.
 	 * @param boulderNum - Number of boulders to place.
 	 */
 	private void placeTiles(int rowLen, int colLen, int wallWidth,
 			Point playerLoc, int boulderNum) {
 		// Creating the board
 		initializeBoard(rowLen, colLen, wallWidth);
 
 		// Creating exit point
 		Point flagLocation = 
 				CreateExit(rowLen, colLen, playerLoc);
 		
 		mTiles[flagLocation.y][flagLocation.x] = new FlagTile(flagLocation);
 
 		// Place the boulders on the board.
 		placeBoulders(rowLen,
 					  colLen, 
 					  playerLoc,
 					  boulderNum);
 	}
 
 	/**
 	 * Place boulders on the board.
 	 * @param rowLen - Row length of the board in tiles.
 	 * @param colLen - Column length of the board in tiles.
 	 * @param playerLoc - Player location.
 	 * @param boulderNum - Number of boulders to place on board.
 	 */
 	private void placeBoulders(int rowLen,
 							  int colLen,
 							  Point playerLoc,
 							  int boulderNum) {
 		Random rand = 
 				GeneralServiceProvider.getInstance().getRandom();
 		
 		int boulderColRand, boulderRowRand, boulderCounter = 0;
 		
 		// Place boulders.
 		// TODO: Might be an infinite loop.
 		while (boulderCounter < boulderNum)
 		{
 			// Making random points
			boulderRowRand = rand.nextInt(rowLen - 2) + 1;
			boulderColRand = rand.nextInt(colLen - 2) + 1;
 
 			// Validate the position.
 			TileValidatorFactory tileValidatorFactory =
 					MapLogicServiceProvider.getInstance().getTileValidatorFactory();
 			
 			// If the location is not valid.
 			if(!tileValidatorFactory.validate(BoulderTile.class, 
 											 boulderRowRand, 
 											 boulderColRand, 
 											 playerLoc.x, 
 											 playerLoc.y, 
 											 mTiles)){
 				continue;
 			}
 			
 			mTiles[boulderRowRand][boulderColRand] = 
 					new BoulderTile(boulderRowRand, boulderColRand);
 			
 		}
 	}
 
 	/**
 	 * Find a location on the board to place the flag in.
 	 * 
 	 * @param rowSize 	- Row size in tiles of the board.
 	 * @param colSize 	- Column size in tiles of the board.
 	 * @param playerLoc - Location of the player.
 	 * 
 	 * @return Location to place the flag.
 	 */
 	private Point CreateExit(int rowSize, int colSize, Point playerLoc)
 	{
 		// Get a random number
 		Random rand = GeneralServiceProvider.getInstance().getRandom();
 		
 		int flagXposition = rand.nextInt(rowSize);
 		int flagYposition = rand.nextInt(colSize);
 		
 		while(playerLoc.equals(flagXposition, flagYposition)){
 			flagXposition = rand.nextInt(rowSize);
 			flagYposition = rand.nextInt(colSize);
 		}
 			
 		return new Point(flagXposition, flagYposition);
 	}
 
 	/**
 	 * Validate that the map is solvable in a specific number of moves.
 	 * @param defaultMoveDirection - The first direction the player moves in.
 	 * @param playerPoint - The starting location of the player.
 	 * @param difficulty - The difficulty of the stage.
 	 * @return true if valid.
 	 */
 	private boolean Validate(EDirection  defaultMoveDirection,
 							 Point 	     playerPoint,
 							 EDifficulty difficulty)
 	{
 		// TODO: Validate the number of steps is good.
 		// Create the root node
 		MapNode root = new MapNode(null, null);
 
 		// Clear the previous stuff
 		root.clear();
 
 		// Add the root to the stack
 		root.addRoot();
 
 		// Fill the nodes
 		MapNode node = 
 				fillNodes(root, 
 						  defaultMoveDirection, 
 						  new Point(playerPoint));
 
 		// Check if it's OK.
 		if (node.getValue() instanceof FlagTile && 
 			node.getLevel() >= difficulty.getMinMoves() &&
 			node.getLevel() <= difficulty.getMaxMoves())
 		{
 			return true;
 //			// Get the number of steps.
 //			m_nStepsTaken = nCurMinSteps;
 //
 //			if (nBreakableObjects == 0)
 //			{
 //				return true;
 //			}
 //			else
 //			{
 //				// Check if breakable nodes exist.
 //				int nRemoveableNum = GetBreakable();
 //
 //				// Check the number of objects.
 //				if (nRemoveableNum < nBreakableObjects)
 //				{
 //					// TODO: What to do?
 //				}
 //
 //				// Just choose the breakable object's you would like.
 //
 //				// Check if succeeded
 //				return (node.IsFlag() && node.GetLevel() >= m_nMinimumNumberOfSteps);
 //			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Change the types matrix boulders that are breakable to breakable.
 	 * 
 	 * @return number of breakable objects.
 	 */
 //	private int GetBreakable()
 //	{
 //		// The current array list of breakable nodes.
 //		ArrayList<Point> lstRowBreakable = new ArrayList<Point>();
 //		ArrayList<Point> lstColBreakable = new ArrayList<Point>();
 //		int nNumberOFBreakable = 0;
 //
 //		for (int i = 0; i < nRowMaxSize; i++)
 //		{
 //			boolean fFoundVisited = false;
 //			lstRowBreakable.clear();
 //			lstColBreakable.clear();
 //
 //			for (int j = 0; j < nColMaxSize; j++)
 //			{
 //				if (fFoundVisited)
 //				{
 //					// Check if we have found another visited place.
 //					if (TempMatrix[i][j])
 //					{
 //						// Go through all the boulders in the current row.
 //						for (SerPoint serPoint : lstRowBreakable)
 //						{
 //							if (TypesMatrix[serPoint.x][serPoint.y] != Types.Breakable)
 //							{
 //								nNumberOFBreakable++;
 //							}
 //
 //							// Set as breakable.
 //							TypesMatrix[serPoint.x][serPoint.y] = Types.Breakable;
 //						}
 //						lstRowBreakable.clear();
 //					}
 //					// Check if we have found another visited place.
 //					if (TempMatrix[j][i])
 //					{
 //						// Go through all the boulders in the current col.
 //						for (SerPoint serPoint : lstColBreakable)
 //						{
 //							if (TypesMatrix[serPoint.x][serPoint.y] != Types.Breakable)
 //							{
 //								nNumberOFBreakable++;
 //							}
 //
 //							// Set as breakable.
 //							TypesMatrix[serPoint.x][serPoint.y] = Types.Breakable;
 //						}
 //						lstColBreakable.clear();
 //					}
 //
 //					if (TypesMatrix[i][j] == Types.Boulder)
 //					{
 //						lstRowBreakable.add(new SerPoint(i, j));
 //					}
 //					if (TypesMatrix[j][i] == Types.Boulder)
 //					{
 //						lstColBreakable.add(new SerPoint(j, i));
 //					}
 //				}
 //				else if (TempMatrix[i][j])
 //				{
 //					fFoundVisited = true;
 //				}
 //			}
 //		}
 //
 //		return nNumberOFBreakable;
 //	}
 
 	/**
 	 * Fill nodes in the board map, return the end node.
 	 * @param curNode - Current received node.
 	 * @param lastDirection - Direction to move the player in.
 	 * @param playerPoint - The current location of the player.
 	 * @return
 	 */
 	private MapNode fillNodes(MapNode      curNode,
 							  EDirection   lastDirection, 
 							  Point        playerPoint)
 	{
 		// Check if root.
 		if (curNode == null)
 		{
 			return curNode;
 		}
 
 		// Checking if the target was found
 		if (StopFillingNodes(curNode))
 		{
 			// Add the flag
 //			curNode.push(new FlagTile());
 		}
 		// Checking if the current place has been checked before from that direction.
 		else if (!mVisitedTiles[playerPoint.y][playerPoint.x])
 		{
 			// Making the currently visited place false, to not
 			// visit it again
 			mVisitedTiles[playerPoint.x][playerPoint.y] = true;
 
 			for (EDirection eDirection : EDirection.values()) {
 				fillNodesInDirection(curNode,eDirection, lastDirection, playerPoint);
 			}
 		}
 
 		return curNode.Peek();
 	}
 
 	/**
 	 * @param curNode
 	 * @param mvMoves
 	 * @param playerPoint
 	 */
 	private void fillNodesInDirection(MapNode    curNode, 
 									  EDirection toMove, 
 									  EDirection lastMove,
 									  Point playerPoint) {
 		MapNode newNode;
 		Point pntNewPoint;
 		
 		// Checking upper position
 		// TODO: Make this more generic
 		if ((!StopFillingNodes(curNode)) &&
 			(mTiles[playerPoint.x + toMove.getDirection().x]
 				   [playerPoint.y + toMove.getDirection().y] instanceof IBlockingTile &&
 			 !lastMove.equals(toMove.getOpositeDirection())))
 		{
 			pntNewPoint = getMove(toMove, playerPoint);
 			newNode = 
 					curNode.push(mTiles[pntNewPoint.x][pntNewPoint.y]);
 
 			fillNodes(newNode, toMove, pntNewPoint);
 		}
 	}
 
 	/**
 	 * Check if to stop filling nodes.
 	 * @param curNode - Current node.
 	 * @return true if stop filling node.
 	 */
 	private boolean StopFillingNodes(MapNode curNode)
 	{
 		return curNode.getValue() instanceof FlagTile;
 	}
 
 	/**
 	 * Get the new location after making move.
 	 * @param toMove - Direction to move.
 	 * @param currPoint - The current point of the player.
 	 * @return Location of the player after making the move.
 	 */
 	private Point getMove(EDirection toMove, Point currPoint)
 	{
 		// Getting the current point
 		ITile tileCurr = 
 				mTiles[currPoint.x + toMove.getDirection().x]
 					  [currPoint.y + toMove.getDirection().y];
 
 		// While not blocked
 		while (!(tileCurr instanceof IBlockingTile))
 		{
 			// Stopping if reached exit.
 			// TODO: make this more generic.
 			if (tileCurr instanceof FlagTile)
 			{
 				break;
 			}
 
 			currPoint.x += toMove.getDirection().x;
 			currPoint.y += toMove.getDirection().y;
 			
 			
 			// Advance
 			tileCurr =
 				mTiles[currPoint.x + toMove.getDirection().x]
 					  [currPoint.y + toMove.getDirection().y];
 		}
 		
 		currPoint.x += toMove.getDirection().x;
 		currPoint.y += toMove.getDirection().y;
 		return (currPoint);
 	}
 }
 
