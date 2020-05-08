 package com.bluespot.logic.values;
 
 /**
  * A {@link Value} implementation that represents a value that can be changed.
  * 
  * @author Aaron Faanes
  * 
  * @param <T>
  *            the type of value held by this {@link Value} implementation
  */
 public final class MutableValue<T> implements Value<T> {
 
     private T value;
 
     /**
      * Constructs a {@link MutableValue} object using the specified value as the
      * initial value.
      * 
      * @param initialValue
      *            the initial value of this {@code Value} object. It may not be
      *            null.
      * @throws NullPointerException
      *             if {@code initialValue} is null
      */
     public MutableValue(final T initialValue) {
         if (initialValue == null) {
             throw new NullPointerException("initialValue cannot be null");
         }
         this.value = initialValue;
     }
 
     /**
      * Sets the value of this {@link Value} object.
      * 
      * @param value
      *            the new value
      * @return the previous value
     * @throws NullPointerException
     *             if {@code value} is null
      */
     public T set(final T value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
         final T oldValue = this.value;
         this.value = value;
         return oldValue;
     }
 
     public T get() {
         return this.value;
     }
 
     @Override
     public String toString() {
         return String.format("MutableValue[%s]", this.get());
     }
 
 }
