 package org.jakub1221.herobrineai.entity;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.jakub1221.herobrineai.HerobrineAI;
 
 import net.minecraft.server.v1_5_R2.ItemStack;
 import net.minecraft.server.v1_5_R2.World;
 
 public class CustomZombie extends net.minecraft.server.v1_5_R2.EntityZombie implements CustomEntity{
 	 
 	private MobType mobType=null;
 	
     public CustomZombie(World world,Location loc,MobType mbt) {
 		super(world);
 		this.mobType=mbt;
         if (mbt==MobType.ARTIFACT_GUARDIAN){
         	spawnArtifactGuardian(loc);
         }else if (mbt==MobType.HEROBRINE_WARRIOR){
         	spawnHerobrineWarrior(loc);
         }
 	}
 
     private void spawnArtifactGuardian(Location loc){
     	
         this.health=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Guardian.HP");
         this.setCustomName("Artifact Guardian");
         this.maxHealth=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Guardian.HP");
         this.setEquipment(0, new ItemStack(net.minecraft.server.v1_5_R2.Item.GOLD_SWORD));
         this.setEquipment(1, new ItemStack(net.minecraft.server.v1_5_R2.Item.GOLD_BOOTS));
         this.setEquipment(2, new ItemStack(net.minecraft.server.v1_5_R2.Item.GOLD_LEGGINGS));
         this.setEquipment(3, new ItemStack(net.minecraft.server.v1_5_R2.Item.GOLD_CHESTPLATE));
         this.setEquipment(4, new ItemStack(net.minecraft.server.v1_5_R2.Item.GOLD_HELMET));
         this.getBukkitEntity().teleport(loc);
         
     
         
     }
     
     private void spawnHerobrineWarrior(Location loc){
     	
         this.health=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Warrior.HP");
         this.setCustomName("Herobrines Warrior");
         this.maxHealth=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc.Warrior.HP");
         this.setEquipment(0, new ItemStack(net.minecraft.server.v1_5_R2.Item.IRON_SWORD));
         this.setEquipment(1, new ItemStack(net.minecraft.server.v1_5_R2.Item.IRON_BOOTS));
         this.setEquipment(2, new ItemStack(net.minecraft.server.v1_5_R2.Item.IRON_LEGGINGS));
         this.setEquipment(3, new ItemStack(net.minecraft.server.v1_5_R2.Item.IRON_CHESTPLATE));
         this.setEquipment(4, new ItemStack(net.minecraft.server.v1_5_R2.Item.IRON_HELMET));
         this.getBukkitEntity().teleport(loc);
         
     }
     
     public CustomZombie(World world) {
         super(world);    
     }
 
 	@Override
 	public void Kill() {
 		
 		String mobS = "";
 		if (this.mobType==MobType.ARTIFACT_GUARDIAN){mobS="Guardian";}else{mobS="Warrior";}
 		
 		for(int i=1;i<=2500;i++){
			if (HerobrineAI.getPluginCore().getConfigDB().npc.contains("npc."+mobS+".Drops."+Integer.toString(i))==true){
 				int chance=new Random().nextInt(100);
						if (chance<=HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc."+mobS+".Drops."+Integer.toString(i)+".Chance")){
							this.getBukkitEntity().getLocation().getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.getMaterial(i),HerobrineAI.getPluginCore().getConfigDB().npc.getInt("npc."+mobS+".Drops."+Integer.toString(i)+".Count")));
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
