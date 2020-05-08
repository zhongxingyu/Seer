 package linewars.display.panels;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 
 import linewars.display.Animation;
 import linewars.gameLogic.GameStateProvider;
 import linewars.gamestate.Player;
 
 @SuppressWarnings("serial")
 public class ResourceDisplayPanel extends Panel
 {
 	/**
 	 * The height and width of the panel
 	 */
	private static final int WIDTH = 75;
 	private static final int HEIGHT = 25;
 	
 	public ResourceDisplayPanel(GameStateProvider stateManager)
 	{
 		super(stateManager, WIDTH, HEIGHT);
 	}
 
 	@Override
 	public void updateLocation()
 	{
 		super.updateLocation();
 
 		setLocation(getParent().getWidth() - getWidth(), 0);
 	}
 	
 	@Override
 	public void paint(Graphics g)
 	{
 		Image buf = new BufferedImage(36, 12, BufferedImage.TYPE_INT_ARGB);
 		Graphics b = buf.getGraphics();
 		
 		//TODO get the correct player
 		Player player = stateManager.getCurrentGameState().getPlayer(0);
 		
 		b.setColor(Color.white);
 //		b.drawString(Integer.toString(3527), 0, 12);
		b.drawString(Integer.toString((int)player.getStuff()), 0, HEIGHT);
 		
 		g.drawImage(buf, 0, 0, WIDTH, HEIGHT, null);
 	}
 }
