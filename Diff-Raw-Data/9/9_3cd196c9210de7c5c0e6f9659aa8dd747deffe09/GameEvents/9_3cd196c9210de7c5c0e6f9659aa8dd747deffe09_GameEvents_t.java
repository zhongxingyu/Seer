 package com.bendude56.hunted.games;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 
 import com.bendude56.hunted.ManhuntPlugin;
 import com.bendude56.hunted.ManhuntUtil;
 import com.bendude56.hunted.chat.ChatManager;
 import com.bendude56.hunted.games.Game.GameStage;
 import com.bendude56.hunted.teams.TeamManager.Team;
 
 public class GameEvents
 {
 	private Game game;
 	private World world;
 	private int schedule;
 	
 	private Long start_setup_tick;
 	private Long start_hunt_tick;
 	private Long stop_hunt_tick;
 	
 	private Long start_timechange;
 	private Long stop_timechange;
 	
 	private Integer countdown;
 	private Integer dayCount;
 	private GameStage stage;
 	private ChatColor color = ChatColor.BLUE;
 	
 	/**
 	 * This instanced class is meant to assist the Game class by handling the
 	 * tedious, scheduled task of handling timed events.
 	 * @param game
 	 */
 	public GameEvents(Game game)
 	{
 		this.game = game;
 		this.world = game.getPlugin().getWorld();
 		
 		this.start_setup_tick = game.getStageStartTick(GameStage.SETUP);
 		this.start_hunt_tick = game.getStageStartTick(GameStage.HUNT);
 		this.stop_hunt_tick = game.getStageStopTick(GameStage.HUNT);
 		
 		this.start_timechange = world.getFullTime() + 100; //Start changing the time 10 seconds after pregame starts
 		this.stop_timechange = start_setup_tick - 400; //And stop 10 seconds before the game starts
 		
 		this.stage = GameStage.PREGAME;
 		this.countdown = 25;
 		this.dayCount = stop_hunt_tick > 0 ? game.getPlugin().getSettings().DAY_LIMIT.value : -1;
 		
 		schedule = Bukkit.getScheduler().scheduleSyncRepeatingTask(ManhuntPlugin.getInstance(), new Runnable()
 		{
 			public void run()
 			{
 				onTick();
 			}
 		}, 0, 1);
 		
 	}
 	
 	private void onTick()
 	{
 		Long time = world.getFullTime();
 		int sec = countdown*20;
 		
 		if (time > start_timechange && time < stop_timechange)
 		{
 			if (time < start_timechange + 3000)
 			{
 				world.setFullTime((long) (time + ((time - start_timechange) * 0.1)));
 			}
 			else if (time < stop_timechange - 3000)
 			{
 				world.setFullTime(time + 300);
 			}
 			else if (time < stop_timechange - 4)
 			{
 				world.setFullTime(time + ((stop_timechange - time) / 10));
 			}
 			else
 			{
 				world.setFullTime(stop_timechange);
 			}
 		}
 		
 		if (stage == GameStage.PREGAME) //---------------- PREGAME STAGE --------------------
 		{
 			if (countdown == 25)
 			{
 				broadcast(ChatManager.bracket1_ + "The Manhunt game " + color + "is about to start!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 20;
 			}
 			else if (countdown == 20 & time > start_setup_tick - sec)
 			{
 				world.setFullTime(start_setup_tick - 400);
 				countdown = 17;
 			}
 			else if (countdown == 17 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + color + "PREPARE FOR TELEPORT" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY);
 				countdown = 13;
 			}
 			else if (countdown == 13 && time > start_setup_tick - sec)
 			{
 				game.getPlugin().getTeams().removeOfflinePlayers();
				if (game == null)
				{
					return;
				}
 				
 				prepareAllPlayers();
 				if (game.getPlugin().getSettings().SETUP_TIME.value <= 0)
 				{
 					stage = GameStage.SETUP;
 				}
 				countdown = 10;
 			}
 			else if (countdown == 10 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "10" + ChatManager.color + " seconds.", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5;
 			}
 			else if (countdown == 5 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 4;
 			}
 			else if (countdown == 4 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 3;
 			}
 			else if (countdown == 3 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 2;
 			}
 			else if (countdown == 2 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 1;
 			}
 			else if (countdown == 1 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				stage = GameStage.SETUP;
 				countdown = 100;
 			}
 		}
 		else if (stage == GameStage.SETUP) //---------------- SETUP STAGE --------------------
 		{
 			if (countdown == 100 && time > start_setup_tick)
 			{
 				if (time < start_hunt_tick - 1200)
 				{
 					broadcast(ChatManager.bracket1_ + "You have " + color + game.getPlugin().getSettings().SETUP_TIME + " minutes" + ChatManager.color + " to prepare for nightfall!" + ChatManager.bracket2_, Team.PREY);
 					broadcast(ChatManager.bracket1_ + "The hunt will start in " + color + game.getPlugin().getSettings().SETUP_TIME + " minutes" + ChatManager.color + "." + ChatManager.bracket2_, Team.HUNTERS, Team.SPECTATORS);
 				}
 				game.freeze_prey = false;
 				game.freeze_hunters = false;
 				countdown = 60;
 			}
 			else if (countdown == 60 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + "The hunt will start in " + color + "1 minute" + ChatManager.color + "." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 30;
 			}
 			else if (countdown == 30 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "30" + ChatManager.color + " seconds...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 18;
 			}
 			else if (countdown == 18 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + color + "PREPARE FOR TELEPORT" + ChatManager.bracket2_, Team.HUNTERS);
 				countdown = 13;
 			}
 			else if (countdown == 13 && time > start_hunt_tick - sec)
 			{
 				game.getPlugin().getTeams().removeOfflinePlayers();
				if (game == null)
				{
					return;
				}
 				
 				//TELEPORT HUNTERS TO HUNTER SPAWN
 				ManhuntUtil.sendTeamToLocation(Team.HUNTERS, game.getPlugin().getSettings().SPAWN_HUNTER.value.clone());
 				game.freeze_hunters = true;
 				
 				countdown = 10;
 			}
 			else if (countdown == 10 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "10" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 9;
 			}
 			else if (countdown == 9 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "9" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 8;
 			}
 			else if (countdown == 8 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "8" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 7;
 			}
 			else if (countdown == 7 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "7" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 6;
 			}
 			else if (countdown == 6 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "6" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5;
 			}
 			else if (countdown == 5 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 4;
 			}
 			else if (countdown == 4 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 3;
 			}
 			else if (countdown == 3 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 2;
 			}
 			else if (countdown == 2 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 1;
 			}
 			else if (countdown == 1 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				stage = GameStage.HUNT;
 				countdown = 100;
 			}
 		}
 		else if (stage == GameStage.HUNT)
 		{
 			if (countdown == 100 && time > start_hunt_tick)
 			{
 				broadcast(ChatManager.bracket1_ + color + "THE HUNT HAS STARTED! LET THE GAMES BEGIN!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				game.freeze_prey = false;
 				game.freeze_hunters = false;
 				countdown = 90;
 			}
 			else if (stop_hunt_tick > 0)
 			{
 				if (countdown == 90 && time > stop_hunt_tick - 12000) //1 day
 				{
 					broadcast(ChatManager.bracket1_ + "The game will end at " + color + "SUNDOWN" + ChatManager.color + "..." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 60;
 				}
 				else if (countdown == 60 && time > stop_hunt_tick - sec) //1 minute
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "1 minute" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 30;
 				}
 				else if (countdown == 30 && time > stop_hunt_tick - sec) //30 sec
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "30 seconds" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 10;
 				}
 				else if (countdown == 10 && time > stop_hunt_tick - sec) //10 sec
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "10" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 9;
 				}
 				else if (countdown == 9 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "9" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 8;
 				}
 				else if (countdown == 8 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "8" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 7;
 				}
 				else if (countdown == 7 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "7" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 6;
 				}
 				else if (countdown == 6 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "6" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 5;
 				}
 				else if (countdown == 5 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 4;
 				}
 				else if (countdown == 4 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 3;
 				}
 				else if (countdown == 3 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 2;
 				}
 				else if (countdown == 2 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					countdown = 1;
 				}
 				else if (countdown == 1 && time > stop_hunt_tick - sec)
 				{
 					broadcast(ChatManager.color + "The game will end in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					stage = GameStage.DONE;
 					countdown = 100;
 				}
 			}
 			
 			if (dayCount < 0) // Day limit is off, so just add up the days left
 			{
 				if (time > start_hunt_tick + (Math.abs(dayCount)*24000))
 				{
 					broadcast(ChatManager.bracket1_ + "We are " + ChatColor.BLUE + (-dayCount) + (dayCount == -1 ? " day" : " days") + ChatManager.color + " into the hunt." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					dayCount--;
 				}
 			}
 			else if (dayCount > 1)
 			{
 				if (time > stop_hunt_tick - (dayCount*24000))
 				{
 					broadcast(ChatManager.bracket1_ + "There are " + ChatColor.BLUE + dayCount + " days" + ChatManager.color + " left in the hunt." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 					dayCount--;
 				}
 			}
 		}
 		else if (stage == GameStage.DONE) //---------------- GAME IS FINISHED --------------------
 		{
 			if (time > stop_hunt_tick)
 			{
 				game.stopGame(true);
 				close();
 			}
 		}
 	}
 
 	private void broadcast(String message, Team...team)
 	{
 		GameUtil.broadcast(message, team);
 	}
 
 	private void prepareAllPlayers()
 	{
 		//TELEPORT PLAYERS TO THEIR SPAWN POINTS
 		//AND SET THEIR INVENTORIES
 		//AND MAKE SPECTATORS INVISIBLE
 		//AND FILL THEIR FOOD AND HEALTH BARS
 		
 		List<Player> hunters = game.getPlugin().getTeams().getTeamPlayers(Team.HUNTERS);
 		for (Player p : hunters)
 		{
 			GameUtil.prepareForGame(p);
 		}
 		ManhuntUtil.sendTeamToLocation(Team.HUNTERS, game.getPlugin().getSettings().SETUP_TIME.value > 0 ? game.getPlugin().getSettings().SPAWN_SETUP.value.clone() : game.getPlugin().getSettings().SPAWN_HUNTER.value.clone());
 		
 		List<Player> prey = game.getPlugin().getTeams().getTeamPlayers(Team.PREY);
 		for (Player p : prey)
 		{
 			GameUtil.prepareForGame(p);
 		}
 		ManhuntUtil.sendTeamToLocation(Team.PREY, game.getPlugin().getSettings().SPAWN_PREY.value.clone());
 		
 		List<Player> spectators = game.getPlugin().getTeams().getTeamPlayers(Team.SPECTATORS);
 		for (Player p : spectators)
 		{
 			GameUtil.prepareForGame(p);
 			GameUtil.makeInvisible(p);
 		}
 		
 		for (Entity e : world.getEntities())
 		{
 			if (ManhuntUtil.isHostile(e))
 			{
 				e.remove();
 			}
 			if (ManhuntUtil.isPassive(e))
 			{
 				e.remove();
 			}
 			if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.PRIMED_TNT || e instanceof Projectile)
 			{
 				e.remove();
 			}
 		}
 		
 		game.freeze_prey = true;
 		game.freeze_hunters = true;
 		
 		broadcast(ChatManager.bracket1_ + color + "All players are in position" + ChatManager.bracket2_, Team.SPECTATORS);
 	}
 
 	public void close()
 	{
 		game = null;
 		Bukkit.getScheduler().cancelTask(schedule);
 	}
 
 }
