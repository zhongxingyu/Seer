 package com.openmap.grupp1;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.SearchView;
 import android.widget.TextView;
 
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class CreateDialogs {
 	
 	private LatLng point;
 	private Resources res;
 	private GoogleMap myMap;
 	private Context context;
 	private PopupWindow searchPopup = new PopupWindow();
     private ListView mListView;
     private SearchView searchView;
     private TagsDbAdapter mDbHelper;
     private TextView tagText;
 
 	
 	public CreateDialogs(){
 		
 	}
 	
 	public boolean isShowingSearch() {
 		return searchPopup.isShowing();
 	}
 	
 	public void dismissSearch() {
 		searchPopup.dismiss();
 	}
 
 	public void confirmLocationPopup(Context context,final LatLng point,final Resources res, final GoogleMap myMap){
 		this.context = context;
 		//POPUP som fungerar
 		   int popupWidth = 700;
 		   int popupHeight = 100;
 
 		   // Inflate the popup_layout.xml
 		LinearLayout viewGroup = (LinearLayout) ((Activity) context).findViewById(R.layout.activity_main);
 		   LayoutInflater layoutInflater = (LayoutInflater) context
 		     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		  
 		   View layout = layoutInflater.inflate(R.layout.confirmview, viewGroup);
 		   // Creating the PopupWindow
 		   final PopupWindow popup = new PopupWindow(context);
 		   popup.setContentView(layout);
 		   popup.setWidth(600);
 		   popup.setHeight(popupHeight);
 		   
 		   Button buttonYES = (Button) layout.findViewById(R.id.buttonYes);
 		   Button buttonNO = (Button) layout.findViewById(R.id.buttonNo);
 			  
 			  buttonYES.setClickable(true);
 			  buttonNO.setClickable(true);
 				 
 				buttonYES.setOnClickListener(
 						new OnClickListener(){
 
 							@Override
 							public void onClick(View arg0) {
 								popup.dismiss();
 								startNewActivity();
 							}
 							
 						});
 				
 				buttonNO.setOnClickListener(new OnClickListener(){
 					@Override
 					public void onClick(View arg0) {
 						popup.dismiss();
 						
 					}});
 		   popup.showAtLocation(layout, Gravity.BOTTOM, 0,  0);
 		   
 	}
 
 	private void startNewActivity() {
 		Intent intent =new Intent(context, CreateEventActivity.class);
 		context.startActivity(intent);		
 	}
 
 private void setValues(String title, String description){
 	MarkerFactory markerFactory = new MarkerFactory();
 	Bitmap scr = markerFactory.createPic(title, res, "Location");
 	Marker m = myMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(scr)));
 	m.isVisible();
 	Log.d("Test", "Test" + title);
 }
 
 
 public void showInfo(Context context,final LatLng point,final Resources res, final GoogleMap myMap){
 
    //POPUP som fungerar
   
	int popupWidth = 400;
	int popupHeight = 400;
 
    // Inflate the popup_layout.xml
 LinearLayout viewGroup = (LinearLayout) ((Activity) context).findViewById(R.layout.activity_main);
    LayoutInflater layoutInflater = (LayoutInflater) context
      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 View layout = layoutInflater.inflate(R.layout.showinfopopup, viewGroup);
 
 
 TextView titleView = (TextView) layout.findViewById(R.id.titleView1);
 TextView descriptionView = (TextView) layout.findViewById(R.id.descriptionView1);
 
 String title = "title";
 String description = "Description";
 
 titleView.setText(title);
 descriptionView.setText(description);
 
    // Creating the PopupWindow
    final PopupWindow popup = new PopupWindow(context);
    popup.setContentView(layout);
    popup.setWidth(popupWidth);
    popup.setHeight(popupHeight);
    popup.setFocusable(true);
    // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
 
    Projection projection =	myMap.getProjection();
    // Clear the default translucent background
  //  popup.setBackgroundDrawable(new BitmapDrawable());
  
    //Displaying the popup at the specified location, + offsets.
    popup.showAtLocation(layout, Gravity.NO_GRAVITY, 
 		   projection.toScreenLocation(point).x -popupWidth/2, 
 		   projection.toScreenLocation(point).y - popupHeight/2);
 }
 /*
 //Undersk varfr det mste vara final hr + styr upp denna koden 
 public void insertInfo(Context context,final LatLng point,final Resources res, final GoogleMap myMap){
 		this.context = context;
 		this.point = point;
 		this.res = res;
 		this.myMap = myMap;
 		
 	final String TAG = "MyActivity";
 		  Log.d("Button", "take picture5");
 		LayoutInflater li = LayoutInflater.from(context);
 		View popupView = li.inflate(R.layout.dialog1, null);
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 		// set prompts.xml to alertdialog builder
 		//the two strings
 		txtTitle = (EditText) popupView.findViewById(R.id.txtTitle);
 		txtDescription = (EditText) popupView.findViewById(R.id.txtDescription);
 			alertDialogBuilder.setView(popupView);
 			Log.d(TEXT_SERVICES_MANAGER_SERVICE, "durhr0");
 		// set dialog message
 		alertDialogBuilder
 		.setCancelable(false)
 		.setPositiveButton("OK",
 		new DialogInterface.OnClickListener() {		
 				  public void onClick(DialogInterface dialog,int id) {
 					  	setValues(txtTitle.getText().toString(),txtDescription.getText().toString());
 					  	
 				  }  })
 				.setNegativeButton("Cancel",
 				  new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog,int id) {
 					dialog.cancel();
 					// marker.remove();
 				    }
 				  });
 
 			// create alert dialog
 			AlertDialog alertDialog = alertDialogBuilder.create();
 			//cancelable(enable backkey)
 			
 			alertDialog.setCancelable(true);
 			// show it
 		
 			alertDialog.show();
 		
 		 // */
 			  
 }
 
 
