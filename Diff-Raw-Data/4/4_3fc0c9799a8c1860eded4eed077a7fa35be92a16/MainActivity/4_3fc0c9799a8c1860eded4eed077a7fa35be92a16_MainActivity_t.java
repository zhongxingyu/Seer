 package net.igconsultores.raymundo.pt2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
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
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
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
 	public String lastLonx;
 	public String lastLaty;
 	public String lastTime;
 //	public String lastSendLonx;
 //	public String lastSendLaty;
 	public String lastSendTime;
 	public String modoTraslado;
 	public String LocManProvider;
 	MediaPlayer mp;
 
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// TODO Put your code here
 		setContentView(R.layout.main);
 
 		final TextView tvConf =(TextView) findViewById(R.id.mainConf_textView2);
 //		final TextView tvPos =(TextView) findViewById(R.id.mainPos_textView3);
 		final TextView tvAcer =(TextView) findViewById(R.id.mainAcer_textView4);
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
 		mp=MediaPlayer.create(MainActivity.this, R.raw.alert);
 
 		//Listener Localizacion
 		locList=new LocationListener() {
 			@Override
 			public void onStatusChanged(String provider, int status, Bundle extras) {
 				// TODO Auto-generated method stub
 				Toast toast = Toast.makeText(MainActivity.this, "SatusChanged "+provider+"."+status,Toast.LENGTH_LONG);
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
 				Toast toast = Toast.makeText(MainActivity.this, "ProviderDisabled "+provider,Toast.LENGTH_LONG);
 				toast.show();
 			}
 
 			@Override
 			public void onLocationChanged(Location location) {
 				// TODO Auto-generated method stub
 				//mostrarLocalizacion(location);
 				Log.e("listener", "Cambio de localizacion: "+location);
 				if(location!=null){	
 					
 					DecimalFormat df=new DecimalFormat("0.00000");
 					String laty = String.valueOf(df.format(location.getLatitude()));
 					String lonx= String.valueOf(df.format(location.getLongitude()));
 					/*
 					String laty = String.valueOf(location.getLatitude());
 					String lonx= String.valueOf(location.getLongitude());
 					*/
 					String timeStamp=String.valueOf(location.getTime());
 					
 					
 					//imprimir datos en pantalla
 					tvLati.setText("Provedor:"+bestProv+"\nLatitud: " + laty);
 					tvLong.setText("Longitud: " + lonx);
 					tvPres.setText("Precision: " + String.valueOf(location.getAccuracy()));			
 					Date date=new Date(location.getTime());
 					tvTimes.setText("TimesStamp: "+ timeStamp+"\n"+"date: "+date.toString());
 
 					//verificar q no se hayan insertado
 					lastSendTime=prefs.getString("lastSendTime", "0");
 					Log.e("if","Si lasSenTime:"+lastSendTime+"=="+timeStamp+" timestamp");
 					if(!lastSendTime.equals(timeStamp)){						
 						String responsePhp;
 						int cont=0;
 						//Enviamos Datos al WS
 						//SharedPreferences prefs= getSharedPreferences(Constantes.prefsName, Context.MODE_WORLD_WRITEABLE);
 						
 						do{
 						String usr=prefs.getString("usr", "sin dato");							
 						bestProv= prefs.getString("bestProv", "sin dato");
 						Log.e("sendData",usr+"/"+ laty+"/"+lonx+"/"+ timeStamp+"/"+bestProv);
 						responsePhp=sendLoc(usr, laty, lonx, timeStamp, bestProv);
 						Log.e("responsePhp",responsePhp);
 						if(responsePhp.contains("_1")){
 							Log.d("ws"," loc insertada "+responsePhp);
 //							prefs.edit().putString("lastSendLonx",laty).commit();
 //							prefs.edit().putString("lastSendLaty",lonx).commit();
 							prefs.edit().putString("lastSendTime",timeStamp).commit();
 							cont=0;
 							}
 						else {
 							Log.d("responsePhp","Loc no insertada"+responsePhp);
 							cont++;
 						}
 						}while(!responsePhp.contains("_1")&&cont<5);
 					
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
 			Log.d("traslado:", "Obtenido "+modoTraslado);	
 		}
 		else {
 			rBtnAuto.setChecked(true);
 			modoTraslado="auto";
 			minTime=prefs.getInt(Constantes.keyMuestreo, 0);
 			minDistancia=minTime*500;
 //			prefs.edit().putString("modoTras",modoTraslado).commit();
 			Log.d("traslado:", "Obtenido "+modoTraslado);
 		}
 		
 		
 		//provedor default
 		if(bestProv.equals("GPS_PROVIDER")){
 			rBtnGps.setChecked(true);
 			
 		}
 		else {
 			rBtnNw.setChecked(true);
 		}
 		
 
 				
 		//Listener Proveedor de localizacion Global
 		rBtnGps.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnGps.isChecked()){
 					Log.d("locmana:", "eliminar update "+bestProv);
 					bestProv="GPS_PROVIDER";					
 //					LocManProvider=getLocManProvider(bestProv);
					Log.d("if", "Gps esta habilitado?"+locMana.isProviderEnabled(LocationManager.GPS_PROVIDER));
					if(!locMana.isProviderEnabled(LocationManager.GPS_PROVIDER)){
 						habGps();
 						Log.d("location", "GPS habilitado");
 						}	
 					
 					prefs.edit().putString("bestProv",bestProv).commit();
 					Log.d("Mejor Proveedor:", "Guardado "+bestProv);
 								
 					//eliminar actualizacion anterior al LocList
 					locMana.removeUpdates(locList);
 					Log.d("locmana:", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min\n Ditancia minima: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 					locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 					Log.d("Location:", "nuevo update "+bestProv);
 				}
 				else Log.d("OnchekedGps:", "Gps Radio no seleccionado "+bestProv);
 			}
 		});
 		
 		rBtnNw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				if (rBtnNw.isChecked()){
 					Log.d("locmana", "eliminar update "+bestProv);
 					bestProv="NETWORK_PROVIDER";
 					prefs.edit().putString("bestProv",bestProv).commit();
 					Log.d("Mejor Proveedor", "Guardado "+bestProv);
 					
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min\n Ditancia minima: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 					
 					locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 					Log.d("Location:", "nuevo update "+bestProv);
 				}
 				else Log.d("OnchekedNW:", "Nw Radio no seleccionado "+bestProv);
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
 					Log.d("modoTras Pie:", "Guardado "+modoTraslado);
 					
 					
 					
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 					
 					if (bestProv.equals("GPS_PROVIDER")){
 						locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 						Log.d("Location:", "Primer update Loc "+ bestProv);
 						}
 					else {
 						locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 						Log.d("Location:", "Primer update Loc "+ bestProv);
 					}
 					
 					
 					
 				}
 //				else Log.d("modoTrasPie:", "NO Guardado "+modoTraslado);
 				
 				
 				
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
 					Log.d("modoTrasAuto:", "Guardado "+modoTraslado);
 					
 					
 					
 					//eliminar actualizacion anterior
 					locMana.removeUpdates(locList);
 					Log.d("locmana", "eliminada update");
 					//crear una nueva
 					minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 					Log.e("Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 					//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 					
 					if (bestProv.equals("GPS_PROVIDER")){
 						locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 						Log.d("Location:", "Primer update Loc "+ bestProv);
 						}
 					else {
 						locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 						Log.d("Location:", "Primer update Loc "+ bestProv);
 					}
 					
 				}
 //				else Log.d("modoTrasAuto:", "NO Guardado "+modoTraslado);
 			}
 		});
 		
 
 		/*
 		Criteria req=new Criteria();
 		//req.setAccuracy(Criteria.ACCURACY_COARSE);
 		//req.setPowerRequirement(Criteria.POWER_MEDIUM);
 		//String bestProv=locMana.getBestProvider(req, false);//el false es si queremos que nos devuelvan los provedores activados
 		 */
 
 		//obtener ultima loc del proveedor
 		Log.e("Location", "antes del if ultima localizacion obtenida");
 		locationMobile= locMana.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(locationMobile!=null){
 			//obtenemos localización
 			Log.e("Location", "ultima localizacion obtenida");
 			lastLaty=String.valueOf(locationMobile.getLatitude());
 			lastLonx=String.valueOf(locationMobile.getLongitude());
 			lastTime=String.valueOf(locationMobile.getTime());
 			
 			//Imprimimos en pantalla
 			tvLati.setText("Provedor:"+bestProv+"\nlastLatitud: " + lastLaty );
 			tvLong.setText("lastLongitud: " + lastLonx);
 			tvPres.setText("Precision: " + String.valueOf(locationMobile.getAccuracy()));			
 			Date date=new Date(locationMobile.getTime());
 			tvTimes.setText("TimesStamp: "+ lastTime+"\n"+ "date.tostring:"+date.toString());
 			Log.e("if", "Obtuve ultima posición");
 //				lastSendLonx=prefs.getString("lastSendLonx", "0");
 //				lastSendLaty=prefs.getString("lastSendLaty", "0");
 			lastSendTime=prefs.getString("lastSendTime", "0");
 			
 			Log.e("lastSendTime",prefs.getString("lastSendTime", "0"));
 //				Log.e("lastSendLaty",prefs.getString("lastSendLaty", "0"));
 //Log.e("lastSendLonx",prefs.getString("lastSendLonx", "0"));
 
 			//Comprobamos si la ultima localización conocida ya fue guardada
 			
 			if(!lastTime.equals(lastSendTime)){
 				String responsePhp;
 				int cont=0;
 				//Enviamos Datos al WS
 				//si no se guarda localización se intenta mandar  5 veces
 				do{
 					
 					String usr=prefs.getString("usr", "sin dato");
 					String timeStamp=String.valueOf(locationMobile.getTime());
 					Log.e("sendData",usr+"/"+ lastLaty+"/"+lastLonx+"/"+ timeStamp+"/"+bestProv);
 					responsePhp=sendLoc(usr,lastLaty,lastLonx,timeStamp, bestProv);
 					Log.e("responsePhp",responsePhp);
 					if(responsePhp.contains("_1")){
 						Log.d("ws"," loc insertada"+responsePhp);
 						
 						//En desarrollo
 						//if(responsePhp.contains("out")&!mp.isPlaying())
 						//	mp.start(); 
 						
 						prefs.edit().putString("lastSendLonx",lastLonx).commit();
 						prefs.edit().putString("lastSendLaty",lastLaty).commit();
 						cont=0;
 					}
 					else{ 
 //Establecer timer para volver a intentar ingresar datos
 						cont++;
 						Log.d("responsePhp","Loc no insertada"+responsePhp);
 					}
 				}while(!responsePhp.contains("_1")&&cont<5);
 			}
 		}
 		else Log.e("if LocMana", "NO Obtuve posición, locmana vacio");
 	//pasar milisegundos a minutos
 			minTime=prefs.getInt(Constantes.keyMuestreo, 0)*(1000*60);
 			Log.e("Location", "tiempo muestreo cada:"+minTime+" "+Constantes.keyMuestreo+"min , distancia min: "+minDistancia);
 			//si es caminando distancia minima a recoger es 66m, 60 min 4km/hr ,  si es en carro cada 500m  20 o 30 km/hr datos Hresendiz 
 			
 			if (bestProv.equals("GPS_PROVIDER")){
 				locMana.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistancia, locList);
 				Log.d("Location:", "Primer update Loc "+ bestProv);
 				}
 			else {
 				locMana.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistancia, locList);
 				Log.d("Location:", "Primer update Loc "+ bestProv);
 			}
 			
 
 		//Menu
 		tvConf.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent confInt = new Intent().setClass(MainActivity.this, ConfReadOnlyActivity.class);
 				Log.e("conf", "mando intent");			
 				startActivity(confInt);
 			}
 		});
 
 		tvAcer.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intAcer = new Intent().setClass(MainActivity.this, AcerActivity.class);
 				startActivity(intAcer);
 			}
 		});
 		
 
 		
 
 	}//Termina OnCreate
 
 	
 	//listener cuando el boton BACK sea presionado
 	
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
 	   Log.d("Back", "onBackPressed Called");
 	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Salir de la aplicación?\n**Puedes presionar el botón \"HOME\" para cambiar de aplicación")
 		.setCancelable(false)
 		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				finish();
 				//Toast.makeText(this, "Presiona la tecla de atrás para volver a la aplicación MobileHunt", Toast.LENGTH_LONG).show();
 			}
 		})
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
 		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				Intent intGpsSettings = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
 				startActivity(intGpsSettings); 
 				//Toast.makeText(this, "Presiona la tecla de atrás para volver a la aplicación MobileHunt", Toast.LENGTH_LONG).show();
 			}
 		})
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
 Log.i("", String.valueOf(loc.getLatitude() + " - " + String.valueOf(loc.getLongitude())));
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
 		InputStream is=null;
 		String responsePhp="";
 		try{
 			//Datos a enviar			
 			List<NameValuePair> nvp= new ArrayList<NameValuePair>();
 			nvp.add(new BasicNameValuePair("usr", usr));
 			nvp.add(new BasicNameValuePair("laty", laty));
 			nvp.add(new BasicNameValuePair("lonx", lonx));
 			nvp.add(new BasicNameValuePair("timestamp", timeStamp));
 			nvp.add(new BasicNameValuePair("bestprov", bestProv));
 			
 			httpPost.setEntity(new UrlEncodedFormEntity(nvp));
 			
 			//Si responde, ejecutamos
 			HttpResponse httpResponse=httpClient.execute(httpPost);
 			//obtenemos respuesta
 			HttpEntity httpEntity=httpResponse.getEntity();
 			is=httpEntity.getContent();
 						
 		}catch (ClientProtocolException e) {
 Log.e("webservice","ClientProtocol"+e.toString());			// TODO: handle exception
 Toast toast = Toast.makeText(MainActivity.this, "webservice ClientProtocol"+e.toString(),Toast.LENGTH_LONG);
 toast.show();
 		}catch (IOException e) {
 			// TODO: handle exception
 Log.e("webservice","ioException"+e.toString());
 Toast toast = Toast.makeText(MainActivity.this, "webservice ioException"+e.toString(),Toast.LENGTH_LONG);
 toast.show();
 		}
 		
 		//convertimos respuesta a string
 		try{
 			BufferedReader bf=new BufferedReader
 					(new InputStreamReader(is,"iso-8859-1"),8);
 			StringBuilder sb=new StringBuilder();
 			String line=null;
 			while((line=bf.readLine())!=null){
 				sb.append(line+"\n");
 			}
 			is.close();
 			responsePhp=sb.toString();
 		}catch (Exception e) {
 			// TODO: handle exception
 Log.e("responsePhp",e.toString());
 Toast toast = Toast.makeText(MainActivity.this, "response"+e.toString(),Toast.LENGTH_LONG);
 toast.show();
 		}
 		return responsePhp;
 	}
 
 
 
 }//termina
