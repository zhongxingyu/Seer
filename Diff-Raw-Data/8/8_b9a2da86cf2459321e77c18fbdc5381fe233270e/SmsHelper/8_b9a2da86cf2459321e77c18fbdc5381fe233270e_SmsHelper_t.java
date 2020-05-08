 package com.android.systemui.statusbar.cmcustom;
 
 import android.content.Context;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.provider.CallLog.Calls;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.PhoneLookup;
 import android.util.Log;
 import android.text.SpannableStringBuilder;
 import android.text.TextUtils;
 import com.android.internal.util.EmojiParser;
 import com.android.internal.util.SmileyParser;
 
 import java.io.InputStream;
 
 public class SmsHelper {
 
     private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
     private static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
 
     private static final String SMS_ID = "_id";
     private static final String SMS_READ_COLUMN = "read";
     private static final String UNREAD_CONDITION = SMS_READ_COLUMN + "=0";
     
     public static int getUnreadSmsCount(Context context) {
         int count = 0;
         Cursor cursor = context.getContentResolver().query(
             SMS_INBOX_CONTENT_URI,
             new String[] { SMS_ID },
             UNREAD_CONDITION, null, null);
         if (cursor != null) {
             try {
                 count = cursor.getCount();
             } finally {
                 cursor.close();
             }
         }
         return count;
     }
 
     public static String getName(Context context, String callNumber) {
         String caller = null;
         Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                 callNumber); 
         Cursor cursor = context.getContentResolver().query(
                 uri, new String[] { PhoneLookup.DISPLAY_NAME },
                 null, null, null);
         String[] contacts = new String[] { PhoneLookup.DISPLAY_NAME };
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(contacts[0]);
                caller = cursor.getString(nameIndex);
            }
         }
         if (caller == null) {
             caller = callNumber;
         }
         if (cursor != null) {
             cursor.close();
         }
         return caller;
     }
 
     public static long getSmsId(Context context) {
         long messageId = 0;
         Cursor cursor = context.getContentResolver().query(
                 SMS_INBOX_CONTENT_URI,
                 new String[] { "_id" },
                 null, null, null);
         if (cursor.moveToFirst()) {
             messageId = cursor.getLong(cursor.getColumnIndex("_id"));
         }
         if (cursor != null) {
             cursor.close();
         }
         return messageId;
     }
 
     public static String getDate(Context context, long dateTaken) {
         String formattedDate = new java.text.SimpleDateFormat("EEE MMM dd HH:mm aa")
                 .format(new java.util.Date(dateTaken));
         return formattedDate;
     }
 
     public static Bitmap getContactPicture(Context context, String callNumber) {
         Bitmap bitmap = null;
         Bitmap scaledBitmap = null;
         Cursor cursor = context.getContentResolver().query(
                 Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                 Uri.decode(callNumber)),
                 new String[] { PhoneLookup._ID },
                 null, null, null);
         if (cursor.moveToFirst()) {
             long contactId = cursor.getLong(0);
             InputStream inputStream = Contacts.openContactPhotoInputStream(
                     context.getContentResolver(),
                     ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId));
             if (inputStream != null) {
                 bitmap = BitmapFactory.decodeStream(inputStream);
                 scaledBitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
             }
         }
         if (cursor != null) {
             cursor.close();
         }
         return scaledBitmap;
     }
 
     public static CharSequence replaceWithEmotes(String body, Context context) {
         SmileyParser.init(context);
         EmojiParser.init(context);
 
         SpannableStringBuilder buf = new SpannableStringBuilder();
 
         if (!TextUtils.isEmpty(body)) {
             SmileyParser parser = SmileyParser.getInstance();
             CharSequence smileyBody = parser.addSmileySpans(body);
             if (true) {
                 EmojiParser emojiParser = EmojiParser.getInstance();
                 smileyBody = emojiParser.addEmojiSpans(smileyBody);
             }
             buf.append(smileyBody);
         }
         return buf;
     }
 }
