 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Player;
 
 import Main.GamePanel;
 import Map.TileMap;
 //import com.sun.xml.internal.bind.v2.model.core.Adapter;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Rune
  */
 public class Player extends NPC {
 
     private int x, y;
     private int dx, dy;
     private int speed;
     private TileMap tileMap;
     private boolean up, down, left, right, facingLeft, interactionPressed,interOk;
     private boolean topLeft, topRight, bottomLeft, bottomRight;
     private final int spriteWidth = 32;
     private final int spriteHeight = 34;
     private int idleDirection = 3;
     private BufferedImage[] walking_sideways;
     private BufferedImage[] walking_up;
     private BufferedImage[] walking_down;
     private BufferedImage[] idleSprite = new BufferedImage[1];
     private BufferedImage[] idleSprite_up = new BufferedImage[1];
     private BufferedImage[] idleSprite_down = new BufferedImage[1];
     private Animation animation;
     private int[][] npc = {{30, 30}, {0, 0}, {100, 100}};
     private String color;
     private boolean OptionTrigger;
     private int lock = 1;
     private int lock2 = 1;
     private ArrayList<NPC> npcs = new ArrayList<NPC>();
 
     public Player(TileMap tileMap, int npcID, int x, int y, int speed, String color) {
         super(npcID, x, y, color);
         this.tileMap = tileMap;
         this.x = x;
         this.y = y;
         this.speed = speed;
 
         try {
 
             idleSprite = new BufferedImage[1];
             walking_sideways = new BufferedImage[2];
             walking_up = new BufferedImage[2];
             walking_down = new BufferedImage[2];
             idleSprite[0] = ImageIO.read(new File("res/player/playeridle.png"));
             idleSprite_up[0] = ImageIO.read(new File("res/player/playeridleup.png"));
             idleSprite_down[0] = ImageIO.read(new File("res/player/playeridledown.png"));
             BufferedImage img = ImageIO.read(new File("res/player/playersidewalk.png"));
             BufferedImage img2 = ImageIO.read(new File("res/player/playerup.png"));
             BufferedImage img3 = ImageIO.read(new File("res/player/playerdown.png"));
             for (int i = 0; i < walking_up.length; i++) {
                 walking_up[i] = img2.getSubimage(i * spriteWidth, 0, spriteWidth, spriteHeight);
                 walking_sideways[i] = img.getSubimage(i * spriteWidth, 0, spriteWidth, spriteHeight);
 
                 walking_down[i] = img3.getSubimage(i * spriteWidth, 0, spriteWidth, spriteHeight);
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         animation = new Animation();
         facingLeft = false;
     }
 
     private void calculateCorners(int x, int y) {
         int leftTile = tileMap.getColTile((int) (x - spriteWidth / 2));
         int rightTile = tileMap.getColTile((int) (x + spriteWidth / 2) - 1);
         int topTile = tileMap.getRowTile((int) (y - spriteHeight / 2));
         int bottomTile = tileMap.getRowTile((int) (y + spriteHeight / 2) - 1);
         topLeft = tileMap.isBlocked(topTile, leftTile);
         topRight = tileMap.isBlocked(topTile, rightTile);
         bottomLeft = tileMap.isBlocked(bottomTile, leftTile);
         bottomRight = tileMap.isBlocked(bottomTile, rightTile);
     }
 
     private boolean interX(int interactionDist, int tx) {
         return (tx + interactionDist > x + 20 && tx - interactionDist < x + 20);
 
     }
 
     public void setNPCs(ArrayList<NPC> npcs) {
         this.npcs = npcs;
     }
 
     private boolean interY(int interactionDist, int ty) {
         return (ty + interactionDist > y && ty - interactionDist < y);
 
     }
 
     private boolean interact(int tx, int ty) {
         int interactionDist = 20;
         if (interY(interactionDist, ty) && interX(interactionDist, tx)) {
             return true;
         }
         return false;
 
     }
 
     public int interaction() {
         for (int i = 0; i < npcs.size(); i++) {
             int a = -npcs.get(i).getX();
             int b = npcs.get(i).getY();
             if (interact(a, b)) {
                 return i;
 
             }
         }
         return -1;
     }
 
     private boolean wall() {
         //boolean outsideOfMap = ((dy/32)>=20 || (dx/32) >= 20)? false:true;
         int tempx = ((-dx-21) / 32);
         int tempy = ((dy-16) / 32);
         tempy = (tempy > 0) ? tempy + 1 : tempy;
         tempx = (tempx > 0) ? tempx + 1 : tempx;
        boolean outsideOfMap = ((tempx)>20 || tempy>20)? true:false;
        return (outsideOfMap && tileMap.getTile(tempy, tempx) >= 13);
 
     }
 
     public void update() {
 //        System.out.println("Y: " + y + ", X: " + x);
 //        dy = 0;
 //        dx = 0;
 //        System.out.println(interaction());
         if (!OptionTrigger&&!interOk) {
             if (up && y != 0) {
                 dy -= speed;
             }
             if (down && y != 410) {
                 dy += speed;
             }
             if (left && x != 0) {
                 dx += speed;
             }
             if (right && x != -630) {
                 dx -= speed;
             }
 //            
         }
 
         System.out.println("X =  " + x + "   y =  " + y);
 
         if (wall()) {// || outsideOfMap) {
             dx = x;
             dy = y;
         } else {
             x = dx;
             y = dy;
         }
         //Sjekker kollisjon
         int curCol = tileMap.getColTile((int) x);
         int curRow = tileMap.getRowTile((int) y);
 
         double toX = x + dx;
         double toY = y + dy;
 
         double tempX = x;
         double tempY = y;
 
 //        calculateCorners(x, toY);
 //        if (dy < 0) {
 //            if (topLeft || topRight) {
 //                dy = 0;
 //                tempY = curRow * tileMap.getTileSize() + spriteHeight / 2;
 //            } else {
 //                tempY += dy;
 //            }
 //        }
 //        if (dy > 0) {
 //            if (bottomLeft || bottomRight) {
 //                dy = 0;
 ////                input.falling.toggle(false);
 ////                falling = false;
 //                tempY = (curRow + 1) * tileMap.getTileSize() - spriteHeight / 2;
 //            } else {
 //                tempY += dy;
 //            }
 //        }
 //        
 //        calculateCorners(toX, y);
 //        if (dx < 0) {
 //            if (topLeft || bottomLeft) {
 //                dx = 0;
 //                tempX = curCol * tileMap.getTileSize() + spriteWidth / 2;
 //            } else {
 //                tempX += dx;
 //            }
 //        }
 //        if (dx > 0) {
 //            if (topRight || bottomRight) {
 //                dx = 0;
 //                tempX = (curCol + 1) * tileMap.getTileSize() - spriteWidth / 2;
 //                
 //            } else {
 //                tempX += dx;
 //            }
 //        }
         //flytter vinduet
 //        tileMap.setX((int) (GamePanel.WIDTH / 2)+x);
 //        tileMap.setY((int) (GamePanel.HEIGHT / 2 - y));
 
         //sprite animation
         if (((right || left) && up) || (up && y != 0)) {
             animation.setFrames(walking_up);
             animation.setDelay(200);
         } else if (((right || left) && down || (down && y != 410))) {
             animation.setFrames(walking_down);
             animation.setDelay(200);
         } else if (left && x != 0 || right && x != -640) {
             animation.setFrames(walking_sideways);
             animation.setDelay(200);
         } else {
             if (idleDirection == 1) {
                 animation.setFrames(idleSprite_up);
                 animation.setDelay(-1);
             } else if (idleDirection == 2) {
                 animation.setFrames(idleSprite);
                 animation.setDelay(-1);
             } else if (idleDirection == 3) {
                 animation.setFrames(idleSprite_down);
                 animation.setDelay(-1);
             } else {
                 animation.setFrames(idleSprite);
                 animation.setDelay(-1);
             }
 
         }
         animation.update();
     }
 
     public boolean getOptionValue() {
         return OptionTrigger;
     }
 
     @Override
     public void draw(Graphics2D g) {
         //filler verdier, bruk tikeMap.getX/Y for å gi størrelsen til mappet
         int tx = tileMap.getX() + 10;
         int ty = tileMap.getY() + 10;
         if (facingLeft) {
             g.drawImage(animation.getImage(), (tx - x - spriteWidth / 2 + spriteWidth), (int) (ty + y - spriteHeight / 2),
                     -spriteWidth, spriteHeight, null);
         } else {
             g.drawImage(animation.getImage(), (int) (tx - x - spriteWidth / 2), (int) (ty + y - spriteWidth / 2), null);
         }
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         int key = e.getKeyCode();
         if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
             up = true;
             idleDirection = 1;
         }
         if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
             facingLeft = true;
             left = true;
             idleDirection = 2;
         }
         if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
             down = true;
             idleDirection = 3;
         }
         if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
             facingLeft = false;
             right = true;
             idleDirection = 4;
         }
         if (key == KeyEvent.VK_E || key == KeyEvent.VK_ENTER) {
            
            if(interaction() != -1 && interOk == false){ 
            interOk=true;
            }else if(interOk == true){
                interOk = false;
            }
            
        }    
 
         if (key == KeyEvent.VK_ESCAPE) {
             if (lock % 2 != 0) {
                 OptionTrigger = true;
                 lock--;
             } else {
                 OptionTrigger = false;
                 lock++;
             }
         }
     }
     public boolean getInterOk(){
         return interOk;
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         int key = e.getKeyCode();
         if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
             up = false;
         }
         if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
             left = false;
         }
         if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
             down = false;
         }
         if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
             right = false;
         }
 
     }
 }
