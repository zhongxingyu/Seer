 package btwmod.livemap;
 
 import java.util.Queue;
 
 import net.minecraft.src.Chunk;
 import btwmods.ModLoader;
 
 public class RenderingThread implements Runnable {
 	
 	private final mod_LiveMap mod;
 	private final MapManager[] maps;
 	
 	private final Queue<Chunk> unloadedChunkQueue;
 	private final RegionIterator regionQueue;
 	
 	private volatile boolean isRunning = true;
 
 	public boolean isRunning() {
 		return isRunning;
 	}
 	
 	public RenderingThread(mod_LiveMap mod, MapManager[] maps, Queue<Chunk> unloadedChunkQueue, RegionIterator regionQueue) {
 		this.mod = mod;
 		this.maps = maps;
 		this.unloadedChunkQueue = unloadedChunkQueue;
 		this.regionQueue = regionQueue;
 	}
 
 	@Override
 	public void run() {
 		while (mod.isRenderingThread(this)) {
 			
 			Chunk chunk = null;
 			int count = 0;
 			int debugCount = 0;
 			long start = System.currentTimeMillis();
 			long nextSave = System.currentTimeMillis() + (1 * 1000);
 			long nextDebug = System.currentTimeMillis() + (10 * 1000);
 			while (mod.isRenderingThread(this) && (chunk = getNextChunk()) != null) {
 				renderChunk(chunk);
 				count++;
				
				if (mod.debugMessages)
					debugCount++;
 				
 				if (System.currentTimeMillis() > nextSave) {
 					save(false);
 					nextSave = System.currentTimeMillis() + (1 * 1000);
 				}
 				
 				if (mod.debugMessages && System.currentTimeMillis() > nextDebug) {
 					ModLoader.outputInfo(mod.getName() + " thread rendered " + debugCount + " chunks.");
 					nextDebug = System.currentTimeMillis() + (10 * 1000);
 					debugCount = 0;
 				}
 				
 				try {
 					Thread.sleep(10L);
 				} catch (InterruptedException e) {
 
 				}
 			}
 
 			save(true);
 
 			if (mod.debugMessages && count > 0)
 				ModLoader.outputInfo(mod.getName() + " thread rendered " + count + " total chunks in " + (System.currentTimeMillis() - start) + "ms.");
 			
 			try {
 				Thread.sleep(1000L);
 			} catch (InterruptedException e) {
 
 			}
 		}
 
 		isRunning = false;
 	}
 	
 	private Chunk getNextChunk() {
 		Chunk chunk = unloadedChunkQueue.poll();
 		if (chunk == null && regionQueue.hasNext()) {
 			chunk = regionQueue.next();
 		}
 		return chunk;
 	}
 	
 	private void renderChunk(Chunk chunk) {
 		for (int i = 0; i < maps.length; i++) {
 			maps[i].processChunk(chunk);
 		}
 	}
 	
 	protected void save(boolean clear) {
 		for (int i = 0; i < maps.length; i++) {
 			maps[i].save(clear);
 		}
 	}
 }
