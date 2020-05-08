 /**
  * GridAverager.java - averages pixel values arranged in a grid
  */
 package edu.bu.cs.cs480.rendering.supersampling;
 

 /**
 * Averages pixel volor values arranged in a grid.
  * 
  * @author Jeffrey Finkelstein <jeffrey.finkelstein@gmail.com>
  * @since Spring 2011
  */
 public abstract class GridAverager implements Averager {
   /**
    * The size of the grid (that is, the length of one side of the square) in
    * number of pixels.
    */
   private int gridSize;
 
   /**
    * Gets the size of the grid (that is, the length of one side of the square)
    * in number of pixels.
    * 
    * @return The number of pixels along one side of the square grid.
    */
   protected int gridSize() {
     return this.gridSize;
   }
 
   /**
    * Sets the size of the grid (that is, the length of one side of the square)
    * in number of pixels.
    * 
    * @param gridSize
    *          The number of pixels along one side of the square grid.
    */
   public void setGridSize(final int gridSize) {
     this.gridSize = gridSize;
   }
 
 }
