 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 import java.io.Serializable;
 
 public class Projectile implements Spatial, Serializable {
   public static final int SIZE = 100;
 
   private final int id;
 
   private final Identifiable source;
 
   private final Position position;
 
   private final Vector velocity;
 
   private final int damage;
 
   private final DamageType type;
 
   public Projectile (int id, Identifiable source, Position position, Vector velocity, int damage, DamageType type) {
     this.id = id;
     this.source = source;
     this.position = position;
     this.velocity = velocity;
     this.damage = damage;
     this.type = type;
   }
 
   public int getId () {
     return this.id;
   }
 
   /**
    * What created this projectile?
    *
    * This may be null.
    */
   public Identifiable getSource () {
     return this.source;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   public Vector getVelocity () {
     return this.velocity;
   }
 
   public int getDamage () {
     return this.damage;
   }
 
   public DamageType getType () {
     return this.type;
   }
 
   public Projectile apply (Transform transform) {
     if (transform instanceof MoveTransform && ((MoveTransform)transform).projectileId == this.id) {
       return new Projectile(this.id, this.source, ((MoveTransform)transform).position, this.velocity, this.damage, this.type);
     } else {
       return this;
     }
   }
 
   @Override
   public Rectangle2D space () {
     return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
   }
 
   @Override
   public Position getCenter () {
     return new Position(this.position.getVector().plus(new Vector(SIZE / 2, SIZE / 2)));
   }
 
   @Override
   public boolean equals (Object o) {
     if (this == o) {
       return true;
     }
 
     if (o == null || getClass() != o.getClass()) {
       return false;
     }
 
     Projectile that = (Projectile)o;
 
    if (this.id != that.id) {
       return false;
     }
 
     return true;
   }
 
   @Override
   public int hashCode () {
    return this.id;
   }
 
   public static class MoveTransform implements Transform {
     private final int projectileId;
 
     private final Position position;
 
     public MoveTransform (int projectileId, Position position) {
       this.projectileId = projectileId;
       this.position = position;
     }
   }
 }
