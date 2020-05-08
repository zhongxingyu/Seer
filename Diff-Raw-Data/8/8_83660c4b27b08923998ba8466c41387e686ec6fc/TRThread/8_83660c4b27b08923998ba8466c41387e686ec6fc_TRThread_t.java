 package com.github.dreadslicer.tekkitrestrict;
 
 import ic2.common.ElectricItem;
 import ic2.common.ItemArmorElectric;
 import ic2.common.ItemElectricTool;
 import ic2.common.StackUtil;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.Item;
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.TileEntity;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.github.dreadslicer.tekkitrestrict.lib.TRCharge;
 
 import ee.EEBase;
 
 public class TRThread {
 	public saveThread st = new saveThread();
 	public DisableItemThread ls = new DisableItemThread();
 	public TWorldScrubber at = new TWorldScrubber();
 	public TGemArmorDisabler t3 = new TGemArmorDisabler();
 	public TEntityRemover t4 = new TEntityRemover();
 	private static TRThread instance;
 
 	public TRThread() {
 		instance = this;
 	}
 
 	public void init() {
 
 		st.setName("TekkitRestrict_SaveThread");
 		ls.setName("TekkitRestrict_InventorySearchThread");
 		at.setName("TekkitRestrict_BlockScrubberThread");
 		t3.setName("TekkitRestrict_GemArmorThread");
 		t4.setName("TekkitRestrict_EntityRemoverThread");
 		st.start();
 		ls.start();
 		at.start();
 		t3.start();
 		t4.start();
 	}
 
 	public static void reload() {
 		// reloads the variables in each thread...
 		instance.at.reload();
 		instance.ls.reload();
 		instance.t3.reload();
 		instance.t4.reload();
 	}
 
 	public static void originalEUEnd() {
 		instance.ls.originalEUEnd();
 	}
 }
 
 class TGemArmorDisabler extends Thread {
 	List<Player> bypassers = Collections
 			.synchronizedList(new LinkedList<Player>());
 	int TSpeed;
 	boolean Movement, Offensive;
 
 	@Override
 	public void run() {
 		boolean done = false;
 		while (!done) {
 			try {
 				GemArmorDisabler();
 				try {
 					Thread.sleep(TSpeed);
 				} catch (InterruptedException e) {
 				}
 			} catch (Exception e) {
 				TRLogger.Log("debug",
 						"Error: [GemArmor thread] " + e.getMessage());
 				for(StackTraceElement eer:e.getStackTrace()){
 					TRLogger.Log("debug","    "+eer.toString()); 
 				}
 			}
 		}
 	}
 
 	private void GemArmorDisabler() {
 		if (tekkitrestrict.EEEnabled) {
 			// ee.EEBase.getInstance();
 			try {
 				// tekkitrestrict.getInstance();
 
 				// tekkitrestrict.log.info("Bypassers: "+bypassers.size());
 				// apply "Treatment"
 				if (!Movement) {
 					synchronized (EEBase.playerArmorMovementToggle) {
 						/*
 						 * for(Object
 						 * xu:EEBase.playerArmorMovementToggle.keySet()){
 						 * EntityHuman hu = (EntityHuman)xu;
 						 * EEBase.playerArmorMovementToggle.put(hu, false); }
 						 */
 						EEBase.playerArmorMovementToggle.clear();
 					}
 					/*
 					 * Set ks = EEBase.playerArmorMovementToggle.keySet();
 					 * Iterator ki = ks.iterator();
 					 * 
 					 * while(ki.hasNext()){ net.minecraft.server.EntityHuman H =
 					 * (net.minecraft.server.EntityHuman)ki.next();
 					 * 
 					 * if(!bypassers.contains(tr.getServer().getPlayer(H.name))){
 					 * EEBase.playerArmorMovementToggle.remove(H);
 					 * tr.getServer().getPlayer(H.name).sendRawMessage("<?>"); }
 					 * }
 					 */
 				}
 				if (!Offensive) {
 					synchronized (EEBase.playerArmorOffensiveToggle) {
 						
 						 /*for(Object xu:EEBase.playerArmorOffensiveToggle.keySet()){
 							 EntityHuman hu = (EntityHuman)xu;
 							 EEBase.playerArmorOffensiveToggle.put(hu, false); 
 						 }*/
 						
 						EEBase.playerArmorOffensiveToggle.clear();
 					}
 					/*
 					 * Set ks1 = EEBase.playerArmorOffensiveToggle.keySet();
 					 * Iterator ki1 = ks1.iterator();
 					 * 
 					 * while(ki1.hasNext()){ net.minecraft.server.EntityHuman H
 					 * = (net.minecraft.server.EntityHuman)ki1.next(); //boolean
 					 * val =
 					 * ((Boolean)EEBase.playerArmorOffensiveToggle.get(H)).
 					 * booleanValue();
 					 * if(!bypassers.contains(tr.getServer().getPlayer
 					 * (H.name))){ EEBase.playerArmorMovementToggle.remove(H);
 					 * tr.getServer().getPlayer(H.name).sendRawMessage("<?>"); }
 					 * }
 					 */
 				}
 			} catch (Exception E) {
 
 			}
 		}
 	}
 
 	public void reload() {
 		this.Offensive = tekkitrestrict.config
 				.getBoolean("AllowGemArmorOffensive");
 		this.Movement = tekkitrestrict.config
 				.getBoolean("AllowGemArmorDefensive");
 		this.TSpeed = tekkitrestrict.config.getInt("GemArmorDThread");
 		reloadBypassers();
 	}
 
 	private void reloadBypassers() {
 		bypassers.clear();
 		Player[] ps = tekkitrestrict.getInstance().getServer()
 				.getOnlinePlayers();
 		// boolean SSDisableGemArmor =
 		// tekkitrestrict.config.getBoolean("SSDisableGemArmor");
 
 		for (int i = 0; i < ps.length; i++) {
 			// boolean abypass = tekkitrestrict.p erm.h as(ps[i],
 			// "tekkitrestrict.abypass");
 			// boolean SSbypass = tekkitrestrict.p erm.h as(ps[i],
 			// "tekkitrestrict.safezone.bypass");
 			boolean abypass = TRPermHandler.hasPermission(ps[i], "abypass", "",
 					"");
 			/*
 			 * boolean SSbypass =
 			 * tekkitrestrict.hasPermission(ps[i],"tekkitrestrict.safezone.bypass"
 			 * ); if(safeZone.inSafeZone(ps[i])){ //we now HAVE to have a
 			 * safezone bypass to disable this. if(SSDisableGemArmor){
 			 * if(SSbypass && abypass){ bypassers.add(ps[i]); } } } else
 			 */
 			if (abypass) {
 				bypassers.add(ps[i]);
 			}
 		}
 	}
 }
 
 class TEntityRemover extends Thread {
 	boolean SSDisableEntities;
 	int TSpeed;
 
 	@Override
 	public void run() {
 		boolean done = false;
 		while (!done) {
 			try {
 				disableEntities();
 				try {
 					Thread.sleep(TSpeed);
 				} catch (InterruptedException e) {
 				}
 			} catch (Exception e) {
 				TRLogger.Log("debug",
 						"Error: [Entity thread] " + e.getMessage());
 				for(StackTraceElement eer:e.getStackTrace()){
 					TRLogger.Log("debug","    "+eer.toString()); 
 				}
 			}
 		}
 	}
 
 	private void disableEntities() {
 		if (this.SSDisableEntities) {
 
 			// search through all of the entities
 			List<World> wlist = tekkitrestrict.getInstance().getServer()
 					.getWorlds();
 
 			for (int i = 0; i < wlist.size(); i++) {
 				net.minecraft.server.World wo = ((org.bukkit.craftbukkit.CraftWorld) wlist
 						.get(i)).getHandle();
 				// synchronize with the world thread...
 
 				for (int j = 0; j < wo.entityList.size(); j++) { // loop through
 																	// entities
 					// synchronized(wo){
 					// synchronize...
 					// try { wo.wait(); } catch (InterruptedException e2) {}
 
 					net.minecraft.server.Entity e1 = (net.minecraft.server.Entity) wo.entityList
 							.get(j);
 					Entity e = e1.getBukkitEntity();
 					if (TRSafeZone.inXYZSafeZone(e.getLocation(), e.getWorld()
 							.getName())) {
 						if (!(e instanceof Player)
 								&& !(e instanceof org.bukkit.entity.Item)) {
 							e.remove();
 						}
 					}
 					// wo.notify();
 					// }
 				}
 			}
 			wlist.clear();
 		}
 	}
 
 	public void reload() {
 		this.TSpeed = tekkitrestrict.config.getInt("SSEntityRemoverThread");
 		this.SSDisableEntities = tekkitrestrict.config
 				.getBoolean("SSDisableEntities");
 	}
 }
 
 class DisableItemThread extends Thread {
 	private int TSpeed, toid;
 	private boolean throttle, SSDechargeEE, SSDisableArcane;
 	private List<TRCacheItem> SSDecharged = Collections
 			.synchronizedList(new LinkedList<TRCacheItem>());
 	private List<TRCharge> MCharges = Collections
 			.synchronizedList(new LinkedList<TRCharge>()), maxEU = Collections
 			.synchronizedList(new LinkedList<TRCharge>());
 	private List<TRCharge> originalEU = Collections
 			.synchronizedList(new LinkedList<TRCharge>());
 	private List<String> MChargeStr = Collections
 			.synchronizedList(new LinkedList<String>()),
 			SSDechargedStr = Collections
 					.synchronizedList(new LinkedList<String>()),
 			maxEUStr = Collections.synchronizedList(new LinkedList<String>());
 	Player oos;
 	ExecutorService exe = Executors.newCachedThreadPool();
 	@Override
 	public void run() {
 		// loop forever, unless told to stop...
 		boolean done = false;
 
 		while (!done) {
 			try {
 				// Disabled Items remover
 				// if (UseNoItem) {
 				Player[] ps = tekkitrestrict.getInstance().getServer()
 						.getOnlinePlayers();
 				for (Player pp : ps) {
 					try {
 						oos = pp;
 
						/*if (throttle) {
 							exe.execute((new Runnable() {
 								@Override
 								public void run() {
 									disableItems(oos);
 								}
 							}));
						} else {*/
 							disableItems(pp);
						//}
 					} catch (Exception e) {
 						TRLogger.Log(
 								"debug",
 								"Error: [ItemDisabler[1] thread] "
 										+ e.getMessage());
 						for(StackTraceElement eer:e.getStackTrace()){
 							TRLogger.Log("debug","    "+eer.toString()); 
 						}
 					}
 				}
 				// }
 
 				try {
 					Thread.sleep(TSpeed);
 				} catch (InterruptedException e) {
 				}
 			} catch (Exception e) {
 				TRLogger.Log("debug",
 						"Error: [ItemDisabler thread] " + e.getMessage());
 			}
 		}
 	}
 
 	private void disableItems(Player player) {
 		try {
 			PlayerInventory inv = player.getInventory();
 			org.bukkit.inventory.ItemStack[] st1 = inv.getContents();
 			org.bukkit.inventory.ItemStack[] st2 = inv.getArmorContents();
 
 			/*try {
 				if (player.getItemOnCursor() != null) {
 					org.bukkit.inventory.ItemStack str = player
 							.getItemOnCursor();
 					if(str != null){
 						ItemStack ee = new ItemStack(str.getTypeId(),
 								str.getAmount(), str.getData().getData());
 						if (TRNoItem.isItemBanned(player, ee)
 								|| TRNoItem.isCreativeItemBanned(player, ee)) {
 							player.setItemOnCursor(null);
 						}
 					}
 				}
 			} catch (Exception e) {
 				TRLogger.Log(
 						"debug",
 						"Error: [Inventory thread] DisableCursorItem "
 								+ e.getMessage());
 				for(StackTraceElement ee:e.getStackTrace()){
 					TRLogger.Log("debug","    "+ee.toString()); 
 				}
 			}*/
 			// //////////// NORMAL INVENTORY
 			boolean changed = false;
 			for (int i = 0; i < st1.length; i++) {
 				try {
 					// org.bukkit.inventory.ItemStack str = st1[i];
 					if (st1[i] != null) {
 
 						ItemStack ee = new ItemStack(st1[i].getTypeId(),
 								st1[i].getAmount(), st1[i].getData().getData());
 						net.minecraft.server.ItemStack var1 = ((org.bukkit.craftbukkit.inventory.CraftItemStack) st1[i])
 								.getHandle();
 
 						// tekkitrestrict.log.info("heh1");
 						// //// BAN THE ITEM
 						if (TRNoItem.isItemBanned(player, ee)
 								|| TRNoItem.isCreativeItemBanned(player, ee)) {
 							st1[i] = new org.bukkit.inventory.ItemStack(toid, 1);
 							changed = true;
 						}
 
 						// //// HANDLE DECHARGE / MAXCHARGE (EE / IC2)
 						if (!changed) {
 							try {
 								String tstr = st1[i].getTypeId() + "";// +":"+st1[i].getData();
 
 								if (tekkitrestrict.EEEnabled) {
 									try {
 										int m = MChargeStr.indexOf(tstr);
 										if (m != -1) {
 											TRCharge g = MCharges.get(m);
 											if (g.id == st1[i].getTypeId()) {
 												if (var1.getItem() instanceof ee.ItemEECharged) {
 													ee.ItemEECharged eer = (ee.ItemEECharged) var1
 															.getItem();
 													double maxEE = eer
 															.getMaxCharge();
 													double per = maxEE / 100.000;
 													int setMax = (new Double(
 															per * g.maxcharge))
 															.intValue();
 													// tekkitrestrict.log.info("Keptcha");
 													short chargeGoal = getShort(
 															st1[i],
 															"chargeGoal");
 													short chargeLevel = getShort(
 															st1[i],
 															"chargeLevel");
 													short chargeTicks = getShort(
 															st1[i],
 															"chargeTicks");
 													if (chargeGoal > setMax
 															|| chargeLevel > setMax) {
 														setShort(st1[i],
 																"chargeLevel",
 																setMax);
 														setShort(st1[i],
 																"chargeGoal",
 																setMax);
 														// var1.setData(setMax);
 														var1.setData(var1.i()
 																- (setMax
 																		* 10
 																		+ chargeTicks << (eer
 																			.canActivate2() ? 2
 																		: ((int) (eer
 																				.canActivate() ? 1
 																				: 0)))));
 														changed = true;
 													}
 												}
 											}
 										}
 									} catch (Exception E) {
 										TRLogger.Log("debug",
 												"Error: [MaxCharge thread] "
 														+ E.getMessage());
 										for(StackTraceElement ae:E.getStackTrace()){
 											TRLogger.Log("debug","    "+ae.toString()); 
 										}
 									}
 								}
 
 								if (maxEUStr.contains(tstr)) {
 									try {
 										TRCharge s = maxEU.get(maxEUStr
 												.indexOf(tstr));
 										Item si = var1.getItem();
 										NBTTagCompound nbttagcompound = StackUtil
 												.getOrCreateNbtData(var1);
 										if (si instanceof ItemArmorElectric) {
 											ItemArmorElectric ci = (ItemArmorElectric) si;
 											if (ci.maxCharge != s.maxcharge
 													|| ci.transferLimit != s.chargerate) {
 												this.addOriginalEU(ci.id,
 														ci.maxCharge,
 														ci.transferLimit, var1);
 												// tekkitrestrict.log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 												double charge = nbttagcompound
 														.getInt("charge");
 												Double newcharge = (new Double(
 														charge) * new Double(
 														s.maxcharge))
 														/ new Double(
 																ci.maxCharge);
 												// tekkitrestrict.log.info("charge: "+charge+" newcharge: "+newcharge);
 												ci.maxCharge = s.maxcharge;
 												ci.transferLimit = s.chargerate;
 												nbttagcompound.setInt("charge",
 														newcharge.intValue());
 
 												ElectricItem.charge(var1, 10,
 														9999, true, false);
 												/*
 												 * if (var1.i() > 2)
 												 * var1.setData(1 +
 												 * ((s.maxcharge - newcharge) *
 												 * (var1.i() - 2)) /
 												 * s.maxcharge); else
 												 * var1.setData(0);
 												 */
 												changed = true;
 											}
 										} else if (si instanceof ItemElectricTool) {
 											ItemElectricTool ci = (ItemElectricTool) si;
 											if (ci.maxCharge != s.maxcharge
 													|| ci.transferLimit != s.chargerate) {
 												this.addOriginalEU(ci.id,
 														ci.maxCharge,
 														ci.transferLimit, var1);
 												// tekkitrestrict.log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 												double charge = nbttagcompound
 														.getInt("charge");
 												Double newcharge = (new Double(
 														charge) * new Double(
 														s.maxcharge))
 														/ new Double(
 																ci.maxCharge);
 												// tekkitrestrict.log.info("charge: "+charge+" newcharge: "+newcharge);
 												ci.maxCharge = s.maxcharge;
 												ci.transferLimit = s.chargerate;
 												nbttagcompound.setInt("charge",
 														newcharge.intValue());
 
 												ElectricItem.charge(var1, 10,
 														9999, true, false);
 												/*
 												 * if (var1.i() > 2)
 												 * var1.setData(1 +
 												 * ((s.maxcharge - newcharge) *
 												 * (var1.i() - 2)) /
 												 * s.maxcharge); else
 												 * var1.setData(0);
 												 */
 												changed = true;
 											}
 										} else if (si instanceof ElectricItem) {
 											ElectricItem ci = (ElectricItem) si;
 											if (ci.maxCharge != s.maxcharge
 													|| ci.transferLimit != s.chargerate) {
 												this.addOriginalEU(ci.id,
 														ci.maxCharge,
 														ci.transferLimit, var1);
 												// tekkitrestrict.log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 												double charge = nbttagcompound
 														.getInt("charge");
 												Double newcharge = (new Double(
 														charge) * new Double(
 														s.maxcharge))
 														/ new Double(
 																ci.maxCharge);
 												// tekkitrestrict.log.info("charge: "+charge+" newcharge: "+newcharge);
 												ci.maxCharge = s.maxcharge;
 												ci.transferLimit = s.chargerate;
 												nbttagcompound.setInt("charge",
 														newcharge.intValue());
 
 												ElectricItem.charge(var1, 10,
 														9999, true, false);
 												/*
 												 * if (var1.i() > 2)
 												 * var1.setData(1 +
 												 * ((s.maxcharge - newcharge) *
 												 * (var1.i() - 2)) /
 												 * s.maxcharge); else
 												 * var1.setData(0);
 												 */
 												changed = true;
 											}
 										}
 										/*
 										 * if (itemstack.i() > 2)
 										 * itemstack.setData(1 +
 										 * ((ielectricitem1.getMaxCharge() - k)
 										 * * (itemstack .i() - 2)) /
 										 * ielectricitem1.getMaxCharge()); else
 										 * itemstack.setData(0);
 										 */
 									} catch (Exception e) {
 										TRLogger.Log("debug",
 												"Error: [Decharger[7] thread] "
 														+ e.getMessage());
 										for(StackTraceElement eer:e.getStackTrace()){
 											TRLogger.Log("debug","    "+eer.toString()); 
 										}
 									}
 								}
 
 								if (TRSafeZone.inSafeZone(player)
 										&& !TRPermHandler.hasPermission(player,
 												"safezone", "bypass", "")) {
 									//tekkitrestrict.log.info("in SS");
 									try {
 										if (SSDisableArcane) {
 											if (st1[i] != null) {
 												if (st1[i].getTypeId() == 27584
 														&& (var1.getData() != 6 || getString(
 																st1[i], "mode") != "earth")) {
 													setString(st1[i], "mode",
 															"earth");
 													var1.setData(6);
 												}
 											}
 										}
 									} catch (Exception ex) {
 										TRLogger.Log("debug",
 												"SSDisableArcane[2] Error! "
 														+ ex.getMessage());
 									}
 									if (SSDechargeEE) {
 										int m = SSDechargedStr.indexOf(tstr);
 										if (m != -1) {
 											try {
 												TRCacheItem g = SSDecharged
 														.get(m);
 												if (g.id == st1[i].getTypeId()) {
 													if (var1.getItem() instanceof ee.ItemEECharged) {
 														//ee.ItemEECharged eer = (ee.ItemEECharged) var1
 														//		.getItem();
 														if (st1[i].getTypeId() == g.id
 																&& (getShort(
 																		st1[i],
 																		"chargeGoal") > 0 || getShort(
 																		st1[i],
 																		"chargeLevel") > 0)) {
 
 															setShort(
 																	st1[i],
 																	"chargeLevel",
 																	0);
 															setShort(
 																	st1[i],
 																	"chargeGoal",
 																	0);
 															var1.setData(200);
 															changed = true;
 															// cx.tag = new
 															// NBTTagCompound();
 															// setShort(cx,
 															// "chargeLevel",
 															// 0);
 														}
 													}
 												}
 											} catch (Exception ex) {
 												TRLogger.Log(
 														"debug",
 														"SSDisableItem[9] Error! "
 																+ ex.getMessage());
 												for(StackTraceElement eer:ex.getStackTrace()){
 													TRLogger.Log("debug","    "+eer.toString()); 
 												}
 											}
 										}
 									}
 								}
 							} catch (Exception e) {
 								TRLogger.Log(
 										"debug",
 										"Error: [Decharger[6] thread] "
 												+ e.getMessage());
 								for(StackTraceElement eer:e.getStackTrace()){
 									TRLogger.Log("debug","    "+eer.toString()); 
 								}
 							}
 						}
 					}
 				} catch (Exception e) {
 					TRLogger.Log("debug", "Error: [ItemDisabler[16] thread] ");
 					for (StackTraceElement ee : e.getStackTrace()) {
 						TRLogger.Log("debug", "  " + ee.toString());
 					}
 					// e.printStackTrace();
 				}
 				//Thread.sleep(3);
 			}
 			// //////////// ARMOR INVENTORY
 			boolean changed1 = false;
 			for (int i = 0; i < st2.length; i++) {
 				try {
 					org.bukkit.inventory.ItemStack str = st2[i];
 					// net.minecraft.server.ItemStack mre =
 					// ((org.bukkit.craftbukkit.inventory.CraftItemStack)str).getHandle();
 					// player.sendRawMessage(str.getTypeId()+":"+str.getData().getData()+" | "+str.getData()+" ");
 					// String.valueOf(noItem.getInstance().isItemBanned(player,
 					// mre)));
 					ItemStack ee = new ItemStack(str.getTypeId(),
 							str.getAmount(), str.getData().getData());
 					if (TRNoItem.isItemBanned(player, ee)
 							|| TRNoItem.isCreativeItemBanned(player, ee)) {
 						// this item is banned/disabled for this player!!!
 						// proceed to remove it.
 						st2[i] = new org.bukkit.inventory.ItemStack(toid, 1);
 						changed1 = true;
 					}
 				} catch (Exception e) {
 
 				}
 				Thread.sleep(3);
 			}
 			// place new inventory back.
 			if (changed) {
 				inv.setContents(st1);
 			}
 			if (changed1) {
 				inv.setArmorContents(st2);
 			}
 		} catch (Exception e) {
 			TRLogger.Log("debug",
 					"Error: [ItemDisabler[2] thread] " + e.getMessage());
 			for(StackTraceElement eer:e.getStackTrace()){
 				TRLogger.Log("debug","    "+eer.toString()); 
 			}
 		}
 	}
 
 	public void reload() {
 		if (this.SSDecharged != null) {
 			this.SSDecharged.clear();
 		}
 		if (this.SSDechargedStr != null) {
 			this.SSDechargedStr.clear();
 		}
 		if (this.MCharges != null) {
 			this.MCharges.clear();
 		}
 		if (this.MChargeStr != null) {
 			this.MChargeStr.clear();
 		}
 		if (this.maxEU != null) {
 			this.maxEU.clear();
 		}
 		if (this.maxEUStr != null) {
 			this.maxEUStr.clear();
 		}
 
 		this.TSpeed = tekkitrestrict.config.getInt("InventoryThread");
 		//this.UseNoItem = tekkitrestrict.config.getBoolean("UseNoItem");
 		this.toid = tekkitrestrict.config.getInt("ChangeDisabledItemsIntoId");
 		this.SSDechargeEE = tekkitrestrict.config.getBoolean("SSDechargeEE");
 		this.SSDisableArcane = tekkitrestrict.config
 				.getBoolean("SSDisableRingOfArcana");
 		this.throttle = tekkitrestrict.config
 				.getBoolean("ThrottleInventoryThread");
 		List<String> MaxCharges = tekkitrestrict.config
 				.getStringList("MaxCharge");
 		List<String> sstr = tekkitrestrict.config.getStringList("DechargeInSS");
 
 		for (String s : sstr) {
 			List<TRCacheItem> iss = TRCacheItem.processItemString("", s, -1);
 			for (TRCacheItem iss1 : iss) {
 				this.SSDecharged.add(iss1);
 				this.SSDechargedStr.add(iss1.id + "");
 			}
 		}
 
 		List<String> meu = tekkitrestrict.config.getStringList("MaxEU");
 		for (String s : meu) {
 			if (s.contains(" ")) {
 				String[] sseu = s.split(" ");
 				int eu = Integer.parseInt(sseu[1]);
 				int chrate = Integer.parseInt(sseu[2]);
 				List<TRCacheItem> iss = TRCacheItem.processItemString("",
 						sseu[0], -1);
 				for (TRCacheItem iss1 : iss) {
 					TRCharge gg = new TRCharge();
 					gg.id = iss1.id;
 					gg.data = iss1.data;
 					gg.maxcharge = eu;
 					gg.chargerate = chrate;
 					this.maxEU.add(gg);
 					this.maxEUStr.add(iss1.id + "");
 				}
 			}
 		}
 
 		// process charges...
 		MCharges.clear();
 		for (int l = 0; l < MaxCharges.size(); l++) {
 			String Charge = MaxCharges.get(l);
 			if (Charge.contains(" ")) {
 				String[] sscharge = Charge.split(" ");
 				int max = Integer.parseInt(sscharge[1]);
 				// ItemStack[] gs = TRNoItem.getRangedItemValues(sscharge[0]);
 				List<TRCacheItem> iss = TRCacheItem.processItemString("",
 						sscharge[0], -1);
 				for (TRCacheItem isr : iss) {
 					TRCharge gg = new TRCharge();
 					gg.id = isr.id;
 					gg.data = isr.getData();
 					gg.maxcharge = max;
 					// tekkitrestrict.log.info(gg.id+":"+gg.data+" "+gg.maxcharge);
 					this.MCharges.add(gg);
 					this.MChargeStr.add(gg.id + "");
 				}
 			}
 		}
 	}
 
 	private void addOriginalEU(int id, int mcharge, int tlimit, Object store) {
 		TRCharge trcc = new TRCharge();
 		trcc.id = id;
 		trcc.maxcharge = mcharge;
 		trcc.chargerate = tlimit;
 		trcc.itemstack = store;
 		originalEU.add(trcc);
 	}
 
 	public void originalEUEnd() {
 		// returns the affected items back to their normal rates... so they are
 		// saved correctly.
 		/*
 		 * for(TRCharge s:originalEU){ if(s.itemstack != null){ try{
 		 * net.minecraft.server.ItemStack var1 =
 		 * (net.minecraft.server.ItemStack)s.itemstack; Item si =
 		 * var1.getItem(); int k = s.maxcharge; NBTTagCompound nbttagcompound =
 		 * StackUtil.getOrCreateNbtData(var1); if(si instanceof
 		 * ItemArmorElectric){ ItemArmorElectric ci = (ItemArmorElectric) si;
 		 * if(ci.maxCharge != s.maxcharge || ci.transferLimit != s.chargerate){
 		 * this.addOriginalEU(ci.id,ci.maxCharge,ci.transferLimit,var1);
 		 * tekkitrestrict
 		 * .log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 		 * double charge = nbttagcompound.getInt("charge"); Double newcharge =
 		 * (new Double(charge)*new Double(s.maxcharge))/new
 		 * Double(ci.maxCharge);
 		 * tekkitrestrict.log.info("charge: "+charge+" newcharge: "+newcharge);
 		 * ci.maxCharge = s.maxcharge; ci.transferLimit = s.chargerate;
 		 * nbttagcompound.setInt("charge",newcharge.intValue());
 		 * 
 		 * ElectricItem.charge(var1, 10, 9999, true, false); /*if (var1.i() > 2)
 		 * var1.setData(1 + ((s.maxcharge - newcharge) * (var1.i() - 2)) /
 		 * s.maxcharge); else var1.setData(0);* / } } else if(si instanceof
 		 * ItemElectricTool){ ItemElectricTool ci = (ItemElectricTool) si;
 		 * if(ci.maxCharge != s.maxcharge || ci.transferLimit != s.chargerate){
 		 * this.addOriginalEU(ci.id,ci.maxCharge,ci.transferLimit,var1);
 		 * //tekkitrestrict
 		 * .log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 		 * int charge = nbttagcompound.getInt("charge"); int newcharge =
 		 * (charge*s.maxcharge)/ci.maxCharge; ci.maxCharge = s.maxcharge;
 		 * ci.transferLimit = s.chargerate;
 		 * nbttagcompound.setInt("charge",newcharge);
 		 * 
 		 * ElectricItem.charge(var1, 10, 9999, true, false); /*if (var1.i() > 2)
 		 * var1.setData(1 + ((ci.getMaxCharge() - k) * (var1.i() - 2)) /
 		 * ci.getMaxCharge()); else var1.setData(0);* / } } else if(si
 		 * instanceof ElectricItem){ ElectricItem ci = (ElectricItem) si;
 		 * if(ci.maxCharge != s.maxcharge || ci.transferLimit != s.chargerate){
 		 * this.addOriginalEU(ci.id,ci.maxCharge,ci.transferLimit,var1);
 		 * //tekkitrestrict
 		 * .log.info(ci.maxCharge+" dur: "+var1.i()+" mc: "+ci.getMaxCharge());
 		 * int charge = nbttagcompound.getInt("charge"); int newcharge =
 		 * (charge*s.maxcharge)/ci.maxCharge; ci.maxCharge = s.maxcharge;
 		 * ci.transferLimit = s.chargerate;
 		 * nbttagcompound.setInt("charge",newcharge);
 		 * 
 		 * ElectricItem.charge(var1, 10, 9999, true, false); /*if (var1.i() > 2)
 		 * var1.setData(1 + ((ci.getMaxCharge() - k) * (var1.i() - 2)) /
 		 * ci.getMaxCharge()); else var1.setData(0);* / } } } catch(Exception
 		 * es){ es.printStackTrace(); } } }
 		 */
 	}
 
 	public short getShort(org.bukkit.inventory.ItemStack varx, String var2) {
 		net.minecraft.server.ItemStack var1 = ((org.bukkit.craftbukkit.inventory.CraftItemStack) varx)
 				.getHandle();
 		if (var1.tag == null) {
 			var1.setTag(new NBTTagCompound());
 		}
 		if (!var1.tag.hasKey(var2)) {
 			setShort(varx, var2, 0);
 		}
 		return var1.tag.getShort(var2);
 	}
 
 	public void setShort(org.bukkit.inventory.ItemStack varx, String var2,
 			int var3) {
 		net.minecraft.server.ItemStack var1 = ((org.bukkit.craftbukkit.inventory.CraftItemStack) varx)
 				.getHandle();
 		if (var1.tag == null) {
 			var1.setTag(new NBTTagCompound());
 		}
 		var1.tag.setShort(var2, (short) var3);
 	}
 
 	public String getString(org.bukkit.inventory.ItemStack varx, String var2) {
 		net.minecraft.server.ItemStack var1 = ((org.bukkit.craftbukkit.inventory.CraftItemStack) varx)
 				.getHandle();
 		if (var1.tag == null) {
 			var1.setTag(new NBTTagCompound());
 		}
 		if (!var1.tag.hasKey(var2)) {
 			setString(varx, var2, "");
 		}
 		return var1.tag.getString(var2);
 	}
 
 	public void setString(org.bukkit.inventory.ItemStack varx, String var2,
 			String var3) {
 		net.minecraft.server.ItemStack var1 = ((org.bukkit.craftbukkit.inventory.CraftItemStack) varx)
 				.getHandle();
 		if (var1.tag == null) {
 			var1.setTag(new NBTTagCompound());
 		}
 		var1.tag.setString(var2, var3);
 	}
 }
 
 class TWorldScrubber extends Thread {
 	int TSpeed;
 	boolean RMDB, UseRPTimer;
 	double time;
 	int toid;
 
 	@Override
 	public void run() {
 		// loop forever, unless told to stop...
 		boolean done = false;
 		while (!done) {
 
 			// Disabled Items remover
 			try {
 				doWScrub();
 			} catch (Exception e) {
 				TRLogger.Log("debug",
 						"Error: [WorldScrubber thread] " + e.getMessage());
 				for(StackTraceElement eer:e.getStackTrace()){
 					TRLogger.Log("debug","    "+eer.toString()); 
 				}
 			}
 
 			try {
 				Thread.sleep(TSpeed);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void doWScrub() {
 		try {
 			TRChunkUnloader.unloadSChunks();
 		} catch (Exception e) {
 		}
 		if (RMDB || UseRPTimer) {
 			int currentChunkCount = 0;
 
 			List<World> wo = tekkitrestrict.getInstance().getServer()
 					.getWorlds();
 			for (int j = 0; j < wo.size(); j++) {
 				World g = wo.get(j);
 
 				net.minecraft.server.WorldServer wo1 = ((org.bukkit.craftbukkit.CraftWorld) g)
 						.getHandle();
 
 				// TileLogic tilelogic = (TileLogic)CoreLib.getTileEntity(g, x,
 				// y, z, eloraam/logic/TileLogic);
 				Chunk[] cc = g.getLoadedChunks();
 				currentChunkCount += cc.length;
 				// loop through all of the blocks in the chunk...
 				for (int k = 0; k < cc.length; k++) {
 					Chunk c = cc[k];
 
 					if (this.RMDB) {
 						for (int x = 0; x < 16; x++) {
 							for (int z = 0; z < 16; z++) {
 								for (int y = 0; y < 256; y++) {
 									// so... yeah.
 									Block bl = c.getBlock(x, y, z);
 									if (TRNoItem.isBlockDisabled(bl)) {
 										bl.setTypeId(toid);
 									}
 								}
 							}
 						}
 					}
 
 					if (UseRPTimer) {
 						if (tekkitrestrict.pm
 								.isPluginEnabled("mod_RedPowerLogic")) {
 							try {
 								BlockState[] ggg = c.getTileEntities();
 								for (BlockState gg : ggg) {
 									TileEntity te = wo1.getTileEntity(
 											gg.getX(), gg.getY(), gg.getZ());
 									if (te instanceof eloraam.logic.TileLogicPointer) {
 										eloraam.logic.TileLogicPointer timer = (eloraam.logic.TileLogicPointer) te;
 
 										double ticktime = time * 20;
 										if (timer.GetInterval() < ticktime) {
 											timer.SetInterval(new Double(
 													ticktime).longValue());
 										}
 									}
 								}
 							} catch (Exception EER) {
 								// EER.printStackTrace();
 								TRLogger.Log("debug",
 										"RPTimerError: " + EER.getMessage());
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void reload() {
 		this.TSpeed = tekkitrestrict.config.getInt("WorldCleanerThread");
 		this.RMDB = tekkitrestrict.config
 				.getBoolean("RemoveDisabledItemBlocks");
 		this.toid = tekkitrestrict.config.getInt("ChangeDisabledItemsIntoId");
 		this.UseRPTimer = tekkitrestrict.config.getBoolean("UseAutoRPTimer");
 		this.time = tekkitrestrict.config.getDouble("RPTimerMin");
 	}
 }
 
 class saveThread extends Thread {
 	@Override
 	public void run() {
 		boolean done = false;
 		while (!done) {
 			// runs save functions for both safezones and itemlimiter
 			try {
 				TRLimitBlock.saveLimiters();
 			} catch (Exception E) {
 			}
 			try {
 				TRSafeZone.save();
 			} catch (Exception E) {
 			}
 
 			// TRThread.reload();
 			TRLogger.saveLogs();
 			TRNoHack.clearMaps();
 			try{TRLimitBlock.manageData();}
 			catch(Exception e){
 				TRLogger.Log("debug", "ManageData AutoSavethread Error: "+e.getMessage());
 				for(StackTraceElement eer:e.getStackTrace()){
 					TRLogger.Log("debug","    "+eer.toString()); 
 				}
 			}
 			/*
 			 * List<World> wog =
 			 * tekkitrestrict.getInstance().getServer().getWorlds(); for(int i =
 			 * 0;i<wog.size();i++){ World wo = wog.get(i); wo.save(); }
 			 */
 			// tekkitrestrict.log.info("Saved!");
 
 			try {
 				Thread.sleep(tekkitrestrict.config
 						.getInt("AutoSaveThreadSpeed"));
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 }
