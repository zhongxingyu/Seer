 package rpg.network.packet;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import rpg.config.base.mage.MageArmor;
 import rpg.config.base.mage.MageWeapons;
 import rpg.playerinfo.PlayerInformation;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.relauncher.Side;
 
 public class PacketChoseMage extends MinePGPacket {
 
     @Override
     protected void execute(EntityPlayer player, Side side) {
         if(side.isServer()) {
             PlayerInformation playerInfo = PlayerInformation.forPlayer(player);
             playerInfo.setCurrency(20);
             player.addExperienceLevel(1);
             playerInfo.setPlayersClass("Mage");
             player.inventory.addItemStackToInventory(new ItemStack(
                     MageWeapons.staffTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     MageArmor.hatTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     MageArmor.robeTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     MageArmor.sandalsTraining, 1));
         }
     }
 
     @Override
     protected void readData(ByteArrayDataInput in) {
         
     }
 
     @Override
     protected void writeData(ByteArrayDataOutput out) {
         
     }
 
 }
