 package me.raino.totalwar;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.google.common.collect.Lists;
 
 public class TotalPlayer {
 
 	static List<TotalPlayer> players = Lists.newArrayList();
 
 	private final Player player;
 	private int kills;
 	private int deaths;
 
 	public TotalPlayer(Player player) {
 		this.player = player;
 	}
 
	public void login() {
		if(!players.contains(this)) players.add(this);
	}
	
	public void logout() {
		if(players.contains(this)) players.remove(this);
	}
	
 	public int getDeaths() {
 		return deaths;
 	}
 
 	public void setDeaths(int deaths) {
 		this.deaths = deaths;
 	}
 
 	public String getName() {
 		return getPlayer().getName();
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public int getKills() {
 		return kills;
 	}
 
 	public void setKills(int kills) {
 		this.kills = kills;
 	}
 
 	public static TotalPlayer getPlayer(Player player) {
 		for (TotalPlayer tp : players) {
 			if (tp.getPlayer() == player) return tp;
 		}
 		return new TotalPlayer(player);
 	}
 
 }
