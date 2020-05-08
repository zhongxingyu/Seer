 /**
  * AuthPlayer: AuthPlayer.java
  * @author Alex Riebs
  * Created Jul 6, 2011
  */
 package archangel.authplayer;
 
 import java.util.logging.Logger;
 
 import net.minecraft.server.EntityPlayer;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author Alex "Arcalyth" Riebs
  * License: Don't steal my shit. Give credit if you modify and/or redistribute any of my code.
  * Otherwise, you're free to do whatever else you want with it.
  * 
  * Also, the Bukkit license applies.
  */
 public class AuthPlayer extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	private final AuthPlayerPlayerListener playerListener = new AuthPlayerPlayerListener(this);
 	private final AuthPlayerBlockListener blockListener = new AuthPlayerBlockListener(this);
 
 	private AuthDatabase db;
 	
 	/*
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	@Override
 	public void onDisable() {
 		log.info("AuthPlayer disabled.");
 	}
 
 	/*
 	 * @see org.bukkit.plugin.Plugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {		
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, 
 				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, 
				Event.Priority.High, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, 
				Event.Priority.High, this);
 		
 		db = new AuthDatabase(this);
 		
 		log.info("AuthPlayer enabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("auth")) {
 			// [?] Provide help on /auth
 			if (args.length <= 0) return false;
 			
 			if (args[0].equals("list"))
 				return authList(sender);
 			else
 				return authLogin(sender, args);
 		}
 		
 		return false; 
 	}
 	
 	public boolean authList(CommandSender sender) {
 		sender.sendMessage("Registered players: \n" + getAuthDatabase().toString());
 		return true;
 	}
 	
 	public boolean authLogin(CommandSender sender, String[] args) {
 		if (args.length < 1) return false;
 		
 		// [?] We can only work with Player/CraftPlayer objects...
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			String username = player.getName();
 			
 			AuthDatabase db = getAuthDatabase();
 			
 			// [?] Catch people that already have a name
 			if (!username.equals("Player")) {
 				// [?] Already have a name, and already in the db? Must be premium or already authenticated.
 				if (db.get(username) != null) {
 					player.sendMessage("You are already authenticated!");
 				} else {
 					db.add(username, new AuthInfo(player, args[0]));
 					player.sendMessage("You have registered and " +
 							"authenticated your name. Welcome, " + username + "!");
 				}
 				
 				return true;
 			} else {
 				// [?] I'm a guest, help!
 				if (args.length < 2) {
 					player.sendMessage("Please supply a username AND password.");
 					return true;
 				}
 				
 				String loginName = args[0];
 				String password = args[1];
 				
 				if (loginName.equals("Player")) {
 					player.sendMessage("Nice try, sly guy.");
 					return true;
 				}
 			
 				// [?] Does the player exist on the server?
 				if (db.get(loginName) == null) {
 					db.add(loginName, new AuthInfo(player, password));
 					player.sendMessage("You have registered the name " + loginName + ". Use /auth again to authenticate.");
 					log.info("AuthPlayer: New user " + loginName + " created");
 				} else {
 					// [?] Player exists, is he online?
 					Player checkPlayer = getServer().getPlayer(loginName);
 					if (checkPlayer != null && checkPlayer.isOnline())
 						player.sendMessage("Someone is already authenticated as " + loginName + "!");
 					else {
 						// [?] Player isn't currently playing, so authenticate.
 						AuthInfo info = db.get(loginName);
 						if (info.authenticate(password)) {
 							EntityPlayer entity = ((CraftPlayer)player).getHandle();
 							entity.name = loginName;
 							entity.displayName = entity.name;
 
 							player.loadData();
 							player.teleport(player);
 
 							player.sendMessage("You are now authenticated. Welcome, " + entity.name + "!");
 							log.info("AuthPlayer: " + entity.name + " identified via /auth");
 						} else {
 							player.sendMessage("Could not authenticate with the given credentials.");
 						}
 					}
 				}					
 			}
 		}
 		
 		return true;
 	}
 
 	public AuthDatabase getAuthDatabase() {
 		return db;
 	}
 }
