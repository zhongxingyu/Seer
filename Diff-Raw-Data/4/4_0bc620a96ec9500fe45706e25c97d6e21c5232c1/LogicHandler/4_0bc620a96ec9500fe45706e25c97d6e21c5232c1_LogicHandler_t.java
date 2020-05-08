 package dungeon.game;
 
 import dungeon.LevelLoadHandler;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Enemy;
 import dungeon.models.Player;
 import dungeon.models.World;
 import dungeon.models.messages.IdentityTransform;
 import dungeon.models.messages.Transform;
 import dungeon.ui.events.MoveCommand;
 
 /**
  * Hier wird die eigentliche Logik des Spiels durchgeführt.
  */
 public class LogicHandler implements MessageHandler {
   private static final float SPEED = 0.1f;
 
   private final Mailman mailman;
 
   private World world;
 
   public LogicHandler (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof LevelLoadHandler.LevelLoadedEvent) {
       this.world = ((LevelLoadHandler.LevelLoadedEvent)message).getWorld();
     } else if (message instanceof MoveCommand) {
       move((MoveCommand)message);
     }
   }
 
   private void move (MoveCommand command) {
     Transform movementTransform = handleMovement(command);
     this.world = this.world.apply(movementTransform);
     this.mailman.send(movementTransform);
 
     Transform enemyTransform = handleEnemies();
     this.world = this.world.apply(enemyTransform);
     this.mailman.send(enemyTransform);
   }
 
   private Transform handleMovement (MoveCommand command) {
     switch (command) {
       case UP:
         if (this.world.getPlayer().getPosition().getY() - SPEED < 0) {
           return new IdentityTransform();
         } else {
           return new Player.MoveTransform(0, -SPEED);
         }
       case DOWN:
         if (this.world.getPlayer().getPosition().getY() + 1 + SPEED > this.world.getCurrentRoom().getYSize()) {
           return new IdentityTransform();
         } else {
           return new Player.MoveTransform(0, SPEED);
         }
       case LEFT:
         if (this.world.getPlayer().getPosition().getX() - SPEED < 0) {
           return new IdentityTransform();
         } else {
           return new Player.MoveTransform(-SPEED, 0);
         }
       case RIGHT:
         if (this.world.getPlayer().getPosition().getX() + 1 + SPEED > this.world.getCurrentRoom().getXSize()) {
           return new IdentityTransform();
         } else {
           return new Player.MoveTransform(SPEED, 0);
         }
       default:
     }
 
     return new IdentityTransform();
   }
 
   private Transform handleEnemies () {
     for (Enemy enemy : this.world.getCurrentRoom().getEnemies()) {
       if (this.world.getPlayer().touches(enemy)) {
         return new Player.HitpointTransform(-1);
       }
     }
    
     return new IdentityTransform();
   }
 }
