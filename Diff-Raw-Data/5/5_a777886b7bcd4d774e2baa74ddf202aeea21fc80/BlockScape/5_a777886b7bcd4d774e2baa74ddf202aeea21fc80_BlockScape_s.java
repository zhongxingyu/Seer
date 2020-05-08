 /*
  * BlockScape
  * Written and Developed by Rock
  * Lots of Help from Darth
  */
 
 package net.blockscape;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 
 import net.blockscape.block.Block;
 import net.blockscape.gui.OptionsScreenEnum;
 import net.blockscape.helper.*;
 import net.blockscape.lib.Hardware;
 import net.blockscape.lib.MainReference;
 import net.blockscape.registry.ButtonRegistry;
 import net.blockscape.registry.GameRegistry;
 import net.blockscape.registry.RegistryRegistry;
 import net.blockscape.registry.TextBoxRegistry;
 import net.blockscape.save.SaveData;
 import net.blockscape.save.WorldSave;
 import net.blockscape.world.World;
 import net.blockscape.world.WorldBlock;
 
 import processing.core.PApplet;
 
 public class BlockScape extends PApplet
 {
 	private static final long serialVersionUID = -1390024970025652247L; //Dunno
 	
 	public static float distBetweenPlayerAndMouse; //The distance between the player and the user's mouse
 	
 	public static int saveDisplayCounter = 100;
 	public static int selectedBlockID = MainReference.DEFAULT_BLOCK_ID;
 	public static Block selectedBlock;
 	public static OptionsScreenEnum screenSelected;
 	
 	//Booleans
 	public static boolean canPlaceOrRemoveBlock;
 	public boolean ground;
 	public static boolean isFlyMode;
 	public static boolean isSaving;
 
 	//TODO VERRY TEMP WORLD NAME
	public static String worldName = "hello";
 	
 	/**
      * @param args
      */
     public static void main(String[] args)
     {
         PApplet.main(new String[]{"net.blockscape.BlockScape"});
     }
     
 	/**
 	 * Called on game start
 	 */
 	public void setup()
 	{
 	    ground = false;
 	    isFlyMode = false;
 	    screenSelected = OptionsScreenEnum.mainScreen;
         
 	    LogHelper.init();
 	    SaveData.initDirectory(this);
 	    RegistryRegistry.init(this);
 	    IconHelper.init(this);
 	    Player.initPlayer(width / 2, 0, this);
         Block.blockInit();
         
         selectedBlock = GameRegistry.getBlock(selectedBlockID);
         
         //Frame Stuffs
 		size(1280,720);
 		
 		if(frame != null)
 		{
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			URL icon = classLoader.getResource(MainReference.ICON_LOCATION);
 			frame.setTitle(MainReference.GAME_NAME);
 			frame.setIconImage(getToolkit().getImage(icon));
 		}
 		
 		frameRate(MainReference.FRAME_RATE);
 		
 		addMouseWheelListener(new MouseWheelListener() { public void mouseWheelMoved(MouseWheelEvent mwe) { mouseWheel(mwe.getWheelRotation()); }});
 	}
 	
 	/**
 	 * main game loop
 	 */
 	public void draw()
 	{
 	    if (screenSelected == OptionsScreenEnum.noScreen)
 	    {
 	        isSaving = true;
 	        saveDisplayCounter = 100;
 	        
 	        background(100, 100, 255);
 	        DrawingAndLogicHelper.drawGame(this);
 	    }
 	    else if (screenSelected == OptionsScreenEnum.mainPause)
 	    {
 	        background(100, 100, 175);
 	        DrawingAndLogicHelper.drawPauseMenu(this);
 	        
 	        if (saveDisplayCounter == 100)
 	            SaveData.saveGame(new WorldSave(worldName, World.getWorld()));
 	        
 	        if (--saveDisplayCounter < 0)
 	            isSaving = false;
 	    }
 	    else if (screenSelected == OptionsScreenEnum.mainScreen)
 	    {
 	        background(100, 100, 207);
 	        DrawingAndLogicHelper.drawMainMenu(this);
 	    }
 	    else if (screenSelected == OptionsScreenEnum.worldMaker)
 	    {
 	        background(100, 100, 207);
             DrawingAndLogicHelper.drawWorldCreateMenu(this);
 	    }
 	}
 	
 	/**
 	 * called when a key is pressed
 	 */
 	public void keyPressed()
 	{
 	    if (screenSelected == OptionsScreenEnum.worldMaker)
 	    {
 	        if (key == BACKSPACE)
 	        {
 	            if(TextBoxRegistry.worldNamer.input.length()>0)
 	                TextBoxRegistry.worldNamer.input = TextBoxRegistry.worldNamer.input.substring(0, TextBoxRegistry.worldNamer.input.length() - 1);
 	        }
	        else if (key != TAB || key != ENTER || key != ESC || key != SHIFT || key != RETURN)
 	            TextBoxRegistry.worldNamer.input = TextBoxRegistry.worldNamer.input + key;
 	        
 	        return;
 	    }
 	    
 	    if (screenSelected != OptionsScreenEnum.noScreen)
 	    {
 	        key = 0;
 	        return;
 	    }
 	    
     	if (key == ' ' && ground)
     	    Player.setYvelocity(-3);
     	if (key == 'a')
     	    Player.left = true;
     	if (key == 'd')
     	    Player.right = true;
     	if (key == 'w' && isFlyMode)
             Player.up = true;
     	if (key == 's' && isFlyMode)
             Player.down = true;
     	if (key == ENTER)
     	{
     		Player.setX(mouseX);
     		Player.setY(mouseY);
     	}
     	if (key == 'r' && !World.isBlockLocationOpen(mouseX / 16, (height - mouseY) / 16))
     	{
     	    WorldBlock block = World.getBlock(mouseX / 16, (height - mouseY) / 16);
             
             if (block != null)
             {
                 selectedBlockID = block.getBlock().blockID;
                 selectedBlock = block.getBlock();
             }
     	}
     	
     	if (key == ESC)
     	{
     	    key = 0;
     	    screenSelected = OptionsScreenEnum.mainPause;
     	}
 	}
 	
 	/**
 	 * called when a key is released
 	 */
 	public void keyReleased()
 	{
     	if (key=='a')
     	    Player.left = false;
     	if (key=='d')
     	    Player.right = false;
     	if (key == 'w')
             Player.up = false;
         if (key == 's')
             Player.down = false;
 	}
 	
 	
 	/**
 	 * called when the mouse wheel is used
 	 * @param delta up (-1) down (1)
 	 */
 	public void mouseWheel(int delta)
 	{
 	    if (delta == Hardware.mwUP)
 	    {
 	        selectedBlockID++;
 	        
 	        if (selectedBlockID > GameRegistry.getBlockRegistrySize())
 	            selectedBlockID = 1;
 	    }
 	    
 	    if (delta == Hardware.mwDWN)
 	    {
 	        selectedBlockID--;
 	        
 	        if (selectedBlockID < 1)
 	            selectedBlockID = GameRegistry.getBlockRegistrySize();
 	    }
 	    
 	    selectedBlock = GameRegistry.getBlock(selectedBlockID);
 	}
 	
 	public void mouseReleased()
     {
         if (ButtonRegistry.flyMode.held)
             isFlyMode = !isFlyMode;
         if (ButtonRegistry.returnToGame.held)
             clearOptionsScreen();
         if (ButtonRegistry.exitGame.held)
             BlockScape.endgame();
         if (ButtonRegistry.loadWorld.held)
         {
             try
             {
                 World.setWorld(SaveData.getWorldSaveData(worldName));
                 Player.setX(SaveData.getPlayerX(worldName));
                 Player.setY(SaveData.getPlayerY(worldName));
             }
             catch (IOException e)
             {
                 e.printStackTrace();
                 return;
             }
             finally
             {
                 ButtonRegistry.loadWorld.held = false;
             }
             
             clearOptionsScreen();
         }
         if (ButtonRegistry.newWorld.held)
         {
             setOptionsScreen(OptionsScreenEnum.worldMaker);
             ButtonRegistry.newWorld.held = false;
         }
         if (ButtonRegistry.createWorld.held)
         {
             worldName = TextBoxRegistry.worldNamer.input;
             
             try
             {
                 generateNewWorld(worldName, this);
             }
             catch (FileNotFoundException e)
             {
                 e.printStackTrace();
             }
             finally
             {
                 ButtonRegistry.createWorld.held = false;
             }
             
             clearOptionsScreen();
         }
             
     }
 	
 	public static void setOptionsScreen(OptionsScreenEnum screenSelected_)
 	{
 	    screenSelected = screenSelected_;
 	}
 	
 	public static void clearOptionsScreen()
 	{
 	    screenSelected = OptionsScreenEnum.noScreen;
 	}
 	
 	public static void endgame()
 	{
 	    System.exit(0);
 	}
 	
 	public static void generateNewWorld(String name, PApplet host) throws FileNotFoundException
 	{
 	    World.initBlankWorld();
         SaveData.addWorld(new WorldSave(name, World.getWorld()));
         TerrainGenerationHelper.generateWorld(host);
 	}
 }
