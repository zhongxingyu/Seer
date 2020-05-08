 package jipdbs.core.model.dao.cached;
 
 import java.util.List;
 
 import jipdbs.core.cache.CacheFactory;
 import jipdbs.core.model.Penalty;
 import jipdbs.core.model.dao.PenaltyDAO;
 
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class PenaltyCachedDAO extends CachedDAO implements PenaltyDAO {
 
 	private PenaltyDAO impl;
 	
 	public PenaltyCachedDAO(PenaltyDAO impl) {
 		this.impl = impl;
 		this.initializeCache();
 	}
 	
 	@Override
 	public void save(Penalty penalty) {
 		impl.save(penalty);
 		cache.put("penalty-" + KeyFactory.keyToString(penalty.getKey()), penalty);
 	}
 
 	@Override
 	public Penalty get(Key key) throws EntityNotFoundException {
 		Penalty penalty = (Penalty) cache.get("penalty-" + KeyFactory.keyToString(key));
 		if (penalty == null) {
 			penalty = impl.get(key);
 			if (penalty != null) cache.put("penalty-" + KeyFactory.keyToString(penalty.getKey()), penalty);
 		}
 		return penalty;
 	}
 
 	@Override
 	public List<Penalty> findByPlayer(Key player) {
 		return this.findByPlayer(player, 1000);
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Penalty> findByPlayer(Key player, int limit) {
 		String cachekey = "penalty-player-" + KeyFactory.keyToString(player) + "l" + Integer.toString(limit);
 		List<Penalty> list = (List<Penalty>) cache.get(cachekey);
 		if (list != null) return list;
 		list = impl.findByPlayer(player, limit);
 		cache.put(cachekey, list);
 		return list;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Penalty> findByType(Long type, int offset, int limit, int[] count) {
 		String key = "penalty-type" + Long.toString(type) + Integer.toString(offset) + "L" + Integer.toString(limit);
 		List<Penalty> list = (List<Penalty>) getCachedList(key, count);
 		if (list != null) return list;
 		list = impl.findByType(type, offset, limit, count);
 		putCachedList(key, list, count);
 		return list;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Penalty> findByPlayerAndType(Key player, Long type, int offset, int limit, int[] count) {
 		String key = "penalty-ptype" + KeyFactory.keyToString(player) + Long.toString(type) + Integer.toString(offset) + "L" + Integer.toString(limit);
 		List<Penalty> list = (List<Penalty>) getCachedList(key, count);
 		if (list != null) return list;
 		list = impl.findByPlayerAndType(player, type, offset, limit, count);
 		putCachedList(key, list, count);
 		return list;
 	}
 
 	@Override
 	protected void initializeCache() {
 		this.cache = CacheFactory.getInstance().getCache("penalty");
 	}
 
 	@Override
 	public void save(List<Penalty> list) {
 		impl.save(list);
 	}
 
 	@Override
 	public void delete(Penalty penalty) {
		impl.delete(penalty);
 		this.cache.clear();
 		
 	}
 
 	@Override
 	public void delete(List<Penalty> list) {
		impl.delete(list);
 		this.cache.clear();
 	}
 
 	@Override
 	public List<Penalty> findByPlayerAndTypeAndActive(Key player, Long type) {
 		return impl.findByPlayerAndTypeAndActive(player, type);
 	}
 
 	@Override
 	public List<Penalty> findByPlayerAndType(Key player, Long type) {
 		return impl.findByPlayerAndType(player, type);
 	}
 
 }
