 package com.runetooncraft.plugins.EasyMobArmory;
 
 
 
 import java.util.HashMap;
 
 import net.minecraft.server.v1_6_R2.Item;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.TileEntityChest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
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
 import org.bukkit.inventory.meta.SkullMeta;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 
 
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
 	if(Armoryenabled.get(p) != null){ if(Armoryenabled.get(p)) {
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
 				if(z.isBaby()) inv.setItem(5, new ItemStack(Material.REDSTONE));
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
 		}else if(e.getType().equals(EntityType.PIG_ZOMBIE)) {
 			ItemStack i = p.getItemInHand();
 			PigZombie pz = (PigZombie) e;
 			if(EMA.Helmets.contains(i)) {
 				pz.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				pz.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				pz.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				pz.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "pigzombieinv");
 				ItemStack[] pigzombieinv = pz.getEquipment().getArmorContents();
 				inv.setContents(pigzombieinv);
 				inv.setItem(4, pz.getEquipment().getItemInHand());
 				if(pz.isBaby()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, pz);
 			}else{
 				pz.getEquipment().setItemInHand(i);
 			}
 		}else if(e.getType().equals(EntityType.SHEEP)) {
 			ItemStack i = p.getItemInHand();
 			Sheep sh = (Sheep) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "sheepinv");
 				if(!sh.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(sh.isSheared()) inv.setItem(6, new ItemStack(Material.SHEARS));
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, sh);
 			}
 		}else if(e.getType().equals(EntityType.PIG)) {
 			ItemStack i = p.getItemInHand();
 			Pig pig = (Pig) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "piginv");
 				if(!pig.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(pig.hasSaddle()) inv.setItem(6, new ItemStack(Material.SADDLE));
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, pig);
 			}
 		}else if(e.getType().equals(EntityType.COW)) {
 			ItemStack i = p.getItemInHand();
 			Cow cow = (Cow) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "cowinv");
 				if(!cow.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, cow);
 			}
 		}else if(e.getType().equals(EntityType.HORSE)) {
 			ItemStack i = p.getItemInHand();
 			Horse h = (Horse) e;
 			if(i.getType().equals(Material.BONE)) {
				Inventory inv = Bukkit.createInventory(p, 9, "horseinv");
 				if(!h.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(h.isTamed()) inv.setItem(6, new ItemStack(Material.HAY_BLOCK));
 				if(h.isCarryingChest()) inv.setItem(7, new ItemStack(Material.CHEST));
 				if(h.isTamed()) {
 					Player owner = (Player) h.getOwner();
 					inv.setItem(8, setOwner(new ItemStack(Material.SKULL_ITEM, 1, (short)3), p.getName()));
 				}
 				p.openInventory(inv);
 				PlayerZombieDataMap.put(p, h);
 			}
 		}
 		}}
 	}
 	@EventHandler
 	public void OnInventoryCloseEvent(InventoryCloseEvent event) {
 	if(Armoryenabled.get(event.getPlayer()) != null){  if(Armoryenabled.get(event.getPlayer())) {
 		if(event.getInventory().getName().equals("zombieinv")) {
 			Inventory i = event.getInventory();
 			Zombie z = (Zombie) PlayerZombieDataMap.get(event.getPlayer());
 			z.getEquipment().setHelmet(i.getItem(3));
 			z.getEquipment().setChestplate(i.getItem(2));
 			z.getEquipment().setLeggings(i.getItem(1));
 			z.getEquipment().setBoots(i.getItem(0));
 			z.getEquipment().setItemInHand(i.getItem(4));
 			if(i.contains(Material.REDSTONE)) { 
 				z.setBaby(true);
 			}else{
 				z.setBaby(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("skeletoninv")) {
 			Inventory i = event.getInventory();
 			Skeleton s = (Skeleton) PlayerZombieDataMap.get(event.getPlayer());
 			s.getEquipment().setHelmet(i.getItem(3));
 			s.getEquipment().setChestplate(i.getItem(2));
 			s.getEquipment().setLeggings(i.getItem(1));
 			s.getEquipment().setBoots(i.getItem(0));
 			s.getEquipment().setItemInHand(i.getItem(4));
 		}
 		else if(event.getInventory().getName().equals("pigzombieinv")) {
 			Inventory i = event.getInventory();
 			PigZombie pz = (PigZombie) PlayerZombieDataMap.get(event.getPlayer());
 			pz.getEquipment().setHelmet(i.getItem(3));
 			pz.getEquipment().setChestplate(i.getItem(2));
 			pz.getEquipment().setLeggings(i.getItem(1));
 			pz.getEquipment().setBoots(i.getItem(0));
 			pz.getEquipment().setItemInHand(i.getItem(4));
 			if(i.contains(Material.REDSTONE)) { 
 				pz.setBaby(true);
 			}else{
 				pz.setBaby(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("sheepinv")) {
 			Inventory i = event.getInventory();
 			Sheep sh = (Sheep) PlayerZombieDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				sh.setBaby();
 			}else{
 				sh.setAdult();
 			}
 			if(i.contains(Material.SHEARS)) {
 				sh.setSheared(true);
 			}else{
 				sh.setSheared(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("piginv")) {
 			Inventory i = event.getInventory();
 			Pig pig = (Pig) PlayerZombieDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				pig.setBaby();
 			}else{
 				pig.setAdult();
 			}
 			if(i.contains(Material.SADDLE)) {
 				pig.setSaddle(true);
 			}else{
 				pig.setSaddle(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("cowinv")) {
 			Inventory i = event.getInventory();
 			Cow cow = (Cow) PlayerZombieDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				cow.setBaby();
 			}else{
 				cow.setAdult();
 			}
 		}
 	}}
 	}
 	public ItemStack setOwner(ItemStack item, String owner) {
 		  SkullMeta meta = (SkullMeta) item.getItemMeta();
 		  meta.setOwner(owner);
 		  item.setItemMeta(meta);
 		  return item;
 		}
 }
