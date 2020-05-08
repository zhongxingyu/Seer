 package BL2.core.helper;
 
 import BL2.item.ItemGun;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 
 /**
  * Borderlands 2
  * 
  * ItemDropHelper
  * 
  * @author lombax5832
  * 
  */
 public class ItemDropHelper {
     
     private static double random;
     
     public static void dropGun(EntityPlayer player, EntityLiving entity){
         
         if(GenericMethods.isHostileEntity(entity)){
             random = Math.random();
             
             if(random < 0d){
                 
                entity.entityDropItem(ItemGun.getRandomGun(), 1);
                 
             }
         }
     }
     
     public static void dropAmmo(EntityPlayer player, EntityLiving entity){
         
         if(GenericMethods.isHostileEntity(entity)){
             random = Math.random();
             
             System.out.println(random);
             
             if(random < 1d){
                 
                 ItemStack ammoType = AmmoDropHelper.ammoDrop();
                 int ammoNum = AmmoDropHelper.ammoAmmount(ammoType);
                 
                 System.out.println(ammoNum);
                 
                 
                for(int x = 0; x < ammoNum; x++){
                    entity.entityDropItem(ammoType, 1);
                     System.out.println(x);
                 }
             }
         }
     }
 }
