 import java.util.HashSet;
 public class Sudoku {
 
   /*
    *  To call this method in another method use
    *  "Sudoku.isValid(sudoku)"
    *  This method determines if the string representation of a suduko is
    *  valid.
    *  @param sudoku a string representation of a sudoku
    *  @param return true if sudoku is valid else return false
    */
   public static boolean isValid(String sudoku) {
    if (sudoku == null) {
      return false;
    }
     if (sudoku.length() != 81) {
       return false;
     }
     int[][] grid = new int[9][9]; //9x9 array representation of the string
 
     //create the sudoku board
     int j = 0;
     for (int i = 0; i < sudoku.length(); i++) {
       j = i / 9; // use the index to determine which row we are currently in
      if (Character.isDigit(sudoku.charAt(i)) && sudoku.charAt(i) != '0') {
         grid[i % 9][j] = Character.getNumericValue(sudoku.charAt(i));
       } else {
         return false;
       }
     }
 
     HashSet<Integer> digits; // an empty set that will soon contain the integers 1-9
 
     //validate the columns
     for (int col = 0; col < 9; col++) { // for each column
       digits = new HashSet<Integer>();
       for (int row = 0; row < 9; row++) {
         // if we fail to add a digit to the set that means it is already in the set
         // so return false
         if (!digits.add(new Integer(grid[col][row]))) { // add the digit to the set
           return false;
         }
       }
     }
 
     //validate the rows
     for (int row = 0; row < 9; row++) { // for each row
       digits = new HashSet<Integer>();
       for (int col = 0; col < 9; col++) {
         // if we fail to add a digit to the set that means it is already in the set
         // so return false
         if (!digits.add(new Integer(grid[col][row]))) { // add the digit to the set
           return false;
         }
       }
     }
 
     //validate each 3x3 square block
     for (int col = 0; col < 9; col+=3) { // These two for loops move through each 3x3 square
       for (int row = 0; row < 9; row+=3) {
         digits = new HashSet<Integer>();
         for (int l = col; l < col + 3; l++) { // These two for loops compute the sum of each 3x3 square.
           for (int m = row; m < row + 3; m++) {
             // if we fail to add a digit to the set that means it is already in the set
             // so return false
             if (!digits.add(new Integer(grid[l][m]))) { // add the digit to the set
               return false;
             }
           }
         }
       }
     }
     return true; // Passed all validations
   }
 
   /*
    *  To compile this program using the command line, type
    *  "javac Sudoku.java"
    *  To run this program using the command line, type
    *  "java Sudoku <string>"
    *  On a successful run, true is outputted to stdout if the sudoku is
    *  valid and false if it is not valid.
    */
   public static void main(String[] args) {
     if (args.length != 1) {
       System.out.println("Usage: java Sudoku <string>");
       return;
     }
     System.out.println(isValid(args[0]));
   }
 }
