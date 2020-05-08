 package net.preoccupied.bukkit.places;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.preoccupied.bukkit.PlayerCommand;
 import net.preoccupied.bukkit.TeleportQueue;
 
 
 
 public class PlacesPlugin extends JavaPlugin {
 
 
     public static final String SPAWN_NAME = "spawn";
 
     public static final String JAIL_NAME = "jail";
 
 
     private Map<String,Map<String,Place>> placesByName = null;
     private Map<String,Map<String,Place>> homesByOwner = null;
 
     private Map<String,Location> invitations = null;
     private Map<String,Location> returnLocations = null;
 
     private TeleportQueue teleportQueue = null;
 
 
 
     private void log(Object... args) {
 	StringBuilder sb = new StringBuilder();
 	for(Object o : args) {
 	    sb.append((o == null)? "[null]": o.toString());
 	    sb.append(" ");
 	}
 
 	getServer().getLogger().info(sb.toString());
     }
 
 
 
     public void onEnable() {
 	placesByName = new HashMap<String,Map<String,Place>>();
 	homesByOwner = new HashMap<String,Map<String,Place>>();
 
 	invitations = new HashMap<String,Location>();
 	returnLocations = new HashMap<String,Location>();
 
 	teleportQueue = new TeleportQueue(this);
 	teleportQueue.enable();
 
 	loadPlaces();
 
 	PluginManager pm = getServer().getPluginManager();
 	EventExecutor ee = null;
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onPlayerJoin((PlayerJoinEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.PLAYER_JOIN, null, ee, Priority.Low, this);
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onPlayerRespawn((PlayerRespawnEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.PLAYER_RESPAWN, null, ee, Priority.Low, this);
 
 	ee = new EventExecutor() {
 		public void execute(Listener ignored, Event e) {
 		    onPlayerTeleport((PlayerTeleportEvent) e);
 		}
 	    };
 	pm.registerEvent(Event.Type.PLAYER_TELEPORT, null, ee, Priority.Low, this);
 
 	setupCommands();
 
 	log(this, "is enabled");
     }
 
 
 
     public void onDisable() {
 	placesByName.clear();
 	homesByOwner.clear();
 	invitations.clear();
 	returnLocations.clear();
 	teleportQueue.disable();
 
 	log(this, "is disabled");
     }
 
 
 
     private void loadPlaces() {
 	try {
 	    for(Place p : getDatabase().find(Place.class).findList()) {
 		
 		Map<String,Map<String,Place>> storage = null;
 		Map<String,Place> ps = null;
 		
 		if(p.isHome()) {
 		    storage = homesByOwner;
 		} else {
 		    storage = placesByName;
 		}
 
 		String pworld = p.getWorld();
 		ps = storage.get(pworld);
 		if(ps == null) {
 		    ps = new HashMap<String,Place>();
 		    storage.put(pworld, ps);
 		}
 
 		ps.put(p.getName(), p);
 	    }
 	    
 	} catch(PersistenceException pe) {
 	    installDDL();
 	}
     }
 
 
 
     private void saveReturn(Player player) {
 	returnLocations.put(player.getName(), player.getLocation());
     }
 
 
 
     private Location getReturn(Player player) {
 	return returnLocations.get(player.getName());
     }
 
 
 
     private void clearReturn(Player player) {
 	returnLocations.remove(player.getName());
     }
 
 
 
     private Place getHome(Player player) {
 	return getHome(player.getLocation().getWorld().getName(), player.getName());
     }
 
 
 
     private Place getHome(String world, String owner) {
 	Map<String,Place> ps = homesByOwner.get(world);
 	if(ps == null) return null;
 	return ps.get(owner);
     }
 
 
 
     private Place getPlace(Player player, String name) {
 	return getPlace(player.getWorld().getName(), name);
     }
 
 
 
     private Place getPlace(String world, String name) {
 	Map<String,Place> ps = placesByName.get(world);
 	if(ps == null) return null;
 	return ps.get(name);
     }
 
 
     
     private Place createHome(Player p) {
 	return createHome(p.getName(), p.getLocation());
     }
 
 
 
     private Place createHome(String owner, Location spot) {
 	return createPlace(owner, spot, true);
     }
 
 
 
     private Place createPlace(String name, Location spot) {
 	return createPlace(name, spot, false);
     }
 
 
 
     private Place createPlace(String n, Location l, boolean home) {
 	Place p = new Place();
 	p.setName(n);
 	p.setSpot(l);
 	p.setEntrance(l);
 	//p.setGraveyard(l);
 	p.setHome(home);
 
 	Map<String,Map<String,Place>> storage = null;
 	if(home) {
 	    storage = homesByOwner;
 	} else {
 	    storage = placesByName;
 	}
 
 	String pworld = l.getWorld().getName();
 	Map<String,Place> ps = storage.get(pworld);
 	if(ps == null) {
 	    ps = new HashMap<String,Place>();
 	    storage.put(pworld, ps);
 	}
 	ps.put(n, p);
 
 	getDatabase().save(p);
 	return p;
     }
 
 
 
     private Place clonePlace(String name, Place original, boolean home) {
 	Place p = createPlace(name, original.getSpot(), home);
 	p.setEntrance(original.getEntrance());
 	p.setGraveyard(original.getGraveyard());
 	updatePlace(p);
 	return p;
     }
 
 
     
     private void deletePlace(Place p) {
 	Map<String,Map<String,Place>> storage = null;
 	if(p.isHome()) {
 	    storage = homesByOwner;
 	} else {
 	    storage = placesByName;
 	}
 
 	Map<String,Place> ps = storage.get(p.getWorld());
 	if(ps != null) ps.remove(p.getName());
        
 	getDatabase().delete(p);
     }
 
 
 
     private void updatePlace(Place p) {
 	getDatabase().update(p);
     }
 
 
 
     private static final String globconvert(String pattern) {
 	pattern = pattern.replace("\\","\\\\");
 	pattern = pattern.replace(".", "\\.");
 	pattern = pattern.replace("?", ".");
 	pattern = pattern.replace("*", ".*");
 	return pattern;
     }
 
 
 
     public void setupCommands() {
 	new PlayerCommand(this, "home") {
 	    public boolean run(Player p) {
 		Place home = getHome(p);
 		if(home == null) {
 		    msg(p, "You've not set your home. You may do so via the /set-home command.");
 
 		} else {
 		    saveReturn(p);
 		    teleportQueue.safeTeleport(p, home.getSpot());
 		    msg(p, "Teleporting home");
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "set-home") {
 	    public boolean run(Player p) {
 		Place home = getHome(p);
 		if(home == null) {
 		    home = createHome(p);
 		} else {
 		    home.setSpot(p.getLocation());
 		    updatePlace(home);
 		}
 
 		msg(p, "Home location has been saved.");
 		log(p.getName(), "set home on", home.getWorld(), "to", home.getSpot());
 
 		return true;
 	    }
 	};
 	
 
 	new PlayerCommand(this, "visit") {
 	    public boolean run(Player p, String n) {
 		Player friend = getServer().getPlayer(n);
 		if(friend == null) {
 		    msg(p, "Player not found:", n);
 		    return true;
 		}
 
 		saveReturn(p);
 		teleportQueue.safeTeleport(p, friend.getLocation());
 		msg(p, "Teleporting to " + n + ".");
 		msg(friend, p.getName() + " is incoming.");
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "visit-place") {
 	    public boolean run(Player p, String name) {
 		Place place = getPlace(p, name);
 		if(place == null) {
 		    msg(p, "No such place:" + name);
 
 		} else {
 		    saveReturn(p);
 		    teleportQueue.safeTeleport(p, place.getEntrance());
 		    msg(p, "Teleporting to the entrance of " + place.getDisplay());
 		}
 
 		return true;
 	    }
 
 	    public boolean run(Player p, String world, String name) {
 		Place place = getPlace(world, name);
 		if(place == null) {
 		    msg(p, "No such place in \"" + world + "\": " + name);
 
 		} else {
 		    saveReturn(p);
 		    teleportQueue.safeTeleport(p, place.getEntrance());
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "graveyard") {
 	    public boolean run(Player p) {
		Nearness near = getNearest(p);
 
 		if(near == null) {
 		    msg(p, "No nearby graveyards.");
 
 		} else {
 		    Place place = near.getPlace();
 		    saveReturn(p);
 		    teleportQueue.safeTeleport(p, place.getGraveyard());
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "invite") {
 	    public boolean run(Player p, String f) {
 		Player friend = getServer().getPlayer(f);
 		if(friend == null) {
 		    msg(p, "Player not found:", f);
 		    return true;
 		}
 
 		if(friend == p) {
 		    err(p, "You cannot invite yourself.");
 		    return true;
 		}
 		
 		Location l = p.getLocation();
 		invitations.put(friend.getName(), l);
 
 		info("player", p.getName(), "invited", friend.getName(), "to", l);
 
 		msg(p, "You have invited", friend.getName(), "to your current location.");
 		msg(friend, "You have been invited to visit", p.getName() + ".",
 		    "You may /accept their invitation to be teleported.");
 		
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "accept") {
 	    public boolean run(Player p) {
 		Location l = invitations.get(p.getName());
 
 		if(l == null) {
 		    msg(p, "You have no pending invitations.");
 
 		} else {
 		    saveReturn(p);
 		    invitations.remove(p.getName());
 		    teleportQueue.safeTeleport(p, l);
 		}
 
 		return true;
 	    }
 	};	
 
 
 	new PlayerCommand(this, "return") {
 	    public boolean run(Player p) {
 		Location r = getReturn(p);
 
 		if(r == null) {
 		    msg(p, "You have no return point stored.");
 
 		} else {
 		    clearReturn(p);
 		    teleportQueue.safeTeleport(p, r);
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "spawn") {
 	    public boolean run(Player p) {
 		Place place = getPlace(p, SPAWN_NAME);
 
 		if(place == null) {
 		    msg(p, "The spawn point has not been set for this world.");
 		    
 		} else {
 		    Location l = place.getEntrance();
 		    if(l == null) l = place.getSpot();
 
 		    saveReturn(p);
 		    teleportQueue.safeTeleport(p, l);
 		}
 
 		return true;
 	    }
 	};
 
 	
 	new PlayerCommand(this, "set-spawn") {
 	    public boolean run(Player p) {
 		Place place = getPlace(p, SPAWN_NAME);
 		Location l = p.getLocation();
 
 		if(place == null) {
 		    place = createPlace(SPAWN_NAME, l);
 		    info("created spawn place for world", place.getWorld());
 
 		} else {
 		    place.setWeight(0);
 		    place.setSpot(l);
 		    place.setEntrance(l);
 		    place.setGraveyard(l);
 		}
 
 		World w = l.getWorld();
 		w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
 		
 		msg(p, "Spawn point has been set");
 		info(p.getName(), "set spawn location on", place.getWorld(), "to", l);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "add-place") {
 	    public boolean run(Player p, String n) {
 		return run(p, n, "0");
 	    }
 
 	    public boolean run(Player p, String n, String weight) {
 		Place place = getPlace(p, n);
 		if(place != null) {
 		    msg(p, "A place with this name already exists: " + n);
 		    return true;
 		}
 		
 		int w = parseInt(p, weight, 0);
 
 		Location l = p.getLocation();
 		place = createPlace(n, l);
 		place.setWeight(w);
 		updatePlace(place);
 
 		msg(p, "Created new place: " + n);
 		info(p.getName(), "created place named", n, "at", l, "with radius", w);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "remove-place") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 		return run(p, place);
 	    }
 
 	    public boolean run(Player p, String w, String n) {
 		Place place = getPlace(w, n);
 		if(place == null) {
 		    msg(p, "No such place on " + w + ": " + n);
 		    return true;
 		}
 		return run(p, place);
 	    }
 
 	    public boolean run(Player p, Place place) {
 		deletePlace(place);
 
 		msg(p, "Deleting place #" + place.getId());
 		info(p.getName(), "deleted place", place.getName(), "#"+place.getId());
 		
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "set-place-weight") {
 	    public boolean run(Player p, String n, String weight) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		int w = 0;
 		try {
 		    w = Integer.parseInt(weight);
 		} catch(Exception e) {
 		    msg(p, "Invalid number format: " + weight);
 		    return true;
 		}
 
 		place.setWeight(w);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' weight set to: " + w);
 		info(p.getName(), "updated place", n, "radius to", w);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "set-place") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		Location l = p.getLocation();
 		place.setSpot(l);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' updated to current location.");
 		info(p.getName(), "updated place", n, "center to", l);
 		
 		return true;
 	    }
 
 	    public boolean run(Player p, String n, String weight) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		int w = 0;
 		try {
 		    w = Integer.parseInt(weight);		    
 		} catch(Exception e) {
 		    msg(p, "Invalid number format: " + weight);
 		    return true;
 		}
 
 		Location l = p.getLocation();
 		place.setSpot(l);
 		place.setWeight(w);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' updated to current location with weight " + w);
 		info(p.getName(), "updated place", n, "center to", l, "with radius", w);
 
 		return true;
 	    }
 	};
 
 	
 	new PlayerCommand(this, "set-place-entrance") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		Location l = p.getLocation();
 		place.setEntrance(l);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' entrance updated to current location.");
 		info(p.getName(), "set entrance for place", n, "to", l);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "set-place-graveyard") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		Location l = p.getLocation();
 		place.setGraveyard(l);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' graveyard updated to current location.");
 		info(p.getName(), "set graveyard for place", n, "to", l);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "clear-place-graveyard") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		place.setGraveyard(null);
 		updatePlace(place);
 
 		msg(p, "Place '" + n + "' graveyard cleared.");
 		info(p.getName(), "cleared graveyard from place", n);
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "set-place-title") {
 	    public boolean run(Player p, String n) {
 		return run(p, n, null);
 	    }
 
 
 	    public boolean run(Player p, String n, String title) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		if(title != null && title.length() == 0)
 		    title = null;
 
 		place.setTitle(title);
 		updatePlace(place);
 		
 		if(title == null) {
 		    msg(p, "Place '" + n + "' title cleared.");
 		} else {
 		    msg(p, "Place '" + n + "' title set to: " + title);
 		}
 
 		info(p.getName(), "set Place", n, "title to", title);
 
 		return true;
 	    }
 	};
 
 
 
 	new PlayerCommand(this, "list-places") {
 	    public boolean run(Player p) {
 		World world = p.getWorld();
 		Map<String,Place> places = placesByName.get(world.getName());
 
 		if(places == null) {
 		    msg(p, "No places on this world.");
 		    return true;
 		}
 
 		msg(p, "Places:");
 		for(Place place : places.values()) {
 		    msg(p, join(' ', "   #" + place.getId(), place.getName(), place.getTitle()));
 		}
 
 		return true;
 	    }	    
 
 	    public boolean run(Player p, String n) {
 		return run(p, p.getWorld().getName(), n);
 	    }
 
 	    public boolean run(Player p, String w, String n) {
 		Map<String,Place> places = placesByName.get(w);
 		
 		if(places == null) {
 		    msg(p, "No places on this world.");
 		    return true;
 		}
 
 		msg(p, "Places matching " + n);
 		n = globconvert(n);
 
 		for(Place place : places.values()) {
 		    if(place.getDisplay().matches(n) || ("#"+place.getId()).matches(n)) {
 			msg(p, join(' ', "   #" + place.getId(), place.getName(), place.getTitle()));
 		    }
 		}
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "place-info") {
 	    public boolean run(Player p, String n) {
 		Place place = getPlace(p, n);
 		if(place == null) {
 		    msg(p, "No such place: " + n);
 		    return true;
 		}
 
 		return run(p, place);
 	    }
 
 	    public boolean run(Player p, String w, String n) {
 		Place place = getPlace(w, n);
 		if(place == null) {
 		    msg(p, "No such place on " + w + ": " + n);
 		    return true;
 		}
 
 		return run(p, place);
 	    }
 
 	    public boolean run(Player p, Place place) {
 		msg(p, "Information for place:", place.getId());
 		msg(p, "  Name:", place.getName());
 		msg(p, "  Title:", place.getTitle());
 		msg(p, "  Weight:", place.getWeight());
 		msg(p, "  Center:", place.getSpot());
 		msg(p, "  Entrance:", place.getEntrance());
 		msg(p, "  Graveyard:", place.getGraveyard());
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "whereis") {
 	    public boolean run(Player p, String n) {
 		Player friend = getServer().getPlayer(n);
 		if(friend == null) {
 		    msg(p, "Player not found:", n);
 		    return true;
 		}
 
 		Nearness near = getNearest(friend.getLocation());
 		if(near == null) {
 		    msg(p, friend.getName(), "is near no places.");		    
 
 		} else {
 		    Direction dir = getDirection(friend, near);
 		    Place place = near.place;
 		    msg(p, friend.getName(), "is", dir.getDisplay(),
 			place.getDisplay() + ".");
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "whereami") {
 	    public boolean run(Player p) {
 		Nearness near = getNearest(p);
 
 		if(near == null) {
 		    msg(p, "There are no nearby places.");
 		    
 		} else {
 		    Direction dir = getDirection(p, near);
 		    Place place = near.getPlace();
 		    
 		    msg(p, "You are", dir.getDisplay(), place.getDisplay() + ".");
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "nearby") {
 	    public boolean run(Player p) {
 		err(p, "unimplemented");
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "jail") {
 	    public boolean run(Player p, String n) {
 		err(p, "unimplemented");
 		return true;
 	    }
 
 	    public boolean run(Player p, String n, String minutes) {
 		err(p, "unimplemented");
 		return true;
 	    }
 	};
 
 
 	new PlayerCommand(this, "unjail") {
 	    public boolean run(Player p, String n) {
 		err(p, "unimplemented");
 		return true;
 	    }
 	};
 	
     }
     
 
 
     private void onPlayerJoin(PlayerJoinEvent pje) {
 	Player player = pje.getPlayer();
 	Place home = getHome(player);
 
 	// we're just trying to catch new players to give them a home
 	// and put them in the correct place
 	if(home != null) return;
 
 	Place spawn = getPlace(player, SPAWN_NAME);
 	if(spawn != null) {
 	    home = clonePlace(player.getName(), spawn, true);
 	    teleportQueue.safeTeleport(player, home.getEntrance());
 
 	} else {
 	    log("player", player.getName(), "did not receive a home:",
 		"no spawn has been defined");
 	}
     }
 
     
     
     private void onPlayerRespawn(PlayerRespawnEvent pre) {
 	// look for the nearest place to player
 	// find graveyard and send them there
 
 	Player player = pre.getPlayer();
 	Location deathpoint = player.getLocation();
 	Nearness near = getNearestGraveyard(deathpoint);
 
 	if(near != null) {
 	    Place place = near.getPlace();
 	    pre.setRespawnLocation(place.getGraveyard());
 
 	    log("respawning", player.getName(), "at graveyard for place:",
 		place.getName());
 	}
     }
 
 
 
     private void onPlayerTeleport(PlayerTeleportEvent pte) {
 	Location orig = pte.getFrom();
 	Location dest = pte.getTo();
 
 	if(orig.getWorld() == dest.getWorld())
 	    return;
 
 	Player player = pte.getPlayer();
 	String dw = dest.getWorld().getName();
 
 	Place home = getHome(dw, player.getName());
 
 	// we're just trying to catch non-new players who may have
 	// teleported onto a world where they do not have a home set
 	if(home != null) return;
 	
 	Place spawn = getPlace(dw, SPAWN_NAME);
 	if(spawn != null) {
 	    home = clonePlace(player.getName(), spawn, true);
 
 	} else {
 	    log("player", player.getName(), "entered a world with no spawn:",
 		dw);
 	}
     }
 
 
 
     private int getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
 	int xd, yd, zd;
 	xd = x1 - x2;
 	yd = y1 - y2;
 	zd = z1 - z2;
 	return (int) Math.sqrt((xd * xd) + (yd * yd) + (zd * zd));
     }
 
 
 
     private int getDistance(Location a, Location b) {
 	int x1, y1, z1;
 	int x2, y2, z2;
 
 	x1 = a.getBlockX();
 	y1 = a.getBlockY();
 	z1 = a.getBlockZ();
 
 	x2 = b.getBlockX();
 	y2 = b.getBlockY();
 	z2 = b.getBlockZ();
 
 	return getDistance(x1, y1, z1, x2, y2, z2);	
     }
 
 
    
     private int getWeightedDistance(Location a, Place b) {
 	return getDistance(a, b.getSpot()) - b.getWeight();
     }
 
 
 
     private int getFlatDistance(int x1, int z1, int x2, int z2) {
 	int xd, zd;
 	xd = x1 - x2;
 	zd = z1 - z2;
 	return (int) Math.sqrt((xd * xd) + (zd * zd));
     }
 
 
 
     private int getFlatDistance(Location a, Location b) {
 	return getFlatDistance(a.getBlockX(), a.getBlockZ(),
 			       b.getBlockX(), b.getBlockZ());
     }
 
 
 
     private int getWeightedFlatDistance(Location a, Place place) {
 	return getFlatDistance(place.getSpot(), a) - place.getWeight();
     }
 
 
 
     private Nearness getNearest(Player player) {
 	return getNearest(player.getLocation());
     }
 
 
 
     private Nearness getNearest(Location l) {
 	return getNearest(l, false);
     }
 
 
 
     private Nearness getNearestGraveyard(Location l) {
 	return getNearest(l, true);
     }
 
 
 
     private Nearness getNearest(Location l, boolean require_graveyard) {
 	Place ret = null;
 	int distance = 0;
 
 	World w = l.getWorld();
 	Map<String,Place> places = placesByName.get(w.getName());
 	if(places == null) return null;
 
 	for(Place place : places.values()) {
 	    if(require_graveyard && (place.getGraveyard() == null)) {
 		continue;
 	    }
 
 	    if(ret == null) {
 		ret = place;
 		distance = getWeightedDistance(l, place);
 
 	    } else {
 		int d = getWeightedDistance(l, place);
 
 		if(distance <= 0) {
 		    // if we're already "inside" of a place
 
 		    if(d <= 0) {
 			// then we only want to consider the new place
 			// if we're inside it, too
 
 			if(place.getWeight() < ret.getWeight()) {
 			    // this place is nearer if it's smaller
 			    // than the previous
 			    distance = d;
 			    ret = place;
 			}
 		    }
 
 		} else if(d < distance) {
 		    // we aren't already inside of a place, and the
 		    // new place is closer.
 
 		    distance = d;
 		    ret = place;
 		}
 	    }
 	}
 	    
 	return (ret == null)? null: new Nearness(ret, distance);
     }
 
 
     
     private static class Nearness implements Comparable<Nearness> {
 	Place place;
 	int distance;
 	Nearness(Place place, int distance) {
 	    this.place = place;
 	    this.distance = distance;
 	}
 	public int compareTo(Nearness c) {
 	    return distance - c.distance;
 	}
 	int getDistance() {
 	    return distance;
 	}
 	Place getPlace() {
 	    return place;
 	}
     }
 
 
 
     private static enum Direction {
 	IN ("in"),
 	OUT ("out"),
 	EAST ("east of"),
 	NORTHEAST ("northeast of"),
 	NORTH ("north of"),
 	NORTHWEST ("northwest of"),
 	WEST ("west of"),
 	SOUTHWEST ("southwest of"),
 	SOUTH ("south of"),
 	SOUTHEAST ("southeast of"),
 	ABOVE ("above"),
 	BELOW ("below");
 
 	String display;
 
 	Direction(String display) {
 	    this.display = display;
 	}
 
 	String getDisplay() {
 	    return display;
 	}
 
 	Direction getReverse() {
 	    switch(this) {
 	    case IN: return OUT;
 	    case OUT: return IN;
 	    case EAST: return WEST;
 	    case NORTHEAST: return SOUTHWEST;
 	    case NORTH: return SOUTH;
 	    case NORTHWEST: return SOUTHEAST;
 	    case WEST: return EAST;
 	    case SOUTHWEST: return NORTHEAST;
 	    case SOUTH: return NORTH;
 	    case SOUTHEAST: return NORTHWEST;
 	    case ABOVE: return BELOW;
 	    case BELOW: return ABOVE;
 	    default: return OUT;
 	    }
 	}
     };
     
 
 
     private Direction getDirection(Player player, Nearness near) {
 	return getDirection(player.getLocation(), near);
     }
 
 
 
     private Direction getDirection(Location loc, Nearness near) {
 	if(near.getDistance() <= 0) {
 	    return Direction.IN;
 
 	} else {
 	    int wfd = getWeightedFlatDistance(loc, near.place);
 	    if(wfd <= 0) {
 		return getVerticalDirection(loc, near.place.getSpot());
 	    } else {
 		return getFlatDirection(loc, near.place.getSpot());
 	    }
 	}	
     }
 
 
 
     private Direction getVerticalDirection(Location a, Location b) {
 	int yd = a.getBlockY() - b.getBlockY();
 	return (yd > 0)? Direction.ABOVE: Direction.BELOW;
     }
 
 
 
     /* Direction is A relative to B, thus "A is NORTHWEST of B" */
     private Direction getFlatDirection(Location a, Location b) {
 	int xd, zd;
 	xd = b.getBlockX() - a.getBlockX();
 	zd = b.getBlockZ() - a.getBlockZ();
 
 	double deg = Math.atan2(xd, zd);
 	deg = Math.toDegrees(deg);
 
 	// TODO: now that we've got it working, we can cut out the
 	// degree conversion and do the logic on the radian values.
 
 	if(deg < 0) deg += 360.0;
 
 	if(deg < 22.5) {
 	    return Direction.EAST;
 	} else if(deg < 67.5) {
 	    return Direction.NORTHEAST;
 	} else if(deg < 112.5) {
 	    return Direction.NORTH;
 	} else if(deg < 157.5) {
 	    return Direction.NORTHWEST;
 	} else if(deg < 202.5) {
 	    return Direction.WEST;
 	} else if(deg < 247.5) {
 	    return Direction.SOUTHWEST;
 	} else if(deg < 292.5) {
 	    return Direction.SOUTH;
 	} else if(deg < 337.5) {
 	    return Direction.SOUTHEAST;
 	} else {
 	    // if(deg <= 360)
 	    return Direction.EAST;
 	}
     }
 
 
 
     public List<Class<?>> getDatabaseClasses() {
 	List<Class<?>> list = new ArrayList<Class<?>>();
 	list.add(Place.class);
 	return list;
     }
 
 
 }
 
 
 
 /* The end. */
