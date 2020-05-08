 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - GameHelper.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: This implements a simple menu-driven UI that an administrator can
  * use for creating, deleting, and viewing the static information about games
  * that should be hosted in the server, and their metadata.
  ******************************************************************************/
 package drexel.edu.blackjack.db.game;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 public class GameHelper {
 	public static int main(String[] args) {
 		GameManagerInterface gm = FlatfileGameManager.getDefaultGameManager();
 		gm.load();
 		GameHelper gh = new GameHelper();
 		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
 		
 		while (true) {
 			System.out.println("1. See all games");
 			System.out.println("2. Add a game");
 			System.out.println("3. Remove a game");
 			System.out.println("0. Exit");
 			try {
 				int i = Integer.parseInt(r.readLine());
 				switch (i) {
 				case 1:
 					gh.list(); break;
 				case 2:
 					gh.add(); break;
 				case 3:
 					gh.remove(); break;
 				default:
 					return 0;	
 				}
 			} catch (Exception e) {
 				
 			}
 		}
 	}
 	
 	private void remove() {
 		System.out.println("Id: ");
 		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
 		try {
 			String s = r.readLine();
 			GameManagerInterface gm = FlatfileGameManager.getDefaultGameManager();
 		} catch (IOException e) {
 			System.out.println("Input ERROR");
 		}
 		
 	}
 
 	private void list() {
 		GameManagerInterface gm = FlatfileGameManager.getDefaultGameManager();
 		for (GameMetadata g: gm.getGames()) {
 			System.out.println(g.toString());
 		}
 	}
 	
 	private void add() {
 		String s;
 		GameMetadata.Builder b = new GameMetadata.Builder();
 		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
 		GameManagerInterface gm = FlatfileGameManager.getDefaultGameManager();
 		try {
 			System.out.println("id:");
 			s = r.readLine(); b.setId(s);
 			System.out.println("numDecks:");
 			s = r.readLine(); b.setNumDecks(Integer.parseInt(s));
 			System.out.println("rules: ( Enter !!! to end rules) ");
 			ArrayList<String> rules = new ArrayList<String>();
 			while (true) {
 				s = r.readLine();
 				if (s.equals("!!!")) break;
 				rules.add(s);
 			}
 			b.setRules(rules);
 			System.out.println("minBet:");
 			s = r.readLine(); b.setMinBet(Integer.parseInt(s));
 			System.out.println("maxBet:");
 			s = r.readLine(); b.setMaxBet(Integer.parseInt(s));
 			System.out.println("minPlayers:");
 			s = r.readLine(); b.setMinPlayers(Integer.parseInt(s));
 			System.out.println("maxPlayers:");
 			s = r.readLine(); b.setMaxPlayers(Integer.parseInt(s));
 			if (gm.add(b.build())) {
 				System.out.println("Added successfully");
 			}
 		} catch (Exception e) {
 			throw new IllegalArgumentException();
 		}
 	}
 }
