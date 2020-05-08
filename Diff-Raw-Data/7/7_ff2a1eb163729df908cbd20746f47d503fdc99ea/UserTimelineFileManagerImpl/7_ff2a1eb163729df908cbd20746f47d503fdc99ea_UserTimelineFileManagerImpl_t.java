 package com.cafeform.iumfs.twitterfs;
 
 import com.cafeform.iumfs.Files;
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.twitterfs.files.NormalTimelineFile;
 import com.cafeform.iumfs.twitterfs.files.UserTimelineFileImpl;
 import com.cafeform.iumfs.twitterfs.files.UserTimelineFileAdapter;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import twitter4j.TwitterException;
 
 /**
  * Manage user timelines for each account.
  * This can eliminate duplicate timeline file being created for followers 
  * directory and friends directory.
  */
 public class UserTimelineFileManagerImpl implements UserTimelineFileManager
 {
     static final Logger logger = Logger.getLogger(
             UserTimelineFileManagerImpl.class.getName());
     private ScheduledExecutorService userTimelineScheduler = null;
     private final Queue<NormalTimelineFile> userTimelineQueue = 
             new ReEnterableListQueue<>();
 
     /**
      * Add user time line which will be feching by dequeing in order.
      *
      * @param newFile
      */
     private void addUserTimeLine (NormalTimelineFile newFile)
     {
         // Start user timeline updater scheduler
         startScheduler();
         if(userTimelineQueue.contains(newFile))
         {
            logger.log(Level.WARNING, "UserTimeline " + newFile.getName() + 
                    " has already exists.");
         }
         else 
         {
              userTimelineQueue.offer(newFile);
         }
     }
 
     /**
      * Start separate thread which update all user/friends timelines
      * in fixed inteval.
      */
     private void startScheduler ()
     {
         if (null == userTimelineScheduler)
         {
             userTimelineScheduler
                     = Executors.newScheduledThreadPool(1);
             Runnable queueWatcher = new Runnable()
             {
                 @Override
                 public void run ()
                 {
                     NormalTimelineFile nextFile = null;
                     try
                     {
                         nextFile = userTimelineQueue.poll();
                         if (null != nextFile)
                         {
                             nextFile.getTimeline();
                             userTimelineQueue.offer(nextFile);    
                         }
                     } 
                     catch (TwitterException ex)
                     {
                        if (401 == ex.getErrorCode())
                        {
                            logger.log(Level.INFO, 
                                    nextFile.getAccount().getUsername() +
                                            "not authorized to get " + 
                                            nextFile.getName() + " timeline.");
                        }
                         logger.log(Level.INFO, "Failed to get " + 
                                 nextFile.getName() + " timeline by " +
                                 nextFile.getAccount().getUsername() + ".", ex);
                     }
                     catch (Exception ex)
                     {
                         logger.log(
                                 Level.INFO, "User timeline watcher thread " + 
                                         "got an Exception.", ex);
                     }
                 }
             };
             userTimelineScheduler.scheduleAtFixedRate(
                     queueWatcher,
                     0,
                     UserTimelineFileImpl.calculateInterval(),
                     TimeUnit.MILLISECONDS);
         }
     }
     
     /**
      * Find and return existing timeline by timeline name.(not pathname)
      * Null if not exist. 
      * 
      * @param name
      * @return 
      */
     private NormalTimelineFile lookupUserTimeline (String name) 
     {
        for(NormalTimelineFile file : userTimelineQueue)
        {
            if (file.getName().equals(name))
            {
                logger.log(Level.FINER, name + " found in existing list.");
                return file;
            }
        }
        logger.log(Level.FINER, name + " not found in existing list.");
         return null;
     }
 
     @Override
     public IumfsFile getTimelineFile (Account account, String pathname)
     {
         String username;
         if ("/user".equals(pathname)) {
             // For authenticated user
             username = account.getUsername();
         } else {
             // For account under followers and friends directory
             username  = Files.getNameFromPathName(pathname);
         }
 
         IumfsFile newFile;
         // Lookup existing UserTimeline by name.
         NormalTimelineFile userTimelineFile = lookupUserTimeline(username);
         if (null == userTimelineFile)
         {
              userTimelineFile = new UserTimelineFileImpl(account, pathname);
              addUserTimeLine(userTimelineFile);
         }
         
         // Create adopter instance which include actual timeline file.
         newFile = new UserTimelineFileAdapter(pathname, userTimelineFile);
         return newFile;
     }
 }
