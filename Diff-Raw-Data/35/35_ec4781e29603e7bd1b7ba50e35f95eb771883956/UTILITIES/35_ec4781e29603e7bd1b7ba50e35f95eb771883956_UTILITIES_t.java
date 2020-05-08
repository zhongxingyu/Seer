 package com.example.MIIOW;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.provider.ContactsContract;
 import android.telephony.SmsManager;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /*
     Miscellaneous constants and functions to be used by the application.
  */
 public class UTILITIES {
     //Constant strings
     public static String ACCOUNT_KEY = "ywdrXOJdA6pYydpGGGmIDTrM88ZVXW";
     public static String ACCOUNT_SECRET = "k8WfVWexlQOKRSIgkJmU3H27Lc3ziY";
     public static String API_URL = "http://app.smartfile.com/api/2";
     public static final String TWITTER_ACCESS_TOKEN = "1348177470-n4VCuo43YD8NFZTO2AgviBzm95Dgd4xom5IY4LS";
     public static final String TWITTER_ACCESS_TOKEN_SECRET = "PJ6FKAXHZGedaFhsLozF4bcMqm4RKozJoBx6iRFDayI";
     public static final String TWITTER_CONSUMER_KEY = "UJaAlQAjwN19yATvN5t3A";
     public static final String TWITTER_CONSUMER_SECRET = "wxn6QvE44Mja9gB6nL7G02HN816196lRZVsk5uebcws";
 
     /*
         @param c The activity context
         Returns true if the activity is online, otherwise returns false
      */
     public static boolean isOnline(Context c) {
         ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo info = cm.getActiveNetworkInfo();
         if (info != null)
             if (info.isConnected())
                 return true;
         return false;
     }
 
     /*
         @param c Activity context
         returns a list of all phone numbers in the phone's contact list
      */
     public static ArrayList<String> getPhoneNumbers(Context c) {
 
         ArrayList<String> numberList = new ArrayList<String>();
         Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
         while (phones.moveToNext())
             numberList.add(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
         return numberList;
     }
 
     /*
         Sends a text message to every phone in the given list.
 
         @param phoneNumber a list of phone numbers
         @param message the SMS message to send
        @param c the calling activity
      */
    public static void sendSMS(ArrayList <String> phoneNumber, String message) {
         SmsManager sms = SmsManager.getDefault();
        ArrayList<String> mArray=sms.divideMessage(message);
        for(String number:phoneNumber)
            sms.sendMultipartTextMessage(number, null, mArray, null, null);
     }
 
     /*
         Gets a list of the emails from every contact in the phone's contact list
 
         @param context the Activity context
      */
     public static String getEmails(Context context) {
         String emlRecs = "";
         HashSet<String> emlRecsHS = new HashSet<String>();
         ContentResolver cr = context.getContentResolver();
         String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                 ContactsContract.Contacts.DISPLAY_NAME,
                 ContactsContract.Contacts.PHOTO_ID,
                 ContactsContract.CommonDataKinds.Email.DATA,
                 ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
         String order = "CASE WHEN "
                 + ContactsContract.Contacts.DISPLAY_NAME
                 + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                 + ContactsContract.Contacts.DISPLAY_NAME
                 + ", "
                 + ContactsContract.CommonDataKinds.Email.DATA
                 + " COLLATE NOCASE";
         String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
         Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
         if (cur.moveToFirst()) {
             do {
                 String emlAddr = cur.getString(3);
                 // keep unique only
                 if (emlRecsHS.add(emlAddr.toLowerCase())) {
                     emlRecs += emlAddr + ",";
                 }
             } while (cur.moveToNext());
         }
         cur.close();
         return emlRecs;
     }
 }
