 package org.spacebar.escape.common;
 
 import java.io.*;
 
 public class Level {
 
     public static class MetaData {
         final public int width;
 
         final public int height;
 
         final public String title;
 
         final public String author;
 
         public MetaData(int width, int height, String title, String author) {
             this.width = width;
             this.height = height;
             this.title = title;
             this.author = author;
         }
     }
 
     // panel colors
     public final static int PANEL_REGULAR = 0;
 
     public final static int PANEL_BLUE = 1;
 
     public final static int PANEL_GREEN = 2;
 
     public final static int PANEL_RED = 3;
 
     // panels under tiles
     public final static int TF_NONE = 0;
 
     /* panel under tile (ie, pushable block) */
     /*
      * if HASPANEL is set, then TF_RPANELH * 2 + TF_RPANELL says what kind (see
      * panel colors above)
      */
     public final static int TF_HASPANEL = 1;
 
     public final static int TF_RPANELL = 4;
 
     public final static int TF_RPANELH = 8;
 
     /* panel under tile in bizarro world */
     /* same refinement */
     public final static int TF_OPANEL = 2;
 
     public final static int TF_ROPANELL = 16;
 
     public final static int TF_ROPANELH = 32;
 
     public final static int TF_TEMP = 64; // used for swapping during play
 
     // panels
     public final static int T_FLOOR = 0;
 
     public final static int T_RED = 1;
 
     public final static int T_BLUE = 2;
 
     public final static int T_GREY = 3;
 
     public final static int T_GREEN = 4;
 
     public final static int T_EXIT = 5;
 
     public final static int T_HOLE = 6;
 
     public final static int T_GOLD = 7;
 
     public final static int T_LASER = 8;
 
     public final static int T_PANEL = 9;
 
     public final static int T_STOP = 10;
 
     public final static int T_RIGHT = 11;
 
     public final static int T_LEFT = 12;
 
     public final static int T_UP = 13;
 
     public final static int T_DOWN = 14;
 
     public final static int T_ROUGH = 15;
 
     public final static int T_ELECTRIC = 16;
 
     public final static int T_ON = 17;
 
     public final static int T_OFF = 18;
 
     public final static int T_TRANSPORT = 19;
 
     public final static int T_BROKEN = 20;
 
     public final static int T_LR = 21;
 
     public final static int T_UD = 22;
 
     public final static int T_0 = 23;
 
     public final static int T_1 = 24;
 
     public final static int T_NS = 25;
 
     public final static int T_NE = 26;
 
     public final static int T_NW = 27;
 
     public final static int T_SE = 28;
 
     public final static int T_SW = 29;
 
     public final static int T_WE = 30;
 
     public final static int T_BUTTON = 31;
 
     public final static int T_BLIGHT = 32;
 
     public final static int T_RLIGHT = 33;
 
     public final static int T_GLIGHT = 34;
 
     public final static int T_BLACK = 35;
 
     public final static int T_BUP = 36;
 
     public final static int T_BDOWN = 37;
 
     public final static int T_RUP = 38;
 
     public final static int T_RDOWN = 39;
 
     public final static int T_GUP = 40;
 
     public final static int T_GDOWN = 41;
 
     public final static int T_BSPHERE = 42;
 
     public final static int T_RSPHERE = 43;
 
     public final static int T_GSPHERE = 44;
 
     public final static int T_SPHERE = 45;
 
     public final static int T_TRAP2 = 46;
 
     public final static int T_TRAP1 = 47;
 
     public final static int T_BPANEL = 48;
 
     public final static int T_RPANEL = 49;
 
     public final static int T_GPANEL = 50;
 
     public final static int T_STEEL = 51;
 
     public final static int T_BSTEEL = 52;
 
     public final static int T_RSTEEL = 53;
 
     public final static int T_GSTEEL = 54;
 
     /**
      * @return Returns the author.
      */
     public String getAuthor() {
         return author;
     }
 
     /**
      * @param author
      *           The author to set.
      */
     public void setAuthor(String author) {
         this.author = author;
     }
 
     /**
      * @return Returns the title.
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title
      *           The title to set.
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     public int getBotX(int index) {
         return bots[index].getX();
     }
 
     public int getBotY(int index) {
         return bots[index].getY();
     }
 
     public int getBotType(int index) {
         return bots[index].getBotType();
     }
 
     public int getPlayerX() {
         return player.getX();
     }
 
     public int getPlayerY() {
         return player.getY();
     }
 
     public int getHeight() {
         return height;
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getBotCount() {
         return bots.length;
     }
 
     // static functions
     static int turnLeft(int d) {
         switch (d) {
         case Entity.DIR_UP:
             return Entity.DIR_LEFT;
         case Entity.DIR_DOWN:
             return Entity.DIR_RIGHT;
         case Entity.DIR_RIGHT:
             return Entity.DIR_UP;
         case Entity.DIR_LEFT:
             return Entity.DIR_DOWN;
         default:
         case Entity.DIR_NONE:
             return Entity.DIR_NONE; /* ? */
         }
     }
 
     static int turnRight(int d) {
         switch (d) {
         case Entity.DIR_UP:
             return Entity.DIR_RIGHT;
         case Entity.DIR_DOWN:
             return Entity.DIR_LEFT;
         case Entity.DIR_RIGHT:
             return Entity.DIR_DOWN;
         case Entity.DIR_LEFT:
             return Entity.DIR_UP;
         default:
         case Entity.DIR_NONE:
             return Entity.DIR_NONE; /* ? */
         }
     }
 
     static IntPair dirChange(int d) {
         int dx, dy;
         switch (d) {
         case Entity.DIR_UP:
             dx = 0;
             dy = -1;
             break;
         case Entity.DIR_LEFT:
             dx = -1;
             dy = 0;
             break;
         case Entity.DIR_RIGHT:
             dx = 1;
             dy = 0;
             break;
         case Entity.DIR_DOWN:
             dx = 0;
             dy = 1;
             break;
         default:
             dx = 0;
             dy = 0;
         }
         return new IntPair(dx, dy);
     }
 
     static String dirString(int d) {
         switch (d) {
         case Entity.DIR_UP:
             return "up";
         case Entity.DIR_LEFT:
             return "left";
         case Entity.DIR_RIGHT:
             return "right";
         case Entity.DIR_DOWN:
             return "down";
         case Entity.DIR_NONE:
             return "none";
         default:
             return "??";
         }
     }
 
     static int dirReverse(int d) {
         switch (d) {
         case Entity.DIR_UP:
             return Entity.DIR_DOWN;
         case Entity.DIR_LEFT:
             return Entity.DIR_RIGHT;
         case Entity.DIR_DOWN:
             return Entity.DIR_UP;
         case Entity.DIR_RIGHT:
             return Entity.DIR_LEFT;
         default:
         case Entity.DIR_NONE:
             return Entity.DIR_NONE;
         }
     }
 
     // member variables
 
     // metadata
     private String title;
 
     private String author;
 
     // width, height
     final int width;
 
     final int height;
 
     final private Player player;
 
     // shown
     private final int tiles[];
 
     // "other" (tiles swapped into bizarro world by panels)
     private final int oTiles[];
 
     // destinations for transporters and panels (as index into tiles)
     private final int dests[];
 
     // has a panel (under a pushable block)? etc.
     private final int flags[];
 
     private Bot bots[];
 
     // dirty
     public final DirtyList dirty;
 
     // cached laser
     private IntTriple laser;
 
     // the meat
     private void warp(Entity ent, int targX, int targY) {
         checkStepOff(ent.getX(), ent.getY());
         ent.setX(targX);
         ent.setY(targY);
 
         switch (tileAt(targX, targY)) {
         case T_PANEL:
             swapO(destAt(targX, targY));
             break;
         default:
             ;
         }
     }
 
     private IntPair where(int idx) {
         int x = idx % width;
         int y = idx / width;
 
         return new IntPair(x, y);
     }
 
     int index(int x, int y) {
         return (y * width) + x;
     }
 
     public int tileAt(int i) {
         return tiles[i];
     }
 
     public int tileAt(int x, int y) {
         return tiles[y * width + x];
     }
 
     public int oTileAt(int x, int y) {
         return oTiles[y * width + x];
     }
 
     private void setTile(int i, int t) {
         tiles[i] = t;
         dirty.setDirty(i);
     }
 
     private void setTile(int x, int y, int t) {
         setTile(y * width + x, t);
     }
 
     private void oSetTile(int x, int y, int t) {
         oTiles[y * width + x] = t;
     }
 
     private void setDest(int x, int y, int xd, int yd) {
         dests[y * width + x] = yd * width + xd;
     }
 
     private int destAt(int x, int y) {
         return dests[y * width + x];
     }
 
     private IntPair getDest(int x, int y) {
         int xd = dests[y * width + x] % width;
         int yd = dests[y * width + x] / width;
 
         return new IntPair(xd, yd);
     }
 
     private int flagAt(int x, int y) {
         return flags[y * width + x];
     }
 
     public boolean isWon() {
         return tileAt(player.getX(), player.getY()) == T_EXIT;
     }
 
     private IntPair travel(int x, int y, int d) {
         switch (d) {
         case Entity.DIR_UP:
             if (y == 0) {
                 return null;
             } else {
                 return new IntPair(x, y - 1);
             }
         case Entity.DIR_DOWN:
             if (y == (height - 1)) {
                 return null;
             } else {
                 return new IntPair(x, y + 1);
             }
         case Entity.DIR_LEFT:
             if (x == 0) {
                 return null;
             } else {
                 return new IntPair(x - 1, y);
             }
         case Entity.DIR_RIGHT:
             if (x == (width - 1)) {
                 return null;
             } else {
                 return new IntPair(x + 1, y);
             }
         default:
             return null; /* ?? */
         }
     }
 
     // Return true if a laser can 'see' the player.
     public IntTriple isDead() {
         if (laser != null) {
             return laser;
         }
 
         // bots kill
         if (isBotAt(player.getX(), player.getY())) {
             laser = new IntTriple(player.getX(), player.getY(), Entity.DIR_NONE);
             return laser;
         }
 
         // otherwise, look for lasers from the current dude
         for (int dd = Entity.FIRST_DIR; dd <= Entity.LAST_DIR; dd++) {
             int lx = player.getX(), ly = player.getY();
 
             IntPair r;
             while ((r = travel(lx, ly, dd)) != null) {
                 lx = r.x;
                 ly = r.y;
 
                 if (tileAt(lx, ly) == T_LASER) {
                     int tileX = r.x;
                     int tileY = r.y;
                     int d = dirReverse(dd);
 
                     laser = new IntTriple(tileX, tileY, d);
                     return laser;
                 }
                 int tt = tileAt(lx, ly);
                 if (tt != T_FLOOR && tt != T_ELECTRIC && tt != T_ROUGH
                         && tt != T_RDOWN && tt != T_GDOWN && tt != T_BDOWN
                         && tt != T_TRAP2 && tt != T_TRAP1 && tt != T_PANEL
                         && tt != T_BPANEL && tt != T_GPANEL && tt != T_RPANEL
                         && tt != T_BLACK && tt != T_HOLE)
                     break;
                 // all robots also block lasers
                 if (isBotAt(lx, ly))
                     break;
             }
         }
         laser = null;
         return null;
     }
 
     private void swapO(int idx) {
         int tmp = tiles[idx];
         setTile(idx, oTiles[idx]);
         oTiles[idx] = tmp;
 
         /* swap haspanel/opanel and their refinements as well */
         flags[idx] =
 
         /* panel bits */
         ((flags[idx] & TF_HASPANEL) != 0 ? TF_OPANEL : TF_NONE)
                 | ((flags[idx] & TF_OPANEL) != 0 ? TF_HASPANEL : TF_NONE)
                 |
 
                 /* refinement */
                 ((flags[idx] & TF_RPANELL) != 0 ? TF_ROPANELL : TF_NONE)
                 | ((flags[idx] & TF_RPANELH) != 0 ? TF_ROPANELH : TF_NONE)
                 |
 
                 /* orefinement */
                 ((flags[idx] & TF_ROPANELL) != 0 ? TF_RPANELL : TF_NONE)
                 | ((flags[idx] & TF_ROPANELH) != 0 ? TF_RPANELH : TF_NONE)
                 |
 
                 /* erase old */
                 (flags[idx] & ~(TF_HASPANEL | TF_OPANEL | TF_RPANELL
                         | TF_RPANELH | TF_ROPANELL | TF_ROPANELH));
     }
 
     /*
      * after stepping off a tile, deactivate a panel if there was one there.
      */
     private void checkLeavePanel(int x, int y) {
         /* nb: only for regular panels */
         if (tileAt(x, y) == T_PANEL) {
             swapO(destAt(x, y));
         }
     }
 
     /* actions on the player stepping off of a tile */
     private void checkStepOff(int x, int y) {
         /* nb: only for regular panels */
         checkLeavePanel(x, y);
         if (tileAt(x, y) == T_TRAP1) {
             setTile(x, y, T_HOLE);
         } else if (tileAt(x, y) == T_TRAP2) {
             setTile(x, y, T_TRAP1);
         }
     }
 
     private static int realPanel(int f) {
         if ((f & TF_RPANELH) != 0) {
             if ((f & TF_RPANELL) != 0)
                 return T_RPANEL;
             else
                 return T_GPANEL;
         } else {
             if ((f & TF_RPANELL) != 0)
                 return T_BPANEL;
             else
                 return T_PANEL;
         }
     }
 
     private boolean isPanel(int t) {
         return (t == T_PANEL || t == T_RPANEL || t == T_GPANEL || t == T_BPANEL);
     }
 
     private boolean isSphere(int t) {
         return (t == T_SPHERE || t == T_RSPHERE || t == T_GSPHERE || t == T_BSPHERE);
     }
 
     private boolean isSteel(int t) {
         return (t == T_STEEL || t == T_RSTEEL || t == T_GSTEEL || t == T_BSTEEL);
     }
 
     private void swapTiles(int t1, int t2) {
         for (int i = (width * height) - 1; i >= 0; i--) {
             if (tiles[i] == t1)
                 setTile(i, t2);
             else if (tiles[i] == t2)
                 setTile(i, t1);
         }
     }
 
     public boolean move(int d) {
         return move(d, null);
     }
 
     public boolean move(int d, Effects e) {
         player.setDir(d); // always set dir
         boolean result = realMove(player, d, e);
 
         if (result) {
             if (e != null) {
                 e.doStep();
             }
 
             // move bots
             for (int i = 0; i < bots.length; i++) {
                 Bot b = bots[i];
                 if (b.getBotType() == Bot.B_DELETED) {
                     continue;
                 }
                 IntPair dirs = b.getDirChoices(player);
 
                 if (dirs.x != Entity.DIR_NONE) {
                     boolean bm = realMove(b, dirs.x, e);
 
                     // no good? try 2nd move
                     if (!bm && dirs.y != Entity.DIR_NONE) {
                         realMove(b, dirs.y, e);
                     }
                 }
             }
         } else {
             if (e != null) {
                 e.doNoStep();
             }
         }
 
         isDead(); // update laser cache
         return result;
     }
 
     private boolean realMove(Entity ent, int d, Effects e) {
         int target;
         IntPair newP;
         if ((newP = travel(ent.getX(), ent.getY(), d)) != null) {
             switch (target = tileAt(newP.x, newP.y)) {
 
             /* these aren't pressed by the player so act like floor */
             case T_BPANEL:
             case T_GPANEL:
             case T_RPANEL:
 
             /* these are only affected when we step *off* */
             case T_TRAP2:
             case T_TRAP1:
 
             case T_FLOOR:
             case T_ROUGH:
             case T_BDOWN:
             case T_RDOWN:
             case T_GDOWN:
             case T_PANEL:
                 // sometimes we will push
                 Entity pushee = null;
                 Bot b;
                 if (player.isAt(newP.x, newP.y)) { // ent is not player!
                     // if player is on bot, no pushing either of them
                     if (getBotAt(newP.x, newP.y) != null) {
                         return false;
                     }
 
                     if (ent.canPushPlayer()) {
                         pushee = player;
                     } else if (ent.crushesPlayer()) {
                         pushee = null; // CRUSH! not push
                     } else {
                         return false;
                     }
                 } else if ((b = getBotAt(newP.x, newP.y)) != null) {
                     if (ent.canPushBots()) {
                         pushee = b;
                     } else if (ent.walksIntoBots()) {
                         pushee = null; // WALK! not push
                     } else {
                         return false;
                     }
                 }
 
                 if (pushee != null) {
                     // we are pushing, do some sort of recursive push
                     IntPair far = travel(newP.x, newP.y, d);
                     if (far != null) {
                         int fTarget = tileAt(far.x, far.y);
                         switch (fTarget) {
                         case T_ELECTRIC:
                             // only bots can go into electric
                             if (pushee == player) {
                                 return false;
                             }
                             break;
                         case T_TRAP2:
                         case T_TRAP1:
                         case T_FLOOR:
                         case T_ROUGH:
                         case T_RDOWN:
                         case T_GDOWN:
                         case T_BDOWN:
                         case T_PANEL:
                         case T_RPANEL:
                         case T_GPANEL:
                         case T_BPANEL:
                             break;
                         default:
                             return false;
                         }
 
                         // can't push 2 entities
                         if (isBotAt(far.x, far.y)) {
                             return false;
                         }
                         if (player.isAt(far.x, far.y)) {
                             return false;
                         }
 
                         // push
                         pushee.setX(far.x);
                         pushee.setY(far.y);
 
                         // zapping
                         if (fTarget == T_ELECTRIC && pushee != player) {
                             ((Bot) pushee).delete();
                         }
 
                         // panels
                         if (fTarget == T_PANEL) {
                             swapO(destAt(far.x, far.y));
                         }
 
                         // handle leaving current (pushed) position
                         if (target == T_PANEL) {
                             // do nothing, or else get a double flip
                             // since pusher is going on here now
                         } else {
                             checkStepOff(newP.x, newP.y);
                         }
 
                         // handle leaving pusher position
                         checkStepOff(ent.getX(), ent.getY());
 
                         // then move
                         ent.setX(newP.x);
                         ent.setY(newP.y);
 
                         // done?
                         return true;
                     } else {
                         return false;
                     }
                 } else {
                     checkBotDeath(newP.x, newP.y, ent); // might have stepped
                     // onto bot
 
                     // panels again
                     checkStepOff(ent.getX(), ent.getY());
                     if (target == T_PANEL) {
                         swapO(destAt(newP.x, newP.y));
                     }
 
                     ent.setX(newP.x);
                     ent.setY(newP.y);
 
                     return true;
                 }
 
             case T_EXIT:
                 // bots don't exit
                 if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
                     return false;
                 }
 
                 checkStepOff(ent.getX(), ent.getY());
                 ent.setX(newP.x);
                 ent.setY(newP.y);
                 return true;
 
             case T_ON: {
                 if (e != null) {
                     e.doElectricOff();
                 }
                 for (int i = (width * height) - 1; i >= 0; i--) {
                     if (tiles[i] == T_ELECTRIC)
                         setTile(i, T_FLOOR);
                 }
                 setTile(newP.x, newP.y, T_OFF);
                 return true;
             }
             case T_0:
             case T_1: {
                 int opp = (target == T_0 ? T_1 : T_0);
 
                 swapTiles(T_UD, T_LR);
 
                 if (e != null) {
                     e.doSwap();
                 }
                 setTile(newP.x, newP.y, opp);
 
                 return true;
             }
 
             case T_BSPHERE:
             case T_RSPHERE:
             case T_GSPHERE:
             case T_SPHERE:
             case T_GOLD: {
 
                 /*
                  * spheres allow pushing in a line: ->OOOO becomes OOO ---->O
                  * 
                  * so keep travelling while the tile in the destination
                  * direction is a sphere of any sort.
                  */
                 IntPair t;
                 while (isSphere(tileAt(newP.x, newP.y))
                         && !(player.isAt(newP.x, newP.y) || isBotAt(newP.x,
                                 newP.y))
                         && (t = travel(newP.x, newP.y, d)) != null
                         && isSphere(tileAt(t.x, t.y))) {
                     newP = t;
                     target = tileAt(t.x, t.y);
                 }
 
                 // can't push if entity there
                 if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
                     return false;
                 }
 
                 int goldX = newP.x, goldY = newP.y;
 
                 /* remove gold block */
                 if ((flagAt(goldX, goldY) & TF_HASPANEL) != 0) {
                     setTile(goldX, goldY, realPanel(flagAt(goldX, goldY)));
                 } else {
                     setTile(goldX, goldY, T_FLOOR);
                 }
 
                 IntPair tGold;
                 while ((tGold = travel(goldX, goldY, d)) != null) {
 
                     int next = tileAt(tGold.x, tGold.y);
                     if (!(next == T_ELECTRIC || next == T_PANEL
                             || next == T_BPANEL || next == T_RPANEL
                             || next == T_GPANEL || next == T_FLOOR
                             || isBotAt(tGold.x, tGold.y) || player.isAt(
                             tGold.x, tGold.y))) {
                         break;
                     }
 
                     goldX = tGold.x;
                     goldY = tGold.y;
 
                     if (next == T_ELECTRIC)
                         break;
                 }
 
                 /* goldx is dest, newx is source */
                 if (goldX != newP.x || goldY != newP.y) {
 
                     int landOn = tileAt(goldX, goldY);
                     boolean doSwap = false;
 
                     /* untrigger from source */
                     if ((flagAt(newP.x, newP.y) & TF_HASPANEL) != 0) {
                         int pan = realPanel(flagAt(newP.x, newP.y));
                         /* any */
                         if (pan == T_PANEL ||
                         /* colors */
                         (target == T_GSPHERE && pan == T_GPANEL)
                                 || (target == T_RSPHERE && pan == T_RPANEL)
                                 || (target == T_BSPHERE && pan == T_BPANEL))
                             doSwap = true;
                     }
 
                     /*
                      * only the correct color sphere can trigger the colored
                      * panels
                      */
                     boolean doSwapT = triggers(target, landOn);
 
                     setTile(goldX, goldY, target);
 
                     boolean zapped = false;
                     if (landOn == T_ELECTRIC) {
                         /*
                          * gold zapped. however, if the electric was the target
                          * of a panel that we just left, the electric has been
                          * swapped into the o world (along with the gold). So
                          * swap there.
                          */
                         if (e != null) {
                             e.doZap();
                         }
                         setTile(goldX, goldY, T_ELECTRIC);
 
                         zapped = true;
                     }
 
                     if (doSwapT) {
                         swapO(destAt(goldX, goldY));
                     }
 
                     if (doSwap) {
                         swapO(destAt(goldX, goldY));
                     }
 
                     if (e != null) {
                         e.doSlide();
                     }
 
                     return true;
                 } else {
                     // didn't move, put it back
                     setTile(newP.x, newP.y, target);
 
                     return false;
                 }
             }
             case T_TRANSPORT: {
                 // not if there's an entity there
                 if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
                     return false;
                 }
 
                 if (ent.canTeleport() || ent.isPlayer()) {
                     IntPair targ;
                     targ = where(dests[width * newP.y + newP.x]);
 
                     if (e != null) {
                         e.doTransport();
                     }
                     warp(ent, targ.x, targ.y);
 
                     checkBotDeath(targ.x, targ.y, ent);
 
                     return true;
                 } else {
                     return false;
                 }
             }
             case T_BUTTON: {
 
                 for (int dd = Entity.FIRST_DIR; dd <= Entity.LAST_DIR; dd++) {
                     /* send a pulse in that direction. */
                     IntPair pulse = newP;
                     int pd = dd;
 
                     while (pd != Entity.DIR_NONE
                             && (pulse = travel(pulse.x, pulse.y, pd)) != null) {
                         switch (tileAt(pulse.x, pulse.y)) {
                         case T_BLIGHT:
                             swapTiles(T_BUP, T_BDOWN);
                             pd = Entity.DIR_NONE;
                             break;
                         case T_RLIGHT:
                             swapTiles(T_RUP, T_RDOWN);
                             pd = Entity.DIR_NONE;
                             break;
                         case T_GLIGHT:
                             swapTiles(T_GUP, T_GDOWN);
                             pd = Entity.DIR_NONE;
                             break;
 
                         case T_NS:
                             if (pd == Entity.DIR_UP || pd == Entity.DIR_DOWN)
                                 continue;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         case T_WE:
                             if (pd == Entity.DIR_LEFT || pd == Entity.DIR_RIGHT)
                                 continue;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         case T_NW:
                             if (pd == Entity.DIR_DOWN)
                                 pd = Entity.DIR_LEFT;
                             else if (pd == Entity.DIR_RIGHT)
                                 pd = Entity.DIR_UP;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         case T_SW:
                             if (pd == Entity.DIR_UP)
                                 pd = Entity.DIR_LEFT;
                             else if (pd == Entity.DIR_RIGHT)
                                 pd = Entity.DIR_DOWN;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         case T_NE:
                             if (pd == Entity.DIR_DOWN)
                                 pd = Entity.DIR_RIGHT;
                             else if (pd == Entity.DIR_LEFT)
                                 pd = Entity.DIR_UP;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         case T_SE:
                             if (pd == Entity.DIR_UP)
                                 pd = Entity.DIR_RIGHT;
                             else if (pd == Entity.DIR_LEFT)
                                 pd = Entity.DIR_DOWN;
                             else
                                 pd = Entity.DIR_NONE;
                             break;
 
                         default:
                             pd = Entity.DIR_NONE;
                         }
                     }
                 }
 
                 if (e != null) {
                     e.doPulse();
                 }
                 return true;
             }
             case T_BROKEN:
                 setTile(newP.x, newP.y, T_FLOOR);
                 if (e != null) {
                     e.doBroken();
                 }
                 return true;
 
             case T_GREEN: {
                 IntPair dest;
                 if ((dest = travel(newP.x, newP.y, d)) != null) {
                     if (tileAt(dest.x, dest.y) == T_FLOOR
                             && !isBotAt(dest.x, dest.y)
                             && !player.isAt(dest.x, dest.y)) {
                         setTile(dest.x, dest.y, T_BLUE);
                         setTile(newP.x, newP.y, T_FLOOR);
 
                         checkStepOff(ent.getX(), ent.getY());
 
                         ent.setX(newP.x);
                         ent.setY(newP.y);
                         return true;
                     } else
                         return false;
                 } else
                     return false;
             }
 
             // steel
             case T_STEEL:
             case T_RSTEEL:
             case T_GSTEEL:
             case T_BSTEEL: {
 
                 /*
                  * three phases. first, see if we can push this whole column one
                  * space.
                  * 
                  * if so, generate animations.
                  * 
                  * then, update panel states. this is tricky.
                  */
 
                 IntPair dest = newP;
                 {
                     int curx = newP.x, cury = newP.y;
                     /*
                      * go until not steel, or if we hit a robot anywhere along
                      * this, end
                      */
                     while (!isBotAt(curx, cury) && !player.isAt(curx, cury)
                             && (dest = travel(curx, cury, d)) != null
                             && isSteel(tileAt(dest.x, dest.y))) {
                         curx = dest.x;
                         cury = dest.y;
                     }
                 }
 
                 /* entity in our column or at the end? sorry */
                 if (isBotAt(dest.x, dest.y) || player.isAt(dest.x, dest.y))
                     return false;
 
                 /* what did we hit? */
                 int hitTile;
                 boolean zap = false;
                 switch (hitTile = tileAt(dest.x, dest.y)) {
                 /*
                  * nb if we "hit" steel, then it's steel to the edge of the
                  * level, so no push.
                  */
                 case T_PANEL:
                 case T_GPANEL:
                 case T_BPANEL:
                 case T_RPANEL:
                 case T_FLOOR:
                     break;
                 case T_ELECTRIC:
                     zap = true;
                     break;
                 default:
                     return (false);
                 }
 
                 /*
                  * guy destx,desty v v [ ][S][S][S][S][ ] ^ steels starting at
                  * newx,newy
                  * 
                  * d ---->
                  */
                 int revD = dirReverse(d);
 
                 /* move the steel blocks first. */
                 {
                     int movex = dest.x, movey = dest.y;
                     while (!(movex == newP.x && movey == newP.y)) {
                         IntPair next;
                         next = travel(movex, movey, revD);
                         setTile(movex, movey, tileAt(next.x, next.y));
                         movex = next.x;
                         movey = next.y;
                     }
                 }
 
                 /* and one more, for the tile that we're stepping onto */
                 {
                     int replacement = ((flagAt(newP.x, newP.y) & TF_HASPANEL) == TF_HASPANEL) ? realPanel(flagAt(
                             newP.x, newP.y))
                             : T_FLOOR;
                     setTile(newP.x, newP.y, replacement);
                 }
 
                 /*
                  * reconcile panels.
                  * 
                  * imagine pushing a row of blocks one space to the right.
                  * 
                  * we loop over the NEW positions for the steel blocks. If a
                  * steel block is on a panel (that it can trigger), then we
                  * trigger that panel as long as the thing to its right (which
                  * used to be there) couldn't trigger it. this handles new
                  * panels that are turned ON.
                  * 
                  * if we can't trigger the panel, then we check to see if the
                  * panel to our right (which used to be there) also can't
                  * trigger it. If so, we don't do anything. Otherwise, we
                  * "untrigger" the panel.
                  * 
                  * To simplify, if triggerstatus_now != triggerstatus_old, we
                  * trigger. (Trigger has the same effect as untriggering.)
                  * 
                  * Because these swaps are supposed to be delayed, we set the
                  * TF_TEMP flag if the tile should do a swap afterwards.
                  */
 
                 boolean swapnew = false;
                 {
                     int lookx = dest.x, looky = dest.y;
                     int prevt = T_FLOOR; /* anything that doesn't trigger */
                     while (!(lookx == newP.x && looky == newP.y)) {
 
                         int heret = tileAt(lookx, looky);
 
                         /* triggerstatus for this location (lookx, looky) */
                         boolean triggerstatus_now = ((flagAt(lookx, looky) & TF_HASPANEL) == TF_HASPANEL)
                                 && triggers(heret, realPanel(flagAt(lookx,
                                         looky)));
 
                         boolean triggerstatus_old = ((flagAt(lookx, looky) & TF_HASPANEL) == TF_HASPANEL)
                                 && isSteel(prevt)
                                 && triggers(prevt, realPanel(flagAt(lookx,
                                         looky)));
 
                         if (triggerstatus_now != triggerstatus_old) {
                             setFlag(lookx, looky, flagAt(lookx, looky)
                                     | TF_TEMP);
                             //           printf("Yes swap at %d/%d\n", lookx, looky);
                         } else
                             setFlag(lookx, looky, flagAt(lookx, looky)
                                     & ~TF_TEMP);
 
                         prevt = heret;
 
                         IntPair next;
                         next = travel(lookx, looky, revD);
 
                         lookx = next.x;
                         looky = next.y;
                     }
 
                     /* first panel is slightly different */
                     {
                         int first = tileAt(newP.x, newP.y);
                         boolean trig_now = (first == T_PANEL);
                         boolean trig_old = isPanel(first)
                                 && triggers(prevt, realPanel(flagAt(newP.x,
                                         newP.y)));
 
                         if (trig_old != trig_now) {
                             swapnew = true;
                         }
                     }
                 } /* zap, if necessary, before swapping */
                 if (zap) {
                     setTile(dest.x, dest.y, T_ELECTRIC);
                     /* XXX animate */
                 }
 
                 /* now we can start swapping. */
                 checkStepOff(ent.getX(), ent.getY());
 
                 /*
                  * this part is now invariant to order, because there is only
                  * one destination per location
                  */
 
                 if (swapnew) {
                     swapO(destAt(newP.x, newP.y));
                 }
 
                 {
                     int lookx = dest.x, looky = dest.y;
                     while (!(lookx == newP.x && looky == newP.y)) {
 
                         if ((flagAt(lookx, looky) & TF_TEMP) == TF_TEMP) {
                             swapO(destAt(lookx, looky));
                             setFlag(lookx, looky, flagAt(lookx, looky)
                                     & ~TF_TEMP);
                         }
 
                         /* next */
                         IntPair next;
                         next = travel(lookx, looky, revD);
                         lookx = next.x;
                         looky = next.y;
                     }
                 }
 
                 /* XXX also boundary conditions? (XXX what does that mean?) */
                 ent.setX(newP.x);
                 ent.setY(newP.y);
 
                 return true;
             }
 
             // simple pushable blocks use this case
             case T_RED:
             case T_NS:
             case T_NE:
             case T_NW:
             case T_SE:
             case T_SW:
             case T_WE:
 
             case T_LR:
             case T_UD:
 
             case T_GREY: {
                 /*
                  * we're always stepping onto the panel that the block was on,
                  * so we don't need to change its state. (if it's a regular
                  * panel, then don't change because our feet are on it. if it's
                  * a colored panel, don't change because neither the man nor the
                  * block can activate it.) But we do need to put a panel there
                  * instead of floor.
                  */
 
                 int replacement = ((flagAt(newP.x, newP.y) & TF_HASPANEL) == TF_HASPANEL) ? realPanel(flagAt(
                         newP.x, newP.y))
                         : T_FLOOR;
 
                 boolean doSwap = false;
                 boolean zap = false;
                 boolean hole = false;
                 IntPair dest;
 
                 if (target == T_LR
                         && (d == Entity.DIR_UP || d == Entity.DIR_DOWN))
                     return false;
                 if (target == T_UD
                         && (d == Entity.DIR_LEFT || d == Entity.DIR_RIGHT))
                     return false;
 
                 if ((dest = travel(newP.x, newP.y, d)) != null) {
                     int destT = tileAt(dest.x, dest.y);
                     if (player.isAt(dest.x, dest.y) || isBotAt(dest.x, dest.y)) {
                         return false;
                     }
                     switch (destT) {
                     case T_FLOOR:
                         /* easy */
                         setTile(dest.x, dest.y, target);
                         setTile(newP.x, newP.y, replacement);
                         break;
                     case T_ELECTRIC:
                         /* Zap! */
                         if (target != T_LR && target != T_UD) {
                             if (e != null) {
                                 e.doZap();
                             }
                             setTile(newP.x, newP.y, replacement);
                         } else
                             return false;
                         zap = true;
                         break;
                     case T_HOLE:
                         /* only grey blocks into holes */
                         if (target == T_GREY) {
                             if (e != null) {
                                 e.doHole();
                             }
                             setTile(dest.x, dest.y, T_FLOOR);
                             setTile(newP.x, newP.y, replacement);
                             hole = true;
                             break;
                         } else
                             return false;
                     case T_BPANEL:
                     case T_RPANEL:
                     case T_GPANEL:
                     case T_PANEL:
                         if (target != T_LR && target != T_UD) {
                             /* delay the swap */
                             doSwap = (destT == T_PANEL); // grey down holes
                             setTile(dest.x, dest.y, target);
                             setTile(newP.x, newP.y, replacement);
                         } else
                             return false;
                         break;
                     default:
                         return false;
                     }
                     checkStepOff(ent.getX(), ent.getY());
 
                     if (doSwap)
                         swapO(destAt(dest.x, dest.y));
 
                     ent.setX(newP.x);
                     ent.setY(newP.y);
                     return true;
                 } else
                     return false;
             }
 
             case T_ELECTRIC:
                 // some bots are stupid enough to zap themselves
                 if (ent != player && ent.zapsSelf()) {
                     // move
                     ent.setX(newP.x);
                     ent.setY(newP.y);
 
                     // kill
                     ((Bot) ent).delete();
                     return true;
                 } else
                     return false;
 
             case T_BLUE:
             case T_HOLE:
             case T_LASER:
             case T_STOP:
             case T_RIGHT:
             case T_LEFT:
             case T_UP:
             case T_DOWN:
             case T_BLIGHT:
             case T_RLIGHT:
             case T_GLIGHT:
             case T_RUP:
             case T_BUP:
             case T_GUP:
             case T_OFF:
             case T_BLACK:
 
             default:
                 return false;
 
             }
         } else
             return false;
     }
 
     private void setFlag(int x, int y, int f) {
         flags[y * width + x] = f;
     }
 
     private boolean triggers(int tile, int panel) {
         /* "anything" triggers grey panels */
         if (panel == T_PANEL)
             return true;
         if (panel == T_RPANEL) {
             return tile == T_RSPHERE || tile == T_RSTEEL;
         }
         if (panel == T_GPANEL) {
             return tile == T_GSPHERE || tile == T_GSTEEL;
         }
         if (panel == T_BPANEL) {
             return tile == T_BSPHERE || tile == T_BSTEEL;
         }
         /* ? */
         return false;
     }
 
     private void checkBotDeath(int x, int y, Entity ent) {
         if (ent != player) {
             for (int b = 0; b < bots.length; b++) {
                 Bot bb = bots[b];
                if (ent != bb && bb != null && x == bb.getX()
                        && y == bb.getY()) {
                     bots[b].delete();
                     ((Bot) ent).setToType(Bot.B_BROKEN);
                 }
             }
         }
     }
 
    
     private boolean isBotAt(int x, int y) {
         return getBotAt(x, y) != null;
     }
 
     private Bot getBotAt(int x, int y) {
         for (int i = 0; i < bots.length; i++) {
             if (bots[i].getBotType() != Bot.B_DELETED && bots[i].isAt(x, y)) {
                 return bots[i];
             }
         }
         return null;
     }
 
     public Level(BitInputStream in) throws IOException {
         MetaData m = getMetaData(in);
 
         width = m.width;
         height = m.height;
 
         author = m.author;
         title = m.title;
 
         int playerX = getIntFromStream(in);
         int playerY = getIntFromStream(in);
 
         player = new Player(playerX, playerY, Entity.DIR_DOWN);
 
         tiles = RunLengthEncoding.decode(in, width * height);
         oTiles = RunLengthEncoding.decode(in, width * height);
         dests = RunLengthEncoding.decode(in, width * height);
         flags = RunLengthEncoding.decode(in, width * height);
 
         // load bots if in file
         int bots;
         int botI[] = null;
         int botT[] = null;
         try {
             bots = getIntFromStream(in);
             botI = RunLengthEncoding.decode(in, bots);
             botT = RunLengthEncoding.decode(in, bots);
         } catch (EOFException e) {
             bots = 0;
         }
 
         this.bots = new Bot[bots];
 
         for (int i = 0; i < this.bots.length; i++) {
             int x = botI[i] % width;
             int y = botI[i] / width;
             this.bots[i] = new Bot(x, y, Entity.DIR_DOWN, botT[i]);
         }
 
         dirty = new DirtyList();
         dirty.setAllDirty();
 
         isDead(); // calculate laser cache
     }
 
     public static MetaData getMetaData(BitInputStream in) throws IOException {
         String magic = getStringFromStream(in, 4);
         if (!magic.equals("ESXL")) {
             throw new IOException("Bad magic");
         }
 
         int width = getIntFromStream(in);
         int height = getIntFromStream(in);
 
         //        System.out.println("width: " + width + ", height: " + height);
 
         int size;
 
         size = getIntFromStream(in);
         String title = getStringFromStream(in, size);
 
         size = getIntFromStream(in);
         String author = getStringFromStream(in, size);
 
         return new MetaData(width, height, title, author);
     }
 
     public class DirtyList {
         boolean allDirty;
 
         private final boolean dirty[];
 
         private final int dirtyList[];
 
         private int numDirty;
 
         DirtyList() {
             int n = width * height;
             dirty = new boolean[n];
             dirtyList = new int[n];
         }
 
         public void clearDirty() {
             for (int i = dirty.length - 1; i >= 0; i--) {
                 dirty[i] = false;
             }
             numDirty = 0;
             allDirty = false;
         }
 
         public void setDirty(int x, int y) {
             setDirty(index(x, y));
         }
 
         public void setDirty(int i) {
             if (dirty[i]) {
                 return;
             }
 
             dirty[i] = true;
             dirtyList[numDirty] = i;
             numDirty++;
         }
 
         public void setAllDirty() {
             allDirty = true;
         }
 
         public boolean isDirty(int i) {
             return allDirty || dirty[i];
         }
 
         public boolean isDirty(int x, int y) {
             return allDirty || dirty[index(x, y)];
         }
 
         public boolean isAnyDirty() {
             return allDirty || numDirty > 0;
         }
     }
 
     public void print(PrintStream p) {
         p.println("\"" + title + "\" by " + author + " (" + width + ","
                 + height + ")" + " player: (" + this.player.getX() + ","
                 + this.player.getY() + ")");
         p.println();
         p.println("tiles");
         printM(p, tiles, width);
 
         p.println();
         p.println("oTiles");
         printM(p, oTiles, width);
 
         p.println();
         p.println("dests");
         printM(p, dests, width);
 
         p.println();
         p.println("flags");
         printM(p, flags, width);
     }
 
     private void printM(PrintStream p, int[] m, int w) {
         int l = 0;
         for (int i = 0; i < m.length; i++) {
             p.print((char) (m[i] + 32));
             l++;
             if (l == w) {
                 p.println();
                 l = 0;
             }
         }
     }
 
     private static int getIntFromStream(InputStream in) throws IOException {
         int r = 0;
 
         r += eofRead(in) << 24;
         r += eofRead(in) << 16;
         r += eofRead(in) << 8;
         r += eofRead(in);
 
         return r;
     }
 
     private static int eofRead(InputStream in) throws IOException {
         int i = in.read();
         if (i == -1) {
             throw new EOFException();
         }
         return i;
     }
 
     private static String getStringFromStream(InputStream in, int size)
             throws IOException {
         byte buf[] = new byte[size];
 
         in.read(buf);
 
         String result = new String(buf);
         return (result);
     }
 
     public int getBotDir(int botIndex) {
         return bots[botIndex].getDir();
     }
 
     public int getPlayerDir() {
         return player.getDir();
     }
 
     public boolean isBotDeleted(int botIndex) {
         return bots[botIndex].getBotType() == Bot.B_DELETED;
     }
 }
