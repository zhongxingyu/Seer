 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.bukkit.ChunkSnapshot;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.RealtimeRender;
 import net.nabaal.majiir.realtimerender.RenderChunkTask;
 import net.nabaal.majiir.realtimerender.image.ChunkRenderer;
 
 public class ChunkManager {
 	
 	private final BlockingQueue<ChunkSnapshot> incoming = new ArrayBlockingQueue<ChunkSnapshot>(200);
 	private final ExecutorService executor = Executors.newCachedThreadPool();
 	private final ReadWriteLock lock = new ReentrantReadWriteLock();
 	private final ChunkPreprocessor processor;
 	private final FileChunkSnapshotProvider provider;
 	private final Set<Coordinate> chunks = new HashSet<Coordinate>();
 	private final Set<Coordinate> tiles = new HashSet<Coordinate>();
 	
 	public ChunkManager(ChunkPreprocessor processor, FileChunkSnapshotProvider provider) {
 		this.processor = processor;
 		this.provider = provider;
 	}
 	
 	public Lock getLock() {
 		return lock.readLock();
 	}
 	
 	public void enqueue(ChunkSnapshot chunkSnapshot) {
 		try {
 			incoming.put(chunkSnapshot);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void doSaveChunk() {
 		lock.readLock().lock();
 		try {
			SerializableChunkSnapshot snapshot = processor.processChunk(incoming.take());
			provider.setSnapshot(snapshot);
 		} catch (InterruptedException e) {
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 	
 	public void startBatch() {
 		lock.writeLock().lock();
 		chunks.addAll(provider.getTiles());
 		for (Coordinate chunk : chunks) {
 			tiles.add(chunk.zoomOut(3));
 		}
 	}
 	
 	public void render(ChunkRenderer chunkRenderer) {
 		for (Coordinate tile : tiles) {
 			List<Future<?>> futures = new ArrayList<Future<?>>();
 			for (Coordinate chunk : chunks) {
 				if (chunk.zoomOut(3).equals(tile)) {
 					futures.add(executor.submit(new RenderChunkTask(chunkRenderer, provider.getSnapshot(chunk))));
 				}
 			}
 			for (Future<?> future : futures) {
 				try {
 					future.get();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 					System.out.println("Exception should be here! #1"); // TODO: Test
 				} catch (ExecutionException e) {
 					e.printStackTrace();
 					System.out.println("Exception should be here! #2");
 				}
 			}
 		}
 	}
 	
 	public void endBatch() {
 		tiles.clear();
 		for (Coordinate chunk : chunks) {
 			if (!provider.getPattern().getFile(chunk).delete()) {
 				RealtimeRender.getLogger().warning("RealtimeRender: could not delete file for chunk: " + chunk);
 			}
 		}
 		chunks.clear();
 		lock.writeLock().unlock();
 	}
 	
 }
