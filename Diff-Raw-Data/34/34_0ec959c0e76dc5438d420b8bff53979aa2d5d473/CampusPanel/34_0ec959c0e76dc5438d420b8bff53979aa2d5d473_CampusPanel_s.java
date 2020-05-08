 package main;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import main.BackToSchool.Screen;
 import main.Day.Course;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.*;
 import java.net.Proxy.Type;
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.Random;
 
 
 public class CampusPanel extends JPanel
 {
 	private int playerX, playerY, screenX, screenY, period=6;
 	private final static int PWIDTH=800, PHEIGHT=600, TILE=50, TILESX=16, TILESY=12, PADX=4, PADY=3, 
 				MOVERATE=750, FRAMERATE=50;
 	private enum Direction{LEFT, UP, RIGHT, DOWN};
 	private Image[][] tiles;
 	private Graphics dbg;
 	private Image buffer;
 	private ArrayList<Timer> moveTimers;
 	private Timer animate;
 	private boolean isLibrary, isTranscript;
 	
 	// BackToSchool classes
 	private BackToSchool frame;
 	private Campus campus;
 	private Tiles tileFactory;
 	private Day day;
 	private Player student;
 	private ArrayList<Pedestrian> pedestrians;
 	private Point destination, library;
 	private Image player, playerUp, playerDown, playerLeft, playerRight;
 	private Sound crowd;
 	private Sound bell;
 	
 	public CampusPanel()
 	{
 		//TODO: more elaborate instatiation
 		
 		campus = new Campus();
 		tiles = new Image[TILESX][TILESY];
 		tileFactory = new Tiles();
 			
 		try 
 		{	playerUp = ImageIO.read(new File("art/characters/student_backside.png"));
 			playerDown = ImageIO.read(new File("art/characters/student.png"));
 			playerLeft = ImageIO.read(new File("art/characters/student_leftside.png"));
 			playerRight = ImageIO.read(new File("art/characters/student_rightside.png"));	
 		} 
 		catch(IOException e){};
 		
 		student = new Player();
 		player = playerDown;
		day = new Day(3);
 		
 		crowd = new Sound("sounds/crowd.wav");
 		bell = new Sound("sounds/schoolbell.mp3");
 		
 		// for multiple pedestrians
 		pedestrians = new ArrayList<Pedestrian>();
 		moveTimers = new ArrayList<Timer>();
 		moveTimers.add(new Timer (MOVERATE, new PedestrianListener(0)));
 		moveTimers.add(new Timer (MOVERATE, new PedestrianListener(1)));
 		moveTimers.add(new Timer (MOVERATE, new PedestrianListener(2)));
 		moveTimers.add(new Timer (MOVERATE, new PedestrianListener(3)));
 		moveTimers.add(new Timer (MOVERATE, new PedestrianListener(4)));
 		
 		addKeyListener(new CampusListener());
 		addMouseListener(new CampusMouseListener());
 		setPreferredSize(new Dimension(PWIDTH,PHEIGHT));
 		setBackground(Color.RED);
 		setFocusable(true);
 		requestFocus();
 		
 		animate = new Timer(FRAMERATE, new AnimationListener());
 	}
 	
 	protected void sendFrame(BackToSchool frame)
 	{
 		this.frame = frame;
 	}
 	
 	public void continueClasses() 
 	{	
 		if (day.isDayEnd())
 			day.nextDay();
 		
 		if(day.getDay() == 10)
 		{
 			frame.addPanel(new FinalBattle(student), Screen.FINALBATTLE);
 			frame.switchPanel(Screen.FINALBATTLE);
 			return;
 		}
 		
 		else if (isLibrary)
 		{	
 			isLibrary = false;
 		}
 		else
 		{
 			generateClassroom();
 			renderScreen();		// so panel can be drawn immediately
 			
 			//* PEDESTRIAN TEST
 			ArrayList<Point> doors = campus.getDoors(false);
 			pedestrians.clear();
 			for (int i=0; i<moveTimers.size(); i++)
 			{
 				Point point = new Point(-1,-1);
 				while (!campus.isTraversable(point.x,point.y))
 				{
 					int x = (int)(Math.random()*campus.getWidth());
 					int y = (int)(Math.random()*campus.getHeight());
 					point.setLocation(x,y);
 				}
 				Point dest = doors.get((int)(Math.random()*doors.size()));
 				dest = new Point(dest.x, dest.y+1);
 				pedestrians.add(i, new Pedestrian(point, dest, campus));
 				moveTimers.get(i).start();
 			}
 		}
 		
 		crowd.playSound();
 		bell.playSoundOnce();
 		
 		animate.start();
 		for (Timer t : moveTimers)
 			t.start();
 	}
 	
 	private void generateClassroom()
 	{
 		ArrayList<Point> doors;
 		if (day.getNextCourse().ordinal() == 0) // first class (could hardcode as HUMANITIES)
 		{
 			playerX = playerY = 5;
 			screenX = screenY = 0;
 			destination = null;
 			
 			doors = campus.getDoors(true);
 			
 			for (int i=0; i<TILESX; i++)
 			{	for (int j=0; j<TILESY; j++)
 				{	Tile tile = campus.getTile(screenX+i, screenY+j);
 					if (tile != null)
 						tiles[i][j] = tileFactory.get(tile);
 				}
 			}
 		}
 		else
 			doors = campus.getDoors(false);
 		
 		if (destination != null)
 			doors.remove(destination);	// remove last used door (no two classes in same place in a row)
 		destination = doors.get((int)(Math.random()*doors.size()));
 		campus.addSign(destination, day.getNextCourse());
 		
 		if (day.isLibrary())
 		{
 			library = destination;
 			while (library.equals(destination))
 				library = doors.get((int)(Math.random()*doors.size()));
 			campus.addLibrary(library);
 		}
 			
 		// Update any sign tiles currently onscreen
 		Rectangle window = new Rectangle(screenX, screenY+1, TILESX, TILESY);
 		for (Point p : doors)
 			if (window.contains(p)) 
 				tiles[p.x-screenX][p.y-1-screenY] = tileFactory.get(campus.getTile(p.x, p.y-1));	
 	}
 	
 	private void handleDestination(boolean classroom) 
 	{	
 		// Shut down timers/sound for panel switch
 		animate.stop();
 		for (Timer t : moveTimers)
 			t.stop();
 		crowd.stopSound();
 		
 		// Handle panel switch
 		if (classroom)
 		{					
 			day.attendClass();
 			
 			if (day.isMidtermNext())
 			{	//System.out.println("MIDTERM");
				frame.addPanel(new Battle(student, day.getNextCourseName()), BackToSchool.Screen.BATTLE);
 				frame.switchPanel(BackToSchool.Screen.BATTLE);
 			}
 			else
 			{	//System.out.println("CLASS");
 				frame.addPanel(new MiniSplashPane(student, day), BackToSchool.Screen.MINISPLASH);
 				frame.switchPanel(BackToSchool.Screen.MINISPLASH);
 			}
 			
 			if(day.isTranscript())	// load transcripts for return from class
 			{
				System.out.println("Transcript made");
 				Transcript transcript = new Transcript(student, day);
 				frame.addPanel(transcript, BackToSchool.Screen.TRANSCRIPT);	
 			}
 		}
 		else
 		{
 			isLibrary = true;
 			day.visitLibrary();
 			frame.addPanel(new Library(student), BackToSchool.Screen.LIBRARY);
 			frame.switchPanel(BackToSchool.Screen.LIBRARY);
 		}
 	}
 	
 	private void updateTiles(Direction dir)
 	{
 		switch (dir) 
 		{
 			case DOWN:
 				shiftDown();
 				break;
 			case LEFT:
 				shiftLeft();
 				break;
 			case RIGHT:
 				shiftRight();
 				break;
 			case UP:
 				shiftUp();
 				break;
 		}
 	}
 
 	private void shiftDown() 
 	{
 		// switch tiles over
 		for (int i=0; i<(TILESX); i++)
 			for (int j=0; j<(TILESY-1); j++)
 				tiles[i][j] = tiles[i][j+1];
 		// adjust screen location (within campus)
 		screenY++;
 		// load in new tiles from campus
 		for (int i=0; i<TILESX; i++)
 			tiles[i][TILESY-1] = tileFactory.get(campus.getTile(i+screenX, (TILESY-1)+screenY));
 	}
 
 	private void shiftLeft() 
 	{
 		if (screenX>0)
 		{
 			for (int i=TILESX-1; i>0; i--)
 				for (int j=0; j<(TILESY); j++)
 					tiles[i][j] = tiles[i-1][j];
 		
 			screenX--;
 		
 			for (int j=0; j<TILESY; j++)
 				tiles[0][j] = tileFactory.get(campus.getTile(screenX, j+screenY));
 		}
 	}
 	
 	private void shiftUp()
 	{
 		if (screenY > 0)
 		{
 			for (int i=0; i<(TILESX); i++)
 				for (int j=TILESY-1; j>0; j--)
 					tiles[i][j] = tiles[i][j-1];
 
 			screenY--;
 
 			for (int i=0; i<TILESX; i++)
 				tiles[i][0] = tileFactory.get(campus.getTile(i+screenX,screenY));
 		}
 	}
 	
 	private void shiftRight()
 	{
 		for (int i=0; i<TILESX-1; i++)
 			for (int j=0; j<(TILESY); j++)
 				tiles[i][j] = tiles[i+1][j];
 		
 		screenX++;
 		
 		for (int j=0; j<TILESY; j++)
 			tiles[TILESX-1][j] = tileFactory.get(campus.getTile((TILESX-1)+screenX, j+screenY));
 	}
 	
 	public void renderScreen()
 	{
 		if (buffer == null)
 		{  // create the buffer
 			buffer = createImage(PWIDTH, PHEIGHT);
 			dbg = buffer.getGraphics();
 		}
 		// render campus
 		dbg.setColor(Color.LIGHT_GRAY);
 		for (int i=0; i<TILESX; i++)
 			for (int j=0; j<TILESY; j++)
 			{	if (tiles[i][j]!=null)
 					dbg.drawImage(tiles[i][j], i*TILE, j*TILE, TILE, TILE, this);//  tiles[i][j].paintIcon(this, g, i*TILE, j*TILE);
 				else
 					dbg.fillRect(i*TILE, j*TILE, TILE, TILE);
 			}
 			
 		// render player
 		dbg.drawImage(player, playerX*TILE, playerY*TILE, TILE, TILE, this); //player.paintIcon(this, g, playerX, playerY);
 		
 		//* render pedestrians (currently even if off screen)
 		for (Pedestrian p : pedestrians)
 		{	if (p.hasMove())
 			{	int pedX = p.getLocation().x-screenX;
 				int pedY = p.getLocation().y-screenY;
 				dbg.drawImage(p.getImage(), pedX*TILE, pedY*TILE, TILE, TILE, this);
 			}
 		}
 		//*/
 		
 		// render interface 
 		//TODO: draw arrow to class
 		
 		dbg.setColor(Color.RED);
 		dbg.drawString("Day: "+day.getDay(), 50, 15);
 		dbg.drawString("Class: "+day.getNextCourseName(), 100, 15);
 		//dbg.drawString("Destination: "+destination.x+","+destination.y, 14*50, 15);		
 	}
 	
 	public void paintComponent(Graphics g)
 	{
     	super.paintComponent(g);
     	if (buffer != null)
     		g.drawImage(buffer, 0, 0, null);
     	//else
     		//System.out.println("no buffer");
 	}
 	
 	/*
 	 * 	SUPPORT CLASSES
 	 */
 	private class AnimationListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent arg0) 
 		{
 			renderScreen();	
 			repaint();
 		}
 	}
 	
 	private class PedestrianListener implements ActionListener
 	{		
 		int pedID;
 		
 		public PedestrianListener(int id)
 		{	
 			super();
 			pedID = id;
 		}
 		
 		public void actionPerformed(ActionEvent e) 
 		{
 			Pedestrian current = pedestrians.get(pedID);
 			if (current.hasMove())
 				current.move();
 			repaint();
 		}	
 	}
 	
 	private class CampusListener extends KeyAdapter
 	{	
 		public void keyPressed(KeyEvent e)
 		{	
 			if(e.getKeyCode() == KeyEvent.VK_UP)
 			{
 				player = playerUp;
 				if ((screenX+playerX==destination.x) && (screenY+playerY-1==destination.y))
 				{	
 					handleDestination(true);			
 				}
 				else if (day.isLibrary() && (screenX+playerX==library.x) && (screenY+playerY-1==library.y))
 				{	
 					handleDestination(false);
 				}
 				
 				if (campus.isTraversable(screenX+playerX, screenY+playerY-1))
 				{	
 					int cHeight = campus.getHeight();
 					if ((playerY-1>PADY-1 && playerY-1<(TILESY-PADY)) || screenY == 0
 							|| ((screenY == cHeight-TILESY) && playerY-1>=(TILESY-PADY)) )
 						playerY--;
 					else
 						updateTiles(Direction.UP);
 				}
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_LEFT)
 			{	
 				player = playerLeft;
 				if (campus.isTraversable(screenX+playerX-1, screenY+playerY))
 				{	
 					int cWidth = campus.getWidth();
 					if ((playerX-1>PADX-1 && playerX-1<(TILESX-PADX)) || screenX == 0
 							|| ((screenX == cWidth-TILESX) && playerX-1>=(TILESX-PADX)) )
 						playerX--;
 					else
 						updateTiles(Direction.LEFT);
 				}
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_DOWN)
 			{	
 				player = playerDown;
 				if (campus.isTraversable(screenX+playerX, screenY+playerY+1))
 				{	
 					int cHeight = campus.getHeight();
 					if ((playerY+1>PADY && playerY+1<(TILESY-PADY)) || screenY == (cHeight-TILESY)
 							|| ((screenY == 0) && playerY+1<=PADY) )
 						playerY++;
 					else
 						updateTiles(Direction.DOWN);
 				}
 			}
 			else if(e.getKeyCode() == KeyEvent.VK_RIGHT)
 			{	
 				player = playerRight;
 				if (campus.isTraversable(screenX+playerX+1, screenY+playerY))
 				{	
 					int cWidth = campus.getWidth();
 					if ((playerX+1>PADX && playerX+1<(TILESX-PADX)) || screenX == (cWidth-TILESX)
 							|| ((screenX == 0) && playerX+1<=PADX) )
 						playerX++;
 					else
 						updateTiles(Direction.RIGHT);
 				}
 			}
 			
 			// debug statements
 			//System.out.println("Player: "+playerX+","+playerY);
 			//System.out.println("Screen: "+screenX+","+screenY);
 			
 		}
 	}
 	
 	private class CampusMouseListener extends MouseAdapter
 	{
 		public void mousePressed(MouseEvent e) 
 		{
 			Point click = e.getPoint();
 			int xIndex = click.x/TILE;
 			int yIndex = click.y/TILE;
 			
 			Tile clicked = campus.getTile(xIndex+screenX, yIndex+screenY);
 			System.out.println("Clicked: " + xIndex + "," + yIndex + ", Tile: " + clicked);
 			
 			if (e.getButton() == MouseEvent.BUTTON3)
 			{
 			    Tile choice = (Tile)JOptionPane.showInputDialog(null, "Please choose a tile:", "Tile Swap",  
 			    		JOptionPane.QUESTION_MESSAGE, null, new Tile[] { 
 				    			// corners
 				    			new Tile(Tile.Type.CORNER, Tile.Direction.UP), new Tile(Tile.Type.CORNER, Tile.Direction.RIGHT), 
 				    			new Tile(Tile.Type.CORNER, Tile.Direction.DOWN), new Tile(Tile.Type.CORNER, Tile.Direction.LEFT), 
 				    			// forks
 				    			new Tile(Tile.Type.FORK, Tile.Direction.UP),  new Tile(Tile.Type.FORK, Tile.Direction.RIGHT),
 				    			new Tile(Tile.Type.FORK, Tile.Direction.DOWN), new Tile(Tile.Type.FORK, Tile.Direction.LEFT),
 				    			// straights
 				    			new Tile(Tile.Type.ROAD), new Tile(Tile.Type.ROAD, Tile.Direction.LEFT), 
 				    			// nature
 				    			new Tile(Tile.Type.TREE), new Tile(Tile.Type.TREE, Tile.Direction.DOWN), 
 				    			new Tile(Tile.Type.FLOWER, Tile.Direction.DOWN), new Tile(Tile.Type.GRASS, Tile.Direction.DOWN), 
 				    			new Tile(Tile.Type.LAND, Tile.Direction.DOWN),
 				    			// buildings 
 				    			new Tile(Tile.Type.DOOR), new Tile(Tile.Type.ROOF), new Tile(Tile.Type.WALL), 
 				    			new Tile(Tile.Type.WINDOW, Tile.Direction.LEFT), new Tile(Tile.Type.WINDOW, Tile.Direction.RIGHT),
 				    			new Tile(Tile.Type.WINDOW), new Tile(Tile.Type.LAND, Tile.Direction.UP) }
 			    		, null);
 			    
 			    // choice is the tile chosen from list
 			    if (choice != null)
 			    {	campus.changeTile(choice, new Point(xIndex+screenX,yIndex+screenY));
 			    	if (choice.getType() == Tile.Type.LAND)
 			    		campus.addLand(new Point(xIndex+screenX, yIndex+screenY));
 			    	tiles[xIndex][yIndex] = tileFactory.get(choice);
 			    	//renderScreen();
 			    }
 			    System.out.println("New choice: " + choice);		
 			    
 			    if (xIndex+yIndex+screenX+screenY == 0)	// right clicked on upper left most tile
 			    {
 			    	int result = JOptionPane.showConfirmDialog(null, "Export code to text?", "Export", JOptionPane.OK_CANCEL_OPTION);
 			    	if (result == JOptionPane.OK_OPTION)
 			    	{
 			    		// Export code to campus.txt
 			    		try 
 			    		{	BufferedWriter writer = new BufferedWriter(new FileWriter(new File("campus.txt")));
 			    			String[] words = campus.getConstructor().split("\n");
 			    			for (String line : words) 
 			    			{
 			    				writer.write(line);
 			    				writer.newLine();
 			    			}
 			    			writer.close();
 			    		} catch (Exception ex) {}
 			    	}
 			    }
 			}	
 		}
 	}
 	
 	public class Sound // Holds one audio file
 	{
 		private AudioClip song; // Sound player
 		private URL songPath; // Sound path
 
 		Sound(String filename)
 		{
 			try
 			{
 				// System.out.println("file:" + System.getProperty("user.dir") + "\\" + filename);
 				songPath = new URL ("file:" + System.getProperty("user.dir") + "\\" + filename);
 				song = Applet.newAudioClip(songPath);
 				// playSound();
 			}catch(Exception e){
 				e.printStackTrace();
 				//e.getMessage();
 			} // Satisfy the catch
 		}
 		
 		public void playSound()
 		{
 			song.loop();
 		}
 		public void stopSound()
 		{
 		  song.stop();
 		}
 		public void playSoundOnce()
 		{
 			song.play(); // Play only once
 		}
 	}
 }
