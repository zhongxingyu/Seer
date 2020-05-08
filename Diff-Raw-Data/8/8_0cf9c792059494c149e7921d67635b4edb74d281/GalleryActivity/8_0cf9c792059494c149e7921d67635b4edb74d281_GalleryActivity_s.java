 package c301.AdventureBook;
 
 import java.util.ArrayList;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 
 public class GalleryActivity extends Activity{
 
 	private static final int PHOTO_ACTIVITY_REQUEST = 1001;
 	private static final int MENU_DELETE_ID = 1002;
 	private int currentId;
 	
 	StoryManager sManagerInst;
 	Page currentPage;
 	Story currentStory;
 	GridView gridview;
 	Button mButtonAddPic;
 	Button mButtonReturn;
 	Typeface font;
 	ArrayList<String> imageBytes;
 	
 	private String show_path;
 	private String imageByte;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gallery);
 
 		sManagerInst = StoryManager.getInstance();
 		currentPage = sManagerInst.getPage();
 		currentStory = sManagerInst.getCurrentStory();
 		imageBytes = currentPage.getImageBytes();
 
 		gridview = (GridView) findViewById(R.id.gridview);
 		gridview.setAdapter(new ImageAdapter(this, imageBytes));
 		registerForContextMenu(gridview);
 		mButtonAddPic = (Button) findViewById(R.id.add_pic);
 		mButtonReturn = (Button) findViewById(R.id.return_from_gallery);
 		font = Typeface.createFromAsset(getAssets(), "fonts/straightline.ttf");
 		mButtonAddPic.setTypeface(font);  
 		mButtonReturn.setTypeface(font);  
 
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				//can implement full screen picture here
 				//Toast.makeText(GalleryActivity.this, "" + position, Toast.LENGTH_SHORT).show();
 			}
 		}); 
 		mButtonAddPic.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Intent intent = new Intent(GalleryActivity.this,
 						TakePhotoActivity.class);
 				startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST);
 			}
 		});
 		mButtonReturn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				finish();
 			}
 		});
 	}
 	
 	@Override
 	//test image
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == PHOTO_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
 			show_path = data.getStringExtra("path");
 			imageByte = data.getStringExtra("imagebyte");
 			currentPage.addImageByte(imageByte);
 			currentStory.replacePage(currentPage);
 			sManagerInst.setCurrentPage(currentPage);
 			sManagerInst.saveStory(currentStory, true);
 		}
 	}
 	@Override
 	//create the keep touching menu for delete 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 				ContextMenuInfo menuInfo) {
 			// TODO Auto-generated method stub
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			currentId = (int)info.id;
 			menu.add(0,MENU_DELETE_ID,0,"Delete");
 	}
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		if (item.getItemId() == MENU_DELETE_ID) {
 			currentPage.removeImageByte(currentId);
 			gridview.setAdapter(new ImageAdapter(this, imageBytes));
 			
 
 		}
 		return super.onContextItemSelected(item);
 	}
 	public void onResume()
 	{  // After a pause OR at startup
 		super.onResume();
 		//Refresh your stuff here
 		imageBytes = currentPage.getImageBytes();
 		gridview.setAdapter(new ImageAdapter(this, imageBytes));
 	}
 
 	public class ImageAdapter extends BaseAdapter {
 		private Context mContext;
 		private Bitmap[] bitmaps;
 
 		public ImageAdapter(Context c, ArrayList<String> imageBytes) {
 			this.mContext = c;
 			bitmaps = new Bitmap[imageBytes.size()];
 
 			if (imageBytes.size() == 0) {
 				
 			}
 			
 			for(int i = 0; i < imageBytes.size(); i++){
 				byte[] decodedString = Base64.decode(imageBytes.get(i), Base64.DEFAULT);
 				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 				//index = extractBitmap(reflectionGap, index, decodedByte);
 				bitmaps[i] = decodedByte;
 			}
 		}
 
 		public int getCount() {
 			return bitmaps.length;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		// create a new ImageView for each item referenced by the Adapter
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView imageView;
 			if (convertView == null) {  // if it's not recycled, initialize some attributes
 				imageView = new ImageView(mContext);
 				imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
 				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 				imageView.setPadding(8, 10, 10, 8);
 			} else {
 				imageView = (ImageView) convertView;
 			}
 
 			imageView.setImageBitmap(bitmaps[position]);
 			return imageView;
 		}
 
 	}
 
 }
