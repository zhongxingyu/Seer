 package me.meiamsome.myriadtowny;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.palmergames.bukkit.TownyChat.Chat;
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.object.Resident;
 import com.palmergames.bukkit.towny.object.Town;
 
 public class MyriadTowny extends JavaPlugin {
 	Chat townyChat;
 	Towny towny;
 	Logger log=Logger.getLogger("Minecraft");
 	PluginManager pm;
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		pm=getServer().getPluginManager();
 		Plugin plugin = pm.getPlugin("Towny");
         if(plugin == null || !(plugin instanceof Towny)) {
 			log.warning("Could not find Towny!");
 		} else {
 			towny=(Towny)plugin;
 		}
 		plugin = pm.getPlugin("TownyChat");
         if(plugin == null || !(plugin instanceof Chat)) {
 			log.warning("Could not find TownyChat!");
 		} else {
 			townyChat=(Chat)plugin;
 		}
         pm.registerEvent(Event.Type.PLAYER_CHAT, new pListen(), Event.Priority.Highest, this);
         pm.registerEvent(Event.Type.BLOCK_PLACE, new bListen(), Event.Priority.Lowest, this);
 	}
 	class pListen extends PlayerListener {
 		@Override
 		public void onPlayerChat(PlayerChatEvent event) {
 			//if(!townyChat.getTowny().getPlayerMode(event.getPlayer()).isEmpty()) {
 			String a="amp";
 			if(event.getPlayer().hasPermission("mt.colorChat")) {
 				while(event.getMessage().contains("["+a+"]")) a+="1";
				a="["+a+"]";
				event.setMessage(event.getMessage().replaceAll("/&", a));
				event.setMessage(event.getMessage().replaceAll("&", "§"));
				event.setMessage(event.getMessage().replaceAll(a, "&"));
 			}
 			//}
 		}
     }
 	class bListen extends BlockListener {
 		@Override
 		public void onBlockPlace(BlockPlaceEvent event) {
 			if(!event.isCancelled()) return;
 			if(towny.getTownyUniverse().isWilderness(event.getBlock())) return;
 			try {
 				Town town=towny.getTownyUniverse().getTownBlock(event.getBlock().getLocation()).getTown();
 				Resident res=towny.getTownyUniverse().getResident(event.getPlayer().getName());
 				if(town.isMayor(res) || town.getAssistants().contains(res)) {
 					event.setCancelled(false);
 				}
 			} catch (Exception e) {}
 		}
     }
 }
