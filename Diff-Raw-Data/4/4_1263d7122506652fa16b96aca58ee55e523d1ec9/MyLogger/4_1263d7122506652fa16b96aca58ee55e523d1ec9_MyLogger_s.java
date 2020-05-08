 package assignmentImplementation;
 
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectOutputStream;
 import java.io.RandomAccessFile;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import keyValueBaseInterfaces.LogRecord;
 import keyValueBaseInterfaces.Logger;
 
 public class MyLogger extends Thread implements Logger {
 
     private RandomAccessFile logFile;
     private ExecutorService executor;
     private boolean enabled;
     private int groupSize;
     private int groupTimeout; // ms
     private int groupWaiting;
     
     public MyLogger(RandomAccessFile logFile) {
         this.logFile = logFile;
         this.enabled = false;
         this.groupSize = 5;
         this.groupTimeout = 150;
         this.groupWaiting = 0;
     }
 
     public MyLogger(RandomAccessFile logFile, int groupSize, int groupTimeout) {
         this(logFile);
         this.enabled = false;
         this.groupSize = groupSize;
     }
 
 	@Override
 	public void run() {
 	    executor = Executors.newFixedThreadPool(1);
 	}
 	
 	public void enable() {
 	    this.enabled = true;
 	}
 
 	@Override
 	synchronized public Future<?> logRequest(final LogRecord record) {
 	    final MyLogger logger = this;
 		Future<?> f = executor.submit(new Callable<Void>() {
 		    public Void call() throws Exception {
 		        if (!enabled) {
 		            return null;
 		        }
 		        
		        logger.wait(groupTimeout);
 
 		        synchronized(logger) {
     		        ByteArrayOutputStream b = new ByteArrayOutputStream();
     		        ObjectOutputStream out = new ObjectOutputStream(b);
     		        try {
     		            out.writeObject(record);
     		            logFile.writeInt(b.size());
         		        logFile.write(b.toByteArray());
     		        } finally {
     		            out.close();
     		        }
 		            groupWaiting--;
 		        }
 		        return null;
 		    }
 		});
 		if (++groupWaiting == groupSize) {
 		    notifyAll();
 		}
 		return f;
 	}
 
 }
