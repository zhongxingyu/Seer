 
 
 package es.igosoftware.io;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import es.igosoftware.util.GAssert;
 import es.igosoftware.util.GHolder;
 import es.igosoftware.util.GLogger;
 import es.igosoftware.util.GStringUtils;
 import es.igosoftware.util.GUtils;
 
 
 public class GHttpLoader
          implements
             ILoader {
 
    private static final GLogger LOGGER                         = GLogger.instance();
 
    private static final String  RENDERING_CACHE_DIRECTORY_NAME = "http-cache";
    private static final File    RENDERING_CACHE_DIRECTORY      = new File(RENDERING_CACHE_DIRECTORY_NAME);
 
    static {
       if (!RENDERING_CACHE_DIRECTORY.exists()) {
          RENDERING_CACHE_DIRECTORY.mkdirs();
       }
    }
 
 
    private class Task {
       private final String           _fileName;
       private final int              _priority;
       private final ILoader.IHandler _handler;
 
       private boolean                _isCanceled;
 
 
       private Task(final String fileName,
                    final int priority,
                    final ILoader.IHandler handler) {
          _fileName = fileName;
          _priority = priority;
          _handler = handler;
       }
 
 
       private void execute() {
          //         final File partFile = new File(_rootCacheDirectory, _fileName + ".part");
 
          final File directory = new File(_rootCacheDirectory, _fileName).getParentFile();
          synchronized (_rootCacheDirectory) {
             if (!directory.exists()) {
                if (!directory.mkdirs()) {
                   LOGGER.severe("can't create directory " + directory);
                }
             }
          }
 
          File partFile = null;
          try {
             partFile = File.createTempFile(_fileName, ".part", _rootCacheDirectory);
          }
          catch (final IOException e) {
             _handler.loadError(ILoader.ErrorType.NOT_FOUND, e);
             return;
          }
 
          partFile.deleteOnExit(); // just in case...
          //         if (partFile.exists()) {
          //            LOGGER.severe("partFile is present: " + partFile);
          //         }
 
 
          InputStream is = null;
          OutputStream out = null;
          try {
             final URL url = new URL(_rootURL, _fileName);
 
             is = new BufferedInputStream(url.openStream());
 
             out = new BufferedOutputStream(new FileOutputStream(partFile));
 
             GIOUtils.copy(is, out);
             is = null; // GIOUtils.copy() closes the in stream
 
             out.flush();
             out.close();
             out = null;
 
             synchronized (_rootCacheDirectory) {
                final File cacheFile = new File(_rootCacheDirectory, _fileName);
 
                //               if (cacheFile.exists()) {
                //                  if (cacheFile.length() != partFile.length()) {
                //                     LOGGER.severe("file " + cacheFile + " (" + cacheFile.length()
                //                                   + ") already exists and it's size it's different than " + partFile + " (" + partFile.length()
                //                                   + ")");
                //                     partFile.delete();
                //                     _handler.loadError(ILoader.ErrorType.CANT_READ, null);
                //                     return;
                //                  }
                //                  partFile.delete();
                //               }
                //               else {
                if (!partFile.renameTo(cacheFile)) {
                   LOGGER.severe("can't rename " + partFile + " to " + cacheFile);
                   _handler.loadError(ILoader.ErrorType.CANT_READ, null);
                   return;
                }
                //               }
 
                final long bytesLoaded = cacheFile.length();
                cacheMiss(bytesLoaded);
 
                if (!_isCanceled) {
                   try {
                      _handler.loaded(cacheFile, bytesLoaded, true);
                   }
                   catch (final ILoader.AbortLoading e) {
                      // do nothing, the file is already downloaded
                   }
                }
             }
          }
          catch (final MalformedURLException e) {
             _handler.loadError(ILoader.ErrorType.NOT_FOUND, e);
          }
          catch (final IOException e) {
             _handler.loadError(ILoader.ErrorType.CANT_READ, e);
          }
          finally {
             GIOUtils.gentlyClose(is);
             GIOUtils.gentlyClose(out);
          }
       }
 
 
       private void cancel() {
          _isCanceled = true;
       }
    }
 
 
    private void cacheHit() {
       synchronized (_statisticsMutex) {
          _loadCounter++;
          _loadCacheHits++;
 
          tryToShowStatistics();
       }
    }
 
 
    private void cacheMiss(final long bytesLoaded) {
       synchronized (_statisticsMutex) {
          _loadCounter++;
          _bytesDownloaded += bytesLoaded;
 
          tryToShowStatistics();
       }
    }
 
 
    private class Worker
             extends
                Thread
             implements
                UncaughtExceptionHandler {
 
 
       private Worker(final int id) {
          super("GHttpLoader " + _rootURL + ", worker #" + id);
          setDaemon(true);
          setPriority(MAX_PRIORITY);
          setUncaughtExceptionHandler(this);
       }
 
 
       @Override
       public void run() {
          try {
             while (true) {
                final Task task = _tasks.poll(1, TimeUnit.DAYS);
                if (task != null) {
                   if (task._isCanceled) {
                      continue; // ignored the canceled task
                   }
 
                   task.execute();
                }
             }
          }
          catch (final InterruptedException e) {
             // do nothing, just exit from run()
          }
       }
 
 
       @Override
       public void uncaughtException(final Thread thread,
                                     final Throwable e) {
         LOGGER.severe("Uncaught exception in thread " + thread, e);
       }
    }
 
 
    private final URL                         _rootURL;
    private final File                        _rootCacheDirectory;
    private final PriorityBlockingQueue<Task> _tasks;
    private final boolean                     _verbose;
 
 
    private final Object                      _statisticsMutex = new Object();
    private int                               _loadCounter     = 0;
    private int                               _loadCacheHits   = 0;
    private long                              _bytesDownloaded = 0;
 
 
    public GHttpLoader(final URL root,
                       final int workersCount,
                       final boolean verbose) {
       this(root, null, workersCount, verbose);
    }
 
 
    public GHttpLoader(final URL root,
                       final File cacheRootDirectory,
                       final int workersCount,
                       final boolean verbose) {
       GAssert.notNull(root, "root");
 
       if (!root.getProtocol().equals("http")) {
         throw new RuntimeException("Only http URLs are supported");
       }
 
 
       _rootURL = root;
       _verbose = verbose;
 
       if (cacheRootDirectory == null) {
          _rootCacheDirectory = new File(RENDERING_CACHE_DIRECTORY, getDirectoryName(_rootURL));
       }
       else {
          _rootCacheDirectory = new File(cacheRootDirectory, getDirectoryName(_rootURL));
       }
 
       //      System.out.println("root cache dir : " + _rootCacheDirectory);
       if (!_rootCacheDirectory.exists()) {
          if (!_rootCacheDirectory.mkdirs()) {
             throw new RuntimeException("Can't create cache directory: " + _rootCacheDirectory.getAbsolutePath());
          }
       }
 
       _tasks = new PriorityBlockingQueue<Task>(25, new Comparator<Task>() {
          @Override
          public int compare(final Task task1,
                             final Task task2) {
             final int priority1 = task1._priority;
             final int priority2 = task2._priority;
 
             if (priority1 < priority2) {
                return 1;
             }
             else if (priority1 > priority2) {
                return -1;
             }
             else {
                return 0;
             }
          }
       });
 
       initializeWorkers(workersCount);
    }
 
 
    private static String getDirectoryName(final URL url) {
       String result = url.toString().replace("http://", "");
 
       if (result.endsWith("/")) {
          result = result.substring(0, result.length() - 1);
       }
 
       result = result.replace("/", "_");
       result = result.replace(":", "_");
 
       return result;
    }
 
 
    private void initializeWorkers(final int workersCount) {
       for (int i = 0; i < workersCount; i++) {
          new Worker(i).start();
       }
    }
 
 
    @Override
    public void load(final String fileName,
                     final long bytesToLoad,
                     final int priority,
                     final ILoader.IHandler handler) {
       GAssert.notNull(fileName, "fileName");
       GAssert.notNull(handler, "handler");
 
       if (bytesToLoad >= 0) {
          throw new RuntimeException("fragment downloading is not supported");
       }
 
       final File file = new File(_rootCacheDirectory, fileName);
 
       if (file.exists()) {
          cacheHit();
 
          try {
             handler.loaded(file, file.length(), true);
          }
          catch (final ILoader.AbortLoading e) {
             // do nothing, the file is already on the cache and there are no download to cancel
          }
 
          return;
       }
 
 
       synchronized (_tasks) {
          for (final Task task : _tasks) {
             if (task._fileName.equals(fileName)) {
                throw new RuntimeException("Can't download the very same file at the same time (" + fileName + ")");
             }
          }
 
          _tasks.add(new Task(fileName, priority, handler));
       }
    }
 
 
    private void tryToShowStatistics() {
       if ((_loadCounter != 0) && ((_loadCounter % 50) == 0)) {
          //      if (_loadCounter != 0) {
          showStatistics();
       }
    }
 
 
    private void showStatistics() {
       if (!_verbose) {
          return;
       }
 
       final double hitsPercent = (double) _loadCacheHits / _loadCounter;
       LOGGER.info("HttpLoader \"" + _rootURL + "\": " + //
                   "loads=" + _loadCounter + ", " + //
                   "cache hits=" + _loadCacheHits + " (" + GStringUtils.formatPercent(hitsPercent) + "), " + //
                   "bytesDownloaded=" + _bytesDownloaded);
    }
 
 
    @Override
    public void cancelLoading(final String fileName) {
       synchronized (_tasks) {
          final Iterator<Task> iterator = _tasks.iterator();
          while (iterator.hasNext()) {
             final Task task = iterator.next();
             if (task._fileName.equals(fileName)) {
                task.cancel();
                iterator.remove();
             }
          }
       }
    }
 
 
    public static void main(final String[] args) throws MalformedURLException {
       final URL url = new URL("http://localhost/PANOS/cantabria1.jpg/");
 
       final GHttpLoader loader = new GHttpLoader(url, 2, true);
 
       final GHolder<Boolean> downloaded = new GHolder<Boolean>(false);
 
       final ILoader.IHandler handler = new ILoader.IHandler() {
          @Override
          public void loaded(final File file,
                             final long bytesLoaded,
                             final boolean completeLoaded) {
             //            if (!completeLoaded) {
             //               return;
             //            }
 
             System.out.println("loaded " + file + ", bytesLoaded=" + bytesLoaded + ", completeLoaded=" + completeLoaded);
             downloaded.set(true);
          }
 
 
          @Override
          public void loadError(final ILoader.ErrorType error,
                                final Throwable e) {
             System.out.println("Error=" + error + ", exception=" + e);
          }
       };
       loader.load("info.txt", -1, 1, handler);
 
       while (!downloaded.get()) {
          GUtils.delay(10);
       }
 
       for (int i = 0; i < 100; i++) {
          loader.load("info.txt", -1, 1, handler);
          loader.load("info.txt", -1, 1, handler);
          loader.load("info.txt", -1, 1, handler);
       }
 
    }
 
 
 }
