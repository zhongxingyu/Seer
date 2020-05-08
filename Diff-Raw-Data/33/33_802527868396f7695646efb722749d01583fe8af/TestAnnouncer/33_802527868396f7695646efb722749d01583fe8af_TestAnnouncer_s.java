 package net.praqma.util.test.junit;
 
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
import org.junit.runners.model.Statement;import java.lang.Override;import java.lang.Throwable;
 import java.util.logging.Logger;
 
 /**
  * @author cwolfgang
  */
 public class TestAnnouncer implements TestRule {
 
    private static Logger logger = Logger.getLogger( TestAnnouncer.class.getName() );
 
     @Override
     public Statement apply( final Statement base, final Description description ) {
 
         return new Statement() {
             @Override
             public void evaluate() throws Throwable {
                 logger.fine( "=====================================================================" );
                 logger.fine( "===== Test class  : " + description.getDisplayName() );
                 logger.fine( "===== Test method : " + description.getMethodName() );
                 logger.fine( "=====================================================================" );
                try {
                    base.evaluate();
                } finally {
                    // logger.fine( "===== Test method : " + description.getMethodName() );
                }
             }
         };
     }
 }
