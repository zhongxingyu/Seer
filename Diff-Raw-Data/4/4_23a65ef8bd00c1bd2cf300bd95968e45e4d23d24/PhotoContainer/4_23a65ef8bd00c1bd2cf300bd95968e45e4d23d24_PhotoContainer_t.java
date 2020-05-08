 package Common;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.util.Log;
 
 public class PhotoContainer {
 
 	private static final String TAG = PhotoContainer.class.getName();
 	private static final PhotoContainer instance = new PhotoContainer();
 
 	private List<Photo> processedPhotos = new ArrayList<Photo>();
 	private BlockingQueue<Photo> buffer = new LinkedBlockingQueue<Photo>();
 
 	private PhotoContainer() {
 	}  
 
 	public static PhotoContainer getInstance() {
 		return instance;
 	}
 
 	public List<Photo> getProcessedPhotos() {
 		return instance.processedPhotos;
 	}
 
 	public Photo getNextPhotoFromBuffer() {
 		return buffer.remove();
 	}
 
 	public void addToBuffer(Photo p) {
 		if (!buffer.contains(p)) {
 			try {
 				buffer.put(p);
 			} catch (InterruptedException e) {
 				Log.e(TAG, "Error adding new photo to container");
 			}
 		}
 	}
 
 	public void moveToProcessedPhotos(Photo photo) {
		if (!processedPhotos.contains(photo)) {
			processedPhotos.add(photo);
		}
 	}
 
 	public void onDelete(String deleted) {
 		// scan queues
 		for (Photo photo : processedPhotos) {
 			if (photo.getFilePath().equals(deleted)) {
 				processedPhotos.remove(photo);
 			}
 		}
 
 		for (Photo photo : buffer) {
 			if (photo.getFilePath().equals(deleted)) {
 				buffer.remove(photo);
 			}
 		}
 	}
 
 	public boolean isEmpty() {
 		return buffer.isEmpty();
 	}
 
 	public void clearProcessPhotos() {
 		instance.processedPhotos.clear();
 	}
 
 }
