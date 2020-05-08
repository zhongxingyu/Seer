 package cz.muni.muniGroup.cookbook.activities;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.Toast;
 import cz.muni.muniGroup.cookbook.R;
 import cz.muni.muniGroup.cookbook.entities.WithIcon;
 import cz.muni.muniGroup.cookbook.managers.ImageDownloader;
 
 public class GetImageTask extends AsyncTask<Integer, Void, Bitmap>
 {
 
 	private Context context;
 	private static final String TAG = "GetImageTask";
 	private final ImageView imageView;
 	private final String path;
 	private WithIcon withIcon;
 	
 
 	public GetImageTask(Context context, ImageView imageView, String path, WithIcon withIcon)
 	{
 		this.context = context;
 		this.imageView = imageView;
 		this.path = path;
 		this.withIcon = withIcon;
 	}
 
 
 	@Override
 	protected Bitmap doInBackground(Integer... ints)
 	{
         String cardPath = null;
         Bitmap bitmap = null;
         
 		// je pametova karta k dispozici?
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
 	    {
 			// u je na kart? 
 			cardPath = context.getExternalFilesDir(null).getAbsolutePath() + "/" + path;
 			Log.i(TAG, "pametova karta je k dispozici. Cesta: " + cardPath);
         	bitmap = BitmapFactory.decodeFile(cardPath);
 	    }
 		if (bitmap == null){
         	bitmap = ImageDownloader.DownloadImage(ImageDownloader.URL + path);
 		}
		if (bitmap != null){
 			String imageName = cardPath.substring(cardPath.lastIndexOf("/")+1);
 			String dir = cardPath.substring(0, cardPath.lastIndexOf("/")-1);
         	ImageDownloader.saveImage(bitmap, dir, imageName);
 		}
 		return bitmap;
 
 	}
 
 
 	protected void onPostExecute(Bitmap result)
 	{
 		if (result == null){
 			// Toast.makeText(context, R.string.connectionProblem, Toast.LENGTH_SHORT).show();
 			Log.i(TAG, "Obrzek se nepodailo nast.");
 		} else {
 			withIcon.setIcon(result);
 			imageView.setImageBitmap(result);
 		}
 		
 	}
 
 
 }
