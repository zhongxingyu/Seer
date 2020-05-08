 package edu.mines.csci598.recycler.frontend.graphics;
 
 import edu.mines.csci598.recycler.frontend.Hand;
 import edu.mines.csci598.recycler.frontend.utils.GameConstants;
 import edu.mines.csci598.recycler.frontend.utils.Log;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * The GameScreen class is responsible for drawing the sprites with their updated time.
  *
  *
  * Created with IntelliJ IDEA.
  * User: jzeimen
  * Date: 10/20/12
  * Time: 9:13 PM
  */
 public class GameScreen extends JPanel implements GraphicsConstants{
 	private static GameScreen INSTANCE;
     private LinkedList<Displayable> drawableLinkedList;
     private Sprite s;
     private Sprite background;
     private LinkedList<Sprite> sprites = new LinkedList<Sprite>();
     private LinkedList<Sprite> spritesToRemove = new LinkedList<Sprite>();
     private Iterator it = sprites.iterator();
     private Hand hand;
 
     private GameScreen() {
         setFocusable(true);
         setBackground(Color.RED);
         setDoubleBuffered(true);
         background = new Sprite("src/main/resources/SpriteImages/background.jpg", 0, 0, 1.0);
         s= new Sprite("src/main/resources/SpriteImages/glass.png", 0, screenHeight -200, 0.1);
         
         // TODO I think this should be a temporary hack
         hand = new Hand();
         addMouseMotionListener(hand);
     }
     
     public static final GameScreen getInstance()
     {
     	if(INSTANCE == null){
     		INSTANCE = new GameScreen();
     	}
     	return INSTANCE;
     }
 
 
     public synchronized void paint(Graphics g) {
         super.paint(g);
         Toolkit.getDefaultToolkit().sync();
         Graphics2D g2d = (Graphics2D)g;
         g2d.drawImage(background.getImage(), background.getX(), background.getY(), this);
         g2d.drawImage(s.getImage(), s.getX(), s.getY(), this);
         for(Sprite sprite : sprites){
         	try{
         		g2d.drawImage(sprite.getImage(), sprite.getX(), sprite.getY(), this);
         	}
         	catch(ConcurrentModificationException e){
         		Log.logError("Trying to draw sprite: " + s);
         	}
         }
         g.dispose();
     }
 
     /**
      * Function only used to test some graphics features during development. Useless for the game will delete in future
      * when we know we don't need it any more.
      */
     public synchronized void start(){
         while(s.getX() < 700){
             s.setHorizontalVelocity(1);
             while(s.getX() <= 500){
                 s.move();
                 repaint();
                 try {
                     Thread.sleep(3);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
             s.setHorizontalVelocity(0);
             s.setVerticalVelocity(-1);
             while(s.getY() != 200){
                 s.move();
                 repaint();
                 try {
                     Thread.sleep(3);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
             s.setVerticalVelocity(0);
             s.setHorizontalVelocity(1);
             s.move();
             repaint();
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             System.out.println(s.getX());
         }
 
     }
 
     public synchronized void addSprite(Sprite s){
         try{
          sprites.addLast(s);
         }catch (ConcurrentModificationException e){Log.logError("Trying to add sprite " + s);}
     }
 
     public synchronized boolean removeSprite(Sprite s){
         return sprites.remove(s);
     }
 
     public synchronized void handleSprites(int flag, Sprite s, double time){
         if(flag == GameConstants.ADD_SPRITE) addSprite(s);
         else if (flag == GameConstants.REMOVE_SPRITE) removeSprite(s);
         else if (flag == GameConstants.UPDATE_SPRITES) update(time);
     }
 
     /**
      * Updates the game screen. You must pass in the time so it knows how far everything needs to move.
      * @param time
      */
     public synchronized void update(double time ){
         try {
             for(Sprite sprite: sprites){
                 try{
                     sprite.updateLocation(time);
                     //if(sprite.getX()>=700) sprites.remove(sprite);
                 }catch (ConcurrentModificationException e){
                     Log.logError("Trying to update sprite " + sprite + " with time " + time);
                 }
             }
 
         }catch(ExceptionInInitializerError e){
             Log.logError("Trying to update sprites with time " + time);
         }
         repaint();
 
     }
 
 }
