 package roguelike_game;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import roguelike_game.entity.Player;
 import roguelike_game.events.Movement;
 import roguelike_game.graphics.Sprite;
 
 public class Roguelike_game extends JFrame {
     public TileMap tilemap;
     public Player player;
     public Movement move;
     public boolean running = false;
     public String version = "Rogue Game - Pre-Alpha build v0.0.1";
     private Painting painting;
     public int FPS = 60;
     
     public Roguelike_game() {
         tilemap = new TileMap(this, 34, 18);
         tilemap.randomMap();
         move = new Movement();
         
         player = new Player(10, 10, tilemap.size, Sprite.PLAYER_UP, tilemap);
         
         painting = new Painting(this);
         this.addKeyListener(move);
         
         add(painting);
         
         running = true;
         
         Thread thread = new Thread(painting);
         thread.start();
     }    
 
     private class Painting extends JPanel implements Runnable {
         Roguelike_game game;
 
         public Painting(Roguelike_game game) {
             this.game = game;
             this.setPreferredSize(new Dimension(1100, 600));
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             game.tilemap.render(g);
             game.player.render(g);
         }
         
         public boolean collision(int x, int y) {
             if ((player.getX() + x) < 0 || (player.getX() + x) >= game.tilemap.width || (player.getY() + y) < 0 || (player.getY() + y) >= tilemap.height) {
                 return true;
             }
             
             if (game.tilemap.tiles[player.getY() + y][player.getX() + x] == 0) {
                 return true;
             } else {
                 return false;
             }
         }
         
         @Override
         public void run() {
             game.setTitle(version);
             int FPSrate = 1000/FPS;
             
            boolean startCycle = false;
            int cycles = 0;
            
            boolean isCycling = false; // check if it's still running the animation
             while(running) {
                 move.update();
                 
                if (!isCycling) {
                     if (move.UP && !collision(0, -1)) {
                         player.setSprite(Sprite.PLAYER_UP);
                         player.setY(player.getY() - 1);
                     } else if (move.DOWN && !collision(0, 1)) {
                         player.setSprite(Sprite.PLAYER_DOWN);
                         player.setY(player.getY() + 1);
                     } else if (move.LEFT && !collision(-1, 0)) {
                         player.setSprite(Sprite.PLAYER_LEFT);
                         player.setX(player.getX() - 1);
                     } else if (move.RIGHT && !collision(1, 0)) {
                         player.setSprite(Sprite.PLAYER_RIGHT);
                         player.setX(player.getX() + 1);
                     }
                     
                    // set delay
                    isCycling = true;
                    cycles = 0;
                    
                    startCycle = true; // trigger cycling
                }
                
                if (cycles == 60) {
                    startCycle = false; // stop cycling
                    if (isCycling) {
                        isCycling = false; // allow next key press to occur
                    }
                 }
                 
                 repaint();
                 
                 try {
                     Thread.sleep(FPSrate);
                 } catch (InterruptedException e) {
                     System.out.println("Interrupted thread!");
                 }
                if (startCycle) {
                	cycles++;
                }
             }
         }
     }
     
     public static void main(String[] args) {
         Roguelike_game game = new Roguelike_game();
         game.setSize(1117, 620);
         game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         game.setLocationRelativeTo(null);
         game.setVisible(true);
     }
 }
