 package us.meghanclark.eratstothenes;
 
 import java.awt.*;
 import java.lang.reflect.Field;
 
 import static java.lang.StrictMath.max;
 
 /**
  * Entry point for commencement of the prettiness.
  *
  * @author Meghan Clark
 * @author Josh Essex
  */
 public class RationalGrid {
 
     public static void main(String[] args) {
         RationalGridContext rationalGridContext = parseArguments(args);
 
         RationalGridFrame rationalGridFrame = new RationalGridFrame(rationalGridContext);
         rationalGridFrame.pack();
         rationalGridFrame.setVisible(true);
     }
 
     /**
      * Parses the command line arguments and attempts to build a {@link RationalGridContext} from the arguments. Will exit the
      * program with an abnormal termination status if there is an issue parsing the arguments.
      *
      * @param args the command line arguments to parse
      * @return a context object built from the arguments
      */
     private static RationalGridContext parseArguments(String[] args) {
         // TODO: Refactor this super hacky, horribly non-robust command line argument logic
 
         if (args.length == 0) {
             int[][] matrix = buildMatrix(RationalGridContext.DEFAULT_ROWS, RationalGridContext.DEFAULT_COLUMNS);
             return new RationalGridContext(matrix);
         }
 
         try {
             int rows = Integer.parseInt(args[0]);
             int columns = Integer.parseInt(args[1]);
             int cellHeight = Integer.parseInt(args[2]);
             int cellWidth = Integer.parseInt(args[3]);
 
             final Field equivField = Color.class.getField(args[4].toUpperCase());
             Color equivalenceColor = (Color) equivField.get(null);
 
             final Field dupField = Color.class.getField(args[5].toUpperCase());
             Color duplicateColor = (Color) dupField.get(null);
 
             int[][] matrix = buildMatrix(rows, columns);
             return new RationalGridContext(matrix, columns, rows, cellWidth, cellHeight, equivalenceColor, duplicateColor);
         }
         catch (Exception e) {
             System.err.println("Usage: java RationalGrid [rows,columns,cell height,cell width, " +
                     "equivalence color,duplicate color]");
             System.err.println("Example: java RationalGrid 225 360 4 4 BLUE BLACK");
 
             System.exit(1);
         }
 
         return null;
     }
 
 
     /**
      * Builds and returns the matrix.
      *
      * @param rows the amount of rows in the matrix
      * @param columns the amount of columns in the matrix
      * @return the matrix
      */
     private static int[][] buildMatrix(int rows, int columns) {
         int[][] matrix = new int[rows][columns];
 
         // Flag duplicates
         for (int z = 2; z <= max(rows, columns); z++) {
             for (int i = 1; z * i <= rows; i++) {
                 for (int j = 1; z * j <= columns; j++) {
                     matrix[z * i - 1][z * j - 1] = RationalGridFrame.DUPLICATE;
                 }
             }
         }
 
         return matrix;
     }
 }
