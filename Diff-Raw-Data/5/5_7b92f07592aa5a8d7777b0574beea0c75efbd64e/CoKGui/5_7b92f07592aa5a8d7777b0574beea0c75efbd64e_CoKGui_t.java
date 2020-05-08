 package de.minestar.cok.gui;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 
 import org.lwjgl.opengl.GL11;
 
 import de.minestar.cok.game.CoKGame;
 import de.minestar.cok.network.CoKCommandPacket;
 import de.minestar.cok.network.PacketHandler;
 
 public class CoKGui extends GuiScreen {
 	
 	private static final int backgroundX = 243;
 	private static final int backgroundY = 139;
 	
 	private static final int START_GAME_BUTTON_ID = 0;
 	private static final int STOP_GAME_BUTTON_ID = 1;
 	
 	private GuiButton startGameButton;
 	private GuiButton stopGameButton;
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void drawScreen(int x, int y, float f){
 		//Setup buttons
 		this.buttonList.remove(startGameButton);
 		this.buttonList.remove(stopGameButton);
 		if(!CoKGame.gameRunning){
 			this.buttonList.add(startGameButton);
 		} else{
 			this.buttonList.add(stopGameButton);
 		}
 		
 		//draw screen
 		drawDefaultBackground();
 		
 		Minecraft.getMinecraft().renderEngine.bindTexture("/mods/ClashOfKingdoms/gui/GuiBackground.png");		
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		
 		int posX = (this.width - backgroundX) / 2;
 		int posY = (this.height - backgroundY) / 2;
 		
 		drawTexturedModalRect(posX, posY, 0, 0, backgroundX, backgroundY);
 		
 		super.drawScreen(x, y, f);
 	}
 	
 	@Override
 	public boolean doesGuiPauseGame() {
 		return false;
 	}
 	
 	
 	/**
      * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
      */
 	@SuppressWarnings("unchecked")
 	@Override
     protected void actionPerformed(GuiButton button) {
 		switch(button.id){
 		case START_GAME_BUTTON_ID: {
 			CoKCommandPacket.sendPacketToServer(PacketHandler.START_GAME_COMMAND, null);
 			break;
 		}
 		case STOP_GAME_BUTTON_ID: {
 			CoKCommandPacket.sendPacketToServer(PacketHandler.STOP_GAME_COMMAND, null);
 			break;
 		}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void initGui() {
 		this.buttonList.clear();
 		
 		int posX = (width - backgroundX) / 2;
 		int posY = (height - backgroundY) / 2;
 		
 		startGameButton = new GuiButton(START_GAME_BUTTON_ID, posX + 20, posY + 20, "Start Game");
 		stopGameButton = new GuiButton(STOP_GAME_BUTTON_ID, posX + 20, posY + 20, "Stop Game");
 		
 		if(!CoKGame.gameRunning){
 			this.buttonList.add(startGameButton);
 		} else{
 			this.buttonList.add(stopGameButton);
 		}
 	}
 
 }
