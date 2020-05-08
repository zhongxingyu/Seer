 package net.meneame.fisgodroid;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 
 public class FisgoService extends Service
 {
     private static final String TAG = "FisgoService";
     private static final String LOGIN_GET_URL = "http://www.meneame.net/login.php";
     private static final String LOGIN_URL = "https://www.meneame.net/login.php";
     private static final String SNEAK_URL = "http://www.meneame.net/sneak.php";
     private static final String SNEAK_BACKEND_URL = "http://www.meneame.net/backend/sneaker2.php";
     private static final String FRIEND_LIST_URL = "http://www.meneame.net/user/?username/friends";
     private static final String UPLOAD_URL = "http://www.meneame.net/backend/tmp_upload.php";
 
     private FisgoBinder mBinder = new FisgoBinder();
     private Thread mThread = null;
     private boolean mIsLoggedIn = false;
     private boolean mIsAdmin = false;
     private IHttpService mHttp = new HttpService();
     private List<ChatMessage> mMessages = new ArrayList<ChatMessage>();
     private List<String> mFriendNames = new ArrayList<String>();
     private String mLastMessageTime = "";
     private String mUsername;
     private String mMyKey;
     private List<String> mOutgoingMessages = new LinkedList<String>();
     private AvatarStorage mAvatars;
     private ChatType mType = ChatType.PUBLIC;
     private int mNumRequests = 0;
     private int mTimeToWait = 5000;
     private int mTimeToWaitWhenFailed = 10000;
     private int mTimeToWaitWhenOnBackground = 15000;
     private boolean mIsOnForeground = false;
     private BroadcastReceiver mConnectivityReceiver;
 
     @Override
     public void onCreate()
     {
         mAvatars = new AvatarStorage(getApplicationContext());
         mTimeToWait = getResources().getInteger(R.integer.time_to_wait);
         mTimeToWaitWhenFailed = getResources().getInteger(R.integer.time_to_wait_when_failed);
         mTimeToWaitWhenOnBackground = getResources().getInteger(R.integer.time_to_wait_when_on_background);
         
         // Register a BroadcastReceiver to detect connectivity changes
         final IntentFilter connectivityIntentFilter = new IntentFilter();
         connectivityIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         mConnectivityReceiver = new BroadcastReceiver ()
         {
             @Override
             public void onReceive(Context context, Intent intent)
             {
                 wakeUp();
             }
         };
         registerReceiver(mConnectivityReceiver, connectivityIntentFilter);
         
         
         // Create the main thread that will keep polling the server
         mThread = new Thread(new Runnable()
         {
             @Override
             public void run()
             {
                 final ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                 
                 synchronized (mHttp)
                 {
                     while (true)
                     {
                         try
                         {
                             if ( !mIsLoggedIn )
                             {
                                 clearSession();
                                 mHttp.wait();
                             }
                             
                             final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                             boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
                             if ( !isConnected )
                             {
                                 mHttp.wait();
                             }
 
                             boolean failed = false;
                             ByteArrayOutputStream result = new ByteArrayOutputStream();
                             boolean containsChat = mOutgoingMessages.size() > 0;
 
                             if ( containsChat )
                             {
                                 Map<String, Object> params = new HashMap<String, Object>();
 
                                 params.put("k", mMyKey);
                                 params.put("v", 5);
                                 params.put("r", ++mNumRequests);
                                 params.put("chat", mOutgoingMessages.get(0));
                                 params.put("nopost", 1);
                                 params.put("novote", 1);
                                 params.put("noproblem", 1);
                                 params.put("nocomment", 1);
                                 params.put("nonew", 1);
                                 params.put("nopublished", 1);
                                 params.put("nopubvotes", 1);
                                 
                                 // Request the appropiate chat type
                                 if ( mType == ChatType.FRIENDS )
                                     params.put("friends", 1);
                                 else if ( mType == ChatType.ADMIN )
                                     params.put("admin", 1);
                                 
                                 if ( mLastMessageTime.equals("") == false )
                                     params.put("time", mLastMessageTime);
                                 
                                 
                                 failed = !mHttp.post(SNEAK_BACKEND_URL, params, result);
 
                                 if ( !failed && result.size() > 0 )
                                     mOutgoingMessages.remove(0);
                             }
                             else
                             {
                                 // Build the request parameters
                                 String uri = SNEAK_BACKEND_URL + "?nopost=1&novote=1&noproblem=1&nocomment=1" + "&nonew=1&nopublished=1&nopubvotes=1&v=5&r=" + (++mNumRequests);
                                 // If we have previous messages, get only the
                                 // new ones
                                 if ( mLastMessageTime.equals("") == false )
                                 {
                                     uri += "&time=" + mLastMessageTime;
                                 }
 
                                 // Request the appropiate chat type
                                 if ( mType == ChatType.FRIENDS )
                                     uri += "&friends=1";
                                 else if ( mType == ChatType.ADMIN )
                                     uri += "&admin=1";
 
                                 failed = !mHttp.get(uri, result);
                             }
 
                             // Get the response JSON value and construct the
                             // chat messages from it
                             if ( result.size() > 0 )
                             {
                                 JSONObject root = new JSONObject(result.toString("UTF-8"));
                                 final boolean isFirstRequest = mLastMessageTime.equals("");
                                 mLastMessageTime = root.getString("ts");
 
                                 JSONArray events = root.getJSONArray("events");
                                 if ( events.length() > 0 )
                                 {
                                     // Create a new list with the new messages
                                     List<ChatMessage> newList = new ArrayList<ChatMessage>();
                                     for (int i = 0; i < events.length(); ++i)
                                     {
                                         JSONObject event = events.getJSONObject(i);
                                         String icon = event.getString("icon");
                                         String title = event.getString("title");
                                         int ts = event.getInt("ts");
                                         String status = event.getString("status");
                                         String who = event.getString("who");
 
                                         // Remove the escaped slashes from the
                                         // icon path
                                         icon = icon.replace("\\/", "/");
 
                                         // Parse the date
                                         Date when = new Date(ts * 1000L);
 
                                         // Construct the message and add it to
                                         // the message list
                                         ChatType type = ChatType.PUBLIC;
                                         if ( status.equals("amigo") )
                                             type = ChatType.FRIENDS;
                                         else if ( status.equals("admin") )
                                             type = ChatType.ADMIN;
                                         ChatMessage msg = new ChatMessage(when, who, title, type, icon);
                                         newList.add(msg);
                                         
                                         // Send a notification if they mentioned us
                                         String lowercaseMsg = msg.getMessage().toLowerCase();
                                         boolean notify = lowercaseMsg.contains(mUsername.toLowerCase()) ||
                                                          ( mIsAdmin && lowercaseMsg.contains("admin") );
                                         if ( !isFirstRequest && notify )
                                         {
                                             Notifications.theyMentionedMe(FisgoService.this, msg);
                                         }
                                     }
 
                                     // Append all the previous messages
                                     newList.addAll(mMessages);
                                     mMessages = newList;
 
                                     // Notify the handlers
                                     notifyHandlers();
                                 }
                             }
 
                             // Make a small delay to poll again
                             if ( failed )
                             {
                                 mHttp.wait(mTimeToWaitWhenFailed);
                             }
                             else if ( mOutgoingMessages.size() == 0 )
                             {
                                 mHttp.wait(mIsOnForeground ? mTimeToWait : mTimeToWaitWhenOnBackground);
                             }
                         }
                         catch (InterruptedException e)
                         {
                         }
                         catch (JSONException e)
                         {
                             e.printStackTrace();
                         }
                         catch (UnsupportedEncodingException e)
                         {
                             e.printStackTrace();
                         }
                     }
                 }
             }
         });
         mThread.start();
     }
     
     @Override
     public void onDestroy ()
     {
         super.onDestroy();
         unregisterReceiver(mConnectivityReceiver);
     }
 
     private void clearSession()
     {
         Notifications.stopOnForeground(this);
         mLastMessageTime = "";
         mMessages.clear();
         mOutgoingMessages.clear();
         mFriendNames.clear();
         notifyHandlers();
     }
 
     private void notifyHandlers()
     {
         Message msg = new Message();
         for (Handler handler : mBinder.getHandlers())
         {
             handler.sendMessage(msg);
         }
     }
     
     public void wakeUp ()
     {
         Log.i(TAG, "Waking up FisgoService");
         synchronized ( mHttp )
         {
             mHttp.notify();            
         }
     }
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return mBinder;
     }
 
     public class FisgoBinder extends Binder
     {
         private Set<Handler> mHandlers = new HashSet<Handler>();
 
         private final Pattern mUseripPattern = Pattern.compile("<input type=\"hidden\" name=\"userip\" value=\"([^\"]+)\"/>");
         private final Pattern mIpcontrolPattern = Pattern.compile("<input type=\"hidden\" name=\"useripcontrol\" value=\"([^\"]+)\"/>");
        private final Pattern mLogoutPattern = Pattern.compile("<a href=\"/login\\.php\\?op=logout");
         private final Pattern mAdminPattern = Pattern.compile("<a href=\"/admin/bans\\.php\">admin</a>");
         private final Pattern mMykeyPattern = Pattern.compile("var mykey = (\\d+);");
         private final Pattern mFriendPattern = Pattern.compile("<div class=\"friends-item\"><a href=\"\\/user\\/([^\"]+)\"");
 
         public boolean isAdmin()
         {
             return mIsAdmin;
         }
         
         public boolean isLoggedIn()
         {
             return mIsLoggedIn;
         }
 
         public void logOut()
         {
             mIsLoggedIn = false;
             clearSession();
             mThread.interrupt();
         }
 
         public LoginStatus logIn(String username, String password)
         {
             if ( username.equalsIgnoreCase("whizzo") )
                 return LoginStatus.INVALID_PASSWORD;
             
             String step1 = mHttp.get(LOGIN_GET_URL);
             if ( "".equals(step1) )
                 return LoginStatus.NETWORK_FAILED;
 
             // Get the userip field
             Matcher m = mUseripPattern.matcher(step1);
             if ( !m.find() )
                 Log.e(TAG, "Couldn't find the userip form field");
             String userip = m.group(1);
 
             // Get the ip control field
             m = mIpcontrolPattern.matcher(step1);
             if ( !m.find() )
                 Log.e(TAG, "Couldn't find the ip control form field");
             String ipcontrol = m.group(1);
 
             // Prepare the POST request
             Map<String, Object> params = new HashMap<String, Object>();
             params.put("username", username);
             params.put("password", password);
             params.put("userip", userip);
             params.put("useripcontrol", ipcontrol);
             params.put("persistent", 1);
             params.put("processlogin", 1);
             params.put("return", "");
             String step2 = mHttp.post(LOGIN_URL, params);
             if ( "".equals(step2) )
                 return LoginStatus.NETWORK_FAILED;
 
             // Did we log in correctly?
            m = mLogoutPattern.matcher(step2);
            mIsLoggedIn = m.find();
             mIsAdmin = false;
 
             if ( mIsLoggedIn )
             {
                 mUsername = username;
                 
                 // Are we administrators?
                 m = mAdminPattern.matcher(step2);
                 mIsAdmin = m.find();
 
                 // Get the mykey value to be able to send messages
                 String step3 = mHttp.get(SNEAK_URL);
                 m = mMykeyPattern.matcher(step3);
                 if ( m.find() )
                 {
                     mMyKey = m.group(1);
                 }
 
                 // Get the friend list
                 String friendsUrl = FRIEND_LIST_URL.replace("?username", mUsername);
                 String friendList = mHttp.get(friendsUrl);
                 if ( friendList.equals("") == false )
                 {
                     m = mFriendPattern.matcher(friendList);
                     while (m.find())
                     {
                         mFriendNames.add(m.group(1));
                     }
                 }
                 
                 // Start this service on foreground
                 Notifications.startOnForeground(FisgoService.this);
             }
 
             mThread.interrupt();
 
             return mIsLoggedIn ? LoginStatus.OK : LoginStatus.INVALID_PASSWORD;
         }
 
         public String getUsername()
         {
             return mUsername;
         }
 
         public List<ChatMessage> getMessages()
         {
             return mMessages;
         }
 
         public List<String> getFriendNames()
         {
             return mFriendNames;
         }
 
         public Set<Handler> getHandlers()
         {
             return mHandlers;
         }
 
         public void addHandler(Handler handler)
         {
             mHandlers.add(handler);
             handler.dispatchMessage(new Message());
         }
 
         public void removeHandler(Handler handler)
         {
             mHandlers.remove(handler);
         }
 
         public void sendChat(String msg)
         {
             if ( mIsLoggedIn )
             {
                 mOutgoingMessages.add(msg);
                 mThread.interrupt();
             }
         }
 
         public AvatarStorage getAvatarStorage()
         {
             return mAvatars;
         }
 
         public void setType(ChatType type)
         {
             if ( type != mType )
             {
                 // Set the new chat type, and reset all the message lists
                 synchronized (mHttp)
                 {
                     mType = type;
                     clearSession();
                     mThread.interrupt();
                 }
             }
         }
         
         public String sendPicture(InputStream data)
         {
             String url = null;
             
             synchronized (mHttp)
             {
                 String result = mHttp.postData(UPLOAD_URL, data);
                 if ( result.equals("") == false )
                 {
                     JSONObject root;
                     try
                     {
                         root = new JSONObject(result);
                         if ( root.has("url") )
                         {
                             url = root.getString("url").replace("\\/", "/");
                         }
                     }
                     catch (JSONException e)
                     {
                         e.printStackTrace();
                     }
                 }
             }
             
             return url;
         }
         
         public void setOnForeground ( boolean isOnForeground )
         {
             Log.i(TAG, "Setting FisgoService on " + (isOnForeground ? "foreground" : "background"));
             mIsOnForeground = isOnForeground;
         }
     }
 }
