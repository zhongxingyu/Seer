 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.util.Util;
 import ca.couchware.wezzle2d.tile.TileEntity;
 import ca.couchware.wezzle2d.tile.Mult2xTileEntity;
 import ca.couchware.wezzle2d.tile.BombTileEntity;
 import java.lang.reflect.Constructor;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Manages the game board.  A replacement for the GameBoard class from
  * the SVG-based Wezzle.
  *  
  * @author cdmckay
  * 
  */
 
 public class BoardManager
 {	
     /**
      * Whether or not this is visible.
      */
     private boolean visible;
         
     /**
      * The layer manager.
      */
     final private LayerManager layerMan;
     
 	/**
 	 * The x-coordiante of the top left corner of the board.
 	 */
 	final private int x;
 	
 	/**
 	 * The y-coordinate of the top left corner of the board.
 	 */
 	final private int y;
 	
 	/**
 	 * The number of columns in the game board.
 	 */
 	final private int columns;
 	
 	/**
 	 * The number of rows in the game board.
 	 */
 	final private int rows;
 	
 	/**
 	 * The total number of cells.
 	 */
 	final private int cells;
 	
 	/**
 	 * The minimum number of tiles in a match.
 	 */
 	final private int minimumMatch;
 	
 	/**
 	 * The width of the board.
 	 */
 	final private int width;
 	
 	/**
 	 * The height of the board.
 	 */
 	final private int height;
 	
 	/**
 	 * The width of a grid cell.
 	 */
 	final private int cellWidth;
 	
 	/**
 	 * The height of a grid cell.
 	 */
 	final private int cellHeight;
 	
 	/**
 	 * The array representing the game board.
 	 */
 	private TileEntity[] board;
 	
     //--------------------------------------------------------------------------
     // Constructor
     //--------------------------------------------------------------------------
     
 	/**
 	 * The constructor.
 	 */
 	public BoardManager(final LayerManager layerMan,
             final int x, final int y, 
             final int columns, final int rows)
 	{
         // Board is initially visible.
         this.visible = true;
         
         // Keep reference to layer manager.
         this.layerMan = layerMan;
         
 		// Set the cell width and height. Hard-coded to 32x32 for now.
 		this.cellWidth = 32;
 		this.cellHeight = 32;
 		
 		// Set the x and y coordinates.
 		this.x = x;
 		this.y = y;
 		
 		// Set columns and rows.
 		this.columns = columns;
 		this.rows = rows;
 		this.cells = columns * rows;
 		
 		// Set the minimum match length.
 		this.minimumMatch = 4;
 		
 		// Set the board width and height.
 		this.width = columns * cellWidth;
 		this.height = rows * cellHeight;
 		
 		// Initialize board.
 		board = new TileEntity[columns * rows];
 	}
     
     //--------------------------------------------------------------------------
     // Instance Methods
     //--------------------------------------------------------------------------
 	
 	/**
 	 * Generates a random game board with the given parameters.
 	 * @param tileMax
 	 */
 	public void generateBoard(int normal, int bomb, int mult2x)
 	{
 		int i;
         for (i = 0; i < normal; i++)
 			this.createTile(i, TileEntity.class, 
                     TileEntity.randomColor());
         
         int j;
         for (j = i; j < normal + bomb; j++)
             this.createTile(j, BombTileEntity.class,
                     TileEntity.randomColor());
         
         int k;
         for (k = j; k < normal + bomb + mult2x; k++)
             this.createTile(k, Mult2xTileEntity.class, 
                     TileEntity.randomColor());
 		
 		shuffleBoard();
 		refactorBoard();
 		
 		HashSet set = new HashSet();		
 
 		while (true)
 		{
 			findXMatch(set);
 			findYMatch(set);
 			
 			if (set.size() > 0)
 			{
 				for (Iterator it = set.iterator(); it.hasNext(); )
 				{
 					Integer n = (Integer) it.next();
 					this.createTile(n.intValue(), 
                             getTile(n.intValue()).getClass(),
                             TileEntity.randomColor());
 				}
 				
 				set.clear();
 			}
 			else
 				break;
 		} // end while	
 	}
 	
 	/**
 	 * Shuffles the board randomly.
 	 */
 	private void shuffleBoard()
 	{
 		for (int i = 0; i < cells; i++)
 			swapTile(i, Util.random.nextInt(cells));
 	}
 	
 	/**
 	 * An instant refactor used for generating boards.
 	 */
 	private void refactorBoard()
 	{
 		startShiftDown(200);
 		for (int i = 0; i < cells; i++)
 			if (board[i] != null)
 				board[i].setYMovement(rows * cellHeight);
 		moveAll(1000);
 		
 		synchronize();
 		
 		startShiftLeft(200);
 		for (int i = 0; i < cells; i++)
 			if (board[i] != null)
 				board[i].setXMovement(-columns * cellWidth);
 		moveAll(1000);
 		
 		synchronize();
 	}
 	
 	/**
 	 * Searches for all matches in the X-direction and returns a linked list
 	 * with the indices of the matches.
 	 * @param set The linked list that will be filled with indices.
 	 */
 	public void findXMatch(Set set)
 	{
 		// Cycle through the board looking for a match in the X-direction.
 		for (int i = 0; i < cells; i++)
 		{
 			// Check to see if there's even enough room for an X-match.
 			if (columns - (i % columns) < minimumMatch)
 				continue;
 			
 			// Make sure there's a tile here.
 			if (board[i] == null)
 				continue;
 			
 			// Get the color of this tile.
 			int color = board[i].getColor();
 			
 			// See how long we have a match for.
 			int j;
 			for (j = 1; j < (columns - (i % columns)); j++)
 			{
 				if (board[i + j] == null || board[i + j].getColor() != color)
 					break;
 			}
 			
 			// Check if we have a match.
 			if (j >= minimumMatch)
 			{
 				Util.handleMessage("XMatch of length " + j + " found.", Thread.currentThread());
 				
 				// Copy all matched locations to the linked list.
 				for (int k = i; k < i + j; k++)				
 					set.add(new Integer(k));				
 				
 				i += j - 1;
 			}
 		}
 	}
 	
 	/**
 	 * Searches for all matches in the Y-direction and returns a set
 	 * with the indices of the matches.
 	 * @param set The linked list that will be filled with indices.
 	 */
 	public void findYMatch(Set set)
 	{
 		// Cycle through the board looking for a match in the Y-direction.
 		for (int i = 0; i < cells; i++)
 		{
 			// Transpose i.
 			int ti = Util.pseudoTranspose(i, columns, rows);
 			
 			// Check to see if there's even enough room for an Y-match.
 			if (rows - (ti / columns) < minimumMatch)
 				continue;
 			
 			// Make sure there's a tile here.
 			if (board[ti] == null)
 				continue;
 			
 			// Get the color of this tile.
 			int color = board[ti].getColor();
 			
 			// See how long we have a match for.
 			int j;
 			for (j = 1; j < (rows - (ti / columns)); j++)
 			{
 				// Transpose i + j.
 				int tij = Util.pseudoTranspose(i + j, columns, rows);
 				
 				if (board[tij] == null 
 						|| board[tij].getColor() != color)
 					break;
 			}
 			
 			// Check if we have a match.
 			if (j >= minimumMatch)
 			{
 				Util.handleMessage("YMatch of length " + j + " found.", Thread.currentThread());
 				
 				// Copy all matched locations to the linked list.
 				for (int k = i; k < i + j; k++)				
 					set.add(new Integer(Util.pseudoTranspose(k, columns, rows)));				
 				
 				i += j - 1;
 			}
 		}
 	}
 	
 	/**
 	 * Synchronizes the current board array with where the tiles are current
 	 * are on the board.  Usually called after a refactor so that the board
 	 * array will accurately reflect the board.
 	 */
 	public void synchronize()
 	{				
 		TileEntity[] newBoard = new TileEntity[columns * rows];
 		
 		for (int i = 0; i < cells; i++)
 		{			
 			if (board[i] != null)
 			{
 				TileEntity t = board[i];
 //				Util.handleMessage("" + (t.getX() - x), null);
 				int column = (t.getX() - x) / cellWidth;
 				int row = (t.getY() - y) / cellHeight;
 												
 //				if (column + (row * columns) != i)
 //				{
 					newBoard[column + (row * columns)] = board[i];
 //					board[i] = null;
 //				}
 			}
 		}
 		
 		board = newBoard;
 	}
 	
 	public void startShiftDown(final int speed)
 	{
 		// Start them moving down.
 		for (int i = 0; i < cells; i++)
 		{
 			if (board[i] != null)
 			{
 				board[i].setYMovement(speed);
 				board[i].calculateBottomBound(countTilesBelowCell(i));
 			}
 		}
 	}
 	
 	public void startShiftLeft(final int speed)
 	{
 		// Start them moving left.
 		for (int i = 0; i < cells; i++)
 		{
 			if (board[i] != null)
 			{
 				board[i].setXMovement(-speed);
 				board[i].calculateLeftBound(countTilesLeftOfCell(i));
 			}
 		}
 	}
 	
 	/**
 	 * Moves all currently moving tiles.
 	 * @returns True if there is still more moving to happen.
 	 */
 	public boolean moveAll(long delta)
 	{
 		// Set to true if there are more movement to happen.
 		boolean moreMovement = false;
 		
 		for (int i = 0; i < cells; i++)		
 			if (board[i] != null)
 			{
 				board[i].move(delta);
 				if (board[i].getXMovement() != 0 
 						|| board[i].getYMovement() != 0)
 					moreMovement = true;
 			}
 		
 		return moreMovement;
 	}
     
     /**
      * Animates all tiles with an animation.
      */
     public void animateAll(long delta)
     {
         for (int i = 0; i < cells; i++)
             if (board[i] != null)
             {
                 board[i].animate(delta);                
             }
     }
 	
 	/**
 	 * Counts all the tiles that are under the tile at the specified
 	 * index.
 	 * 
 	 * For example, if we had a 3x3 board like this:
 	 * 
 	 * 012 .X.
 	 * 345 XX.
 	 * 678 .XX
 	 * 
 	 * where "X" is a tile and "." is an empty space, then calling
 	 * this method on index 1 would return 2.
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public int countTilesBelowCell(int index)
 	{
 		// Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// The current column and row.
 		int column = index % columns;
 		int row = index / columns;
 		
 		// If we're at the bottom row, return 0.
 		if (row == rows - 1)
 			return 0;
 		
 		// The tile count.
 		int count = 0;
 		
 		// Cycle through the column rows, counting tiles.
 		for (int j = row + 1; j < rows; j++)
 			if (getTile(column, j) != null)
 				count++;
 		
 		// Return the count.
 		return count;
 	}
 	
 	/**
 	 * See <pre>countTilesBelowCell</pre>.
 	 * @param index
 	 * @return
 	 */
 	public int countTilesLeftOfCell(int index)
 	{
 		// Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// The current column and row.
 		int column = index % columns;
 		int row = index / columns;
 		
 		// If we're at the bottom row, return 0.
 		if (column == 0)
 			return 0;
 		
 		// The tile count.
 		int count = 0;
 		
 		// Cycle through the column rows, counting tiles.
 		for (int i = column - 1; i >= 0; i--)
 			if (getTile(i, row) != null)
 				count++;
 		
 		// Return the count.
 		return count;
 	}
 	
 	public void createTile(final int index, final Class c, final int color)
 	{
          // Sanity check.
         assert (index >= 0 && index < cells);
         assert (c != null);
         assert (color >= 0 && color < TileEntity.NUMBER_OF_COLORS);
         
         try
         {           
             // Get the constructor.  All tiles must have the same constructor.
             Constructor con = c.getConstructor(new Class[] { BoardManager.class, 
                 Integer.TYPE, Integer.TYPE, Integer.TYPE });
 
             TileEntity t = (TileEntity) con.newInstance(this, color, 
 				x + (index % columns) * cellWidth,
 				y + (index / columns) * cellHeight);
 
            // If we're overwriting a tile, remove it first.
            if (getTile(index) != null)
                removeTile(index);
            
             setTile(index, t);
             
             // Add the tile to the bottom layer too.
             layerMan.add(t, 0);
         }
         catch (Exception ex)
         {
             Util.handleException(ex);
         }
 	}
     
     public void removeTile(final int index)
     {
         // Sanity check.
 		assert(index >= 0 && index < cells);
 
         // Remove from layer manager.
         layerMan.remove(getTile(index), 0);
         
         // Remove the tile.
         setTile(index, null);        
     }
     
     public void removeTile(final int column, final int row)
     {
         // Sanity check.
 		assert(column >= 0 && column < columns);
         assert(row >= 0 && row < rows);
         
         // Passthrough.
         removeTile(column + (row * columns));
     }
     
     public void removeTiles(final Set set)
     {
         for (Iterator it = set.iterator(); it.hasNext(); )        
             removeTile((Integer) it.next());        
     }
 	
 	public TileEntity getTile(int index)
 	{
 		// Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// Set the tile.
 		return board[index];
 	}
 	
 	public TileEntity getTile(int column, int row)
 	{
 		// Make sure we're within parameters.
 		assert(column >= 0 && row >= 0 && column < columns && row < rows);
 		
 		return getTile(column + (row * columns));
 	}
 	
 	public void setTile(int index, TileEntity tile)
 	{
 		// Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// Set the tile.
 		board[index] = tile;
 	}
 	
 	public void setTile(int column, int row, TileEntity tile)
 	{
 		// Make sure we're within parameters.
 		assert(column < columns && row < rows);
 		
 		// Forward.
 		setTile(column + (row * columns), tile);
 	}
 
 	public void swapTile(int index1, int index2)
 	{
 		// Validate parameters.
 		assert(index1 >= 0 && index1 < cells);
 		assert(index2 >= 0 && index2 < cells);
 		
 		TileEntity t = board[index1];
 		board[index1] = board[index2];
 		board[index2] = t;
 		
 		if (board[index1] != null)
 		{
 			board[index1].setX(x + (index1 % columns) * cellWidth);
 			board[index1].setY(y + (index1 / columns) * cellHeight);
 		}
 		
 		if (board[index2] != null)
 		{
 			board[index2].setX(x + (index2 % columns) * cellWidth);
 			board[index2].setY(y + (index2 / columns) * cellHeight);
 		}
 	}
     
     /**
 	 * Determines where tiles are affected by the bomb explosion.
      * 
 	 * @param bombIndex     
      * @return The set of indices (including the bomb) affected by the bomb.
 	 */
 	private Set<Integer> processBomb(final int bombIndex)
 	{		
 		// List of additional bomb tiles.
 		Set<Integer> affectedTileSet = new HashSet<Integer>();		
 		
 		// Return if bomb is null.
 		if (getTile(bombIndex) == null)
 			return null;				
 		
 		// Determine affected tiles.
 		for (int j = -1; j < 2; j++)
 		{
 			for (int i = -1; i < 2; i++)
 			{
 				if ((bombIndex % columns) + i >= 0
 						&& (bombIndex % columns) + i < this.getColumns()
 						&& (bombIndex / columns) + j >= 0
 						&& (bombIndex / columns) + j < this.getRows())
 				{
 					if (getTile(bombIndex % columns + i, bombIndex / columns + j) != null)
 						affectedTileSet.add(new Integer(bombIndex + i + (j * columns)));														
 				}
 			} // end for i
 		} // end for j
 				
 		// Pass back affected tiles.
 		return affectedTileSet;
 	}
 
 	/**
 	 * Feeds all the bombs in the bomb processor.
      * 
 	 * @param bombTileSet
      * @return A list of a bombs triggered by the bombs blast.
 	 */
 	public Set<Integer> processBombs(Set bombSet)
 	{				
 		// A list of tiles affected by the blast.
 		Set affectedSet = new HashSet();				
 		
 		// Gather affected tiles.
 		for (Iterator<Integer> it = bombSet.iterator(); it.hasNext(); )		
 			affectedSet.addAll(this.processBomb(it.next()));			
 		
 		// Return the set of tiles affected by these bombs.
 		return affectedSet;
 	}
     
     public Set scanBombs(Set tileSet)
     {
         // The set of indices that have bombs.
         Set bombSet = new HashSet();
         
         for (Iterator it = tileSet.iterator(); it.hasNext(); )
         {
             Integer index = (Integer) it.next();
             if (getTile(index).getClass() == BombTileEntity.class)            
                 bombSet.add(index);                            
         } // end for
         
         // Return the set.
         return bombSet;
     }
 
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------
     
 	/**
 	 * @return The cellWidth.
 	 */
 	public int getCellWidth()
 	{
 		return cellWidth;
 	}
 
 	/**
 	 * @return The cellHeight.
 	 */
 	public int getCellHeight()
 	{
 		return cellHeight;
 	}			
 	
 	/**
 	 * Gets the width.
 	 * @return The width.
 	 */
 	public int getWidth()
 	{
 		return width;
 	}
 
 	/**
 	 * Gets the height.
 	 * @return The height.
 	 */
 	public int getHeight()
 	{
 		return height;
 	}
 
 	/**
 	 * Gets the x.
 	 * @return The x.
 	 */
 	public int getX()
 	{
 		return x;
 	}
 
 	/**
 	 * Gets the y.
 	 * @return The y.
 	 */
 	public int getY()
 	{
 		return y;
 	}
 
     public int getColumns()
     {
         return columns;
     }
 
     public int getRows()
     {
         return rows;
     }    
     
 //	/**
 //	 * Draw the board to the screen.
 //	 */
 //	public void draw()
 //	{
 //        // Don't draw if not visible.
 //        if (isVisible() == false)
 //            return;
 //        
 //		for (int i = 0; i < board.length; i++)
 //			if (board[i] != null)
 //				board[i].draw();
 //	}
 	
 	/**
 	 * Prints board to console (for debugging purposes).
 	 */
 	public void print()
 	{
 		for (int i = 0; i < board.length; i++)
 		{
 			if (board[i] == null)
 				System.out.print(".");
 			else
 				System.out.print("X");
 			
 			if (i % columns == columns - 1)
 				System.out.println();
 		}
 		
 		System.out.println();
 	}
 
     public void setVisible(boolean visible)
     {
         this.visible = visible;
     }
 
     public boolean isVisible()
     {
         return visible;
     }
 }
