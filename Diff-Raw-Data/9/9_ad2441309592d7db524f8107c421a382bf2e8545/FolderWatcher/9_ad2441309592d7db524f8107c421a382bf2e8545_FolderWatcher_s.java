 package org.backgitup;
 
 import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
 
 import java.io.IOException;
 import java.nio.file.ClosedWatchServiceException;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchEvent.Kind;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class FolderWatcher implements Runnable {
 
 	private static Logger LOGGER = Logger.getLogger(FolderWatcher.class.getName());
 	
 	private final WatchService fWatcher;
 	private final boolean fRecursive;
 	private final WatchKey fRootKey;
 	private final ExecutorService fThreadPool;
 
 	@SuppressWarnings("unchecked")
 	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
 		return (WatchEvent<T>) event;
 	}
 
 	/**
 	 * Creates a WatchService and registers the given directory
 	 */
 	FolderWatcher(Path dir, boolean recursive) throws IOException {
 		fThreadPool = Executors.newSingleThreadExecutor();
 
 		this.fWatcher = FileSystems.getDefault().newWatchService();
 		this.fRecursive = recursive;
 
 		if (recursive) {
 			fRootKey = WatcherServiceUtil.registerAll(fWatcher, dir);
 		} else {
 			fRootKey = WatcherServiceUtil.register(fWatcher, dir);
 		}
 	}
 
 	/**
 	 * Process all events for keys queued to the watcher
 	 */
 	@Override
 	public void run() {
 		for (;;) {
 
 			// wait for key to be signalled
 			WatchKey key;
 			try {
 				key = fWatcher.take();
 			} catch (InterruptedException x) {
 				fThreadPool.shutdown();
 				return;
 			} catch (ClosedWatchServiceException e) {
 				fThreadPool.shutdown();
 				return;
 			}
 
 			for (WatchEvent<?> event : key.pollEvents()) {
 				final Kind<?> kind = event.kind();
 				final WatchEvent<Path> castedEvent = cast(event);
 				final Path watchable = (Path) key.watchable();
 				final Path eventContext = castedEvent.context();
 
 				final Path target = watchable.resolve(eventContext);
 
 				EventResponseFactory responseFactory = new EventResponseFactory(target);
 				
 				if (kind == OVERFLOW) {
 					LOGGER.log(Level.WARNING, "Overfow event");
 					continue;
 				} else if (fRecursive && kind == ENTRY_CREATE) {
 					if (Files.isDirectory(target, NOFOLLOW_LINKS)) {
 						try {
 							WatcherServiceUtil.registerAll(fWatcher, target);
 						} catch (IOException e) {
 							LOGGER.log(Level.SEVERE, e.getMessage(), e);
 						}
 					}
 				}
 					
				EventResponse response = responseFactory.create((Kind<Path>) kind);
 				if (response != null) {
 					fThreadPool.execute(response);
 				} else {
 					LOGGER.log(Level.SEVERE, "Unsupported event received");
 				}
 				
 				key.reset();
 
 				if (!fRootKey.isValid()) {
 					fThreadPool.shutdown();
 					try {
 						fWatcher.close();
 					} catch (IOException e) {
 						LOGGER.log(Level.SEVERE, e.getMessage(), e);
 					}
 					return;
 				}
 			}
 		}
 	}
 }
