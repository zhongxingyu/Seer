 package de.raptor2101.GalDroid.Activities.Helpers;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.media.ExifInterface;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.util.Log;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.Listeners.CommentLoaderListener;
 import de.raptor2101.GalDroid.Activities.Listeners.TagLoaderListener;
 import de.raptor2101.GalDroid.WebGallery.GalleryCache;
 import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 import de.raptor2101.GalDroid.WebGallery.Tasks.CommentLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.TagLoaderTask;
 
 public class ImageInformationExtractor {
 	private final static String ClassTag = "ImageInformationExtractor";
 	private final Activity mActivity;
 	private final GalleryCache mCache;
 	private final CommentLoaderListener mCommentLoaderListener;
 	private final TagLoaderListener mTagLoaderListener;
 	private final WebGallery mWebGallery;
 	
 	private TagLoaderTask mTagLoaderTask;
 	private CommentLoaderTask mCommentLoaderTask;
 	
 	public ImageInformationExtractor(Activity activity, GalleryCache cache, WebGallery webGallery, TagLoaderListener tagLoaderListener, CommentLoaderListener commentLoaderListener) {
 		mActivity = activity;
 		mCache = cache;
 		mTagLoaderListener = tagLoaderListener;
 		mCommentLoaderListener = commentLoaderListener;
 		mWebGallery = webGallery;
 	}
 	
 	public void extractImageInformations(GalleryImageView imageView) {
 		if(imageView != null && imageView.isLoaded()) {
 			Log.d(ClassTag, "Starting decoding GalleryObject information");
 			GalleryObject galleryObject = imageView.getGalleryObject();
 			extractObjectInformation(galleryObject);
 			extractExifInformation(galleryObject);
 		} else {
 			clearImageInformations();
 		}
 		
 	}
 	
 	private void clearImageInformations() {
 		TextView textView = (TextView) mActivity.findViewById(R.id.textTitle);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textTags);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textUploadDate);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifCreateDate);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifAperture);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifExposure);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifFlash);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifISO);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifModel);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifModel);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifMake);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifFocalLength);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textExifWhiteBalance);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textGeoLat);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textGeoLong);
 		textView.setText("");
 		
 		textView = (TextView) mActivity.findViewById(R.id.textGeoHeight);
 		textView.setText("");
 	}
 
 	private void extractObjectInformation(GalleryObject galleryObject) {
 
 		TextView textTitle = (TextView) mActivity.findViewById(R.id.textTitle);
 		TextView textUploadDate = (TextView) mActivity.findViewById(R.id.textUploadDate);
 		
 		
 		textTitle.setText(galleryObject.getTitle());
 		textUploadDate.setText(galleryObject.getDateUploaded().toLocaleString());
 		
 		if(mTagLoaderTask != null && (mTagLoaderTask.getStatus() == Status.RUNNING || mTagLoaderTask.getStatus() == Status.PENDING)) {
 			Log.d(ClassTag, "Aborting TagLoaderTask");
 			mTagLoaderTask.cancel(true);
			while(mTagLoaderTask.getStatus() == Status.RUNNING) {
				Thread.yield();
			}
			Log.d(ClassTag, "TagLoaderTask aborted");
 		}
 		
 		if(mCommentLoaderTask != null && (mCommentLoaderTask.getStatus() == Status.RUNNING || mCommentLoaderTask.getStatus() == Status.PENDING)) {
 			Log.d(ClassTag, "Aborting CommentLoaderTask");
 			mCommentLoaderTask.cancel(true);
			while(mCommentLoaderTask.getStatus() == Status.RUNNING) {
				Thread.yield();
			}
			Log.d(ClassTag, "CommentLoaderTask aborted");
 		}
 		
 		mTagLoaderTask = new TagLoaderTask(mWebGallery, mTagLoaderListener);
 		mTagLoaderTask.execute(galleryObject);
 		
 		mCommentLoaderTask = new CommentLoaderTask(mWebGallery, mCommentLoaderListener);
 		mCommentLoaderTask.execute(galleryObject);
 	}
 	
 	private void extractExifInformation(GalleryObject galleryObject) {
 		File cachedFile = mCache.getFile(galleryObject.getImage().getUniqueId());
 		try {
 			ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());
 			
 			
 			TextView textField = (TextView) mActivity.findViewById(R.id.textExifCreateDate);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
 			
 			textField = (TextView) mActivity.findViewById(R.id.textExifAperture);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
 			
 			try {
 				textField = (TextView) mActivity.findViewById(R.id.textExifExposure);
 				float exposureTime = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
 				textField.setText(String.format("1/%.0fs", exposureTime));
 			} catch (Exception e) {
 				
 			}
 			
 			try {
 				textField = (TextView) mActivity.findViewById(R.id.textExifFlash);
 				int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
 				textField.setText(String.format("%d", flash));
 			} catch (Exception e) {
 				
 			}
 			
 			textField = (TextView) mActivity.findViewById(R.id.textExifISO);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_ISO));
 			
 			textField = (TextView) mActivity.findViewById(R.id.textExifModel);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
 			
 			textField = (TextView) mActivity.findViewById(R.id.textExifModel);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
 			
 			textField = (TextView) mActivity.findViewById(R.id.textExifMake);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
 			
 			try {
 				double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
 				textField = (TextView) mActivity.findViewById(R.id.textExifFocalLength);
 				textField.setText(String.format("%.0fmm", focalLength));
 			} catch (Exception e) {
 
 			}
 			
 			try {
 				textField = (TextView) mActivity.findViewById(R.id.textExifWhiteBalance);
 				int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
 				if(whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
 					textField.setText(R.string.object_exif_whitebalance_auto);
 				}
 				else {
 					textField.setText(R.string.object_exif_whitebalance_manual);
 				}
 			} catch (Exception e) {
 				
 			}
 			
 			textField = (TextView) mActivity.findViewById(R.id.textGeoLat);
 			String latitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
 			textField.setText(latitude);
 			
 			textField = (TextView) mActivity.findViewById(R.id.textGeoLong);
 			String longitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
 			textField.setText(longitude);
 			
 			textField = (TextView) mActivity.findViewById(R.id.textGeoHeight);
 			String height = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
 			textField.setText(parseHeight(height));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private String parseDegMinSec(String tagGpsValue) {
 		try {
 			String[] values = tagGpsValue.split("[,/]");
 			float deg = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
 			float min = Float.parseFloat(values[2])/Float.parseFloat(values[3]);
 			float sec = Float.parseFloat(values[4])/Float.parseFloat(values[5]);
 			return String.format("%.0f° %.0f' %.2f\"", deg,min,sec);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 	
 	private String parseHeight(String tagGpsValue) {
 		try {
 			String[] values = tagGpsValue.split("/");
 			float height = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
 			return String.format("%.2fm", height);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 }
