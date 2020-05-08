 package com.hipsterrific.FaceDetector;
 
 import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
 import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.util.Log;
 
 import com.googlecode.javacv.cpp.opencv_core.CvRect;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import com.googlecode.javacv.cpp.opencv_objdetect.CascadeClassifier;
 import com.hipsterrific.FaceDetector.FaceDetector.ClassifierType;
 
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import static com.googlecode.javacv.cpp.opencv_highgui.*;
 
 public class Face {
 
 	public enum FeatureType {
 		EYES,
 		MOUTH,
 		LEFT_EAR,
 		RIGHT_EAR
 	}
 	
 	public FaceDetector detector;
 	public IplImage image;
 	public Rect rect;
 	public HashMap<FeatureType, FaceFeature> features;
 	public List<OverlaySpec> overlays;
 	
 	public Face(FaceDetector detector, Rect rect) throws Exception {
 		this.detector = detector;
 		this.rect = rect;
 		this.image = detector.image;
 		cvSetImageROI(this.image, IplImageHelper.cvRectFromRect(rect));
 		this.features = new HashMap<FeatureType, FaceFeature>();
 		this.overlays = new ArrayList<OverlaySpec>();
 	}
 	
 	public boolean detectFeatures() throws Exception {
 		//featureRects is actually a pointer, but because of JavaCV's implementation of ClassifierCascade.detectMultiscale it is treated as a CvRect
 		// (which basically is a pointer as well)
 		CvRect featureRects = new CvRect(null);
 		
 		HashMap<FeatureType, ClassifierType> featureClassifiers = new HashMap<FeatureType, ClassifierType>();
 		
 		featureClassifiers.put(FeatureType.EYES,      ClassifierType.EYES);
 		featureClassifiers.put(FeatureType.MOUTH,     ClassifierType.MOUTH);
 
 		//20% for a feature is quite big, let's change it to about 5% of the picture.
		int featureSize = Math.round(image.height() * 0.20f);
 		
 		this.features.clear();
 		
 		for (HashMap.Entry<FeatureType, ClassifierType> entry : featureClassifiers.entrySet()) {
 		    FeatureType featureType = entry.getKey();
 		    ClassifierType classifierType = entry.getValue();
 		    
 			CascadeClassifier featureClassifier = this.detector.classifiers.get(classifierType);
 			featureClassifier.detectMultiScale(
 					this.image, // image
 					featureRects, // objects
 					1.2, // scaleFactor
 					5,  // minNeighbours
 					CV_HAAR_DO_CANNY_PRUNING, // flags 
 					new CvSize(featureSize, featureSize), // minSize 
 					new CvSize(this.image.width(), this.image.height())); // maxSize
 
 			int total = featureRects.capacity();
 			
 			Log.i("Face", "Found " + total + " " + featureType + " feature(s)");
 			
 			if (featureRects.capacity() >= 1) {
 				// We're assuming the first result is the correct one, but really we should check the confidence in some way.
 				CvRect cvRect = featureRects.position(0);
 				Rect rect = IplImageHelper.rectFromCvRect(cvRect);
 				for(int i = 0; i < total; i ++){
					Log.i("Features", "Feature " + featureType + " "+i+" is found in rect " + rect.flattenToString());
 				}
 				
 				FaceFeature feature = new FaceFeature(this, rect);
 				this.features.put(featureType, feature);
 			}
 		}
 		
 		return true;
 	}
 	
 	public void render(Canvas canvas) {
 		for (OverlaySpec overlay : overlays) {
 			try {
 				overlay.render(this, canvas);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
