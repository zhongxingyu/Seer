 package com.android.shaastra;
 
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.android.helpers.DatabaseHelper;
 import com.android.helpers.FullCoordinatorListAdapter;
 
 public class FullCordListActivity extends ListActivity
 {
 	String query = "";
 
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		query = "";
 
 		handleIntent(getIntent());
 
 	}
 
 	protected void onNewIntent(Intent intent)
 	{
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	private void handleIntent(Intent intent)
 	{
 		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
 		{
 			Log.e("search", "Its working");
 			query = intent.getStringExtra(SearchManager.QUERY);
 
 		}
 
 		DatabaseHelper myDbHelper = new DatabaseHelper(this);
 		
 		myDbHelper.openDataBase();
 		Cursor cursor;
 		if (query == "")
 			cursor = myDbHelper.fetchAllCords();
 		else
 			cursor = myDbHelper.fetchCords(query);
 		startManagingCursor(cursor);
 		FullCoordinatorListAdapter adapter = new FullCoordinatorListAdapter(
 				this, cursor);
 		this.setListAdapter(adapter);
 		myDbHelper.close();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		super.onListItemClick(l, v, position, id);
 		TextView phoneView = (TextView) v.findViewById(R.id.cordphone);
 		String phone = (String) phoneView.getText();
 		final String uri = "tel:" + phone;
 		final String messageUri = "sms:" + phone;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Do you want to call or message the coordinator?")
 				.setCancelable(true)
 				.setPositiveButton("Call",
 						new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int id)
 							{
 								Intent intent = new Intent(Intent.ACTION_CALL,
 										Uri.parse(uri));
 								startActivity(intent);
 							}
 						})
 				.setNegativeButton("Message",
 						new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int id)
 							{
 								Intent intent = new Intent(Intent.ACTION_VIEW,
 										Uri.parse(messageUri));
 								//  intent.setType("vnd.android-dir/mms-sms");
 								startActivity(intent);
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.search_menu, menu);
 
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 		case R.id.search_menu:
 			onSearchRequested();
 			return true;
 		default:
 			return false;
 
 		}
 	}
 }
