 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.awt.*;
 
 import java.util.*;
 
 import javax.swing.*;
 
 public class DrawPanel extends JPanel implements KeyListener {
     
     private Player player;
     private Prize prize;
     
     //Prizes collected
     private int prizeCount;
     
     //enemies
     private ArrayList<Enemy> enemies;
     
     //Used for smooth animation
     private BufferedImage buffer;
     
     //Random class
     private final Random random = new Random();
     
     //CONSTRUCTOR
     public DrawPanel() {
         setIgnoreRepaint(true);
         addKeyListener(this);
         setFocusable(true);
     }
     
     //Method called from Gui containing main loop
     public void start() {
         buffer = new BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);
         //Very Important. Almost forgot
         init();
         
         long timeStarted = System.currentTimeMillis();
         long acumulatedTime = timeStarted;
         
         while(true) {
             try {
                 long timePassed = System.currentTimeMillis() - acumulatedTime;
                 acumulatedTime += timePassed;
                 update(timePassed);
                 checkCollisions();
                 if(prize.isCollected()) {
                     prizeCount++;
                    prize = new Prize(random.nextInt(this.getWidth()) - 14, random.nextInt(this.getHeight()) - 14, 15, 15);
                    enemies.add(new Enemy(random.nextInt(this.getWidth()) - 49, random.nextInt(this.getHeight()) - 49, 50, 50, randomSpeedDirection()));
                 }
                 drawBuffer();
                 drawScreen();
                 Thread.sleep(15);
             } catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     private void init() {
         prizeCount = 0;
         loadEntities();   
     }
     
     private void loadEntities() {
         player = new Player(0, 0, 50, 50);
         prize = new Prize(random.nextInt(this.getWidth() - 14), random.nextInt(this.getHeight() - 14), 15, 15);
         enemies = new ArrayList<Enemy>();
         
         //gotta fix this hardcoding in the position
         enemies.add(new Enemy(random.nextInt(this.getWidth() - 49),random.nextInt(this.getHeight() - 49) , 50, 50, randomSpeedDirection())); 
     }
     
     private void update(long timePassed) {
         player.update(timePassed);
         for(Enemy enemy : enemies) {
             enemy.update(timePassed);
         }
         
         
         //Is anyone out of screen?
         outOfBoundries();
     }
     
     private void outOfBoundries() {
         
         //player treatment
         if(player.getX() < 0) {
             player.setX(0);
         }
         if(player.getX() + player.getWidth() > this.getWidth()) {
             player.setX(this.getWidth() - player.getWidth());
         }
         if(player.getY() < 0) {
             player.setY(0);
         }
         if(player.getY() + player.getHeight() > this.getHeight()) {
             player.setY(this.getHeight() - player.getHeight());
         }
         
         //enemies treatment
         for(Enemy enemy : enemies) {
             if(enemy.getX() < 0) {
                 enemy.setVelocityX(-enemy.getVelocityX());
             }
             if(enemy.getX() + enemy.getWidth() > this.getWidth()) {
                 enemy.setVelocityX(-enemy.getVelocityX());
             }
             if(enemy.getY() < 0) {
                 enemy.setVelocityY(-enemy.getVelocityY());
             }
             if(enemy.getY() + enemy.getHeight() > this.getHeight()) {
                 enemy.setVelocityY(-enemy.getVelocityY());
             }
         }
     }
     
     private void checkCollisions() {
         
        //Have you touched any of the enemies?
        for(Entity enemy : enemies) {
             if(player.getRectangle().intersects(enemy.getRectangle())) {
                 player.setCollided(true);
            }
         }
        
        //Have you collected the prize?
        if(player.getRectangle().intersects(prize.getRectangle())) {
            prize.setCollected(true);
        }
     }
     
     private Enemy.SpeedDirection randomSpeedDirection() {
         int pick = random.nextInt(Enemy.SpeedDirection.values().length);
         return Enemy.SpeedDirection.values()[pick];
     }
     
     private void drawBuffer() {
         Graphics2D b = buffer.createGraphics();
         b.setColor(Color.black);
         b.fillRect(0, 0, 800, 600);
 
         if (player.isCollided() == false) {
             b.setColor(Color.red);
             b.fillRect(Math.round(player.getX()), Math.round(player.getY()), player.getWidth(), player.getHeight());
             b.setColor(Color.blue);
             for(Entity enemy : enemies) {
                 b.fillRect(Math.round(enemy.getX()), Math.round(enemy.getY()), enemy.getWidth(), enemy.getHeight());
             }
             b.setColor(Color.green);
             b.fillRect(Math.round(prize.getX()), Math.round(prize.getY()), prize.getWidth(), prize.getHeight());
             b.setColor(Color.white);
             b.setFont(new Font("Arial", Font.PLAIN, 32));
             b.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             b.drawString(String.valueOf(prizeCount), 750, 50);
             b.dispose();
         } else {
             b.setColor(Color.white);
             b.setFont(new Font("Arial", Font.PLAIN, 24));
             b.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             b.drawString("C O L L I S I O N !", 300, 300);
             b.setFont(new Font("Arial", Font.PLAIN, 18));
             b.drawString("press enter to try again", 300, 350);
             b.dispose();
         }   
     }
     
     private void drawScreen() {
         Graphics2D g = (Graphics2D)this.getGraphics();
         g.drawImage(buffer, 0 , 0, this);
         Toolkit.getDefaultToolkit().sync();
         g.dispose();
     }
     
     public void keyPressed(KeyEvent e) {
         int keyCode = e.getKeyCode();
         
         //Wanna exit?
         if(keyCode == KeyEvent.VK_ESCAPE) {
             System.exit(0);
         }
         
         //Wanna restart?
         if(keyCode == KeyEvent.VK_ENTER) {
             init();
         }
         
         if(keyCode == KeyEvent.VK_UP) {
             player.setVelocityY(-0.3f);
         }
         if(keyCode == KeyEvent.VK_DOWN) {
             player.setVelocityY(0.3f);
         }
         if(keyCode == KeyEvent.VK_RIGHT) {
             player.setVelocityX(0.3f);
         }
         if(keyCode == KeyEvent.VK_LEFT) {
             player.setVelocityX(-0.3f);
         }
     }
     
     public void keyReleased(KeyEvent e) {
         int keyCode = e.getKeyCode();
         
         if(keyCode == KeyEvent.VK_UP) {
             player.setVelocityY(0);
         }
         if(keyCode == KeyEvent.VK_DOWN) {
             player.setVelocityY(0);
         }
         if(keyCode == KeyEvent.VK_RIGHT) {
             player.setVelocityX(0);
         }
         if(keyCode == KeyEvent.VK_LEFT) {
             player.setVelocityX(0);
         }
     }
     
     //Nothing here... it doesn't matter
     public void keyTyped(KeyEvent e) {
         e.consume();
     }
 }
