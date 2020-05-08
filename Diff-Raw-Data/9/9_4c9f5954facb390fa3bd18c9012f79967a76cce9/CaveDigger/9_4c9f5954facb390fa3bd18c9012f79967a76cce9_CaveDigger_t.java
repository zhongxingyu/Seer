 package com.macbury.procedular;
 
 import java.util.Random;
 
 import com.macbury.unamed.level.Block;
 import com.macbury.unamed.level.Level;
 import com.macbury.unamed.level.Sidewalk;
 
 public class CaveDigger {
   private static final float CHANCE_TO_SPAWN_NEW_MINER = 0.92f;
   private int x = 0;
   private int y = 0;
   
   private int dy = 0;
   private int dx = 0;
   private Level level;
   private Random random;
   
   public CaveDigger(Level level, Random random) {
     this.level = level;
     this.random = random;
 
   }
   
   public int getX() {
     return x;
   }
   public void setX(int x) {
     this.x = x;
   }
   public int getY() {
     return y;
   }
   public void setY(int y) {
     this.y = y;
   }
   public int getDy() {
     return dy;
   }
   public void setDy(int dy) {
     this.dy = dy;
   }
   public int getDx() {
     return dx;
   }
   public void setDx(int dx) {
     this.dx = dx;
   }
 
   public CaveDigger tryToSpawnDigger() {
     if (random.nextFloat() >= CHANCE_TO_SPAWN_NEW_MINER) {
       CaveDigger caveDigger = new CaveDigger(level, random);
       caveDigger.setX(x);
       caveDigger.setY(y);
       return caveDigger;
     } else {
       return null;
     }
   }
   
   public boolean dig() {
     if (random.nextBoolean()) {
       randomDirection();
     }
     
     int nx = dx + this.x;
     int ny = dy + this.y;
     
     Block block = level.getBlockForPosition(nx, ny);
     
     if (block != null && (block.isDirt() || block.isCobbleStone())) {
       this.x = nx;
       this.y = ny;
       this.level.setBlock(this.x, this.y, new Sidewalk(this.x, this.y));
       return true;
     } else {
       randomDirection();
       return false;
     }
   }
 
   private void randomDirection() {
     dx = 0;
     dy = 0;
     
     switch (random.nextInt(7)) {
       case 0:
         dx = 1;
         dy = 0;
       break;
       case 1:
         dx = 1;
         dy = 1;
       break;
       case 2:
         dx = 0;
         dy = 1;
       break;
       case 3:
         dx = -1;
         dy = 0;
       break;
       case 4:
         dx = -1;
         dy = -1;
       break;
       case 5:
         dx = 0;
         dy = -1;
       break;
       case 6:
         dx = -1;
         dy = 1;
       break;
       case 7:
         dx = 1;
         dy = -1;
       break;
       default:
         dx = 1;
         dy = 0;
       break;
     }
 
   }
   
   
 }
