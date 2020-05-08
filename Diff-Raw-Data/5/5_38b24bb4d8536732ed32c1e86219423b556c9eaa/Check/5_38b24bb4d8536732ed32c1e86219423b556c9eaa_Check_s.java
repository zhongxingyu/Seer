 package de.skuzzle.polly.tools;
 
 import java.util.Collection;
 
 public final class Check {
 
     
     
     private static class AbstractChecker {
         
         public ObjectChecker andObjects(Object...o) {
             return new ObjectChecker(o);
         }
         
         
         
         public <T> CollectionChecker<T> andCollection(Collection<T> c) {
             return new CollectionChecker<>(c);
         }
         
         
         
         public IntChecker andInts(int...i) {
             return new IntChecker(i);
         }
         
         
         
         public StringChecker andString(String s) {
             return new StringChecker(s);
         }
     }
     
     
     
     public static class ObjectChecker extends AbstractChecker {
         
         private final Object[] o;
         
         
         private ObjectChecker(Object[] o) {
            if (this.o == null) {
                 throw new NullPointerException();
             }
             this.o = o;
         }
         
         
         
         private void notNull(Object o, int i) {
             if (o == null) { 
                 throw new NullPointerException("object is null: " + i);
             }
         }
         
         
         
         public ObjectChecker notNull() {
             for (int i = 0; i < this.o.length; ++i) {
                 this.notNull(this.o[i], i);
             }
             return this;
         }
         
         
         
         public ObjectChecker isInstance(Class<?> cls) {
             for (int i = 0; i < this.o.length; ++i) {
                 if (!cls.isInstance(this.o[i])) {
                     throw new IllegalArgumentException(
                             this.o[i] + " is no instance of " + cls);
                 }
             }
             return this;
         }
         
         
         
         public ObjectChecker equal(Object other) {
             for (int i = 0; i < this.o.length; ++i) {
                 if (!this.o[i].equals(other)) {
                     throw new IllegalArgumentException(this.o[i] + 
                             " does not equal " + other);
                 }
             }
             return this;
         }
     }
     
     
     
     public static class CollectionChecker<T> extends AbstractChecker {
         
         private final Collection<T> c;
         
         
         private CollectionChecker(Collection<T> c) {
             this.c = c;
         }
         
         
         
         public CollectionChecker<T> notNull() {
             if (this.c == null) {
                 throw new NullPointerException();
             }
             return this;
         }
         
         
         
         public CollectionChecker<T> notEmpty() {
             if (this.c.isEmpty()) {
                 throw new IllegalArgumentException("collection is empty");
             }
             return this;
         }
         
         
         
         public CollectionChecker<T> minimumSize(int i) {
             if (this.c.size() < i) {
                 throw new IllegalArgumentException(
                         "collection contains less than " + i + " elements");
             }
             return this;
         }
         
         
         
         public CollectionChecker<T> equal(Object o) {
             if (!this.c.equals(o)) {
                 throw new IllegalArgumentException(c + " does not equal " + o);
             }
             return this;
         }
         
         
         
         public CollectionChecker<T> contains(T o) {
             if (!this.c.contains(o)) {
                 throw new IllegalArgumentException("collection does not contain " + o);
             }
             return this;
         }
     }
     
     
     
     public static class IntChecker extends AbstractChecker {
         
         private final int[] i;
         
         
         private IntChecker(int[] i) {
             this.i = i;
         }
         
         
         
         public IntChecker isBetween(int bound1, int bound2) {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
             
                 final int lower = Math.min(bound1, bound2);
                 final int upper = Math.max(bound1, bound2);
             
                 if (i < lower || i > upper) {
                     throw new IllegalArgumentException(i + " is not between " + lower + 
                             " and " + upper);
                 }
             }
             return this;
         }
         
         
         
         public IntChecker isLowerThan(int bound) {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
                 if (i < bound) {
                     throw new IllegalArgumentException(i + " < " + bound);
                 }
             }
             return this;
         }
         
         
         
         public IntChecker isPositiveOrZero() {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
                 if (i < 0) {
                     throw new IllegalArgumentException(i + " is < 0");
                 }
             }
             return this;
         }
         
         
         
         public IntChecker isPositive() {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
                 if (i <= 0) {
                     throw new IllegalArgumentException(i + " is <= 0");
                 }
             }
             return this;
         }
         
         
         
         public IntChecker isNegativeOrZero() {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
                 if (i >= 0) {
                     throw new IllegalArgumentException(i + " is >= 0");
                 }
             }
             return this;
         }
         
         
         
         public IntChecker isNegative() {
             for (int j = 0; j < this.i.length; ++j) {
                 final int i = this.i[j];
                 if (i > 0) {
                     throw new IllegalArgumentException(i + " is > 0");
                 }
             }
             return this;
         }
     }
     
     
     
     public static class StringChecker extends AbstractChecker {
         
         private final String s;
         
         
         private StringChecker(String s) {
             this.s = s;
         }
         
         
         
         public StringChecker notNull() {
             if (this.s == null) {
                 throw new NullPointerException();
             }
             return this;
         }
         
         
         
         public StringChecker notEmpty() {
             if (this.s.isEmpty()) {
                 throw new IllegalArgumentException("string is empty");
             }
             return this;
         }
         
         
         
         public StringChecker equal(Object o) {
             if (!this.s.equals(o)) {
                 throw new IllegalArgumentException(this.s + " does not equal " + o);
             }
             return this;
         }
     }
     
     
     
     public static ObjectChecker objects(Object...o) {
         return new ObjectChecker(o);
     }
     
     
     
     public static <T> CollectionChecker<T> collection(Collection<T> c) {
         return new CollectionChecker<>(c);
     }
 
     
     
    public static IntChecker integer(int... i) {
         return new IntChecker(i);
     }
     
     
     
     public static StringChecker string(String s) {
         return new StringChecker(s);
     }
 }
