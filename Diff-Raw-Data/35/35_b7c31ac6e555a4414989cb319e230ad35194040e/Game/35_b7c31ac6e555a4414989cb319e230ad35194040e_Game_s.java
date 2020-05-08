 package game;
 
 public class Game extends Plugin {
 	
 	// Variables
 	
 	private boolean hasWon;
 	private String winner;
 	
 	private int randomNumber;
 	
 	// Constructors
 	
 	public Game() {
 		super();
 		
		Function guess = new Function("guess", this) {
 			@Override
 			public Object run(Object args) {
 				int g = 0;
 				try {
 					g = ((Double) ((Object[]) args)[0]).intValue();
 				} catch(Exception e) {e.printStackTrace(); }
 				
 				return new Boolean(g == randomNumber);
 			}
 		};
		guess.desc = "Call this to guess a number. Arguments: [int guess]";
 	}
 	
 	public void startGame() {
 		hasWon = false;
 		winner = "";
 		
 		generateRandNum();
 	}
 	
 	private void generateRandNum() {
 		this.randomNumber = (int) (Math.random() * 10);
 	}
 }
