 package DerpyAI;
 
 public class Driver {
 
 	public static void main(String[] args) {
 
 		DerpyBoard db = new DerpyBoard();
 		DerpyAI aiOne = new DerpyAI(true);
		DerpyAI aiTwo = new DerpyAI(false);
 		for (int i = 0; i < 20; i++) {
 			aiOne.makeMove(db);
			aiTwo.makeMove(db);
 		}
 	}
 
 }
