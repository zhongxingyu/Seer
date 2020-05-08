 package conrad.treasureMap;
 
 /**
  * class will be used for the business logic to display
  * treasure chests nearby
  * 
  * @author Conrad
  *
  */
 public class TreasureChestManager implements TreasureChestCounter {
 
 	public void displayTreasureChestNearby(Grid grid) {
 		System.out.println("RESULTS");
 		if(grid!=null){
 			TreasureChest[][] treasureChestGrid = grid.getTreasureChest();
 			int rows = grid.getRows();
 			int cols =grid.getColumns();
 
 			for(int row=0;row<rows;row++){
 				StringBuilder result = new StringBuilder();
 				// check first col
 				for(int col=0;col<cols;col++){
 					int counter = 0 ;
 					counter=checkTreasureChestsNearby(row, col, rows, cols, counter, treasureChestGrid);
 					result.append(counter);
 				}
 				System.out.println(result.toString());
 			}
 		}
 	}
 
 	private int checkPreviousColumn(int col,int row,TreasureChest[][] treasureChestGrid,int counter){
 		if(col!=0){
 			counter=checkColumn(treasureChestGrid, row, col-1, counter);
 		}
 		return counter;
 	}
 
 	private int  checkPreviousColumnRowBelow(int row,int col,TreasureChest[][]treasureChestGrid,int counter){
 		if(col!=0){
 			counter=checkColumn(treasureChestGrid, row+1, col-1, counter);
 		}
 		return counter;
 	}
 
 	private int checkRightColumn(int row,int col,int cols,TreasureChest[][]treasureChestGrid,int counter){
 
 		if(col!=(cols-1)){
 			counter=checkColumn(treasureChestGrid, row, col+1, counter);
 		}
 
 		return counter;
 	}
 
 	private int checkRightColumnRowBelow(int row,int col,int cols,TreasureChest[][]treasureChestGrid,int counter){
 
 		if(col!=(cols-1)){
 			counter=checkColumn(treasureChestGrid, row+1, col+1, counter);
 		}
 		return counter;
 	}
 
 	private int checkPreviousColumnRowAbove(int row, int col,TreasureChest[][]treasureChestGrid,int counter){
 		// check previous col if it is not 0
 		if(col!=0){
 			counter=checkColumn(treasureChestGrid, row-1, col-1, counter);
 
 		}
 		return counter;
 	}
 
 	private int checkRightColumnRowAbove(int row,int col,int cols,TreasureChest[][]treasureChestGrid,int counter){
 		if(col!=(cols-1)){
 			counter=checkColumn(treasureChestGrid, row-1, col+1, counter);
 		}
 		return counter ;
 
 	}
 
 	private int checkColumn(TreasureChest[][]treasureChestGrid,int row,int col,int counter){
		TreasureChest nChest =treasureChestGrid[row][col];
		if(nChest!=null){
			counter++;
 		}
 		return counter;
 	}
 
 	private int checkFirstRow(int row,int col,int cols,int counter,TreasureChest[][] treasureChestGrid){
 		counter =checkColumn(treasureChestGrid, row, col, counter);
 		counter=checkPreviousColumn(col, row, treasureChestGrid, counter);
 		counter=checkRightColumn(row, col, cols, treasureChestGrid, counter);
 
 		return counter ;
 
 	}
 
 	private int checkNextRow(int row,int col,int rows,int cols,int counter,TreasureChest[][] treasureChestGrid){
 		if(row!=(rows-1)){
 			counter=checkColumn(treasureChestGrid, row+1, col, counter);
 			counter=checkPreviousColumnRowBelow(row, col, treasureChestGrid, counter);
 			counter=checkRightColumnRowBelow(row, col, cols, treasureChestGrid, counter);
 		}
 		return counter;
 	}
 
 	private int checkRowAbove(int row,int col,int rows,int cols,int counter,TreasureChest[][] treasureChestGrid){
 		if(row!=0){
 			counter=checkColumn(treasureChestGrid, row-1, col, counter);
 			counter=checkPreviousColumnRowAbove(row, col, treasureChestGrid, counter);
 			counter=checkRightColumnRowAbove(row, col, cols, treasureChestGrid, counter);
 		}
 		return counter;
 	}
 
 	private int checkTreasureChestsNearby(int row,int col,int rows,int cols,int counter,TreasureChest[][] treasureChestGrid){
		counter =checkFirstRow(rows, col, cols, counter, treasureChestGrid);
 		counter=checkNextRow(row, col, rows, cols, counter, treasureChestGrid);
 		counter= checkRowAbove(row, col, rows, cols, counter, treasureChestGrid);
 		return counter;
 	}
 
 }
