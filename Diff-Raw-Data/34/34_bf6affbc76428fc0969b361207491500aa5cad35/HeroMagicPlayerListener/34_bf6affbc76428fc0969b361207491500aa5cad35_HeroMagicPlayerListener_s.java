 package com.bukkit.Dgco.HeroMagic;
 
 import java.io.*;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.block.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.*;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockEvent;
 //import org.bukkit.event.block.BlockRightClickEvent;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.*;
 import org.bukkit.material.MaterialData;
 import org.bukkit.*;
 
 
 
 
 
 /**
  * Handle events for all Player related events
  * @author Dgco
  */
 public class HeroMagicPlayerListener extends PlayerListener 
 {
     private final HeroMagic plugin;
     protected HashMap<String,Long> lastcast;
 
     public HeroMagicPlayerListener(HeroMagic instance) 
     {
         plugin = instance;
         lastcast = new HashMap<String,Long>();
     }
     
     public boolean onPlayerCommand(CommandSender  sender,  
   		  Command  command,  
 		  String  label,  
 		  String[]  args) 
     {
  	  
  	   
  		if (command.getName().toString().equalsIgnoreCase("cast")) 
  		{
  			
  			if(args.length >0 && args[0].equalsIgnoreCase("blink"))
  			{
  				if(castBlink(sender,command,args))
  				{
  					//spell worked if here
  				}
  			}
  			if(args.length >0 && args[0].equalsIgnoreCase("cost"))
  			{
  				if(castCost(sender,command,args))
  				{
  					//spell worked if here :D
  				}
  			}
  			
  			if(args.length >0 && args[0].equalsIgnoreCase("mark"))
  			{
  				setPlayerMark( ((Player) sender), ((Player) sender).getLocation());
  			}
  			
  			if(args.length >0 && args[0].equalsIgnoreCase("recall"))
  			{
  				return castRecall((Player) sender);
  			}
  			if(args.length >0 && args[0].equalsIgnoreCase("gate"))
  			{
  				if(canCastSpell((Player) sender,"Gate"))
  				{
  					if(isOnCooldown((Player) sender,"Gate",1800))
  					{
  						sender.sendMessage("Spell Is On Cooldown");
  					} else if (!removeRegents((Player) sender,getSpellCost("Gate"))) {
 		    			sender.sendMessage("No Regents");
  		    			return false;
  		    		} else {
  		    			Location loc = ((Player) sender).getWorld().getSpawnLocation();
  		    			((Player) sender).teleport(loc);
  		    			startCooldown((Player) sender,"Gate",600);
  		    			
  		    			return true;
  		    		}
  				}
  			}
  			return true;
  		}
  		return false;
     }
  	
     public boolean castRecall(Player player)
     {
     	if(canCastSpell(player,"Recall"))
     	{
     		
     		if (isOnCooldown(player,"Recall",600)) {
     			player.sendMessage("Spell Is On Cooldown");
     			return false;
     		} else if (!removeRegents(player,getSpellCost("Recall"))) {
    			player.sendMessage("No Regents");
     			return false;
     		} else {
     			Location loc = getPlayerMark(player);
     			player.teleport(loc);
     			startCooldown(player,"Recall",600);
     			
     			return true;
     		}
     	}
     	return false;
     }
     
     
     private boolean castCost(CommandSender sender, Command command,
 			String[] args) {
     	Player player = (Player) sender;
    	player.chat("You have checked a spell cost :D");
     	
     	if(canCastSpell(player,"Cost") && args.length >= 2)
     	{
     		int[] rh = getSpellCost(args[1]);
     		if(rh != null)
     		{
     			player.sendMessage("The Spell " + args[1] + " Costs " + rh[1] + " " + rh[0] +
     				" And " + rh[3] + " " + rh[2]);
     		} else {
     			player.sendMessage("You should NOT get this message :O");
     		}
     		
     	}
 		
 		return true;
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event)
     {
     	event.getAction();
 		if (event.getAction().equals(Action.LEFT_CLICK_AIR))
     	{
    		event.getPlayer().sendMessage("Left Clicked in Air :D");
     	}
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
 		{
			event.getPlayer().sendMessage("Right Clicked Block :D");
 			if(event.getClickedBlock().getTypeId() == 47)
 			{
				event.getPlayer().sendMessage("Clicked ona book shelf");
 				String spellname = getSpellLocations().get(event.getClickedBlock().getLocation());
 				if(spellname != null)
 				{
					event.getPlayer().sendMessage("SPELL LEARNEDZOMG");
 					addSpell(event.getPlayer(),spellname);
 				}
 			}
 		}
     	return;
     }
     public void onPlayerAnimation(PlayerAnimationEvent event)
     {
     	event.getAnimationType();
 		if(event.getAnimationType().equals(PlayerAnimationType.ARM_SWING))
     	{
 			//event.getPlayer().sendMessage("arm swung");
     	}
 		
 		
     }
     
     
     
     
     @SuppressWarnings("unused")
 	public boolean castBlink(CommandSender sender,Command command,String[] args)
     {
     	Player player = (Player) sender;
     	player.chat("You have blinked :D");
     	
     	if(canCastSpell(player,"Blink"))
     	{
     		//old magic spells code
     		Block target = player.getTargetBlock(null, 20); //second int is max range
     		BlockFace face = target.getFace(player.getLocation().getBlock());
     		
     		if (target == null) {
     			player.sendMessage("Too Far To Blink");
     			return false;
    		} else if (80 > 0 && getDistance(player,target) > 80) {
    			player.sendMessage("Too Far To Blink b");
     			return false;
     		} else if (60 > 0 && isOnCooldown(player,"Blink",60)) {
     			player.sendMessage("Spell Is On Cooldown");
     			return false;
     		}  else if (player.getWorld().getBlockTypeIdAt(target.getX(),target.getY()+1,target.getZ()) == 0 && player.getWorld().getBlockTypeIdAt(target.getX(),target.getY()+2,target.getZ()) == 0) {
     			// teleport to top of target block if possible
     			if (!removeRegents(player,getSpellCost("Blink"))) {
        			player.sendMessage("No Regents");
         			return false;
         		}
     			player.sendMessage("You Cast Blink!");
     		//	sendMessageToPlayersInRange(player,STR_CAST_OTHERS.replace("[caster]",player.getName()));
     			player.teleport(new Location(player.getWorld(), target.getX()+.5, (double)target.getY()+1, target.getZ()+.5 ,player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch()  ));
     			//if (COOLDOWN > 0) {
     				startCooldown(player,"Blink",60);
     			//}
     			return true;
     		} else if (target.getTypeId() == 0 && player.getWorld().getBlockTypeIdAt(face.getModX(),face.getModY()+1,face.getModZ()) == 0) {
     			// otherwise teleport to face of target block
     			if (!removeRegents(player,getSpellCost("Blink"))) {
        			player.sendMessage("No Regents");
         			return false;
         		}
     			player.sendMessage("You cast blink");
     			//sendMessageToPlayersInRange(player,STR_CAST_OTHERS.replace("[caster]",player.getName()));
     			player.teleport(new Location(player.getWorld(),face.getModX()+.5,(double)face.getModY(),face.getModZ()+.5,player.getEyeLocation().getYaw(), player.getEyeLocation().getPitch()));
     			//if (COOLDOWN > 0) {
     				startCooldown(player,"Blink",60);
     			//}
     			return true;
     		} else {
     			// no place to stand
    			player.sendMessage("No place to stand :/");
     			return false;
     		}
 	
     		
     		
     	} 
     	return false;
     }
     
     
     private void startCooldown(Player player, String spellname, int cooldowntime)
     {
     	String str = player.getName()+spellname;
     	if(!lastcast.containsKey(str))
     	{
     		lastcast.put(str, System.currentTimeMillis());
     	}
     }
     private boolean isOnCooldown(Player player, String spellname, int cooldown) {
 		if (lastcast == null) {
 			return false;
 		}/* else if (player.isInGroup(FREE_SPELL_RANK)) {
 			return false;
 		}*/ else if (!lastcast.containsKey(player.getName()+spellname)) {
 			return false;
 		} else if (System.currentTimeMillis() - lastcast.get(player.getName()+spellname) > cooldown*1000) {
 			lastcast.remove(player.getName()+spellname);
 			return false;
 		} else {
 			return true;
 		}
 	}
     
     private boolean removeRegents(Player player, int[] regents)
     {
     	
     	Inventory inv = player.getInventory();
     	ItemStack[] items = inv.getContents();
     	int counter=0;
     	int holdi =-1;
     	int holdj =-1;
     	
     	
     	for(int i =0; i < items.length;i++)
     	{
     		if(items[i].getTypeId() == regents[0] && items[i].getAmount() >= regents[1])
     		{
     			holdi = i;
     			counter++;
     			break;
     		}
     	}
     	if(counter == 0 && regents[1] == 0)
     	{
     		counter++;
     	}
     	for(int j =0; j< items.length;j++)
     	{
     		if(items[j].getTypeId() == regents[2] && items[j].getAmount() >= regents[3])
     		{
     			holdj = j;
     			counter++;
     			break;
     		}
     	}
     	if(counter == 1 && regents[3] == 0 && holdj == -1)
     	{
     		counter++;
     	}
     	if(counter >=2)
     	{
     		if(holdi != -1) items[holdi].setAmount(items[holdi].getAmount() - regents[1]);
     		if(holdj != -1) items[holdj].setAmount(items[holdj].getAmount() - regents[3]);
     		return true;
     	}
     	
     	
     	
 		return false;
     }
 
  
     public boolean canCastSpell(Player player, String spellname)
     {
     	return true;
     }    
     public int[] getSpellCost(String spellname)
     {
     	int[] spellcost = {0,0,0,0};
     	spellcost[0]=331;
     	spellcost[1]=0;
     	spellcost[2]=3;
     	spellcost[3]=1;
     	
     	return spellcost;
     }  
     public void addSpell(Player player, String spellname)
     {
     	
     }
     public void removeSpell(Player player, String spellname)
     {
     	
     }
     public int getSpellCooldown(String spellname)
     {
     	return 30;
     }
     public HashMap<Location,String> getSpellLocations()
     {
     	Location temploc = new Location(plugin.getServer().getWorld("world"), 100, 68, 90);
     	HashMap<Location,String> map = new HashMap<Location,String>();
     	map.put(temploc, "Blink");
     	return map;
     	
     }
     public void setPlayerMark(Player player,Location loc)
     {
     	
     }
     public Location getPlayerMark(Player player)
     {
     	return new Location(plugin.getServer().getWorld("world"),101,68,88);
     }
     
 	public double getDistance(Player pl, Block tg) {
 		Location player = pl.getLocation();
 		Location target = tg.getLocation();
 		return Math.sqrt(Math.pow(player.getX()-target.getX(),2) + Math.pow(player.getY()-target.getY(),2) + Math.pow(player.getZ()-target.getZ(),2));
 	}
 
 }
 
