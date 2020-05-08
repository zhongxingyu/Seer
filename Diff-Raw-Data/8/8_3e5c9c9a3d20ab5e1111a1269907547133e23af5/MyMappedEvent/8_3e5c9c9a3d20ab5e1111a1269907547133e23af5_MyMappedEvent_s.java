 package com.andrewsoft;
 
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class MyMappedEvent extends TabActivity {
 	private TabHost tabHost;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		setTabs();
 
 		// First load?
 		SharedPreferences preferences = this.getSharedPreferences(
 				this.getString(R.string.sharedSettingsName),
 				MODE_WORLD_WRITEABLE);
 		boolean firstLoad = preferences.getBoolean("firstLoad", true);
 		if (firstLoad) {
			this.getUserPreferences();
 		}
 	}
 
 	private void getUserPreferences() {
 		getStartRecording();
 		getEventType();
 	}
 
 	/**
 	 * 
 	 */
 	private void getStartRecording() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Do you want to start recording?")
 				.setCancelable(false)
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								Common.setRecording(true);
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
 
 	/**
 	 * 
 	 */
 	private void getEventType() {
 		final CharSequence[] events = { "25 mile", "60 miles", "100 miles" };
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Choose your event");
 		builder.setItems(events, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				Common.setMapType(events[item].toString());
 			}
 		});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 	}
 
 	private void setTabs() {
 		tabHost = getTabHost();
 
 		addTab(R.string.mapTabText, R.drawable.a, Map.class);
 		addTab(R.string.turnsTabText, R.drawable.tab_info, MockActivity.class);
 		addTab(R.string.infoTabText, R.drawable.tab_info, InfoActivity.class);
 		addTab(R.string.shareTabText, R.drawable.tab_info, MockActivity.class);
 	}
 
 	private <T> void addTab(int labelId, int drawableId, Class<T> classToAdd) {
 		Intent intent = new Intent(this, classToAdd);
 		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);
 
 		View tabIndicator = LayoutInflater.from(this).inflate(
 				R.layout.tab_indicator, getTabWidget(), false);
 
 		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
 		title.setText(labelId);
 		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
 		icon.setImageResource(drawableId);
 
 		spec.setIndicator(tabIndicator);
 		spec.setContent(intent);
 		tabHost.addTab(spec);
 	}
 }
