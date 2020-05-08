 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.model;
 
 import org.ref_send.promise.eventual.Loop;
 
 /**
  * A persistent application model.
  * <p>
  * This class gets its name from the Model in the Model-View-Control (MVC)
  * pattern. An application should be composed of separate classes that fall into
  * only one of these categories. Objects forming the application's model should
  * be stored in the {@link Model} to make them persistent. Objects in the other
  * categories should be transient and so reconstructed each time the application
  * is revived from its persistent state. Following this convention reduces the
  * number of classes with a persistent representation that MUST be supported
  * across application upgrade. When designing classes for your model, take care
  * to limit their complexity and plan for upgrade.
  * </p>
  */
 public abstract class
 Model {
 
     /**
     * indicates a {@linkplain #enter transaction} may modify existing state
      */
     static public final boolean change = false;
 
     /**
     * indicates a {@linkplain #enter transaction} will only query existing state
      */
     static public final boolean extend = true;
 
     /**
     * Schedules deferred {@linkplain #enter access} to this model.
      */
     public final Loop<Service> service;
 
     /**
      * Constructs an instance.
      * @param service   {@link #service}
      */
     protected
     Model(final Loop<Service> service) {
         this.service = service;
     }
 
     /**
      * Processes a transaction within this model.
      * <p>
      * The implementation MUST ensure only one transaction is active in the
      * model at any time. An invocation from another thread MUST block until the
      * model becomes available. A recursive invocation from the same thread MUST
      * cause an error.
      * </p>
      * <p>
     * If {@linkplain Transaction#run invocation} of the <code>body</code>
     * causes an {@link Error}, all modifications to objects in the model MUST
     * be discarded, and the {@link Error} allowed to propagate up the call
     * chain. For subsequent transactions, it MUST be as if the aborted
     * transaction was never attempted.
      * </p>
      * <p>
      * The implementation MUST NOT rely on the <code>extend</code> argument
      * accurately describing the behavior of the <code>body</code> argument.
      * If {@link #extend} is specified, the implementation MUST check that no
      * existing state was modified. If state was modified, the transaction MUST
      * be aborted, in the same manner described previously.
     * {@linkplain Root#store Storing} a new {@link Root root} binding is
     * considered a modification.
      * </p>
      * <p>
      * The <code>body</code> MUST NOT retain references to any of the objects
      * in the model beyond completion of the transaction. The model
      * implementation can rely on the <code>body</code> being well-behaved in
      * this respect. An identifier for an object in the model may be retained
     * across transactions by either {@linkplain Root#store storing}, or
     * {@linkplain Root#export exporting} it.
      * </p>
      * <p>
      * If invocation of this method returns without producing an {@link Error},
      * all modifications to objects in the model MUST be committed. Only if the
     * current transaction commits will the {@linkplain Root#effect enqueued}
     * {@link Effect}s be {@linkplain Transaction#run executed}; otherwise, the
      * implementation MUST discarded them. The effects MUST be executed in the
      * same order as they were enqueued. Effects from a subsequent transaction
      * MUST NOT be executed until all effects from the current transaction have
      * been executed. An {@link Effect} MUST NOT access objects in the model,
      * but may schedule additional effects.
      * </p>
      * @param <R> <code>body</code>'s return type
      * @param extend either {@link #change} or {@link #extend}
      * @param body transaction body
      * @return <code>body</code>'s return
      * @throws Exception any exception thrown from the <code>body</code>
      * @throws Error any problem in processing the transaction
      */
     public abstract <R> R
     enter(boolean extend, Transaction<R> body) throws Exception, Error;
 }
