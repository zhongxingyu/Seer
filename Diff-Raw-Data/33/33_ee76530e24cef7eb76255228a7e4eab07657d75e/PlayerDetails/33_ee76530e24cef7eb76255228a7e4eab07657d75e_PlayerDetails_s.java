 package com.theminequest.MineQuest.Player;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Configuration.PropertiesFile;
 import com.theminequest.MineQuest.Quest.Quest;
 
 import com.theminequest.MineQuest.PlayerEvent.ExpEvent;
 
 /**
  * Extra details about the Player
  * 
  * @author MineQuest
  * 
  */
 public class PlayerDetails {
 
 	private long quest;
 	private long team;
 	private PropertiesFile playerspecs;
 	private Player player;
 
 	public PlayerDetails(Player p) {
 		quest = -1;
 		team = -1;
 		player = p;
 		// get all these details...
 		playerspecs = new PropertiesFile(Bukkit.getPluginManager()
 				.getPlugin("MineQuest").getDataFolder()
 				+ "/players/" + p.getName() + ".properties");
 	}
 	
 	public long getQuest(){
 		return quest;
 	}
 	
 	public void setQuest(long q){
 		quest = q;
 	}
 	
 	public long getTeam(){
 		return team;
 	}
 	
 	public void setTeam(long t){
 		team = t;
 	}
 	
 	public void modifyExperienceBy(int e){
 		long currentexp = playerspecs.getLong("experience");
 		currentexp+=e;
 		playerspecs.setLong("experience", currentexp);
 		playerspecs.save();
 		ExpEvent event = new ExpEvent(player, e);
 		Bukkit.getPluginManager().callEvent(event);
 	}
 	
 	public void modifyManaBy(int mana){
 		long currentmana = playerspecs.getLong("mana");
 		currentmana+=mana;
 	}
 
 }
