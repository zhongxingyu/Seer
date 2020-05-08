 package com.nesscomputing.lifecycle.junit;
 
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.inject.Module;
 import com.nesscomputing.lifecycle.DefaultLifecycle;
 import com.nesscomputing.lifecycle.Lifecycle;
 import com.nesscomputing.lifecycle.LifecycleStage;
 import com.nesscomputing.lifecycle.guice.AbstractLifecycleModule;
 
 /**
  * Test rule to run lifecycle start and stop for unit tests. Unfortunately, this must be run around a method (after \#0064Before and before \#0064After annotations), so
  * it can only be used in conjunction with the {@link LifecycleRunner}.
  */
 public final class LifecycleStatement implements TestRule
 {
    public static final LifecycleStatement defaultLifecycle()
     {
         return new LifecycleStatement(new DefaultLifecycle(), LifecycleStage.START_STAGE, LifecycleStage.STOP_STAGE);
     }
 
    public static final LifecycleStatement serviceDiscoveryLifecycle()
     {
         return new LifecycleStatement(new DefaultLifecycle(), LifecycleStage.ANNOUNCE_STAGE, LifecycleStage.STOP_STAGE);
     }
 
     private final Lifecycle lifecycle;
 
     private final LifecycleStage startStage;
     private final LifecycleStage stopStage;
 
     public LifecycleStatement(final Lifecycle lifecycle, final LifecycleStage startStage, final LifecycleStage stopStage)
     {
         this.lifecycle = lifecycle;
         this.startStage = startStage;
         this.stopStage = stopStage;
     }
 
     public Module getLifecycleModule()
     {
         return new AbstractLifecycleModule() {
             @Override
             public void configureLifecycle() {
                 bind(Lifecycle.class).toInstance(lifecycle);
             }
         };
     }
 
     @VisibleForTesting
     Lifecycle getLifecycle()
     {
         return lifecycle;
     }
 
     @Override
     public Statement apply(Statement base, final Description description)
     {
         return new LifecycleStatementWrapper(base);
     }
 
     public class LifecycleStatementWrapper extends Statement
     {
         private final Statement delegate;
 
         LifecycleStatementWrapper(final Statement delegate)
         {
             this.delegate = delegate;
         }
 
         @Override
         public void evaluate() throws Throwable {
             try {
                 lifecycle.executeTo(startStage);
                 delegate.evaluate();
             }
             finally {
                 lifecycle.executeTo(stopStage);
             }
         }
     }
 }
