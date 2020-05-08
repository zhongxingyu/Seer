 package main.activities;
 
 
 import java.io.File;
 import java.text.DecimalFormat;
 
 import test.bluetooth.BTInitHandler;
 import test.bluetooth.BTRunTask;
 import test.bluetooth.Bluetooth;
 import test.camera.CameraInitHandler;
 import test.camera.CameraRecorder;
 import test.camera.CameraRunHandler;
 import test.data.BracDataHandler;
 import test.file.BracValueFileHandler;
 import test.file.ImageFileHandler;
 import test.gps.GPSInitTask;
 import test.gps.GPSRunTask;
 import test.ui.UIMsgBox;
 import test.ui.UIRotate;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.RotateAnimation;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Point;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 
 public class TestFragment extends Fragment {
 
 	private Activity context;
 	private TestFragment testFragment;
 	private View view;
 	private TextView messageView;
 	private String timestamp;
 	
 	private final boolean[] INIT_PROGRESS={false,false,false};
 	private final boolean[] DONE_PROGRESS={false,false,false};
 	
 	public static final int _GPS=0;
 	public static final int _BT = 1;
 	public static final int _CAMERA = 2;
 	
 	private boolean keepMsgBox;
 	
 	//GPS
 	private LocationManager locationManager;
 	private GPSInitTask gpsInitTask;
 	private GPSRunTask gpsRunTask;
 	private boolean gps_state = false;
 	
 	//Bluetooth
 	private Bluetooth bt;
 	private BTInitHandler btInitHandler;
 	private BTRunTask btRunTask;
 	
 	//Camera
 	private CameraInitHandler cameraInitHandler;
 	private CameraRecorder cameraRecorder;
 	private CameraRunHandler cameraRunHandler;
 	
 	//File
 	private File mainDirectory;
 	private BracValueFileHandler bracFileHandler;
 	private ImageFileHandler imgFileHandler;
 	
 	//Uploader
 	private BracDataHandler BDH;
 	
 	private RelativeLayout main_layout;
 	private UIMsgBox msgBox;
 	private UIRotate rotate;
 
 	private LoadingHandler loadingHandler;
 	private FailBgHandler failBgHandler;
 	private MsgLoadingHandler msgLoadingHandler;
 	private TestHandler testHandler;
 	private TimeUpHandler timeUpHandler;
 	private ChangeTabsHandler changeTabsHandler;
 	private ImageView sensor_button;
 	private ImageView[] sensor_lights;
 	private Animation blink_anim;
 	
 	private ImageView bg, startLine, startCircle;
 	private Bitmap bgBmp, startLineBmp, startCircleBmp;
 	private TextView startText;
 	/*
 	private ImageView animation;
 	private Bitmap[] animationBmp;
 	private AnimationDrawable animationDrawable;
 	*/
 	private FrameLayout preview_layout;
 	
 	private static Object init_lock  = new Object();
 	private static Object done_lock  = new Object();
 	
 	private TextView failHelp;
 	
 	
 	private EditText debugMsg;
 	private ChangeMsgHandler msgHandler;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		keepMsgBox = false;
 	}
 	
 	public void onPause(){
 		if (!isKeepMsgBox()){
 			Log.d("onpause","do");
 			stop();
 			clear();
 		}
 		else{
 			Log.d("onpause","skip");
 		}
 		if (blink_anim!=null)
 			blink_anim.cancel();
 		if (sensor_lights!=null){
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.INVISIBLE);
 		}
 		super.onPause();
 	}
 	
 	public void onResume(){
 		super.onResume();
 		Log.d("test","onresume");
 		if (!isKeepMsgBox()){
 			LoadingBox.show(this.getActivity());
 			context = this.getActivity();
 			testFragment = this;
 			setting();
 			loadingHandler.sendEmptyMessage(0);
 		}
 		else{
 			setKeepMsgBox(false);
			runGPS();
 		}
 		
 		Log.d("test","onresume end");
 	}
 	
 	private void setting(){
 		bg = (ImageView) view.findViewById(R.id.test_background);
 		startLine = (ImageView) view.findViewById(R.id.test_background_line);
 		startCircle = (ImageView) view.findViewById(R.id.test_start_button);
 		startText =(TextView) view.findViewById(R.id.test_start_text);
 		Point screen = FragmentTabs.getSize();
 		startText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(screen.x * 74.0/720.0));
 		//animation = (ImageView) view.findViewById(R.id.test_animation);
 		//animation.setVisibility(View.INVISIBLE);
 		main_layout = (RelativeLayout) view.findViewById(R.id.test_fragment_main_layout);
 		failHelp = (TextView) view.findViewById(R.id.test_fail_help);
 		startText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(screen.x * 49.0/720.0));
 		messageView = (TextView) view.findViewById(R.id.test_message);
 		messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(screen.x * 42.0/720.0));
 		RelativeLayout.LayoutParams mParam = (LayoutParams) messageView.getLayoutParams();
 		mParam.topMargin = (int)(screen.x * 36.0/720.0);
 		if (msgBox==null)
 			msgBox = new UIMsgBox(testFragment,main_layout);
 		rotate = new UIRotate(testFragment,main_layout);
 		preview_layout = (FrameLayout) view.findViewById(R.id.test_camera_preview_layout);
 		
 		sensor_button = (ImageView) view.findViewById(R.id.test_sensor_button);
 		RelativeLayout.LayoutParams sParam = (LayoutParams) sensor_button.getLayoutParams();
 		sParam.topMargin = (int)(screen.x * 800.0/720.0);
 		sParam.leftMargin = (int)(screen.x * 620.0/720.0);
 		sParam.width =  (int)(screen.x * 18.0/720.0);
 		sParam.height=  (int)(screen.x * 36.0/720.0);
 		
 		sensor_lights = new ImageView[2];
 		sensor_lights[0] = (ImageView) view.findViewById(R.id.test_sensor_light_red);
 		sensor_lights[1] = (ImageView) view.findViewById(R.id.test_sensor_light_yellow);
 
 		debugMsg = (EditText) view.findViewById(R.id.debug_msg);
 		
 		
 		RelativeLayout.LayoutParams l_redParam = (LayoutParams) sensor_lights[0].getLayoutParams();
 		l_redParam.topMargin = (int)(screen.x * 470.0/720.0);
 		l_redParam.leftMargin = (int)(screen.x * 290.0/720.0);
 		l_redParam.width =  (int)(screen.x * 32.0/720.0);
 		l_redParam.height=  (int)(screen.x * 32.0/720.0);
 		
 		RelativeLayout.LayoutParams l_yellowParam = (LayoutParams) sensor_lights[1].getLayoutParams();
 		l_yellowParam.topMargin = (int)(screen.x * 470.0/720.0);
 		l_yellowParam.leftMargin = (int)(screen.x * 346.0/720.0);
 		l_yellowParam.width =  (int)(screen.x * 32.0/720.0);
 		l_yellowParam.height=  (int)(screen.x * 32.0/720.0);
 		
 		
 		blink_anim = new AlphaAnimation(1.0f,0.1f);
 		blink_anim.setRepeatCount(1000);
 		blink_anim.setRepeatMode(Animation.REVERSE);
 		blink_anim.setDuration(100);
 		
 		loadingHandler = new LoadingHandler();
 		msgLoadingHandler = new MsgLoadingHandler();
 		failBgHandler = new FailBgHandler();
 		testHandler = new TestHandler();
 		timeUpHandler = new TimeUpHandler();
 		changeTabsHandler = new ChangeTabsHandler(); 
 	}
 	
 	public void reset(){
 		checkDebug();
 		timestamp = setTimeStamp();
 		setStorage();
 		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		cameraRecorder = new CameraRecorder(testFragment,imgFileHandler);
 		cameraRunHandler = new CameraRunHandler(cameraRecorder);
 		bt = new Bluetooth(testFragment,cameraRunHandler,bracFileHandler);
 		for (int i=0;i<3;++i)
 			INIT_PROGRESS[i]=DONE_PROGRESS[i]=false;
 	}
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	view = inflater.inflate(R.layout.new_test_fragment, container,false);
     	return view;
     }
 	
 	public void startGPS(boolean enable){
 		msgBox.generateInitializingBox();
 		if (enable){
 			Log.d("GPS","ENABLE GPS");
 			gps_state = true;
 			Object[] gps_enable={gps_state};
 			gpsInitTask = new GPSInitTask(testFragment,locationManager);
 			gpsInitTask.execute(gps_enable);
 		}
 		else{
 			gps_state = false;
 			runGPS();
 		}
 	}
 	
 	public void runGPS(){
 		if (gps_state){
 			gpsInitTask.cancel(true);
 			gpsRunTask = new GPSRunTask(this,locationManager,mainDirectory);
 			Object[] a = {gps_state};
 			gpsRunTask.execute(a);
 		}
 		else{
 			updateDoneState(_GPS);
 		}
 	}
 	
 	public void startBT(){
 		messageView.setTextColor(0xFFFFFFFF);
 		messageView.setText("請稍候");
 		//initialize bt task
 		btInitHandler = new BTInitHandler(testFragment,bt);
 		btInitHandler.sendEmptyMessage(0);
 		Log.d("INIT","BT TASK STARTED");
 		
 		//initialize camera task
 		cameraInitHandler = new CameraInitHandler(testFragment,cameraRecorder);
 		cameraInitHandler.sendEmptyMessage(0);
 		Log.d("INIT","Camera TASK STARTED");
 	}
 	
 	public void runBT(){
 		if (testHandler==null)
 			testHandler = new TestHandler();
 		
 			testHandler.sendEmptyMessage(0);
 
 	}
 	
 	public void failBT(){
 		messageView.setText("");
 		cleanMsgBox();
 		Message msg = new Message();
 		Bundle data = new Bundle();
 		data.putString("msg","未啟用酒測裝置");
 		msg.setData(data);
 		msg.what = 0;
 		if (failBgHandler!=null)
 			failBgHandler.sendMessage(msg);
 	}
 	
 	private class StartOnClickListener implements View.OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			messageView.setText("");
 			startLine.setVisibility(View.INVISIBLE);
 			startCircle.setVisibility(View.INVISIBLE);
 			startText.setVisibility(View.INVISIBLE);
 			reset();
 			SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(testFragment.getActivity());
 			boolean firstTime = sp.getBoolean("first", true);
 			if (firstTime){
 				SharedPreferences.Editor editor = sp.edit();
 				editor.putBoolean("first", false);
 				editor.commit();
 			}
 				messageView.setText("請按酒測裝置黑色按鈕\n以啟用酒測裝置");
 				messageView.setTextColor(0xFFFFFFFF);
 				sensor_button.setBackgroundColor(0xFF00CCAA);
 				sensor_lights[0].setVisibility(View.VISIBLE);
 				sensor_lights[1].setVisibility(View.INVISIBLE);
 				Thread t = new Thread(new TimeUpRunnable(0,1500));
 				t.start();
 			
 		}
 	}
 	
 	private class EndTestOnClickListener implements View.OnClickListener{
 		@Override
 		public void onClick(View v) {
 			failHelp.setVisibility(View.INVISIBLE);
 			stopDueToInit();
 			if (loadingHandler!=null)
 				loadingHandler.sendEmptyMessage(0);
 			blink_anim.cancel();
 			sensor_lights[1].setAnimation(null);
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.INVISIBLE);
 			
 		}
 	}
 	
 	private String setTimeStamp(){
 		long time_in_sec = System.currentTimeMillis() / 1000L;
 		DecimalFormat d = new DecimalFormat("0");
 		return d.format(time_in_sec);
 	}
 	
 	private void setStorage(){
 		String state = Environment.getExternalStorageState();
 		File dir = null;
 		if (state.equals(Environment.MEDIA_MOUNTED))
 			dir = new File(Environment.getExternalStorageDirectory(),"drunk_detection");
 		else
 			dir = new File(this.getActivity().getFilesDir(),"drunk_detection");
 		if (!dir.exists())
 			if (!dir.mkdirs())
 				Log.d("TEST_STORAGE","FAIL TO CREATE DIR");
 		
 		mainDirectory = new File(dir,timestamp);
 		if (!mainDirectory.exists())
 			if (!mainDirectory.mkdirs()){
 				return;
 			}
 		
 		bracFileHandler = new BracValueFileHandler(mainDirectory,timestamp);
 		imgFileHandler = new ImageFileHandler(mainDirectory,timestamp);
 		
 	}
 	
 	public void updateInitState(int type){
 		synchronized(init_lock){
 			if (INIT_PROGRESS[type]==true)
 				return;
 			INIT_PROGRESS[type]=true;
 			if (INIT_PROGRESS[_BT] && INIT_PROGRESS[_CAMERA]){
 				btInitHandler.removeMessages(0);
 				cameraInitHandler.removeMessages(0);
 				btRunTask = new BTRunTask(this,bt);
 				btRunTask.execute();
 				messageView.setText("已啟用酒測裝置");
 				sensor_button.setBackgroundColor(0xFF000000);
 				showDebug("Device launched");
 				Thread t = new Thread(new TimeUpRunnable(1,1500));
 				t.start();
 			}
 		}
 	}
 	
 	public void updateDoneState(int type){
 		synchronized(done_lock){
 			if (DONE_PROGRESS[type]==true)
 				return;
 			DONE_PROGRESS[type]=true;
 			
 			if (!DONE_PROGRESS[_GPS]&&DONE_PROGRESS[_BT]&&DONE_PROGRESS[_CAMERA]){
 				stop();
 					if (msgBox == null)
 						msgBox = new UIMsgBox(testFragment,main_layout);
 					if (msgLoadingHandler == null)
 						msgLoadingHandler = new MsgLoadingHandler();
 					msgLoadingHandler.sendEmptyMessage(0);
 				}
 			}
 			if (DONE_PROGRESS[_GPS]&&DONE_PROGRESS[_BT]&&DONE_PROGRESS[_CAMERA]){
 				BDH = new BracDataHandler(timestamp, testFragment);
 				int bdh_result = BDH.start();
 				if (bdh_result != BracDataHandler.ERROR){
 					double result = BDH.getResult();
 					Log.d("TEST RESULT",String.valueOf(result));
 				changeTabsHandler.sendEmptyMessage(0);
 			}
 		}
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data){
		/*if (requestCode==_GPS){
 			runGPS();
		}*/
 	}
 	
 	public void stopDueToInit(){
 		if (cameraRecorder!=null)
 			cameraRecorder.close();
 		
 		if (bt!=null)
 			bt.close();
 		
 		if (gpsInitTask!=null)
 			gpsInitTask.cancel(true);
 		
 		if (btInitHandler!=null)
 			btInitHandler.removeMessages(0);
 		
 		if (cameraInitHandler!=null)
 			cameraInitHandler.removeMessages(0);
 		
 		if (btRunTask!=null)
 			btRunTask.cancel(true);
 
 		if (gpsRunTask!=null){
 			gpsRunTask.cancel(true);
 		}
 	}
 	
 	public void stop(){
 		Log.d("test","stop");
 		
 		if (cameraRecorder!=null)
 			cameraRecorder.close();
 		
 		if (bt!=null)
 			bt.close();
 		
 		if (gpsInitTask!=null)
 			gpsInitTask.cancel(true);
 		
 		if (btInitHandler!=null)
 			btInitHandler.removeMessages(0);
 		
 		if (cameraInitHandler!=null)
 			cameraInitHandler.removeMessages(0);
 		
 		if (btRunTask!=null)
 			btRunTask.cancel(true);
 
 		if (gpsRunTask!=null){
 			gpsRunTask.cancel(true);
 		}
 		
 		if (loadingHandler!=null){
 			loadingHandler.removeMessages(0);
 			loadingHandler = null;
 		}
 		if (msgLoadingHandler !=null){
 			msgLoadingHandler.removeMessages(0);
 			msgLoadingHandler = null;
 		}
 		if (testHandler!=null){
 			testHandler.removeMessages(0);
 			testHandler = null;
 		}
 		if (failBgHandler!=null){
 			failBgHandler.removeMessages(0);
 			failBgHandler = null;
 		}
 		
 		if (msgHandler!=null){
 			msgHandler.removeMessages(0);
 			msgHandler = null;
 		}
 	}
 	
 	private void clear(){
 		Log.d("test","clear");
 		cleanMsgBox();
 		cleanRotate();
 	}
 	
     private void cleanMsgBox(){
     	if (msgBox!=null){
     		msgBox.clear();
     		msgBox = null;
     	}
     }
 	
     private void cleanRotate(){
     	if (rotate!=null){
     		rotate.clear();
     		rotate = null;
     	}
     }
     
 	@SuppressLint("HandlerLeak")
 	private class LoadingHandler extends Handler{
 		
 		private Resources r;
 		public void handleMessage(Message msg){
 			
 			Log.d("test","load handler");
 			
 			r = getResources();
     		bg.setImageBitmap(null);
     		
     		failHelp.setVisibility(View.INVISIBLE);
     		
     		Point screen = FragmentTabs.getSize();
 			
 			if (bgBmp!=null && !bgBmp.isRecycled()){
 				bgBmp.recycle();
 				bgBmp = null;
 			}
 			
 			
 			Bitmap tmp = BitmapFactory.decodeResource(r, R.drawable.test_start_bg_2);
 			bgBmp = Bitmap.createScaledBitmap(tmp, screen.x, screen.y, true);
 			tmp.recycle();
 			String bgBmp_size = bgBmp.getWidth() + "/"+bgBmp.getHeight();
 			Log.d("bgBMP",bgBmp_size);
 			
 			
 			
 			if (startLineBmp==null ||startLineBmp.isRecycled()){
 				tmp = BitmapFactory.decodeResource(r, R.drawable.test_start_line);
 				startLineBmp = Bitmap.createScaledBitmap(tmp, screen.x, (int)(screen.x * 146.0/720.0), true);
 				tmp.recycle();
 			}
 			RelativeLayout.LayoutParams startLineParam = (LayoutParams) startLine.getLayoutParams();
 			startLineParam.height = (int)(screen.x * 146.0/720.0);
 			startLineParam.topMargin = (int)(screen.x * 223.0/720.0);
 			
 			if (startCircleBmp==null ||startCircleBmp.isRecycled()){
 				tmp = BitmapFactory.decodeResource(r, R.drawable.test_start_start);
 				startCircleBmp = Bitmap.createScaledBitmap(tmp, (int)(screen.x * 358.0/720.0), (int)(screen.x * 356.0/720.0), true);
 				tmp.recycle();
 			}
 			RelativeLayout.LayoutParams startCircleParam = (LayoutParams) startCircle.getLayoutParams();
 			startCircleParam.width = (int)(screen.x * 358.0/720.0);
 			startCircleParam.height = (int)(screen.x * 356.0/720.0);
 			startCircleParam.leftMargin = (int)(screen.x * 357.0/720.0);
 			startCircleParam.topMargin = (int)(screen.x * 112.0/720.0);
 			
 			RelativeLayout.LayoutParams startTextParam = (LayoutParams) startText.getLayoutParams();
 			startTextParam.topMargin = (int)(screen.x * 246.0/720.0);
 			startTextParam.leftMargin =(int)(screen.x * 435.0/720.0);
 			
 			RelativeLayout.LayoutParams previewParam = (LayoutParams) preview_layout.getLayoutParams();
 			previewParam.width = (int)(screen.x * 588.0/720.0);
 			previewParam.height = (int)(screen.x * 750.0/720.0);
 			previewParam.leftMargin = (int)(screen.x * 62.0/720.0);
 			previewParam.topMargin = (int)(screen.x * 233.0/720.0);
 			
 			//Bugs Here
 			if(bgBmp!=null && !bgBmp.isRecycled())
 				bg.setImageBitmap(bgBmp);
 			
 			if(startLineBmp!=null && !startLineBmp.isRecycled())
 				startLine.setImageBitmap(startLineBmp);
 			startLine.setVisibility(View.VISIBLE);
 			
 			if (startCircleBmp!=null && !startCircleBmp.isRecycled())
 				startCircle.setImageBitmap(startCircleBmp);
 			//End
 			startCircle.setVisibility(View.VISIBLE);
 			startText.setText("點我開始");
 			startText.setVisibility(View.VISIBLE);
 			
 			messageView.setText("請點選'點我開始'以進行測試");
 			messageView.setTextColor(0xFFFFFFFF);
 			
 			startCircle.setOnClickListener(new StartOnClickListener());
 			bg.setOnClickListener(null);
 		
 			sensor_button.setBackgroundColor(0xFF000000);
 			sensor_button.setVisibility(View.VISIBLE);
 			if (blink_anim!=null)
 				blink_anim.cancel();
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.INVISIBLE);
 			LoadingBox.dismiss();
 		}
 	}
     
 	@SuppressLint("HandlerLeak")
 	private class MsgLoadingHandler extends Handler{
 		
 		public void handleMessage(Message msg){
 			if (msgBox!=null){
 				msgBox.settingPreTask();
 				msgBox.settingInBackground();
 				msgBox.settingPostTask();
 			}
 			cleanRotate();
 			bg.setImageBitmap(null);
 			if (bgBmp!=null && !bgBmp.isRecycled()){
 				bgBmp.recycle();
 				bgBmp = null;
 			}
 			Point screen = FragmentTabs.getSize();
 			Bitmap tmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.test_start_bg_2);
 			bgBmp = Bitmap.createScaledBitmap(tmp, screen.x, screen.y, true);
 			tmp.recycle();
 			if(bgBmp!=null && !bgBmp.isRecycled())
 				bg.setImageBitmap(bgBmp);
 			if (msgBox!=null){
 				msgBox.generateGPSCheckBox();
 				messageView.setText("請依對話框指示進行操作");
 				messageView.setTextColor(0xFFFFFFFF);
 			}else{
 				messageView.setText("請依對話框指示進行操作XXXX");
 				messageView.setTextColor(0xFFFFFFFF);
 			}
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.INVISIBLE);
 		}
 	}
     
 	@SuppressLint("HandlerLeak")
 	private class FailBgHandler extends Handler{
 		
 		private String msgStr;
 	
 		public void handleMessage(Message msg){
 			if (msgLoadingHandler !=null){
 				msgLoadingHandler.removeMessages(0);
 				msgLoadingHandler = null;
 			}
 			if (testHandler!=null){
 				testHandler.removeMessages(0);
 				testHandler = null;
 			}
 			
 			this.msgStr = msg.getData().getString("msg");
 			cleanRotate();
 			msgStr = msgStr.concat("\n請點選畫面以重新開始");
 			bg.setImageBitmap(null);
 			if(bgBmp!=null && !bgBmp.isRecycled()){
 				bgBmp.recycle();
 				bgBmp = null;
 			}
 			
 			Point screen = FragmentTabs.getSize();
 			Bitmap tmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.test_start_bg_2);
 			bgBmp = Bitmap.createScaledBitmap(tmp, screen.x, screen.y, true);
 			tmp.recycle();
 			
 			bg.setImageBitmap(bgBmp);
 			sensor_button.setBackgroundColor(0xFF000000);
 			messageView.setText(msgStr);
 			messageView.setTextColor(0xFFFF0000);
 			bg.setOnClickListener(new EndTestOnClickListener());
 			
 			sensor_button.setVisibility(View.VISIBLE);
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.VISIBLE);
 			
 			sensor_lights[1].setAnimation(blink_anim);
 			blink_anim.start();
 		}
 	}
 	@SuppressLint("HandlerLeak")
 	private class TestHandler extends Handler{
 		public void handleMessage(Message msg){
 			bg.setImageBitmap(null);
 			if (rotate == null)
 				rotate = new UIRotate(testFragment,main_layout);
 			rotate.settingPreTask();
 			
 			if(bgBmp!=null && !bgBmp.isRecycled()){
 				
 				bgBmp.recycle();
 				bgBmp = null;
 			}
 			
 			Point screen = FragmentTabs.getSize();   
 			Bitmap tmp = BitmapFactory.decodeResource(view.getResources(), R.drawable.test_camera_bg);
 			bgBmp = Bitmap.createScaledBitmap(tmp, screen.x, screen.y, true);
 			tmp.recycle();
 			
 			rotate.settingInBackground();
 			
 			bg.setImageBitmap(bgBmp);
 			bg.setOnClickListener(null);
 			rotate.settingPostTask();
 			
 			messageView.setText("請依照圓圈內指示進行測試,\n並將臉對於螢幕中央");
 			messageView.setTextColor(0xFFFFFFFF);
 			if (bt!=null && cameraRecorder!=null){
 				bt.start();
 				cameraRecorder.start();
 			}
 			sensor_button.setVisibility(View.INVISIBLE);
 			sensor_lights[0].setVisibility(View.INVISIBLE);
 			sensor_lights[1].setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	public void changeTestMessage(int time){
 		rotate.setText(time);
 	}
 	
 	public void changeTestSpeed(int change){
 		rotate.setSpeed(change);
 	}
 	
 	public void stopByFail(){
 		Log.d("test","stop by time out");
 		Message msg = new Message();
 		Bundle data = new Bundle();
 		data.putString("msg","測試失敗,可能因為測試超時或酒測器沒電了");
 		msg.setData(data);
 		msg.what = 0;
 		if (failBgHandler!=null)
 			failBgHandler.sendMessage(msg);
 	}
 
 	public boolean isKeepMsgBox() {
 		return keepMsgBox;
 	}
 
 	public void setKeepMsgBox(boolean keepMsgBox) {
 		this.keepMsgBox = keepMsgBox;
 	}
 	
 	@SuppressLint("HandlerLeak")
 	private class TimeUpHandler extends Handler{
 		public void handleMessage(Message msg){
 			int t = msg.what;
 			if (t == 0){
 				showDebug(">Try to start the device");
 				startBT();
 			}
 			else if (t == 1){
 				showDebug(">Start to run the  device");
 				runBT();
 			}
 		}
 	}
 	
 	private class TimeUpRunnable implements Runnable{
 
 		int msg;
 		int time;
 		public TimeUpRunnable (int msg, int time){
 			this.msg = msg;
 			this.time = time;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				Thread.sleep(time);
 				timeUpHandler.sendEmptyMessage(msg);
 			} catch (InterruptedException e) {}
 		}
 		
 	}
 
 	@SuppressLint("HandlerLeak")
 	private class ChangeTabsHandler extends Handler{
 		public void handleMessage(Message msg){
 			if (msgBox!=null)
 				msgBox.closeInitializingBox();
 			FragmentTabs.changeTab(1);
 		}
 	}
 	
 	private void checkDebug(){
 		SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this.getActivity());
 		Boolean debug = sp.getBoolean("debug", false);
 		ScrollView sv = (ScrollView) view.findViewById(R.id.debugView);
 		if (debug){
 			sv.setVisibility(View.VISIBLE);
 			msgHandler = new ChangeMsgHandler();
 			debugMsg.setText("");
 			debugMsg.setVisibility(View.VISIBLE);
 			debugMsg.setClickable(false);
 			debugMsg.setOnKeyListener(null);
 			debugMsg.setEnabled(false);
 		}else{
 			sv.setVisibility(View.INVISIBLE);
 			debugMsg.setVisibility(View.INVISIBLE);
 		}
 			
 	}
 	
 	
 	public void showDebug(String message){
 		SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this.getActivity());
 		Boolean debug = sp.getBoolean("debug", false);
 		if (msgHandler!=null && debug){
 			Message msg = new Message();
 			Bundle data = new Bundle();
 			data.putString("message", message);
 			msg.setData(data);
 			msg.what = 0;
 			msgHandler.sendMessage(msg);
 		}
 		
 	}
 	
 	@SuppressLint("HandlerLeak")
 	private class ChangeMsgHandler extends Handler{
 		public void handleMessage(Message msg){
 			debugMsg.append("\n"+msg.getData().getString("message"));
 			debugMsg.setSelection(debugMsg.getText().length());
 		}
 	}
 	
 }
