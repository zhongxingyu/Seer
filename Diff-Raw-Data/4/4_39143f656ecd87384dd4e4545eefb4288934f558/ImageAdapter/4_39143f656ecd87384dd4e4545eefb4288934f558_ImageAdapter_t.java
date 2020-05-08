 package app.xzone.storyline.adapter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.sax.StartElementListener;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import app.xzone.storyline.GalleryPhotosActivity;
 import app.xzone.storyline.HomeActivity;
 import app.xzone.storyline.MapPlaceActivity;
 import app.xzone.storyline.R;
 import app.xzone.storyline.component.ProgressCustomDialog;
 import app.xzone.storyline.helper.Helper;
 import app.xzone.storyline.model.Event;
 import app.xzone.storyline.model.Story;
 
 public class ImageAdapter {
 	private Context context;
 	private static ImageAdapter imageAdapter;
 	private static File imageFolder;
 	private static String photoDir = "photos";
 	private static String facebookDir = "facebook";
 	private static File sdcardDir = Environment.getExternalStorageDirectory();
 	private static DecimalFormat formatter = new DecimalFormat("000");
 
 	private static String rootPath;
 
 	public ImageAdapter(Context context) {
 		this.context = context;
 
 	}
 
 	public ImageAdapter getInstance(Context context) {
 		if (context == null) {
 			imageAdapter = new ImageAdapter(context);
 		}
 		return imageAdapter;
 	}
 
 	public static void takeCamera(Activity a) {
 		rootPath = sdcardDir + File.separator
 				+ AppAdapter.getInstance(a).getAppName();
 		String photoPath = rootPath + File.separator + photoDir;
 
 		Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		// Create Directory App
 		imageFolder = new File(photoPath);
 		imageFolder.mkdirs();
 		a.startActivityForResult(camera, Helper.REQUEST_CODE_IMAGE_CAMERA);
 	}
 
 	public static void takeGallery(Activity a) {
 		rootPath = sdcardDir + File.separator
 				+ AppAdapter.getInstance(a).getAppName();
 		String photoPath = rootPath + File.separator + photoDir;
 
 		Intent i = new Intent(Intent.ACTION_PICK,
 				MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 		// Create Directory App
 		imageFolder = new File(photoPath);
 		imageFolder.mkdirs();
 		a.startActivityForResult(i, Helper.REQUEST_CODE_IMAGE_GALLERY);
 	}
 
 	public static void copyFile(Bitmap bmp, Event event, Context context)
 			throws IOException {
 		int imageNum = 1;
 
 		String photoPath = rootPath + File.separator + photoDir;
 		String fileName = "image_" + String.valueOf(formatter.format(imageNum))
 				+ ".jpg";
 
 		// Create Directory App
 		imageFolder = new File(photoPath);
 		imageFolder.mkdirs();
 
 		File output = new File(photoPath, fileName);
 		while (output.exists()) {
 
 			fileName = "image_" + String.valueOf(formatter.format(++imageNum))
 					+ ".jpg";
 			output = new File(imageFolder, fileName);
 
 		}
 		FileOutputStream out = new FileOutputStream(output);
 		bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
 		out.flush();
 		out.close();
 
 		savePhoto(fileName, event, new DBAdapter(context));
 
 		Toast customToast = new Toast(context);
 		customToast = Toast.makeText(context, "Image Transferred",
 				Toast.LENGTH_LONG);
 		customToast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
 		customToast.show();
 	}
 
 	public static boolean savePhoto(String photo, Event e, DBAdapter db) {
 		
 		String fphotos = e.getPhotos();
 		if(fphotos.equals("")){ fphotos = photo; }
 		else{ fphotos += ";"+photo; }
 		
 		e.setPhotos(fphotos);
 
 		return db.savePhoto(e);
 	}
 
 	public static String[] getPhotosPath(Event e){
 		String[] paths = e.getPhotos().split(";"); 
 		if(!e.getPhotos().equals("")){
 			if(paths.length == 0){
 				return new String[]{e.getPhotos()};
 			}else{
 				return paths;
 			}
 		}
 		
 		return null;
 	}
 	
 	public static Bitmap decodeUri(Context c, Uri uri, final int requireSize)
 			throws FileNotFoundException {
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 
 		BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri),
 				null, o);
 
 		int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		int scale = 1;
 		while (true) {
 			if (width_tmp / 2 < requireSize || height_tmp / 2 < requireSize)
 				break;
 			width_tmp /= 2;
 			height_tmp /= 2;
 			scale *= 2;
 		}
 
 		BitmapFactory.Options o2 = new BitmapFactory.Options();
 		o2.inSampleSize = scale;
 		return BitmapFactory.decodeStream(c.getContentResolver()
 				.openInputStream(uri), null, o2);
 	}
 
 	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
 		int width = bm.getWidth();
 		int height = bm.getHeight();
 		float scaleWidth = ((float) newWidth) / width;
 		float scaleHeight = ((float) newHeight) / height;
 
 		// create a matrix for the manipulation
 		Matrix matrix = new Matrix();
 
 		// resize the bit map
 		matrix.postScale(scaleWidth, scaleHeight);
 
 		// recreate the new Bitmap
 		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
 				matrix, false);
 
 		return resizedBitmap;
 	}
 
 	public static String getPath(Uri uri, Activity a) {
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = a.managedQuery(uri, projection, null, null, null);
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 	
 	public static Bitmap[] getPhotosEvent(Event event){
 		if(event.equals(null)) return null;
 		
		
 		String[] fnames = event.getPhotos().split(";");
		Bitmap[] bmps = new Bitmap[fnames.length];
 		if(fnames.equals("")) fnames = new String[]{event.getPhotos()};
 		
 		int idx = 0;
 		for(String fname : fnames){
 			File imageFile = new File("/sdcard/Storyline/photos/"+fname);
 			Bitmap photo = BitmapFactory.decodeFile(	imageFile.getAbsolutePath() 	);
 			bmps[idx] = photo;
 					
 			idx++;
 		}
 		return bmps;
 	}
 	
 	public static void buildPhotosLayout(Event event, View vi, Activity a){
 		String[] fnames = event.getPhotos().split(";");
 		if(fnames.equals("")) fnames = new String[]{event.getPhotos()};
 		
 		for(String fname : fnames){
 			
 			File imageFile = new File("/sdcard/Storyline/photos/"+fname);
 			
 			Bitmap photo = BitmapFactory.decodeFile(	imageFile.getAbsolutePath() 	);
 			setPhotoLayout(photo, vi, event, a);
 		}
 		
 	}
 	
 	public static void setPhotoLayout(Bitmap photo, View vi, final Event event, final Activity a) {
 		// initialize image container
 		View bubbleEventHeader = (LinearLayout) vi.findViewById(R.id.bubble_event_header);
 		LinearLayout bubbleImage = (LinearLayout) bubbleEventHeader.findViewById(R.id.bubble_image);
 		
 		if(bubbleEventHeader.getVisibility() == View.GONE){
 			bubbleEventHeader.setVisibility(View.VISIBLE); 
 		}
 		
         
         Bitmap bmp = ImageAdapter.getResizedBitmap(photo, 56, 70);
         
         ImageView imageEvent = new ImageView(a);
         imageEvent.setImageBitmap(bmp);
         imageEvent.setPadding(5, 1, 5, 3);
         bubbleImage.addView(imageEvent, 0);
         
         // go to gallery photos
         imageEvent.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(a, GalleryPhotosActivity.class);
 				
 				intent.putExtra("app.event.photos", event);
 				a.startActivity(intent);
 			}
 		});
         
 	}
 }
