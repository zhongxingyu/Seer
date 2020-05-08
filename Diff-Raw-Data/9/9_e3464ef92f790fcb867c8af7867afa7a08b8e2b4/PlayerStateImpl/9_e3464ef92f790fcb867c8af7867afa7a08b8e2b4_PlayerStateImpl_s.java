 /**
  * 
  */
 package org.morganm.heimdall.player;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.morganm.heimdall.Heimdall;
 import org.morganm.heimdall.log.GriefLog;
 import org.morganm.heimdall.util.Debug;
 import org.morganm.heimdall.util.PermissionSystem;
 
 /**
  * @author morganm
  *
  */
 public class PlayerStateImpl implements PlayerState {
 	private final transient Heimdall plugin;  
 	private final transient PermissionSystem permSystem;  
 	private final String name;
 	private float griefPoints=0;
 	private float antiGriefPoints=0;
 	/* We track pointsByOwner so that if a player friends another player (after they've accumulated
 	 * some grief as a result of breaking others players blocks, for example), we can subtract the
 	 * points from the player that are owned by the new friend.
 	 * 
 	 */
 	private Map<String, Float> pointsByOwner;
 	
 	private final transient File dataFile;
 	private final transient PlayerStateManager playerStateManager;
 	private transient GriefLog griefLog;
 	private transient YamlConfiguration dataStore;
 	private final transient FriendTracker friendTracker;
 	
 	public PlayerStateImpl(final Heimdall plugin, final String name, final PlayerStateManager playerStateManager) {
 		this.plugin = plugin;
 		this.permSystem = this.plugin.getPermissionSystem();
 		this.name = name;
 		this.playerStateManager = playerStateManager;
 		this.dataFile = new File("plugins/Heimdall/playerData/"+name.toLowerCase()+".yml");
 		this.friendTracker = this.plugin.getFriendTracker();
 	}
 	
 	@Override
 	public String getName() { return name; }
 
 	@Override
 	public float incrementGriefPoints(final float f, final String owner) {
 		// we used to let griefPoints go negative before we had the notion of
 		// antiGriefPoints. So if we have negative griefPoints already, reset
 		// to baseline of 0.
 		if( griefPoints < 0 )
 			griefPoints=0;
 		
 		if( f > 0 )
 			griefPoints += f;
 		else
 			antiGriefPoints += f;
 		
 		Debug.getInstance().debug("incrementGriefPoints(player = "+name+") points="+f+", owner="+owner);
 		
 		// track owner points, if owner was given
 		if( owner != null && griefPoints > 0 ) {
 			if( pointsByOwner == null )
 				pointsByOwner = new HashMap<String, Float>();
 			
 			Float ownerPoints = pointsByOwner.get(owner);
 			if( ownerPoints == null )
 				ownerPoints = Float.valueOf(f);
 			else
 				ownerPoints += f;
 			pointsByOwner.put(owner, ownerPoints);
 		}
 		
 		return griefPoints;
 	}
 
 	@Override
 	public float getGriefPoints() {
 		return griefPoints;
 	}
 	
 	public float getAntiGriefPoints() {
 		return antiGriefPoints;
 	}
 
 	@Override
 	public boolean isExemptFromChecks() {
 		List<String> exemptPerms = plugin.getConfig().getStringList("exemptPermissions");
 		if( exemptPerms != null ) {
 			for(String perm : exemptPerms) {
 				if( permSystem.has(name, perm) )
 					return true;
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
 	public boolean isFriend(PlayerState p) {
 		return friendTracker.isFriend(name, p.getName());
 	}
 
 	@Override
 	public float getPointsByOwner(PlayerState p) {
 		return pointsByOwner.get(p.getName());
 	}
 	
 	@Override
 	public void clearPointsByOwner(PlayerState p) {
		griefPoints -= getPointsByOwner(p);
		pointsByOwner.remove(p);
 	}
 
 	@Override
 	public GriefLog getGriefLog() {
 		if( griefLog == null ) {
 			File logFile = new File("plugins/Heimdall/griefLog/"+name.toLowerCase()+".dat");
 			griefLog = new GriefLog(plugin, logFile);
 		}
 		
 		return griefLog;
 	}
 	
 	public void close() throws Exception {
 		save();
 		if( griefLog != null ) {
 			griefLog.close();
 			griefLog = null;
 		}
 		playerStateManager.removePlayerState(this);
 	}
 	
 	public void save() throws IOException {
 		// make parent directories if necessary
 		if( !dataFile.exists() ) {
 			File path = new File(dataFile.getParent());
 			if( !path.exists() )
 				path.mkdirs();
 		}
 		
 		// don't do anything if nothing to record
 		if( griefPoints == 0 && antiGriefPoints == 0 && pointsByOwner == null )
 			return;
 		
 		if( dataStore == null )
 			dataStore = new YamlConfiguration();
 		
 		dataStore.set("griefPoints", griefPoints);
 		dataStore.set("antiGriefPoints", antiGriefPoints);
 		
 		if( pointsByOwner != null ) {
 			for(Map.Entry<String, Float> entry : pointsByOwner.entrySet()) {
 				dataStore.set("pointsByOwner."+entry.getKey(), entry.getValue());
 			}
 		}
 		
 		dataStore.set("friends", friendTracker.getFriends(name));
 		dataStore.set("notFriends", friendTracker.getNotFriends(name));
 		
 		Set<String> possibleFriends = friendTracker.getAllPossibleFriends(name);
 		for(String friend : possibleFriends) {
 			float points = friendTracker.getFriendPoints(name, friend);
 			if( points > 0 )
 				dataStore.set("possibleFriend."+friend, friendTracker.getFriendPoints(name, friend));
 		}
 
 		dataStore.save(dataFile);
 	}
 	
 	public void load() throws IOException, InvalidConfigurationException {
 		// if there's no data file, do nothing
 		if( !dataFile.exists() )
 			return;
 		
 		if( dataStore == null )
 			dataStore = YamlConfiguration.loadConfiguration(dataFile);
 		else
 			dataStore.load(dataFile);
 		
 		griefPoints = (float) dataStore.getDouble("griefPoints");
 		antiGriefPoints = (float) dataStore.getDouble("antiGriefPoints");
 		
 		if( pointsByOwner == null )
 			pointsByOwner = new HashMap<String, Float>();
 		ConfigurationSection section = dataStore.getConfigurationSection("pointsByOwner");
 		if( section != null ) {
 			Set<String> owners = section.getKeys(false);
 			if( owners != null ) {
 				for(String owner : owners) {
 					pointsByOwner.put(owner, Float.valueOf((float) section.getDouble(owner)));
 				}
 			}
 		}
 		
 		List<String> friends = dataStore.getStringList("friends");
 		for(String friend : friends) {
 			friendTracker.setFriend(name, friend);
 		}
 		
 		List<String> notFriends = dataStore.getStringList("notFriends");
 		for(String notFriend : notFriends) {
 			friendTracker.setNotFriend(name, notFriend);
 		}
 		
 		section = dataStore.getConfigurationSection("possibleFriend");
 		if( section != null ) {
 			Set<String> possibleFriends = section.getKeys(false);
 			if( possibleFriends != null ) {
 				for(String friend : possibleFriends) {
 //					Debug.getInstance().debug("PlayerStateImpl::load possibleFriend = ",friend);
 					float points = 0;
 					Double d = section.getDouble(friend);
 					if( d != null )
 						points = (float) d.doubleValue();
 					friendTracker.setFriendPoints(name, friend, points);
 				}
 			}
 		}
 	}
 }
