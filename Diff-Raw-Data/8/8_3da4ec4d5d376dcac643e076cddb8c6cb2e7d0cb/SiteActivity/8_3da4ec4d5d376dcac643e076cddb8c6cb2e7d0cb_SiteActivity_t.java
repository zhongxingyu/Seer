 /**
  * 
  */
 package com.github.progval.openquote;
 
 // Project specific
 import com.github.progval.openquote.SiteItem;
 import com.github.progval.openquote.sites.VdmActivity;
 
 // User interface
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 // Android
import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 
 /**
  * Abstract class for a source site.
  * 
  * @author ProgVal
  *
  */
 public abstract class SiteActivity extends ListActivity implements OnClickListener {
 	private ArrayList<String> listItems = new ArrayList<String>();
 	ArrayAdapter<String> adapter;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.siteactivity);
 		adapter=new ArrayAdapter<String>(this,
 				R.layout.siteitem,
 				listItems);
 		setListAdapter(adapter);
 
 		Button buttonLatest = (Button)findViewById(R.id.buttonLatest);
 		buttonLatest.setOnClickListener(this);
 		Button buttonTop = (Button)findViewById(R.id.buttonTop);
 		buttonTop.setOnClickListener(this);
 
 		this.onClick(buttonLatest);
 	}
 
 	public void onClick(View v) {
 		try {
 			switch (v.getId()) {
 				case R.id.buttonLatest:
 					this.populate(this.getLatest());
 					break;
 				case R.id.buttonTop:
 					this.populate(this.getTop());
 					break;
 			}
 		}
 		catch (IOException e) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("Error");
			adb.setMessage("The quotes could not be loaded due to a network issue.");
			adb.setPositiveButton("Ok", null);
			adb.show();
 		}
 	}
 	
 	/** Populate the activity interface with latest quotes */
 	public abstract SiteItem[] getLatest()  throws IOException;
 	/** Populate the activity interface with top quotes */
 	public abstract SiteItem[] getTop()  throws IOException;
 
 	/** Takes a list of items, and add them to the ListView */
 	public void populate(SiteItem[] latest) {
 		this.clearList();
 		for (SiteItem item : latest) {
 			this.addItem(item.toString(), false);
 		}
 	}
 	/** Add an item to the list */
 	private void addItem(String item, boolean top) {
 		if (top) {
 			listItems.add(0, item);
 		}
 		else {
 			listItems.add(item);
 		}
 		adapter.notifyDataSetChanged();
 	}
 	/** Prepend an item to the list */
 	public void addItem(String item) {
 		 listItems.add(0, item);
 		 adapter.notifyDataSetChanged();
 	}
 	/** Clear the list */
 	public void clearList() {
 		listItems.clear();
 		 adapter.notifyDataSetChanged();
 	}
 }
