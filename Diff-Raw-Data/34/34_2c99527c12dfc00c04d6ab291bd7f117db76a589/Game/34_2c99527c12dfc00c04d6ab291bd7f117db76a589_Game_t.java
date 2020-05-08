 package cell.g4;
 
 
 // A class to access global information
 public class Game {
 	private static Game thegame;
 	private int ourIndex = -1;
 	
 	private Team[] teams;
 	private int[][] tradersLast = null;
 	
 	public static Game getGame() {
 		assert(thegame != null);
 		return thegame;
 	}
 	
 	private Game(int[] location, int[][] players) {
 		teams = new Team[players.length];
 		for (int i = 0; i < teams.length; i++)
 			teams[i] = new Team(); 
 		ourIndex = findPlayerIndex(location, players);
 	}
 	
 	
 	private int findPlayerIndex(int[] location, int[][] players) {
 		int index = -1;
 		for (int i = 0; i < players.length; i++) {
 			if (location[0] == players[i][0] && location[1] == players[i][1]) {
 				index = i;
 				break;
 			}
 		}
 		assert(index >= 0);
 		return index;
 	}
 	
 	public static Game initGame(int[] location, int[][] players) {
 		thegame = new Game(location, players);
 		return thegame;
 	}
 
 	// VERY TRICKY !!!
 	// NEED TO KEEP TRACK OF LAST LOCATION OF A TRADER
 	public void updateTrades(int[][] players, int[][] traders) {
 		if (tradersLast == null) {
 			tradersLast = traders; 
 			return;
 		}
 		
 		for (int i = 0; i < traders.length; i++) {
 			if (traders[i][0] != tradersLast[i][0] || traders[i][1] != tradersLast[i][1]) {
 				for (int j = 0; j < players.length; j++) {
					if (players[j] == null)
						continue;
 					if (players[j][0] == tradersLast[i][0] && players[j][1] == tradersLast[i][1]) {
 						teams[j].addTrade();
 						break;
 					}
 				}
 			}
 		}
 		tradersLast = traders;
 	}
 	
 	public Team[] getTeams() {
 		return teams;
 	}
 	
 	public int getOurIndex() {
 		return ourIndex;
 	}
 }
