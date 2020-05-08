 package com.runetooncraft.plugins.EasyMobArmory;
 
 
 
 import java.util.HashMap;
 
 import net.minecraft.server.v1_6_R2.TileEntityChest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 
 
 public class EMAListener implements Listener {
 	Config config;
 	public static HashMap<Player, Entity> PlayerZombieDataMap = new HashMap<Player, Entity>();
 	public static HashMap<Player, Boolean> Armoryenabled = new HashMap<Player, Boolean>();
 	public EMAListener(Config config) {
 		this.config = config;
 	}
 	@EventHandler
 	public void OnPlayerEntityInteract(PlayerInteractEntityEvent event) {
 		Entity e = event.getRightClicked();
 		Player p = event.getPlayer();
	if(Armoryenabled.get(p) == null || Armoryenabled.get(p)){
 		if(e.getType().equals(EntityType.ZOMBIE)) {
 			ItemStack i = p.getItemInHand();
 			Zombie z = (Zombie) e;
 			if(EMA.Helmets.contains(i)) {
 				z.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				z.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				z.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				z.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "zombieinv");
 				ItemStack[] zombieinv = z.getEquipment().getArmorContents();
 				inv.setContents(zombieinv);
 				inv.setItem(4, z.getEquipment().getItemInHand());
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, z);
 			}else{
 				z.getEquipment().setItemInHand(i);
 			}
 		}else if(e.getType().equals(EntityType.SKELETON)) {
 			ItemStack i = p.getItemInHand();
 			Skeleton s = (Skeleton) e;
 			if(EMA.Helmets.contains(i)) {
 				s.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				s.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				s.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				s.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "skeletoninv");
 				ItemStack[] skeletoninv = s.getEquipment().getArmorContents();
 				inv.setContents(skeletoninv);
 				inv.setItem(4, s.getEquipment().getItemInHand());
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, s);
 			}else{
 				s.getEquipment().setItemInHand(i);
 			}
 		}
 		}
 	}
 	@EventHandler
 	public void OnInventoryCloseEvent(InventoryCloseEvent event) {
	if(Armoryenabled.get(event.getPlayer()) == null || Armoryenabled.get(event.getPlayer())){
 		if(event.getInventory().getName().equals("zombieinv")) {
 			Inventory i = event.getInventory();
 			Zombie z = (Zombie) PlayerZombieDataMap.get(event.getPlayer());
 			z.getEquipment().setHelmet(i.getItem(3));
 			z.getEquipment().setChestplate(i.getItem(2));
 			z.getEquipment().setLeggings(i.getItem(1));
 			z.getEquipment().setBoots(i.getItem(0));
 			z.getEquipment().setItemInHand(i.getItem(4));
 		}
 		if(event.getInventory().getName().equals("skeletoninv")) {
 			Inventory i = event.getInventory();
 			Skeleton s = (Skeleton) PlayerZombieDataMap.get(event.getPlayer());
 			s.getEquipment().setHelmet(i.getItem(3));
 			s.getEquipment().setChestplate(i.getItem(2));
 			s.getEquipment().setLeggings(i.getItem(1));
 			s.getEquipment().setBoots(i.getItem(0));
 			s.getEquipment().setItemInHand(i.getItem(4));
 		}
 	}
 	}
 }
