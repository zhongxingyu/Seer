 package rpg.network.packet;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import rpg.config.base.warrior.WarriorArmor;
 import rpg.config.base.warrior.WarriorWeapons;
 import rpg.playerinfo.PlayerInformation;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.relauncher.Side;
 
 public class PacketChoseWarrior extends MinePGPacket {
 
     @Override
     protected void execute(EntityPlayer player, Side side) {
         if(side.isServer()) {
             PlayerInformation playerInfo = PlayerInformation.forPlayer(player);
             playerInfo.setCurrency(20);
             player.addExperienceLevel(1);
             playerInfo.setPlayersClass("Warrior");
             player.inventory.addItemStackToInventory(new ItemStack(
                     WarriorWeapons.swordTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     WarriorArmor.helmetTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     WarriorArmor.cuirassTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     WarriorArmor.greavesTraining, 1));
             player.inventory.addItemStackToInventory(new ItemStack(
                     WarriorArmor.sabatonsTraining, 1));
         }
     }
 
     @Override
     protected void readData(ByteArrayDataInput in) {
         
     }
 
     @Override
     protected void writeData(ByteArrayDataOutput out) {
         
     }
 
 }
