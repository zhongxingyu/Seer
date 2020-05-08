 /*
  * Copyright (C) 2010 Kouji Ohura
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.hekatoncheir.quicksendto;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 
 import android.os.Bundle;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.content.Intent;
 import android.net.Uri;
 
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.LayoutInflater;
 import android.widget.BaseAdapter;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.Cursor;
 import android.provider.BaseColumns;
 
 import java.util.Vector;
 
 public class QuickSendTo extends Activity
 {
 //    private final String TAG = "QuickSendTo.SelectEmailAddress";
 
 	private static final int MENU_ID_ADD_EMAIL_ADDRESS = 0;
 	private static final int ACTIVITY_EDIT_TEMPLATE = 0;
 	
 	private DatabaseHelper _dbhelper;
 	Vector<Template> _templateList = new Vector<Template>();
 	ContactAdapter _adapter;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.selecttemplate);
 		
 		_dbhelper = new DatabaseHelper(this);
 		_adapter = new ContactAdapter(this);
 		
 
 		ListView contact_list = (ListView)findViewById(R.id.contact_list);
 		contact_list.setAdapter(_adapter);
 		contact_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 //				ListView listView = (ListView) parent;
 				Template email = (Template) _templateList.get(position);
 // 				Log.d(TAG, "onItemClick "+email);
 				
				String escapeMailAddress = Uri.encode(email._mailaddress);
				
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+escapeMailAddress)); 
 				intent.putExtra(Intent.EXTRA_SUBJECT, email._subject); 
 				intent.putExtra(Intent.EXTRA_TEXT, email._message); 
 				startActivity(intent);
 			}
 		});
 		
 		contact_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
 //				ListView listView = (ListView) parent;
 				
  				final Template email = (Template) _templateList.get(position);
             	(new AlertDialog.Builder(QuickSendTo.this))
                 .setTitle(email.toString())
                 .setItems(R.array.dialog_edit_template_list, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
 						switch(which){
 						case 0:	// Edit
 							Intent intent = new Intent(QuickSendTo.this, EditTemplate.class);
 							intent.putExtra(EditTemplate.INTENT_PARAM_ID, email._id);
 							startActivityForResult(intent , ACTIVITY_EDIT_TEMPLATE);
 							break;
 						case 1:	// Delete
 							{
 				 				Template template = (Template) _templateList.get(position);
 // 				 				Log.d(TAG, "onItemClick "+email);
 				 				showDeleteDialog(template);
 							}break;
 						case 2:	// Duplicate
 			 				{
 								Template template = (Template) _templateList.get(position);
 			 					duplicateEntry(template);
 							}break;
 						}
                     }
                 })
                 .show();
 
 				return true;
 			}
 		});
 		 
 		
 		reloadList();
 		
 		// Show Toast, if template is empty. 
 		if( _templateList.size() == 0 ){
 			Toast toast =
 				Toast.makeText(getApplicationContext(), R.string.empty_list_toast_message, Toast.LENGTH_LONG);
 			toast.show();
 		}
 	}
 	
     @Override
 	public void onResume()
 	{
         super.onResume();
 	}
 	
 	private void reloadList()
 	{
 		_templateList.clear();
 		
 		SQLiteDatabase db = _dbhelper.getWritableDatabase();
 		String query = "select "
 			+ BaseColumns._ID+","
 			+ DatabaseHelper.TITLE + ","
 			+ DatabaseHelper.SUBJECT + ","
 			+ DatabaseHelper.MAILADDRESS + ","
 			+ DatabaseHelper.MESSAGE
 			+ " from " + DatabaseHelper.TABLE_NAME;
 		
 		Cursor c = db.rawQuery(query, null);
 		boolean b = c.moveToFirst();
 		while (b){
 			long cid = c.getLong (0);
 			String title = c.getString(1);
 			String subject = c.getString(2);
 			String mailaddress = c.getString(3);
 			String message = c.getString(4);
 			
 			_templateList.add( new Template(cid, title, subject, mailaddress, message) );
 			
 			b = c.moveToNext();
 		}
 		db.close();
 		
 		_adapter.notifyDataSetChanged();
 	}
 	
 	private void deleteFromList(Template emailAddress)
 	{
 		if( emailAddress._id == -1 )return;
 		
 		String args[] = {""+emailAddress._id};
 		SQLiteDatabase db = _dbhelper.getWritableDatabase();
 		db.delete(DatabaseHelper.TABLE_NAME, BaseColumns._ID+"=?", args);
 		
 		_templateList.remove(emailAddress);
 		_adapter.notifyDataSetChanged();
 	}
 
     @Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		MenuItem item = menu.add(Menu.NONE, MENU_ID_ADD_EMAIL_ADDRESS, Menu.NONE, R.string.menu_add_email_address);
 		item.setIcon(android.R.drawable.ic_menu_add);
 		return true;
 	}
 
     @Override
 	public boolean onMenuItemSelected(int id, MenuItem item ){
 		switch(item.getItemId() ){
 		case MENU_ID_ADD_EMAIL_ADDRESS:
 			{
 				startActivityForResult( new Intent(this, EditTemplate.class), ACTIVITY_EDIT_TEMPLATE);
 			}break;
 		}
 		return true;
 	}
 
 	public void onActivityResult(int requestCode, int resultCode,  Intent data) {
 		if (requestCode == ACTIVITY_EDIT_TEMPLATE) {
 			if (resultCode == RESULT_OK) {
 // 				Log.d(TAG, "onActivityResult");
 				reloadList();
 				_adapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
     public class ContactAdapter extends BaseAdapter {
         public ContactAdapter(Context c) {
             _context = c;
         }
 
         public int getCount() {
             return _templateList.size();
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
 			Template email = _templateList.get(position);
 			
 			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 			View v = inflater.inflate(R.layout.selecttemplate_entry, null);
 			TextView t= (TextView) v.findViewById(R.id.contact_entry_text);
 			if(t != null)t.setText(email.toString());
 
 			return v;
         }
 
         private Context _context;
     }
 	
 	public void showDeleteDialog(final Template template)
 	{
 		Resources r = getResources();
 		
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		alertDialogBuilder.setTitle( r.getString(R.string.del_dialog_title));
 		alertDialogBuilder.setMessage(template.toString());
 		alertDialogBuilder.setPositiveButton(r.getString(R.string.del_dialog_ok_btn), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				deleteFromList(template);
 			}});
 		alertDialogBuilder.setNegativeButton(r.getString(R.string.del_dialog_cancel_btn), null);
 		alertDialogBuilder.setCancelable(true);
 		
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.setIcon(android.R.drawable.ic_menu_delete);
 		alertDialog.show();
 	}
 	
 	private void duplicateEntry(Template template)
 	{
 		SQLiteDatabase db = _dbhelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(DatabaseHelper.TITLE, template._title);
 		values.put(DatabaseHelper.MAILADDRESS, template._mailaddress);
 		values.put(DatabaseHelper.SUBJECT, template._subject);
 		values.put(DatabaseHelper.MESSAGE, template._message);
 		
 		db.insert(DatabaseHelper.TABLE_NAME,null,values);
 		db.close();
 		
 		reloadList();
 	}
 }
