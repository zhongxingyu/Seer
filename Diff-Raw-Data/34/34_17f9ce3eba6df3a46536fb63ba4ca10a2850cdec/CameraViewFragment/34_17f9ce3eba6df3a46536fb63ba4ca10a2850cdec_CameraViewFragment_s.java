 /*----------------------------------------------------------------------------
 
   WhiteBoardCorrection 
 
   This code is part of the following publication and was subject
   to peer review:
 
     "WhiteBoardCorrection" by Nekomeshi
 
   Copyright (c) Nekomeshi <Nekomeshi312@gmail.com>
 
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU Affero General Public License for more details.
 
   You should have received a copy of the GNU Affero General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>.
 
   ----------------------------------------------------------------------------*/
 package com.nekomeshi312.whiteboardcorrection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opencv.android.Utils;
 import org.opencv.core.CvType;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.nekomeshi312.cameraandparameters.CameraAndParameters;
 
 
 public class CameraViewFragment extends SherlockFragment
 								implements SurfaceHolder.Callback,
 											Camera.PreviewCallback,
 											CameraSurfaceView.CameraPreview{
 
 	public interface LineDetectCallback{
 		public void onLineDetected(ArrayList<Point> points);
 		public void onShutterReleased();
 		public void onJpegFileSelected(String fn, int width, int height);
 	}
 	
 	private static final String LOG_TAG = "CameraViewFragment";
 	protected Activity mParentActivity = null;
 	private CameraSurfaceView mCameraSurfaceView = null;
 	private WhiteBoardAreaView mWhiteBoardView = null;
 	private boolean mCameraSettingPrefOpen = false;
 	private TextView mTextViewLineDetectErrorMsg;	
 	private int mBaseWidth = -1;
 	private int mBaseHeight = -1;
 	private int mCameraSurfaceViewWidth = -1;
 	private int mCameraSurfaceViewHeight = -1;
 
 	private static final int ACTIVIY_REQUEST_CAMERA_SETTING_PREF = 0;
 	private static final int ACTIVIY_REQUEST_GALLERY_FILE_SELECT = 1;
 	private static final int ACTIVIY_REQUEST_GALLERY_IMAGE_CHECK = 2;
 
 	private  static CameraAndParameters mCameraSetting = WhiteBoardCorrectionActivity.getCameraSetting();
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onCreate");
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.actionbarsherlock.app.SherlockFragment#onDetach()
 	 */
 	@Override
 	public void onDetach() {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onDetach");
 		super.onDetach();
 		mParentActivity = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
 	 */
 	@Override
 	public void onAttach(Activity activity) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onAttach");
 		super.onAttach(activity);
 		if(!(activity instanceof LineDetectCallback)){
 			Log.e(LOG_TAG, "Parent is not instance of LineDetectCallback");
 			mParentActivity = null;
 		}
 		else{
 			mParentActivity = activity;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onActivityCreated");
 		super.onActivityCreated(savedInstanceState);
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.i(LOG_TAG, "onCreateView " + this.toString());
         ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_camera_view, container, false);
 
         if(mCameraSetting.openCamera(CameraAndParameters.CAMERA_FACING_BACK) == null){
 			Toast.makeText(mParentActivity, R.string.error_msg_cant_open_camera, Toast.LENGTH_SHORT).show();
 		}
         //previewのsurfaceViewを置くベースのlayoutサイズを初期化
     	mBaseWidth = -1;
     	mBaseHeight = -1;
     	mCameraSurfaceViewWidth = -1;
     	mCameraSurfaceViewHeight = -1;
 
     	ActionBar actionBar = ((SherlockFragmentActivity) mParentActivity).getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 
 		return root;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onDestroyView()
 	 */
 	@Override
 	public void onDestroyView() {
 		// TODO Auto-generated method stub
 		super.onDestroyView();
 		Log.i(LOG_TAG, "onDestroyView");
		if(!mCameraSettingPrefOpen){
 			mCameraSetting.releaseCamera();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.
 	 * support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
 	 */
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onViewCreated");
 
 		super.onViewCreated(view, savedInstanceState);
 		mTextViewLineDetectErrorMsg = (TextView)mParentActivity.findViewById(R.id.line_detection_error_msg);
 		mCameraSurfaceView = (CameraSurfaceView)mParentActivity.findViewById(R.id.camera_surface_view);
     	SurfaceHolder holder = mCameraSurfaceView.getHolder();
 		holder.addCallback(this);
 		if(Build.VERSION.SDK_INT < 11){
 			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 		mCameraSurfaceView.setCameraPreview(this);
 		mWhiteBoardView = (WhiteBoardAreaView)mParentActivity.findViewById(R.id.whiteboard_area_view);
 		mWhiteBoardView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
 			@Override
 			public void onGlobalLayout() {
 				// TODO Auto-generated method stub
 				if(mBaseWidth == -1){
					fitChildSize2CamSize();
 				}
 			}
 		});
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onPause()
 	 */
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onPause");
 
 		super.onPause();
		stopPreview();
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onResume()
 	 */
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG)Log.d(LOG_TAG, "onResume");
 
 		super.onResume();
		startPreview();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.actionbarsherlock.app.SherlockFragment#onCreateOptionsMenu(com.actionbarsherlock.view.Menu, com.actionbarsherlock.view.MenuInflater)
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// TODO Auto-generated method stub
     	inflater.inflate(R.menu.fragment_camera_view, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG) Log.i(LOG_TAG, item.toString());
 		Intent intent;
 		switch(item.getItemId()){
 			case R.id.menu_shutter_button:
 				LineDetectCallback cb = (LineDetectCallback)mParentActivity;
 				cb.onShutterReleased();
 				return true;
 			case R.id.menu_load_and_correct:
 				// インテント設定
 				intent = new Intent(Intent.ACTION_PICK);
 				intent.setType("image/jpeg");
 				// ギャラリー表示
 				startActivityForResult(intent, ACTIVIY_REQUEST_GALLERY_FILE_SELECT);
 				return true;
 			case R.id.menu_image_check:
 				// インテント設定
 				intent = new Intent(Intent.ACTION_PICK);
 				intent.setType("image/jpeg");
 				// ギャラリー表示
 				startActivityForResult(intent, ACTIVIY_REQUEST_GALLERY_IMAGE_CHECK);
 				return true;
 			case R.id.menu_settings:
 				mCameraSettingPrefOpen = true;
 				intent = new Intent(mParentActivity, 
 									CameraSettingActivity.class);
 				startActivityForResult(intent, ACTIVIY_REQUEST_CAMERA_SETTING_PREF);
 				return true;
 				
 			default:
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode){
 			case ACTIVIY_REQUEST_CAMERA_SETTING_PREF:
 				mCameraSettingPrefOpen = false;
				fitChildSize2CamSize();
 				break;
 			case ACTIVIY_REQUEST_GALLERY_FILE_SELECT:
 				if(data == null) break;
 				Cursor query = MediaStore.Images.Media.query(mParentActivity.getContentResolver(), 
 															data.getData(), 
 									new String[]{ MediaStore.Images.ImageColumns.DISPLAY_NAME, 
 													MediaStore.Images.ImageColumns.TITLE, //ついでに
 													MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,//ついでに
 													MediaStore.Images.ImageColumns.WIDTH,
 													MediaStore.Images.ImageColumns.HEIGHT
 													}, null, null);
 				query.moveToFirst();
 				//ファイル名(拡張子付き
 				String dispName = query.getString(0);
 				//表示名
 				String title = query.getString(1);
 				//たぶんフォルダ名（AllPicturesとかでも、実際の方が取れるので）
 				String backet = query.getString(2);				
 				int width = query.getInt(3);
 				int height = query.getInt(4);
 				if(MyDebug.DEBUG)Log.d(LOG_TAG, "dispname = " + dispName + 
 												"title = " + title + 
 												"backet = " + backet + 
 												"w/h = " + width + "/" + height);
 				if(width <= 0 || height <= 0){
 					Toast.makeText(mParentActivity, R.string.error_msg_saved_file_info, Toast.LENGTH_SHORT).show();
 					break;
 				}
 				((LineDetectCallback) mParentActivity).onJpegFileSelected(dispName, width, height);
 				break;
 			case ACTIVIY_REQUEST_GALLERY_IMAGE_CHECK:
 				if(data == null) break;
 				Intent intent = new Intent(Intent.ACTION_VIEW); 
 				try{
 					intent.setData(data.getData());
 					startActivity(intent); 
 				}
 				catch(Exception e){
 					e.printStackTrace();
 				}
 
 				break;
 		}
 	}
 
 	@Override
 	public Bitmap getCameraBitmap() {
 		// TODO Auto-generated method stub
 		return mBmp;
 	}	
 
 	private void fitChildSize2CamSize(){
 		final View viewBase = (View)mParentActivity.findViewById(R.id.camera_view_base);
 		final int w = viewBase.getWidth();
 		final int h = viewBase.getHeight();
 		Size sz = calcPreviewSize(w, h);
 		if(sz == null)return;
 		if(mBaseWidth == sz.width && mBaseHeight == sz.height) return;
 		mBaseWidth = sz.width;
 		mBaseHeight = sz.height;
 		
 		final View viewCamera = (View)mParentActivity.findViewById(R.id.camera_view_layout);
 		FrameLayout.LayoutParams prms = (FrameLayout.LayoutParams)viewCamera.getLayoutParams();
 		prms.width = mBaseWidth;
 		prms.height = mBaseHeight;
 		final int leftMargin = (w - mBaseWidth)/2;
 		final int topMargin = (h - mBaseHeight)/2;
 		prms.leftMargin = leftMargin;
 		prms.rightMargin = leftMargin;
 		prms.topMargin = topMargin;
 		prms.bottomMargin = topMargin;
 		viewCamera.setLayoutParams(prms);
 	}
 	
 	public SurfaceHolder getHolder(){
 		if(mCameraSurfaceView == null){
 			return null;		
 		}
 		return mCameraSurfaceView.getHolder();
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
 		// TODO Auto-generated method stub
 		if(MyDebug.DEBUG) Log.i(LOG_TAG, "surfaceChanged + " + width + ":" + height);
 		if(!mCameraSetting.isCameraOpen()) return;
 //		if(mCameraSurfaceViewWidth == width && mCameraSurfaceViewHeight == height) return; //surfaceのサイズが変わっていないのに呼ばれることがある（端末がある？）ので変わっていないときは処理しない
 		startPreview(width, height);			
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private DetectLines mLineDetector = null;
 
 	private byte[] mBuffer = null;
 	
 	private WhiteBoardDetect mWbDetect;
 
     private Mat mMatYuv = null;
 	private Mat mMatRgba = null;
     private Bitmap mBmp = null;
     private int[] 	mRgb = null;
     private int mLines[] = new int[DetectLines.MAX_LINE_NUM*5];// x5は 始点のｘ，ｙ　終点のx, y、太さの５つ
     
 
     private static final long CHECK_UNDETECT_TIME = 1000;//mS
     private long mLastUnDetectTime = -1;
 
 	@Override
 	public void onPreviewFrame(byte[] data, Camera camera) {
 		// TODO Auto-generated method stub
 		if(data == null || mMatYuv.cols()*mMatYuv.rows() != data.length) return;
 		
 	    mMatYuv.put(0, 0, data);
 	    //javaでHoughやる場合
 //		decodeYUV420SP(mRgb, data, mViewWidth, mViewHeight);
 //		mBmp.setPixels(mRgb, 0, mViewWidth, 0, 0, mViewWidth, mViewHeight);
 //		Utils.bitmapToMat(mBmp, mMatRgba);
 		//Canny -> Houghで線分抽出
 //		Mat gray = new Mat(mViewHeight, mViewWidth, CvType.CV_8UC1);
 //        Imgproc.cvtColor(mMatRgba, gray, Imgproc.COLOR_BGRA2GRAY, 0);
 //        Imgproc.Canny(gray, gray, 80, 100);
 //        Mat lines = new Mat();
 //        Imgproc.HoughLinesP(gray, lines, 1, Math.PI/180.0, (int) 50, 100.0, 20.0);        
 //		gray.release();
 	    //直線検出
 	    
 	    int lineNum = mLineDetector.lineDetect(mMatYuv, true,  mMatRgba, mLines);
 		Log.d(LOG_TAG, "line Num = " + lineNum);
 	
 		//ホワイトボードの4点座標を計算
 		ArrayList<Point> points = new ArrayList<Point>();
 		LineDetectCallback callback = (LineDetectCallback)mParentActivity;
 		
 		if(mWbDetect.detectWhiteBoard(mLines, lineNum, points, mMatRgba)){
 			mWhiteBoardView.setWhiteBoardCorners(points);
 			callback.onLineDetected(points);
 			if(mLastUnDetectTime > 0) {
 				mTextViewLineDetectErrorMsg.setVisibility(View.INVISIBLE);
 				mLastUnDetectTime = -1;
 			}
 		}
 		else{
 			callback.onLineDetected(null);
 			long currentSec = System.currentTimeMillis();
 			if(mLastUnDetectTime < 0){//前回まではホワイトボードが検出できていた
 				mLastUnDetectTime = currentSec;
 			}
 			else if( (currentSec - mLastUnDetectTime) < CHECK_UNDETECT_TIME){//規定時間立っていない
 			}
 			else if(mTextViewLineDetectErrorMsg.getVisibility() != View.VISIBLE){//規定時間たった
 				mTextViewLineDetectErrorMsg.setVisibility(View.VISIBLE);
 			}
 		}
 		if(mMatRgba != null){
 			Utils.matToBitmap(mMatRgba, mBmp);
 			mCameraSurfaceView.invalidate();
 		}
 		if(mCameraSetting == null) return;
 		if(mCameraSetting.getCamera() == null) return;
 		if(!mCameraSetting.isCameraOpen()) return;
 		mCameraSetting.getCamera().addCallbackBuffer(mBuffer);
 	}
 	
 	
 	private void setPreviewCallback(){
 		final Camera camera = mCameraSetting.getCamera();
 		//カメラの色深度を取得 bit/pixel
 		PixelFormat pixelinfo = new PixelFormat();
 		int pixelformat = camera.getParameters().getPreviewFormat();
 		PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);
 		//カメラのプレビュー解像度を取得
         Camera.Parameters parameters = camera.getParameters();
         Size sz = parameters.getPreviewSize();
         //プレビュー画像のサイズを計算し、バッファを確保
 		int bufSize = sz.width * sz.height * pixelinfo.bitsPerPixel / 8;		
 		mBuffer = new byte[bufSize];
 		camera.addCallbackBuffer(mBuffer);
 		camera.setPreviewCallbackWithBuffer(this);
 	}
 
 	
 	
 	static private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height){
 		final int frameSize = width * height;
 		for (int j = 0, yp = 0; j < height; j++) {
 			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
 			for (int i = 0; i < width; i++, yp++) {
 				int y = (0xff & ((int) yuv420sp[yp])) - 16;
 				if (y < 0) y = 0;
 				if ((i & 1) == 0) {
 					v = (0xff & yuv420sp[uvp++]) - 128;
 					u = (0xff & yuv420sp[uvp++]) - 128;
 				}
 				int y1192 = 1192 * y;
 				int r = (y1192 + 1634 * v);
 				int g = (y1192 - 833 * v - 400 * u);
 				int b = (y1192 + 2066 * u);
 				if (r < 0) r = 0; else if (r > 262143) r = 262143;
 				if (g < 0) g = 0; else if (g > 262143) g = 262143;
 				if (b < 0) b = 0; else if (b > 262143) b = 262143;
 				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
 						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
 			}
 		}
 	}
 
 	private class StartPreviewAsyncTask extends AsyncTask<Integer, Void, Boolean>{
 		private ProgressDialog mLoadingDialog;
 
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onCancelled()
 		 */
 		@Override
 		protected void onCancelled() {
 			// TODO Auto-generated method stub
 			super.onCancelled();
 			try{
 				if(mLoadingDialog != null) mLoadingDialog.dismiss();
 			}
 			catch(IllegalArgumentException e){//親のactivityが閉じてしまっている場合のエラーを無視する
 				e.printStackTrace();
 			}
 			mLoadingDialog = null;
 		}
 
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 		 */
 		@Override
 		protected void onPostExecute(Boolean result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			try{
 				if(mLoadingDialog != null) mLoadingDialog.dismiss();
 			}
 			catch(IllegalArgumentException e){//親のactivityが閉じてしまっている場合のエラーを無視する
 				e.printStackTrace();
 			}
 			mLoadingDialog = null;
 		}
 
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPreExecute()
 		 */
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			super.onPreExecute();
 			mLoadingDialog = new ProgressDialog(mParentActivity);
 			String msg = mParentActivity.getString(R.string.starting_camera);
 			mLoadingDialog.setMessage(msg);
 		    // 円スタイル（くるくる回るタイプ）に設定します
 		    mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		    mLoadingDialog.setCancelable(false);
 		    // プログレスダイアログを表示
 		    mLoadingDialog.show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Integer... params) {
 			// TODO Auto-generated method stub
 			final int width = params[0];
 			final int height = params[1];
 			if(MyDebug.DEBUG)Log.d(LOG_TAG, "wh = " + width + ":" + height + " mCameraSurfaceView = " + mCameraSurfaceViewWidth + ":" + mCameraSurfaceViewHeight);
 			mCameraSurfaceViewWidth = width;
 			mCameraSurfaceViewHeight = height;
 			try{
 				new Mat();//OpenCVライブラリがロードされているかどうか確認する
 			}
 			catch(UnsatisfiedLinkError e){
 				e.printStackTrace();
 				
 				return false;
 			}
 			try {
 				stopPreview();
 				mCameraSetting.getCamera().setPreviewDisplay(mCameraSurfaceView.getHolder());
 				Size minSize = calcPreviewSize(width, height);
 				mCameraSetting.mPreviewSize.setValueToPref(minSize, true);
 				mCameraSetting.mPictureSize.setValueToCam();//Preferenceの値をカメラにセット
 				Thread.sleep(100);//現在進行中のキャプチャがすべて掃けるのを待つ
 				setPreviewCallback();
 
 				mWbDetect = new WhiteBoardDetect(minSize.width, minSize.height);
 			    mWhiteBoardView.setInvalidWhiteBoardArea(mWbDetect.getUnAcceptableLineArea());
 				mLineDetector = new DetectLines(minSize.width, minSize.height, 4);
 
 		        mMatYuv = new Mat(minSize.height + minSize.height/2, minSize.width, CvType.CV_8UC1);
 //		    	mMatRgba = new Mat(minSize.height, minSize.width, CvType.CV_8UC4);
 //		        mBmp = Bitmap.createBitmap(minSize.width, minSize.height, Bitmap.Config.ARGB_8888);
 //		        mRgb = new int[minSize.width * minSize.height];
 
 				mCameraSetting.getCamera().startPreview();
 				return true;
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return false;
 		}
 	}
 
 	public boolean startPreview(){
 		if(MyDebug.DEBUG)Log.i(LOG_TAG, "startPreview()");
 		return startPreview(mCameraSurfaceViewWidth, mCameraSurfaceViewHeight);
 	}
 	private boolean startPreview(int width, int height) {
 		// TODO Auto-generated method stub
 		if(width < 0 || height < 0) return false;
 		StartPreviewAsyncTask prev = new StartPreviewAsyncTask();
 		prev.execute(width, height);
 		return true;
 	}
 
 	public void stopPreview() {
 		// TODO Auto-generated method stub
 		Log.i(LOG_TAG, "stopPreview");
 		if(mCameraSetting == null) return;
 		try{
 			if(mCameraSetting.getCamera() != null){
 				final Camera camera = mCameraSetting.getCamera();
             	camera.setPreviewCallbackWithBuffer(null);
             	mBuffer = null;
 				camera.stopPreview();
             	if(mWbDetect != null){
             		mWbDetect = null;
             	}
             	if(mLineDetector != null){
             		mLineDetector.cleanUp();
             		mLineDetector = null;
             	}
             	
 				if(mMatYuv != null) {
 					mMatYuv.release();
 					mMatYuv = null;
 				}
 				if(mMatRgba != null){
 					mMatRgba.release();
 					mMatRgba = null;
 				}
 				if(mBmp != null ){
 					mBmp.recycle();
 					mBmp = null;
 				}
 			}
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	private Size calcPreviewSize(int baseWidth, int baseHeight){
 
 		//撮影解像度取得
 		Size picSize = mCameraSetting.mPictureSize.getValueFromPref();
 		if(picSize == null) return null;
 		final double picAspect = (double)picSize.width/(double)picSize.height;
 
 		List<Size> size = mCameraSetting.mPreviewSize.getSupportedList();
 		if(MyDebug.DEBUG){
 			Log.d(LOG_TAG, "size_num = " + size.size());
 			Log.d(LOG_TAG, "picSize = " + picSize.width + ":" + picSize.height);
 			Log.d(LOG_TAG, "measurespecSize = " + baseWidth + ":" + baseHeight);
 		}
 		final double ASPECT_TOLERANCE = 0.1;
         double minDiff = Double.MAX_VALUE;
         double minAspect = Double.MAX_VALUE;
 
 		Size camS = null;
 		for(Size s:size){//preview sizeの中で、　撮影解像度に最も近いアスペクトでview横幅以下で、view高さ以下で最大となるものを探す
 			if(s.width > baseWidth) continue;
 			if(s.height > baseHeight)continue;
 			final double aspect = (double)s.width/(double)s.height;
 			final double diffW = Math.abs(s.width - baseWidth);
 			final double diffH = Math.abs(s.height - baseHeight);
 			if(minAspect < ASPECT_TOLERANCE ){
 				if(Math.abs(aspect - picAspect) >= ASPECT_TOLERANCE) continue;
 	            if (Math.min(diffW, diffH) < minDiff) {
 	            	camS = s;
 	            	minDiff = Math.min(diffW, diffH);
 	            }
 			}
 			else if(Math.abs(aspect - picAspect) < minAspect) {
             	minAspect = Math.abs(aspect - picAspect);
             	camS = s;
             	minDiff = Math.min(diffW, diffH);
             }
 		}
 		if(camS != null) Log.i(LOG_TAG, "preview size = " + camS.width + ":" + camS.height);
 		return camS;
 	}
 }
