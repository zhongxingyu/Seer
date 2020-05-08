 package org.net;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.net.ClusterMapUtil.GetQuery;
 import org.net.ClusterMapUtil.PutQuery;
 import org.util.Util;
 import org.util.Util.Setter;
 import org.util.Util.Transformer;
 
 public class ClusterMap<K, V> {
 
 	private Cluster cluster;
 	private List<String> peers = new ArrayList<String>();
 	private Map<K, V> localMap = new HashMap<K, V>();
 
 	public ClusterMap(Cluster cluster) {
 		this.cluster = cluster;
 
 		synchronized (cluster) { // Avoid missed cluster events
 			peers.addAll(cluster.getActivePeers());
 			Collections.sort(peers);
 
 			cluster.addOnJoined(onJoined);
 			cluster.addOnLeft(onLeft);
 			cluster.setOnReceive(GetQuery.Request.class, onGet);
 			cluster.setOnReceive(PutQuery.Request.class, onPut);
 		}
 	}
 
 	private final Setter<String> onJoined = new Setter<String>() {
 		public Void perform(String peer) {
 			synchronized (ClusterMap.this) {
 				peers.add(peer);
 				Collections.sort(peers);
 				return null;
 			}
 		}
 	};
 
 	private final Setter<String> onLeft = new Setter<String>() {
 		public Void perform(String peer) {
 			synchronized (ClusterMap.this) {
 				peers.remove(peer);
 				Collections.sort(peers);
 				return null;
 			}
 		}
 	};
 
 	private final Transformer<GetQuery.Request, GetQuery.Response> onGet = new Transformer<GetQuery.Request, GetQuery.Response>() {
 		public GetQuery.Response perform(GetQuery.Request request) {
 			GetQuery.Response response = new GetQuery.Response();
 			response.value = localMap.get(request.key);
 			return response;
 		}
 	};
 
 	private final Transformer<PutQuery.Request, PutQuery.Response> onPut = new Transformer<PutQuery.Request, PutQuery.Response>() {
 		public PutQuery.Response perform(PutQuery.Request request) {
 			@SuppressWarnings("unchecked")
 			K key = (K) request.key;
 			@SuppressWarnings("unchecked")
 			V value = (V) request.value;
			PutQuery.Response response = new PutQuery.Response();
			response.value = localMap.put(key, value);
			return response;
 		}
 	};
 
 	public V get(K key) {
 		return getFromPeer(getPeerByHash(key), key);
 	}
 
 	public V set(K key, V value) {
 		return putToPeer(getPeerByHash(key), key, value);
 	}
 
 	private V getFromPeer(String peer, K key) {
 		GetQuery.Request request = new GetQuery.Request();
 		request.key = key;
		Serializable object = requestForResponse(peer, request);
		GetQuery.Response response = (GetQuery.Response) object;

		@SuppressWarnings("unchecked")
		V value = (V) response.value;
		return value;
 	}
 
 	private V putToPeer(String peer, K key, V value) {
 		PutQuery.Request request = new PutQuery.Request();
 		request.key = key;
 		request.value = value;
		Object object = requestForResponse(peer, request);
		PutQuery.Response response = (PutQuery.Response) object;
 
 		@SuppressWarnings("unchecked")
		V value1 = (V) response.value;
		return value1;
	}

	private Serializable requestForResponse(String peer, Serializable request) {
		return (Serializable) cluster.requestForResponse(peer, request);
 	}
 
 	private String getPeerByHash(K key) {
 		int hash = Util.hashCode(key);
 		return peers.get(hash % peers.size());
 	}
 
 }
