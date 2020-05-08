 /**
  * Game menu
  * 
  * @author	James Schwinabart
  */
 
 import java.awt.*;
 import java.awt.image.BufferStrategy;
 
 public class Menu
 {
 	public static final int TOP_SPACING				= 120;
 	public static final int LINE_SPACING			= 40;
 	public static final Font TEXT_FONT				= new Font("Dialog", Font.PLAIN, 32);
 	public static final Color BG_COLOR				= Color.black;
 	public static final Color TEXT_COLOR			= Color.white;
 	public static final Color SELECTED_TEXT_COLOR	= new Color(255, 0, 255);
 	
 	protected ParkViewProtector driver;
 	protected Graphics g;
 	protected BufferStrategy strategy;
 	
 	private MenuItem[] items			= {
 			new MenuItem("Back", 1),
 			new MenuItem("Options", 2),
 			new MenuItem("Save Game", 4),
			//new MenuItem("Load Game", 5),
 			new MenuItem("Quit Game", 9),
 		};
 	protected int selectedItem			= 0;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param p Driver class
 	 */
 	public Menu(ParkViewProtector p)
 	{
 		this.driver						= p;
 		this.g							= p.getGraphics();
 		this.strategy					= p.getBufferStrategy();
 	}
 	
 	public void show()
 	{
 		// handle key presses
 		if(ParkViewProtector.upPressed && selectedItem > 0)
 		{
 			selectedItem--;
 		}
 		else if(ParkViewProtector.downPressed && selectedItem < items.length - 1)
 		{
 			selectedItem++;
 		}
 		else if(ParkViewProtector.enterPressed)
 		{
 			execute(items[selectedItem].getAction());
 			
 			ParkViewProtector.enterPressed	= false;
 		}
 		else if(ParkViewProtector.escPressed)
 		{
 			ParkViewProtector.showMenu		= false;
 			ParkViewProtector.escPressed	= false;
 		}
 		
 		g									= (Graphics) strategy.getDrawGraphics();
 		
 		// draw the background
 		g.setColor(BG_COLOR);
 		g.fillRect(0, 0, ParkViewProtector.WIDTH, ParkViewProtector.HEIGHT);
 		
 		// set font and color
 		g.setFont(TEXT_FONT);
 		
 		// draw menu items
 		for(int i = 0; i < items.length; i++)
 		{
 			// set text color
 			if(i == selectedItem)
 			{
 				g.setColor(SELECTED_TEXT_COLOR);
 			}
 			else {
 				g.setColor(TEXT_COLOR);
 			}
 			
 			items[i].drawCentered(g, ParkViewProtector.WIDTH / 2, TOP_SPACING + (i + 1) * LINE_SPACING);
 		}
 		
 		// finish drawing
 		g.dispose();
 		strategy.show();
 		
 		// keep the game from running too fast
 		try
 		{
 			Thread.sleep(100);
 		}
 		catch(Exception e) {}
 	}
 	
 	/**
 	 * Runs the action for the menu item (almost certainly not the best way to do this)
 	 * 
 	 * @param actionId
 	 */
 	private void execute(int actionId)
 	{
 		switch(actionId)
 		{
 			case 1:
 				ParkViewProtector.showMenu	= false;
 				break;
 			
 			case 4:
 				DataSaver.save(driver.getGame());
 				break;
 			
 			case 5:
				// FIXME: Loading from the menu screen is broken
				
 				Game gameData				= DataSaver.load();
 				
 				if(gameData != null)
 				{
 					driver.setGame(gameData);
 
 					ParkViewProtector.showTitle	= false;
 				}
 				
 				break;
 				
 			case 9:
 				driver.quit();
 				break;
 		}
 	}
 }
