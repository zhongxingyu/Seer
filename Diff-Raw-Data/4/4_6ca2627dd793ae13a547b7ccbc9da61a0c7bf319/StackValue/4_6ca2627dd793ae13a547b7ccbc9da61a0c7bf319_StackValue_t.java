 package ox.stackgame.stackmachine;
 
 import ox.stackgame.stackmachine.exceptions.TypeException;
 
 
 public abstract class StackValue<T> {
     
     /**
      * Create a StackValue from a string
      * @param str       String to convert into a StackValue
      * @return          true if successful, false otherwise
      */
     public abstract boolean init( String str );
 
     /**
      * Get the value stored within the StackValue
      * @return
      */
     public abstract T getValue();
     
     public abstract StackValue<?> add(StackValue<?> y) throws TypeException;
     public abstract StackValue<?> sub(StackValue<?> y) throws TypeException;
     public abstract StackValue<?> mul(StackValue<?> y) throws TypeException;
     public abstract StackValue<?> div(StackValue<?> y) throws TypeException;
     
     public abstract boolean equals(Object other);
    
    public String toString() {
        return getValue().toString();
    }
 }
