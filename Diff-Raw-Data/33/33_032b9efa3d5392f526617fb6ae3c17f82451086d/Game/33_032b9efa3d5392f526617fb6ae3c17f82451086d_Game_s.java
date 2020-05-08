 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 
 public class Game extends JComponent implements Runnable, MouseListener, MouseMotionListener
 {
 	Thread gameThread;
 	int mouseX, mouseY;
 	int attack = 25;
 	boolean enabled = true;
 	boolean fleet = false;
 	BufferedImage bufferedImage;
 	int draw = 0;
 	private String statsPlayer, statsBot;
 
 	private GamePiece selected;         // `null` if no GamePiece has been selected
 	private GamePiece hoverOn;      // `null` if the mouse is hovering over no GamePieces
 
 	private Color selectedColor = Color.BLUE;
 	private Color neutralUiColor = Color.GRAY;
 
 	private Universe universe;
 
 	private Player humanPlayer;
 
 
 	/**
 	 * Run a game with 2 players
 	 */
 	public Game()
 	{
 		this(2);
 	}
 
 	public Game(int numberOfPlayers)
 	{
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
 		setOpaque(true);
 		setPreferredSize(screenSize);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 
 		// create a universe with 12 planets and a minimum separation of 50 units
		this.universe = new Universe(screenSize.width, screenSize.height, 12, 50);
 
 		// add a human player, then computer players.
 		this.humanPlayer=new Player(1);
 		this.universe.addPlayer(humanPlayer);
 		for (int i=1; i < numberOfPlayers; i++) {
 			this.universe.addPlayer(new ComputerPlayer(i+1));
 		}
 
 		// get the Universe to set up the players with planets, etc.
 		this.universe.setUpPlayers();
 
 		gameThread = new Thread(this);
 		gameThread.start();
 
 
 
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D) g.create();
 
 		// draw the universe background
 		g2.drawImage(this.universe.getBackground(), 0, 0, null);
 
 		// load a fleet image, which is reused
 		// TODO: remove this, as fleets provide their own images
 		BufferedImage b;
 		try {
 			File f = new File("resources/images/fleets/fleet1.png");
 			b = ImageIO.read(f);
 			bufferedImage=GamePiece.resize(b, 10);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// set the Swing component to be opaque
 		setOpaque(true);
 
 		// TODO: write separate methods to draw all planets/fleets
 
 		// draw planets
 		Planet[] planets = this.universe.getPlanets();
 		for (int i=0; i < planets.length; i++) 
 		{
 			Planet p = planets[i];
 			g2.drawImage(p.getImage(), p.getX(), p.getY(), null);
 
 			// draw the number of ships on the planet for the controlling player
 			g2.setColor(this.neutralUiColor);
 			if (p.getControllingPlayer() != null) 
 			{
 				g2.setColor(p.getControllingPlayer().getColor());
 			}
 			g2.drawString("ships: "+ p.getPlayerShips(p.getControllingPlayer()), p.getX(), p.getY());
 
 
 			//Draw the % of capture in the middle of the Planet.
 			g2.drawString((int)p.percentCaptured()+"%", p.getXCenter(), p.getYCenter());
 
 			// draw the number of Resource Units the planet has 
 			g2.setColor(Color.GRAY);
 			g2.drawString("RUs: "+ p.getResourceValue(), p.getX() + 2*p.getRadius(), p.getY());
 		}
 
 		// draw Fleets
 		for (int i=0; i < this.universe.getFleets().length; i++) 
 		{
 			Fleet f = this.universe.getFleets()[i];
 			g2.drawImage(f.getImage(), (int)f.getXDouble(), (int)f.getYDouble(), null);
 
 			// draw the number of ships in the fleet
 			g2.setColor(this.neutralUiColor);
 			if (f.getPlayer() != null) 
 			{
 				g2.setColor(f.getPlayer().getColor());
 			}
 			g2.drawString("ships: "+ f.getShips(), (int)f.getXDouble(), (int)f.getYDouble());
 		}
 
 		// NOTE: what does this do?
 		if(draw == 1) g2.drawOval(200,200,400,400);
 
 		// TODO: selection and hovering ought to use GamePiece.getColour() 
 		// draw around the selected GamePiece
 		if (this.selected != null)
 		{
 			g2.setColor(this.selectedColor);
 			g2.setStroke(new BasicStroke(5F));
 			g2.drawOval(this.selected.getX(), this.selected.getY(), this.selected.getRadius()*2, this.selected.getRadius()*2);
 		}
 
 		if(this.selected instanceof Planet)
 		{
 			Planet p=(Planet)this.selected;
 			g2.drawOval(p.getXCenter()-p.travelRadius(), p.getYCenter()-p.travelRadius(), p.travelRadius()*2, p.travelRadius()*2);
 		}
 
 
 		// draw around the hoverOn GamePiece
 		if (this.hoverOn != null) 
 		{
 			g2.setColor(this.neutralUiColor);
 			if (this.hoverOn.getPlayer() != null) {        		
 				g2.setColor(this.hoverOn.getPlayer().getColor());
 			}
 			g2.setStroke(new BasicStroke(5F));
 			g2.drawOval(this.hoverOn.getX(), this.hoverOn.getY(), this.hoverOn.getRadius()*2, this.hoverOn.getRadius()*2);
 			// TODO: GamePiece.getSelectionRadius() method - public int
 		}
 
 		// draw lines between the selected GamePiece and hoveredOver GamePiece
 		if ((this.selected != null) && (this.hoverOn != null)) 
 		{
 			g2.setColor(Color.red);
 			g2.setStroke(new BasicStroke(3F));
 			g2.drawLine(this.selected.getXCenter(), this.selected.getYCenter(), this.hoverOn.getXCenter(), this.hoverOn.getYCenter());
 		}
 
 		g2.dispose();
 	}
 
 
 	/*
 	 * Code to be executed by a thread.
 	 * 
 	 * This loop should update the state of everything, then sleep for 10ms.
 	 */
 	public void run()
 	{
 		while(true)
 		{
 
 			// TODO: update all the planets
 			for (Planet p : this.universe.getPlanets()) {
 				p.update();
 			}
 
 			// TODO: update all the fleets
 			for (Fleet f : this.universe.getFleets())
 			{
 				f.update();
 				if(f.hasArrived())
 				{
 					int d=f.getTarger().getPlayerShips(f.getPlayer());
 					d+=f.getShips();
 					f.getTarger().setPlayerShips(f.getPlayer(), d);
 					universe.deleteFleet(f);
 				}
 			}
 
 			// TODO: update all the players
 			for (Player p : this.universe.getPlayers()) {
 				p.update(this.universe);
 			}
 
 
 			// set the process to sleep for 10ms
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			// then repaint the window
 			repaint();
 
 		}
 	}
 
 	/*
 	 * Method creating output string for stats of Player
 	 */
 
 	public String getStatsPlayer(){
 		int totalShips = 0, totalResources = 0, planets = 0;
 		statsPlayer = "PLAYER \n";
 		for (int i=0; i< this.universe.getPlanets().length;i++)
 		{
 			if(this.universe.getPlayers()[0].owns(this.universe.getPlanets()[i])) 
 			{
 				statsPlayer = statsPlayer + "\n" + 
 						"Planet" + i + "\n" +
 						"---Ships: " + this.universe.getPlanets()[i].getPlayerShips(this.universe.getPlayers()[0])+ " ---Resources: " + this.universe.getPlanets()[i].getResourceValue();
 				totalShips = totalShips + this.universe.getPlanets()[i].getPlayerShips(this.universe.getPlayers()[0]);
 				totalResources = totalResources + this.universe.getPlanets()[i].getResourceValue();
 				planets++;
 
 			}
 		}
 
 		return this.statsPlayer + "\n" + 
 		"Total number of ships: " + totalShips + "\n"+ 
 		"Total number of resources: " + totalResources + "\n" +
 		"Percentage of universe: " + (int) (planets/this.universe.getPlanets().length)*100 + "%";
 	}
 
 	/*
 	 * Method creating output string for stats of ComputerPlayer
 	 */
 
 	public String getStatsBot(){
 		int totalShips = 0, totalResources = 0, planets = 0;
 		statsBot = "Bot \n";
 		for (int i=0; i< this.universe.getPlanets().length;i++)
 		{
 			if(this.universe.getPlayers()[1].owns(this.universe.getPlanets()[i])) 
 			{
 				statsBot = statsBot + "\n" + 
 						"Planet" + i + "\n" +
 						"---Ships: " + this.universe.getPlanets()[i].getPlayerShips(this.universe.getPlayers()[1])+ " ---Resources: " + this.universe.getPlanets()[i].getResourceValue();
 				totalShips = totalShips + this.universe.getPlanets()[i].getPlayerShips(this.universe.getPlayers()[1]);
 				totalResources = totalResources + this.universe.getPlanets()[i].getResourceValue();
 				planets++;
 			}
 		}
 
 		return this.statsBot + "\n" + 
 		"Total number of ships: " + totalShips + "\n"+ 
 		"Total number of resources: " + totalResources + "\n" + 
 		"Percentage of universe: " + (int) (planets/this.universe.getPlanets().length)*100 + "%";
 	}
 
 
 	// ******************************************************************
 	// METHODS BELOW HERE ARE APPLIED WHEN THE APPROPRIATE ACTIONS OCCUR
 	// ******************************************************************
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* mousePressed is checking coordinates of mouse whenever it was pressed
 	 * in this case whether the cursor is inside of either planet, calls appropriate action
 	 *  
 	 * Matej
 	 */
 	@Override
 	public void mousePressed(MouseEvent e)
 	{
 		int clickX = e.getX();
 		int clickY = e.getY();
 
 		boolean l=false; //l stands for lamp.
 
 		// TODO: if the mouse has clicked on co-ordinates not inside a GamePiece
 		// ...
 		for (Planet g : this.universe.getPlanets()) 
 		{
 			// if the mouse has been clicked inside a GamePiece without any other selected, set this GamePiece to be selected
 			if (g.isCoordinateInside(clickX, clickY))
 			{
 				l=true;
 				if(this.selected == null)
 				{
 					this.selected = g;
 					System.out.println(this.selected);
 
 					//repaint();
 					break;
 				}
 				// if another GamePiece has already been selected, perform the appropriate action
 				else
 				{
 					// if previously selected item is not the same, the inputDialog window should pop up
 					// however, this does not work properly(sometimes it does, sometimes it doesnt)..but I cant seem to figure it out now
 					if(this.selected!=g)
 					{
 
 						if(this.selected instanceof Planet)
 						{
 							//Check if the clicked Planet is in the attack radius of the selected one.
 							Planet p=(Planet)this.selected;
 							if(p.canReach(g) && p.getPlayerShips(humanPlayer)>0 && p.getOwner()==humanPlayer)
 							{
 								System.out.println(this.selected + "\n" + g);
 								Object[] possibilities = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
 								String s = (String)JOptionPane.showInputDialog(this.getParent(), "Number of ships to be sent: ","ATTACK",JOptionPane.PLAIN_MESSAGE, null, possibilities, "2");
 
 								int startingShips = p.getPlayerShips(humanPlayer);
 								int shipsToSend = Math.min(Integer.parseInt(s),startingShips);
 								humanPlayer.initiateMovement(p, g, shipsToSend);
 								l = false;
 							}
 							//If it's not, then don't do anything and stop the loop.
 							else
 							{
 							}
 						}
 
 
 					}
 					else
 					{
 						l=false; //If we click on the selected planet again we de-select it.
 					}
 					// TODO: if a planet has been selected, and another planet clicked on, show UI to send a fleet
 					// TODO: figure out what needs to be done for the other cases (Planet->Fleet, Fleet->Whatever)
 				}
 				break;
 			}
 		}
 		if(!l)this.selected = null; //The click was not inside any planet or was inside the selected one.
 		repaint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 	/*
 	 * If the mouseMoved inside of a GamePiece's clickRadius, set this.hoverOn to be that GamePiece.
 	 * Otherwise set this.hoverOn to be `null`.
 	 */
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();
 
 		// TODO: if the mouse is inside a GamePiece, set the GamePiece to be the hoveredOn GamePiece
 		for (Planet g : this.universe.getPlanets()) 
 		{
 			if (g.isCoordinateInside(mouseX, mouseY)) 
 			{
 				this.hoverOn = g;
 				repaint();
 				break;
 			} 
 			else 
 			{
 				this.hoverOn = null;
 			}
 		}
 	}
 }
