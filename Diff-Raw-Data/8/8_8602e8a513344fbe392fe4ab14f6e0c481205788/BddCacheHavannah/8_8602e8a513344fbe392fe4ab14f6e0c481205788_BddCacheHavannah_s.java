 package com.strategy.havannah.logic;
 
 import net.sf.javabdd.BDD;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.RemovalListener;
 import com.google.common.cache.RemovalNotification;
 import com.strategy.api.logic.BddCache;
 import com.strategy.api.logic.Position;
 import com.strategy.util.Preferences;
 import com.strategy.util.StoneColor;
 
 /**
  * @author Ralph Dürig
  */
 public class BddCacheHavannah implements BddCache {
 
 	// private Map<BddCacheIndex, BDD> cache;
 	private Cache<BddCacheIndex, BDD> cache;
 
 	// private BDDCacheStatus stats;
 
 	public BddCacheHavannah() {
 		// cache = Maps.newHashMap();
 		// stats = new BDDCacheStatus();
 		RemovalListener<BddCacheIndex, BDD> listener = new RemovalListener<BddCache.BddCacheIndex, BDD>() {
 			@Override
 			public void onRemoval(
 					RemovalNotification<BddCacheIndex, BDD> notification) {
 				notification.getValue().free();
 			}
 		};
 		// cache = CacheBuilder.newBuilder().recordStats().maximumSize(1)
 		// .removalListener(listener).build();
 		cache = CacheBuilder.newBuilder().recordStats()
 				.removalListener(listener).build();
 	}
 
 	@Override
 	public BDD restore(StoneColor color, Position p, Position q, int i) {
 		// stats.incrementRestores();
 		// if (cache.containsKey(BddCacheIndex.getIndex(color, p, q, i))) {
 		// return cache.get(BddCacheIndex.getIndex(color, p, q, i)).id();
 		// } else if (cache.containsKey(BddCacheIndex.getIndex(color, q, p, i)))
 		// {
 		// return cache.get(BddCacheIndex.getIndex(color, q, p, i)).id();
 		// } else {
 		// return null;
 		// }
 		if (cache.asMap().containsKey(BddCacheIndex.getIndex(color, p, q, i))) {
 			return cache.getIfPresent(BddCacheIndex.getIndex(color, p, q, i))
 					.id();
 		} else if (cache.asMap().containsKey(
 				BddCacheIndex.getIndex(color, q, p, i))) {
 			return cache.getIfPresent(BddCacheIndex.getIndex(color, q, p, i))
 					.id();
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public BDD store(StoneColor color, Position p, Position q, int i, BDD bdd) {
 		// stats.incrementStores();
 		if (null == bdd) {
 			return null;
 		}
 		cache.put(BddCacheIndex.getIndex(color, p, q, i), bdd.id());
 		return bdd.id();
 	}
 
 	@Override
 	public boolean isCached(StoneColor color, Position p, Position q, int i) {
 		return cache.asMap()
 				.containsKey(BddCacheIndex.getIndex(color, p, q, i))
 				|| cache.asMap().containsKey(
 						BddCacheIndex.getIndex(color, q, p, i));
 		// return false;
 	}
 
 	@Override
 	public void free() {
 		// Output.print(stats.toString(), BddCacheHavannah.class);
 		// for (BDD bdd : cache.values()) {
 		// bdd.free();
 		// }
 		// cache.clear();
		Preferences.getInstance().getOut().println(cache.stats());
 		cache.invalidateAll();
 	}
 
 }
