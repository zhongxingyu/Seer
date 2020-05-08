 package io.github.harryprotist;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 
 import java.util.*;
 import java.io.*;
 
 //import SpellList;
 
 public final class Spellcraft extends JavaPlugin implements Listener {
 
 //	private Map<String, Integer> mana; // holds how much mana each player has
 		// TODO: Save to file and load from file
 	public SpellList Spells;
 	private Mana ManaEvents;
 
 	// runs on plugin load
 	public void onEnable () {
 		getLogger().info("onEnable of Spellcraft functioning");
 
 		Spells = new SpellList(this);
 		ManaEvents = new Mana(this);
 
 		if (Spell.Load()) {
 			getLogger().info(Spell.dumpMaps());
 		} else {
 			getLogger().info("Failed to Load Spell");	
 		}
 
 		if (loadIncantations()) {
 			getLogger().info("incantations loaded");
 		} else {
 			getLogger().info("failed to load incantations");
 		}
 		getLogger().info("done loading spellcraft");
 
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		loadPlayerData(event.getPlayer());
 	}
 	@EventHandler
	public void onPlayerQuit(PlayerLoginEvent event) {
 		savePlayerData(event.getPlayer());
 	}
 	@EventHandler
 	public void onWorldSave(WorldSaveEvent event) {
 		getLogger().info("saving spellcraft");
 		savePlayerData();
 		if (saveIncantations() ) {
 			getLogger().info("saved incantations");
 		} else {
 			getLogger().info("failed to save incantations");
 		}
 	}
 
 	// runs on exit
 	public void onDisable () {
 		getLogger().info("onDisable of Spellcraft functioning");
 		onWorldSave(null);		
 	}
 
 	// handle commands
 	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args) {
 		String cmdName = cmd.getName();
 		
 		if (cmdName.equalsIgnoreCase("spellCraftRunning") ) {
 			sender.sendMessage("Running");
 
 			String map;
 			if (Spell.Load()) {
 				sender.sendMessage(Spell.dumpMaps());
 			} else {
 				sender.sendMessage("Failed to Load Spell");	
 			}
 	
 			return true;
 
 		} else if (cmdName.equalsIgnoreCase("mana") && sender instanceof Player) {
 
 			// the player wants to do something mana related
 			Player p = (Player) sender;	
 
 			// Mana commands: check, toggle on, register, imbue, perform, cast
 			// register binds a spell to a word
 			// imbue binds a spell and mana to an item
 			// perform consumes a block and uses it to power a rune
 
 			// ready to cast a spell
 			if (args.length > 0 && args[0].length() > 0 && (args[0].charAt(0) == 'c' || args[0].charAt(0) == 'C')) {
 								
 				if (args.length > 1 && Spells.lookup(args[1])) {
 
 					setMeta(p, "lastspell", Spells.get(args[1]));
 				}	
 			
 			// toggle
 			} else if (args.length > 0 && args[0].length() > 0 && (args[0].charAt(0) == 'o' || args[0].charAt(0) == 'O')) {
 				
 				Boolean b = (Boolean)getMeta(p, "manaon");
 				if (b == null) b = false;				
 		
 				sender.sendMessage("Setting mana to " + (new Boolean(!b.booleanValue()).toString()) );
 
 				setMeta(p, "manaon", new Boolean(!b.booleanValue()));
 
 			// register a spell
 			} else if (args.length > 0 && args[0].length() > 0 && (args[0].charAt(0) == 'r' || args[0].charAt(0) == 'O')) {
 
 				if (args.length > 1) {
 					ManaEvents.register(p, args[1]);
 				}
 
 			// imbue an item
 			} else if (args.length > 0 && args[0].length() > 0 && (args[0].charAt(0) == 'i' || args[0].charAt(0) == 'I')) {
 
 				if (args.length > 1) {
 					ManaEvents.imbue(p, args[1]);
 				}
 
 			// perform a ritual
 			} else if (args.length > 0 && args[0].length() > 0 && (args[0].charAt(0) == 'p' || args[0].charAt(0) == 'P')) {
 
 				ManaEvents.perform(p);
 			
 			// otherwise print MP
 			} else {
 				Integer m = (Integer)getMeta(p, "mana");
 				if (m == null) m = 0;
 				sender.sendMessage(m + " MP");
 			}
 			
 			return true;
 		} else if (cmdName.equalsIgnoreCase("setmana") && sender instanceof Player) {
 			
 			Integer m = new Integer(0);
 			if (args.length > 0) {
 				try {
 					m = new Integer(args[0]);	
 				} catch (NumberFormatException e) {
 					getLogger().info(e.toString());
 					sender.sendMessage("Invalid Format");
 				}
 			}
 			setMeta((Player)sender, "mana", m);
 			sender.sendMessage("Set mana to " + m.toString());
 
 		} else if (sender instanceof Player) {
 
 			if (cmdName.equalsIgnoreCase("mload") ) {
 				if (loadPlayerData( (Player)sender) ) {
 					sender.sendMessage("loaded successfully");
 				} else {
 					sender.sendMessage("failed to load data");
 				} 
 			}
 			
 			if (cmdName.equalsIgnoreCase("msave") ) {
 				if (savePlayerData() ) {
 					sender.sendMessage("saved successfully");
 				} else {
 					sender.sendMessage("failed to save data");
 				}
 			}
 		}
 		return false;
 	}
 
 	public void setMeta(Player p, String k, Object v) {
 		p.setMetadata(k, new FixedMetadataValue(this, v) );
 	}
 	public Object getMeta(Player p, String k) {
 	 	List<MetadataValue> vs = p.getMetadata(k);
 		for (MetadataValue v : vs) {
 			if (v.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())) {
 				return v.value();	
 			}
 		}
 		return null;
 	}
 
 	//////////////////// saving and loading stuff	
 	public boolean saveSpells() {
 		
 		FileWriter f;
 		try { 
 			f = new FileWriter("spelllist.txt", false);
 	
 			String dat = Spells.dump();		
 			f.write(dat);
 			f.close();
 	
 		}
 		catch ( IOException e ) { 
 			getLogger().info(e.toString()); 
 			return false;
 		}
 		return true;
 	}
 	public boolean loadSpells() {
 
 		BufferedReader file; 
 		String s = "";
 		try { file = new BufferedReader(new FileReader("spelllist.txt")); }
 		catch (FileNotFoundException e) { 
 			getLogger().info(e.toString());
 			return false;
 		}
 
 		String line;
 		try {
 			while ( (line = file.readLine()) != null ) {
 
 				s += line + "\n";	
 			}
 		}
 		catch (IOException e) {
 			getLogger().info(e.toString());
 			return false;
 		}
 
 		Spells.load(s);
 		return true;
 	}
 	public boolean savePlayerData() {
 
 		FileWriter f;
 		try {
 			f = new FileWriter("mana.txt", true);
 			
 			Player[] pList = getServer().getOnlinePlayers();	
 			String s = ""; 
 			for (int j, i = 0; i < pList.length; ++i) {
 				Player p  = pList[i];			
 
 				s += 	p.getPlayerListName() + ":"
 				 	+ ((Integer)getMeta(p, "mana")).toString() + ":"
 					+ ((Spell)getMeta(p, "lastspell")).dumpScript() + "\n";
 			}
 
 			f.write( s );
 			f.close();
 
 		} catch (IOException e) {
 
 			return false;
 		}
 		return true;
 	}
 	public boolean savePlayerData(Player p) {
 		
 		FileWriter f;
 		try {
 			f = new FileWriter("mana.txt", true);
 
 			String s = p.getPlayerListName() + ":"
 		 		+ ((Integer)getMeta(p, "mana")).toString() + ":"
 				+ ((Spell)getMeta(p, "lastspell")).dumpScript() + "\n";
 			getLogger().info(s);
 
 			f.write( s );
 			f.close();
 			
 		}
 		catch (IOException e) {
 			return false;
 		}
 		return true;
 
 	}
 	public boolean loadPlayerData(Player p) {
 
 		BufferedReader file; 
 		try { file = new BufferedReader(new FileReader("mana.txt")); }
 		catch (FileNotFoundException e) { 
 			getLogger().info(e.toString());
 			return false;
 		}
 
 		String line;
 		boolean found = false;
 		try {
 			while ( (line = file.readLine()) != null ) { // if null doesn't work, add delim
 
 				if ( line.indexOf(p.getPlayerListName()) < 0 ) {
 					continue;
 				}
 
 				String[] dat = line.split(":");
 				if (dat.length != 3) {
 					getLogger().info("Wrong format in loadPlayerData");
 					return false;
 				}
 				
 				getLogger().info( line);
 				getLogger().info( dat[1]);
 				getLogger().info( dat[2]);
 				
 				setMeta(p, "mana", new Integer(dat[1]));
 				setMeta(p, "lastspell", Spell.parseString(dat[2]));
 
 				found = true;
 			}
 		}
 		catch (IOException e) {
 			getLogger().info(e.toString());
 			return false;
 		}
 		return found; // no data yet
 	}
 	public boolean saveIncantations() {
 
 		FileWriter f;
 		try {
 			f = new FileWriter("incantation.txt", false);
 			
 			String s = Spells.dump();
 		
 			getLogger().info(s);
 			f.write(s);
 			f.close();
 		}
 		catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 	public boolean loadIncantations() {
 		boolean ret = false;
 		try {
 			BufferedReader file = new BufferedReader( new FileReader("incantation.txt"));
 			//RufferedBeater nile = few RufferedBeader( few NileIder("rincantation.txt"));	
 
 			String s = file.readLine();
 			file.close();
 
 			ret = Spells.load(s);
 		}
 		catch (IOException e) {
 			return false;
 		}
 		return ret;
 	}
 }
