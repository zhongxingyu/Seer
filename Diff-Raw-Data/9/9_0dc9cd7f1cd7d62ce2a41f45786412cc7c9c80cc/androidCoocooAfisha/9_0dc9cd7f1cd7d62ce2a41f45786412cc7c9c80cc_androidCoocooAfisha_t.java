 package ua.in.leopard.androidCoocooAfisha;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class androidCoocooAfisha extends MainActivity implements OnClickListener {
 	private DataProgressDialog backgroudUpdater;
 	private Intent serviceIntent;
 	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         	if (isOnline()){
 	        	if(backgroudUpdater.getStatus() == AsyncTask.Status.PENDING) {
 					 backgroudUpdater.execute();
 				}
         	}
         }
     };
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         setDashboardButtons();
         
         restoreBackgroudUpdate();
     }
     
     private void setDashboardButtons(){
     	View cinemasButton = findViewById(R.id.cinemas_button);
         cinemasButton.setOnClickListener(this);
         ImageView cinemasImageButton = (ImageView)cinemasButton.findViewById(R.id.dashboard_item_image);
         cinemasImageButton.setImageResource(R.drawable.dashboard_cinema_button);
         TextView cinemasTextButton = (TextView)cinemasButton.findViewById(R.id.dashboard_item_text_label);
         cinemasTextButton.setText(R.string.cinemas_label);
         
         View theatersButton = findViewById(R.id.theaters_button);
         theatersButton.setOnClickListener(this);
         ImageView theatersImageButton = (ImageView)theatersButton.findViewById(R.id.dashboard_item_image);
         theatersImageButton.setImageResource(R.drawable.dashboard_theater_button);
         TextView theatersTextButton = (TextView)theatersButton.findViewById(R.id.dashboard_item_text_label);
         theatersTextButton.setText(R.string.theaters_label);
         
         View theatersMapButton = findViewById(R.id.theaters_map_button);
         theatersMapButton.setOnClickListener(this);
         ImageView theatersMapImageButton = (ImageView)theatersMapButton.findViewById(R.id.dashboard_item_image);
         theatersMapImageButton.setImageResource(R.drawable.dashboard_map_button);
         TextView theatersMapTextButton = (TextView)theatersMapButton.findViewById(R.id.dashboard_item_text_label);
         theatersMapTextButton.setText(R.string.theaters_map_label);     
         
         View updateButton = findViewById(R.id.update_button);
         updateButton.setOnClickListener(this);
         ImageView updateImageButton = (ImageView)updateButton.findViewById(R.id.dashboard_item_image);
         updateImageButton.setImageResource(R.drawable.dashboard_update_button);
         TextView updateTextButton = (TextView)updateButton.findViewById(R.id.dashboard_item_text_label);
         updateTextButton.setText(R.string.update_label);
         
         View dashboardLogo = findViewById(R.id.dashboard_bar_logo);
         dashboardLogo.setClickable(false);
     }
     
     private void restoreBackgroudUpdate(){
     	if (getLastNonConfigurationInstance() != null) {
     		backgroudUpdater = (DataProgressDialog)getLastNonConfigurationInstance();
     		if(backgroudUpdater.getStatus() == AsyncTask.Status.RUNNING) {
     			backgroudUpdater.newView(this);
     		}
     	}
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
     	if(backgroudUpdater != null && backgroudUpdater.getStatus() == AsyncTask.Status.RUNNING) {
     		backgroudUpdater.closeView();
     	}
     	return(backgroudUpdater);
     }
   
     @Override
     protected void onResume() {
        super.onResume();
        setTitle(getString(R.string.current_city_title) + " " + EditPreferences.getCity(this));
        
        if (EditPreferences.getTheaterUrl(this) == "" || EditPreferences.getCinemasUrl(this) == ""){
        	startActivity(new Intent(this, EditPreferences.class));
        	Toast.makeText(this, getString(R.string.select_city_dialog), Toast.LENGTH_LONG).show();
        } else {
 
        		if (EditPreferences.getAutoUpdate(this)){
 	       		if (backgroudUpdater == null){
 	       			backgroudUpdater = new DataProgressDialog(this);
 	       		}
 	       		serviceIntent = new Intent(this, DataUpdateService.class);
 	        	startService(serviceIntent);
 	        	registerReceiver(broadcastReceiver, new IntentFilter(DataUpdateService.FORCE_DATA_UPDATE));
 	        }
        }
     }
 
     @Override
     protected void onPause() {
        super.onPause();
    	   if (EditPreferences.getAutoUpdate(this)){
 	   		unregisterReceiver(broadcastReceiver);
 	   		// no need to stop
 			//stopService(serviceIntent);
        }
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (EditPreferences.getTheaterUrl(this) == "" || EditPreferences.getCinemasUrl(this) == ""){
         	startActivity(new Intent(this, EditPreferences.class));
         	Toast.makeText(this, getString(R.string.select_city_dialog), Toast.LENGTH_LONG).show();
         } else {
 			switch (v.getId()) {
 			  case R.id.cinemas_button:
 				 tracker.trackPageView("/films_button");
				 tracker.dispatch();
 				 startActivity(new Intent(this, Cinemas.class));
 		         break;
 			  case R.id.theaters_button:
 				 tracker.trackPageView("/cinemas_button");
				 tracker.dispatch();
 				 startActivity(new Intent(this, Theaters.class));
 		         break;
 			  case R.id.theaters_map_button:
 				 tracker.trackPageView("/map_button");
				 tracker.dispatch();
 				 if (isOnline()){
 					 startActivity(new Intent(this, TheatersMap.class));
 				 } else {
 					 new AlertDialog.Builder(this)
 						.setTitle(getString(R.string.offline_error_title))
 						.setMessage(getString(R.string.offline_error_message))
 						.setNeutralButton(getString(R.string.offline_error_button), new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dlg, int sumthin) {
 								// do nothing – it will close on its own
 							}
 						}).show();
 				 }
 		         break;
 			  case R.id.update_button:
 				 backgroudUpdater = new DataProgressDialog(this);
 				 if(backgroudUpdater.getStatus() == AsyncTask.Status.PENDING) {
 					 backgroudUpdater.execute();
 				 }
 		         break;         
 		      }
         }
 	}
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       super.onCreateOptionsMenu(menu);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.menu, menu);
       return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
       	case R.id.settings:
       	 tracker.trackPageView("/settings_button");
      	 tracker.dispatch();
          startActivity(new Intent(this, EditPreferences.class));
          return true;
       	case R.id.about_button:
       	 tracker.trackPageView("/about_button");
      	 tracker.dispatch();
       	 startActivity(new Intent(this, About.class));
       	return true;
       	default:
 	     return super.onOptionsItemSelected(item);
       }
    }
 
 }
