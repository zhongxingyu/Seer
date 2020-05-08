 package player;
 
 import enemies.Enemy;
 import util.EnemyDeletable;
 import java.awt.*;
 import util.EnemyPredicate;
 
 public abstract class Player {
 
     private static final int 
             SENBONSAKURA_RADIUS = 250, 
             SENBONSAKURA_SQUARE = SENBONSAKURA_RADIUS*SENBONSAKURA_RADIUS,
             IMMUNITY_LIFETIME = 2000,
             SENBONSAKURA_TIMEOUT = 5000;
     protected int lives;
     protected boolean isActive;
     protected boolean hasImmunity;
     protected long lastImmunity;
     protected int x;
     protected int y;
     protected EnemyDeletable parent;
     private int senbonSakuraN;
     private int senbonSakuraC;
    private long senbonSakuraT = 0;
     protected int infoOffset;
 
     public Player(int _x, int _y, int numberOfLives, boolean startActive, int numberOfSenbonsakura, EnemyDeletable _parent, int _infoOffset) {
         lives = numberOfLives;
         isActive = startActive;
         x = _x;
         y = _y;
         parent = _parent;
         senbonSakuraN = numberOfSenbonsakura;
         infoOffset = _infoOffset;
     }
 
     public int getX() {
         return x;
     }
 
     public int getY() {
         return y;
     }
 
     public int getLives() {
         return lives;
     }
     
     public void setImmunity(boolean _hasImmunity)
     {
         hasImmunity = _hasImmunity;
         lastImmunity = System.currentTimeMillis();
     }
     
     public boolean getImmunity()
     {
         if((lastImmunity+IMMUNITY_LIFETIME) < System.currentTimeMillis())
         {
             hasImmunity = false;       
         }
         return hasImmunity;
     }
 
     public void resetLives(int _lives) {
         lives = _lives;
     }
 
     public void decLives(int _x, int _y) {
         lives--;
         x = _x;
         y = _y;
     }
 
     public boolean isActive() {
         return isActive;
     }
 
     public void setActive(boolean isActive) {
         this.isActive = isActive;
     }
 
     public abstract void move();
 
     protected void senbonSakura() {
        if (((System.currentTimeMillis() - senbonSakuraT)>SENBONSAKURA_TIMEOUT)&&(senbonSakuraN-- > 0)) {
             senbonSakuraC = 40;
             parent.deleteIf(new EnemyPredicate() {
                 public boolean satisfiedBy(Enemy e) {
                     float p1 = (x + 5) - e.getX();
                     float p2 = (y + 5) - e.getY();
                     return (p1*p1 + p2*p2) < SENBONSAKURA_SQUARE;
                 }
             });
             senbonSakuraT = System.currentTimeMillis();
         }
     }
 
     public void paint(Graphics g) {
         g.setColor(Color.WHITE);
         g.fillOval(x - 5, y - 5, 10, 10);
         if (senbonSakuraC-->0) {
             g.setColor(Color.PINK);
             g.fillOval(x-SENBONSAKURA_RADIUS/2, y-SENBONSAKURA_RADIUS/2, SENBONSAKURA_RADIUS, SENBONSAKURA_RADIUS);
         }
        int barWidth = (int)Math.min(SENBONSAKURA_TIMEOUT, System.currentTimeMillis()-senbonSakuraT);
         barWidth *=.05;
         g.setColor(Color.PINK);
         g.fillRect(100, infoOffset, barWidth, 5);
     }
 }
