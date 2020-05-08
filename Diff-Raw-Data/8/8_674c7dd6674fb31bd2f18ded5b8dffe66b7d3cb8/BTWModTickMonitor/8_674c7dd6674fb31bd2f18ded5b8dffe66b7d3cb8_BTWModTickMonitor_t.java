 package btwmod.tickmonitor;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ChunkCoordIntPair;
 import net.minecraft.src.CommandHandler;
 
 import btwmods.IMod;
 import btwmods.ModLoader;
 import btwmods.StatsAPI;
 import btwmods.io.Settings;
 import btwmods.measure.Average;
 import btwmods.network.CustomPacketEvent;
 import btwmods.network.INetworkListener;
 import btwmods.player.IInstanceListener;
 import btwmods.player.InstanceEvent;
 import btwmods.stats.IStatsListener;
 import btwmods.stats.StatsEvent;
 import btwmods.stats.data.BasicStats;
 import btwmods.stats.data.BasicStatsComparator;
 import btwmods.stats.data.BasicStatsMap;
 import btwmods.util.BasicFormatter;
 import btwmods.Util;
 
 public class BTWModTickMonitor implements IMod, IStatsListener, INetworkListener, IInstanceListener {
 
 	private static int topNumber = 20;
 	private static boolean includeHistory = false;
 	private static String publicLink = null;
 	private static File htmlFile = new File(new File("."), "stats.html");
 	private static File jsonFile = new File(new File("."), "stats.txt");
     private static long tooLongWarningTime = 1000;
     private static long reportingDelay = 1000;
 	
 	public static boolean includeHistory() {
 		return includeHistory;
 	}
 	
 	public static int getTopNumber() {
 		return topNumber;
 	}
 	
     private boolean isRunning = true; // TODO: make this false by default.
 	private long lastStatsTime = 0;
 	private int lastTickCounter = -1;
 	private Average statsActionTime = new Average(10);
 	private Average statsActionIOTime = new Average(10);
 	private Average ticksPerSecond = new Average(10);
 	
 	private Gson gson;
 	
 	public volatile boolean hideChunkCoords = true;
     
 	@Override
 	public String getName() {
 		return "Tick Monitor";
 	}
 
 	@Override
 	public void init(Settings settings) {
 		lastStatsTime = System.currentTimeMillis();
 		
 		gson = new GsonBuilder()
 			.registerTypeAdapter(Class.class, new TypeAdapters.ClassAdapter())
 			.registerTypeAdapter(ChunkCoordIntPair.class, new TypeAdapters.ChunkCoordIntPairAdapter())
 			.registerTypeAdapter(Average.class, new TypeAdapters.AverageTypeAdapter(this))
 			.registerTypeAdapter(BasicStatsMap.class, new TypeAdapters.BasicStatsMapAdapter())
 			.setPrettyPrinting()
 			.enableComplexMapKeySerialization()
 			.create();
 		
 		// Load settings
 		if (settings.hasKey("publiclink") && !(new File(settings.get("publiclink")).isDirectory())) {
 			publicLink = settings.get("publiclink");
 		}
 		if (settings.hasKey("htmlfile") && !(new File(settings.get("htmlfile")).isDirectory())) {
 			htmlFile = new File(settings.get("htmlfile"));
 		}
 		if (settings.hasKey("jsonfile") && !(new File(settings.get("jsonfile")).isDirectory())) {
 			jsonFile = new File(settings.get("jsonfile"));
 		}
 		if (settings.isBoolean("runonstartup")) {
 			isRunning = settings.getBoolean("runonstartup");
 		}
 		if (settings.isInt("reportingdelay")) {
 			reportingDelay = Math.max(50, settings.getInt("reportingdelay"));
 		}
 		if (settings.isLong("toolongwarningtime")) {
 			tooLongWarningTime = Math.min(500L, settings.getLong("toolongwarningtime"));
 		}
 		if (settings.isBoolean("hidechunkcoords")) {
 			hideChunkCoords = settings.getBoolean("hidechunkcoords");
 		}
 		if (settings.isBoolean("includehistory")) {
 			includeHistory = settings.getBoolean("includehistory");
 		}
 		
 		// Add the listener only if isRunning is true by default.
 		if (isRunning)
 			StatsAPI.addListener(this);
 		
 		((CommandHandler)MinecraftServer.getServer().getCommandManager()).registerCommand(new MonitorCommand(this));
 	}
 
 	@Override
 	public void unload() {
 		StatsAPI.removeListener(this);
 	}
 	
 	public void setIsRunning(boolean value) {
 		if (isRunning == value)
 			return;
 		
 		lastStatsTime = 0;
 		lastTickCounter = -1;
 		
 		if (isRunning)
 			StatsAPI.removeListener(this);
 		else
 			StatsAPI.addListener(this);
 		
 		isRunning = value;
 	}
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	/**
 	 * WARNING: This runs in a separate thread. Be very strict about what this accesses beyond the passed parameter. Do
 	 * not access any of the APIs, and be careful how class variables are used outside of statsAction().
 	 */
 	@Override
 	public void statsAction(StatsEvent event) {
 		long currentTime = System.currentTimeMillis();
 		long startNano = System.nanoTime();
 		boolean detailedMeasurementsEnabled = StatsAPI.detailedMeasurementsEnabled;
 		
 		if (lastStatsTime == 0) {
 			lastStatsTime = System.currentTimeMillis();
 		}
 		
 		else if (currentTime - lastStatsTime > reportingDelay) {
 			int numTicks = event.tickCounter - lastTickCounter;
 			
 			long timeElapsed = currentTime - lastStatsTime;
 			long timeSinceLastTick = currentTime - event.serverStats.lastTickEnd;
			ticksPerSecond.record((long)((double)numTicks / (double)timeElapsed * 100000D));
 
 			long jsonTime = System.nanoTime();
 			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("tickCounter", event.tickCounter);
 			jsonObj.addProperty("timeSinceLastTick", timeSinceLastTick);
			jsonObj.addProperty("ticksSinceLastStatsAction", numTicks);
 			jsonObj.addProperty("time", BasicFormatter.dateFormat.format(new Date(currentTime)));
 			jsonObj.addProperty("detailedMeasurements", StatsAPI.detailedMeasurementsEnabled);
 			
 			jsonObj.add("statsActionTime", gson.toJsonTree(statsActionTime));
 			jsonObj.add("statsActionIOTime", gson.toJsonTree(statsActionIOTime));
 			jsonObj.add("ticksPerSecondArray", gson.toJsonTree(ticksPerSecond));
 			
 			jsonObj.addProperty("jsonTime", "_____JSONTIME_____");
 			jsonObj.add("statsEvent", gson.toJsonTree(event));
 			String json = gson.toJson(jsonObj).replace("_____JSONTIME_____", Util.DECIMAL_FORMAT_3.format((jsonTime = System.nanoTime() - jsonTime) * 1.0E-6D));
 			
 			// Debugging loop to ramp up CPU usage by the thread.
 			//for (int i = 0; i < 20000; i++) new String(new char[10000]).replace('\0', 'a');
 			
 			StringBuilder html = new StringBuilder("<html><head><title>Minecraft Server Stats</title><meta http-equiv=\"refresh\" content=\"2\"></head><body><h1>Minecraft Server Stats</h1>");
 			
 			html.append("<table border=\"0\"><tbody>"); 
 			
 			html.append("<tr><th align=\"right\">Updated:<th><td>").append(BasicFormatter.dateFormat.format(new Date())).append("</td></tr>");
 			
 			if (event.serverStats.lastTickEnd >= 0) {
 				html.append("<tr><th align=\"right\">Last Tick:<th><td>").append(timeSinceLastTick >= 1000 ? "Over " + (timeSinceLastTick / 1000) + " seconds" : timeSinceLastTick + "ms").append(" ago.</td></tr>");
 			}
 			
 			html.append("<tr><th align=\"right\">Average StatsAPI Thread Time:<th><td>").append(Util.DECIMAL_FORMAT_3.format(event.serverStats.statsThreadTime.getAverage() * 1.0E-6D)).append(" ms</td></tr>");
 			html.append("<tr><th align=\"right\">Average StatsAPI Polled:<th><td>").append(Util.DECIMAL_FORMAT_3.format(event.serverStats.statsThreadQueueCount.getAverage())).append("</td></tr>");
 			
 			html.append("<tr><th align=\"right\">Average Tick Monitor Time:<th><td>");
 			if (statsActionTime.getTick() == 0)
 				html.append("...");
 			else
 				html.append(Util.DECIMAL_FORMAT_3.format(statsActionTime.getAverage() * 1.0E-6D)).append("ms (" + (int)(statsActionIOTime.getAverage() * 100 / statsActionTime.getAverage()) + "% IO)");
 			html.append("</td></tr>");
 			
 			html.append("<tr><td colspan=\"2\" style=\"height: 16px\"></td></tr>");
 			
 			html.append("<tr><th align=\"right\">Tick Num:<th><td>").append(event.tickCounter);
 			if (lastTickCounter >= 0) html.append(" (~").append(Util.DECIMAL_FORMAT_3.format(ticksPerSecond.getAverage() / 100D)).append("/sec)");
 			html.append("</td></tr>");
 			
 			html.append("<tr><th align=\"right\">Average Full Tick:<th><td>").append(Util.DECIMAL_FORMAT_3.format(event.serverStats.tickTime.getAverage() * 1.0E-6D)).append(" ms</td></tr>");
 			
 			html.append("<tr><td colspan=\"2\" style=\"height: 16px\"></td></tr>");
 			
 			double worldsTotal = 0;
 			for (int i = 0; i < event.worldStats.length; i++) {
 				
 				worldsTotal += event.worldStats[i].worldTickTime.getAverage();
 				
 				html.append("<tr><th align=\"right\">World ").append(i).append(" Averages:<th><td>")
 					.append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].worldTickTime.getAverage() * 1.0E-6D) + "ms");
 				
 				if (detailedMeasurementsEnabled)
 					html.append(" (")
 						.append("E: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].entities.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + M: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].mobSpawning.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + B: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].blockTick.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + W: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].weather.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + T: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].timeSync.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + CS: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].buildActiveChunkSet.getAverage() * 1.0E-6D)).append("ms")
 						.append(" + L: ").append(Util.DECIMAL_FORMAT_3.format(event.worldStats[i].checkPlayerLight.getAverage() * 1.0E-6D)).append("ms")
 						.append(")");
 				
 				html.append("</td></tr>");
 				
 				html.append("<tr><th align=\"right\">&nbsp;<th><td>")
 					.append("Chunks: ")
 					.append((int)event.worldStats[i].loadedChunks.getLatest()).append(" loaded, ")
 					.append(event.worldStats[i].id2ChunkMap).append(" cached, ")
 					.append((int)event.worldStats[i].droppedChunksSet.getAverage()).append(" dropped");
 				
 				if (detailedMeasurementsEnabled)
 					html.append(" | ").append((int)event.worldStats[i].measurementQueue.getAverage()).append(" measurements per tick");
 				
 				html.append("</td></tr>");
 			}
 
 			html.append("<tr><th align=\"right\">Worlds Total:<th><td>").append(Util.DECIMAL_FORMAT_3.format(worldsTotal * 1.0E-6D)).append(" ms (" + (int)(worldsTotal / event.serverStats.tickTime.getAverage() * 100) + "% of full tick)</td></tr>");
 			
 			html.append("<tr><td colspan=\"2\" style=\"height: 16px\"></td></tr>");
 			
 			html.append("<tr><th align=\"right\">Average Received Packet Count:<th><td>").append(Util.DECIMAL_FORMAT_3.format(event.serverStats.receivedPacketCount.getAverage())).append("</td></tr>");
 			html.append("<tr><th align=\"right\">Average Sent Packet Count:<th><td>").append(Util.DECIMAL_FORMAT_3.format(event.serverStats.sentPacketCount.getAverage())).append("</td></tr>");
 			
 			html.append("<tr><td colspan=\"2\" style=\"height: 16px\"></td></tr>");
 
 			html.append("<tr><th align=\"right\">Average Received Packet Size:<th><td>").append((int)event.serverStats.receivedPacketSize.getAverage()).append(" bytes</td></tr>");
 			html.append("<tr><th align=\"right\">Average Sent Packet Size:<th><td>").append((int)event.serverStats.sentPacketSize.getAverage()).append(" bytes</td></tr>");
 			
 			html.append("</tbody></table>");
 			
 			if (detailedMeasurementsEnabled) {
 				for (int i = 0; i < event.worldStats.length; i++) {
 					outputWorldDetails(event, html, i);
 				}
 			}
 	
 			html.append("</body></html>");
 
 			long startWriteTime = System.currentTimeMillis();
 			long startWriteNano = System.nanoTime();
 			
 			try {
 				FileWriter htmlWriter = new FileWriter(htmlFile);
 				htmlWriter.write(html.toString());
 				htmlWriter.close();
 			}
 			catch (Throwable e) {
 				ModLoader.outputError(getName() + " failed to write to " + htmlFile.getPath() + ": " + e.getMessage());
 			}
 			
 			try {
 				FileWriter jsonWriter = new FileWriter(jsonFile);
 				jsonWriter.write(json);
 				jsonWriter.close();
 			}
 			catch (Throwable e) {
 				ModLoader.outputError(getName() + " failed to write to " + jsonFile.getPath() + ": " + e.getMessage());
 			}
 			
 			long endNano = System.nanoTime();
 			long endTime = System.currentTimeMillis();
 			
 			if (System.currentTimeMillis() - currentTime > tooLongWarningTime)
 				ModLoader.outputError(getName() + " took " + (endTime - currentTime) + "ms (~" + ((endTime - startWriteTime) * 100 / (endTime - currentTime)) + "% disk IO) to process stats. Note: This will *not* slow the main Minecraft server thread.");
 
 			statsActionTime.record(endNano - startNano);
 			statsActionIOTime.record(endNano - startWriteNano);
 			lastTickCounter = event.tickCounter;
 			lastStatsTime = System.currentTimeMillis();
 		}
 	}
 	
 	private void outputWorldDetails(StatsEvent event, StringBuilder html, int world) {
 		
 		List<Map.Entry<ChunkCoordIntPair, BasicStats>> chunkEntries = new ArrayList<Map.Entry<ChunkCoordIntPair, BasicStats>>(event.worldStats[world].chunkStats.entrySet());
 
 		html.append("<h2>World ").append(world).append(" Top " + topNumber + ":</h2>");
 		
 		html.append("<table border=\"0\"><thead><tr><th>Chunks By Tick Time</th><th>Chunks By Entity Count</th><th>Entities By Tick Time</th><th>Entities By Count</th><th>TileEntities By Tick Time</th></tr></thead><tbody><tr><td valign=\"top\">");
 
 		{
 			Collections.sort(chunkEntries, new BasicStatsComparator<ChunkCoordIntPair>(BasicStatsComparator.Stat.TICKTIME, true));
 			html.append("<table border=\"0\"><thead><tr><th>Chunk</th><th>Tick Time</th><th>Entities</th></tr></thead><tbody>");
 			double chunksTotal = 0;
 			int entitiesTotal = 0;
 			int displayed = 0;
 			for (int i = 0; i < chunkEntries.size(); i++) {
 				if (chunkEntries.get(i).getValue().tickTime.getTotal() != 0 && displayed <= topNumber) {
 					displayed++;
 					html.append("<tr><td>");
 					
 					if (hideChunkCoords)
 						html.append("(hidden)");
 					else
 						html.append(chunkEntries.get(i).getKey().chunkXPos).append("/").append(chunkEntries.get(i).getKey().chunkZPos);
 					
 					html.append("</td><td>").append(Util.DECIMAL_FORMAT_3.format(chunkEntries.get(i).getValue().tickTime.getAverage() * 1.0E-6D))
 						.append(" ms</td><td>").append(chunkEntries.get(i).getValue().count)
 						.append("</td></tr>");
 				}
 
 				chunksTotal += chunkEntries.get(i).getValue().tickTime.getAverage();
 				entitiesTotal += chunkEntries.get(i).getValue().count;
 			}
 
 			html.append("<tr><td>Totals</td><td>").append(Util.DECIMAL_FORMAT_3.format(chunksTotal * 1.0E-6D)).append("ms</td><td>").append(entitiesTotal).append("</td></tr>");
 			html.append("</tbody></table>");
 		}
 		
 		html.append("</td><td valign=\"top\">");
 
 		{
 			Collections.sort(chunkEntries, new BasicStatsComparator<ChunkCoordIntPair>(BasicStatsComparator.Stat.COUNT, true));
 			html.append("<table border=\"0\"><thead><tr><th>Chunk</th><th>Tick Time</th><th>Entities</th></tr></thead><tbody>");
 			double chunksTotal = 0;
 			int displayed = 0;
 			int entitiesTotal = 0;
 			for (int i = 0; i < chunkEntries.size(); i++) {
 				if (chunkEntries.get(i).getValue().tickTime.getTotal() != 0 && displayed <= topNumber) {
 					displayed++;
 					html.append("<tr><td>");
 					
 					if (hideChunkCoords)
 						html.append("(hidden)");
 					else
 						html.append(chunkEntries.get(i).getKey().chunkXPos).append("/").append(chunkEntries.get(i).getKey().chunkZPos);
 					
 					html.append("</td><td>").append(Util.DECIMAL_FORMAT_3.format(chunkEntries.get(i).getValue().tickTime.getAverage() * 1.0E-6D))
 						.append(" ms</td><td>").append(chunkEntries.get(i).getValue().count)
 						.append("</td></tr>");
 				}
 
 				chunksTotal += chunkEntries.get(i).getValue().tickTime.getAverage();
 				entitiesTotal += chunkEntries.get(i).getValue().count;
 			}
 
 			html.append("<tr><td>Totals</td><td>").append(Util.DECIMAL_FORMAT_3.format(chunksTotal * 1.0E-6D)).append("ms</td><td>").append(entitiesTotal).append("</td></tr>");
 			html.append("</tbody></table>");
 		}
 
 		List<Map.Entry<Class, BasicStats>> entityEntries = new ArrayList<Map.Entry<Class, BasicStats>>(event.worldStats[world].entityStats.entrySet());
 		
 		html.append("</td><td valign=\"top\">");
 
 		{
 			Collections.sort(entityEntries, new BasicStatsComparator<Class>(BasicStatsComparator.Stat.TICKTIME, true));
 			html.append("<table border=\"0\"><thead><tr><th>Entity</th><th>Tick Time</th><th>Count</th></tr></thead><tbody>");
 			double entitiesTotal = 0;
 			int displayed = 0;
 			for (int i = 0; i < entityEntries.size(); i++) {
 				if (entityEntries.get(i).getValue().tickTime.getTotal() != 0 && displayed <= topNumber) {
 					displayed++;
 					html.append("<tr><td>").append(entityEntries.get(i).getKey().getSimpleName())
 							.append("</td><td>").append(Util.DECIMAL_FORMAT_3.format(entityEntries.get(i).getValue().tickTime.getAverage() * 1.0E-6D))
 							.append(" ms</td><td>").append(entityEntries.get(i).getValue().count)
 							.append("</td></tr>");
 				}
 
 				entitiesTotal += entityEntries.get(i).getValue().tickTime.getAverage();
 			}
 
 			html.append("<tr><td>Totals</td><td colspan=\"2\">").append(Util.DECIMAL_FORMAT_3.format(entitiesTotal * 1.0E-6D)).append("ms</td></tr>");
 			html.append("</tbody></table>");
 		}
 		
 		html.append("</td><td valign=\"top\">");
 
 		{
 			Collections.sort(entityEntries, new BasicStatsComparator<Class>(BasicStatsComparator.Stat.COUNT, true));
 			html.append("<table border=\"0\"><thead><tr><th>Entity</th><th>Tick Time</th><th>Count</th></tr></thead><tbody>");
 			double entitiesTotal = 0;
 			int displayed = 0;
 			for (int i = 0; i < entityEntries.size(); i++) {
 				if (entityEntries.get(i).getValue().tickTime.getTotal() != 0 && displayed <= topNumber) {
 					displayed++;
 					html.append("<tr><td>").append(entityEntries.get(i).getKey().getSimpleName())
 							.append("</td><td>").append(Util.DECIMAL_FORMAT_3.format(entityEntries.get(i).getValue().tickTime.getAverage() * 1.0E-6D))
 							.append(" ms</td><td>").append(entityEntries.get(i).getValue().count)
 							.append("</td></tr>");
 				}
 
 				entitiesTotal += entityEntries.get(i).getValue().tickTime.getAverage();
 			}
 
 			html.append("<tr><td>Totals</td><td colspan=\"2\">").append(Util.DECIMAL_FORMAT_3.format(entitiesTotal * 1.0E-6D)).append("ms</td></tr>");
 			html.append("</tbody></table>");
 		}
 
 		List<Map.Entry<Class, BasicStats>> tileEntityEntries = new ArrayList<Map.Entry<Class, BasicStats>>(event.worldStats[world].tileEntityStats.entrySet());
 		
 		html.append("</td><td valign=\"top\">");
 
 		{
 			Collections.sort(tileEntityEntries, new BasicStatsComparator<Class>(BasicStatsComparator.Stat.TICKTIME, true));
 			html.append("<table border=\"0\"><thead><tr><th>Tile Entity</th><th>Tick Time</th><th>Count</th></tr></thead><tbody>");
 			double entitiesTotal = 0;
 			int displayed = 0;
 			for (int i = 0; i < tileEntityEntries.size(); i++) {
 				if (tileEntityEntries.get(i).getValue().tickTime.getTotal() != 0 && displayed <= topNumber) {
 					displayed++;
 					html.append("<tr><td>").append(tileEntityEntries.get(i).getKey().getSimpleName())
 							.append("</td><td>").append(Util.DECIMAL_FORMAT_3.format(tileEntityEntries.get(i).getValue().tickTime.getAverage() * 1.0E-6D))
 							.append(" ms</td><td>").append(tileEntityEntries.get(i).getValue().count)
 							.append("</td></tr>");
 				}
 
 				entitiesTotal += tileEntityEntries.get(i).getValue().tickTime.getAverage();
 			}
 
 			html.append("<tr><td>Totals</td><td colspan=\"2\">").append(Util.DECIMAL_FORMAT_3.format(entitiesTotal * 1.0E-6D)).append("ms</td></tr>");
 			html.append("</tbody></table>");
 		}
 
 		html.append("</td></tr></tbody></table>");
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 
 	@Override
 	public void customPacketAction(CustomPacketEvent event) {
 		// TODO: remove debug throw
 		throw new IllegalArgumentException();
 	}
 
 	@Override
 	public void instanceAction(InstanceEvent event) {
 		if (publicLink != null && event.getType() == InstanceEvent.TYPE.LOGIN)
 			event.getPlayerInstance().sendChatToPlayer("Tick stats are available at " + publicLink);
 	}
 }
