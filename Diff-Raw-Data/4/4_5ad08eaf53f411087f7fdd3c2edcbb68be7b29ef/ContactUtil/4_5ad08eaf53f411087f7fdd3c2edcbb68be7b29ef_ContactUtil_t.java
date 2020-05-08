 package com.snot.smswakeup;
 
 import android.content.Context;
 import android.util.Log;
 import android.content.ContentResolver;
 import android.net.Uri;
 import android.provider.ContactsContract.PhoneLookup;
 import android.database.Cursor;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.content.ContentUris;
 import android.widget.Toast;
 import android.provider.ContactsContract.Contacts;
 
 /**
  * @author snot
  *
  */
 
 
 public class ContactUtil {
 	private static final String TAG = "ContactUtil";
 
 /**
  * Returns an array of CONTACT_ID's associated with phoneNumber
  *
  * @param context Application context
  * @param phoneNumber The phone number used to query
  * @return An array of CONTACT_ID's
  *
  */
 	public static long[] getContactIdsByPhoneNumber(Context context, String phoneNumber) {
 		ContentResolver contentResolver = context.getContentResolver();
 		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
 		String[] projection = new String[] { PhoneLookup._ID };
 		Cursor cursor = contentResolver.query(uri, projection, null, null, null);
 		if (cursor == null || !cursor.moveToFirst()) {
 			return null;
 		}
 
 		int count = cursor.getCount();
		Log.d(TAG, "cursor.getCount: " + count);
 		// TODO: is getCount zero indexed... ???
 		long[] ids = new long[count];
 		for(int i = 0; i < count; i++, cursor.moveToNext())
 		{
			ids[i] = cursor.getLong(0);
 		}
 		return ids;
 	}
 
 // TODO: perhaps rename to getContactIdByDataUri
 	public static long getContactIdByUri(Context context, Uri uri)
 	{
 		String[] projection = new String[] { Data.CONTACT_ID };
 		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
 		long id = -1;
 		if(cursor.moveToFirst()) {
 			id = cursor.getLong(0);
 		}
 		return id;
 	}
 
 	public static String getContactName(Context context, long id) {
 		ContentResolver contentResolver = context.getContentResolver();
 		Uri uri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(id));
 		String[] projection = new String[] { Contacts.DISPLAY_NAME };
 		Cursor cursor = contentResolver.query(uri, projection, null, null, null);
 		if(cursor == null) {
 			return null;
 		}
 		String contactName = null;
 		if(cursor.moveToFirst()) {
 			//contactName = cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
 			contactName = cursor.getString(0);
 		}
 		if(cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		return contactName;
 	}
 
 //	public static String getContactName(Context context, String phoneNumber) {
 //		ContentResolver contentResolver = context.getContentResolver();
 //		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
 //		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID };
 //		Cursor cursor = contentResolver.query(uri, projection, null, null, null);
 //		if (cursor == null) {
 //			return null;
 //		}
 //		String contactName = null;
 //		String id = null;
 //		if(cursor.moveToFirst()) {
 //			id = cursor.getString(cursor.getColumnIndex(PhoneLookup._ID));
 //			contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 //		}
 //		if(cursor != null && !cursor.isClosed()) {
 //			cursor.close();
 //		}
 //		Toast.makeText(context, "id: " + id + "\nname: " + contactName, Toast.LENGTH_SHORT).show();
 //		return contactName;
 //	}
 
 //	public static String getPhoneNumber(Context context, Uri uri)
 //	{
 //		Toast.makeText(context, "uri: " + uri.toString(), Toast.LENGTH_SHORT).show();
 //		Toast.makeText(context, "id: " + ContentUris.parseId(uri), Toast.LENGTH_SHORT).show();
 //		
 //		String[] projection = {Phone.NUMBER, Contacts._ID, Contacts.Entity.RAW_CONTACT_ID};
 //		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
 //		cursor.moveToFirst();
 //		// Retrieve the phone number from the NUMBER column
 //		int column = cursor.getColumnIndex(Phone.NUMBER);
 //		int idx = cursor.getColumnIndex(Contacts._ID);
 //		int raw_idx = cursor.getColumnIndex(Contacts.Entity.RAW_CONTACT_ID);
 //
 //		String phoneNumber = null;
 //		if(column != -1)
 //		{
 //				phoneNumber = cursor.getString(column);
 //		}
 //
 //		String id = cursor.getString(idx);
 //		String raw_id = cursor.getString(raw_idx);
 //
 //		Toast.makeText(context, "id: " + id + "\nraw id: " + raw_id, Toast.LENGTH_SHORT).show();
 //
 //		cursor.close();
 //		return phoneNumber;
 //	}
 }
 
