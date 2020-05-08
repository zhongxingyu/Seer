 /*	GameHandler
 
 	Associates two UserInfo (players) with a Game instance, and handles things like whose
 	turn it is, the performance of moves, and broadcasting of the game's state.
 
 	Things still needed: 
 		
 	-	A "observers" array, which will probably be an ArrayList for easyness 
 		purposes. 
 
 	-	Changes to make this use the Protocol class for I/O. (positions where this is needed
 		are indicated by '// PROTOCOL' as a comment.
 
 	-	Exception handling, handling a player who wishes to exit, etc. 
 
  */ 
 
 public class GameHandler
 {
 	private UserInfo[] players; 	// Information on each of the players
 	private Game game;		// Their game.
 	private int turnIndex;		// Index into array of players to determine whose turn it is. 
 	private boolean inSession;	// Indicates whether a game is in session between these two players. 
 	private Protocol prtcl;
 
 	public GameHandler(UserInfo player1, UserInfo player2)
 	{
 		prtcl = new Protocol();
 	
 		// Initialize the players array to hold both User(Info)s
 		players = new UserInfo[2];
 		players[0] = player1;
 		players[1] = player2;
 	
 		// Let the players know that their game is not far away. (and who they'll be playing)
 		broadcast(prtcl.makePacket("The game is about to begin.", "MESSAGE"));
 		players[0].sendMessage(prtcl.makePacket("You'll be playing '"+players[1].getName() + "' today.", "message"));
 		players[1].sendMessage(prtcl.makePacket("You'll be playing '"+players[0].getName() + "' today.", "message"));
 		
 		// The "first player" is always 0, which should be the player who waited the longest / joined server first. 
 		turnIndex = 0;
 		inSession = false;
 	}
 
 /*	broadcast()
 
 	Transmits the passed string to all players (and potentially all watchers in the near future. 
 
  */
 	private void broadcast(String message)
 	{
 		for(int i = 0; i < players.length; i++)
 		{
 // PROTOCOL
 			players[i].sendMessage(message);
 // PROTOCOL
 		}
 	}
 
 /*	start()
 
 	Initialize the game, and send strings to both players introducing them to each other, as well as 
 	a picture of the game state. 
 
 	Can return false if the game is in session (at which point the server is being bad, or something).
 		Alternatively, if we handle players leaving by completing the game, but not updating inSession,
 		then we can use that as a termination, but that's not very obvious.
 
  */
 	public boolean start()
 	{
 
 		game = new Game();
 	
 
 		// We can't specify newlines to broadcast for now. This should change when we have a protocol set up.
 		// (right now, client reads a line - i.e. to the first newline char)
 		broadcast(" ");			
 		broadcast(game.toString());
 		if(inSession == false)
 		{
 			inSession = true;
 			
 			while(!game.isDone())
 				promptTurn();
 			
 			players[turnIndex].sendMessage(prtcl.makePacket("You are Winner! Ha Ha Ha!", "message"));
 			players[(turnIndex + 1) % 2].sendMessage(prtcl.makePacket("Thou hast been defeated, mortal.", "message"));
 			broadcast(prtcl.makePacket(" ", "message"));
 		
 			inSession = false;
 		}
 		
 		return !inSession;
 	}
 
 /* 	promptTurn()
 
 	Does what it says - Prompts the person in index turnIndex to make a move - processes that move. 
 
  */	
 	private void promptTurn()
 	{
 		UserInfo curr;
 		String packet;
 		curr = players[turnIndex];
 		String[] tokens;
 		int x;
 		int y;
 	
  	
 		curr.sendMessage(prtcl.makePacket("Please enter a move", "message")); 
 		packet = curr.getMessage();
 		
		if(prtcl.getCommand(packet).equals("HELP"))
 		{
 			//curr.sendMessage(prtcl.makePacket(helpMessage()));
 		}
 		
		tokens = prtcl.getData(packet).split(" ");
 
 		x = Integer.parseInt(tokens[0]);
 		y = Integer.parseInt(tokens[1]);
 	
 		// Should have a method to process the user's input, and provide an error message back to them if necessary. 
 		if(game.remove(x, y))
 		{
 			players[(turnIndex + 1) % 2].sendMessage(prtcl.makePacket("Other player took " + tokens[0] + " items from row " + tokens[1] + ".", "message"));
 			broadcast(prtcl.makePacket(game.toString(), "message"));
 			// This is where the check to see if the game is won occurs. If so, a winner (the person who just moved) is 
 			// congratulated. 
 			if(!game.isDone())
 			{
 				turnIndex += 1;
 				turnIndex %= 2;
 			}
 		} else
 		{
 // PROTOCOL
 			players[turnIndex].sendMessage(prtcl.makePacket("Something was wrong with that. Try again.", "ERROR"));
 // PROTOCOL
 		}	
 	}
 	
 	/*private String helpMessage(String msg)
 	{
 		String message;
 		String[] = {"Help", "Login", "Remove", "Bye"};
 		HashMap msgs = new HashMap();
 		
 		msgs.put("help", "Usage: $help\n Displays all possible commands.");
 		msgs.put("login", "Usage: $login [username]\n Logs in the player 'username'.");
 		msgs.put("remove", "Usage: $remove [set] [number]\n Removes 'number' objects from set 'set'.");
 		msgs.put("bye", "Usage: $bye\n Quit the game.");
 	}*/
 }
