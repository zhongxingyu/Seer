 package org.javateerz.ParkViewProtector.Menu;
 
 import org.javateerz.ParkViewProtector.DataStore;
import org.javateerz.ParkViewProtector.KeyboardConfig;
 import org.javateerz.ParkViewProtector.ParkViewProtector;
 import org.javateerz.ParkViewProtector.Sprite;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 
 public class GameOver extends Menu implements KeyListener
 {
 	public static final int TOP_SPACING				= 300;
 	
 	private Sprite gameOver;
 	
 	private MenuItem[] items						= {
 			new MenuItem("Try Again!", Menu.RESET),
 			new MenuItem("Quit Game", Menu.QUIT),
 		};
 	
 	public GameOver(ParkViewProtector p)
 	{
 		super(p);
 		
 		try
 		{
 			ParkViewProtector.playSound("game_over.wav");
 		}
 		catch(SlickException e1)
 		{
 			System.out.println("Couldn't play 'game over' sound");
 		}
 		
 		gameOver						= DataStore.INSTANCE.getSprite("game_over.png");
 	}
 	
	public void keyPressed(int key, char c)
	{
		switch(key)
		{
			case KeyboardConfig.NAV_UP:
				if(selectedItem > 0)
					selectedItem--;
				break;
		
			case KeyboardConfig.NAV_DOWN:
				if(selectedItem < items.length - 1)
					selectedItem++;
				break;
				
			case KeyboardConfig.ENTER:
				execute(items[selectedItem].getAction());
				break;
		
			case KeyboardConfig.BACK:
				ParkViewProtector.showMenu	= false;
				break;
		}
		
		clearKeyPressedRecord();
	}
	
	public boolean isAcceptingInput()
	{
		return true;
	}
	
 	public void show()
 	{
 		// key events
 		addKeyListener(this);
 		poll();
 		
 		// draw background
 		gameOver.draw(0, 0);
 		
 		// draw menu items
 		for(int i = 0; i < items.length; i++)
 		{
 			items[i].setFont(textFont);
 			
 			// set text color
 			if(i == selectedItem)
 			{
 				items[i].setColor(SELECTED_TEXT_COLOR);
 			}
 			else {
 				items[i].setColor(TEXT_COLOR);
 			}
 			
 			items[i].drawCentered(ParkViewProtector.WIDTH / 2, TOP_SPACING + (i + 1) * LINE_SPACING);
 		}
 		
 		// keep the game from running too fast
 		try
 		{
 			Thread.sleep(100);
 		}
 		catch(Exception e) {}
 	}
 }
