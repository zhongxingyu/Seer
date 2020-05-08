 package jp.skypencil.util.twitter4j;
 
 import java.util.Iterator;
 
 import twitter4j.PagableResponseList;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.UserList;
 import twitter4j.api.ListMethods;
 
 public final class ListsIterator implements Iterable<UserList> {
 	private final ListMethods twitter;
 	private final String screenName;
 
 	/**
 	 * @throws TwitterException 
 	 * @throws IllegalStateException 
 	 * @deprecated use {@link ListsIterator(ListMethods, String)}
 	 */
 	public ListsIterator(Twitter twitter) throws IllegalStateException, TwitterException {
 		this(twitter, twitter.getScreenName());
 	}
 
 	public ListsIterator(ListMethods twitter, String screenName) {
 		this.twitter = twitter;
 		this.screenName = screenName;
 	}
 
 	@Override
 	public Iterator<UserList> iterator() {
 			return new Iter(twitter, screenName);
 	}
 
 	private static class Iter implements Iterator<UserList> {
 		private final ListMethods twitter;
 		private final String screenName;
 		private long cursor;
 		
 		private int nextIndex = -1;
 		private PagableResponseList<UserList> lists;
 		Iter(ListMethods twitter, String screenName) {
 			this.cursor = -1L;
 			this.twitter = twitter;
 			this.screenName = screenName;
 		}
 
 		@Override
 		public boolean hasNext() {
 			try {
 				if (lists == null || lists.size() <= nextIndex) {
 					lists = twitter.getUserListMemberships(screenName, cursor);
 					nextIndex = 0;
 					cursor = lists.getNextCursor();
 				}
 		
 				return lists.size() > 0;
 			} catch (TwitterException e) {
				// TODO K؂ȗOɖ|
 				throw new RuntimeException(e);
 			}
 		}
 	
 		@Override
 		public UserList next() {
 			UserList next = lists.get(nextIndex);
 			++nextIndex;
 			return next;
 		}
 	
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 	}
 }
