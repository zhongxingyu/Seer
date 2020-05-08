 package com.tools.tvguide.managers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tools.tvguide.components.ShortcutInstaller;
 import com.tools.tvguide.components.Shutter;
 import com.tools.tvguide.components.SplashDialog;
 import com.tools.tvguide.data.GlobalData;
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 public class BootManager implements Shutter
 {
     private Context             mContext;
     private SplashDialog        mSplashDialog;
     private boolean             mShowSplash                                 = !EnvironmentManager.isDevelopMode;
     private SharedPreferences   mPreference;
     private static final String SHARE_PREFERENCES_NAME                      = "boot_settings";
     private static final String KEY_FIRST_START_FLAG                        = "key_first_start_flag";
     private static final String KEY_LAST_START_FLAG                         = "key_last_start_flag";
     private List<OnSplashFinishedCallback> mOnSplashFinishedCallbackList;
     private OnStartedCallback  mOnStartedCallback;
     private boolean             mIsSplashShowing                            = false;
     private enum SelfMessage {Msg_Need_Update};
     
     public interface OnSplashFinishedCallback
     {
         void OnSplashFinished();
     }
     
     public interface OnStartedCallback
     {
         void OnUpdateInfo(boolean needUpdate);
     }
     
     public BootManager(Context context)
     {
         assert (context != null);
         mContext = context;
         mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
         GlobalData.UserAgent = getUserAgentInternal();
         mOnSplashFinishedCallbackList = new ArrayList<BootManager.OnSplashFinishedCallback>();
     }
     
     public void start(OnStartedCallback callback)
     {
         assert (callback != null);
         mOnStartedCallback = callback;
         start();
     }
     
     public void start()
     {
         if (mShowSplash)
             showSplash();
         
         checkNetwork();
         
         AppEngine.getInstance().getEnvironmentManager().init();
         AppEngine.getInstance().getUrlManager().init(new UrlManager.OnInitCompleteCallback() 
         {
             @Override
             public void OnInitComplete(int result)
             {
                 AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
                 {
                     public void OnIOComplete(CheckResult result) 
                     {
                         if (result == CheckResult.Need_Update)
                             uiHandler.obtainMessage(SelfMessage.Msg_Need_Update.ordinal()).sendToTarget();
                     }
                 });
                 AppEngine.getInstance().getLoginManager().login();
             }
         });
         if (isFirstStart())
             new ShortcutInstaller(AppEngine.getInstance().getContext()).createShortCut();
         
         AppEngine.getInstance().getServiceManager().init();
         AppEngine.getInstance().getServiceManager().startMonitor();
         mPreference.edit().putLong(KEY_LAST_START_FLAG, System.currentTimeMillis()).commit();
     }
     
     public void addOnSplashFinishedCallback(final OnSplashFinishedCallback callback)
     {
         mOnSplashFinishedCallbackList.add(callback);
     }
     
     // This API should be called in UI main thread
     private String getUserAgentInternal()
     {
         WebView webView = new WebView(AppEngine.getInstance().getContext());
         webView.layout(0, 0, 0, 0);
         WebSettings settings = webView.getSettings();
         return settings.getUserAgentString();
     }
     
     public boolean isFirstStart()
     {
        return mPreference.getBoolean(KEY_FIRST_START_FLAG, true);
     }
     
     public long getLastStartTime()
     {
         return mPreference.getLong(KEY_LAST_START_FLAG, 0);
     }
     
     public boolean isShowSplash()
     {
         return mShowSplash;
     }
     
     public void showSplash()
     {
         mSplashDialog = new SplashDialog(AppEngine.getInstance().getContext());
         mSplashDialog.showSplash();
     }
     
     public boolean isSplashShowing()
     {
         return mIsSplashShowing;
     }
     
     public void removeSplash()
     {
         if (mSplashDialog != null)
             mSplashDialog.checkTimeToRemove();
     }
     
     public void onSplashStarted()
     {
         mIsSplashShowing = true;
         removeSplash();
     }
     
     public void onSplashFinished()
     {        
         for (int i=0; i<mOnSplashFinishedCallbackList.size(); ++i)
             mOnSplashFinishedCallbackList.get(i).OnSplashFinished();
         
         mOnSplashFinishedCallbackList.clear();
         mSplashDialog = null;
         mIsSplashShowing = false;
         
         new Handler().postDelayed(new Runnable() 
         {
             @Override
             public void run() 
             {
                 AppEngine.getInstance().getAlarmHelper().resetAllAlarms();
             }
         }, 1000);
     }
     
     @Override
     public void onShutDown()
     {
         if (isFirstStart())
             mPreference.edit().putBoolean(KEY_FIRST_START_FLAG, false).commit();
     }
     
     private void checkNetwork()
     {
         if (!Utility.isNetworkAvailable())
             Toast.makeText(AppEngine.getInstance().getApplicationContext(), "注意：当前网络不可用！", Toast.LENGTH_LONG).show();
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             SelfMessage selfMsg = SelfMessage.values()[msg.what];
             switch(selfMsg)
             {
                 case Msg_Need_Update:
                     Toast.makeText(AppEngine.getInstance().getApplicationContext(), "有新版本啦，请检查更新", Toast.LENGTH_LONG).show();
                     if (mOnStartedCallback != null)
                         mOnStartedCallback.OnUpdateInfo(true);
                     break;
             }
         }
     };
 }
