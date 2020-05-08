 package com.legit2.Demigods.Deities;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.Libraries.ReflectCommand;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 import com.legit2.Demigods.Utilities.DUtil;
 
 public class Template_deity implements Listener
 {	
 	// Create required universal deity variables
 	private static final String DEITYNAME = "Template";
 	private static final String DEITYALLIANCE = "Test";
 	private static final ChatColor DEITYCOLOR = ChatColor.BLACK;
 
 	/*
 	 *  Set deity-specific ability variable(s).
 	 */
 	// "/testabil" Command:
 	private static String TEST_NAME = "Testabil"; // Sets the name of this command
 	private static long TEST_TIME; // Creates the variable for later use
 	private static final int TEST_COST = 170; // Cost to run command in "favor"
 	private static final int TEST_DELAY = 0; // In milliseconds
 
 	// "/testult" Command:
 	private static String ULTIMATE_NAME = "Testult";
 	private static long ULTIMATE_TIME; // Creates the variable for later use
 	private static final int ULTIMATE_COST = 3700; // Cost to run command in "favor"
 	private static final int ULTIMATE_COOLDOWN_MAX = 600; // In seconds
 	private static final int ULTIMATE_COOLDOWN_MIN = 60; // In seconds
 	
 	public String loadDeity()
 	{
 		DUtil.plugin.getServer().getPluginManager().registerEvents(this, DUtil.plugin);
 		ULTIMATE_TIME = System.currentTimeMillis();
 		TEST_TIME = System.currentTimeMillis();
 		return DEITYNAME + " loaded.";
 	}
 	
 	public ArrayList<Material> getClaimItems()
 	{
 		ArrayList<Material> claimItems = new ArrayList<Material>();
 		
 		claimItems.add(Material.BEDROCK);
 
 		return claimItems;
 	}
 
 	public ArrayList<String> getInfo(Player player)
 	{		
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		if(DUtil.canUseDeitySilent(player, DEITYNAME))
 		{
 			toReturn.add(ChatColor.YELLOW + "[Demigods] " + ChatColor.AQUA + DEITYNAME); //TODO
 			toReturn.add(ChatColor.GREEN + "You are a follower of " + DEITYNAME + "!");
 			
 			return toReturn;
 		}
 		else
 		{
 			// Get Claim Item Names from ArrayList
 			ArrayList<String> claimItemNames = new ArrayList<String>();
 			for(Material item : getClaimItems())
 			{
 				claimItemNames.add(item.name());
 			}
 			
 			// Make Claim Items readable.
 			String claimItems = Joiner.on(", ").join(claimItemNames);
 			
 			toReturn.add(ChatColor.YELLOW + "[Demigods] " + ChatColor.AQUA + DEITYNAME); //TODO
 			toReturn.add("Claim Items: " + claimItems);
 			
 			return toReturn;
 		}
 	}
 
	// This sets the particular passive ability for the Template_deity deity.
 	// Whether or not this is used, along with other things, will vary depending
 	// on the particular deity you're creating.
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDamange(EntityDamageEvent damageEvent)
 	{
 		if(damageEvent.getEntity() instanceof Player)
 		{
 			Player player = (Player)damageEvent.getEntity();
 			if(!DUtil.canUseDeitySilent(player, DEITYNAME)) return;
 			
 			// If the player receives falling damage, cancel it
 			if(damageEvent.getCause() == DamageCause.FALL)
 			{
 				damageEvent.setDamage(0);
 				return;
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onPlayerInteract(PlayerInteractEvent interactEvent)
 	{
 		// Set variables
 		Player player = interactEvent.getPlayer();
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(!DUtil.canUseDeitySilent(player, DEITYNAME)) return;
 
 		if(DCharUtil.isEnabledAbility(player, TEST_NAME) || ((player.getItemInHand() != null) && (player.getItemInHand().getType() == DCharUtil.getBind(player, TEST_NAME))))
 		{
 			if(!DCharUtil.isCooledDown(player, TEST_NAME, TEST_TIME, true)) return;
 
 			// Set the ability's delay
 			TEST_TIME = System.currentTimeMillis() + TEST_DELAY;
 
 			// Check to see if player has enough favor to perform ability
 			if(DCharUtil.getFavor(player, charID) >= TEST_COST)
 			{
 				testabil(player);
 				DCharUtil.subtractFavor(player, charID, TEST_COST);
 				return;
 			}
 			else
 			{
 				player.sendMessage(ChatColor.YELLOW + "You do not have enough " + ChatColor.GREEN + "favor" + ChatColor.RESET + ".");
 				DCharUtil.disableAbility(player, TEST_NAME);
 			}
 		}
 	}
 
 	/* ------------------
 	 *  Command Handlers
 	 * ------------------
 	 *
 	 *  Command: "/test"
 	 */
 	@ReflectCommand.Command(name = "testabil", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME)
 	public static void testCommand(Player player, String arg1)
 	{		
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		if(arg1.equalsIgnoreCase("bind"))
 		{		
 			// Bind item
 			DCharUtil.setBound(player, TEST_NAME, player.getItemInHand().getType());
 		}
 		else
 		{
 			if(DCharUtil.isEnabledAbility(player, TEST_NAME))
 			{
 				DCharUtil.disableAbility(player, TEST_NAME);
 				player.sendMessage(ChatColor.YELLOW + TEST_NAME + " is no longer active.");
 			}
 			else
 			{
 				DCharUtil.enableAbility(player, TEST_NAME);
 				player.sendMessage(ChatColor.YELLOW + TEST_NAME + " is now active.");
 			}
 		}
 	}
 
 	// The actual ability command
 	public static void testabil(Player player)
 	{
 		player.sendMessage(ChatColor.YELLOW + "You just used the \"" + TEST_NAME.toLowerCase() + "\" ability!");
 	}
 
 	/*
 	 *  Command: "/testult"
 	 */
 	@ReflectCommand.Command(name = "testult", sender = ReflectCommand.Sender.PLAYER, permission = "demigods." + DEITYALLIANCE + "." + DEITYNAME + ".ultimate")
 	public static void ultimateCommand(Player player)
 	{		
 		// Set variables
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(!DUtil.canUseDeity(player, DEITYNAME)) return;
 
 		// Check if the ultimate has cooled down or not
 		if(System.currentTimeMillis() < ULTIMATE_TIME)
 		{
 			player.sendMessage(ChatColor.YELLOW + "You cannot use the " + DEITYNAME + " ultimate again for " + ((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000)))/60 + " minutes");
 			player.sendMessage(ChatColor.YELLOW + "and "+((((ULTIMATE_TIME)/1000)-(System.currentTimeMillis()/1000))%60)+" seconds.");
 			return;
 		}
 
 		// Perform ultimate if there is enough favor
 		if(DCharUtil.getFavor(player, charID) >= ULTIMATE_COST)
 		{
 			if(!DUtil.canLocationPVP(player.getLocation()))
 			{
 				testHelper(player);
 				player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
 				return;
 			}
 
 			player.sendMessage(ChatColor.YELLOW + "You just used the ultimate for " + DEITYNAME + "!");
 
 			// Set favor and cooldown
 			DCharUtil.subtractFavor(player, charID, ULTIMATE_COST);
 			player.setNoDamageTicks(1000);
 			int cooldownMultiplier = (int)(ULTIMATE_COOLDOWN_MAX - ((ULTIMATE_COOLDOWN_MAX - ULTIMATE_COOLDOWN_MIN) * ((double) DCharUtil.getAscensions(player, charID) / 100)));
 			ULTIMATE_TIME = System.currentTimeMillis() + cooldownMultiplier * 1000;
 		}
 		// Give a message if there is not enough favor
 		else player.sendMessage(ChatColor.YELLOW + ULTIMATE_NAME + " requires " + ULTIMATE_COST + ChatColor.GREEN + " favor" + ChatColor.RESET + ".");
 	}
 
 	/*
 	 * Command Helper Methods
 	 */
 	private static void testHelper(Player player)
 	{
 		player.sendMessage(ChatColor.YELLOW + "This command called a helper!");
 	}
 
 	// Don't touch these, they're required to work.
 	public static String getName() { return DEITYNAME; }
 	public static String getAlliance() { return DEITYALLIANCE; }
 	public static ChatColor getColor() { return DEITYCOLOR; }
 }
