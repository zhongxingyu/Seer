 /*
  * Created on Mar 22, 2005
  */
 package org.spacebar.escape.common;
 
 import java.io.PrintStream;
 
 /**
  * @author adam
  */
 abstract public class Entity {
     static private final int CAP_IS_PLAYER = 1;
 
     static private final int CAP_CAN_TELEPORT = 2;
 
     static private final int CAP_CRUSH_PLAYER = 4;
 
     static private final int CAP_WALK_INTO_BOTS = 8;
 
     static private final int CAP_PUSH_PLAYER = 16;
 
     static private final int CAP_ZAP_SELF = 32;
 
     static private final int CAP_PUSH_BOTS = 64;
 
     static private final int CAP_HEARTFRAMERS = 128;
 
     protected final void iCanTeleport() {
         capabilities |= CAP_CAN_TELEPORT;
     }
 
     protected final void iCrushPlayer() {
         iWalkIntoBots();
     }
 
     protected final void iAmPlayer() {
         capabilities |= CAP_IS_PLAYER;
     }
 
     protected final void iPushBots() {
         iPushPlayer();
     }
 
     protected final void iPushPlayer() {
         capabilities |= CAP_PUSH_BOTS;
         capabilities |= CAP_PUSH_PLAYER;
     }
 
     protected final void iWalkIntoBots() {
         capabilities |= CAP_CRUSH_PLAYER;
         capabilities |= CAP_WALK_INTO_BOTS;
         capabilities |= CAP_ZAP_SELF;
     }
 
     protected final void iZapSelf() {
         iWalkIntoBots();
     }
 
     protected final void iGetHeartFramers() {
         capabilities |= CAP_HEARTFRAMERS;
     }
 
     protected final void clearCapabilities() {
         capabilities = 0;
     }
 
     private int capabilities;
 
     private byte d;
 
     private int x;
 
     private int y;
 
     // directions
     public final static byte DIR_NONE = 0;
 
     public final static byte FIRST_DIR = 1;
 
     public final static byte DIR_UP = 1;
 
     public final static byte DIR_DOWN = 2;
 
     public final static byte DIR_LEFT = 3;
 
     public final static byte DIR_RIGHT = 4;
 
     public final static byte LAST_DIR = 4;
 
     public final static byte DIR_LEFT_RIGHT = 5;
 
     public final static byte DIR_UP_DOWN = 6;
 
     static public String directionToString(byte dir) {
         String s;
 
         switch (dir) {
         case DIR_UP:
             // s = "up";
             // s = "↑";
             s = "u";
             break;
         case DIR_DOWN:
             // s = "down";
             // s = "↓";
             s = "d";
             break;
         case DIR_LEFT:
             // s = "left";
             // s = "←";
             s = "l";
             break;
         case DIR_RIGHT:
             // s = "right";
             // s = "→";
             s = "r";
             break;
         default:
             s = "?";
         }
         return s;
     }
 
     public Entity(int x, int y, byte d) {
         this.x = x;
         this.y = y;
         this.d = d;
     }
 
     final public boolean canTeleport() {
         return (capabilities & CAP_CAN_TELEPORT) == CAP_CAN_TELEPORT;
     }
 
     final public boolean crushesPlayer() {
         return (capabilities & CAP_CRUSH_PLAYER) == CAP_CRUSH_PLAYER;
     }
 
     /**
      * @return Returns the d.
      */
     final public byte getDir() {
         return d;
     }
 
     /**
      * @return Returns the x.
      */
     final public int getX() {
         return x;
     }
 
     /**
      * @return Returns the y.
      */
     final public int getY() {
         return y;
     }
 
     final public boolean isPlayer() {
         return (capabilities & CAP_IS_PLAYER) == CAP_IS_PLAYER;
     }
 
     final public boolean canPushBots() {
         return (capabilities & CAP_PUSH_BOTS) == CAP_PUSH_BOTS;
     }
 
     final public boolean canPushPlayer() {
         return (capabilities & CAP_PUSH_PLAYER) == CAP_PUSH_PLAYER;
     }
 
     /**
      * @param d
      *            The d to set.
      */
     final public void setDir(byte d) {
         this.d = d;
     }
 
     /**
      * @param x
      *            The x to set.
      */
     final public void setX(int x) {
         this.x = x;
     }
 
     /**
      * @param y
      *            The y to set.
      */
     final public void setY(int y) {
         this.y = y;
     }
 
     final public boolean isAt(int x, int y) {
         return this.x == x && this.y == y;
     }
 
     final public boolean walksIntoBots() {
         return (capabilities & CAP_WALK_INTO_BOTS) == CAP_WALK_INTO_BOTS;
     }
 
     final public boolean zapsSelf() {
         return (capabilities & CAP_ZAP_SELF) == CAP_ZAP_SELF;
     }
 
     public void print(PrintStream p) {
         p.print(getClass().getName() + ": (" + x + "," + y + "," + d + ") [");
         if (isPlayer())
             p.print(" isPlayer");
         if (canTeleport())
             p.print(" canTeleport");
         if (crushesPlayer())
             p.print(" crushesPlayer");
         if (walksIntoBots())
             p.print(" walksIntoBots");
         if (canPushPlayer())
             p.print(" pushesPlayer");
         if (zapsSelf())
             p.print(" zapsSelf");
         if (canPushBots())
             p.print(" pushesBots");
         if (canGetHeartframers()) {
             p.print(" heartframers");
         }
         p.print(" ]");
     }
 
     public boolean canGetHeartframers() {
         return (capabilities & CAP_HEARTFRAMERS) == CAP_HEARTFRAMERS;
     }
 
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
 
         if (obj instanceof Entity) {
             Entity e = (Entity) obj;
 
             return x == e.x && y == e.y && type == e.type
                    && (!isBomb() || bombTimer == e.bombTimer);
         }
         return false;
     }
 
     public int hashCode() {
         // give 4 bits to type, 8 to timer, and 10 to x and y
         return type << 28 + bombTimer << 20 + x << 10 + y;
     }
 
     public boolean isBomb() {
         return type >= B_BOMB_0 && type <= B_BOMB_MAX;
     }
 
     public void armFuseIfBomb() {
         if (isBomb()) {
             bombTimer = (byte) (type - B_BOMB_0);
         }
     }
 
     public void expireTimer() {
         bombTimer = 0;
     }
 
     public byte getBombTimer() {
         return bombTimer;
     }
 
     public void burnLitFuse() {
         if (bombTimer != -1) {
             bombTimer--;
         }
     }
 
     static IntPair dirChange(int d) {
         int dx, dy;
         switch (d) {
         case DIR_UP:
             dx = 0;
             dy = -1;
             break;
         case DIR_LEFT:
             dx = -1;
             dy = 0;
             break;
         case DIR_RIGHT:
             dx = 1;
             dy = 0;
             break;
         case DIR_DOWN:
             dx = 0;
             dy = 1;
             break;
         default:
             dx = 0;
             dy = 0;
         }
         return new IntPair(dx, dy);
     }
 
     static String dirString(byte d) {
         switch (d) {
         case DIR_UP:
             return "up";
         case DIR_LEFT:
             return "left";
         case DIR_RIGHT:
             return "right";
         case DIR_DOWN:
             return "down";
         case DIR_NONE:
             return "none";
         default:
             return "??";
         }
     }
 
     static byte dirReverse(byte d) {
         switch (d) {
         case DIR_UP:
             return DIR_DOWN;
         case DIR_LEFT:
             return DIR_RIGHT;
         case DIR_DOWN:
             return DIR_UP;
         case DIR_RIGHT:
             return DIR_LEFT;
         default:
         case DIR_NONE:
             return DIR_NONE;
         }
     }
 
     // static functions
     static byte turnLeft(byte d) {
         switch (d) {
         case DIR_UP:
             return DIR_LEFT;
         case DIR_DOWN:
             return DIR_RIGHT;
         case DIR_RIGHT:
             return DIR_UP;
         case DIR_LEFT:
             return DIR_DOWN;
         default:
         case DIR_NONE:
             return DIR_NONE; /* ? */
         }
     }
 
     static byte turnRight(byte d) {
         switch (d) {
         case DIR_UP:
             return DIR_RIGHT;
         case DIR_DOWN:
             return DIR_LEFT;
         case DIR_RIGHT:
             return DIR_DOWN;
         case DIR_LEFT:
             return DIR_UP;
         default:
         case DIR_NONE:
             return DIR_NONE; /* ? */
         }
     }
 
     public static final byte BOMB_MAX_TIMER = 10;
 
     // exploded bomb, becomes deleted next turn
     public static final byte B_BOMB_X = -3;
 
     public static final byte B_DELETED = -2;
 
     public static final byte B_PLAYER = -1;
 
     public static final byte B_BROKEN = 0;
 
     public static final byte B_DALEK = 1;
 
     public static final byte B_HUGBOT = 2;
 
     public static final byte B_DALEK_ASLEEP = 3;
 
     public static final byte B_HUGBOT_ASLEEP = 4;
 
     public static final byte B_BOMB_0 = 5;
 
     protected byte bombTimer = -1;
 
     public static final byte B_BOMB_MAX = B_BOMB_0 + BOMB_MAX_TIMER;
 
     protected byte type;
 }
