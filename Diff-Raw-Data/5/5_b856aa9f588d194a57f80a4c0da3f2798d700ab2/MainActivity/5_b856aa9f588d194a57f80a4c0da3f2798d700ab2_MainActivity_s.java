 package net.igconsultores.raymundo.pt2;
 
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.MediaPlayer;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class MainActivity extends Activity {
 
 	public LocationManager locMana;
 	public LocationListener locList;
 	//	public BatteryManager batteryManager;
 	public Location locationMobile;
 	public SharedPreferences prefs;
 	public int minTime=0;
 	public int minDistancia=500;//en metros
 	public Context context;
 	public String bestProv;
 	public String wsGetLocUrl="http://igconsultores.net/raymundo/wsgetloc.php";
 	public String wsSendMail="http://igconsultores.net/raymundo/ws_send_mail.php";
 	public String lastLonx;
 	public String lastLaty;
 	public String lastTime;
 	//	public String lastSendLonx;
 	//	public String lastSendLaty;
 	public String lastSendTime;
 	public String modoTraslado;
 	public String LocManProvider;
 	public String wViewContainUrl="file:///android_asset/wscontainandroid.html";
 	public String wsContainPto="";
 	public String jsRest;
 
 	/**
 	 * @return 
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// TODO Put your code here
 		setContentView(R.layout.main);
 
 		//		final TextView tvConf =(TextView) findViewById(R.id.mainConf_textView2);
 		//		final TextView tvPos =(TextView) findViewById(R.id.mainPos_textView3);
 		//		final TextView tvAcer =(TextView) findViewById(R.id.mainAcer_textView4);
 		final TextView tvLati =(TextView) findViewById(R.id.textView2MainLatiTxt);
 		final TextView tvLong =(TextView) findViewById(R.id.textView4MainLongTxt);
 		final TextView tvPres =(TextView) findViewById(R.id.textView1MainPres);
 		final TextView tvTimes =(TextView) findViewById(R.id.textView2MainTimes);
 		final RadioButton rBtnGps=(RadioButton) findViewById(R.id.radio0MainGps);
 		final RadioButton rBtnNw=(RadioButton) findViewById(R.id.radio1MainNw);
 		final RadioButton rBtnPie=(RadioButton) findViewById(R.id.radioMainPie);
 		final RadioButton rBtnAuto=(RadioButton) findViewById(R.id.radioMainAuto);
 
 		prefs= getSharedPreferences(Constantes.prefsName, Context.MODE_WORLD_WRITEABLE);
 		locMana=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
 
 		/**
 		 *laty y lonx vienen con COMA en lugar de punto, por lo que se
 		 * cambia el formato del número y se pide se trunque a 5 decimales
 		 */
 		final DecimalFormatSymbols dfs=new DecimalFormatSymbols();
 		dfs.setDecimalSeparator('.');
 		final DecimalFormat df=new DecimalFormat("0.00000",dfs);
 		//Listener Localizacion
 		locList=new LocationListener() {
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				// TODO Auto-generated method stub
 				Toast toast = Toast.makeText(MainActivity.this, "StatusChanged "+provider+"."+status,Toast.LENGTH_LONG);
 				toast.show();				
 			}
 
 			@Override
 			public void onProviderEnabled(String provider) {
 				// TODO Auto-generated method stub
 				Toast toast = Toast.makeText(MainActivity.this, "ProviderEnabled "+provider,Toast.LENGTH_LONG);
 				toast.show();
 			}
 
 			@Override
 			public void onProviderDisabled(String provider) {
 				// TODO Auto-generated method stub
 				//habGps();
 				Toast toast = Toast.makeText(MainActivity.this, "ProviderDisabled "+provider,Toast.LENGTH_LONG);
 				toast.show();
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				// TODO Auto-generated method stub
 				//mostrarLocalizacion(location);
 				Log.e("MobileHunt - listener", "Cambio de localizacion: "+location);
 				if(location!=null){	
 
 					String laty = df.format(location.getLatitude());
 					String lonx= df.format(location.getLongitude());
 					String timeStamp=String.valueOf(location.getTime());
 
 					//imprimir datos en pantalla
					tvLati.setText("Provedor:"+bestProv+"\nLatitud: " + laty);
 					tvLong.setText("Longitud: " + lonx);
 					tvPres.setText("Precision: " + String.valueOf(location.getAccuracy()));			
 					Date date=new Date(location.getTime());
 					tvTimes.setText("TimesStamp: "+ timeStamp+"\n"+"Date: "+date.toString());
 
 					//verificar q no se hayan insertado
 					lastSendTime=prefs.getString("lastSendTime", "0");
 					Log.e("MobileHunt - if","Si lastSendTime:"+lastSendTime+"=="+timeStamp+" timestamp");
 					if(!lastSendTime.equals(timeStamp)&&isOnline()){						
 						String responsePhp="";
 						String usr=prefs.getString("usr", "sin dato");							
 						bestProv= prefs.getString("bestProv", "sin dato");
 						int cont=0;
 
 						//Enviamos Datos al WS
 						//SharedPreferences prefs= getSharedPreferences(Constantes.prefsName, Context.MODE_WORLD_WRITEABLE);
 						do{
 							Log.e("MobileHunt - sendData",usr+"/"+ laty+"/"+lonx+"/"+ timeStamp+"/"+bestProv);
 							responsePhp=sendLoc(usr, laty, lonx, timeStamp, bestProv);
 							Log.e("MobileHunt - responsePhp",responsePhp);
 							if(responsePhp.contains("_1")){
 								Log.d("MobileHunt - ws"," Loc insertada");
 								prefs.edit().putString("lastSendLonx",lonx).commit();
 								prefs.edit().putString("lastSendLaty",laty).commit();
 								prefs.edit().putString("lastSendTime",timeStamp).commit();
 								cont=0;
 							}
 							else {
 								Log.d("MobileHunt - responsePhp","Loc no insertada"+cont);
 								cont++;
 							}
 						}while(!responsePhp.contains("_1")&&cont<2);
 
 						//Verificamos si la localización o punto está dentro del poligono o restricción
 						//prefs.edit().putString("responsePHP", responsePhp).commit();
 						contain(laty, lonx, responsePhp);
 						//SystemClock.sleep(500);
 						Log.v("MobileHunt - contain","La restricción contiene a la localización?:"+wsContainPto);
 						wsContainPto="sin Dato";
 						Log.v("MobileHunt - FIN","verificación COMPLETA");
 
 					}else{
 						AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 						builder.setMessage(R.string.mainOnlineNo)
 						.setCancelable(false)
 						.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 						AlertDialog alertDialog = builder.create();
 						alertDialog.show();
 					}
 
 				}
 			}
 
 		};
 
 		//Modo traslado default
 		bestProv=prefs.getString("bestProv","GPS_NETWORK");
 		modoTraslado=prefs.getString("modoTras","pie");
 		if(modoTraslado.equals("pie")){
 			rBtnPie.setChecked(true);
 			modoTraslado="pie";
 			minTime=prefs.getInt(Constantes.keyMuestreo, 0);
 			minDistancia=minTime*50;
 			//prefs.edit().putString("modoTras",modoTraslado).commit();
 			Log.d("MobileHunt - traslado:", "Obtenido "+modoTraslado);	
 		}
 		else {
 			rBtnAuto.setChecked(true);
 			modoTraslado="auto";
 			minTime=prefs.getInt(Constantes.keyMuestreo, 0);
 			minDistancia=minTime*500;
 			//			prefs.edit().putString("modoTras",modoTraslado).commit();
 			Log.d("MobileHunt - traslado:", "Obtenido "+modoTraslado);
 		}
 
 
 		//provedor default
 		if(bestProv.equals("GPS_PROVIDER")){
 			rBtnGps.setChecked(true);
 			if(!locMana.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 				habGps();
 				Log.d("MobileHunt - location", "GPS habilitado");
 			}			
 		}
 		else {
 			rBtnNw.setChecked(true);
 		}
 
 
 
 		//Listener GPS
 		rBtnGps.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnGps.isChecked()){
 					Log.d("MobileHunt - locmana:", "eliminar update "+bestProv);
 					bestProv="GPS_PROVIDER";					
 					//					LocManProvider=getLocManProvider(bestProv);
 					Log.d("MobileHunt - if", "Gps esta habilitado?"+locMana.isProviderEnabled(LocationManager.GPS_PROVIDER));
 					if(!locMana.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 						habGps();
 						Log.d("MobileHunt - location", "GPS habilitado");
 					}	
 
 					prefs.edit().putString("bestProv",bestProv).commit();
 					Log.d("MobileHunt - Mejor Proveedor:", "Guardado "+bestProv);
 
 					//eliminar actualizacion anterior al LocList
 					locMana.removeUpdates(locList);
 					Log.d("MobileHunt - locmana:", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("MobileHunt - Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min\n Ditancia minima: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 					locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 					Log.d("MobileHunt - Location:", "nuevo update "+bestProv);
 				}
 				else Log.d("MobileHunt - OnchekedGps:", "Gps Radio no seleccionado "+bestProv);
 			}
 		});
 
 		rBtnNw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnNw.isChecked()){
 					Log.d("MobileHunt - locmana", "eliminar update "+bestProv);
 					bestProv="NETWORK_PROVIDER";
 					prefs.edit().putString("bestProv",bestProv).commit();
 					Log.d("MobileHunt - Mejor Proveedor", "Guardado "+bestProv);
 
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("MobileHunt - locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("MobileHunt - Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min\n Ditancia minima: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 
 					locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 					Log.d("MobileHunt - Location:", "nuevo update "+bestProv);
 				}
 				else Log.d("MobileHunt - OnchekedNW:", "Nw Radio no seleccionado "+bestProv);
 			}
 		});
 
 
 
 		//Modo de traslado
 		//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz
 		rBtnPie.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnPie.isChecked()){
 					modoTraslado="pie";
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0);
 					minDistancia=minTime*50;
 					prefs.edit().putString("modoTras",modoTraslado).commit();
 					Log.d("MobileHunt - modoTras Pie:", "Guardado "+modoTraslado);
 
 
 
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("MobileHunt - locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("MobileHunt - Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 
 					if (bestProv.equals("GPS_PROVIDER")){
 						locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 						Log.d("MobileHunt - Location:", "Cambio modo de traslado: "+ bestProv);
 					}
 					else {
 						locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 						Log.d("MobileHunt - Location:", "Cambio modo de traslado: "+ bestProv);
 					}
 
 
 
 				}
 				//				else Log.d("MobileHunt - modoTrasPie:", "NO Guardado "+modoTraslado);
 
 
 
 			}
 		});
 
 		rBtnAuto.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnAuto.isChecked()){
 					modoTraslado="auto";
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0);
 					minDistancia=minTime*500;
 					prefs.edit().putString("modoTras",modoTraslado).commit();
 					Log.d("MobileHunt - modoTrasAuto:", "Guardado "+modoTraslado);
 
 
 
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("MobileHunt - locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("MobileHunt - Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 
 					if (bestProv.equals("GPS_PROVIDER")){
 						locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 						Log.d("MobileHunt - Location:", "Auto GPS Update Loc "+ bestProv);
 					}
 					else {
 						locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 						Log.d("MobileHunt - Location:", " update Loc "+ bestProv);
 					}
 
 				}
 				//				else Log.d("MobileHunt - modoTrasAuto:", "NO Guardado "+modoTraslado);
 			}
 		});
 
 
 		/*
 		Criteria req=new Criteria();
 		//req.setAccuracy(Criteria.ACCURACY_COARSE);
 		//req.setPowerRequirement(Criteria.POWER_MEDIUM);
 		//String bestProv=locMana.getBestProvider(req, false);//el false es si queremos que nos devuelvan los provedores activados
 		 */
 
 		//obtener ultima loc del proveedor
 		Log.e("MobileHunt - Location", "antes del if ultima localizacion obtenida");
 
 		//ERROR NO DEBE SER GPS_PROVIDER		
 		locationMobile= locMana.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(locationMobile!=null){
 			//obtenemos localización
 			Log.e("MobileHunt - Location", "ultima localizacion obtenida");
 			lastLaty=df.format(locationMobile.getLatitude());
 			lastLonx=df.format(locationMobile.getLongitude());
 			lastTime=String.valueOf(locationMobile.getTime());
 
 			//Imprimimos en pantalla
			tvLati.setText("Provedor:"+bestProv+"\nlastLatitud: " + lastLaty );
 			tvLong.setText("lastLongitud: " + lastLonx);
 			tvPres.setText("Precision: " + String.valueOf(locationMobile.getAccuracy()));			
 			Date date=new Date(locationMobile.getTime());
 			tvTimes.setText("TimesStamp: "+ lastTime+"\n"+ "Date: "+date.toString());
 			Log.e("MobileHunt - if", "Obtuve ultima posición");
 			//				lastSendLonx=prefs.getString("lastSendLonx", "0");
 			//				lastSendLaty=prefs.getString("lastSendLaty", "0");
 			lastSendTime=prefs.getString("lastSendTime", "0");
 
 			Log.e("MobileHunt - lastSendTime",prefs.getString("lastSendTime", "0"));
 			//Log.e("MobileHunt - lastSendLaty",prefs.getString("lastSendLaty", "0"));
 			//Log.e("MobileHunt - lastSendLonx",prefs.getString("lastSendLonx", "0"));
 
 			//Comprobamos si la ultima localización conocida ya fue guardada			
 			if(!lastTime.equals(lastSendTime)&&isOnline()){
 				String responsePhp="";
 				String usr=prefs.getString("usr", "sin dato");
 				String timeStamp=String.valueOf(locationMobile.getTime());
 				int cont=0;
 				//Enviamos Datos al WS
 				//si no se guarda localización se intenta mandar  3 veces
 				do{					
 					Log.e("MobileHunt - sendData",usr+"/"+ lastLaty+"/"+lastLonx+"/"+ timeStamp+"/"+bestProv);
 					responsePhp=sendLoc(usr,lastLaty,lastLonx,timeStamp, bestProv);
 					Log.e("MobileHunt - responsePhp",responsePhp);
 					if(responsePhp.contains("_1")){
 						Log.d("MobileHunt - ws"," Loc insertada");						
 						prefs.edit().putString("lastSendTime",lastTime).commit();
 						prefs.edit().putString("lastSendLonx",lastLonx).commit();
 						prefs.edit().putString("lastSendLaty",lastLaty).commit();
 						cont=0;
 					}
 					else{ 
 						//Establecer timer para volver a intentar ingresar datos
 						cont++;
 						Log.d("MobileHunt - responsePhp","Loc no insertadac"+cont);
 					}
 				}while(!responsePhp.contains("_1")&&cont<2);
 
 				//prefs.edit().putString("responsePHP", responsePhp).commit();
 				//Verificamos si la localización o punto está dentro del poligono o restricción				
 				Log.v("MobileHunt - var wsContainPto","valor inicial variable "+ wsContainPto);
 				//contain(lastLaty, lastLonx, responsePhp);
 				contain(lastLaty, lastLonx,responsePhp);
 				//SystemClock.sleep(500);
 				Log.v("MobileHunt - contain","La restricción contiene a la localización?"+wsContainPto);			
 				wsContainPto="sin Dato";
 				Log.v("MobileHunt - FIN","verificación COMPLETA");
 			}else{ 
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(R.string.mainOnlineNo)
 				.setCancelable(false)
 				.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				AlertDialog alertDialog = builder.create();
 				alertDialog.show();
 			}
 
 		}else Log.e("MobileHunt - if LocMana", "NO Obtuve posición, locmana vacio");
 		//pasar milisegundos a minutos
 		minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 		Log.e("MobileHunt - Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 		//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 
 		if (bestProv.equals("GPS_PROVIDER")){
 			locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 			Log.d("MobileHunt - Location:", "Primer update Loc "+ bestProv);
 		}
 		else {
 			locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 			Log.d("MobileHunt - Location:", "Primer update Loc "+ bestProv);
 		}
 
 
 	}//Termina OnCreate
 
 
 	//Listener cuando el boton BACK sea presionado
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
 				&& keyCode == KeyEvent.KEYCODE_BACK
 				&& event.getRepeatCount() == 0) {
 			// Take care of calling this method on earlier versions of
 			// the platform where it doesn't exist.
 			onBackPressed();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		Log.d("MobileHunt - Back", "onBackPressed Called");
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Salir de la aplicación?\n**Puedes presionar el botón \"HOME\" para cambiar de aplicación")
 		.setCancelable(false)
 		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				finish();
 				//Toast.makeText(this, "Presiona la tecla de atrás para volver a la aplicación MobileHunt", Toast.LENGTH_LONG).show();
 			}
 		})
 		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) { 
 				dialog.cancel();
 			}
 		});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 	}
 
 
 	//Habilita GPS
 	public void habGps(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("El GPS esta deshabilitado, quieres habilitarlo ahora?\nRecuerda: PRESIONAR EL BOTON \"ATRAS\"para volver a esta aplicación")
 		.setCancelable(false)
 		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intGpsSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(intGpsSettings); 
 				//Toast.makeText(this, "Presiona la tecla de atrás para volver a la aplicación MobileHunt", Toast.LENGTH_LONG).show();
 			}
 		})
 		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) { 
 				dialog.cancel();
 			}
 		});
 		AlertDialog alertDialog = builder.create();
 		alertDialog.show();
 	}
 
 	/*
 public void mostrarLocalizacion (Location loc, TextView){
 	if(loc != null)
 	{
 		tvLati.setText("Latitud: " + String.valueOf(loc.getLatitude()));
 		tvLong.setText("Longitud: " + String.valueOf(loc.getLongitude()));
 		tvPres.setText("Precision: " + String.valueOf(loc.getAccuracy()));
 		tvTimes.setText("TimesStamp: " + String.valueOf(loc.getTime()));
 Log.i("MobileHunt - ", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
 	}
 	else
 	{
 		tvLati.setText("Latitud: (sin_datos)");
 		tvLong.setText("Longitud: (sin_datos)");
 		tvPres.setText("Presicion: (sin_datos)");
 		tvTimes.setText("Presicion: (sin_datos)");
 	}
 
 }	
 	 */
 
 	public String sendLoc(String usr, String laty, String lonx, String timeStamp, String bestProv){
 		HttpClient httpClient= new DefaultHttpClient();
 		HttpPost httpPost=new HttpPost(wsGetLocUrl);
 		HttpEntity httpEntity=null;
 		InputStream is=null;
 		String responsePhp="";
 		//Datos a enviar			
 		List<NameValuePair> nvp= new ArrayList<NameValuePair>();
 		nvp.add(new BasicNameValuePair("usr", usr));
 		nvp.add(new BasicNameValuePair("laty", laty));
 		nvp.add(new BasicNameValuePair("lonx", lonx));
 		nvp.add(new BasicNameValuePair("timestamp", timeStamp));
 		nvp.add(new BasicNameValuePair("bestprov", bestProv));
 		try{
 
 			httpPost.setEntity(new UrlEncodedFormEntity(nvp));
 
 			//manejamos respuesta Si responde, ejecutamos
 			HttpResponse httpResponse=httpClient.execute(httpPost);
 			//obtenemos respuesta
 			Log.v("MobileHunt - webservice","Status "+ httpResponse.getStatusLine());
 			httpEntity=httpResponse.getEntity();
 		}catch (ClientProtocolException e) {
 			Log.e("MobileHunt - webservice","ClientProtocol"+e.toString());			// TODO: handle exception
 			Toast toast = Toast.makeText(MainActivity.this, "webservice ClientProtocol"+e.toString(),Toast.LENGTH_LONG);
 			toast.show();
 		}catch (IOException e) {
 			// TODO: handle exception
 			Log.e("MobileHunt - webservice","ioException"+e.toString());
 			Toast toast = Toast.makeText(MainActivity.this, "webservice ioException"+e.toString(),Toast.LENGTH_LONG);
 			toast.show();
 		}
 
 		//convertimos respuesta a string
 		if(httpEntity!=null){
 			try{
 				is=httpEntity.getContent();
 				BufferedReader bf=new BufferedReader
 						//(new InputStreamReader(is,"iso-8859-1"),8);
 						(new InputStreamReader(is,"utf-8"),8);
 
 				StringBuilder sb=new StringBuilder();
 				String line=null;
 				while((line=bf.readLine())!=null){
 					sb.append(line+"\n");
 				}
 				is.close();
 				responsePhp=sb.toString();
 			}catch (Exception e) {
 				// TODO: handle exception
 				Log.e("MobileHunt - responsePhp",e.toString());
 				Toast toast = Toast.makeText(MainActivity.this, "response"+e.toString(),Toast.LENGTH_LONG);
 				toast.show();
 			}			
 		}
 		return responsePhp;
 	}
 
 	//Enviar mail administrador
 	public String sendMail(String usr, String laty, String lonx, String comp, String desc, String mail, String bestProv){
 		HttpClient httpClient= new DefaultHttpClient();
 		HttpPost httpPost=new HttpPost(wsSendMail);
 		InputStream is=null;
 		String responsePhp="";
 		try{
 			//Datos a enviar			
 			List<NameValuePair> nvp= new ArrayList<NameValuePair>();
 			nvp.add(new BasicNameValuePair("usr", usr));
 			nvp.add(new BasicNameValuePair("laty", laty));
 			nvp.add(new BasicNameValuePair("lonx", lonx));
 			nvp.add(new BasicNameValuePair("comp", comp));
 			nvp.add(new BasicNameValuePair("desc", desc));
 			nvp.add(new BasicNameValuePair("mail", mail));
 			nvp.add(new BasicNameValuePair("bestprov", bestProv));
 
 			httpPost.setEntity(new UrlEncodedFormEntity(nvp));
 			//Si responde, ejecutamos
 			HttpResponse httpResponse=httpClient.execute(httpPost);
 			//obtenemos respuesta
 			HttpEntity httpEntity=httpResponse.getEntity();
 			is=httpEntity.getContent();
 
 		}catch (ClientProtocolException e) {
 			Log.e("MobileHunt - webservice","ClientProtocol"+e.toString());			// TODO: handle exception
 			Toast toast = Toast.makeText(MainActivity.this, "webservice ClientProtocol"+e.toString(),Toast.LENGTH_LONG);
 			toast.show();
 		}catch (IOException e) {
 			// TODO: handle exception
 			Log.e("MobileHunt - webservice","ioException"+e.toString());
 			Toast toast = Toast.makeText(MainActivity.this, "webservice ioException"+e.toString(),Toast.LENGTH_LONG);
 			toast.show();
 		}
 
 		//convertimos respuesta a string
 		try{
 			BufferedReader bf=new BufferedReader
 					//(new InputStreamReader(is,"iso-8859-1"),8);
 					(new InputStreamReader(is,"utf-8"),8);
 
 			StringBuilder sb=new StringBuilder();
 			String line=null;
 			while((line=bf.readLine())!=null){
 				sb.append(line+"\n");
 			}
 			is.close();
 			responsePhp=sb.toString();
 		}catch (Exception e) {
 			// TODO: handle exception
 			Log.e("MobileHunt - responsePhp",e.toString());
 			Toast toast = Toast.makeText(MainActivity.this, "response"+e.toString(),Toast.LENGTH_LONG);
 			toast.show();
 		}
 		return responsePhp;
 	}
 
 
 	//Inyección de código JavaScript a wscontainandroid.html
 	public void contain (final String laty, final String lonx, final String responsePhp){
 		//si no hay Restricción lo identificaremos con -SR-
 		//String responsePhp=prefs.getString("responsePHP", "-SR-");
 		jsRest=responsePhp.substring(4, (responsePhp.length()-2));
 		if (!jsRest.contains("-SR-")&&jsRest!=""&&jsRest!=null){
 
 			//se puede eliminar codigo innecesario, la configuración del poligono es la misma siempre
 			//jsRest=jsRest.replaceAll("(\\r|\\n)"," ");
 			jsRest=jsRest.replaceAll("(<comas>)","\"");	
 			Log.v("MobileHunt - Restricción","Restricción de área var jsRest=\n"+jsRest+"\n---");
 			Log.v("MobileHunt - var wsContainPto","valor inicial variable "+ wsContainPto);
 			//wsContainPto="sin dato";
 			final WebView wViewContain=(WebView) findViewById(R.id.webViewMainJs);
 			final JavaScriptInterface jsi = new JavaScriptInterface(this);
 
 			wViewContain.addJavascriptInterface(jsi, "androidFunction");
 			wViewContain.getSettings().setJavaScriptEnabled(true);
 
 			//función que espera hasta que la pagina haya sido cargada para inyectar js
 			//marca out porque JS se ejcuta asincronamente, loadurl android webview syncronus
 			wViewContain.setWebViewClient(new WebViewClient(){
 				public void onPageFinished(WebView view, String url){
 					Log.v("MobileHunt - inyección","Pagina cargada, Inicio JS");
 					wViewContain.loadUrl("javascript: "+jsRest+";");
 					wViewContain.loadUrl("javascript: var poligono = new google.maps.Polygon(polyOptions);");
 					wViewContain.loadUrl("javascript: var punto=new google.maps.LatLng("+laty+","+lonx+");");
 					wViewContain.loadUrl("javascript: iniciar();");
 					//String msgToSend="Funcionaaa por favor";
 					//wViewContain.loadUrl("javascript: callFromActivity(\""+msgToSend+"\");");
 					Log.v("MobileHunt - inyección","Fin JS");
 				}
 			});			
 			wViewContain.loadUrl(wViewContainUrl);			
 		}
 		else Log.v("MobileHunt - Sin Restricción","Sin restricción de área var jsRest="+jsRest);
 		Log.v("MobileHunt - Contain","fin función contain");
 	}
 
 	public class JavaScriptInterface {
 		Context mContext;
 		Date fecha;
 		JavaScriptInterface(Context c) {
 			mContext = c;
 		}
 
 		public void puntoIn(){
 			fecha=new Date();
 			Log.v("MobileHunt - androidFunction","puntoIn "+fecha);
 			wsContainPto="in";    
 		}
 
 		public void puntoOut (){
 			fecha=new Date();
 			Log.v("MobileHunt - androidFunction","puntoOut "+fecha);
 			wsContainPto="out";
 			String usr=prefs.getString("usr", "sin dato");
 			String laty=prefs.getString("lastSendLaty", "sin dato");
 			String lonx=prefs.getString("lastSendLonx", "sin dato");
 			String comp=prefs.getString("comp", "sin dato");
 			String desc=prefs.getString("desc", "sin dato");
 			String mail=prefs.getString("mail", "raymundoc.vela@hotmail.com");
 			//String timeStamp=prefs.getString("lastSendTime", "sin dato");
 			Log.v("MobileHunt - SendMail",usr+" 1/"+laty+" 2/"+lonx+" 3/"+comp+" 4/"+desc+" 5/"+mail+" 6/"+bestProv);
 			String responsePhp=sendMail(usr, laty, lonx, comp, desc, mail, bestProv);
 			Log.v("MobileHunt - SendMail","responsePhp: "+responsePhp);
 			MediaPlayer mp=MediaPlayer.create(MainActivity.this, R.raw.alert);
 			mp.start();
 		}
 
 		public void sinRest (){
 			fecha=new Date();
 			Log.v("MobileHunt - androidFunction","SinRest o no se inyecto JS "+fecha);
 		}
 	}
 
 	// Inflate Menu from XML	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_principal, menu);
 		return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {  
 		switch (item.getItemId()) {  
 		case R.id.itemConf:  
 			// TODO Auto-generated method stub
 			Intent confInt = new Intent().setClass(MainActivity.this, ConfReadOnlyActivity.class);
 			Log.e("MobileHunt - conf", "mando intent");			
 			startActivity(confInt); 
 			break;
 		case R.id.itemAcer: 
 			Intent intAcer = new Intent().setClass(MainActivity.this, AcerActivity.class);
 			startActivity(intAcer);
 			break;
 		default: Log.e("MobileHunt - MENU","Default");
 		// put your code here
 
 		}  
 		return false;  
 	}
 
 	//Verifica conexión a internet
 	public boolean isOnline(){
 		ConnectivityManager cm=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo=cm.getActiveNetworkInfo();
 		if(networkInfo!=null && networkInfo.isConnected()) return true;
 		else return false;
 	}
 
 }//terminaActivity
