 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 
 public class tamedMobHelperListener implements Listener {
 
 	private rodolffoUtilsReloaded plugin;
 
 	public tamedMobHelperListener(rodolffoUtilsReloaded instance)
 	{
 		this.plugin = instance;
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void interact(PlayerInteractEntityEvent e)
 	{
 		Player p = e.getPlayer();
 		if((e.getRightClicked() instanceof Wolf || e.getRightClicked() instanceof Ocelot) && (plugin.perm.has(p, "rur.tamedmob") || p.isOp()))
 		{
 			if(e.getRightClicked() instanceof Tameable && e.getRightClicked() instanceof Animals)
 			{
				Tameable ent = (Tameable)e.getRightClicked();
 				String name = e.getRightClicked().getType().getName().equals("Wolf")?"farkas":"macska";
				p.sendMessage(plugin.messages.getString("othercommand.animalowner").replace("%m", name).replace("%n", ((Player)ent.getOwner()).getName()));
 			}
 		}
 	}
 }
