 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 
 import java.awt.geom.Rectangle2D;
 
 public class Player {
   public static final int SIZE = 900;
 
   private final String name;
 
   private final int lives;
 
   private final int hitPoints;
 
   private final int maxHitPoints;
 
   /**
    * Which level is the player currently in?
    */
   private final String levelId;
 
   /**
    * Which room is the player currently in?
    */
   private final String roomId;
 
   /**
    * His position in the room.
    */
   private final Position position;
 
   /**
    * In which room was the player, when he activated the current save point?
    *
    * Whenever the player enters a new level, this should be reset to the starting room's id.
    */
   private final String savePointRoomId;
 
   /**
    * At which position was the player, when he activated the current save point?
    *
    * Whenever the player enters a new level, this should be reset to the player's position in the starting room.
    */
   private final Position savePointPosition;
 
   public Player (String name, int lives, int hitPoints, int maxHitPoints, String levelId, String roomId, Position position, String savePointRoomId, Position savePointPosition) {
     this.name = name;
     this.lives = lives;
     this.hitPoints = hitPoints;
     this.maxHitPoints = maxHitPoints;
     this.levelId = levelId;
     this.roomId = roomId;
     this.position = position;
     this.savePointRoomId = savePointRoomId;
     this.savePointPosition = savePointPosition;
   }
 
   public String getName () {
     return this.name;
   }
 
   public int getLives () {
     return this.lives;
   }
 
   public int getHitPoints () {
     return this.hitPoints;
   }
 
   public int getMaxHitPoints () {
     return this.maxHitPoints;
   }
 
   public String getLevelId () {
     return this.levelId;
   }
 
   public String getRoomId () {
     return this.roomId;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   public String getSavePointRoomId () {
     return this.savePointRoomId;
   }
 
   public Position getSavePointPosition () {
     return this.savePointPosition;
   }
 
   /**
    * Checks if the player touches the enemy #enemy.
    */
   public boolean touches (Enemy enemy) {
     Rectangle2D enemySpace = new Rectangle2D.Float(enemy.getPosition().getX(), enemy.getPosition().getY(), Enemy.SIZE, Enemy.SIZE);
 
     return this.playerSpace().intersects(enemySpace);
   }
 
   /**
    * Checks if the player touches the 1x1 tile at Position (#x, #y).
    */
   public boolean touches (Tile tile) {
     Rectangle2D tileSpace = new Rectangle2D.Float(tile.getPosition().getX(), tile.getPosition().getY(), Tile.SIZE, Tile.SIZE);
 
     return this.playerSpace().intersects(tileSpace);
   }
 
   public boolean touches (SavePoint savePoint) {
     Rectangle2D savePointSpace = new Rectangle2D.Float(savePoint.getPosition().getX(), savePoint.getPosition().getY(), SavePoint.SIZE, SavePoint.SIZE);
 
     return this.playerSpace().intersects(savePointSpace);
   }
 
   /**
    * Returns a rectangle that represents the space occupied by the player.
    */
   private Rectangle2D playerSpace () {
     return new Rectangle2D.Float(this.getPosition().getX(), this.getPosition().getY(), Player.SIZE, Player.SIZE);
   }
 
   public Player apply (Transform transform) {
     String name = this.name;
     int lives = this.lives;
     int hitPoints = this.hitPoints;
     int maxHitPoints = this.maxHitPoints;
     String levelId = this.levelId;
     String roomId = this.roomId;
     Position position = this.position;
     String savePointRoomId = this.savePointRoomId;
     Position savePointPosition = this.savePointPosition;
 
     if (transform instanceof MoveTransform) {
       MoveTransform move = (MoveTransform)transform;
 
       position = new Position(this.position.getX() + move.xDelta, this.position.getY() + move.yDelta);
     } else if (transform instanceof HitpointTransform) {
       HitpointTransform hpTransform = (HitpointTransform)transform;
 
       hitPoints += hpTransform.delta;
     } else if (transform instanceof LivesTransform) {
       LivesTransform livesTransform = (LivesTransform)transform;
 
       lives += livesTransform.delta;
     } else if (transform instanceof TeleportTransform) {
       TeleportTransform teleportTransform = (TeleportTransform)transform;
 
       roomId = teleportTransform.roomId;
       position = new Position(teleportTransform.x, teleportTransform.y);
     } else if (transform instanceof SavePointTransform) {
       SavePointTransform savePointTransform = (Player.SavePointTransform)transform;
 
      roomId = savePointTransform.roomId;
      position = new Position(savePointTransform.x, savePointTransform.y);
     }
 
     return new Player(name, lives, hitPoints, maxHitPoints, levelId, roomId, position, savePointRoomId, savePointPosition);
   }
 
   public static class MoveTransform implements Transform {
     private final int xDelta;
 
     private final int yDelta;
 
     public MoveTransform (int xDelta, int yDelta) {
       this.xDelta = xDelta;
       this.yDelta = yDelta;
     }
   }
 
   public static class HitpointTransform implements Transform {
     private final int delta;
 
     public HitpointTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class LivesTransform implements Transform {
     private final int delta;
 
     public LivesTransform (int delta) {
       this.delta = delta;
     }
   }
 
   public static class TeleportTransform implements Transform {
     private final String roomId;
 
     private final int x;
 
     private final int y;
 
     public TeleportTransform (String roomId, int x, int y) {
       this.roomId = roomId;
       this.x = x;
       this.y = y;
     }
   }
 
   public static class SavePointTransform implements Transform {
     private final String roomId;
 
     private final int x;
 
     private final int y;
 
     public SavePointTransform (String roomId, int x, int y) {
       this.roomId = roomId;
       this.x = x;
       this.y = y;
     }
   }
 
 }
