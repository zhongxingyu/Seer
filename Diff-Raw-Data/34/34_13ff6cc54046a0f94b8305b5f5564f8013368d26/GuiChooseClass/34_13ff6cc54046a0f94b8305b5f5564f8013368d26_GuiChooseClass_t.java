 package rpg.client.gui;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.inventory.GuiContainer;
 import net.minecraft.item.Item;
 import rpg.BaseClassList;
 import rpg.gui.ContainerEmpty;
 import rpg.network.packet.PacketChoseArcher;
 import rpg.network.packet.PacketChoseMage;
 import rpg.network.packet.PacketChoseWarrior;
 import rpg.playerinfo.PlayerInformation;
 
 public class GuiChooseClass extends GuiContainer {
 
     String[] starterList;
     Item item;
 
     public GuiChooseClass() {
         super(new ContainerEmpty());
         starterList = BaseClassList.getStarterStringList();
     }
    
     @Override
     @SuppressWarnings("unchecked")
     public void initGui() {
         super.initGui();
         buttonList.clear();
         for (int i = 0; i < starterList.length; i++) {
             buttonList.add(new GuiButton(i, width / 3 - 100, height / 6 + i
                     * 20, starterList[i]));
         }
     }
     
     @Override
     public void actionPerformed(GuiButton button) {        
         switch(button.id) {
             case 0:
                 new PacketChoseArcher().sendToServer();
                 break;
             case 1:
                 new PacketChoseMage().sendToServer();
                 break;
             case 2:
                 new PacketChoseWarrior().sendToServer();
                 break;
         }
         PlayerInformation playerInfo = PlayerInformation.forPlayer(mc.thePlayer);
         mc.thePlayer.sendChatToPlayer("[MinePG] You have chosen the path of the "
                 + playerInfo.getPlayersClass());
         mc.thePlayer.sendChatToPlayer("<Mysterious Voice> Take care in this world my Friend...");
         mc.thePlayer.sendChatToPlayer("<Mysterious Voice> Many things lurk here that are better left alone");
         mc.thePlayer.sendChatToPlayer("<Mysterious Voice> ...");
         mc.thePlayer.sendChatToPlayer("<Mysterious Voice> You will need to be equiped");
         mc.thePlayer.sendChatToPlayer("<Mysterious Voice> Have this equipment, learn to use it");
         mc.thePlayer.closeScreen();
        //MinecraftForge.EVENT_BUS.register(new HudOverlayHandler());
     }
 
     @Override
     public void drawGuiContainerBackgroundLayer(float par3, int par1, int par2) {
         drawDefaultBackground();
         drawString(fontRenderer, "Welcome to the world of MinePG!!", 3, 10,
                 0xFFFFFF);
         drawString(fontRenderer, "Please pick your desired Class:", 3, 20,
                 0xFFFFFF);
     }
 
 
 
     @Override
     public void keyTyped(char i, int i1) {
     }
 }
