 package oving5;
 
 import java.util.Random;
 import java.util.Scanner;
 
 public class Simulator {
 	MinConflict mc;
 	Board board;
 	
 	public Simulator(int size){
 		board = new Board(size);
 		generateBoard(size);
 	}
 	
 	private void generateBoard(int size){
 		Random random = new Random();
 		for (int i = 0; i < size;) {
 			int x = random.nextInt(size);
 			int y = random.nextInt(size);
 			if (!board.getSquare(x, y).isQueen()) {
 				board.getSquare(x, y).setQueen(true);
 				i++;
 			}
 		}
 	}
 	
 	public void start() {
 		
 		long startTime = System.nanoTime();
 		mc = new MinConflict(10000, board);
 		if(mc.solve()){
 			long timeSpent = (long)((System.nanoTime()-startTime)/1000000);
 			System.out.println("Min-conflict was successful:");
 			if(board.k < 35) {
 				GuiFrame f = new GuiFrame(board, timeSpent, mc.getNmbOfIterations());
 			} else
 				mc.printBoard();
 		}else{
			System.out.println("Min-conflict was not successful, this is its last result:");
 			mc.printBoard();
 		}
 	}
 	
 	
 	public static void main(String[] args) {
 		
 		Scanner input = new Scanner(System.in);
 		
 		System.out.println("Size of board:");
 		int size = 0;
 		try {
 			size = input.nextInt();
 		} catch (Exception e) {
 			System.out.println("Thats not a integer");
 			e.printStackTrace();
 		}
 		
 		Simulator sim = new Simulator(size);
 		sim.start();
 	}
 
 
 }
