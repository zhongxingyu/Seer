 package org.g_okuyama.log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.Calendar;
 
 import org.g_okuyama.log.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RatingBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.DatePicker;
 import android.widget.Toast;
 
 public class RegisterActivity extends Activity {
     public static final String TAG = "DrinkLog";
     public static final int RATING_STAR_NUM = 5;
     public static final float RATING_STEP = (float)0.5;
     public static final int REQUEST_CODE_GALLERY = 9999; 
     public static final int REQUEST_CODE_CAMERA = 9998;
 	public static final int RESPONSE_EDIT = 6667;	
     
     DatabaseHelper mHelper = null;
     File mSaveFile = null;
     String mCurDate = "unknown";
     int mCategory;
     boolean mEditFlag = false;
         
     int mYear;
     int mMonth;
     int mDay;
 
     boolean mPicFlag = false;
     //ȗv{^ꂽH
     boolean mOtherFlag = false;
     
     //ҏWptB[h
     private int mDBID = -9876;
 	String mName;
     String mImageURL = null;
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
         
         Bundle extra = getIntent().getExtras();
         mCategory = extra.getInt("category");
         String flag = extra.getString("from");
         if(flag.equals("LogDetailActivity")){
         	mEditFlag = true;
         	mDBID = extra.getInt("dbid");
         	getDBData(mDBID);
         }
         
         //ŃL[{[hoȂ悤ɐݒ
         this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);         
         setContentView(R.layout.register);
 
         mHelper = new DatabaseHelper(this);
 
         setLayout();
     }
     
     private void getDBData(int dbid){
     	DatabaseHelper helper = new DatabaseHelper(this);
     	SQLiteDatabase db = helper.getWritableDatabase();
     	String query = "select * from logtable where rowid = ?;";
     	Cursor c = db.rawQuery(query, new String[]{Integer.toString(dbid)});
 
     	c.moveToFirst();
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
     
     private void setLayout(){
     	initDate();
         setName();
         setImage();
         setEvaluate();
         setComment();
         setOther();
         setRegister();
     }
     
     private void initDate(){
     	Calendar cal1 = Calendar.getInstance();
     	mYear = cal1.get(Calendar.YEAR);
     	mMonth = cal1.get(Calendar.MONTH);
     	mDay = cal1.get(Calendar.DATE);
     	mCurDate = getDate(mYear, mMonth, mDay);
     }
         
     private void setName(){
         AutoCompleteTextView view = (AutoCompleteTextView)findViewById(R.id.name);
 
         int id = getCategoryID(mCategory);
         if((id != -1) && !mEditFlag){
             ArrayAdapter name_adapter = ArrayAdapter.createFromResource(
                     this, id, android.R.layout.simple_dropdown_item_1line);
             view.setAdapter(name_adapter);
         }
         else if(mEditFlag){
         	//ҏW͓͕⊮͕KvȂ
         	view.setText(mName);        	
         }
 
         //TODO:(option)o^ĂȂOœo^ꂽꍇ́Aname_adapterɒǉ
         
         view.addTextChangedListener(new TextWatcher(){
             public void afterTextChanged(Editable edit) {
                 String name = edit.toString();
                 //TODO:On<-]Tł
             }
 
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
             public void onTextChanged(CharSequence s, int start, int before, int count) {
             }
         });
     }
     
     private int getCategoryID(int idx){
         switch(idx){
             case DrinkLogActivity.CATEGORY_WHISKEY:
                 return R.array.name_array_wh;
                 
             case DrinkLogActivity.CATEGORY_COCKTAIL:
             	return R.array.name_array_co;
             	
                 //TODO:ޒǉ
             default:
                 return -1;
         }
     }
     
     private void setImage(){
         ImageView image = (ImageView)findViewById(R.id.cur_image);
         
         final String[] setting_list = getResources().getStringArray(R.array.image_setting_array);        
         ImageButton imageBtn = (ImageButton)findViewById(R.id.picture);
         imageBtn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 new AlertDialog.Builder(RegisterActivity.this)
                 .setIcon(android.R.drawable.ic_dialog_info)
                 .setTitle(R.string.dialog_image)
                 .setItems(setting_list, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         if(which == 0){
                             //gallery
                             Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                             intent.setType("image/*");
                             startActivityForResult(
                                     Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY);
                         }
                         else{
                             //camera
                             File file = new File(Environment.getExternalStorageDirectory(), "/DrinkLog");
                             if(file.exists() == false){
                                 file.mkdir();
                             }
                             
                             Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                             mSaveFile = new File(file, String.valueOf(System.currentTimeMillis() + ".jpg"));
                             intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mSaveFile));
                             startActivityForResult(intent, REQUEST_CODE_CAMERA);
                         }
                     }
                 })
                .show();
             }
         });
         
         //ftHg摜\
         image.setImageResource(R.drawable.default_image);
 
         if(mEditFlag){
         	if(!mImageURL.equals("none")){
         		Uri uri = Uri.parse(mImageURL);
                 image.setImageBitmap(RegisterActivity.uri2bmp(this, uri, 160, 120));
         	}
         }
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent data){        
         if(requestCode == REQUEST_CODE_GALLERY){
             if(data == null){
                 return;
             }
 
             if(resultCode == RESULT_OK){
                 ImageView image = (ImageView)findViewById(R.id.cur_image);
                 Uri uri = data.getData();
                 //ۑp
                 mImageURL = uri.toString();
                 image.setImageBitmap(uri2bmp(this, uri, 160, 120));
             }
             else if(resultCode == RESULT_CANCELED){
                 Toast.makeText(this, R.string.user_canceled, Toast.LENGTH_SHORT).show();
             }
             else{
                 Toast.makeText(this, R.string.get_image_error, Toast.LENGTH_SHORT).show();
             }
         }
         else if(requestCode == REQUEST_CODE_CAMERA){
             if(resultCode == RESULT_OK){
                 ImageView image = (ImageView)findViewById(R.id.cur_image);
                 Uri uri = Uri.fromFile(mSaveFile);
                 image.setImageBitmap(uri2bmp(this, uri, 160, 120));
                 //ۑp
                 mImageURL = uri.toString();
             }
             else if(resultCode == RESULT_CANCELED){
                 Toast.makeText(this, R.string.user_canceled, Toast.LENGTH_SHORT).show();
             }
             else{
                 Toast.makeText(this, R.string.get_image_error, Toast.LENGTH_SHORT).show();
             }            
         }
     }
     
     public static Bitmap uri2bmp(Context context,Uri uri,int maxW,int maxH) {
         BitmapFactory.Options options;
         InputStream in=null;
         try {
             //摜TCY̎擾
             options=new BitmapFactory.Options();
             options.inJustDecodeBounds=true;
             in=context.getContentResolver().openInputStream(uri);  
             BitmapFactory.decodeStream(in,null,options);
             in.close();
             int scaleW=options.outWidth /maxW+1;
             int scaleH=options.outHeight/maxH+1;
             int scale =Math.max(scaleW,scaleH);
          
             //摜̓ǂݍ
             options=new BitmapFactory.Options();
             options.inJustDecodeBounds=false;
             options.inSampleSize=scale;
             options.inPurgeable=true;
             in=context.getContentResolver().openInputStream(uri);  
             Bitmap bmp = BitmapFactory.decodeStream(in, null, options);
             in.close();
             
             //]
             int w = bmp.getWidth();
             int h = bmp.getHeight();
             if(w > h){
                 Matrix matrix = new Matrix();
                 // ]pxw
                 matrix.postRotate(90.0f);   
                 Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);
                 bmp = null;
                 bmp = bmp2;
             }
             
             return bmp;
         } catch (Exception e) {
             try {
                 if (in!=null) in.close();
             } catch (Exception e2) {
             }
             return null;
         }
     }
     
     private void setEvaluate(){
         RatingBar bar = (RatingBar)findViewById(R.id.rating);
         bar.setNumStars(RATING_STAR_NUM);
         
         if(mEditFlag){
         	float rate = Float.valueOf(mRate);
         	bar.setRating(rate);
         }
     }
     
     private void setComment(){
     	if(mEditFlag){
     		if(!mComment.equals("none")){
     			EditText edit = (EditText)findViewById(R.id.impression);
     			edit.setText(mComment);
     		}
     	}
     }
     
     private void setOther(){
         final LinearLayout otherLayout = (LinearLayout)findViewById(R.id.other_display_layout);
         Button other = (Button)findViewById(R.id.log_other);
         other.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 mOtherFlag = true;
                 //ȗv{^̃CAEg\
                 otherLayout.setVisibility(View.GONE);
 
                 //̑̍ڂ𓮓Iɒǉ(ނƂɈقȂ)
                 LinearLayout linear = (LinearLayout)findViewById(R.id.linearLayout1);
                 final LayoutInflater mInflater;
                 mInflater = (LayoutInflater)RegisterActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 mInflater.inflate(R.layout.register_add, linear);
         
                 setVintage();
                 setType();
                 setArea();
                 setDate();
                 setPlace();
                 setPrice();
                 //JeSƂɂȂڂ}XN
                 setMask();
             }
         });
     }
     
     private void setVintage(){
     	if(mEditFlag){
     		if(!mVintage.equals("none")){
     			TextView view = (TextView)findViewById(R.id.year);
     			view.setText(mVintage);
     		}
     	}
     }
     
     private void setType(){
     	ArrayAdapter adapter = null;
     	Spinner spinner = null;
         int id = getTypeID(mCategory);
         if(id != -1){
             adapter = ArrayAdapter.createFromResource(
                     this, id, android.R.layout.simple_spinner_item);
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
             spinner = (Spinner)findViewById(R.id.type_spinner);
             spinner.setAdapter(adapter);
         }
         
     	if(mEditFlag){
     		if(!mType.equals("none")){
     			int i;
     			for(i=0; i<adapter.getCount(); i++){
     				String type = (String)adapter.getItem(i);
     				if(type.equals(mType)){
     					spinner.setSelection(i);
     	    			return;
     				}
     			}    			
     		}
     	}    	
     }
     
     private int getTypeID(int idx){
         switch(idx){
             case DrinkLogActivity.CATEGORY_WHISKEY:
                 return R.array.type_array_wh;
                 
             case DrinkLogActivity.CATEGORY_COCKTAIL:
             	return R.array.type_array_co;
                 
             case DrinkLogActivity.CATEGORY_WINE:
             	return R.array.type_array_wi;
 
             case DrinkLogActivity.CATEGORY_SHOCHU:
                 return R.array.type_array_sh;
 
             case DrinkLogActivity.CATEGORY_SAKE:
             	return R.array.type_array_ja;
 
             case DrinkLogActivity.CATEGORY_BRANDY:
             	return R.array.type_array_br;
 
             case DrinkLogActivity.CATEGORY_BEER:
                 return R.array.type_array_be;
 
             default:
             	return -1;
         }
     }
     
     private void setArea(){
     	ArrayAdapter adapter = null;
     	Spinner spinner = null;
         int id = getAreaID(mCategory);
         if(id != -1){
             adapter = ArrayAdapter.createFromResource(
                     this, id, android.R.layout.simple_spinner_item);
             adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
             spinner = (Spinner)findViewById(R.id.area_spinner);
             spinner.setAdapter(adapter);        
         }
         
     	if(mEditFlag){
     		if(!mArea.equals("none")){
     			int i;
     			for(i=0; i<adapter.getCount(); i++){
     				String type = (String)adapter.getItem(i);
     				if(type.equals(mArea)){
     					spinner.setSelection(i);
     	    			return;
     				}
     			}    			
     		}
     	}    	
     }
     
     private int getAreaID(int idx){
         switch(idx){
             case DrinkLogActivity.CATEGORY_WHISKEY:
                 return R.array.area_array_wh;
                 
             case DrinkLogActivity.CATEGORY_WINE:
             	return R.array.area_array_wi;
 
             case DrinkLogActivity.CATEGORY_SHOCHU:
                 return R.array.area_array_sh;
 
             case DrinkLogActivity.CATEGORY_SAKE:
             	return R.array.area_array_ja;
 
             case DrinkLogActivity.CATEGORY_BRANDY:
             	return R.array.area_array_br;
 
             case DrinkLogActivity.CATEGORY_BEER:
                 return R.array.area_array_be;
 
             default:
             	return -1;
         }
     }
     
     private void setDate(){
         TextView text = (TextView)findViewById(R.id.date);
         if(mEditFlag){
         	text.setText(mDate);
         }
         else{
         	text.setText(mCurDate);
         }
         
         ImageButton selectBtn = (ImageButton)findViewById(R.id.date_select);
         selectBtn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 DatePickerDialog dialog = new DatePickerDialog(
                         RegisterActivity.this,
                         new DatePickerDialog.OnDateSetListener() {
                             public void onDateSet(DatePicker view, int year, int month, int day) {
                                 //w肳ꂽtݒ
                                 TextView text = (TextView)findViewById(R.id.date);
                                 mCurDate = getDate(year, month, day);
                                 text.setText(mCurDate);
                             }
                         },
                         mYear, mMonth, mDay);
                 dialog.show();
             }
         });
     }
     
     private String getDate(int year, int mon, int d){        
         String month = Integer.toString(mon + 1);
         if(month.length() == 1){
             month = "0" + month;
         }
         String day = Integer.toString(d);
         if(day.length() == 1){
             day = "0" + day;
         }
         return Integer.toString(year) + "/" + month + "/" + day;
     }
     
     private void setPlace(){
     	if(mEditFlag){
     		if(!mPlace.equals("none")){
     			TextView view = (TextView)findViewById(R.id.place);
     			view.setText(mPlace);
     		}
     	}
 	
     }
     
     private void setPrice(){
     	if(mEditFlag){
     		if(!mPrice.equals("none")){
     			TextView view = (TextView)findViewById(R.id.price);
     			view.setText(mPrice);
     		}
     	}
     	
     }
     
     private void setMask(){
     	LinearLayout year = (LinearLayout)findViewById(R.id.reg_year_layout);
     	LinearLayout area = (LinearLayout)findViewById(R.id.reg_area_layout);
     	LinearLayout type = (LinearLayout)findViewById(R.id.reg_type_layout);    	
 
     	switch(mCategory){
         case DrinkLogActivity.CATEGORY_WHISKEY:
             break;
             
         case DrinkLogActivity.CATEGORY_COCKTAIL:
         	year.setVisibility(View.GONE);
         	area.setVisibility(View.GONE);
             break;
             
         case DrinkLogActivity.CATEGORY_WINE:
             break;
 
         case DrinkLogActivity.CATEGORY_SHOCHU:
         	year.setVisibility(View.GONE);
             break;
 
         case DrinkLogActivity.CATEGORY_SAKE:
         	year.setVisibility(View.GONE);
             break;
 
         case DrinkLogActivity.CATEGORY_BRANDY:
             break;
 
         case DrinkLogActivity.CATEGORY_BEER:
         	year.setVisibility(View.GONE);
             break;
 
         case DrinkLogActivity.CATEGORY_OTHER:
         	year.setVisibility(View.GONE);
         	type.setVisibility(View.GONE);
         	area.setVisibility(View.GONE);
             break;
 
         default:
             break;
         }
     }
         
     private void setRegister(){
         final Handler handler = new Handler();
         //final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
         
         Button regBtn = (Button)findViewById(R.id.register_commit);
         regBtn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 Thread thread = new Thread() {
                     public void run() {
                         //0̖O͎󂯕tȂ
                         TextView name_view = (TextView)findViewById(R.id.name);
                         String name = name_view.getText().toString();
                         if(name.length() == 0){
                             handler.post(new Runnable(){
                                 public void run(){            
                                     new AlertDialog.Builder(RegisterActivity.this)
                                     .setTitle(R.string.register_warning)
                                     .setMessage(R.string.input_request)
                                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int which) {
                                             return;
                                         }
                                     }).show();
                                 }
                             });
                         }
                         else{
                             register(name);
                             
                             //o^ʒm
                             handler.post(new Runnable(){
                                 public void run(){
                                     new AlertDialog.Builder(RegisterActivity.this)
                                     .setMessage(R.string.dialog_notify)
                                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int which) {
                             	            Intent intent = new Intent();
                             	            setResult(RESPONSE_EDIT, intent);
                                             finish();
                                         }
                                     })
                                     .show();
                                 }
                             });
                         }
                     }
                 };
                 thread.start();
             }
         });
         
         Button canclBtn = (Button)findViewById(R.id.register_cancel);
         canclBtn.setOnClickListener(new OnClickListener(){
             public void onClick(View v) {
                 new AlertDialog.Builder(RegisterActivity.this)
                 .setTitle(R.string.cancel_confirm_title)
                 .setMessage(R.string.cancel_confirm)
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         finish();
                     }
                 })
                 .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         //Ȃ
                         return;
                     }
                 })
                 .show();
             }
         });
     }
     
     private void register(String name){
         switch(mCategory){
             case DrinkLogActivity.CATEGORY_WHISKEY:
                 putWhData(mCategory, name);
                 break;
                 
             case DrinkLogActivity.CATEGORY_COCKTAIL:
                 putCoData(mCategory, name);
                 break;
                 
             case DrinkLogActivity.CATEGORY_WINE:
                 putWiData(mCategory, name);
                 break;
 
             case DrinkLogActivity.CATEGORY_SHOCHU:
                 putShData(mCategory, name);
                 break;
 
             case DrinkLogActivity.CATEGORY_SAKE:
                 putJaData(mCategory, name);
                 break;
 
             case DrinkLogActivity.CATEGORY_BRANDY:
                 putBrData(mCategory, name);
                 break;
 
             case DrinkLogActivity.CATEGORY_BEER:
                 putBeData(mCategory, name);
                 break;
 
             case DrinkLogActivity.CATEGORY_OTHER:
                 putOtData(mCategory, name);
                 break;
 
             default:
                 Log.e(TAG, "Unknown Category");
                 break;
         }
     }
     
     private void putWhData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putYear(values);
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putCoData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putType(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putWiData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putYear(values);
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putShData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
     
     private void putJaData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putBrData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putYear(values);
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putBeData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putType(values);
         putArea(values);
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
 
     private void putOtData(int category, String name){
         ContentValues values = new ContentValues();
         initValues(values);
 
         values.put("category", String.valueOf(category));
         putName(values, name);
         putImage(values);
         putEvaluate(values);
         putComment(values);
         /*ȉ͂̑̍*/
         putDate(values);
         putPlace(values);
         putPrice(values);
 
         registerDB(values);
     }
     
     private void registerDB(ContentValues values){
         SQLiteDatabase db = mHelper.getWritableDatabase();
 
         if(mEditFlag){
         	db.update("logtable", values, "rowid = ?", new String[]{Integer.toString(mDBID)});
         }
         else{
         	db.insert("logtable", null, values);
         }
         
         db.close();            	
     }
     
     private void initValues(ContentValues values){
         values.put("image", "none");
         values.put("comment", "none");
         values.put("year", "none");
         values.put("type", "none");
         values.put("area", "none");
         values.put("place", "none");
         values.put("price", "none");    	
     }
 
     private void putName(ContentValues values, String name){
         values.put("name", name);
     }
     
     private void putImage(ContentValues values){
         if(mImageURL != null){
             values.put("image", mImageURL);
         }        
     }
     
     private void putEvaluate(ContentValues values){
         RatingBar bar = (RatingBar)findViewById(R.id.rating);
         float rate = bar.getRating();
         String rate_str = String.valueOf(rate);
         values.put("evaluate", rate_str);        
     }
     
     private void putComment(ContentValues values){
         EditText comment_text = (EditText)findViewById(R.id.impression);
         String comment = comment_text.getText().toString();
         if(comment.length() != 0){
             values.put("comment", comment);
         }
     }
     
     private void putYear(ContentValues values){
         if(mOtherFlag){
             EditText year_text = (EditText)findViewById(R.id.year);
             String year = year_text.getText().toString();
             if(year.length() != 0){
                 values.put("year", year);
             }
         }
         else{
             if(mEditFlag){
             	/*
             	 * ҏWAȗv{^Ȃꍇ́A
             	 * DB璼ڎof[^o^B
             	 * (ȉAputXXXn͓)
             	 */
             	values.put("year", mYear);
             }
         }        
     }
     
     private void putType(ContentValues values){
         if(mOtherFlag){
         	//TODO:RgO
         	/*
             Spinner type_spinner = (Spinner)findViewById(R.id.type_spinner);
             String type = type_spinner.getSelectedItem().toString();
             if(!type.equals("")){
                 values.put("type", type);
             }
             */
         }
         else{
             if(mEditFlag){
             	values.put("year", mType);
             }
         }        
     }
     
     private void putArea(ContentValues values){
         //n
         if(mOtherFlag){
             Spinner area_spinner = (Spinner)findViewById(R.id.area_spinner);
             String area = area_spinner.getSelectedItem().toString();
             if(!area.equals("")){
                 values.put("area", area);
             }
         }
         else{
             if(mEditFlag){
             	values.put("year", mArea);
             }
         }
     }
     
     private void putDate(ContentValues values){
        if(mEditFlag){
        	values.put("date", mDate);        	 
        }
        else{
        	values.put("date", mCurDate);
        }
     }
 
     private void putPlace(ContentValues values){
         if(mOtherFlag){
             EditText place_text = (EditText)findViewById(R.id.place);
             String place = place_text.getText().toString();
             if(place.length() != 0){
                 values.put("place", place);
             }
         }
         else{
             if(mEditFlag){
             	values.put("year", mPlace);
             }
         }
     }
 
     private void putPrice(ContentValues values){
         if(mOtherFlag){
             EditText price_text = (EditText)findViewById(R.id.price);
             String price = price_text.getText().toString();
             if(price.length() != 0){
                 values.put("price", price);
             }
         }
         else{
             if(mEditFlag){
             	values.put("year", mPrice);
             }
         }
     }
     
     public boolean onKeyDown(int keyCode, KeyEvent event){
         if(keyCode == KeyEvent.KEYCODE_BACK){
             new AlertDialog.Builder(RegisterActivity.this)
             .setTitle(R.string.cancel_confirm_title)
             .setMessage(R.string.cancel_confirm)
             .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     finish();
                 }
             })
             .setNegativeButton(R.string.ng, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                     //Ȃ
                 }
             })
             .show();
             return false;
         }
         else{
             return super.onKeyDown(keyCode, event);   
         }
     }
 }
