 package edu.rpi.phil.legup.puzzles.treetent;
 
 import javax.swing.ImageIcon;
 
 import edu.rpi.phil.legup.BoardState;
 import edu.rpi.phil.legup.Contradiction;
 
 public class ContradictionAdjacentTents extends Contradiction
 {	 
 	
 	public ContradictionAdjacentTents()
 	 {
 		setName("Adjacent Tents");
		description = "Tents cannot be adjacent or diagonal.";
 		image = new ImageIcon("images/treetent/contra_adjacentTents.png");
 	 }
 	 
 	 /**
      * Checks if the contradiction was applied correctly to this board state
      *
      * @param state The board state
      * @return null if the contradiction was applied correctly, the error String otherwise
      */
     protected String checkContradictionRaw(BoardState state)
     {
     	String error = "No two tents are adjacent to each other.";
     	int height = state.getHeight();
     	int width = state.getWidth();
 
     	// Check all tents to see if they are adjacent to a tree
     	for (int y=0;y<height;y++)
     	{
     	    for (int x=0;x<width;x++)
     	    {
     	    	if (state.getCellContents(x,y) == TreeTent.CELL_TENT)
     	    	{
     	    		for (int cx = -1; cx < 2; ++cx)
     	    		{
     	    			for (int cy = -1; cy < 2; ++cy)
     	    			{
     	    				int curX = x + cx;
     	    				int curY = y + cy;
     	    				
     	    				if (curX >= width)
     	    					continue;
     	    				else if (curY >= height)
     	    					continue;
     	    				else if (curX < 0)
     	    					continue;
     	    				else if (curY < 0)
     	    					continue;	    				
     	    				else if (cx == 0 && cy == 0)
     	    					continue;
     	    				
     	    				if (state.getCellContents(curX,curY) == TreeTent.CELL_TENT)
     	    				{ // correct application
     	    					error = null;
     	    				}
     	    			}
     	    		}
     	    	}
     	    }
     	}
 
 		return error;
     }
 }
