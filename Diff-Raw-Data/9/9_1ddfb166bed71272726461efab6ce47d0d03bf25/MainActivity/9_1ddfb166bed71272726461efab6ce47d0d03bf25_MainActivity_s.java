 /*
 * Copyright (C) 2013 Juan Pons (see README for details)
 * This file is part of miAulaVirtual.
 *
 * miAulaVirtual is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * miAulaVirtual is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with miAulaVirtual. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */
 
 package com.jp.miaulavirtual;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.Map;
 
 import org.jsoup.Jsoup;
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.content.pm.ActivityInfo;
 
 
 public class MainActivity extends Activity {
 	
     // flag for Internet connection status
     Boolean isInternetPresent = false;
     // Connection detector class
     ConnectionDetector cd;
 	
 	// Service
 	
 	// User data
 	private String user;
 	private String pass;
 	private String url;
 	private String panel;
 	
 	private String tag = "Lifecycle";
 	
 	//Respuesta
 	private Response res;
 	private Context mycontext;
 	
 	private Map<String, String> cookies;
 	
 	public void process() {
 		finish();
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 	}
 	
 	public void process(int id) {
 		switch(id) {
 		case 0: 
     		setContentView(R.layout.activity_main); 
     		Toast.makeText(getBaseContext(),getString(R.string.bad_data), Toast.LENGTH_SHORT).show();
             break;
 		case 1:
 			Toast.makeText(getBaseContext(),getString(R.string.toast_6), Toast.LENGTH_LONG).show();
 			setContentView(R.layout.activity_main);
 			break;
 		default:
 			Toast.makeText(getBaseContext(),getString(R.string.toast_5), Toast.LENGTH_LONG).show();
 			setContentView(R.layout.activity_main);
 			break;
 		}
     	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 	}
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         mycontext = this;
         // creating connection detector class instance
         cd = new ConnectionDetector(getApplicationContext());
         isInternetPresent = cd.isConnectingToInternet();
         
         // SharedPreference instance
         PreferenceManager.setDefaultValues(this, R.xml.apppreferences, false);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         // Just in case...we delete the cookie, again.
         Editor editor = prefs.edit();
     	editor.remove("cookies");
     	editor.commit();
     	
         // panel
     	panel = prefs.getString("panel", "2");
         
     	// Getting user data
         user = prefs.getString("myuser", "Ninguno");
         pass = prefs.getString("mypass", "Ninguno");
         url = "/dotlrn/?page_num="+panel;
        
         if(!user.equals("Ninguno")){ //Si el usuario entra a la aplicación y ya está logueado
         	
 	        /* We don't want change screen orientation while loading petition */
 	        //---get the current display info---
 	        WindowManager wm = getWindowManager();
 	        Display d = wm.getDefaultDisplay();
 	        if (d.getWidth() > d.getHeight()) {
 	        	//---change to landscape mode---
 		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
 	        } else {
 	        	//---change to portrait mode---
 		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
 	        }
         	
         	setContentView(R.layout.activity_main2); //Content si está logueado - Loader mientras espera el scrap
         	
         	Log.d("DATOS", user);
             Log.d("DATOS", pass);
             
             if(isInternetPresent) {
             	new urlConnect().execute();
             } else {
             	setContentView(R.layout.activity_main);
             	Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
             }
         } else {
         setContentView(R.layout.activity_main); //Content si no está logueado
         }
         Log.d(tag, "In the onCreate() event");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /**
      * Acci�n para cada Item del Menu
      */
     public boolean onOptionsItemSelected(MenuItem item){
         /*El switch se encargar� de gestionar cada elemento del men� dependiendo de su id,
         por eso dijimos antes que ning�n id de los elementos del men� podia ser igual.
         */
         switch(item.getItemId()){
         case R.id.preferencias: //Nombre del id del men�, para combrobar que se ha pulsado
         	startActivity(new Intent(this, SettingsActivity.class));;
         	break;
         case R.id.acercade: //Nombre del id del men�, para combrobar que se ha pulsado
         	startActivity(new Intent(this, AboutActivity.class));
         	break;
         }
         return true;
     }
     
     
     public void onStart()
     {
         super.onStart();
         Log.d(tag, "In the onStart() event");
     }
     
     public void onRestart()
     {
         super.onRestart();
         isInternetPresent = cd.isConnectingToInternet();
         Log.d(tag, "In the onRestart() event"); 
     }
     
     public void onResume()
     {
         super.onResume();
         Log.d(tag, "In the onResume() event");
     }
     
     public void onPause()
     {
         super.onPause();
         Log.d(tag, "In the onPause() event");
     }
     
     public void onStop()
     {
         super.onStop();
         Log.d(tag, "In the onStop() event");  
     }
     
     public void onDestroy()
     {
         super.onDestroy();
         Log.d(tag, "In the onDestroy() event");
     }
     
     /** Llamada cuando el usuario hace clic en 'Enviar' */
     @SuppressWarnings("deprecation")
 	public void sendMessage(View view) {
     	/**
          * Habr� que guardar el Usuario y Contrase�a si es la primera vez que loguea (es la primera vez que loguea porque se envia el SEND.
          * 
          */
     	isInternetPresent = cd.isConnectingToInternet();
     	if(isInternetPresent) {
 	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	        Editor editor = prefs.edit();
 	        EditText tuser = (EditText) findViewById(R.id.user);
 	    	EditText tpass = (EditText) findViewById(R.id.pass);
 	    	//Incertamos el usuario y la contrase�a en las preferencias
 	        editor.putString("myuser", tuser.getText().toString());
 	        editor.putString("mypass", tpass.getText().toString());
 	        editor.commit();
 	        
 	        /* We don't want change screen orientation while loading petition */
 	        //---get the current display info---
 	        WindowManager wm = getWindowManager();
 	        Display d = wm.getDefaultDisplay();
 	        if (d.getWidth() > d.getHeight()) {
 	        	//---change to landscape mode---
 		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
 	        } else {
 	        	//---change to portrait mode---
 		        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
 	        }
 	    	
 	        //El usuario ha logueado, ponemos la pantalla de carga
 	        setContentView(R.layout.activity_main2);
 	        
 	        // panel
 	    	panel = prefs.getString("panel", "2");
 	        
 	        //Actualizamos el usuario y contrase�a
 	        user = tuser.getText().toString();
 	        pass = tpass.getText().toString();
 	        url = "/dotlrn/?page_num="+panel;
 	        
 	        Log.d("DATOS2", user);
 	        Log.d("DATOS2", pass);
 	        
 	        // Iniciamos el servicio de Scrap
 	        new urlConnect().execute();
     	} else {
     		Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
     	}
     }
 
     /* TASKS OR SERVICES */
     
     private class urlConnect extends AsyncTask<Void, Void, Response> {
     	
     	int a;
     	protected Response doInBackground(Void... params) {
         	//Mirar si hay datos en cache, si los hay, cogerlos y hacer el get()
         		try {
             		setData();
             	}catch(SocketTimeoutException e)
             	{
             		res = null;
             	}catch(IOException e)
             	{
             		res = null;
             		a = 1;
             	}
             return res;
         }
         protected void onPostExecute(Response response) {
             if(res != null) {
 	        	if(res.hasCookie("ad_user_login")) { // El usuario y la contrase�a son correctas
 		             startOk(response, mycontext);
 		             process();
 	            } else if(res.hasCookie("ad_session_id")) { // Usuario y contrase�a incorrectos. No tiene ni "ad_user_login" ni "fs_block_id"
 	            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
 	            	Editor editor = prefs.edit();
 		            editor.remove("cookies");
 		            editor.commit();
 			        process(0);
 	        	} else {
 	        		process(6);
 	        	}
 	        } else if(a==1) {
             	startOk2(mycontext, false);
             	process();
 	        } else {
 	            process(1);
 	        }
         }
     }
     private void startOk(Response response, Context context) {
     	Elements count = null;
     	String out = "";
     	Intent i = new Intent(this, TabsActivity.class);
     	  try {
 			count = response.parse().body().select("td[headers=contents_name] a[href], td[headers=folders_name] a[href]");;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	  if(count.size()>0) {
     		  try {
 				out = response.parse().body().toString();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     		  i.putExtra("out", out);
     	  }
     	  i.putExtra("user", user);
     	  i.putExtra("pass", pass);
     	  i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     	  startActivity(i);
     	}
     
     private void startOk2(Context context, Boolean status) {
     	Intent i = new Intent(this, TabsActivity.class);
     	i.putExtra("user", user);
   	  	i.putExtra("pass", pass);
   	  	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   	  	startActivity(i);
     	}
     
     // CONEXIÓN
     
     public void setData() throws IOException, SocketTimeoutException {
     	
    	Document av = Jsoup.connect("http://aulavirtual.uv.es").timeout(10*1000).get();
 	    	
 		Elements inputs = av.select("input[name=__confirmed_p], input[name=__refreshing_p], input[name=form:id], input[name=form:mode], input[name=formbutton:ok], input[name=hash], input[name=time], input[name=token_id]");
 			
 		Response resp = Jsoup.connect("http://aulavirtual.uv.es/register/")
 			    .data("__confirmed_p", inputs.get(2).attr("value"), "__refreshing_p", inputs.get(3).attr("value"), "form:id", inputs.get(1).attr("value"), "form:mode", inputs.get(0).attr("value"), "formbutton:ok", inputs.get(7).attr("value"), "hash", inputs.get(6).attr("value"), "time", inputs.get(4).attr("value"), "return_url", url, "token_id", inputs.get(5).attr("value"), "username", user, "password", pass)
 			    .method(Method.POST)
 			    .timeout(10*1000)
 			    .execute();
 		res = resp; //IMPORTANTE verificará si el usuario ha logueado o por el contrario ha dado una contraseña o usuario incorrecto(s)
 		cookies = resp.cookies();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
         Editor editor = prefs.edit();
         editor.putString("cookies", cookies.toString());
         editor.commit();
         Log.d("Connect", "Conectando sin cookies");
     } 
 }
