 package utils;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import play.Logger;
 
 public class PerfLogger {
 
 	private static Map<String, Long> timers = new HashMap<String, Long>();
 
 	private static Set<String> pathsToLog = new HashSet<String>();
 
 	static {
 		pathsToLog.add("ValueOccurrence.save");
 		pathsToLog.add("events.saveEventList");
 		pathsToLog.add("events.saveEventList");
 		pathsToLog.add("events.sendToDatabase");
 		pathsToLog.add("Run.calcStateAndStatus");
 		pathsToLog.add("Run.save");
 		pathsToLog.add("TaskOccurrence.calcStateAndStatus");
 		pathsToLog.add("TaskOccurrence.save");
		pathsToLog.add("Timeline.getTasks");
 		pathsToLog.add("Value.save");
 		pathsToLog.add("ValueOccurrence.save");
 		pathsToLog.add("ValueOccurrence.updateAndSave");
 		pathsToLog.add("Example");
 	}
 
 	public static void log(String path, int number) {
 		if (!pathsToLog.contains(path)) {
 
 			long now = System.currentTimeMillis();
 
 			if ((number == 1) || (timers.get(path) == null)) {
 				Logger.debug(path + " " + number);
 			} else {
 				Logger.debug(path + " " + number + " : " + (now - timers.get(path)));
 			}
 
 			timers.put(path, now);
 		}
 	}
 }
