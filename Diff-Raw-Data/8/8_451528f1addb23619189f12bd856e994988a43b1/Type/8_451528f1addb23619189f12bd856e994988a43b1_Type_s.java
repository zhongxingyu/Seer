 public abstract class Type {
 
    public boolean is_bool() {
       return this instanceof BoolType;
    }
 
    public String toString() {
       return this.getClass().getName();
    }
 }
