 package se.chalmers.h_sektionen;
 
 import java.util.ArrayList;
 
 import se.chalmers.h_sektionen.adapters.NewsAdapter;
 import se.chalmers.h_sektionen.containers.NewsItem;
 import se.chalmers.h_sektionen.utils.LoadData;
 import se.chalmers.h_sektionen.utils.MenuItems;
 import se.chalmers.h_sektionen.utils.OnBottomScrollListener;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 public class MainActivity extends BaseActivity {
 	
 	private NewsAdapter newsAdapter;
 	private ListView newsFeed;
 	private View aniFooter;
 	private boolean loadingFirstTime;
 	private boolean currentlyLoading;
 	private int descending;
 	private AsyncTask<Integer, String, Boolean> loadNewsTask;
 	
 	/**
 	 * Superclass method onResume
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		newsAdapter = new NewsAdapter(MainActivity.this, R.layout.news_feed_item, new ArrayList<NewsItem>());
 		loadingFirstTime = true;
 		currentlyLoading = false;
 		setCurrentView(MenuItems.NEWS);
 		createNewsView();
 		descending = 0;
 	}
 	
 	/**
 	 * On pause: cancel the AsyncTask that downloads the lunch menu.
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (loadNewsTask != null && !loadNewsTask.isCancelled()) {
 			loadNewsTask.cancel(true);
 		}
 	}
     
 	/**
 	 * Creates news view GUI
 	 */
     private void createNewsView(){
     	
     	if (connectedToInternet()){
 	    	getFrameLayout().removeAllViews();
 			getFrameLayout().addView(getLayoutInflater().inflate(R.layout.view_news, null));
 			
 			newsFeed = (ListView) findViewById(R.id.news_feed);
 			aniFooter = getLayoutInflater().inflate(R.layout.footer_animation, null);
 			
 			//Initialize header
 			ImageView imgHeader = new ImageView(MainActivity.this);
 			imgHeader.setAdjustViewBounds(true);
 			imgHeader.setImageResource(R.drawable.news);
 
 			//Add to list view
			if (newsFeed.getHeaderViewsCount() == 0) {
				newsFeed.addHeaderView(imgHeader,null,false);
			}
			
 			newsFeed.setAdapter(newsAdapter);
 
 			
 			newsFeed.setOnScrollListener(new OnBottomScrollListener(){
 				@Override
 				protected void doOnScrollCompleted() {
 					if (!currentlyLoading){
 						new LoadNewsInBg().execute(++descending);
 					}			
 				}});
 			
 			loadNewsTask = new LoadNewsInBg().execute(descending);
     	} else {
     		setErrorView(getString(R.string.INTERNET_CONNECTION_ERROR_MSG));
     	}
     }
     
     /**
      * Adds a loading animation to the listview footer.
      */
     private void addFooterAnimation(){
 		newsFeed.addFooterView(aniFooter,null,false);
     }
     
     /**
      * Removes loading animation from listview footer.
      */
     private void removeFooterAnimation(){
     	newsFeed.removeFooterView(aniFooter);
     }
     
     /**
      * AsyncTask for loading news feed posts in background.
      */
     private class LoadNewsInBg extends AsyncTask<Integer, String, Boolean> {
 		
 		
     	/**
 		 * Superclass method onPreExecte
 		 */
 		@Override
 		protected void onPreExecute(){
 			
 			if (loadingFirstTime){
 				runTransparentLoadAnimation();
 			} else {
 				addFooterAnimation();
 			}
 		}
 		
 		/**
 		 * Superclass method doInBackground
 		 */
 		@Override
 		protected Boolean doInBackground(Integer... descending) {	
 			try {
 				newsAdapter.addAll(LoadData.loadNews(descending[0]));
 				return true;
 			} catch (Exception e){
 				return false;
 			}
 		}
 		
 		/**
 		 * Superclass method onPostExecute
 		 */
 		@Override
         protected void onPostExecute(Boolean feedLoadedSuccessfully){
 			
 			if (feedLoadedSuccessfully){
 				if (loadingFirstTime){
 					stopAnimation();
 					loadingFirstTime = false;
 				} else {
 					removeFooterAnimation();
 				}
 				
 				currentlyLoading = false;
 				newsAdapter.notifyDataSetChanged();
 				
 			} else {
 				setErrorView(getString(R.string.GET_FEED_ERROR_MSG));
 			}
 			
 		}
     }
 }
