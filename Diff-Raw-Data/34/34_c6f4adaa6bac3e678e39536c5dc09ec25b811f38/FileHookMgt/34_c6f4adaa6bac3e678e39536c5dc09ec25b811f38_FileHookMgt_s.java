 package fr.warlog.bus;
 
 import java.io.IOException;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.nio.file.StandardWatchEventKinds;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Get the hook on modified files
  * 
 * @author ABC-OBJECTIF\philippe.demanget
  */
 public class FileHookMgt {
 
 	public FileHookMgt() {
 	}
 
 	public static void trackFolder() throws IOException, InterruptedException {
 		// Path path = FileSystems.getDefault().getPath("/var/log/");
 		Path path = FileSystems.getDefault().getPath("/");
 		WatchService watcher = FileSystems.getDefault().newWatchService();
 		path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
 		WatchKey poll = watcher.take();
		System.out.println(poll.pollEvents().get(0));
 	}
 
 	public static void trackFile(String pathname,Map<String, Object> events ) throws IOException,
 			InterruptedException {
 		// Path path = FileSystems.getDefault().getPath("/var/log/");
 
 		Path file = FileSystems.getDefault().getPath(pathname);
 		String filename = file.getFileName().toString();
 		Path path = file.getParent();
 
 		while (true) {
 			WatchService watcher = FileSystems.getDefault().newWatchService();
 			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
 			WatchKey poll = watcher.take();
 			for (WatchEvent evt : poll.pollEvents()) {
 				if (((Path) evt.context()).toString().equals(filename)) {
					System.out.println("modified" + pathname);
 					synchronized(events){
 						FileNode node = new FileNode();
 						node.setPath(pathname);
 						node.setModified(new Date());
 						events.put(pathname, node);
 						events.notifyAll();
 					}
 					watcher.close();
 					return;
 				} else {
					System.out.println("skipping modification " + evt.context());
 					watcher.close();
 				}
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			trackFile("/var/log/test.log", new HashMap<String,Object>());
 		} catch (IOException | InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
