 /*
  * BlockScape
  * Written and Developed by Rock
  * Lots of Help from Darth the Pterodactyl
  */
 
 package net.blockscape;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.io.FileNotFoundException;
 import java.net.URL;
 
 import net.blockscape.block.Block;
 import net.blockscape.gui.Button;
 import net.blockscape.gui.OptionsScreenEnum;
 import net.blockscape.helper.DrawingAndLogicHelper;
 import net.blockscape.helper.GeneralHelper;
 import net.blockscape.helper.IconHelper;
 import net.blockscape.helper.LogHelper;
 import net.blockscape.helper.TerrainGenerationHelper;
 import net.blockscape.lib.Hardware;
 import net.blockscape.lib.MainReference;
 import net.blockscape.registry.ButtonRegistry;
 import net.blockscape.registry.GameRegistry;
 import net.blockscape.registry.RegistryRegistry;
 import net.blockscape.registry.TextBoxRegistry;
 import net.blockscape.save.SaveData;
 import net.blockscape.world.World;
 import net.blockscape.world.WorldBlock;
 import processing.core.PApplet;
 
 public class BlockScape extends PApplet
 {
 	public float deltaTime;
	private static final long serialVersionUID = 6L; //Bump this up for every commit
 	
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
 	public static String worldName = "New World";
 	
 	public static Player player;
 	
 	public static BlockScape instance;
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
 		instance = this;
 	    ground = false;
 	    isFlyMode = false;
 	    screenSelected = OptionsScreenEnum.mainScreen;
 	    
 	    LogHelper.init();
 	    SaveData.initDirectory(this);
 	    RegistryRegistry.init(this);
 	    IconHelper.init(this);
 	    player = new Player(width / 2, 0, this);
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
 		deltaTime = 1/frameRate;
 		
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
 	            SaveData.saveGame(worldName);
 	        
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
 	    else if (screenSelected == OptionsScreenEnum.worldSelector)
 	    {
 	        background(100, 100, 207);
             DrawingAndLogicHelper.drawWorldSelectionMenu(this);
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
 	        else if (GeneralHelper.isCharInput(key, keyCode))
 	            TextBoxRegistry.worldNamer.input = TextBoxRegistry.worldNamer.input + key;
 	        
 	        return;
 	    }
 	    else if (screenSelected == OptionsScreenEnum.noScreen)
 	    {
 	    	if (key == ' ' && ground)
 	    	    player.setYvelocity(MainReference.JUMP_AMOUNT * MainReference.FRAME_RATE);
         	if (key == 'a')
         	    player.left = true;
         	if (key == 'd')
         	    player.right = true;
         	if (key == 'w' && isFlyMode)
                 player.up = true;
         	if (key == 's' && isFlyMode)
                 player.down = true;
         	if (key == ENTER)
         	{
         		player.setX(mouseX);
         		player.setY(mouseY);
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
         	    setOptionsScreen(OptionsScreenEnum.mainPause);
 	    }
 	    else if (screenSelected == OptionsScreenEnum.mainPause)
 	    {
 	    	if (key == ESC)
 	    	    clearOptionsScreen();
 	    }
 	    
 	    if (key == ESC)
 	    {
 	        key = 0;
 	        return;
 	    }
 	}
 	
 	/**
 	 * called when a key is released
 	 */
 	public void keyReleased()
 	{
     	if (key=='a')
     	    player.left = false;
     	if (key=='d')
     	    player.right = false;
     	if (key == 'w')
             player.up = false;
         if (key == 's')
             player.down = false;
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
         if (ButtonRegistry.returnToMenu.held)
         {
             setOptionsScreen(OptionsScreenEnum.mainScreen);
             
             ButtonRegistry.returnToMenu.held = false;
         }
         if (ButtonRegistry.loadWorld.held)
         {
             setOptionsScreen(OptionsScreenEnum.worldSelector);
             ButtonRegistry.loadWorld.held = false;
             ButtonRegistry.generateWorldButtons();
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
         if (ButtonRegistry.backFromCreate.held)
         {
             setOptionsScreen(OptionsScreenEnum.mainScreen);
             ButtonRegistry.backFromCreate.held = false;
         }
         
         
         for (Button b : ButtonRegistry.worldButtons)
         {
             if(b.held)
             {
                 try
                 {
                     SaveData.loadGame(b.getText(), player);
                     clearOptionsScreen();
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
                 finally
                 {
                     b.held = false;
                 }
             }
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
         TerrainGenerationHelper.generateWorld(host);
 	}
 }
