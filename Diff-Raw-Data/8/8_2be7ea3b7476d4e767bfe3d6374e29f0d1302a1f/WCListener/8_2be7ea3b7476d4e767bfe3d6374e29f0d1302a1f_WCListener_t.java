 package me.FluffyWolfers.WC;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 @SuppressWarnings("deprecation")
 public class WCListener implements Listener{
 	
 	Entity en;
 	
 	ArrayList<Player> pla = new ArrayList<Player>();
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void clickWolf(PlayerInteractEntityEvent e){
 		
 		Player p = e.getPlayer();
 		Entity clicked = e.getRightClicked();
 		
 		if(p.getItemInHand().getType().equals(Material.STICK)){
 		
 			if(clicked instanceof Wolf){
 				
 				if(((Wolf) clicked).isTamed()){
 					
 					if(((Wolf) clicked).getOwner().equals(p)){
 
 						en = clicked;
 						
 						pla.add(p);
 						
 						p.sendMessage(ChatColor.GOLD + "Choose a color: " + ChatColor.BLUE + "blue, " + ChatColor.LIGHT_PURPLE + "pink, " + ChatColor.DARK_GREEN + "green, " + ChatColor.GREEN + "lime, " + ChatColor.GOLD + "orange, " + ChatColor.RED + "red, " + ChatColor.YELLOW + "yellow, or " + ChatColor.DARK_PURPLE + "purple");
 						
 					}else{
 						p.sendMessage(ChatColor.DARK_RED + "Not yours!");
 						return;
 					}
 					
 				}else{
 					p.sendMessage(ChatColor.DARK_RED + "Not tamed!");
 					return;
 				}
 				
 			}else{
 				p.sendMessage(ChatColor.DARK_RED + "Not a wolf!");
 				return;
 			}
 		
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onChat(PlayerChatEvent e){
 		
 		Player p = e.getPlayer();
 		String msgStr = e.getMessage();
		String msg = ChatColor.stripColor(msgStr.toLowerCase());
 		
 		if(pla.contains(p)){
 			
 			e.setCancelled(true);
 			
 			if(en instanceof Wolf){
 				
 				Wolf wolf = (Wolf) en;
 				
				p.sendMessage(msgStr);
				
 				if(msg.equalsIgnoreCase("blue")){
 					wolf.setCollarColor(DyeColor.BLUE);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("green")){
 					wolf.setCollarColor(DyeColor.GREEN);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("red")){
 					wolf.setCollarColor(DyeColor.RED);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("yellow")){
 					wolf.setCollarColor(DyeColor.YELLOW);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("aqua")){
 					wolf.setCollarColor(DyeColor.LIGHT_BLUE);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("purple")){
 					wolf.setCollarColor(DyeColor.PURPLE);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("lightgreen")){
 					wolf.setCollarColor(DyeColor.LIME);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("orange")){
 					wolf.setCollarColor(DyeColor.ORANGE);
 					pla.remove(p);
 				}else if(msg.equalsIgnoreCase("pink")){
 					wolf.setCollarColor(DyeColor.PINK);
 					pla.remove(p);
 				}else{
 					p.sendMessage(ChatColor.DARK_RED + "Not a color! Please choose again!");
					p.sendMessage(ChatColor.GOLD + "Choose a color: " + ChatColor.BLUE + "blue, " + ChatColor.LIGHT_PURPLE + "pink, " + ChatColor.DARK_GREEN + "green, " + ChatColor.GREEN + "lime, " + ChatColor.GOLD + "orange, " + ChatColor.RED + "red, " + ChatColor.YELLOW + "yellow, or " + ChatColor.DARK_PURPLE + "purple");
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 }
