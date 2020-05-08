 package com.ftwinston.KillerMinecraft;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 import com.ftwinston.KillerMinecraft.Configuration.TeamInfo;
 
 class GameConfiguration
 {
 	private Game game;
 	EnumMap<Menu, Inventory> inventories = new EnumMap<GameConfiguration.Menu, Inventory>(Menu.class);
 	
 	ItemStack backItem;
 	ItemStack rootGameMode, rootGameModeConfig, rootWorldGen, rootWorldGenConfig, rootPlayerNumbers, rootMonsters, rootAnimals;
 	ItemStack[] gameModeItems, worldGenItems, monsterItems, animalItems;
 	
 	public GameConfiguration(Game game)
 	{
 		this.game = game;
 		createMenus();
 	}
 	
 	enum Menu
 	{
 		ROOT(true),
 		GAME_MODE(true),
 		GAME_MODE_CONFIG(true),
 		WORLD_GEN(true),
 		WORLD_GEN_CONFIG(true),
 		PLAYERS(true),
 		MONSTERS(true),
 		ANIMALS(true),
 		SPECIFIC_OPTION_CHOICE(true),
 		
 		TEAM_SELECTION(false);
 		
 		public final boolean onlyOneViewer;
 		Menu(boolean onlyOneViewer)
 		{
 			this.onlyOneViewer = onlyOneViewer;
 		}
 	}
 	
 	private static String /*loreStyle = "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC,*/ highlightStyle = "" + ChatColor.YELLOW + ChatColor.ITALIC;
 	Menu currentMenu = Menu.ROOT;
 	private final short playerHeadDurability = 3; 
 	
	void createMenus()
 	{
 		backItem = new ItemStack(Material.IRON_DOOR);
 		setNameAndLore(backItem, highlightStyle + "Go back", "Return to the previous menu");
 		
 		createGameModeMenu();
 		createWorldGenMenu();
 		createPlayersMenu();
 		createMonstersMenu();
 		createAnimalsMenu();
 		createRootMenu();
 		
 		createTeamMenu();
 	}
 	
 	private void createRootMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, game.getName() + " configuration");
 		inventories.put(Menu.ROOT, menu);
 		
 		rootGameMode = new ItemStack(Material.CAKE);
 		rootGameModeConfig = new ItemStack(Material.DISPENSER);
 		rootWorldGen = new ItemStack(Material.GRASS);
 		rootWorldGenConfig = new ItemStack(Material.DROPPER);
 		//rootMutators = new ItemStack(Material.EXP_BOTTLE);
 		rootPlayerNumbers = new ItemStack(Material.SKULL_ITEM, 1, playerHeadDurability); // steve head
 		rootMonsters = new ItemStack(Material.SKULL_ITEM, 1, (short)4); // creeper head
 		rootAnimals = new ItemStack(Material.EGG);
 		
 		setNameAndLore(rootGameMode, "Change Game Mode", "");
 		setNameAndLore(rootGameModeConfig, "Configure Game Mode", "Any options the current game", "mode has can be configured here");
 		setNameAndLore(rootWorldGen, "Change World Generator", "");
 		setNameAndLore(rootWorldGenConfig, "Configure World Generator", "Any options the current world", "generator has can be configured here");
 		//setNameAndLore(rootMutators, "Select Mutators", "Mutators change specific aspects", "of a game, but aren't specific", "to any particular game mode");
 		setNameAndLore(rootPlayerNumbers, "Player limits", "Specify the maximum number of", "players allowed into the game");
 		setNameAndLore(rootMonsters, "Monster Numbers", "Control the number of", "monsters that spawn");
 		setNameAndLore(rootAnimals, "Animal Numbers", "Control the number of", "animals that spawn");
 		
 		gameModeChanged(game.getGameMode());
 		worldGeneratorChanged(game.getWorldGenerator());
 		
 		menu.setItem(0, rootGameMode);
 		menu.setItem(1, rootGameModeConfig);
 		//menu.setItem(4, rootMutators);
 		menu.setItem(6, rootPlayerNumbers);
 		menu.setItem(7, rootMonsters);
 		menu.setItem(8, rootAnimals);
 	}
 	
 	private void createGameModeMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(GameMode.gameModes.size() + 2), "Game mode selection");
 		menu.setItem(0, backItem);
 		inventories.put(Menu.GAME_MODE, menu);
 		
 		gameModeItems = new ItemStack[GameMode.gameModes.size()];
 		for ( int i=0; i<GameMode.gameModes.size(); i++ )
 		{
 			GameModePlugin mode = GameMode.get(i);
 			gameModeItems[i] = new ItemStack(mode.getMenuIcon());
 			setupGameModeItemLore(i, mode, game.getGameMode().getName().equals(mode.getName()));
 		}
 	}
 	
 	private void setupGameModeItemLore(int num, GameModePlugin mode, boolean current) 
 	{
 		ItemStack item = gameModeItems[num];
 		if ( current )
 		{
 			String[] desc = mode.getDescriptionText();
 			ArrayList<String> lore = new ArrayList<String>(desc.length + 1);
 			lore.add(highlightStyle + "Current game mode");
 			for ( int j=0; j<desc.length; j++)
 				lore.add(desc[j]);
 			setNameAndLore(item, mode.getName(), lore);
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		}
 		else
 			setNameAndLore(item, mode.getName(), mode.getDescriptionText());
 
 		inventories.get(Menu.GAME_MODE).setItem(num+2, item);
 	}
 	
 	private void createGameModeConfigMenu(GameMode mode)
 	{
 		Inventory menu = createModuleOptionsMenu(mode);
 		inventories.put(Menu.GAME_MODE_CONFIG, menu);
 	}
 	
 	private void createWorldGenMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(WorldGenerator.worldGenerators.size() + 2), "World generator selection");
 		menu.setItem(0, backItem);
 		inventories.put(Menu.WORLD_GEN, menu);
 		
 		worldGenItems = new ItemStack[WorldGenerator.worldGenerators.size()];
 		for ( int i=0; i<WorldGenerator.worldGenerators.size(); i++ )
 		{
 			WorldGeneratorPlugin world = WorldGenerator.get(i);
 			
 			ItemStack item = new ItemStack(world.getMenuIcon());
 			worldGenItems[i] = item;
 			setupWorldGenItemLore(i, world, game.getWorldGenerator().getName().equals(world.getName()));
 		}
 	}
 
 	private void setupWorldGenItemLore(int num, WorldGeneratorPlugin world, boolean current)
 	{
 		ItemStack item = worldGenItems[num];
 		if ( current )
 		{
 			String[] desc = world.getDescriptionText();
 			ArrayList<String> lore = new ArrayList<String>(desc.length + 1);
 			lore.add(highlightStyle + "Current world generator");
 			for ( int j=0; j<desc.length; j++)
 				lore.add(desc[j]);
 			setNameAndLore(item, world.getName(), lore);
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		}
 		else
 			setNameAndLore(item, world.getName(), world.getDescriptionText());
 
 		inventories.get(Menu.WORLD_GEN).setItem(num+2, item);
 	}
 
 	private void createWorldGenConfigMenu(WorldGenerator world)
 	{
 		Inventory menu = createModuleOptionsMenu(world);
 		inventories.put(Menu.WORLD_GEN_CONFIG, menu);
 	}
 	
 	private Inventory createModuleOptionsMenu(KillerModule module)
 	{
 		Inventory menu = Bukkit.createInventory(null, nearestNine(module.options.length + 2), module.getName() + " options");
 		generateOptionMenuItems(menu, module.options);
 		return menu;
 	}
 	
 	private void generateOptionMenuItems(Inventory menu, Option[] options)
 	{
 		menu.setItem(0, backItem);
 		
 		for ( int i=0; i<options.length; i++ )
 		{
 			Option option = options[i];
 			ItemStack item = new ItemStack(option.getDisplayMaterial());
 			setNameAndLore(item, option.getName(), option.getDescription());
 			menu.setItem(i+2, item);
 		}
 	}
 
 	private void createPlayersMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, "Player settings");
 		menu.setItem(0, backItem);
 		inventories.put(Menu.PLAYERS, menu);
 		
 		populatePlayersMenu(menu);
 	}
 	
 	private void populatePlayersMenu(Inventory menu)
 	{
 		ItemStack usePlayerLimit = new ItemStack(Material.HOPPER);
 		setNameAndLore(usePlayerLimit, "Use player limit", game.usesPlayerLimit() ? ChatColor.GREEN + "Current limit: " + game.getPlayerLimit() : ChatColor.RED + "No limit set");
 		
 		if ( game.usesPlayerLimit() )
 		{
 			usePlayerLimit = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(usePlayerLimit);
 			if ( game.getPlayerLimit() > 1)
 			{
 				ItemStack playerLimitDown = new ItemStack(Material.BUCKET);
 				setNameAndLore(playerLimitDown, "Decrease player limit", "Change limit to " + (game.getPlayerLimit() - 1));
 				menu.setItem(4, playerLimitDown);
 			}
 			else
 				menu.setItem(4, new ItemStack(Material.AIR));
 			
 			ItemStack playerLimitUp = new ItemStack(Material.MILK_BUCKET);
 			setNameAndLore(playerLimitUp, "Increase player limit", "Change limit to " + (game.getPlayerLimit() + 1));
 			menu.setItem(5, playerLimitUp);
 		}
 		else
 		{
 			menu.setItem(4, new ItemStack(Material.AIR));
 			menu.setItem(5, new ItemStack(Material.AIR));
 		}
 
 		menu.setItem(2, usePlayerLimit);
 		
 		ItemStack lockGame = new ItemStack(Material.IRON_FENCE);
 		if ( game.isLocked() )
 		{
 			setNameAndLore(lockGame, "Unlock the game", "This game is locked, so", "no one else can join,", "even if players leave");
 			lockGame = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(lockGame);
 		}
 		else
 			setNameAndLore(lockGame, "Lock the game", "Lock this game, so that", "no one else can join,", "even if players leave");
 		
 		menu.setItem(7, lockGame);
 		
 		// if game mode allows team selection, a toggle to allow manual team selection (and show the team selection scoreboard) ...
 		// if disabled, players shouldn't see the scoreboard thing, teams will be auto-assigned
 		if ( game.getGameMode().allowTeamSelection() )
 		{
 			ItemStack teamSelection = new ItemStack(Material.DIODE);
 			setNameAndLore(teamSelection, "Allow team selection", "When enabled, players will be", "able to choose their own teams.", "When disabled, teams will be", "allocated randomly.");
 			
 			if ( teamSelectionEnabled() )
 				teamSelection = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(teamSelection);
 			
 			menu.setItem(8, teamSelection);
 		}
 		else
 			menu.setItem(8, new ItemStack(Material.AIR));
 	}
 	
 	final int numQuantityItems = 5;
 	static final String[] animalDescriptions = new String[] { "No animals will spawn", "Reduced animal spawn rate", "Normal animal spawn rate", "High animal spawn rate", "Excessive animal spawn rate" };
 	static final String[] monsterDescriptions = new String[] { "No monsters will spawn", "Reduced monster spawn rate", "Normal monster spawn rate", "High monster spawn rate", "Excessive monster spawn rate" };
 	private void createMonstersMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, "Monster numbers");
 		menu.setItem(0, backItem);
 		
 		monsterItems = new ItemStack[numQuantityItems];
 		for ( int i=0; i<numQuantityItems; i++ )
 		{
 			ItemStack item = monsterItems[i] = createQuantityItem(i, game.monsterNumbers == i, monsterDescriptions[i]);
 			menu.setItem(i+2, item);
 		}
 		
 		inventories.put(Menu.MONSTERS, menu);
 	}
 	
 	private void createAnimalsMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, "Animal numbers");
 		menu.setItem(0, backItem);
 		
 		animalItems = new ItemStack[numQuantityItems];
 		for ( int i=0; i<numQuantityItems; i++ )
 		{
 			ItemStack item = animalItems[i] = createQuantityItem(i, game.animalNumbers == i, animalDescriptions[i]);
 			menu.setItem(i+2, item);
 		}
 		
 		inventories.put(Menu.ANIMALS, menu);
 	}
 	
 	private static final String teamSelectionMenuName = "Select your team"; 
 	private void createTeamMenu()
 	{
 		Inventory menu = Bukkit.createInventory(null, 9, teamSelectionMenuName);
 		
 		ItemStack autoAssign = new ItemStack(Material.MOB_SPAWNER);
 		setNameAndLore(autoAssign, "Auto assign", "Automatically assigns you to", "the team with the fewest", "players, or randomly in the", "event of a tie.");
 		menu.setItem(0, autoAssign);
 		
 		inventories.put(Menu.TEAM_SELECTION, menu);
 		populateTeamMenu();
 	}
 	
 	protected void populateTeamMenu()
 	{
 		Inventory menu = inventories.get(Menu.TEAM_SELECTION);
 		int slot = 1;
 		TeamInfo[] teams = game.getGameMode().getTeams();
 		if ( teams != null )
 			for ( TeamInfo team : teams )
 			{
 				ItemStack item = new ItemStack(Material.WOOL, 1, team.getWoolColor());
 				setNameAndLore(item, team.getChatColor() + team.getName(), "Join the " + team.getName());
 				menu.setItem(slot, item);
 				slot++;
 			}	
 		while ( slot < 9 )
 		{
 			ItemStack item = new ItemStack(Material.AIR);
 			menu.setItem(slot, item);
 			slot++;
 		}
 	}
 	
 	private boolean teamSelectionEnabled = true;
 	boolean teamSelectionEnabled() { return teamSelectionEnabled; }
 
 	private ItemStack createQuantityItem(int quantity, boolean selected, String lore)
 	{
 		ItemStack item = new ItemStack(Material.STICK);
 		
 		if ( selected )
 			setNameAndLore(item, GameInfoRenderer.getQuantityText(quantity), highlightStyle + "Current Setting", lore);
 		else
 			setNameAndLore(item, GameInfoRenderer.getQuantityText(quantity), lore);
 		
 		switch ( quantity )
 		{
 		case 1:
 			item.setType(Material.WOOD_PICKAXE); break;
 		case 2:
 			item.setType(Material.STONE_PICKAXE); break;
 		case 3:
 			item.setType(Material.IRON_PICKAXE); break;
 		case 4:
 			item.setType(Material.DIAMOND_PICKAXE); break;
 		}
 
 		if ( selected )
 			item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);
 		return item;
 	}
 	
 	private void setNameAndLore(ItemStack item, String name, List<String> lore)
 	{
 		ItemMeta meta = item.getItemMeta();
 		meta.setDisplayName(ChatColor.RESET + name);
 		meta.setLore(lore);
 		item.setItemMeta(meta);
 	}
 	
 	private void setNameAndLore(ItemStack item, String name, String... lore)
 	{
 		setNameAndLore(item, name, Arrays.asList(lore));
 	}
 	
 	private void setLore(ItemStack item, List<String> lore)
 	{
 		ItemMeta meta = item.getItemMeta();
 		meta.setLore(lore);
 		item.setItemMeta(meta);
 	}
 	
 	private void setLore(ItemStack item, String... lore)
 	{
 		setLore(item, Arrays.asList(lore));
 	}
 	
 	private int nearestNine(int num)
 	{
 		return 9*(int)Math.ceil(num/9.0);
 	}
 
 	private int findMatch(ItemStack[] set, ItemStack item)
 	{
 		for ( int i=0; i<set.length; i++ )
 		{
 			ItemStack test = set[i];
 			if ( item.getType() == test.getType() && item.getItemMeta().getDisplayName() == test.getItemMeta().getDisplayName() )
 				return i;
 		}
 		return -1;
 	}
 	
 	public void show(Player player)
 	{
 		String configuring = game.getConfiguringPlayer();
 		if ( configuring != null )
 		{
 			if ( !configuring.equals(player.getName()) )
 				player.sendMessage(game.getName() + " is already being configured by " + configuring);
 			
 			return;
 		}
 		
 		game.setConfiguringPlayer(player.getName());
 		showMenu(player, Menu.ROOT);
 	}
 
 	void showMenu(Player player, Menu menu)
 	{
 		Inventory inv = inventories.get(menu);
 		if ( inv != null )
 		{
 			currentMenu = menu;
 			player.openInventory(inv);
 		}
 	}
 	
 	public void gameModeChanged(GameMode gameMode)
 	{
 		setLore(rootGameMode, highlightStyle + "Current mode: " + gameMode.getName(), "The game mode is the main set of rules,", "and controls every aspect of a game.");
 		
 		Inventory root = inventories.get(Menu.ROOT); 
 		root.setItem(0, rootGameMode);
 		createGameModeConfigMenu(gameMode);
 		
 		if ( gameMode.allowWorldGeneratorSelection() )
 		{
 			root.setItem(2, rootWorldGen);
 			root.setItem(3, rootWorldGenConfig);
 		}
 		else
 		{
 			root.remove(2);
 			root.remove(3);
 		}
 		
 		game.scoreboard = createTeamSelectionScoreboard();
 	}
 
 	public void worldGeneratorChanged(WorldGenerator world)
 	{
 		setLore(rootWorldGen, highlightStyle + "Current generator: "+ world.getName(), "The world generator controls", "the terrain in the game's world(s)");
 		inventories.get(Menu.ROOT).setItem(2, rootWorldGen);
 		createWorldGenConfigMenu(world);
 	}
 	
 	static final String unallocatedTeamName = "unallocated";
 	Score unallocatedScore = null;
 	Scoreboard createTeamSelectionScoreboard()
 	{
 		Scoreboard scoreboard;
 		if ( !game.allowTeamSelection() )
 		{
 			scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
 			for ( Player player : game.getOnlinePlayers() )
 				player.setScoreboard(scoreboard);
 			TeamInfo[] teams = game.getGameMode().getTeams();
 			if ( teams != null )
 				for ( TeamInfo teamInfo : teams )
 					teamInfo.setScoreboardScore(null);
 			unallocatedScore = null;
 			return scoreboard;
 		}
 		
 		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
 		List<Player> players = game.getOnlinePlayers();
 		Objective objective = scoreboard.registerNewObjective("teamSizes", "dummy");
 		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
 		objective.setDisplayName("Team sizes");
 		
 		TeamInfo[] teams = game.getGameMode().getTeams();
 		int playerCount;
 		for ( TeamInfo teamInfo : teams )
 		{
 			Team team = scoreboard.registerNewTeam(teamInfo.getName());
 			team.setPrefix(teamInfo.getChatColor().toString());
 			
 			Score score = objective.getScore(Bukkit.getOfflinePlayer(teamInfo.getChatColor() + teamInfo.getName()));
 			playerCount = 0;
 			
 			for ( Player player : players )
 				if ( game.getTeamForPlayer(player) == teamInfo )
 				{
 					team.addPlayer(player);
 					playerCount++;
 				}
 			score.setScore(playerCount);
 			teamInfo.setScoreboardScore(score);
 		}
 		
 		Team unallocated = scoreboard.registerNewTeam(unallocatedTeamName);
 		unallocatedScore = objective.getScore(Bukkit.getOfflinePlayer(unallocatedTeamName));
 		playerCount = 0;
 		
 		for ( Player player : players )
 			if ( game.getTeamForPlayer(player) == null )
 			{
 				unallocated.addPlayer(player);
 				playerCount ++;
 			}
 		
 		unallocatedScore.setScore(playerCount);
 		for ( Player player : game.getOnlinePlayers() )
 			player.setScoreboard(scoreboard);
 
 		return scoreboard;
 	}
 
 	private void rootMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == rootGameMode.getType() )
 			showMenu(player, Menu.GAME_MODE);
 		else if ( item.getType() == rootGameModeConfig.getType() )
 			showMenu(player, Menu.GAME_MODE_CONFIG);
 		else if ( item.getType() == rootWorldGen.getType() )
 			showMenu(player, Menu.WORLD_GEN);
 		else if ( item.getType() == rootWorldGenConfig.getType() )
 			showMenu(player, Menu.WORLD_GEN_CONFIG);
 		else if ( item.getType() == rootPlayerNumbers.getType() && item.getDurability() == playerHeadDurability )
 			showMenu(player, Menu.PLAYERS);
 		else if ( item.getType() == rootMonsters.getType() )
 			showMenu(player, Menu.MONSTERS);
 		else if ( item.getType() == rootAnimals.getType() )
 			showMenu(player, Menu.ANIMALS);
 	}
 
 	private void gameModeMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		int i = findMatch(gameModeItems, item);
 		if ( i < 0 )
 			return;
 
 		GameModePlugin mode = GameMode.get(i); 
 		GameModePlugin prev = (GameModePlugin)game.getGameMode().getPlugin();
 		game.setGameMode(mode);
 		teamSelectionEnabled = true;
 		populatePlayersMenu(inventories.get(Menu.PLAYERS)); // repopulate players menu so that "team selection" item is present (or not) depending on new game mode
 		populateTeamMenu();
 		if ( !game.getGameMode().allowTeamSelection() )
 		{// close team selection menu for anyone that has it open
 			Inventory menu = inventories.get(Menu.TEAM_SELECTION);
 			for ( HumanEntity viewer : menu.getViewers() )
 				viewer.closeInventory();
 		}
 		
 		showMenu(player, Menu.GAME_MODE_CONFIG);
 		
 		// update which item gets the "current game mode" lore text on the game mode menu
 		setupGameModeItemLore(GameMode.indexOf(prev), prev, false);
 		setupGameModeItemLore(i, mode, true);
 	}
 	
 	private void gameModeConfigClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		Option option = game.getGameMode().options[itemSlot-2];
 			
 		ItemStack[] choiceItems = option.optionClicked();
 		if ( choiceItems != null )
 			showChoiceOptionMenu(player, option, choiceItems, Menu.GAME_MODE_CONFIG);
 		else
 			generateOptionMenuItems(inventories.get(Menu.GAME_MODE_CONFIG), game.getGameMode().options);
 	}
 	
 	private void worldGenMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		int i = findMatch(worldGenItems, item);
 		if ( i < 0 )
 			return;
 
 		WorldGeneratorPlugin generator = WorldGenerator.get(i); 
 		WorldGeneratorPlugin prev = (WorldGeneratorPlugin)game.getWorldGenerator().getPlugin();
 		game.setWorldGenerator(generator);
 		showMenu(player, Menu.WORLD_GEN_CONFIG);
 		
 		// update which item gets the "current world generator" lore text
 		setupWorldGenItemLore(WorldGenerator.indexOf(prev), prev, false);
 		setupWorldGenItemLore(i, generator, true);
 	}
 
 	private void worldGenConfigClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 
 		Option option = game.getWorldGenerator().options[itemSlot-2];
 			
 		ItemStack[] choiceItems = option.optionClicked();
 		if ( choiceItems != null )
 			showChoiceOptionMenu(player, option, choiceItems, Menu.WORLD_GEN_CONFIG);
 		else
 			generateOptionMenuItems(inventories.get(Menu.WORLD_GEN_CONFIG), game.getWorldGenerator().options);
 	}
 	
 	private void playersMenuClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		if ( itemSlot == 2 )
 		{
 			if ( game.usesPlayerLimit() )
 				game.setUsesPlayerLimit(false);
 			else
 			{
 				game.setUsesPlayerLimit(true);
 				game.setPlayerLimit(game.getOnlinePlayers().size());
 			}
 		}
 		else if ( itemSlot == 4 )
 			game.setPlayerLimit(Math.max(1, game.getPlayerLimit()-1));
 		else if ( itemSlot == 5 )
 			game.setPlayerLimit(game.getPlayerLimit()+1);
 		else if ( itemSlot == 7 )
 			game.setLocked(!game.isLocked());
 		else if ( itemSlot == 8 )
 		{
 			teamSelectionEnabled = !teamSelectionEnabled;
 			game.scoreboard = createTeamSelectionScoreboard();
 		}
 		
 		populatePlayersMenu(inventories.get(Menu.PLAYERS));
 	}
 
 	private void monstersMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		for ( int i=0; i<numQuantityItems; i++ )
 			if ( item.getType() == monsterItems[i].getType() && item.getItemMeta().getDisplayName() == monsterItems[i].getItemMeta().getDisplayName() )
 			{
 				if ( game.monsterNumbers == i )
 					return;
 				
 				int prev = game.monsterNumbers;
 				game.monsterNumbers = i;
 				
 				item = monsterItems[i] = createQuantityItem(i, true, monsterDescriptions[i]);
 				inventories.get(Menu.MONSTERS).setItem(i+2, item);
 				item = monsterItems[prev] = createQuantityItem(prev, false, monsterDescriptions[prev]);
 				inventories.get(Menu.MONSTERS).setItem(prev+2, item);
 				
 				game.miscRenderer.allowForChanges();
 				return;
 			}
 	}
 	
 	private void animalsMenuClicked(Player player, ItemStack item)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			showMenu(player, Menu.ROOT);
 			return;
 		}
 		
 		for ( int i=0; i<numQuantityItems; i++ )
 			if ( item.getType() == animalItems[i].getType() && item.getItemMeta().getDisplayName() == animalItems[i].getItemMeta().getDisplayName() )
 			{
 				if ( game.animalNumbers == i )
 					return;
 				
 				int prev = game.animalNumbers;
 				game.animalNumbers = i;
 				
 				item = animalItems[i] = createQuantityItem(i, true, animalDescriptions[i]);
 				inventories.get(Menu.ANIMALS).setItem(i+2, item);
 				item = animalItems[prev] = createQuantityItem(prev, false, animalDescriptions[prev]);
 				inventories.get(Menu.ANIMALS).setItem(prev+2, item);
 				
 				game.miscRenderer.allowForChanges();
 				return;
 			}
 	}
 	
 	Menu choiceOptionGoBackTo;
 	Option currentOption;
 	private void choiceOptionMenuClicked(Player player, ItemStack item, int itemSlot)
 	{
 		if ( item.getType() == backItem.getType() )
 		{
 			currentOption = null;
 			inventories.put(Menu.SPECIFIC_OPTION_CHOICE, null);
 			
 			if ( choiceOptionGoBackTo == Menu.GAME_MODE_CONFIG )
 				generateOptionMenuItems(inventories.get(choiceOptionGoBackTo), game.getGameMode().options);
 			else if ( choiceOptionGoBackTo == Menu.WORLD_GEN_CONFIG ) 
 				generateOptionMenuItems(inventories.get(choiceOptionGoBackTo), game.getWorldGenerator().options);
 			
 			showMenu(player, choiceOptionGoBackTo);
 			return;
 		}
 		
 		currentOption.setSelectedIndex(itemSlot-2);
 		populateChoiceOptionMenu(currentOption.optionClicked(), currentOption.getSelectedIndex(), inventories.get(Menu.SPECIFIC_OPTION_CHOICE));
 	
 		// which of these could have changed depends on if this is an option for the game mode or world generator 
 		game.modeRenderer.allowForChanges();
 		game.miscRenderer.allowForChanges();
 	}
 	
 	private void showChoiceOptionMenu(Player player, Option option, ItemStack[] choiceItems, Menu goBackTo)
 	{
 		choiceOptionGoBackTo = goBackTo;
 		currentOption = option;
 		Inventory menu = Bukkit.createInventory(null, nearestNine(choiceItems.length + 2), option.getName());
 		menu.setItem(0, backItem);	
 		
 		populateChoiceOptionMenu(choiceItems, option.getSelectedIndex(), menu);
 		
 		inventories.put(Menu.SPECIFIC_OPTION_CHOICE, menu);
 		currentMenu = Menu.SPECIFIC_OPTION_CHOICE;
 		player.openInventory(menu);
 	}
 	
 	private void teamSelectionClicked(Player player, int selectedIndex)
 	{
 		if ( selectedIndex == 0 )
 		{
 			game.getGameMode().setTeam(player, null);
 			return;
 		}
 		
 		TeamInfo[] teams = game.getGameMode().getTeams();
 		if ( selectedIndex > teams.length )
 			return;
 		
 		TeamInfo team = teams[selectedIndex-1];
 		game.getGameMode().setTeam(player, team);
 	}
 
 	private void populateChoiceOptionMenu(ItemStack[] choiceItems, int selectedIndex, Inventory menu)
 	{
 		for ( int i=0; i<choiceItems.length; i++ )
 		{
 			ItemStack item = choiceItems[i];
 			if ( selectedIndex == i )
 				item = KillerMinecraft.instance.craftBukkit.setEnchantmentGlow(item);				
 			menu.setItem(i+2, item);
 		}
 	}
 
 	private static Game getGameByConfiguringPlayer(Player player)
 	{	
 		for ( Game game : KillerMinecraft.instance.games )
 		{
 			String name = game.getConfiguringPlayer();
 			 
 			if ( name != null && name.equals(player.getName()) )
 				return game;
 		}
 		return null;
 	}
 	
 	public static void checkEvent(InventoryClickEvent event)
 	{
 		if ( !(event.getWhoClicked() instanceof Player) || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR )
 			return;
 		
 		// team selection menu clicked ... well, it depends what team the player is in, which menu to show.
 		// but we need to get the game before we can see if its a team selection inventory or not.
 		// making getGameByConfiguringPlayer a bit of a waste of time, instead we just want
 		// getGameByPlayer and isConfiguringPlayer.
 		
 		Player player = (Player)event.getWhoClicked();
 		Game game = KillerMinecraft.instance.getGameForPlayer(player);
 		
 		if ( game == null )
 			return;
 		
 		if ( event.getInventory().getSize() == 9 && event.getInventory().getName().equals(teamSelectionMenuName))
 		{
 			if ( outOfRange(event, player) )
 				return;
 			
 			game.configuration.teamSelectionClicked(player, event.getRawSlot());
 			return;
 		}
 		
 		String configuringPlayer = game.getConfiguringPlayer();
 		if ( configuringPlayer == null ||  !configuringPlayer.equals(player.getName()) )
 			return;
 
 		if ( outOfRange(event, player) )
 			return;
 		
 		switch (game.configuration.currentMenu)
 		{
 		case ROOT:
 			game.configuration.rootMenuClicked(player, event.getCurrentItem()); break;
 		case GAME_MODE:
 			game.configuration.gameModeMenuClicked(player, event.getCurrentItem()); break;
 		case GAME_MODE_CONFIG:
 			game.configuration.gameModeConfigClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		case WORLD_GEN:
 			game.configuration.worldGenMenuClicked(player, event.getCurrentItem()); break;
 		case WORLD_GEN_CONFIG:
 			game.configuration.worldGenConfigClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		case PLAYERS:
 			game.configuration.playersMenuClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		case MONSTERS:
 			game.configuration.monstersMenuClicked(player, event.getCurrentItem()); break;
 		case ANIMALS:
 			game.configuration.animalsMenuClicked(player, event.getCurrentItem()); break;
 		case SPECIFIC_OPTION_CHOICE:
 			game.configuration.choiceOptionMenuClicked(player, event.getCurrentItem(), event.getRawSlot()); break;
 		}
 	}
 
 	private static boolean outOfRange(InventoryClickEvent event, Player player)
 	{
 		event.setCancelled(true);
 		if ( event.getRawSlot() >= event.getInventory().getSize() ) // click in player's own inventory
 		{
 			player.closeInventory();
 			return true;
 		}
 		return false;
 	}
 
 	public static void checkEvent(InventoryCloseEvent event)
 	{
 		if ( !(event.getPlayer() instanceof Player) )
 			return;
 		
 		Player player = (Player)event.getPlayer();
 		Game game = getGameByConfiguringPlayer(player);
 		if ( game == null )
 			return;
 		
 		// only clear the configuring player if the closed inventory is the "current" one (decided by checking the name)
 		if ( event.getInventory().getName().equals(game.configuration.inventories.get(game.configuration.currentMenu).getName()) )
 			game.setConfiguringPlayer(null);		
 	}
 	
 	private static void checkClearConfiguringPlayer(Player player)
 	{
 		Game game = getGameByConfiguringPlayer(player);
 		if ( game != null )
 		{
 			game.setConfiguringPlayer(null);
 			player.closeInventory();
 		}
 	}
 	
 	public static void checkEvent(PlayerChangedWorldEvent event)
 	{
 		checkClearConfiguringPlayer(event.getPlayer());
 	}
 	
 	public static void checkEvent(PlayerDeathEvent event)
 	{
 		checkClearConfiguringPlayer(event.getEntity());
 	}
 	
 	public static void checkEvent(PlayerQuitEvent event)
 	{
 		checkClearConfiguringPlayer(event.getPlayer());
 	}
 }
