 
 package org.bukkit.entity;
 
 /**
  * A wild tameable cat
  */
 public interface Ocelot extends Animals, Tameable {
     /**
      * Gets the current type of this cat.
      *
      * @return Type of the cat.
      */
     public Type getCatType();
 
     /**
      * Sets the current type of this cat.
      *
      * @param type New type of this cat.
      */
     public void setCatType(Type type);
 
     /**
      * Represents the various different cat types there are.
      */
     public enum Type {
         WILD_OCELOT(0),
         BLACK_CAT(1),
         RED_CAT(2),
         SIAMESE_CAT(3);
 
         private static final Type[] types = new Type[Type.values().length];
         private final int id;
 
        static {
            for (Type type : values()) {
                types[type.getId()] = type;
            }
        }

         private Type(int id) {
             this.id = id;
         }
 
         /**
          * Gets the ID of this cat type.
          *
          * @return Type ID.
          */
         public int getId() {
             return id;
         }
 
         /**
          * Gets a cat type by its ID.
          *
          * @param id ID of the cat type to get.
          * @return Resulting type, or null if not found.
          */
         public static final Type getType(int id) {
             return (id >= types.length) ? null : types[id];
         }
     }
 }
