 package arcane_arcade_menus;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import arcane_arcade_main.GameSettings;
 import arcane_arcade_worlds.Navigator;
 import worlds.Room;
 import gameobjects.DrawnObject;
 import handleds.Actor;
 import handlers.ActorHandler;
 import handlers.DrawableHandler;
 import handlers.MouseListenerHandler;
 import helpAndEnums.CollisionType;
 import helpAndEnums.DepthConstants;
 
 /**
  * Menubuttons have different functions and can be clicked. MenuButtons also 
  * draw their description near them and change sprite index when mouse goes 
  * over them.
  *
  * @author Mikko Hilpinen.
  *         Created 4.9.2013.
  */
 public abstract class MenuButton extends AbstractButton
 {
 	// ATTRIBUTES	-----------------------------------------------------
 	
 	private boolean mouseon;
 	
 	
 	// CONSTRUCTOR	-----------------------------------------------------
 	
 	/**
 	 * Creates a menubutton to the given position with the given information 
 	 * added to the given handlers
 	 *
 	 * @param x The x-coordinate of the button
 	 * @param y The y-coordinate of the button
 	 * @param drawer The drawablehandler that will draw the button
 	 * @param actorhandler The actorhandler that will inform the button about 
 	 * step events
 	 * @param mouselistenerhandler The mouselistenerhandler that will inform 
 	 * the button about mouse events
 	 * @param room The room where the button is created at
 	 * @param message The message the button will show when the mouse hovers 
 	 * over it
 	 */
 	public MenuButton(int x, int y, DrawableHandler drawer, 
 			ActorHandler actorhandler, 
 			MouseListenerHandler mouselistenerhandler, Room room, 
 			String message)
 	{
 		super(x, y, DepthConstants.FOREGROUND, 
 				Navigator.getSpriteBank("menu").getSprite("button"), 
 				drawer, mouselistenerhandler, room);
 		
 		// Initializes attributes
 		this.mouseon = false;
 		
 		// Sets the radius and collision type
 		setCollisionType(CollisionType.CIRCLE);
 		setRadius(getSpriteDrawer().getSprite().getOriginX());
 		
 		// Creates the textdrawer
 		new ButtonTextDrawer((int) getX(), (int) getY(), drawer, actorhandler, 
 				message);
 	}
 	
 	
 	// IMPLEMENTED METHODS	--------------------------------------------
 
 	@Override
 	public boolean listensMouseEnterExit()
 	{
 		return true;
 	}
 
 	@Override
 	public void onMousePositionEvent(MousePositionEventType eventType,
 			Point2D mousePosition, double eventStepTime)
 	{
 		// Changes sprite index when mouse enters or exits the button
 		if (eventType == MousePositionEventType.ENTER)
 			getSpriteDrawer().setImageIndex(1);
 		else if (eventType == MousePositionEventType.EXIT)
 			getSpriteDrawer().setImageIndex(0);
 	}
 	
 	
 	// SUBCLASSES	-----------------------------------------------------
 	
 	// The buttontextdrawer draws a text over the button when the mouse is 
 	// over it
 	private class ButtonTextDrawer extends DrawnObject implements Actor
 	{
 		// ATTRIBUTES	-------------------------------------------------
 		
 		private char[] message;
 		
 		
 		// CONSTRUCTOR	-------------------------------------------------
 		
 		/**
 		 * Creates a new textdrawer to the given position added to the given 
 		 * handlers showing the given message
 		 *
 		 * @param x The x-coordinate of the text
 		 * @param y The y-coordinate of the text
 		 * @param drawer The drawer that will draw the text
 		 * @param actorhandler The actorhandler that will inform the object 
 		 * about step events
 		 * @param text The text the drawer will draw
 		 */
 		public ButtonTextDrawer(int x, int y, DrawableHandler drawer, 
 				ActorHandler actorhandler, String text)
 		{
 			super(x, y, DepthConstants.FOREGROUND, drawer);
 			
 			// Initializes attributes
 			this.message = text.toCharArray();
 			
 			// Adds the object to the handler
 			if (actorhandler != null)
 				actorhandler.addActor(this);
 		}
 
 		@Override
 		public int getOriginX()
 		{
 			return 0;
 		}
 
 		@Override
 		public int getOriginY()
 		{
 			return 0;
 		}
 
 		@Override
 		public void drawSelfBasic(Graphics2D g2d)
 		{
 			// Draws the text with the basic font and white colour
 			g2d.setFont(GameSettings.BASICFONT);
 			g2d.setColor(GameSettings.WHITETEXTCOLOR);
 			
 			g2d.rotate(Math.toRadians(-15 * this.message.length / 2 + 7));
 			
 			for (int i = 0; i < this.message.length; i++)
 			{
 				g2d.drawChars(this.message, i, 1, -10, 
 						- MenuButton.this.getWidth() / 2 - 20);
 				g2d.rotate(Math.toRadians(15));
 			}
 		}
 
 		@Override
 		public boolean isActive()
 		{
 			return MenuButton.this.isActive();
 		}
 
 		@Override
 		public void activate()
 		{
 			// Can't be activated through the textdrawer
 		}
 
 		@Override
 		public void inactivate()
 		{
 			// Can't bedeactivated through the textdrawer
 		}
 
 		@Override
 		public void act(double steps)
 		{
 			// Adjusts the alpha value of the text
 			if (MenuButton.this.mouseon)
 			{
 				if (getAlpha() < 1)
 					adjustAlpha((float) (0.05 * steps));
 			}
 			else
 			{
 				if (getAlpha() > 0)
 					adjustAlpha((float) (-0.01 * steps));
 			}
 		}
 		
 		@Override
 		public boolean isVisible()
 		{
 			return MenuButton.this.isVisible() && getAlpha() > 0;
 		}
 		
 		@Override
 		public boolean isDead()
 		{
 			return MenuButton.this.isDead();
 		}
 	}
 }
