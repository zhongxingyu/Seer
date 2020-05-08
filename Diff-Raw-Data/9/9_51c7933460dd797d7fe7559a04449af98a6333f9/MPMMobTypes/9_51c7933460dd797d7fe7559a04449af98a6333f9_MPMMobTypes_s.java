 package com.ainast.morepowerfulmobsreloaded;
 
import me.egordm.simpleattributes.API.SimpleAttributesAPI;
import me.egordm.simpleattributes.Attributes.AttributeType;

 import org.bukkit.Location;
 import org.bukkit.Material;
import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
import org.bukkit.util.permissions.BroadcastPermissions;
 import org.shininet.bukkit.playerheads.Tools;
 
 import com.herocraftonline.heroes.characters.Monster;
import com.sk89q.worldedit.MobType;
 
 public class MPMMobTypes {
 
 	public static void spawnCorruptedResident(LivingEntity entity){
 		System.out.println("Spawn Corrupted Resident");
 		
 		entity.setCustomName("Corrupted Resident");
 		entity.setCustomNameVisible(true);
 		//set entity equipment and drop chance, put to 0 and use droptable instead
 		//I use equipment to increase mob attribute modifiers, pick one.
 		entity.getEquipment().setHelmet( Tools.Skull("Steve"));
 		entity.getEquipment().setHelmetDropChance(0);
 		entity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
 		entity.getEquipment().setChestplateDropChance(0);
 		entity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
 		entity.getEquipment().setLeggingsDropChance(0);
 		entity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
 		entity.getEquipment().setBootsDropChance(0);
 		
 		//set mob max HP
 		entity.setMaxHealth(220);
 		entity.setHealth(220);
 
 		//heroes mob stuff, don't know if works;
 		Monster m = MPMTools.getHeroes().getCharacterManager().getMonster(entity);
 		m.setDamage(m.getDamage()*3);
 		m.setExperience(31);
 		
 		
 	}
 	
 	public static void spawnCorruptedGuard(Location location){
 		
 	}
 	
 	public static ItemStack getCorruptedGuardDrop() {
 		ItemStack[] dropTable = { //add items seperated by comma
 								TieredItems.getTier1PristinePlateGreaves(),
 								TieredItems.getTestBow()
 								};
 		int i = MPMTools.generator.nextInt(dropTable.length);
 		ItemStack dropItem =  dropTable[i]; 
 		return dropItem;
 	}
 	
 	public static void spawnCorruptedDefender(Location location){
 		
 	}
 	
 	public static void spawnForestBanditScavenger(Location location){
 		
 	}
 	
 	public static void spawnForestBanditArcher(Location location){
 		
 	}
 	
 	public static void spawnForestBanditWarrior(Location location){
 		
 	}
 	
 }
