 
 package hu.rgai.android.tools;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.provider.ContactsContract;
 import android.util.Log;
 import hu.rgai.android.test.R;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Tamas Kojedzinszky
  */
 public class ProfilePhotoProvider {
 
   private static Map<Long, Bitmap> photos = null;
   
   /**
    * 
    * @param context
    * @param type type of 
    * @param contactId android contact id
    * @return 
    */
   public static Bitmap getImageToUser(Context context, long contactId) {
     if (contactId != -1) {
 //      Log.d("rgai", "VALID contact id -> " + contactId);
     }
     Bitmap img = null;
     if (photos == null) {
       photos = new HashMap<Long, Bitmap>();
     }
     if (photos.containsKey(contactId)) {
       return photos.get(contactId);
     } else {
       img = getImgToUserId(context, contactId);
       if (img != null) {
         photos.put(contactId, img);
       }
     }
     if (img == null) {
       img = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
     }
     return img;
   }
   
   private static Bitmap getImgToUserId(Context context, long uid) {
     Bitmap bm = null;
     
     Cursor cursor = context.getContentResolver().query(
             ContactsContract.Data.CONTENT_URI,
             new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO},
 //            new String[] {ContactsContract.Data._ID, ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.DISPLAY_NAME_PRIMARY},
             ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
 //            ContactsContract.Data.MIMETYPE + " = ?",
             new String[]{uid + "", ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE},
 //            new String[]{ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE},
             null);
     if (cursor != null) {
       while (cursor.moveToNext()) {
             
         int photoIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO);
         byte[] data = cursor.getBlob(photoIdx);
        bm = BitmapFactory.decodeByteArray(data, 0, data.length);
 //        int nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME_PRIMARY);
 //        String name = cursor.getString(nameIdx);
 //
 //        int _idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
 //        String _id = cursor.getString(_idIdx);
 //
 //        int rawIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID);
 //        String rawId = cursor.getString(rawIdx);
 //        Log.d("rgai", "FOUND name -> " + name);
 //        Log.d("rgai", "FOUND _id  -> " + _id);
 //        Log.d("rgai", "FOUND rawId-> " + rawId);
       }
       cursor.close();
     }
     
     return bm;
   }
   
 //  private static Long getUserId(Context context, MessageProvider.Type type, String id) {
 //    ContentResolver cr = context.getContentResolver();
 //    String[] projection = new String[] {
 ////        ContactsContract.Data._ID,
 //        ContactsContract.Data.RAW_CONTACT_ID,
 ////        ContactsContract.Data.DISPLAY_NAME_PRIMARY
 //    };
 //    
 //    String selection = "";
 //    String[] selectionArgs = null;
 //    if (type.equals(MessageProvider.Type.FACEBOOK)) {
 //      selection = ContactsContract.Data.MIMETYPE + " = ?"
 //              + " AND " + ContactsContract.Data.DATA2 + " = ?"
 //              + " AND " + ContactsContract.Data.DATA5 + " = ?"
 //              + " AND " + ContactsContract.Data.DATA6 + " = ?"
 //              + " AND " + ContactsContract.Data.DATA10 + " = ?";
 //      selectionArgs = new String[]{
 //        ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
 //        ContactsContract.CommonDataKinds.Im.TYPE_OTHER + "",
 //        ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM + "",
 //        Settings.Contacts.DataKinds.Facebook.CUSTOM_NAME,
 //        id
 //      };
 //    } else if (type.equals(MessageProvider.Type.EMAIL) || type.equals(MessageProvider.Type.GMAIL)) {
 //      selection = ContactsContract.Data.MIMETYPE + " = ?"
 //              + " AND " + ContactsContract.Data.DATA1 + " = ? ";
 //      selectionArgs = new String[]{
 //        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
 //        id
 //      };
 //    } else if (type.equals(MessageProvider.Type.SMS)) {
 //      throw new RuntimeException("Need to implement query for SMS messages!");
 //    }
 //    
 //    Cursor cu = cr.query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
 //    
 //    cu.moveToFirst();
 //    String _id = "-1";
 ////    String rawId = "-1";
 //    ArrayList<Long> ids = new ArrayList<Long>();
 //    while (!cu.isAfterLast()) {
 //      int idIdx = cu.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID);
 //      _id = cu.getString(idIdx);
 //      ids.add(Long.parseLong(_id));
 //      
 ////      int rawIdIdx = cu.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID);
 ////      rawId = cu.getString(rawIdIdx);
 ////      
 ////      int nameIdx = cu.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME_PRIMARY);
 ////      String n = cu.getString(nameIdx);
 ////      Log.d("rgai", "_id, rawId, name -> " + _id + " - " + rawId + " - " + n);
 //      
 //      cu.moveToNext();
 //    }
 //    if (cu != null) {
 //      cu.close();
 //    }
 //    
 //    if (ids.size() > 0) {
 //      return ids.get(0);
 //    } else {
 //      return -1L;
 //    }
 //    
 //  }
   
 }
