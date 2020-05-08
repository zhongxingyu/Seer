 package codeplanes;
 
 import java.awt.geom.Point2D;
 
 /**
  * Represents programmer-controllable plane
  */
 public class Plane extends Body {
     /**
      * Plane constructor
      * @param id unique identifier of the plane
      * @param position Cartesian position on the plane
      * @param angle Clockwise angle in radians between vector of plane direction and x-axis
      * @param speed Plane's speed
      * @param playerId Id of player plane belongs to
      */
     public Plane(final int id, final Point2D position, final double angle, final double speed, final int playerId) {
         super(id, position, angle, speed);
         this.playerId = playerId;
     }
 
     /**
      *
      * @return Id of player plane belongs to
      */
     public final int getPlayerId() {
         return playerId;
     }
 
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         if (!super.equals(o)) return false;
 
         final Plane plane = (Plane) o;
 
         return playerId == plane.playerId;
 
     }
 
     @Override
     public Plane setAngle(double angle) {
         return new Plane(getId(), getPosition(), angle, getSpeed(), playerId);
     }
 
     @Override
     public Plane moveForward() {
         return new Plane(
                 getId(),
                 new Point2D.Double(
                         getPosition().getX() + getSpeed() * Math.cos(getAngle()),
                        getPosition().getY() + getSpeed() * Math.sin(getAngle())),
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
