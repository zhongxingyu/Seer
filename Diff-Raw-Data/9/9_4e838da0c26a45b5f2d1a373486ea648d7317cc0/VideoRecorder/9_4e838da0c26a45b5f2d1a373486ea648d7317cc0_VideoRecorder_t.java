 package io.trigger.forge.android.shortvideo.activity;
 
 import io.trigger.forge.android.core.ForgeApp;
 import io.trigger.forge.android.core.ForgeLog;
 import io.trigger.forge.android.modules.shortvideo.API;
 import io.trigger.forge.android.shortvideo.net.CustomMultipartEntity;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketTimeoutException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.media.AudioManager;
 import android.media.CamcorderProfile;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ConnectTimeoutException;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class VideoRecorder extends Activity implements SurfaceHolder.Callback {
 	private SurfaceHolder mSurfaceHolder;
 	private SurfaceView mSurfaceView;
 	private MediaRecorder mRecorder;
 	private MediaPlayer mPlayer;
 
 	private ImageButton mStartButton = null;
 	private ImageButton mReturnButton = null;
 	private ImageButton mRedoButton = null;
 	private ProgressBar mUploadProgress = null;
 	private boolean mRecording;
 	private boolean mUploading;
 
 	private Handler mHandler;
 
 	private File mVideo;
 	private Camera mCamera;
 	long mUploadSize;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		mHandler = new Handler();
 		mRecorder = new MediaRecorder();
 		mPlayer = new MediaPlayer();
 
 		setContentView(ForgeApp.getResourceId("video_layout", "layout"));
 
 		SurfaceView surfaceGrid = (SurfaceView) findViewById(ForgeApp
 				.getResourceId("surface_grid", "id"));
 		surfaceGrid.setBackgroundResource(ForgeApp.getResourceId("grid",
 				"drawable"));
 
 		mSurfaceView = (SurfaceView) findViewById(ForgeApp.getResourceId(
 				"surface_camera", "id"));
 
 		mUploadProgress = (ProgressBar) findViewById(ForgeApp.getResourceId(
 				"upload_progress", "id"));
 		mUploadProgress.setProgressDrawable(getApplicationContext()
 				.getResources().getDrawable(
 						ForgeApp.getResourceId("progress", "drawable")));
 		mUploadProgress.setIndeterminateDrawable(getApplicationContext()
 				.getResources().getDrawable(
 						ForgeApp.getResourceId("progress", "drawable")));
 	}
 
 
 	private void setButtonText(String resource, ImageButton button) {
 		Bitmap buttonText = BitmapFactory.decodeResource(getResources(),
 				ForgeApp.getResourceId(resource, "drawable"));
 		button.setImageBitmap(Bitmap.createScaledBitmap(buttonText,
 				buttonText.getWidth() / 2, buttonText.getHeight() / 2, false));
 		buttonText.recycle();
 	}
 
 	private void initStartButton() {
 		mStartButton = (ImageButton) findViewById(ForgeApp.getResourceId(
 				"buttonstart", "id"));
 		setButtonText("record", mStartButton);
 		mStartButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
 
 		mUploadProgress.setSecondaryProgress(0);
 		mUploadProgress.setProgress(100);
 		mStartButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (!mRecording) {
 					try {
 						stopPlaying();
 						startRecording();
 						mRecording = true;
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						mRecording = false;
 						e.printStackTrace();
 					}
 				}
 			}
 
 		});
 	}
 
 	private void initRedoButton() {
 		mRedoButton = (ImageButton) findViewById(ForgeApp.getResourceId(
 				"redobutton", "id"));		
 		setButtonText("redo", mRedoButton);
 		mRedoButton.setBackgroundResource(ForgeApp.getResourceId("ltgrey",
 				"color"));
 
 		mRedoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 			}
 		});
 	}
 
 	private void initReturnButton() {
 		mReturnButton = (ImageButton) findViewById(ForgeApp.getResourceId(
 				"returnbutton", "id"));
 		setButtonText("returnbtn", mReturnButton);
 		mReturnButton.setBackgroundResource(ForgeApp.getResourceId("button",
 				"drawable"));
 		mReturnButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				makeResult();
 				cleanUp();
 				finish();
 			}
 		});
 	}
 
 	private void onRedo() {
 		stopPlaying();
 		initStartButton();
 		initRedoButton();
 		initCamera();
 	}
 
 	public void onResume() {
 		super.onResume();
 		mRecording = false;
 		mSurfaceHolder = mSurfaceView.getHolder();
 		mSurfaceHolder.addCallback(this);
 
 		initStartButton();
 		initRedoButton();
 		initReturnButton();
 	}
 
 	private void uploadVideo() {
 		if (!mUploading) {
 			UploadTask task = new UploadTask();
 			task.execute(mVideo);
 		}
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		initCamera();
 		try {
 			mCamera.setPreviewDisplay(mSurfaceHolder);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 	}
 
 	public void onDestroy() {
 		stopPlaying();
 		stopRecording();
 		super.onDestroy();
 	}
 
 	private void onUploadFailure() {
 		mUploadProgress.setProgress(0);
 		mUploadProgress.setSecondaryProgress(100);
 		setButtonText("tryagain", mStartButton);
 	}
 
 	private void onUploadSuccess() {
 		mUploadProgress.setProgress(100);
 		setButtonText("successbtn", mStartButton);
 
 		mStartButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				makeResult();
 				cleanUp();
 				finish();
 			}
 		});
 		initRedoButton();
 
 		// let's get out of here
 		Runnable goHome = new Runnable() {
 			public void run() {
 				makeResult();
 				cleanUp();
 				try {
 					Thread.sleep(250);
 					finish();				
 				} catch (InterruptedException e) {
 					finish();				
 				}				
 			}			
 		};
 		new Thread(goHome).run();
 	}
 
 	@SuppressLint({ "InlinedApi", "NewApi" })
 	private void startRecording() throws IOException {
 		new RecordTask().execute();		
 	}
 
 	private void stopRecording() {
 		if (mRecording) {
 			mRecorder.stop();
 			mRecorder.reset();
 		}
 		mRecorder.release();
 		if (mCamera != null) {
 			mCamera.stopPreview();
 			mCamera.lock();
 			mCamera.release();
 		}
 		mCamera = null;
 		mRecording = false;
 	}
 
 	private void playVideo() {
 		mPlayer = new MediaPlayer();
 		try {
 			final Runnable startVideo = new Runnable() {
 				public void run() {
 					mPlayer.start();
 				}
 			};
 			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			mPlayer.setDataSource(mVideo.getAbsolutePath());
 			mPlayer.setDisplay(mSurfaceHolder);
 			mPlayer.setLooping(true);			
 			mPlayer.prepare();
 			new Thread(startVideo).run();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		ForgeLog.d("started video");
 	}
 
 	private void stopPlaying() {
 		if (mPlayer != null) {
 			if (mPlayer.isPlaying()) {
 				mPlayer.stop();
 			}
 			mPlayer.reset();
 			mPlayer.release();
 			mPlayer = null;
 		}
 	}
 
 	private void makeResult() {
 		Intent data = new Intent();
 		if (mVideo != null) {
 			data.putExtra(API.VIDEO_KEY, Uri.fromFile(mVideo).toString());
 			this.setResult(RESULT_OK, data);
 		}
 		mVideo = null;
 		finish();
 	}
 
 	@SuppressLint({ "InlinedApi", "NewApi"})
 	private void initCamera() {
 		mCamera = Camera.open();
 		setCameraDisplayOrientation(ForgeApp.getActivity(), 0, mCamera);
 		try {
 			mCamera.setPreviewDisplay(mSurfaceHolder);
 			Parameters parameters = mCamera.getParameters();
 			parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
 			mCamera.setParameters(parameters);
 			mCamera.startPreview();			
 		} catch (IOException e) {
 			e.printStackTrace();
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	private void cleanUp() {
 		if (mCamera != null) {
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 		}
 		if (mRecorder != null) {
 			if (mRecording) {
 				mRecorder.stop();
 				mRecording = false;
 			}
 			mRecorder.release();
 		}
 		if (mPlayer != null) {
 			if (mPlayer.isPlaying()) {
 				mPlayer.stop();
 			}
 			mPlayer.reset();
 			mPlayer.release();
 			mPlayer = null;
 		}
 		if (mVideo != null) {
 			mVideo = null;
 		}
 	}
 
 	@SuppressLint("NewApi")
 	public static void setCameraDisplayOrientation(Activity activity,
 			int cameraId, Camera camera) {
 		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
 		android.hardware.Camera.getCameraInfo(cameraId, info);
 		int rotation = activity.getWindowManager().getDefaultDisplay()
 				.getRotation();
 		int degrees = 0;
 		switch (rotation) {
 		case Surface.ROTATION_0:
 			degrees = 0;
 			break;
 		case Surface.ROTATION_90:
 			degrees = 90;
 			break;
 		case Surface.ROTATION_180:
 			degrees = 180;
 			break;
 		case Surface.ROTATION_270:
 			degrees = 270;
 			break;
 		}
 
 		int result;
 		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 			result = (info.orientation + degrees) % 360;
 			result = (360 - result) % 360; // compensate the mirror
 		} else { // back-facing
 			result = (info.orientation - degrees + 360) % 360;
 		}
 		camera.setDisplayOrientation(result);
 	}
 
 	private class UploadTask extends AsyncTask<File, Integer, Boolean> {
 		@Override
 		protected void onPreExecute() {
 			mUploading = true;
 			setButtonText("uploading", mStartButton);
 			mUploadProgress.setProgress(0);
 			mUploadProgress.setSecondaryProgress(0);
 		}
 
 		@Override
 		protected Boolean doInBackground(File... files) {
 			Intent intent = getIntent();
 			String term = intent.getStringExtra("term");
 			String token = intent.getStringExtra("token");
 
 			try {
 				CustomMultipartEntity entity = new CustomMultipartEntity(
 						new CustomMultipartEntity.ProgressListener() {
 							public void transferred(long transferred) {
 								publishProgress((int) ((transferred / (float) mUploadSize) * 100));
 							}
 
 						});
 
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httppost = new HttpPost(API.UPLOAD_URL);
 				httppost.addHeader("X-Token", token);
 
 				entity.addPart("term", new StringBody(term));
 				entity.addPart("file", new FileBody(mVideo));
 
 				mUploadSize = entity.getContentLength();
 				httppost.setEntity(entity);
 
 				HttpResponse response = httpclient.execute(httppost);
 				InputStream inputStream = response.getEntity().getContent();
 				java.util.Scanner s = new java.util.Scanner(inputStream)
 				.useDelimiter("\\A");
 				ForgeLog.d(s.next());
 			}
 			// show error if connection not working
 			catch (SocketTimeoutException e) {
 				e.printStackTrace();
 				return false;
 			}
 
 			catch (ConnectTimeoutException e) {
 				e.printStackTrace();
 				return false;
 			}
 
 			catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			mUploadProgress.setProgress(progress[0]);
 			super.onProgressUpdate(progress);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				onUploadSuccess();
 			} else {
 				onUploadFailure();
 			}
 			mUploading = false;
 		}
 	}
 
 	private class RecordTask extends AsyncTask<Void, Void, Void> {
 		final Runnable progressUpdate = new Runnable() {
 			public void run() {
 				mUploadProgress
 				.setSecondaryProgress(mUploadProgress
 						.getSecondaryProgress() + 100 / 10);
 				if (mUploadProgress.getSecondaryProgress() >= 100) {
 					return;
 				}
 				mHandler.postDelayed(this, 100);
 			}
 
 		};
 
 		@SuppressLint("NewApi")
 		@Override				
 		protected void onPreExecute() {			
 			mUploadProgress.setProgress(0);
 			mUploadProgress.setSecondaryProgress(0);			
			setButtonText("recordingtxt", mStartButton);
 
 			if (mCamera == null) {
 				initCamera();
 			}
 			mCamera.unlock();
 			mRecorder = new MediaRecorder();
 			mRecorder.setCamera(mCamera);
 		}
 
 		@SuppressLint("NewApi")
 		@Override
 		protected Void doInBackground(Void... arg0) {
 
 			mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
 			mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 			mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
 			mRecorder.setMaxDuration(1000);
 
 			CamcorderProfile profile;
 			if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
 				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
 			} else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
 				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
 			} else {
 				profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
 			}
 
 			mRecorder.setProfile(profile);
 			mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
 				public void onInfo(MediaRecorder mr, int what, int extra) {
 					if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
 						stopRecording();
 						mHandler.removeCallbacks(progressUpdate);
 						mUploadProgress.setSecondaryProgress(0);
 						mUploadProgress.setProgress(100);
 
 						playVideo();
 						mRedoButton.setBackgroundResource(ForgeApp
 								.getResourceId("drkblue", "color"));
 						mRedoButton.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								onRedo();
 							}
 						});
 						setButtonText("upload", mStartButton);
 						mStartButton.setOnClickListener(new OnClickListener() {
 							public void onClick(View v) {
 								uploadVideo();
 							}
 						});
 					}
 				}
 			});
 
 			File mediaStorageDir = new File(
 					Environment
 							.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
 					"ShortVideo");
 			if (!mediaStorageDir.exists()) {
 				if (!mediaStorageDir.mkdirs()) {
 					ForgeLog.d("failed to create directory");
 				}
 			}
 			try {
 				mVideo = File.createTempFile("zzzz", ".mp4", mediaStorageDir);
 				mRecorder.setOutputFile(mVideo.getAbsolutePath());
 				mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());	
 				mRecorder.setOrientationHint(90);
 				mRecorder.prepare();
 				mRecorder.start();
 				mHandler.post(progressUpdate);
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 			}
 			return null;
 		}
 
 	}
 }
