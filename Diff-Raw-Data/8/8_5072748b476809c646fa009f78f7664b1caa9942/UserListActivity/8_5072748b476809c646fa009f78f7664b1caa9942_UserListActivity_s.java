 package com.mycode.mycalendar;
 
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class UserListActivity extends ListActivity implements OnCheckedChangeListener{
 
 	private static final int PICK_AVAILABLE_STYLE_REQUEST = 10;
 	public static final String TOTAL_USER_NUMBER = "TotalUserNumber";
 	public static final String CHECKED_USER_NUMBER = "CheckedUserNumber";
 	
     private ArrayList<HashMap<String, Object>> mListItems;
     private ListView mListView = null;
    // private String[] mUserNames = new String[]{"Bob", "Jimmy", "Tim"};
     private ArrayList<HashMap<String, Object>> mUserInformation;
     private String[] mItemControlsName = new String[]{"nameCheck", "txtUserName", "txtUserId"};
     private int[] mListItemControls = new int[]{R.id.nameCheck, R.id.txtUserName, R.id.txtUserId};
     private int mAvailableStyle = 0;
     private long mPickDate = 0;
     private int mItemId = 0;
     private String mItemName = null;
     private boolean mHasRecord = false;
     private ContentResolver mResolver = null;
     private Cursor mCursor = null;
     private boolean misListInit = false;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // TODO Auto-generated method stub
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.view_user_list);
          
         mListItems = new ArrayList<HashMap<String,Object>>(); 
 
         MySimpleAdapter adapter = new MySimpleAdapter( 
                 this, 
                 mListItems,
                 R.layout.view_name_list,
                 mItemControlsName,
                 mListItemControls); 
        
         this.setListAdapter(adapter);
         
         
     	Uri uri = SchedularProviderMetaData.UserNameTableMetaData.CONTENT_URI_NAME;
     	ContentResolver resolver= this.getContentResolver();
     	
     	Cursor cursor = resolver.query(uri, null, null, null, null);
     	String[] userNames = null;
     	if(null != cursor){
     		userNames = new String[cursor.getCount()];
     		int index = 0;
     		int iName = cursor.getColumnIndex(SchedularProviderMetaData.UserNameTableMetaData.COLUMN_USER_NAME);
     		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
     			userNames[index++] = cursor.getString(iName);
     		}
     	}
     	
 		if (userNames != null) {
 			for (int i = 0; i < userNames.length; i++) {
 				HashMap<String, Object> map = new HashMap<String, Object>();
 				map.put(mItemControlsName[0], false);
 				map.put(mItemControlsName[1], userNames[i]);
 				mListItems.add(map);
 			}
 		}
         Intent intent = this.getIntent();
         mPickDate = intent.getLongExtra(MainActivity.CHOOSED_DAY, System.currentTimeMillis());
         mResolver = this.getContentResolver();
         mListView = getListView();
         
         //simulate to add user
         
         QueryDatabase();
         
         
         //insertARecord();
         //UpdateControl();
     }
     
     @Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
     	if (mCursor != null){
     		mCursor.close();
     		mCursor = null;
     	}
 		super.onDestroy();
 	}
 
 	private void insertARecord() {
 		// TODO Auto-generated method stub
     	 Uri uri = SchedularProviderMetaData.SchedularTableMetaData.CONTENT_URI;
 	     ContentValues values = new ContentValues();
 	     values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_ID, 1);
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME, "Jim");
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE, mPickDate);
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE, 4);
 		 mResolver.insert(uri, values);
     }
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		
 		
 		super.onStart();
 	}
 	
 	private void QueryDatabase(){
         Uri uri = SchedularProviderMetaData.SchedularTableMetaData.CONTENT_URI;
 		
 		String selection = SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE
   		      + "=? ";
         String[] selectionArgs = {String.valueOf(mPickDate)};
 
         
 		mCursor = mResolver.query(uri,
                 null,
                 selection,
                 selectionArgs,
                 null);
 		/*
 		int iUserId = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_ID);
 		int iUserName = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME);
 		int iDate = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE);
 		int iAvalibleStyle = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE);
 		
 		int[] views = new int[]{R.id.txtUserName, R.id.nameCheck};
 		String[] columns = new String[]{SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME,
 				SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE};
 */
 	}
 	
 	
 	private void UpdateControl(CheckBox cb, TextView txtView) {
 		
 		int iAvalibleStyle = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE);
 		int iUserName = mCursor.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME);
 		
 		if(mCursor.moveToFirst()){
 			for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()){
 				String userName = mCursor.getString(iUserName);
 				if(userName.equals(txtView.getText())){
 					 cb.setChecked( (boolean)(0 != mCursor.getInt(iAvalibleStyle)));
 				}
 			}
 		}
 	}
 	
 
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         // TODO Auto-generated method stub
         if (PICK_AVAILABLE_STYLE_REQUEST == requestCode){
             if (RESULT_OK == resultCode){
 
                 boolean isAllDayStyle = intent.getBooleanExtra(ChooseAvailableType.TAG_ALLDAY, false);
                 boolean isMorningStyle = intent.getBooleanExtra(ChooseAvailableType.TAG_MORNING, false);
                 boolean isAfternoonStyle = intent.getBooleanExtra(ChooseAvailableType.TAG_AFTERNOON, false);
                 boolean isEveningStyle = intent.getBooleanExtra(ChooseAvailableType.TAG_EVENING, false);
                 
                 
                 mAvailableStyle = ((isAllDayStyle?1:0) << 3)
                         | ((isMorningStyle?1:0) << 2)
                         | ((isAfternoonStyle?1:0) << 1)
                         | (isEveningStyle?1:0);
                 
                 
                 SubmitRecord();
                 
                 UpdateList();
                 
                 mAvailableStyle = 0;
             }
         }
         
         super.onActivityResult(requestCode, resultCode, intent);
         
     }
 
 	private void UpdateList(){
 		
 		int listItemCount = mListView.getCount();
 		for (int i = 0; i < listItemCount; i++) {
 			View view = (View) mListView.getChildAt(i);
 			CheckBox cb = (CheckBox) view.findViewById(R.id.nameCheck);
 			TextView txtName = (TextView) view.findViewById(R.id.txtUserName);
 			
 			if (mItemName.equals(txtName.getText().toString())){
 				cb.setChecked((boolean) (0 != mAvailableStyle));
 			}
 		}
 	}
 	private void SubmitRecord() {
 		// TODO Auto-generated method stub
 		 Uri uri = SchedularProviderMetaData.SchedularTableMetaData.CONTENT_URI;
 	     ContentValues values = new ContentValues();
 	     values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_ID, mItemId);
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_NAME, mItemName);
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE, mPickDate);
 		 values.put(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE, mAvailableStyle);
 		 
          
          
          if (mHasRecord){
              
              String where = SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_ID 
             		      + "=? AND "
             		      + SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE
             		      + "=? ";
              String[] whereClause = {String.valueOf(mItemId), String.valueOf(mPickDate)};
              
              mResolver.update(uri, values, where, whereClause);
              mHasRecord = false;
          }else{
              mResolver.insert(uri, values);
          }
          
          
 	}
 	
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         // TODO Auto-generated method stub
         super.onListItemClick(l, v, position, id);
         CheckBox cb = (CheckBox) v.findViewById(R.id.nameCheck);
         TextView txtName = (TextView)v.findViewById(R.id.txtUserName);
         mItemId = (Integer)cb.getTag();
         mItemName = txtName.getText().toString();
         Intent intentChooseAvailableType = new Intent(Intent.ACTION_VIEW);
         intentChooseAvailableType.setClass(this, ChooseAvailableType.class);
 
         //this part should be in the work thread
         Uri uri = SchedularProviderMetaData.SchedularTableMetaData.CONTENT_URI;
 		
 		String selection = SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_USER_ID
   		      + "=? AND "
   		      + SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_DATE
               + "=? ";
         String[] selectionArgs = {String.valueOf(mItemId), String.valueOf(mPickDate)};
 
         
 		Cursor cursor = mResolver.query(uri,
                 null,
                 selection,
                 selectionArgs,
                 null);
 		
 		int iAvalibleStyle = cursor
 				.getColumnIndex(SchedularProviderMetaData.SchedularTableMetaData.SCHEDULAR_AVAILABLE_STYLE);
 		int avalibleStyle = 0;
 		if (cursor.moveToFirst()) {
 			mHasRecord = true;
 			avalibleStyle = cursor.getInt(iAvalibleStyle);
 		}
 		cursor.close();
 		
 		boolean isAllDayStyle = (boolean)((avalibleStyle & 0x08) != 0);
         boolean isMorningStyle = (boolean)((avalibleStyle & 0x04) != 0);
         boolean isAfternoonStyle = (boolean)((avalibleStyle & 0x02) != 0);
         boolean isEveningStyle = (boolean)((avalibleStyle & 0x01) != 0);
         
 		intentChooseAvailableType.putExtra(ChooseAvailableType.TAG_ALLDAY, isAllDayStyle);
 		intentChooseAvailableType.putExtra(ChooseAvailableType.TAG_MORNING, isMorningStyle);
 		intentChooseAvailableType.putExtra(ChooseAvailableType.TAG_AFTERNOON, isAfternoonStyle);
 		intentChooseAvailableType.putExtra(ChooseAvailableType.TAG_EVENING, isEveningStyle);
 		
 		
         this.startActivityForResult(intentChooseAvailableType, PICK_AVAILABLE_STYLE_REQUEST);
         //Toast.makeText(this, "position"+position+"id"+id, Toast.LENGTH_LONG).show();
     }
     
     
     private class MySimpleAdapter extends SimpleAdapter {
 
         public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
                 String[] from, int[] to) {
             super(context, data, resource, from, to);
             // TODO Auto-generated constructor stub
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             // TODO Auto-generated method stub
 			View view = super.getView(position, convertView, parent);
 			if (view != null) {
 				CheckBox cb = (CheckBox) view.findViewById(R.id.nameCheck);
 				TextView txtView = (TextView) view
 						.findViewById(R.id.txtUserName);
 				cb.setTag(txtView.getText().hashCode());
 				cb.setOnCheckedChangeListener(UserListActivity.this);
 				
 				UpdateControl(cb, txtView);
 				
 				misListInit = true;
 				
 			}
 			return view;
 		}
     
     }
 
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.user_list_select_menu, menu);
         return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch(item.getItemId()){
         case R.id.menu_action_submit:
             
             
             Intent intent = new Intent();  
             int totalUser = mListView.getCount();
             int checkedUser = 0;
             intent.putExtra(TOTAL_USER_NUMBER, totalUser);
             
             for (int i = 0; i < totalUser; i++) {
     			View view = (View) mListView.getChildAt(i);
     			CheckBox cb = (CheckBox) view.findViewById(R.id.nameCheck);
     			if(cb.isChecked()){
     				checkedUser++;
     			}
     		}
             intent.putExtra(CHECKED_USER_NUMBER, checkedUser);
             
             setResult(RESULT_OK, intent);  
             this.finish();
             break;
         default:
             break;
     }
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         // TODO Auto-generated method stub
         int row = (Integer) buttonView.getTag();
         //Toast.makeText(this, ""+row+"еcheckBox", Toast.LENGTH_LONG).show();
     }
 
 }
 
