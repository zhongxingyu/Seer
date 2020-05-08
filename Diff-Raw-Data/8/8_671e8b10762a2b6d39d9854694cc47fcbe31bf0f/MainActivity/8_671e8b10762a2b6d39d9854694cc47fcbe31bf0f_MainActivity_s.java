 package ua.ck.android.geekhubandroidfeedreader;
 
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ua.ck.android.geekhubandroidfeedreader.adapters.JSONParserGeekHub;
 import ua.ck.android.geekhubandroidfeedreader.db.Article;
 import ua.ck.android.geekhubandroidfeedreader.db.ArticleDAO;
 import ua.ck.android.geekhubandroidfeedreader.db.HelperFactory;
 import ua.ck.android.geekhubandroidfeedreader.fragments.Fragment1;
 import ua.ck.android.geekhubandroidfeedreader.fragments.Fragment1.onShowArticle;
 import ua.ck.android.geekhubandroidfeedreader.fragments.Fragment2;
 import ua.ck.android.geekhubandroidfeedreader.service.DownloadServiceGeekHub;
 import ua.ck.android.geekhubandroidfeedreader.twitter.TwitterUtils;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.FacebookRequestError;
 import com.facebook.HttpMethod;
 import com.facebook.Request;
 import com.facebook.RequestAsyncTask;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.model.GraphUser;
 
 public class MainActivity extends SherlockFragmentActivity implements onShowArticle {
 	//https://developers.facebook.com/docs/howtos/androidsdk/3.0/publish-to-feed/
 	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
 	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
 	private boolean pendingPublishReauthorization = false;
 	
 	private final int REQ_UPD = 1000;
 	private final int RES_UPD = 1001;
 	public final int STATE_LIST_ONLY = 2000;
 	public final int STATE_ARTICLE_ONLY = 2001;
 	public final int STATE_LIST_AND_ARTICEL = 2002;
 	
 	private final String TAG_JSON = "TAG_JSON";
 	private final static String PARAM_PINTENT = "pendingIntent";
 	private Boolean isShowingAll, isCurrentArticleLiked;
 	
 	
 	String DEBUG = "DEBUG";
 	Boolean isLandTablet;
 	FrameLayout frame1, frame2;
 	PendingIntent pi;
 	String JSON;
 	public Article[] articlesAll;
 	Article currArticle;
 	public Article[] arts;
 	Fragment1 frag1;
 	Fragment2 frag2;
 	Intent intent;
 	private BroadcastReceiver broadcastReceiver;
 	private IntentFilter intentFilter;
 	public int state;
 	 	
 	
 	
 	public Boolean getIsShowingAll() {
 		return isShowingAll;
 	}
 
 	public void setIsShowingAll(Boolean isShowingAll) {
 		this.isShowingAll = isShowingAll;
 	}
 
 	public Boolean getIsCurrentArticleLiked() {
 		return isCurrentArticleLiked;
 	}
 
 	public void setIsCurrentArticleLiked(Boolean isCurrentArticleLiked) {
 		this.isCurrentArticleLiked = isCurrentArticleLiked;
 	}
 
 	
 	
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("state", state);
 		super.onSaveInstanceState(outState);
 	}
 	
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (savedInstanceState != null){
 			state = savedInstanceState.getInt("state");
 		}
 		isCurrentArticleLiked = false;
 		HelperFactory.SetHelper(getApplicationContext());
 		setTheme(R.style.Theme_Sherlock_Light);
 		getSupportActionBar().setTitle("GH");
 		if (!getInternetState(getApplicationContext())){
 			Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_LONG).show();
 		}else{
 			pi = createPendingResult(REQ_UPD, new Intent(), 0);
 			intent = new Intent(MainActivity.this, DownloadServiceGeekHub.class);
 			intent.putExtra(PARAM_PINTENT, pi);
 			startService(intent);
 		}
 		setContentView(R.layout.activity_main);
 		frame1 = (FrameLayout)findViewById(R.id.frame1);
 		frame2 = (FrameLayout)findViewById(R.id.frame2);
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		frag1  = new Fragment1();
 		isShowingAll = true;
 		if (state == STATE_ARTICLE_ONLY){
 			
 		}else{
 			if (frame2 == null){
 				Log.i(DEBUG,"not isLandTablet");
 				isLandTablet = false;
 				state = STATE_LIST_ONLY;
 			}else{
 				Log.i(DEBUG,"isLandTablet");
 				isLandTablet = true;
 				state = STATE_LIST_AND_ARTICEL;
 				frag2  = new Fragment2();
 				ft.add(R.id.frame2, frag2);
 			}
 			ft.add(R.id.frame1, frag1);
 			ft.commit();
 		}
 		broadcastReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				
 				if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
 				    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
 				    if(networkInfo.isConnected()) {
 				        // Wifi is connected
 				        Log.d("Inetify", "Wifi is connected: " + String.valueOf(networkInfo));
 				       // stopService(new Intent(MainActivity.this, DownloadServiceGeekHub.class));
 				        pi = createPendingResult(REQ_UPD, new Intent(), 0);
 						intent = new Intent(MainActivity.this, DownloadServiceGeekHub.class);
 						intent.putExtra(PARAM_PINTENT, pi);
 						startService(intent);
 				    }
 				} else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
 				    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 				    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected()) {
 				        // Wifi is disconnected
 				        Log.d("Inetify", "Wifi is disconnected: " + String.valueOf(networkInfo));
 				    }
 				}
 			}
 		};
 		intentFilter = new IntentFilter();
         intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
         registerReceiver(broadcastReceiver, intentFilter);
         
         
 	}//onCreate
 	
 	public static Boolean getInternetState(Context context) {
 		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		boolean isWifiConn = networkInfo.isConnected();
 		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConn = networkInfo.isConnected();
 		return (isWifiConn || isMobileConn);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		unregisterReceiver( broadcastReceiver );
 		HelperFactory.ReleaseHelper();
 		super.onDestroy();
 	}
 	
 	@Override
 	public void showArticle(Article article) {
 		
 		if (isLandTablet){
 			frag2.setData(article);
 		}else{
 			frag2  = new Fragment2();
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.replace(R.id.frame1, frag2);
 			ft.addToBackStack(null);
 			ft.commit();
 			
 			frag2.setData(article);
 			currArticle = article;
 			state = STATE_ARTICLE_ONLY;
 			invalidateOptionsMenu();
 			
 		}
 	}
 	
 	
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		Log.i(DEBUG,"onActivityResult");
 		if (requestCode == REQ_UPD){
 			if (resultCode == RES_UPD){
 				JSON = data.getExtras().getString(TAG_JSON);
 				//Log.i("Activity JSON", JSON);
 				try{
 					articlesAll = JSONParserGeekHub.parse(JSON);
 					((Fragment1)frag1).update(articlesAll);
 				}catch (Exception e) {
 					// TODO: handle exception
 				}
 				
 				Log.i(DEBUG,"update has come" );
 			}
 		}else{
 			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 		}
 	}
 	
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.all_liked:
 			if (isShowingAll){
 				try{
 					List<Article> articlesListFromDB;
 					ArticleDAO articleDAO = HelperFactory.GetHelper().getArticleDAO();
 					articlesListFromDB = articleDAO.getAllArticles();
 					if (articlesListFromDB.size() > 0){
 						arts = new Article[articlesListFromDB.size()];// = (Article[])articlesListFromDB.toArray();
 						for (int i= 0; i< articlesListFromDB.size(); i++){
 							arts[i] = articlesListFromDB.get(i);
 						}
 						((Fragment1)frag1).update(arts);
 						item.setIcon(R.drawable.ic_action_all);
 						isShowingAll = false;
 					}else{
 						Toast.makeText(getApplicationContext(), "Database is empty", Toast.LENGTH_SHORT).show();
 					}
 				}catch (SQLException e) {
 					Toast.makeText(getApplicationContext(), "Database error", Toast.LENGTH_SHORT).show();
 				}
 			}else{
 				((Fragment1)frag1).update(articlesAll);
 				item.setIcon(R.drawable.ic_action_liked);
 				isShowingAll = true;
 			}
 			break;
 		case R.id.like_dislike:
 			try{
 				ArticleDAO articleDAO = HelperFactory.GetHelper().getArticleDAO();
 				/*if (frag2 != null){
 					if (frag2.getCurArticle() != null){	
 					List<Article>all = articleDAO.getAllArticles();
 					if (all.contains(frag2.getCurArticle()));
 						articleDAO.delete(frag2.getCurArticle());
 						Toast.makeText(getApplicationContext(), "Article unmarked as liked", Toast.LENGTH_LONG).show();
 					}
 				}*/
 				
 				if (isCurrentArticleLiked){
 					if (frag2 != null){
 						if (frag2.getCurArticle() != null){							
 							articleDAO.delete(frag2.getCurArticle());	
 							Toast.makeText(getApplicationContext(), "Article unmarked as liked", Toast.LENGTH_LONG).show();
 						}
 					}
 				}else{
 					if (frag2 != null){
 						if (frag2.getCurArticle() != null){
 							articleDAO.create(frag2.getCurArticle());	
 							Toast.makeText(getApplicationContext(), "Article marked as liked", Toast.LENGTH_LONG).show();
 						}
 					}
 				}
 				//((MainActivity)getSherlockActivity()).setIsCurrentArticleLiked(true);
 			}catch (SQLException e) {
 				// TODO: handle exception
 			}	
 				
 				break;
 		case R.id.facebook:
 			// start Facebook Login
 	        Session.openActiveSession(this, true, new Session.StatusCallback() {
 	          // callback when session changes state
 	          @Override
 	          public void call(Session session, SessionState state, Exception exception) {
 	            if (session.isOpened()) {
 	              // make request to the /me API
 	              Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
 	                // callback after Graph API response with user object
 	                @Override
 	                public void onCompleted(GraphUser user, Response response) {
 	                  if (user != null) {
 	                	Toast.makeText(getApplicationContext(), "Hello " + user.getName(), Toast.LENGTH_LONG).show();  
 	                	postOnWall();
 	                  }
 	                }
 	              });
 	            }
 	          }
 	        });
 	        
 			break;
 		case R.id.twitter:
 			new Thread(new Runnable() {
                 @Override
                 public void run() {
                     TwitterUtils.sendTweetExec(MainActivity.this, "I like GeekHub Android developers Feed reader made by Sergey Tsibrovskiy");
                 }
             }).start();
             
 			break;
 		default:
 			break;
 		}
 		//Toast.makeText(getApplicationContext(), Integer.toString(item.getItemId()), Toast.LENGTH_LONG).show();
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void postOnWall(){
 		Session session = Session.getActiveSession();
 	    if (session != null){
 	    	Log.i("POST","Session != null");
 	    	// Check for publish permissions    
 	        List<String> permissions = session.getPermissions();
 	    	if (!isSubsetOf(PERMISSIONS, permissions)) {
 	            pendingPublishReauthorization = true;
 	            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
 				session.requestNewPublishPermissions(newPermissionsRequest);
 	            return;
 	        }
 	    	Log.i("POST","Permissions OK");
 	    	Bundle postParams = new Bundle();
 	        postParams.putString("name", "I like GeekHub Android developers Feed reader made by Sergey Tsibrovskiy");
 	        postParams.putString("caption", "Read android developers blog in your android device");
 	        postParams.putString("description", "Awesome! Now You can read Android Developers feed from the Application");
 	        postParams.putString("link", "http://android.ck.ua");
 	        postParams.putString("picture", "http://android.ck.ua/logo.jpg");
 	        
 	        Request.Callback callback= new Request.Callback() {
 	            public void onCompleted(Response response) {
 	                JSONObject graphResponse = response
 	                                           .getGraphObject()
 	                                           .getInnerJSONObject();
 	                String postId = null;
 	                try {
 	                    postId = graphResponse.getString("id");
 	                } catch (JSONException e) {
 	                    Log.i("POST","JSON error "+ e.getMessage());
 	                }
 	                FacebookRequestError error = response.getError();
 	                if (error != null) {
 	                    Toast.makeText(MainActivity.this.getApplicationContext(),error.getErrorMessage(),Toast.LENGTH_SHORT).show();
 	                    } else {
 	                        Toast.makeText(MainActivity.this.getApplicationContext(),"Post success",Toast.LENGTH_LONG).show();
 	                }
 	            }
 	        };
 	        Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
 	        RequestAsyncTask task = new RequestAsyncTask(request);
 	        task.execute();
 	    }else{
 	    	Log.i("POST","Session == null");	    	
 	    }
 	}
 	
 	
 	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
 	    for (String string : subset) {
 	        if (!superset.contains(string)) {
 	            return false;
 	        }
 	    }
 	    return true;
 	}
 	
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu  menu) {
 		if (state == STATE_LIST_ONLY){
 			getSupportMenuInflater().inflate(R.menu.menu_list, menu);
 			if (!isShowingAll){
 				menu.getItem(0).setIcon(R.drawable.ic_action_liked);
 			}
 		}else if (state == STATE_LIST_AND_ARTICEL){
 			getSupportMenuInflater().inflate(R.menu.menu_article_and_list, menu);
 		}else{
 			getSupportMenuInflater().inflate(R.menu.menu_article, menu);
 		}
         return true;
     }	
 }
