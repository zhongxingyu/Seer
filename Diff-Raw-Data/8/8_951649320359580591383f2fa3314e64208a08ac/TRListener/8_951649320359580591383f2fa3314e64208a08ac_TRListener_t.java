 package com.github.dreadslicer.tekkitrestrict;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.dreadslicer.tekkitrestrict.commands.TRCommandAlc;
 import com.github.dreadslicer.tekkitrestrict.lib.TRNoClick;
 
 import eloraam.core.TileCovered;
 
 public class TRListener implements Listener {
 	private static TRListener instance;
 	boolean SSInnvincible, UseBlockLimit;
 	boolean LogAmulets, LogRings, LogDMTools, LogRMTools, LogEEMisc;
 	boolean AntiFly, AntiForcefield;
 	private Map<Integer, String> EENames = Collections
 			.synchronizedMap(new HashMap<Integer, String>());
 	private int[] Exceptions = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
 			11, 12, 13, 17, 24, 35, 44, 98, 142 };
 
 	public TRListener() {
 
 		// gah, this took forever to plug in...
 		EENames.put(27526, "Philosopher Stone");
 		EENames.put(27527, "Destruction Catalyst");
 		EENames.put(27528, "Iron Band");
 		EENames.put(27529, "Soul Stone");
 		EENames.put(27530, "Evertide Amulet");
 		EENames.put(27531, "Volcanite Amulet");
 		EENames.put(27532, "Black Hole Band");
 		EENames.put(27533, "Ring of Ignition");
 		EENames.put(27534, "Archangel's Smite");
 		EENames.put(27535, "Hyperkinetic Lens");
 
 		EENames.put(27536, "SwiftWolf's Rending Gale");
 		EENames.put(27537, "Harvest Ring");
 		EENames.put(27538, "Watch of Flowing Time");
 		EENames.put(27539, "Alchemical Coal");
 		EENames.put(27540, "Mobius Fuel");
 		EENames.put(27541, "Dark Matter");
 		EENames.put(27542, "Covalence Dust");
 
 		EENames.put(27543, "Dark Matter Pickaxe");
 		EENames.put(27544, "Dark Matter Spade");
 		EENames.put(27545, "Dark Matter Hoe");
 		EENames.put(27546, "Dark Matter Sword");
 		EENames.put(27547, "Dark Matter Axe");
 		EENames.put(27548, "Dark Matter Shears");
 
 		EENames.put(27549, "Dark Matter Armor");
 		EENames.put(27550, "Dark Matter Helmet");
 		EENames.put(27551, "Dark Matter Greaves");
 		EENames.put(27552, "Dark Matter Boots");
 
 		EENames.put(27553, "Gem of Eternal Density");
 		EENames.put(27554, "Repair Talisman");
 		EENames.put(27555, "Dark Matter Hammer");
 		EENames.put(27556, "Cataclyctic Lens");
 		EENames.put(27557, "Klien Star Ein");
 		EENames.put(27558, "Klien Star Zwei");
 		EENames.put(27559, "Klien Star Drei");
 		EENames.put(27560, "Klien Star Vier");
 		EENames.put(27561, "Klien Star Sphere");
 		EENames.put(27591, "Klien Star Omega");
 		EENames.put(27562, "Alchemy Bag");
 		EENames.put(27563, "Red Matter");
 		EENames.put(27564, "Red Matter Pickaxe");
 		EENames.put(27565, "Red Matter Spade");
 		EENames.put(27566, "Red Matter Hoe");
 		EENames.put(27567, "Red Matter Sword");
 		EENames.put(27568, "Red Matter Axe");
 		EENames.put(27569, "Red Matter Shears");
 		EENames.put(27570, "Red Matter Hammer");
 		EENames.put(27571, "Arternalis Fue;");
 		EENames.put(27572, "Red Matter Katar");
 		EENames.put(27573, "Red Matter Morning Star");
 		EENames.put(27574, "Zero Ring");
 		EENames.put(27575, "Red Matter Armor");
 		EENames.put(27576, "Red Matter Helmet");
 		EENames.put(27577, "Red Matter Greaves");
 		EENames.put(27578, "Red Matter Boots");
 		EENames.put(27579, "Infernal Armor (Gem)");
 		EENames.put(27580, "Abyss Helmet (Gem)");
 		EENames.put(27581, "Gravity Greaves (Gem)");
 		EENames.put(27582, "Hurricane Boots (Gem)");
 		EENames.put(27583, "Mercurial Eye");
 		EENames.put(27584, "Ring of Arcana");
 		EENames.put(27585, "Divining Rod");
 		EENames.put(27588, "Body Stone");
 		EENames.put(27589, "Life Stone");
 		EENames.put(27590, "Mind Stone");
 		EENames.put(27592, "Transmutation Tablet");
 		EENames.put(27593, "Void Ring");
 		EENames.put(27594, "Alchemy Tome");
 		instance = this;
 	}
 
 	public static void reload() {
 		instance.SSInnvincible = tekkitrestrict.config
 				.getBoolean("SSInvincible");
 		instance.UseBlockLimit = tekkitrestrict.config
 				.getBoolean("UseItemLimiter");
 
 		instance.LogAmulets = tekkitrestrict.config.getBoolean("LogAmulets");
 		instance.LogRings = tekkitrestrict.config.getBoolean("LogRings");
 		instance.LogDMTools = tekkitrestrict.config.getBoolean("LogDMTools");
 		instance.LogRMTools = tekkitrestrict.config.getBoolean("LogRMTools");
 		instance.LogEEMisc = tekkitrestrict.config.getBoolean("LogEEMisc");
 		instance.AntiFly = tekkitrestrict.config.getBoolean("UseAntiFlyHack");
 		instance.AntiForcefield = tekkitrestrict.config
 				.getBoolean("UseAntiForcefield");
 
 		TRNoClick.reload();
 		TRNoDupe_BagCache.reload();
 	}
 
 	public static TRListener getInstance() {
 		return instance;
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent e) {
 		// forget about basic types!
 		for (int eee : Exceptions) {
 			if (e.getBlock().getTypeId() == eee) {
 				return;
 			}
 		}
 		
 		String[] exempt = new String[] { "[buildcraft]", "[redpower]" };
 		try{
 		if (e.getPlayer() != null) {
 			Player player = e.getPlayer();
 			for (String ex : exempt) {
 				if (ex == player.getName().toLowerCase()) {
 					return;
 				}
 			}
 		}
 		} catch(Exception eee){}
 		try{
 			if (UseBlockLimit) {
 				String pl = TRLimitBlock.getPlayerAt(e.getBlock());
 				//tekkitrestrict.log.info(pl);
 				if (pl != null) {
 					TRLimitBlock il = TRLimitBlock.getLimiter(pl);
 					il.checkBreakLimit(e);
 				}
 			}
 		} catch(Exception eee){}
 	}
 
 	int lastdata = 0;
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e) {
 		// forget about basic types!
 		for (int eee : Exceptions) {
 			if (e.getBlock().getTypeId() == eee) {
 				return;
 			}
 		}
 		try{
 			if (e.getPlayer() != null) {
 				Player player = e.getPlayer();
 				TRLWCProtect.checkLWC(e);
 	
 				int id = e.getBlock().getTypeId();
 				int data = e.getBlock().getData();
 				WorldServer ws = ((org.bukkit.craftbukkit.CraftWorld) e.getBlock()
 						.getWorld()).getHandle();
 				Block block = e.getBlock();
 				TileEntity te1 = ws.getTileEntity(block.getX(), block.getY(),
 						block.getZ());
 	
 				if (UseBlockLimit) {
 					TRLimitBlock il = TRLimitBlock.getLimiter(player);
 					if (!il.checkLimit(e)) {
 						if (!TRPermHandler.hasPermission(player, "limiter",
 								"bypass", "")) {
 							player.sendRawMessage("[TRItemLimiter] You cannot place down any more of that block!");
 							e.setCancelled(true);
 							if (te1 instanceof TileCovered) {
 								TileCovered tc = (TileCovered) te1;
 								for (int i = 0; i < 6; i++) {
 									if (tc.getCover(i) != -1
 											&& tc.getCover(i) == data) {
 										tc.tryRemoveCover(i);
 									}
 								}
 								tc.updateBlockChange();
 							}
 						}
 					}
 				}
 	
 				if (te1 != null && data == 0) {
 	
 					if (te1 instanceof TileCovered) {
 						// TileCovered tc = (TileCovered)te1;
 						// tekkitrestrict.log.info("ar "+lastdata);
 						data = lastdata;
 					}
 				}
 				com.github.dreadslicer.tekkitrestrict.ItemStack cc = new com.github.dreadslicer.tekkitrestrict.ItemStack(
 						id, 0, data);
 				if (TRNoItem.isItemBanned(e.getPlayer(), cc)) {
 					// tekkitrestrict.log.info(cc.id+":"+cc.getData());
 					e.getPlayer()
 							.sendRawMessage(
 									"[TRItemDisabler] You cannot place down this type of block!");
 					e.setCancelled(true);
 					if (te1 instanceof TileCovered) {
 						TileCovered tc = (TileCovered) te1;
 						for (int i = 0; i < 6; i++) {
 							if (tc.getCover(i) != -1 && tc.getCover(i) == data) {
 								tc.tryRemoveCover(i);
 							}
 						}
 						tc.updateBlockChange();
 					}
 				}
 			}
 		}
 		catch(Exception eee){}
 		lastdata = e.getBlock().getData();
 	}
 
 	@EventHandler
 	public void onDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
 		try {
 			TRNoDupe.handleDropDupes(event);
 		} catch (Exception e) {
 			TRLogger.Log("debug", "Error! [TRNoDropDupe] : " + e.getMessage());
 		}
 		try{
 			Player player = event.getPlayer();
 			net.minecraft.server.EntityPlayer ep = ((org.bukkit.craftbukkit.entity.CraftPlayer) player)
 					.getHandle();
 			if (ep.abilities.canInstantlyBuild) {
 				if (!TRPermHandler.hasPermission(player, "creative", "bypass", "")) {
 					/*Item ccr = event.getItemDrop();
 					ItemStack ccc = ccr.getItemStack();*/
 					event.setCancelled(true);
 					player.sendMessage("[TRLimitedCreative] You cannot drop items!");
 				}
 			}
 		} catch(Exception e){
 			TRLogger.Log("debug", "Error! [TRLimitedCreative Drop Listener] : " + e.getMessage());
 			for(StackTraceElement eer:e.getStackTrace()){
 				TRLogger.Log("debug","    "+eer.toString()); 
 			}
 		}
 	}
 
 	// /////// START INTERACT //////////////
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
 	public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent e) {
 		if (e.getPlayer() != null) {
 			// determine if this is Buildcraft or RedPower... Then exempt.
 			String pname = e.getPlayer().getName().toLowerCase();
 			if (!pname.equals("[buildcraft]") && !pname.equals("[redpower]")) {
 				// lets do this based on a white-listed approach.
 				// First, lets loop through the DisableClick list to stop
 				// clicks.
 				// Perf: 8x
 				try {
 					TRNoClick.compareAll(e);
 				} catch (Exception e1) {
 					TRLogger.Log("debug", "Error: [ListenInteract TRNoClick] "
 							+ e1.getMessage());
 				}
 				
 				try{TRNoDupeProjectTable.checkTable(e);}catch(Exception eee){}
 
 				try {
 					// if(e.getAction() == Action.RIGHT_CLICK_BLOCK ||
 					// e.getAction() == Action.RIGHT_CLICK_AIR){
 
 					Player player = e.getPlayer();
 					net.minecraft.server.EntityPlayer ep = ((org.bukkit.craftbukkit.entity.CraftPlayer) player)
 							.getHandle();
 					if (ep.abilities.canInstantlyBuild) {
 						if (e.getPlayer().getItemInHand() != null) {
 							org.bukkit.inventory.ItemStack str = e.getPlayer()
 									.getItemInHand();
 							com.github.dreadslicer.tekkitrestrict.ItemStack ee = new com.github.dreadslicer.tekkitrestrict.ItemStack(
 									str.getTypeId(), str.getAmount(), str
 											.getData().getData());
 							if (TRNoItem
 									.isCreativeItemBanned(e.getPlayer(), ee)) {
 								e.getPlayer()
 										.sendRawMessage(
 												"[TRLimitedCreative] You may not interact with this item.");
 								e.setCancelled(true);
 								e.getPlayer().setItemInHand(null);
 							}
 						}
 					}
 					// }
 				} catch (Exception e1) {
 					TRLogger.Log(
 							"debug",
 							"Error: [ListenInteract TRLimitedCreative] "
 									+ e1.getMessage());
 				}
 
 				// Lastly, lets see if it's loggable.
 				// Perf: 19
 				if (!e.isCancelled()) {
 					itemLogUse(e);
 				}
 			}
 		}
 	}
 
 	private void itemLogUse(org.bukkit.event.player.PlayerInteractEvent e) {
 		try {
 			Player p = e.getPlayer();
 			int x = p.getLocation().getBlockX();
 			int y = p.getLocation().getBlockY();
 			int z = p.getLocation().getBlockZ();
 			if (tekkitrestrict.EEEnabled) { // may spare us usage.
 				// log for EE stuffs.
 				ItemStack a = e.getPlayer().getItemInHand();
 				// net.minecraft.server.ItemStack aae =
 				// ((org.bukkit.craftbukkit.inventory.CraftItemStack)a).getHandle();
 				int id = a.getTypeId();
 				if (inRange(id, 27530, 27531)) {
 					TRLogger.Log("EEAmulet", "[" + p.getName() + "]["
 							+ p.getWorld().getName() + "-" + x + "," + y + ","
 							+ z + "] used (" + id + ")`" + EENames.get(id)
 							+ "`");
 				} else if (inRange(id, 27532, 27534) || id == 27536
 						|| id == 27537 || id == 27574 || id == 27584
 						|| id == 27593) {
 					TRLogger.Log("EERing", "[" + p.getName() + "]["
 							+ p.getWorld().getName() + "-" + x + "," + y + ","
 							+ z + "] used (" + id + ")`" + EENames.get(id)
 							+ "`");
 				} else if (e.getAction() != Action.LEFT_CLICK_AIR
 						&& e.getAction() != Action.LEFT_CLICK_BLOCK) {
 					if (inRange(id, 27543, 27548) || id == 27555) {
 						TRLogger.Log("EEDmTool",
 								"[" + p.getName() + "]["
 										+ p.getWorld().getName() + "-" + x
 										+ "," + y + "," + z + "] used (" + id
 										+ ")`" + EENames.get(id) + "`");
 					} else if (inRange(id, 27564, 27573)) {
 						TRLogger.Log("EERmTool",
 								"[" + p.getName() + "]["
 										+ p.getWorld().getName() + "-" + x
 										+ "," + y + "," + z + "] used (" + id
 										+ ")`" + EENames.get(id) + "`");
 					}
 				} else if (id == 27527 || id == 27556 || id == 27535) {
 					TRLogger.Log("EEDestructive", "[" + p.getName() + "]["
 							+ p.getWorld().getName() + "-" + x + "," + y + ","
 							+ z + "] used (" + id + ")`" + EENames.get(id)
 							+ "`");
 				} else if (id == 27538 || id == 27553 || id == 27562
 						|| id == 27583 || id == 27585 || id == 27592) {
 					TRLogger.Log("EEMisc", "[" + p.getName() + "]["
 							+ p.getWorld().getName() + "-" + x + "," + y + ","
 							+ z + "] used (" + id + ")`" + EENames.get(id)
 							+ "`");
 				}
 			}
 		} catch (Exception e1) {
 		}
 	}
 
 	private boolean inRange(int stack, int from, int to) {
 		if (stack >= from && stack <= to) {
 			return true;
 		}
 		return false;
 	}
 
 	// /////////// END INTERACT /////////////
 
 	// /////////// START INVClicks/////////////
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void eventInventoryClick(InventoryClickEvent event) {
 		try {
 			// we want to stop non-players from being activated here.
 			if (event.getWhoClicked() != null) {
 				// Determine if this inventory click is a dupe action:
 				// Perf: 13-27-(9+14x)
 				try {
 					TRNoDupe.handleDupes(event);
 				} catch (Exception e) {
 					TRLogger.Log("debug",
 							"Error! [TRNoDupe] : " + e.getMessage());
 					for(StackTraceElement eer:e.getStackTrace()){
 						TRLogger.Log("debug","    "+eer.toString()); 
 					}
 				}
 
 				try {
 					Player player = tekkitrestrict.getInstance().getServer()
 							.getPlayer(event.getWhoClicked().getName());
 					net.minecraft.server.EntityPlayer ep = ((org.bukkit.craftbukkit.entity.CraftPlayer) player)
 							.getHandle();
 					if (ep.abilities.canInstantlyBuild) {
 						TRLimitedCreative.handleCreativeInvClick(event);
 					}
 				} catch (Exception e) {
 					TRLogger.Log("debug",
 							"Error! [handleCreativeInv Listener] : " + e.getMessage());
 					for(StackTraceElement eer:e.getStackTrace()){
 						TRLogger.Log("debug","    "+eer.toString()); 
 					}
 				}
 				// Determine if they are crafting an uncraftable. Log EE
 				// Crafting.
 				// Perf: [0]
 				try {
 					handleCraftBlock(event);
 				} catch (Exception e) {
 					TRLogger.Log("debug",
 							"Error! [TRhandleCraftBlock] : " + e.getMessage());
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	private void handleCraftBlock(InventoryClickEvent event) {
 		Player player = tekkitrestrict.getInstance().getServer()
 				.getPlayer(event.getWhoClicked().getName());
 		try {if (event.getCurrentItem() != null) {
 			if (!TRPermHandler.hasPermission(player, "noitem", "bypass", "")) {
 				ItemStack ccc = event.getCurrentItem();
 				if (TRNoItem.isItemBanned(
 						player,
 						new com.github.dreadslicer.tekkitrestrict.ItemStack(ccc
 								.getTypeId(), 0, ccc.getData().getData()))) {
 					player.sendRawMessage("[TRItemDisabler] You cannot obtain/modify this Item type!");
 					event.setCancelled(true);
 				}
 			}
 		}}catch(Exception eee){}
 		
 	}
 
 	// ////////////////END INVClicks //////////////////////////
 
 	@EventHandler
 	public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent e) {
		if ((tekkitrestrict.config.getBoolean("UseItemLimiter") != null) && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 			try {TRLimitBlock.setExpire(e.getPlayer().getName());}catch(Exception eee){}
 			try {TRNoHack.playerLogout(e.getPlayer());}catch(Exception eee){}
 			try{TRNoDupeProjectTable.playerUnuse(e.getPlayer().getName().toLowerCase());}catch(Exception eee){}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent e) {
		if (tekkitrestrict.config.getBoolean("UseItemLimiter") != null) && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 			try {TRLimitBlock.setExpire(e.getPlayer().getName());}catch(Exception eee){}
 			try {TRNoHack.playerLogout(e.getPlayer());}catch(Exception eee){}
 			try{TRNoDupeProjectTable.playerUnuse(e.getPlayer().getName().toLowerCase());}catch(Exception eee){}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerLogin(org.bukkit.event.player.PlayerJoinEvent e) {
 		try{
			if (tekkitrestrict.config.getBoolean("UseItemLimiter") != null) && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 				TRLimitBlock.removeExpire(e.getPlayer().getName());
 				TRLimitBlock.getLimiter(e.getPlayer());
 			}
 			if (TRNoDupe.lastPlayer.equals(e.getPlayer().getName())) {
 				TRNoDupe.lastPlayer = "";
 			}
 		}
 		catch(Exception e1){}
 		try{TRPermHandler.testPerms(e.getPlayer());}catch(Exception e1){}
 		try{TRNoDupe_BagCache.setCheck(e.getPlayer());}catch(Exception e1){}
 	}
 
 	@EventHandler
 	public void onInventoryCloseEvent(
 			org.bukkit.event.inventory.InventoryCloseEvent e) {
 		HumanEntity player = e.getPlayer();
 		String pname = player.getName();
 		try{TRNoDupeProjectTable.playerUnuse(pname.toLowerCase());}catch(Exception eee){}
 		try {
 			TRCommandAlc.setPlayerInv(pname);
 		} catch (Exception e1) {
 		}
 	}
 
 	@EventHandler
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
 		// ookay.
 		if (AntiForcefield) {
 			try {TRNoHackForcefield.checkForcefield(e);}catch(Exception eee){}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerMoveEvent(org.bukkit.event.player.PlayerMoveEvent e) {
 		// this event is going to happen... often.
 		if (AntiFly) {
 			try {TRHandleFly.handleFly(e);}catch(Exception eee){}
 			try {TRNoHackSpeed.handleMove(e);}catch(Exception eee){}
 		}
 	}
 
 	private Map<Player, Integer> PickupTick = Collections
 			.synchronizedMap(new HashMap<Player, Integer>());
 
 	@EventHandler
 	public void onPlayerPickupEvent(
 			org.bukkit.event.player.PlayerPickupItemEvent e) {
 		Player player = e.getPlayer();
 		try {
 			TRNoDupe_BagCache cache;
 			if ((cache = TRNoDupe_BagCache.check(player)) != null) {
 				e.setCancelled(true);
 				// player.kickPlayer("[TRDupe] you have a Black Hole Band in your ["+Color+"] Alchemy Bag! Please remove it NOW!");
 
 				// if(showDupesOnConsole)
 				// tekkitrestrict.log.info(player.getName()+" ["+cache.inBagColor+" bag] attempted to dupe with the "+cache.dupeItem+"!");
 				// TRLogger.Log("Dupe",
 				// player.getName()+" ["+cache.inBagColor+" bag] attempted to dupe with the "+cache.dupeItem+"!");
 				// TRLogger.broadcastDupe(player.getName(),
 				// "the Alchemy Bag and "+cache.dupeItem);
 
 				if (PickupTick.get(player) != null) {
 					if (PickupTick.get(player) >= 40) {
 						TRLogger.Log("Dupe", player.getName() + " ["
 								+ cache.inBagColor
 								+ " bag] attempted to pick up (dupe) with the "
 								+ cache.dupeItem + "!");
 						// player.sendMessage("You may not pick that up while a "+cache.dupeItem+" is in your ["+cache.inBagColor+" bag]");
 						player.kickPlayer("[TRDupe] A " + cache.dupeItem
 								+ " has been removed from your ["
 								+ cache.inBagColor + "] Alchemy Bag!");
 						TRLogger.broadcastDupe(player.getName(),
 								"the Alchemy Bag and " + cache.dupeItem, "alc");
 
 						// remove the BHB / Void ring!!!
 						cache.removeAlc();
 					}
 				}
 				PickupTick
 						.put(player,
 								PickupTick.get(player) != null ? PickupTick
 										.get(player) + 1 : 1);
 			}
 		} catch (Exception ee) {
 			TRLogger.Log("debug",
 					"Error! [TRNoDupePickup] : " + ee.getMessage());
 			for(StackTraceElement eer:ee.getStackTrace()){
 				TRLogger.Log("debug","    "+eer.toString()); 
 			}
 		}
 	}
 }
