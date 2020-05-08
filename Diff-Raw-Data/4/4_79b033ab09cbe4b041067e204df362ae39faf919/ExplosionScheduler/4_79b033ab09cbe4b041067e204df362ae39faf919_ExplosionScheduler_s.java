 package me.asofold.bukkit.fattnt.scheduler;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.asofold.bukkit.fattnt.config.Path;
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 /**
  * 
  * @author mc_dev
  *
  */
 public final class ExplosionScheduler {
 	
 	private static final class ChunkPos{
 		private static final int p1 = 73856093;
 	    private static final int p2 = 19349663;
 //		private static final int p3 = 83492791;
 		final int x;
 		final int z;
 		final int hashCode;
 		public ChunkPos(final int x, final int z){
 			this.x = x;
 			this.z = z;
 			hashCode = p1*x ^ p2*z;
 		}
 		@Override
 		public final int hashCode(){
 			return hashCode;
 		}
 		@Override
 		public final boolean equals(final Object obj) {
 			if (obj instanceof ChunkPos){
 				final ChunkPos other = (ChunkPos) obj;
 				return x == other.x && z == other.z;
 			}
 			else return false;
 		}
 		
 	}
 	
	private int maxExplodeTotal = 50;
 	private int chunkSize = 16;
 	private int maxStoreTotal = 5000;
 	private int maxStoreChunk = 50; 
 	
 	private final Map<ChunkPos, List<ScheduledExplosion>> stored = new LinkedHashMap<ExplosionScheduler.ChunkPos, List<ScheduledExplosion>>(300);
 	
 	private int totalSize = 0;
 	
 	public void fromConfig(CompatConfig cfg){
 		maxExplodeTotal = cfg.getInt(Path.schedMaxExplodeTotal, 50);
 		chunkSize = cfg.getInt(Path.schedChunkSize, 16);
 		maxStoreTotal = cfg.getInt(Path.schedMaxStoreTotal, 50);
 		maxStoreChunk = cfg.getInt(Path.schedMaxStoreChunk, 10);
 	}
 	
 	/**
 	 * 
 	 * Simple getting algorithm, loop through all "chunks" and add one by one till limit reached. If many chunks are there, reorder the processed ones to the end, to prevent starvation.
 	 * @return
 	 */
 	public final List<ScheduledExplosion> getNextExplosions(){
 		final List<ScheduledExplosion> next = new LinkedList<ScheduledExplosion>();
 		if (stored.isEmpty()) return next;
 		int done = 0;
 		final List<ChunkPos> rem = new LinkedList<ChunkPos>();
 		final Set<ChunkPos> reSchedule = new LinkedHashSet<ChunkPos>();
 		boolean many = stored.size() > maxExplodeTotal;
 		while (!stored.isEmpty() && done < maxExplodeTotal){
 			for (final Entry<ChunkPos, List<ScheduledExplosion>> entry : stored.entrySet()){
 				final List<ScheduledExplosion> list = entry.getValue();
 				next.add(list.remove(0));
 				if (list.isEmpty()) rem.add(entry.getKey());
 				else if (many) reSchedule.add(entry.getKey());
 				done ++;
 				totalSize --;
 			}
 			for (final ChunkPos pos : rem){
 				stored.remove(pos);
 				if (many) reSchedule.remove(pos);
 			}
 			rem.clear();
 		}
 		if (stored.size() > maxExplodeTotal){
 			for (final ChunkPos pos : reSchedule){
 				stored.put(pos, stored.remove(pos));
 			}
 		}
 		return next;
 	}
 	
 	/**
 	 * Add the explosion, remove one if too many.
 	 * @param explosion
 	 */
 	public final void addExplosion(final ScheduledExplosion explosion){
 		if (totalSize >= maxStoreTotal) reduceStore();
 		final ChunkPos pos = new ChunkPos(Utils.floor(explosion.x / chunkSize), Utils.floor(explosion.z / chunkSize));
 		List<ScheduledExplosion> list = stored.get(pos);
 		if (list == null){
 			list = new LinkedList<ScheduledExplosion>();
 			stored.put(pos, list);
 		}
 		list.add(explosion);
 		totalSize ++;
 		if (list.size() > maxStoreChunk){
 			list.remove(0);
 			totalSize --;
 		}
 	}
 
 	/**
 	 * Attempt at first: 
 	 */
 	private final void reduceStore() {
 		boolean avOk = true;
 		boolean anyOk = false;
 		final int av;
 		final int sz  = stored.size();
 		if (totalSize == sz) av = 0;
 		else av = totalSize / stored.size();
 		final List<ChunkPos> rem = new LinkedList<ChunkPos>();
 		while (totalSize > maxExplodeTotal){
 			for (final Entry<ChunkPos, List<ScheduledExplosion>> entry : stored.entrySet()){
 				final List<ScheduledExplosion> list = entry.getValue();
 				final int lsz = list.size();
 				
 				if (lsz > maxStoreChunk);
 				else if (avOk && lsz <= av) continue;
 				else if (!anyOk && lsz == 1) continue;
 				
 				list.remove(0);
 				totalSize --;
 				if (lsz == 1) rem.add(entry.getKey());
 				if (totalSize <= maxExplodeTotal) break;
 			}
 			for (final ChunkPos pos : rem){
 				stored.remove(pos);
 				// ? consider sorting to end.
 			}
 			rem.clear();
 			if (totalSize > maxStoreTotal){
 				if (avOk) avOk = false;
 				else if (!anyOk) anyOk = true;
 			}
 		}
 	}
 	
 	public final boolean hasEntries(){
 		return totalSize > 0;
 	}
 	
 	public final void clear(){
 		// TODO: maybe do more looping to set members to null.
 		stored.clear();
 		totalSize = 0;
 	}
 }
