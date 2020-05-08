 package com.ChewieLouie.Topical;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.ChewieLouie.Topical.GooglePlusIfc.DataType;
 import com.ChewieLouie.Topical.PersistentStorageIfc.ValueType;
 import com.ChewieLouie.Topical.View.NullViewWatchedTopic;
 import com.ChewieLouie.Topical.View.ViewWatchedTopicIfc;
 
 public class Topic implements GooglePlusSearchCallbackIfc, TopicIfc {
 
 	public boolean hasChanged = false;
 	
 	private ViewWatchedTopicIfc view = new NullViewWatchedTopic();
 	private String topicName = "";
 	private GooglePlusIfc googlePlus = null;
 	private PersistentStorageIfc storage = null;
 	private Set<String> currentPostIDs = null;
 	private Set<String> newPostIDs = null;
 	private boolean updating = false;
 
 	public Topic( String topicName, GooglePlusIfc googlePlus, PersistentStorageIfc storage ) {
 		this.topicName = topicName;
 		this.googlePlus = googlePlus;
 		this.storage = storage;
 		String postIDsList = storage.loadValueByKeyAndType( topicName, ValueType.POST_ID_LIST );
 		if( postIDsList != null )
 			currentPostIDs = new HashSet<String>( StringUtils.split( postIDsList, "," ) );
 	}
 
 	@Override
 	public void viewIsNoLongerUsable() {
 		view = new NullViewWatchedTopic();
 	}
 
 	@Override
 	public void showStatus( ViewWatchedTopicIfc view ) {
 		this.view = view;
 		if( updating )
 			view.activityStarted();
 		view.setText( topicName );
 		updateViewWithStatus();
 	}
 
 	private void updateViewWithStatus() {
 		if( hasChanged )
 			view.setTopicResultsHaveChanged();
 		else
 			view.setTopicResultsHaveNotChanged();
 	}
 
 	@Override
 	public String topicName() {
 		return topicName;
 	}
 
 	@Override
 	public void updateStatus() {
 		if( hasChanged )
 			updateViewWithStatus();
 		else {
 			updating = true;
 			view.activityStarted();
 	   		googlePlus.search( topicName, this );
 		}
 	}
 
 	@Override
 	public void searchResults( List<Map<DataType, String>> results ) {
 		if( results != null )
 			if( haveTopicResultsChanged( results ) )
 				cacheNewPostIDs( results );
 		updateViewWithStatus();
 		view.activityStopped();
 		updating = false;
 	}
 
 	private void cacheNewPostIDs( List<Map<DataType, String>> results ) {
 		newPostIDs = extractPostIDs( results );
 	}
 
 	@Override
 	public void viewed() {
 		savePostIDs();
 	}
 
 	private void savePostIDs() {
		currentPostIDs = newPostIDs;
		storage.saveValueByKeyAndType( createPostIDsString( currentPostIDs ), topicName, ValueType.POST_ID_LIST );
 		hasChanged = false;
 	}
 
 	private Set<String> extractPostIDs( List<Map<DataType, String>> results ) {
 		Set<String> extractedPostIDs = new HashSet<String>();
 		for( Map<DataType, String> result : results )
 			extractedPostIDs.add( result.get( DataType.POST_ID ) );
 		return extractedPostIDs;
 	}
 
 	private String createPostIDsString( Set<String> postIDsList ) {
 		return StringUtils.join( ",", postIDsList.toArray( new String[0] ) );
 	}
 
 	private boolean haveTopicResultsChanged( List<Map<DataType, String>> results ) {
 		if( extractPostIDs( results ).equals( currentPostIDs ) )
 			hasChanged = false;
 		else
 			hasChanged = true;
 		return hasChanged;
 	}
 }
