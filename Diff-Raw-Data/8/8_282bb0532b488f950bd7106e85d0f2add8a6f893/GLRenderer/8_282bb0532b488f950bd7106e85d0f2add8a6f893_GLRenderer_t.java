 package com.slsw.fiarworks.firework;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.util.Arrays;
 import java.util.Calendar;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 import android.graphics.Rect;
 import android.graphics.YuvImage;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 
 import com.slsw.fiarworks.CameraPreview;
 import com.slsw.fiarworks.masker.AlphaMake;
 import com.slsw.fiarworks.bitmapTools.PixelTools;
 
 public class GLRenderer implements GLSurfaceView.Renderer {
 	private static final String TAG = "GLRenderer";
 	private Firework mFirework;
 	private GLCamera mCamera;
 	private float[] mProjMatrix = new float[16];
 	private float[] mVMatrix;
 	private float[] mMVPMatrix = new float[16];
 	private GLBackground mBackground;
 	private Bitmap mBackgroundImage = Bitmap.createBitmap(1, 1,
 			Bitmap.Config.RGB_565);
 	private static Bitmap myMask = Bitmap.createBitmap(1, 1,
 			Bitmap.Config.RGB_565);
 	private Bitmap[] imgs = new Bitmap[8];
 	private int captured = 0;
 	private int wait = 0;
 
 	@Override
 	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		mBackground = new GLBackground();
 		mFirework = new Firework();
 		mCamera = new GLCamera();
 		mBackgroundImage = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
 		mBackgroundImage.setPixel(0, 0, 0xffffff);
 		myMask = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
 		myMask.setPixel(0, 0, 0xffffff);
 		mFirework.Launch(0.2f, 10.0f);
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 unused, int width, int height) {
 		// Adjust the viewport based on geometry changes,
 		// such as screen rotation
 		GLES20.glViewport(0, 0, width, height);
 
 		float ratio = (float) width / height;
 
 		// this projection matrix is applied to object coordinates
 		// in the onDrawFrame() method
 		Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f,
 				100.0f);
 
 	}
 
 	@Override
 	public void onDrawFrame(GL10 unused) {
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 		// Draw background color
 		// GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
 		// Set the camera position (View matrix)
 		mVMatrix = mCamera.view();
 
 		// Calculate the projection and view transformation
 		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
 		mFirework.update();
 		mFirework.draw(mMVPMatrix);
 		mBackground.draw(mBackgroundImage);
 
 	}
 
 	public void setTextures(byte[] image, int width, int height,
 			CameraPreview prev) {
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		Bitmap BackgroundImage = PixelTools.makeBlackAndWhiteBitmap(image,
 				width, height);
 		// Bitmap BackgroundImage = PixelTools.makeColorBitmap(image, width,
 		// height);
 		if (BackgroundImage == null) {
 			System.err.println("FAILED to make Bitmap");
 		} else {
 			// mBackgroundImage.recycle();
 			mBackgroundImage = BackgroundImage;
 			// saveImages();
 		}
 
 		myMask = AlphaMake.makeSimpleMask(image, width, height,
 				prev.mRotVec);
 
 		mCamera.updateView(prev.mRotVec);
 	}
 
 	private void saveImages() {
 		if (wait < 60) {
 			wait++;
 		} else if (captured < imgs.length) {
 			imgs[captured] = mBackgroundImage;
 			captured++;
 		} else if (captured == imgs.length) {
 			// Save images to file
 			Calendar c = Calendar.getInstance();
 			int seconds = c.get(Calendar.SECOND);
 			int min = c.get(Calendar.MINUTE);
 			String seq = min + "_" + seconds;
 			for (int i = 0; i < captured; i++) {
 				Bitmap bmp = imgs[i];
 				try {
 					FileOutputStream o = new FileOutputStream(
 							"storage/sdcard0/testimg" + seq + "_" + i + ".png");
 					bmp.compress(Bitmap.CompressFormat.PNG, 100, o);
 					o.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			captured++;
 		}
 	}
 
 	public void setChange(float[] rot) {
 		System.err.println(Arrays.toString(rot));
 	}
     //x,y are screen coords
     //we need to get the y-axis angle in world coords
 	public void launchFirework(float x, float y) {
        float angle = 0.0f;
         // float angle = zAxis(mCamera.mViewMatrix)
         mFirework.Launch(angle, 10.0f);
 	}
 
 }
