 package to.rcpt.chronicle;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.hardware.Camera;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AnalogClock;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 import java.io.IOException;
 import java.util.List;
 
 import to.rcpt.DailySnap.R;
 
 public class Chronicle extends Activity {
     private Preview mPreview;
     Camera mCamera;
     int numberOfCameras;
     int cameraCurrentlyLocked;
 
     // The first rear facing camera
     int defaultCameraId;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Hide the window title.
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         // Create a RelativeLayout container that will hold a SurfaceView,
         // and set it as the content of our activity.
         String backgroundImagePath = getPreferences(MODE_PRIVATE).getString("referenceImage", null);
         Drawable d;
         if(backgroundImagePath == null)
         	d = null;
         else
         	d = getBackgroundDrawable(backgroundImagePath);
         mPreview = new Preview(this, d);
         setContentView(mPreview);
 
         // Find the total number of cameras available
         numberOfCameras = Camera.getNumberOfCameras();
 
         // Find the ID of the default camera
         CameraInfo cameraInfo = new CameraInfo();
         for (int i = 0; i < numberOfCameras; i++) {
         	Camera.getCameraInfo(i, cameraInfo);
         	if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
         		defaultCameraId = i;
         	}
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Open the default i.e. the first rear facing camera.
         mCamera = Camera.open();
         cameraCurrentlyLocked = defaultCameraId;
         mPreview.setCamera(mCamera);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (mCamera != null) {
             mPreview.setCamera(null);
             mCamera.release();
             mCamera = null;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.camera_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 //        // Handle item selection
         switch (item.getItemId()) {
         case R.id.pick_picture:
         	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         	intent.setType("image/*");
         	startActivityForResult(intent, 0);
 //        case R.id.switch_cam:
 //            // check for availability of multiple cameras
 //            if (numberOfCameras == 1) {
 //                AlertDialog.Builder builder = new AlertDialog.Builder(this);
 //                builder.setMessage(this.getString(R.string.camera_alert))
 //                       .setNeutralButton("Close", null);
 //                AlertDialog alert = builder.create();
 //                alert.show();
 //                return true;
 //            }
 //
 //            // OK, we have multiple cameras.
 //            // Release this camera -> cameraCurrentlyLocked
 //            if (mCamera != null) {
 //                mCamera.stopPreview();
 //                mPreview.setCamera(null);
 //                mCamera.release();
 //                mCamera = null;
 //            }
 //
 //            // Acquire the next camera and request Preview to reconfigure
 //            // parameters.
 //            mCamera = Camera
 //                    .open((cameraCurrentlyLocked + 1) % numberOfCameras);
 //            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
 //                    % numberOfCameras;
 //            mPreview.switchCamera(mCamera);
 //
 //            // Start the preview
 //            mCamera.startPreview();
 //            return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
         super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
 
         switch(requestCode) { 
         case 0:
             if(resultCode == RESULT_OK){  
                 Uri selectedImage = imageReturnedIntent.getData();
                 String[] filePathColumn = {MediaStore.Images.Media.DATA};
 
                 Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                 cursor.moveToFirst();
 
                 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                 String filePath = cursor.getString(columnIndex);
                 cursor.close();
 
                 
         		mPreview.setBackgroundDrawable(getBackgroundDrawable(filePath));
 
                 Editor prefs = getPreferences(MODE_PRIVATE).edit();
                 prefs.putString("referenceImage", filePath);
                 prefs.commit();
                 
             }
         }
     }
 
 	private Drawable getBackgroundDrawable(String filePath) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inSampleSize = 8;
 		Bitmap b = BitmapFactory.decodeFile(filePath, options);
 		BitmapDrawable d = new BitmapDrawable(getResources(), b);
 		d.setAlpha(128);
 		return d;
 	}
 }
 
 // ----------------------------------------------------------------------
 
 /**
  * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
  * to the surface. We need to center the SurfaceView because not all devices have cameras that
  * support preview sizes at the same aspect ratio as the device's display.
  */
 class Preview extends FrameLayout implements SurfaceHolder.Callback {
     private final String TAG = "Preview";
 
     SurfaceView mSurfaceView;
     SurfaceHolder mHolder;
     Size mPreviewSize;
     List<Size> mSupportedPreviewSizes;
     Camera mCamera;
 
 	private TextView text;
 
     Preview(Context context, Drawable d) {
         super(context);
 
         mSurfaceView = new SurfaceView(context);
         addView(mSurfaceView);
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         mHolder = mSurfaceView.getHolder();
         mHolder.addCallback(this);
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
         text = new TextView(context);
        text.setText("  Fooooooo  !");        
		text.setBackgroundDrawable(d);
 		addView(text);
     }
 
     public void setBackgroundDrawable(Drawable d) {
     	text.setBackgroundDrawable(d);
     }
     
     public void setCamera(Camera camera) {
         mCamera = camera;
         if (mCamera != null) {
             mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
             requestLayout();
         }
     }
 
     public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();
 
        camera.setParameters(parameters);
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         // We purposely disregard child measurements because act as a
         // wrapper to a SurfaceView that centers the camera preview instead
         // of stretching it.
         final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
         final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
         setMeasuredDimension(width, height);
 
         if (mSupportedPreviewSizes != null) {
             mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
         }
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         if (changed)
           for(int i = 0; i < getChildCount(); i++) {
             final View child = getChildAt(i);
 
             final int width = r - l;
             final int height = b - t;
 
             int previewWidth = width;
             int previewHeight = height;
             if (mPreviewSize != null) {
                 previewWidth = mPreviewSize.width;
                 previewHeight = mPreviewSize.height;
             }
 
             // Center the child SurfaceView within the parent.
             if (width * previewHeight > height * previewWidth) {
                 final int scaledChildWidth = previewWidth * height / previewHeight;
                 child.layout((width - scaledChildWidth) / 2, 0,
                         (width + scaledChildWidth) / 2, height);
             } else {
                 final int scaledChildHeight = previewHeight * width / previewWidth;
                 child.layout(0, (height - scaledChildHeight) / 2,
                         width, (height + scaledChildHeight) / 2);
             }
         }
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, acquire the camera and tell it where
         // to draw.
         try {
             if (mCamera != null) {
                 mCamera.setPreviewDisplay(holder);
             }
         } catch (IOException exception) {
             Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
         }
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         // Surface will be destroyed when we return, so stop the preview.
         if (mCamera != null) {
             mCamera.stopPreview();
         }
     }
 
 
     private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
         final double ASPECT_TOLERANCE = 0.1;
         double targetRatio = (double) w / h;
         if (sizes == null) return null;
 
         Size optimalSize = null;
         double minDiff = Double.MAX_VALUE;
 
         int targetHeight = h;
 
         // Try to find an size match aspect ratio and size
         for (Size size : sizes) {
             double ratio = (double) size.width / size.height;
             if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
             if (Math.abs(size.height - targetHeight) < minDiff) {
                 optimalSize = size;
                 minDiff = Math.abs(size.height - targetHeight);
             }
         }
 
         // Cannot find the one match the aspect ratio, ignore the requirement
         if (optimalSize == null) {
             minDiff = Double.MAX_VALUE;
             for (Size size : sizes) {
                 if (Math.abs(size.height - targetHeight) < minDiff) {
                     optimalSize = size;
                     minDiff = Math.abs(size.height - targetHeight);
                 }
             }
         }
         return optimalSize;
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // Now that the size is known, set up the camera parameters and begin
         // the preview.
         Camera.Parameters parameters = mCamera.getParameters();
         parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
         requestLayout();
 
         mCamera.setParameters(parameters);
         mCamera.startPreview();
     }
 
 }
