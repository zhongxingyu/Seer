 package me.gameplax.youtube;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class Eventsalle implements Listener {
 
 	
 	@EventHandler
 	public void onPlayerDeathEvent(PlayerDeathEvent event){
 		
 		Player p = event.getEntity().getPlayer();
 		
 		event.setDeathMessage(null);
 		
 		p.setHealth(20);
 		p.setFoodLevel(20);
 		p.setFireTicks(0);
 		
 		p.teleport(new Location(Bukkit.getServer().getWorld("world"), -13, 93, 371));
 		
 	}
 	
 	
 	@EventHandler
 	public void onPLayInteract(PlayerInteractEvent e){
 		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
         	Block i = e.getClickedBlock();
         	if(i.getState() instanceof Sign){
         		
             Player p = e.getPlayer();
             String playername = p.getPlayer().getName();
             	BlockState stateBlock = i.getState();
             	Sign sign = (Sign) stateBlock;
             	
             	if(sign.getLine(0).equalsIgnoreCase("YouTube") && sign.getLine(1).equalsIgnoreCase("Start")){
             		
             		
             		World welt = p.getWorld();
             		
 
           		 Location loc1 = new Location(welt, -156, 66, 441);
           		 Location loc2 = new Location(welt, -167, 65, 447);
           		 Location loc3 = new Location(welt, -177, 65, 458);
           		 Location loc4 = new Location(welt, -186, 65, 443);
            		 
            		 
            		 
            		 Random rnd = new Random();
            		 int zufallszahl = rnd.nextInt(3);
            		 
            		 Location loc = null;
            		 
            		 switch(zufallszahl){
            		 case 0:
            			 loc = loc1;
            			 break;
           		 case 1:
         			 loc = loc2;
         			 break;
         		 case 2:
         			 loc = loc3;
         			 break;
         		 case 3:
         			 loc = loc4;
 
            		 }
            		 
            		 p.teleport(loc);
            		 
            		 
             		
             	}
             	
         	}
 		}
 	}
 	
 	
 	
 
 
 
 }
