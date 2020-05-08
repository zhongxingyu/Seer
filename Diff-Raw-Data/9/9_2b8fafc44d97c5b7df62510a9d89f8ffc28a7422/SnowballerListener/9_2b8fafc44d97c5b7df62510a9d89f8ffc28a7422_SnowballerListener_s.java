 package co.networkery.uvbeenzaned;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import javax.swing.Timer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
 import org.kitteh.tag.TagAPI;
  
  public class SnowballerListener implements Listener
  {
 	 public static String pg = ChatColor.AQUA + "[Snowballer] " +  ChatColor.RESET;
 	 public static ConfigAccessor config;
 	 public static ConfigAccessor scores;
 	 public static boolean gameon = false;
 	 public static boolean timergame = false;
 	 public static List<String> teamcyan = new ArrayList<String>();
 	 public static List<String> teamlime = new ArrayList<String>();
 	 public static List<String> teamcyaninarena = new ArrayList<String>();
 	 public static List<String> teamlimeinarena = new ArrayList<String>();
 	 public static HashMap<String, Integer> hitcnts = new HashMap<String, Integer>();
 	 
 	 public SnowballerListener(JavaPlugin jp)
 	 {
 		 config = new ConfigAccessor(jp, "config.yml");
 		 scores = new ConfigAccessor(jp, "scores.yml");
 	 }
 	 
 	 @EventHandler
 	 public void playerLeave(PlayerQuitEvent event)
 	 {
 		 String pll = event.getPlayer().getName();
 		 if(gameon)
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pll);
 				}
 				checkTeamsInArena();
 				if(teamcyan.contains(pll))
 				{
 					scores.saveConfig();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 					terminateAll();
 				}
 				if(teamlime.contains(pll))
 				{
 					scores.saveConfig();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 					terminateAll();
 				}
 		 }
 		 else
 		 {
 				if(teamcyaninarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamcyaninarena.remove(pll);
 				}
 				if(teamlimeinarena.contains(pll))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pll).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pll);
 				}
 				if(teamcyan.contains(pll))
 				{
 					scores.saveConfig();
 					teamcyan.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 					terminateAll();
 				}
 				if(teamlime.contains(pll))
 				{
 					scores.saveConfig();
 					teamlime.remove(pll);
 					sendAllTeamsMsg(pg + pll + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getPlayer().teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					event.getPlayer().getInventory().clear();
 					event.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR, 1));
 					terminateAll();
 				}
 		 }
 		 if(hitcnts.containsKey(event.getPlayer().getName()))
 		 {
 			 hitcnts.remove(event.getPlayer().getName());
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void playerDeath(PlayerDeathEvent event)
 	 {
 		 String pld = event.getEntity().getName();
 		 if(gameon)
 		 {
 			    if(teamcyaninarena.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			    	teamcyaninarena.remove(pld);
 			    	event.getDrops().clear();
 			    	Bukkit.getPlayer(pld).setPlayerListName(Bukkit.getPlayer(pld).getName());
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pld);
 					event.getDrops().clear();
 					Bukkit.getPlayer(pld).setPlayerListName(Bukkit.getPlayer(pld).getName());
 				}
 				checkTeamsInArena();
 			    if(teamcyan.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getDrops().clear();
 					Bukkit.getPlayer(pld).setPlayerListName(Bukkit.getPlayer(pld).getName());
 					terminateAll();
 				}
 				if(teamlime.contains(pld))
 				{
 					scores.saveConfig();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getDrops().clear();
 					Bukkit.getPlayer(pld).setPlayerListName(Bukkit.getPlayer(pld).getName());
 					terminateAll();
 				}
 				if(hitcnts.containsKey(event.getEntity().getName()))
 				{
 					hitcnts.remove(event.getEntity().getName());
 				}
 		 }
 		 else
 		 {
 			    if(teamcyaninarena.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 			    	teamcyaninarena.remove(pld);
 			    	event.getDrops().clear();
 				}
 				if(teamlimeinarena.contains(pld))
 				{
 					scores.saveConfig();
 					Bukkit.getServer().getPlayer(pld).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					teamlimeinarena.remove(pld);
 					event.getDrops().clear();
 				}
 			    if(teamcyan.contains(pld))
 			    {
 			    	scores.saveConfig();
 			    	teamcyan.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.AQUA + "CYAN" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamcyan.size() + " players on team " + ChatColor.AQUA + "CYAN.");
 					event.getDrops().clear();
 					terminateAll();
 				}
 				if(teamlime.contains(pld))
 				{
 					scores.saveConfig();
 					teamlime.remove(pld);
 					sendAllTeamsMsg(pg + pld + " has left team " + ChatColor.GREEN + "LIME" + ChatColor.RESET +"!");
 					sendAllTeamsMsg(pg + "There are now " + teamlime.size() + " players on team " + ChatColor.GREEN + "LIME.");
 					event.getDrops().clear();
 					terminateAll();
 				}
 				if(hitcnts.containsKey(event.getEntity().getName()))
 				{
 					hitcnts.remove(event.getEntity().getName());
 				}
 				refreshAllTags();
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void playerInvClick(InventoryClickEvent event)
 	 {
 		 if(teamcyan.contains(event.getWhoClicked().getName()) || teamlime.contains(event.getWhoClicked().getName()))
 		 {
 			 if(event.getSlotType() == SlotType.ARMOR)
 			 {
 				 event.setCancelled(true); 
 			 }
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void onSnowballThrow(ProjectileLaunchEvent event)
 	 {
 		 if(gameon)
 		 {
 			 if(event.getEntityType() == EntityType.SNOWBALL)
 			 {
 				 if(event.getEntity().getShooter().getType() == EntityType.PLAYER)
 				 {
 					 if(teamcyan.contains(((Player)event.getEntity().getShooter()).getName()) || teamlime.contains(((Player)event.getEntity().getShooter()).getName()))
 					 {
 						 config.getConfig().set("snowballthrowncount", config.getConfig().getInt("snowballthrowncount") + 1);
 					 }
 				 }
 			 }
 		 }
 	 }
 	 
 	 @EventHandler
 	 public void entityDamage(EntityDamageByEntityEvent event)
 	 {
 		 if(gameon)
 		 {
 			 if(event.getEntity() instanceof Player && event.getDamager() instanceof Player)
 			 {
 				 Player plhit = (Player)event.getEntity();
 				 Player plenemy = (Player)event.getDamager();
 				 if(teamcyaninarena.contains(plhit.getName()) || teamlimeinarena.contains(plhit.getName()))
 				 {
 					 if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 					 {
 						 if(!teamcyaninarena.contains(plhit.getName()) || !teamcyaninarena.contains(plenemy.getName()))
 						 {
 							 if(!teamlimeinarena.contains(plhit.getName()) || !teamlimeinarena.contains(plenemy.getName()))
 							 {
 								 event.setCancelled(true);
 							 }
 						 }
 					 }
 				 }
 			 }
 			 if ((event.getEntity() instanceof Player && event.getDamager() instanceof Snowball))
 			 {
 				 Player plhit = (Player)event.getEntity();
 				 Snowball sb = (Snowball)event.getDamager();
 				 Player plenemy = (Player)sb.getShooter();
 				 if(plhit != plenemy)
 				 {
 					 if(teamcyaninarena.contains(plhit.getName()) || teamlimeinarena.contains(plhit.getName()))
 					 {
 						 if(teamcyaninarena.contains(plenemy.getName()) || teamlimeinarena.contains(plenemy.getName()))
 						 {
 							 if(!teamcyaninarena.contains(plhit.getName()) || !teamcyaninarena.contains(plenemy.getName()))
 							 {
 								 if(!teamlimeinarena.contains(plhit.getName()) || !teamlimeinarena.contains(plenemy.getName()))
 								 {
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 1);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 2);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 3);
 									 plhit.getWorld().playSound(plhit.getLocation(), Sound.NOTE_PIANO, 10, 4);
 									 Location locyplus1 = plhit.getLocation();
 									 locyplus1.setY(locyplus1.getY() + 1);
 									 plhit.getWorld().playEffect(locyplus1, Effect.ENDER_SIGNAL, 0);
 									 plhit.getWorld().playEffect(plhit.getLocation(), Effect.ENDER_SIGNAL, 0);
 									 plhit.teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 									 plhit.getInventory().clear();
 									 if(teamcyaninarena.contains(plhit.getName()))
 									 {
 										 teamcyaninarena.remove(plhit.getName());
 										 Rank.giveRank(plhit);
 									 }
 									 if(teamlimeinarena.contains(plhit.getName()))
 									 {
 										 teamlimeinarena.remove(plhit.getName());
 										 Rank.giveRank(plhit);
 									 }
 									 scores.getConfig().set(plhit.getName(), scores.getConfig().getInt(plhit.getName()) - 1);
 									 plhit.sendMessage(pg + "-1 point!  Your score is now " + String.valueOf(scores.getConfig().getInt(plhit.getName())) + ".");
 									 if(!hitcnts.containsKey(plenemy.getName()))
 									 {
 										 hitcnts.put(plenemy.getName(), 1);
 									 }
 									 else
 									 {
 										 hitcnts.put(plenemy.getName(), hitcnts.get(plenemy.getName()) + 1);
 									 }
 									 scores.getConfig().set(plenemy.getName(), scores.getConfig().getInt(plenemy.getName()) + 1);
 									 plenemy.sendMessage(pg + "+1 point!  Your score is now " + String.valueOf(scores.getConfig().getInt(plenemy.getName())) + ".");
 									 scores.saveConfig();
 									 sendAllTeamsMsg(pg + teamcyaninarena.size() + " " + ChatColor.AQUA + "CYAN" + ChatColor.RESET + " vs " + teamlimeinarena.size() + " " + ChatColor.GREEN + "LIME");
 									 sendAllTeamsMsg(pg + getNamewColor(plenemy) + ChatColor.RED + " snowbrawled " + getNamewColor(plhit) + ".");
 									 checkTeamsInArena();
 									 event.setCancelled(true);
 								 }
 							 }
 						 }
 					 }
 				 }
 			 }
 		 }
 	 }
 	 
 		@EventHandler
 		 public void onNameTag(PlayerReceiveNameTagEvent event)
 		 {
 			 if(teamcyan.contains(event.getNamedPlayer().getName()))
 			 {
 				 event.setTag(ChatColor.AQUA + event.getNamedPlayer().getName());
 			 }
 			 else
 			 {
 				 if(teamlime.contains(event.getNamedPlayer().getName()))
 				 {
 					 event.setTag(ChatColor.GREEN + event.getNamedPlayer().getName());
 				 }
 				 else
 				 {
 					 event.setTag(event.getNamedPlayer().getName());
 				 }
 			 }
 		 }
 		
 		@EventHandler
 		public void onNewSign(SignChangeEvent event)
 		{
 			if(event.getPlayer().isOp())
 			{
 				if(event.getLine(0).equalsIgnoreCase("[sbr]"))
 				{
 					event.setLine(0, ChatColor.AQUA + "[Snowballer]");
 					event.setLine(1, "(click here)");
 					event.setLine(2, ChatColor.BLUE + "([RANK])");
 					event.setLine(3, "(points)");
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onSignClick(PlayerInteractEvent event)
 		{
 			if(teamcyan.contains(event.getPlayer().getName()) || teamlime.contains(event.getPlayer().getName()))
 			{
 				if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
 				{
 					if(event.getClickedBlock().getType() == Material.SIGN || event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
 					{
 						Sign s = (org.bukkit.block.Sign)event.getClickedBlock().getState();
 						if(s.getLine(0).equalsIgnoreCase(ChatColor.AQUA + "[Snowballer]"))
 						{
 							s.setLine(1, event.getPlayer().getName());
 							s.setLine(2, ChatColor.BLUE + Rank.getRankName(event.getPlayer()));
 							s.setLine(3, String.valueOf(SnowballerListener.scores.getConfig().getInt(event.getPlayer().getName())));
 							s.update();
 						}
 					}
 				}
 			}
 		}
 		
 		@EventHandler
 		public void onChat(AsyncPlayerChatEvent event)
 		{
 			ChatColor cc = null;
 			if(teamcyan.contains(event.getPlayer().getName()))
 			 {
 				cc = ChatColor.AQUA;
 				event.setFormat(ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(event.getPlayer()) + ChatColor.RESET + ChatColor.GOLD + "]" + ChatColor.RESET + "<" + cc + event.getPlayer().getName() + ChatColor.RESET + "> " + event.getMessage());
 			 }
 			 if(teamlime.contains(event.getPlayer().getName()))
 			 {
 				 cc = ChatColor.GREEN;
 				 event.setFormat(ChatColor.GOLD + "[" + ChatColor.RESET + ChatColor.BOLD + ChatColor.BLUE + Rank.getRankName(event.getPlayer()) + ChatColor.RESET + ChatColor.GOLD + "]" + ChatColor.RESET + "<" + cc + event.getPlayer().getName() + ChatColor.RESET + "> " + event.getMessage());
 			 }
 		}
 		
 		public static void refreshAllTags()
 		{
 			for(String pl : teamcyan)
 			{
 				TagAPI.refreshPlayer(Bukkit.getPlayer(pl));
 			}
 			for(String pl : teamlime)
 			{
 				TagAPI.refreshPlayer(Bukkit.getPlayer(pl));
 			}
 			for(Player pl : Bukkit.getServer().getOnlinePlayers())
 			{
 				TagAPI.refreshPlayer(pl);
 			}
 		}
 		
 		public static String getNamewColor(Player p) 
 		{
 			String newname = null;
 			 if(teamcyan.contains(p.getName()))
 			 {
 				 newname = ChatColor.AQUA + p.getName();
 			 }
 			 else
 			 {
 				 if(teamlime.contains(p.getName()))
 				 {
 					 newname = ChatColor.GREEN + p.getName();
 				 }
 				 else
 				 {
 					 newname = p.getName();
 				 } 
 			 }
 			return newname + ChatColor.RESET;
 		}
 		
 		public static String getNamewColor(String p) 
 		{
 			String newname = null;
 			 if(teamcyan.contains(p))
 			 {
 				 newname = ChatColor.AQUA + p;
 			 }
 			 else
 			 {
 				 if(teamlime.contains(p))
 				 {
 					 newname = ChatColor.GREEN + p;
 				 }
 				 else
 				 {
 					 newname = p;
 				 }
 			 }
 			return newname + ChatColor.RESET;
 		}
 	 
      public static ActionListener taskPerformer = new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
          	if(timergame == true && gameon == false)
          	{
          		if(!teamcyan.isEmpty() && !teamlime.isEmpty())
          		{
 	            		sendAllTeamsMsg(pg + "Starting next round....");
 		            	randomMap();
 		            	gameon = true;
          		}
          		else
          		{
          			if(teamlime.isEmpty())
          			{
          				cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
          				cyanMsg(pg + "Stopping game and waiting for another player to join....");
          				timer.stop();
          				gameon = false;
          				timergame = false;
          			}
          			if(teamcyan.isEmpty())
          			{
          				limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
          				limeMsg(pg + "Stopping game and waiting for another player to join....");
          				timer.stop();
          				gameon = false;
          				timergame = false;
          			}
          		}
          	}
          }
          };
          
          public static Timer timer;
 	 
 	 public static void startIndependentTimerRound()
 	 {
 		 if(timergame == true && gameon == false)
 		 {
 			 if(!teamcyan.isEmpty() && !teamlime.isEmpty())
 			 {
 				 timer = new Timer(config.getConfig().getInt("timerdelay"), taskPerformer);
 				 timer.setRepeats(false);
 			     timer.start();
 			     sendAllTeamsMsg(pg + "Next round starts in " + Integer.toString(config.getConfig().getInt("timerdelay") / 1000) + " seconds!");
 			 }
 			 else
 			 {
       			if(teamlime.isEmpty())
       			{
       				cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
       				cyanMsg(pg + "Waiting for another player to join....");
       			}
       			if(teamcyan.isEmpty())
       			{
       				limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
       				limeMsg(pg + "Waiting for another player to join....");
       			}
 			 }
 		 }
 	 }
 	 
 	 public static Random r = new Random();
 	 
 	 public static void randomMap()
 	 {
 		hitcnts.clear();
 		r.setSeed(System.currentTimeMillis());
 		int mapnum = r.nextInt(config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false).size());
 		int i = 0;
 		for(String key : config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false))
 		{
 			if(mapnum == i)
 			{
 				for(String pl : teamcyan)
 				{
 					if(Bukkit.getServer().getPlayer(pl).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl));
 					teamcyaninarena.add(pl);
 					Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("teamcyanarenasides." + key)));
 				}
 				for(String pl : teamlime)
 				{
 					if(Bukkit.getServer().getPlayer(pl).getGameMode() == GameMode.CREATIVE)
 					{
 						Bukkit.getServer().getPlayer(pl).setGameMode(GameMode.SURVIVAL);
 					}
 					Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					giveSnowballs(Bukkit.getServer().getPlayer(pl));
 					teamlimeinarena.add(pl);
 					Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("teamlimearenasides." + key)));
 				}
 			}
 			i++;
 		}
 	 }
 	 
 	 public static void terminateAll()
 	 {
 		checkTeamsInArena();
 		hitcnts.clear();
 		if(teamcyan.isEmpty() || teamlime.isEmpty())
 		{
 			gameon = false;
 			timergame = false;
 			if(timer != null && timer.isRunning())
 			{
 				timer.stop();
 			}
 			teamcyaninarena.clear();
 			teamlimeinarena.clear();
   			if(teamlime.isEmpty())
   			{
   				cyanMsg(pg + "There are no players on team " + ChatColor.GREEN + "LIME " + ChatColor.RESET + "to play with.");
   				cyanMsg(pg + "Waiting for another player to join....");
   			}
   			if(teamcyan.isEmpty())
   			{
   				limeMsg(pg + "There are no players on team " + ChatColor.AQUA + "CYAN " + ChatColor.RESET + "to play with.");
   				limeMsg(pg + "Waiting for another player to join....");
   			}
 		}
 	 }
 	 
 	 public static void checkTeamsInArena()
 	 {
 		 if(gameon == true)
 		 {
 			 if(teamcyaninarena.size() == 0)
 			 {
 				 for(String pl : teamlimeinarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 for(String pl : teamcyaninarena)
 				 {
 					 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 					 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 				 }
 				 limeMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 				 cyanMsg(pg + "Team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + "wins!");
 				 teamcyaninarena.clear();
 				 teamlimeinarena.clear();
 				 for(String pl : teamlime)
 				 {
 					 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints") * teamcyan.size());
 				 }
 				 limeMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamcyan.size()) + " points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 				 cyanMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamcyan.size()) + " points for all of team" + ChatColor.GREEN + " LIME " + ChatColor.RESET + ".");
 				 if(!hitcnts.isEmpty())
 				 {
 					 String hskiller = "";
 					 int hshits = 0;
 					 for(Entry<String, Integer> e : hitcnts.entrySet())
 					 {
 						 if(e.getValue() > hshits)
 						 {
 							 hskiller = e.getKey();
 							 hshits = e.getValue();
 						 }
 					 }
 					 scores.getConfig().set(hskiller, scores.getConfig().getInt(hskiller) + hshits * hshits);
 					 sendAllTeamsMsg(pg + getNamewColor(hskiller) + " was awarded " + hshits * hshits + " points for the most player hits!");
 					 hitcnts.clear();
 				 }
 				 scores.saveConfig();
 				 config.saveConfig();
 				 gameon = false;
 			 }
 			 else
 			 {
 				 if(teamlimeinarena.size() == 0)
 				 {
 					 for(String pl : teamcyaninarena)
 					 {
 						 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					 }
 					 for(String pl : teamlimeinarena)
 					 {
 						 Bukkit.getServer().getPlayer(pl).teleport(LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation")));
 						 Bukkit.getServer().getPlayer(pl).getInventory().clear();
 					 }
 					 cyanMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 					 limeMsg(pg + "Team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + "wins!");
 					 teamcyaninarena.clear();
 					 teamlimeinarena.clear();
 					 for(String pl : teamcyan)
 					 {
 						 scores.getConfig().set(pl, scores.getConfig().getInt(pl) + config.getConfig().getInt("teampoints") * teamlime.size());
 					 }
 					 limeMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamlime.size()) + " points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 					 cyanMsg(pg + "+" + String.valueOf(config.getConfig().getInt("teampoints") * teamlime.size()) + " points for all of team" + ChatColor.AQUA + " CYAN " + ChatColor.RESET + ".");
 					 if(!hitcnts.isEmpty())
 					 {
 						 String hskiller = "";
 						 int hshits = 0;
 						 for(Entry<String, Integer> e : hitcnts.entrySet())
 						 {
 							 if(e.getValue() > hshits)
 							 {
 								 hskiller = e.getKey();
 								 hshits = e.getValue();
 							 }
 						 }
 						 scores.getConfig().set(hskiller, scores.getConfig().getInt(hskiller) + hshits * hshits);
 						 sendAllTeamsMsg(pg + getNamewColor(hskiller) + " was awarded " + hshits * hshits + " points for the most player hits!");
 						 hitcnts.clear();
 					 }
 					 scores.saveConfig();
 					 config.saveConfig();
 					 gameon = false;
 				 } 
 			 }
 			 if(timergame == true && gameon == false)
 			 {
 				 startIndependentTimerRound();
 			 }
 		 }
 	 }
 	 
 	 public static String checkPlayerLead()
 	 {
 		 String player = null;
 		 int points = 0;
 		 for(Entry<String, Integer> e : hitcnts.entrySet())
 		 {
 			 if(player == null && points == 0)
 			 {
 				 player = e.getKey();
 				 points = e.getValue();
 			 }
 			 else
 			 {
 				 if(e.getValue() > points)
 				 {
 					 player = e.getKey();
 					 points = e.getValue();
 				 }
 			 }
 		 }
 		 return player + "," + String.valueOf(points);
 	 }
 	 
 	 public static void giveSnowballs(Player pl)
 	 {
 		 for(int x = 0; x < 9; x++)
 		 {
 			 pl.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 64));
 		 }
 	 }
 	 
 	 public static void sendAllTeamsMsg(String msg)
 	 {
 		 for(String pl : teamlime)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 		 for(String pl : teamcyan)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 } 
 	 }
 	 
 	 public static void cyanMsg(String msg)
 	 {
 		 for(String pl : teamcyan)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 	 }
 	 
 	 public static void limeMsg(String msg)
 	 {
 		 for(String pl : teamlime)
 		 {
 			 Bukkit.getServer().getPlayer(pl).sendMessage(msg);
 		 }
 	 }
  }
