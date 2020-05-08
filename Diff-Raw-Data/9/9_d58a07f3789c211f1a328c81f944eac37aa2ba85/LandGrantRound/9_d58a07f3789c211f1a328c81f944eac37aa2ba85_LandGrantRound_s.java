 package game.round;
 
 import game.Character;
 import game.Map;
 import game.Plot;
 
 import java.util.ArrayList;
 
 import core.Button;
 import core.render.RenderableString;
 import core.render.SimpleRender;
 
 /**
  * This class represents the logic and painting for the LandGrantRound
  * which occurs once at the very start of the game.
  * 
  * @author grant
  * @author
  */
 public class LandGrantRound extends Round
 {	
 	private SimpleRender characterOverview;
 	private RenderableString prompt;
 	
 	private ArrayList<Character> characters;
 	private int currentCharacterIndex;
 	private int metaRound;
 	
 	private Button passButton;
 	
 	/**
 	 * This instantiates some constant images and Renderable objects 
 	 * present throughout round as well as prepare ArrayList of 
 	 * Characters for use in the round. 
 	 * 
 	 * @param session Contains all info about game like players, round, etc
 	 */
 	public LandGrantRound()
 	{	
 		metaRound = 1;
 		
 		characterOverview = new SimpleRender("assets/images/characterStatBackground.png");
 		characterOverview.setX(0);
 		characterOverview.setY(350);
 
 		currentCharacterIndex = 0;
 	}
 	
 	public void init()
 	{		
 		characters = new ArrayList<Character>();
 		for (Character character : session.getCharacters())
 		{
 			characters.add(character);
 		}
 		
 		prompt = new RenderableString();
 		prompt.setX(250);
 		prompt.setY(390);
 		
 		passButton = new Button("assets/images/passButton.png");
 		passButton.setX(550);
 		passButton.setY(360);
 		passButton.setWidth(50);
 		passButton.setHeight(30);
 	}
 
 	/**
 	 * Alert the state that an mouse click occurred
 	 * 
 	 * @param x The x pos in pixels
 	 * @param y The y pos in pixels
 	 * @param leftMouse Whether the left mouse was pressed
 	 */
 	public void click(int x, int y, boolean leftMouse)
 	{
 		int xGridPos = (int)Math.floor(y / Plot.SIZE); // so you see the y -> x and x -> switch because of how coordinate
 		int yGridPos = (int)Math.floor(x / Plot.SIZE); // system is opposite of array indexing 
 		
 		if (metaRound <= 2)
 		{	
 			if (validClick( x, y))
 			{
 				buyProperty(xGridPos, yGridPos, 0); // first two rounds offer plots for free!
 			}
 		} 
 		else 
 		{
 			if (passButton.inBounds(x,y))
 			{		
 	               characters.remove(currentCharacterIndex);
 	               currentCharacterIndex = currentCharacterIndex++;
 	               // handle when index exceeds bounds of ArrayList
 	               if (currentCharacterIndex >= characters.size() && characters.size() != 0)
 	            	   currentCharacterIndex %= characters.size();
 	               // If we arrive back at first person (index 0), we move on to next metaround
 	               if (currentCharacterIndex == 0) 
 	               {
 	            	   metaRound++;
 	               }
 	        }
 			else 
 	        {
 	        	if (validClick(x, y)) 
 	        	{
 	        		buyProperty(xGridPos, yGridPos, 300); // $300 for each plot after first 2 rounds
 	        	}
 	        }
 		}
 	}
 	
 	/**
 	 * Since this same code occurred twice in click method in the conditionals,
 	 * I decided to seperate it out. This handles all the necessary work whenever
 	 * a character decides to buy a plot at any cost. For example, update index to
 	 * next player, add plot to list of plots owned by player, and subtract cost from
 	 * player's money amount.
 	 * 
 	 * @param xGridPos index of row in array of plots
 	 * @param yGridPos index of column in array of plots
 	 * @param cost cost of plot (in this round either $0 or $300)
 	 */
 	private void buyProperty(int xGridPos, int yGridPos, int cost) 
 	{
         Map map = session.getMap();
         Plot plot = map.get(xGridPos,yGridPos);
         
         if (plot.isOwned())
         {
         	return;
         }
         
         Character character = characters.get(currentCharacterIndex);
         
         plot.setIsOwned(true);
         plot.setColor(character.getColor());
                
         character.addPlot(plot);
         
         int currentMoney = character.getMoney();
         
         character.setMoney(currentMoney - cost);
         
         // if player does not have enough money to even purchase one more plot, then remove from ArrayList
         if (character.getMoney() < 300) 
  		{
  			characters.remove(currentCharacterIndex);
  		}
 		currentCharacterIndex++;
  		
         if (currentCharacterIndex >= characters.size() && characters.size() != 0) 
         {
      	   currentCharacterIndex %= characters.size();
         }
  		if (currentCharacterIndex == 0) 
  		{
  			metaRound++;
  		}
 
 }
 	
 	/**
 	 * This simply takes in x and y coordinates and checks to see if 
 	 * those coordinates are within the plot taken up by the "Town."
 	 * 
 	 * @param x Horizontal distance from left edge of window in pixels
 	 * @param y Vertical distance from top of window in pixels
 	 * @return Whether or not those coordinates are within the "Town"
 	 */
 	private boolean validClick(int x, int y)
 	{
 		return !( (  y < ( Plot.SIZE * 5 ) ) && ( y >= ( Plot.SIZE * 2) && y < ( Plot.SIZE * 3) && ( x >= ( Plot.SIZE * 4) && x < ( Plot.SIZE * 5) ) ) );
 	}
 	
 	/**
 	 * Update called every EventLoop cycle. Continually repaints 
 	 * things on JPanel in order to keep screen updated and current.
 	 */
 	public void update() 
 	{
 		renderables.clear();
 		renderableStrings.clear();
 		
 		// plots
 		for (int a = 0; a < 5; a++)
 		{
 			for (int b = 0; b < 9; b++)
 			{
 				Map map = session.getMap();
 				Plot plot = map.getPlot(a, b);
 				renderables.add(plot);
 			}
 		}
 
 		renderables.add(characterOverview);
 		
		ArrayList<Character> characters = session.getCharacters();
 		Character character = characters.get(currentCharacterIndex);
 		
 		// Players should not be able to pass if plots are free!
 		if (metaRound > 2) {
 			renderables.add(passButton);
 		}
 		
		
 		prompt.setText(character.getName() + " please select a plot");
 		renderableStrings.add(prompt);
 		
 		RenderableString name = new RenderableString(character.getName(), 15, 364);
 		renderableStrings.add(name);
 		
 		RenderableString money = new RenderableString("$" + character.getMoney(), 30, 380);
 		renderableStrings.add(money);
 
 		RenderableString ore = new RenderableString("" + character.getOre(), 30, 395);
 		renderableStrings.add(ore);
 
 		RenderableString food = new RenderableString("" + character.getFood(), 30, 410);
 		renderableStrings.add(food);
 
 		RenderableString energy = new RenderableString("" + character.getEnergy(), 90, 395);
 		renderableStrings.add(energy);
 
 		RenderableString crystite = new RenderableString("" + character.getCrystite(), 90, 410);
 		renderableStrings.add(crystite);
 	}
 
 	/**
 	 * Whenever a player passes or no longer has enough money for a new 
 	 * plot, they are removed from ArrayList. When no players left, the 
 	 * round is over.
 	 */
 	public boolean isDone() 
 	{
 		if (0 == characters.size())
 		{
 			return true;
 		}
 		
 		return false;
 	}
 }
