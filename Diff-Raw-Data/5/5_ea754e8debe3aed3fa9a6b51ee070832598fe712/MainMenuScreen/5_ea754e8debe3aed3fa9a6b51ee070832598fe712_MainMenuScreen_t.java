 package me.game.screen;
 
 import java.io.File;
 
 import me.engine.effect.FlipTransistion;
 import me.engine.guiobject.ButtonListener;
 import me.engine.guiobject.GuiObjectButton;
 import me.engine.math.RectangleI;
 import me.engine.render.RenderEngine;
 import me.engine.screen.ContainerGuiScreen;
 import me.engine.screen.LoadingScreen;
 import me.engine.screen.WorldLoaderScreen;
 import me.game.core.MainGame;
 import me.game.core.MyGameType;
 import me.game.world.WorldDebugger;
 import me.game.world.entity.MyPlayerEntity;
 
 public class MainMenuScreen extends ContainerGuiScreen implements ButtonListener
 {
 	private GuiObjectButton[] buttons;
 	private FlipTransistion flip;
 	
 	public MainMenuScreen()
 	{
 		initButtons();
 	}
 	
 	private void initButtons()
 	{
 		buttons = new GuiObjectButton[2];
 		
		buttons[0] = new GuiObjectButton("New Game", "gui;gui.png");
 		buttons[0].setListener(this);
 		buttons[0].setBounds(new RectangleI(300, 300, 424, 40));
 		add(buttons[0]);
 		
		buttons[1] = new GuiObjectButton("Load Game", "gui;gui.png");
 		buttons[1].setListener(this);
 		buttons[1].setBounds(new RectangleI(300, 380, 424, 40));
 		add(buttons[1]);
 	}
 	
 	@Override
 	public void offFocus()
 	{
 		super.offFocus();
 	}
 	
 	@Override
 	public void update()
 	{
 		if(flip != null)
 		{
 			if(flip.isDone())
 			{
 				startNewGame();
 			}
 			
 			flip.tick();
 		}
 		
 		updateObjects();
 	}
 	
 	@Override
 	public void render()
 	{
 		RenderEngine.resetColor();
 		
 		RenderEngine.bind("gui;menu_bg.png");
 		RenderEngine.drawTexture(new RectangleI(0, 0, 1024, 640), new RectangleI(0, 0, 800, 640));
 		
 		renderObjects();
 		
 		if(flip != null)
 		{
 			flip.render();
 		}
 	}
 
 	public void onButtonPress(GuiObjectButton source)
 	{
 		if(source == buttons[0])
 		{
 			flip = new FlipTransistion(500, false);
 		}
 	}
 	
 	private void startNewGame()
 	{
 		WorldLoaderScreen screen = new WorldLoaderScreen(null, "Room.map", MyGameType.class, "Start");
 		
 		canvas.setGuiScreen(screen);
 	}
 }
