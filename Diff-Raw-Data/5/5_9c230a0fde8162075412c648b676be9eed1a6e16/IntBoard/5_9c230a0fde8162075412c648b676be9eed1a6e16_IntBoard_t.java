 package experiment;
 
 import java.util.LinkedList;
 import java.util.TreeSet;
 
 public class IntBoard {
 
 	public IntBoard() {
 		// TODO implement
 	}
 	
 	public void calcAdjacencies() {
 		// TODO implement
 	}
 	
 	public void calcTargets(int startCell, int steps) {
 		// TODO implement
 	}
 	
 	public TreeSet<Integer> getTargets() {
 		// TODO implement
 		return new TreeSet<Integer>();
 	}
 	
 	public LinkedList<Integer> getAdjList(int cell) {
 		// TODO implement
 		return new LinkedList<Integer>();
 	}
 	
	public int calcIndex(int row, int col) {
		return row*4+col;
 	}
 }
