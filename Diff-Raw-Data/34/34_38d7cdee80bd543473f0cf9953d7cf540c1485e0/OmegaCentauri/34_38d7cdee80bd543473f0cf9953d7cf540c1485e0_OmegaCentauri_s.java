 package MainPackage;
 
 import java.awt.Graphics;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.*;
 import javax.swing.*;
 
 // @author Michael Kieburtz
 public class OmegaCentauri extends JFrame implements KeyListener {
 
     private final Game game;
     private boolean forward, rotateRight, rotateLeft = false;
     private final java.util.Timer timer = new java.util.Timer();
     private final int timerDelay;
     private final Renderer renderer;
     private final Panel panel = new Panel(1000, 600);
     private final Point2D.Double middleOfPlayer = new Point2D.Double();
     private boolean Slowingdown = false;
     private double FPS = 0;
     private java.util.List<Long> updateTimes = new ArrayList<Long>();
 
     public OmegaCentauri(int width, int height, int desiredFrameRate) {
         
         timerDelay = 15;
         setUpWindow(width, height);
         this.game = new Game(width, height);
         renderer = new Renderer();
         setIconImage(game.getPlayer().getImage());
 
         timer.schedule(new MovementTimer(game.getPlayer()), timerDelay);
         middleOfPlayer.x = game.getPlayer().getLocation().x + game.getPlayer().getImage().getWidth() / 2;
         middleOfPlayer.y = game.getPlayer().getLocation().y + game.getPlayer().getImage().getWidth() / 2;
     }
 
     private void setUpWindow(int width, int height) {
         setSize(width, height);
         setResizable(false);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         addKeyListener(this);
         setTitle("Omega Centauri");
        //add(fpsLabel); 
         add(panel);
         setContentPane(panel);
     }
 
     private class MovementTimer extends TimerTask {
 
         Player player;
 
         public MovementTimer(Player player) {
             this.player = player;
         }
         
         @Override
         public void run() {
             FPS = getFrameRate();
             System.out.println(FPS);
             
             if (forward) {
 
                 game.movePlayer(false);
                 middleOfPlayer.x = game.getPlayer().getLocation().x + game.getPlayer().getImage().getWidth() / 2;
                 middleOfPlayer.y = game.getPlayer().getLocation().y + game.getPlayer().getImage().getHeight() / 2;
                 repaint();
             }
             if (rotateRight) {
 
                 game.rotatePlayer(true); // positive
                 repaint();
             }
 
             if (rotateLeft) {
                 game.rotatePlayer(false); // negitive
                 repaint();
             }
 
             if (!forward) {
                 Slowingdown = true;
             }
 
             if (Slowingdown) {
                 game.movePlayer(true);
                 middleOfPlayer.x = game.getPlayer().getLocation().x + game.getPlayer().getImage().getWidth() / 2;
                 middleOfPlayer.y = game.getPlayer().getLocation().y + game.getPlayer().getImage().getHeight() / 2;
                 repaint();
             }
             
             timer.schedule(new MovementTimer(player), timerDelay);
         }
     }
     int keyCode;

     @Override
     public void keyPressed(KeyEvent e) {
         keyCode = e.getKeyCode();

         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = true;
                 Slowingdown = false;
                 game.changePlayerImage(1);
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = true;
                game.changePlayerImage(3);
                 
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = true;
                game.changePlayerImage(2);
             }
             break;
 
             case KeyEvent.VK_SPACE: {
                 game.getPlayer().location.x = 500;
                 game.getPlayer().location.y = 250;
                 game.setVel(0, 0);
             }
 
         } // end switch
 
     } // end method
 
     @Override
     public void keyReleased(KeyEvent e) {
         keyCode = e.getKeyCode();
 
         switch (keyCode) {
             case KeyEvent.VK_W: {
                 forward = false;
                 game.changePlayerImage(0);
                 if (rotateRight)
                     game.changePlayerImage(3);
                 else if (rotateLeft)
                     game.changePlayerImage(2);
             }
             break;
 
             case KeyEvent.VK_D: {
                 rotateRight = false;
                game.changePlayerImage(0);
             }
             break;
 
             case KeyEvent.VK_A: {
                 rotateLeft = false;
                game.changePlayerImage(0);
             }
             
             break;
 
         } // end switch
     }
 
     public class Panel extends JPanel {
 
         int width;
         int height;
 
         public Panel(int width, int height) {
             this.width = width;
             this.height = height;
             setSize(width, height);
             setVisible(true);
             setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
 
             renderer.drawScreen(g, game.getPlayer(), middleOfPlayer.x, middleOfPlayer.y, Math.ceil(FPS));
         }
     }
     // WARNING: USELESS METHOD.
 
     public void keyTyped(KeyEvent ke) {
     }
 
     private float getFrameRate() {
         long time = System.currentTimeMillis();
 
         updateTimes.add(new Long(time));
 
         float timeInSec = (time - updateTimes.get(0)) / 1000f;
 
         float FPS = 30f / timeInSec;
 
         if (updateTimes.size() == 31) {
             updateTimes.remove(0);
         }
 
         return FPS;
     }
 
 }
