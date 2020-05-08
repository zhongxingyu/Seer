 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package atomgameproject.game;
 
 import atomgameproject.game.bullet.AntiNeutronBullet;
 import atomgameproject.game.bullet.GameBullet;
 import atomgameproject.game.bullet.NeutronBullet;
 import atomgameproject.game.bullet.ShootManager;
 import atomgameproject.launcher.config.KeyBindWrapper;
 import atomgameproject.world.Camera;
 import atomgameproject.world.GameWorld;
 import atomgameproject.world.components.SimplePhysicsComponent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import simplegames.animation.GameDrawingPanel;
 import simplegames.behaviors.Updatable;
 import simplegames.inputlisteners.KeyDownListener;
 import simplegames.inputlisteners.KeyUpListener;
 import simplegames.inputlisteners.MouseDownListener;
 import simplegames.inputlisteners.MouseUpListener;
 
 /**
  *
  * @author Jonathon
  */
 public class PlayerAtom extends GameAtom implements KeyDownListener, KeyUpListener, Updatable, MouseDownListener, MouseUpListener {
     
     private boolean[] compass;
     private KeyBindWrapper keyBindings;
     private SimplePhysicsComponent spc;
     private GameWorld world;
     private GameDrawingPanel panel;
     private int bulletSpeed;
     private ShootManager sm;
     
     public PlayerAtom(int x, int y, int width, int height, GameDrawingPanel panel, KeyBindWrapper keyBindings) {
         super(x, y, width, height);
         world = GameWorld.getWorld();
         compass = new boolean[4];
         sm = new ShootManager(this, 20);
         bulletSpeed = 11;
         this.panel = panel;
         this.keyBindings = keyBindings;
         getAtomComponent().setParent(this);
         getAtomComponent().setProtons(54);
         getAtomComponent().setElectrons(54);
         getAtomComponent().setNeutrons(76);
     }
 
     @Override
     public void reactToKeyDown(KeyEvent e) {
         int key = e.getKeyCode();
         if (key==keyBindings.getMovementBinding()[0]) {
             startUp();
         }
         if (key==keyBindings.getMovementBinding()[1]) {
             startRight();
         }
         if (key==keyBindings.getMovementBinding()[2]) {
             startDown();
         }
         if (key==keyBindings.getMovementBinding()[3]) {
             startLeft();
         }
         if (key==KeyEvent.VK_K) {
            addNeutron();
         }
         if (key==KeyEvent.VK_L) {
             removeNeutron();
         }
     }
 
     @Override
     public void reactToKeyUp(KeyEvent e) {
         int key = e.getKeyCode();
         if (key==keyBindings.getMovementBinding()[0]) {
             stopUp();
         }
         if (key==keyBindings.getMovementBinding()[1]) {
             stopRight();
         }
         if (key==keyBindings.getMovementBinding()[2]) {
             stopDown();
         }
         if (key==keyBindings.getMovementBinding()[3]) {
             stopLeft();
         }
     }
     
     public void startUp() {
         compass[0] = true;
     }
     
     public void startDown() {
         compass[2] = true;
     }
     
     public void startLeft() {
         compass[3] = true;
     }
     
     public void stopRight() {
         compass[1] = false;
     }
     
     public void stopUp() {
         compass[0] = false;
     }
     
     public void stopDown() {
         compass[2] = false;
     }
     
     public void stopLeft() {
         compass[3] = false;
     }
     
     public void startRight() {
         compass[1] = true;
     }
     
     @Override
     public void update(float timeDiff) {
         if (spc==null) {
             spc = ((SimplePhysicsComponent)this.getComponent(SimplePhysicsComponent.class));
         }
         /////Handle movement
         spc.setxVel(0);
         spc.setyVel(0);
         spc.setBothSpeed(2.5f);
         if (compass[0] && !compass[2]) {
             spc.setyVel(-spc.getySpeed());
           //   System.out.println("Move up! "+spc.getyVel());
         }
         if (compass[1] && !compass[3]) {
             spc.setxVel(spc.getxSpeed());
          //  System.out.println("Move right! "+spc.getxVel());
         }
         if (compass[2] && !compass[0]) {
             spc.setyVel(spc.getySpeed());
            // System.out.println("Move down! "+spc.getyVel());
         }
         if (compass[3] && !compass[1]) {
             spc.setxVel(-spc.getxSpeed());
           //  System.out.println("Move left! "+spc.getxVel());
         }
         sm.update(timeDiff);
         sm.tryToFire();
         ///Handle shooting
         int mouseInWorldX = 0, mouseInWorldY = 0, worldCamX = 0, worldCamY = 0, bulletLocX = 0, bulletLocY = 0;
         try {    
             mouseInWorldX = (int)panel.getMousePosition().getX();
             mouseInWorldY = (int)panel.getMousePosition().getY();
         worldCamX = (int)GameWorld.getWorld().getCamera().getX();
         worldCamY = (int)GameWorld.getWorld().getCamera().getY();
         bulletLocX = mouseInWorldX+worldCamX;
         bulletLocY = mouseInWorldY+worldCamY;
         sm.setX(bulletLocX);
         sm.setY(bulletLocY);
        } catch(Exception e) { 
            System.out.println("Mouse not there"); 
            sm.stopShooting();
        }
     //    System.out.println("Shooting1 is " + shooting[0] + " and Shooting2 is " + shooting[1] + "");
         Camera cam = GameWorld.getWorld().getCamera();
         cam.setLocation((int)(getX()+(getWidth()/2)-(cam.getWidth())/2),(int)(getY()+(getHeight()/2)-(cam.getHeight())/2));
         for (int i=0; i<getComponents().size(); i++) {
             getComponents().get(i).updateComponent(timeDiff);
         }
     }
 
     @Override
     public void reactToMouseDown(int x, int y, MouseEvent e) {
         if (e.getButton()==keyBindings.getShootBinding()[0]) {
             sm.setCanShootRed(true);
         }
         if (e.getButton()==keyBindings.getShootBinding()[1]) {
             sm.setCanShootBlue(true);
         }
     }
 
     @Override
     public void reactToMouseUp(int x, int y, MouseEvent e) {
         sm.stopShooting();
     }
 
     public int getBulletSpeed() {
         return bulletSpeed;
     }
 
     public void setBulletSpeed(int bulletSpeed) {
         this.bulletSpeed = bulletSpeed;
     }
 }
