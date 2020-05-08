 package medievalhero;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.*;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import javax.swing.JPanel;
 import java.io.IOException;
 
 
 // LIKE *DIABLO* AND *PATH OF EXCILE*, LOADING LEVEL WILL PUT PLAYER IN LAST SAFE SPOT
 // (TOWN OR CAMP) BECAUSE MONSTERS WILL BE LOADED AGAIN WHEN THE MAP LOADS
 
 
 public class Board extends JPanel implements Runnable, KeyListener, MouseListener {
     
     protected boolean inGame = false, subMenu = false, inGameMenu = false, playerWindows = false;
     protected int subMenuCount; //10=Options, 11=VideoOptions, 12=ControleOptions, 13=AudioOptions, 14=GameplayOptions, 20=CharacterCreation1(race/gender/faction), 21=CharacterCreation2(skillpoints/class), 22=CharacterCreation3(history), 30=MapEditor, 50=credits
     protected int inGameMenuCount; 
     protected int playerWindowsType; // 0=Inventory, 1=CharacterSheet, 2=SkillWindow
     
     private Thread thread;
     private final int DELAY = 20;
     
     protected int screenWidth = 800, screenHeight = 640;
     protected int cameraX, cameraY;
     
     protected ControlerObject myControle;
     protected BitmapFactory bitmapFact;
     protected Mapper mapTool;
     protected InputControle input;
     
     //How to do ZONES? In memory when adjecent to zone where you walk in, ...
     // Look in mail: "Gedacht voor game"
     protected Tile[][] tileArray = new Tile[1025][1025]; //one extra for mapping tool cursors?
     public List<Area> areas = new ArrayList<Area>();
     public List<Item> items = new ArrayList<Item>();
     
     protected Hero player;
     
     public Board() throws IOException {
         
         setBackground(Color.BLACK);
         addKeyListener(this);
         addMouseListener(this);
         setFocusable(true);
         setDoubleBuffered(true);
         setSize(screenWidth, screenHeight);
         
         myControle = new ControlerObject(this);
         
         bitmapFact = new BitmapFactory(this);
         bitmapFact.loadBasicImages();
         
         mapTool = new Mapper(this);
         mapTool.readyTiles(1);
 //        mapTool.loadTestMap();
         mapTool.loadMap("testlevel");
         
         input = new InputControle(this);
         
         player = new Hero(this);
         player.setStartLocation(64, 64);
         
     }
     
     public void addNotify() {
         super.addNotify();
         thread = new Thread(this);
         thread.start();
     }
     
     public void run() {
         
         long beforeTime, timeDiff, sleep;
         
         beforeTime = System.nanoTime();
         
         while(true) {
             timeDiff = System.nanoTime() - beforeTime;
             beforeTime = System.nanoTime();
             sleep = DELAY - timeDiff;
             
             if(sleep < 0) {
                 sleep = DELAY;
             }
             
             try {
                 thread.sleep(sleep);
                 input.tick();
                 myControle.doKeyStrokes(input);
                 
                 if(inGame) { //do i need this tree? - YES!!
                     if(inGameMenu) {
                         //Pause Game, do ingame menu
                     } else if(playerWindows) {
                         //do Player window stuff
                     } else if(!inGameMenu && !playerWindows) {
                         // do ingame stuff
                     }
                     
                     //Rectangle heroRect = player.getBounds();
                     Rectangle target;
                     for (int z = 0; z < areas.size(); z++) {
                         //Check collision and/or inside (contains) with player!
                         target = areas.get(z).getBounds();
                     }
                 }
             } catch (InterruptedException e) {
                 System.out.println("interrupted in RUN - " + e);
             }
             
             repaint();
             
         }
         
     }
     
     public void paint(Graphics g) {
         
         super.paint(g);
         Graphics2D g2d = (Graphics2D)g;
         
         if(inGame) {
             //inGame
             //1st Layer: TILES
             for(int i = 0; i < 1024; i++) {
                 for (int j = 0; j < 1024; j++) {
                     if (tileArray[i][j].getX() > (cameraX - 64) && tileArray[i][j].getX() < (cameraX + 864)) {
                         if (tileArray[i][j].getY() > (cameraY - 64) && tileArray[i][j].getY() < (cameraY + 704)) {
                             tileArray[i][j].draw(g2d, cameraX, cameraY);
                         }
                     }
                 }
             }
             
             //2nd Layer: ITEMS
             
             //3rd Layer: PLAYER
             player.draw(g2d);
             
             //4th Layer: OBJECTS ON TOP (where you can get behind)
             
             //5th Layer: GUI
             
             //6th Layer: PLAYER WINDOWS
             
             //7th Layer: GAME PAUSED SHROUD
             
             //8th Layer: INGAME MENU
             
             if(inGameMenu) {
                 //draw menu
             }
             if(playerWindows) {
                 //draw player windows
             }
         } else {
             // inMenu
             if(subMenu) {
                 switch(subMenuCount) {
                     case 30:
                         for(int i = 0; i < 1025; i++) {
                             for (int j = 0; j < 1025; j++) {
                                 if (tileArray[i][j].getX() > (cameraX - 64) && tileArray[i][j].getX() < (cameraX + 864)) {
                                     if (tileArray[i][j].getY() > (cameraY - 64) && tileArray[i][j].getY() < (cameraY + 704)) {
                                     tileArray[i][j].draw(g2d, cameraX, cameraY);
                                     }
                                 }
                             }
                         }
                         break;
                 }
             } else {
                 // do menu drawing
                 g.drawImage(bitmapFact.getMenuImg("background"), 0, 0, null);
                 g.drawImage(bitmapFact.getMenuImg("startbutton"), 345, 432, null);
             }
         }
     }
     
     public void keyPressed(KeyEvent e) {
         input.set(e.getKeyCode(), true);
     }
     
     public void keyReleased(KeyEvent e) {
         
     }
     
     public void keyTyped(KeyEvent e) {
         
     }
     
     public void mousePressed(MouseEvent e) {
         
     }
     
     public void mouseReleased(MouseEvent e) {
         
     }
     
     public void mouseEntered(MouseEvent e) {
         
     }
     
     public void mouseExited(MouseEvent e) {
         
     }
     
     public void mouseClicked(MouseEvent e) {
         
     }
     
 }
