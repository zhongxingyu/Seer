 package net.milkycraft.config;
 
 import static java.lang.Byte.MAX_VALUE;
 import static net.milkycraft.objects.Type.ALL;
 import static net.milkycraft.objects.Type.BABY;
 import static net.milkycraft.objects.Type.BOTH;
 
 import java.util.Set;
 
import net.milkycraft.Utility;
 import net.milkycraft.objects.Item;
 import net.milkycraft.objects.Meta;
 import net.milkycraft.objects.Spawnable;
 import net.milkycraft.objects.Type;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.potion.Potion;
 
 public class ConfigUtility {
 
 	protected static void loadBlockedItems(WorldConfiguration wc) {
 		for (String s : wc.c.getStringList("Disable.Usage.Blocked_Items")) {
 			try {
 				Material mat = Material.valueOf(s.toUpperCase());
 				wc.usageBlock.add(new Item(mat.getId()));
 			} catch (Exception ex) {
 				if (s.toLowerCase().startsWith("potion")) {
 					String[] args = s.split(":");
 					Integer id = Integer.parseInt(args[1]);
					Potion p = Utility.fromDamage(id);
 					wc.usageBlock.add(new Item(373, p.getNameId(), true));
 				} else {
 					wc.plugin.getLogger().severe("Invalid value: " + s);
 					wc.plugin.getLogger().severe("Reference: http://goo.gl/f1Nmb");
 				}
 			}
 		}
 	}
 
 	protected static void loadBlockedDispenserItems(WorldConfiguration wc) {
 		for (String s : wc.c.getStringList("Disable.Dispensing.Blocked_Items")) {
 			try {
 				Material mat = Material.valueOf(s.toUpperCase());
 				wc.dispBlock.add(new Item(mat.getId()));
 			} catch (Exception ex) {
 				if (s.toLowerCase().startsWith("potion")) {
 					String[] args = s.split(":");
 					Integer id = Integer.parseInt(args[1]);
					Potion p = Utility.fromDamage(id);
 					wc.dispBlock.add(new Item(373, p.getNameId(), true));
 				} else {
 					wc.plugin.getLogger().severe("Invalid value: " + s);
 					wc.plugin.getLogger().severe("Reference: http://goo.gl/f1Nmb");
 				}
 			}
 		}
 	}
 
 	protected static void loadBlockedSpawnEggs(WorldConfiguration wc) {
 		for (String s : wc.c.getStringList("EggManager.Disabled_Eggs")) {
 			try {
 				wc.disEggs.add(EntityType.valueOf(s.toUpperCase()).getTypeId());
 			} catch (Exception ex) {
 				wc.plugin.getLogger().severe("Invalid value: " + s);
 				wc.plugin.getLogger().severe("Reference: http://goo.gl/E7mVB");
 			}
 		}
 	}
 
 	protected static void loadBlockedEntities(WorldConfiguration wc) {
 		Set<Spawnable> l = wc.disMobs;
 		for (String s : wc.c.getStringList("SpawnManager.Disallowed_Mobs")) {
 			try {
 				if (s.indexOf(":") <= 0) {
 					short e = EntityType.valueOf(s.toUpperCase()).getTypeId();
 					l.add(new Spawnable(e, ALL, MAX_VALUE));
 				} else {
 					String[] a = s.split(":");
 					EntityType t = EntityType.valueOf(a[0].toUpperCase());
 					String s1 = t.toString();
 					String s2 = a[1].toUpperCase();
 					if (a.length == 2) {
 						Meta meta;
 						try {
 							meta = new Meta(Type.valueOf(s2), MAX_VALUE);
 						} catch (IllegalArgumentException ex) {
 							meta = new Meta(ALL, DyeColor.valueOf(s2).getWoolData());
 						}
 						l.add(new Spawnable(t.getTypeId(), meta));
 					} else if (a.length == 3) {
 						String s3 = a[2].toUpperCase();
 						if (s1.equals("ZOMBIE")) {
 							if (s2.equals("BABY") || s2.equals("VILLAGER")) {
 								if (s3.equals("BABY") || s3.equals("VILLAGER")) {
 									l.add(new Spawnable(t.getTypeId(), BOTH, MAX_VALUE));
 								}
 							}
 						} else if (s1.equals("SHEEP")) {
 							if (s2.equals("BABY")) {
 								byte b = DyeColor.valueOf(s3).getWoolData();
 								l.add(new Spawnable(t.getTypeId(), BABY, b));
 							}
 						}
 					}
 				}
 			} catch (IllegalArgumentException ex) {
 				wc.plugin.getLogger().severe("Invalid value: " + s);
 				wc.plugin.getLogger().severe("Reference: http://goo.gl/E7mVB");
 			} catch (Exception ex) {
 				wc.plugin.getLogger().severe("Severe unhandled error occured");
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	protected static void loadBlockedSpawnReasons(WorldConfiguration wc) {
 		for (String s : wc.c.getStringList("SpawnManager.Disallowed_Reasons")) {
 			try {
 				wc.disReasons.add(SpawnReason.valueOf(s.toUpperCase()).toString());
 			} catch (Exception ex) {
 				wc.plugin.getLogger().severe("Invalid value: " + s);
 				wc.plugin.getLogger().severe("Reference: http://goo.gl/a4XRB");
 			}
 		}
 	}
 
 }
