 package org.xmodel.log;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import org.xmodel.IModelObject;
 import org.xmodel.Xlate;
 
 /**
  * An implementation of Log.ISink that logs to rolling log files.
  */
 public final class FileSink implements ILogSink
 {
   public FileSink()
   {
     this( null, "", 0, (long)10e6, 0, (long)100e6);
   }
   
   /**
    * Create a FileSink with the specified properties.
    * @param logFolder The path to the folder where the log files will be written.
    * @param filePrefix The prefix for log files.
    * @param maxFileCount The maximum number of log files.
    * @param maxFileSize The maximum size of a log file.
    * @param maxFileAge The maximum age of a log file.
    * @param maxFolderSize The maximum size of the log folder.
    */
   public FileSink( String logFolder, String filePrefix, int maxFileCount, long maxFileSize, long maxFileAge, long maxFolderSize)
   {
     if ( logFolder == null)
     {
       logFolder = System.getProperty( "org.xmodel.log.folder");
       if ( logFolder == null) logFolder = "logs";
     }
     
     this.logFolder = new File( logFolder).getAbsoluteFile();
     this.filePrefix = filePrefix;
     this.maxFileCount = maxFileCount;
     this.maxFileSize = maxFileSize;
     this.maxFileAge = maxFileAge;
     this.maxFolderSize = maxFolderSize;
     this.queue = new LinkedBlockingQueue<String>();
     this.files = new ArrayList<String>();
     this.started = new AtomicBoolean( false);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.log.ILogSink#configure(org.xmodel.IModelObject)
    */
   @Override
   public void configure( IModelObject config)
   {
     logFolder = Xlate.get( config, "logFolder", Xlate.childGet( config, "logFolder", logFolder));
     filePrefix = Xlate.get( config, "filePrefix", Xlate.childGet( config, "filePrefix", filePrefix));
     maxFileCount = Xlate.get( config, "maxFileCount", Xlate.childGet( config, "maxFileCount", maxFileCount));
     maxFileSize = Xlate.get( config, "maxFileSize", Xlate.childGet( config, "maxFileSize", maxFileSize));
   }
 
   /**
    * Start the logging thread.
    */
   public synchronized void start()
   {
     init();
     
     if ( thread != null) stop();
     
     thread = new Thread( consumerRunnable, "File-Logging");
     thread.setDaemon( true);
     thread.start();
   }
   
   /**
    * Stop the logging thread.
    */
   public synchronized void stop()
   {
     thread.interrupt();
     thread = null;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String)
    */
   @Override
   public void log( Log log, int level, Object message)
   {
     if ( !started.getAndSet( true)) start();
     queue.offer( (message != null)? message.toString(): "null");
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.Throwable)
    */
   @Override
   public void log( Log log, int level, Throwable throwable)
   {
     if ( !started.getAndSet( true)) start();
     queue.offer( throwable.toString());
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.log.Log.ISink#log(org.xmodel.log.Log, int, java.lang.String, java.lang.Throwable)
    */
   @Override
   public void log( Log log, int level, Object message, Throwable throwable)
   {
     if ( !started.getAndSet( true)) start();
     queue.offer( ((message != null)? message: "null") + "\n" + throwable);
   }
   
   /**
    * Queue processing loop.
    */
   private void queueLoop()
   {
     List<String> messages = new ArrayList<String>();
     try
     {
       roll();
       
       while( true)
       {
         messages.clear();
         messages.add( queue.take());
         queue.drainTo( messages);
 
         for( String message: messages)
         {
           message = message + "\n";
           byte[] bytes = message.getBytes();
           stream.write( bytes);
           
           currentFileSize += bytes.length;
           if ( currentFileSize > maxFileSize) roll();
         }
         
         stream.flush();
       }
     }
     catch( InterruptedException e)
     {
       Thread.interrupted();
       try
       {
         stream.write( "\n** Logging Thread Interrupted **\n".getBytes());
         stream.flush();
       }
       catch( Exception e2)
       {
         System.err.println( e.getMessage());
       }
     }
     catch( Exception e)
     {
       try
       {
         stream.write( "\n** Logging Thread Caught Exception **\n".getBytes());
         stream.write( e.getMessage().getBytes());
         stream.flush();
       }
       catch( Exception e2)
       {
         System.err.println( e.getMessage());
       }
     }
   }
   
   /**
    * Create a new log file.
    */
   private void roll() throws IOException
   {
     if ( stream != null)
     {
       currentFileSize = 0;
       stream.close();
       stream = null;
       
       compress();
       enforceLimits();
     }
 
     if ( !logFolder.exists()) logFolder.mkdirs();
     
     String name = String.format( "%s%s_%s.log", filePrefix, Integer.toString( ++counter, 36).toUpperCase(), dateFormat.format( new Date()));
     stream = new FileOutputStream( new File( logFolder, name));
     files.add( name);
   }
   
   /**
    * Initialize by loading the current log files and creating the first log.
    */
   private void init()
   {
     if ( logFolder.exists())
     {
       FilenameFilter filter = new FilenameFilter() {
         @Override public boolean accept( File folder, String name)
         {
           return name.startsWith( filePrefix) && fileRegex.matcher( name.substring( filePrefix.length())).matches();
         }
       };
 
       counter = -1;
       for( String name: logFolder.list( filter))
       {
         files.add( name);
 
         Matcher matcher = runRegex.matcher( name);
         if ( matcher.find( filePrefix.length()))
          counter = Integer.parseInt( matcher.group( 1), 36);
       }
       
       Collections.sort( files, lastModifiedComparator);
       
       compress();
     }
   }
 
   /**
    * Compress the specified log file.
    * @param file The log file.
    */
   private void compress()
   {
     for( int i=0; i<files.size(); i++)
     {
       String name = files.get( i);
       String compressedName = name + ".zip";
       if ( name.endsWith( ".zip")) continue;
       
       File file = new File( logFolder, name);
       try
       {
         FileInputStream in = new FileInputStream( file);
         BufferedInputStream fin = new BufferedInputStream( in);
         File zipFile = new File( logFolder, compressedName);
         FileOutputStream fout = new FileOutputStream( zipFile);
         ZipOutputStream out = new ZipOutputStream( fout);
         
         ZipEntry entry = new ZipEntry( file.getName());
         entry.setTime( file.lastModified());
         out.putNextEntry( entry);
         
         byte[] buffer = new byte[ 4096];
         int read = fin.read( buffer);
         while( read >= 0)
         {
           out.write( buffer, 0, read);
           read = fin.read( buffer);
         }
     
         out.closeEntry();
         out.flush();
         out.close();
         fin.close();
         in.close();
         
         entry = null; out = null; fout = null; zipFile = null; fin = null; out = null; in = null;
         System.gc();
         
         files.set( i, compressedName);
         file.delete();
       }
       catch( Exception e)
       {
         SLog.exception( this, e);
       }
     }
   }
   
   /**
    * Enforce the maximum file age, and maximum log folder size limits.
    */
   private void enforceLimits()
   {
     long now = System.currentTimeMillis();
     long oldestFileDate = now - maxFileAge;
 
     long folderSize = getFolderSize();
     
     for( int i=0; i<files.size(); i++)
     {
       File file = new File( logFolder, files.get( i));
       long modified = file.lastModified();
 
       boolean delete = 
         (maxFileAge > 0 && oldestFileDate > modified) || 
         (maxFolderSize > 0 && maxFolderSize < folderSize) ||
         (maxFileCount > 0 && maxFileCount < files.size());
 
       if ( delete)
       {
         folderSize -= file.length();
         files.remove( i--);
         file.delete();
       }
     }
   }
   
   /**
    * @return Returns the size of the content of the log folder.
    */
   private long getFolderSize()
   {
     int folderSize = 0;
     for( int i=0; i<files.size(); i++)
     {
       File file = new File( logFolder, files.get( i));
       folderSize += file.length();
     }
     return folderSize;
   }
   
   private Runnable consumerRunnable = new Runnable() {
     public void run()
     {
       queueLoop();
     }
   };
   
   private final static Comparator<String> lastModifiedComparator = new Comparator<String>() {
     public int compare( String path1, String path2)
     {
       File file1 = new File( path1);
       File file2 = new File( path2);
       return (int)(file1.lastModified() - file2.lastModified());
     }
   };
 
   private final static DateFormat dateFormat = new SimpleDateFormat( "MMddyy_HHmmss");
   private final static Pattern fileRegex = Pattern.compile( ".*\\d{6}_\\d{6}\\.log(\\.zip)?$");
  private final static Pattern runRegex = Pattern.compile( "([0-9A-Z]++)_");
 
   private File logFolder;
   private String filePrefix;
   private int maxFileCount;
   private long maxFileSize;
   private long maxFolderSize;
   private long maxFileAge;
   private long currentFileSize;
   private int counter;
   private List<String> files;
   private OutputStream stream;
   private BlockingQueue<String> queue;
   private Thread thread;
   private AtomicBoolean started;
 }
