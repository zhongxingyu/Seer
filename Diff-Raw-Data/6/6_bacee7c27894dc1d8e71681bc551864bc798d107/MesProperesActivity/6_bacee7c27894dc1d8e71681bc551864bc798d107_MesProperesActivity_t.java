 package com.jesjimher.bicipalma;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jesjimher.bicipalma.ResultadoBusqueda;
 
 public class MesProperesActivity extends Activity implements LocationListener,DialogInterface.OnDismissListener,SharedPreferences.OnSharedPreferenceChangeListener {
 	LocationManager locationManager;
 	Location lBest=null;
 	// Tiempo inicial de bsqueda de ubicacin
 	long tIni;
 	ProgressDialog dRecuperaEst;
 	private String mUbic;
 	private SharedPreferences prefs;
 	
 	private ArrayList<Estacion> estaciones;
 	
 	private RecuperarEstacionesTask descargaEstaciones;
 	
 	private boolean estatWifi=false;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mesproperes);
         this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         // Si s'ha d'activar el wifi en inici, fer-ho
     	WifiManager wm=(WifiManager) this.getSystemService(Context.WIFI_SERVICE);        	
     	// Guardar el estado actual para restaurarlo al salir
     	this.estatWifi=wm.isWifiEnabled();
         if (prefs.getBoolean("activarWifiPref", false)) 
         	wm.setWifiEnabled(true);        	
         
         estaciones=new ArrayList<Estacion>();
         
         // Leer las estaciones de disco si estn disponibles
         // Se busca primero una copia previa, y si no hay (primera ejecucin) se usa la esttica        
         try {
         	BufferedReader fis;
 			File f=new File(getFilesDir(),"estaciones.json");
			if (f.exists() && (f.length()>1000))
 				fis=new BufferedReader(new FileReader(f));
 			else
 				fis=new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.estaciones)));
 			
 			String s=fis.readLine();
 			fis.close();
 			estaciones=leerFicheroEstaciones(new JSONArray(s));
 			// Poner n de bicis/anclajes a desconocido
 			for(Estacion e:estaciones) {
 				e.setAnclajesLibres(-1);
 				e.setBicisLibres(-1);
 			}
 			actualizarListado();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
         	
         // Descargar las estaciones desde la web (en un thread aparte)
 		// TODO: timeout si la web est cada
         descargaEstaciones=new RecuperarEstacionesTask(this);
         descargaEstaciones.execute();
         
         // Inicialmente se busca por red (ms rpido)
  //    	dBuscaUbic=ProgressDialog.show(c, "",getString(R.string.buscandoubica),true,true);
 //        Toast.makeText(getApplicationContext(), "Activando", Toast.LENGTH_SHORT).show();
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mUbic=LocationManager.NETWORK_PROVIDER;
         locationManager.requestLocationUpdates(mUbic, 10, 0, (LocationListener) this);
 
         // Usar ltima ubicacin conocida de red para empezar y recibir futuras actualizaciones
         lBest=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      
     	// Guardar el inicio de bsqueda de ubicacin para no pasarse de tiempo
     	//tIni=new Date().getTime();
         tIni=System.currentTimeMillis();
 
         // Crear listener para abrir una estacin en Google Maps al seleccionarla
         // TODO: Mirar si abrir GMaps externo o interno
         // TODO: Men con clic largo para abrir en gmaps, navigation
         ListView lv=(ListView) findViewById(R.id.listado);
         lv.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View v,int position,long id) {
         		ResultadoBusqueda rb=(ResultadoBusqueda) parent.getAdapter().getItem(position);
         		// Ms rpido en posicionar, pero no muestra pin
 //        		String uri="geo:"+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude();
         		String uri="geo:0,0?q="+rb.getEstacion().getLoc().getLatitude()+","+rb.getEstacion().getLoc().getLongitude()+" ("+rb.getEstacion().getNombre()+")";
         		startActivity(new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(uri)));
 //        		Toast.makeText(getApplicationContext(), rb.getEstacion().getNombre(),Toast.LENGTH_SHORT).show();
         	}
 		});        	
     }
 
 	/**
 	 * Activa la bsqueda de ubicacin usando el mejor mtodo disponible
 	 */
 	private void activarUbicacion() {
 		locationManager.removeUpdates(this);
 		// Comprobar si se ha activado o no el GPS, y decidir el mtodo para ubicarse
         if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
         	mUbic=LocationManager.GPS_PROVIDER;
         else {
 //        	Toast.makeText(getApplicationContext(), R.string.avisonogps, Toast.LENGTH_LONG).show();
         	mUbic=LocationManager.NETWORK_PROVIDER;
         }
         locationManager.requestLocationUpdates(mUbic, 10, 0, (LocationListener) this);
 	}
         
     /**
      *	Actualiza el listado de estaciones 
      */
     public void actualizarListado() {
     	// Si no se han descargado las estaciones y hay ubicacin disponible, no hacer nada
     	if ((estaciones.size()==0) || (lBest==null))
     		return;
     	
     	// Ocultar el dilogo de bsqueda de ubicacin si se estaba visualizando
     	if (dRecuperaEst.isShowing())
     		dRecuperaEst.dismiss();
 
     	// Mirar si est activa la opcin de ocultar estaciones vacas
         SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         boolean ocultarVacios=sharedPrefs.getBoolean("ocultarVaciosPref", false);
 
         // Calcular distancias desde la ubicacin actual hasta cada estacin, generando
 		// un objeto Resultado
         ArrayList<ResultadoBusqueda> result=new ArrayList<ResultadoBusqueda>();
         Iterator<Estacion> i=estaciones.iterator();
         while (i.hasNext()) {
         	Estacion e=(Estacion) i.next();
         	Location aux=e.getLoc();
         	Double dist=Double.valueOf(lBest.distanceTo(aux));
         	if (!(ocultarVacios && (e.getBicisLibres()<=0)))
         		result.add(new ResultadoBusqueda(e,dist));
         }	        
         	       	        
         // Ordenar por distancia
         Collections.sort(result);	        
 
         // Mostrar
         ListView l=(ListView) this.findViewById(R.id.listado);
         l.setAdapter(new ResultadoAdapter(this,result));	    	
     }
     
     // Cuando llega una nueva ubicacin mejor que la actual, reordenamos el listado
     public void onLocationChanged(Location location) {
 		// Slo hacer algo si la nueva ubicacin es mejor que la actual
     	if (isBetterLocation(location, lBest)) {
 	    		
 //          Toast.makeText(getApplicationContext(), "Ubicacin encontrada", Toast.LENGTH_SHORT).show();
           	dRecuperaEst.setMessage(getString(R.string.recuperandolista));
 			// Actualizar precisin
 	    	TextView pre=(TextView) this.findViewById(R.id.precisionNum);
 	    	if (location.hasAccuracy())
 	    		pre.setText(String.format("%.0f m",location.getAccuracy()));
 	    	else
 	    		pre.setText("Desconocida");
 	    	
 	    	lBest=location;
 	        
     		actualizarListado();
         
 		} else {
 	    	//Toast.makeText(getApplicationContext(), "Ignorando ubicacin chunga", Toast.LENGTH_SHORT).show();
 		}
     	// Si estamos en red y est el GPS activado, pasar a GPS
     	// TODO: En API 9 se puede recibir un evento cuando se active el GPS. Investigar si se puede hacer con los modernos sin perder compatibilidad con API 8
     	if ((mUbic.equals(LocationManager.NETWORK_PROVIDER)) && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)))
     		activarUbicacion();
     }
     
     /** Determines whether one Location reading is better than the current Location fix
      *  (EXTRADO DEL SDK, MODIFICADO PARA ADAPTARLO A UBICACIN RPIDA)
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 	   // A new location is always better than no location
        if (currentBestLocation == null)
            return true;
 
        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isNewer = timeDelta > 0;
 
        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = location.getProvider().equals(currentBestLocation.getProvider());
 
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
            return true;
        return false;
    }
 
 
    // Si el GPS deja de funcionar, pasar a modo red  
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getApplicationContext(), "Cambio estado GPS", Toast.LENGTH_SHORT).show();
     	if (provider.equals(LocationManager.GPS_PROVIDER)) {
     		if (status!=LocationProvider.AVAILABLE)
     			activarUbicacion();
     	}    	    	
     }
 
 	public void onProviderEnabled(String provider) {}
     
 	public void onProviderDisabled(String provider) {}
 
 	// Dejamos de buscar ubicacin al salir y restauramos el wifi
     @Override
     public void onPause() {
     	if (locationManager!=null)
     		locationManager.removeUpdates(this);
     	// Si habia descargas en curso, pararlas
     	if (descargaEstaciones!=null) {
     		descargaEstaciones.cancel(true);
     		descargaEstaciones=null;
     	}
     	
 		WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
 		wm.setWifiEnabled(this.estatWifi);
     	
     	super.onPause();
     }
     
     // Reactivar wifi si es necesario
     public void onResume() {
     	// Reactivar suscripcin a ubicaciones
     	activarUbicacion();
 
         // Reactivar wifi si es necesario
     	if (prefs.getBoolean("activarWifiPref", false)) {
     		WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
     		wm.setWifiEnabled(true);
     	}
     	
     	super.onResume();
     }
     
     protected void OnStop() {
     	super.onStop();    	
     }
 
 	public void onDismiss(DialogInterface arg0) {
 //		Toast.makeText(getApplicationContext(), "Fin de bsqueda de ubicacin", Toast.LENGTH_SHORT).show();
     	if (locationManager!=null)
     		locationManager.removeUpdates(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    return true;
 	}	
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		//TODO: Estado del servicio
 		// Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.preferencias:
 	    	Intent settingsActivity = new Intent(getBaseContext(),PreferenciasActivity.class);
 	    	startActivity(settingsActivity);
 	        prefs.registerOnSharedPreferenceChangeListener(this);
 	        return true;
 	    case R.id.actualizar:
 	        Toast.makeText(getApplicationContext(), getString(R.string.recuperandolista), Toast.LENGTH_SHORT).show();
 	        activarUbicacion();
 	        descargaEstaciones=new RecuperarEstacionesTask(this);
 	        descargaEstaciones.execute();
 	    	return true;
 	    case R.id.estado:    	
 	    	mostrarEstadisticas();
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 
 	/**
 	 * 
 	 */
 	private void mostrarEstadisticas() {
 		int bTot=0;
 		int bAver=0;
 		int aAver=0;
 		int bLib=0;
 		int aLib=0;
 		int noBicis=0;
 		
 		for(Estacion e:estaciones) {
 			bTot+=e.getAnclajesAveriados()+e.getAnclajesLibres()+e.getAnclajesUsados();
 			bAver+=e.getBicisAveriadas();
 			aAver+=e.getAnclajesAveriados();
 			bLib+=e.getBicisLibres();
 			aLib+=e.getAnclajesLibres();
 			if (e.getBicisLibres()==0)
 				noBicis++;
 		}
 		AlertDialog.Builder builder=new AlertDialog.Builder(this);
 		// Si el n de bicis es negativo, es q an no se han descargado los datos
 		if (bLib<0) {
 			builder.setMessage(R.string.sinDescargaTodavia)
 				   .setCancelable(true)
 				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();						
 					}
 				});
 			AlertDialog alert=builder.create();
 			alert.show();
 		}
 		else {
 			String mensBicis=String.format(getString(R.string.estadisticasBicis),
 					bTot,
 					bTot-(bLib+bAver),
 					100*(1-bLib/(0.0+bTot-bAver)),
 					bLib,
 					100*bLib/(0.0+bTot-bAver),
 					bAver,
 					100*bAver/Double.valueOf(bTot)
 					);
 			String mensAnclajes=String.format(getString(R.string.estadisticasAnclajes), 
 					bTot,
 					bTot-(aLib+aAver),
 					100*(1-aLib/(0.0+bTot-aAver)),
 					aLib,
 					100*aLib/(0.0+bTot-aAver),
 					aAver,
 					100*aAver/Double.valueOf(bTot)
 					);
 			
 			String mensVacias=String.format(getString(R.string.estadisticasVacias), noBicis,estaciones.size());
 			
 			builder.setMessage(mensBicis+mensAnclajes+mensVacias)
 				   .setTitle(R.string.estadoservicio)
 				   .setCancelable(true)
 				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();						
 					}
 				});
 			AlertDialog alert=builder.create();
 			alert.show();
 		}
 	}	
 	// Clase privada para recuperar la lista de estaciones en segundo plano
 	private class RecuperarEstacionesTask extends AsyncTask<Void, Void, ArrayList<Estacion>> {
 
 		Context c;
 		
 	    public RecuperarEstacionesTask(Context c) {
 	    	this.c=c;
 	    }
 	    
 	    @Override
 		protected void onPreExecute() {
 	    	int mensaje;
 	    	if (lBest==null)
 	    		mensaje=R.string.buscandoubicaylista;
 	    	else
 	    		mensaje=R.string.recuperandolista;
     		dRecuperaEst = ProgressDialog.show(c, "", getString(mensaje),true,true);
     		// Si ya tenemos las estaciones cacheadas, cambiar el texto
 	    	if (estaciones.size()>0) {
 	    		if (lBest==null)
 	    			dRecuperaEst.setMessage(getText(R.string.buscandoubica));
 	    		else
 	    			dRecuperaEst.dismiss();
 	    	}
 	    	
 	    	ProgressBar pb=(ProgressBar) findViewById(R.id.progreso);
 	    	pb.setIndeterminate(true);
 	    	pb.setVisibility(View.VISIBLE);
 	    }
 		
 	    // Cuando acabe de descargar, activar la bsqueda de ubicacin 
 	    protected void onPostExecute(ArrayList<Estacion> result) {
 //	          Toast.makeText(getApplicationContext(), "Descargadas estaciones", Toast.LENGTH_SHORT).show();
 	    	// Cerrar dilogo y guardar resultados
 	    	if (result==null) {
 	    		AlertDialog.Builder builder=new AlertDialog.Builder(c);
 				builder.setMessage(R.string.errorconexion)
 				   .setCancelable(true)
 				   .setTitle(R.string.error)
 				   .setPositiveButton(R.string.cerrar, new DialogInterface.OnClickListener() {
 					
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();						
 					}
 				});
 			AlertDialog alert=builder.create();
 			alert.show();
 	    	}
 	    	else {
 	    		estaciones=result;
 	    		actualizarListado();
 	    	}
 	    	
 	    	if (lBest==null)
 	    		dRecuperaEst.setMessage(getString(R.string.buscandoubica));
 	    	else
 	    		dRecuperaEst.dismiss();
 	    	
 	    	ProgressBar pb=(ProgressBar) findViewById(R.id.progreso);
 	    	pb.setVisibility(View.INVISIBLE);
 
 	    }
 
 		@Override
 		protected ArrayList<Estacion> doInBackground(Void... arg0) {
 	    	JSONArray json=BicipalmaJsonClient.connect("http://83.36.51.60:8080/eTraffic3/DataServer?ele=equ&type=401&li=2.6226425170898&ld=2.6837539672852&ln=39.588022779794&ls=39.555621694894&zoom=15&adm=N&mapId=1&lang=es");
 	    	
 	    	if (json.length()>0)
 	    		return leerFicheroEstaciones(json);
 	    	else {	    		
 	    		return null;
 	    	}
 		}
 	 }
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
 		  if (key.equals("ocultarVaciosPref"))
 			  actualizarListado(); 		
 		  
 		  if (key.equals("activarWifiPref")) {
 			  if (sharedPreferences.getBoolean("activarWifiPref", false)) {
 				  WifiManager wm=(WifiManager) getSystemService(Context.WIFI_SERVICE);
 				  wm.setWifiEnabled(true);
 			  }
 		  }
 	}
 
 	/**
 	 * @param json
 	 * @return
 	 * @throws FileNotFoundException 
 	 */
 	private ArrayList<Estacion> leerFicheroEstaciones(JSONArray json) {
 		// Extraer estaciones del JSON
 		ArrayList<Estacion> est=new ArrayList<Estacion>();
 		for(int i=0;i<json.length();i++) {
 			try {
 				String nombre=json.getJSONObject(i).getString("alia");
 				Location pos=new Location("network");
 				pos.setLatitude(json.getJSONObject(i).getDouble("realLat"));
 				pos.setLongitude(json.getJSONObject(i).getDouble("realLon"));
 				Estacion e=new Estacion(nombre,pos);
 				String html=json.getJSONObject(i).getString("paramsHtml");
 				int pos2=html.indexOf("Bicis Libres:</span>")+"Bicis Libres:</span>".length();
 				if (pos2>0)
 					e.setBicisLibres(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
 				else
 					e.setBicisLibres(0);
 				pos2=html.indexOf("Bicis Averiadas:</span>")+"Bicis Averiadas:</span>".length();
 				if (pos2>0)
 					e.setBicisAveriadas(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
 				else
 					e.setBicisAveriadas(0);
 				pos2=html.indexOf("Anclajes Libres:</span>")+"Anclajes Libres:</span>".length();
 				if (pos2>0)
 					e.setAnclajesLibres(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
 				else
 					e.setAnclajesLibres(0);
 				pos2=html.indexOf("Anclajes Usados:</span>")+"Anclajes Usados:</span>".length();
 				if (pos2>0)
 					e.setAnclajesUsados(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
 				else
 					e.setAnclajesUsados(0);
 				pos2=html.indexOf("Anclajes Averiados:</span>")+"Anclajes Averiados:</span>".length();
 				if (pos2>0)
 					e.setAnclajesAveriados(Integer.valueOf(html.substring(pos2, pos2+3).trim()));
 				else
 					e.setAnclajesAveriados(0);
 				est.add(e);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		// Escribir el JSON a disco para acelerar futuros accesos
 		try {
 			if (est.size()>0) {
 				FileOutputStream fos=openFileOutput("estaciones.json", Context.MODE_PRIVATE);
 				fos.write(json.toString().getBytes());
 				fos.close();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return est;
 	}
 	
 }
 
