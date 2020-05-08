 package com.hipsterrific.FaceDetector;
 
 import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.util.Log;
 
 import com.googlecode.javacpp.Loader;
 import com.googlecode.javacv.cpp.opencv_core.CvRect;
 import com.googlecode.javacv.cpp.opencv_core.CvSize;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 import com.googlecode.javacv.cpp.opencv_objdetect;
 import com.googlecode.javacv.cpp.opencv_objdetect.CascadeClassifier;
 
 public class FaceDetector {
 	public enum ClassifierType {
 		FACE, EYES, NOSE, MOUTH
 	}
 
 	public Bitmap photo;
 	public File photoFile;
 	// Holds the grayscale image
 	protected IplImage image;
 
 	protected HashMap<ClassifierType, CascadeClassifier> classifiers;
 	public List<Face> faces;
 
 	public FaceDetector(File originalPhotoFile, File manipulatedPhotoFile,
 			HashMap<ClassifierType, String> classifierPaths)
 			throws Exception {
 		Loader.load(opencv_objdetect.class);
 
 		String originalPhotoPath = originalPhotoFile.getPath();
 
		this.photo = BitmapHelper.sampledFromPath(originalPhotoPath, 1200, 1200);
 
 		// this.photo has been resized and rotated, so save it.
 		this.photoFile = BitmapHelper.saveToFile(this.photo,
 				manipulatedPhotoFile);
 
 		// We no longer need to scale the image, because the Bmp is already
 		// scaled when it's loaded from the Path (see
 		// decodeSampledBitmapFromPath)
 
 		this.image = ImageHelper.getGrayScaleFor(ImageHelper
 				.fromBitmap(this.photo));
 
 		// We don't need the photo anymore for now. detectFaces() uses
 		// this.image, and processPhoto() will reload the photo.
 		this.photo = null;
 
 		this.faces = new ArrayList<Face>();
 		this.classifiers = new HashMap<ClassifierType, CascadeClassifier>();
 		for (ClassifierType classifierType : ClassifierType.values()) {
 			if (!classifierPaths.containsKey(classifierType)) {
 				throw new Exception(classifierType
 						+ " classifier path is required.");
 			}
 			String classifierPath = classifierPaths.get(classifierType);
 
 			CascadeClassifier classifier = new CascadeClassifier(classifierPath);
 			this.classifiers.put(classifierType, classifier);
 		}
 	}
 
 	/**
 	 * Detect faces and populate {@link #faces} accordingly
 	 * 
 	 * @throws Exception
 	 */
 	public void detectFaces() throws Exception {
 		if (!this.faces.isEmpty()) {
 			return;
 		}
 
 		// faceRects is actually a pointer, but because of JavaCV's
 		// implementation of ClassifierCascade.detectMultiscale it is treated as
 		// a CvRect
 		// (which basically is a pointer as well)
 		CvRect rects = new CvRect(null);
 
 		// this is where the magic happens
 		// To speed up the algorithm, define a minimum faceSize (about 20% of
 		// the image will do?)
 		int size = Math.round(this.image.height() * 0.2f);
 
 		// DetectMultiscale seems faster than cvHaarDetectObjects
 		CascadeClassifier classifier = this.classifiers
 				.get(ClassifierType.FACE);
 		classifier.detectMultiScale(this.image, // image
 				rects, // objects
 				1.2, // scaleFactor
 				5, // minNeighbours
 				CV_HAAR_DO_CANNY_PRUNING, // flags
 				new CvSize(size, size), // minSize
 				new CvSize(this.image.width(), this.image.height())); // maxSize
 
 		this.faces.clear();
 
 		int count = rects.capacity();
 		Log.i("Faces", "Found " + count + " faces!");
 
 		for (int i = 0; i < count; i++) {
 			CvRect cvRect = rects.position(i);
 
 			Rect rect = ImageHelper.rectFromCvRect(cvRect);
 
 			Log.i("Faces",
 					"Face " + i + " found in rect " + rect.flattenToString());
 
 			Face face = new Face(this, rect);
 
 			// If detectFeatures() returns false, no eyes, nose or mouth were
 			// found. This face is useless.
 			if (face.detectFeatures()) {
 				this.faces.add(face);
 			}
 		}
 
 		return;
 	}
 
 	/**
 	 * Processes the Bitmap by calling {@link #Face.render(Canvas)}
 	 * 
 	 * @return The processed Bitmap
 	 */
 	public Bitmap processPhoto() {
 		if (this.photo == null || this.photo.isRecycled()) {
 			this.photo = BitmapHelper.fromFile(this.photoFile);
 		}
 		// after convertToMutable(), this.photo will be recycled (removed from
 		// memory)
 		Bitmap processedPhoto = BitmapHelper.getMutableFor(this.photo);
 		this.photo = null;
 		Canvas canvas = new Canvas(processedPhoto);
 
 		for (Face face : this.faces) {
 			// Cut the Face's part out of the full photo
 			Bitmap faceBitmap = Bitmap.createBitmap(processedPhoto,
 					face.rect.left, face.rect.top, face.rect.width(),
 					face.rect.height());
 			faceBitmap = faceBitmap.copy(faceBitmap.getConfig(), true);
 			Canvas faceCanvas = new Canvas(faceBitmap);
 
 			// Render the Face on canvas, that's connected with the mutable
 			// faceBitmap
 			face.render(faceCanvas);
 
 			// Draw the rendered faceBitmap on the original canvas, that's
 			// connected with the mutable processedPhoto
 			canvas.drawBitmap(faceBitmap, face.rect.left, face.rect.top, null);
 
 			faceCanvas = null;
 			faceBitmap.recycle();
 		}
 		canvas = null;
 
 		return processedPhoto;
 	}
 }
