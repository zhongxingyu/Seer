 package crypt.actor;
 
 import crypt.Game;
 
 public class Bug extends Actor {
 
   private int tick = 0;
   private static final int SPEED = 4;
   private static final Point[] directions =
   { Point.UP, Point.DOWN, Point.LEFT, Point.RIGHT };
 
   public Bug(Point position, Game game) {
     super(':', position, game);
   }
 
   @Override
   public void update() {
 
     if (tick++ % SPEED != 0)
       return;
 
     Point direction = getRandomDirection();
 
     Point nextPosition = new Point(getPosition());
     nextPosition.add(direction);
    Actor target = game.getBoard().getActorAt(nextPosition);
    game.collide(this, target);
   }
 
   private Point getRandomDirection() {
     int dirIdx = (int) (Math.random() * 4);
     return directions[dirIdx];
   }
 
 }
