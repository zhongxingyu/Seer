 package net.slipcor.pvparena.modules.vaultsupport;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import net.slipcor.pvparena.PVPArena;
 import net.slipcor.pvparena.arena.Arena;
 import net.slipcor.pvparena.arena.ArenaPlayer;
 import net.slipcor.pvparena.arena.ArenaTeam;
 import net.slipcor.pvparena.arena.ArenaPlayer.Status;
 import net.slipcor.pvparena.command.PAAJoin;
 import net.slipcor.pvparena.command.PAA_Command;
 import net.slipcor.pvparena.core.Language;
 import net.slipcor.pvparena.core.StringParser;
 import net.slipcor.pvparena.managers.Arenas;
 import net.slipcor.pvparena.managers.Teams;
 import net.slipcor.pvparena.neworder.ArenaModule;
 
 public class VaultSupport extends ArenaModule {
 
 	public static Permission permission = null;
 	public static Economy economy = null;
 	private static HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();
 	private HashMap<String, Double> paPlayersJoinAmount = new HashMap<String, Double>();
 
 	public VaultSupport() {
 		super("VaultSupport");
 	}
 
 	@Override
 	public String version() {
		return "v0.8.6.19";
 	}
 
 	@Override
 	public void addSettings(HashMap<String, String> types) {
 		types.put("money.entry", "int");
 		types.put("money.reward", "int");
 		types.put("money.killreward", "double");
 		types.put("money.minbet", "double");
 		types.put("money.maxbet", "double");
 		types.put("money.betWinFactor", "double");
 		types.put("money.betTeamWinFactor", "double");
 		types.put("money.betPlayerWinFactor", "double");
 		types.put("money.winFactor", "double");
 	}
 
 	@Override
 	public boolean checkJoin(Arena arena, Player player) {
 		if (arena.cfg.getInt("money.entry", 0) > 0) {
 			if (economy != null) {
 				if (!economy.hasAccount(player.getName())) {
 					db.s("Account not found: " + player.getName());
 					return false;
 				}
 				if (!economy.has(player.getName(),
 						arena.cfg.getInt("money.entry", 0))) {
 					// no money, no entry!
 					Arenas.tellPlayer(
 							player,
 							Language.parse("notenough", economy
 									.format(arena.cfg.getInt("money.entry", 0))),
 							arena);
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void commitCommand(Arena arena, CommandSender sender, String[] args) {
 		if (!(sender instanceof Player)) {
 			Language.parse("onlyplayers");
 			return;
 		}
 
 		Player player = (Player) sender;
 
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 
 		// /pa bet [name] [amount]
 		if (Teams.getTeam(arena, ap) != null) {
 			Arenas.tellPlayer(player, Language.parse("betnotyours"), arena);
 			return;
 		}
 
 		if (economy == null)
 			return;
 
 		if (args[0].equalsIgnoreCase("bet")) {
 
 			Player p = Bukkit.getPlayer(args[1]);
 			if (p != null) {
 				ap = ArenaPlayer.parsePlayer(p);
 			}
 			if ((p == null) && (Teams.getTeam(arena, args[1]) == null)
 					&& (Teams.getTeam(arena, ap) == null)) {
 				Arenas.tellPlayer(player, Language.parse("betoptions"), arena);
 				return;
 			}
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[2]);
 			} catch (Exception e) {
 				Arenas.tellPlayer(player,
 						Language.parse("invalidamount", args[2]), arena);
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				db.s("Account not found: " + player.getName());
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				Arenas.tellPlayer(player,
 						Language.parse("notenough", economy.format(amount)),
 						arena);
 				return;
 			}
 
 			if (amount < arena.cfg.getDouble("money.minbet")
 					|| (amount > arena.cfg.getDouble("money.maxbet"))) {
 				// wrong amount!
 				Arenas.tellPlayer(player, Language.parse("wrongamount",
 						economy.format(arena.cfg.getDouble("money.minbet")),
 						economy.format(arena.cfg.getDouble("money.maxbet"))),
 						arena);
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
 			Arenas.tellPlayer(player, Language.parse("betplaced", args[1]),
 					arena);
 			paPlayersBetAmount.put(player.getName() + ":" + args[1], amount);
 			return;
 		} else {
 
 			double amount = 0;
 
 			try {
 				amount = Double.parseDouble(args[0]);
 			} catch (Exception e) {
 				return;
 			}
 			if (!economy.hasAccount(player.getName())) {
 				db.s("Account not found: " + player.getName());
 				return;
 			}
 			if (!economy.has(player.getName(), amount)) {
 				// no money, no entry!
 				Arenas.tellPlayer(player,
 						Language.parse("notenough", economy.format(amount)),
 						arena);
 				return;
 			}
 
 			PAA_Command command = new PAAJoin();
 			
 			if (!command.checkJoin(arena, player)) {
 				return;
 			}
 
 			economy.withdrawPlayer(player.getName(), amount);
 			Arenas.tellPlayer(player, Language.parse("betplaced", args[1]),
 					arena);
 			paPlayersJoinAmount.put(player.getName(), amount);
 			command.commit(arena, player, null);
 		}
 	}
 	
 	public void commitPlayerDeath(Arena arena, Player p,
 			EntityDamageEvent cause) {
 		killreward(arena,p,ArenaPlayer.getLastDamagingPlayer(cause));
 	}
 
 	@Override
 	public boolean commitEnd(Arena arena, ArenaTeam aTeam) {
 
 		if (economy != null) {
 			db.i("eConomy set, parse bets");
 			for (String nKey : paPlayersBetAmount.keySet()) {
 				db.i("bet: " + nKey);
 				String[] nSplit = nKey.split(":");
 
 				if (Teams.getTeam(arena, nSplit[1]) == null
 						|| Teams.getTeam(arena, nSplit[1]).getName()
 								.equals("free"))
 					continue;
 
 				if (nSplit[1].equalsIgnoreCase(aTeam.getName())) {
 					double teamFactor = arena.cfg
 							.getDouble("money.betTeamWinFactor")
 							* arena.teamCount;
 					if (teamFactor <= 0) {
 						teamFactor = 1;
 					}
 					teamFactor *= arena.cfg.getDouble("money.betWinFactor");
 
 					double amount = paPlayersBetAmount.get(nKey) * teamFactor;
 
 					if (!economy.hasAccount(nSplit[0])) {
 						db.s("Account not found: " + nSplit[0]);
 						return true;
 					}
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						Arenas.tellPlayer(Bukkit.getPlayer(nSplit[0]), Language
 								.parse("youwon", economy.format(amount)), arena);
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void configParse(Arena arena, YamlConfiguration config, String type) {
 		config.addDefault("money.entry", Integer.valueOf(0));
 		config.addDefault("money.reward", Integer.valueOf(0));
 		config.addDefault("money.killreward", Double.valueOf(0));
 		config.addDefault("money.minbet", Double.valueOf(0));
 		config.addDefault("money.maxbet", Double.valueOf(0));
 		config.addDefault("money.betWinFactor", Double.valueOf(1));
 		config.addDefault("money.betTeamWinFactor", Double.valueOf(1));
 		config.addDefault("money.betPlayerWinFactor", Double.valueOf(1));
 		config.addDefault("money.winFactor", Double.valueOf(2));
 
 		config.options().copyDefaults(true);
 	}
 
 	@Override
 	public void giveRewards(Arena arena, Player player) {
 		if (economy != null) {
 			for (String nKey : paPlayersBetAmount.keySet()) {
 				String[] nSplit = nKey.split(":");
 
 				if (nSplit[1].equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.playerCount
 							* arena.cfg.getDouble("money.betPlayerWinFactor");
 
 					if (playerFactor <= 0) {
 						playerFactor = 1;
 					}
 
 					playerFactor *= arena.cfg.getDouble("money.betWinFactor");
 
 					double amount = paPlayersBetAmount.get(nKey) * playerFactor;
 
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						PVPArena.instance.getAmm().announcePrize(
 								arena,
 								Language.parse("awarded",
 										economy.format(amount)));
 						Arenas.tellPlayer(Bukkit.getPlayer(nSplit[0]), Language
 								.parse("youwon", economy.format(amount)), arena);
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 
 			if (arena.cfg.getInt("money.reward", 0) > 0) {
 				economy.depositPlayer(player.getName(),
 						arena.cfg.getInt("money.reward", 0));
 				Arenas.tellPlayer(player, Language.parse("awarded",
 						economy.format(arena.cfg.getInt("money.reward", 0))),
 						arena);
 
 			}
 
 			for (String nKey : paPlayersJoinAmount.keySet()) {
 
 				if (nKey.equalsIgnoreCase(player.getName())) {
 					double playerFactor = arena.cfg.getDouble("money.winFactor");
 
 					double amount = paPlayersJoinAmount.get(nKey) * playerFactor;
 
 					economy.depositPlayer(nKey, amount);
 					try {
 						PVPArena.instance.getAmm().announcePrize(
 								arena,
 								Language.parse("awarded",
 										economy.format(amount)));
 						Arenas.tellPlayer(Bukkit.getPlayer(nKey), Language
 								.parse("youwon", economy.format(amount)), arena);
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean hasPerms(CommandSender player, String perms) {
 		return permission == null ? player.hasPermission(perms) : permission
 				.has(player, perms);
 	}
 
 	@Override
 	public void initLanguage(YamlConfiguration config) {
 		config.addDefault("log.iconomyon", "<3 eConomy");
 		config.addDefault("log.iconomyoff", "</3 eConomy");
 
 		config.addDefault("lang.notenough", "You don't have %1%.");
 		config.addDefault("lang.betnotyours",
 				"Cannot place bets on your own match!");
 		config.addDefault("lang.betoptions",
 				"You can only bet on team name or arena player!");
 		config.addDefault("lang.wrongamount",
 				"Bet amount must be between %1% and %2%!");
 		config.addDefault("lang.invalidamount", "Invalid amount: %1%");
 		config.addDefault("lang.betplaced", "Your bet on %1% has been placed.");
 		config.addDefault("lang.youwon", "You won %1%");
 		config.addDefault("lang.joinpay", "You paid %1% to join the arena!");
 
 		config.addDefault("lang.killreward", "You received %1% for killing %2%!");
 		
 		config.addDefault("lang.refunding", "Refunding %1%!");
 	}
 
 	private void killreward(Arena arena, Player p, Entity damager) {
 		Player player = null;
 		if (damager instanceof Player) {
 			player = (Player) damager;
 		}
 		if (player == null) {
 			return;
 		}
 		double amount = arena.cfg
 				.getDouble("money.killreward");
 
 		if (amount < 0.01) {
 			return;
 		}
 		
 		if (!economy.hasAccount(player.getName())) {
 			db.s("Account not found: " + player.getName());
 			return;
 		}
 		economy.depositPlayer(player.getName(), amount);
 		try {
 			Arenas.tellPlayer(Bukkit.getPlayer(player.getName()), Language
 					.parse("youwon", economy.format(amount)), arena);
 		} catch (Exception e) {
 			// nothing
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
 			setupPermissions();
 			setupEconomy();
 		}
 	}
 
 	@Override
 	public boolean parseCommand(String cmd) {
 		try {
 			double amount = Double.parseDouble(cmd);
 			db.i("parsing join bet amount: " + amount);
 			return true;
 		} catch (Exception e) {
 			return cmd.equalsIgnoreCase("bet");
 		}
 
 	}
 
 	@Override
 	public void parseInfo(Arena arena, CommandSender player) {
 		player.sendMessage("");
 		player.sendMessage("6Economy (Vault): f entry: "
 				+ StringParser.colorVar(arena.cfg.getInt("money.entry", 0))
 				+ " || reward: "
 				+ StringParser.colorVar(arena.cfg.getInt("money.reward", 0))
 				+ " || killreward: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.killreward", 0))
 				+ " || winFactor: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.winFactor", 0)));
 
 		player.sendMessage("minbet: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.minbet", 0))
 				+ " || maxbet: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.maxbet", 0))
 				+ " || betWinFactor: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.betWinFactor", 0)));
 		
 		player.sendMessage("betTeamWinFactor: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.betTeamWinFactor", 0))
 				+ " || betPlayerWinFactor: "
 				+ StringParser.colorVar(arena.cfg.getDouble("money.betPlayerWinFactor", 0)));
 	}
 
 	@Override
 	public void parseJoin(Arena arena, Player player, String coloredTeam) {
 		int entryfee = arena.cfg.getInt("money.entry", 0);
 		if (entryfee > 0) {
 			if (economy != null) {
 				economy.withdrawPlayer(player.getName(), entryfee);
 				Arenas.tellPlayer(player,
 						Language.parse("joinpay", economy.format(entryfee)),
 						arena);
 			}
 		}
 	}
 
 	@Override
 	public void parseRespawn(Arena arena, Player player, ArenaTeam team,
 			int lives, DamageCause cause, Entity damager) {
 		killreward(arena, player, damager);
 	}
 
 	protected void pay(Arena arena, HashSet<String> result) {
 		if (result == null || result.size() == arena.teamCount) {
 			return;
 		}
 		if (economy != null) {
 			for (String nKey : paPlayersBetAmount.keySet()) {
 				String[] nSplit = nKey.split(":");
 				ArenaTeam team = Teams.getTeam(arena, nSplit[1]);
 				if (team == null || team.getName().equals("free"))
 					continue;
 
 				if (result.contains(nSplit[1])) {
 					double teamFactor = arena.cfg
 							.getDouble("money.betTeamWinFactor")
 							* arena.teamCount;
 					if (teamFactor <= 0) {
 						teamFactor = 1;
 					}
 					teamFactor *= arena.cfg.getDouble("money.betWinFactor");
 
 					double amount = paPlayersBetAmount.get(nKey) * teamFactor;
 
 					if (!economy.hasAccount(nSplit[0])) {
 						db.s("Account not found: " + nSplit[0]);
 						continue;
 					}
 					economy.depositPlayer(nSplit[0], amount);
 					try {
 						Arenas.tellPlayer(Bukkit.getPlayer(nSplit[0]), Language
 								.parse("youwon", economy.format(amount)), arena);
 					} catch (Exception e) {
 						// nothing
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void reset(Arena arena, boolean force) {
 		paPlayersBetAmount.clear();
 		paPlayersJoinAmount.clear();
 	}
 	
 	@Override
 	public void resetPlayer(Arena arena, Player player) {
 		ArenaPlayer ap = ArenaPlayer.parsePlayer(player);
 		if (ap.getStatus().equals(Status.LOBBY) ||
 				ap.getStatus().equals(Status.READY)) {
 			int entryfee = arena.cfg.getInt("money.entry", 0);
 			Arenas.tellPlayer(player, Language.parse("refunding", economy.format(entryfee)));
 			if (!economy.hasAccount(player.getName())) {
 				db.s("Account not found: " + player.getName());
 				return;
 			}
 			economy.depositPlayer(player.getName(), entryfee);
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
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = Bukkit
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	public void timedEnd(Arena arena, HashSet<String> result) {
 		pay(arena, result);
 	}
 
 }
