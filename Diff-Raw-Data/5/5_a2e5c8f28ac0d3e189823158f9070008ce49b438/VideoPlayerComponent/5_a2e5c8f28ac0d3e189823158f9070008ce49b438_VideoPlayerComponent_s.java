 /*
  * Kurento Android MSControl: MSControl implementation for Android.
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.mscontrol.kas.mediacomponent.internal;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.ErrorCallback;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.hardware.Camera.Size;
 import android.os.Build.VERSION;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 
 import com.kurento.commons.config.Parameters;
 import com.kurento.kas.media.profiles.VideoProfile;
 import com.kurento.mscontrol.commons.MsControlException;
 import com.kurento.mscontrol.commons.join.Joinable;
 import com.kurento.mscontrol.kas.join.VideoJoinableStreamImpl;
 import com.kurento.mscontrol.kas.mediacomponent.AndroidAction;
 import com.kurento.mscontrol.kas.mediacomponent.AndroidInfo;
 
 public class VideoPlayerComponent extends MediaComponentBase implements
 		SurfaceHolder.Callback, PreviewCallback {
 
 	private static final String LOG_TAG = "VideoPlayer";
 
 	private int width;
 	private int height;
 	private int widthInfo = 0;
 	private int heightInfo = 0;
 
 	private Camera mCamera;
 	private int cameraFacing = 0;
 
 	private final int screenOrientation;
 
 	private final Preview preview;
 	private final ViewGroup parent;
 	private boolean isReleased;
 
 	private synchronized int getWidthInfo() {
 		return widthInfo;
 	}
 
 	private synchronized void setWidthInfo(int widthInfo) {
 		this.widthInfo = widthInfo;
 	}
 
 	private synchronized int getHeightInfo() {
 		return heightInfo;
 	}
 
 	private synchronized void setHeightInfo(int heightInfo) {
 		this.heightInfo = heightInfo;
 	}
 
 	@Override
 	public boolean isStarted() {
 		return mCamera != null;
 	}
 
 	public VideoPlayerComponent(Parameters params) throws MsControlException {
 		if (params == null)
 			throw new MsControlException("Parameters are NULL");
 
 		parent = params.get(PREVIEW_SURFACE_CONTAINER).getValue();
 		if (parent == null)
 			throw new MsControlException(
 					"Params must have VideoPlayerComponent.PREVIEW_SURFACE param");
 
 		preview = new Preview(parent.getContext());
 		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 		parent.addView(preview);
 
 		screenOrientation = params.get(DISPLAY_ORIENTATION).getValue() * 90;
 		isReleased = false;
 		try {
 			cameraFacing = params.get(CAMERA_FACING).getValue();
 		} catch (Exception e) {
 			cameraFacing = 0;
 		}
 	}
 
 	private Camera openFrontFacingCameraGingerbread() {
 		int cameraCount = 0;
 		Camera cam = null;
 		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
 		cameraCount = Camera.getNumberOfCameras();
 
 		if (cameraCount == 1) {
 			try {
 				cam = Camera.open(0);
 			} catch (RuntimeException e) {
 				Log.e(LOG_TAG,
 						"Camera failed to open: " + e.getLocalizedMessage()
 								+ " ; " + e.toString());
 			}
 		} else {
 			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
 				Camera.getCameraInfo(camIdx, cameraInfo);
 				if (cameraInfo.facing == cameraFacing) {
 					try {
 						cam = Camera.open(camIdx);
 					} catch (RuntimeException e) {
 						Log.e(LOG_TAG,
 								"Camera failed to open: "
 										+ e.getLocalizedMessage() + " ; "
 										+ e.toString());
 					}
 				}
 			}
 		}
 
 		return cam;
 	}
 
 	@Override
 	public void onPreviewFrame(byte[] data, Camera camera) {
 		if (data == null)
 			return;
 		long time = System.currentTimeMillis();
 		try {
 			for (Joinable j : getJoinees(Direction.SEND))
 				if (j instanceof VideoSink)
 					((VideoSink) j).putVideoFrame(data, width, height, time);
 		} catch (MsControlException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void start() throws MsControlException {
 		VideoProfile videoProfile = null;
 		for (Joinable j : getJoinees(Direction.SEND))
 			if (j instanceof VideoJoinableStreamImpl) {
 				videoProfile = ((VideoJoinableStreamImpl) j).getVideoProfile();
 			}
 		if (videoProfile == null)
 			throw new MsControlException("Cannot get video profile.");
 
 		this.width = videoProfile.getWidth();
 		this.height = videoProfile.getHeight();
 		setWidthInfo(width);
 		setHeightInfo(height);
 
 		if (preview.getSurface().isShown()) {
 			startCamera(preview.getHolder());
 		}
 
 		preview.getHolder().addCallback(this);
 	}
 
 	private void startCamera(SurfaceHolder surfHold) {
 		Log.d(LOG_TAG, "start camera");
 		if (isReleased)
 			return;
 
 		if (mCamera == null) {
 			if (VERSION.SDK_INT < 9) {
 				mCamera = Camera.open();
 			} else
 				mCamera = openFrontFacingCameraGingerbread();
 		}
 
 		mCamera.setErrorCallback(new ErrorCallback() {
 			@Override
 			public void onError(int error, Camera camera) {
 				Log.e(LOG_TAG, "Camera error : " + error);
 			}
 		});
 
 		Camera.Parameters parameters = mCamera.getParameters();
 		List<Size> sizes = parameters.getSupportedPreviewSizes();
 
 		boolean isSupport = false;
 		int sizeSelected = -1;
 		for (int i = 0; i < sizes.size(); i++) {
 			if ((width == sizes.get(i).width)
 					&& (height == sizes.get(i).height)) {
 				isSupport = true;
 				break;
 			}
 			if (sizeSelected == -1) {
 				if (sizes.get(i).width <= width)
 					sizeSelected = i;
 			} else if ((sizes.get(i).width >= sizes.get(sizeSelected).width)
 					&& (sizes.get(i).width <= width))
 				sizeSelected = i;
 		}
 		if (sizeSelected == -1)
 			sizeSelected = sizes.size() - 1;
 		if (!isSupport) {
 			width = sizes.get(sizeSelected).width;
 			height = sizes.get(sizeSelected).height;
 		}
 		parameters.setPreviewSize(width, height);
 
		if (VERSION.SDK_INT >= 14)
 			parameters
 					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
 		mCamera.setParameters(parameters);
 
 		String cad = "";
 		for (int i = 0; i < sizes.size(); i++)
 			cad += sizes.get(i).width + " x " + sizes.get(i).height + "\n";
 		Log.d(LOG_TAG, "getPreviewSize: " + parameters.getPreviewSize().width
 				+ " x " + parameters.getPreviewSize().height);
 		Log.d(LOG_TAG, "getSupportedPreviewSizes:\n" + cad);
 
 		try {
 			mCamera.setPreviewDisplay(surfHold);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(LOG_TAG, "Can not set preview display.");
 		}
 		try {
 			mCamera.startPreview();
 			mCamera.setPreviewCallback(this);
 			preview.requestLayout();
 		} catch (Throwable e) {
 			e.printStackTrace();
 			Log.e(LOG_TAG, "Can not start camera preview");
 		}
 	}
 
 	@Override
 	public void stop() {
 		if (mCamera != null) {
 			mCamera.setPreviewCallback(null);
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	@Override
 	public void release() {
 		stop();
 		isReleased = true;
 		if (preview.getHolder() != null)
 			preview.getHolder().removeCallback(this);
 
 		parent.removeView(preview);
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		Log.d(LOG_TAG, "Surface destroyed");
 		if (mCamera != null) {
 			mCamera.setPreviewCallback(null);
 			mCamera.stopPreview();
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		Log.d(LOG_TAG, "Surface created");
 		startCamera(preview.getHolder());
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		Log.d(LOG_TAG, "Surface changed");
 	}
 
 	@Override
 	public Object getInfo(AndroidInfo info) throws MsControlException {
 		if (AndroidInfo.FRAME_TX_WIDTH.equals(info)) {
 			return getWidthInfo();
 		} else if (AndroidInfo.FRAME_TX_HEIGHT.equals(info)) {
 			return getHeightInfo();
 		} else
 			return super.getInfo(info);
 	}
 
 	@Override
 	public void onAction(AndroidAction action) throws MsControlException {
 		if (action == null)
 			throw new MsControlException("Action not supported");
 
 		if (AndroidAction.CAMERA_AUTOFOCUS.equals(action)) {
 			try {
 				mCamera.autoFocus(null);
 			} catch (Exception e) {
 				Log.e(LOG_TAG, e.getMessage(), e);
 			}
 		} else if (AndroidAction.CAMERA_TAKEPHOTO.equals(action)) {
 			if (mCamera != null) {
 				mCamera.takePicture(myShutterCallback, myPictureCallback_RAW,
 						myPictureCallback_JPG);
 			}
 		} else if (AndroidAction.CAMERA_FLASH_ON.equals(action)) {
 			try {
 				Camera.Parameters params = mCamera.getParameters();
 				params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
 				mCamera.setParameters(params);
 				mCamera.startPreview();
 			} catch (Exception e) {
 				Log.w(LOG_TAG,
 						"Camera does not support flash. " + e.getMessage(), e);
 			}
 		} else if (AndroidAction.CAMERA_FLASH_OFF.equals(action)) {
 			try {
 				Camera.Parameters params = mCamera.getParameters();
 				params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
 				mCamera.setParameters(params);
 				mCamera.startPreview();
 			} catch (Exception e) {
 				Log.w(LOG_TAG,
 						"Camera does not support flash. " + e.getMessage(), e);
 			}
 		} else
 			throw new MsControlException("Action not supported");
 	}
 
 	ShutterCallback myShutterCallback = new ShutterCallback() {
 		@Override
 		public void onShutter() {
 
 		}
 	};
 
 	PictureCallback myPictureCallback_RAW = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] arg0, Camera arg1) {
 
 		}
 	};
 
 	PictureCallback myPictureCallback_JPG = new PictureCallback() {
 		@Override
 		public void onPictureTaken(byte[] arg0, Camera arg1) {
 			Log.d(LOG_TAG, "onPictureTaken");
 			mCamera.startPreview();
 		}
 	};
 
 	class Preview extends ViewGroup {
 		private final String TAG = "Preview";
 
 		SurfaceView mSurfaceView;
 		SurfaceHolder mHolder;
 
 		Preview(Context context) {
 			super(context);
 
 			mSurfaceView = new SurfaceView(context);
 			mSurfaceView.setLayoutParams(new LayoutParams(
 					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 			addView(mSurfaceView);
 
 			mHolder = mSurfaceView.getHolder();
 		}
 
 		public SurfaceHolder getHolder() {
 			return mHolder;
 		}
 
 		public View getSurface() {
 			return mSurfaceView;
 		}
 
 		@Override
 		protected void onLayout(boolean changed, int l, int t, int r, int b) {
 			if (getChildCount() > 0) {
 				int previewWidth = getWidth();
 				int previewHeight = getHeight();
 
 				try {
 					float aspectScreen = (float) previewWidth
 							/ (float) previewHeight;
 					float aspectFrame = (float) width / (float) height;
 					double aux;
 					int heightAux, widthAux;
 					if (aspectFrame > aspectScreen) {
 						aux = (double) previewWidth / width;
 						heightAux = (int) (aux * height);
 						widthAux = previewWidth;
 					} else {
 						aux = (double) previewHeight / height;
 						heightAux = previewHeight;
 						widthAux = (int) (aux * width);
 					}
 
 					int left = (previewWidth - widthAux) / 2;
 					int top = (previewHeight - heightAux) / 2;
 
 					mSurfaceView.layout(left, top, left + widthAux, top
 							+ heightAux);
 				} catch (Throwable th) {
 					Log.e(TAG, "Error resizing: " + th.getMessage());
 				}
 			}
 		}
 	}
 
 }
