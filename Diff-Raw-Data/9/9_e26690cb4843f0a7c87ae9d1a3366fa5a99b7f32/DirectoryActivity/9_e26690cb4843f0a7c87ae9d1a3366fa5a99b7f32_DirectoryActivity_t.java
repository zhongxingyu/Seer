 package com.dev.campus.directory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dev.campus.R;
 import com.dev.campus.SettingsActivity;
 import com.dev.campus.util.FilterDialog;
 import com.unboundid.ldap.sdk.LDAPException;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class DirectoryActivity extends ListActivity {
 
 	private ActionBar mActionBar;
 	private Resources mResources;
 	private FilterDialog mFilterDialog;
 	
 	private Contact mContact;
 	private DirectoryAdapter mDirectoryAdapter;
 	private DirectoryManager mDirectoryManager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mFilterDialog = new FilterDialog(this);
 		mActionBar = getActionBar();
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		mResources = getResources();
 
 		final ListView listview = getListView();
 		View header = (View) getLayoutInflater().inflate(R.layout.directory_list_header, listview, false);
 		listview.addHeaderView(header, null, true);
 		
 		mDirectoryManager = new DirectoryManager();
 		mDirectoryAdapter = new DirectoryAdapter(this, new ArrayList<Contact>());
 		listview.setAdapter(mDirectoryAdapter);
 		registerForContextMenu(listview);
 		listview.setOnItemClickListener(new OnItemClickListener() {
 
 		       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		    	   mContact = (Contact) parent.getItemAtPosition(position);
 		    	   listview.showContextMenuForChild(view);
 		       }
 		});
 		
 		
 		final ImageButton searchButton = (ImageButton) findViewById(R.id.buttonSearchDirectory);
 		searchButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				new SearchResultTask().execute();
 			}
 		});
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
 	    super.onCreateContextMenu(menu, v, menuInfo);
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.directory_contextual, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case R.id.menu_call:
 	        	callContact();
 	            return true;
 	        case R.id.menu_mail:
 	        	emailContact();
 	            return true;
 	        case R.id.menu_add_to_contacts:	
 	        	addToContacts();
 	            return true;
 	        case R.id.menu_website:	  
 	        	visitContactWebsite();
 	            return true;
 	        default:
 	            return super.onContextItemSelected(item);
 	    }
 	}
 	
 	public void callContact() {
 		if (mContact.hasTel()) {
 			Intent callIntent = new Intent(Intent.ACTION_CALL);
 			callIntent.setData(Uri.parse("tel:" + mContact.getTel()));
 			startActivity(callIntent);
 		}
 		else
 			Toast.makeText(this, mResources.getString(R.string.no_tel), Toast.LENGTH_SHORT).show();
 	}
 	
 	public void emailContact() {
 		if (mContact.hasEmail()) {
 			Intent emailIntent = new Intent(Intent.ACTION_SEND);
 			emailIntent.setType("plain/text");  
 			emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[] {mContact.getEmail()});
 			startActivity(Intent.createChooser(emailIntent, mResources.getString(R.string.menu_complete_action)));
 		}
 		else
 			Toast.makeText(this, mResources.getString(R.string.no_email), Toast.LENGTH_SHORT).show();
 	}
 	
 	public void addToContacts() {
 		String contactFullName =  mContact.getFirstName() + " " + mContact.getLastName();
 		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT,ContactsContract.Contacts.CONTENT_URI);
 		intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
 		intent.putExtra(ContactsContract.Intents.Insert.NAME, contactFullName);
 		
 		if (mContact.hasTel())
 			intent.putExtra(ContactsContract.Intents.Insert.PHONE, mContact.getTel());	
 		if (mContact.hasEmail())
 			intent.putExtra(ContactsContract.Intents.Insert.EMAIL, mContact.getEmail());
 		startActivity(intent);
 	}
 
 	public void visitContactWebsite() {
 		if (mContact.hasWebsite()) {
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContact.getWebsite()));
 			startActivity(browserIntent);
 		}
 		else
 			Toast.makeText(this, mResources.getString(R.string.no_website), Toast.LENGTH_SHORT).show();
 	}
 	
 	public void reloadContacts(List<Contact> matchingContacts){
 		mDirectoryAdapter.clear();
 		mDirectoryAdapter.addAll(matchingContacts);
 		mDirectoryAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.with_actionbar, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(DirectoryActivity.this, SettingsActivity.class));
 			return true;
 		case R.id.menu_filters:
 			mFilterDialog.showDialog();
 			return true;
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	private class SearchResultTask extends AsyncTask<Void, Void, List<Contact>> {
 
 		private ProgressDialog progressDialog = new ProgressDialog(DirectoryActivity.this);
 
 		@Override
 		protected void onPreExecute() {
 			progressDialog.setTitle(mResources.getString(R.string.contacts_loading));
 			progressDialog.setMessage(mResources.getString(R.string.please_wait));
 			progressDialog.setIndeterminate(true);
 			progressDialog.setCancelable(false);
 			progressDialog.show();
 		}
 
 		@Override
 		protected List<Contact> doInBackground(Void... params) {
 			final String firstName = ((TextView) findViewById(R.id.editTextFirstName)).getText().toString();
 			final String lastName = ((TextView) findViewById(R.id.editTextLastName)).getText().toString();
 
			int searchMinChar = 1;
			List<Contact> searchResult = new ArrayList<Contact>();
 			if (firstName.length() >= searchMinChar || lastName.length() >= searchMinChar) {
 				try {
					searchResult = mDirectoryManager.searchContact(firstName, lastName);
 				} catch (LDAPException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			else {
 				//Toast.makeText(mContext, searchMinChar+" charactères minimum!", Toast.LENGTH_SHORT).show();
 			}
			return searchResult;
 		}
 
 		@Override
 		protected void onPostExecute(List<Contact> contacts) {
 			reloadContacts(contacts);
 			progressDialog.dismiss();
 		}
 
 		@Override
 		protected void onCancelled() {
 			progressDialog.dismiss();
 			super.onCancelled();
 		}
 	}
 
 }
