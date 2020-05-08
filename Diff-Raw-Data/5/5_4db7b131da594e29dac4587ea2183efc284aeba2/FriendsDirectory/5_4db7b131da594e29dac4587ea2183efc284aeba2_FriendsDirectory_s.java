 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.NotADirectoryException;
 import com.cafeform.iumfs.twitterfs.Account;
 import com.cafeform.iumfs.twitterfs.IumfsTwitterFactory;
 import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
 import java.util.Date;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.logging.Level;
 import twitter4j.PagableResponseList;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.User;
 
 /**
  * Directory entry which includes friends name as file. Entries under this
  * directory are dynamically created.
  */
 public class FriendsDirectory extends TwitterFsDirectory
 {
 
     private static final long UPDATE_INTERVAL = 3600000; // 1h 
     private static final long REQUEST_INTERVAL = 60000;  // 1m
 
     private Date lastUpdate = new Date(0);
     private final ScheduledExecutorService pool
             = Executors.newSingleThreadScheduledExecutor();
 
     public FriendsDirectory(Account account, String name)
     {
         super(account, name);
     }
 
     @Override
     public IumfsFile[] listFiles()
     {
         Date now = new Date();
 
         // Updater task
         final Runnable updater = new Runnable()
         {
             final Twitter twitter
                     = IumfsTwitterFactory.getInstance(getUsername());
 
             long cursor = -1;
 
             @Override
             public void run()
             {
                 logger.log(Level.FINER, "updater is called");
                 PagableResponseList<User> friendsList = null;
                 while (true)
                 {
                     logger.log(Level.FINER, "cursor = " + cursor);
                     try
                     {
                         friendsList = twitter.getFriendsList(
                                 getUsername(),
                                 cursor);
                         for (User user : friendsList)
                         {
                             addFile(new UserTimeLineFile(
                                     account,
                                     "/friends/" + user.getScreenName()));
/*
                            addFile(new TwitterFsDirectory(
                                    account,
                                    "/friends/" + user.getScreenName()));
        */
                         }
                         cursor = friendsList.getNextCursor();
                     } 
                     catch (TwitterException ex)
                     {
                         logger.log(Level.WARNING, "Unable to get friends list: "
                                 + ex.getMessage(), ex);
                     }
                     
                     logger.log(Level.FINER, "Got " + friendsList.size()
                             + " friends data. Next cursor = " + cursor);
 
                     // Wait REQUEST_INTERVAL to avoid exceeding 
                     // rate limit of twitter api.
                     // Also wait if this is first try and failed with exception.
                     if (0 != cursor)
                     {
                         try
                         {
                             Thread.sleep(REQUEST_INTERVAL);
                         } catch (InterruptedException ex){}
                     }                    
                     else
                     {
                         // Have gotton all friends data.
                         break;
                     }
                 } 
             }
         };
 
         if (now.getTime() - lastUpdate.getTime() > UPDATE_INTERVAL)
         {
             // If has passed UPDATE_INTERVAL since last update
             // update friends list again.
             pool.execute(updater);
             pool.shutdown();
             lastUpdate = new Date();
         } 
         else 
         {
             logger.log(Level.FINER, "updater not called");                                        
         }
         return super.listFiles();
     }
 
     @Override
     public void addFile(IumfsFile file) throws NotADirectoryException
     {
         logger.log(Level.FINEST, file.getName() + " is added.");                                        
         super.addFile(file);
     }
 }
