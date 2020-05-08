 
 package Sirius.navigator;
 
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author martin.scholl@cismet.de
  */
 public final class DefaultNavigatorExceptionHandler implements Thread.UncaughtExceptionHandler {
 
     //~ Static fields/initializers ---------------------------------------------
 
    private static final transient Logger LOG = Logger.getLogger(DefaultNavigatorExceptionHandler.class);
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void uncaughtException(final Thread thread, final Throwable error) {
         LOG.error("uncaught exception in thread: " + thread, error);
     }
 }
