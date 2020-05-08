 package tice.weibo.List;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.regex.Matcher;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import tice.weibo.App;
 import tice.weibo.R;
 import tice.weibo.Activities.DirectActivity;
 import tice.weibo.Activities.FavoriteActivity;
 import tice.weibo.Activities.HomeActivity;
 import tice.weibo.Activities.MentionActivity;
 import tice.weibo.Activities.SearchActivity;
 import tice.weibo.Activities.StatusesShowActivity;
 import tice.weibo.Activities.TrendsActivity;
 import tice.weibo.Activities.UserInfoActivity;
 import tice.weibo.Activities.UserStatusesActivity;
 import tice.weibo.DB.DBTweetsHelper;
 import tice.weibo.HttpClient.TwitterClient;
 import tice.weibo.Setting.Setting;
 import tice.weibo.Util.ProgressData;
 import tice.weibo.Util.TweetsData;
 import tice.weibo.Util.TwitterItem;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.TypedArray;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore.Images.Media;
 import android.text.ClipboardManager;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.AnimationSet;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class TweetsListActivity extends Activity {
 
 	public App _App = null;
 
 	public final static int INPUT_NEWPOST = 1;
 	public final static int INPUT_REPLY = 2;
 	public final static int INPUT_RETWEET = 3;
 	public final static int INPUT_DIRECT = 4;
 	public final static int PHOTO_PICKED = 1;
 	public final static int PREVIEW_WIDTH = 200;
 	public final static int PREVIEW_HEIGHT = 150;
 	public final static int APP_CHAINCLOSE = 88888;
 	public final static int READ_STATE_UNKNOW = 0;
 	public final static int READ_STATE_UNREAD = 1;
 	public final static int READ_STATE_READ = 2;
 	public final static int READ_STATE_MARKUNREAD = 3;
 	public final static int PORTSSL = 443;
 	public final static int PORT = 80;
 	public final static int ADD_TYPE_APPEND = 0;
 	public final static int ADD_TYPE_INSERT = 1;
 	public final static int ADD_TYPE_REPLACE = 2;
 
 	private static float last_x,last_y,last_z;
 	private static long last_update = 0;
 	private static double last_distance = 0;
 	private static int last_freecount = -1;
 	private static long last_click_time = 0;
 
 	protected int _CurrentThread = -1;
 	protected ArrayList<String> _Links;
 	protected ArrayList<String> _Replies;
 	//protected long _LastID = 0;
 	//protected long _PrevID = Long.MAX_VALUE;
 	protected boolean _StartLoading = false;
 	protected boolean _Refresh = false;
 	protected ItemAdapter _Items;
 	//protected TweetsCursorAdapter _Items;
 	protected LinearLayout _Statuspanel;
 	protected LinearLayout _Toolbarpanel;
 	protected LinearLayout _Previewpanel;
 	protected LinearLayout _FixToolbarpanel;
 	protected ListView _list;
 	//protected Menu _Menu;
 	protected Handler _Handler;
 	protected Context _Context;
 	protected int _ActivityType;
 	protected String _ActivityName;
 	protected int _InputType;
 	//protected static SensorManager _SensorMgr = null;
 	protected Sensor _Sensor;
 	//protected int _Page = 1;
 	protected Uri _UploadFile = null;
 	protected String _PictureURL = "";
 	protected Bitmap _bitmap = null;
 	protected ProgressDialog _Progressdialog = null;
 	protected boolean _ReceiverIntent = false;
 	protected boolean mScrolling = false;
 	protected int mAddType = ADD_TYPE_INSERT;
 	protected String mOldAccount = "";
 
 	//protected boolean _InRefresh = false;
 
 	//protected int _HomeType = TwitterClient.HOME_HOME;
 
 	public interface UpdateViewCallback{
 		public void Update(Message msg,boolean append, boolean order);
 	}
 
     protected void SetWindowTitle(int type){
     	String app_name = getResources().getString(R.string.app_name);
     	String title = app_name;
 
     	switch (type){
     	case TwitterClient.HOME_HOME:
     		title = String.format("Home");
     		_ActivityName = "Home";
     		break;
     	case TwitterClient.HOME_MENTIONS:
     		title = String.format("Mentions");
     		_ActivityName = "Mentions";
     		break;
     	case TwitterClient.HOME_DIRECT:
     		title = String.format("Inbox");
     		_ActivityName = "Inbox";
     		break;
     	case TwitterClient.HOME_DIRECT_SENT:
     		title = String.format("Outbox");
     		_ActivityName = "Outbox";
     		break;
     	case TwitterClient.HOME_FAVORITES:
     		title = String.format("Favorites");
     		_ActivityName = "Favorites";
     		break;
     	case TwitterClient.HOME_STATUSES_SHOW:
     		title = String.format("Threads");
     		_ActivityName = "Threads";
     		break;
     	case TwitterClient.HOME_SEARCH:
     		title = String.format("Search");
     		_ActivityName = "Search";
     		break;
     	case TwitterClient.HOME_USERINFO:
     		title = String.format("Profile");
     		_ActivityName = "UserInformation";
     		break;
     	case TwitterClient.HOME_TRENDS:
     		title = String.format("Trends");
     		_ActivityName = "Trends";
     		break;
     	case TwitterClient.HOME_FRIENDS:
     		title = String.format("Following");
     		_ActivityName = "Following";
     		break;
     	case TwitterClient.HOME_FOLLOWERS:
     		title = String.format("Followers");
     		_ActivityName = "Followers";
     		break;
     	}
 
     	title = String.format("%s | %s | %s", app_name, _App._Username, title);
 
         final TextView leftText = (TextView) findViewById(R.id.title_text);
         leftText.setText(title);
 
         //_App._tracker.trackPageView("/" + _ActivityName);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
         switch(resultCode){
         case APP_CHAINCLOSE:
              setResult(APP_CHAINCLOSE);
              finish();
              if(_ActivityType == TwitterClient.HOME_HOME){
             	 //_AlarmMgr.cancel(_Sender);
             	 if(_App._NotiServices != null){
             		 stopService(_App._NotiServices);
             	 }
              }
              break;
         }
 
     	super.onActivityResult(requestCode, resultCode, data);
 
         if ((requestCode == 1 || requestCode == 2) && resultCode == Activity.RESULT_OK){
 
         	try {
 
         		if(requestCode == 1){
         			_UploadFile = data.getData();
         		}else if (requestCode == 2){
         			_bitmap = (Bitmap) data.getExtras().get("data");
                     ContentValues values = new ContentValues();
                     values.put(Media.TITLE, "title");
                     values.put(Media.BUCKET_ID, "upload");
                     values.put(Media.DESCRIPTION, "upload Image taken");
                     values.put(Media.MIME_TYPE, "image/jpeg");
                     Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
                     OutputStream outstream;
                     outstream = getContentResolver().openOutputStream(uri);
                     _bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                     outstream.close();
                     _UploadFile = uri;
                     _bitmap.recycle();
                     _bitmap = null;
         		}
 
 	        	int samplesize = 12;
 	        	BitmapFactory.Options options;
 	        	ImageView view = (ImageView) findViewById(R.id.ImagePrview);
 	        	InputStream thePhoto = getContentResolver().openInputStream(_UploadFile);
 	        	InputStream testSize = getContentResolver().openInputStream(_UploadFile);
 
 	        	options = new BitmapFactory.Options();
 	        	options.inJustDecodeBounds = true;
 	        	BitmapFactory.decodeStream(testSize, null,options);
 
 	        	if(options.outHeight >= options.outHeight){
 	        		samplesize = options.outHeight / PREVIEW_HEIGHT;
 	        	}else{
 	        		samplesize = options.outWidth / PREVIEW_WIDTH;
 	        	}
 
 	        	if (_bitmap != null){
 	        		_bitmap.recycle();
 	        		_bitmap = null;
 	        	}
 	        	options.inDither = true;
 	        	options.inSampleSize = samplesize;
 	        	options.inJustDecodeBounds = false;
 	        	options.inPreferredConfig = Bitmap.Config.RGB_565;
 	        	_bitmap = BitmapFactory.decodeStream(thePhoto, null,options);
 
 	        	_Previewpanel.setVisibility(View.VISIBLE);
 	        	view.setImageBitmap(_bitmap);
 
 	        	thePhoto.close();
 	        	testSize.close();
 
 	        } catch (FileNotFoundException e) { } catch (IOException e) { }
 	    }
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
     	_App = (App)getApplicationContext();
 
 //        if(_SensorMgr == null){
 //        	_SensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
 //        	_Sensor = _SensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 //           	_SensorMgr.registerListener(SensorEventListener,_Sensor,SensorManager.SENSOR_DELAY_UI);
 //        }
 
 		registerReceiver(mReceiver, new IntentFilter("tice.twitterwalk.INAPP_AUTO_REFRESH"));
 
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
         setTheme(_App._Theme);
         setContentView(R.layout.main);
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_1);
         setMyProgressBarVisibility(false);
 
         _list = (ListView)findViewById(android.R.id.list);
         _Statuspanel = (LinearLayout)findViewById(R.id.StatusLayout);
         _FixToolbarpanel = (LinearLayout)findViewById(R.id.FixToolbarLayout);
        	_Toolbarpanel = (LinearLayout)findViewById(R.id.ToolbarLayout);
 
        	_Toolbarpanel.setVisibility(View.INVISIBLE);
         _Statuspanel.setVisibility(View.INVISIBLE);
 
         if(_App._AutoHideToolbar == true){
         	_FixToolbarpanel.setVisibility(View.GONE);
         }else{
         	_Toolbarpanel = _FixToolbarpanel;
         }
 
 	    TypedArray atts = obtainStyledAttributes(new int []{R.attr.ListViewBackground,R.attr.ListViewDivider});
 
         _Items = new ItemAdapter(this);
         _Links = new ArrayList<String>();
         _Replies = new ArrayList<String>();
  		_list.setAdapter(_Items);
  		_list.setSelection(0);
         _list.setFadingEdgeLength(2);
         _list.setClickable(true);
         _list.setFocusable(true);
         //_list.setDividerHeight(1);
         _list.setFadingEdgeLength(0);
         _list.setDivider(new BitmapDrawable(BitmapFactory.decodeResource(getResources(),atts.getResourceId(1, R.drawable.theme_default_divider))));
         _list.setFocusableInTouchMode(true);
         _list.setOnScrollListener(OnScrollListener);
         _list.setOnItemLongClickListener(OnItemLongClickListener);
 
         atts.recycle();
 
         _App._HaveNotification = false;
         LoadTweetItemsFromDB();
         mOldAccount = _App._Username;
     }
 
     @Override
 	protected void onDestroy() {
 
 //    	_SensorMgr.unregisterListener(SensorEventListener,_Sensor);
 
 		try{unregisterReceiver(mReceiver);}catch(Exception e){}
 
 		super.onDestroy();
 	}
 
     @Override
     protected void onResume() {
 
     	super.onResume();
 
     	_App._InApp = true;
 
         if(_App._ReloadSettings == true){
         	_App._ReloadSettings = false;
             _App.LoadSettings(this);
         	_App.InitTwitterClient();
         	startService(_App._NotiServices);
         }
 
         if(mOldAccount.equals(_App._Username) == false){
 			//String str = String.format("Changing to %s ...", _App._Username);
 			//Toast.makeText(_App._this, str, Toast.LENGTH_SHORT).show();
         	_Items.Clear();
         	LoadTweetItemsFromDB();
 			mOldAccount = _App._Username;
 			_Items.notifyDataSetChanged();
 			SetWindowTitle(_ActivityType);
         }
 
         _App._NotificationMgr.cancelAll();
 
         if(_App._ShowStatusbar == false){
         	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
         else{
         	getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
 
 //		final TextView rightText = (TextView) findViewById(R.id.title_api);
 //		rightText.setText(_App._TitleString);
 
 		if(_Refresh == true){
 			setMyProgressBarVisibility(true);
 		}
 
 		if(_App._HaveNotification == true){
 			_App._HaveNotification = false;
 			LoadTweetItemsFromDB();
 		}
     }
 
     @Override
     protected void onPause() {
 
  		new Thread(new Runnable(){
 			public void run() {
 				UpdateTweetItemsToDB();
 			}
  		}).start();
 
     	super.onPause();
 
     	_App._InApp = false;
     }
 
     private BroadcastReceiver mReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
 
 			if( (_ActivityType == TwitterClient.HOME_HOME && _App._Notification_home == true) ||
 				(_ActivityType == TwitterClient.HOME_MENTIONS && _App._Notification_mention == true) ||
 				( _ActivityType == TwitterClient.HOME_DIRECT && _App._Notification_direct == true) )
 			{
 
 				String str = String.format("Refreshing...");
 				Toast.makeText(_App._this, str, Toast.LENGTH_SHORT).show();
 
 				setMyProgressBarVisibility(true);
 				if (_App._twitter != null) _App._twitter.Get_timeline(_Handler, _ActivityType);
 				PanelAnimationOff(false, _Statuspanel);
 				PanelAnimationOff(false, _Toolbarpanel);
 			}
         }
     };
 
     SensorEventListener SensorEventListener = new SensorEventListener(){
 
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		}
 
 		public void onSensorChanged(SensorEvent event) {
 
 			if (_Refresh == true) return;
 
 			long curTime = System.currentTimeMillis();
 			float x,y,z;
 
 
 			if ((curTime - last_update) > 100) {
 				//long diffTime = (curTime - last_update);
 				last_update = curTime;
 
 				x = event.values[SensorManager.DATA_X];
 				y = event.values[SensorManager.DATA_Y];
 				z = event.values[SensorManager.DATA_Z];
 
 				double distance = Math.sqrt(Math.pow(x-last_x, 2) + Math.pow(y-last_y, 2) + Math.pow(z-last_z, 2));
 
 				if (distance >= 10 && distance <= 20) {
 					last_freecount = 0;
 					last_distance += distance;
 
 					if(last_distance >= 35){
 
 						String str = String.format("Refreshing...");
 						Toast.makeText(_App._this, str, Toast.LENGTH_SHORT).show();
 
 						_Refresh = true;
 						//_InRefresh = true;
 						last_distance = 0;
 						last_freecount = 0;
 
 						setMyProgressBarVisibility(true);
 						if (_App._twitter != null) _App._twitter.Get_timeline(_Handler, _ActivityType);
 						PanelAnimationOff(false, _Statuspanel);
 						PanelAnimationOff(false, _Toolbarpanel);
 					}
 
 				}else{
 					if( last_freecount >= 0){
 						last_freecount ++;
 						if(last_freecount  >= 5){
 							last_distance = 0;
 							last_freecount = -1;
 						}
 					}
 				}
 
 				last_x = x;
 				last_y = y;
 				last_z = z;
 			}
 		}
     };
 
 	protected void LoadTweetItemsFromDB(){
     	Cursor tweetCursor;
 
     	String limit = String.format("%d", _App._Tweetscount);
 
 		if(_ActivityType == TwitterClient.HOME_FRIENDS || _ActivityType == TwitterClient.HOME_FOLLOWERS){
 			tweetCursor = _App._DbHelper.fetchAll(_App._Username, _ActivityType, null, limit);
 		}else{
 			tweetCursor = _App._DbHelper.fetchAll(_App._Username, _ActivityType, " time DESC", limit);
 		}
 		if(tweetCursor == null) return;
     	if (tweetCursor.getCount() != 0) {
 			String screenname,title,text,source,replyid,iconuri,picurl,reteeted_screenname,reteeted_text;
 			long time,id;
 			boolean favorited,following;
 			int read;
 			tweetCursor.moveToFirst();
 			_Items.RemoveAll();
 			do{
 				screenname = tweetCursor.getString(DBTweetsHelper.COL_SCREENNAME);
 				title = tweetCursor.getString(DBTweetsHelper.COL_TITLE);
 				text = tweetCursor.getString(DBTweetsHelper.COL_TEXT);
 				time = tweetCursor.getLong(DBTweetsHelper.COL_TIME);
 				id = tweetCursor.getLong(DBTweetsHelper.COL_ID);
 				source = tweetCursor.getString(DBTweetsHelper.COL_SOURCE);
 				replyid = tweetCursor.getString(DBTweetsHelper.COL_REPLYID);
 				favorited = (tweetCursor.getInt(DBTweetsHelper.COL_FAVORITE) == 1)? true : false;
 				following = (tweetCursor.getInt(DBTweetsHelper.COL_FOLLOWING) == 1)? true : false;
 				read = tweetCursor.getInt(DBTweetsHelper.COL_READ);
 				iconuri = tweetCursor.getString(DBTweetsHelper.COL_ICONURL);
 				picurl = "";
 				reteeted_screenname = tweetCursor.getString(DBTweetsHelper.COL_RETWEETED_SCREENNAME);
 				reteeted_text = tweetCursor.getString(DBTweetsHelper.COL_RETWEETED_TEXT);
				_Items.addThread(read, screenname, title, text, time, source, id, replyid, favorited, following, iconuri, true, picurl);
 
 				//if(id > _LastID){
 				//	_LastID = id;
 				//}
 				//if(id < _PrevID){
 				//	_PrevID = id;
 				//}
 
 			}while (tweetCursor.moveToNext());
 			tweetCursor.close();
 
 			Date now = new Date();
 			if(_ActivityType == TwitterClient.HOME_HOME){
 				if(now.getTime() - time >= 30 * 60 * 1000){
 					mAddType = ADD_TYPE_REPLACE;
 //					_Items.addThread(READ_STATE_READ, "", "", "", 0, "", 0, "null", false, false, "", true);
 //					_Items.SetLoadingItem();
 //					_LastID = 0;
 //					_PrevID = Long.MAX_VALUE;
 				}
 			}
 
			_Items.addThread(READ_STATE_READ, "", "", "", 0, "", 0, "null", false, false, "", true, picurl);
 			_Items.SetLoadingItem();
 
 		}
     }
 
     protected long UpdateTweetItemsToDB(){
 
         int count = _Items.getCount();
         TwitterItem item;
         long ret = 0;
         ContentValues values = new ContentValues();
 
         for(int i=0;i<count && i < _App._Tweetscount;i++){
         	item = _Items.Get(i);
         	if(item != null){
         		item.mType = _ActivityType;
         		values.put(DBTweetsHelper.KEY_READ, item.mRead);
 	        	ret = _App._DbHelper.updatetweet(_App._Username, _ActivityType, item.mID,values);
         	}
         }
 
         return ret;
     }
 
     protected long SaveTweetItemsToDB(ArrayList<TwitterItem> items){
 
         int count = items.size();
         TwitterItem item;
         long ret = 0;
 
         for(int i=0;i<count;i++){
         	item = items.get(i);
         	if(TextUtils.isEmpty(item.mScreenname) == false){
         		item.mType = _ActivityType;
         		item.mAccount = _App._Username;
 	        	ret = _App._DbHelper.updatetweet(_ActivityType,item);
         	}
         }
 
         return ret;
     }
 
     protected long SaveTweetItemsToDB(){
 
         int count = _Items.getCount();
         TwitterItem item;
         long ret = 0;
 
         _App._DbHelper.deleteAll(_App._Username, _ActivityType);
 
         for(int i=0;i<count && i < _App._Tweetscount;i++){
         	item = _Items.Get(i);
         	if(item != null && TextUtils.isEmpty(item.mScreenname) == false){
         		item.mType = _ActivityType;
         		item.mAccount = _App._Username;
 	        	ret = _App._DbHelper.createtweet(_ActivityType,item);
         	}
         }
 
         return ret;
     }
 
 /*    protected void InitTwitterClient(){
 
         int port;
         String url,searchurl;
 
         if(_App._Https == true){
         	url = String.format("https://%s", _App._Baseapi);
         	searchurl = String.format("https://%s", _App._Searchapi);
         	port = PORTSSL;
         }else{
         	url = String.format("http://%s", _App._Baseapi);
         	searchurl = String.format("http://%s", _App._Searchapi);
         	port = PORT;
         }
 
         URI host = null;
 		try {
 			host = new URI(url);
 
 			_App._twitter = new TwitterClient(this, host.getHost(), port, url, searchurl, _App._Username, _App._Password);
 
 		} catch (URISyntaxException e) {
 			Bundle err = new Bundle();
 			err.putString(TwitterClient.KEY, e.getLocalizedMessage());
 			TwitterClient.SendMessage(_Handler, TwitterClient.HTTP_ERROR, err);
 		}
     }*/
 
  	protected void PanelAnimationOn(boolean top, ViewGroup layout){
 
  		if (layout.getVisibility() == View.INVISIBLE){
  			int height = layout.getHeight();
 
  			height = ( ( top == true ) ? ( height * (-1)) : height );
 
  			TranslateAnimation myAnimation1=new TranslateAnimation(0, 0, height, 0);
  			//AlphaAnimation myAnimation2=new AlphaAnimation(0.5f, 1f);
 
  			myAnimation1.setDuration(400);
  			myAnimation1.setInterpolator( new AccelerateInterpolator((float) 0.1) );
  			//myAnimation2.setDuration(200);
 
  			AnimationSet animSet = new AnimationSet(true);
 
  			animSet.addAnimation(myAnimation1);
  			//animSet.addAnimation(myAnimation2);
 
 	        layout.startAnimation(animSet);
 	        layout.setVisibility(View.VISIBLE);
  		}
  	}
 
  	protected void PanelAnimationOff(boolean top, ViewGroup layout){
 
  		if(layout == _Toolbarpanel && _App._AutoHideToolbar == false) return;
 
  		if (layout.getVisibility() == View.VISIBLE){
  			int height = layout.getHeight();
 
  			height = ( ( top == true ) ? ( height * (-1) ) : height );
 
  			TranslateAnimation myAnimation1=new TranslateAnimation(0, 0, 0, height);
  			//AlphaAnimation myAnimation2=new AlphaAnimation(1f, 0.5f);
 
  			myAnimation1.setDuration(400);
  			myAnimation1.setInterpolator( new DecelerateInterpolator((float) 0.1) );
  			//myAnimation2.setDuration(200);
 
  			AnimationSet animSet = new AnimationSet(true);
 
  			animSet.addAnimation(myAnimation1);
  			//animSet.addAnimation(myAnimation2);
 
 	        layout.startAnimation(animSet);
 	        layout.setVisibility(View.INVISIBLE);
  		}
  	}
 
  	private void EnableAllButtons(){
 
  		TwitterItem item = _Items.Get(_CurrentThread);
 
  		Button threads =  (Button)findViewById(R.id.thread);
 		Button delete =  (Button)findViewById(R.id.delete);
 
 		if(threads != null){
 			threads.setEnabled(false);
 			if(item != null){
 				String replyid = item.mReplyID;
 				if(replyid.length() != 0 && replyid.matches("null") == false){
 					threads.setEnabled(true);
 				}
 			}
 		}
 
 		if(delete != null){
 			delete.setEnabled(false);
 			if (item != null){
 				if(item.mScreenname.matches(_App._Username)){
 					delete.setEnabled(true);
 				}
 			}
 		}
  	}
 
  	private void doReplySingle(){
 
  		TwitterItem item = _Items.Get(_CurrentThread);
  		if(item == null) return;
 
 		String reply="";
 		EditText edit = (EditText)findViewById(R.id.EditText);
 		String name = "";
 
 		if(_Replies.size() == 0){
 			name =  "@" + item.mScreenname;
 		}else{
 			name = _Replies.get(0);
 		}
 
 		_InputType = INPUT_REPLY;
 
 		String oldtext = edit.getText().toString();
 		reply = oldtext;
 		if(item != null && oldtext.indexOf(name) == -1){
 			reply = String.format("%s %s", name ,oldtext);
 		}
 
 		edit.setText(reply);
 		edit.setSelection(reply.length());
 		PanelAnimationOn(false, _Statuspanel);
 		PanelAnimationOff(false, _Toolbarpanel);
 
  	}
 
  	private void doReplyMulti(){
 
  		TwitterItem item = _Items.Get(_CurrentThread);
  		if(item == null) return;
 
  		String name="";
 		String reply="";
 		EditText edit = (EditText)findViewById(R.id.EditText);
 		String oldtext = edit.getText().toString();
 
 		_InputType = INPUT_REPLY;
 
 		for(int i =0;i<_Replies.size();i++){
 			name = _Replies.get(i);
 			if(oldtext.indexOf(name) == -1){
 				reply +=name + " ";
 			}
 		}
 
 		edit.setText(String.format("%s%s", reply, oldtext));
 		edit.setSelection(reply.length());
 		PanelAnimationOn(false, _Statuspanel);
 		PanelAnimationOff(false, _Toolbarpanel);
 
  	}
 
  	private void doReply(){
 
 		TwitterItem item = _Items.Get(_CurrentThread);
 		if(item == null) return;
 
 		Matcher m2 = ItemAdapter.p2.matcher(item.mText);
 		String screenname = item.mScreenname.trim();
 		String name = "@" + _App._Username;
 
 		_Replies.clear();
 		if(name.equals("@" + screenname) == false){
 			_Replies.add("@" + screenname);
 		}
 		while(m2.find()){
 			name = m2.group();
 			if (_Replies.indexOf(name) == -1){
 				if(name.equals("@" + _App._Username) == false){
 					_Replies.add(name);
 				}
 			}
 		}
 
 		if(_Replies.size() <= 1){
 			doReplySingle();
 		}else{
 	        new AlertDialog.Builder(TweetsListActivity.this)
             .setTitle("Reply...")
             .setItems(new String[] {"Reply", "Relay all"}, new DialogInterface.OnClickListener() {
             	public void onClick(DialogInterface dialog, int which) {
             		if (which == 0) doReplySingle();
             		if (which == 1) doReplyMulti();
                 }
             })
             .show();
 		}
  	}
 
     public void InitButtons(){
 
         _Previewpanel = (LinearLayout)findViewById(R.id.PreviewPanel);
         if(_Previewpanel != null) _Previewpanel.setVisibility(View.GONE);
 
     	Button reply =  (Button)findViewById(R.id.reply);
     	Button retweet =  (Button)findViewById(R.id.retweet);
     	Button destory =  (Button)findViewById(R.id.delete);
     	Button thread =  (Button)findViewById(R.id.thread);
     	Button Send = (Button)findViewById(R.id.Send);
     	Button favorite = (Button)findViewById(R.id.favorite);
     	EditText edit = (EditText)findViewById(R.id.EditText);
     	Button gallery = (Button)findViewById(R.id.Gallery);
     	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //    	Button upload = (Button)findViewById(R.id.Upload);
     	Button posturl = (Button)findViewById(R.id.PostURL);
     	Button directmsg = (Button)findViewById(R.id.direcrmsg);
     	Button menubtn = (Button)findViewById(R.id.menubtn);
     	Button inbox = (Button)findViewById(R.id.inbox);
     	Button outbox = (Button)findViewById(R.id.outbox);
     	Button links = (Button)findViewById(R.id.links);
 
     	if(reply != null){
 	    	reply.setOnClickListener(new OnClickListener(){
 				public void onClick(View v) {
 					String reply="";
 					EditText edit = (EditText)findViewById(R.id.EditText);
 					if(_ActivityType == TwitterClient.HOME_DIRECT){
 						_InputType = INPUT_DIRECT;
 						reply = "";
 						edit.setText(reply);
 						edit.setSelection(reply.length());
 		 				PanelAnimationOn(false, _Statuspanel);
 						PanelAnimationOff(false, _Toolbarpanel);
 					}else{
 						doReply();
 					}
 				}
 	    	});
     	}
 
     	if(retweet != null){
 	    	retweet.setOnClickListener(new OnClickListener(){
 				public void onClick(View v) {
 					String retweet="";
 					EditText edit = (EditText)findViewById(R.id.EditText);
 					TwitterItem item = _Items.Get(_CurrentThread);
 					_InputType = INPUT_RETWEET;
 					if (item != null) retweet = String.format("RT @%s: %s ", item.mScreenname, item.mText);
 	 				edit.setText(retweet);
 					PanelAnimationOn(false, _Statuspanel);
 					PanelAnimationOff(false, _Toolbarpanel);
 				}
 	    	});
     	}
 
     	if(destory != null){
         	destory.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				if(_CurrentThread == -1) return;
     				try{
 	    	            new AlertDialog.Builder(_Context)
 	    	            .setTitle("Delete confirmation")
 	    	            .setMessage("Are you sure want to delete it?")
 	    	            .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	    	                public void onClick(DialogInterface dialog, int whichButton) {
 	    	                }
 	    	            })
 	    	            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	    	                public void onClick(DialogInterface dialog, int whichButton) {
 	    	                	TwitterItem item = _Items.Get(_CurrentThread);
 	    	                	if(item != null){
 		    	                	long id = item.mID;
 		    	                	setMyProgressBarVisibility(true);
 		    	    				PanelAnimationOff(false, _Statuspanel);
 		    	    				PanelAnimationOff(false, _Toolbarpanel);
 		    	    				if (_App._twitter != null) _App._twitter.Post_destory(_Handler, _ActivityType, id);
 	    	                	}
 	    	                }
 	    	            })
 	    	            .show();
     				}catch (Exception err){}
     			}
         	});
     	}
 
     	if(thread != null){
         	thread.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				TwitterItem item = _Items.Get(_CurrentThread);
     				if(item != null){
 	    				PanelAnimationOff(false, _Statuspanel);
 	    				PanelAnimationOff(false, _Toolbarpanel);
 	    				String id = String.valueOf(item.mID);
 	    		    	Intent i = new Intent(_Context, StatusesShowActivity.class);
 	    		    	i.putExtra("replyid", id);
 	    		    	startActivityForResult(i,APP_CHAINCLOSE);
     				}
     			}
         	});
     	}
 
     	if(Send != null){
          	Send.setOnClickListener(new OnClickListener(){
         		public void onClick(View v) {
 
         			TwitterItem item = _Items.Get(_CurrentThread);
         			Button gallery = (Button)findViewById(R.id.Gallery);
         			EditText edit = (EditText)findViewById(R.id.EditText);
         			String text = edit.getText().toString();
 
         			if(text.length() != 0){
         				if (_UploadFile == null){
 	         				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
 	         				inputMethodManager.hideSoftInputFromWindow(edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS );
 
 	        				setMyProgressBarVisibility(true);
 
 	        				if(_ActivityType == TwitterClient.HOME_SEARCH ){
 	        					if (_App._twitter != null) _App._twitter.Get_search(_Handler, text);
 	        				}else{
 		        				if(_InputType == INPUT_NEWPOST || _InputType == INPUT_RETWEET){
 		        					if (_App._twitter != null) _App._twitter.Post_statuses_post(_Handler, 0, text);
 		        				}else if (_InputType == INPUT_REPLY){
 		        					if(item != null){
 			        					long replyid = item.mID;
 			        					if (_App._twitter != null) _App._twitter.Post_statuses_post(_Handler, replyid, text);
 		        					}
 		        				}else if (_InputType == INPUT_DIRECT){
 		        					EditText rece = (EditText)findViewById(R.id.Receive);
 		        					String user = rece.getText().toString();
 		        					if(user.length() != 0){
 		        						if (_App._twitter != null) _App._twitter.Post_direct_messages_new(_Handler, user, text);
 		        					}else{
 		        						return;
 		        					}
 		        				}
 	        				}
 	        				v.setEnabled(false);
 	        				edit.setEnabled(false);
 	        				if(gallery != null)	gallery.setEnabled(false);
         				} else {
         		          	_Progressdialog = new ProgressDialog(_Context);
         		          	_Progressdialog.setMessage("Uploading picture ...");
         		          	_Progressdialog.setIndeterminate(true);
         		          	_Progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         		          	_Progressdialog.setCancelable(false);
         		          	_Progressdialog.show();
 
         		          	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //        		          	Button upload = (Button)findViewById(R.id.Upload);
         		          	Button posturl = (Button)findViewById(R.id.PostURL);
 
         		          	closepreview.setEnabled(false);
 //        		          	upload.setEnabled(false);
         		          	posturl.setEnabled(false);
 
         		          	if (_App._twitter != null) _App._twitter.Post_image(_Handler, _App._Pictureapi, _App._Username, _App._Password, _UploadFile, text);
         		          	_UploadFile = null;
         				}
         			}
         		}
 
          	});
     	}
 
     	if(favorite != null){
     		favorite.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				TwitterItem item = _Items.Get(_CurrentThread);
     				setMyProgressBarVisibility(true);
     	    		PanelAnimationOff(false, _Statuspanel);
     	    		PanelAnimationOff(false, _Toolbarpanel);
     				if(item != null){
     					long id = item.mID;
 	    	            if(item.mFavorite == false){
 	    	            	if (_App._twitter != null) _App._twitter.Post_favorites_create(_Handler, id);
 	    	            }else{
 	    	            	try{
 		        	            new AlertDialog.Builder(_Context)
 		        	            .setTitle("Delete confirmation")
 		        	            .setMessage("Are you sure want to unfavorite it?")
 		        	            .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		        	                public void onClick(DialogInterface dialog, int whichButton) {
 		        	                }
 		        	            })
 		        	            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		        	                public void onClick(DialogInterface dialog, int whichButton) {
 		        	                	TwitterItem item = _Items.Get(_CurrentThread);
 		        	                	if(item != null){
 			        	                	long id = item.mID;
 			        	                	if (_App._twitter != null) _App._twitter.Post_destory(_Handler, TwitterClient.HOME_FAVORITES, id);
 		        	                	}
 		        	                }
 		        	            })
 		        	            .show();
 	    	            	}catch (Exception err){}
 	    	            }
     				}
     			}
         	});
     	}
 
     	if(edit != null){
     		edit.addTextChangedListener(new TextWatcher(){
 
 				public void afterTextChanged(Editable arg0) {
 					TextView hint = (TextView)findViewById(R.id.HintTextLength);
 					Button Send = (Button)findViewById(R.id.Send);
     				if(hint != null){
 	    				int length = 140 - arg0.toString().length();
 	    				if (length > 10){
 	    					Send.setEnabled(true);
 	    					hint.setHintTextColor(-8355712);
 	    				}else if(length <= 10 && length >= 0 ){
 	    					Send.setEnabled(true);
 	    					hint.setHintTextColor(0x4fff0000 + 0x10000000 * (10 - length));
 	   					}else if(length <0){
 	    					hint.setHintTextColor(0xffff0000);
 	    					Send.setEnabled(false);
 	    				}
 
 	    				String srt = String.format("%d",length);
 	    				hint.setHint(srt);
     				}
 				}
 
 				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				}
 
 				public void onTextChanged(CharSequence s, int start,int before, int count) {
 				}
     		});
     	}
 
     	if(gallery != null){
     		gallery.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				String[] titles = {"Pick from gallery","Capture a picture"};
     	            try{
 	    				new AlertDialog.Builder(_Context)
 	                    .setTitle("Insert Picture ...")
 	                    .setItems(titles, new DialogInterface.OnClickListener() {
 	                        public void onClick(DialogInterface dialog, int which) {
 	                        	if(which == 0){
 	                	            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
 	                	            intent.setType("image/*");
 	                	            startActivityForResult(intent, 1);
 	                        	}else{
 	                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 	                                startActivityForResult(intent,2);
 
 	                        	}
 	                        }
 	                    })
 	                    .show();
     	            }catch (Exception err){}
     			}
         	});
     	}
 
     	if(closepreview != null){
     		closepreview.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     	        	ImageView view = (ImageView) findViewById(R.id.ImagePrview);
     	        	view.setImageBitmap(null);
     	        	_Previewpanel.setVisibility(View.GONE);
     	        	_PictureURL = "";
     	        	if (_bitmap != null){
     	        		_bitmap.recycle();
     	        		_bitmap = null;
     	        	}
     	        	_UploadFile = null;
     			}
         	});
     	}
 
     	if(posturl != null){
     		posturl.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
                 	EditText edit = (EditText)findViewById(R.id.EditText);
                 	String str = edit.getEditableText().toString();
         			if(_PictureURL.length() != 0){
         				edit.setText(_PictureURL + " " + str);
         			}
     			}
         	});
     	}
 
 //    	if(upload != null){
 //    		upload.setOnClickListener(new OnClickListener(){
 //    			public void onClick(View v) {
 //    	        	if (_UploadFile == null) return;
 //
 //                	_Progressdialog = new ProgressDialog(_Context);
 //                	_Progressdialog.setMessage("Uploading picture ...");
 //                	_Progressdialog.setIndeterminate(true);
 //                	_Progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 //                	_Progressdialog.setCancelable(false);
 //                	_Progressdialog.show();
 //
 //                	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //                	Button upload = (Button)findViewById(R.id.Upload);
 //                	Button posturl = (Button)findViewById(R.id.PostURL);
 //
 //                	closepreview.setEnabled(false);
 //                	upload.setEnabled(false);
 //                	posturl.setEnabled(false);
 //
 //                	if (_App._twitter != null) _App._twitter.Post_image(_Handler, _App._Pictureapi, _App._Username, _App._Password, _UploadFile);
 //    			}
 //        	});
 //    	}
 
     	if(directmsg != null){
     		directmsg.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
 					TwitterItem item = _Items.Get(_CurrentThread);
 					if(item != null){
 	    				String receiver = item.mScreenname;
 	    		    	Intent i = new Intent(_Context, DirectActivity.class);
 	    		    	i.putExtra("receiver", receiver);
 	    		    	startActivityForResult(i, APP_CHAINCLOSE);
 					}
     			}
         	});
     	}
 
     	if(menubtn != null){
     		menubtn.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				openOptionsMenu();
     			}
         	});
     	}
 
     	if(inbox != null){
     		inbox.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     	    		PanelAnimationOff(false,_Toolbarpanel);
     	    		PanelAnimationOff(false,_Statuspanel);
     				_ActivityType = TwitterClient.HOME_DIRECT;
     				_Items.Clear();
     				LoadTweetItemsFromDB();
     				_Items.notifyDataSetChanged();
     				SetWindowTitle(_ActivityType);
     			}
         	});
     	}
 
     	if(outbox != null){
     		outbox.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     	    		PanelAnimationOff(false,_Toolbarpanel);
     	    		PanelAnimationOff(false,_Statuspanel);
     				_ActivityType = TwitterClient.HOME_DIRECT_SENT;
     				_Items.Clear();
     				LoadTweetItemsFromDB();
     				_Items.notifyDataSetChanged();
     				SetWindowTitle(_ActivityType);
     			}
         	});
     	}
 
     	if(links != null){
 
     		if(_App._LongClick == true){
     			links.setVisibility(View.GONE);
     		}else{
     			links.setVisibility(View.VISIBLE);
     		}
 
     		links.setOnClickListener(new OnClickListener(){
     			public void onClick(View v) {
     				if(_CurrentThread == -1) return;
     				defaultOnItemLongClick(_CurrentThread);
     			}
         	});
     	}
 
     }
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		if(keyCode == KeyEvent.KEYCODE_BACK){
 
 			if( _Statuspanel.getVisibility() == View.VISIBLE || (_App._AutoHideToolbar == true && _Toolbarpanel.getVisibility() == View.VISIBLE ))
 			{
 				PanelAnimationOff(false, _Statuspanel);
  				PanelAnimationOff(false, _Toolbarpanel);
 
 				EditText edit = (EditText)findViewById(R.id.EditText);
 				if(edit != null){
 					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
 					inputMethodManager.hideSoftInputFromWindow(edit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS );
 				}
 				return false;
 			}
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public int findPositionByIDFromUser(Message msg){
 		String json = msg.getData().getString(TwitterClient.KEY);
  		try{
  			JSONObject aline = new JSONObject(json);
  			JSONObject status = aline.getJSONObject("status");
  			long id = status.getLong("id");
  			for(int i=0;i<_Items.getCount();i++){
  				if(id == _Items.Get(i).mID){
  					return i;
  				}
  			}
  		}catch (JSONException e) {
 
 			Bundle err = new Bundle();
 			err.putString(TwitterClient.KEY, json);
 			TwitterClient.SendMessage(_Handler, TwitterClient.HTTP_ERROR, err);
 			return -1;
 		}
 
 		return -1;
 	}
 
 	public int findPositionByIDFromStatus(Message msg){
 		String json = msg.getData().getString(TwitterClient.KEY);
  		try{
  			JSONObject aline = new JSONObject(json);
  			long id = aline.getLong("id");
  			for(int i=0;i<_Items.getCount();i++){
  				if(id == _Items.Get(i).mID){
  					return i;
  				}
  			}
  		}catch (JSONException e) {
 
 			Bundle err = new Bundle();
 			err.putString(TwitterClient.KEY, json);
 			TwitterClient.SendMessage(_Handler, TwitterClient.HTTP_ERROR, err);
 			return -1;
 		}
 
 		return -1;
 	}
 
 	public void defaultonItemClick(int position, Bundle b) {
 		if (position >= _Items.getCount() || position < 0 ) return;
 		int count = _Items.getCount() - 1;
 		if(position == count){
 			if(_Items.Get(count).mScreenname.length() == 0){
 				if (_StartLoading == false && _Refresh == false){
 					_Items.SetStartLoadingItem();
 					_Refresh = true;
 					setMyProgressBarVisibility(true);
 					_StartLoading = true;
 					long _PrevID = Long.MAX_VALUE;;
 					TwitterItem item = _Items.Get(count - 1);
 					if (item != null) _PrevID = item.mID;
 					if(_ActivityType == TwitterClient.HOME_SEARCH){
 						EditText edit = (EditText)findViewById(R.id.EditText);
 						String query = edit.getText().toString();
 						if(query.length() != 0){
 							if (_App._twitter != null) _App._twitter.Get_search_next(_Handler, query, _PrevID);
 						}else{
 							query = b.getString("search");
 							if (_App._twitter != null) _App._twitter.Get_search_next(_Handler, query, _PrevID);
 						}
 					}else if (_ActivityType == TwitterClient.HOME_USERINFO){
 						String user = _Items.Get(0).mScreenname;
 						if(user.length() !=0){
 							if (_App._twitter != null) _App._twitter.Get_user_timeline_max(_Handler, user, _PrevID);
 						}
 					}else if (_ActivityType == TwitterClient.HOME_TRENDS){
 						;
 					}else{
 						if (_App._twitter != null) _App._twitter.Get_timeline_max(_Handler, _ActivityType, _PrevID);
 					}
 					return;
 				}else {
 		 			//_Items.notifyDataSetChanged();
 		 			return;
 				}
 			}
 		}
 
 //		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 //	    clipboard.setText(_Items.Get(position).mScreenname);
 
 		_CurrentThread = position;
 		PanelAnimationOn(false, _Toolbarpanel);
 		PanelAnimationOff(false, _Statuspanel);
 		_Items.notifyDataSetInvalidated();
 
 		_Items.Get(position).mRead = READ_STATE_READ;
 
 		EnableAllButtons();
 	}
 
 	private void defaultOnItemLongClick(int position){
 
 		if(position >= _Items.getCount() || position < 0) return;
 
 		boolean popdlg = true;
 		String link = "";
 		String text = _Items.Get(position).mText;
 		String screenname = _Items.Get(position).mScreenname.trim();
 
 		if(screenname.length() == 0) return;
 
 		_CurrentThread = position;
 
 		_Links.clear();
 
 		Matcher m1 = ItemAdapter.p1.matcher(text);
     	Matcher m2 = ItemAdapter.p2.matcher(text);
     	Matcher m3 = ItemAdapter.p3.matcher(text);
 
     	_Links.add("@" + screenname);
 
 		while(m2.find()){
 			 link = m2.group();
 			 if (_Links.indexOf(link) == -1){
 				 _Links.add(link);
 			 }
 		}
 
 		while(m1.find()){
 			 link = m1.group();
 			 if (_Links.indexOf(link) == -1){
 				 _Links.add(link);
 			 }
 		}
 
 		while(m3.find()){
 			 link = m3.group();
 			 if (_Links.indexOf(link) == -1){
 				 _Links.add(link);
 			 }
 		}
 
 		_Links.add("Share ...");
 		_Links.add("Mark as unread");
 		_Links.add("Copy to clipboard");
 
 		if( popdlg == true){
 
             int count = _Links.size();
             String[] links = new String[count];
             for (int i=0;i<count;i++){
             	links[i] = _Links.get(i);
             }
 
             try{
 		        new AlertDialog.Builder(TweetsListActivity.this)
 	            .setTitle("View Content")
 	            .setItems(links, new DialogInterface.OnClickListener() {
 	            	public void onClick(DialogInterface dialog, int which) {
 	            		if(_Links.get(which).equals("Share ...")){
 	            			TwitterItem item = _Items.Get(_CurrentThread);
 	            			if(item != null){
 		            			try {
 		            				Intent intent = new Intent();
 		            				intent.setAction(Intent.ACTION_SEND);
 		            				intent.setType("text/plain");
 		            				intent.putExtra(Intent.EXTRA_TEXT, item.mText);
 		            				startActivity(Intent.createChooser(intent, "Share Content"));
 		            			} catch (Exception ex) {
 		            				Toast.makeText(_Context, "Can't launch application" ,Toast.LENGTH_SHORT).show();
 		            			}
 	            			}
             			}else if(_Links.get(which).equals("Mark as unread")){
             				TwitterItem item = _Items.Get(_CurrentThread);
             				item.mRead = READ_STATE_MARKUNREAD;
             				_Items.notifyDataSetChanged();
             			}else if(_Links.get(which).equals("Copy to clipboard")){
             				TwitterItem item = _Items.Get(_CurrentThread);
             				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
             			    clipboard.setText(item.mText);
             			}else if(_Links.get(which).indexOf("@") == 0){
 	            			Intent i = new Intent(_Context, UserInfoActivity.class);
 	            			i.putExtra("user", _Links.get(which).substring(1));
 	                        startActivityForResult(i,APP_CHAINCLOSE);
 	            		}else if(_Links.get(which).indexOf("#") == 0){
 	            			Intent i = new Intent(_Context, SearchActivity.class);
 	            			i.putExtra("search", _Links.get(which).substring(1));
 	                        startActivityForResult(i,APP_CHAINCLOSE);
 	            		}else{
 	            			Uri uri = Uri.parse(_Links.get(which));
 	            			Intent it  = new Intent(Intent.ACTION_VIEW,uri);
 	            			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 	            		    clipboard.setText(uri.toString());
 	            			startActivity(it);
 	            		}
 	                }
 	            })
 	            .show();
             }catch (Exception err){}
 		}
 	}
 
  	private OnItemLongClickListener OnItemLongClickListener = new OnItemLongClickListener(){
 
 		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 
 			defaultOnItemLongClick(position);
 
 			return true;
 		}
 
  	};
 
  	private OnScrollListener OnScrollListener = new OnScrollListener (){
 
  		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,int totalItemCount){
  		}
 
  		public void onScrollStateChanged(AbsListView view, int scrollState) {
 
  			_Items.SetScrollState(scrollState);
 
  	        switch (scrollState) {
  	        case SCROLL_STATE_IDLE:
  	        {
  	            mScrolling = false;
 
  	            int firstVisibleItem = view.getFirstVisiblePosition();
  	            int lastVisibleItem = view.getLastVisiblePosition();
  	 			TwitterItem temp;
 
  	 			for(int i=0;i<_Items.getCount();i++){
  	 				if(i < firstVisibleItem || i > lastVisibleItem ){
  	 					temp = _Items.Get(i);
  	 					if(temp != null){
  	 						if(temp.mRead == READ_STATE_UNREAD) temp.mRead = READ_STATE_READ;
  	 					}
  	 				}
  	 			}
  	 			_Items.notifyDataSetChanged();
  	            break;
  	        }
  	        case SCROLL_STATE_TOUCH_SCROLL:
  	            mScrolling = true;
  	            if (_CurrentThread != -1){
  	            	PanelAnimationOff(false, _Toolbarpanel);
  	            }
  	            break;
  	        case SCROLL_STATE_FLING:
  	            mScrolling = true;
  	            break;
  	        }
 		}
  	};
 
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 
 		float y;
 		int action = event.getAction();
 		long curTime;
 
 		if(action ==  MotionEvent.ACTION_DOWN)
 		{
 			y = event.getY();
 
 			if(y <= 50){
 				curTime = System.currentTimeMillis();
 
 				if( curTime - last_click_time <= 300){
 					_list.setSelection(0);
 				}
 				else{
 					last_click_time = curTime;
 				}
 			}
 		}
 		return super.onTouchEvent(event);
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         //_Menu = menu;
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main,menu);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	super.onMenuItemSelected(featureId, item);
 
     	Intent i;
 
     	switch(item.getItemId()) {
         case R.id.home:
         	i = new Intent(this, HomeActivity.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.mentions:
         	i = new Intent(this, MentionActivity.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.direct:
         	i = new Intent(this, DirectActivity.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.favorites:
         	i = new Intent(this, FavoriteActivity.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.search:
         	i = new Intent(this, SearchActivity.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.setting:
         	i = new Intent(this, Setting.class);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
         	break;
         case R.id.exit:
         	setResult(APP_CHAINCLOSE);
         	finish();
             if(_ActivityType == TwitterClient.HOME_HOME){
             	if(_App._NotiServices != null){
             		stopService(_App._NotiServices);
            		}
             }
         	break;
         case R.id.trends_current:
         	i = new Intent(this, TrendsActivity.class);
         	i.putExtra("trends_type",TwitterClient.TRENDS_CURRENT);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.trends_daily:
         	i = new Intent(this, TrendsActivity.class);
         	i.putExtra("trends_type",TwitterClient.TRENDS_DAILY);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.trends_weekly:
         	i = new Intent(this, TrendsActivity.class);
         	i.putExtra("trends_type",TwitterClient.TRENDS_WEEKLY);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.following:
         	i = new Intent(this, UserStatusesActivity.class);
         	i.putExtra("type",TwitterClient.HOME_FRIENDS);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.followers:
         	i = new Intent(this, UserStatusesActivity.class);
         	i.putExtra("type",TwitterClient.HOME_FOLLOWERS);
         	if(_App._MainAtivity != null) _App._MainAtivity.startActivityForResult(i,APP_CHAINCLOSE);
             if(_ActivityType != TwitterClient.HOME_HOME) finish();
         	break;
         case R.id.newpost:
 			EditText edit = (EditText)findViewById(R.id.EditText);
 			edit.setSelection(edit.getText().toString().length());
 			PanelAnimationOn(false, _Statuspanel);
 			PanelAnimationOff(false, _Toolbarpanel);
 			if(_ActivityType == TwitterClient.HOME_DIRECT){
 				_InputType = INPUT_DIRECT;
 			}else{
 				_InputType = INPUT_NEWPOST;
 			}
         	break;
     	}
 
         return true;
     }
 
     public void defaulthandleMessage(final Message msg) {
 
     	String ErrorMsg;
 
     	switch (msg.what){
     	case TwitterClient.HTTP_POSTIMAGE_ERROR:
     		{
     			if (_Progressdialog.isShowing()) _Progressdialog.dismiss();
             	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //            	Button upload = (Button)findViewById(R.id.Upload);
             	Button posturl = (Button)findViewById(R.id.PostURL);
             	closepreview.setEnabled(true);
 //            	upload.setEnabled(true);
             	posturl.setEnabled(true);
             	ErrorMsg = String.format("%s", msg.getData().getString(TwitterClient.KEY));
             	try{
             		new AlertDialog.Builder(_Context)
             		.setTitle("Error")
             		.setMessage(ErrorMsg)
             		.setNegativeButton("OK", new DialogInterface.OnClickListener() {
             			public void onClick(DialogInterface dialog, int whichButton) {
             			}
             		})
             		.show();
             	}catch (Exception err){}
     			break;
     		}
     	case TwitterClient.HTTP_POSTIMAGE_PROGRESS:
     		{
     			if(_Progressdialog.isShowing()){
     				int max = msg.getData().getInt("progress_max");
     				int cur = msg.getData().getInt("progress_cur");
     				_Progressdialog.setIndeterminate(false);
     				_Progressdialog.setMax(max);
     				_Progressdialog.setProgress(cur);
     			}
     			break;
     		}
     	case TwitterClient.HTTP_POSTIMAGE_SUCCESSFUL:
     		{
     			if (_Progressdialog.isShowing()) _Progressdialog.dismiss();
             	Button closepreview = (Button)findViewById(R.id.ClosePreview);
 //            	Button upload = (Button)findViewById(R.id.Upload);
             	Button posturl = (Button)findViewById(R.id.PostURL);
             	closepreview.setEnabled(true);
 //            	upload.setEnabled(true);
             	posturl.setEnabled(true);
 
             	EditText edit = (EditText)findViewById(R.id.EditText);
             	String str = edit.getEditableText().toString();
             	String response = msg.getData().getString(TwitterClient.KEY);
 
     			Matcher m1 = ItemAdapter.p1.matcher(response);
 
     			while(m1.find()){
     				_PictureURL = m1.group();
     				break;
     			}
 
     			if(_PictureURL.length() != 0){
     				edit.setText(_PictureURL + " " + str);
     			}
     			break;
     		}
 		case TwitterClient.HTTP_ERROR:
 			{
 				_Refresh = false;
 				_StartLoading = false;
 				setMyProgressBarVisibility(false);
 				ErrorMsg = msg.getData().getString(TwitterClient.KEY);
 				if (_ActivityType != TwitterClient.HOME_STATUSES_SHOW) {
 					_Items.SetLoadingItem();
 					_Items.notifyDataSetChanged();
 				}
 				Button Send = (Button)findViewById(R.id.Send);
 				Send.setEnabled(true);
 				EditText edit = (EditText)findViewById(R.id.EditText);
 				edit.setEnabled(true);
 				Button gallery = (Button)findViewById(R.id.Gallery);
 				if(gallery != null) gallery.setEnabled(true);
             	ErrorMsg = String.format("Network Error: %s", msg.getData().getString(TwitterClient.KEY));
 				/*try{
 			        new AlertDialog.Builder(_Context)
 			        .setTitle("Error")
 			        .setMessage(ErrorMsg)
 			        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
 			            public void onClick(DialogInterface dialog, int whichButton) {
 			            }
 			        })
 			        .show();
 				}catch (Exception err){}*/
 				Toast.makeText(this, Html.fromHtml(ErrorMsg), Toast.LENGTH_LONG).show();
 				break;
 			}
 
 		case TwitterClient.HTTP_FETCH_IMAGE:
 			{
 				_Items.notifyDataSetChanged();
 				break;
 			}
     	case TwitterClient.HTTP_STATUSES_UPDATE:
     		{
 				EditText edit = (EditText)findViewById(R.id.EditText);
 				Button send = (Button)findViewById(R.id.Send);
 				Button gallery = (Button)findViewById(R.id.Gallery);
 				ImageView view = (ImageView) findViewById(R.id.ImagePrview);
 				edit.setText("");
 				edit.setEnabled(true);
 				send.setEnabled(true);
 				if(gallery !=null) gallery.setEnabled(true);
 				PanelAnimationOff(false,_Toolbarpanel);
 				PanelAnimationOff(false,_Statuspanel);
 	        	view.setImageBitmap(null);
 	        	if (_Previewpanel != null) _Previewpanel.setVisibility(View.GONE);
 	        	_PictureURL = "";
 	        	if (_bitmap != null){
 	        		_bitmap.recycle();
 	        		_bitmap = null;
 	        	}
 	        	setMyProgressBarVisibility(false);
 				break;
     		}
     	case TwitterClient.HTTP_FAVORITES_DESTORY:
     	case TwitterClient.HTTP_FAVORITES_CREATE:
 			{
 				setMyProgressBarVisibility(false);
 				String json = msg.getData().getString(TwitterClient.KEY);
 				TwitterItem item = null;
 
 		 		try{
 		 			JSONObject aline = new JSONObject(json);
 		 			long id = aline.getLong("id");
 		 			for(int i=0;i<_Items.getCount();i++){
 		 				item = _Items.Get(i);
 		 				if(id == item.mID){
 		 					item.mFavorite = !aline.getBoolean("favorited");
 		 					break;
 		 				}
 		 			}
 		 		}catch (JSONException e) {
 
 					Bundle err = new Bundle();
 					err.putString(TwitterClient.KEY, e.getLocalizedMessage());
 					TwitterClient.SendMessage(_Handler, TwitterClient.HTTP_ERROR, err);
 					return;
 				}
 				_Items.notifyDataSetChanged();
 				break;
 			}
     	case TwitterClient.HTTP_RATE_LIMIT:
     		try{
 	    		TweetsData data = (TweetsData) msg.getData().getSerializable(TwitterClient.KEY);
 	    		if(data == null) return;
 	    		int remain = Integer.valueOf(data.mJSONObject.getString("remaining_hits"));
 	    		int limit = Integer.valueOf(data.mJSONObject.getString("hourly_limit"));
 	    		String str = String.format("%d%%", (remain * 100) / limit);
 //	    		final TextView rightText = (TextView) findViewById(R.id.title_api);
 //	    		rightText.setText(str);
 	    		_App._TitleString = str;
     		} catch (JSONException e1){} catch (Exception e2){}
     	}
     }
 
 	public void defaultUpdateListViewThread(Bundle bundle,int addtype, boolean order){
 		bundle.putInt("append", addtype);
 		new defaultUpdateListAsyncTask().execute(bundle);
     }
 
 	public void setMyProgressBarVisibility(boolean visibilty){
 		ProgressBar progressbar = (ProgressBar) findViewById(R.id.titleProgressBar);
 		progressbar.setVisibility(visibilty == true? View.VISIBLE: View.INVISIBLE);
 	}
 
     public void addThread(ArrayList<TwitterItem> items, TwitterItem obj){
     	obj.mTimeSource = CreateTimeSource(obj.mTime, obj.mSource);
     	items.add(new TwitterItem(obj));
      }
 
 	public boolean defaultDecodeJSON(ArrayList<TwitterItem> items, TwitterItem item){
 		if (_App._twitter != null) _App._twitter.FrechImg(_Handler, item.mImageurl,item.mScreenname);
  		addThread(items, item);
  		return true;
     }
 
 	public void defaultUpdateListViewProcessFrontEnd(ProgressData tweets){
 
 		ArrayList<TwitterItem> items = tweets.items;
 
  		if(items.size() == 0) {
  			_Items.SetLoadingItem();
  			TwitterClient.SendMessage(_Handler, TwitterClient.UI_REFRESHVIEWS, null);
  			return;
  		}
 
  		int index;
  		int count;
 
  		int addtype = tweets.addtype;
  		int mincount = Math.min(items.size(), _Items.getCount());
  		count = items.size();
  		TwitterItem t;
 
  		if(addtype == ADD_TYPE_INSERT){
 	 		for (index = 0; index < count; index++){
 	 			t = items.get(index);
 	 			if(t == null) continue;
 	 			_Items.addThread(index, t, addtype, _ActivityType);
 	 		}
 
 	 		if(_Items.getCount() >= TwitterClient.MAX_TWEETS_COUNT){
 	 			for (index = _Items.getCount() - 1; index >= _App._Tweetscount; index = _Items.getCount() - 1){
 	 				_Items.Remove(index);
 	 			}
 	 		}
  		}else if (addtype == ADD_TYPE_REPLACE){
  			mAddType = ADD_TYPE_INSERT;
  	 		for (index = 0; index < mincount; index++){
 	 			t = items.get(index);
 	 			if(t == null) continue;
  	 			_Items.ReplaceThread(index, t, _ActivityType);
   			}
 
   			for (; index < count; index++){
 	 			t = items.get(index);
 	 			if(t == null) continue;
 				_Items.addThread(index, t, addtype, _ActivityType);
  	 		}
 
  	 		for (index = _Items.getCount() - 1; index >= count;index = _Items.getCount() - 1){
  	 			_Items.Remove(index);
  	 		}
  		} else if (addtype == ADD_TYPE_APPEND){
  			if(count !=0 && _Items.getCount() != 0){
  	 			_Items.Remove(_Items.getCount() - 1);
  	 		}
 
  			for (index = 0; index < count; index++){
 	 			t = items.get(index);
 	 			if(t == null) continue;
  				_Items.addThread(index, t, addtype, _ActivityType);
  			}
  		}
 
  		//_Items.notifyDataSetChanged();
 
  		if (items.size() != 0 && _Items.getCount() <= TwitterClient.MAX_TWEETS_COUNT){
  			_Items.addThread(READ_STATE_READ, "", "", "", 0, "", 0, "null", false, false, "", true, "");
  			_Items.SetLoadingItem();
  		}
 
  		new Thread(new Runnable(){
 				public void run() {
 					SaveTweetItemsToDB();
 				}
 		}).start();
 
  		TwitterClient.SendMessage(_Handler, TwitterClient.UI_REFRESHVIEWS, null);
 	}
 
 	public void defaultUpdateListViewProcessBackEnd(ProgressData tweets, Bundle bundle){
 
 		ArrayList<TwitterItem> items = tweets.items;
 
 		if (_App._twitter != null) _App._twitter.Get_rate_limit_status(_Handler);
 
 		TweetsData data = (TweetsData) bundle.getSerializable(TwitterClient.KEY);
 
  		if(data == null || data.items == null) return;
 
  		int index = 0;
  		boolean ret = false;
  		Bundle userdata = new Bundle();
  		int count = ( (data.items == null) ? 0 : data.items.size() );
  		TwitterItem insertitem;
  		int readstate;
 
 		for (index = 0; index < count; index++){
 			insertitem = data.Get(index);
 			if(insertitem != null){
 				readstate = _App._DbHelper.FindTweet(_App._Username, _ActivityType, insertitem.mID) == false ? READ_STATE_UNKNOW : READ_STATE_READ;
 				if( mAddType != ADD_TYPE_REPLACE && readstate == READ_STATE_READ){
 					items.add(null);
 					continue;
 				}
 				insertitem.mRead = readstate;
 				ret = defaultDecodeJSON(items, insertitem);
 			}
 		}
 
 		count = _Items.getCount();
 		TwitterItem item;
 		for(index=0;index<count;index++){
 			item = _Items.Get(index);
 			if(item != null){
 				item.mTimeSource = CreateTimeSource(item.mTime, item.mSource);;
 			}
 		}
 
 	 	if(_ActivityType == TwitterClient.HOME_USERINFO && data.user != null){
 
 	 		userdata.putBoolean("ret", ret);
 	 		userdata.putSerializable(TwitterClient.KEY, data.user);
 	 		TwitterClient.SendMessage(_Handler, TwitterClient.UI_REFRESHVIEWS_PRE, userdata);
 	 	}
 	}
 
 	private class defaultUpdateListAsyncTask extends AsyncTask <Bundle, ProgressData, Long> {
 
 		@Override
 		protected void onPreExecute(){
 			_Refresh = true;
 			setMyProgressBarVisibility(true);
 		}
 
 		@Override
 		protected Long doInBackground(Bundle... bundle) {
 	 		try{
 	 			ProgressData tweets = new ProgressData();
 		 		int addtype = bundle[0].getInt("append");
 		 		tweets.addtype = addtype;
 				defaultUpdateListViewProcessBackEnd(tweets, bundle[0]);
 				publishProgress(tweets);
 	 		} catch (Exception e){}
 			return null;
 		}
 
         protected void onProgressUpdate(ProgressData ... tweets) {
 			defaultUpdateListViewProcessFrontEnd(tweets[0]);
 		}
 
         @Override
         protected void onPostExecute(Long Result){
         	_Refresh = false;
         }
 
 	}
 
     static public String CreateTimeSource(long time, String source){
     	StringBuilder timesource = new StringBuilder();
 
     	if(time != 0){
     		timesource.append(TwitterClient.GetSmartTimeString(time));
     	}
 
     	if(source.length() != 0){
     		timesource.append(" from ");
     		timesource.append(source);
     	}
 
     	return timesource.toString();
     }
 }
