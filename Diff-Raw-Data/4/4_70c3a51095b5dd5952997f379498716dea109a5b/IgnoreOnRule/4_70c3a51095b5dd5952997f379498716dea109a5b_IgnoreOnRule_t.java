 package org.whiskeysierra.primal;
 
import com.google.common.base.Throwables;
 import org.junit.Assume;
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 public final class IgnoreOnRule implements TestRule {
 
     private final Class<? extends Exception> type;
 
     public IgnoreOnRule(Class<? extends Exception> type) {
         // TODO notnullcheck
         this.type = type;
     }
 
     @Override
     public Statement apply(final Statement base, Description desc) {
         return new IgnoreStatement(base, type);
     }
 
     private static class IgnoreStatement extends Statement {
 
         private final Statement base;
         private Class<? extends Exception> type;
 
         public IgnoreStatement(Statement base, Class<? extends Exception> type) {
             this.base = base;
             this.type = type;
         }
 
         @Override
         public void evaluate() throws Throwable {
             try {
                 base.evaluate();
             } catch (Throwable e) {
                if (type.isInstance(Throwables.getRootCause(e))) {
                     Assume.assumeNoException(e);
                     return;
                 }
                 throw e;
             }
         }
 
     }
 
 }
