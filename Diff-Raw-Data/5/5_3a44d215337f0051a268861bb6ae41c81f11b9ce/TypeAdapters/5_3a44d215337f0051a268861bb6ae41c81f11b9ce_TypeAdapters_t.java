 package btwmod.tickmonitor;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.minecraft.src.ChunkCoordIntPair;
 
 import btwmods.Util;
 import btwmods.measure.Average;
 import btwmods.stats.data.BasicStats;
 import btwmods.stats.data.BasicStatsComparator;
 import btwmods.stats.data.BasicStatsMap;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import com.google.gson.TypeAdapter;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 
 public class TypeAdapters {
 	
 	public static class ClassAdapter implements JsonSerializer<Class> {
 
 		@Override
 		public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
 			return context.serialize(src.getSimpleName());
 		}
 		
 	}
 	
 	public static class ChunkCoordIntPairAdapter implements JsonSerializer<ChunkCoordIntPair> {
 
 		@Override
 		public JsonElement serialize(ChunkCoordIntPair src, Type typeOfSrc, JsonSerializationContext context) {
 			return context.serialize(src.chunkXPos + "," + src.chunkZPos);
 		}
 	}
 	
 	public static class AverageTypeAdapter extends TypeAdapter<Average> {
 		
 		private final mod_TickMonitor monitor;
 		
 		public AverageTypeAdapter(mod_TickMonitor monitor) {
 			this.monitor = monitor;
 		}
 
 		@Override
 		public void write(JsonWriter out, Average average) throws IOException {
 			out.beginObject();
 			
 			out.name("average");
 			out.value(Util.DECIMAL_FORMAT_3.format(average.getAverage()));
 			
 			out.name("latest");
 			out.value(average.getLatest());
 			
 			if (monitor.includeHistory()) {
 				out.name("resolution");
 				out.value(average.getResolution());
 				
 				out.name("history");
 				out.beginArray();
 				if (average.getTotal() > 0 && average.getTick() >= 0) {
 					long[] history = average.getHistory();
 					int backIndex = average.getTick() - average.getResolution();
 					for (int i = average.getTick(); i >= 0 && i > backIndex; i--) {
 						out.value(history[i % average.getResolution()]);
 					}
 				}
 				out.endArray();
 			}
 			
 			out.endObject();
 		}
 
 		@Override
 		public Average read(JsonReader in) throws IOException {
 			return null;
 		}
 	}
 	
 	public static class BasicStatsMapAdapter<T> implements JsonSerializer<BasicStatsMap> {
 		
 		@Override
 		public JsonElement serialize(BasicStatsMap src, Type typeOfSrc, JsonSerializationContext context) {
 			JsonObject obj = new JsonObject();
 			BasicStatsMap outMap = src;
 			
 			obj.addProperty("total", src.size());
 			
 			double totalTickTime = 0;
 			long totalCount = 0;
 			List<T> topTickTime = new ArrayList<T>();
 			List<T> topCount = new ArrayList<T>();
 			
 			if (src.size() > 0) {
 				outMap = new BasicStatsMap();
 				
 				Set<Map.Entry<T, BasicStats>> statsSet = src.entrySet();
 				
 				// Get the entries as a list that can be sorted.
 				List<Map.Entry<T, BasicStats>> entries = new ArrayList<Map.Entry<T, BasicStats>>(statsSet);
 				
 				// Get the totals.
 				for (Map.Entry<T, BasicStats> entry : entries) {
 					totalTickTime += entry.getValue().tickTime.getAverage();
 					totalCount += entry.getValue().count;
 				}
 				
 				// Sort by tick time.
 				Collections.sort(entries, new BasicStatsComparator<T>(BasicStatsComparator.Stat.TICKTIME, true));
 				
 				// Add the top X chunks from the sorted list, and also mark them as chunks to include.
 				for (int i = 0; i < Math.min(entries.size(), mod_TickMonitor.getTopNumber()) - 1; i++) {
 					outMap.put(entries.get(i).getKey(), entries.get(i).getValue());
 					topTickTime.add(entries.get(i).getKey());
 				}
 				
				// Sort by count.
 				Collections.sort(entries, new BasicStatsComparator<T>(BasicStatsComparator.Stat.COUNT, true));
 				
 				// Add the top X chunks from the sorted list, and also mark them as chunks to include.
 				for (int i = 0; i < Math.min(entries.size(), mod_TickMonitor.getTopNumber()) - 1; i++) {
 					outMap.put(entries.get(i).getKey(), entries.get(i).getValue());
 					topCount.add(entries.get(i).getKey());
 				}
 			}
 			
 			obj.addProperty("totalTickTime", Util.DECIMAL_FORMAT_3.format(totalTickTime));
 			obj.addProperty("totalCount", totalCount);
			obj.add("topTickTime", context.serialize(topTickTime));
 			obj.add("topCount", context.serialize(topCount));
 			obj.add("lookup", context.serialize(outMap, Map.class));
 			
 			return obj;
 		}
 		
 	}
 }
