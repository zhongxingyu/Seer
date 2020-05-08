 public abstract class Type {
 
    public boolean is_bool() {
       return this instanceof BoolType;
    }
 
   public boolean is_int() {
      return this instanceof IntType;
   }

   public boolean equals(Object obj) {
      return obj.getClass().equals(this.getClass());
   }

    public String toString() {
       return this.getClass().getName();
    }
 }
