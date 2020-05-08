 package com.android.movingServer;
 
 
 import uit.nfc.EasyNdef;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.nfc.NdefMessage;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class MovingApplicationActivity extends ListActivity {
     /** Called when the activity is first created. */
     
 	private static final int ACTIVITY_CREATE_BOX = 0;
 	private static final int ACTIVITY_EDIT_BOX = 1;
 	private static final int ACTIVTY_ITEMS_LIST = 2;
 	private static final int ACTIVTY_CREATE_TAG = 3;
 	
 	private MovingDbAdapter mDbHelper;
 	private Cursor mMovingCursor;
 	
 	private static final int INSERT_ID = Menu.FIRST;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private static final int EDIT_BOX = Menu.FIRST + 2;
 	private static final int CREATE_TAG = Menu.FIRST + 3;
 	
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.box_list);
         
         mDbHelper = new MovingDbAdapter(this);
         mDbHelper.open();
                 
         fillData();
         
         registerForContextMenu(getListView());
     }
     
     private void fillData() {
    	Toast.makeText(this, "Why2?", 1000).show();
     	
     	mMovingCursor = mDbHelper.fetchAllBoxes();
     	startManagingCursor(mMovingCursor);
     	
     	String[] from = new String[]{MovingDbAdapter.KEY_BOX_NAME, MovingDbAdapter.KEY_BOX_DESC};
     	
     	int[] to = new int[]{R.id.name, R.id.description};
     	
     	SimpleCursorAdapter boxes =
     		new SimpleCursorAdapter(this, R.layout.box_row, mMovingCursor, from, to);
     	setListAdapter(boxes);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, INSERT_ID, 0, R.string.addBoxMenu);
         //menu.add(0, DELETE_ALL, 0, R.string.menu_delete_all);
         return true;
     }
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
         case INSERT_ID:
             createBox();
         	return true;
         }
         
         return super.onMenuItemSelected(featureId, item);
     }
     
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, CREATE_TAG, 0, R.string.boxListMenuCreateTag);
 		menu.add(0, EDIT_BOX, 0, R.string.boxListMenuEdit);
 		menu.add(0, DELETE_ID, 0, R.string.boxListMenuDelete);
 		
 	}
     
     @Override
 	public boolean onContextItemSelected(MenuItem item) {
     	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
     	switch (item.getItemId()){
 		case DELETE_ID:
 			
 	        mDbHelper.deleteBox(info.id);
 	        fillData();
 			return true;
 		
 		case EDIT_BOX:
 
 			Cursor c = mMovingCursor;
 	        c.moveToPosition(info.position);
 	        Intent i = new Intent(this, BoxEdit.class);
 	        i.putExtra(MovingDbAdapter.KEY_BOX_ID, info.id);
 	        i.putExtra(MovingDbAdapter.KEY_BOX_NAME, c.getString(
 	                c.getColumnIndexOrThrow(MovingDbAdapter.KEY_BOX_NAME)));
 	        i.putExtra(MovingDbAdapter.KEY_BOX_DESC, c.getString(
 	                c.getColumnIndexOrThrow(MovingDbAdapter.KEY_BOX_DESC)));
 	        startActivityForResult(i, ACTIVITY_EDIT_BOX);
 			return true;
 		
     	case CREATE_TAG:
     		createTag(info.id);
     		
     		//Intent ci = new Intent(this, CreateTag.class);
     		//ci.putExtra(MovingDbAdapter.KEY_BOX_ID, info.id);
     		//startActivityForResult(ci, ACTIVTY_CREATE_TAG);
     		return true;
     	}
     	
     	return super.onContextItemSelected(item);
 	}
     
     protected void onListItemClick(ListView l, View v, int position, long id){
     	super.onListItemClick(l, v, position, id);
     	Intent i = new Intent(this, itemList.class);
     	i.putExtra(MovingDbAdapter.KEY_BOX_ID, id);
     	startActivityForResult(i, ACTIVTY_ITEMS_LIST);
     }
 	
 	
     private void createBox() {
     	Intent i = new Intent(this, BoxEdit.class);
     	startActivityForResult(i, ACTIVITY_CREATE_BOX);
     }
     
     private void createTag(long BID) {
     	
     	//NdefMessage tagContent = EasyNdef.ndefFromString("TEST");
     	
     	Intent i = new Intent(this, CreateTag.class);
     	
    	i.putExtra(CreateTag.TAG_TEXT, "TEST");
     	
     	startActivity(i);
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	super.onActivityResult(requestCode, resultCode, intent);
     	
     	Bundle extras = intent.getExtras();
         
     	switch(requestCode){
         
         case ACTIVITY_CREATE_BOX:
         	String BoxName = extras.getString(MovingDbAdapter.KEY_BOX_NAME);
         	String BoxDescription = extras.getString(MovingDbAdapter.KEY_BOX_DESC);
         	if (BoxName.length() == 0){
             	BoxName = "Div box";
             }
         	
         	if (BoxDescription.length() == 0){
             	BoxDescription = "no description";
             }
         	
         	mDbHelper.createBox(BoxName, BoxDescription);
         	fillData();
         	break;
         
         case ACTIVITY_EDIT_BOX:
         	String NewBoxName = extras.getString(MovingDbAdapter.KEY_BOX_NAME);
         	String NewBoxDescription = extras.getString(MovingDbAdapter.KEY_BOX_DESC);
         	Long BoxID = extras.getLong(MovingDbAdapter.KEY_BOX_ID);
         	
         	mDbHelper.editBox(BoxID, NewBoxName, NewBoxDescription);
         	fillData();
         	break;
         	
         case ACTIVTY_ITEMS_LIST:
         	fillData();
         	break;
     	}
     }
     		
 }
