 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
 import java.awt.image.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import javax.swing.*;
 import java.util.concurrent.*;
 import javax.imageio.ImageIO;
 
 // This class is the master content manager, it has a list of managers.
 // Also, this class may manange a few list of entities on it's own.
 public class Content extends JPanel
 {
     private final Frame frame;
     private CopyOnWriteArrayList<Manager> list;
     private Manager<Platform> pltfrmMng;
     private Manager<Enemy> enemyMng;
     public static Manager<Bullet> bulletMng;
     public static Manager<PowerUp> powerUpsMng;
 	
 	private Player playChar;
     //private final Manager target;
     private boolean run;
     private Room currRoom;
     public static Graphics2D darkness;
     private boolean setOpaque = false;
     private Graphics2D bloodOverlayG;
     private int bloodOverlay = 0;
     private BufferedImage blood;
 	
 	Vector currSpawn;
     
     // Constructor
     public Content(Frame fIn)
     {		
         frame = fIn;
         list = new CopyOnWriteArrayList<>();
         run = true;
 
         setBackground(Color.WHITE);
         currRoom = new Room("start.json", frame);
         //currRoom.getData();
 
         //Create the platforms in the starting room
         pltfrmMng = new Manager(frame, this);
         add(pltfrmMng);
         pltfrmMng.addAll(currRoom.getPlatforms());
 
         //Spawn all of the enemies
         enemyMng = new Manager(frame, this);
         add(enemyMng);
         enemyMng.addAll(currRoom.getEnemies());
 
         //Create all of the powerups
         powerUpsMng = new Manager(frame, this);
         add(powerUpsMng);
         powerUpsMng.addAll(currRoom.getPowerUps());
 
         bulletMng = new Manager(frame, this);
         add(bulletMng);
         
         //Create the map
         Map.defaultMap = new Map(currRoom);
 
         //Create the player and their spawn point
         currSpawn = currRoom.getWestSpawn();
         add(new Manager<Player>(frame, this));
 		playChar = new Player(new Vector (0, 0));
 		playChar.setCenter(currSpawn);
         getLast().add(playChar);
 
         setBloodImage();
     }
     public void add(Manager mIn)
     {
         list.add(mIn);
     }
     public Manager getLast()
     {
         if(list.isEmpty())
         {
             return null;
         }
         return list.get(list.size() - 1);
     }
     public Manager getType(String sIn)
     {
         for(Manager m : list)
         {
             if(m.getType() != null)
             {
                 if(m.getType().equals(sIn))
                 {
                     return m;
                 }
             }
         }
         return null;
     }
     public void remove(Manager mIn)
     {
         mIn.dispose();
         list.remove(mIn);
     }
     public void dispose()
     {
         for(Manager m : list)
         {
             remove(m);
         }
     }
     public void logic()
     {
         //Pause the game
         if(Keyboard.isPressed(KeyEvent.VK_P))
         {
             run = !run;
             Keyboard.release(KeyEvent.VK_P);
             if(!run)
             {
                 setOpaque = !setOpaque;
             }
             else
             {
                 setOpaque = !setOpaque;
             }
         }
         //Update everything if the game is not paused
         if(run)
         {
             for(Manager m : list)
             {
                 m.logic();
             }
         }
 
         //Move to the next room if necessary
 		moveRoom();
 		//Respawn the player and reload the room if the character died
 		if(!((Player)getType("Player").get(0)).isAlive())
 			reloadRoom();
     }
 	
 	public void reloadRoom()
 	{
 		//Load the room. This adds overhead, but it either adds it here
 		// or has to clone a lot of objects
 		currRoom.loadRoom();
 		pltfrmMng.drop();
 		bulletMng.drop();
 		enemyMng.drop();
 		powerUpsMng.drop();
 		pltfrmMng.addAll(currRoom.getPlatforms());
 		enemyMng.addAll(currRoom.getEnemies());
 		powerUpsMng.addAll(currRoom.getPowerUps());
 		//System.out.println("(" + currSpawn.x + ", " + currSpawn.y + ")");
 		getType("Player").get(0).setCenter(currSpawn);
 		//System.out.println("(" + getType("Player").get(0).getCenter().x + ", " + getType("Player").get(0).getCenter().y + ")");
 		((Player)getType("Player").get(0)).revive();
 		
 	}
 	
 	public void moveRoom()
 	{
 		Vector currPos = getType("Player").get(0).getCenter();
 
         boolean isMove = false;
 
         //Go north?
         if(currPos.y < 0)
         {
             //Read the data about the room
             currRoom.setRoomName(currRoom.getNorth());
             Map.defaultMap.Update(0, -1, currRoom);
             currSpawn = currRoom.getSouthSpawn();
            isMove = true;
         }
         //Go south?
         else if(currPos.y > frame.getHeight())
         {
             //Read the data about the room
             currRoom.setRoomName(currRoom.getSouth());
             Map.defaultMap.Update(0, 1, currRoom);
             currSpawn = currRoom.getNorthSpawn();
            isMove = true;
         }
         //Go east?
         else if(currPos.x > frame.getWidth())
         {
             //Read the data about the room
             currRoom.setRoomName(currRoom.getEast());
            Map.defaultMap.Update(1, 0, currRoom);
             currSpawn = currRoom.getWestSpawn();
            isMove = true;
         }
         //Go west?
         else if(currPos.x < 0)
         {
             //Read the data about the room
             currRoom.setRoomName(currRoom.getWest());
            Map.defaultMap.Update(-1, 0, currRoom);
             currSpawn = currRoom.getEastSpawn();
            isMove = true;
         }
 
         //If the player is actually moving to a new room, recreate 
         //the managers
        if(isMove)
        {
            reloadRoom();
        }
 	}
 	
     public void setBloodOverlay()
     {
         setBloodImage();
         bloodOverlay = 25;
     }
     private void setBloodImage()
     {
         Random rand = new Random();
         String path = new String("bin\\images\\splatter\\Splatter" + new String(Integer.toString((rand.nextInt() % 5 + 1))) + ".png");
         try
         {
             blood = ImageIO.read(new File(path));
         }
         catch(IOException e)
         {
             System.out.println("Error Loading Blood Splatter Image!");
             System.out.println("Bad Path: " + path);
         }
     }
     @Override
     public void paint(Graphics gIn)
     {
         super.paint(gIn);
         Graphics2D g = (Graphics2D)gIn;
         //AffineTransform trans = new AffineTransform();
         //trans.scale(0.5, 0.5);
         //g.transform(trans);
 		//g.scale(0.5, 0.5);
 
         BufferedImage temp = new BufferedImage(800, 800, BufferedImage.TYPE_4BYTE_ABGR);
         darkness = temp.createGraphics();
         darkness.setColor(Color.BLACK);
         darkness.fillRect(0, 0, frame.getWidth(), frame.getHeight());
         darkness.setComposite(AlphaComposite.DstOut);
 
         currRoom.paint(g);
         for(Manager m : list)
         {
             m.paint(g);
         }
         g.drawImage(temp, null, 0, 0);
         if(!setOpaque)
         { //If the game is "running" (not paused)
             if(bloodOverlay > 0)
             {
                 bloodOverlayG = blood.createGraphics();
                 bloodOverlayG.fillRect(0, 0, frame.getWidth(), frame.getHeight());
                 g.drawImage(blood, null, 0, 0);
                 bloodOverlay--;
             }
         }
         else
         { //If the game is "Paused"
             //This is used to Gray out the background When Pause is Selected
             g.setColor(new Color(255, 255, 255, 51)); //Lets make a box that is 20% opaque!
             //g.setColor(Color.black); //I was going to do this, but I really like the random color that it makes it haha
             g.fillRect(0, 0, getWidth(), getHeight());//Lets make it the size of the window!
 			g.setColor(Color.WHITE);
             drawCenteredString(g, "GAME PAUSED", getWidth()/2, 15); //Let the Player know the game is Paused
             drawCenteredString(g, "Press 'P' to Resume Game", getWidth()/2, 30); //And how they Can Return to it!
 			
 			int i = 1;
 			Font tempFont = g.getFont();
 			g.setFont(g.getFont().deriveFont(Font.BOLD));
 			g.drawString("Power Ups:", 10, 50);
 			g.setFont(tempFont);
 			for(PowerUp.Type t : Player.powerUps)
 				g.drawString(t.toString(), 30, 25*(i++) + 50);
 			
 			Map.defaultMap.draw(g, getWidth()/4, getHeight()/4, getWidth()/2, getHeight()/2);
         }
     }
     public static void drawCenteredString(Graphics2D g, String s, int x, int y)
     {
         int centerX = (int)g.getFontMetrics().getStringBounds(s, g).getWidth() / 2;
         g.drawString(s, x - centerX, y);
     }
 }
