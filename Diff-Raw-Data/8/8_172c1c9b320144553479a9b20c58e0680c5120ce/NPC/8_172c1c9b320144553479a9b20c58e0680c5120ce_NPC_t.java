 package dungeon.models;
 
 import dungeon.models.messages.Transform;
 import dungeon.util.Vector;
 
 import java.awt.geom.Rectangle2D;
 
 public class NPC implements Spatial {
   public static final int SIZE = 1000;
 
   private final int id;
 
   private final Position position;
 
   private final String name;
 
   private final String saying;
 
   public NPC (int id, Position position, String name, String saying) {
     this.id = id;
     this.position = position;
     this.name = name;
     this.saying = saying;
   }
 
   public int getId () {
     return this.id;
   }
 
   public Position getPosition () {
     return this.position;
   }
 
   public String getName () {
     return this.name;
   }
 
   public String getSaying () {
     return this.saying;
   }
 
   @Override
   public Rectangle2D space () {
     return new Rectangle2D.Float(this.position.getX(), this.position.getY(), SIZE, SIZE);
   }
 
   @Override
   public Position getCenter () {
     return new Position(this.position.getVector().plus(new Vector(SIZE / 2, SIZE / 2)));
   }
 
   /**
    * This does not actually transform anything, but tells the UI to display the dialog.
    */
   public static class InteractTransform implements Transform {
     private final NPC npc;
 
     public InteractTransform (NPC npc) {
       this.npc = npc;
     }
 
     public NPC getNpc () {
      return this.npc;
     }
   }
 }
