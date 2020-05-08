 package com.tools.tvguide.managers;
 
import com.tools.tvguide.utils.MyApplication;

 import android.content.Context;
 
 public class AppEngine 
 {
     private static final String                     TAG                         = "AppEngine";
     private static AppEngine                        mInstance;
     private Context                                 mContext;
     private Context                                 mApplicationContext;
     private CollectManager                          mUserSettingManager;
     private LoginManager                            mLoginManager;
     private ContentManager                          mContentManager;
     private CacheManager                            mCacheManager;
     private AlarmHelper                             mAlarmHelper;
     private UrlManager                              mUrlManager;
     private DnsManager                              mDnsManager;
     private UpdateManager                           mUpdateManager;
     private BootManager                             mBootManager;
     private HotHtmlManager                          mHotHtmlManager;
     private ChannelHtmlManager                      mChannelHtmlManager;
     private ProgramHtmlManager                      mProgramHtmlManager;
     
     /********************************* Manager定义区，所有受AppEngine管理的Manger统一定义 **********************************/
     
     public static AppEngine getInstance()
     {
         if (mInstance == null)
             mInstance = new AppEngine();
         return mInstance;
     }
     
     public void setContext(Context context)
     {
         mContext = context;
     }
 
     public void setApplicationContext(Context context)
     {
         mApplicationContext = context;
     }
 
     public Context getApplicationContext()
     {
         return mApplicationContext;
     }
 
     public Context getContext()
     {
         return mContext;
     }
     
    private void checkInitialized()
    {
        if (mApplicationContext == null)
            mApplicationContext = MyApplication.getInstance().getApplicationContext();
    }
    
     public CollectManager getCollectManager()
     {
        checkInitialized();
         if (mUserSettingManager == null)
             mUserSettingManager = new CollectManager(mApplicationContext);
         return mUserSettingManager;
     }
     
     public LoginManager getLoginManager()
     {
        checkInitialized();
         if (mLoginManager == null)
             mLoginManager = new LoginManager(mApplicationContext);
         return mLoginManager;
     }
     
     public ContentManager getContentManager()
     {
        checkInitialized();
         if (mContentManager == null)
             mContentManager = new ContentManager(mApplicationContext);
         return mContentManager;
     }
     
     public CacheManager getCacheManager()
     {
        checkInitialized();
         if (mCacheManager == null)
             mCacheManager = new CacheManager(mApplicationContext);
         return mCacheManager;
     }
     
     public AlarmHelper getAlarmHelper()
     {
        checkInitialized();
         if (mAlarmHelper == null)
             mAlarmHelper = new AlarmHelper(mApplicationContext);
         return mAlarmHelper;
     }
     
     public UrlManager getUrlManager()
     {
        checkInitialized();
         if (mUrlManager == null)
             mUrlManager = new UrlManager(mApplicationContext);
         return mUrlManager;
     }
     
     public DnsManager getDnsManager()
     {
        checkInitialized();
         if (mDnsManager == null)
             mDnsManager = new DnsManager(mApplicationContext);
         return mDnsManager;
     }
     
     public UpdateManager getUpdateManager()
     {
        checkInitialized();
         if (mUpdateManager == null)
             mUpdateManager = new UpdateManager(mApplicationContext);
         return mUpdateManager;
     }
     
     public BootManager getBootManager()
     {
        checkInitialized();
         assert (mContext != null);
         if (mBootManager == null)
             mBootManager = new BootManager(mContext);
         return mBootManager;
     }
     
     public HotHtmlManager getHotHtmlManager()
     {
        checkInitialized();
         if (mHotHtmlManager == null)
             mHotHtmlManager = new HotHtmlManager(mApplicationContext);
         return mHotHtmlManager;
     }
     
     public ChannelHtmlManager getChannelHtmlManager()
     {
        checkInitialized();
         if (mChannelHtmlManager == null)
             mChannelHtmlManager = new ChannelHtmlManager(mApplicationContext);
         return mChannelHtmlManager;
     }
     
     public ProgramHtmlManager getProgramHtmlManager()
     {
        checkInitialized();
         if (mProgramHtmlManager == null)
             mProgramHtmlManager = new ProgramHtmlManager(mApplicationContext);
         return mProgramHtmlManager;
     }
     
     public void prepareBeforeExit()
     {
         if (mBootManager != null)
             mBootManager.shutDown();
         
         if (mUserSettingManager != null)
             mUserSettingManager.shutDown();
         
         if (mAlarmHelper != null)
             mAlarmHelper.shutDown();
         
         if (mContentManager != null)
             mContentManager.shutDown();
         
         exit();
     }
     
     public void exit()
     {
         mInstance = null;
     }
 }
