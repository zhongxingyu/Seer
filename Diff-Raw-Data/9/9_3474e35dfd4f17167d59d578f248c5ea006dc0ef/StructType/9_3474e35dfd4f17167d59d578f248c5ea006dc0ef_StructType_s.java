 import java.util.HashMap;
 
 public class StructType extends Type implements Cloneable {
    private HashMap<String, Type> members;
    private String name; // For error message purposes.
 
    StructType() {
       members = new HashMap<String, Type>();
       name = "shouldn't exist";
    }
 
    public StructType clone() {
       return this; /** TODO if we want this for more than type checking */
    }
 
    public boolean equals(Object other) {
       StructType otherStruct;
 
       if (other instanceof StructType) {
          otherStruct = (StructType)other;
 
          return otherStruct instanceof NullType 
           || otherStruct.getName().equals(getName());
       }
 
       return false;
    }
 
    public void addField(Symbol s) {
       members.put(s.getName(), s.getType());
    }
 
    public Type getField(String field) {
      Type t = members.get(field);

      if (t == null) {
         Evil.error("Trying to access undeclared field " + field 
          + " in struct " + getName());
      }

      return t;
    }
 
    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }
 
    public String toString() {
       return super.toString() + " " + getName();
    }
 }
