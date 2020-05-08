 import java.util.LinkedList;
 import java.util.ListIterator;
 
 /* This class is where we solve the sudoku */
 public class SolveSudoku {
 	
 	// TODO: write your shit here to solve the read in sudoku sohaib
 	//		 if it doesn't work do it in the SudokuFileReader and i'll just rename the class
 	//		 daniel feel free to do some work
    private static final int SMALLBOX_SIZE = 3;
 	boolean[] digits;
    
 	//private SudokuBoard sBoard;
 	
 	public SolveSudoku(){
 		//this.sBoard = sBoard;
 	}
 	
 	public SudokuBoard recursiveBruteForceSolver(SudokuBoard sBoard) {
       int i, j;
       int[] emptyCell = findEmptyCell(sBoard);
       i = emptyCell[0];
       j = emptyCell[1];
       if (i == -1 || j == -1) {
          return sBoard;
       }
       System.out.print("Modifying cell ("+ i+","+j+")\n");
       LinkedList<Integer> possibilities= getPossibilities(i, j, sBoard);
       System.out.print("possible values:{");
       for (Integer k : possibilities)
       {
          System.out.print(k.intValue());
          System.out.print(" ");
       }
       System.out.print("}\n");
       if (possibilities.size() == 0) return sBoard;
       for (Integer k : possibilities)
       {
          System.out.print("1\n");
          sBoard.setCellNum(k.intValue(), i, j);
          System.out.print("2\n");
          sBoard.printBoard();
          SudokuBoard temp = copy(sBoard);
          System.out.print("3\n");
          temp = recursiveBruteForceSolver(temp);
          System.out.print("4\n");
          if (isLegalBoard(temp) && isComplete(temp)) {
             System.out.print("5\n");
             return temp;
          }
          System.out.print("not valid \n");
       } 
 
       return null;
    }
    
    private int[] findEmptyCell(SudokuBoard sBoard)
    {
       int[] emptyCell = {-1, -1};
       for (int i = 0; i < 9; i++)
       {
          for (int j = 0; i < 9; i++)
          {
             if (sBoard.getBoard()[j][i]==0) {
                emptyCell[0] = i;
                emptyCell[1] = j;
             }
          }
       }
       return emptyCell;
    }
 
    private LinkedList<Integer> getPossibilities(int row, int column, SudokuBoard sBoard)
    {
       LinkedList<Integer> numbers = new LinkedList<Integer>();
       for (int i = 1; i < 10; i++) 
       {
          numbers.add(i);
       }
       //ListIterator<Integer> it = numbers.listIterator();
       /*
       for (Integer i: numbers)
       {
          for (int j = 0; j < 9; j++)
          {
             if (sBoard.getBoard()[row][j] == i.intValue() ||
                 sBoard.getBoard()[j][column] == i.intValue()) {
                numbers.remove(i);
             }
          }
       }*/
       //while (it.hasNext()){
          for (int j = 0; j < 9; j++)
          {
             //Integer temp = it.next();
             //System.out.print(temp.intValue() + " " +sBoard.getBoard()[row][j]+" \n");
             //if (sBoard.getBoard()[row][j] == temp.intValue()) {
             if (numbers.contains(new Integer(sBoard.getBoard()[row][j]))) {
                numbers.remove(new Integer(sBoard.getBoard()[row][j]));
             }
             if (numbers.contains(new Integer(sBoard.getBoard()[j][column]))) {
                numbers.remove(new Integer(sBoard.getBoard()[j][column]));
             }
          }
       //}
       /*
       while (it.hasNext()){
          for (int j = 0; j < 9; j++)
          {
             Integer temp = it.next();
             System.out.print(temp.intValue() + " "+ sBoard.getBoard()[j][column] +" \n");
             if (sBoard.getBoard()[j][column] == temp.intValue()) {
                numbers.remove(it);
             }
          }
       }*/
          
       return numbers;
    }
    
    
    //public static void Validate(final int [][] sudokuBoard)
    
   public static void Valiate(SudokuBoard sBoard){
 	   
 	   
 	   for(int i = 0; i < 9; i ++){
 		   if(!isValidRow(sBoard, i, 9)){
 			   //row has repitions
 			   }
 	   }   
 	   for(int j = 0; j < 9; j++){
 		   if(!isValidColumn(sBoard, j, 9)){
 			   // columns has repitions
 		   }
 	   }   
	   
    }
    
    private static boolean isValidColumn(SudokuBoard sBoard, int column, int height){
 	   for(int i = 0; i < 9; i++){
 		   for(int j = i + 1; j < 9; j++){
 			   if(sBoard.getBoard()[i][column] == sBoard.getBoard()[j][column]){
 				   return false;
 			   }
 		   }
 	   }
 	   return true;
    }
    
    private static boolean isValidRow(SudokuBoard sBoard, int row, int width){
 	   for(int i = 0; i < 9; i++){
 		   for(int j = i + 1; j < 9; j++){
 			   if(sBoard.getBoard()[row][i] == sBoard.getBoard()[row][i]){
 				   return false;
 			   }
 		   }
 	   }
 	   return true;
    }
    
    /**
     * main function checks board is valid
     * @return
     */
 //   private boolean isLegalBoard(SudokuBoard sBoard){
 //      //if (sBoard == null) return false;
 //      for(int i = 0; i <9; i++ ){
 //    	  if(!rowValid(i, sBoard)){
 //    		  return false;
 //    	  }
 //      }
 //      
 //      for(int i = 0; i < 9; i++){
 //    	  if(!columnValid(i, sBoard)){
 //    		  return false;
 //    	  }
 //      }
 //      
 //      for(int i =0; i < 9; i++){
 //    	  for(int j = 0; j < 9; j += 3){
 //    		  if(!smallBoxValid(i, j, sBoard)){
 //    			  return false;
 //    		  }
 //    	  }
 //      }
 //      return true;
 //   }
 //   
 //   /**
 //    * checks a row is valid
 //    * @param row
 //    * @return
 //    */
 //   public boolean rowValid (int row, SudokuBoard sBoard){
 //	   resetnumbers();
 //	   for( int k =0; k <9; k++){
 //
 //		   if(!numbersManager(sBoard.getBoard()[row][k])){
 //			   return false;
 //		   }
 //	   }
 //	   return true;
 //   }
 //   
 //   /**
 //    * checks a column is valid
 //    * @param column
 //    * @return
 //    */
 //   // comments : numbersMANAGER (INT) _> (INT [][] 
 //  
 //   //find the location of col in the boad
 //   //basically you need ot test if col 
 //   //numbersMNAGER 
 //   
 //   public boolean columnValid(int column, SudokuBoard sBoard){
 //	   resetnumbers();
 //	     for( int k =0; k <9; k++){
 //		   
 //		   
 //		   
 //	   }
 //	   return true;
 //   }
 //   
 //   /**
 //    * checks the smallboxes in sudoku are valid
 //    * @param row
 //    * @param column
 //    * @return
 //    */
 //   public boolean smallBoxValid(int row, int column, SudokuBoard sBoard){
 //	   resetnumbers();
 //	   
 //	   for(int k =0; k < 3;  k++){
 //		   for(int c = 0; c <3; c++){
 //			   if(!numbersManager(sBoard.getBoard()[k + row][c + column])){
 //				   return false;
 //			   }
 //		   }
 //	   }
 //	   return true;
 //   }
 //   /**
 //    * resets numbers to false
 //    */
 //   public void resetnumbers(){
 //	   digits = new boolean[10];
 //   }
 //   
 //  /**
 //   * keeps track of numbers used in Sudoku
 //   * @param number
 //   * @return
 //   */
 //   public boolean numbersManager(int number){
 //	   if( number != 0 && digits[number]){
 //		   return false;
 //	   }else{
 //		   digits[number] = true;
 //		   return true;
 //	   }
 //   }
    
    
    
    /**
     * checks if board is legal and according to laws of Sudoku
     * @param number
     * @param row
     * @param column
     * @return
     */
 //   private boolean isLegalBoardCell(int number, int row, int column){
 //	   int boardrow = (row/SMALLBOX_SIZE) * SMALLBOX_SIZE;
 //	   int boardcolumn = (row/SMALLBOX_SIZE) * SMALLBOX_SIZE;
 //	   
 //	   for (int  i = 0; i < 9; i++){
 //		   if(sBoard.getCellNum(row, i)== number ||
 //			  sBoard.getCellNum(i, column) == number ||
 //			  sBoard.getCellNum(boardrow + (i % SMALLBOX_SIZE), boardcolumn + (i / SMALLBOX_SIZE) ) == number){
 //			   return false;
 //		   }
 //			   
 //	   }
 //	   return true;
 //   }
    
    
    private boolean isComplete(SudokuBoard sBoard)
    {
       //if (sBoard == null) return false;
       for (int i = 0; i < 9; i++)
       {
          for (int j = 0; j < 9; j++)
          {
             /*
             if (sBoard == null) {
                return false;
             }*/
             System.out.print(i + ", " + j + "\n");
             System.out.print(sBoard.getBoard()[i][j]);
             if (sBoard.getBoard()[i][j] == 0) {
                return false;
             }
          }
       }
       return true;
    }
    
    private SudokuBoard copy(SudokuBoard sBoard)
    {
       int[][] newGrid = new int[9][9];
       for (int i = 0; i < 9; i++) {
          for (int j = 0; j < 9; j++) {
           newGrid[i][j] = sBoard.getBoard()[i][j];
          }
         }
       SudokuBoard newBoard = new SudokuBoard(newGrid);
       return newBoard;
    }
    
 }
