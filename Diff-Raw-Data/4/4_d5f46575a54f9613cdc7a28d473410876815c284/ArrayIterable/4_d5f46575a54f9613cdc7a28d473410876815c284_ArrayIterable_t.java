 package net.jhorstmann.json;
 
 import java.lang.reflect.Array;
 import java.util.Iterator;
 
 class ArrayIterable implements Iterable {
     
     private Object array;
     private int length;
 
     ArrayIterable(Object array) {
        this.length = Array.getLength(array);
        this.array = array;
     }
 
     public Iterator<Object> iterator() {
         return new ArrayIterator(array, length);
     }
 }
