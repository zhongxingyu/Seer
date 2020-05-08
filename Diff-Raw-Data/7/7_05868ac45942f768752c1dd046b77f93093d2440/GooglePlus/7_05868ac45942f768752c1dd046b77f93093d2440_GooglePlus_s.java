 package com.ChewieLouie.Topical;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import android.os.AsyncTask;
 
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.plus.Plus;
 import com.google.api.services.plus.model.Activity;
 import com.google.api.services.plus.model.ActivityFeed;
 import com.google.api.services.plus.model.Comment;
 import com.google.api.services.plus.model.CommentFeed;
 
 public class GooglePlus implements GooglePlusIfc {
 
 	private static GooglePlus myPlus = null;
 	
 	public static GooglePlus Make() {
 		if( myPlus == null )
 			myPlus = new GooglePlus();
 		return myPlus;
 	}
 
 	private static final String googleAPIKey = "AIzaSyA1UkMy-J3PbX6ozp22P0KbV7XApguSb7s";
 	private static final String appName = "Topical";
 	private static final String collectionPublic = "public";
 
 	private Plus plus = null;
 	private Map<String, Map<DataType, String>> queryResultCache = new HashMap<String, Map<DataType, String>>();
 	private Map<String, Semaphore> queriesInProgress = new HashMap<String, Semaphore>();
 	private Semaphore queriesInProgressSema4 = new Semaphore( 1 );
 
 	private GooglePlus() {
 		plus = new Plus( appName, new NetHttpTransport(), new GsonFactory() );
 		plus.setKey( googleAPIKey );
 	}
 
 	public void getPostInformation( GooglePlusCallbackIfc callbackObj, GooglePlusQuery query, int requestID ) {
 		new GetPostTask( callbackObj, requestID, query ).execute();
 	}
 
 	private class GetPostTask extends AsyncTask<Void, Void, Void> {
     	private String errorText = null;
     	private GooglePlusQuery query = null;
     	private GooglePlusCallbackIfc callbackObj = null;
     	private int requestID;
     	private String queryKey = "";
     	
     	public GetPostTask( GooglePlusCallbackIfc callbackObj, int requestID, GooglePlusQuery query ) {
     		this.callbackObj = callbackObj;
     		this.requestID = requestID;
     		this.query = query;
     		queryKey = query.makeKeyFromAuthorAndURL();
     	}
 
     	@Override
 		protected Void doInBackground( Void... params ) {
 			queriesInProgressSema4.acquireUninterruptibly();
 			if( isQueryInProgress() )
 				waitForQueryToFinish();
     		else
     			startQuery();
     		return null;
 		}
 
     	private boolean isQueryInProgress() {
     		return queriesInProgress.containsKey( queryKey );
     	}
     	
     	private void waitForQueryToFinish() {
 			Semaphore sem = queriesInProgress.get( queryKey );
 			queriesInProgressSema4.release();
 			sem.acquireUninterruptibly();
     	}
     	
     	private void startQuery() {
 			queriesInProgress.put( queryKey, new Semaphore( 0 ) );
 			queriesInProgressSema4.release();
 			queryResultCache.put( queryKey, extractDataFromActivity( findActivity( query ) ) );
     	}
     	
     	private Activity findActivity( GooglePlusQuery query ) {
     		try {
 				if( query.postIDIsValid() )
 					return plus.activities.get( query.postID ).execute();
 				else if( query.authorIDIsValid() && query.urlIsValid() )
 					return findActivityByAuthorAndURL();
 			} catch (IOException e) {
 				e.printStackTrace();
 				errorText = e.getMessage();
 			}
 			return null;
     	}
    
     	private Activity findActivityByAuthorAndURL() throws IOException {
 			Plus.Activities.List request = plus.activities.list( query.authorID, collectionPublic );
 			ActivityFeed feed = null;
 			Activity foundActivity = null;
 			do {
 				feed = request.execute();
 				foundActivity = findActivityByURL( query.url, feed.getItems() );
 				request.setPageToken( feed.getNextPageToken() );
 			} while( foundActivity == null && feed.getNextPageToken() != null );
 			return foundActivity;
     	}
     	
     	private Activity findActivityByURL( String url, List<Activity> activities ) {
     		if( activities != null )
     			for( Activity activity : activities )
     				if( activity.getUrl().equals( url ) )
     					return activity;
 			return null;
     	}
 
 		@Override
 		protected void onPostExecute( Void voidArg ) {
 			super.onPostExecute( voidArg );
 			callCallbackObj( queryKey, callbackObj, errorText, requestID );
 			notifyWaitingThreads();
 		}
 		
 		private void notifyWaitingThreads() {
 			queriesInProgressSema4.acquireUninterruptibly();
 			Semaphore sem = queriesInProgress.get( queryKey );
 			if( sem != null ) {
 				sem.release();
 				if( sem.hasQueuedThreads() == false )
 					queriesInProgress.remove( queryKey );
 			}
 			queriesInProgressSema4.release();
 		}
     }
 	
 	private void callCallbackObj( String queryKey, GooglePlusCallbackIfc callbackObj, String errorText, int requestID ) {
 		Map<DataType, String> postInfo = queryResultCache.get( queryKey );
 		if( postInfo == null && errorText == null )
 			errorText = "No Google Plus post found";
 		if( errorText != null )
 			callbackObj.postInformationError( errorText, requestID );
 		else
 			callbackObj.postInformationResults( postInfo, requestID );
 	}
 
 	@Override
 	public void getComments(GooglePlusCallbackIfc callbackObj, String postID) {
 		new GetCommentsTask( callbackObj, postID ).execute();
 	}
 
 	private class GetCommentsTask extends AsyncTask<Void, Void, Void> {
     	private String errorText = null;
     	private String postID = null;
     	private GooglePlusCallbackIfc callbackObj = null;
     	private List<PostComment> postComments = new ArrayList<PostComment>();
 
     	public GetCommentsTask( GooglePlusCallbackIfc callbackObj, String postID ) {
     		this.callbackObj = callbackObj;
     		this.postID = postID;
     	}
 
     	@Override
 		protected Void doInBackground( Void... params ) {
     		Plus.Comments.List request = plus.comments.list( postID );
 			CommentFeed feed = null;
 			try {
 				do {
 					feed = request.execute();
 					addToPostComments( feed.getItems() );
 					request.setPageToken( feed.getNextPageToken() );
 				} while( feed.getNextPageToken() != null );
 			} catch (IOException e) {
 				e.printStackTrace();
 				errorText = e.getMessage();
 			}
     		return null;
 		}
 
 		private void addToPostComments(List<Comment> comments) {
			for( Comment comment : comments )
				postComments.add( new PostComment( comment.getActor().getDisplayName(), 
						comment.getUpdated(), comment.getPlusObject().getContent() ) );
 		}
 
 		@Override
 		protected void onPostExecute( Void voidArg ) {
 			super.onPostExecute( voidArg );
 			if( errorText != null )
 				callbackObj.commentsError( errorText );
 			else
 				callbackObj.commentResults( postComments );
 		}
 	}
 
 	@Override
 	public void search( String searchText, GooglePlusCallbackIfc callbackObj ) {
 		new SearchTask( callbackObj, searchText ).execute();
 	}
 
 	private class SearchTask extends AsyncTask<Void, Void, Void> {
     	private GooglePlusCallbackIfc callbackObj = null;
     	private String searchText;
 		private List< Map<DataType,String> > results = new ArrayList< Map<DataType,String> >();
 		private Plus.Activities.Search request = null;
    		private ActivityFeed feed = null;
 		private final int maxResults = 20;
 
     	public SearchTask( GooglePlusCallbackIfc callbackObj, String searchText ) {
     		this.callbackObj = callbackObj;
     		this.searchText = searchText;
     	}
 
     	@Override
 		protected Void doInBackground( Void... params ) {
     		request = plus.activities.search();
     		request.setQuery( searchText );
     		try {
     			do {
     				processActivityFeed();
 				} while( moreResultsAvailable() );
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
     		return null;
     	}
 
     	private void processActivityFeed() throws IOException {
     		feed = request.execute();
     		if( feed != null && feed.getItems() != null ) {
 				for( Activity activity : feed.getItems() )
 					results.add( extractDataFromActivity( activity ) );
 				request.setPageToken( feed.getNextPageToken() );
     		}
     	}
     	
     	private boolean moreResultsAvailable() {
     		return results.size() < maxResults && feed != null && feed.getNextPageToken() != null;
     	}
 
     	@Override
 		protected void onPostExecute( Void voidArg ) {
 			super.onPostExecute( voidArg );
 			callbackObj.searchResults( results );
 		}
 	}
 
 	private static Map<DataType, String> extractDataFromActivity( Activity activity ) {
 		Map<DataType, String> values = null;
 		if( activity != null ) {
 			values = new HashMap<DataType, String>();
 			putString( values, DataType.AUTHOR_ID, activity.getActor().getId() );
 			putString( values, DataType.AUTHOR_IMAGE, activity.getActor().getImage().getUrl() );
 			final String authorName = activity.getActor().getDisplayName();
 			putString( values, DataType.AUTHOR_NAME, authorName );
 			putString( values, DataType.COMMENTS, activity.getPlusObject().getReplies().toString() );
 			if( activity.getUpdated() != null )
 				putString( values, DataType.MODIFICATION_TIME, activity.getUpdated().toStringRfc3339() );
 			else
 				putString( values, DataType.MODIFICATION_TIME, activity.getPublished().toStringRfc3339() );
 			putString( values, DataType.POST_CONTENT, activity.getPlusObject().getContent() );
 			putString( values, DataType.POST_ID, activity.getId() );
 			putString( values, DataType.SUMMARY, activity.getTitle() );
 			putString( values, DataType.TITLE, authorName );
 			putString( values, DataType.URL, activity.getUrl() );
 		}
 		return values;
 	}
 
 	private static void putString( Map<DataType, String> values, DataType type, String value ) {
 		if( value == null )
 			values.put( type, "" );
 		else
 			values.put( type, value );
 	}
 }
