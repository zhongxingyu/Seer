 package org.minyanmate.minyanmate.services;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import org.minyanmate.minyanmate.contentprovider.MinyanMateContentProvider;
 import org.minyanmate.minyanmate.database.MinyanEventsTable;
 import org.minyanmate.minyanmate.database.MinyanGoersTable;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.PhoneLookup;
 import android.telephony.PhoneNumberUtils;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * Checks whether a Minyan is active against the time stamps in 
  * {@link MinyanEventsTable} and whether the received Sms is from
  * someone in the {@link MinyanGoersTable}; if so, and if the
  * invited party has not previously responded (
  * {@link MinyanGoersTable#COLUMN_INVITE_STATUS} is 1), log their
  * attendance. 
  */
 public class OnSmsReceiver extends BroadcastReceiver{
 	
 	public static final String POSITIVE_RESPONSE = "!yes!";
 	public static final String NEGATIVE_RESPONSE = "!no!";
 	
 	final SmsManager smsManager = SmsManager.getDefault();
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// TODO Auto-generated method stub
 			
 		Bundle bundle = intent.getExtras();
 		 
 		try {
 		     
 		    if (bundle != null) {
 		         
 		        final Object[] pdusObj = (Object[]) bundle.get("pdus");
 		         
 		        for (int i = 0; i < pdusObj.length; i++) {
 		             
 		            SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
 		            String phoneNumber = currentMessage.getDisplayOriginatingAddress();
 		            String response = currentMessage.getDisplayMessageBody();
 		 
 		            Log.i("SmsReceiver", "senderNum: "+ phoneNumber + "; response: " + response);
 		            
		            /*
		            // Show alert
		            int duration = Toast.LENGTH_LONG;
		            Toast toast = Toast.makeText(context, "senderNum: "+ phoneNumber + ", message: " + message, duration);
		            toast.show();
		            */
 		            
 		            ContentResolver cr = context.getContentResolver();
 		            
 		            //Get contact ID associated with phone number which sent SMS
 		            Uri contentURI = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
 
 	                Cursor c = cr.query(contentURI, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup.LOOKUP_KEY }, null, null, null);
 		            
 		            if(c.getCount() < 1) {
 		            	Log.e("SmsReceiver", "Unidentifiable Phone Number");
 		            	return;
 		            }
 		            
 		            c.moveToFirst();
 		            String senderDisplayName = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 		            String senderLookupKey = c.getString(c.getColumnIndex(PhoneLookup.LOOKUP_KEY));
 		            c.close();
 		            
 		            //Retrieve & update Goer row associated with this Display Name
 		            contentURI = MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS;
 		            //String[] projection = new String[] { MinyanGoersTable.COLUMN_MINYAN_EVENT_ID, MinyanGoersTable.COLUMN_INVITE_STATUS, MinyanGoersTable.COLUMN_LOOKUP_KEY };
 		            String[] projection = null;
		            String selection = MinyanGoersTable.COLUMN_LOOKUP_KEY + " = ?";
 		            String[] selectionArgs = new String[] { senderLookupKey };
 		            String sortOrder = null;
 		            
 		            c = cr.query(contentURI, projection, selection, selectionArgs, sortOrder);
 		            
 		            if(c.getCount() < 1) {
 		            	Log.e("SmsReceiver", "Phone Number isn't associated with a MinyanMate Goer");
 		            	return;
 		            }
 		            
 		            c.moveToFirst();
 		            
 		            int eventId = c.getInt(c.getColumnIndex(MinyanGoersTable.COLUMN_MINYAN_EVENT_ID));
 		            int inviteStatus = c.getInt(c.getColumnIndex(MinyanGoersTable.COLUMN_INVITE_STATUS));
 		            int responseCode = 1;
 		            
 		            switch (inviteStatus) {
 		            case 2:
 		            case 3:
 		            	Log.i("SmsReceiver", "Already received RSVP from "+senderDisplayName+", updating to "+response+".");
 		            }
 		            
 		            if(response.toLowerCase(Locale.ENGLISH).equals("!yes!"))
 		            	responseCode = 2;
 		            else if(response.toLowerCase(Locale.ENGLISH).equals("!no!"))
 		            	responseCode = 3;
 		            
 		            ContentValues goerUpdates = new ContentValues();
 					goerUpdates.put(MinyanGoersTable.COLUMN_INVITE_STATUS, responseCode);
 					
 					selection = MinyanGoersTable.COLUMN_LOOKUP_KEY + " = ? AND " + MinyanGoersTable.COLUMN_MINYAN_EVENT_ID + " = ?";
 		            selectionArgs = new String[] { senderLookupKey, Integer.toString(eventId) };
 		            
 		            int rowsUpdated = cr.update(MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS, goerUpdates, selection, selectionArgs);
 		            
 		            if(rowsUpdated < 1)
 		            	Log.e("SmsReceiver", "MinyanMateGoersTable Update Failed");
 		            else if(rowsUpdated > 1)
 		            	Log.e("SmsReceiver", "MinyanMateGoersTable Updated Too Many Rows");
 		            
 		            c.close(); 
 		            
 		            //Check how many Goers are going to the event, update event completion status if attendance count > 9
 		            contentURI = MinyanMateContentProvider.CONTENT_URI_EVENT_GOERS;
 		            //projection = new String[] { MinyanGoersTable.COLUMN_GENERAL_NAME };
 		            projection = null;
 		            selection = MinyanGoersTable.COLUMN_MINYAN_EVENT_ID + " = ? AND " + MinyanGoersTable.COLUMN_INVITE_STATUS + " = 2";
 		            selectionArgs = new String[] { Integer.toString(eventId) };
 		            sortOrder = null;
 		            
 		            c = cr.query(contentURI, projection, selection, selectionArgs, null);
 		            
 		            if(c.getCount() > 9) {
 		            	
 		            	ContentValues eventUpdates = new ContentValues();
 		            	eventUpdates.put(MinyanEventsTable.COLUMN_IS_MINYAN_COMPLETE, 1);
 						
 						selection = MinyanEventsTable.COLUMN_ID + " = ?";
 			            selectionArgs = new String[] { Integer.toString(eventId) };
 			            
 			            rowsUpdated = cr.update(MinyanMateContentProvider.CONTENT_URI_EVENTS, eventUpdates, selection, selectionArgs);
 			            
 		            }
 		            
 		            c.close();
 		            
 		        } // end for loop
 		      } // bundle is null
 		 
 		} catch (Exception e) {
 		    Log.e("SmsReceiver", "Exception smsReceiver" +e);
 		     
 		}
 	}
 
 }
