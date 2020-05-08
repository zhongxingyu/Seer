 package fr.eyal.datalib.sample.netflix.data.model.movieimage;
 
 import java.lang.ref.SoftReference;
 
 import android.content.Context;
 import android.content.OperationApplicationException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.os.RemoteException;
 import fr.eyal.lib.data.model.ResponseBusinessObject;
 import fr.eyal.lib.data.service.model.ComplexOptions;
 import fr.eyal.lib.data.service.model.DataLibRequest;
 import fr.eyal.lib.util.FileManager;
 
 
 public class MovieImageBase implements ResponseBusinessObject {
 
 	@SuppressWarnings("unused")
     private static final String TAG = MovieImageBase.class.getSimpleName();
 
     protected static String CACHE_DIRECTORY = "movieimage";
 
 	/**
 	 * A soft reference to the Bitmap
 	 */
 	public SoftReference<Bitmap> image;
 	
 	/**
 	 * The last {@link BitmapFactory.Options} used to load the bitmap
 	 */
 	public BitmapFactory.Options lastOptions;
 	
 	/**
 	 * The image file path
 	 */
 	public String imagePath;
 
 	protected FileManager mFileManager = null;
 
 	
     public MovieImageBase() {
         super();
     }
 
     /**
      * Constructor to build the image 
      * 
      * @param fingerprint
      */
     public MovieImageBase(String fingerprint, ComplexOptions complexOptions) {
         super();
         loadFromCache(fingerprint, complexOptions);
     }
 
 
     /**
      * PARCELABLE MANAGMENT
      */
 
 	public static final Parcelable.Creator<MovieImageBase> CREATOR = new Parcelable.Creator<MovieImageBase>() {
 	    @Override
 	    public MovieImageBase createFromParcel(final Parcel in) {
 	        return new MovieImageBase(in);
 	    }
 	
 	    @Override
 	    public MovieImageBase[] newArray(final int size) {
 	        return new MovieImageBase[size];
 	    }
 	};
 	
 	@Override
 	public int describeContents() {
 	    return 0;
 	}
 
 	@Override
 	public void writeToParcel(final Parcel dest, final int flags) {
		if(image != null)
			dest.writeParcelable(image.get(), flags);
		else
			dest.writeParcelable(null, flags);
 	}
 
 	public MovieImageBase(final Parcel in) {
 		image = new SoftReference<Bitmap>((Bitmap) in.readParcelable(Bitmap.class.getClassLoader()));
 	}    
 
     @Override
     public void save(final DataLibRequest request) throws RemoteException, OperationApplicationException {
 		if((mFileManager = FileManager.getInstance()) == null)
     		return;
 
     	String extension = FileManager.getFileExtension(request.url);
 		String name = request.getFingerprint();
 
     	imagePath = mFileManager.saveInInternalCache(CACHE_DIRECTORY, name, extension, image.get(), 100);
     }
 
 	/**
      * Load the associated cached file thanks to its request's fingerprint
      * 
      * @param fingerprint
      */
     protected void loadFromCache(String fingerprint, ComplexOptions complexOptions){
     	if((mFileManager = FileManager.getInstance()) == null)
     		return;
 		
     	//we get the bitmap options
     	BitmapFactory.Options options;
     	if(complexOptions != null)
     		options = (BitmapFactory.Options) complexOptions.getBitmapOptions();
     	else
     		options = new BitmapFactory.Options();
     	
     	//we get the bitmap from a cache file
     	Bitmap bmp = mFileManager.getPictureFromInternalCache(CACHE_DIRECTORY, fingerprint, options);
     	if(bmp != null)
     		image = new SoftReference<Bitmap>(bmp);
     	else
     		image = null;
     	
     	//we store the options after treatment
     	lastOptions = options;
     	//we store the image path file for futur use
 		imagePath = mFileManager.getPathFromInternalCache(CACHE_DIRECTORY, fingerprint);
     }
 }
 
