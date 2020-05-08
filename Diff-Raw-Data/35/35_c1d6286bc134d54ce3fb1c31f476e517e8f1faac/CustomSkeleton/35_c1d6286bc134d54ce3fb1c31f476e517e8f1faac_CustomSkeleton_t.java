 package org.jakub1221.herobrineai.entity;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Color;
 import org.jakub1221.herobrineai.HerobrineAI;
 import org.jakub1221.herobrineai.misc.ItemName;
 
 import net.minecraft.server.v1_5_R2.World;
 
 public class CustomSkeleton extends net.minecraft.server.v1_5_R2.EntitySkeleton implements CustomEntity{
 
 	private MobType mobType=null;
 	
     public CustomSkeleton(World world,Location loc,MobType mbt) {
 		super(world);
 		this.mobType=mbt;
         if (mbt==MobType.DEMON){
         	spawnDemon(loc);
         }
 	}
 	
     public void spawnDemon(Location loc){
 
         this.health=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Demon.HP");
         this.setCustomName("Demon");
         this.maxHealth=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Demon.HP");
         
         ((Skeleton)this.getBukkitEntity()).getEquipment().setItemInHand(new ItemStack(Material.GOLDEN_APPLE,1));
         ((Skeleton)this.getBukkitEntity()).getEquipment().setHelmet(ItemName.colorLeatherArmor(new ItemStack(Material.LEATHER_HELMET,1), Color.RED));
         ((Skeleton)this.getBukkitEntity()).getEquipment().setChestplate(ItemName.colorLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE,1), Color.RED));
         ((Skeleton)this.getBukkitEntity()).getEquipment().setLeggings(ItemName.colorLeatherArmor(new ItemStack(Material.LEATHER_LEGGINGS,1), Color.RED));
         ((Skeleton)this.getBukkitEntity()).getEquipment().setBoots(ItemName.colorLeatherArmor(new ItemStack(Material.LEATHER_BOOTS,1), Color.RED));
         this.getBukkitEntity().teleport(loc);
         
     }
     
 	public CustomSkeleton(World world) {
 		super(world);
 	}
 
 	@Override
 	public void Kill(){
 		for(int i=1;i<=2500;i++){
			if (HerobrineAI.getPluginCore().getConfigDB().npc.contains("npc.Demon.Drops."+Integer.toString(i))==true){
 				int chance=new Random().nextInt(100);
						if (chance<=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Demon.Drops."+Integer.toString(i)+".Chance")){
							this.getBukkitEntity().getLocation().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), new ItemStack(Material.getMaterial(i),HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Demon.Drops."+Integer.toString(i)+".Count")));
 						}
 			}
 		}
 		this.health=0;	
 	}
 
 	@Override
 	public MobType getMobType() {
 		return this.mobType;
 	}
 
 }
