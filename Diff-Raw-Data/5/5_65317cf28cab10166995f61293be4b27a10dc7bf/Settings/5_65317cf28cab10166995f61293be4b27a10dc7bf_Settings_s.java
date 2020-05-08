 package com.wolvencraft.prison.mines.settings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Material;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 
 public class Settings extends com.wolvencraft.prison.settings.Settings {
 	public final boolean PLAYERS_TP_ON_RESET;
 	public final boolean RESET_FORCE_TIMER_UPDATE;
 	public final boolean RESET_ALL_MINES_ON_STARTUP;
 	public final boolean RESET_TRIGGERS_CHILDREN_RESETS;
 	public final List<String> BANNEDNAMES;
	public final Material[] TOOLS = {Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE};
 	public final int DEFAULTTIME;
 	
 	public Settings(PrisonMine plugin) {
 		super(PrisonMine.getPrisonSuite());
 		PLAYERS_TP_ON_RESET = plugin.getConfig().getBoolean("players.teleport-players-out-of-the-mine");
 		RESET_FORCE_TIMER_UPDATE = plugin.getConfig().getBoolean("reset.force-reset-timer-on-mine-reset");
 		RESET_ALL_MINES_ON_STARTUP = plugin.getConfig().getBoolean("reset.reset-all-mines-on-startup");
 		RESET_TRIGGERS_CHILDREN_RESETS = plugin.getConfig().getBoolean("reset.manual-resets-children");
 		BANNEDNAMES = new ArrayList<String>();
 		for(CommandManager cmd : CommandManager.values()) {
 			for(String alias : cmd.getLocalAlias()) {
 				BANNEDNAMES.add(alias);
 			}
 		}
 		BANNEDNAMES.add("all");
 		BANNEDNAMES.add("none");
 		BANNEDNAMES.add("super");
 		DEFAULTTIME = 900;
 	}
 }
