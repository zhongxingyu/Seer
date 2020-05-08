 import java.util.HashMap;
 public class MinesweeperThread extends Thread {
 	final Board board;
 	final int[] pixels;
 	final static int screen_width=1920;
 	final static int screen_height=1080;
 	final int board_width= 30 * 24;
 	final int board_height= 30 * 24;
 	final int mines = (int) (99 * 24 * 24 * 1.05);
 	
 	public static void main(String[] args){
 		MinesweeperThread ms = new MinesweeperThread(new int[screen_width * screen_height]);
 		while(true) ms.run();
 		
 	}
 	
 	public MinesweeperThread(int[] pixels){
 		board = new Board(board_width,board_height,mines,pixels);
 		this.pixels = pixels;
 	}
 
 	// call start to start continually solving minesweeper boards
 	public void run(){
 		while(true){
 			board.new_game();
 			
 			while(!board.lost && !board.won){
				/*try {
					sleep(0);
				} catch (InterruptedException e) {}*/
 				boolean made_a_move = false;
 				
 				for(int x = 0; x<board.width; x++){
 					//System.out.println("1");
 					for(int y = 0; y<board.height; y++){
 						if(board.view_cell(x, y) > 0){
 							
 							int surrounding_empty_spaces = board.total_arround_cell(x, y, "uncovered?", false);
 							//int surrounding_flags = board.total_arround_cell(x, y, "flagged?");
 							
 							if (surrounding_empty_spaces == board.view_cell(x,y)){
 								made_a_move = board.right_click(x-1, y-1) || made_a_move;
 								made_a_move = board.right_click(x-1, y  ) || made_a_move;
 								made_a_move = board.right_click(x-1, y+1) || made_a_move;
 								made_a_move = board.right_click(x  , y+1) || made_a_move;
 								made_a_move = board.right_click(x+1, y+1) || made_a_move;
 								made_a_move = board.right_click(x+1, y  ) || made_a_move;
 								made_a_move = board.right_click(x+1, y-1) || made_a_move;
 								made_a_move = board.right_click(x  , y-1) || made_a_move;
 							}
 						}
 					}
 				}
				int total_clicks = 0;
 				for(int x = 0; x<board.width; x++){
 					//System.out.println("2");
 					for(int y = 0; y<board.height; y++){
 						int surrounding_empty_spaces = board.total_arround_cell(x, y, "uncovered?", false);
 						int surrounding_flags = board.total_arround_cell(x, y, "flagged?");
 						
 						if(surrounding_empty_spaces > 0 && 
 						   surrounding_flags == board.view_cell(x, y) && 
						   (boolean) board.board[x][y].get("uncovered?") &&
						   total_clicks++ < 30){
 								made_a_move = board.middle_click(x, y) || made_a_move;
 						}
 					}
 				}
 				while(!made_a_move){
 					int x = (int) (Math.random() * board.width);
 					int y = (int) (Math.random() * board.height);
 					//System.out.println("guessing (" +x+","+y+")");
 					made_a_move = board.left_click(x, y);
 				}
 				if(board.won){
 					//System.out.println("WON!");
 					//board.print_board();
 					//while(true){}
 				} else if(board.lost){
 					//System.out.println("LOST!");
 					//board.print_board();
 					//while(true){}
 				}
 			}
 		}
 	}
 }
