 package com.wolvencraft.prison.mines.settings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.wolvencraft.prison.mines.CommandHandler;
 import com.wolvencraft.prison.mines.PrisonMine;
 
 public class Settings extends com.wolvencraft.prison.settings.Settings {
 	public final boolean TPONRESET;
	public final boolean MANUALTIMERRESET;
 	public final List<String> BANNEDNAMES;
 	
 	public Settings(PrisonMine plugin) {
 		super(PrisonMine.getPrisonSuite());
 		TPONRESET = plugin.getConfig().getBoolean("teleport-on-reset");
		MANUALTIMERRESET = plugin.getConfig().getBoolean("reset-timer-on-manual");
 		BANNEDNAMES = new ArrayList<String>();
 		for(CommandHandler cmd : CommandHandler.values()) {
 			for(String alias : cmd.getLocalAlias()) {
 				BANNEDNAMES.add(alias);
 			}
 		}
 	}
 }
