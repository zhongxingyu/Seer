 /*
  * Created On:  8-Nov-06 11:31:46 AM
  */
 package com.thinkparity.ophelia.browser.platform.action;
 
 import com.thinkparity.codebase.swing.SwingWorker;
 
 /**
  * <b>Title:</b>thinkParity Swing Worker<br>
  * <b>Description:</b>An abstraction of a long-running swing task for the
  * thinkParity browser.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public abstract class ThinkParitySwingWorker<T extends AbstractAction> extends SwingWorker {
 
     /** An <code>AbstractAction</code>. */
     protected final T action;
 
     /** The <code>ThinkParitySwingMonitor</code>. */
     protected ThinkParitySwingMonitor monitor;
 
     /**
      * Create ThinkParitySwingWorker.
      *
      */
     protected ThinkParitySwingWorker(final T action) {
         super();
         this.action = action;
     }
 
     /**
      * Obtain monitor.
      *
      * @return A ThinkParitySwingMonitor.
      */
     public ThinkParitySwingMonitor getMonitor() {
         return monitor;
     }
 
     /**
      * Determine if the monitor has been set.
      * 
      * @return <code>Boolean.True</code> if the monitor has been set.
      */
     public Boolean isSetMonitor() {
         return null != monitor;
     }
 
     /**
      * Set monitor.
      *
      * @param monitor
      *		A ThinkParitySwingMonitor.
      */
     public void setMonitor(final ThinkParitySwingMonitor monitor) {
         this.monitor = monitor;
     }
 
     /**
      * @see com.thinkparity.codebase.swing.SwingWorker#construct()
      * 
      */
 	@Override
 	public final Object construct() {
 		try {
 			return run();
 		} catch (final Throwable t) {
            action.logger.logError(t, "An error occured invoking {0}.",
                    action.getName());
 			getErrorHandler(t).run();
 			return null;
 		}
 	}
 
 	/**
 	 * Run the swing worker.
 	 * 
 	 */
 	public abstract Object run();
 
 	/**
 	 * Obtain an error handler.
 	 * 
 	 * @return A <code>Runnable</code>.
 	 */
 	public abstract Runnable getErrorHandler(final Throwable t);
 }
