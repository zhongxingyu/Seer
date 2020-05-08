 package codeplanes;
 
 import java.awt.geom.Point2D;
 
 /**
  * Represents bullet shot by player
  */
 public class Bullet extends Body {
     /**
      * Bullet constructor
      * @param id unique identifier of the bullet
      * @param position Cartesian position on the bullet
      * @param angle Clockwise angle in radians between vector of bullet direction and x-axis
      * @param speed Bullet's speed
      * @param playerId Id of player bullet belongs to
      */
     public Bullet(final int id, final Point2D position, final double angle, final double speed, final int playerId) {
         super(id, position, angle, speed);
         this.playerId = playerId;
     }
 
     /**
      *
      * @return Id of player who shot the bullet
      */
     public final int getPlayerId() {
         return playerId;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         if (!super.equals(o)) return false;
 
         final Bullet bullet = (Bullet) o;
 
         return playerId == bullet.playerId;
 
     }
 
     @Override
     public Bullet setAngle(double angle) {
         return new Bullet(getId(), getPosition(), angle, getSpeed(), playerId);
     }
 
     @Override
     public Bullet moveForward() {
         return new Bullet(
                 getId(),
                 new Point2D.Double(
                         getPosition().getX() + getSpeed() * Math.cos(getAngle()),
                        getPosition().getY() - getSpeed() * Math.sin(getAngle())),
                 getAngle(),
                 getSpeed(),
                 getPlayerId()
         );
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + playerId;
         return result;
     }
 
     private final int playerId;
 }
