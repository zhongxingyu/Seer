 package com.arpia49;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VistaAlarmas extends Activity {
 
 	public static final String PREFS_NAME = "PrefTimbre";
 	public static final int ACT_ADD_ALARMA = 1;
 	public static final int ACT_DEL_ALARMA = 2;
 	public static final int ACT_LISTA_ALERTAS = 3;
 	static final private int ADD_ALARMA = Menu.FIRST;
 	static final private int DEL_ALARMA = Menu.FIRST + 1;
 	static final private int LISTA_ALERTAS = Menu.FIRST + 2;
 	static final private int INFO = Menu.FIRST + 3;
 	int usando;
 	private static String PROXIMITY_ALERT = "com.arpia49.action.proximityalert";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// meterBasuras();
 		setContentView(R.layout.main);
 		cargarPosiciones((LinearLayout) findViewById(R.id.mainLay));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		// Create and add new menu items.
 		MenuItem itemAdd = menu.add(0, ADD_ALARMA, Menu.NONE,
 				R.string.menu_add_alarma);
 		MenuItem itemDel = menu.add(0, DEL_ALARMA, Menu.NONE,
 				R.string.menu_del_alarma);
 		MenuItem itemLista = menu.add(0, LISTA_ALERTAS, Menu.NONE,
 				R.string.menu_lista_alertas);
 		MenuItem itemInfo = menu.add(0, INFO, Menu.NONE, R.string.menu_info);
 		// Assign icons
 		itemAdd.setIcon(R.drawable.add);
 		itemDel.setIcon(R.drawable.del);
 		itemLista.setIcon(R.drawable.list);
 		itemInfo.setIcon(R.drawable.help);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case (ADD_ALARMA): {
 			Intent intent = new Intent(this, AddAlarma.class);
 			startActivityForResult(intent, ACT_ADD_ALARMA);
 			return true;
 		}
 		case (DEL_ALARMA): {
 			Intent intent = new Intent(this, DelAlarmas.class);
 			startActivityForResult(intent, ACT_DEL_ALARMA);
 			return true;
 		}
 		case (LISTA_ALERTAS): {
 			Intent intent = new Intent(this, ListarAlertas.class);
 			startActivityForResult(intent, ACT_LISTA_ALERTAS);
 			return true;
 		}
 		case (INFO): {
 			Toast.makeText(getApplicationContext(),
 					"Mostrando ayuda! (mentira)", Toast.LENGTH_SHORT).show();
 			return true;
 		}
 		}
 		return false;
 	}
 
 	@Override
 	public void onActivityResult(int reqCode, int resCode, Intent data) {
 		super.onActivityResult(reqCode, resCode, data);
 		switch (reqCode) {
 		case (ACT_ADD_ALARMA): {
 			if (resCode == Activity.RESULT_OK) {
 				crearAlarma(data.getStringExtra("nombreAlarma"), data
 						.getStringExtra("descAlarma"), data
 						.getStringExtra("ubicAlarma"), data.getIntExtra(
 						"radioAlarma", 0), data.getFloatExtra("latAlarma", 0),
 						data.getFloatExtra("lngAlarma", 0));
 				Toast.makeText(getApplicationContext(), "Alarma añadida!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"La alarma no se ha creado", Toast.LENGTH_SHORT).show();
 			}
 		}
 			break;
 		case (ACT_DEL_ALARMA): {
 			if (resCode == Activity.RESULT_OK) {
 				delAlarma(Integer.parseInt(data.getStringExtra("idAlarma")) + 1);
 				Toast.makeText(getApplicationContext(), "Alarma  eliminada!",
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"No se han borrado alarmas", Toast.LENGTH_SHORT).show();
 			}
 		}
 			break;
 		}
 	}
 
 	private void cargarPosiciones(LinearLayout lx) {
 
 		// Leemos el número de alarmas
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		int numAlarmas = settings.getInt("numAlarmas", 0);
 		for (int i = 1; i <= numAlarmas; i++) {
 			addAlarma(i, settings.getBoolean("estadoAlarma" + i, false),
 					settings.getString("nombreAlarma" + i, "sin nombre"),
 					settings.getString("descAlarma" + i, "sin descripción"),
 					settings.getString("ubicAlarma" + i, "sin ubicación"),
 					settings.getInt("radioAlarma" + i, 0), settings.getFloat(
 							"latAlarma" + i, 0), settings.getFloat("lngAlarma"
 							+ i, 0));
 		}
 	}
 
 	private void crearAlarma(String nombre, String desc, String ubic,
 			int radio, float lat, float lng) {
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		int numAlarmas = settings.getInt("numAlarmas", 0) + 1;
 
 		// Añadimos los datos de la alarma al registro
 		editor.putInt("numAlarmas", numAlarmas);
 		editor.putString("nombreAlarma" + numAlarmas, nombre);
 		editor.putString("descAlarma" + numAlarmas, desc);
 		editor.putBoolean("estadoAlarma" + numAlarmas, false);
 		editor.putBoolean("activada" + numAlarmas, false);
 		if (!ubic.equals("")) {
 			editor.putString("ubicAlarma" + numAlarmas, ubic);
 			editor.putInt("radioAlarma" + numAlarmas, radio);
 			editor.putFloat("latAlarma" + numAlarmas, lat);
 			editor.putFloat("lngAlarma" + numAlarmas, lng);
 		} else {
 			editor.putString("ubicAlarma" + numAlarmas, "");
 			editor.putInt("radioAlarma" + numAlarmas, 0);
 			editor.putFloat("latAlarma" + numAlarmas, 0);
 			editor.putFloat("lngAlarma" + numAlarmas, 0);
 		}
 		editor.commit();
 
 		addAlarma(numAlarmas, false, nombre, desc, ubic, radio, lat, lng);
 
 	}
 
 	private void addAlarma(int id, boolean estado, String nombre, String desc,
 			String ubic, final int radio, float lat, float lng) {
 
 		LinearLayout lx = (LinearLayout) findViewById(R.id.mainLay);
 		LinearLayout la = new LinearLayout(this);
 		la.setId(id);
 		la.setOrientation(1);
 
 		// Creamos la alarma en la vista
 		CheckBox cb = new CheckBox(this);
 		TextView tbdesc = new TextView(this);
 		cb.setId(id);
 		tbdesc.setId(id);
 		tbdesc.setSingleLine(false);
 		cb.setText(nombre);
 		if (estado) {
 			setProximityAlert(id, lat, lng, radio);
 		}
 		cb.setChecked(estado);
 		if (!ubic.equals("")) {
 			tbdesc.setText(desc + " - " + ubic + " (" + radio + "m)");
 		} else {
 			tbdesc.setText(desc);
 		}
 
 		final float flat = lat;
 		final float flng = lng;
 		cb.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				int v_id = v.getId();
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 				SharedPreferences.Editor editor = settings.edit();
 				if (((CheckBox) v).isChecked()) {
 					editor.putBoolean("estadoAlarma" + v_id, true);
 					editor.commit();
 					setProximityAlert(v_id, flat, flng, radio);
 				} else {
 					editor.putBoolean("estadoAlarma" + v_id, false);
 					editor.commit();
 					removeProximityAlert(v_id);
 				}
 			}
 		});
 		la.addView(cb);
 		la.addView(tbdesc);
 		lx.addView(la);
 
 	}
 
 	private void delAlarma(int id) {
 
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		int numAlarmas = settings.getInt("numAlarmas", 0);
 		LinearLayout ll = (LinearLayout) findViewById(id);
 		ll.setId(numAlarmas + 1);
 		editor.putBoolean("estadoAlarma" + numAlarmas + 1, settings.getBoolean(
 				"estadoAlarma" + id, false));
 		editor.putString("nombreAlarma" + numAlarmas + 1, settings.getString(
 				"nombreAlarma" + id, "sin nombre"));
 		editor.putString("descAlarma" + numAlarmas + 1, settings.getString(
 				"descAlarma" + id, "sin descripción"));
 		editor.putString("ubicAlarma" + numAlarmas + 1, settings.getString(
 				"ubicAlarma" + id, "sin ubicación"));
 		editor.putInt("radioAlarma" + numAlarmas + 1, settings.getInt(
 				"radioAlarma" + id, 0));
 		editor.putFloat("latAlarma" + numAlarmas + 1, settings.getFloat(
 				"latAlarma" + id, 0));
 		editor.putFloat("lngAlarma" + numAlarmas + 1, settings.getFloat(
 				"lngAlarma" + id, 0));
 		editor.putBoolean("activada" + numAlarmas + 1, settings.getBoolean(
 				"activada" + id, false));
 		for (int i = id; i < numAlarmas; i++) {
 			editor.putBoolean("estadoAlarma" + i, settings.getBoolean(
 					"estadoAlarma" + (i + 1), false));
 			editor.putString("nombreAlarma" + i, settings.getString(
 					"nombreAlarma" + (i + 1), "sin nombre"));
 			editor.putString("descAlarma" + i, settings.getString("descAlarma"
 					+ (i + 1), "sin descripción"));
 			editor.putString("ubicAlarma" + i, settings.getString("ubicAlarma"
 					+ (i + 1), "sin ubicación"));
 			editor.putInt("radioAlarma" + i, settings.getInt("radioAlarma"
 					+ (i + 1), 0));
 			editor.putFloat("latAlarma" + i, settings.getFloat("latAlarma"
 					+ (i + 1), 0));
 			editor.putFloat("lngAlarma" + i, settings.getFloat("lngAlarma"
 					+ (i + 1), 0));
 			editor.putBoolean("activada" + i, settings.getBoolean("activada"
 					+ (i + 1), false));
 			if (settings.getBoolean("estadoAlarma" + (i + 1), false)) {
 				removeProximityAlert(i + 1);
 				setProximityAlert(i, settings
 						.getFloat("latAlarma" + (i + 1), 0), settings.getFloat(
 						"lngAlarma" + (i + 1), 0), settings.getInt(
 						"radioAlarma" + (i + 1), 0));
 			}
 			LinearLayout ll2 = (LinearLayout) findViewById(i + 1);
 			ll2.setId(i);
 		}
 		editor.putInt("numAlarmas", numAlarmas - 1);
 		editor.commit();
 		removeProximityAlert(id);
 		this.onCreate(null);
 	}
 
 	private void setProximityAlert(int id, double lat, double lng, int distancia) {
 		String locService = Context.LOCATION_SERVICE;
 		LocationManager locationManager;
 		locationManager = (LocationManager) getSystemService(locService);
 
 		long expiration = -1; // do not expire
 		Intent intent = new Intent(PROXIMITY_ALERT);
 		intent.putExtra("id", Integer.toString(id));
 		intent.setData((Uri.parse(id + "://" + id)));
 
 		PendingIntent proximityIntent = PendingIntent.getBroadcast(
 				getApplicationContext(), id, intent, 0);
 		locationManager.addProximityAlert(lat, lng, distancia, expiration,
 				proximityIntent);
 
 		IntentFilter filter = new IntentFilter(PROXIMITY_ALERT);
 		filter.addDataScheme(Integer.toString(id));
 		registerReceiver(new AlertaEntrante(), filter);
 
 		// guardamos las preferencias
 		Toast.makeText(getApplicationContext(), "Añadida alerta" + id,
 				Toast.LENGTH_SHORT).show();
 	}
 
 	private void removeProximityAlert(int id) {
 		String locService = Context.LOCATION_SERVICE;
 		LocationManager locationManager;
 		locationManager = (LocationManager) getSystemService(locService);
 		Intent intent = new Intent(PROXIMITY_ALERT);
 		PendingIntent proximityIntent = PendingIntent.getBroadcast(
 				getApplicationContext(), id, intent, 0);
 		locationManager.removeProximityAlert(proximityIntent);
 		Toast.makeText(getApplicationContext(), "Borrada alerta" + id,
 				Toast.LENGTH_SHORT).show();
 	}
 }
