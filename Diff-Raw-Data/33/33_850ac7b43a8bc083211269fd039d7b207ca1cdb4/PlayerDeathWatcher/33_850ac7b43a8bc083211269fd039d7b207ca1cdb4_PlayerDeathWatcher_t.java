 package com.BASeCamp.SurvivalChests;
 
 import java.util.LinkedList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 
 
 public class PlayerDeathWatcher implements Listener{
 	private BCRandomizer _owner=null;
 	private World watchworld;
 	public static LinkedList<GameTracker> _Trackers= new LinkedList<GameTracker>();
 	//public GameTracker _Tracker=null;
 	public PlayerDeathWatcher(BCRandomizer bcRandomizer,GameTracker gt,World watchworld) {
 			_owner=bcRandomizer;
 			_Trackers.add(gt);
 			
 			
 	}
 	
 	public static boolean allCaps(String strtest){
 	for(int i=0;i<strtest.length();i++){
 		
 		char currchar = strtest.charAt(i);
 		if(!Character.isUpperCase(currchar)){
 		return false;	
 		
 		}
 		
 	}
 	return true;
 	}
 	public static String capitalizeString(String string) {
 		  char[] chars = string.toLowerCase().toCharArray();
 		  boolean found = false;
 		  for (int i = 0; i < chars.length; i++) {
 		    if (!found && Character.isLetter(chars[i])) {
 		      chars[i] = Character.toUpperCase(chars[i]);
 		      found = true;
 		    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' ) { // You can add other chars here
 		      found = false;
 		    }
 		  }
 		  return String.valueOf(chars);
 		}
 	private static String FriendlizeName(String source)
 	{
 		source = source.replace("item_"," ");
 		source = source.replace('_', ' ');
 		
 		
 		
 		return allCaps(source)?capitalizeString(source):source;
 		
 	}
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event){
 		if(event instanceof EntityDamageByEntityEvent)
 		{
 			EntityDamageByEntityEvent edam = (EntityDamageByEntityEvent)event;
 		
 		
 		if(edam.getEntity() instanceof Player){
 			
 			
 			//entity is the player that is damaged.
 			Player damaged = (Player)edam.getEntity();
 			
 			if(damaged.getWorld()!=watchworld) return;
 			if(edam.getDamager() instanceof Player){
 				Player Attacker = (Player)edam.getDamager();
 				//only Player versus Player damage is reported.
 				//<Attacker> has struck you with a <Weapon> for <Damage>
 				//only report when the distance between the players is more than three blocks.
 				if(Attacker!=null){
 				if(damaged.getLocation().distance(Attacker.getLocation()) > 3){
 					damaged.sendMessage("You have been struck by " + Attacker.getDisplayName() + "!");
 				}
 				}
 			}
 			
 		}
 		}
 	}
 	public static String getFriendlyNameFor(ItemStack Item){
 		
 		if(Item==null) return "Nothing";
 		String weapon = Item.getType().name();
 		
 		ItemNamer.load(Item);
 		String gotname= ItemNamer.getName();
 		if(gotname!=null && gotname!="")
 			weapon = gotname;
 		else{
 			if(RandomData.isHead(Item)){
 				weapon = RandomData.getHeadName(Item);
 				
 			}
 			else if(RandomData.isDye(Item)){
 				weapon = RandomData.getDyeName(Item);
 			}
 		
 		
 		}
 		return FriendlizeName(weapon);
 	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		
		if(_owner.Randomcommand.getaccepting()){
			
			event.getPlayer().sendMessage(ChatColor.YELLOW + "Welcome! A game is being prepared. ");
			event.getPlayer().sendMessage(ChatColor.YELLOW + "use /joingame to join, and /spectategame to watch.");
			
			
		}
		
		
		
		
	}
	
    @EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event){
 		
 	   if(_Trackers==null || _Trackers.size() == 0) return;
 		//Bukkit.broadcastMessage(event.getEntity().getName() + " has been slain!");
 		ChatColor usecolor = ChatColor.YELLOW;
 		String usemessage = event.getDeathMessage();
 		Player dyingPlayer = event.getEntity();
 		
 	
 		
 		
 		String DyingName = dyingPlayer.getName();
 		String KillerName="";
 		Player Killer = dyingPlayer.getKiller();
 		if(Killer!=null) KillerName = Killer.getName();
 		if(dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.FALL))
 		{
 		
 		usemessage=dyingPlayer.getName()+ " Fell " + Math.round(dyingPlayer.getFallDistance()) + " Blocks to their Death." ;
 			
 		}
 		else if(dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.BLOCK_EXPLOSION)){
 			String[] explosionstrings = new String[] {
 					dyingPlayer + " blew up.",
 					dyingPlayer + "'s body parts became shrapnel",
 					dyingPlayer + " exploded.",
 					dyingPlayer + " imitated a Creeper."
 					
 			};
 			usemessage = RandomData.Choose(explosionstrings);
 		
 		}
 		else if((dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.DROWNING))){
 		String[] possiblemessages = new String[] {
 				DyingName + " Drowned.",
 				DyingName + " Forgot to come up for air",
 				DyingName + " Sleeps with the fishes"};
 		usemessage = RandomData.Choose(possiblemessages);
 		
 				
 				
 		}
 		else if((dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.SUICIDE))){
 			String[] possiblemessages = new String[]{
 					DyingName + " ended it all",
 					DyingName + " didn't even leave a suicide note",
 					DyingName + " was a casualty of their own genius",
 					DyingName + " killed their own dumb self",
 					DyingName + " committed suicide.",
 					DyingName + " cried for Celestia.",
 					DyingName + " failed to survive death"
 					
 			};
 			usemessage = RandomData.Choose(possiblemessages);
 			
 		}
 		else if((dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.POISON))){
 		String[] possiblemessages = new String[] {
 			DyingName + " threw up and then died.",
 			DyingName + " should have seen a doctor.",
 			DyingName + " couldn't find the antidote.",
 			DyingName + " died from poison."};
 			usemessage = RandomData.Choose(possiblemessages);
 		
 		}
 		else if((dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.SUFFOCATION))){
 			String[] possiblemessages = new String[] {
 			DyingName + " tried to share space with a wall. It didn't work out.",
 			DyingName + " suffocated in a wall.",
 			DyingName + " choked on their own stupidity."
 					
 			};
 			usemessage = RandomData.Choose(possiblemessages);
 		}
 		else if((dyingPlayer.getLastDamageCause().getCause().equals(DamageCause.LIGHTNING))){
 			
 			String[] possiblemessages = new String[] {
 			 DyingName + " was struck down by Celestia",
 			 DyingName + " was struck down by Luna",
 			 DyingName + " was electrocuted.",
 			 DyingName + " acted as a lightning rod",
 			 DyingName + " was struck by lightning"
 					
 			};
 			
 			
 		}
 		else if(Killer!=null){
 			//ok, get the item the Killer has.
 			if(Killer instanceof Player){
 			String weapon = getFriendlyNameFor(Killer.getItemInHand());
 			
 			
 			
 			
 			
 			String[] possiblemessages = new String[] {
 					DyingName + " met there end from " + 
 					KillerName + "'s " + weapon + ".",
 					KillerName + " and their " + weapon + " slaughtered " + DyingName,
 					DyingName + " was slain by " + KillerName + " using " + weapon,
 					KillerName + " killed " + DyingName + " with a " + weapon,
 					KillerName + " gave " + DyingName + " a closer look at their " + weapon
 			};
 			
 			
 			usemessage = RandomData.Choose(possiblemessages);
 			
 			}
 		}
 		usemessage = usemessage.replace(DyingName, ChatColor.RED + DyingName + usecolor);
 		if(KillerName.length() > 0)
 			usemessage = usemessage.replace(KillerName,ChatColor.BLUE + KillerName + ChatColor.YELLOW);
 		usemessage=usemessage + "(" + dyingPlayer.getLastDamage() + " damage)";
 				BCRandomizer.emitmessage(usemessage + dyingPlayer.getLastDamageCause().toString());
 		event.setDeathMessage(usecolor + usemessage);
 		
 		ItemStack createdhead = RandomData.getHead(DyingName);
 		Enchantment useenchant;
 		
 		EnchantmentProbability ep = new EnchantmentProbability();
 		ep.Apply(createdhead, true);
 		
 		
 		
 		event.getDrops().add(createdhead);
 		if(_Trackers!=null){
 			//if there is a Tracker, notify it of the player death. do this after. The tracker
 			//tracks the game itself.
 			//_Tracker.PlayerDeath(dyingPlayer,Killer);
 			for(GameTracker Tracker : _Trackers){
 				Tracker.PlayerDeath(dyingPlayer, Killer);
 			}
 		}
 		
 	}
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        if(_Trackers!=null)
     	   for(GameTracker Tracker : _Trackers){
     	   BCRandomizer.clearPlayerInventory(event.getPlayer());
     	   
     	   Tracker.PlayerDeath(event.getPlayer(), null);
     	   }   
        
    }
 }
