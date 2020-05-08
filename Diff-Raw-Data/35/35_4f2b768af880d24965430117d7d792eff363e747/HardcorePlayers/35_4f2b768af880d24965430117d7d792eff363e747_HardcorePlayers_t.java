 package au.com.addstar.truehardcore;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class HardcorePlayers {
 
 	private Map<String, HardcorePlayer> Players;
 	public HardcorePlayers() {
 		Players = new HashMap<String, HardcorePlayer>();
 	}
 	
 	public static enum PlayerState {
 		NOT_IN_GAME,
 		IN_GAME,
 		ALIVE,
 		DEAD;
 	}
 	
 	static class HardcorePlayer {
 		private String PlayerName;
 		private String World;
 		private Location LastPos;
 		private Date LastJoin = new Date();
 		private Date LastQuit = null;
 		private Date GameStart = new Date();
 		private Date GameEnd = null;
 		private Integer GameTime = 0;
 		private Integer Level = 0;
 		private float Exp = 0;
 		private Integer Score = 0;
 		private Integer TopScore = 0;
 		private PlayerState State = PlayerState.NOT_IN_GAME;
 		private String DeathMsg = "";
 		private Location DeathPos;
 		private Integer Deaths = 0;
 		
 		public Integer getDeaths() {
 			return Deaths;
 		}
 		public void setDeaths(Integer deaths) {
 			Deaths = deaths;
 		}
 		public String getPlayerName() {
 			return PlayerName;
 		}
 		public void setPlayerName(String playerName) {
 			PlayerName = playerName;
 		}
 		public String getWorld() {
 			return World;
 		}
 		public void setWorld(String world) {
 			World = world;
 		}
 		public Location getLastPos() {
 			return LastPos;
 		}
 		public void setLastPos(Location lastPos) {
 			LastPos = lastPos;
 		}
 		public Date getLastJoin() {
 			return LastJoin;
 		}
 		public void setLastJoin(Date lastJoin) {
 			LastJoin = lastJoin;
 		}
 		public Date getLastQuit() {
 			return LastQuit;
 		}
 		public void setLastQuit(Date lastQuit) {
 			LastQuit = lastQuit;
 		}
 		public Date getGameStart() {
 			return GameStart;
 		}
 		public void setGameStart(Date gameStart) {
 			GameStart = gameStart;
 		}
 		public Date getGameEnd() {
 			return GameEnd;
 		}
 		public void setGameEnd(Date gameEnd) {
 			GameEnd = gameEnd;
 		}
 		public Integer getGameTime() {
 			return GameTime;
 		}
 		public void setGameTime(Integer gameTime) {
 			GameTime = gameTime;
 		}
 		public Integer getLevel() {
 			return Level;
 		}
 		public void setLevel(Integer level) {
 			Level = level;
 		}
 		public float getExp() {
 			return Exp;
 		}
 		public void setExp(float exp) {
 			Exp = exp;
 		}
 		public Integer getScore() {
 			return Score;
 		}
 		public void setScore(Integer score) {
 			Score = score;
 		}
 		public Integer getTopScore() {
 			return TopScore;
 		}
 		public void setTopScore(Integer topScore) {
 			TopScore = topScore;
 		}
 		public PlayerState getState() {
 			return State;
 		}
 		public void setState(PlayerState state) {
 			if ((state == PlayerState.DEAD) && (State != PlayerState.DEAD)) {
 				// Player has died
 				setGameEnd(new Date());
 				setLastQuit(new Date());
 			}
 			else if ((state == PlayerState.IN_GAME) && (State != PlayerState.IN_GAME)) {
 				// Joining a game
 				if (State != PlayerState.ALIVE) {
 					// Starting a new game
 					setGameStart(new Date());
 				}
 				// Always set the join date when transitioning -> IN_GAME
 				setLastJoin(new Date());
 			}
 			else if ((State == PlayerState.IN_GAME) && (state != PlayerState.IN_GAME)) {
 				// Leaving a game (for any reason)
 				setLastQuit(new Date());
 			}
 			
 			State = state;
 		}
 		public String getDeathMsg() {
 			return DeathMsg;
 		}
 		public void setDeathMsg(String deathMsg) {
 			DeathMsg = deathMsg;
 		}
 		public Location getDeathPos() {
 			return DeathPos;
 		}
 		public void setDeathPos(Location deathPos) {
 			DeathPos = deathPos;
 		}
 		public void updatePlayer(Player player) {
 			setExp(player.getExp());
 			setLastPos(player.getLocation());
 			setGameTime(0);  // TODO: fix this
 			setScore(player.getTotalExperience());
 			setLevel(player.getLevel());
 		}
 	}
 	
 	// TODO: Add sanity checks
 	public HardcorePlayer NewPlayer(String world, String name) {
 		HardcorePlayer hcp = new HardcorePlayer();
		hcp.setPlayerName(name.toLowerCase());
 		hcp.setWorld(world);
		AddPlayer(world, name.toLowerCase(), hcp);
 		return hcp;
 	}
 
 	// TODO: Add sanity checks
 	public HardcorePlayer Get(String world, String name) {
		String key = world + "/" + name.toLowerCase();
 		HardcorePlayer hcp = Players.get(key);
 		return hcp;
 	}
 	
 	// TODO: Add sanity checks
 	public HardcorePlayer Get(World world, Player player) {
 		return Get(world.getName(), player.getName());
 	}
 
 	// TODO: Add sanity checks
 	public HardcorePlayer Get(Player player) {
 		return Get(player.getLocation().getWorld().getName(), player.getName());
 	}
 	
 	public HardcorePlayer Get(String key) {
 		return Players.get(key);
 	}
 	
 	public boolean AddPlayer(String world, String name, HardcorePlayer hcp) {
		String key = world + "/" + name.toLowerCase();
 		Players.put(key, hcp);
 		return true;
 	}
 	
 	public boolean IsHardcorePlayer(Player player) {
 		return (Get(player) != null);
 	}
 	
 	public void Clear() {
 		Players.clear();
 	}
 	
 	public Map<String, HardcorePlayer> AllRecords() {
 		return Players; 
 	}
 }
