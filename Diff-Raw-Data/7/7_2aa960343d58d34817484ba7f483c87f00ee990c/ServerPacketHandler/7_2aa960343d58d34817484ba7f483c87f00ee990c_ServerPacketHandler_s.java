 package enhancedbooks.common.core;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ari
  * Date: 02/01/13
  * Time: 3:33 AM
  */
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.network.packet.Packet43Experience;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 
 public class ServerPacketHandler implements IPacketHandler {
 
     int packetType = -1;
 
     /*
      * Packet format: byte 0: Packet Type Currently available packet types
      * Server: 0 = Button pressed in GuiExpBook byte 1: button pressed (1 is
      * add, -1 is subtract)
      */
 
     @Override
     public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player) {
         Utils.log("ServerPacketHandler: Side is " + FMLCommonHandler.instance().getSide());
         Utils.log("ServerPacketHandler: Packet Recieved");
         DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
         // Read the first int to determine packet type
         try {
             this.packetType = stream.readInt();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         /*
          * each packet type needs to implement an if and then whatever other
          * read functions it needs complete with try/catch blocks
          */
         if (this.packetType == 0) { // GuiExpBook button pressed
             int buttonPressed;
             try {
                 buttonPressed = stream.readInt(); // Grab which button the user pressed
                 Utils.log("ServerPacketHandler: Player pressed button " + buttonPressed);
             } catch (Exception ex) {
                 Utils.log("ServerPacketHandler: Invalid packet data from GUI!!!!");
                 return;
             }
 
             handleExpBookGuiButtonPressed(buttonPressed, (EntityPlayer) player);
 
         }
         if (this.packetType == 1) {
             // Atlas update NBT!
         }
     }
 
     private void handleExpBookGuiButtonPressed(int buttonPressed, EntityPlayer player) {
         if (player.isDead) return;
         ItemStack item = player.inventory.getCurrentItem();
         if (player.capabilities.isCreativeMode) return;
         if (buttonPressed == 1) {
             // Add one level to book
             if (player.experienceLevel >= 1 && item.getItemDamage() >= 1) {
                 Utils.log("ServerPacketHandler: Added one level to book");
                 item.damageItem(-1, player);
                if (player.experience < 5) {
                    player.addExperienceLevel(-1);
                    player.experience += player.xpBarCap(); //WRONG HOW THE FUCK DO I GET XP NEEDED TO LEVEL WAT
                }
                player.experience -= 5;
                //player.experienceLevel -= 1;
             }
         }
 
         if (buttonPressed == 2) {
             while (player.experienceLevel >= 1 && item.getItemDamage() >= 1) {
                 // Add all the player's xp! :D
                 item.damageItem(-1, player);
                 player.experienceLevel -= 1;
             }
             Utils.log("ServerPacketHandler: Added all levels to book");
         }
 
         // KK, subtracting now
         if (buttonPressed == -1) {
             // Subtract 1 level from book
             if (item.getItemDamage() < item.getMaxDamage()) {
                 item.damageItem(1, player);
                 player.experienceLevel += 1;
                 Utils.log("ServerPacketHandler: Subtracted one level");
             }
         }
         if (buttonPressed == -2) {
             // Subtract all the XP from the book! :D
             while (item.getItemDamage() < item.getMaxDamage()) {
                 item.damageItem(1, player);
                 player.experienceLevel += 1;
             }
             Utils.log("ServerPacketHandler: Subtracted all levels");
         }
         // Send a packet back to the client to tell it to update XP
         PacketDispatcher.sendPacketToPlayer(new Packet43Experience(player.experience, player.experienceTotal, player.experienceLevel), (Player) player);
     }
 
 }
