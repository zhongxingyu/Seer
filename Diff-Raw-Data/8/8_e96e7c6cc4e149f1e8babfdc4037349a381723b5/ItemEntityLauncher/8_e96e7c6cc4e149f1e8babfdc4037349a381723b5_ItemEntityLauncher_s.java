 package xlogisticzz.learningModding.Items;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import xlogisticzz.learningModding.LearningModdingCreativeTab;
import xlogisticzz.learningModding.Entities.EntityBlockEntityTeleport;
 import xlogisticzz.learningModding.Lib.Constants;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 /**
  * Learning Modding Mod
  * 
  * @author xLoGisTicZz.
  * 
  *         Some code may be from tutorials.
  * 
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 
 public class ItemEntityLauncher extends Item {
     
     public ItemEntityLauncher(int par1) {
     
         super(par1);
         
         this.setCreativeTab(LearningModdingCreativeTab.tabLearningModding);
         this.setUnlocalizedName(Constants.UnLocalisedNames.ENTITY_LAUNCHER);
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
     
         itemIcon = par1IconRegister.registerIcon(Constants.Mod.MODID + ":" + Constants.Icons.ENTITY_LAUNCHER);
     }
     
     @Override
     public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
     
         if (!world.isRemote){
            EntityBlockEntityTeleport launched = new EntityBlockEntityTeleport(world);
             
            launched.setLaunchPos(x, y, z);
             
             world.spawnEntityInWorld(launched);
             
             if (!player.capabilities.isCreativeMode){
                 stack.stackSize--;
             }
             return true;
         }else{
             return false;
         }
     }
     
 }
