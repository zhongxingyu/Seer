 package com.fyodorwolf.studybudy;
  
 import java.io.File;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
  
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.FailReason;
 import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
 /**
  * This Activity bridges the gap that doesn't allow
  * Android intents to request for more than a single
  * image URI from the user's gallery at a time. 
  * 
  * @author fwolf
  *
  */
 public class MultiPhotoSelectActivity extends Activity {
 
 	public final String TAG = MultiPhotoSelectActivity.class.getSimpleName();
 	
     public static final String RESULT_BUNDLE_INDENTIFIER = "com.fyodorwolf.studyBudy.imageStrings";
 	
     private static final int CONTENT_REQUEST = 2;
 
     private ImageLoader _imageLoader = ImageLoader.getInstance();
     
     private ArrayList<String> _imageUrls;
     private ImageAdapter _imageAdapter;
  
     @Override  public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         
         /*VIEW SETUP & INSTANTIATION*/
         setContentView(R.layout.ac_image_grid);
         setTitle("Select Card Images");
         
         setImages();
     }
 
 	@Override public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.photo_select, menu);
 		return true;
 	}
 	
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
             case R.id.camera:
         		Date now = new Date();
             	String fileName = DateFormat.getDateTimeInstance().format(now);
             	long dateTaken = now.getTime();
             	
             	ContentValues image = new ContentValues();
         	    image.put(MediaStore.Images.Media.TITLE, fileName);
         	    image.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
         	    image.put(MediaStore.Images.Media.DESCRIPTION, fileName);
         	    image.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
         	    image.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
         	    image.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
         	    image.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
         	    image.put(MediaStore.Images.Media.ORIENTATION, 0);
         	    //creates empty File for image to be stored to.
         	    Uri taken_image_uri =  getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
             	
         	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         	    intent.putExtra(MediaStore.EXTRA_OUTPUT, taken_image_uri);
         	    startActivityForResult(intent, CONTENT_REQUEST);
         	    break;
         }
         return true;
     }
 
 	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == CONTENT_REQUEST)
             if (resultCode == Activity.RESULT_OK) {
             	setImages();
             }
     }
 	
 	private void setImages(){
 
         /*FETCH MEDIA*/
         final String[] columns = { MediaStore.Images.Media.DATA};
         Cursor imagecursor = this.getContentResolver().query(
             MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media.DATE_TAKEN+" DESC"
         );
  
         /*SET GRIDVIEW ADAPTER*/
         this._imageUrls = new ArrayList<String>();
         imagecursor.moveToPosition(-1);
         while (imagecursor.moveToNext()) {
             int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
             String uri = imagecursor.getString(dataColumnIndex);
             if ((new File(uri)).exists())
             	//For some reason, a blank image could be still registered in the MediaStore cache.
             	_imageUrls.add(uri);
         }
         imagecursor.close();
         _imageAdapter = new ImageAdapter(this, _imageUrls);
         ((GridView)findViewById(R.id.gridview)).setAdapter(_imageAdapter);
         
         /*ACTIONS*/
         ((Button)findViewById(R.id.choose_photos)).setOnClickListener(new OnClickListener(){
 			@Override public void onClick(View v) {          
 				ArrayList<String> selectedItems = _imageAdapter.getCheckedItems();
 				String[] files = new String[selectedItems.size()];
 	          	selectedItems.toArray(files);
 	          	Intent resultIntent = new Intent();
 	          	resultIntent.putExtra(RESULT_BUNDLE_INDENTIFIER, files);
 	          	setResult(Activity.RESULT_OK, resultIntent);
 	          	finish();
 			}
         });
         
 	}
 	
 	
/*******************************************************************************
 * THIS ADAPTER IS USED TO SET THE CONTENT OF THE GRID VIEW FOR THIS ACTIVITY. *
 *******************************************************************************/
     public class ImageAdapter extends BaseAdapter {
  
         ArrayList<String> mList;
         LayoutInflater mInflater;
         Context mContext;
         SparseBooleanArray mSparseBooleanArray;
         OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
         	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
             }
         };
         
         public ImageAdapter(Context context, ArrayList<String> imageList) {
             mContext = context;
             mInflater = LayoutInflater.from(mContext);
             mSparseBooleanArray = new SparseBooleanArray();
             mList = new ArrayList<String>();
             this.mList = imageList;
         }
  
 		public ArrayList<String> getCheckedItems() {
             ArrayList<String> mTempArry = new ArrayList<String>();
             for(int i=0;i<mList.size();i++) {
                 if(mSparseBooleanArray.get(i)) {
                     mTempArry.add(mList.get(i));
                 }
             }
             return mTempArry;
         }
         @Override public int getCount() {  return _imageUrls.size();}
         @Override public Object getItem(int position) { return null;}
         @Override public long getItemId(int position) {return position;}
  
         @Override public View getView(final int position, View convertView, ViewGroup parent) {
             if(convertView == null) {
                 convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
             }
             final CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
             final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
  
             _imageLoader.displayImage("file://"+_imageUrls.get(position), imageView, null, new SimpleImageLoadingListener() {
             	@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
 //            		Log.d(TAG, "Broke: "+ imageUri);
             	}
             	@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage){
 //                    Log.d(TAG, "Loaded: "+imageUri);
                     Animation anim = AnimationUtils.loadAnimation(MultiPhotoSelectActivity.this, android.R.anim.fade_in);
                     imageView.setAnimation(anim);
                     anim.start();
             	}
             });
             
             imageView.setOnClickListener(new OnClickListener(){
         		@Override public void onClick(View v) {
                     mCheckBox.setChecked(!mSparseBooleanArray.get(position));
 				}
     		});
  
             mCheckBox.setTag(position);
             mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);
             mCheckBox.setChecked(mSparseBooleanArray.get(position));
             return convertView;
         }
 
     }
  
 }
