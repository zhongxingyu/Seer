 package ca.ualberta.cmput301w13t11.FoodBook;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import ca.ualberta.cmput301w13t11.FoodBook.controller.DbController;
 import ca.ualberta.cmput301w13t11.FoodBook.model.DbManager;
 import ca.ualberta.cmput301w13t11.FoodBook.model.FView;
 import ca.ualberta.cmput301w13t11.FoodBook.model.Photo;
 
 
 /**
  * This class only shows the recipe's pictures in a gallery. Capturing or deleting is not allowed.
  * Images are enlarged by clicking on them (starts FullImageViewPhotosActivity.java). 
  * @author jaramill
  * 
  * ! We have to make the upload button work you guys, or delete the button. -Pablo
  *
  */
 public class ViewPhotosActivity extends Activity implements FView<DbManager>
 {
 	//intent 'handshakes'
 	static final String EXTRA_URI = "extra_uri";				//recipe uri, incoming
 	static final String EXTRA_IMG_ID = "extra_img_id";		//image id (timestamp), incoming
 	static final String EXTRA_IMG_PATH = "extra_img_path";	//image path, incoming
 	public static String CALLER = "caller";
 	private boolean queryResultsDb = false;
 	private long uri = 0; 							// photos (no bitmap data in them)	
 	private ArrayList<Photo> photos;
 
 	private GridView gridview = null;					//3 x ? gridview for photo icons
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_photos);
 		String caller = "";
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		if (extras != null) {
 			
 			caller = extras.getString(CALLER);
 			if (caller.equals("ViewSearchResultActivity")) {
 				queryResultsDb = true;
 			}
 		}
 		uri = intent.getLongExtra(EXTRA_URI, 0);
 		Log.d("ViewPhotosActivity in onCreate()", "uri passed to activity = " + uri);
 		this.updateView();
 
 	}
 	
 	/**
 	 * This method updates the gallery images. 
 	 * It is called on creation of the activity and by the update method (called by the database manager).
 	 */
 	protected void updateView(){
 
 		DbController DbC = DbController.getInstance(this, this);
 		if (queryResultsDb)
 			photos = DbC.getStoredRecipePhotos(uri);
 		else
 			photos = DbC.getRecipePhotos(uri);
 		if (photos.isEmpty()) {
 			Log.d("ViewPhotosActivity updateView():", "photos is empty");
 		}
 		
 		if (!photos.isEmpty()) {
 			gridview = (GridView) findViewById(R.id.gridView1);
 			gridview.setAdapter(new ImageAdapter(this));    
 			gridview.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View v,
 						int position, long id) {
					Intent i = new Intent(ViewPhotosActivity.this, FullImageViewPhotosActivity.class);
 					Bundle bundle = new Bundle();
 					bundle.putString(EXTRA_IMG_ID, photos.get(position).getId());
 					bundle.putString(EXTRA_IMG_PATH, photos.get(position).getPath());
 					bundle.putLong(EXTRA_URI, uri);
 					//Log.d("recipe", Long.toString(uri));
 					//Log.d("filename", photos.get(position).getName());
 					i.putExtras(bundle);
 					startActivity(i);
 				}
 			});
 		} 
 	}
 
 
 	/**
 	 * This class helps produce a gallery (grid view) of small images which can be clicked and enlarged.
 	 * The BaseAdapter class is extended accordingly.
 	 */
 	public class ImageAdapter extends BaseAdapter {
 		@SuppressWarnings("all")
 
 		private Context mContext;
 
 		public ImageAdapter(Context c) {
 			mContext = c;
 		}
 
 		public int getCount() {
 			return photos.size();
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		// create a new ImageView for each item referenced by the Adapter
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			 //galchild -gallery child-is layout with a a 100x100 dp image view
 			v = vi.inflate(R.layout.galchild, null);
 
 			try {
 				ImageView imageView = (ImageView) v.findViewById(R.id.imageView1);
 				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
 				imageView.setPadding(8, 8, 8, 8);
 				Bitmap bmp = decodeURI(photos.get(position).getPath());
 				imageView.setImageBitmap(bmp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return v;
 		}
 	}
 	
 	/**
 	 *  This method retrieves a bitmap image  stored in a compressed format.
 	 * @param filePath- complete path where the actual compressed image (jpg, pgn, etc) is stored 
 	 * @return a possibly scaled down bitmap image 
 	 */
 	public Bitmap decodeURI(String filePath){
 
 		Options options = new Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(filePath, options);
 
 		// Only scale if we need to 
 		// (16384 buffer for img processing)
 		Boolean scaleByHeight = Math.abs(options.outHeight - 100) >= Math.abs(options.outWidth - 100);
 		if(options.outHeight * options.outWidth * 2 >= 16384){
 			// Load, scaling to smallest power of 2 that'll get it <= desired dimensions
 			double sampleSize = scaleByHeight
 					? options.outHeight / 100
 							: options.outWidth / 100;
 			options.inSampleSize = 
 					(int)Math.pow(2d, Math.floor(
 							Math.log(sampleSize)/Math.log(2d)));
 		}
 
 		// Do the actual decoding
 		options.inJustDecodeBounds = false;
 		options.inTempStorage = new byte[512];  
 		Bitmap output = BitmapFactory.decodeFile(filePath, options);
 
 		return output;
 	}
 	/**
 	 *  Returns the user to the previous screen, called on pressing the Go Back button.
 	 * @param View - The view calling this method.
 	 */
 	public void OnGoBack(View View)
 	{
 		// responds to button Go Back
 		ViewPhotosActivity.this.finish();
 	}
 	
 	/**
 	 *  Called by the database manager to update views, part of MVC
 	 */
 	@Override
 	public void update(DbManager db)
 	{
 		// TODO Auto-generated method stub
 		//create a new adapter
 		this.updateView();
 
 	}
 	public void onDestroy()
 	{	super.onDestroy();
 	DbController DbC = DbController.getInstance(this, this);
 	DbC.deleteView(this);
 	}
 
 
 }
