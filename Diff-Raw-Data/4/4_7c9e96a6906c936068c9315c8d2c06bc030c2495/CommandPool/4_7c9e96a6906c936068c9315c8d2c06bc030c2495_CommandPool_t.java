 import org.apache.commons.logging.Log;
 
 import javax.sql.DataSource;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: Ivan Lyutov
  * Date: 11/25/12
  * Time: 1:49 AM
  */
 public class CommandPool extends ThreadPoolExecutor {
     private DataSource dataSource;
     private BufferedUpdater bufferedUpdater;
     private static final int updateBufferSize = 3000;
     private String updateFormat = "update commands set status='" + Command.Status.DONE +
                                   "' where id IN(%s) and status='" + Command.Status.IN_PROGRESS + "'";
     private boolean hasError = false;
     private Log logger;
 
     public CommandPool(DataSource dataSource) {
         super(0,
               Integer.MAX_VALUE,
               60L, TimeUnit.SECONDS,
               new LinkedBlockingQueue<Runnable>());
         this.dataSource = dataSource;
         this.bufferedUpdater = new BufferedUpdater(updateBufferSize, updateFormat, dataSource);
     }
 
     public CommandPool(DataSource dataSource, Log logger) {
         super(0,
                 Integer.MAX_VALUE,
                 60L, TimeUnit.SECONDS,
                 new LinkedBlockingQueue<Runnable>());
         this.dataSource = dataSource;
         this.logger = logger;
         this.bufferedUpdater = new BufferedUpdater(updateBufferSize, updateFormat, dataSource, logger);
     }
 
     public void run() {
        execute(getQueue().remove());
     }
 
     @Override
     protected void afterExecute(Runnable r, Throwable t) {
         bufferedUpdater.add(((Command) r).getId());
     }
 
     @Override
     protected void terminated() {
         bufferedUpdater.flushUpdate();
         hasError = bufferedUpdater.hasError();
         super.terminated();
     }
 
     public boolean hasError() {
         return hasError;
     }
 }
