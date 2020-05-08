 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.test;
 
 import static org.ref_send.promise.Eventual.ref;
 
 import java.io.Serializable;
 
 import org.ref_send.promise.Deferred;
 import org.ref_send.promise.Do;
 import org.ref_send.promise.Eventual;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Resolver;
 
 /**
  * Test condition operations.
  */
 public final class
 Logic {
    private Logic() {}
 
     /**
      * Creates a when block that compares against an expected value.
      * @param <T> referent type
      * @param expected  expected value
      */
     static public <T> Do<T,Promise<?>>
     was(final T expected) {
         class Was extends Do<T,Promise<?>> implements Serializable {
             static private final long serialVersionUID = 1L;
 
             public Promise<?>
             fulfill(final T value) throws Exception {
                 if (expected.equals(value)) { return ref(true); }
                 throw new Exception();
             }
         }
         return new Was();
     }
 
     /**
      * Fulfills a promise after each listed promise is fulfilled.
      * @param _ eventual operator
      * @param tests each promise
      * @return promise that is fulfilled if each of the listed promises is
      *         fulfilled; otherwise, the promise is rejected
      */
     static public Promise<?>
     join(final Eventual _, final Promise<?>... tests) {
         if (0 == tests.length) { return ref(true); }
         final Deferred<Object> answer = _.defer();
         final int[] todo = { tests.length };
         final Resolver<Object> resolver = answer.resolver;
         for (final Promise<?> test : tests) {
             class Join extends Do<Object,Void> implements Serializable {
                 static private final long serialVersionUID = 1L;
 
                 public Void
                 fulfill(final Object value) {
                     if (0 == --todo[0]) {
                         resolver.apply(true);
                     } else {
                         resolver.progress();
                     }
                     return null;
                 }
                 public Void
                 reject(final Exception e) {
                     resolver.reject(e);
                     return null;
                 }
             }
             _.when(test, new Join());
         }
         return answer.promise;
     }
 }
