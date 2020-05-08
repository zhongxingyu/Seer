 package DerpyAI;
 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);
 		for (int i = 0; i < 20; i++) {
 			aiOne.makeMove(db);
 		}
 	}
 
 }
