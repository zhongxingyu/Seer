 /**
  *
  */
 package org.selfip.bkimmel.progress;
 
 /**
  * A <code>ProgressMonitor</code> that synchronizes all operations on itself
  * and its descendant <code>ProgressMonitor</code>s.  This class is a
  * decorator.
  * @author brad
  */
 public final class SynchronizedProgressMonitor implements ProgressMonitor {
 
 	/** The <code>ProgressMonitor</code> to decorate. */
 	private final ProgressMonitor monitor;
 
 	/**
 	 * The <code>Object</code> to synchronize against.  This will be the root
 	 * <code>ProgressMonitor</code>.
 	 */
 	private final Object syncObject;
 
 	/**
 	 * Creates a new <code>SynchronizedProgressMonitor</code>.
 	 * @param root The <code>ProgressMonitor</code> whose operations are to be
 	 * 		synchronized.
 	 */
 	public SynchronizedProgressMonitor(ProgressMonitor root) {
 		this(root, root);
 	}
 
 	/**
 	 * Creates a new <code>SynchronizedProgressMonitor</code>.
 	 * @param monitor The <code>ProgressMonitor</code> to decorate.
 	 * @param syncObject The <code>Object</code> on which to synchronize all
 	 * 		operations.
 	 */
 	private SynchronizedProgressMonitor(ProgressMonitor monitor, Object syncObject) {
 		this.monitor = monitor;
 		this.syncObject = syncObject;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#createChildProgressMonitor(java.lang.String)
 	 */
 	public ProgressMonitor createChildProgressMonitor(String title) {
		return new SynchronizedProgressMonitor(monitor.createChildProgressMonitor(title), syncObject);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#isCancelPending()
 	 */
 	public boolean isCancelPending() {
 		synchronized (syncObject) {
 			return monitor.isCancelPending();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyCancelled()
 	 */
 	public void notifyCancelled() {
 		synchronized (syncObject) {
 			monitor.notifyCancelled();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyComplete()
 	 */
 	public void notifyComplete() {
 		synchronized (syncObject) {
 			monitor.notifyComplete();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyIndeterminantProgress()
 	 */
 	public boolean notifyIndeterminantProgress() {
 		synchronized (syncObject) {
 			return monitor.notifyIndeterminantProgress();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyProgress(int, int)
 	 */
 	public boolean notifyProgress(int value, int maximum) {
 		synchronized (syncObject) {
 			return monitor.notifyProgress(value, maximum);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyProgress(double)
 	 */
 	public boolean notifyProgress(double progress) {
 		synchronized (syncObject) {
 			return monitor.notifyProgress(progress);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.reporting.ProgressMonitor#notifyStatusChanged(java.lang.String)
 	 */
 	public void notifyStatusChanged(String status) {
 		synchronized (syncObject) {
 			monitor.notifyStatusChanged(status);
 		}
 	}
 
 }
