 package cl.own.usi.service.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import cl.own.usi.cache.SortedCachedUser;
 import cl.own.usi.service.CachedScoreService;
 
 @Component
 public class CachedScoreServiceImpl implements CachedScoreService {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(CachedScoreServiceImpl.class);
 
 	private final static StringBuilder EMPTY_SB = new StringBuilder();
 
 	final SortedUsersList sortedUsersList = new SortedUsersList(100);
 
 	@Override
 	public StringBuilder getBefore(final String userId, final int limit) {
 
 		long starttime = System.currentTimeMillis();
 
 		List<SortedCachedUser> users = sortedUsersList.getBefore(userId, limit);
 
		if (users.isEmpty()) {
 			return EMPTY_SB;
 		} else {
 			StringBuilder sb = appendUsersScores(users);
 
 			long deltatime = System.currentTimeMillis() - starttime;
 			if (deltatime > 200L) {
 				LOGGER.info("getBefore for user {} took {} ms", userId,
 						deltatime);
 			}
 			return sb;
 		}
 	}
 
 	@Override
 	public StringBuilder getAfter(final String userId, final int limit) {
 
 		long starttime = System.currentTimeMillis();
 
 		List<SortedCachedUser> users = sortedUsersList.getAfter(userId, limit);
 
		if (users.isEmpty()) {
 			return EMPTY_SB;
 		} else {
 			StringBuilder sb = appendUsersScores(users);
 
 			long deltatime = System.currentTimeMillis() - starttime;
 			if (deltatime > 200L) {
 				LOGGER.info("getAfter for user {} took {} ms", userId,
 						deltatime);
 			}
 			return sb;
 		}
 	}
 
 	@Override
 	public void addUser(final String userId, final String lastname,
 			final String firstname, final String email, final int score) {
 		SortedCachedUser user = new SortedCachedUser(lastname, firstname,
 				email, score);
 		sortedUsersList.insert(userId, user);
 	}
 
 	@Override
 	public void flush() {
 		sortedUsersList.flush();
 	}
 
 	public static StringBuilder appendUsersScores(List<SortedCachedUser> users) {
 		StringBuilder sb = new StringBuilder();
 		StringBuilder scoresMails = new StringBuilder("\"mail\":[");
 		StringBuilder scoresScores = new StringBuilder("\"scores\":[");
 		StringBuilder scoresFirstName = new StringBuilder("\"firstname\":[");
 		StringBuilder scoresLastname = new StringBuilder("\"lastname\":[");
 		boolean first = true;
 		for (SortedCachedUser user : users) {
 			if (!first) {
 				scoresMails.append(",");
 			}
 			scoresMails.append("\"").append(user.getFullEmail()).append("\"");
 			if (!first) {
 				scoresScores.append(",");
 			}
 			scoresScores.append("\"").append(user.getScore()).append("\"");
 			if (!first) {
 				scoresFirstName.append(",");
 			}
 			scoresFirstName.append("\"").append(user.getFirstname())
 					.append("\"");
 			if (!first) {
 				scoresLastname.append(",");
 			}
 			scoresLastname.append("\"").append(user.getLastname()).append("\"");
 			first = false;
 		}
 		sb.append(scoresMails).append("],");
 		sb.append(scoresScores).append("],");
 		sb.append(scoresFirstName).append("],");
 		sb.append(scoresLastname).append("]");
 		return sb;
 	}
 
 	private static class DoubleLinkedNode<T extends Comparable<T>> {
 		private final T payload;
 		private DoubleLinkedNode<T> before = null;
 		private DoubleLinkedNode<T> after = null;
 
 		public DoubleLinkedNode(T payload) {
 			this.payload = payload;
 		}
 
 		public DoubleLinkedNode<T> getBefore() {
 			return before;
 		}
 
 		public DoubleLinkedNode<T> getAfter() {
 			return after;
 		}
 
 		public void insertBefore(DoubleLinkedNode<T> afterNode) {
 			DoubleLinkedNode<T> tmpBefore = afterNode.before;
 			after = afterNode;
 			afterNode.before = this;
 			if (tmpBefore != null) {
 				tmpBefore.after = this;
 				before = tmpBefore;
 			}
 		}
 
 		public void insertAfter(final DoubleLinkedNode<T> beforeNode) {
 			DoubleLinkedNode<T> tmpAfter = beforeNode.after;
 			before = beforeNode;
 			beforeNode.after = this;
 			if (tmpAfter != null) {
 				tmpAfter.before = this;
 				after = tmpAfter;
 			}
 		}
 
 		public T getPayload() {
 			return payload;
 		}
 
 		@Override
 		public String toString() {
 			return "Node (" + getPayload().toString() + ")";
 		}
 	}
 
 	private static class NodeComparator<T extends Comparable<T>> implements
 			Comparator<DoubleLinkedNode<T>> {
 		@Override
 		public int compare(DoubleLinkedNode<T> arg0, DoubleLinkedNode<T> arg1) {
 			return arg0.getPayload().compareTo(arg1.getPayload());
 		}
 	}
 
 	private static class SortedList<K, T extends Comparable<T>> {
 
 		private final ConcurrentMap<K, DoubleLinkedNode<T>> nodeMap = new ConcurrentHashMap<K, DoubleLinkedNode<T>>();
 		private final ConcurrentSkipListSet<DoubleLinkedNode<T>> sortedNodeList = new ConcurrentSkipListSet<DoubleLinkedNode<T>>(
 				new NodeComparator<T>());
 
 		DoubleLinkedNode<T> firstNode = null;
 		DoubleLinkedNode<T> lastNode = null;
 		int i = 0;
 		final int ratio;
 
 		public SortedList(int ratio) {
 			this.ratio = ratio;
 		}
 
 		public synchronized void insert(K key, T payload) {
 
 			if (!nodeMap.containsKey(key)) {
 				DoubleLinkedNode<T> newNode = new DoubleLinkedNode<T>(payload);
 				nodeMap.put(key, newNode);
 				if (i++ % ratio == 0) {
 					sortedNodeList.add(newNode);
 				}
 				if (firstNode == null || lastNode == null) {
 					firstNode = newNode;
 					lastNode = newNode;
 				} else {
 					DoubleLinkedNode<T> lowerNode = sortedNodeList
 							.lower(newNode);
 
 					if (lowerNode == null) {
 						// lower starting point not found.
 						// we iterate from firstNode.
 						lowerNode = firstNode;
 					}
 
 					do {
 						if (newNode.getPayload().compareTo(
 								lowerNode.getPayload()) < 0) {
 							newNode.insertBefore(lowerNode);
 							if (lowerNode == firstNode) {
 								firstNode = newNode;
 							}
 							break;
 						}
 						lowerNode = lowerNode.after;
 					} while (lowerNode != null);
 
 					if (lowerNode == null) {
 						newNode.insertAfter(lastNode);
 						lastNode = newNode;
 					}
 				}
 			}
 		}
 
 		public List<T> getAfter(final K key, final int limit) {
 			DoubleLinkedNode<T> node = nodeMap.get(key);
 			if (node != null) {
 
 				final List<T> payloads = new ArrayList<T>(limit);
 
 				for (int i = 0; i < limit; i++) {
 					node = node.getAfter();
 					if (node == null) {
 						break;
 					} else {
 						payloads.add(node.getPayload());
 					}
 				}
 				return payloads;
 			} else {
 				return Collections.emptyList();
 			}
 		}
 
 		public List<T> getBefore(final K key, final int limit) {
 			DoubleLinkedNode<T> node = nodeMap.get(key);
 			if (node != null) {
 
 				final List<T> payloads = new ArrayList<T>(limit);
 
 				for (int i = 0; i < limit; i++) {
 					node = node.getBefore();
 					if (node == null) {
 						break;
 					} else {
 						payloads.add(node.getPayload());
 					}
 				}
 				return payloads;
 			} else {
 				return Collections.emptyList();
 			}
 		}
 
 		public void flush() {
 			firstNode = null;
 			lastNode = null;
 			i = 0;
 			nodeMap.clear();
 			sortedNodeList.clear();
 		}
 	}
 
 	private static class SortedUsersList extends
 			SortedList<String, SortedCachedUser> {
 		public SortedUsersList(int ratio) {
 			super(ratio);
 		}
 	}
 
 }
