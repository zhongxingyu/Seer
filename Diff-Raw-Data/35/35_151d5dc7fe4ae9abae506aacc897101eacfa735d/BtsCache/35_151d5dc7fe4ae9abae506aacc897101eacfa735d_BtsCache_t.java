 package carnero.netmap.model;
 
 import android.util.Log;
 import carnero.netmap.App;
 import carnero.netmap.common.Constants;
 import carnero.netmap.common.Util;
 import carnero.netmap.database.BtsDb;
 import carnero.netmap.listener.OnBtsCacheChangedListener;
 
 import java.util.*;
 
 public class BtsCache {
 
 	private static final HashMap<String, Bts> mCache = new HashMap<String, Bts>();
 	private static final ArrayList<OnBtsCacheChangedListener> mListeners = new ArrayList<OnBtsCacheChangedListener>();
 
 	public static void addListener(OnBtsCacheChangedListener listener) {
 		mListeners.add(listener);
 	}
 
 	public static void removeListener(OnBtsCacheChangedListener listener) {
 		if (mListeners.contains(listener)) {
 			mListeners.remove(listener);
 		}
 	}
 
 	public static void add(Bts bts) {
 		synchronized (mCache) {
 			mCache.put(Bts.getId(bts), bts);
 		}
 
 		Log.d(Constants.TAG, "New BTS added");
 		notifyListeners(bts);
 
 		BtsDb.save(App.getDatabase(), bts);
 	}
 
 	public static Bts update(String operator, int lac, int cid, int type) {
 		if (lac < 0 || cid < 0) {
 			return null;
 		}
 
 		final String id = Bts.getId(lac, cid);
 		Bts cached;
 		synchronized (mCache) {
 			cached = mCache.get(id);
 		}
 
 		if (cached == null) {
 			cached = new Bts(lac, cid, type);
 			add(cached);
 		} else {
 			if (Util.getNetworkLevel(cached.type) < Util.getNetworkLevel(type)) {
 				cached.type = type;
 
 				Log.d(Constants.TAG, "Changed BTS type to " + cached.type);
 				notifyListeners(cached);
 
 				BtsDb.updateType(App.getDatabase(), cached);
 			}
 		}
 
 		return cached;
 	}
 
 	public static Bts update(Bts bts) {
 		if (bts == null) {
 			return null;
 		}
 
 		final String id = Bts.getId(bts.lac, bts.cid);
 		Bts cached = mCache.get(id);
 
 		if (cached == null) {
 			add(bts);
 		} else {
 			Log.d(Constants.TAG, "type change: " + cached.type + " - " + bts.type);
 
 			if (Util.getNetworkLevel(cached.type) < Util.getNetworkLevel(bts.type)) {
 				cached.type = bts.type;
 
 				Log.d(Constants.TAG, "Changed BTS type to " + cached.type);
 				notifyListeners(cached);
 
 				BtsDb.updateType(App.getDatabase(), cached);
 			}
 
			boolean locationChanged = false;

			if (cached.location == null || (bts.locationNew != null && (bts.locationNew.latitude != cached.location.latitude || bts.locationNew.longitude != cached.location.longitude))) {
				cached.location = bts.locationNew;
				locationChanged = true;
			}

			if (cached.location == null || (bts.location != null && (bts.location.latitude != cached.location.latitude || bts.location.longitude != cached.location.longitude))) {
 				cached.location = bts.location;
				locationChanged = true;
			}

			if (locationChanged) {
				cached.locationNew = null;
 
 				Log.d(Constants.TAG, "Changed BTS location to " + cached.location.latitude + ", " + cached.location.longitude);
 				notifyListeners(cached);
 
 				BtsDb.updateLocation(App.getDatabase(), cached);
 			} else {
 				Log.d(Constants.TAG, "Changed BTS kept to " + cached.location.latitude + ", " + cached.location.longitude);
 			}
 		}
 
 		return cached;
 	}
 
 	public static List<Bts> getAll() {
 		final ArrayList<Bts> list = new ArrayList<Bts>();
 
 		synchronized (mCache) {
 			Set<Map.Entry<String, Bts>> entries = mCache.entrySet();
 			for (Map.Entry entry : entries) {
 				list.add((Bts) entry.getValue());
 			}
 		}
 
 		return list;
 	}
 
 	private static void notifyListeners(Bts bts) {
 		for (OnBtsCacheChangedListener listener : mListeners) {
 			listener.onBtsCacheChanged(bts);
 		}
 	}
 }
