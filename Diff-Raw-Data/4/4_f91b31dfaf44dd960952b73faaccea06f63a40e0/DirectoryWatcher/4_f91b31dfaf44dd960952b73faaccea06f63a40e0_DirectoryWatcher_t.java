 package com.darylteo.nio;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.FileVisitor;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import com.sun.nio.file.SensitivityWatchEventModifier;
 
 /**
  * <p>
  * The DirectoryWatcher represents a watcher on a whole directory tree. Hence it
  * is unnecessary to recursively register watchers for every directory. It
  * detects changes and passes these events on to its subscribers
  * </p>
  * <a name="Registering"><h4>Registering</h4></a>
  * 
  * <p>
  * Creating a new DirectoryWatcher is done through an instance of
  * {@link DirectoryWatchService}.
  * </p>
  * 
  * <pre>
 * ThreadPoolDirectoryWatchService factory = new ThreadPoolDirectoryWatchService(); // or PollingDirectoryWatchService
  * DirectoryWatcher watcher = factory.newWatcher(&quot;&quot;);
  * </pre>
  * 
  * <a name="Subscribing"><h4>Subscribing</h4></a>
  * <p>
  * In order to respond to file system changes, a
  * {@link DirectoryWatcherSubscriber} should be provided to the
  * DirectoryWatcher. Subscribers can respond to 3 types of events:
  * </p>
  * 
  * <pre>
  * watcher.subscribe(new DirectoryWatcherSubscriber() {
  *   public void entryCreated(DirectoryWatcher watcher, Path file) {
  *     // ...
  *   }
  * 
  *   public void entryModified(DirectoryWatcher watcher, Path file) {
  *     // ...
  *   }
  * 
  *   public void entryDeleted(DirectoryWatcher watcher, Path file) {
  *     // ...
  *   }
  * });
  * </pre>
  * 
  * <p>
  * It is not mandatory to respond to all types of events. Simple override the
  * methods corresponding to the events you'd like to track. As a convenience,
  * you can also use a {@link DirectoryChangedSubscriber}, which will notify
  * you of all events.
  * </p>
  * 
  * <pre>
  * watcher.subscribe(new DirectoryChangedSubscriber() {
  *   public void directoryChanged(DirectoryWatcher watcher, Path path) {
  *     // ...
  *   }
  * });
  * </pre>
  * 
  * <a name="Filtering"><h4>Filtering</h4></a>
  * <p>
  * By default, the DirectoryWatcher will notify subscribers of every change in
  * every file under its base path. Often times, this is not desired. You can
  * fine tune which path changes you'd like to respond to through Ant style path
  * <a href="http://ant.apache.org/manual/dirtasks.html"
  * target="_blank">filtering</a>
  * </p>
  * <p>
  * All events must satisfy <strong>all</strong> "include" conditions, and
  * <strong>no</strong> "exclude" conditions, before being sent to subscribers.
  * You can use {@link DirectoryWatcher#shouldTrack} to check if a particular
  * path is being watched.
  * </p>
  * 
  * <p>
  * Some (inexhaustive) Examples:
  * </p>
  * 
  * <h5>Track a specific file in the base directory</h5>
  * 
  * <pre>
  * watcher.include(&quot;file&quot;);
  * 
  * watcher.shouldTrack(Paths.get(&quot;file&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;file.json&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file.json&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/bar/file.json&quot;)); // false
  * </pre>
  * 
  * <h5>Track all .json files in the base directory</h5>
  * 
  * <pre>
  * watcher.include(&quot;*.json&quot;);
  * 
  * watcher.shouldTrack(Paths.get(&quot;file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;file.json&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;foo/file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file.json&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/bar/file.json&quot;)); // false
  * </pre>
  * 
  * 
  * <h5>Track all .json files in all directories</h5>
  * 
  * <pre>
  * watcher.include(&quot;*&#42;/file.json&quot;);
  * 
  * watcher.shouldTrack(Paths.get(&quot;file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;file.json&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;foo/file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file.json&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;foo/bar/file.json&quot;)); // true
  * </pre>
  * 
  * <h5>Ignore changes in a directory and subdirectories</h5>
  * 
  * <pre>
  * watcher.exclude(&quot;foo/**&quot;);
  * 
  * watcher.shouldTrack(Paths.get(&quot;file&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;file.json&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;foo/file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file.json&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/bar/file.json&quot;)); // false
  * </pre>
  * 
  * <h5>Ignore changes within a directory but not subdirectories</h5>
  * 
  * <pre>
  * watcher.exclude(&quot;foo/*&quot;);
  * 
  * watcher.shouldTrack(Paths.get(&quot;file&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;file.json&quot;)); // true
  * watcher.shouldTrack(Paths.get(&quot;foo/file&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/file.json&quot;)); // false
  * watcher.shouldTrack(Paths.get(&quot;foo/bar/file.json&quot;)); // true
  * </pre>
  * 
  * <h4>Technical Notes</h4>
  * 
  * <h5>Directory Changes</h5>
  * <p>
  * Events from files are relatively straight forward. However, directories are a
  * little trickier, as actions taken on its contents may trigger events
  * propagating from the directory itself (depending on operating system). For
  * example, adding a file to a watched directory may cause 2 events (a File
  * Created event and a Directory Modified event).
  * 
  * Therefore, if you wish to completely ignore all changes within a directory,
  * it may also be necessary to exclude changes to the directory itself.
  * </p>
  * 
  * <pre>
  * watcher.exclude(&quot;foo&quot;);
  * watcher.exclude(&quot;foo/**&quot;);
  * </pre>
  * 
  * <h5>Tracking Deleted Entries</h5>
  * <p>
  * In order to make this library as performant as possible, there is no
  * functionality currently available to provide information as to whether the
  * deleted entry was originally a directory or a file. If this functionality is
  * required you should track the file structure yourself.
  * </p>
  * 
  * @author Daryl Teo <i.am@darylteo.com>
  */
 public class DirectoryWatcher {
   /* Properties */
   private Path path;
   private WatchService watcher;
 
   /* Subscriptions */
   private final List<DirectoryWatcherSubscriber> subscribers = new ArrayList<>();
 
   /* Used to filter files */
   private final List<Pattern> includes = new LinkedList<>();
   private final List<Pattern> excludes = new LinkedList<>();
 
   /* Used to determine watch status */
   private final Set<WatchKey> keys = new HashSet<>();
 
   /* Constructors */
   DirectoryWatcher(final WatchService watcher, final Path path) throws IOException {
     this.path = path.toAbsolutePath();
     this.watcher = watcher;
 
     Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
       @Override
       public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
         register(dir, watcher);
         return FileVisitResult.CONTINUE;
       }
     });
   }
 
   /**
    * @return the base of the directory tree being watched.
    */
   public Path getPath() {
     return this.path;
   }
 
   /* WatchService */
   private void register(Path path, final WatchService watcher) throws IOException {
     path = path.toAbsolutePath();
 
     keys.add(path.register(
       watcher,
       new WatchEvent.Kind<?>[] {
         StandardWatchEventKinds.ENTRY_CREATE,
         StandardWatchEventKinds.ENTRY_DELETE,
         StandardWatchEventKinds.ENTRY_MODIFY
       },
       new WatchEvent.Modifier[] { SensitivityWatchEventModifier.HIGH }
       ));
   }
 
   private void deregister(WatchKey key) {
     keys.remove(key);
   }
 
   /* Subscriptions */
   List<DirectoryWatcherSubscriber> getSubscribers() {
     return this.subscribers;
   }
 
   public void subscribe(DirectoryWatcherSubscriber subscriber) {
     subscribers.add(subscriber);
   }
 
   public void unsubscribe(DirectoryWatcherSubscriber subscriber) {
     subscribers.remove(subscriber);
   }
 
   /* Filters */
   public void include(String filter) {
     includes.add(compileFilter(filter));
   }
 
   public void exclude(String filter) {
     excludes.add(compileFilter(filter));
   }
 
   private Pattern compileFilter(String filter) {
     if (filter.endsWith("/") || filter.endsWith("\\")) {
       filter = filter + "**";
     }
 
     String[] subs = filter.split("[/|\\\\]");
     StringBuilder pattern = new StringBuilder("^");
     boolean appendDelimiter = false;
 
     for (String sub : subs) {
       if (appendDelimiter) {
         pattern.append(File.separator);
       } else {
         appendDelimiter = true;
       }
 
       if (sub.equals("**")) {
         pattern.append(".*?");
         appendDelimiter = false;
       } else {
         pattern.append(sub
           .replace(".", "\\.")
           .replace("?", ".")
           .replace("*", "[^" + File.separator + "]*?")
           );
       }
     }
 
     pattern.append("$");
     return Pattern.compile(pattern.toString());
   }
 
   /* Filter Checking */
   public boolean shouldTrack(Path path) {
     return shouldTrack(path.toString());
   }
 
   public boolean shouldTrack(String path) {
     return shouldInclude(path) && !shouldExclude(path);
   }
 
   private boolean shouldInclude(String path) {
     if (includes.isEmpty()) {
       return true;
     }
 
     for (Pattern pattern : includes) {
       if (pattern.matcher(path).matches()) {
         return true;
       }
     }
 
     return false;
   }
 
   private boolean shouldExclude(String path) {
     if (excludes.isEmpty()) {
       return false;
     }
 
     for (Pattern pattern : excludes) {
       if (pattern.matcher(path).matches()) {
         return true;
       }
     }
 
     return false;
   }
 
   /* Handlers */
   void handleCreateEvent(WatchKey key, Path path) {
     if (!isTrackingKey(key)) {
       return;
     }
 
     path = actualPath(key, path);
 
     // if a new dir is created we need to register it to our watcher
     // else inner events won't be tracked. In some cases, we may only
     // receive an event for the top level dir: any further nested dir
     // will not have any event as we haven't registered them. We'll
     // need to manually traverse and make sure we got them too.
     if (Files.isDirectory(path)) {
       FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
           register(dir, watcher);
 
           entryCreated(dir);
           return FileVisitResult.CONTINUE;
         }
 
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
           entryCreated(file);
 
           return FileVisitResult.CONTINUE;
         }
       };
 
       try {
         Files.walkFileTree(path, visitor);
       } catch (IOException e) {
       }
     }
   }
 
   void handleModifyEvent(WatchKey key, Path path) {
     if (!isTrackingKey(key)) {
       return;
     }
 
     entryModified(actualPath(key, path));
   }
 
   void handleDeleteEvent(WatchKey key, Path path) {
     if (!isTrackingKey(key)) {
       return;
     }
 
     entryDeleted(actualPath(key, path));
   }
 
   void handleKeyInvalid(WatchKey key) {
     if (!isTrackingKey(key)) {
       return;
     }
 
     deregister(key);
   }
 
   void entryCreated(Path entry) throws IOException {
     entry = relativePath(entry);
 
     if (!shouldTrack(entry)) {
       return;
     }
 
     for (DirectoryWatcherSubscriber sub : subscribers) {
       sub.entryCreated(DirectoryWatcher.this, entry);
     }
   }
 
   void entryModified(Path entry) {
     entry = relativePath(entry);
 
     if (!shouldTrack(entry)) {
       return;
     }
 
     for (DirectoryWatcherSubscriber sub : subscribers) {
       sub.entryModified(this, entry);
     }
   }
 
   void entryDeleted(Path entry) {
     entry = relativePath(entry);
 
     if (!shouldTrack(entry)) {
       return;
     }
 
     for (DirectoryWatcherSubscriber sub : subscribers) {
       sub.entryDeleted(this, entry);
     }
   }
 
   private boolean isTrackingKey(WatchKey key) {
     return keys.contains(key);
   }
 
   private Path actualPath(WatchKey key, Path path) {
     if (path == null) {
       path = Paths.get("");
     }
     return ((Path) key.watchable()).resolve(path);
   }
 
   private Path relativePath(Path path) {
     return this.path.relativize(path);
   }
 }
