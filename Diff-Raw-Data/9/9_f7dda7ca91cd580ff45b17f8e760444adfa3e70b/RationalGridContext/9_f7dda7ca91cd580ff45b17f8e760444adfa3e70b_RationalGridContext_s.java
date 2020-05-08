 package us.meghanclark.eratstothenes;
 
 import java.awt.*;
 
 /**
  * A context object to bundle information needed by the frame for drawing.
  *
 * @author josh.essex
  */
 public class RationalGridContext {
 
     protected static final int DEFAULT_ROWS = 225;
     protected static final int DEFAULT_COLUMNS = 360;
     private static final int DEFAULT_CELL_WIDTH = 4;
     private static final int DEFAULT_CELL_HEIGHT = 4;
     private static final Color DEFAULT_EQUIVALENCE_COLOR = Color.BLUE;
     private static final Color DEFAULT_DUPLICATE_COLOR = Color.BLACK;
 
     private final int[][] matrix;
 
     private final int columns;
     private final int rows;
     private final int cellWidth;
     private final int cellHeight;
 
     private final Color equivalenceColor;
     private final Color duplicateColor;
 
     public RationalGridContext(int[][] matrix) {
         this(matrix, DEFAULT_COLUMNS, DEFAULT_ROWS, DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT,
                 DEFAULT_EQUIVALENCE_COLOR, DEFAULT_DUPLICATE_COLOR);
     }
 
     public RationalGridContext(int[][] matrix, int columns, int rows, int cellWidth, int cellHeight,
                                Color equivalenceColor, Color duplicateColor) {
         this.matrix = matrix;
 
         this.columns = columns;
         this.rows = rows;
         this.cellWidth = cellWidth;
         this.cellHeight = cellHeight;
 
         this.equivalenceColor = equivalenceColor;
         this.duplicateColor = duplicateColor;
     }
 
     public int[][] getMatrix() {
         return matrix;
     }
 
     public int getColumns() {
         return columns;
     }
 
     public int getRows() {
         return rows;
     }
 
     public int getCellWidth() {
         return cellWidth;
     }
 
     public int getCellHeight() {
         return cellHeight;
     }
 
     public Color getEquivalenceColor() {
         return equivalenceColor;
     }
 
     public Color getDuplicateColor() {
         return duplicateColor;
     }
 
     /**
      * @return the total width of a drawing area using the parameters from this context, equal to {@link #columns} *
      * {@link #cellWidth}
      */
     public int getAreaWidth() {
         return this.columns * this.cellWidth;
     }
 
     /**
      * @return the total height of a drawing area using the parameters from this context, equal to {@link #rows} *
      * {@link #cellHeight}
      */
     public int getAreaHeight() {
         return this.rows * this.cellHeight;
     }
 }
