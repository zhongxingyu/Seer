 package com.rnm.keepintouch.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.CallLog;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 
 import com.rnm.keepintouch.data.ContactEvent.TYPE;
 
 public class ContactsData {
 
 	List<Contact> contacts;
 	
 	public void update(Context context) {
 		Log.d("Contacts", "************************************ Starting run...");
 		long start = System.currentTimeMillis();
 		this.contacts = gatherData(context, contacts);
 		Log.d("Contacts", "************************************ Finishing contacts. elapsed time: "+(System.currentTimeMillis()-start));
 	}
 	
 	public List<Contact> gatherData(Context context, List<Contact> contacts) {
 		  long start = System.currentTimeMillis();
 		if (contacts == null) contacts = getContacts(context);
 		  Log.d("Contacts", "********* contactsmap1.elapsed time: "+(System.currentTimeMillis()-start));
 		  start = System.currentTimeMillis();
 		updateCallLogIntoList(context, contacts);
 		  Log.d("Contacts", "********* calllog.     elapsed time: "+(System.currentTimeMillis()-start));
 		  start = System.currentTimeMillis();
 //		try {
 			updateSMSIntoList(context, contacts);
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 		  Log.d("Contacts", "********* smslist.     elapsed time: "+(System.currentTimeMillis()-start));
 		return contacts;
 	}
 	
 	public void updateContact(Context context, Contact contact) {
 		Log.d("Contacts", "************************************ Starting run...");
 		long start = System.currentTimeMillis();
 		updateCallLogForContact(context, contact);
 		updateSMSForContact(context, contact);
 		Log.d("Contacts", "************************************ Finishing contacts. elapsed time: "+(System.currentTimeMillis()-start));
 	}
 	
 	
 	public List<Contact> getFavoriteContacts() {
 //		List<Contact> favorites = new ArrayList<Contact>();
 //		for (Contact c : contacts) if (c.starred) favorites.add(c);
 		Collections.sort(contacts, new Comparator<Contact>() {
 
 			@Override
 			public int compare(Contact lhs, Contact rhs) {
 				if (lhs.lastcontact < rhs.lastcontact)
 					return -1;
 				else if (lhs.lastcontact > rhs.lastcontact)
 					return 1;
 				else
 					return 0;
 			}
 		});
 //		for (Contact favorite : favorites) Log.d("Contact", "Favorite: "+favorite);
 
 		return contacts;
 	}
 	
 	
 	
 	private List<Contact> getContacts(Context context) {
 		ArrayList<Contact> contacts = new ArrayList<Contact>();
 		/**
 		 * http://www.higherpass.com/Android/Tutorials/Working-With-Android-Contacts/
 		 */
 		ContentResolver cr = context.getContentResolver();
 		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, 
 				new String[] {ContactsContract.Contacts._ID,
 				              ContactsContract.Contacts.DISPLAY_NAME, 
 				              ContactsContract.Contacts.STARRED,
 				              ContactsContract.Contacts.HAS_PHONE_NUMBER}, 
 				ContactsContract.Contacts.STARRED+" = ?", new String[] {"1"}, null);
 		if (cur.getCount() > 0) {
 			while (cur.moveToNext()) {
 				Contact contact = new Contact();
 				contact.id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
 				contact.name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 				contact.starred = Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.STARRED))) != 0;
 				contact.uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id).toString();
 				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 					
 					/** Query the secondary database to get the actual phone number **/
 					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 							new String[] {Phone.NUMBER, Phone.NORMALIZED_NUMBER},
 							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", 
 							new String[] { contact.id }, null);
 					while (pCur.moveToNext()) {
 						String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 						String normnumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
 						if (number != null) {
 							contact.phonenumber.add(number);
 							contact.normphonenumber.add(normnumber);
 						}
 					}
 					pCur.close();	
 					
 					/** Query the raw database to get the other IDs this contact goes by **/
 					 pCur = cr.query(RawContacts.CONTENT_URI,
 					          new String[]{RawContacts._ID},
 					          RawContacts.CONTACT_ID + "=?",
 					          new String[]{String.valueOf(contact.id)}, null);
 					while (pCur.moveToNext()) {
 						String id = pCur.getString(pCur.getColumnIndex(RawContacts._ID));
 						if (id != null) {
 							contact.rawids.add(id);
 						}
 					}
 					pCur.close();	
 
 
 					
 					contacts.add(contact); //only add if we have a phone number
 					Log.d("Contact", contact.toString());
 				}
 			}
 		}
 		
 		return contacts;
 	}
 	
 	
 	
 	private void updateCallLogIntoList(Context context, List<Contact> contacts) {
 		for (Contact contact : contacts) 
 			updateCallLogForContact(context, contact);
 	}
 	private void updateCallLogForContact(Context context, Contact contact) {
 		for (String number : contact.normphonenumber)
 			updateCallLogForContact(context, contact, number);
 	}
 	private void updateCallLogForContact(Context context, Contact contact, String number) {
		if (number == null) return; //nothing to do.
 		/**
 		 * http://malsandroid.blogspot.com/2010/06/accessing-call-logs.html
 		 */
 		Uri allCalls = Uri.parse("content://call_log/calls");
 
 		
 		Log.d("Contact", "Looking up :"+contact.name+ " with number: "+number);
 		
 		Cursor c = context.getContentResolver().query(allCalls, 
         		new String[] {CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.DURATION, "normalized_number"}/*null*/,
        		"normalized_number = ?", new String[] {number}, null);
         		/*"normalized_number = ?", new String[] {number}, null);*/
         if (c.moveToFirst()) {
            do {
 //				Log.d("Contacts", "row: "+Arrays.toString(c.getColumnNames()));
 //				for (String s : c.getColumnNames()) Log.d("Contacts", "     "+s+": "+c.getString(c.getColumnIndex(s)));
 //				Log.d("Contacts", "     "+CallLog.Calls._ID+": "+c.getString(c.getColumnIndex(CallLog.Calls._ID)));
 				
                String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
                long timestamp  = Long.parseLong(c.getString(c.getColumnIndex(CallLog.Calls.DATE)));
                int type = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)));
                int duration = Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.DURATION)));
 
                final int CALL_DURATION_THRESH = 1;
                //Only log a call if it's not a missed call, and it's a certain duration.
                if (duration >= CALL_DURATION_THRESH && type != CallLog.Calls.MISSED_TYPE) {
 	               
 	               ContactEvent event = new ContactEvent();
 	               event.type = TYPE.CALL;
 	               event.timestamp = timestamp;
 	               event.callType = type;
 	               event.number = num;
 	               
 	               contact.contactEvents.add(event);
 	               if (event.timestamp > contact.lastcontact) {
 	            	   contact.lastcontact = event.timestamp;
 	               }
                }
            } while (c.moveToNext());
         }	
 	}
 	
 	private void updateSMSIntoList(Context context, List<Contact> contacts) {
 		for (Contact contact: contacts)
 				updateSMSForContact(context, contact);
 	}
 	private void updateSMSForContact(Context context, Contact contact) {
 		for (String rawid : contact.rawids) 	
 			updateSMSForContact(context, contact, rawid);
 	}
 	private void updateSMSForContact(Context context, Contact contact, String rawid) {
 		Uri uri = Uri.parse("content://sms");
 		Log.d("Contact", "SMSLooking up :"+contact.name+ " with number: ["+rawid+"]");
 		Cursor c = context.getContentResolver().query(uri, new String[] {"MAX(date) as date", "type", "address", "person"}, "person = ?", new String[] {rawid}, null);
 		//Cursor c = context.getContentResolver().query(uri, null, null, null, null);
 
 		if (c.moveToFirst()) {
 			for (int i = 0; i < c.getCount(); i++) {
 //				Log.d("Contacts", "row: "+Arrays.toString(c.getColumnNames()));
 //				for (String s : c.getColumnNames()) Log.d("Contacts", "     "+s+": "+c.getString(c.getColumnIndex(s)));
 				
 				if (c.getString(c.getColumnIndexOrThrow("date")) != null) {
 					//String body = c.getString(c.getColumnIndexOrThrow("body")).toString();
 					long timestamp = Long.parseLong(c.getString(c.getColumnIndexOrThrow("date")).toString());
 					int type = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("type")).toString());
 					String number = c.getString(c.getColumnIndexOrThrow("address")).toString();
 					ContactEvent event = new ContactEvent();
 					event.type = TYPE.SMS;
 					event.timestamp = timestamp;
 					//event.message = body;
 					event.number = number;
 					event.callType = type;
 					
 					contact.contactEvents.add(event);
 					if (event.timestamp > contact.lastcontact) {
 						contact.lastcontact = event.timestamp;
 					}
 				}
 				c.moveToNext();
 
 			}
 		}
 		c.close();
 	}
 
 }
