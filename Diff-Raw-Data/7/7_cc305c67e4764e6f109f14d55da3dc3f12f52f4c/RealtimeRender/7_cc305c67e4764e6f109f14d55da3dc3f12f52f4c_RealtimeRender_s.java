 package net.nabaal.majiir.realtimerender;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.JarURLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 import net.nabaal.majiir.realtimerender.commit.CommitProvider;
 import net.nabaal.majiir.realtimerender.commit.PluginCommitProvider;
 import net.nabaal.majiir.realtimerender.rendering.ChunkFilePattern;
 import net.nabaal.majiir.realtimerender.rendering.ChunkManager;
 import net.nabaal.majiir.realtimerender.rendering.FileChunkSnapshotProvider;
 import net.nabaal.majiir.realtimerender.rendering.NoOpChunkPreprocessor;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import flexjson.JSONSerializer;
 
 public class RealtimeRender extends JavaPlugin {
 	
 	private static Logger log;
 	
 	private final RealtimeRenderWorldListener worldListener = new RealtimeRenderWorldListener(this);
 	
 	private final PluginCommitProvider commitProvider = new PluginCommitProvider();
 	private final Lock renderLock = new ReentrantLock();
 	private ChunkManager chunkManager;
 	private ChunkSaveTask chunkSaveTask;
 	
 	private World world;
 	private int startDelay;
 	private int intervalDelay;
 	private int saveThreads;
 	private boolean renderLoaded;
 	private int zoomsIn;
 	private int zoomsOut;
 	
 	private boolean redoZooms = false;
 	private File options;
 	
	public RealtimeRender() {
		RealtimeRender.log = this.getLogger(); 
	}
	
 	@Override
 	public void onDisable() {	
 		log.info(String.format("%s: rendering loaded chunks...", this.getDescription().getName()));
 		
 		// TODO: Some way for tasks to not run simultaneously by accident, yet ensure the chunk queue is empty before shutting down.
 		new EnqueueAndRenderTask(this, true, true, renderLock).run();
 		
 		chunkSaveTask.stop();
 		
 		log.info(String.format("%s: disabled.", this.getDescription().getName()));
 	}
 
 	@Override
 	public void onEnable() {
 		
 		if (this.getDataFolder().mkdir()) {
 			log.info(String.format("%s: created plugin data directory.", this.getDescription().getName()));
 		}
 		
 		if (!new File(this.getDataFolder(), "config.yml").exists()) {
 			this.saveDefaultConfig();
 		}
 		
 		Configuration config = this.getConfig();
 		
 		world = this.getServer().getWorld(config.getString("world"));
 		startDelay = config.getInt("startDelay");
 		intervalDelay = config.getInt("intervalDelay");
 		saveThreads = config.getInt("saveThreads");
 		renderLoaded = config.getBoolean("renderLoaded");
 		zoomsIn = config.getInt("zoomsIn");
 		zoomsOut = config.getInt("zoomsOut");
 		
 		chunkManager = new ChunkManager(new NoOpChunkPreprocessor(), new FileChunkSnapshotProvider(new ChunkFilePattern(this.getDataFolder(), "world")));
 		chunkSaveTask = new ChunkSaveTask(chunkManager);
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		pm.registerEvents(worldListener, this);
 		
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new EnqueueAndRenderTask(this, false, renderLoaded, renderLock), startDelay * 20, intervalDelay * 20);
 		for (int i = 0; i < saveThreads; i++) {
 			this.getServer().getScheduler().scheduleAsyncDelayedTask(this, chunkSaveTask);
 		}
 		
 		int interval = config.getInt("discoverInterval");
 		
 		if (interval > 0) {
 			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new DiscoverTerrainTask(this, 4.0), 100, interval);
 		} else {
 			log.warning("RealtimeRender: discover interval must be greater than zero. (check config.yml)");
 		}
 		
 		getCommand("map").setExecutor(new CommandManager(this));
 		
 		options = new File(getDataFolder(), "options.json");
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(options);
 			new JSONSerializer().include("markerGroups", "markerGroups.markers").exclude("class", "*.class", "*.level").serialize(new MapOptions(zoomsIn * -1, zoomsOut, world, loadMarkerGroups(config.getConfigurationSection("markers"))), writer);
 			writer.close();
 		} catch (IOException e) {
 			log.warning(String.format("%s: failed to write options file!", this.getDescription().getName()));
 		}
 		
 		log.info(String.format("%s: enabled.", this.getDescription().getName()));
 	}
 	
 	public void registerCommitPlugin(CommitProvider provider) {
 		commitProvider.registerProvider(provider);
 		provider.commitFiles(Arrays.asList(new File[] { options }));
 	}
 	
 	public ChunkManager getChunkManager() {
 		return chunkManager;
 	}
 	
 	public World getWorld() {
 		return this.world;
 	}
 	
 	public int getZoomsIn() {
 		return zoomsIn;
 	}
 	
 	public int getZoomsOut() {
 		return zoomsOut;
 	}
 	
 	public boolean getRedoZooms() {
 		return redoZooms;
 	}
 	
 	public void setRedoZooms(boolean redoZooms) {
 		this.redoZooms = redoZooms;
 	}
 	
 	public void commit(Iterable<File> files) {
 		commitProvider.commitFiles(files);
 	}
 	
 	public static Logger getPluginLogger() {
 		return log;
 	}
 
 	private static ChunkSnapshot getChunkSnapshot(Chunk chunk) {
 		return chunk.getChunkSnapshot(true, true, true);
 	}
 	
 	public void enqueueChunk(Chunk chunk) {
 		chunkManager.enqueue(getChunkSnapshot(chunk));
 	}
 	
 	public void enqueueLoadedChunks() {
 		for (Chunk chunk : world.getLoadedChunks()) {
 			this.enqueueChunk(chunk);
 		}
 	}
 	
 	public void installRemote(final CommandSender sender) {
 		Thread thread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				List<File> files = new ArrayList<File>();
 				try {
 					files.addAll(copyJarResourcesRecursively(getDataFolder(), (JarURLConnection) getClass().getResource("/web").openConnection()));
 				} catch (IOException e) {
 					e.printStackTrace();
 					sender.sendMessage(ChatColor.RED + "Failed to install map viewer! See console for details.");
 					log.warning("RealtimeRender: failed to install map viewer! (jar error)");
 					return;
 				}
 				commitProvider.commitFiles(files);
 				sender.sendMessage(ChatColor.GREEN + "Map viewer installed.");
 			}
 		});
 		thread.start();
 	}
 
 	private List<File> copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) throws IOException {
 		List<File> files = new ArrayList<File>();
 	    JarFile jarFile = jarConnection.getJarFile();
 	    for (JarEntry entry : iterable(jarFile.entries())) {
 	        if (entry.getName().startsWith(jarConnection.getEntryName())) {
 	            String fileName = removeStart(entry.getName(), jarConnection.getEntryName());
 	            if (!entry.isDirectory()) {
 	                InputStream entryInputStream = null;
 	                try {
 	                    entryInputStream = jarFile.getInputStream(entry);
 	                    File file = new File(destination, fileName);
 	                    files.add(file);
 	                    copyStream(entryInputStream, new FileOutputStream(file));
 	                } finally {
 	                    entryInputStream.close();
 	                }
 	            } else {
 	            	new File(destination, fileName).mkdirs();
 	            }
 	        }
 	    }
 	    return files;
 	}
 	
 	private void copyStream(InputStream is, OutputStream os) throws IOException {
 		InputStream bis = new BufferedInputStream(is);
 		OutputStream bos = new BufferedOutputStream(os);
 		try {
 			byte[] b = new byte[512];
 			int len;
 			while ((len = bis.read(b)) > 0) {
 				bos.write(b, 0, len);
 			}
 		} finally {
 			bis.close();
 			bos.close();
 		}
 	}
 	
 	private <T> Iterable<T> iterable(final Enumeration<T> en) {
 		return new Iterable<T>() {
 			@Override
 			public Iterator<T> iterator() {
 				return new Iterator<T>() {
 					@Override
 					public boolean hasNext() {
 						return en.hasMoreElements();
 					}
 					@Override
 					public T next() {
 						return en.nextElement();
 					}
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 				};
 			}
 		};
 	}
 	
 	private String removeStart(String full, String start) {
 		if (full.startsWith(start)) {
 			return full.substring(start.length());
 		}
 		return full;
 	}
 
 	private List<MarkerGroup> loadMarkerGroups(ConfigurationSection config) {
 		List<MarkerGroup> groups = new ArrayList<MarkerGroup>();
 		if (config == null) {
 			return groups;
 		}
 		for (Entry<String, Object> entry : config.getValues(false).entrySet()) {
 			if (!(entry.getValue() instanceof ConfigurationSection)) {
 				log.warning("RealtimeRender: could not read config section for marker group \"" + entry.getKey() + "\"");
 				continue;
 			}
 			ConfigurationSection groupConfig = (ConfigurationSection) entry.getValue();
 			List<Marker> markers = new ArrayList<Marker>();
 			for (Entry<String, Object> m : groupConfig.getValues(false).entrySet()) {
 				Marker marker;
 				if (m.getValue() instanceof List<?>) {
 					marker = loadMarker(m.getKey(), (List<?>) m.getValue());
 				} else if (m.getValue() instanceof ConfigurationSection) {
 					marker = loadMarker((ConfigurationSection) m.getValue());
 				} else {
 					log.warning("RealtimeRender: could not read section for marker \"" + m.getKey() + "\"");
 					continue;
 				}
 				if (marker != null) {
 					markers.add(marker);
 				}
 			}
 			groups.add(new MarkerGroup(entry.getKey(), markers));
 		}
 		return groups;
 	}
 	
 	private Marker loadMarker(ConfigurationSection config) {
 		try {
 			return new Marker(config.getName(), new Coordinate(config.getInt("x"), config.getInt("z"), Coordinate.LEVEL_BLOCK));
 		} catch (Exception e) {
 			log.warning("RealtimeRender: could not read parameters for marker \"" + config.getName() + "\"");
 			return null;
 		}
 	}
 	
 	private Marker loadMarker(String label, List<?> xz) {
 		try {
 			if (xz.size() != 2) {
 				log.warning("RealtimeRender: too many coordinates for marker \"" + label + "\"");
 				return null;
 			}
 			return new Marker(label, new Coordinate((Integer) xz.get(0), (Integer) xz.get(1), Coordinate.LEVEL_BLOCK));
 		} catch (Exception e) {
 			log.warning("RealtimeRender: could not read coordinates for marker \"" + label + "\"");
 			return null;
 		}
 	}
 	
 	// TODO TODO
 	// Images with alpha channel (catch-all magenta for chunks?)
 	// Zoom 'in' (textured blocks?)
 	// Purge command
 	// Separate commit-to-file and commit-to-webserver timers
 	// Statistics (per-session)
 	// Height shading
 	// Depth shading
 	// Color map (incl data values/IDs)
 	// Biome shade adjustment (the colors are gaudy; make them more reasonable)
 	// Different leaf types
 	// (Semi-)transparent blocks
 	// Proper block detection (air, flowers, structures, torches, longgrass, flowing water)
 	// Flowing water whiteness/sparkle
 	// Light shading
 	
 	// Precomputed map:
 	// * blue = terrain height
 	// * green = tree canopy height  
 	// * red = light levels (with bit indicating 'source')
 	
 	// TODO: Test
 	// * Are only changed files committed?
 	// * Are files only accessed when necessary?
 	// * Any memory leaks?
 
 }
