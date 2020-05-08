 package assets.levelup;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ItemRespecBook extends Item
 {
     public ItemRespecBook(int i)
     {
         super(i);
     }
@Override
     public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
     {
         PlayerExtendedProperties.resetSkills(entityplayer);
         int deathLvl = PlayerExtendedProperties.getPlayerDeathLevel(entityplayer);
         if (entityplayer.experienceLevel < deathLvl)
         {
         	PlayerExtendedProperties.getSkillMap(entityplayer).put("XP", deathLvl * 3);
         }
         else
         {
         	PlayerExtendedProperties.getSkillMap(entityplayer).put("XP", entityplayer.experienceLevel * 3);
         }
        itemstack.stackSize--;
         return itemstack;
     }
 }
