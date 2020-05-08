 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import java.lang.Math;
 
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class Board extends JPanel implements ActionListener {
 
     private Timer timer;
     private Player player;
     private ArrayList buildings;
     private boolean ingame;
     private String endgameMessage = "";
     private int B_WIDTH;
     private int B_HEIGHT;
     private static Board instance;
     private double speed;
 
     public Board() {
         addKeyListener(new TAdapter());
         setFocusable(true);
         setBackground(Color.BLACK);
         setDoubleBuffered(true);
         ingame = true;
 
         setSize(Collision.width, Collision.height);
 
         player = new Player();
 
         initBuildings();
 
         timer = new Timer(5, this);
         timer.start();
         this.speed = 1;
         Board.instance = this;
     }
 
     public static Board getInstance() {
         return Board.instance;
     }
 
     public void addNotify() {
         super.addNotify();
         B_WIDTH = getWidth();
         B_HEIGHT = getHeight();   
     }
 
     public double getSpeed() {
         return speed;
     }
 
     public void initBuildings() {
         this.buildings = new ArrayList();
         
         this.buildings.add(new Building(500, 500, 0, 300));
         this.buildings.add(new Building(500, 500, 600, 400));
     }
 
     public void addBuilding() {
        Building b = new Building(getRandom(200,500), 500, getWidth()+getRandom(50,200), getRandom(200,350));
         b.spawnObstacles();
         this.buildings.add(b);
     }
 
 
     public void paint(Graphics g) {
         super.paint(g);
 
         if (ingame) {
 
             Graphics2D g2d = (Graphics2D)g;
             
             g2d.drawImage(player.getImage(), (int)player.getX(), (int)player.getY(), this);
 
 
             g2d.setColor(Color.WHITE);
 
             for(int i = 0; i<buildings.size();++i) {
                 Building b = (Building) buildings.get(i);
                 b.paintComplete(g2d);
             }
 
             g2d.drawString("Position: " + player.getX() + " - " + player.getY() + " - DY: " + player.getDY() + " - DX: " + player.getDX() + " - Speed: " + speed, 5, 15);
 
         } else {
             String msg = "Game Over - " + endgameMessage;
             Font small = new Font("Helvetica", Font.BOLD, 14);
             FontMetrics metr = this.getFontMetrics(small);
 
             g.setColor(Color.white);
             g.setFont(small);
             g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2,
                          B_HEIGHT / 2);
         }
 
         Toolkit.getDefaultToolkit().sync();
         g.dispose();
     }
 
 
     public void actionPerformed(ActionEvent e) {
         player.move();
         for (int i = 0; i < buildings.size(); ++i) {
             Building b = (Building) buildings.get(i);
             b.move();
         }
 
         Building b = (Building) buildings.get(buildings.size()-1);
         // Add new building if last one completely visible
         if(b.getX() + b.getWidth() < getWidth()) {
             addBuilding();
         }
 
         // Increase speed
         if(speed < 4)
             speed += 0.0004;
 
         checkCollisions();
         repaint();  
     }
 
     public void checkCollisions() {
 
         Rectangle r3 = player.getBounds();
 
         for (int i = 0; i < buildings.size(); ++i) {
             Building b = (Building) buildings.get(i);
             if(player.intersects(b.getItem())) {
                if(player.getX() < b.getX() && (player.getY() + player.getHeight() - 10) > b.getY())
                 {
                     endgameMessage = "You hit a building.";
                     stopGame();
                 }
                 else
                 {
                     player.setDY(0.0);
                     player.setY(b.getY() - player.getHeight() + 1);
                 }
             }
             // Check obstacles
             ArrayList obstacles = b.getObstacles();
             for(int j = 0; j < obstacles.size(); ++j) {
                 Obstacle o = (Obstacle) obstacles.get(j);
                 if(o.isActive()) {
                     if(player.intersects(o.getItem())) {
                         o.deactivate();
                         speed -= 0.5;
                     }
                 }
             }
         }
     }
 
     public void stopGame() {
         ingame = false;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public static int getRandom(int from, int to) {
         int diff = to - from;
         double random = Math.random();
         return from + (int)(diff * random);
     }
 
 
     private class TAdapter extends KeyAdapter {
 
         public void keyReleased(KeyEvent e) {
             player.keyReleased(e);
         }
 
         public void keyPressed(KeyEvent e) {
             player.keyPressed(e);
         }
     }
 }
