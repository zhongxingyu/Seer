 package com.psddev.dari.db;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import com.psddev.dari.util.ObjectUtils;
 
 /** Atomic operation on a field value within a state. */
 public abstract class AtomicOperation {
 
     private final String field;
 
     /** Creates an instance that operates on the given {@code field}. */
     protected AtomicOperation(String field) {
         this.field = field;
     }
 
     /**
      * Returns the name of the field whose value is to be changed
      * atomically.
      */
     public String getField() {
         return field;
     }
 
     /** Executes this atomic operation on the given {@code state}. */
     public abstract void execute(State state);
 
     /**
      * Atomic arithmetic increment operation. Use a negative number
      * for a decrement operation.
      */
     public static class Increment extends AtomicOperation {
 
         private final double value;
 
         public Increment(String field, double value) {
             super(field);
             this.value = value;
         }
 
         @Override
         public void execute(State state) {
             String field = getField();
             Object oldValue = state.getValue(field);
             double newValue = value;
 
             if (oldValue instanceof Number) {
                 newValue += ((Number) oldValue).doubleValue();
             }
 
             state.putValue(field, newValue);
         }
     }
 
     /**
      * Atomic add operation to a collection. If the referenced field
      * doesn't contain a collection, one's automatically created.
      */
     public static class Add extends AtomicOperation {
 
         private final Object value;
 
         public Add(String field, Object value) {
             super(field);
             this.value = value;
         }
 
         @Override
         public void execute(State state) {
             String field = getField();
             Object oldValue = state.getValue(field);
 
             if (oldValue instanceof Collection) {
                 @SuppressWarnings("unchecked")
                 Collection<Object> values = (Collection<Object>) oldValue;
                 values.add(value);
 
             } else {
                 List<Object> values = new ArrayList<Object>();
                 values.add(value);
                 state.putValue(field, values);
             }
         }
     }
 
     /**
      * Atomic remove operation from a collection. If the referenced field
      * doesn't contain a collection, one's automatically created.
      */
     public static class Remove extends AtomicOperation {
 
         private final Object value;
 
         public Remove(String field, Object value) {
             super(field);
             this.value = value;
         }
 
         @Override
         public void execute(State state) {
             String field = getField();
             Object oldValue = state.getValue(field);
 
             if (oldValue instanceof Collection) {
                 Collection<?> collection = (Collection<?>) oldValue;
                 while (collection.remove(value)) {
                 }
             }
         }
     }
 
     /**
      * Atomic replace operation that only sets the new value if the old
      * value didn't change. May throw {@link ReplacementException} if the
      * old value changes before the operation can execute.
      */
     public static class Replace extends AtomicOperation {
 
         private final Object oldValue;
         private final Object newValue;
 
         public Replace(String field, Object oldValue, Object newValue) {
             super(field);
             this.oldValue = oldValue;
             this.newValue = newValue;
         }
 
         @Override
         public void execute(State state) {
             String field = getField();
 
             if (ObjectUtils.equals(state.getValue(field), oldValue)) {
                 state.putValue(field, newValue);
 
             } else {
                 throw new ReplacementException(state, field, oldValue, newValue);
             }
         }
     }
 
 
     /** Atomic put operation. */
     public static class Put extends AtomicOperation {
 
         private final Object value;
 
         public Put(String field, Object value) {
             super(field);
             this.value = value;
         }
 
         @Override
         public void execute(State state) {
             state.putValue(getField(), value);
         }
     }
 
     /**
      * Thrown if the object state changes between when an atomic
      * replacement operation is requested and executed.
      */
    public static class ReplacementException extends RuntimeException {
 
         private static final long serialVersionUID = 1L;
 
         private final State state;
         private final String field;
         private final Object oldValue;
         private final Object newValue;
 
         public ReplacementException(State state, String field, Object oldValue, Object newValue) {
             super(String.format("Can't replace [%s] in #[%s]!", field, state.getId()));
             this.state = state;
             this.field = field;
             this.oldValue = oldValue;
             this.newValue = newValue;
         }
 
         /** Returns the state where the atomic operation was executed. */
         public State getState() {
             return state;
         }
 
         /** Returns the name of the field whose value was being replaced. */
         public String getField() {
             return field;
         }
 
         /** Returns the field value when the replacement was requested. */
         public Object getOldValue() {
             return oldValue;
         }
 
         /** Returns the replacement field value. */
         public Object getNewValue() {
             return newValue;
         }
     }
 }
