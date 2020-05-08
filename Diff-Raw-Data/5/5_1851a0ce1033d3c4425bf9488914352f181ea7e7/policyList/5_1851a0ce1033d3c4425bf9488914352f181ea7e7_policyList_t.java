 package com.android.mobsec;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.android.mobsec.PolicyEntry;
 import com.android.mobsec.policyPref;
 import com.android.mobsec.policyElem.Elements;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class policyList extends ListActivity {
     /** Called when the activity is first created. */
     // Menu item ids
     public static final int MENU_ITEM_DELETE = Menu.FIRST;
     public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;	
     public static final int MENU_ITEM_PREFERENCE = Menu.FIRST + 2;
     
     private static boolean mPolSyncMode = true; // true for local mode and false for remote mode
     private static Context mCurContext = null;
     
     private static String  mRemoteServerAddr = null;
     /**
      * Standard projection for the interesting columns of a normal note.
      */
     private static final String[] PROJECTION = new String[] {
             Elements._ID, // 0
             Elements.NAME, // 1
     };   
     private static final String[] PROJECTION1 = new String[] {
         Elements._ID, // 0
         Elements.NAME, // 1
         Elements.TYPE, // 2
         Elements.IPADDR, //3
         Elements.NETMASK, //4
     };   
     
     private static final String LOCAL_FILE = "mobSec_lo.txt";
     private static final String REMOTE_FILE = "mobSec_bk.txt";
     
     /** The index of the title column */
     private static final int COLUMN_INDEX_ID = 0;
     private static final int COLUMN_INDEX_TITLE = 1;
     
     static final int PROGRESS_DIALOG = 0;
 
     ProgressThread progressThread;
     ProgressDialog progressDialog;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // If no data was given in the intent (because we were started
         // as a MAIN activity), then use our default content provider.
         Intent intent = getIntent();
         if (intent.getData() == null) {
             intent.setData(Elements.CONTENT_URI);
         }
         
         mCurContext = this;
         
         // Inform the list we provide context menus for items
         getListView().setOnCreateContextMenuListener(this);
         
         // Perform a managed query. The Activity will handle closing and requerying the cursor
         // when needed.
         Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                 Elements.DEFAULT_SORT_ORDER);
         
         // Used to map notes entries from the database to views
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.policy_list, cursor,
                 new String[] { Elements.NAME }, new int[] { R.id.policyList });
         setListAdapter(adapter);
     }
     
 	private byte[] onDownloadPolicy () {
         URL url;
 	    TelephonyManager telMan = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE));
 	    String deviceId = telMan.getDeviceId();
 		try {
 			url = new URL("http://"+ mRemoteServerAddr + "/" + deviceId + ".txt");
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return null;
 		}
         HttpURLConnection urlConnection;
 		try {
 			urlConnection = (HttpURLConnection) url.openConnection();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return null;
 		}
 		byte[] data = null;
         try {
             InputStream in = new BufferedInputStream(urlConnection.getInputStream());
             data = new byte[in.available()];
             in.read(data);
         }
         catch(IOException e) {
         	e.printStackTrace();
         }
         finally {
             urlConnection.disconnect();
         }
         return data;
     }
 	
 	private void onReadFile(String filename) {
 		File path = getExternalFilesDir(null);
 		File file = new File(path, filename);
 		InputStream is;
 		
 		deleteAllContentsData();
 		if(file.exists() == false) {
 			return;
 		}
 		
 		try {
 		    is = new BufferedInputStream(new FileInputStream(file));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		byte[] data = null;
 		try {
             data = new byte[is.available()];
             is.read(data);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		
		// skip file whose length is lower than 5
		if(data.length < 5) {
			return;
		}
		
     	String strPolicy = new String(data);
     	
     	String[] fields = strPolicy.split("\n");
     	int policyNum = fields.length;
     	for(int i = 0; i < policyNum; i++) {
     		String policyEntry[] = fields[i].split(" ");
     		Uri uri = getContentResolver().insert(Elements.CONTENT_URI, null);
             ContentValues values = new ContentValues();
             values.put(Elements.MODIFIED_DATE, System.currentTimeMillis());
             values.put(Elements.NAME, policyEntry[0]);
             values.put(Elements.TYPE, policyEntry[1]);
             values.put(Elements.IPADDR, policyEntry[2]);
             if(policyEntry[1].equalsIgnoreCase("0")) {
             	values.put(Elements.NETMASK, policyEntry[3]);
             }
             // Commit all of our changes to persistent storage. When the update completes
             // the content provider will notify the cursor of the change, which will
             // cause the UI to be updated.
             getContentResolver().update(uri, values, null, null);
     	}
 	}
     
     public native int updateFwAcl(String configPath);
     
     private void onSaveFile() {
 		OutputStream os;
 		File path = getExternalFilesDir(null);
 		File file = new File(path, LOCAL_FILE);
 		
 		if(file.exists() == false) {
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 		}
 			
 		try {
 			os = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 
         Cursor cursor = managedQuery(getIntent().getData(), PROJECTION1, null, null,
                 Elements.DEFAULT_SORT_ORDER);
         String name;
         String type;
         String ipAddr;
         String netMask;
 		String strSpa = new String(" ");
 		String strEnter = new String("\n");
 		String strType0 = new String("0");
 		if(cursor.moveToFirst() == false) {
 			return;
 		}
         while(cursor != null) {
         	name = cursor.getString(COLUMN_INDEX_TITLE);
         	type = cursor.getString(COLUMN_INDEX_TITLE + 1);
         	ipAddr = cursor.getString(COLUMN_INDEX_TITLE + 2);
         	netMask = cursor.getString(COLUMN_INDEX_TITLE + 3);
         
 			try {
 				os.write(name.getBytes());
 				os.write(strSpa.getBytes());
 				os.write(type.getBytes());
 				os.write(strSpa.getBytes());
 				os.write(ipAddr.getBytes());
 				if (type.compareTo(strType0) == 0)
 				{
 					os.write(strSpa.getBytes());
 					os.write(netMask.getBytes());
 				}
 				os.write(strEnter.getBytes());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if(cursor.moveToNext() == false) {
 				break;
 			}
         }
 		try {
 			os.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}    
 		
 		//inform msa firewall driver to update ACL configuration
 		String configFile = file.getAbsolutePath();
 		int ret;
 		
 		ret = updateFwAcl(configFile);
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         String strMode;
         
         deleteAllContentsData();
         
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         mRemoteServerAddr = prefs.getString("mobsec_remoteserver", new String(""));
         
         strMode = prefs.getString("list_preference", new String(""));
         if(strMode.equalsIgnoreCase(new String("remote"))) {
         	mPolSyncMode = false;
             setListAdapter(null);
             
             showDialog(PROGRESS_DIALOG);
         }
         else {
         	mPolSyncMode = true;
         	
         	onReadFile(LOCAL_FILE);
             // Perform a managed query. The Activity will handle closing and requerying the cursor
             // when needed.
             Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                     Elements.DEFAULT_SORT_ORDER);
             
             // Used to map notes entries from the database to views
             SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.policy_list, cursor,
                     new String[] { Elements.NAME }, new int[] { R.id.policyList });
             setListAdapter(adapter);
         }
     }
     
     private void deleteAllContentsData() {
         Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                 Elements.DEFAULT_SORT_ORDER);   	
         if(cursor.moveToFirst() == false) { 
         	return;
         }
         while(cursor != null) {
         	long id = cursor.getLong(COLUMN_INDEX_ID);
             Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
             getContentResolver().delete(noteUri, null, null);
             
             if(cursor.moveToNext() == false) {
             	break;
             } 
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	if(resultCode == RESULT_OK) {
     		// update main UI list
     		data.getData();
     		onSaveFile();
     	}
     }
         
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // This is our one standard application action -- inserting a
         // new note into the list.
         menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_add)
                 .setShortcut('3', 'a')
                 .setIcon(android.R.drawable.ic_menu_add);
         menu.add(1, MENU_ITEM_PREFERENCE, 1, R.string.menu_pref)
         .setShortcut('4', 'p')
         .setIcon(android.R.drawable.ic_menu_preferences);
 
         return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu (Menu menu) 
     {
     	super.onPrepareOptionsMenu(menu);
         
         if(mPolSyncMode == false) {
         	menu.setGroupEnabled(0, false);
         }
         else {
         	menu.setGroupEnabled(0, true);
         }
     	return true;
     }
         
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case MENU_ITEM_INSERT:
             // Launch activity to insert a new item
         	 try {
         		 startActivityForResult (new Intent(Intent.ACTION_INSERT, getIntent().getData(), this, PolicyEntry.class), 1); 
         	 }
         	 catch (android.content.ActivityNotFoundException e) {
         		  e.getClass();
         	 }
             break;
         case MENU_ITEM_PREFERENCE:
         	try {
         		startActivity (new Intent(this, policyPref.class));
         	}
        	 	catch (android.content.ActivityNotFoundException e) {
        	 		e.getClass();
        	 	}
         	break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
         
 	   	try {
 	   		startActivityForResult (new Intent(Intent.ACTION_EDIT, uri, this, PolicyEntry.class), 1); 
 	   	}
 		catch (android.content.ActivityNotFoundException e) {
 			e.getClass();
 		}
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
         AdapterView.AdapterContextMenuInfo info;
         try {
              info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         } catch (ClassCastException e) {
             return;
         }
 
         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
 
         // Setup the menu header
         menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));
 
         // Add a menu item to delete the note
         menu.add(0, MENU_ITEM_DELETE, 1, R.string.menu_delete)
         .setShortcut('4', 'd')
         .setIcon(android.R.drawable.ic_menu_delete);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info;
         try {
              info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (ClassCastException e) {
             return false;
         }
 
         switch (item.getItemId()) {
             case MENU_ITEM_DELETE: {
                 // Delete the note that the context menu is for
                 Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                 getContentResolver().delete(noteUri, null, null);
                 onSaveFile();
                 return true;
             }
         }
         return false;
     }
     
     /** Nested class that performs progress calculations (counting) */
     final class ProgressThread extends Thread {
         Handler mHandler;
         final static int STATE_DONE = 0;
         final static int STATE_RUNNING = 1;
         int mState;
         int total;
        
 		ProgressThread(Handler h) {
             mHandler = h;
         }
 		
 		public boolean postDeviceData() {
 		    // Create a new HttpClient and Post Header
 		    HttpClient httpclient = new DefaultHttpClient();
 		    HttpPost httppost = new HttpPost("http://" + mRemoteServerAddr + "/cgi-bin/devInit.pl");
 		    TelephonyManager telMan = ((TelephonyManager)getSystemService(TELEPHONY_SERVICE));
 		    String deviceId = telMan.getDeviceId();
 		    String version = Build.VERSION.RELEASE;
 		    
 		    try {
 		        // Add your data
 		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 		        nameValuePairs.add(new BasicNameValuePair("deviceId", deviceId));
 		        nameValuePairs.add(new BasicNameValuePair("version", version));
 		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 		        // Execute HTTP Post Request
 		        HttpResponse response = httpclient.execute(httppost);
 		        int stCode = response.getStatusLine().getStatusCode();
 		        if(stCode == 200) {
 		        	return true;
 		        }
 		    } catch (ClientProtocolException e) {
 		        // TODO Auto-generated catch block
 		    	e.getCause();
 		    } catch (IOException e) {
 		        // TODO Auto-generated catch block
 		    	e.getCause();
 		    }
 		    
 		    return false;
 		} 
        
         public void run() {
             mState = STATE_RUNNING;   
             total = 0;
             while (mState == STATE_RUNNING) {
             	if(postDeviceData() == false) {
                     Message msg = mHandler.obtainMessage();
                     msg.arg1 = 120;
                     mHandler.sendMessage(msg);
                     break;
             	}
             	
             	byte data[] = onDownloadPolicy();
             	if(data == null || data.length < 5) {
                     Message msg = mHandler.obtainMessage();
                     msg.arg1 = 103;
                     mHandler.sendMessage(msg);
                     break;
             	}
             	OutputStream os;
         		File path = getExternalFilesDir(null);
         		File file = new File(path, REMOTE_FILE);
         		
         		if(file.exists() == false) {
         			try {
         				file.createNewFile();
         			} catch (IOException e) {
         				// TODO Auto-generated catch block
         				e.printStackTrace();
         				return;
         			}
         		}
         			
         		try {
         			os = new FileOutputStream(file);
         		} catch (FileNotFoundException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         			return;
         		}
 
         		if(data != null) {
         			try {
         				// remove /r/n in each line
         				String strData = new String(data);
         				String[] dataArr = strData.split("\r\n");
         				String strEnter = new String("\n");
         				for(int i = 0; i < dataArr.length; i++)
         				{
         					os.write(dataArr[i].getBytes());
         					os.write(strEnter.getBytes());
         				}
         			} catch (IOException e) {
         				// TODO Auto-generated catch block
         				e.printStackTrace();
         			}
         			finally {
         				try {
         					os.close();
         				} catch (IOException e) {
         					// TODO Auto-generated catch block
         					e.printStackTrace();
         				}
         			}
         		}
         		
         		//inform msa firewall driver to update ACL configuration
         		String configFile = file.getAbsolutePath();
         		int ret;
         		
         		ret = updateFwAcl(configFile);
         		        		
             	String strPolicy = new String(data);
             	
             	String[] fields = strPolicy.split("\n");
             	int policyNum = fields.length;
             	for(int i = 0; i < policyNum; i++) {
             		String policyEntry[] = fields[i].split(" ");
             		Uri uri = getContentResolver().insert(Elements.CONTENT_URI, null);
                     ContentValues values = new ContentValues();
                     values.put(Elements.MODIFIED_DATE, System.currentTimeMillis());
                     values.put(Elements.NAME, policyEntry[0]);
                     values.put(Elements.TYPE, policyEntry[1]);
                     values.put(Elements.IPADDR, policyEntry[2]);
                     if(policyEntry[1].equalsIgnoreCase("0")) {
                     	values.put(Elements.NETMASK, policyEntry[3]);
                     }
                     // Commit all of our changes to persistent storage. When the update completes
                     // the content provider will notify the cursor of the change, which will
                     // cause the UI to be updated.
                     getContentResolver().update(uri, values, null, null);
             	}
             	
             	total += 100;
                 Message msg = mHandler.obtainMessage();
                 msg.arg1 = total;
                 mHandler.sendMessage(msg);
                 total++;
                 break;
             }
         }
         
         /* sets the current state for the thread,
          * used to stop the thread */
         public void setState(int state) {
             mState = state;
         }
     }
     
     // Define the Handler that receives messages from the thread and update the progress
     final Handler handler = new Handler() {
         public void handleMessage(Message msg) {
             int total = msg.arg1;
             progressDialog.setProgress(total);
             if (total >= 100){
                 dismissDialog(PROGRESS_DIALOG);
                 progressThread.setState(ProgressThread.STATE_DONE);
                 Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                         Elements.DEFAULT_SORT_ORDER);
                 
                 // Used to map notes entries from the database to views
                 SimpleCursorAdapter adapter = new SimpleCursorAdapter(mCurContext, R.layout.policy_list, cursor,
                         new String[] { Elements.NAME }, new int[] { R.id.policyList });
                 setListAdapter(adapter);
                
             }
         }
     };
     
     protected Dialog onCreateDialog(int id) {
         switch(id) {
         case PROGRESS_DIALOG:
             progressDialog = new ProgressDialog(policyList.this);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             progressDialog.setMessage(getString(R.string.progress_message));
             return progressDialog;
         default:
             return null;
         }
     }
     
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch(id) {
         case PROGRESS_DIALOG:
             progressDialog.setProgress(0);
             progressThread = new ProgressThread(handler);
             progressThread.start();
         }
     }
 
     static {
         System.loadLibrary("msaFw");
     }
 }
