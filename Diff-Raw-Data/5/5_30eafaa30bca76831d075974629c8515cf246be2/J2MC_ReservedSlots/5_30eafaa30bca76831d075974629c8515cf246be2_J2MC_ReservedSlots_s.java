 package to.joe.j2mc.resslots;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent.Result;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class J2MC_ReservedSlots extends JavaPlugin implements Listener{
 
 	public void onEnable(){
 		this.getServer().getPluginManager().registerEvents(this, this);
 		this.getLogger().info("Reserved slots module enabled");
 	}
 	
 	public void onDisable(){
 		this.getLogger().info("Reserved slots module disabled");
     }
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
 		boolean isAdmin = J2MC_Manager.getPermissions().isAdmin(event.getName());
 		boolean isDonator = J2MC_Manager.getPermissions().hasFlag(event.getName(), 'd');
 		if(this.getServer().getOnlinePlayers().length >= this.getServer().getMaxPlayers()){
 			if(!isAdmin && !isDonator){
				event.disallow(Result.KICK_OTHER, "Server full. Donators get a reserved slot. Donate at donate.joe.to");
 			}else{
 				if((this.getServer().getMaxPlayers() + 10) >= this.getServer().getOnlinePlayers().length){
 					for(Player plr : this.getServer().getOnlinePlayers()){
 						if(!J2MC_Manager.getPermissions().isAdmin(plr.getName()) || !J2MC_Manager.getPermissions().hasFlag(plr.getName(), 'd')){
							plr.kickPlayer("Making room for admin or donator");
 							break;
 						}
 					}
 					event.allow();
 				}else{
 					event.allow();
 				}
 			}
 		}
 	}
 	
 }
