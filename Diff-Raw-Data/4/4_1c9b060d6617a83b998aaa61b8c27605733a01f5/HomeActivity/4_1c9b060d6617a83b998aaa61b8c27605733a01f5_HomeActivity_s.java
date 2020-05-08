 package com.hackathon.locateme;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.hackathon.locateme.services.IncomingUpdateService;
 import com.hackathon.locateme.utility.IncomingDBAdapter;
 import com.hackathon.locateme.utility.SmsUtility;
 
 public class HomeActivity extends Activity {
 	
 	private static final String TAG = HomeActivity.class.getName();
 	public static final int CONTACT_RESULT = 0;
 	
 	private HomeModel m_model;
 	private boolean m_isRetainingNonConfig;
 	private final int STOP_SERVICE = 1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_layout);
         
         m_model = (HomeModel) getLastNonConfigurationInstance();
         if(m_model != null)
         {
         	m_model.resetActivity(this);
         	m_model.attachViewsToActivity();
         }
         else
         {
         	m_model = new HomeModel(this);
         }
     }
 
     @Override
     public HomeModel onRetainNonConfigurationInstance() 
     {
     	m_isRetainingNonConfig = true;
         m_model.releaseViewsFromActivity();
         return m_model;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
         menu.add(0, STOP_SERVICE  , 0, "Kill location broadcast service.");
         return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case STOP_SERVICE:
         	Intent stopBroadcasting = new Intent(this, IncomingUpdateService.class);
             this.stopService(stopBroadcasting);
             return true;
         }
        
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
     	if(requestCode == CONTACT_RESULT)
     	{
     		if(resultCode == Activity.RESULT_OK)
     		{
     			/*
     			 * Get the selected contacts name and phone number, and save this to the incoming database.
     			 * Send the text message info.
     			 * 
     			 * TODO check their phone number against the database record (server side). if they have
     			 * downloaded the app, send parseable text message, otherwise send human readable.
     			 */
     			String theirNumber = null;
     			String name = null;
     			// get contact data, create SMS and send
     			Uri uri = data.getData();
     			ContentResolver cr = getContentResolver();
     			Cursor cur = cr.query(uri,
     	                null, null, null, null);
     	        if (cur.getCount() > 0)
     	        {
 	    		    while (cur.moveToNext()) 
 	    		    {
 	    		        String id = cur.getString(
 	    	                        cur.getColumnIndex(ContactsContract.Contacts._ID));
 	    		        name = cur.getString(
 	    	                        cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 		    	 		if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
 		    	 		{
 		    	 			Cursor pCur = cr.query(
 		    	 		 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 		    	 		 		    null, 
 		    	 		 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
 		    	 		 		    new String[]{id}, null);
 		    	 		 	        while (pCur.moveToNext()) {
 		    	 		 		       theirNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 		    	 		 	        } 
 		    	 		 	        pCur.close();
 		    	 	    }
 	    		    }
 	    		    // store name/phoneNumber for pending incoming
 	    		    IncomingDBAdapter db = new IncomingDBAdapter(this);
 	    		    db.createEntry(name, theirNumber, null, null, "false");
	    		    
 	    		    String myLocation = m_model.getCurrentLocation();
 	    		    if(myLocation != null)
 	    		    {
 	    		    	TelephonyManager mTelephonyMgr;
 				        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
 				        String myNumber = mTelephonyMgr.getLine1Number();
 				    	SmsUtility.sendLocationText(theirNumber, myNumber, myLocation, name);
 	    		    }
 	    		    else
 	    		    {
 	    		    	m_model.createErrorToast(R.string.no_location);
 	    		    }
 
     	        }
     	        else
     	        {
     	        	Log.e(TAG, "Weird error, no contacts in cursor.");
     	        	throw new IllegalStateException();
     	        }
     		}
     		else
     		{
     			// error, make error toast
     			m_model.createErrorToast(R.string.contact_retrieval_error);
     		}
     	}
     }
    
     @Override
 	protected void onDestroy()
     {
     	super.onDestroy();
     	if(!m_isRetainingNonConfig)
     	{
     		try
 			{
 				m_model.close();
 			}
 			catch (IOException e)
 			{
 			}
     	}
     }
 }
