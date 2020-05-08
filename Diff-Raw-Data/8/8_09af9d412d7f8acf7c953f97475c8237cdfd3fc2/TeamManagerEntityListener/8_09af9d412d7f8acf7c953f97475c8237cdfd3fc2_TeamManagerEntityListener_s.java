 package com.xhizors.TeamManager;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import com.nijiko.permissions.PermissionHandler;
 
 public class TeamManagerEntityListener extends EntityListener {
 	
 	private TeamManager teamManager;
 	public List<String> maps;
 	
 	public TeamManagerEntityListener(TeamManager instance) {
 		teamManager = instance;
 	}
 	
 	@Override
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (teamManager.friendlyfire) {
 			return;
 		}
 		if (event instanceof EntityDamageByEntityEvent) {
 			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;
			if (evt.getEntity() instanceof Player && evt.getDamager() instanceof Player) {
 				if (!teamManager.worlds.contains(event.getEntity().getWorld().getName())) {
 					return;
 				}
 				Player damager = (Player) evt.getDamager();
				Player damagee = (Player) evt.getEntity();
 				PermissionHandler ph = teamManager.getPermissionHandler();
 				String[] groups = ph.getGroups(damagee.getWorld().getName(), damagee.getName());
 				for (String group : groups) {
					if (ph.inGroup(damager.getWorld().getName(), damager.getName(), group)) {
 						damager.sendMessage(ChatColor.RED + "Friendly Fire disabled");
 						event.setCancelled(true);
 						event.setDamage(0);
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 }
