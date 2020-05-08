 package com.mann.drunktexter;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.*;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Contacts;
 import android.provider.ContactsContract;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mseger
  * Date: 9/16/11
  * Time: 10:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SMS extends Activity{
 
     Button btnSendSMS;
     Button newRandomContact;
     ArrayList<String> contactInfo;
     String contact_name;
     String contact_number;
     TextView display_name;
     TextView display_num;
     ImageView contact_photo;
     ArrayList<String> randoTexts;
     String message;
     ContentResolver cResolver;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.sms);
 
         cResolver = getContentResolver();
 
         // get the random contact to send message to
         contactInfo = getRandomContact();
         contact_name = contactInfo.get(0);
         contact_number = contactInfo.get(1);
         Log.v("DURNK", "THE CONTACT NUMBER IS: " + contact_number);
 
         // display chosen contact info
         display_name = (TextView) findViewById(R.id.contactName);
         display_num = (TextView) findViewById(R.id.contactNum);
         display_name.setVisibility(1);
         display_name.setText(contact_name);
         display_num.setVisibility(1);
         display_num.setText(contact_number);
 
         // fill the random text message list
         randoTexts = new ArrayList<String>();
         randoTexts.add("I want to plow her like an amish guy supporting his family");
         randoTexts.add("Dude i'm not sure who's apartment i woke up in but i just showered here and their shampoo in phenomenal...");
         randoTexts.add("if beer pong were an olympic sport, I'd be the Michael Phelps of this city!");
         randoTexts.add("I just woke up naked and covered in skittles. Best night ever?");
         randoTexts.add("DUDE! IM GONNA BE ON COPS!");
         randoTexts.add("HE'S turngign 18teen real soon.k");
         randoTexts.add("I AM HAVING A WEIRD OUT OF BODY EXPERIENCE. IN CAPS LOCK.");
         randoTexts.add("im drinking tequila tonight so will you babysit my bra?");
         randoTexts.add("ROOF CAVED IN, WE'RE GUNNA MAKE A WATERSLIDE");
         randoTexts.add("The floor and the wall just switched. I'm falling.");
         randoTexts.add("Blackout strip poker. Now. Bring flashlights because we found that candles are dangerous with nudity.");
         randoTexts.add("noo wooreez bout me. i am drancking sakee and i am goin to danse on a pole. sexsexsex. loving you.");
 
 
         btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
         newRandomContact = (Button) findViewById(R.id.newRandomContact);
 
         btnSendSMS.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 message = getRandomMessage();
                 if (contact_number.length()>0 && message.length()>0){
                     sendSMS(contact_number, message);
 
                     Intent mistake = new Intent(SMS.this, YourMistake.class);
                     mistake.putExtra("message", message);
                     mistake.putExtra("name", contact_name);
                     mistake.putExtra("number", contact_number);
 
                     startActivity(mistake);
 
                 }
                 else
                     Toast.makeText(getBaseContext(),
                             "Please enter both phone number and message.",
                             Toast.LENGTH_SHORT).show();
             }
         });
 
          newRandomContact.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 // get the random contact to send message to
                 contactInfo = getRandomContact();
                 contact_name = contactInfo.get(0);
                 contact_number = contactInfo.get(1);
                 Log.v("DURNK", "THE CONTACT NUMBER IS: " + contact_number);
 
                 // display chosen contact info
                 display_name.setText(contact_name);
                 display_num.setVisibility(1);
                 display_num.setText(contact_number);
             }
 
         });
     }
 
     private void sendSMS(String phoneNumber, String message)
     {
         ArrayList<PendingIntent> sent_PI_List = new ArrayList<PendingIntent>();
         ArrayList<PendingIntent> delivered_PI_list = new ArrayList<PendingIntent>();
         ArrayList<String> messages = new ArrayList<String>();
 
         String SENT = "SMS_SENT";
         String DELIVERED = "SMS_DELIVERED";
 
         PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
             new Intent(SENT), 0);
         sent_PI_List.add(sentPI);
 
         PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
             new Intent(DELIVERED), 0);
         delivered_PI_list.add(deliveredPI);
 
         //---when the SMS has been sent---
         registerReceiver(new BroadcastReceiver(){
             @Override
             public void onReceive(Context arg0, Intent arg1) {
                 switch (getResultCode())
                 {
                     case Activity.RESULT_OK:
                         Toast.makeText(getBaseContext(), "SMS sent",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                         Toast.makeText(getBaseContext(), "Generic failure",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_NO_SERVICE:
                         Toast.makeText(getBaseContext(), "No service",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_NULL_PDU:
                         Toast.makeText(getBaseContext(), "Null PDU",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case SmsManager.RESULT_ERROR_RADIO_OFF:
                         Toast.makeText(getBaseContext(), "Radio off",
                                 Toast.LENGTH_SHORT).show();
                         break;
                 }
             }
         }, new IntentFilter(SENT));
 
         //---when the SMS has been delivered---
         registerReceiver(new BroadcastReceiver(){
             @Override
             public void onReceive(Context arg0, Intent arg1) {
                 switch (getResultCode())
                 {
                     case Activity.RESULT_OK:
                         Toast.makeText(getBaseContext(), "SMS delivered",
                                 Toast.LENGTH_SHORT).show();
                         break;
                     case Activity.RESULT_CANCELED:
                         Toast.makeText(getBaseContext(), "SMS not delivered",
                                 Toast.LENGTH_SHORT).show();
                         break;
                 }
             }
         }, new IntentFilter(DELIVERED));
 
         SmsManager sms = SmsManager.getDefault();
         messages.add(message);
         sms.sendMultipartTextMessage(contact_number, null, messages, sent_PI_List, delivered_PI_list);
     }
 
     public ArrayList<String> getRandomContact(){
 
         ArrayList<String> name_number = new ArrayList<String>();
         Cursor managedCursor;
         int size;
 
         String phoneNumber = new String();
         String name = new String();
 
         managedCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
         size = managedCursor.getCount();
 
         // from the contacts grab a random one
         boolean found = false;
         Random rnd = new Random();
         while(!found) {
           int index = rnd.nextInt(size);
           managedCursor.moveToPosition(index);
          name = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
           String found_intermediate = managedCursor.getString(managedCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
 
           if(found_intermediate.equals("1")){
                 found = true;
           }
             else{
                 found = false;
           }
           if (found) {
             Cursor phones = getContentResolver().query(
             ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ (index + 1), null, null);
 
             // load the contact's photo
            Bitmap contactBitmap = loadContactPhoto(cResolver, index + 1);
             if (contactBitmap != null){
                 contact_photo = (ImageView) findViewById(R.id.contact_image);
                 contact_photo.setImageBitmap(contactBitmap);
             }else{
                 contact_photo = (ImageView) findViewById(R.id.contact_image);
                 contact_photo.setImageResource(R.drawable.android_drinking);
             }
 
               while (phones.moveToNext()) {
                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).toString();
                    Log.d("Phone found:", phoneNumber);
               }
             phones.close();
           }
         }
 
         name_number.add(name);
         name_number.add(phoneNumber);
         Log.v("DURNK", "THE NAME_NUMBER IS: " + name_number);
         return name_number;
     }
 
 
     public String getRandomMessage() {
         Random rnd = new Random();
         int index = rnd.nextInt(randoTexts.size());
         return randoTexts.get(index);
     }
 
     public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
         Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
         InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
         if (input == null) {
             return null;
         }
         return BitmapFactory.decodeStream(input);
 }
 
 
 }
 
