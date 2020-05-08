 package ca.gibstick.discosheep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class DiscoSheep extends JavaPlugin {
 
 	static final String PERMISSION_PARTY = "discosheep.party";
 	static final String PERMISSION_ALL = "discosheep.partyall";
 	static final String PERMISSION_FIREWORKS = "discosheep.fireworks";
 	static final String PERMISSION_STOPALL = "discosheep.stopall";
 	static final String PERMISSION_RELOAD = "discosheep.reload";
 	static final String PERMISSION_OTHER = "discosheep.other";
 	static final String PERMISSION_CHANGEPERIOD = "discosheep.changeperiod";
 	static final String PERMISSION_CHANGEDEFAULTS = "discosheep.changedefaults";
 	static final String PERMISSION_SAVECONFIG = "discosheep.saveconfig";
 	static final String PERMISSION_ONJOIN = "discosheep.onjoin";
 	static final String PERMISSION_SPAWNGUESTS = "discosheep.spawnguests";
 	Map<String, DiscoParty> parties = new HashMap<String, DiscoParty>();
 	private GlobalEvents globalEvents = new GlobalEvents(this);
 
 	@Override
 	public void onEnable() {
 		getCommand("ds").setExecutor(new DiscoSheepCommandExecutor(this));
 		getServer().getPluginManager().registerEvents(globalEvents, this);
 
 		getConfig().addDefault("party-on-join.enabled", DiscoParty.partyOnJoin);
 		getConfig().addDefault("max.sheep", DiscoParty.maxSheep);
 		getConfig().addDefault("max.radius", DiscoParty.maxRadius);
 		getConfig().addDefault("max.duration", toSeconds_i(DiscoParty.maxDuration));
 		getConfig().addDefault("max.period-ticks", DiscoParty.maxPeriod);
 		getConfig().addDefault("min.period-ticks", DiscoParty.minPeriod);
 		getConfig().addDefault("default.sheep", DiscoParty.defaultSheep);
 		getConfig().addDefault("default.radius", DiscoParty.defaultRadius);
 		getConfig().addDefault("default.duration", toSeconds_i(DiscoParty.defaultDuration));
 		getConfig().addDefault("default.period-ticks", DiscoParty.defaultPeriod);
 
 		/*
 		 * Iterate through all live entities and create default configuration values for them
		 * excludes bosses and other mobs that throw NPE
 		 */
 		for (EntityType ent : EntityType.values()) {
 			if (ent.isAlive()
 					&& !ent.equals(EntityType.ENDER_DRAGON)
 					&& !ent.equals(EntityType.WITHER)
 					&& !ent.equals(EntityType.PIG_ZOMBIE)
					&& !ent.equals(EntityType.OCELOT)
					&& !ent.equals(EntityType.CAVE_SPIDER)
					&& !ent.equals(EntityType.MAGMA_CUBE)
					&& !ent.equals(EntityType.MUSHROOM_COW)
					&& !ent.equals(EntityType.IRON_GOLEM)
 					&& !ent.equals(EntityType.PLAYER)) {
 				getConfig().addDefault("default.guests." + ent.toString(), 0);
 				getConfig().addDefault("max.guests." + ent.toString(), 0);
 			}
 		}
 
 		loadConfigFromDisk();
 	}
 
 	void loadConfigFromDisk() {
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 
 		DiscoParty.partyOnJoin = getConfig().getBoolean("party-on-join.enabled");
 		DiscoParty.maxSheep = getConfig().getInt("max.sheep");
 		DiscoParty.maxRadius = getConfig().getInt("max.radius");
 		DiscoParty.maxDuration = toTicks(getConfig().getInt("max.duration"));
 		DiscoParty.maxPeriod = getConfig().getInt("max.period-ticks");
 		DiscoParty.minPeriod = getConfig().getInt("min.period-ticks");
 		DiscoParty.defaultSheep = getConfig().getInt("default.sheep");
 		DiscoParty.defaultRadius = getConfig().getInt("default.radius");
 		DiscoParty.defaultDuration = toTicks(getConfig().getInt("default.duration"));
 		DiscoParty.defaultPeriod = getConfig().getInt("default.period-ticks");
 
 		for (String key : getConfig().getConfigurationSection("default.guests").getKeys(false)) {
 			DiscoParty.getDefaultGuestNumbers().put(key, getConfig().getInt("default.guests." + key));
 		}
 
 		for (String key : getConfig().getConfigurationSection("max.guests").getKeys(false)) {
 			DiscoParty.getMaxGuestNumbers().put(key, getConfig().getInt("max.guests." + key));
 		}
 
 	}
 
 	void reloadConfigFromDisk() {
 		reloadConfig();
 		loadConfigFromDisk();
 	}
 
 	void saveConfigToDisk() {
 		getConfig().set("default.sheep", DiscoParty.defaultSheep);
 		getConfig().set("default.radius", DiscoParty.defaultRadius);
 		getConfig().set("default.duration", toSeconds_i(DiscoParty.defaultDuration));
 		getConfig().set("default.period-ticks", DiscoParty.defaultPeriod);
 
 		for (Map.Entry<String, Integer> entry : DiscoParty.getDefaultGuestNumbers().entrySet()) {
 			getConfig().set("default.guests." + entry.getKey(), entry.getValue());
 		}
 
 		saveConfig();
 	}
 
 	@Override
 	public void onDisable() {
 		this.stopAllParties(); // or else the parties will continue FOREVER
 	}
 
 	int toTicks(double seconds) {
 		return (int) Math.round(seconds * 20.0);
 	}
 
 	double toSeconds(int ticks) {
 		return (double) Math.round(ticks / 20.0);
 	}
 
 	int toSeconds_i(int ticks) {
 		return (int) Math.round(ticks / 20.0);
 	}
 
 	public synchronized Map<String, DiscoParty> getPartyMap() {
 		return this.parties;
 	}
 
 	public synchronized ArrayList<DiscoParty> getParties() {
 		return new ArrayList(this.getPartyMap().values());
 	}
 
 	public void stopParty(String name) {
 		if (this.hasParty(name)) {
 			this.getParty(name).stopDisco();
 		}
 	}
 
 	public void stopAllParties() {
 		for (DiscoParty party : this.getParties()) {
 			party.stopDisco();
 		}
 	}
 
 	public boolean hasParty(String name) {
 		return this.getPartyMap().containsKey(name);
 	}
 
 	public DiscoParty getParty(String name) {
 		return this.getPartyMap().get(name);
 	}
 
 	public void removeParty(String name) {
 		if (this.hasParty(name)) {
 			this.getPartyMap().remove(name);
 		}
 	}
 
 	/*-- Actual commands begin here --*/
 	boolean helpCommand(CommandSender sender) {
 		sender.sendMessage(ChatColor.YELLOW
 				+ "DiscoSheep Help\n"
 				+ ChatColor.GRAY
 				+ "  Subcommands\n"
 				+ ChatColor.WHITE + "me, stop, all, stopall, save, reload\n"
 				+ "other <players>: start a party for the space-delimited list of players\n"
 				+ "defaults: Change the default settings for parties (takes normal arguments)\n"
 				+ ChatColor.GRAY + "  Arguments\n"
 				+ ChatColor.WHITE + "-n <integer>: set the number of sheep per player that spawn\n"
 				+ "-t <integer>: set the party duration in seconds\n"
 				+ "-p <ticks>: set the number of ticks between each disco beat\n"
 				+ "-r <integer>: set radius of the area in which sheep can spawn\n"
 				+ "-g <mob> <number>: set spawns for other mobs"
 				+ "-fw: enables fireworks");
 		return true;
 	}
 
 	boolean stopMeCommand(CommandSender sender) {
 		stopParty(sender.getName());
 		return true;
 	}
 
 	boolean stopAllCommand(CommandSender sender) {
 		if (sender.hasPermission(PERMISSION_STOPALL)) {
 			stopAllParties();
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_STOPALL);
 		}
 	}
 
 	boolean partyCommand(Player player, DiscoParty party) {
 		if (player.hasPermission(PERMISSION_PARTY)) {
 			if (!hasParty(player.getName())) {
 				party.setPlayer(player);
 				party.startDisco();
 			} else {
 				player.sendMessage(ChatColor.RED + "You already have a party. Are you underground?");
 			}
 			return true;
 		} else {
 			return noPermsMessage(player, PERMISSION_PARTY);
 		}
 	}
 
 	boolean reloadCommand(CommandSender sender) {
 		if (sender.hasPermission(PERMISSION_RELOAD)) {
 			reloadConfigFromDisk();
 			sender.sendMessage(ChatColor.GREEN + "DiscoSheep config reloaded from disk");
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_RELOAD);
 		}
 	}
 
 	boolean partyOtherCommand(String[] players, CommandSender sender, DiscoParty party) {
 		if (sender.hasPermission(PERMISSION_OTHER)) {
 			Player p;
 			for (String playerName : players) {
 				p = Bukkit.getServer().getPlayer(playerName);
 				if (p != null) {
 					if (!hasParty(p.getName())) {
 						DiscoParty individualParty = party.DiscoParty(p);
 						individualParty.startDisco();
 					}
 				} else {
 					sender.sendMessage("Invalid player: " + playerName);
 				}
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_OTHER);
 		}
 	}
 
 	boolean partyAllCommand(CommandSender sender, DiscoParty party) {
 		if (sender.hasPermission(PERMISSION_ALL)) {
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 				if (!hasParty(p.getName())) {
 					DiscoParty individualParty = party.DiscoParty(p);
 					individualParty.startDisco();
 					p.sendMessage(ChatColor.RED + "LET'S DISCO!");
 				}
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_ALL);
 		}
 	}
 
 	void partyOnJoin(Player player) {
 		if (player.hasPermission(PERMISSION_ONJOIN)) {
 			DiscoParty party = new DiscoParty(this, player);
 			party.startDisco();
 		}
 	}
 
 	boolean setDefaultsCommand(CommandSender sender, DiscoParty party) {
 		if (sender.hasPermission(PERMISSION_CHANGEDEFAULTS)) {
 			party.setDefaultsFromCurrent();
 			sender.sendMessage(ChatColor.GREEN + "DiscoSheep configured with new defaults (not saved to disk yet)");
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_CHANGEDEFAULTS);
 		}
 	}
 
 	boolean saveConfigCommand(CommandSender sender) {
 		if (sender.hasPermission(PERMISSION_SAVECONFIG)) {
 			saveConfigToDisk();
 			sender.sendMessage(ChatColor.GREEN + "DiscoSheep config saved to disk");
 			return true;
 		} else {
 			return noPermsMessage(sender, PERMISSION_SAVECONFIG);
 		}
 
 	}
 
 	boolean zeroGuests(DiscoParty party) {
 		party.getGuestNumbers().clear();
 		return true;
 	}
 
 	boolean noPermsMessage(CommandSender sender, String permission) {
 		sender.sendMessage(ChatColor.RED + "You do not have the permission node " + ChatColor.GRAY + permission);
 		return false;
 	}
 }
