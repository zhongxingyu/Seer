 package com.nbos.phonebook;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.CursorJoiner;
 import android.database.MatrixCursor;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Contacts;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.FilterQueryProvider;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.nbos.phonebook.database.IntCursorJoiner;
 import com.nbos.phonebook.database.tables.BookTable;
 import com.nbos.phonebook.value.ContactRow;
 
 public class GroupActivity extends ListActivity {
 
 	String id, name;
 	static String tag = "GroupActivity";
 	MatrixCursor m_cursor;
     List<byte[]> photos;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.group);
 	    // this.registerForContextMenu(getListView());
 	    Bundle extras = getIntent().getExtras();
 	    if(extras !=null)
 	    {
 	    	id = extras.getString("id");
 	    	name = extras.getString("name");
 	    }
 		queryGroup();
 		registerForContextMenu(getListView());  
 		getListView().setTextFilterEnabled(true);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 	    // Get the info on which item was selected
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 	    m_cursor.moveToPosition(info.position);
 	    String contactName = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 		menu.setHeaderTitle("Menu: "+contactName);
 		menu.add(0, v.getId(), 0, "Call");
 		menu.add(0, v.getId(), 1, "Remove from group");
 		//  menu.add(0, v.getId(), 0, "Action 2");
 	}
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    // Get the info on which item was selected
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 
 	    m_cursor.moveToPosition(info.position);
 	    String contactId = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Contacts._ID)),
 	    	name = m_cursor.getString(m_cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 
 	    Log.i(tag, "position is: "+info.position+", contactId: "+contactId+", name: "+name);
 	    // Retrieve the item that was clicked on
 	    // Object o = adapter.getItem(info.position);
 		
 		if (item.getTitle() == "Remove from group") {
 			Log.i(tag, "Remove: " + item.getItemId());
 			removeFromGroup(contactId);
 		} else if (item.getTitle() == "Call") {
 			//call the guy
 			callFromGroup(contactId);
 		} 
 		else {
 			return false;
 		}
 		return true;
 	}   	
 	
 	
     private void callFromGroup(String contactId) {
     	
         Uri myPhoneUri = Uri.withAppendedPath(
                 ContactsContract.CommonDataKinds.Phone.CONTENT_URI, contactId);
         Log.i(tag, "Phone uri is: "+myPhoneUri);
 
         Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
     		null, 		
     		ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
     		null, null);	
         
         Log.i(tag, "Calling phonenumber contactID is: "+contactId);
        
         Log.i(tag, "There are "+phones.getCount()+" phone numbers");
         
         String phoneNumber=null; 
         phones.moveToFirst();
         
         Log.i(tag, "Calling phonenumber contactID is  :::::::::::"+contactId); 
         
         phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
         Log.i(tag, "Phone number is: "+phoneNumber);
         
 	 	Intent callIntent = new Intent(Intent.ACTION_CALL);
         callIntent.setData(Uri.parse("tel:"+phoneNumber));
         startActivity(callIntent);
 	}
 
 	private void removeFromGroup(String contactId) {
     	String [] args = { id, contactId  };
         int b = getContentResolver().delete(ContactsContract.Data.CONTENT_URI, 
         		ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID+ " = ? "
         		+ " and "+ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID +" = ? ", 
         		args);
         
         Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
         DatabaseHelper.setGroupDirty(id, getContentResolver());
         //notify registered observers that a row was updated
         getContentResolver().notifyChange(ContactsContract.Data.CONTENT_URI, null);
         queryGroup();
     }
 	
 	private int numContacts() {	
         Cursor contactsCursor = DatabaseHelper.getContacts(this);//getContacts();
         Log.i(tag, "There are "+contactsCursor.getCount()+" contacts");
         int numContacts = 0;
         Cursor dataCursor = DatabaseHelper.getBook(this, id);
         Log.i(tag, "There are "+dataCursor.getCount()+" contacts sharing this group");
 	    IntCursorJoiner joiner = new IntCursorJoiner(
 	    		contactsCursor, new String[] {ContactsContract.Contacts._ID},
 	    		dataCursor,	new String[] {BookTable.CONTACTID}
 	    );
 
         for (CursorJoiner.Result joinerResult : joiner) 
         {
         	switch (joinerResult) {
         		case BOTH: // handle case where a row with the same key is in both cursors
         			numContacts++;        			
         		break;
         	}
         }	 
         Log.i(tag, "Sharing with "+numContacts+" contacts");
         return numContacts;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.group_menu, menu);
 	    return true;
 		
 	}
 	
 	
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Log.i(tag, "Clicked ..");
 	}
 
 
 
 	
 	Cursor dataCursor;
 	ImageCursorAdapter adapter;
 	
     private void queryGroup() {
     		setTitle("Group: "+name+" ("+numContacts()+" contacts sharing with)");
     	dataCursor = DatabaseHelper.getContactsInGroup(id, this.getContentResolver());
   	   	getContactsFromGroupCursor("");
         String[] fields = new String[] {
                 ContactsContract.Contacts.DISPLAY_NAME
         };
         adapter = new ImageCursorAdapter(this, R.layout.contact_entry, m_cursor, photos,
                 fields, new int[] {R.id.contact_name});
         
         adapter.setStringConversionColumn(
                 m_cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
       
         adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
             public Cursor runQuery(CharSequence constraint) {
                 String partialItemName = null;
                 if (constraint != null) {
                     partialItemName = constraint.toString();
                 }
                 getContactsFromGroupCursor(partialItemName);
                 adapter.setCursor(m_cursor);
                 adapter.setImages(photos);
                 return m_cursor;
             }
         });
         
         getListView().setAdapter(adapter);
 	    
 	    Log.i(tag, "There are "+m_cursor.getCount()+" contacts in this group");
 	    
 
 	}
     
     private void getContactsFromGroupCursor(String search) {
     	photos = new ArrayList<byte[]>();
     	Log.i(tag, "There are "+dataCursor.getCount()+" contacts in data for groupId: "+id);
  	   	Cursor contactsCursor = DatabaseHelper.getContacts(this, search);
  	    Log.i(tag, "There are "+contactsCursor.getCount()+" contacts matching "+search);
  	    
  	    IntCursorJoiner joiner = new IntCursorJoiner(
  	    		contactsCursor, new String[] {ContactsContract.Contacts._ID} ,
  	    		dataCursor, new String[] {ContactsContract.Data.RAW_CONTACT_ID}
  	    );
          m_cursor = new MatrixCursor( 
          	new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME}, 10);
          
          List<ContactRow> rows =new ArrayList<ContactRow>();
          for (CursorJoiner.Result joinerResult : joiner) 
          {
          	switch (joinerResult) {
          		case BOTH: // handle case where a row with the same key is in both cursors
          			String contactId = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
          			String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
         			String serverId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.RawContacts.SOURCE_ID));
         			Log.i(tag, "Contact id: "+contactId+", name: "+name+", serverId: "+serverId);
          			
          			byte [] photo = null;
          			
          		     Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(contactId));
          		     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
          		     Cursor cursor = getContentResolver().query(photoUri,
          		          new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);
          		     try {
          		         if (cursor.moveToFirst()) {
          		             photo = cursor.getBlob(0);
          		             if (photo != null) 
          		            	Log.i(tag, "name is: "+name+", photo data is "+photo.length + " bytes");
          		         }
          		     } finally {
          		         cursor.close();
          		     }
          			if(name != null)
          				rows.add(new ContactRow(contactId, name, photo));
          		break;
          	}
          }	    
          Collections.sort(rows);
          Log.i(tag, "There are "+rows.size()+" contacts matching "+search);
          for(ContactRow row : rows)
          {
          	 m_cursor.addRow(new String[] {row.id, row.name});
          	 Log.i(tag, "Adding row["+row.id+"] = "+row.name);
          	 photos.add(row.image);
          }
  		
 	}
 	ListView mGroupList;
 	boolean mShowInvisible = false;
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    	case R.id.delete_group:
 	    		showDeleteGroupDialog();
 	    		break;
 	    	case R.id.add_contacts:
 	    		showAddContacts();
 	    		break;
 	    	case R.id.share_group:
 	    		showShareGroup();
 	    		break;
 	    		
 	    }
 	     return true;
 	    /*case R.id.help:
 	        showHelp();
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);*/
     }
 
 	private void showShareGroup() {
 		Intent i = new Intent(GroupActivity.this, SharingWithActivity.class);
 		i.putExtra("id", id);
 		i.putExtra("name", name);
         startActivityForResult(i, SHARE_GROUP);	
 	}
 
 	static int ADD_CONTACTS = 1, SHARE_GROUP = 2;
 	private void showAddContacts() {
 		Intent i = new Intent(GroupActivity.this, AddContactsActivity.class);
 		i.putExtra("id", id);
 		i.putExtra("name", name);
         startActivityForResult(i, ADD_CONTACTS);	
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		queryGroup();
 	}
 	
 	private void showDeleteGroupDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Are you sure you want to delete this group?")
 		       .setCancelable(false)
 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                deleteGroup();
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();	
 		alert.show();
 	}
 
 	private void deleteGroup() {
 		System.out.println("delete group: "+id+", "+name);
 		String [] args = { id };
 	       try {
 	            int b = getContentResolver().delete(ContactsContract.Groups.CONTENT_URI, "_ID=?", args);
 
 	            Toast.makeText(this, "Deleted",Toast.LENGTH_SHORT).show();
 	            //notify registered observers that a row was updated
 	            getContentResolver().notifyChange(ContactsContract.Groups.CONTENT_URI, null);
 
 	        } catch (Exception e) {
 	            Log.v(tag, e.getMessage(), e);
 	            Toast.makeText(this, tag + " Delete Failed", Toast.LENGTH_LONG).show();
 	        }
 	        setResult(RESULT_OK, null);
 	        finish();
 	}
 }
 
 class ImageCursorAdapter extends SimpleCursorAdapter {
 
 	private Cursor c;
 	private Context context;
 	List<byte[]> images;
 	
 	public ImageCursorAdapter(Context context, int layout, Cursor c, List<byte[]> images,
 			String[] from, int[] to) {
 		super(context, layout, c, from, to);
 		this.c = c;
 		this.context = context;
 		this.images = images;
 	}
 
 	public void setCursor(Cursor c) { this.c = c; }
 	public void setImages(List<byte[]> images) { this.images = images; }
 	@Override
 	public View getView(int pos, View inView, ViewGroup parent) {
 		String tag = "GroupActivity";
 		Log.v(tag, "position of image " +pos);
 		View v = inView;
 		if (v == null) {
 			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		    v = inflater.inflate(R.layout.contact_entry, null);
 		}
 		this.c.moveToPosition(pos);	
 		
 		// ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Photo.PHOTO}, 10);
 		String contactName = this.c.getString(this.c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 		byte[] pic = images.get(pos);//this.c.getBlob(this.c.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
 		Log.v(tag, "image #" +pos+", "+pic+", num images: "+images.size());
 		if (pic != null) {
 			ImageView iv = (ImageView) v.findViewById(R.id.contact_pic);
 			iv.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.length));
 			iv.setScaleType(ScaleType.FIT_XY);
 		}
 		TextView cName = (TextView) v.findViewById(R.id.contact_name);
 		cName.setText(contactName);
 		return v;
 	}
 
 }
 
