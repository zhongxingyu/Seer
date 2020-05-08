 package com.etriacraft.tamemanagement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.Claim;
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Ocelot.Type;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class MobListener implements Listener {
 
 	TameManagement plugin;
 
 	public static boolean griefpreventionsupport;
 	public static HashMap<String, String> transfers = new HashMap<String, String>();
 	public static HashMap<String, String> releases = new HashMap<String, String>();
 	public static HashMap<String, Horse.Style> horsestyles = new HashMap<String, Horse.Style>();
 	public static HashMap<String, Horse.Color> horsecolors = new HashMap<String, Horse.Color>(); 
 	public static HashMap<String, Horse.Variant> horsevariants = new HashMap<String, Horse.Variant>();
 	public static Set<String> horseclaims = new HashSet<String>();
 	public static Set<String> getInfo = new HashSet<String>();
 
 	public MobListener(TameManagement instance) {
 		this.plugin = instance;
 	}
 
 	public static String Prefix;
 	public static String doesNotOwn;
 	public static String animalDoesNotBelongToYou;
 	public static String cantInteractWithHorse;
 	public static String cantChangeStyle;
 	public static String horseAlreadyOwned;
 	public static String styleChanged;
 	public static String cantChangeColor;
 	public static String colorChanged;
 	public static String cantChangeVariant;
 	public static String changedVariant;
 	public static String animalReleased;
 	public static String animalTransferred;
 	public static String horseClaimed;
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent e) {
 		if (e.getSpawnReason() == SpawnReason.BREEDING) {
 			Entity entity = e.getEntity();
 			if (entity instanceof Wolf) {
 				if (!plugin.getConfig().getBoolean("Breeding.Wolf")) {
 					e.setCancelled(true);
 				}
 			}
 			if (entity instanceof Ocelot) {
 				if (!plugin.getConfig().getBoolean("Breeding.Ocelot")) {
 					e.setCancelled(true);
 				}
 			}
 			if (entity instanceof Horse) {
 				if (!plugin.getConfig().getBoolean("Breeding.Horse")) {
 					e.setCancelled(true);
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void EntityDamageEvent(EntityDamageByEntityEvent e) {
 		Entity damager = e.getDamager();
 		Entity damaged = e.getEntity();
 
 		if (damager instanceof Player) {
 			Player p = (Player) damager;
 			if (damaged instanceof Tameable) {
 				if (((Tameable) damaged).isTamed()) {
 					AnimalTamer tameOwner = ((Tameable) damaged).getOwner();
 					if (tameOwner == null) {
 						e.setCancelled(false);
 					}
 					if (plugin.getConfig().getBoolean("ProtectTames") == true) {
 						if (griefpreventionsupport == true) {
 							Location loc = damaged.getLocation();
 							Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
							try {
								if (claim.allowAccess(p).equals(null)) {
									e.setCancelled(false);
								}
							} catch (Exception ex) {
 							}
 						}
 						if (!p.getName().equals(tameOwner.getName())) {
 							p.sendMessage(Prefix + doesNotOwn.replace("%owner", tameOwner.getName()));
 							e.setCancelled(true);
 						}
 					}
 				}
 
 			}
 		}
 	}
 
 	@EventHandler
 	public void PlayerInteract(PlayerInteractEntityEvent e) {
 		Player p = e.getPlayer();
 		Entity entity = e.getRightClicked();
 		if (entity instanceof Tameable) {
 			AnimalTamer currentOwner = ((Tameable) entity).getOwner();
 			if (entity instanceof Horse) {
 				Horse horse = (Horse) entity;
 				if (horseclaims.contains(p.getName())) {
 					if(horse.getOwner() == null) {
 						horse.setOwner(p);
 						horseclaims.remove(p.getName());
 						p.sendMessage(Prefix + horseClaimed);
 						e.setCancelled(true);
 					} else {
 						p.sendMessage(Prefix + horseAlreadyOwned.replace("%owner", currentOwner.getName()));
 						horseclaims.remove(p.getName());
 						e.setCancelled(true);
 					}
 				}
 				// Runs this code on the /tame horse setstyle command.
 				if (plugin.getConfig().getBoolean("ProtectHorses")) {
 					if (griefpreventionsupport == true) {
 						Location loc = horse.getLocation();
 						Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
 						if (claim.allowAccess(p).equals(null)) {
 							e.setCancelled(false);
 						}
 					} else {
 						if (horse.isTamed()) {
 							if (currentOwner == null) {
 								e.setCancelled(false);
 							} else {
 								if (!currentOwner.getName().equals(p.getName()) && !p.hasPermission("tamemanagement.protecthorses.override")) {
 									p.sendMessage(Prefix + cantInteractWithHorse.replace("%owner", currentOwner.getName()));
 									e.setCancelled(true);
 								}
 							}
 						}
 					}
 				}
 
 				if (horsestyles.containsKey(p.getName())) {
 					if (horse.isTamed()) {
 						if (!currentOwner.getName().equals(p.getName())) {
 							p.sendMessage(Prefix + cantChangeStyle.replace("%owner", currentOwner.getName()));
 							horsestyles.remove(p.getName());
 							e.setCancelled(true);
 						} else {
 							horse.setStyle(horsestyles.get(p.getName()));
 							p.sendMessage(Prefix + styleChanged);
 							e.setCancelled(true);
 							horsestyles.remove(p.getName());
 						}
 					}
 				}
 				if (horsecolors.containsKey(p.getName())) {
 					if (horse.isTamed()) {
 						if (!currentOwner.getName().equals(p.getName())) {
 							p.sendMessage(Prefix + cantChangeColor.replace("%owner", currentOwner.getName()));
 							horsecolors.remove(p.getName());
 							e.setCancelled(true);
 						} else {
 							horse.setColor(horsecolors.get(p.getName()));
 							p.sendMessage(Prefix + colorChanged);
 							e.setCancelled(true);
 							horsecolors.remove(p.getName());
 						}
 					}
 				}
 				if (horsevariants.containsKey(p.getName())) {
 					if (horse.isTamed()) {
 						if (!currentOwner.getName().equals(p.getName())) {
 							p.sendMessage(Prefix + cantChangeVariant.replace("%owner", currentOwner.getName()));
 							horsevariants.remove(p.getName());
 							e.setCancelled(true);
 						} else {
 							horse.setVariant(horsevariants.get(p.getName()));
 							p.sendMessage(Prefix + changedVariant);
 							e.setCancelled(true);
 							horsevariants.remove(p.getName());
 						}
 					}
 				}
 
 			}		
 			// This code runs on the /tame release command.
 			if (releases.containsKey(p.getName())) {
 				if (!p.getName().equals(currentOwner.getName())) {
 					p.sendMessage(Prefix + doesNotOwn.replace("%owner", currentOwner.getName()));
 					return;
 				}
 				((Tameable) entity).setOwner(null);
 				if (entity instanceof Horse) {
 					Horse horse = (Horse) entity;
 					if (horse.getInventory().getSaddle() != null) {
 						ItemStack horseSaddle = horse.getInventory().getSaddle();
 						horse.getInventory().setSaddle(null);
 						p.getWorld().dropItem(p.getLocation(), horseSaddle);
 					}
 					if (horse.getInventory().getArmor() != null) {
 						ItemStack horseArmor = horse.getInventory().getArmor();
 						horse.getInventory().setArmor(null);
 						p.getWorld().dropItem(p.getLocation(), horseArmor);
 					}
 				}
 				if (entity instanceof Wolf) {
 					Wolf wolf = (Wolf) entity;
 					if (wolf.isSitting()) {
 						wolf.setSitting(false);
 					}
 				}
 				if (entity instanceof Ocelot) {
 					Ocelot ocelot = (Ocelot) entity;
 					if (ocelot.isSitting()) {
 						ocelot.setSitting(false);
 					}
 					ocelot.setCatType(Type.WILD_OCELOT);
 				}
 				p.sendMessage(Prefix + animalReleased);
 				e.setCancelled(true);
 				releases.remove(p.getName());
 			}
 
 			// This code runs on /tame transfer.
 			if (transfers.containsKey(p.getName())) {
 				String newOwner = transfers.get(p.getName());
 				if (!p.getName().equals(currentOwner.getName())) {
 					p.sendMessage(Prefix + doesNotOwn.replace("%owner", currentOwner.getName()));
 					return;
 				}
 				Player p2 = Bukkit.getPlayer(newOwner);
 				AnimalTamer newOwner2 = p2;
 				((Tameable) entity).setOwner(newOwner2);
 				p.sendMessage(Prefix + animalTransferred.replace("%newowner", newOwner));
 				transfers.remove(p.getName());		
 				e.setCancelled(true);
 			}
 		}
 	}
 
 }
