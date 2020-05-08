 package com.smit.EasyLauncher;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 
 
 
 import com.openims.utility.PushServiceUtil;
 import com.smit.DeskView.vodvideo.VODVideoFragment;
 import com.smit.DeskView.vodvideo.VODVideoListFragment;
 import com.smit.MyView.MyViewctrl;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.app.StatusBarManager;
 import android.app.WallpaperManager;
 import android.appwidget.AppWidgetHost;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProviderInfo;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.Intent.ShortcutIconResource;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.os.SystemClock;
 import android.os.SystemProperties;
 import android.provider.LiveFolders;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Selection;
 import android.text.SpannableStringBuilder;
 import android.text.TextUtils;
 import android.text.method.TextKeyListener;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.HapticFeedbackConstants;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Animation.AnimationListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EasyLauncher extends FragmentActivity implements View.OnClickListener, OnLongClickListener,LauncherModel.Callbacks{
     /** Called when the activity is first created. */
 	static final String TAG = "EasyLauncher";
 	static final boolean LOGD = false;
     static final boolean PROFILE_STARTUP = false;
     static final boolean DEBUG_WIDGETS = false;
     static final boolean DEBUG_USER_INTERFACE = false;
     private DragController mDragController;
     private Workspace mWorkspace;
     private DragLayer dragLayer;
     private ImageButton mPagebutton1,mPagebutton2,mPagebutton3,mPagebutton4,mPagebutton5;
     private ImageButton mSetbutton,mNewsbutton,mMoviebutton,mMusicButton,mTvbutton,mAppbutton;
     private ImageView mhomeImageView,imRecy=null;
     private ImageButton mLoginButton;
     private AppWidgetManager mAppWidgetManager;
     private LauncherAppWidgetHost mAppWidgetHost;
     private LayoutInflater mInflater;
 	private Context	mContext;	
     private Animation myAnimation_Rotate;  //旋转
     
     private CellLayout.CellInfo mAddItemCellInfo;
     private CellLayout.CellInfo mMenuAddInfo;
     private final int[] mCellCoordinates = new int[2];
     
     private static final int WALLPAPER_SCREENS_SPAN = 2;
     
     static final int SCREEN_COUNT = 5;
     static final int DEFAULT_SCREEN = 2;
     static final int NUMBER_CELLS_X = 4;
     static final int NUMBER_CELLS_Y = 4;
 
     private LinearLayout mLineLayout; 
     private WindowManager mWindowManager;
     private LinearLayout mControlView;
     
     private static final Object sLock = new Object();
     private static int sScreen = DEFAULT_SCREEN;
    // public LinkedList<WidgetInfo> mwidgetInfo=null;
 	//new LinkedList<WidgetInfo>();
   //  private ImageView mailView;
     private static final String EXTRA_CUSTOM_WIDGET = "custom_widget";	
     
     public static final int SCREEN_H=0,SCREEN_V=1;	//横竖屏
     public static final int SCREEN_WITH=800,SCREEN_HEIGHT=480;
     public int curdir;
     public Rect rcRecy=null;
     private boolean mWorkspaceLoading = true;
     private ImageView mBgImage;
 	static final int APPWIDGET_HOST_ID = 1024;
 	//private LinearLayout preview;
 	private static final int REQUEST_CREATE_SHORTCUT = 1;
 	private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
 	private static final int REQUEST_CREATE_APPWIDGET = 5;
 	private static final int REQUEST_PICK_APPLICATION = 6;
 	private static final int REQUEST_PICK_SHORTCUT = 7;
 	private static final int REQUEST_PICK_LIVE_FOLDER = 8;
 	private static final int REQUEST_PICK_APPWIDGET = 9;
 	private static final int REQUEST_PICK_WALLPAPER = 10;
 	
     
     private static final int MENU_GROUP_ADD = 1;
     private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;
 
     private static final int MENU_ADD = Menu.FIRST + 1;
     private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
     // Type: int
     private static final String RUNTIME_STATE_CURRENT_SCREEN = "EasyLauncher.current_screen";
     // Type: boolean
     private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "EasyLauncher.all_apps_folder";
     // Type: long
     private static final String RUNTIME_STATE_USER_FOLDERS = "EasyLauncher.user_folder";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "EasyLauncher.add_screen";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "EasyLauncher.add_cellX";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "EasyLauncher.add_cellY";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "EasyLauncher.add_spanX";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "EasyLauncher.add_spanY";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "EasyLauncher.add_countX";
     // Type: int
     private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "EasyLauncher.add_countY";
     // Type: int[]
     private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "EasyLauncher.add_occupied_cells";
     // Type: boolean
     private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "EasyLauncher.rename_folder";
     // Type: long
     private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "EasyLauncher.rename_folder_id";
 	
     static final int DIALOG_CREATE_SHORTCUT = 1;
     static final int DIALOG_RENAME_FOLDER = 2;
 	
 
 	private Animation push_pop_amation;
 	private Animation push_in_amation;
 	private Animation netmivie_pop_amation;
 	private Animation netmivie_in_amation;
 	private Animation unreadmail_visble_amation;
 	private Animation unreadmail_gone_amation;
 	private Animation quit_recy_out_amation;
 	private Animation quit_recy_in_amation;
 
 	private View tvView; 
     private boolean mPaused = true;
     private boolean mRestoring;
     private boolean mWaitingForResult;
     private boolean mOnResumeNeedsLoad;
     private boolean mLocaleChanged = false;
    private boolean isLogin = false;
     private ProgressDialog m_Dialog;
 
     private Bundle mSavedInstanceState;
 
     private DeleteZone mDeleteZone;
     private LauncherModel mModel;
     
     private Bundle mSavedState;
     
     private Bitmap mySourceBmp;
     private int widthOrig;
     private int heightOrig;
     static int mCurrentConfiguration=-1;
     
     private View mControl;
     private static boolean isFirstInit=true;
     
     private SpannableStringBuilder mDefaultKeySsb = null;
 	
     private ArrayList<ItemInfo> mDesktopItems = new ArrayList<ItemInfo>();
     
     //private final BroadcastReceiver mCloseSystemDialogsReceiver= new CloseSystemDialogsIntentReceiver();
     private BroadcastReceiver receiver = new LoginStatusReceiver();
     private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (GetCurDir()==SCREEN_V) {
 			curdir=SCREEN_V;
 		}else {
 			curdir=SCREEN_H;
 		}
         if(mCurrentConfiguration==-1||mCurrentConfiguration==curdir)
         {
         	isFirstInit=true;
         	mCurrentConfiguration=curdir;
         }
         else
         {
         	mCurrentConfiguration=curdir;
         	isFirstInit=false;
         }
         mContext = this;
         mDesktopItems.clear();
         mModel = new LauncherModel();
         mModel.initialize(this);
         mDragController = new DragController(this);
         mInflater = getLayoutInflater();
         mAppWidgetManager = AppWidgetManager.getInstance(this);
 		mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
         mAppWidgetHost.startListening();
         
         registerContentObservers();
         //setWallpaperDimension();
       //  setTheme(R.style.Transparent);
         setContentView(R.layout.easylauncher);
         View destopv=findViewById(R.id.destop);
     	myAnimation_Rotate= AnimationUtils.loadAnimation(this,R.anim.my_initrotate_action);
     	//View dragLayer;
     	
         setupViews();
         
         
         mSavedState = savedInstanceState;
         restoreState(mSavedState);
         int mScreen=mWorkspace.getCurrentScreen();
         setPagebuttonFocus(mScreen+1,true);
         if (!mRestoring) {
             mModel.startLoader(this, true, mLocaleChanged);
         }
         if(!isFirstInit)
         destopv.startAnimation(myAnimation_Rotate);
         //isFirstInit=false;
        
 		startService(new Intent(PushServiceUtil.ACTION_SERVICE_CONNECT));
     }
     public static boolean isFirstInit()
     {
     	return isFirstInit;
     }
     public void setPageButton()
     {
     	
     }
     static int getScreen() {
         synchronized (sLock) {
             return sScreen;
         }
     }
 
     static void setScreen(int screen) {
         synchronized (sLock) {
             sScreen = screen;
         }
     }
     public void setBgImage()
     {
     	if(GetCurDir()==SCREEN_H)
     	{
     		setBgViewRotate(270);
     	}else
     	{
     		setBgViewRotate(0);
     	}
     }
     private void setBgViewRotate(int rotate)
     {
     	// scaleTimes缁存寔1:1鐨勫楂樻瘮渚�				
 		int newWidth = widthOrig ;
 		int newHeight = heightOrig ;
 
 		float scaleWidth = ((float) newWidth) / widthOrig;
 		float scaleHeigth = ((float) newHeight) / heightOrig;
 		Matrix matrix = new Matrix();
 		// 浣跨敤Matrix.postRotate鐨勬柟娉曡缃淮搴�				
 		matrix.postRotate(5 * rotate);
 
 		// 鍒涘缓鏂扮殑Bitmap瀵硅薄
 		Bitmap resizedBitmap = Bitmap.createBitmap(mySourceBmp, 0, 0,
 				widthOrig, heightOrig, matrix, true);
 		@SuppressWarnings("deprecation")
 		BitmapDrawable myNewBitmapDrawable = new BitmapDrawable(
 				resizedBitmap);
 		//resizedBitmap.recycle();
 
 		mBgImage.setImageDrawable(myNewBitmapDrawable);
     }
     private BitmapDrawable SetBmpRotate(Bitmap bm)
     {
 			
 		Matrix matrix = new Matrix();
 		// 浣跨敤Matrix.postRotate鐨勬柟娉曡缃淮搴�				
 		matrix.postRotate(5 * 90);
 
 		// 鍒涘缓鏂扮殑Bitmap瀵硅薄
 		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0,
 				bm.getWidth(), bm.getHeight(), matrix, true);
 		@SuppressWarnings("deprecation")
 		BitmapDrawable myNewBitmapDrawable = new BitmapDrawable(
 				resizedBitmap);
     	
 		return myNewBitmapDrawable;
     	
     }
     private void setWallpaperDimension() {
         WallpaperManager wpm = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);
 
         Display display = getWindowManager().getDefaultDisplay();
         boolean isPortrait = display.getWidth() < display.getHeight();
 
         final int width = isPortrait ? display.getWidth() : display.getHeight();
         final int height = isPortrait ? display.getHeight() : display.getWidth();
         wpm.suggestDesiredDimensions(width * WALLPAPER_SCREENS_SPAN, height);
     }
 	private void setupViews() {
 		// TODO Auto-generated method stub
 		DragController dragController = mDragController;
 
         dragLayer = (DragLayer) findViewById(R.id.drag_layer);
 
         dragLayer.setDragController(dragController);
         
         mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
         final Workspace workspace = mWorkspace;
         workspace.setHapticFeedbackEnabled(false);
         DeleteZone deleteZone = (DeleteZone) dragLayer.findViewById(R.id.delete_zone);
         mDeleteZone = deleteZone;
         
        // LinearLayout favorite = (LinearLayout) mInflater.inflate(R.layout.control, null, false);
         mControlView=(LinearLayout) View.inflate(getApplicationContext(), R.layout.control, null);
         mControl=mControlView.findViewById(R.id.all_apps_button_cluster);
 //       WindowManager wm=(WindowManager)getApplicationContext()
 //    	.getSystemService("window"); 
 //    	WindowManager.LayoutParams wmParams =new WindowManager.LayoutParams(); 
 //    	wmParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; 
 //    	wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
 //    	wmParams.width=60;//WindowManager.LayoutParams.WRAP_CONTENT; 
 //   	wmParams.height=(int) 400;//WindowManager.LayoutParams.WRAP_CONTENT; 
 //  	wm.addView(mControl,wmParams);
  
   	
         
       //  mailView=(ImageView)findViewById(R.id.iconmail);
         mLoginButton=(ImageButton)findViewById(R.id.bt_login);
         mSetbutton=(ImageButton)mControlView.findViewById(R.id.bt_setting);
         mNewsbutton=(ImageButton)mControlView.findViewById(R.id.bt_info);
         mMoviebutton=(ImageButton)mControlView.findViewById(R.id.bt_movie);
         mMusicButton=(ImageButton)mControlView.findViewById(R.id.bt_music);
         mTvbutton=(ImageButton)mControlView.findViewById(R.id.bt_tv);
         mAppbutton=(ImageButton)mControlView.findViewById(R.id.bt_allapp);
         mSetbutton.setOnClickListener(controlbt_click);
         mNewsbutton.setOnClickListener(controlbt_click);
         mMoviebutton.setOnClickListener(controlbt_click);
         mMusicButton.setOnClickListener(controlbt_click);
         mTvbutton.setOnClickListener(controlbt_click);
         mAppbutton.setOnClickListener(controlbt_click);
 
         mPagebutton1=(ImageButton)findViewById(R.id.page1);
         mPagebutton2=(ImageButton)findViewById(R.id.page2);
         mPagebutton3=(ImageButton)findViewById(R.id.page3);
         mPagebutton4=(ImageButton)findViewById(R.id.page4);
         mPagebutton5=(ImageButton)findViewById(R.id.page5);
         mPagebutton1.setOnClickListener(page1_click);
         mPagebutton2.setOnClickListener(page2_click);
         mPagebutton3.setOnClickListener(page3_click);
         mPagebutton4.setOnClickListener(page4_click);
         mPagebutton5.setOnClickListener(page5_click);
         
         mLoginButton.setOnClickListener(login_click);
         mBgImage=(ImageView) findViewById(R.id.bgimage);
       //  mBgImage.setBackgroundResource(R.drawable.s0_bg);//(R.drawable.s0_bg);//BitmapFactory.decodeResource(getResources(),
 				//R.drawable.s0_bg);
         mySourceBmp=BitmapFactory.decodeResource(getResources(),
 				R.drawable.s0_bg);
 		widthOrig = mySourceBmp.getWidth();
 		heightOrig = mySourceBmp.getHeight();
 		
 		// 绋嬪簭鍒氳繍琛岋紝鍔犺浇榛樿鐨凞rawable
 		mBgImage.setImageBitmap(mySourceBmp);
 	//	setBgImage();
         workspace.setOnLongClickListener(this);
       // workspace.setOnLongClickListener(this);
         workspace.setDragController(dragController);
         workspace.setLauncher(this);
        // mWorkspace.setBackgroundDrawable(R.drawable.bg_icon);
         //mWorkspace.setBackgroundResource(R.drawable.bg_icon_0);
        /// mWorkspace.
 
   	
     	
         deleteZone.setLauncher(this);
         deleteZone.setDragController(dragController);
         deleteZone.setHandle(mControl);
         
         dragController.setDragScoller(workspace);
         dragController.setScrollView(dragLayer);
         dragController.setDragListener(deleteZone);
         dragController.setMoveTarget(workspace);
 
         // The order here is bottom to top.
         dragController.addDropTarget(workspace);
         dragController.addDropTarget(deleteZone);
         
         mDefaultKeySsb = new SpannableStringBuilder();
         Selection.setSelection(mDefaultKeySsb, 0);
 
         mWindowManager=(WindowManager)getApplicationContext()
 	    	.getSystemService("window"); 
 	    	
 	    	WindowManager.LayoutParams wmParams =new WindowManager.LayoutParams(); 
 	    	wmParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; 
 	    	wmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
 	    		|WindowManager.LayoutParams.ALPHA_CHANGED
 	    		|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
 	    	wmParams.width=WindowManager.LayoutParams.WRAP_CONTENT; 
 	    	wmParams.height=WindowManager.LayoutParams.WRAP_CONTENT; 
 	    	wmParams.format = PixelFormat.TRANSLUCENT;
 	    	wmParams.windowAnimations = 0;
 	        if (curdir!=SCREEN_V) {
 	        	wmParams.gravity=Gravity.CENTER_VERTICAL|Gravity.RIGHT;
 			}else {
 				wmParams.gravity=Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
 			}
 	    	//wmParams.gravity=Gravity.CENTER_VERTICAL|Gravity.RIGHT;
 	    	//wmParams.alpha=(float) 0.4;
 	    	
 		  // 	Button b = new Button(getApplicationContext());
 		   //	favorite.setOnTouchListener(l)
 	    	mControlView.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View arg0) {				
 				}
 				
 			});;
 			mControlView.setOnTouchListener(new OnTouchListener()
 			{
 
 				@Override
 				public boolean onTouch(View arg0, MotionEvent arg1) {
 					// TODO Auto-generated method stub
 					return false;
 				}
 				
 			});;
 			//b.setText("hello");
 			mWindowManager.addView(mControlView, wmParams);
 
 	}
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
     	// TODO Auto-generated method stub
     	
     	super.onConfigurationChanged(newConfig);
 //    	if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE)
 //    	{
 //    		//mWorkspace.startAnimation(animation);
 //			
 //    	}
     	
         try {
             mAppWidgetHost.stopListening();
         } catch (NullPointerException ex) {
             Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
         }
 
         TextKeyListener.getInstance().release();
 
         mModel.stopLoader();
 
         unbindDesktopItems();
 
         getContentResolver().unregisterContentObserver(mWidgetObserver);
         
         if (GetCurDir()==SCREEN_V) {
 			curdir=SCREEN_V;
 		}else {
 			curdir=SCREEN_H;
 		}
         mDesktopItems.clear();
         mModel = new LauncherModel();
         mModel.initialize(this);
         mDragController = new DragController(this);
         mInflater = getLayoutInflater();
         mAppWidgetManager = AppWidgetManager.getInstance(this);
 		mAppWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
         mAppWidgetHost.startListening();
         
         registerContentObservers();
         setWallpaperDimension();
       //  this.getWindow().
     	setContentView(R.layout.easylauncher);
 //        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
 //        {
 //          /* 若当下为横排，则更改为竖排呈现 */
 //          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 //        } else if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
 //        {O
 //          /* 若当下为竖排，则更改为横排呈现 */
 //          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 //        }
 
 	//	setContentView(R.layout.easylauncher);
 		//setupViews();
 		//this.
     	//newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE
     	
     	
     }
 	public void setPagebuttonFocus(int index ,boolean isfocus)
 	{
 //	    Drawable[] array = new Drawable[] {
 //	    		this.getResources().getDrawable(R.drawable.page),
 //	    		this.getResources().getDrawable(R.drawable.page1),
 //	    		this.getResources().getDrawable(R.drawable.page2),
 //	    		this.getResources().getDrawable(R.drawable.page3),
 //	    		this.getResources().getDrawable(R.drawable.page4),
 //	    		this.getResources().getDrawable(R.drawable.page5)
 //	    		};
 	    Drawable[] array = new Drawable[] {
 	    		this.getResources().getDrawable(R.drawable.pagebg),
 	    		this.getResources().getDrawable(R.drawable.paged),
 	    		this.getResources().getDrawable(R.drawable.paged),
 	    		this.getResources().getDrawable(R.drawable.paged),
 	    		this.getResources().getDrawable(R.drawable.paged),
 	    		this.getResources().getDrawable(R.drawable.paged)
 	    		};
 		switch(index)
 		{
 			case 1:
 			{
 				
 				if(isfocus)
 					mPagebutton1.setImageDrawable(array[1]);//(R.drawable.page1);
 				else
 					mPagebutton1.setImageDrawable(array[0]);
 			}
 			break;
 			case 2:
 			{
 				if(isfocus)
 					mPagebutton2.setImageDrawable(array[2]);
 				else
 					mPagebutton2.setImageDrawable(array[0]);
 			}
 			break;
 			case 3:
 			{
 				if(isfocus)
 					mPagebutton3.setImageDrawable(array[3]);
 				else
 					mPagebutton3.setImageDrawable(array[0]);
 			}
 			break;
 			case 4:
 			{
 				if(isfocus)
 					mPagebutton4.setImageDrawable(array[4]);
 				else
 					mPagebutton4.setImageDrawable(array[0]);
 			}
 			break;
 			case 5:
 			{
 				if(isfocus)
 					mPagebutton5.setImageDrawable(array[5]);
 				else
 					mPagebutton5.setImageDrawable(array[0]);
 			}
 			break;
 		}
 	}
 	private ImageButton.OnClickListener controlbt_click= new ImageButton.OnClickListener()
 	{
 //        mSetbutton.setOnClickListener(controlbt_click);
 //        mNewsbutton.setOnClickListener(controlbt_click);
 //        mMoviebutton.setOnClickListener(controlbt_click);
 //        mMusicButton.setOnClickListener(controlbt_click);
 //        mTvbutton.setOnClickListener(controlbt_click);
 //        mAppbutton.setOnClickListener(controlbt_click);
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			if(arg0 == mSetbutton)
 			{
 			
 			
 			}
 			if(arg0 == mNewsbutton)
 			{
 			
 			
 			}
 			if(arg0 == mMoviebutton)
 			{
 
 	            String packageName ="com.yinhui.EasyTouch";
 	            String className ="com.yinhui.EasyTouch.EasyTouch";
 	            boolean hasPackage = true;
 	            ComponentName cn = new ComponentName(packageName, className);
 	            if(cn!=null){
 				 Intent it = new Intent(Intent.ACTION_VIEW);  
 				 it.setComponent(cn);
 				 startActivity(it);
 	            }
 			
 			}
 			if(arg0 ==  mMusicButton)
 			{
 			
 			
 			}
 			if(arg0 == mTvbutton)
 			{
 			
 			
 			}
 			if(arg0==mAppbutton)
 			{
 				
 			}
 			
 		}
 	};
 	
 	private ImageButton.OnClickListener page1_click= new ImageButton.OnClickListener()
 	{
    
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			int index=mWorkspace.getCurrentScreen()+1;
 			if(index!=1)
 			{
 				mWorkspace.snapToScreen(0);
 			}
 		}
 		
 	};
 	private ImageButton.OnClickListener page2_click= new ImageButton.OnClickListener()
 	{
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			int index=mWorkspace.getCurrentScreen()+1;
 			if(index!=2)
 			{
 				mWorkspace.snapToScreen(1);
 			}
 		}
 		
 	};
 	private ImageButton.OnClickListener page3_click= new ImageButton.OnClickListener()
 	{
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			int index=mWorkspace.getCurrentScreen()+1;
 			if(index!=3)
 			{
 				mWorkspace.snapToScreen(2);
 			}
 		}
 		
 	};
 	private ImageButton.OnClickListener page4_click= new ImageButton.OnClickListener()
 	{
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			int index=mWorkspace.getCurrentScreen()+1;
 			if(index!=4)
 			{
 				mWorkspace.snapToScreen(3);
 			}
 		}
 		
 	};
 	private ImageButton.OnClickListener login_click=new ImageButton.OnClickListener()
 	{
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			if(!isLogin){
 					Intent intent = new Intent();
 					//Bundle extras = new Bundle();
 					
 					//extras.putString("indexHTMLPath", info.getPath());
 					//intent.putExtras(extras);
 					intent.setClass(mContext, LoginActivity.class);
 					//LoginActivity
 					startActivity(intent);	
 		//			final LoginView mQuickAction 	= new LoginView(arg0);
 		//			//mQuickAction.window.getContentView().setf
 		//			mQuickAction.show();
 			}else{
 		    	m_Dialog = ProgressDialog.show
                 (
                   mContext,
                   getString(R.string.login_wait),
                   getString(R.string.login_quiting), 
                   true
                 );
 		    		
 		    	startService(new Intent(PushServiceUtil.ACTION_SERVICE_LOGOUT));	
 			}
 
 		}
 		
 	};
 	private ImageButton.OnClickListener page5_click= new ImageButton.OnClickListener()
 	{
 
 		@Override
 		public void onClick(View arg0) {
 			// TODO Auto-generated method stub
 			int index=mWorkspace.getCurrentScreen()+1;
 			if(index!=5)
 			{
 				mWorkspace.snapToScreen(4);
 			}
 		}
 		
 	};
 	  //得到屏幕方向
 	public int GetCurDir(){
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
 			return SCREEN_H;
 		}else{
 			return SCREEN_V;
 		}
 	}
 	public int GetScreenDir(){
 		return curdir;
 	}
 	
 	public void popRecy(){
 
 		if (imRecy.getVisibility()==View.GONE) {
 			imRecy.setVisibility(View.INVISIBLE);   		
 			quit_recy_out_amation.setAnimationListener(new AnimationListener(){
 				public void onAnimationStart(Animation arg0) {	
 				}
 				public void onAnimationRepeat(Animation arg0) {
 				}
 				public void onAnimationEnd(Animation arg0) {
 					mhomeImageView.setVisibility(View.GONE);
 					imRecy.setImageResource(R.drawable.trashcan);
 					imRecy.setVisibility(View.VISIBLE);
 					imRecy.startAnimation(quit_recy_in_amation);
 				}
 			});
 			mhomeImageView.startAnimation(quit_recy_out_amation);
 		}	    	
 	}
     public void popfocusRecy(boolean flag){
     	if (flag) {
     		imRecy.setImageResource(R.drawable.trashcan_hover);
 	    	imRecy.setVisibility(View.VISIBLE);
 		}else {
 			imRecy.setImageResource(R.drawable.trashcan);
 	    	//imRecy.setVisibility(View.VISIBLE);
 		}
     
     }
     
     public void hideRecy(){
     	if (mhomeImageView.getVisibility()==View.GONE) {
     		
 	    	mhomeImageView.setVisibility(View.INVISIBLE);
 	    	quit_recy_out_amation.setAnimationListener(new AnimationListener(){
 	    		 public void onAnimationStart(Animation arg0) {	
 	    		 }
 	    		 public void onAnimationRepeat(Animation arg0) {
 	    		 }
 	    		 public void onAnimationEnd(Animation arg0) {
 	    			 imRecy.setVisibility(View.GONE);
 	    			 mhomeImageView.setVisibility(View.VISIBLE);
 	    			 mhomeImageView.startAnimation(quit_recy_in_amation);
 	    		 }
 	    	});
 	    	imRecy.startAnimation(quit_recy_out_amation);
 		}
     
     }
     
     //是不是在回收站上
     public boolean IsOvewRecy(int x,int y) {
 		boolean ret=false;
 		if(rcRecy.contains(x, y)){
 			ret=true;
 		}else{
 			ret=false;
 		}
 		
 		return ret;
 	}
     /**
      * 请求添加一个新的widget
      */
     private void addWidget() {
     	int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
 
         Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
         pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
         // add the search widget
 /*        ArrayList<AppWidgetProviderInfo> customInfo =
                 new ArrayList<AppWidgetProviderInfo>();
         AppWidgetProviderInfo info = new AppWidgetProviderInfo();
         info.provider = new ComponentName(getPackageName(), "XXX.YYY");
         info.label = "Search";
         info.icon = R.drawable.ic_search_widget;
         customInfo.add(info);
         pickIntent.putParcelableArrayListExtra(
                 AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
         ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
         Bundle b = new Bundle();
         b.putString(EXTRA_CUSTOM_WIDGET, "search_widget");
         customExtras.add(b);
         pickIntent.putParcelableArrayListExtra(
                 AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
 */
         // start the pick activity
         startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
     }  
   //del a widget
     public void delAWidget(View v){
     	int i=0;
     	
     	while (true) {	
     		int count=mDesktopItems.size();	
     		for (i = 0; i < count; i++) {
     			ItemInfo widget=mDesktopItems.get(i);
     			if (widget!=null) {
     				
     				//widgetmanage.setWidgetCell(widget.untionCell, -1);
     				mWorkspace.removeView(v);
     				mDesktopItems.remove(i);
     				break;
     			}		
     		}
     		
     		if (i==count) {
     			break;
     		}
     		
     	}
     	
     	}
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
         mWaitingForResult = false;
 
         // The pattern used here is that a user PICKs a specific application,
         // which, depending on the target, might need to CREATE the actual target.
 
         // For example, the user would PICK_SHORTCUT for "Music playlist", and we
         // launch over to the Music app to actually CREATE_SHORTCUT.
 
         if (resultCode == RESULT_OK && mAddItemCellInfo != null) {
             switch (requestCode) {
                 case REQUEST_PICK_APPLICATION:
                    // completeAddApplication(this, data, mAddItemCellInfo);
                     break;
                 case REQUEST_PICK_SHORTCUT:
                    // processShortcut(data);
                     break;
                 case REQUEST_CREATE_SHORTCUT:
                    // completeAddShortcut(data, mAddItemCellInfo);
                     break;
                 case REQUEST_PICK_LIVE_FOLDER:
                    // addLiveFolder(data);
                     break;
                 case REQUEST_CREATE_LIVE_FOLDER:
                     //completeAddLiveFolder(data, mAddItemCellInfo);
                     break;
                 case REQUEST_PICK_APPWIDGET:
                     addAppWidget(data);
                     break;
                 case REQUEST_CREATE_APPWIDGET:
                     completeAddAppWidget(data, mAddItemCellInfo);
                     break;
                 case REQUEST_PICK_WALLPAPER:
                     // We just wanted the activity result here so we can clear mWaitingForResult
                     break;
             }
         } else if ((requestCode == REQUEST_PICK_APPWIDGET ||
                 requestCode == REQUEST_CREATE_APPWIDGET) && resultCode == RESULT_CANCELED &&
                 data != null) {
             // Clean up the appWidgetId if we canceled
             int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
             if (appWidgetId != -1) {
                 mAppWidgetHost.deleteAppWidgetId(appWidgetId);
             }
         }
 	}
     
     
     @Override
     protected void onResume() {
         super.onResume();
         mPaused = false;
         if (mRestoring || mOnResumeNeedsLoad) {
             mWorkspaceLoading = true;
           //  mModel.startLoader(this, true);
             mRestoring = false;
             mOnResumeNeedsLoad = false;
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mPaused = true;
       //  dismissPreview(mPreviousView);
        // dismissPreview(mNextView);
         mDragController.cancelDrag();
     }
     private boolean acceptFilter() {
         final InputMethodManager inputManager = (InputMethodManager)
                 getSystemService(Context.INPUT_METHOD_SERVICE);
         return !inputManager.isFullscreenMode();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         boolean handled = super.onKeyDown(keyCode, event);
         if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
             boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                     keyCode, event);
             if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                 // something usable has been typed - start a search
                 // the typed text will be retrieved and cleared by
                 // showSearchDialog()
                 // If there are multiple keystrokes before the search dialog takes focus,
                 // onSearchRequested() will be called for every keystroke,
                 // but it is idempotent, so it's fine.
                 return onSearchRequested();
             }
         }
 
         // Eat the long press event so the keyboard doesn't come up.
         if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
             return true;
         }

         return handled;
     }
 
     private String getTypedText() {
         return mDefaultKeySsb.toString();
     }
 
     private void clearTypedText() {
         mDefaultKeySsb.clear();
         mDefaultKeySsb.clearSpans();
         Selection.setSelection(mDefaultKeySsb, 0);
     }
 
     
     /**
      * Restores the previous state, if it exists.
      *
      * @param savedState The previous state.
      */
     private void restoreState(Bundle savedState) {
         if (savedState == null) {
             return;
         }
 
         final boolean allApps = savedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
         if (allApps) {
           //  showAllApps(false);
         }
 
         final int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
         if (currentScreen > -1) {
             mWorkspace.setCurrentScreen(currentScreen);
         }
 
         final int addScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
         if (addScreen > -1) {
             mAddItemCellInfo = new CellLayout.CellInfo();
             final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
             addItemCellInfo.valid = true;
             addItemCellInfo.screen = addScreen;
             addItemCellInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
             addItemCellInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
             addItemCellInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
             addItemCellInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
             addItemCellInfo.findVacantCellsFromOccupied(
                     savedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
                     savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
                     savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
             mRestoring = true;
         }
 
         boolean renameFolder = savedState.getBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, false);
         if (renameFolder) {
             long id = savedState.getLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID);
            // mFolderInfo = mModel.getFolderById(this, sFolders, id);
             mRestoring = true;
         }
     }
     
     
     @SuppressWarnings({"UnusedDeclaration"})
     public void previousScreen(View v) {
     	mWorkspace.scrollLeft();
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public void nextScreen(View v) {
     	mWorkspace.scrollRight();
     }
     private  int indexd=0;
     /**
      * Add a widget to the workspace.
      *
      * @param data The intent describing the appWidgetId.
      * @param cellInfo The position on screen where to create the widget.
      */
     private void completeAddAppWidget(Intent data, CellLayout.CellInfo cellInfo) {
         Bundle extras = data.getExtras();
         int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
 
         if (LOGD) Log.d(TAG, "dumping extras content=" + extras.toString());
 
         AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
 
         // Calculate the grid spans needed to fit this widget
         CellLayout layout = (CellLayout) mWorkspace.getChildAt(cellInfo.screen);
         int[] spans = layout.rectToCell(appWidgetInfo.minWidth, appWidgetInfo.minHeight);
 
         // Try finding open space on Launcher screen
         final int[] xy = mCellCoordinates;
         if (!findSlot(cellInfo, xy, spans[0], spans[1])) {
             if (appWidgetId != -1) mAppWidgetHost.deleteAppWidgetId(appWidgetId);
             return;
         }
 
         // Build Launcher-specific widget info and save to database
         LauncherAppWidgetInfo launcherInfo = new LauncherAppWidgetInfo(appWidgetId);
         launcherInfo.spanX = spans[0];
         launcherInfo.spanY = spans[1];
 
         LauncherModel.addItemToDatabase(this, launcherInfo,
                 LauncherSettings.Favorites.CONTAINER_DESKTOP,
                 mWorkspace.getCurrentScreen(), xy[0], xy[1], false);
 
         if (!mRestoring) {
             mDesktopItems.add(launcherInfo);
 
             // Perform actual inflation because we're live
             launcherInfo.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
 
             launcherInfo.hostView.setAppWidget(appWidgetId, appWidgetInfo);
             launcherInfo.hostView.setTag(launcherInfo);
 
             mWorkspace.addInCurrentScreen(launcherInfo.hostView, xy[0], xy[1],
                     launcherInfo.spanX, launcherInfo.spanY, isWorkspaceLocked());
         }
     }
 
     public void removeAppWidget(LauncherAppWidgetInfo launcherInfo) {
         mDesktopItems.remove(launcherInfo);
         launcherInfo.hostView = null;
     }
 
     public LauncherAppWidgetHost getAppWidgetHost() {
         return mAppWidgetHost;
     }
     
     void closeSystemDialogs() {
         getWindow().closeAllPanels();
 
         try {
             dismissDialog(DIALOG_CREATE_SHORTCUT);
             // Unlock the workspace if the dialog was showing
         } catch (Exception e) {
             // An exception is thrown if the dialog is not visible, which is fine
         }
 
         try {
             dismissDialog(DIALOG_RENAME_FOLDER);
             // Unlock the workspace if the dialog was showing
         } catch (Exception e) {
             // An exception is thrown if the dialog is not visible, which is fine
         }
 
         // Whatever we were doing is hereby canceled.
         mWaitingForResult = false;
     }
 //  //载入widget
 //    public void loadWidgetDatebase(){
 //    	int count=0;	
 //    	Cursor cursor = getContentResolver().query(Uri.parse(Users.User.CONTENT_URI+"/"+PersonalityProvider.TABLE_WIDGET), null, null, null, null);
 //    	if (cursor==null) {
 //    		return;
 //    	}
 //    	count=cursor.getCount();
 //    	if (count<=0) {
 //    		return;
 //    	}
 //    	mwidgetInfo.clear();
 //    	for (int i = 0; i < count; i++) {
 //    		cursor.moveToPosition(i);
 //    		WidgetInfo widgetInfo=new WidgetInfo();
 //    		widgetInfo.posx=cursor.getInt(1);
 //    		widgetInfo.posy=cursor.getInt(2);
 //    		widgetInfo.cellx=cursor.getInt(3);
 //    		widgetInfo.celly=cursor.getInt(4);
 //    		widgetInfo.widgetid=cursor.getInt(5);
 //    		widgetInfo.widgetsetid=i+1;
 //    		mwidgetInfo.add(widgetInfo);
 //    	}	
 //    }
 //
 //    //保存widget
 //    public void saveWidgetDatebase(){
 //    	int count=0;
 //    	WidgetInfo widgetInfo;
 //    	final AppWidgetManager widgets = AppWidgetManager.getInstance(this);
 //    	count=mwidgetInfo.size();
 //    	//Cursor cursor = getContentResolver().query(Uri.parse(Users.User.CONTENT_URI+"/"+PersonalityProvider.TABLE_WIDGET), null, null, null, null);
 //    	getContentResolver().delete(Uri.parse(Users.User.CONTENT_URI+"/"+PersonalityProvider.TABLE_WIDGET), null, null);
 //    	for (int i = 0; i < count; i++) {
 //    		widgetInfo=mwidgetInfo.get(i);
 //            int appWidgetId = widgetInfo.widgetid;
 //            final AppWidgetProviderInfo provider = widgets.getAppWidgetInfo(appWidgetId);
 //            if (provider!=null) {
 //            	ContentValues values = new ContentValues();	
 //            	
 //            	values.put("_pos_x", widgetInfo.posx);
 //    			values.put("_pos_y",widgetInfo.posy);
 //    			values.put("_cell_x", widgetInfo.cellx);
 //    			values.put("_cell_y", widgetInfo.celly);
 //    			values.put("_widget_id", widgetInfo.widgetid);
 //    			values.put("_widget_add_id",i+1);
 //    			getContentResolver().insert(Uri.parse(Users.User.CONTENT_URI+"/"+PersonalityProvider.TABLE_WIDGET), values);
 //    		}
 //    		
 //    	}
 //    }
 	@Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
 
         // Close the menu
         if (Intent.ACTION_MAIN.equals(intent.getAction())) {
             // also will cancel mWaitingForResult.
             closeSystemDialogs();
 
             boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                         != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
             boolean allAppsVisible = isAllAppsVisible();
             if (!mWorkspace.isDefaultScreenShowing()) {
                 mWorkspace.moveToDefaultScreen(alreadyOnHome && !allAppsVisible);
             }
             closeAllApps(alreadyOnHome && allAppsVisible);
 
             final View v = getWindow().peekDecorView();
             if (v != null && v.getWindowToken() != null) {
                 InputMethodManager imm = (InputMethodManager)getSystemService(
                         INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
             }
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         // Do not call super here
         mSavedInstanceState = savedInstanceState;
     }
     
 	@Override
 	protected void onStart() {
 
 		super.onStart();
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(PushServiceUtil.ACTION_STATUS);
         registerReceiver(receiver, intentFilter); 
 	}
     
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		//mWindowManager.removeView(mControlView);
 		//mWindowManager.
 		super.onStop();
         unregisterReceiver(receiver); 
 	}
 	
     public class LoginStatusReceiver extends BroadcastReceiver{        
     	@Override
     	public void onReceive(Context context,Intent intent){
 
     		if(intent.getAction().equals(PushServiceUtil.ACTION_STATUS)){
 	    		String status = intent.getStringExtra(PushServiceUtil.PUSH_STATUS);
 
 	    		if(status.equals(PushServiceUtil.PUSH_STATUS_LOGIN_SUC)){
 	    			if(!isLogin){
 	    			mLoginButton.setImageResource(R.drawable.quitface_selector);	
 	    			isLogin = true;
 	    			}
 	    		}else if(status.equals(PushServiceUtil.PUSH_STATUS_LOGOUT)){
 	    			if(isLogin){
 		    			if(m_Dialog != null){
 		    				m_Dialog.dismiss();
 		    			}	    
 	    			mLoginButton.setImageResource(R.drawable.unloginface_selector);	
 	    			isLogin = false;	
 					Toast.makeText(context, R.string.login_quit_safely, Toast.LENGTH_SHORT).show();		
 	    			}
     			}
     		}
     	}
     }	
 	
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	int page=mWorkspace.getCurrentScreen();
     	//mWindowManager.removeView(mControlView);
         outState.putInt(RUNTIME_STATE_CURRENT_SCREEN, page);
         super.onSaveInstanceState(outState);
         /*
         final ArrayList<Folder> folders = mWorkspace.getOpenFolders();
         if (folders.size() > 0) {
             final int count = folders.size();
             long[] ids = new long[count];
             for (int i = 0; i < count; i++) {
                 final FolderInfo info = folders.get(i).getInfo();
                 ids[i] = info.id;
             }
             outState.putLongArray(RUNTIME_STATE_USER_FOLDERS, ids);
         } else {
             super.onSaveInstanceState(outState);
         }
         */
 
         // TODO should not do this if the drawer is currently closing.
         if (isAllAppsVisible()) {
             outState.putBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, true);
         }
 
         if (mAddItemCellInfo != null && mAddItemCellInfo.valid && mWaitingForResult) {
             final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
             final CellLayout layout = (CellLayout) mWorkspace.getChildAt(addItemCellInfo.screen);
 
             outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, addItemCellInfo.screen);
             outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, addItemCellInfo.cellX);
             outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, addItemCellInfo.cellY);
             outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, addItemCellInfo.spanX);
             outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, addItemCellInfo.spanY);
             outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X, layout.getCountX());
             outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y, layout.getCountY());
             outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
                    layout.getOccupiedCells());
         }
 
 //        if (mFolderInfo != null && mWaitingForResult) {
 //            outState.putBoolean(RUNTIME_STATE_PENDING_FOLDER_RENAME, true);
 //            outState.putLong(RUNTIME_STATE_PENDING_FOLDER_RENAME_ID, mFolderInfo.id);
 //        }
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
       
    	startService(new Intent(PushServiceUtil.ACTION_SERVICE_LOGOUT));	
 
         try {
             mAppWidgetHost.stopListening();
         } catch (NullPointerException ex) {
             Log.w(TAG, "problem while stopping AppWidgetHost during Launcher destruction", ex);
         }
 
         TextKeyListener.getInstance().release();
 
         mModel.stopLoader();
 
         unbindDesktopItems();
 
         getContentResolver().unregisterContentObserver(mWidgetObserver);
         mWindowManager.removeView(mControlView);
         mySourceBmp.recycle();
       //  mBgImage.destroyDrawingCache();
       // mWindowManager.mWindowManagermWindowManager
        //mBgImage.getDrawable().
         //dismissPreview(mPreviousView);
        // dismissPreview(mNextView);
 
        // unregisterReceiver(mCloseSystemDialogsReceiver);
     }
 
     @Override
     public void startActivityForResult(Intent intent, int requestCode) {
         if (requestCode >= 0) mWaitingForResult = true;
         super.startActivityForResult(intent, requestCode);
     }
 
     @Override
     public void startSearch(String initialQuery, boolean selectInitialQuery,
             Bundle appSearchData, boolean globalSearch) {
 
         closeAllApps(true);
 /* 修改屏蔽错误1
         if (initialQuery == null) {
             // Use any text typed in the launcher as the initial query
             initialQuery = getTypedText();
             clearTypedText();
         }
         if (appSearchData == null) {
             appSearchData = new Bundle();
             appSearchData.putString(Search.SOURCE, "launcher-search");
         }
 
         final SearchManager searchManager =
                 (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
             appSearchData, globalSearch);
             */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         if (isWorkspaceLocked()) {
             return false;
         }
 
         super.onCreateOptionsMenu(menu);
 
         menu.add(MENU_GROUP_ADD, MENU_ADD, 0, R.string.menu_add)
                 .setIcon(android.R.drawable.ic_menu_add)
                 .setAlphabeticShortcut('A');
 //        menu.add(0, MENU_MANAGE_APPS, 0, R.string.menu_manage_apps)
 //                .setIcon(android.R.drawable.ic_menu_manage)
 //                .setAlphabeticShortcut('M');
         menu.add(MENU_GROUP_WALLPAPER, MENU_WALLPAPER_SETTINGS, 0, R.string.menu_wallpaper)
                  .setIcon(android.R.drawable.ic_menu_gallery)
                  .setAlphabeticShortcut('W');
 //        menu.add(0, MENU_SEARCH, 0, R.string.menu_search)
 //                .setIcon(android.R.drawable.ic_search_category_default)
 //                .setAlphabeticShortcut(SearchManager.MENU_KEY);
 //        menu.add(0, MENU_NOTIFICATIONS, 0, R.string.menu_notifications)
 //                .setIcon(com.android.internal.R.drawable.ic_menu_notifications)
 //                .setAlphabeticShortcut('N');
 
 //        final Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
 //        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
 //                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
 //
 //        menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings)
 //                .setIcon(android.R.drawable.ic_menu_preferences).setAlphabeticShortcut('P')
 //                .setIntent(settings);
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         // If all apps is animating, don't show the menu, because we don't know
         // which one to show.
 
 
         // Only show the add and wallpaper options when we're not in all apps.
         boolean visible = true;//!mAllAppsGrid.isOpaque();
         menu.setGroupVisible(MENU_GROUP_ADD, visible);
         menu.setGroupVisible(MENU_GROUP_WALLPAPER, visible);
 
         // Disable add if the workspace is full.
         if (visible) {
             mMenuAddInfo = mWorkspace.findAllVacantCells(null);
             menu.setGroupEnabled(MENU_GROUP_ADD, mMenuAddInfo != null && mMenuAddInfo.valid);
         }
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_ADD:
                 addItems();
                 return true;
 //            case MENU_MANAGE_APPS:
 //                manageApps();
 //                return true;
             case MENU_WALLPAPER_SETTINGS:
                 startWallpaper();
                 return true;
 //            case MENU_SEARCH:
 //                onSearchRequested();
 //                return true;
 //            case MENU_NOTIFICATIONS:
 //                showNotifications();
 //                return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Indicates that we want global search for this activity by setting the globalSearch
      * argument for {@link #startSearch} to true.
      */
 
     @Override
     public boolean onSearchRequested() {
         startSearch(null, false, null, true);
         return true;
     }
 
     public boolean isWorkspaceLocked() {
         return mWorkspaceLoading || mWaitingForResult;
     }
 
     private void addItems() {
         closeAllApps(true);
         showAddDialog(mMenuAddInfo);
     }
 
     private void manageApps() {
         startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS));
     }
 
     void addAppWidget(Intent data) {
         // TODO: catch bad widget exception when sent
         int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
         AppWidgetProviderInfo appWidget = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
 
         if (appWidget.configure != null) {
             // Launch over to configure widget, if needed
             Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
             intent.setComponent(appWidget.configure);
             intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 
             startActivityForResultSafely(intent, REQUEST_CREATE_APPWIDGET);
         } else {
             // Otherwise just add it
             onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK, data);
         }
     }
 
     void processShortcut(Intent intent) {
         // Handle case where user selected "Applications"
         String applicationName =null;// getResources().getString(R.string.group_applications);
         String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
 
         if (applicationName != null && applicationName.equals(shortcutName)) {
             Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
             mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
 
             Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
             pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
             //pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_application));
             startActivityForResultSafely(pickIntent, REQUEST_PICK_APPLICATION);
         } else {
             startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT);
         }
     }
 
     void addLiveFolder(Intent intent) {
         // Handle case where user selected "Folder"
         String folderName = null;//getResources().getString(R.string.group_folder);
         String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
 
         if (folderName != null && folderName.equals(shortcutName)) {
             //addFolder();
         } else {
             startActivityForResultSafely(intent, REQUEST_CREATE_LIVE_FOLDER);
         }
     }
 
 //    void addFolder() {
 //        UserFolderInfo folderInfo = new UserFolderInfo();
 //        folderInfo.title = getText(R.string.folder_name);
 //
 //        CellLayout.CellInfo cellInfo = mAddItemCellInfo;
 //        cellInfo.screen = mWorkspace.getCurrentScreen();
 //        if (!findSingleSlot(cellInfo)) return;
 //
 //        // Update the model
 //        LauncherModel.addItemToDatabase(this, folderInfo,
 //                LauncherSettings.Favorites.CONTAINER_DESKTOP,
 //                mWorkspace.getCurrentScreen(), cellInfo.cellX, cellInfo.cellY, false);
 //        sFolders.put(folderInfo.id, folderInfo);
 //
 //        // Create the view
 //        FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
 //                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), folderInfo);
 //        mWorkspace.addInCurrentScreen(newFolder,
 //                cellInfo.cellX, cellInfo.cellY, 1, 1, isWorkspaceLocked());
 //    }
 //
 //    void removeFolder(FolderInfo folder) {
 //        sFolders.remove(folder.id);
 //    }
 //
 //    private void completeAddLiveFolder(Intent data, CellLayout.CellInfo cellInfo) {
 //        cellInfo.screen = mWorkspace.getCurrentScreen();
 //        if (!findSingleSlot(cellInfo)) return;
 //
 //        final LiveFolderInfo info = addLiveFolder(this, data, cellInfo, false);
 //
 //        if (!mRestoring) {
 //            final View view = LiveFolderIcon.fromXml(R.layout.live_folder_icon, this,
 //                (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
 //            mWorkspace.addInCurrentScreen(view, cellInfo.cellX, cellInfo.cellY, 1, 1,
 //                    isWorkspaceLocked());
 //        }
 //    }
 //
 //    static LiveFolderInfo addLiveFolder(Context context, Intent data,
 //            CellLayout.CellInfo cellInfo, boolean notify) {
 //
 //        Intent baseIntent = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT);
 //        String name = data.getStringExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME);
 //
 //        Drawable icon = null;
 //        Intent.ShortcutIconResource iconResource = null;
 //
 //        Parcelable extra = data.getParcelableExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON);
 //        if (extra != null && extra instanceof Intent.ShortcutIconResource) {
 //            try {
 //                iconResource = (Intent.ShortcutIconResource) extra;
 //                final PackageManager packageManager = context.getPackageManager();
 //                Resources resources = packageManager.getResourcesForApplication(
 //                        iconResource.packageName);
 //                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
 //                icon = resources.getDrawable(id);
 //            } catch (Exception e) {
 //                Log.w(TAG, "Could not load live folder icon: " + extra);
 //            }
 //        }
 //
 //        if (icon == null) {
 //            icon = context.getResources().getDrawable(R.drawable.ic_launcher_folder);
 //        }
 //
 //        final LiveFolderInfo info = new LiveFolderInfo();
 //        info.icon = Utilities.createIconBitmap(icon, context);
 //        info.title = name;
 //        info.iconResource = iconResource;
 //        info.uri = data.getData();
 //        info.baseIntent = baseIntent;
 //        info.displayMode = data.getIntExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
 //                LiveFolders.DISPLAY_MODE_GRID);
 //
 //        LauncherModel.addItemToDatabase(context, info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
 //                cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);
 //        sFolders.put(info.id, info);
 //
 //        return info;
 //    }
 
     private boolean findSingleSlot(CellLayout.CellInfo cellInfo) {
         final int[] xy = new int[2];
         if (findSlot(cellInfo, xy, 1, 1)) {
             cellInfo.cellX = xy[0];
             cellInfo.cellY = xy[1];
             return true;
         }
         return false;
     }
 
     private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
         if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
             boolean[] occupied = mSavedState != null ?
                     mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
             cellInfo = mWorkspace.findAllVacantCells(occupied);
             if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                 Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
                 return false;
             }
         }
         return true;
     }
 
     private void showNotifications() {
         final StatusBarManager statusBar = (StatusBarManager) getSystemService(STATUS_BAR_SERVICE);
         if (statusBar != null) {
             statusBar.expand();
         }
     }
 
     private void startWallpaper() {
         closeAllApps(true);
         final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
         Intent chooser = Intent.createChooser(pickWallpaper,
                 getText(R.string.chooser_wallpaper));
         // NOTE: Adds a configure option to the chooser if the wallpaper supports it
         //       Removed in Eclair MR1
 //        WallpaperManager wm = (WallpaperManager)
 //                getSystemService(Context.WALLPAPER_SERVICE);
 //        WallpaperInfo wi = wm.getWallpaperInfo();
 //        if (wi != null && wi.getSettingsActivity() != null) {
 //            LabeledIntent li = new LabeledIntent(getPackageName(),
 //                    R.string.configure_wallpaper, 0);
 //            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
 //            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
 //        }
         startActivityForResult(chooser, REQUEST_PICK_WALLPAPER);
     }
 
     /**
      * Registers various content observers. The current implementation registers
      * only a favorites observer to keep track of the favorites applications.
      */
     private void registerContentObservers() {
         ContentResolver resolver = getContentResolver();
         resolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI,
                 true, mWidgetObserver);
     }
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         if (event.getAction() == KeyEvent.ACTION_DOWN) {
             switch (event.getKeyCode()) {
                 case KeyEvent.KEYCODE_HOME:
                     return true;
                 case KeyEvent.KEYCODE_VOLUME_DOWN:
                     if (SystemProperties.getInt("debug.launcher2.dumpstate", 0) != 0) {
                         dumpState();
                         return true;
                     }
                     break;
             }
         } else if (event.getAction() == KeyEvent.ACTION_UP) {
             switch (event.getKeyCode()) {
                 case KeyEvent.KEYCODE_HOME:
                     return true;
             }
         }
 
         return super.dispatchKeyEvent(event);
     }
 
     @Override
     public void onBackPressed() {
     	super.onBackPressed();
        // dismissPreview(mPreviousView);
        // dismissPreview(mNextView);
     }
 
     private void closeFolder() {
 //        Folder folder = mWorkspace.getOpenFolder();
 //        if (folder != null) {
 //            closeFolder(folder);
 //        }
     }
 
 //    void closeFolder(Folder folder) {
 //        folder.getInfo().opened = false;
 //        ViewGroup parent = (ViewGroup) folder.getParent();
 //        if (parent != null) {
 //            parent.removeView(folder);
 //            if (folder instanceof DropTarget) {
 //                // Live folders aren't DropTargets.
 //                mDragController.removeDropTarget((DropTarget)folder);
 //            }
 //        }
 //        folder.onClose();
 //    }
 
     /**
      * Re-listen when widgets are reset.
      */
     private void onAppWidgetReset() {
         mAppWidgetHost.startListening();
     }
 
     /**
      * Go through the and disconnect any of the callbacks in the drawables and the views or we
      * leak the previous Home screen on orientation change.
      */
     private void unbindDesktopItems() {
         for (ItemInfo item: mDesktopItems) {
             item.unbind();
         }
     }
 
     /**
      * Launches the intent referred by the clicked shortcut.
      *
      * @param v The view representing the clicked shortcut.
      */
     public void onClick(View v) {
         Object tag = v.getTag();
 //        if (tag instanceof ShortcutInfo) {
 //            // Open shortcut
 //            final Intent intent = ((ShortcutInfo) tag).intent;
 //            int[] pos = new int[2];
 //            v.getLocationOnScreen(pos);
 //            intent.setSourceBounds(new Rect(pos[0], pos[1],
 //                    pos[0] + v.getWidth(), pos[1] + v.getHeight()));
 //            startActivitySafely(intent, tag);
 //        } else if (tag instanceof FolderInfo) {
 //            handleFolderClick((FolderInfo) tag);
 //        } else if (v == mHandleView) {
 //            if (isAllAppsVisible()) {
 //                closeAllApps(true);
 //            } else {
 //                showAllApps(true);
 //            }
 //        }
     }
 
     void startActivitySafely(Intent intent, Object tag) {
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         try {
             startActivity(intent);
         } catch (ActivityNotFoundException e) {
             Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
             Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
         } catch (SecurityException e) {
             Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
             Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                     ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                     "or use the exported attribute for this activity. "
                     + "tag="+ tag + " intent=" + intent, e);
         }
     }
     
     void startActivityForResultSafely(Intent intent, int requestCode) {
         try {
             startActivityForResult(intent, requestCode);
         } catch (ActivityNotFoundException e) {
             Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
         } catch (SecurityException e) {
             Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
             Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                     ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                     "or use the exported attribute for this activity.", e);
         }
     }
 
 //    private void handleFolderClick(FolderInfo folderInfo) {
 //        if (!folderInfo.opened) {
 //            // Close any open folder
 //            closeFolder();
 //            // Open the requested folder
 //            openFolder(folderInfo);
 //        } else {
 //            // Find the open folder...
 //            Folder openFolder = mWorkspace.getFolderForTag(folderInfo);
 //            int folderScreen;
 //            if (openFolder != null) {
 //                folderScreen = mWorkspace.getScreenForView(openFolder);
 //                // .. and close it
 //                closeFolder(openFolder);
 //                if (folderScreen != mWorkspace.getCurrentScreen()) {
 //                    // Close any folder open on the current screen
 //                    closeFolder();
 //                    // Pull the folder onto this screen
 //                    openFolder(folderInfo);
 //                }
 //            }
 //        }
 //    }
 
     /**
      * Opens the user fodler described by the specified tag. The opening of the folder
      * is animated relative to the specified View. If the View is null, no animation
      * is played.
      *
      * @param folderInfo The FolderInfo describing the folder to open.
      */
 //    private void openFolder(FolderInfo folderInfo) {
 //        Folder openFolder;
 //
 //        if (folderInfo instanceof UserFolderInfo) {
 //            openFolder = UserFolder.fromXml(this);
 //        } else if (folderInfo instanceof LiveFolderInfo) {
 //            openFolder = com.android.launcher2.LiveFolder.fromXml(this, folderInfo);
 //        } else {
 //            return;
 //        }
 //
 //        openFolder.setDragController(mDragController);
 //        openFolder.setLauncher(this);
 //
 //        openFolder.bind(folderInfo);
 //        folderInfo.opened = true;
 //
 //        mWorkspace.addInScreen(openFolder, folderInfo.screen, 0, 0, 4, 4);
 //        openFolder.onOpen();
 //    }
 
     public boolean onLongClick(View v) {
     	
 			
 //        switch (v.getId()) {
 //            case R.id.previous_screen:
 //                if (!isAllAppsVisible()) {
 //                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
 //                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 //                    showPreviews(v);
 //                }
 //                return true;
 //            case R.id.next_screen:
 //                if (!isAllAppsVisible()) {
 //                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
 //                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 //                    showPreviews(v);
 //                }
 //                return true;
 //            case R.id.all_apps_button:
 //                if (!isAllAppsVisible()) {
 //                    mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
 //                            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
 //                    showPreviews(v);
 //                }
 //                return true;
 //        }
 
 //        if (isWorkspaceLocked()) {
 //            return false;
 //        }
 
         if (!(v instanceof CellLayout)) {
             v = (View) v.getParent();
         }
 
         CellLayout.CellInfo cellInfo = (CellLayout.CellInfo) v.getTag();
 
         // This happens when long clicking an item with the dpad/trackball
         if (cellInfo == null) {
             return true;
         }
         if(cellInfo.cell instanceof MyViewctrl )
         	cellInfo.type=Workspace.TYPE_VIEW;
         else
         	cellInfo.type=Workspace.TYPE_WIDGET;
         if (mWorkspace.allowLongPress()) {
             if (cellInfo.cell == null) {
                 if (cellInfo.valid) {
                     // User long pressed on empty space
                     mWorkspace.setAllowLongPress(false);
                     mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                             HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                     showAddDialog(cellInfo);
                 }
                 addWidget();
             } else {
 //                if (!(cellInfo.cell instanceof Folder)) {
 //                    // User long pressed on an item
                     mWorkspace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                             HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                     mWorkspace.startDrag(cellInfo);
 //                }
             }
         }
         
         return true;
     }
 
     @SuppressWarnings({"unchecked"})
     private void dismissPreview(final View v) {
         final PopupWindow window = (PopupWindow) v.getTag();
         if (window != null) {
             window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                 public void onDismiss() {
                     ViewGroup group = (ViewGroup) v.getTag(R.id.workspace);
                     int count = group.getChildCount();
                     for (int i = 0; i < count; i++) {
                         ((ImageView) group.getChildAt(i)).setImageDrawable(null);
                     }
                     //ArrayList<Bitmap> bitmaps = (ArrayList<Bitmap>) v.getTag(R.id.icon);
                  //   for (Bitmap bitmap : bitmaps) bitmap.recycle();
 
                     v.setTag(R.id.workspace, null);
                    // v.setTag(R.id.icon, null);
                     window.setOnDismissListener(null);
                 }
             });
             window.dismiss();
         }
         v.setTag(null);
     }
 
 //    private void showPreviews(View anchor) {
 //        showPreviews(anchor, 0, mWorkspace.getChildCount());
 //    }
 //
 //    private void showPreviews(final View anchor, int start, int end) {
 //        final Resources resources = getResources();
 //        final Workspace workspace = mWorkspace;
 //
 //        CellLayout cell = ((CellLayout) workspace.getChildAt(start));
 //        
 //        float max = workspace.getChildCount();
 //        
 //        final Rect r = new Rect();
 //        resources.getDrawable(R.drawable.preview_background).getPadding(r);
 //        int extraW = (int) ((r.left + r.right) * max);
 //        int extraH = r.top + r.bottom;
 //
 //        int aW = cell.getWidth() - extraW;
 //        float w = aW / max;
 //
 //        int width = cell.getWidth();
 //        int height = cell.getHeight();
 //        int x = cell.getLeftPadding();
 //        int y = cell.getTopPadding();
 //        width -= (x + cell.getRightPadding());
 //        height -= (y + cell.getBottomPadding());
 //
 //        float scale = w / width;
 //
 //        int count = end - start;
 //
 //        final float sWidth = width * scale;
 //        float sHeight = height * scale;
 //
 //        LinearLayout preview = new LinearLayout(this);
 //
 //        PreviewTouchHandler handler = new PreviewTouchHandler(anchor);
 //        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(count);
 //
 //        for (int i = start; i < end; i++) {
 //            ImageView image = new ImageView(this);
 //            cell = (CellLayout) workspace.getChildAt(i);
 //
 //            final Bitmap bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
 //                    Bitmap.Config.ARGB_8888);
 //
 //            final Canvas c = new Canvas(bitmap);
 //            c.scale(scale, scale);
 //            c.translate(-cell.getLeftPadding(), -cell.getTopPadding());
 //            cell.dispatchDraw(c);
 //
 //            image.setBackgroundDrawable(resources.getDrawable(R.drawable.preview_background));
 //            image.setImageBitmap(bitmap);
 //            image.setTag(i);
 //            image.setOnClickListener(handler);
 //            image.setOnFocusChangeListener(handler);
 //            image.setFocusable(true);
 //            if (i == mWorkspace.getCurrentScreen()) image.requestFocus();
 //
 //            preview.addView(image,
 //                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 //
 //            bitmaps.add(bitmap);            
 //        }
 //
 //        final PopupWindow p = new PopupWindow(this);
 //        p.setContentView(preview);
 //        p.setWidth((int) (sWidth * count + extraW));
 //        p.setHeight((int) (sHeight + extraH));
 //        p.setAnimationStyle(R.style.AnimationPreview);
 //        p.setOutsideTouchable(true);
 //        p.setFocusable(true);
 //        p.setBackgroundDrawable(new ColorDrawable(0));
 //        p.showAsDropDown(anchor, 0, 0);
 //
 //        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
 //            public void onDismiss() {
 //                dismissPreview(anchor);
 //            }
 //        });
 //
 //        anchor.setTag(p);
 //        anchor.setTag(R.id.workspace, preview);
 //      //  anchor.setTag(R.id.icon, bitmaps);        
 //    }
 
     class PreviewTouchHandler implements View.OnClickListener, Runnable, View.OnFocusChangeListener {
         private final View mAnchor;
 
         public PreviewTouchHandler(View anchor) {
             mAnchor = anchor;
         }
 
         public void onClick(View v) {
             mWorkspace.snapToScreen((Integer) v.getTag());
             v.post(this);
         }
 
         public void run() {
             dismissPreview(mAnchor);            
         }
 
         public void onFocusChange(View v, boolean hasFocus) {
             if (hasFocus) {
                 mWorkspace.snapToScreen((Integer) v.getTag());
             }
         }
     }
 
     Workspace getWorkspace() {
         return mWorkspace;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
 //        switch (id) {
 //            case DIALOG_CREATE_SHORTCUT:
 //                return new CreateShortcut().createDialog();
 //            case DIALOG_RENAME_FOLDER:
 //                return new RenameFolder().createDialog();
 //        }
 
         return super.onCreateDialog(id);
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
             case DIALOG_CREATE_SHORTCUT:
                 break;
             case DIALOG_RENAME_FOLDER:
            
                 break;
         }
     }
 
 //    void showRenameDialog(FolderInfo info) {
 //        mFolderInfo = info;
 //        mWaitingForResult = true;
 //        showDialog(DIALOG_RENAME_FOLDER);
 //    }
 
     private void showAddDialog(CellLayout.CellInfo cellInfo) {
         mAddItemCellInfo = cellInfo;
         mWaitingForResult = true;
         showDialog(DIALOG_CREATE_SHORTCUT);
     }
 
 //    private void pickShortcut() {
 //        Bundle bundle = new Bundle();
 //
 //        ArrayList<String> shortcutNames = new ArrayList<String>();
 //        shortcutNames.add(getString(R.string.group_applications));
 //        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
 //
 //        ArrayList<ShortcutIconResource> shortcutIcons = new ArrayList<ShortcutIconResource>();
 //        shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
 //                        R.drawable.ic_launcher_application));
 //        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
 //
 //        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 //        pickIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
 //        pickIntent.putExtra(Intent.EXTRA_TITLE, getText(R.string.title_select_shortcut));
 //        pickIntent.putExtras(bundle);
 //
 //        startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT);
 //    }
 
 //    private class RenameFolder {
 //        private EditText mInput;
 //
 //        Dialog createDialog() {
 //            final View layout = View.inflate(Launcher.this, R.layout.rename_folder, null);
 //            mInput = (EditText) layout.findViewById(R.id.folder_name);
 //
 //            AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
 //            builder.setIcon(0);
 //            builder.setTitle(getString(R.string.rename_folder_title));
 //            builder.setCancelable(true);
 //            builder.setOnCancelListener(new Dialog.OnCancelListener() {
 //                public void onCancel(DialogInterface dialog) {
 //                    cleanup();
 //                }
 //            });
 //            builder.setNegativeButton(getString(R.string.cancel_action),
 //                new Dialog.OnClickListener() {
 //                    public void onClick(DialogInterface dialog, int which) {
 //                        cleanup();
 //                    }
 //                }
 //            );
 //            builder.setPositiveButton(getString(R.string.rename_action),
 //                new Dialog.OnClickListener() {
 //                    public void onClick(DialogInterface dialog, int which) {
 //                        changeFolderName();
 //                    }
 //                }
 //            );
 //            builder.setView(layout);
 //
 //            final AlertDialog dialog = builder.create();
 //            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
 //                public void onShow(DialogInterface dialog) {
 //                    mWaitingForResult = true;
 //                    mInput.requestFocus();
 //                    InputMethodManager inputManager = (InputMethodManager)
 //                            getSystemService(Context.INPUT_METHOD_SERVICE);
 //                    inputManager.showSoftInput(mInput, 0);
 //                }
 //            });
 //
 //            return dialog;
 //        }
 
 //        private void changeFolderName() {
 //            final String name = mInput.getText().toString();
 //            if (!TextUtils.isEmpty(name)) {
 //                // Make sure we have the right folder info
 //                mFolderInfo = sFolders.get(mFolderInfo.id);
 //                mFolderInfo.title = name;
 //                LauncherModel.updateItemInDatabase(Launcher.this, mFolderInfo);
 //
 //                if (mWorkspaceLoading) {
 //                    lockAllApps();
 //                    mModel.startLoader(Launcher.this, false);
 //                } else {
 //                    final FolderIcon folderIcon = (FolderIcon)
 //                            mWorkspace.getViewForTag(mFolderInfo);
 //                    if (folderIcon != null) {
 //                        folderIcon.setText(name);
 //                        getWorkspace().requestLayout();
 //                    } else {
 //                        lockAllApps();
 //                        mWorkspaceLoading = true;
 //                        mModel.startLoader(Launcher.this, false);
 //                    }
 //                }
 //            }
 //            cleanup();
 //        }
 
 //        private void cleanup() {
 //            dismissDialog(DIALOG_RENAME_FOLDER);
 //            mWaitingForResult = false;
 //            mFolderInfo = null;
 //        }
 //    }
 
     // Now a part of LauncherModel.Callbacks. Used to reorder loading steps.
     public boolean isAllAppsVisible() {
     	return false;
        // return (mAllAppsGrid != null) ? mAllAppsGrid.isVisible() : false;
     }
 
     // AllAppsView.Watcher
     public void zoomed(float zoom) {
         if (zoom == 1.0f) {
             mWorkspace.setVisibility(View.GONE);
         }
     }
 
     void showAllApps(boolean animated) {
 //        mAllAppsGrid.zoom(1.0f, animated);
 //
 //        ((View) mAllAppsGrid).setFocusable(true);
 //        ((View) mAllAppsGrid).requestFocus();
 //        
 //        // TODO: fade these two too
 //        mDeleteZone.setVisibility(View.GONE);
     }
 
     /**
      * Things to test when changing this code.
      *   - Home from workspace
      *          - from center screen
      *          - from other screens
      *   - Home from all apps
      *          - from center screen
      *          - from other screens
      *   - Back from all apps
      *          - from center screen
      *          - from other screens
      *   - Launch app from workspace and quit
      *          - with back
      *          - with home
      *   - Launch app from all apps and quit
      *          - with back
      *          - with home
      *   - Go to a screen that's not the default, then all
      *     apps, and launch and app, and go back
      *          - with back
      *          -with home
      *   - On workspace, long press power and go back
      *          - with back
      *          - with home
      *   - On all apps, long press power and go back
      *          - with back
      *          - with home
      *   - On workspace, power off
      *   - On all apps, power off
      *   - Launch an app and turn off the screen while in that app
      *          - Go back with home key
      *          - Go back with back key  TODO: make this not go to workspace
      *          - From all apps
      *          - From workspace
      *   - Enter and exit car mode (becuase it causes an extra configuration changed)
      *          - From all apps
      *          - From the center workspace
      *          - From another workspace
      */
     void closeAllApps(boolean animated) {
 //        if (mAllAppsGrid.isVisible()) {
 //            mWorkspace.setVisibility(View.VISIBLE);
 //            mAllAppsGrid.zoom(0.0f, animated);
 //            ((View)mAllAppsGrid).setFocusable(false);
 //            mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
 //        }
     }
 
     void lockAllApps() {
         // TODO
     }
 
     void unlockAllApps() {
         // TODO
     }
 
     /**
      * Displays the shortcut creation dialog and launches, if necessary, the
      * appropriate activity.
      */
     private class CreateShortcut implements DialogInterface.OnClickListener,
             DialogInterface.OnCancelListener, DialogInterface.OnDismissListener,
             DialogInterface.OnShowListener {
 
 //        private AddAdapter mAdapter;
 //
 //        Dialog createDialog() {
 //            mAdapter = new AddAdapter(Launcher.this);
 //
 //            final AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
 //            builder.setTitle(getString(R.string.menu_item_add_item));
 //            builder.setAdapter(mAdapter, this);
 //
 //            builder.setInverseBackgroundForced(true);
 //
 //            AlertDialog dialog = builder.create();
 //            dialog.setOnCancelListener(this);
 //            dialog.setOnDismissListener(this);
 //            dialog.setOnShowListener(this);
 //
 //            return dialog;
 //        }
 
         public void onCancel(DialogInterface dialog) {
             mWaitingForResult = false;
             cleanup();
         }
 
         public void onDismiss(DialogInterface dialog) {
         }
 
         private void cleanup() {
             try {
                 dismissDialog(DIALOG_CREATE_SHORTCUT);
             } catch (Exception e) {
                 // An exception is thrown if the dialog is not visible, which is fine
             }
         }
 
         /**
          * Handle the action clicked in the "Add to home" dialog.
          */
         public void onClick(DialogInterface dialog, int which) {
             Resources res = getResources();
             cleanup();
 
 //            switch (which) {
 //                case AddAdapter.ITEM_SHORTCUT: {
 //                    // Insert extra item to handle picking application
 //                    pickShortcut();
 //                    break;
 //                }
 //
 //                case AddAdapter.ITEM_APPWIDGET: {
 //                    int appWidgetId = Launcher.this.mAppWidgetHost.allocateAppWidgetId();
 //
 //                    Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
 //                    pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 //                    // start the pick activity
 //                    startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
 //                    break;
 //                }
 //
 //                case AddAdapter.ITEM_LIVE_FOLDER: {
 //                    // Insert extra item to handle inserting folder
 //                    Bundle bundle = new Bundle();
 //
 //                    ArrayList<String> shortcutNames = new ArrayList<String>();
 //                    shortcutNames.add(res.getString(R.string.group_folder));
 //                    bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames);
 //
 //                    ArrayList<ShortcutIconResource> shortcutIcons =
 //                            new ArrayList<ShortcutIconResource>();
 //                    shortcutIcons.add(ShortcutIconResource.fromContext(Launcher.this,
 //                            R.drawable.ic_launcher_folder));
 //                    bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons);
 //
 //                    Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
 //                    pickIntent.putExtra(Intent.EXTRA_INTENT,
 //                            new Intent(LiveFolders.ACTION_CREATE_LIVE_FOLDER));
 //                    pickIntent.putExtra(Intent.EXTRA_TITLE,
 //                            getText(R.string.title_select_live_folder));
 //                    pickIntent.putExtras(bundle);
 //
 //                    startActivityForResult(pickIntent, REQUEST_PICK_LIVE_FOLDER);
 //                    break;
 //                }
 //
 //                case AddAdapter.ITEM_WALLPAPER: {
 //                    startWallpaper();
 //                    break;
 //                }
 //            }
         }
 
         public void onShow(DialogInterface dialog) {
             mWaitingForResult = true;            
         }
     }
 
     /**
      * Receives notifications when applications are added/removed.
      */
     private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             closeSystemDialogs();
             String reason = intent.getStringExtra("reason");
             if (!"homekey".equals(reason)) {
                 boolean animate = true;
                 if (mPaused || "lock".equals(reason)) {
                     animate = false;
                 }
                 closeAllApps(animate);
             }
         }
     }
 
     /**
      * Receives notifications whenever the appwidgets are reset.
      */
     private class AppWidgetResetObserver extends ContentObserver {
         public AppWidgetResetObserver() {
             super(new Handler());
         }
 
         @Override
         public void onChange(boolean selfChange) {
             onAppWidgetReset();
         }
     }
 
     /**
      * If the activity is currently paused, signal that we need to re-run the loader
      * in onResume.
      *
      * This needs to be called from incoming places where resources might have been loaded
      * while we are paused.  That is becaues the Configuration might be wrong
      * when we're not running, and if it comes back to what it was when we
      * were paused, we are not restarted.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      *
      * @return true if we are currently paused.  The caller might be able to
      * skip some work in that case since we will come back again.
      */
     public boolean setLoadOnResume() {
         if (mPaused) {
             Log.i(TAG, "setLoadOnResume");
             mOnResumeNeedsLoad = true;
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Implementation of the method from LauncherModel.Callbacks.
      */
     public int getCurrentWorkspaceScreen() {
         if (mWorkspace != null) {
             return mWorkspace.getCurrentScreen();
         } else {
             return SCREEN_COUNT / 2;
         }
     }
 
     /**
      * Refreshes the shortcuts shown on the workspace.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
     public void startBinding() {
         final Workspace workspace = mWorkspace;
         int count = workspace.getChildCount();
         for (int i = 0; i < count; i++) {
             // Use removeAllViewsInLayout() to avoid an extra requestLayout() and invalidate().
             ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();
         }
 
         if (DEBUG_USER_INTERFACE) {
             android.widget.Button finishButton = new android.widget.Button(this);
             finishButton.setText("Finish");
             workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);
 
             finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
                 public void onClick(View v) {
                     finish();
                 }
             });
         }
     }
 
     /**
      * Bind the items start-end from the list.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
     public void bindItems(ArrayList<ItemInfo> shortcuts, int start, int end) {
 
         setLoadOnResume();
 
         final Workspace workspace = mWorkspace;
 
         for (int i=start; i<end; i++) {
             final ItemInfo item = shortcuts.get(i);
             mDesktopItems.add(item);
             switch (item.itemType) {
                 case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                 case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
 //                    final View shortcut = createShortcut((ShortcutInfo)item);
 //                    workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
 //                            false);
                     break;
                 case LauncherSettings.Favorites.ITEM_TYPE_OWNVIEW:
 //                    final FolderIcon newFolder = FolderIcon.fromXml(R.layout.folder_icon, this,
 //                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
 //                            (UserFolderInfo) item);
 //                    workspace.addInScreen(newFolder, item.screen, item.cellX, item.cellY, 1, 1,
 //                            false);
                     break;
                 case LauncherSettings.Favorites.ITEM_TYPE_OWNEIDGET:
                 	if(item.id==0)
                 	{
 	                	final View NewView = createNewsView(item);
 	                	workspace.addInScreen(NewView, item.screen, item.cellX, item.cellY, item.spanX, item.spanY,
 	                              false);
                 	}
                 	
                 	else if(item.id==1)
                 	{
                 		
 //                		final FrameLayout destop =(FrameLayout)findViewById(R.id.destop);
 //	                	final View shortcut =(View)findViewById(R.id.tvliveview);// tvliveview
 //	                	destop.removeView(shortcut);
                 		final View shortcut = createVideoView(item);
 	                	workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, item.spanX, item.spanY,
 	                              false);
                 	}
                 	else if(item.id==2)
                 	{
 	                	final View TvView = createTvView(item);
 	                	workspace.addInScreen(TvView, item.screen, item.cellX, item.cellY, item.spanX, item.spanY,
 	                              false);
                 	}
                 	else if(item.id==3)
                 	{
 	                	final View TvView = createImView(item);
 	                	workspace.addInScreen(TvView, item.screen, item.cellX, item.cellY, item.spanX, item.spanY,
 	                              false);
                 	}else if(item.id==4)
                 	{
 	                	final View pushContentView = createPushContentView(item);
 	                	workspace.addInScreen(pushContentView, item.screen, item.cellX, item.cellY, item.spanX, item.spanY,
 	                              false);
                 	}
                 	//Button bt=preview.findViewById(R.id.)
                 //	mDesktopItems.add(item);
 //                    VODVideoFragment newFragment=new VODVideoFragment();
 //                	//vedio
 //                	//int id=preview.getId();
 //                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 //                    //ft.replace(R.id.vodvideo_bk, newFragment);
 //                    ft.add(R.id.vodvideo_bk,newFragment, "video");
 //                  //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 //                    ft.commit();
 //                    
                   
                 	//if(newFragment.getView()!=null)
                 	//preview.addView(newFragment.getView(),p);
 //                    final FolderIcon newLiveFolder = LiveFolderIcon.fromXml(
 //                            R.layout.live_folder_icon, this,
 //                            (ViewGroup) workspace.getChildAt(workspace.getCurrentScreen()),
 //                            (LiveFolderInfo) item);
 
                     break;
             }
         }
 
         workspace.requestLayout();
     }
     View createTvView(ItemInfo info) {
         return createOwnView(R.layout.tv,
                 (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
     }
     View createImView(ItemInfo info) {
         return createOwnView(R.layout.im,
                 (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
     }
     View createPushContentView(ItemInfo info) {
         return createOwnView(R.layout.pushcontent,
                 (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
     }    
     View createVideoView(ItemInfo info) {
         return createOwnView(R.layout.video,
                 (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
 
     }
     View createNewsView(ItemInfo info) {
         return createOwnView(R.layout.news,
                 (ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
     }
 
     View createOwnView(int layoutResId, ViewGroup parent, ItemInfo info) {
     	
     	LinearLayout favorite = (LinearLayout) mInflater.inflate(layoutResId, parent, false);
     	favorite.setTag(info);
 //        VODVideoFragment newFragment=new VODVideoFragment();
 //    	//vedio
 //    	//int id=preview.getId();
 //        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 //        //ft.replace(R.id.vodvideo_bk, newFragment);
 //        ft.add(R.id.video,newFragment, "video");
 //      //  ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 //        ft.commit();
         return favorite;
     }
     /**
      * Implementation of the method from LauncherModel.Callbacks.
      */
 //    public void bindFolders(HashMap<Long, FolderInfo> folders) {
 //        setLoadOnResume();
 //        sFolders.clear();
 //        sFolders.putAll(folders);
 //    }
 
     /**
      * Add the views for a widget to the workspace.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
     public void bindAppWidget(LauncherAppWidgetInfo item) {
         setLoadOnResume();
 
         final long start = DEBUG_WIDGETS ? SystemClock.uptimeMillis() : 0;
         if (DEBUG_WIDGETS) {
             Log.d(TAG, "bindAppWidget: " + item);
         }
         final Workspace workspace = mWorkspace;
 
         final int appWidgetId = item.appWidgetId;
         final AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
         if (DEBUG_WIDGETS) {
             Log.d(TAG, "bindAppWidget: id=" + item.appWidgetId + " belongs to component " + appWidgetInfo.provider);
         }
 
         item.hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
 
         item.hostView.setAppWidget(appWidgetId, appWidgetInfo);
         item.hostView.setTag(item);
 
         workspace.addInScreen(item.hostView, item.screen, item.cellX,
                 item.cellY, item.spanX, item.spanY, false);
 
         workspace.requestLayout();
 
         mDesktopItems.add(item);
 
         if (DEBUG_WIDGETS) {
             Log.d(TAG, "bound widget id="+item.appWidgetId+" in "
                     + (SystemClock.uptimeMillis()-start) + "ms");
         }
     }
 
     /**
      * Callback saying that there aren't any more items to bind.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
     public void finishBindingItems() {
         setLoadOnResume();
 
         if (mSavedState != null) {
             if (!mWorkspace.hasFocus()) {
                 mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
             }
 
 //            final long[] userFolders = mSavedState.getLongArray(RUNTIME_STATE_USER_FOLDERS);
 //            if (userFolders != null) {
 //                for (long folderId : userFolders) {
 //                    final FolderInfo info = sFolders.get(folderId);
 //                    if (info != null) {
 //                        openFolder(info);
 //                    }
 //                }
 //                final Folder openFolder = mWorkspace.getOpenFolder();
 //                if (openFolder != null) {
 //                    openFolder.requestFocus();
 //                }
 //            }
 
             mSavedState = null;
         }
 
         if (mSavedInstanceState != null) {
             super.onRestoreInstanceState(mSavedInstanceState);
             mSavedInstanceState = null;
         }
 
         mWorkspaceLoading = false;
     }
 
     /**
      * Add the icons for all apps.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
 //    public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
 //        mAllAppsGrid.setApps(apps);
 //    }
 
     /**
      * A package was installed.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
 //    public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
 //        setLoadOnResume();
 //        removeDialog(DIALOG_CREATE_SHORTCUT);
 //        mAllAppsGrid.addApps(apps);
 //    }
 
     /**
      * A package was updated.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
 //    public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {
 //        setLoadOnResume();
 //        removeDialog(DIALOG_CREATE_SHORTCUT);
 //        mWorkspace.updateShortcuts(apps);
 //        mAllAppsGrid.updateApps(apps);
 //    }
 
     /**
      * A package was uninstalled.
      *
      * Implementation of the method from LauncherModel.Callbacks.
      */
 //    public void bindAppsRemoved(ArrayList<ApplicationInfo> apps, boolean permanent) {
 //        removeDialog(DIALOG_CREATE_SHORTCUT);
 //        if (permanent) {
 //            mWorkspace.removeItems(apps);
 //        }
 //        mAllAppsGrid.removeApps(apps);
 //    }
 
     /**
      * Prints out out state for debugging.
      */
     public void dumpState() {
         Log.d(TAG, "BEGIN launcher2 dump state for launcher " + this);
         Log.d(TAG, "mSavedState=" + mSavedState);
         Log.d(TAG, "mWorkspaceLoading=" + mWorkspaceLoading);
         Log.d(TAG, "mRestoring=" + mRestoring);
         Log.d(TAG, "mWaitingForResult=" + mWaitingForResult);
         Log.d(TAG, "mSavedInstanceState=" + mSavedInstanceState);
         Log.d(TAG, "mDesktopItems.size=" + mDesktopItems.size());
 //        Log.d(TAG, "sFolders.size=" + sFolders.size());
 //        mModel.dumpState();
 //        mAllAppsGrid.dumpState();
         Log.d(TAG, "END launcher2 dump state");
     }
 }
