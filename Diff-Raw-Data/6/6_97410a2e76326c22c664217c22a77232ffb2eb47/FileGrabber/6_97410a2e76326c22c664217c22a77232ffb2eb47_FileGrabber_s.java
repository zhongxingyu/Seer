 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.grabber;
 
 import java.io.File;
 import java.util.Date;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.aclslib.service.ServiceException;
 import au.edu.uq.cmm.aclslib.service.SimpleService;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatus;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager.Status;
 
 /**
  * A FileGrabber service is registered as a listener for FileWatcher events
  * for a particular Facility.  The first event for a file generates WorkEntry 
  * object that is enqueued.  Subsequent events are added to the WorkEntry.
  * <p>
  * The FileGrabber's thread pulls WorkEntry objects from the queue and processed 
  * them as follows:
  * <ul>
  * <li>It waits until the file events stop arriving.</li>
  * <li>It optionally locks the file.</li>
  * <li>It captures the user / account from the ACLS status manager.</li>
  * <li>It copies the file to another directory.</li>
  * <li>It records the file's administrative metadata.</li>
  * <li>It releases the lock.</li>
  * </ul>
  * <p>
  * The functionality is mostly implemented in the {@link WorkEntry} class.
  * 
  * @author scrawley
  */
 public class FileGrabber extends AbstractFileGrabber implements SimpleService {
     static final Logger LOG = LoggerFactory.getLogger(FileGrabber.class);
     static final int DEFAULT_FILE_SETTLING_TIME = 2000;  // 2 seconds
     
     private final PausableQueue<Runnable> work = new PausableQueue<Runnable>();
     private final FacilityStatusManager statusManager;
     private File safeDirectory;
     private ThreadPoolExecutor executor;
     private final EntityManagerFactory entityManagerFactory;
     private final QueueManager queueManager;
     
     public FileGrabber(Paul services, Facility facility) {
         super(services, facility);
         statusManager = services.getFacilityStatusManager();
         queueManager = services.getQueueManager();
         FacilityStatus status = statusManager.getStatus(facility);
         status.setFileGrabber(this);
         safeDirectory = new File(
                 services.getConfiguration().getCaptureDirectory());
         if (!safeDirectory.exists() || !safeDirectory.isDirectory()) {
             throw new PaulException(
                     "The grabber's safe directory doesn't exist: " + 
                             safeDirectory);
         }
         entityManagerFactory = services.getEntityManagerFactory();
     }
 
     public File getSafeDirectory() {
         return safeDirectory;
     }
 
     public FacilityStatusManager getStatusManager() {
         return statusManager;
     }
     
     @Override
     public void shutdown() throws InterruptedException {
         setShuttingDown(true);
         executor.shutdown();
         if (executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS)) {
             LOG.info("FileGrabber's executor shut down");
         } else {
             LOG.warn("FileGrabber's executor didn't shut down cleanly");
         }
     }
 
     @Override
     public void startup() {
         setShuttingDown(false);
         FacilityStatus status = statusManager.getStatus(getFacility());
         Date catchupFrom = queueManager.getCatchupTimestamp(getFacility());
         Date hwm = status.getGrabberHWMTimestamp();
         LOG.debug("Catchup from = " + catchupFrom + ", hwm = " + hwm);
         if (hwm != null && (catchupFrom == null || hwm.getTime() == catchupFrom.getTime())) {
             executor = new ThreadPoolExecutor(0, 1, 999, TimeUnit.SECONDS, work);
             // We do "catchup" event generation with the executor paused, so that the worker
             // thread doesn't jump the gun and start processing work entries before all events
             // have been accumulated.
             // Note: datasets grabbed in catchup will contain all files whose names match,
             // irrespective of the file timestamps.  This is the best we can do in the circumstances.
             work.pause();
             LOG.info("Commencing catchup treewalk for " + status.getLocalDirectory());
             int count = analyseTree(status.getLocalDirectory(), catchupFrom.getTime(), Long.MAX_VALUE);
             LOG.info("Catchup treewalk found " + count + " files");
             // This ensures that the "caught-up" datasets get ingested in roughly the order
             // that the original files were saved rather than a seemingly random order, for
             // a better Mirage user experience ...
             reorderQueue(work);
             LOG.info("Resuming the worker thread");
             work.resume();
         } else {
             status.setStatus(Status.OFF);
             status.setMessage("Grabber HWM needs attention");
             throw new ServiceException(status.getMessage());
         }
     }
 
     @Override
     protected void enqueueWorkEntry(WorkEntry entry) {
        executor.execute(entry);
     }
 
     public EntityManager getEntityManager() {
         return entityManagerFactory.createEntityManager();
     }
 }
