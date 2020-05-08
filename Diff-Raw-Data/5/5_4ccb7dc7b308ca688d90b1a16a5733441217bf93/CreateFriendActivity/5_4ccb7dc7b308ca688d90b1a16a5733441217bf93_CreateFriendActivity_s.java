 package ch.unibe.ese.shopnote.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Friend;
 import ch.unibe.ese.shopnote.core.FriendsManager;
 
 /**
  * 	Creates a frame to create new friends or edit them if the intent has an extra
  */
 public class CreateFriendActivity extends BaseActivity {
 	private static final int PICK_CONTACT = 2110;
 	private FriendsManager friendsManager;
 	private Friend friend;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		friendsManager = getFriendsManager();
 	
 		// check if friend edited, if not, open address book
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			setContentView(R.layout.activity_create_friend);
 			getActionBar().hide();
 
 			editFriend(extras);
 		} else {
 			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);		
 		}
 	}
 
 	/**
 	 * Allows to edit friend by adding the data of the friend to the textviews
 	 * 
 	 * @param extras
 	 */
 	private void editFriend(Bundle extras) {
 
 		// get friend
 		long friendId = extras.getLong(EXTRAS_FRIEND_ID);
 		this.friend = friendsManager.getFriend(friendId);
 
 		if (friend != null) {
 			// set name of friend
 			setTextViewText(R.id.edit_friend_name, friend.getName());
 
 			// set phoneNr but uneditable
 			EditText friendNr = (EditText) findViewById(R.id.edit_friend_phone_number);
 			friendNr.setText("" + friend.getPhoneNr());
 			friendNr.setEnabled(false);
 		} else {
 			String name = extras.getString(EXTRAS_FRIEND_NAME);
 			setTextViewText(R.id.edit_friend_name, name);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.create_friends, menu);
 		return true;
 	}
 
 	/**
 	 * Reads the input and tries to creates a friend with it or edits an old one
 	 * 
 	 * @param View
 	 */
 	public void addEntryToList(View w) {
 		try {
 			String name = getTextViewText(R.id.edit_friend_name);
 
 			friend.setName(name);
 			friendsManager.update(friend);
 			finish();
 
 		} catch (Exception e) {
 			Toast.makeText(this, this.getString(R.string.error_enter),
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void goBack(View view) {
 		finish();
 	}
 
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (resultCode == Activity.RESULT_OK && requestCode == PICK_CONTACT) 
 				addChosenFriend(data);
 	}
 
 	/**
 	 * Extracts the Contact from data and adds it to the database
 	 * 
 	 * @param data
 	 */
 	private void addChosenFriend(Intent data) {
 		Uri contactData = data.getData();
 		Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
 	     
 		 if (cursor.moveToFirst()) {
 			  String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
 			  String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
 	
 			  if (hasPhone.equalsIgnoreCase("1")) {
 				  Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
 						  ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
 				  phones.moveToFirst();
 				  String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
 				  String cName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
 				  // Remove all unneccessary characters
 				  cNumber = cNumber.replaceAll("[^0-9]", "");
 				  addNewFriend(cName, cNumber);
 			  }	
 	     }
 	}
 	
 	private void addNewFriend(String name, String nr) {
 		friend = new Friend(nr, name);
 		long id = friendsManager.addFriend(friend);
 		
 		finishTheActivity(id);
 	}
 	
 
 	/**
 	 * finishes the program with no result when called from ManageFriendsActivity
 	 * or with result when called by ShareListActivity
 	 */
 	private void finishTheActivity(long id) {
 		if (friend != null) {
 			Intent intent = new Intent();
 			intent.putExtra(EXTRAS_FRIEND_ID, id);
 			setResult(RESULT_OK, intent);
 		}
 		finish();
 	}
 	
 }
