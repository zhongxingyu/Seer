 /*
  * Mar 18, 2006
  */
 package com.thinkparity.ophelia.browser.application.system;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.thinkparity.ophelia.browser.application.system.dialog.DisplayInfoFrame;
 import com.thinkparity.ophelia.browser.application.system.dialog.Notification;
 import com.thinkparity.ophelia.browser.application.system.dialog.NotifyFrame;
 import com.thinkparity.ophelia.browser.application.system.tray.Tray;
 import com.thinkparity.ophelia.browser.platform.Platform;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 class SystemApplicationImpl extends Thread {
 
 	/**
 	 * Flag indicating the current running state.
 	 * 
 	 * @see #run()
 	 */
 	Boolean running = Boolean.FALSE;
     
 	/** Flag indicating the DisplayInfo dialog has been requested. */
     private Boolean displayInfoRequested = Boolean.FALSE;
 
 	/** Queue of notifications not yet displayed. */
 	private final List<Notification> queue;
 
 	/** The system application. */
 	private final SystemApplication sysApp;
 
 	/** The system tray functionality. */
 	private Tray sysTray;
 
 	SystemApplicationImpl(final SystemApplication sysApp) {
		super("TPS-OpheliaUI-SystemApplication");
 		this.sysApp = sysApp;
         this.queue = new LinkedList<Notification>();
 	}
 
 	/**
 	 * @see java.lang.Thread#run()
 	 * 
 	 */
 	public void run() {
 		while(running) {
 			try {
                 synchronized(this) {
                     wait();
                 }
 			} catch(final InterruptedException ix) {}
             // do not try to process the queue if after waking up
             // we are no longer running
             if (running) {
     			try {
                     processDisplayInfo();
                     processQueue();
     			} catch (final RuntimeException rx) {
     				throw sysApp.translateError(rx);
     			}
             }
 		}
 	}
 
 	/**
 	 * @see java.lang.Thread#start()
 	 * 
 	 */
 	public synchronized void start() {
 		running = Boolean.TRUE;
 
 		sysTray = new Tray(sysApp, sysApp.getProfile());
         sysTray.install();
 
 		super.start();
 	}
 
     /**
      * Display the "display info" dialog.
      */
     void displayInfo() {
         synchronized (this) {
             displayInfoRequested = Boolean.TRUE;
             notifyAll();
         }
     }
 
 	/**
 	 * End the application.
 	 *
 	 */
 	void end() {
 		running = Boolean.FALSE;
 
         sysTray.unInstall();
         sysTray = null;
 
         NotifyFrame.close();
 
         synchronized (this) {
             notifyAll();
         }
 	}
 
 	/**
 	 * Notification that an artifact has been received.
 	 * 
 	 * @param artifact
 	 *            The artifact.
 	 */
 	void fireNotification(final Notification notification) {
 		synchronized (this) {
 			queue.add(notification);
 			notifyAll();
 		}
 	}
 
 	String getString(final String localKey) {
 		return sysApp.getString(localKey);
 	}
 
 	String getString(final String localKey, final Object[] arguments) {
 		return sysApp.getString(localKey, arguments);
 	}
 
 	/** Set the menu for the system tray. */
     void reloadConnectionStatus(final Platform.Connection cx) {
         synchronized(this) { sysTray.reloadConnection(cx); }
     }
 
     /**
 	 * Reset the number of queue items.
 	 *
 	 */
 	void resetQueue() {
 		synchronized(this) {
 			queue.clear();
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Obtain the total number of queued events.
 	 * 
 	 * @return The total number of queued artifacts and artifact versions.
 	 */
 	private Integer getQueueTotal() { return queue.size(); }
 
     /**
      * Process a request for the display info dialog.
      */
     private void processDisplayInfo() {
         if (displayInfoRequested) {
             synchronized (this) {
                 DisplayInfoFrame.display(sysApp);
                 displayInfoRequested = Boolean.FALSE;
             }
         }
     }
 
 	/**
 	 * Process pending queue events.
 	 *
 	 */
 	private void processQueue() {
         if (0 < getQueueTotal()) {
             synchronized (this) {
                 Notification notification;
                 for (final Iterator<Notification> i = queue.iterator();
                         i.hasNext();) {
                     notification = i.next();
                     NotifyFrame.display(notification);
                     i.remove();
                 }
             }
         }
 	}
 }
