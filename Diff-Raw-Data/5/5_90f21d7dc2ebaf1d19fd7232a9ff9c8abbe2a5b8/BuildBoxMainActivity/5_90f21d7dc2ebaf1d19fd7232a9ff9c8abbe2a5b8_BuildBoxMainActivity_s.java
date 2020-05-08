 package at.tectas.buildbox;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.animation.Animation;
 import android.widget.ImageView;
 import at.tectas.buildbox.adapters.TabsAdapter;
 import at.tectas.buildbox.communication.callbacks.BuildBoxDownloadCallback;
 import at.tectas.buildbox.communication.callbacks.BuildBoxDeserializeMapFinishedCallback;
 import at.tectas.buildbox.library.fragments.ContentListFragment;
 import at.tectas.buildbox.library.fragments.DetailFragment;
 import at.tectas.buildbox.library.fragments.DownloadListFragment;
 import at.tectas.buildbox.library.communication.callbacks.interfaces.DownloadBaseCallback;
 import at.tectas.buildbox.library.content.ItemList;
 import at.tectas.buildbox.library.content.items.DetailItem;
 import at.tectas.buildbox.library.content.items.Item;
 import at.tectas.buildbox.library.download.DownloadActivity;
 import at.tectas.buildbox.library.helpers.PropertyHelper;
 import at.tectas.buildbox.library.service.DownloadService;
 import at.tectas.buildbox.R;
 
 @SuppressLint("DefaultLocale")
 public class BuildBoxMainActivity extends DownloadActivity {
 	
 	ViewPager mViewPager;
 	
 	protected Dialog splashScreen = null;
 	public ActionBar bar = null;
 	protected TabsAdapter adapter = null;
 	protected DetailItem romItem = null;
	protected ItemList contentItems = null;
 	protected HashSet<String> contentUrls = new HashSet<String>();
 	protected Hashtable<String, Bitmap> remoteDrawables = new Hashtable<String, Bitmap>();
 	protected DownloadBaseCallback downloadCallback = null;
 	
 	@Override
 	public DownloadBaseCallback getDownloadCallback() {
 		return this.downloadCallback;
 	}
 	
 	@Override
 	public void setDownloadCallback(DownloadBaseCallback callback) {
 		this.downloadCallback = callback;
 	}
 	
 	@Override
 	public Hashtable<String, Bitmap> getRemoteDrawables() {
 		return this.remoteDrawables;
 	}
 	
 	public void setRemoteDrawables(Hashtable<String, Bitmap> drawables) {
 		this.remoteDrawables = drawables;
 	}
 	
 	@Override
 	public ItemList getContentItems() {
 		return this.contentItems;
 	}
 	
 	@Override
 	public Fragment getCurrentFragment() {
 		return this.adapter.getCurrentFragment();
 	}
 	
 	public DetailItem getRomItem() {
 		return this.romItem;
 	}
 	
 	@Override
 	public String getDownloadDir() {
 		return this.helper.downloadDir;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		/*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         .detectDiskReads()
         .detectDiskWrites()
         .detectNetwork()
         .penaltyLog()
         .build());
 		*/
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_main);
 		
 		this.showSplashscreen();
 		
 		this.initialize();
 		
 		this.bar = getActionBar();
 		this.bar.setDisplayShowHomeEnabled(false);
 		this.bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		this.bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
 		
 		this.adapter = new TabsAdapter(this, (ViewPager)findViewById(R.id.pager));
 	}
 	
 	@Override
 	public void startServiceDownload() {
 		if (this.downloadCallback == null) {
 			this.downloadCallback = new at.tectas.buildbox.communication.callbacks.BuildBoxDownloadCallback(this);
 		}
 		
 		super.startServiceDownload(this.downloadCallback, this.downloadCallback, this.downloadCallback);
 	}
 	
 	@Override
 	public void onBackPressed() {
 	    if (this.adapter.getCurrentFragment() == null || !this.adapter.getCurrentFragment().getChildFragmentManager().popBackStackImmediate()) {
 	    	if (!this.getFragmentManager().popBackStackImmediate())
 	    		finish();
 	    }
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		this.fillBackupList();
 		
 		if (this.backupList.size() == 0) {
 			menu.getItem(3).setVisible(false);
 		}
 		else {
 			menu.getItem(3).setVisible(true);
 		}
 		
 		if (this.bar.getTabCount() == 0 || !this.bar.getTabAt(this.adapter.getViewPagerIndex()).getText().equals("Downloads")) {
 			menu.getItem(0).setVisible(false);
 			menu.getItem(1).setVisible(false);
 			menu.getItem(2).setVisible(false);
 		}
 		else {
 			
 			if (this.downloadMapContainsBrokenOrAborted()) {
 				menu.getItem(0).setVisible(true);
 				menu.getItem(1).setVisible(true);
 				menu.getItem(2).setVisible(false);
 			}
 			else if (this.getDownloads().size() == 0) {
 				menu.getItem(0).setVisible(false);
 				menu.getItem(1).setVisible(false);
 				menu.getItem(2).setVisible(false);
 			}
 			else {
 				menu.getItem(0).setVisible(false);
 				menu.getItem(1).setVisible(true);
 				menu.getItem(2).setVisible(true);
 			}
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.clear();
 		
 		getMenuInflater().inflate(R.menu.download_view_menu, menu);
 		
 		return true;
 	};
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch(requestCode){
 			case PACKAGE_MANAGER_RESULT:
 				if (!this.downloadMapRestored) {
 					this.getDownloads().clear();
 					
 					this.downloadMapRestored = true;
 					this.loadDownloadsMapFromCacheFile(new BuildBoxDeserializeMapFinishedCallback(this));
 				}
   				return;
    		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	@Override
 	public void getServiceDownloadMap(boolean addListeners) {
 		if (addListeners == true) {
 			if (this.downloadCallback == null) {
 				this.downloadCallback = new BuildBoxDownloadCallback(this);
 			}
 			
 			super.getServiceDownloadMap(this.downloadCallback, this.downloadCallback, this.downloadCallback);
 		}
 		else {
 			this.getServiceDownloadMap();
 		}
 	}
 	
 	@Override
 	public void updateWithJsonObject(JsonObject result) {
 		try {
 			this.romItem = (DetailItem) this.parser.parseJsonToItem(result);
 			
 			if (this.romItem == null && this.helper.presetContentUrl == null) {
 				this.refreshDownloadsView();
 			}
 			
 			this.adapter.addTab(
 					this.bar.newTab().setText("Rom"), 
 					DetailFragment.class, 
 					this.romItem.parseItemToBundle());
 
 		} 
 		catch (NullPointerException e) {
 			e.printStackTrace();
 			if (this.helper.presetContentUrl == null) {
 				this.refreshDownloadsView();
 			}
 		}
 		
 		if (PropertyHelper.stringIsNullOrEmpty(this.helper.presetContentUrl)) {
 			if (DownloadService.Started) {
 				this.getServiceMap();
 			}
 			else {
 				this.loadDownloadsMapFromCacheFile();
 			}
 			
 			this.removeSplashscreen();
 		}
 	}
 	
 	@Override
 	public void updateWithJsonArray(JsonArray result) {
 		
 		try {
 			this.contentItems = this.parser.parseJson(result);
 			
 			int i = 0;
 			
 			if ((this.contentItems == null || this.contentItems.size() == 0) && this.romItem == null) {
 				this.refreshDownloadsView();
 				return;
 			}
 			
 			for (Item item : this.contentItems) {
 				Bundle bundle = new Bundle();
 				
 				bundle.putInt("index", i);
 				
 				this.adapter.addTab(
 						this.bar.newTab().setText(item.title), 
 						ContentListFragment.class, 
 						bundle);
 				
 				i++;
 			}
 		}
 		catch (NullPointerException e) {
 			e.printStackTrace();
 			this.refreshDownloadsView();
 		}
 		
 		if (DownloadService.Started) {
 			this.getServiceMap();
 		}
 		else {
 			this.loadDownloadsMapFromCacheFile();
 		}
 		
 		this.removeSplashscreen();
 	}
 
 	@Override
 	public void updateWithImage(ImageView view, Bitmap bitmap) {
 		try {
 			if (view != null) {				
 				if (!this.remoteDrawables.containsKey((String)view.getTag())) {
 					this.remoteDrawables.put((String)view.getTag(), bitmap);
 				}
 				else {
 					bitmap = this.remoteDrawables.get((String)view.getTag());
 				}
 				
 				view.setImageBitmap(bitmap);
 				
 				Animation animation = view.getAnimation();
 				if (animation != null)
 					animation.cancel();
 			}
 		}
 		catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addTab(String title, Class<?> clss, Bundle bundle) {
 		this.adapter.addTab(this.bar.newTab().setText(title), clss, bundle);
 	}
 	
 	@Override
 	public void refreshDownloadsView() {
		if ((this.getDownloads().size() > 0 && !this.bar.getTabAt(this.bar.getTabCount() - 1).getText().equals("Downloads")) || this.bar.getTabCount() == 0) {
 			this.addTab("Downloads", DownloadListFragment.class, new Bundle());
 		}
 
 		super.refreshDownloadsView();
 	}
 
 	protected void showSplashscreen() {
 		this.splashScreen = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
 		this.splashScreen.setContentView(R.layout.splashscreen_dialog);
 		this.splashScreen.setCancelable(false);
 		this.splashScreen.show();
 	     
 	    final BuildBoxMainActivity activity = this;
 	    final Handler handler = new Handler();
 	    handler.postDelayed(new Runnable() {
 	      @Override
 	      public void run() {
 	    	  activity.removeSplashscreen();
 	      }
 	    }, 10000);
 	}
 	
 	public void removeSplashscreen() {
 		if (this.splashScreen != null) {
 			this.splashScreen.dismiss();
 			this.splashScreen = null;
 		}
 	}
 }
