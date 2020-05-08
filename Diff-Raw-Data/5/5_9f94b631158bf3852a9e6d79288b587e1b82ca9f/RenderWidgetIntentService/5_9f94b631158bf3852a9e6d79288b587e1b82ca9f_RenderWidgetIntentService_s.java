 package com.anderspersson.xbmcwidget.recentvideo;
 
 import java.io.File;
 
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Handler;
 import android.view.View;
 import android.widget.RemoteViews;
 
 import com.anderspersson.xbmcwidget.R;
 import com.anderspersson.xbmcwidget.common.FileLog;
 import com.anderspersson.xbmcwidget.common.ToastRunnable;
 import com.anderspersson.xbmcwidget.common.UpdateTimer;
 import com.anderspersson.xbmcwidget.common.XbmcWidgetApplication;
 import com.anderspersson.xbmcwidget.xbmc.XbmcService;
 
 public abstract class RenderWidgetIntentService extends IntentService {
 	
 	protected enum REFRESH_STATE { UNCHANGED, OK, FAILURE };
 
 	private Handler _handler;
 	private CachedFanArtDownloader _fanArtDownloader;
 	
 	public RenderWidgetIntentService(String name) {
 		super(name);
 		_fanArtDownloader = new CachedFanArtDownloader(this, getFanArtSize());
 	}
 	
 	@Override 
 	public void onCreate() {
 		super.onCreate();
 		_handler = new Handler();
 	}
 	
    @Override
    protected void onHandleIntent(Intent intent) {
 		String action = intent.getAction();
 	   
 		FileLog.appendLog("Render - " + action);
 		
 	   if(action.equals(RecentVideoWidget.RECENT_VIDEO_UPDATE_WIDGET)) {
 		   createAppWidget();
 		   return;
 	   }
 	   
 	   if(action.equals(RecentVideoIntentActions.REFRESHED) 
 			   || action.equals(RecentVideoIntentActions.NO_CHANGE)){
 		   updateShows(REFRESH_STATE.OK);
 		   return;
 	   }
 	   
 	   if(action.equals(RecentVideoIntentActions.REFRESH_FAILED)){
 		   updateShows(REFRESH_STATE.FAILURE);
 		   return;
 	   }
 	   
 	   if(action.equals(RecentVideoIntentActions.FANART_DOWNLOADED)) {
 		   refreshCurrent();
 		   return;
 	   }
 	   
 	   if(action.equals(RecentVideoIntentActions.RETRY)) {
 		   retry();
 		   return;
 	   }
 	   
 	   if(action.equals(RecentVideoIntentActions.NAVIGATE)) {
 		   moveTo(intent.getIntExtra("toIndex", -1));
 		   return;
 	   }
 	   
 	   if(action.equals(XbmcService.PLAY_EPISODE_ACTION)) {
 	        handlePlayClick(intent.getStringExtra(XbmcService.EXTRA_ITEM));
 		   return;
 	   }
    }
    
 	protected abstract Class<?> getWidgetClass();
 	protected abstract int getLoadingViewId();
 	protected abstract int getFailedViewId();
 	protected abstract boolean hasWidgetData();
 	protected abstract void refreshCurrent();
 	protected abstract void moveTo(int index);
 	protected abstract void setupViewData(int episodeIndex, RemoteViews rv, REFRESH_STATE state);
 	protected abstract int getMaxIndex();
 	protected abstract FanArtSize getFanArtSize();
 	
 	protected XbmcWidgetApplication getWidgetApplication() {
 		return (XbmcWidgetApplication)getApplicationContext();
 	}
 	
 	protected void setupNavigationButton(RemoteViews remoteViews, int viewId, int toIndex, Boolean enabled) {
 		
 		if(enabled == false) {
 			remoteViews.setBoolean(viewId, "setEnabled", false);
 			return;
 		}
 		
 		Intent navigateIntent = new Intent(this, this.getClass());
 		navigateIntent.setAction(RecentVideoIntentActions.NAVIGATE);
 		navigateIntent.putExtra("toIndex", toIndex);
 		
 		// To make it unique - due to a bug the following line must be here
 		navigateIntent.setData(Uri.parse(navigateIntent.toUri(Intent.URI_INTENT_SCHEME))); 
         PendingIntent pendingIntent = PendingIntent.getService(this, 0, navigateIntent, 0);
         
         remoteViews.setBoolean(viewId, "setEnabled", true);
         remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
 	}
 	
 	protected void setupClick(String file, RemoteViews rv) {
 		Intent playIntent = new Intent(this, this.getClass());
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		playIntent.putExtra(XbmcService.EXTRA_ITEM, file);
 		PendingIntent toastPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.play_recenttv_button, toastPendingIntent);
 	}
 	
 	protected void setupFanArt(RemoteViews rv, String fanArtPath, int drawableId) {
 		
 		if(_fanArtDownloader.isCached(fanArtPath)) {
 			String fanartPathCachedOnStorage = _fanArtDownloader.download(fanArtPath);
 			
 			rv.setViewVisibility(R.id.default_header, View.INVISIBLE);
 			rv.setImageViewUri(R.id.fanArt, Uri.fromFile(new File(fanartPathCachedOnStorage)));
 			return;
 		}
 				
 		rv.setImageViewResource(R.id.fanArt, drawableId);
 		rv.setViewVisibility(R.id.default_header, View.VISIBLE);
 		Intent fanArtDownloadIntent = new Intent(this, FanArtDownloaderIntentService.class);
 		fanArtDownloadIntent.putExtra("path", fanArtPath);
 		fanArtDownloadIntent.putExtra("replyTo", this.getClass().getName());
 
 		startService(fanArtDownloadIntent);
 	}
 	
 	protected void setupNavigationButtons(int viewIndex, RemoteViews rv) {
 		Boolean leftArrowEnabled = viewIndex != 0;
 		Boolean rightArrowEnabled = viewIndex != getMaxIndex();
 		
 		setupNavigationButton(rv, R.id.next_show, viewIndex+1,  rightArrowEnabled);
 		setupNavigationButton(rv, R.id.prev_show, viewIndex-1, leftArrowEnabled);
 	}
 	
 	protected void setupBorderColor(RemoteViews rv, REFRESH_STATE state) {
 		if(state == REFRESH_STATE.UNCHANGED)
 			return;
 					
 		int color = state == REFRESH_STATE.OK
 				? Color.parseColor("#A9A9A9") 
				: Color.RED;;
 		
		rv.setInt(R.id.fanArt, "setBackgroundColor", color);
 	}
 	
 	protected void createAndUpdateView(int viewIndex, REFRESH_STATE state) {
 		RemoteViews rv = new RemoteViews( this.getPackageName(), 
 				R.layout.recent_video_widget );
 	    
 		if(viewIndex != -1) {
 			setupViewData(viewIndex, rv, state);
 		}
 		
 	    getWidgetManager().updateAppWidget( getComponentName(), rv );
 	}
 	
 	private void retry() {
 		createLoadingView();
 		new UpdateTimer().reset(this);
 	}
 	
 	private void createLoadingView() {
 		AppWidgetManager appWidgetManager = getWidgetManager();
 		int[] widgetIds = getWidgetIds();
 		
 		for(int i = 0; i < widgetIds.length; i++) {
 			RemoteViews rv = new RemoteViews( this.getPackageName(), getLoadingViewId());
 			ComponentName recentTvWidget = new ComponentName( this, getWidgetClass() );	
 		    appWidgetManager.updateAppWidget( recentTvWidget, rv );
 		}
 	}
 	
 	private void createAppWidget() { 
 		if(hasWidgetData()) {
 			refreshCurrent();
 		}
 		else {
 			createFailedView();
 		}
 	}
 
 	private void updateShows(REFRESH_STATE state) {
 		if(hasWidgetData() == false) {
 			createFailedView();
 			return;
 		}
 		createAndUpdateView(0, state);
 	}
 
 
 	private void handlePlayClick(String filePath) {
 		_handler.post(new ToastRunnable(this, "Playing on XBMC"));
 		
 		Intent playIntent = new Intent(this, XbmcService.class);
 		playIntent.setAction(XbmcService.PLAY_EPISODE_ACTION);
 		playIntent.putExtra(XbmcService.EXTRA_ITEM, filePath);
 		
 		startService(playIntent);	
 	}
 	
 	protected void createFailedView() {
 		RemoteViews rv = new RemoteViews( this.getPackageName(), getFailedViewId());
 		
 		Class<? extends RenderWidgetIntentService> renderClass = this.getClass();
 		Intent retryIntent = new Intent(this, renderClass);
 		retryIntent.setAction(RecentVideoIntentActions.RETRY);
 		PendingIntent toastPendingIntent = PendingIntent.getService(this, 0, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         rv.setOnClickPendingIntent(R.id.retry_button, toastPendingIntent);
 		
         getWidgetManager().updateAppWidget( getComponentName(), rv );
 	}
 	
 	private int[] getWidgetIds() {
         return getWidgetManager().getAppWidgetIds(getComponentName());
 	}
 	
 	private ComponentName getComponentName() {
 		String className = getWidgetClass().getName();
 		return new ComponentName(getPackageName(), className );
 	}
 	
 	private AppWidgetManager getWidgetManager() {
         return AppWidgetManager.getInstance(this);
 	}
 }
