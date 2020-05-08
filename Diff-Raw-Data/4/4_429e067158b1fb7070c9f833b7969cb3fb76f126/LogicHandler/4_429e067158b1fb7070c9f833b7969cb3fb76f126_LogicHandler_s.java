 package dungeon.game;
 
 import dungeon.game.messages.DefeatEvent;
 import dungeon.game.messages.WinEvent;
 import dungeon.load.messages.LevelLoadedEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.*;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.messages.MoveCommand;
 
 /**
  * Handles the game logic.
  */
 public class LogicHandler implements MessageHandler {
   private static final int SPEED = 100;
 
   private final Mailman mailman;
 
   private World world;
 
   public LogicHandler (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof LevelLoadedEvent) {
       this.world = ((LevelLoadedEvent)message).getWorld();
     } else if (message instanceof MoveCommand) {
       move((MoveCommand)message);
     }
   }
 
   private void move (MoveCommand command) {
     this.applyTransform(this.handleMovement(command));
     this.applyTransform(this.handleEnemies());
     this.applyTransform(this.handleTeleporters());
 
     this.handleDefeat();
     this.handleWin();
   }
 
   /**
    * Applies a transform to the internal World object and send it to the mailman.
    */
   private void applyTransform (Transform transform) {
     this.world = this.world.apply(transform);
     this.mailman.send(transform);
   }
 
   private Transform handleMovement (MoveCommand command) {
     Transform movementTransform = moveTransform(command);
     movementTransform = filterWalls(movementTransform);
 
     return filterBorders(movementTransform);
   }
 
   /**
    * Create the appropriate MoveTransform for the command.
    */
   private Transform moveTransform (MoveCommand command) {
     switch (command) {
       case UP:
         return new Player.MoveTransform(0, -SPEED);
       case DOWN:
         return new Player.MoveTransform(0, SPEED);
       case LEFT:
         return new Player.MoveTransform(-SPEED, 0);
       case RIGHT:
         return new Player.MoveTransform(SPEED, 0);
       default:
     }
 
     return new IdentityTransform();
   }
 
   /**
    * Prevent movement if the player would walk on a wall.
    */
   private Transform filterWalls (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     for (Tile tile : this.world.getCurrentRoom().getTiles()) {
       if (tile.isBlocking()) {
         if (movedPlayer.touches(tile)) {
           return new IdentityTransform();
         }
       }
     }
 
     return transform;
   }
 
   /**
    * Prevent movement if the player would leave the playing field.
    */
   private Transform filterBorders (Transform transform) {
     Player movedPlayer = this.world.getPlayer().apply(transform);
 
     if (movedPlayer.getPosition().getY() < 0
       || movedPlayer.getPosition().getY() + Player.SIZE > this.world.getCurrentRoom().getYSize()
       || movedPlayer.getPosition().getX() < 0
       || movedPlayer.getPosition().getX() + Player.SIZE > this.world.getCurrentRoom().getXSize()) {
       return new IdentityTransform();
     } else {
       return transform;
     }
   }
 
   /**
    * Translate enemy contact into a transform.
    */
   private Transform handleEnemies () {
     for (Enemy enemy : this.world.getCurrentRoom().getEnemies()) {
       if (this.world.getPlayer().touches(enemy)) {
         return new Player.HitpointTransform(-1);
       }
     }
 
     return new IdentityTransform();
   }
 
   /**
    * Create a teleport transform it the player touches a teleporter.
    */
   private Transform handleTeleporters () {
     for (Tile tile : this.world.getCurrentRoom().getTiles()) {
       if (tile instanceof TeleporterTile) {
         TeleporterTile teleporter = (TeleporterTile)tile;
 
         if (this.world.getPlayer().touches(teleporter)) {
           TeleporterTile.Target target = teleporter.getTarget();
 
           return new Player.TeleportTransform(target.getRoomId(), target.getX(), target.getY());
         }
       }
     }
 
     return new IdentityTransform();
   }
 
   private void handleDefeat () {
     if (this.world.getPlayer().getHitPoints() == 0) {
       this.mailman.send(new DefeatEvent());
     }
   }
 
   private void handleWin () {
     for (Tile tile : this.world.getCurrentRoom().getTiles()) {
       if (tile instanceof VictoryTile) {
         VictoryTile victory = (VictoryTile)tile;
 
         if (this.world.getPlayer().touches(victory)) {
           this.mailman.send(new WinEvent());
         }
       }
     }
   }
 }
