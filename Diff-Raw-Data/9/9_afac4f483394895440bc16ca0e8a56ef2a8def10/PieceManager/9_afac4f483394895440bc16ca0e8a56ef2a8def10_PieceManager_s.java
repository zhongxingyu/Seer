 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.util.*;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.animation.*;
 import ca.couchware.wezzle2d.piece.*;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * The piece manager keeps track of where the mouse pointer is on the board
  * and places the piece selector accordingly when its draw method is called.
  * 
  * @author cdmckay
  *
  */
 
 public class PieceManager implements 
         Drawable, MouseListener, MouseMotionListener
 {	
     // -------------------------------------------------------------------------
     // Constants
     // -------------------------------------------------------------------------       
     
     /**
      * Is the board dropping in a tile?
      */
     private boolean tileDropInProgress = false;
     
     /**
      * Is the board animating the tile dropped?
      */
     private boolean tileDropAnimationInProgress = false;
     
     /**
      * The tile drop count.
      */
     private int tileDropCount = 0;
     
     /**
      * The index list.
      */
     private ArrayList indexList;
     
     /**
      * The tile currently being dropped.
      */
     private TileEntity tileDropped[];
     
      /**
      * Was the board recently refactored?
      */
     private boolean refactored = false;
     
 	/**
 	 * The current location of the mouse pointer.
 	 */
 	private volatile XYPosition mousePosition;
 	
     /**
      * Was the left mouse button clicked?
      */
     private volatile boolean mouseLeftReleased;
     
     /**
      * Was the right mouse button clicked?
      */
     private volatile boolean mouseRightReleased;
     
     /**
      * The current piece.
      */
     private Piece piece;
     
 	/**
 	 * The piece grid.  The graphical representation of the piece.
 	 */
 	private PieceGrid pieceGrid;       
 	
 	/**
 	 * The board manager the piece manager to attached to.
 	 */
 	private BoardManager boardMan;
 
     //--------------------------------------------------------------------------
     // Constructor
     //--------------------------------------------------------------------------
     
 	/**
 	 * The constructor.
 	 * 
 	 * @param boardMan The board manager.
 	 */
 	public PieceManager(BoardManager boardMan)
 	{       
 		// Set the reference.
 		this.boardMan = boardMan;
         
         // Default the mouse buttons to not clicked.
         mouseLeftReleased = false;
         mouseRightReleased = false;
         
         // Create new piece entity at the origin of the board.
 		pieceGrid = new PieceGrid(boardMan, 
                 boardMan.getX() + boardMan.getCellWidth(),
                 boardMan.getY() + boardMan.getCellHeight());
         
         // Load a random piece.
         loadRandomPiece();
 		
 		// Create initial mouse position.
 		mousePosition = new XYPosition(boardMan.getX(), boardMan.getY());
         
         // Adjust the position.
         XYPosition ap = adjustPosition(mousePosition);
         
         // Move the piece there.
         pieceGrid.setX(ap.getX());
         pieceGrid.setY(ap.getY());
         
         // Create the index list.
         this.indexList = new ArrayList();        
 	}	
     
     //--------------------------------------------------------------------------
     // Instance Methods
     //--------------------------------------------------------------------------    
   
     /**
      * Load a piece into the piece grid.
 	 * @param piece The piece to set.
 	 */
 	public void loadPiece(final Piece piece)
 	{
         // Remember which piece it is for rotating.
         this.piece = piece;
         
         // Load the piece into the piece grid.
 		pieceGrid.loadStructure(piece.getStructure());
 	}	
     
     /**
      * Loads a random piece into the piece grid.
      */
     public void loadRandomPiece()
     {
 		switch(Util.random.nextInt(5))		
 		{
 			case Piece.PIECE_DOT:
 				loadPiece(new PieceDot());
 				break;
 				
 			case Piece.PIECE_DASH:
 				loadPiece(new PieceDash());
 				break;
 				
 			case Piece.PIECE_LINE:
 				loadPiece(new PieceLine());
 				break;
 				
 			case Piece.PIECE_DIAGONAL:
 				loadPiece(new PieceDiagonal());
 				break;
 				
 			case Piece.PIECE_L:
 				loadPiece(new PieceL());
 				break;
 				
 			default:
 				throw new RuntimeException("Unrecognized piece number.");
 		}
         
         // Rotate it up to 3 times.
         int numberOfRotations = Util.random.nextInt(4) + 1;
         
         for (int i = 0; i <= numberOfRotations; i++)
             piece.rotate();
 	}
     
     /**
      * Adjusts the position of the piece grid so that it is within the board's
      * boundaries.
      * 
      * @param p The position to adjust.
      * @return The adjusted position.
      */
     public XYPosition adjustPosition(XYPosition p)
 	{
 		int column = convertXToColumn(p.getX());
 		int row = convertYToRow(p.getY());
 		
 		if (column >= boardMan.getColumns())
 			column = boardMan.getColumns() - 1;
 		
 		if (row >= boardMan.getRows())
 			row = boardMan.getRows() - 1;
 		
 		// Get the piece structure.
 		Boolean[][] structure = piece.getStructure();
 		
 		// Cycle through the structure.
 		for (int j = 0; j < structure[0].length; j++)
 		{
 			for (int i = 0; i < structure.length; i++)
 			{	
 				if (structure[i][j] == true)
 				{					
 					if (column - 1 + i < 0)
 						column++;					
 					else if (column - 1 + i >= boardMan.getColumns())
 						column--;
 						
 					if (row - 1 + j < 0)
 						row++;
 					else if (row - 1 + j >= boardMan.getRows())
 						row--;										
 				}										
 			} // end for				
 		} // end for
 		
 		return new XYPosition(
                 boardMan.getX() + (column * boardMan.getCellWidth()), 
                 boardMan.getY() + (row * boardMan.getCellHeight()));
 	}
     
     public Set getSelectedIndexSet(XYPosition p)
     {
          // Create a set.
         HashSet indexSet = new HashSet();
         
         // Convert to rows and columns.
         XYPosition ap = adjustPosition(p);
         int column = convertXToColumn(ap.getX());
 		int row = convertYToRow(ap.getY());
         
         // Get the piece struture.
         Boolean[][] structure = piece.getStructure();
         
         for (int j = 0; j < structure[0].length; j++)
         {
             for (int i = 0; i < structure.length; i++)
             {	
                 if (structure[i][j] == true 
                         && boardMan.getTile(column - 1 + i, row - 1 + j) != null)
                 {
                     // Remove the tile.
                    indexSet.add(new Integer((column - 1 + i) 
                            + (row - 1 + j) * boardMan.getColumns()));                   
                 }		
             } // end for				
         } // end for
         
         // Return the set.
         return indexSet;
     }
     
     public void notifyRefactored()
     {
         refactored = true;
     }
     
     private boolean isOnBoard(final XYPosition p)
     {
         if (p.getX() > boardMan.getX()
                 && p.getX() <= boardMan.getX() + boardMan.getWidth()
                 && p.getY() > boardMan.getY()
                 && p.getY() <= boardMan.getY() + boardMan.getHeight())
             return true;
         else
             return false;
     }
     
     private int convertXToColumn(final int x)
     {
         return (x - boardMan.getX()) / boardMan.getCellWidth();
     }    
     
     private int convertYToRow(final int y)
     {
        return (y - boardMan.getY()) / boardMan.getCellHeight(); 
     }
     
     private void startAnimationAt(final XYPosition p)
     {
         // Add new animations.
         Set indexSet = getSelectedIndexSet(p);
         for (Iterator it = indexSet.iterator(); it.hasNext(); )
         {
             final TileEntity t = boardMan.getTile((Integer) it.next());
 
             // Skip if this is not a tile.
             if (t == null)
                 continue;
 
             // Make sure they have a pulse animation.                   
             t.setAnimation(new PulseAnimation(t));           
         }
     }
     
     private void stopAnimationAt(final XYPosition p)
     {
         // Remove old animations.
         Set indexSet = getSelectedIndexSet(p);
         for (Iterator it = indexSet.iterator(); it.hasNext(); )
         {                    
             final TileEntity t = boardMan.getTile((Integer) it.next());
 
             // Skip if this is not a tile.
             if (t == null)
                 continue;
 
             Animation a = t.getAnimation();
 
             if (a != null)
                 a.cleanUp();
 
             t.setAnimation(null);
         }
     }
     
     //--------------------------------------------------------------------------
     // Logic
     //--------------------------------------------------------------------------
     
     public void updateLogic(final Game game)
     {        
         // If the board is refactoring, do not logicify.
         if (game.isBusy() == true)
              return;                 
                     
         // Drop in any tiles that need to be dropped, one at a time. 
         //
         // The if statement encompasses the entire function in order to ensure
         // that the board is locked while tiles are dropping.
         if (tileDropInProgress == true)
         {          
             // Is the tile dropped currently being animated?
             // If not, that means we need to drop a new one.            
             if (tileDropAnimationInProgress == false)
             {
                 // The number of pieces to drop in.
                 int numberToDropIn = game.worldMan.getParallelDropInAmount();
                 
                 // Adjust for the pieces left to drop in.
                 if (numberToDropIn > tileDropCount)
                     numberToDropIn = tileDropCount;
               
                 // Count the openColumns and build a list of all open indeces.
                 indexList.clear();
                 int openColumns = 0;
                 for (int i = 0; i < boardMan.getColumns(); i++)
                 {
                     if (boardMan.getTile(i) == null)
                     {
                         openColumns++;
                         indexList.add(new Integer(i));
                     }
                 }                                        
                 
                 // Automatically adjust the number of pieces to fall in based
                 // on the number of open columns.
                 if (numberToDropIn > openColumns)
                     numberToDropIn = openColumns;
                               
                 // The indices, corresponds 1:1 with the tileDropped.
                 int[] index = new int[numberToDropIn];
                 
                 assert(index.length <= indexList.size());
                 
                 // Generate the indices. pick a random index from the available
                 // indices in the indexList.
                 for (int i = 0; i < index.length; i++)
                 {
                     int randIndex =  Util.random.nextInt(indexList.size());
                     index[i] = ((Integer) indexList.get(randIndex)).intValue();
                     indexList.remove(randIndex);
                 }                
 
                 // Determine the type of tile to drop. This is done by 
                 // consulting the available tiles and their probabilities
                 // from the item list and checking to see if a special tile
                 // needs to be dropped.                      
                 tileDropped = new TileEntity[numberToDropIn];
                 
                 // If there are less items left (to ensure only 1 drop per drop 
                 // in) and we have less than the max number of items...
                 //  drop an item in. Otherwise drop a normal.
                 if (openColumns == 0 && tileDropCount > 0)                
                 {                    
                     // The tile drop is no longer in progress.
                     tileDropInProgress = false;
                     
                     // Start the game over routine.
                     game.startGameOver();
                 }
                 else if (tileDropCount == 1 && game.boardMan.getNumberOfItems() 
                         < game.worldMan.getNumMaxItems())
                 {
                     // The tile is an item.
                     tileDropped[0] = boardMan.createTile(index[0], 
                             game.worldMan.pickRandomItem(), 
                             TileEntity.randomColor()); 
                     
                     // Null out the rest.
                     for (int i = 1; i < tileDropped.length; i++)                    
                         tileDropped[i] = null;                    
                 }
                 else if (tileDropCount <= tileDropped.length 
                        && game.boardMan.getNumberOfItems() 
                        < game.worldMan.getNumMaxItems())
                 {
                     // Drop in the first x amount.
                     for (int i = 0; i < tileDropCount-1; i++)
                     {
                         tileDropped[i] =
                                 boardMan.createTile(index[i],
                                 TileEntity.class,
                                 TileEntity.randomColor()); 
 
                     }
                     
                     // Drop in the item tile.
                     tileDropped[tileDropped.length - 1] =
                             boardMan.createTile(index[tileDropped.length - 1],
                             game.worldMan.pickRandomItem(),
                             TileEntity.randomColor()); 
                     
                     // Any unused slots should be nulled.
                     for(int i = tileDropCount+1; i < tileDropped.length; i++)
                     {
                         tileDropped[i] = null;
                     }
                 }
                 else
                 {
                     // They are all normals.
                     for (int i = 0; i < tileDropped.length; i++)
                     {
                         tileDropped[i] = boardMan.createTile(index[i],
                                 TileEntity.class,
                                 TileEntity.randomColor()); 
                     }                 
                 }                
                           
                 // See if the tile drop is still in progress.  If it's not
                 // (which would be in the case of a game over), don't play
                 // the sound or set the animation flag.
                 if (tileDropInProgress == true)
                 {        
                     // Start the animation.
                     game.soundMan.play(SoundManager.BLEEP);
                     
                     for (int i = 0; i < tileDropped.length; i++)
                     {
                         if (tileDropped[i] != null)
                         {                            
                             tileDropped[i].setAnimation(
                                     new ZoomInAnimation(tileDropped[i]));
                         }
                     }
                  
                     // Set the animation flag.
                     tileDropAnimationInProgress = true;
                 }                                                     
             } 
             // If we got here, the animating is in progress, so we need to check
             // if it's done.  If it is, de-reference it and refactor.
             else if (isAnimationDone(tileDropped) == true)
             {
                 // Clear the flag.
                 tileDropAnimationInProgress = false;
                                
                 // Run refactor.
                 game.startRefactor(300);                
                 
                 // Decrement the number of tiles by the number of tiles that are
                 // not null in the array.                
                 int numberToDecrement = 0;
                 for (int i = 0; i < tileDropped.length; i++)
                 {
                     if (tileDropped[i] != null)
                         numberToDecrement++;
                 }
                 
                 tileDropCount -= numberToDecrement;
                 
                 // De-reference the tile dropped.
                 tileDropped = null;
                 
                 // Check to see if we have more tiles to drop. 
                 // If not, stop tile dropping.
                 if (tileDropCount == 0)                
                     tileDropInProgress = false;
                 else if (tileDropCount < 0)
                 {
                     throw new IllegalStateException("Tile drop count is: "
                             + tileDropCount);
                 }
                 else
                     // Continue with loop.
                     ;
             }
         }
         // In this case, the tile drop is not activated, so proceed normally
         // and handle mouse clicks and such.
         else
         {      
             // Grab the current mouse position.
             final XYPosition p = getMousePosition();             
             
             if (isMouseLeftReleased() == true)
             {            
                initiateCommit(game);
             }
             else if (isMouseRightReleased() == true)
             {
                 // Rotate the piece.            
                 stopAnimationAt(pieceGrid.getXYPosition());
                 piece.rotate();
                 startAnimationAt(pieceGrid.getXYPosition());
 
                 // Reset flag.
                 clearMouseButtons();
             }
             // Animate selected pieces.
             else if (isOnBoard(p) == true)
             {
                 // Filter the current position.
                 XYPosition ap = adjustPosition(p);
 
                 // If the position changed, or the board was refactored.
                 if (ap.getX() != pieceGrid.getX()
                         || ap.getY() != pieceGrid.getY()
                         || refactored == true)                    
                 {
                     // Clear refactored flag.
                     refactored = false;
 
                     // Stop the old animations.
                     stopAnimationAt(pieceGrid.getXYPosition());
 
                     // Update piece grid position.
                     pieceGrid.setX(ap.getX());
                     pieceGrid.setY(ap.getY()); 
 
                     // Start new animation.
                     startAnimationAt(ap);
                 } // end if           
             } // end if
         } // end if
     }
     
     public void initiateCommit(final Game game)
     {
         // If a tile drop is already in progress, then return.
         if (tileDropInProgress == true)
             return;
         
         // Get the indices of the committed pieces.
         final Set indexSet = 
                 getSelectedIndexSet(this.pieceGrid.getXYPosition());
         
         // Remove and score the piece.
         int deltaScore = game.scoreMan.calculatePieceScore(indexSet);                
                 
         // Add score SCT.
         game.animationMan.add(new FloatLabelAnimation(
                 game.boardMan.determineCenterPoint(indexSet), game.layerMan,
                 String.valueOf(deltaScore),
                 Game.SCORE_PIECE_COLOR,
                 game.scoreMan.determineFontSize(deltaScore)));
         
         // Remove the tiles.
         game.boardMan.removeTiles(indexSet);
 
         // Set the count to the piece size.
         this.tileDropCount = 
                 game.worldMan.calculateDropNumber(game, this.piece.getSize());
 
         // Increment the moves.
         game.moveMan.incrementMoveCount();
 
         // Play the sound.
         game.soundMan.play(SoundManager.CLICK);
 
         // Start a tile drop.
         this.tileDropInProgress = true;
 
         // Make visible.
         this.setVisible(false);
 
         // Run a refactor.
         game.startRefactor(200);
 
         // Reset flag.
         clearMouseButtons();
 
         // Pause timer.
         game.timerMan.setPaused(true);
     }
     
     /**
      * Check if an array of animations is done.
      * @param tiles
      * @return
      */
     private boolean isAnimationDone(TileEntity [] tiles)
     {   
         for (int i = 0; i < tiles.length; i++)
         {
             if (tiles[i] == null)
                 continue;
             else if (tiles[i].getAnimation().isDone() == false)
                 return false;  
         }
         
         return true;
     }
         
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------
     
 	/**
 	 * Gets the mousePosition.
 	 * @return The mousePosition.
 	 */
 	public XYPosition getMousePosition()
 	{
 		return mousePosition;
 	}
 
 	/**
 	 * Sets the mousePosition.
 	 * @param mousePosition The mousePosition to set.
 	 */
 	public void setMousePosition(int x, int y)
 	{
 		this.mousePosition = new XYPosition(x, y);
 	}
 
     public boolean isMouseLeftReleased()
     {
         return mouseLeftReleased;
     }
 
     public void setMouseLeftReleased(boolean mouseLeftReleased)
     {
         this.mouseLeftReleased = mouseLeftReleased;
         
         if (mouseLeftReleased == true)
             Util.handleMessage("Left mouse set.", Thread.currentThread());
         else
             Util.handleMessage("Left mouse cleared.", Thread.currentThread());
     }
 
     public boolean isMouseRightReleased()
     {
         return mouseRightReleased;
     }
 
     public void setMouseRightReleased(boolean mouseRightReleased)
     {
         this.mouseRightReleased = mouseRightReleased;
     }
     
     public void clearMouseButtons()
     {
         setMouseLeftReleased(false);
         setMouseRightReleased(false);
     }
     
     public boolean isTileDropInProgress()
     {
         return tileDropInProgress;
     }
     
     public void setVisible(boolean visible)
     {
          pieceGrid.setVisible(visible);
     }
 
     public boolean isVisible()
     {
         return pieceGrid.isVisible();
     }
     
     public void setDirty(boolean dirty)
     {
         pieceGrid.setDirty(dirty);
     }
 
     public boolean isDirty()
     {
         return pieceGrid.isDirty();
     }
     
     //--------------------------------------------------------------------------
     // Draw
     //--------------------------------------------------------------------------
     
 	/**
 	 * Draws the piece to the screen at the current cursor
 	 * location.
 	 */
 	public void draw()
 	{
         // Don't draw if invisible.
         if (isVisible() == false)
             return;                 
 		
 		// Draw the piece.
 		pieceGrid.draw();
 	}
     
     //--------------------------------------------------------------------------
     // Events
     //--------------------------------------------------------------------------
     
     public void mouseClicked(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseEntered(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseExited(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mousePressed(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void mouseReleased(MouseEvent e)
 	{
         // Debug.
         //Util.handleMessage("Button clicked.", Thread.currentThread());                
         
         // Retrieve the mouse position.
         final XYPosition p = getMousePosition();
         
         // Ignore click if we're outside the board.
         if (isOnBoard(p) == false)
             return;            
             
 		// Check which button.
         switch (e.getButton())
         {
             // Left mouse clicked.
             case MouseEvent.BUTTON1:
                 this.setMouseLeftReleased(true);
                 break;
               
             // Right mouse clicked.
             case MouseEvent.BUTTON3:
                 this.setMouseRightReleased(true);
                 break;
                 
             default:
                 Util.handleMessage("No recognized button pressed.", 
                         Thread.currentThread());
         }
 	}
 
 	public void mouseDragged(MouseEvent e)
 	{
 		// Set the mouse position.
 		setMousePosition(e.getX(), e.getY());
 	}
 
 	/**
 	 * Called automatically when the mouse is moved.
 	 */
 	public void mouseMoved(MouseEvent e)
 	{	    
 		// Set the mouse position.
 		setMousePosition(e.getX(), e.getY());
     }
 
     public Rectangle getDrawRect()
     {
         return pieceGrid.getDrawRect();
     }
     
     public void resetDrawRect()
     {
         pieceGrid.resetDrawRect();
     }
     
 }
