 package ro.gagarin.utils;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 
 public class StatisticsContainer {
 
     private static ConcurrentHashMap<String, Statistic> statContainer = new ConcurrentHashMap<String, Statistic>(
 	    new HashMap<String, Statistic>());
 
     public static void add(Statistic statistic) {
 	statContainer.put(statistic.getName(), statistic);
     }
 
     public static List<Statistic> exportStatistics(String filter) {
 	Pattern pattern = null;
 	if (filter != null) {
 	    pattern = Pattern.compile(filter);
 	}
 	ArrayList<Statistic> list = new ArrayList<Statistic>();
 	for (Statistic stat : statContainer.values()) {
	    if (pattern == null) {
 		list.add(new Statistic(stat));
	    } else if (pattern.matcher(stat.getName()).matches()) {
		list.add(new Statistic(stat));
	    }
 	}
 	return list;
     }
 }
