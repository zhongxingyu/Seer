 package com.android.untetheredrunning;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.Utils;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfDMatch;
 import org.opencv.core.MatOfKeyPoint;
 import org.opencv.core.Scalar;
 import org.opencv.features2d.DescriptorExtractor;
 import org.opencv.features2d.DescriptorMatcher;
 import org.opencv.features2d.FeatureDetector;
 import org.opencv.features2d.Features2d;
 import org.opencv.imgproc.Imgproc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class UntheredRunningActivity extends Activity {
     /** Called when the activity is first created. */
 
     Button btnTakePhoto;
     ImageView imgTakenPhoto;
     private static final int CAMERA_PIC_REQUEST = 1313;
     final String TAG = "MyCamera";
     
     private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
           switch (status) {
              case LoaderCallbackInterface.SUCCESS:
              {
                 Log.i(TAG, "OpenCV loaded successfully");
     	        // Create and set View
     	        setContentView(R.layout.activity_untethered_running);
     	        btnTakePhoto = (Button) findViewById(R.id.button1);
     	        imgTakenPhoto = (ImageView) findViewById(R.id.imageView1);
 
     	        btnTakePhoto.setOnClickListener(new btnTakePhotoClicker());
     	      
     	     } break;
     	     default:
     	     {
     	        super.onManagerConnected(status);
     	     } break;
     	  }
        }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Log.i(TAG, "Trying to load OpenCV library");
         if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
         {
           Log.e(TAG, "Cannot connect to OpenCV Manager");
         }
 
         
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
         
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
           if (data != null) {
         	 //Captured frame from Camera App 
              Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
              //Convert to grayscale
              thumbnail = toGrayscale(thumbnail);
              //thumbnail = toGrayscale(thumbnail,80,60);
              Mat CamImage = new Mat();
              Mat LogoImage = new Mat();
              Mat CamDescriptors = new Mat();
              Mat LogoDescriptors = new Mat();
              MatOfKeyPoint CamKeypoints = new MatOfKeyPoint();
              MatOfKeyPoint LogoKeypoints = new MatOfKeyPoint();
              List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
              
              //Get logo/initial image to compare to
              File rootsd = Environment.getExternalStorageDirectory();
              
             //***File cannot fit Mat formatting, need picture taken or smaller pic... not sure
             //File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/Camera/Capstone_Team_Logo.png");
             //***different file for testing purposes, delete later
<<<<<<< HEAD
              File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/Camera/IMG_20130205_171648.jpg"); 
              
              Bitmap logo = BitmapFactory.decodeFile(dcim.toString());
              
              //converts to grayscale based on captured picture's width & height (change later)
              logo = toGrayscale(logo);
 
              
              //Configure bitmap pixels
              Bitmap mBitmap1 = thumbnail.copy(Bitmap.Config.ARGB_8888, false); 
              Bitmap mBitmap2 = logo.copy(Bitmap.Config.ARGB_8888, false);
              
             
              //Convert bitmap to mat
              Utils.bitmapToMat(mBitmap1, CamImage);
       		 Utils.bitmapToMat(mBitmap2, LogoImage);
       		
       		 Mat o_image1 = new Mat();
       		     		 
       		 Mat rgb1 = new Mat();
          	 Mat rgb2 = new Mat();
       	     Mat rgb3 = new Mat();
       		 
          	 //Color for circles
       		 //Scalar color2 = new Scalar(255,0,0);
       		 
              FeatureDetector FAST = FeatureDetector.create(FeatureDetector.FAST);
              
              // extract keypoints
              FAST.detect(LogoImage, LogoKeypoints);
              FAST.detect(CamImage, CamKeypoints);
              
              //Color space conversion
              Imgproc.cvtColor(CamImage, rgb1, Imgproc.COLOR_RGBA2RGB);
              Imgproc.cvtColor(LogoImage, rgb2, Imgproc.COLOR_RGBA2RGB);
              
              //Features2d.drawKeypoints(rgb1, CamKeypoints, rgb1, color2, 0);
              //Features2d.drawKeypoints(rgb2, LogoKeypoints, rgb2, color2, 0);
              
              DescriptorExtractor extracter = DescriptorExtractor.create(DescriptorExtractor.FREAK);
              
              extracter.compute(rgb1, CamKeypoints, CamDescriptors);
              extracter.compute(rgb2, LogoKeypoints, LogoDescriptors);
              
              DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
              //imgTakenPhoto.setImageBitmap(mBitmap2);
              //matcher.match(CamDescriptors, LogoDescriptors, matches);
              matcher.radiusMatch(CamDescriptors, LogoDescriptors, matches, 1000.0f);
              //if (!matches.isEmpty()) {
              Features2d.drawMatches2(rgb1, CamKeypoints, rgb2, LogoKeypoints, matches, rgb3);
              
              Imgproc.cvtColor(rgb3, o_image1, Imgproc.COLOR_RGB2RGBA);
              Bitmap bmp = Bitmap.createBitmap(o_image1.cols(), o_image1.rows(), Bitmap.Config.ARGB_8888);
              Utils.matToBitmap(o_image1, bmp);
              
              imgTakenPhoto.setImageBitmap(bmp);
             
              //}
           }
        }
     }
     
     public Bitmap toGrayscale(Bitmap bmpOriginal)
     {        
         int width, height;
         height = bmpOriginal.getHeight();
         width = bmpOriginal.getWidth();    
 
         Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
         Canvas c = new Canvas(bmpGrayscale);
         Paint paint = new Paint();
         ColorMatrix cm = new ColorMatrix();
         cm.setSaturation(0);
         ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
         paint.setColorFilter(f);
         c.drawBitmap(bmpOriginal, 0, 0, paint);
         return bmpGrayscale;
     }
 
     public Bitmap toGrayscale(Bitmap bmpOriginal,int setWidth,int setHeight)
     {  
 
         Bitmap bmpGrayscale = Bitmap.createBitmap(setWidth, setHeight, Bitmap.Config.RGB_565);
         Canvas c = new Canvas(bmpGrayscale);
         Paint paint = new Paint();
         ColorMatrix cm = new ColorMatrix();
         cm.setSaturation(0);
         ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
         paint.setColorFilter(f);
         c.drawBitmap(bmpOriginal, 0, 0, paint);
         return bmpGrayscale;
     }
     
     class btnTakePhotoClicker implements Button.OnClickListener
     {
         @Override
         public void onClick(View v) {
             // TODO Auto-generated method stub
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
         }
     }
 }
