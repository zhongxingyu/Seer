 package kniemkiewicz.jqblocks.ingame;
 
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
 import kniemkiewicz.jqblocks.ingame.util.QuadTree;
 import kniemkiewicz.jqblocks.util.Assert;
 import org.newdawn.slick.geom.Shape;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.List;
 
 /**
  * User: knie
  * Date: 8/2/12
  */
 @Component
 public class CollisionController {
 
   public <T extends QuadTree.HasShape> List<T> getAll(EnumSet<ObjectType> types) {
     List<T> objects = new ArrayList<T>();
     for (ObjectType type : types) {
       quadTrees.get(type).listAll((List<QuadTree.HasShape>) objects);
     }
     return objects;
   }
 
   public enum ObjectType {
     MOVING_OBJECT,
     PICKABLE
   }
 
   EnumMap<ObjectType, QuadTree<QuadTree.HasShape>> quadTrees = new EnumMap<ObjectType, QuadTree<QuadTree.HasShape>>(ObjectType.class);
 
   public CollisionController() {
     for (ObjectType type : ObjectType.values()) {
       quadTrees.put(type, new QuadTree<QuadTree.HasShape>());
     }
   }
 
   /**
    * Unless forced, this will add object to all requested types or to none.
    */
   public boolean add(EnumSet<ObjectType> types, QuadTree.HasShape object, boolean force) {
     assert Assert.validateSerializable(object);
     ObjectType failedType = null;
     boolean result = true;
     for (ObjectType type : types) {
       if (!quadTrees.get(type).add(object)) {
         result = false;
         if (!force) {
           failedType = type;
         }
       }
     }
     // Reverting the change.
     if (failedType != null) {
       for (ObjectType type : ObjectType.values()) {
         if (type == failedType) break; // We haven't tried anything past that anyway.
         Assert.executeAndAssert(quadTrees.get(type).remove(object));
       }
       return false;
     }
     return result;
   }
 
   public <T extends QuadTree.HasShape> List<T> fullSearch(EnumSet<ObjectType> types, Shape shape) {
     List<T> objects = new ArrayList<T>();
     for (ObjectType type : types) {
       quadTrees.get(type).fullSearch(shape, (List<QuadTree.HasShape>) objects);
     }
     return objects;
   }
 
   public boolean intersects(EnumSet<ObjectType> types, Shape shape) {
     List<QuadTree.HasShape> objects = new ArrayList<QuadTree.HasShape>();
     for (ObjectType type : types) {
       quadTrees.get(type).fullSearch(shape, objects);
       if (objects.size() > 0) return true;
     }
     return false;
   }
 
   // Removes given object from all passed types. Returns true if all types contained this object.
   public boolean remove(EnumSet<ObjectType> types, QuadTree.HasShape object) {
     boolean result = true;
     for (ObjectType type : types) {
       if (!quadTrees.get(type).remove(object)) {
         result = false;
       }
     }
     return result;
   }
 
   public boolean update(EnumSet<ObjectType> types, QuadTree.HasShape object) {
     boolean result = true;
     for (ObjectType type : types) {
       if (!quadTrees.get(type).update(object)) {
         result = false;
       }
     }
     return result;
   }
 }
