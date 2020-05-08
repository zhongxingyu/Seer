 
 import java.util.logging.Level;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author jonathan
  */
 public class Whitelist extends Mod {
 	protected static Playerlist players;
 	protected static Opslist ops;
 
 	@Override
 	public void activate() {
 		Whitelist.players = new Playerlist();
 		Whitelist.players.load();
 		Whitelist.ops = new Opslist();
 		Whitelist.ops.load();
 	}
 
 	protected boolean parseCommand(Player player, String[] tokens) {
 		if( !player.isAdmin() ) return false;
 		
 		String command = tokens[0].substring(1);
 		if( command.equalsIgnoreCase("whitelist") ) {
 			String playerName = tokens[1].trim();
 			boolean success = players.addPlayer(playerName);
 			players.save();
 			if( success ) {
 				players.save();
 				player.sendChat(String.format("Whitelist: adding %s", playerName));
 			}
 			else {
 				player.sendChat(String.format("Whitelist: %s already whitelisted.", playerName));
 			}
 			return true;
 		}
 		else if( command.equalsIgnoreCase("unwhitelist") ) {
 			String playerName = tokens[1].trim();
 			boolean success = players.removePlayer(playerName);
 			if( success ) {
 				players.save();
 				player.sendChat(String.format("Whitelist: removing %s", playerName));
 			}
 			else {
 				player.sendChat(String.format("Whitelist: %s not on whitelist.", playerName));
 			}
 			return true;
 		}
 		else if( command.equalsIgnoreCase("help") ) {
 			player.sendChat("Whitelist: !whitelist, !unwhitelist");
			return tokens[0].substring(0,1).equalsIgnoreCase("!");
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean onPlayerChat(Player player, String message) {
 		return this.parseCommand(player, message.split(" "));
 	}
 
 	@Override
 	public boolean onPlayerCommand(Player player, String[] command) {
 		return this.parseCommand(player, command);
 	}
 	
 	@Override
 	public String onPlayerLoginCheck(String playerName) {
 		if( ops.isOp(playerName) ) {
 			Server.log(Level.INFO, "Admin Login: "+playerName);
 			return null;
 		}
 		else if( !players.isWhitelisted(playerName) ) {
 			Server.log(Level.WARNING, "Denied access to: "+playerName);
 			Server.sendGlobalMessage("[SERVER] Denied access to: "+playerName);
 			return "Access denied.";
 		}
 		return null;
 	}
 }
