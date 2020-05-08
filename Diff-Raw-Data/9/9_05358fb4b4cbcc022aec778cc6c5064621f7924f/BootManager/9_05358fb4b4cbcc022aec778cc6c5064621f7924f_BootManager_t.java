 package com.tools.tvguide.managers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tools.tvguide.components.ShortcutInstaller;
 import com.tools.tvguide.components.SplashDialog;
 import com.tools.tvguide.managers.UpdateManager.IOCompleteCallback;
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 public class BootManager 
 {
     private Context             mContext;
     private SplashDialog        mSplashDialog;
     private boolean             mShowSplash                                 = !EnvironmentManager.isDevelopMode;
     private SharedPreferences   mPreference;
     private static final String SHARE_PREFERENCES_NAME                      = "boot_settings";
     private static final String KEY_FIRST_START_FLAG                        = "key_first_start_flag";
     private String              mUA;
     private List<OnSplashFinishedCallback> mOnSplashFinishedCallbackList;
     private boolean             mIsSplashShowing                            = false;
     
     public interface OnSplashFinishedCallback
     {
         void OnSplashFinished();
     }
     
     // 因为BootManager中有对UI的操作，所以BootManager必须放在UI线程中创建，一般应放在MainActivity中创建
     public BootManager(Context context)
     {
        assert (context != null);
         mContext = context;
         mPreference = context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
         mUA = getUserAgentInternal();
         mOnSplashFinishedCallbackList = new ArrayList<BootManager.OnSplashFinishedCallback>();
     }
     
     public void start()
     {
         if (mShowSplash)
             showSplash();
         
         checkNetwork();
         
         AppEngine.getInstance().getUrlManager().init(new UrlManager.OnInitCompleteCallback() 
         {
             @Override
             public void OnInitComplete(int result)
             {
                 AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
                 {
                     public void OnIOComplete(int result) 
                     {
                         uiHandler.sendEmptyMessage(result);
                     }
                 });
                 AppEngine.getInstance().getLoginManager().login();
             }
         });
         if (isFirstStart())
             new ShortcutInstaller(mContext).createShortCut();
     }
     
     public String getUserAgent()
     {
         return mUA;
     }
     
     public void addOnSplashFinishedCallback(final OnSplashFinishedCallback callback)
     {
         mOnSplashFinishedCallbackList.add(callback);
     }
     
     // This API should be called in UI main thread
     private String getUserAgentInternal()
     {
         WebView webView = new WebView(mContext);
         webView.layout(0, 0, 0, 0);
         WebSettings settings = webView.getSettings();
         return settings.getUserAgentString();
     }
     
     public boolean isFirstStart()
     {
         return mPreference.getBoolean(KEY_FIRST_START_FLAG, true);
     }
     
     public boolean isShowSplash()
     {
         return mShowSplash;
     }
     
     public void showSplash()
     {
         mSplashDialog = new SplashDialog(mContext);
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
         
         mOnSplashFinishedCallbackList = null;
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
     
     public void shutDown()
     {
         if (isFirstStart())
             mPreference.edit().putBoolean(KEY_FIRST_START_FLAG, false).commit();
     }
     
     private void checkNetwork()
     {
         if (!Utility.isNetworkAvailable())
             Toast.makeText(mContext, "注意：当前网络不可用！", Toast.LENGTH_LONG).show();
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             switch(msg.what)
             {
                 case IOCompleteCallback.NEED_UPDATE:
                     Toast.makeText(mContext, "有新版本啦，请检查更新", Toast.LENGTH_LONG).show();
                     break;
                 case IOCompleteCallback.NO_NEED_UPDATE:
                     break;
             }
         }
     };
 }
