 /**
 * @author stephane coutant
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>
 */
 package org.scoutant.tf;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.scoutant.tf.adapter.NoScrollArrayAdater;
 import org.scoutant.tf.command.GetTraffic;
 import org.scoutant.tf.command.Init;
 import org.scoutant.tf.model.LatLng;
 import org.scoutant.tf.model.Model;
 import org.scoutant.tf.model.Network;
 import org.scoutant.tf.overlay.NightOverlay;
 import org.scoutant.tf.overlay.TrafficOverlay;
 import org.scoutant.tf.util.AppRater;
 import org.scoutant.tf.util.BusyIndicator;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class TrafficMap extends MapActivity {
 	private MapView mapView;
 	private MapController mapController;
 	
 	static final int MENU_ITEM_PREFERENCES=-1;
 	static final int MENU_HELP = 0;
 	static final int MENU_REFRESH = 1;
 	static final int MENU_VOTE = 2;
 	static final int MENU_DAY = 3;
 	static final int MENU_NIGHT = 4;
 	
 	private static final String tag = "activity";
 	public SharedPreferences prefs;
 	private Overlay overlay;
 	private ArrayAdapter<CharSequence> adapter;
 	private Spinner spinner;
 	private Timer timer;
 	private BusyIndicator indicator;
 	private Toast toast;
 	private Display display;
 	private Editor editor;
 	private boolean initDone=false;
 	public static final int ALPHA=146;
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 
 		setContentView(R.layout.map);
         findViewById(R.id.minus).setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mapController.zoomOut();
 			}
 		});
         findViewById(R.id.help).setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				openOptionsMenu();
 			}
 		});
         prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		editor = prefs.edit();
 		
         Log.d(tag, "onCreate");
         timer = new Timer(true);
         
         spinner = (Spinner) findViewById(R.id.spinner);
         adapter = NoScrollArrayAdater.createFromResource( this, R.array.cityNames, R.layout.spinner_item);
         adapter.setDropDownViewResource( R.layout.spinner_dropdown_item);
         spinner.setAdapter(adapter);
         
         mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapController = mapView.getController();
         overlay = new TrafficOverlay( prefs);
 		mapView.getOverlays().add( overlay);
 		new Init().execute();
 		mapController.setCenter( new LatLng(45.1794,5.7316) );
         mapController.setZoom(13 );
         mapView.setBuiltInZoomControls(false);
         
         View progress = findViewById(R.id.ProgressBar);
         indicator = new BusyIndicator(this, progress);
         
     	toast = new Toast(this);
     	toast.setView( getLayoutInflater().inflate(R.layout.wellcome_toast, (ViewGroup) findViewById(R.id.toast)));
     	toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 200);
     	toast.setDuration(Toast.LENGTH_SHORT);
     	
 		AppRater.app_launched( this);
 		
 		mapView.getOverlays().add( new NightOverlay( prefs));
 		}
 	  
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(tag, "on resume...");
 		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		int mode = (isNight() ? WindowManager.LayoutParams.FLAG_FULLSCREEN : WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, mode);
 		
 		showToast(null);
         if (isAirplaneModeOn(this)) {
         	// TODO suggest to get directly to that setting screen?
 			new AlertDialog.Builder(this)
 			.setMessage("Pour utiliser cette application, il faut autoriser la connexion internet! Typiquement désactiver le mode 'Avion' \nAller dans Accueil > menu > paramètres > Sans fil et réseau > Mode avion.")
 			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 					}
 				})
 			.create()
 			.show();
 			return;
         }
         spinner.setSelection( preferred());
        // TODO : let the spinner visible on resume and then launch an animation to hide it...  
        spinner.setVisibility( isNight() ? View.GONE : View.VISIBLE);
         if (spinner.getOnItemSelectedListener()==null) {
        	// setting the listener launches it!
 	        spinner.setOnItemSelectedListener( new OnItemSelectedListener() {
 				@Override
 	            public void onItemSelected( AdapterView<?> parent, View view, int position, long id) {
 	        		Network n = Model.model().country.find( position);
 	        		if (n!=null) {
 	        			saveSelected(position);
 	       				refresh();
 	        		}
 				}
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					Log.d(tag, "nothing selected or no different selection? ");
 				}
 	        });
         } else {
         	refresh();
         }
         
         ((ImageView) findViewById(R.id.help)).setAlpha( isNight() ? ALPHA/4 : ALPHA );
         ((ImageView) findViewById(R.id.plus)).setAlpha( isNight() ? ALPHA/4 : ALPHA );
         ((ImageView) findViewById(R.id.minus)).setAlpha(isNight() ? ALPHA/4 : ALPHA );
         ((ImageView) findViewById(R.id.trafic_fute)).setAlpha(isNight() ? 127 : 255 );
 	} 
 	  
 	@Override
 	protected void onPause() {
 		Log.d(tag, "onPause");
 		timer.cancel();
 		timer.purge();
 		indicator.hide();
 		super.onPause();
 	}
 	@Override
 	protected void onDestroy() {
 		Log.d(tag, "onDestroy");
 		super.onDestroy();
 	}
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 
 	/** 
 	 * Icons from http://commons.wikimedia.org/wiki/Crystal_Clear, With licence Creative Commons share Alike, CC BY-SA
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		menu.clear();
 		menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Aide").setIcon( R.drawable.help_48);
 		boolean isNight = prefs.getBoolean( NightOverlay.KEY, false);
 		if (isNight) {
 			menu.add(Menu.NONE, MENU_DAY, Menu.NONE, "mode normal").setIcon( R.drawable.help_48);			
 		} else {
 			menu.add(Menu.NONE, MENU_NIGHT, Menu.NONE, "mode nuit").setIcon( R.drawable.help_48);
 		}
 		menu.add(Menu.NONE, MENU_VOTE, Menu.NONE, "votre avis").setIcon( R.drawable.love_48);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		if (id == MENU_ITEM_PREFERENCES) 
 			startActivity( new Intent(this, org.scoutant.tf.Settings.class));
 		if (id==MENU_HELP)
 	        startActivity( new Intent(this, Help.class));
 //		if (id==MENU_REFRESH)
 //			refresh();
 		if (id==MENU_VOTE) {
 			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.scoutant.tf")); 
 			startActivity(intent);
 		}
 		if (id==MENU_NIGHT) {
 			editor.putBoolean( NightOverlay.KEY, true).commit();
 			onResume();
 		}
 		if (id==MENU_DAY) {
 			editor.putBoolean( NightOverlay.KEY, false).commit();
 			onResume();
 		}
 		return true;
 	}
 	
 	
 	public void refresh() {
 		Network n = Model.model().country.find( preferred() );
 		if (n!=null) {
 			mapController.setZoom( n.zoom);
 			mapController.animateTo( n.center );
 		}
 		timer.cancel();
 		timer.purge();
 		timer = new Timer(true);
 		// the web services are refreshed every 6 min. Here we are poling at half period.
 		timer.schedule( new RepetedGetTrafficTask(), 0, 3*60*1000);
 	}
 	
 	public int preferred() {
 		// Rennes, as n°2 in list arrays.xml, to be default...
 		return new Integer( prefs.getString("city", "2"));
 	}
 	
 	public void saveSelected(int id) {
 		editor.putString("city", ""+id);
 		editor.commit();
 		Log.d(tag, "saving preference : " + id);
 	}
 	
 	
 	private class RepetedGetTrafficTask extends TimerTask {
 		@Override
 		public void run() {
 			Log.d(tag, "********************************************* THREAD by TIMER *****************************************");
 			indicator.show();
 			try {
 				new GetTraffic().execute( preferred() );
 			} catch (Exception e) {
 				Log.e(tag, "Could not get traffic for : " + preferred(), e);
 			}
 			indicator.hide();
 			mapView.postInvalidate();
 		}
 	}
 	
 	private void showToast(CharSequence msg) {
     	toast.show();
     }
 
     /**
      * Gets the state of Airplane Mode.
      * @param context
      * @return true if enabled.
      */
     private static boolean isAirplaneModeOn(Context context) {
         return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
     }
 	
     /**
      * On small and medium devices (smartphones, Galaxy Tab 7'') we'll display the spinner only in portrait mode.
      * on tablets, the spinner is display in any case.
      */
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
     	super.onConfigurationChanged(newConfig);
    	spinner.setVisibility( isTabletOrSmartphoneAsPortrait() && !isNight() ? View.VISIBLE  : View.GONE);
     }
     
     private boolean isTabletOrSmartphoneAsPortrait() {
 //    	Log.d(tag, "height : " + display.getHeight() + ", width : " + display.getWidth());
 		if (display.getHeight() >= display.getWidth()) return true;		
 		if (display.getHeight() >= 720) return true;    	
     	return false;
     }
 	private boolean isNight() {
 		return prefs.getBoolean( NightOverlay.KEY, false); 
 	}
 }
