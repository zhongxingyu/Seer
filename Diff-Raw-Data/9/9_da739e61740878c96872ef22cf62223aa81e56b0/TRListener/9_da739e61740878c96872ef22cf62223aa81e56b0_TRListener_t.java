 package com.github.dreadslicer.tekkitrestrict;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.dreadslicer.tekkitrestrict.commands.TRCommandAlc;
 import com.github.dreadslicer.tekkitrestrict.lib.TRNoClick;
 
 import eloraam.core.TileCovered;
 
 public class TRListener implements Listener {
 	private static TRListener instance;
 	boolean SSInnvincible, UseBlockLimit;
 	boolean LogAmulets, LogRings, LogDMTools, LogRMTools, LogEEMisc;
 	private Map<Integer, String> EENames = Collections.synchronizedMap(new HashMap<Integer, String>());
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
 		instance.SSInnvincible = tekkitrestrict.config.getBoolean("SSInvincible");
 		instance.UseBlockLimit = tekkitrestrict.config.getBoolean("UseItemLimiter");
 
 		instance.LogAmulets = tekkitrestrict.config.getBoolean("LogAmulets");
 		instance.LogRings = tekkitrestrict.config.getBoolean("LogRings");
 		instance.LogDMTools = tekkitrestrict.config.getBoolean("LogDMTools");
 		instance.LogRMTools = tekkitrestrict.config.getBoolean("LogRMTools");
 		instance.LogEEMisc = tekkitrestrict.config.getBoolean("LogEEMisc");
 		
 		TRNoClick.reload();
 		TRNoDupe_BagCache.reload();
 	}
 
 	public static TRListener getInstance() {
 		return instance;
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
 		
 		
 		Player player = e.getPlayer();
 		if (player == null) {
 			lastdata = e.getBlock().getData();
 			return;
 		}
 		
 		try {
 			TRLWCProtect.checkLWC(e);
 
 			Block block = e.getBlock();
 			int id = block.getTypeId();
 			int data = block.getData();
 			WorldServer ws = ((CraftWorld) block.getWorld()).getHandle();
 			
 			TileEntity te1 = ws.getTileEntity(block.getX(), block.getY(), block.getZ());
 
 			if (UseBlockLimit) {
 				TRLimitBlock il = TRLimitBlock.getLimiter(player);
 				if (!il.checkLimit(e)) {
 					if (!Util.hasBypass(player, "limiter")) { //TODO tr.bypass.limiter or tr.bypass.limit
 						player.sendMessage("[TRItemLimiter] You cannot place down any more of that block!");
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
 
 			if (te1 != null && data == 0) {
 
 				if (te1 instanceof TileCovered) {
 					// TileCovered tc = (TileCovered)te1;
 					// tekkitrestrict.log.info("ar "+lastdata);
 					data = lastdata;
 				}
 			}
 			com.github.dreadslicer.tekkitrestrict.ItemStack cc = new com.github.dreadslicer.tekkitrestrict.ItemStack(id, 0, data);
 			if (TRNoItem.isItemBanned(player, cc)) {
 				// tekkitrestrict.log.info(cc.id+":"+cc.getData());
 				player.sendMessage("[TRItemDisabler] You cannot place down this type of block!");
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
 			lastdata = e.getBlock().getData();
 		} catch(Exception ex){
 			tekkitrestrict.log.warning("A minor exception occured in tekkitrestrict. Please give the developer the following information: ");
 			tekkitrestrict.log.warning(" - onBlockPlace, " + ex.getMessage());
 		}
 		
 	}
 
 	@EventHandler
 	public void onDropItem(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		if (player == null) return;
 		try {
 			TRNoDupe.handleDropDupes(event);
 		} catch (Exception ex) {
 			tekkitrestrict.log.warning("A minor exception occured in tekkitrestrict. Please give the developer the following information: ");
 			tekkitrestrict.log.warning(" - onDropItem, handleDropDupes");
 		}
 		
 		try {
 			EntityPlayer ep = ((CraftPlayer) player).getHandle();
 			if (ep.abilities.canInstantlyBuild) {
 				if (!Util.hasBypass(player, "creative")) {
 					/*Item ccr = event.getItemDrop();
 					ItemStack ccc = ccr.getItemStack();*/
 					event.setCancelled(true);
 					player.sendMessage("[TRLimitedCreative] You cannot drop items!");
 				}
 			}
 		} catch(Exception ex){
 			TRLogger.Log("debug", "Error! [TRLimitedCreative Drop Listener] : " + ex.getMessage());
 			Log.Exception(ex);
 		}
 	}
 
 	// /////// START INTERACT //////////////
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		Player player = e.getPlayer();
 		if (player == null) return;
 		
 		// determine if this is Buildcraft or RedPower... Then exempt.
 		String pname = player.getName().toLowerCase();
 		if (pname.equals("[buildcraft]") || pname.equals("[redpower]")) return;
 		// lets do this based on a white-listed approach.
 		// First, lets loop through the DisableClick list to stop clicks.
 		// Perf: 8x
 		try {
 			TRNoClick.compareAll(e);
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error: [ListenInteract TRNoClick] " + ex.getMessage());
 		}
 		
 		try {
 			TRNoDupeProjectTable.checkTable(e);
 		} catch(Exception ex){}
 
 		try {
 			// if(e.getAction() == Action.RIGHT_CLICK_BLOCK ||
 			// e.getAction() == Action.RIGHT_CLICK_AIR){
 
 			
 			//EntityPlayer ep = ((CraftPlayer) player).getHandle();
 			//if (ep.abilities.canInstantlyBuild) {
 			if (player.getGameMode() == GameMode.CREATIVE) {
 				org.bukkit.inventory.ItemStack str = player.getItemInHand();
 				if (str != null) {
 					com.github.dreadslicer.tekkitrestrict.ItemStack ee = new com.github.dreadslicer.tekkitrestrict.ItemStack(
 							str.getTypeId(), str.getAmount(), str.getData().getData());
 					if (TRNoItem.isCreativeItemBanned(player, ee)) {
 						player.sendMessage(ChatColor.RED + "[TRLimitedCreative] You may not interact with this item.");
 						e.setCancelled(true);
 						player.setItemInHand(null);
 					}
 				}
 			}
 			// }
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error: [ListenInteract TRLimitedCreative] " + ex.getMessage());
 		}
 
 		if (!e.isCancelled()) itemLogUse(e);
 		
 	}
 
 	private void itemLogUse(PlayerInteractEvent e) {
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
 		if (event.getWhoClicked() == null) return;
 		try {
 			Player player = (Player) event.getWhoClicked();
 			if (player.getGameMode() == GameMode.CREATIVE)
 				TRLimitedCreative.handleCreativeInvClick(event);
 			
 			//EntityPlayer ep = ((CraftPlayer) player).getHandle();
 			//if (ep.abilities.canInstantlyBuild) {
			//TRLimitedCreative.handleCreativeInvClick(event);
 			//}
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error! [handleCreativeInv Listener] : " + ex.getMessage());
 			Log.Exception(ex);
 		}
 		// Determine if they are crafting an uncraftable. Log EE
 		// Crafting.
 		// Perf: [0]
 		try {
 			handleCraftBlock(event);
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error! [TRhandleCraftBlock] : " + ex.getMessage());
 			Log.Exception(ex);
 		}
 		
 	}
 
 	private void handleCraftBlock(InventoryClickEvent event) {
 		Player player = (Player) event.getWhoClicked();
 		ItemStack currentItem = event.getCurrentItem();
 		if (currentItem == null) return;
 		
 		if (Util.hasBypass(player, "noitem")) return;
 			
 		if (TRNoItem.isItemBanned(player,
 				new com.github.dreadslicer.tekkitrestrict.ItemStack(currentItem.getTypeId(), 0, currentItem.getDurability()))) {
 			player.sendMessage("[TRItemDisabler] You cannot obtain/modify this Item type!");
 			event.setCancelled(true);
 		}
 		
 	}
 
 	// ////////////////END INVClicks //////////////////////////
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e) {
 		//IMPORTANT assigner
 		Player player = e.getPlayer();
 		TRCommandAlc.setPlayerInv(player);
 		if (tekkitrestrict.config.getBoolean("UseItemLimiter") && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 			try {TRLimitBlock.setExpire(player.getName());}catch(Exception eee){}
 			try {TRNoHack.playerLogout(player);}catch(Exception eee){}
 			try {TRNoDupeProjectTable.playerUnuse(player.getName());}catch(Exception eee){}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerKick(PlayerKickEvent e) {
 		//IMPORTANT assigner
 		Player player = e.getPlayer();
 		TRCommandAlc.setPlayerInv(player);
 		if (tekkitrestrict.config.getBoolean("UseItemLimiter") && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 			try {TRLimitBlock.setExpire(player.getName());}catch(Exception eee){}
 			try {TRNoHack.playerLogout(player);}catch(Exception eee){}
 			try {TRNoDupeProjectTable.playerUnuse(player.getName());}catch(Exception eee){}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerLogin(PlayerJoinEvent e) {
 		Player player = e.getPlayer();
 		try {
 			if (tekkitrestrict.config.getBoolean("UseItemLimiter") && tekkitrestrict.config.getBoolean("UseItemLimiter")) {
 				TRLimitBlock.removeExpire(player.getName());
 				TRLimitBlock.getLimiter(player);
 			}
 		} catch(Exception e1){}
 		
 		//try{TRPermHandler.testPerms(player);}catch(Exception e1){}
 		try{TRNoDupe_BagCache.setCheck(player);}catch(Exception e1){}
 	}
 
 	@EventHandler
 	public void onInventoryCloseEvent(InventoryCloseEvent e) {
 		try {
 			TRNoDupeProjectTable.playerUnuse(e.getPlayer().getName());
 		} catch(Exception ex){}
 		TRCommandAlc.setPlayerInv2((Player) e.getPlayer());
 	}
 
 	private Map<Player, Integer> PickupTick = Collections.synchronizedMap(new HashMap<Player, Integer>());
 
 	@EventHandler
 	public void onPlayerPickupEvent(PlayerPickupItemEvent e) {
 		//IMPORTANT Fix this in the next version to the new version.
 		Player player = e.getPlayer();
 		try {
 			TRNoDupe_BagCache cache;
 			if ((cache = TRNoDupe_BagCache.check(player)) != null) {
 				e.setCancelled(true);
 				// player.kickPlayer("[TRDupe] you have a Black Hole Band in your ["+Color+"] Alchemy Bag! Please remove it NOW!");
 
 				// if(showDupesOnConsole)
 				// tekkitrestrict.log.info(player.getName()+" ["+cache.inBagColor+" bag] attempted to dupe with the "+cache.dupeItem+"!");
 				// TRLogger.Log("Dupe", player.getName()+" ["+cache.inBagColor+" bag] attempted to dupe with the "+cache.dupeItem+"!");
 				// TRLogger.broadcastDupe(player.getName(), "the Alchemy Bag and "+cache.dupeItem);
 
 				Integer tick = PickupTick.get(player);
 				if (tick != null) {
 					if (tick >= 40) {
 						// player.sendMessage("You may not pick that up while a "+cache.dupeItem+" is in your ["+cache.inBagColor+" bag]");
 						player.kickPlayer("[TRDupe] A " + cache.dupeItem + " has been removed from your [" + cache.inBagColor + "] Alchemy Bag!");
 						Log.Dupe("a "+ cache.inBagColor + " Alchemy Bag and " + cache.dupeItem, "alc", player.getName());
 
 						// remove the BHB / Void ring!!!
 						cache.removeAlc();
 						PickupTick.put(player, 1);
 					} else {
 						PickupTick.put(player, tick + 1);
 					}
 				} else
 					PickupTick.put(player, 1);
 				
 			}
 		} catch (Exception ex) {
 			TRLogger.Log("debug", "Error! [TRNoDupePickup] : " + ex.getMessage());
 			Log.Exception(ex);
 		}
 	}
 }
