 
 package com.mycode.mycalendar;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.DatePicker;
 import android.widget.ImageView;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.Calendar;
 
 public class MainActivity extends FragmentActivity implements
     OnDateSetListener, OnMenuItemClickListener, OnFocusChangeListener {
 
 	public static final String CHOOSED_DAY = "ChoosedDay";
 	public static final int PICK_CLICK_CELL_REQUEST_CODE = 15;
 	public static final int PICK_ADD_USER_REQUEST_CODE = 16;
 	public static final int PICK_DELETE_USER_REQUEST_CODE = 17;
 	
     private ViewPager mPager;
     private PagerAdapter mPagerAdapter;
     private View mImgPreviousMonth;
     private View mImgNextMonth;
     private DateFormatter mFormatter;
     
     private TextView mTxtTitleGregorian;
     private TextView mTxtTitleAddition;
     private TextView mTxtTitleLunar;
     
     private ImageView mCellImgView;
     final static int mMonthAYear = 12;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mFormatter = new DateFormatter(this.getResources());
         mImgPreviousMonth = findViewById(R.id.imgPreviousMonth);
         mImgNextMonth = findViewById(R.id.imgNextMonth);
         mTxtTitleGregorian = (TextView)findViewById(R.id.txtTitleGreorian);
         mTxtTitleAddition = (TextView)findViewById(R.id.txtTitleAddition);
         mTxtTitleLunar = (TextView)findViewById(R.id.txtTitleLunar);
         
         mPager = (ViewPager)findViewById(R.id.pager);
         
         mPagerAdapter = new CalendarPagerAdapter(getSupportFragmentManager());
         mPager.setAdapter(mPagerAdapter);
         mPager.setOnPageChangeListener(new simplePageChangeListener());
         mPager.setCurrentItem(getTodayMonthIndex());
         
         QueryUserCount();
     }
 
     private void QueryUserCount() {
 		// TODO Auto-generated method stub
     	Uri uri = SchedularProviderMetaData.UserNameTableMetaData.CONTENT_URI_NAME;
     	ContentResolver resolver= this.getContentResolver();
     	Cursor cursor = resolver.query(uri, null, null, null, null);
     	int userCount = 0;
     	if(null != cursor){
     		userCount = cursor.getCount();
     	}
    	cursor.close();
     	
     	SharedPreferences pref = this.getSharedPreferences("preferences",Context.MODE_PRIVATE); 
     	Editor editor = pref.edit();
     	editor.putInt("UserCount", userCount);
     	editor.commit();
     }
 
 	
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     /**
      * onFocus when cell has/lose focus
      */
     public void onFocusChange(View v, boolean hasFocus) {
         // TODO Auto-generated method stub
         if(!hasFocus){
             return;
         }
         LunarCalendar lc = (LunarCalendar)v.getTag();
         CharSequence[] info = mFormatter.getFullDateInfo(lc);
         mTxtTitleLunar.setText(info[1]);
         mTxtTitleAddition.setText(info[0]);
         
     }
 
     public boolean onMenuItemClick(MenuItem item) {
         // TODO Auto-generated method stub
         switch(item.getItemId()){
             case R.id.menuGoto:
                 int year = mPager.getCurrentItem() / mMonthAYear + LunarCalendar.getMinYear();
                 int month = mPager.getCurrentItem() % mMonthAYear;
                 DatePickerDialog dpd = new DatePickerDialog(
                         this,
                         android.R.style.Theme_DeviceDefault_DialogWhenLarge_NoActionBar, 
                         this,
                         year,
                         month,
                         1);
                 dpd.getDatePicker().setCalendarViewShown(false);
                 dpd.show();
                 return true;
                 //break;
             case R.id.menuAddUsers:
             	Intent intentUserNameAdd = new Intent(Intent.ACTION_VIEW);
             	intentUserNameAdd.setClass(this, UserNameInformation.class);
                 this.startActivityForResult(intentUserNameAdd, PICK_ADD_USER_REQUEST_CODE);
             	//AddUsers();
             	break;
             case R.id.menuDeleteUsers:
             	Intent intentUserNameDelete = new Intent(Intent.ACTION_VIEW);
             	intentUserNameDelete.setClass(this, UserNameInformation.class);
                 this.startActivityForResult(intentUserNameDelete, PICK_DELETE_USER_REQUEST_CODE);
             	break;
             default:
                     break;
         }
         return false;
     }
     
     private void DeleteUsers(String name) {
 		// TODO Auto-generated method stub
     	//String Name = "Lionel";
     	Uri uri = SchedularProviderMetaData.UserNameTableMetaData.CONTENT_URI_NAME;
     	ContentResolver resolver= this.getContentResolver();
     	ContentValues values = new ContentValues();
     	values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME, name);
     	
     	String whereClause = SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME
     		      + "=? ";
           String[] whereArgs = {name};
     	resolver.delete(uri, whereClause, whereArgs);
 	}
 
 	private void AddUsers(String name) {
 		// TODO Auto-generated method stub
     	//String Name = "Lionel";
     	Uri uri = SchedularProviderMetaData.UserNameTableMetaData.CONTENT_URI_NAME;
     	ContentResolver resolver= this.getContentResolver();
     	ContentValues values = new ContentValues();
     	values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME, name);
     	
     	String selection = SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME
     		      + "=? ";
           String[] selectionArgs = {name};
     	Cursor cursor = resolver.query(uri, null, selection, selectionArgs, null);
     	
     	
     	if(cursor != null && !cursor.moveToFirst()){
     		resolver.insert(uri, values);
     	}else{
     		Toast.makeText(this, "Add name failed!", Toast.LENGTH_SHORT).show();
     	}
     	cursor.close();
     	
 	}
 
 	public void onMenuImageClick(View v){
         switch (v.getId()){
             case R.id.imgPreviousMonth:
                 mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                 break;
             case R.id.imgNextMonth:
                 mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                 break;
             case R.id.imgToday:
                 mPager.setCurrentItem(getTodayMonthIndex());
                 break;
             case R.id.imgPopupMenu:
                 PopupMenu popup = new PopupMenu(this, v);
                 popup.inflate(R.menu.main);
                 popup.setOnMenuItemClickListener(this);
                 popup.show();
                 break;
             default:
                 break;
         }
         
     }
     
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		// TODO Auto-generated method stub
     	if(RESULT_OK == resultCode){
             switch (requestCode){
             case PICK_CLICK_CELL_REQUEST_CODE: 
             	int totalUser = intent.getIntExtra(
 						UserListActivity.TOTAL_USER_NUMBER, 1);
 				int checkedUser = intent.getIntExtra(
 						UserListActivity.CHECKED_USER_NUMBER, 1);
 
 				if ((totalUser - checkedUser) <= 1) {
 					mCellImgView.setImageDrawable(getResources().getDrawable(
 							R.drawable.img_cell_green));
 				} else if ((checkedUser * 2) >= totalUser) {
 
 					mCellImgView.setImageDrawable(getResources().getDrawable(
 							R.drawable.img_cell_yellow));
 				} else if ((checkedUser * 2) < totalUser) {
 					mCellImgView.setImageDrawable(getResources().getDrawable(
 							R.drawable.img_cell_red));
 				}
             	break;
             case PICK_ADD_USER_REQUEST_CODE:
             	String nameAdd = intent.getStringExtra(UserNameInformation.TAG_PERSON_NAME);
             	AddUsers(nameAdd);
             	break;
             case PICK_DELETE_USER_REQUEST_CODE:
             	String nameDelete = intent.getStringExtra(UserNameInformation.TAG_PERSON_NAME);
             	DeleteUsers(nameDelete);
             	break;
             default:
             	break;
             }
 		}
 
 		super.onActivityResult(requestCode, resultCode, intent);
 	}
 
 	public void onCellClick(View v) {
     	mCellImgView = (ImageView)v.findViewById(R.id.imgCellHint);
         //Toast.makeText(this, v.getTag().toString(), Toast.LENGTH_SHORT).show();
         Intent intentUserList = new Intent(Intent.ACTION_VIEW);
         intentUserList.setClass(this, UserListActivity.class);
         LunarCalendar date = (LunarCalendar)v.getTag();
         long cellMillis = date.getTimeInMillis();
         intentUserList.putExtra(CHOOSED_DAY, cellMillis);
         this.startActivityForResult(intentUserList, PICK_CLICK_CELL_REQUEST_CODE);
     }
     
 
     /**
      * has set the Date in Cell
      */
     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
         // TODO Auto-generated method stub
         int offset = (year - LunarCalendar.getMinYear()) * mMonthAYear + monthOfYear;
         mPager.setCurrentItem(offset);
     }
     
     private int getTodayMonthIndex(){
         Calendar today = Calendar.getInstance();
         final int mMonthAYear = 12;
         int offset = (today.get(Calendar.YEAR) - LunarCalendar.getMinYear()) * mMonthAYear
                 + today.get(Calendar.MONTH);
         return offset;
     }
     
     private class simplePageChangeListener extends ViewPager.SimpleOnPageChangeListener {
 
         @Override
         public void onPageSelected(int position) {
             // TODO Auto-generated method stub
             super.onPageSelected(position);
             
             StringBuilder title = new StringBuilder();
             title.append(LunarCalendar.getMinYear() + position / mMonthAYear);
             
             title.append('-');
             int month = position % mMonthAYear + 1;
             if (month < 10){
                 title.append('0');
             }
             
             title.append(month);
             mTxtTitleGregorian.setText(title);
             mTxtTitleLunar.setText("");
             mTxtTitleAddition.setText("");
             
             if ( position < (mPagerAdapter.getCount() -1)
               && !mImgNextMonth.isEnabled()) {
                 mImgNextMonth.setEnabled(true);
             }
             
             if (position > 0 && !mImgPreviousMonth.isEnabled()){
                 mImgPreviousMonth.setEnabled(true);
             }
         }
         
     }
 
 }
