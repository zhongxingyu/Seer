 package org.g_okuyama.log;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class LogDetailActivity extends Activity {
 	public static final String TAG = "DrinkLog";
 	private static final int MENU_EDIT = 0;
 	private static final int MENU_DELETE = 1;
 	private static final int MENU_SHARE = 2;
     public static final int RESPONSE_DELETE = 7777;
 	public static final int REQUEST_EDIT = 6666;	
 	public static final int RESPONSE_EDIT = 6667;
     public static final int REQUEST_TWEET = 5555; 
     public static final int RESPONSE_TWEET = 5556; 
 	
 	private int mDBID;
 	
 	int mCategory;
 	String mName;
 	String mImageURL;
 	float mRate;
 	String mComment;
 	String mVintage;
 	String mType;
 	String mArea;
 	String mDate;
 	String mPlace;
 	String mPrice;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Bundle extras = getIntent().getExtras();
         mDBID = extras.getInt("dbid");
         
         setContentView(R.layout.reference);
         setLayout();
     }
     
     private void setLayout(){
         getDetailData(mDBID);
         setName();
         setImage();
         setRate();
         setComment();
         setTweet();
         setOtherLayout();
     }
     
     private void getDetailData(int dbid){
     	//DB擾
     	DatabaseHelper helper = new DatabaseHelper(this);
     	SQLiteDatabase db = helper.getWritableDatabase();
     	String query = "select * from logtable where rowid = ?;";
     	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid)});
 
     	c.moveToFirst();
     	String cat = c.getString(1);
     	mCategory = Integer.valueOf(cat);
 		mName = c.getString(2);
 		mImageURL = c.getString(3);
 		String rate = c.getString(4);
 		mRate = Float.valueOf(rate);
 		mComment = c.getString(5);
 		mVintage = c.getString(6);
 		mType = c.getString(7);
 		mArea = c.getString(8);
 		mDate = c.getString(9);
 		mPlace = c.getString(10);
 		mPrice = c.getString(11);
 		
 		c.close();
     }
     
     private void setName(){
     	TextView name = (TextView)findViewById(R.id.ref_name);
     	name.setText(mName);
     }
     
     private void setImage(){
     	ImageView image = (ImageView)findViewById(R.id.ref_image);
     	if(mImageURL.equals("none")){
     		image.setImageResource(R.drawable.default_image);
     	}
     	else{
     		Log.d(TAG, "URL = " + mImageURL);
     		Uri uri = Uri.parse(mImageURL);
             image.setImageBitmap(RegisterActivity.uri2bmp(this, uri, 160, 120));
             
             image.setOnClickListener(new OnClickListener(){
                 public void onClick(View arg0) {
                     Intent intent = new Intent(LogDetailActivity.this, ImageViewActivity.class);
                     intent.putExtra("imageurl", mImageURL);
                     intent.putExtra("name", mName);
                     startActivity(intent);
                 }
             });
     	}    	
     }
     
     private void setRate(){
     	RatingBar rate = (RatingBar)findViewById(R.id.ref_rating);
     	rate.setRating(mRate);
     }
     
     private void setComment(){
     	TextView comment = (TextView)findViewById(R.id.ref_impression);
     	if(mComment.equals("none")){
     		comment.setText(getString(R.string.ref_no_comment));
     	}
     	else{
     		comment.setText(mComment);
     	}
     }
     
     private void setTweet(){
         ImageButton tweetBtn = (ImageButton)findViewById(R.id.tweet_bird);
         tweetBtn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 share();
             }
         });
     }
     
     private void setOtherLayout(){
         final LinearLayout otherLayout = (LinearLayout)findViewById(R.id.ref_other_display_layout);
         Button other = (Button)findViewById(R.id.ref_other);
         other.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 //ȗv{^̃CAEg\
                 otherLayout.setVisibility(View.GONE);
 
                 LinearLayout linear = (LinearLayout)findViewById(R.id.ref_linearLayout1);
                 final LayoutInflater mInflater;
                 mInflater = (LayoutInflater)LogDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 mInflater.inflate(R.layout.reference_add, linear);
                 
                 setOtherLog();
             }
         });
     }
     
     private void setOtherLog(){
     	TextView year = (TextView)findViewById(R.id.ref_year);
     	if(mVintage.equals("none")){
     		year.setText(getString(R.string.ref_no_setting));
     	}
     	else{
     		year.setText(mVintage);
     	}
     	
     	TextView area = (TextView)findViewById(R.id.ref_area);
     	if(mArea.equals("none")){
     		area.setText(getString(R.string.ref_no_setting));	
     	}
     	else{
     		area.setText(mArea);
     	}
     	
     	TextView date = (TextView)findViewById(R.id.ref_date);
     	//TODO:p\L
     	date.setText(mDate);
     	
     	TextView place = (TextView)findViewById(R.id.ref_place);
     	if(mPlace.equals("none")){
     		place.setText(getString(R.string.ref_no_setting));
     	}
     	else{
     		place.setText(mPlace);
     	}
     	
     	TextView price = (TextView)findViewById(R.id.ref_price);
     	if(mPrice.equals("none")){
     		price.setText(getString(R.string.ref_no_setting));
     	}
     	else{
     		price.setText(mPrice);
     	}
     }
     
     /*
      * IvVj[̍쐬
      * EҏW
      * E폜
      * Ee(twitter)
      */
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         MenuItem edit = menu.add(0, MENU_EDIT, 0 ,R.string.menu_edit);
         edit.setIcon(android.R.drawable.ic_menu_edit);
 
         MenuItem delete = menu.add(0, MENU_DELETE, 0 ,R.string.menu_delete);
         delete.setIcon(android.R.drawable.ic_menu_delete);
 
         MenuItem share = menu.add(0, MENU_SHARE, 0 ,R.string.menu_share);
         share.setIcon(android.R.drawable.ic_menu_share);
 
         return true;
     }
     
     //IvVj[ĨXi
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case MENU_EDIT:
     		edit();
     		break;
     		
     	case MENU_DELETE:
     		delete();
     		break;
     		
     	case MENU_SHARE:
     		share();
     		break;
 
     	default:
     		//Ȃ
     	}
 
     	return true;
     }
     
     private void edit(){
     	Intent intent = new Intent(this, RegisterActivity.class);
     	intent.putExtra("category", mCategory);
     	intent.putExtra("from", "LogDetailActivity");
     	intent.putExtra("dbid", mDBID);
     	startActivityForResult(intent, REQUEST_EDIT);
     }
     
     
     public void onActivityResult(int requestCode, int resultCode, Intent data){
     	if(requestCode == REQUEST_EDIT){
     		if(resultCode == RESPONSE_EDIT){
     			//ҏW̃O\
                 setLayout();
     		}
     	}
     	else if(requestCode == REQUEST_TWEET){
     	    //Ȃ
     	}
     }
     
     private void delete(){
     	new AlertDialog.Builder(this)
     	.setTitle(R.string.cancel_confirm_title)
     	.setMessage(getString(R.string.list_alert_confirm))
     	.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 		    	DatabaseHelper helper = new DatabaseHelper(LogDetailActivity.this);
 		    	SQLiteDatabase db = helper.getWritableDatabase();
 		    	db.delete("logtable", "rowid = ?", new String[]{Integer.toString(mDBID)});
 		    	//OʂActivityɖ߂
 	            Intent intent = new Intent();
 	            setResult(RESPONSE_DELETE, intent);
 		    	finish();
 			}
 		})
 		.setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				//Ȃ
 			}
 		})
 		.show();
     }
     
     private void share(){
     	Intent intent = new Intent(this, TwitterActivity.class);
     	intent.putExtra("name", mName);
     	intent.putExtra("rate", String.valueOf(mRate));
     	intent.putExtra("comment", mComment);
     	intent.putExtra("path", mImageURL);
     	startActivityForResult(intent, REQUEST_TWEET);
     }
 }
