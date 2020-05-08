 package de.freinsberg.pomecaloco;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.Point;
 import org.opencv.core.Range;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.core.Size;
 import org.opencv.highgui.Highgui;
 import org.opencv.imgproc.Imgproc;
 
 import android.graphics.Bitmap;
 import android.os.Environment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.Display;
 import android.widget.Toast;
 
 
 public class ObjectDetector{
 	
 	private static final int ORIENTATION_0 = 0;
 	private static final int ORIENTATION_90 = 90;
 	private static final int ORIENTATION_270 = 270;
 	private static final int LEFT_LANE = 1;
 	private static final int RIGHT_LANE = 2;
 	final public static int NO_CAR = 0x00;
 	final public static int RIGHT_CAR = 0x01;
 	final public static int LEFT_CAR = 0x10;
 	final public static int BOTH_CAR = 0x11;
 	private static Mat mInputFrame;	
 	private static Mat mInputFramePortrait;
 	private Mat mStaticImage = null;
 	private Mat mRgba = null;
 	private Mat mGray = null;
 	private Mat mHSV = null; 
 	private Mat mEdges = null; 
 	private Mat mHoughLines = null;
 	private int mHLthreshold = 30;
     private int mHLminLineSize = 200;
     private int mHLlineGap = 20;
     private Point mLowerPoint;
     private Scalar mUpperLaneColor;
     private Scalar mLowerLaneColor;
     private Point mSeparatorPoint;
     private Point mUpperPoint;
     private boolean mFoundSeparatorLine;
     private boolean mFoundUpperLine;
     private boolean mFoundLowerLine;
     private boolean mDrawedLinesOnFrame;    
     private boolean mColorsOnLeftLane;
     private boolean mColorsOnRightLane;
 	private int mLowerThreshold;
 	private int mUpperThreshold;
 	private Mat mThreshed = null; 
 	private Mat mLowerRangeImage = null;
 	private Mat mUpperRangeImage = null;
 	private Mat mEmptyTrack = null;
 	private Mat track_overlay = null;
 	private Bitmap mTrackOverlay= null;
 	private Mat mCarRecognizerOverlay;
 	private Bitmap mCarRecognizer;
 	private Scalar mLeftCarColorScalar;
 	private Scalar mRightCarColorScalar;
 	private Mat mLeftCarColor;
 	private Mat mRightCarColor;
 	private Bitmap mLeftCarColorImage;
 	private Bitmap mRightCarColorImage;
 	private Bitmap[] mCarColorImages;
 	
 	double[] mScannedTrackPixelColor;
 	double[] mEmptyTrackPixelColor;
 	List <double[]> mFoundColors = new ArrayList<double[]>();
 	
 	private int avg_gray;
 	private File mStorageDir;
 	private File mHoughLinesImage;
 	private File mCannyEdgeImage;
 	private File mTrackColor;
 	private String mPath;
 	private static ObjectDetector mObjectDetector = new ObjectDetector();
 	
 	private ObjectDetector(){				
 		
 	}
 	
 	public static ObjectDetector getInstance(Mat inputFrame){
 		
 
 			mInputFrame = inputFrame;	
 			mInputFramePortrait = inputFrame.t();		
 			Core.flip(mInputFramePortrait, mInputFramePortrait, 1);	
 
 	
 		return mObjectDetector;
 	}
 	
 	public static ObjectDetector getInstance() {	
 		return mObjectDetector;
 	}
 	
 	private Mat correct_rotation(Display d, Mat m){
 		//Log.i("debug", "Jetzt wird rotiert!");
 		//Mat gray = new Mat(m.size(), m.type());
 		//Mat rotated = new Mat(new Size(m.rows(),m.cols()), m.type());
 		
 		//Imgproc.cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);
 
 		//Log.i("debug", "Found rotation: "+d.getRotation());
 	    int screenOrientation = d.getRotation();
 	    switch (screenOrientation){
 	        default:
 	        case ORIENTATION_0: // Portrait	
 	        	/*Log.i("debug", "Original Channels: "+m.channels());
 	        	Log.i("debug", "Original Size: "+m.size().toString());
 	        	Log.i("debug", "Original Type: "+m.type());
 	        	Log.i("debug", "Grayscale Channels: "+gray.channels());
 	        	Log.i("debug", "Grayscale Size: "+gray.size().toString());
 	        	Log.i("debug", "Grayscale Type: "+gray.type());
 	            Core.flip(gray.t(), gray, 1);
 	            Log.i("debug", "Grayscale after transpose Channels: "+gray.channels());
 	        	Log.i("debug", "Grayscale after transpose Size: "+gray.size().toString());
 	        	Log.i("debug", "Grayscale after transpose Type: "+gray.type());
 	            Imgproc.cvtColor(gray, rotated, Imgproc.COLOR_GRAY2RGB, 4);
 	        	Log.i("debug", "Rotated after cvt Channels: "+rotated.channels());
 	        	Log.i("debug", "Rotated after cvt Size: "+rotated.size().toString());
 	        	Log.i("debug", "Rotated after cvt Type: "+rotated.type());*/
 	        	
 	            //Log.i("debug", "Depth: "+m.depth()+ "Height: "+m.height()+"Width: "+m.width());
 	            //Core.flip(m.t(), rotated, 1);
 	            //Log.i("debug", "Depth: "+rotated.depth()+ "Height: "+rotated.height()+"Width: "+rotated.width());
 	            //Log.i("debug", "to Portrait");
 	            //rotated.release();
 	            break;
 	        case ORIENTATION_90: // Landscape right
 	            // do smth.
 	            break;
 	        case ORIENTATION_270: // Landscape left
 	            // do smth.
 	            break;
 	    }
 	    return m;
 	}
 		
 //	public Mat draw_colorrange_on_frame (Display d, Scalar lowerLimit, Scalar upperLimit){
 //		//Log.i("debug", "onCameraFrame");
 //		mRgba = correct_rotation(d, mInputFrame.rgba());
 //		if(mThreshed != null)
 //			mThreshed.release();
 //		mThreshed = new Mat(mRgba.size(),mRgba.type());
 //		//Log.i("debug",Integer.toString(mRgba.cols()));
 //		//mEdges = new Mat(mRgba.size(),mRgba.type());
 //		mHSV = new Mat(mRgba.size(),mRgba.type());	
 //		Imgproc.cvtColor(mRgba, mHSV, Imgproc.COLOR_RGB2HSV);	
 //		Core.inRange(mHSV, lowerLimit, upperLimit, mThreshed);
 //		
 //		mHSV.release();
 //	/*		Imgproc.cvtColor((Mat) inputFrame, mColoredFrame, Imgproc.COLOR_RGB2HSV);
 //			Log.i("debug", "Color to HSV");
 //			Core.inRange(mColoredFrame, lowerColorLimit, upperColorLimit, mColoredResult);
 //			Log.i("debug", "Colored Frame!");
 //			mFilteredFrame.setTo(new Scalar(0, 0, 0));
 //			Log.i("debug", "cleared new Frame");
 //			((Mat) inputFrame).copyTo(mFilteredFrame, mColoredResult);
 //			Log.i("debug", "Result!");*/	
 //			//Log.i("debug", "Grayscaled");
 //		return mThreshed;
 //	}
 	public boolean getFoundLines(){
 		return mFoundSeparatorLine && mFoundUpperLine && mFoundLowerLine;
 	}
 	
 	public int[] getCenterOfLanes(){
 		int[] arr = new int[2];
 		arr[0] = (int) (((mSeparatorPoint.x - mUpperPoint.x) / 2) + mUpperPoint.x);
 		arr[1] = (int) (((mLowerPoint.x -mSeparatorPoint.x) /2)+mSeparatorPoint.x);
 		
 		return arr;
 	}
 	
 	public Bitmap draw_car_recognizer(){
 		try{
 		mCarRecognizerOverlay = new Mat(new Size(mInputFramePortrait.cols(), mInputFramePortrait.rows()), mInputFramePortrait.type(), new Scalar(0, 0, 0, 0));
 		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[0] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFramePortrait.rows()/2) + 15), new Scalar(255,255,255,255));
 		Core.rectangle(mCarRecognizerOverlay, new Point(getCenterOfLanes()[1] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFramePortrait.rows()/2) + 15), new Scalar(255,255,255,255));
 		mCarRecognizer = Bitmap.createBitmap(mCarRecognizerOverlay.cols(), mCarRecognizerOverlay.rows(), Bitmap.Config.ARGB_8888);
 		Utils.matToBitmap(mCarRecognizerOverlay, mCarRecognizer);
 		}catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}		
 		return mCarRecognizer;
 	}
 	
 	public Rect[] getLanesToScanColor(){
 		Rect[] arr = new Rect[2];
 		arr[0] = new Rect(new Point(getCenterOfLanes()[0] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[0] + 15, (mInputFramePortrait.rows()/2) + 15));
 		arr[1] = new Rect(new Point(getCenterOfLanes()[1] - 15, (mInputFramePortrait.rows()/2) - 15), new Point(getCenterOfLanes()[1] + 15, (mInputFramePortrait.rows()/2) + 15));
 		
 		return arr;
 	}
 	
 	public Scalar getCarColor(int lane) {
 		if (lane == LEFT_LANE)
 			return mLeftCarColorScalar;
 		else 
 			return mRightCarColorScalar;
 	}
 	
 	public Bitmap getColorInOffset(int x_offset, int y_offset, int lane){
 		Log.i("debug", "Into getColorInOffset for lane :"+lane);	
 		if(mInputFramePortrait.empty())
 			Log.i("debug","mInputFramePortrait is empty!");
 		mRgba = mInputFramePortrait;
 		double[] oldColors = new double[4];
 		double[] newColors = new double[4];
 		double[] avg_oldColors = new double[4];
 		double[] avg_newColors = new double[4];
 		double[] foundColor = new double[4];
 		int scan_counter = 0;		
 		if(lane == LEFT_LANE)
 			mColorsOnLeftLane = false;
 		else if(lane == RIGHT_LANE)
 			mColorsOnRightLane = false;			
 		Log.i("debug", "Before FOR getColorInOffset for lane :"+lane);			
 		for(int x = x_offset;x< x_offset +30;x++){
 			for (int y = y_offset;y < y_offset+30;y++){
 				if(mEmptyTrack.empty())
 					Log.i("debug","mEmptyTrack is empty");
 				if(mRgba.empty())
 					Log.i("debug","mRgba is empty");
 				mEmptyTrackPixelColor = mEmptyTrack.get(y, x);
 				mScannedTrackPixelColor = mRgba.get(y, x);
 				if(mScannedTrackPixelColor == null)
 					Log.i("debug", "mScannedTrackPixelColor == null, at: "+x+", "+y);
 				if(mEmptyTrackPixelColor == null)
 					Log.i("debug","mEmptyTrackPixelColor == null, at: "+x+", "+y);
 				scan_counter++;
 //				double min = 256;
 //				double max = -1;
 //				
 //				for(int k = 0; k < 3; k++) {
 //					if(mScannedTrackPixelColor[k] < min)
 //						min = mScannedTrackPixelColor[k];
 //					if(mScannedTrackPixelColor[k] > max)
 //						max = mScannedTrackPixelColor[k];
 //				}
 //				
 //				double min2 = 256;
 //				double max2 = -1;
 //				
 //				for(int k = 0; k < 3; k++) {
 //					if(mEmptyTrackPixelColor[k] < min2)
 //						min2 = mEmptyTrackPixelColor[k];
 //					if(mEmptyTrackPixelColor[k] > max2)
 //						max2 = mEmptyTrackPixelColor[k];
 //				}
 				for(int i = 0; i <= 3; i++) {
 				newColors[i] += mScannedTrackPixelColor[i];
 				}
 
 			
 				for(int i = 0; i <= 3; i++) {
 					oldColors[i] += mEmptyTrackPixelColor[i];
 				}
 				
 				
 		
 			}		
 		}
 		Log.i("debug", "Between FOR getColorInOffset for lane :"+lane);	
 		for(int i = 0; i <= 3; i++) {
 			avg_oldColors[i] = oldColors[i] / scan_counter;
 			avg_newColors[i] = newColors[i] / scan_counter;
 
 		}
 		Log.i("debug", "After FOR getColorInOffset for lane :"+lane);	
 		Log.i("debug", "avg_Old: r"+avg_oldColors[0]+ ", g"+avg_oldColors[1]+ ", b"+avg_oldColors[2]);
 		Log.i("debug", "avg_New: r"+avg_newColors[0]+ ", g"+avg_newColors[1]+ ", b"+avg_newColors[2]);
 		boolean colorDetected = isDifferent(avg_newColors, avg_oldColors);	
 		Log.i("farbeee", "color on left lane AFTER for loop and BEFORE ifelses: "+ mLeftCarColorScalar);
 		Log.i("farbeee", "color on right lane AFTER for loop and BEFORE ifelses: "+ mRightCarColorScalar);
 		if(lane == LEFT_LANE)
 		{
 			if(!colorDetected)
 				mColorsOnLeftLane = false;
 			else{
 				mColorsOnLeftLane = true;
 				mLeftCarColorScalar = new Scalar(avg_newColors);
 				Log.i("farbeee", "color on left lane IN ifelses: "+ mLeftCarColorScalar);
 				mLeftCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFramePortrait.type(),mLeftCarColorScalar);				
 				mLeftCarColorImage = Bitmap.createBitmap(mLeftCarColor.cols(), mLeftCarColor.rows(), Bitmap.Config.ARGB_8888);
 				Utils.matToBitmap(mLeftCarColor, mLeftCarColorImage);					
 				mFoundColors.clear();
 				if(mColorsOnLeftLane)
 					Log.i("debug", "color on leftlane");
 				return mLeftCarColorImage;
 
 			}
 		}else if(lane == RIGHT_LANE)
 		{
 			if(!colorDetected)
 				mColorsOnRightLane = false;
 			else{
 				mColorsOnRightLane = true;
 				mRightCarColorScalar = new Scalar(avg_newColors);
 				Log.i("farbeee", "color on right lane IN ifelses: "+ mRightCarColorScalar);
 				mRightCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFramePortrait.type(),mRightCarColorScalar);
 				mRightCarColorImage = Bitmap.createBitmap(mRightCarColor.cols(), mRightCarColor.rows(), Bitmap.Config.ARGB_8888);
 				Utils.matToBitmap(mRightCarColor, mRightCarColorImage);				
 				mFoundColors.clear();
 				if(mColorsOnRightLane)
 					Log.i("debug", "color on rightlane");
 				return mRightCarColorImage;
 			}
 		}
 		return null;		
 	}
 	
 	public Bitmap generate_track_overlay() {
 		// Load an Image to try operations on local stored files
 		mFoundSeparatorLine = false;
 		mFoundUpperLine = false;
 		mFoundLowerLine = false;
 		mGray = new Mat();
 		mEdges = new Mat();
 		mHoughLines = new Mat();
 		mEmptyTrack = new Mat();
 		mStaticImage = Highgui
 				.imread(Environment
 						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
 						+ "track.jpg");
 
 		track_overlay = new Mat(new Size(mInputFramePortrait.cols(), mInputFramePortrait
 				.rows()), mInputFramePortrait.type(), new Scalar(0, 0, 0,
 				0));
 		Log.i("debug", "Channels: " + track_overlay.channels());
 
 		Imgproc.cvtColor(mInputFramePortrait, mGray, Imgproc.COLOR_RGBA2GRAY);
 
 		double[] grays = null;
 		double wert;
 		double count = 0;
 		double divider = 0;
 		for (int i = 0; i < mGray.rows(); i++) {
 			for (int j = 0; j < mGray.cols(); j++) {
 				if (grays != null)
 					grays = null;
 				grays = mGray.get(i, j);
 				wert = grays[0];
 
 				divider++;
 				count = count + wert;
 			}
 		}
 		avg_gray = (int) (count / divider);
 		Log.i("debug", "avg_gray: " + avg_gray);
 
 		mLowerThreshold = (int) (avg_gray * 0.66);
 		mUpperThreshold = (int) (avg_gray * 1.33);
 
 		Imgproc.Canny(mGray, mEdges, mLowerThreshold, mUpperThreshold);
 		//mGray.release();
 		// Highgui.imwrite("/houghlines.png", mHoughLines);
		Log.i("debug","Status Sd-Karte: "+ Environment.getExternalStorageState());
		mStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
 
 //		mCannyEdgeImage = new File(mStorageDir, "canny.bmp");
 //		mPath = mCannyEdgeImage.toString();
 //		Boolean bool = Highgui.imwrite(mPath, mEdges);
 //		if (bool)
 //			Log.i("debug", "SUCCESS writing canny.bmp to external storage");
 //		else
 //			Log.i("debug", "Fail writing canny.bmp to external storage");
 		
 		if((!mEdges.empty() && !mInputFramePortrait.empty()))
 		{	
 			Imgproc.HoughLinesP(mEdges, mHoughLines, 1, Math.PI / 180,
 					mHLthreshold, mHLminLineSize, mHLlineGap);
 			
 			for (int x = 0; x < mHoughLines.cols(); x++) {
 				double[] vec = mHoughLines.get(0, x);
 				double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
 	
 				if (x1 > mInputFramePortrait.cols() / 2 - 30
 						&& x1 < mInputFramePortrait.cols() / 2 + 30
 						&& x2 > mInputFramePortrait.cols() / 2 - 30
 						&& x2 < mInputFramePortrait.cols() / 2 + 30) {					
 					Point start = new Point(x1, y1);
 					Point end = new Point(x2, y2);				
 					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);
 					mSeparatorPoint = new Point(x1,y1);
 					mDrawedLinesOnFrame = true;
 					mFoundSeparatorLine = true;						
 				}
 				else if(x1 < 50 && x2 < 50)
 				{				
 					Point start = new Point(x1, y1);
 					Point end = new Point(x2, y2);				
 					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);	
 					mUpperPoint = new Point(x1,y1);
 					mDrawedLinesOnFrame = true;
 					mFoundUpperLine = true;
 				}
 				else if(x1 > mInputFramePortrait.cols() - 50 && x2 > mInputFramePortrait.cols() - 50)
 				{				
 					Point start = new Point(x1, y1);
 					Point end = new Point(x2, y2);				
 					Core.line(track_overlay, start, end,new Scalar(0, 0, 255, 255), 3);			
 					mLowerPoint = new Point(x1,y1);
 					mDrawedLinesOnFrame = true;
 					mFoundLowerLine = true;
 				}
 			}
 			
 			mTrackOverlay = Bitmap.createBitmap(track_overlay.cols(),
 					track_overlay.rows(), Bitmap.Config.ARGB_8888);
 			Utils.matToBitmap(track_overlay, mTrackOverlay);
 			
 			mInputFramePortrait.copyTo(mEmptyTrack); 	
 		}
 		//mHoughLinesImage = new File(mStorageDir, "houghed.png");
 		//mPath = mHoughLinesImage.toString();
 		//Highgui.imwrite(mPath, track_overlay);
 		
 		
 		//Getting average color for every lane
 		
 		
 		
 		double[] rgba = null;
 		double R, G, B, A;
 		double RCount = 0;
 		double GCount = 0;
 		double BCount = 0;
 		double ACount = 0;
 		double avgR = 0;
 		double avgG = 0;
 		double avgB = 0;
 		double avgA = 0;
 		
 		//Upper lane		
 //		if(mFoundUpperLine){				
 //			for(int i = ((int)mUpperPoint.y+10); i < mSeparatorPoint.y-10; i++){
 //				for (int j = 0;j < mEmptyTrack.cols();j++){
 //				if (rgba != null)
 //					rgba = null;
 //				rgba = mEmptyTrack.get(i, j);
 //				R = rgba[0];
 //				G = rgba[1];
 //				B = rgba[2];	
 //				A = rgba[3];
 //				divider++;
 //				RCount += R;
 //				GCount += G;
 //				BCount += B;
 //				ACount += A;						
 //				}
 //			}	
 //			
 //			mUpperLaneColor = new Scalar(avgR,avgG,avgB,avgA);
 //			avgR = RCount /divider;
 //			avgG = GCount /divider;
 //			avgB = BCount/divider;
 //			avgA = ACount/ divider;
 //			Log.i("debug", "Start: "+mUpperPoint.y);
 //			Log.i("debug", "Ende: "+mSeparatorPoint.y);
 //			Log.i("debug", "UPPER LANE: ");
 //			Log.i("debug", "avg R: "+avgR);
 //			Log.i("debug", "avg G: "+avgG);
 //			Log.i("debug", "avg B: "+avgB);
 //			Log.i("debug", "avg Alpha: "+avgA);	
 //			
 //		}
 //		//Lower lane
 //		//TODO Null Pointer Exception when line was found before --> but other lines not and then when this line not found this option is still true from before
 //		if(mFoundLowerLine){				
 //			for(int i = ((int)mLowerPoint.y-10); i > mSeparatorPoint.y+10; i--){
 //				for (int j = 0;j < mEmptyTrack.cols();j++){
 //				if (rgba != null)
 //					rgba = null;
 //				rgba = mEmptyTrack.get(i, j);
 //				R = rgba[0];
 //				G = rgba[1];
 //				B = rgba[2];	
 //				A = rgba[3];
 //				divider++;
 //				RCount += R;
 //				GCount += G;
 //				BCount += B;
 //				ACount += A;						
 //				}
 //			}	
 //					
 //			mLowerLaneColor = new Scalar(avgR,avgG,avgB,avgA);
 //			avgR = RCount /divider;
 //			avgG = GCount /divider;
 //			avgB = BCount/divider;
 //			avgA = ACount/ divider;
 //			Log.i("debug", "Start: "+mLowerPoint.y);
 //			Log.i("debug", "Ende: "+mSeparatorPoint.y);
 //			Log.i("debug", "LOWER LANE: ");
 //			Log.i("debug", "avg R: "+avgR);
 //			Log.i("debug", "avg G: "+avgG);
 //			Log.i("debug", "avg B: "+avgB);
 //			Log.i("debug", "avg Alpha: "+avgA);	
 //		}
 		
 		//Core.flip(track_overlay.t(), track_overlay, 1);
 
 		
 		return mTrackOverlay;
 	}
 		
 	public boolean isDifferent(double[] avg_oldColors, double[] avg_newColors){
 		
 		for(int i = 0; i < 4; i++)
 		{
 			if(avg_newColors[i] > avg_oldColors[i] + 50 ||avg_newColors[i] < avg_oldColors[i] - 50)			
 				return true;			
 		}
 		return false;
 	}
 	
 	public int car_status(){
 		int carStatus = 0;
 		if(mColorsOnLeftLane && mColorsOnRightLane)
 			carStatus = BOTH_CAR;
 		else if(mColorsOnLeftLane)
 			carStatus = LEFT_CAR;
 		else if(mColorsOnRightLane)
 			carStatus = RIGHT_CAR;
 		else  
 			carStatus = NO_CAR;
 		Log.i("debug", "carStatus: "+carStatus);
 		return carStatus;
 		}
 	
 	public int getNumberOfCars() {
 		if(mColorsOnLeftLane && mColorsOnRightLane)
 			return 2;
 		else if(mColorsOnLeftLane)
 			return 1;
 		else if(mColorsOnRightLane)
 			return 1;
 		else
 			return 0;
 		
 		
 	}
 
 	public Bitmap[] get_cars_colors (){
 		Log.i("debug", "Into get_car_colors!");		
 		int x_offset_left = getCenterOfLanes()[0]-15;
 		int y_offset_left = (mInputFramePortrait.rows()/2)-15;	
 		int x_offset_right = getCenterOfLanes()[1]-15;
 		int y_offset_right = (mInputFramePortrait.rows()/2)-15;
 		mCarColorImages = new Bitmap[2];				
 		Log.i("debug", "In the middle of get_car_colors!");	
 		mCarColorImages[0] = getColorInOffset(x_offset_left, y_offset_left, LEFT_LANE);
 		mCarColorImages[1] = getColorInOffset(x_offset_right, y_offset_right, RIGHT_LANE);		
 		Log.i("debug", "Tschüss get_car_colors!");	
 		return mCarColorImages;
 		
 //		Mat mDiff = new Mat();		
 //		Mat hsva = new Mat();
 //		Mat hsvb = new Mat();
 //				
 //		Imgproc.cvtColor(mRgba, hsva, Imgproc.COLOR_BGR2HSV, 3);		
 //		//Imgproc.cvtColor(mEmptyTrack, hsvb, Imgproc.COLOR_BGR2HSV, 3);
 //		//Core.absdiff(hsva, hsvb, mDiff);
 //		Imgproc.cvtColor(mDiff, mDiff, Imgproc.COLOR_HSV2BGR);
 //		//Imgproc.cvtColor(mDiff, mDiff, Imgproc.COLOR_BGR2RGB);
 //		
 //		Highgui.imwrite(new File(mStorageDir, "mDiff.png").toString(), mDiff);
 //		Highgui.imwrite(new File(mStorageDir, "hsva.png").toString(), hsva);
 //		Highgui.imwrite(new File(mStorageDir, "hsvb.png").toString(), hsvb);
 //		Highgui.imwrite(new File(mStorageDir, "mRgba.png").toString(), mRgba);
 //		Highgui.imwrite(new File(mStorageDir, "mEmptyTrack.png").toString(), mEmptyTrack);
 	
 
 		
 //		for(int x = x_offset_right;x< x_offset_right +30;x++){
 //			for (int y = y_offset_right;y < y_offset_right+30;y++){
 //				Log.i("debug", "x_offset(col): "+x);
 //				Log.i("debug", "y_offset(row): "+y);
 //				mEmptyTrackPixelColor = mEmptyTrack.get(y, x);
 //				mScannedTrackPixelColor = mRgba.get(y, x);
 //
 //				double min = 256;
 //				double max = -1;
 //				
 //				for(int k = 0; k < 3; k++) {
 //					if(mScannedTrackPixelColor[k] < min)
 //						min = mScannedTrackPixelColor[k];
 //					if(mScannedTrackPixelColor[k] > max)
 //						max = mScannedTrackPixelColor[k];
 //				}
 //				
 //				double min2 = 256;
 //				double max2 = -1;
 //				
 //				for(int k = 0; k < 3; k++) {
 //					if(mEmptyTrackPixelColor[k] < min2)
 //						min2 = mEmptyTrackPixelColor[k];
 //					if(mEmptyTrackPixelColor[k] > max2)
 //						max2 = mEmptyTrackPixelColor[k];
 //				}
 //				
 //				if((max - min) > (max2 - min2) + 10){
 //					mRgba.put(y, x, new double[]{0,0,255,255});
 //					mFoundColors.add(mScannedTrackPixelColor);		
 //				}		
 //			}		
 //		}
 //		int counter_right = 0;
 //		int blue_right = 0;
 //		int yellow_right = 0;
 //		int red_right = 0;
 //		for(double[] d : mFoundColors){
 //			counter_right++;
 //			blue_right += d[0];
 //			yellow_right += d[1];
 //			red_right += d[2];
 //		}
 //		if(counter_right == 0)
 //			mColorsOnRightLane = false;
 //		else{
 //			mColorsOnRightLane = true;
 //			Scalar right_car_color = new Scalar(red_right/counter_right,yellow_right/counter_right, blue_right/counter_right,255);
 //			mRightCarColor = new Mat(getLanesToScanColor()[0].x,getLanesToScanColor()[0].y, mInputFramePortrait.type(),right_car_color);
 //			mRightCarColorImage = Bitmap.createBitmap(mRightCarColor.cols(), mRightCarColor.rows(), Bitmap.Config.ARGB_8888);
 //			Utils.matToBitmap(mRightCarColor, mRightCarColorImage);
 //			mCarColorImages[1] = mRightCarColorImage;
 //			mFoundColors.clear();
 //		}
 		
 //		for(double[] d: foundcolors)
 //		Log.i("debug","Different Colors: "+d[0]+", "+d[1]+", "+d[2]);
 //		mHSV = new Mat();
 //		mLowerRangeImage = new Mat();
 //		mUpperRangeImage = new Mat();
 //		mThreshed = new Mat();
 //		Imgproc.cvtColor(mEmptyTrack, mHSV, Imgproc.COLOR_BGR2HSV);	
 //		double[] rgba = null;
 //		double R, G, B, A;
 //		double RCount = 0;
 //		double GCount = 0;
 //		double BCount = 0;
 //		double ACount = 0;
 //		double avgR = 0;
 //		double avgG = 0;
 //		double avgB = 0;
 //		double avgA = 0;
 //		
 //		double divider = 0;
 //		for (int i = 0; i < mHSV.rows(); i++) {
 //			for (int j = 0; j < mHSV.cols(); j++) {
 //				if (rgba != null)
 //					rgba = null;
 //				rgba = mEmptyTrack.get(i, j);
 //				R = rgba[0];
 //				G = rgba[1];
 //				B = rgba[2];	
 //				A = rgba[3];
 //				divider++;
 //				RCount += R;
 //				GCount += G;
 //				BCount += B;
 //				ACount += A;
 //								
 //			}
 //	
 //		}	
 //		avgR = RCount /divider;
 //		avgG = GCount /divider;
 //		avgB = BCount/divider;
 //		avgA = ACount/ divider;
 //		Log.i("debug", "avg R: "+avgR);
 //		Log.i("debug", "avg G: "+avgG);
 //		Log.i("debug", "avg B: "+avgB);
 //		Log.i("debug", "avg Alpha: "+avgA);	
 //
 //		
 //		Core.inRange(mHSV, new Scalar(0,0,0), new Scalar(avgR-10,1,1), mLowerRangeImage);
 //		Core.inRange(mHSV, new Scalar(avgR+10,0,0), new Scalar(255,1,1), mUpperRangeImage);
 //		Core.add(mLowerRangeImage, mUpperRangeImage, mThreshed);
 //		
 //		mTrackColor = new File(mStorageDir, "track.png");
 //		mPath = mTrackColor.toString();
 //		Boolean bool = Highgui.imwrite(mPath, mThreshed);
 //		if (bool)
 //			Log.i("debug", "SUCCESS writing track.png to external storage");
 //		else
 //			Log.i("debug", "Fail writing track.png to external storage");
 //		
 		
 		//Hier muss ein Bild mit den Fahrzeugen auf dem Streckenausschnitt ankommen
 		//Dann werden die Positionen gesetzt.
 		
 		
 	}
 
 }
