 package net.thiagoalz.hermeto.control;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.thiagoalz.hermeto.panel.GameManager;
 import net.thiagoalz.hermeto.player.Player;
 import android.util.Log;
 
 public class ADKGameplayControl implements GameplayControl {
 
 	protected static final String tag = ADKGameplayControl.class.getCanonicalName();
 	
 	protected GameManager gameManager;
	protected Map<Integer, String> playerIDMap;
 	
 	private boolean defaultPlayersConnected = false;
 	
 	public ADKGameplayControl(GameManager gameManager){
 		this.gameManager = gameManager;
		playerIDMap = new HashMap<Integer, String>();
 	}
 	
 	/**
 	 * Connect a number of players taken as parameters.
 	 * @param number The number of players to connect.
 	 */
 	public void connectDefaultPlayers(int number) {
 		// Connecting ADK players
 		for(int i = 0; i < number; i++){
 			connectPlayer(i);
 		}
 		defaultPlayersConnected = true;
 	}
 	
 	/**
 	 * Execute a command in the application. The command is associated 
 	 * with the player. The command options can be see here:
 	 * 
 	 * Command - 	Value - 	Description
 	 * 
 	 * NOTHING		0			The player is still. //Not in use.
 	 * TOP			1			Move to the top one square from where the player is.
 	 * DOWN			2			Move to the down one square from where the player is.
 	 * LEFT			3			Move to the left one square from where the player is.
 	 * RIGHT		4			Move to the right one square from where the player is.
 	 * BUTTON		5			Mark the square where the player is located.
 	 * CONNECT		6			Connect the player to the game. //Not in use.
 	 * 
 	 * @param playerReference The number that identify the user (on the ADK 1-4).
 	 * @param command The command that will be executed.
 	 * @return True if the command executes successfully. Otherwise return false. 
 	 */
 	@Override
 	public boolean processMessage(String playerReference, String message){
 		try{
 			int playerID=Integer.parseInt(playerReference);
 			int command=Integer.parseInt(message);
 			
 			if (command > 0 && command <= 4) {
 				Log.d(tag, "Executing moving command");
 				return movePlayer(playerID, command);			
 			} else if (command == 5){
 				Log.d(tag, "Executing mark command");
 				return markSquare(playerID);
 			} else if (command == 6) {
 				Log.d(tag, "Executing connecting command");
 				return connectPlayer(playerID);
 			}
 		}catch(NumberFormatException e){
 			Log.d(tag, "Error trying to parseint: Player("+playerReference+"), Message("+message+")");
 		}
 		return false;
 	}
 	
 	protected boolean movePlayer(int playerID, int command) {
 		Player.Direction direction = null;
 		switch(command) {
 			case 1:
 				direction = Player.Direction.UP;
 				break;
 			case 2:
 				direction = Player.Direction.DOWN;
 				break;
 			case 3:
 				direction = Player.Direction.LEFT;
 				break;
 			case 4:
 				direction = Player.Direction.RIGHT;
 				break;
 		}
 		if (direction != null && playerIDMap.get(playerID)!=null) {
 			return gameManager.getPlayer(playerIDMap.get(playerID)).move(direction);
 		}
 		return false;
 	}
 	
 	protected boolean markSquare(int id) {
 		String playerID = playerIDMap.get(id);
 
 		if(playerID!=null){
 			return gameManager.getPlayer(playerID).mark();
 		}
 		
 		return false;
 	}
 	
 	protected boolean connectPlayer(int id) {
 		String playerID = gameManager.connectPlayer().getId();
 		Log.d(tag, "Mapping the player with ID # " + id + " to " + playerID);
 		playerIDMap.put(id, playerID);
 		return playerID != null;
 	}
 	
 	public boolean isDefaultPlayersConnected() {
 		return defaultPlayersConnected;
 	}
 	
 	public void disconnectAllPlayers() {
 		if(playerIDMap!=null){
 			for (Integer id : playerIDMap.keySet()) {
 				String playerID = playerIDMap.get(id);
 				
 				Player player = gameManager.getPlayer(playerID);
 				if (player!=null){//The player is still connected?
 					gameManager.disconnectPlayer(player);
 				}
 			}
 		}
		playerIDMap = new HashMap<Integer, String>();
 		defaultPlayersConnected = false;
 	}
 
 }
