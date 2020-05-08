 package rpg.network.packet;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import rpg.config.base.archer.ArcherArmor;
 import rpg.config.base.archer.ArcherWeapons;
 import rpg.playerinfo.PlayerInformation;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.relauncher.Side;
 
 public class PacketChoseArcher extends MinePGPacket {
 
     @Override
     protected void execute(EntityPlayer player, Side side) {
         if(side.isServer()) {
             PlayerInformation playerInfo = PlayerInformation.forPlayer(player);
             playerInfo.setPlayersClass("Archer");
             playerInfo.setCurrency(20);
             player.addExperienceLevel(1);
             player.inventory.addItemStackToInventory(new ItemStack(
                     ArcherWeapons.bowTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     ArcherWeapons.arrowTraining, 32));
             player.inventory.addItemStackToInventory(new ItemStack(
                     ArcherArmor.tunicTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     ArcherArmor.legsTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     ArcherArmor.bootsTraining, 1));
         }
     }
 
     @Override
     protected void readData(ByteArrayDataInput in) {
         
     }
 
     @Override
     protected void writeData(ByteArrayDataOutput out) {
         
     }
 
 }
