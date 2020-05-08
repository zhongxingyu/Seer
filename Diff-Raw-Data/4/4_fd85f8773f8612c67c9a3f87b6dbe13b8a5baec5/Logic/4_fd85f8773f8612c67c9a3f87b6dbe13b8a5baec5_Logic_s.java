 package oving4;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Logic {
 	
 	private Board board;
 	private Random random;
 	
 	private int sides;
 	
 	private final int penaltyModifier = 1;
 	private final int dTemp = 1;
 	private final int tempMax = 500;
 	
 	
 	public Logic(int sides, int maxNumberOfEggs){
 		this.board = new Board(sides, maxNumberOfEggs, penaltyModifier);
 		this.sides = sides;
 	}
 	
 	
 	public Board saAlgorithm() {
 		Board current = board;
 		int temp = tempMax;
 		double fP = current.evaluate();
 		while(fP >= 1) {
 			ArrayList<Board> neighbors = getNeighbors(current);
 			double fpMax = 0;
 			Board pMax = null;
 			for(Board n : neighbors) {
 				double nScore = board.evaluate();
 				if(nScore > fpMax) {
 					fpMax = nScore;
 					pMax = n;
 				}
 			}
 			double q = ((fpMax-fP)/fP);
 			double p = Math.min(1, Math.pow(Math.E, ((-q)/temp)));
 			double x = Math.random();
 			if(x>p)
 				current = pMax;
 			else 
 				current = neighbors.get(random.nextInt(neighbors.size()));
 			temp = temp - dTemp;
 		}
 		return current;
 	}
 	
 	/**
 	 * Generates semi-random neighbors from the given board
 	 * @param b		The board the neigbors will be generated from
 	 * @return		An ArrayList containing the neighbors
 	 */
 	public ArrayList<Board> getNeighbors(Board b) {
 		ArrayList<Board> neighbors = new ArrayList<Board>();
 		for(int i=0; i<7; i++) {
 			Board n = null;
 			
 			//Try to clone the Board
 			try {
 				n = b.clone();
 			} catch (CloneNotSupportedException e) {
 				System.out.println("Fail! Hva skjer?");
 				e.printStackTrace();
 			}
 			//Generate semi-random neighbors.
 			switch(i) {
 			case 0: { n.setEgg(random.nextInt(sides), random.nextInt(sides), true);	} 			//Place 2 eggs
 			case 1: {} 																			//Place 1 egg
 			case 2: { n.setEgg(random.nextInt(sides), random.nextInt(sides), true); break; }	//Place 1 egg
			case 3: { n.invertEgg(random.nextInt(sides), random.nextInt(sides));	} 			//Inverse 2 eggs
			default: {n.invertEgg(random.nextInt(sides), random.nextInt(sides)); 	} 			//Inverse 1 egg
 			}
 			neighbors.add(n);
 		}
 		return neighbors;	
 	}
 
 }
