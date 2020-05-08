 package edu.android.podcast_listener;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AbsListView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.ToggleButton;
 
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEnclosure;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
 import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FeedFetcher;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.FetcherException;
 import com.google.code.rome.android.repackaged.com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
 
 import edu.android.podcast_listener.adapters.ItemsAdapter;
 import edu.android.podcast_listener.db.CategoryDAO;
 import edu.android.podcast_listener.db.PodcastDAO;
 import edu.android.podcast_listener.rss.Channel;
 import edu.android.podcast_listener.rss.Item;
 import edu.android.podcast_listener.rss.Items;
 import edu.android.podcast_listener.util.PodcastConstants;
 
 public class FindCastsResultsActivity extends Activity {
 	ListView listView;
 	ListView listAlert;
 	String image;
 	String rssUrl;
 	String category;
 	ProgressDialog progressDialog;
 	String channelTitle;
 	ToggleButton toggleButton;
 	PodcastDAO podDB;
 	CategoryDAO catDB;
 	Channel channelInfo;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_find_casts_results);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		prepareActivityFields();
		
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
 				Item listItem = (Item) listView.getItemAtPosition(position);
 				intent.putExtra(PodcastConstants.EXTRA_MESSAGE, listItem.getLink());
 				intent.putExtra(PodcastConstants.IMAGE_MESSAGE, image);
 				intent.putExtra(PodcastConstants.TITLE_MESSAGE, channelTitle);
 				intent.putExtra(PodcastConstants.EPISODE_MESSAGE, listItem.getTitle());
 				intent.putExtra(PodcastConstants.EPISODE_DESC, listItem.getDescription());
 				startActivity(intent);
 			}					
 		});
 		
 		new RSSAsyncActivity().execute(rssUrl);
 	}
 
 	public void onToggleClicked(View view) {
 		boolean on = ((ToggleButton) view).isChecked();
 		podDB.open();
 		if (on) {
 			podDB.subscribeToPodcast(channelInfo.getTitle(), rssUrl, channelInfo.getImage(), category);
 		} else {
 			podDB.unsubscribeFromPodcast(rssUrl);
 		}
 		podDB.close();
 	}
 	
 	private void checkIfSub(String rssUrl) {
 		podDB.open();
 		if (podDB.isSubscribedToPodcast(rssUrl)) {
 			toggleButton.setChecked(true);
 		} else {
 			toggleButton.setChecked(false);
 		}
 		podDB.close();
 	}
 	
 	private void prepareActivityFields() {
 		podDB = new PodcastDAO(this);
 		catDB = new CategoryDAO(this);
 		Intent intent = getIntent();
 		rssUrl = intent.getStringExtra(PodcastConstants.EXTRA_MESSAGE);
 		listView = (ListView) findViewById(R.id.podcastsList);
 		progressDialog = new ProgressDialog(this);
 		toggleButton = (ToggleButton) findViewById(R.id.subscribe_toggle);
 		checkIfSub(rssUrl);
 		category = "Uncategorized";
 	}
 	
 	public void onSetCategoryClicked(View view) {
 		final Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.category_choice);
 		dialog.setTitle("Select Category");
 		dialog.setCancelable(true);
 		
 		catDB.open();
 		final List<String> categoryNames = catDB.getCategoriesAsString();
 		listAlert = (ListView) dialog.findViewById(R.id.category_list);
 		listAlert.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.category_list_item, categoryNames));
 		
 		listAlert.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> av, View v, int pos,
 					long id) {
 				category = categoryNames.get(pos);
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 		catDB.close();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_find_casts_results, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.menu_categories:
 			toCategories();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void toCategories() {
 		Intent intent = new Intent(this, CategoryManagementActivity.class);
 		startActivity(intent);
 	}
 	
 	public SyndFeed getMostRecentNews( final String feedUrl ) {
         try {
             return retrieveFeed(feedUrl);
         } catch (Exception e) {
         	Log.e(PodcastConstants.ERROR_TAG, "An issue occured while getting the feed: " + e.getMessage());
         }
 		return null;
     }
 
     private SyndFeed retrieveFeed(final String feedUrl) throws IOException, FeedException, FetcherException {
         FeedFetcher feedFetcher = new HttpURLFeedFetcher();
         return feedFetcher.retrieveFeed(new URL(feedUrl));
     }
 	
     @SuppressWarnings("rawtypes") 
 	class RSSAsyncActivity extends AsyncTask<String, Void, Channel> {
 		
 		@Override
 		protected void onPreExecute() {
 			progressDialog = ProgressDialog.show(FindCastsResultsActivity.this,"","Just a second, getting your podcast...", true,false);
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected Channel doInBackground(String... urls) {
 			try {				
 				Channel channel = null;
 				URL url = new URL(urls[0]);
 				
 				Log.d(PodcastConstants.DEBUG_TAG, "Entered the XML Parsing section: " + url.toString());
 				SyndFeed feed = getMostRecentNews(urls[0]);					
 				
 				if (feed != null) {
 					channel = rssFeedParser(feed);
 				}
 				
 				Log.d(PodcastConstants.DEBUG_TAG, "Completed parsing RSS feed");
 				progressDialog.dismiss();
 				
 				return channel;
 			} catch (IOException e) {
 				Log.e(PodcastConstants.ERROR_TAG, "Error in the Input parsing: " + e.getMessage());	
 			} catch (IllegalArgumentException e) {
 				Log.e(PodcastConstants.ERROR_TAG, "An error occured in the arguments: " + e.getMessage());
 			} 
 			return null;
 		}
 		
 		protected void onPostExecute(Channel channel) {	
 			progressDialog.dismiss();
 			if (channel != null) {
 				channelInfo = channel;
 				try {
 					ItemsAdapter adapter = new ItemsAdapter(getApplicationContext(), R.layout.listview_row_item, channel.getItems());
 					listView.setAdapter(adapter);
 					image = channel.getImage();
 					channelTitle = channel.getTitle();
 				} catch (NullPointerException e) {
 					Log.wtf(PodcastConstants.WTF_TAG, "Something bad happened while parsing the XML, so we got here: " + e.getMessage());
 				}
 			}
 		}
 		
 		/**
 		 * Parses an RSS 2.0 feed, grabbing the title of the channel, the image,
 		 * and all the items contained in the feed.
 		 * @param feed
 		 * @return the <code>Channel</code> object containing all necessary item information
 		 */
 		private Channel rssFeedParser(SyndFeed feed) {
 			Channel channel = new Channel();
 			Items items = new Items();
 			List entries = feed.getEntries();	
 			
 			// Creates the feed list for the channel
 			if (feed.getImage() != null) {
 				channel.setImage(feed.getImage().getUrl());
 			} 
 			channel.setTitle(feed.getTitle());
 			Iterator itr = entries.iterator();
 			while (itr.hasNext()) {
 				SyndEntry entry = (SyndEntry) itr.next();
 				String title = entry.getTitle();
 				List enclosures = null; 
 				String description = entry.getDescription().getValue();
 				String link = entry.getLink();
 				Date pubDate = entry.getPublishedDate();
 				Item item = new Item();
 				item.setPubDate(pubDate.toString());
 				if (entry.getEnclosures() != null && entry.getEnclosures().size() > 0) {
 					enclosures = entry.getEnclosures();
 					item.setLink(((SyndEnclosure)enclosures.get(0)).getUrl());
 					item.setSize(((SyndEnclosure)enclosures.get(0)).getLength());
 				} else {
 					item.setLink(link);
 				}
 				item.setTitle(title);
 				item.setDescription(stripHTMLMarkupFromDescription(description));
 				items.add(item);
 			}
 			channel.setItems(items);
 			
 			return channel;
 		}
 		
 		private String stripHTMLMarkupFromDescription(String description) {						
 			return description.replaceAll("(<([^>]+)>)", "");
 		}
 	}
 
 }
