 
 package net.slipcor.pvparena.modules.vaultsupport;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.ArenaClass;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.classes.PACheck;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.Config.CFG;
 import net.slipcor.pvparena.core.Language.MSG;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.events.PAGoalEvent;
 import net.slipcor.pvparena.events.PAPlayerClassChangeEvent;
 import net.slipcor.pvparena.loadables.ArenaModule;
 import net.slipcor.pvparena.loadables.ArenaModuleManager;
 
 public class VaultSupport extends ArenaModule implements Listener {
 
 	private static Economy economy = null;
 	private static Permission permission = null;
 	private HashMap<String, Double> playerBetMap = null;
 	private HashMap<String, Double> playerJoinMap = null;
 	private double pot = 0;
 	private Map<String, Double> list = null;
 
 	public VaultSupport() {
 		super("Vault");
 	}
 
 	@Override
 	public String version() {
		return "v1.1.0.395";
 	}
 
 	@Override
 	public boolean checkCommand(String cmd) {
 		try {
 			double amount = Double.parseDouble(cmd);
 			arena.getDebugger().i("parsing join bet amount: " + amount);
 			return true;
 		} catch (Exception e) {
 			return cmd.equalsIgnoreCase("bet");
 		}
 
 	}
 
 	@Override
 	public PACheck checkJoin(CommandSender sender,
 			PACheck res, boolean join) {
 		
 		if (res.hasError() || !join) {
 			return res;
 		}
 		
 		if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE) > 0) {
 			if (economy != null) {
 				if (!economy.hasAccount(sender.getName())) {
 					arena.getDebugger().i("Account not found: " + sender.getName(), sender);
 					res.setError(this, "account not found: " + sender.getName());
 					return res;
 				}
 				if (!economy.has(sender.getName(),
 						arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))) {
 					// no money, no entry!
 
 					res.setError(this, Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy
 							.format(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))));
 					return res;
 				}
 			}
 		}
 		return res;
 	}
 
 	@Override
 	public void commitCommand(CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) { //TODO move to new parseCommand
 			Language.parse(MSG.ERROR_ONLY_PLAYERS);
 			return;
 		}
 
 		Player player = (Player) sender;
 
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 
 		// /pa bet [name] [amount]
 		if (ap.getArenaTeam() != null) {
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETNOTYOURS));
 			return;
 		}
 
 		if (economy == null)
 			return;
 
 		if (args[0].equalsIgnoreCase("bet")) {
 			
 			int maxTime = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_BETTIME);
 			if (maxTime > 0 && maxTime > arena.getPlayedSeconds()) {
 				arena.msg(player, Language.parse(MSG.ERROR_INVALID_VALUE,
 						"2l8"));
 				return;
 			}
 
 			Player p = Bukkit.getPlayer(args[1]);
 			if (p != null) {
 				ap = ArenaPlayer.parsePlayer(p.getName());
 			}
 			if ((p == null) && (arena.getTeam(args[1]) == null)
 					&& (ap.getArenaTeam() == null)) {
 				arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETOPTIONS));
 				return;
 			}
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[2]);
 			} catch (Exception e) {
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_INVALIDAMOUNT, args[2]));
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				arena.getDebugger().i("Account not found: " + player.getName(), sender);
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy.format(amount)));
 				return;
 			}
 			
 			double maxBet = arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET);
 
 			if (amount < arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET)
 					|| (maxBet > 0.01 && amount > maxBet)) {
 				// wrong amount!
 				arena.msg(player, Language.parse(MSG.ERROR_INVALID_VALUE,
 						economy.format(amount)));
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_BETPLACED, args[1]));
 			getPlayerBetMap().put(player.getName() + ":" + args[1], amount);
 			return;
 		} else {
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[0]);
 			} catch (Exception e) {
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				arena.getDebugger().i("Account not found: " + player.getName(), sender);
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				arena.msg(player,
 						Language.parse(MSG.MODULE_VAULT_NOTENOUGH, economy.format(amount)));
 				return;
 			}
 			PACheck res = new PACheck();
 			checkJoin(sender, res, true);
 			
 			if (res.hasError()) {
 				arena.msg(sender, res.getError());
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_JOINPAY, args[0]));
 			getPlayerJoinMap().put(player.getName(), amount);
 			commitCommand(player, null);
 		}
 	}
 
 	@Override
 	public boolean commitEnd(ArenaTeam aTeam) {
 
 		if (economy != null) {
 			arena.getDebugger().i("eConomy set, parse bets");
 			for (String nKey : getPlayerBetMap().keySet()) {
 				arena.getDebugger().i("bet: " + nKey);
 				String[] nSplit = nKey.split(":");
 
 				if (arena.getTeam(nSplit[1]) == null
 						|| arena.getTeam(nSplit[1]).getName()
 								.equals("free"))
 					continue;
 
 				if (nSplit[1].equalsIgnoreCase(aTeam.getName())) {
 					double teamFactor = arena.getArenaConfig()
 							.getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
 							* arena.getTeamNames().size();
 					if (teamFactor <= 0) {
 						teamFactor = 1;
 					}
 					teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 
 					double amount = getPlayerBetMap().get(nKey) * teamFactor;
 
 					if (!economy.hasAccount(nSplit[0])) {
 						arena.getDebugger().i("Account not found: " + nSplit[0]);
 						return true;
 					}
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public void onThisLoad() {
 		if (economy == null && Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
 			setupEconomy();
 			setupPermission();
 			Bukkit.getPluginManager().registerEvents(this, PVPArena.instance);
 		}
 	}
 	
 	private Map<String, Double> getPermList() {
 		if (list == null) {
 			list = new HashMap<String, Double>();
 			
 			if (null == arena.getArenaConfig().getUnsafe("modules.vault.permfactors")) {
 				
 				list.put("pa.vault.supervip", 3d);
 				list.put("pa.vault.vip", 2d);
 				for (String node : list.keySet()) {
 					arena.getArenaConfig().setManually("modules.vault.permfactors."+node, list.get(node));
 					arena.getArenaConfig().save();
 				}
 			} else {
 				ConfigurationSection cs = arena.getArenaConfig().getYamlConfiguration().
 						getConfigurationSection("modules.vault.permfactors");
 				for (String node : cs.getKeys(false)) {
 					list.put(node, cs.getDouble(node));
 				}
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * bettingPlayerName:betGoal => betAmount
 	 * @return
 	 */
 	private Map<String, Double> getPlayerBetMap() {
 		if (playerBetMap == null) {
 			playerBetMap = new HashMap<String, Double>();
 		}
 		return playerBetMap;
 	}
 	
 	/**
 	 * bettingPlayerName => joinBetAmount
 	 * @return
 	 */
 	private Map<String, Double> getPlayerJoinMap() {
 		if (playerJoinMap == null) {
 			playerJoinMap = new HashMap<String, Double>();
 		}
 		return playerJoinMap;
 	}
 	
 	@Override
 	public void giveRewards(Player player) {
 		final int minPlayTime = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_MINPLAYTIME);
 		
 		if (minPlayTime > arena.getPlayedSeconds()) {
 			arena.getDebugger().i("no rewards, game too short!");
 			return;
 		}
 		
 		arena.getDebugger().i("giving rewards to player " + player.getName(), player);
 
 		int winners = 0;
 		arena.getDebugger().i("giving Vault rewards to Player " + player, player);
 		for (ArenaPlayer p : arena.getFighters()) {
 			arena.getDebugger().i("- checking fighter " + p.getName(), p.getName());
 			if (p.getStatus() != null && p.getStatus().equals(Status.FIGHT)) {
 				arena.getDebugger().i("-- added!", p.getName());
 				winners++;
 			}
 		}
 		arena.getDebugger().i("winners: " + winners, player);
 		
 		if (economy != null) {
 			arena.getDebugger().i("checking on bet amounts!", player);
 			for (String nKey : getPlayerBetMap().keySet()) {
 				String[] nSplit = nKey.split(":");
 
 				if (nSplit[1].equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.getFighters().size()
 							* arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR);
 
 					if (playerFactor <= 0) {
 						playerFactor = 1;
 					}
 
 					playerFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 
 					double amount = getPlayerBetMap().get(nKey) * playerFactor;
 
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						
 						ArenaModuleManager.announce(
 								arena,
 								Language.parse(MSG.NOTICE_PLAYERAWARDED,
 										economy.format(amount)), "PRIZE");
 						arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 			
 			if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_WINPOT)) {
 				arena.getDebugger().i("calculating win pot!", player);
 				double amount = winners > 0 ? pot / winners : 0;
 				
 
 				
 				if (player != null) {
 					double factor = 1d;
 					for (String node : getPermList().keySet()) {
 						if (player.hasPermission(node)) {
 							factor = Math.max(factor, getPermList().get(node));
 						}
 					}
 					
 					amount *= factor;
 				}
 				
 				economy.depositPlayer(player.getName(), amount);
 				arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
 						economy.format(amount)));
 			} else if (arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0) > 0) {
 
 				double amount = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD, 0);
 				arena.getDebugger().i("calculating win reward: " + amount, player);
 				
 
 				
 				if (player != null) {
 					double factor = 1d;
 					for (String node : getPermList().keySet()) {
 						if (player.hasPermission(node)) {
 							factor = Math.max(factor, getPermList().get(node));
 						}
 					}
 					
 					amount *= factor;
 				}
 				
 				economy.depositPlayer(player.getName(), amount);
 				arena.msg(player, Language.parse(MSG.NOTICE_AWARDED,
 						economy.format(amount)));
 
 			} 	
 
 			for (String nKey : getPlayerJoinMap().keySet()) {
 
 				if (nKey.equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINFACTOR);
 
 					double amount = getPlayerJoinMap().get(nKey) * playerFactor;
 
 
 					
 					if (player != null) {
 						double factor = 1d;
 						for (String node : getPermList().keySet()) {
 							if (player.hasPermission(node)) {
 								factor = Math.max(factor, getPermList().get(node));
 							}
 						}
 						
 						amount *= factor;
 					}
 					
 					economy.depositPlayer(nKey, amount);
 					try {
 						
 						ArenaModuleManager.announce(
 								arena,
 								Language.parse(MSG.NOTICE_PLAYERAWARDED,
 										economy.format(amount)), "PRIZE");
 						arena.msg(Bukkit.getPlayer(nKey), Language
 								.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 	}
 
 	private void killreward(Player p, Entity damager) {
 		Player player = null;
 		if (damager instanceof Player) {
 			player = (Player) damager;
 		}
 		if (player == null) {
 			return;
 		}
 		double amount = arena.getArenaConfig()
 				.getDouble(CFG.MODULES_VAULT_KILLREWARD);
 
 		if (amount < 0.01) {
 			return;
 		}
 		
 		if (!economy.hasAccount(player.getName())) {
 			arena.getDebugger().i("Account not found: " + player.getName(), player);
 			return;
 		}
 		economy.depositPlayer(player.getName(), amount);
 		try {
 			arena.msg(Bukkit.getPlayer(player.getName()), Language
 					.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 
 	@Override
 	public void displayInfo(CommandSender player) {
 		player.sendMessage("entryfee: "
 				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE))
 				+ " || reward: "
 				+ StringParser.colorVar(arena.getArenaConfig().getInt(CFG.MODULES_VAULT_WINREWARD))
 				+ " || killreward: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_KILLREWARD))
 				+ " || winFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_WINFACTOR)));
 
 		player.sendMessage("minbet: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MINIMUMBET))
 				+ " || maxbet: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_MAXIMUMBET))
 				+ " || betWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR)));
 
 		player.sendMessage("betTeamWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR))
 				+ " || betPlayerWinFactor: "
 				+ StringParser.colorVar(arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINPLAYERFACTOR)));
 
 		player.sendMessage(StringParser.colorVar(
 				"bet pot",arena.getArenaConfig().getBoolean(
 						CFG.MODULES_VAULT_BETPOT))
 				+ " || "
 				+ StringParser.colorVar(
 				"win pot",arena.getArenaConfig().getBoolean(
 						CFG.MODULES_VAULT_WINPOT)));
 	}
 
 	@Override
 	public void parseJoin(CommandSender sender, ArenaTeam team) {
 		int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE, 0);
 		if (entryfee > 0) {
 			if (economy != null) {
 				economy.withdrawPlayer(sender.getName(), entryfee);
 				arena.msg(sender,
 						Language.parse(MSG.MODULE_VAULT_JOINPAY, economy.format(entryfee)));
 				pot += entryfee;
 			}
 		}
 	}
 	
 	public void parsePlayerDeath(Player p,
 			EntityDamageEvent cause) {
 		killreward(p,ArenaPlayer.getLastDamagingPlayer(cause, p));
 	}
 
 	protected void pay(HashSet<String> result) {
 		if (result == null || result.size() == arena.getTeamNames().size()) {
 			return;
 		}
 		arena.getDebugger().i("Paying winners: " + StringParser.joinSet(result, ", "));
 		
 		if (economy == null) {
 			return;
 		}
 			
 		double pot = 0;
 		double winpot = 0;
 		
 		for (String s : getPlayerBetMap().keySet()) {
 			String[] nSplit = s.split(":");
 			
 			pot += getPlayerBetMap().get(s);
 			
 			if (result.contains(nSplit)) {
 				winpot += getPlayerBetMap().get(s);
 			}
 		}
 		
 		for (String nKey : getPlayerBetMap().keySet()) {
 			String[] nSplit = nKey.split(":");
 			ArenaTeam team = arena.getTeam(nSplit[1]);
 			if (team == null || team.getName().equals("free")) {
 				if (Bukkit.getPlayerExact(nSplit[1]) == null) {
 					continue;
 				}
 			}
 
 			if (result.contains(nSplit[1])) {
 				double amount = 0;
 				
 				if (arena.getArenaConfig().getBoolean(CFG.MODULES_VAULT_BETPOT)) {
 					if (winpot > 0) {
 						amount = pot * getPlayerBetMap().get(nKey) / winpot;
 					}
 				} else {
 					double teamFactor = arena.getArenaConfig()
 							.getDouble(CFG.MODULES_VAULT_BETWINTEAMFACTOR)
 							* arena.getTeamNames().size();
 					if (teamFactor <= 0) {
 						teamFactor = 1;
 					}
 					teamFactor *= arena.getArenaConfig().getDouble(CFG.MODULES_VAULT_BETWINFACTOR);
 					amount = getPlayerBetMap().get(nKey) * teamFactor;
 				}
 
 				if (!economy.hasAccount(nSplit[0])) {
 					arena.getDebugger().i("Account not found: " + nSplit[0]);
 					continue;
 				}
 				
 				Player player = Bukkit.getPlayer(nSplit[0]);
 				
 				if (player == null) {
 					System.out.print("player null: " + nSplit[0]);
 					arena.getDebugger().i("Player is null!");
 				} else {
 					double factor = 1d;
 					for (String node : getPermList().keySet()) {
 						if (player.hasPermission(node)) {
 							factor = Math.max(factor, getPermList().get(node));
 						}
 					}
 					
 					amount *= factor;
 				}
 				
 				economy.depositPlayer(nSplit[0], amount);
 				try {
 					arena.msg(Bukkit.getPlayer(nSplit[0]), Language
 							.parse(MSG.MODULE_VAULT_YOUWON, economy.format(amount)));
 				} catch (Exception e) {
 					// nothing
 				}
 			}
 		}
 	}
 
 	@Override
 	public void reset(boolean force) {
 		getPlayerBetMap().clear();
 		getPlayerJoinMap().clear();
 		pot = 0;
 	}
 	
 	@Override
 	public void resetPlayer(Player player, boolean force) {
 		if (player == null) {
 			return;
 		}
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
 		if (ap == null) {
 			return;
 		}
 		if (ap.getStatus() == null || force) {
 			return;
 		}
 		if (ap.getStatus().equals(Status.LOUNGE) ||
 				ap.getStatus().equals(Status.READY)) {
 			int entryfee = arena.getArenaConfig().getInt(CFG.MODULES_VAULT_ENTRYFEE);
 			if (entryfee < 1) {
 				return;
 			}
 			arena.msg(player, Language.parse(MSG.MODULE_VAULT_REFUNDING, economy.format(entryfee)));
 			if (!economy.hasAccount(player.getName())) {
 				arena.getDebugger().i("Account not found: " + player.getName(), player);
 				return;
 			}
 			economy.depositPlayer(player.getName(), entryfee);
 			pot -= entryfee;
 		}
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = Bukkit
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 	
 	private boolean setupPermission() {
 		RegisteredServiceProvider<Permission> permProvider = Bukkit
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.permission.Permission.class);
 		if (permProvider != null) {
 			permission = permProvider.getProvider();
 		}
 		
 		return (permission != null);
 	}
 	
 	public void timedEnd(HashSet<String> result) {
 		pay(result);
 	}
 	
 	@EventHandler
 	public void onClassChange(PAPlayerClassChangeEvent event) {
 		if (event.getArena().equals(arena)) {
 			
 			String autoClass = arena.getArenaConfig().getString(CFG.READY_AUTOCLASS);
 			
 			if (event.getArenaClass() == null || 
 					!autoClass.equals("none") ||
 					!event.getArenaClass().getName().equals(autoClass)) {
 				return; // class will be removed OR no autoClass OR no>T< autoClass
 			}
 			
 			String group = null;
 			
 			try {
 				group = permission.getPrimaryGroup(event.getPlayer());
 			} catch (Exception e) {
 				
 			}
 			ArenaClass aClass = arena.getClass("autoClass_"+group);
 			if (aClass != null) {
 				event.setArenaClass(aClass);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onGoalScore(PAGoalEvent event) {
 		
 		String lastTrigger = "";
 		if (arena == null) {
 			debug.i("catching GoalEvent in Vault!");
 		} else {
 			arena.getDebugger().i("catching GoalEvent in Vault!");
 		}
 		
 		if (event.getArena().equals(arena)) {
 			arena.getDebugger().i("it's us!");
 			String[] contents = event.getContents();
 			/*
 			* content.length == 1
 			* * content[0] = "" => end!
 			* 
 			* content[X].contains(playerDeath) => "playerDeath:playerName"
 			* content[X].contains(playerKill) => "playerKill:playerKiller:playerKilled"
 			* content[X].contains(trigger) => "trigger:playerName" triggered a score
 			* content[X].equals(tank) => player is tank
 			* content[X].equals(infected) => player is infected
 			* content[X].equals(doesRespawn) => player will respawn
 			* content[X].contains(score) => "score:player:team:value"
 			*
 			*/
 			
 			for (String node : contents) {
 				node = node.toLowerCase();
 				if (node.contains("trigger")) {
 					lastTrigger = node.substring(8);
 					newReward(lastTrigger, "TRIGGER");
 				}
 				
 				if (node.contains("playerDeath")) {
 					newReward(node.substring(12), "DEATH");
 				}
 				
 				if (node.contains("playerKill")) {
 					String[] val = node.split(":");
 					newReward(val[1], "KILL");
 				}
 				
 				if (node.contains("score")) {
 					String[] val = node.split(":");
 					newReward(val[1], "SCORE", Integer.parseInt(val[3]));
 				}
 				
 				if (node.equals("")) {
 					newReward(lastTrigger, "WIN");
 				}
 			}
 			
 		}
 	}
 
 	private void newReward(String playerName, String rewardType) {
 		arena.getDebugger().i("new Reward: " + playerName + " -> " + rewardType);
 		newReward(playerName, rewardType, 1);
 	}
 
 	private void newReward(String playerName, String rewardType, int amount) {
 		arena.getDebugger().i("new Reward: " + amount + "x "+ playerName + " -> " + rewardType);
 		try {
 			
 			double value = arena.getArenaConfig().getDouble(
 					CFG.valueOf("MODULES_VAULT_REWARD_"+rewardType), 0d);
 			
 			double maybevalue = arena.getArenaConfig().getDouble(
 					CFG.valueOf("MODULES_VAULT_REWARD_"+rewardType), -1d);
 
 			PVPArena.instance.getLogger().info("Giving "+economy.format(value)+" to "+playerName);
 			if (maybevalue < 0) {
 				PVPArena.instance.getLogger().warning("config value is not set: " + CFG.valueOf("MODULES_VAULT_REWARD_"+rewardType).getNode());
 			}
 			
 			economy.depositPlayer(playerName, value);
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 	}
 }
