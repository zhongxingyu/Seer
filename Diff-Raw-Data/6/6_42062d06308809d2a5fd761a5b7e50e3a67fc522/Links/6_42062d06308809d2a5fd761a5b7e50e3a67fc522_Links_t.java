 ﻿package de.tum.in.tumcampus;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.Toast;
 import de.tum.in.tumcampus.models.Link;
 import de.tum.in.tumcampus.models.LinkManager;
 
 /**
  * Activity to show Links
  */
 public class Links extends Activity implements OnItemClickListener,
 		OnItemLongClickListener, View.OnClickListener, ViewBinder {
 
 	SimpleCursorAdapter adapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.links);
 
 		// get all links from database
 		LinkManager lm = new LinkManager(this, Const.db);
 		Cursor c = lm.getAllFromDb();
 
 		adapter = new SimpleCursorAdapter(this, R.layout.links_listview, c,
 				c.getColumnNames(), new int[] { R.id.icon, R.id.name });
 		adapter.setViewBinder(this);
 
 		// add footer view to add new links
 		View view = getLayoutInflater().inflate(R.layout.links_footer, null,
 				false);
 
 		ListView lv = (ListView) findViewById(R.id.listView);
 		lv.addFooterView(view);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 		lv.setOnItemLongClickListener(this);
 		lm.close();
 
 		Button save = (Button) view.findViewById(R.id.save);
 		save.setOnClickListener(this);
 
 		// reset new items counter
 		LinkManager.lastInserted = 0;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> aview, View view, int position,
 			long id) {
 		ListView lv = (ListView) findViewById(R.id.listView);
 		Cursor c = (Cursor) lv.getAdapter().getItem(position);
 		String url = c.getString(c.getColumnIndex("url"));
 
 		// Open Url in Browser
 		try {
 			Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			startActivity(viewIntent);
 		} catch (Exception e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> av, View v,
 			final int position, long id) {
		if (id == -1) {
			return false;
		}
 
 		// confirm delete
 		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {

 				// delete link from list, refresh link list
 				Cursor c = (Cursor) av.getAdapter().getItem(position);
 				int _id = c.getInt(c.getColumnIndex("_id"));
 
 				LinkManager lm = new LinkManager(av.getContext(), Const.db);
 				lm.deleteFromDb(_id);
 				adapter.changeCursor(lm.getAllFromDb());
 				lm.close();
 			}
 		};
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Wirklch löschen?");
 		builder.setPositiveButton("Ja", listener);
 		builder.setNegativeButton("Nein", null);
 		builder.show();
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 		// add a new link
 		EditText editName = (EditText) findViewById(R.id.lname);
 		EditText editUrl = (EditText) findViewById(R.id.url);
 
 		// prepend http:// if needed
 		String url = editUrl.getText().toString();
 		if (url.length() > 0 && !url.contains(":")) {
 			url = "http://" + url;
 		}
 		String name = editName.getText().toString();
 
 		LinkManager lm = new LinkManager(this, Const.db);
 		try {
 			Link link = new Link(name, url);
 			lm.insertUpdateIntoDb(link);
 		} catch (Exception e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 		// refresh link list
 		adapter.changeCursor(lm.getAllFromDb());
 		lm.close();
 
 		// clear form
 		editName.setText("");
 		editUrl.setText("");
 	}
 
 	@Override
 	public boolean setViewValue(View view, Cursor cursor, int index) {
 		// hide empty view elements
 		if (cursor.getString(index).length() == 0) {
 			view.setVisibility(View.GONE);
 
 			// no binding needed
 			return true;
 		} else {
 			view.setVisibility(View.VISIBLE);
 		}
 		return false;
 	}
 }
