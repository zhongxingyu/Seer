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
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.jsoup.Jsoup;
 import org.jsoup.Connection.Method;
 import org.jsoup.Connection.Response;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.webkit.MimeTypeMap;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class DisplayMessageActivity extends Activity {
 
     // flag for Internet connection status
     private Boolean isInternetPresent = false;
     // Connection detector class
     ConnectionDetector cd;
 	
 	//Variables POST
     private String url;
 	public final static String RESPONSE = "com.jp.miaulavirtual.RESPONSE";
 	private Boolean task_status = true;
 	
 	//Respuesta
 	private Response res;
     
 	private int a = 0; //Control nº de tareas 1 = GET || POST 2 = GET cookie vencida, POST, GET.
 	private String scookie;
 	private Map<String, String> cookies; //Obtener la cookie del cache
 	
 	// Activity
 	private Context mycontext;
 	
 	// User data
 	private String user;
 	private String pass;
 	private String panel; // control de panel number of Documents section
 	
 	// Valores del HOME de documentos
 	String[] names;
 	String[] urls;
 	String[] types;
 	private ArrayList<String[]> onData = new ArrayList<String[]>(); // we need to know where the user is and where he has been to provide navigation back
 	
 	//Nombres URL's y Types por las que se pasa y Boolean necesarios
 	private Boolean isTheHome; 
 	private Boolean comunidades = false; // URL principal documentos y comunidades = true -> Carpeta Comunidades url /clubs/; Sino carpeta Principal, url-> /classes/ y otras
 	private int clickedPosition = 0; // Necesitamos saber la posición del archivo clickeado para poder mandar la URL desde fuera del Listener
 	
 	// Interface
 	private ListView lstDocs;
 	private ListAdapter lstAdapter;
 	private ProgressDialog dialog;
 	private TextView headerTitle;
 	
     public void process(Boolean status, int id) {
     	if(status) {
 			Toast.makeText(getBaseContext(),getString(R.string.toast_1), Toast.LENGTH_SHORT).show();
 		} else {
 			String msg = null;
 		    switch(id) {
 		    case 0: // cancelled
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_0);
 		    	break;
 		    case 2: // not enough free storage space
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_2);
 		    	break;
 		    case 3: // invalid URL
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_3);
 		    	break;
 		    case 4: // file not found
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_4);
 		    	break;
 		    case 5: // general error
 		    	msg = getString(R.string.toast_5);
 		    	break;
 		    case 6: // conection problems
 		    	msg = getString(R.string.toast_6);
 		    	afterBroadcaster2();
 		    	break;
 		    case 7: // conection problems 2
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_6);
 		    	afterBroadcaster2();
 		    	break;
 		    case 8: // general error 2
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_5);
 		    	afterBroadcaster2();
 		    	break;
 		    case 9: // conection problems 2
 		    	dialog.dismiss();
 		    	msg = getString(R.string.toast_7);
 		    	afterBroadcaster2();
 		    	break;
 		    } 
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
 		}  
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 		task_status = true;
     }
 
 	public void process(String out) {
 		String response = out;
 		if(isTheHome) {
     		if(clickedPosition == 0) comunidades = true; //Click Comunidades y otros
     		String[] theData = new String[2];
     		theData[0] = urls[clickedPosition].toString(); // url
     		theData[1] = names[clickedPosition].toString(); // name
     		onData.add(theData);
     	} else { 
     		if(clickedPosition==0){ //Click atrás
     			onData.remove(onData.size() - 1); // remove data
     			comunidades = false;
     			if(onData.size()>=2 && (onData.get(0)[0].toString().equals(onData.get(1)[0].toString()))) comunidades = true;
     		} else {
     			String[] theData = new String[2];
     			theData[0] = urls[clickedPosition].toString(); // url
         		theData[1] = names[clickedPosition].toString(); // name
         		onData.add(theData);
     		}
     	}
 		afterBroadcaster(response);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
 		task_status = true;
 	}
 	
 	/**
 	 * We need to save the Response of the connection for recreate it when activity change orientation
 	 */
 	
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 	 
 		//---save whatever you want here; it takes in an Object type---
 		// we pass the Arraylist to String[] so we can put it, after, inside the 'data' and then can pass the ALL the data needed with one Object
 		ArrayList<Object[]> passData = new ArrayList<Object[]>();
 		// Save onData
 		String [][] mydata = new String[onData.size()][2];
 		mydata = onData.toArray(mydata);
 		
 		// Save the cookie
 		String[] cookie_values = cookies.values().toArray(new String[0]);
 		String[] cookie_keys = cookies.keySet().toArray(new String[0]);
 
 		// add to oBJECT
 		passData.add(names);
 		passData.add(urls);
 		passData.add(types);
 		passData.add(mydata);
 		passData.add(cookie_keys);
 		passData.add(cookie_values);
 		
 		return(passData);
 	} 
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_message);
 		Log.d("Lifecycle8", "In onCreate()");
 		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
         
         // creating connection detector class instance
         cd = new ConnectionDetector(getApplicationContext());
         
         // general context
         mycontext = this;
         
         // Preferences
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
         // panel
     	panel = prefs.getString("panel", "2");
         
         // Get the onRetainNonConfigurationInstance()
         final ArrayList<Object[]> passData = (ArrayList<Object[]>) getLastNonConfigurationInstance();
         
         try{
 	        if(passData != null) { 
 	            // onUrl and onName to ArrayList
 	            onData = new ArrayList<String[]>(Arrays.asList((String[][]) passData.get(3))); // hierarchical urls
 	            passData.remove(3);
 	            
 	            // cookies to Map
 	            cookies = new HashMap<String, String>();
 	            for (int i = 0; i < passData.get(3).length; i++) {
 			          cookies.put(passData.get(3)[i].toString(), passData.get(4)[i].toString());
 			    }
 	            scookie = cookies.toString(); // ¡IMPORTANT!
 	            passData.remove(3);
 	            passData.remove(3);
 	            
 	        	// names
 	            int length = passData.get(0).length;
 	            names =  new String[length];
 	            System.arraycopy(passData.get(0), 0, names, 0, length);
 	            passData.remove(0);
 	            
 	            // urls
 	            length = passData.get(0).length;
 	            urls =  new String[length];
 	            System.arraycopy(passData.get(0), 0, urls, 0, length);
 	            passData.remove(0);
 	            
 	            // types
 	            length = passData.get(0).length;
 	            types =  new String[length];
 	            System.arraycopy(passData.get(0), 0, types, 0, length);
 	            passData.clear();
 	            
 	            // Update de subtitle
 	            headerTitle = (TextView) findViewById(R.id.LblSubTitulo); // Título Header
 	            headerTitle.setTextColor(getResources().getColor(R.color.list_title));
 	            headerTitle.setTypeface(null, 1);
 	            headerTitle.setText(onData.get(onData.size()-1)[1]);
 	            
 	            // retrieve the same View before change orientation
 	            lstDocs = (ListView)findViewById(R.id.LstDocs); // Declaramos la lista
 	            lstAdapter = new ListAdapter(this, names, types);
 	            lstDocs.setAdapter(lstAdapter); // Declaramos nuestra propia clase adaptador como adaptador
 	
 	        } else {
 	            // cookies
 	            scookie = prefs.getString("cookies", ""); //�Existe cookie?
 	    	    if(scookie!="") {
 	    	    	cookies = toMap(scookie); //Si existe cookie la pasamos a MAP
 	    	    }
 	        
 	    	    String [] theData = new String[2];
 	    	    theData[0] = "/dotlrn/?page_num="+panel;
 	    	    theData[1] = "Documentos";
 		        onData.add(theData);
 		        
 		        // Actualizamos nombre de LblSubTitulo
 		        headerTitle = (TextView) findViewById(R.id.LblSubTitulo); // Título Header
 		        headerTitle.setTextColor(getResources().getColor(R.color.list_title));
 		        headerTitle.setTypeface(null, 1);
 		        headerTitle.setText(theData[1]);
 		        
 		        //Recibimos primera llamada al crear la Actividad
 		        Intent intent = getIntent();
 		        
 			    // Datos de usuario
 			    user = intent.getStringExtra("user");
 			    pass = intent.getStringExtra("pass");
 			    
 			    // Recibimos datos HOME
 		        String response = intent.getStringExtra("out");
 				Document doc = Jsoup.parse(response);
 				Elements elements = null;
 		        try {
 		        	elements = scrap2(doc);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 		        asigsToArray(elements, true, comunidades);
 		        urlsToArray(elements, true, comunidades);
 		        typeToArray(elements, true, comunidades, urls.length); // Añadimos el Array con los TYPES al ArrayList - [2]
 		        
 		        lstDocs = (ListView)findViewById(R.id.LstDocs); // Declaramos la lista
 		        lstAdapter = new ListAdapter(this, names, types);
 		        lstDocs.setAdapter(lstAdapter); // Declaramos nuestra propia clase adaptador como adaptador
 	        }    
 	        lstDocs.setOnItemClickListener(new OnItemClickListener() {
 	            @Override
 	            public void onItemClick(AdapterView<?> a, View v, int position, long id) { //Al clicar X item de la lista
 	            	isInternetPresent = cd.isConnectingToInternet();
 	            	if(isInternetPresent) {
 		            	if(!(types[position].toString().equals("0")) && !(types[position].toString().equals("1")) && !(types[position].toString().equals("6"))) {
 		            		clickedPosition = position;
 		            		Log.d("TIPO", "DOCUMENTO");
 		            		// ProgressDialog (salta para mostrar el proceso del archivo descargándose)
 		            		dialog = new ProgressDialog(mycontext);
 		                    
 		            		// Servicio para la descarga del archivo
 		            		url = urls[clickedPosition].toString();
 		            		String url_back = onData.get(onData.size() - 2)[0];
 		            		new docDownload(url_back).execute();
 	
 		            	} else {
 			            	if((onData.get(onData.size() - 1)[0].equalsIgnoreCase(("/dotlrn/?page_num="+panel))) && !comunidades){
 			                	isTheHome = true;
 			                } else {
 			                	isTheHome = false;
 			                }
 			            	//Cuando se hace click en una opción de la lista, queremos borrar todo mientras carga, incluído el título header
 			            	headerTitle.setText(null);
 			            	//Copia de FIRST que puede ser borrada
 			            	lstAdapter.clearData();
 			            	// Refrescamos View
 			            	lstAdapter.notifyDataSetChanged();
 			            	lstDocs.setDividerHeight(0);
 			            	clickedPosition = position;
 			            	Log.d("URL", onData.get(onData.size() - 1)[0]);
 			            	
 			            	url = urls[clickedPosition].toString();
 			            	Log.d("URL2", urls[clickedPosition].toString());
 			            	new urlConnect().execute();
 		            	}
 		            } else {
 		            	Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
 		            }
 	            }
 	        }); 
         } catch(ArrayIndexOutOfBoundsException e) {
 	        TextView tv = (TextView) findViewById(R.id.panel_error);
 	        tv.setVisibility(0);
 	        setRestrictedOrientation();
 	        Toast.makeText(getBaseContext(),getString(R.string.panel_error2), Toast.LENGTH_LONG).show();
         } catch(IndexOutOfBoundsException e) {
 	        TextView tv = (TextView) findViewById(R.id.panel_error);
 	        tv.setVisibility(0);
 	        setRestrictedOrientation();
 	        Toast.makeText(getBaseContext(),getString(R.string.panel_error2), Toast.LENGTH_LONG).show();
 		} catch(IllegalArgumentException e) {
 	        TextView tv = (TextView) findViewById(R.id.panel_error);
 	        tv.setVisibility(0);
 	        setRestrictedOrientation();
 	        Toast.makeText(getBaseContext(),getString(R.string.panel_error2), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public void afterBroadcaster(String mydoc) { //Método proceso GET
 		Boolean isHome;
 		
 		// Actualizamos nombre de LblSubTitulo;
 		TextView headerTitle = (TextView) findViewById(R.id.LblSubTitulo); // Título Header
 		headerTitle.setTextColor(getResources().getColor(R.color.list_title));
         headerTitle.setTypeface(null, 1);
         headerTitle.setText(onData.get(onData.size() - 1)[1]);
 		
 		Document doc = Jsoup.parse(mydoc);
 		Elements elements = null;
         if((onData.get(onData.size() - 1)[0].equalsIgnoreCase(("/dotlrn/?page_num="+panel))) && !comunidades){
         	isHome = true;
         } else {
         	isHome = false;
 
         }
 		try {
         	if(isHome) {
         		elements = scrap2(doc);
         	} else {
         		elements = scrap(doc);
         	}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log.d("Doc", onData.get(onData.size() - 1)[0]);
 		Log.d("Doc", "/dotlrn/?page_num="+panel);
 		Log.d("Doc", String.valueOf(isHome));
 
 		asigsToArray(elements, isHome, comunidades); //Array con los  nombres de Carpetas, Asignaturas y Archivos
 		urlsToArray(elements, isHome, comunidades);
 		typeToArray(elements, isHome, comunidades, urls.length);
         
         // Re-iniciamos adaptador de lista
         lstAdapter = new ListAdapter(this, names, types);
         lstDocs.setDividerHeight(1);
         lstDocs.setAdapter(lstAdapter);
 	}
 	
 	public void afterBroadcaster2() {
 		lstDocs = (ListView)findViewById(R.id.LstDocs); // Declaramos la lista
 		lstAdapter = new ListAdapter(this, names, types);
         lstDocs.setAdapter(lstAdapter); // Declaramos nuestra propia clase adaptador como adaptador
 	}
 	
     public void onStart()
     {
         super.onStart();
         Log.d("Lifecycle8", "In onStart()");
         
     }
     
     public void onRestart()
     {
         super.onRestart();
         // we must recheck the internet connection
         isInternetPresent = cd.isConnectingToInternet();
         String p2 = panel;
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
         // panel; We reload the app if panel change
     	panel = prefs.getString("panel", "2");
     	if(!p2.equalsIgnoreCase(panel)) {
     		Intent i = new Intent(this, MainActivity.class);
     		startActivity(i); 
     		finish();
     	}
     	Log.d("Lifecycle8", "In onRestart()");
     }
     
     public void onResume()
     {
         super.onResume();
         Log.d("Lifecycle8", "In onResume()");
 
     }
     
     public void onPause()
     {
         super.onPause();
         Log.d("Lifecycle8", "In onPause()");
     }
     
     public void onStop()
     {
         super.onStop();
         Log.d("Lifecycle8", "In onStop()");
     }
     
     public void onDestroy()
     {
         super.onDestroy();
         
         //ELIMINAR COOKIE DEL CACHE para que siempre que se inicie la aplicacion haga POST
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Editor editor = prefs.edit(); //eliminamos la cookie
     	editor.remove("cookies"); //ELIMINAMOS LA COOKIE DE LA PASADA VEZ
     	editor.commit();
     	
     	Log.d("Lifecycle8", "In onDestroy()");
     }
 
 	
     /**
      * Acción para cada Item del Menu
      */
     public boolean onOptionsItemSelected(MenuItem item){
         /*El switch se encargar� de gestionar cada elemento del men� dependiendo de su id,
         por eso dijimos antes que ning�n id de los elementos del men� podia ser igual.
         */
         switch(item.getItemId()){
         case R.id.preferencias: //Nombre del id del men�, para combrobar que se ha pulsado
         	startActivity(new Intent(this, SettingsActivity.class));
         	break;
         case R.id.acercade: //Nombre del id del men�, para combrobar que se ha pulsado
         	startActivity(new Intent(this, AboutActivity.class));
         	break;
         case R.id.mysesion:
         	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             Editor editor = prefs.edit();
             editor.remove("myuser");
             editor.remove("mypass");
             editor.commit();
             Intent intent = new Intent(this, MainActivity.class);
             startActivity(intent);
             finish();
             break;
         case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
         }
         return true;
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_display_message, menu);
         return true;
 	}
 
     public Elements scrap(Document mdoc) throws IOException {
 		Elements elem = mdoc.select("tbody tr[class=odd], tbody tr[class=even], tbody tr[class=even last], tbody tr[class=odd last]"); //Filas de Documentos
 	    Log.d("Service", "Passing elements variable");
 		return elem;
 	}
     
     public Elements scrap2(Document mdoc) throws IOException {
 		Elements elem = mdoc.select("table[summary] tbody tr[class=odd], table[summary] tbody tr[class=even], table[summary] tbody tr[class=even last], table[summary] tbody tr[class=odd last]"); //Filas de Documentos
 	    Log.d("Service", "Passing elements variable2");
 		return elem;
 	}
 	
 	public void asigsToArray(Elements melem, Boolean isHome, Boolean comun) throws IndexOutOfBoundsException {
 		int i = 1;
 		Elements elem;
 		if(isHome) {
 			elem = melem.select("td[headers=contents_name] a, td[headers=folders_name] a").not("[href*=/clubs/]"); //Nombre Asignaturas String !"Comunuidades"
 			names = new String[(elem.size())+1]; //todo-comunidades + 1(carpeta comunidades)
 			names[0] = "Comunidades y otros";
 			for(Element el : elem){
 			    names[i] = el.text();
 			    i++;
 			}
 		} else if(comun) {
 			elem = melem.select("td[headers=contents_name] a[href*=/clubs/], td[headers=folders_name] a[href*=/clubs/]"); //Nombre Asignaturas String "Comunuidades"
 			names = new String[elem.size()+1]; //comunidades + 1(atrás)
 			names[0] = "Atrás " + onData.get(onData.size() - 2)[1];
 			for(Element el : elem){
 			    names[i] = el.text();
 			    i++;
 			}
 		} else {
 			elem = melem.select("td[headers=contents_name] a[href], td[headers=folders_name] a[href]"); //Nombre Asignaturas String 
 			names = new String[elem.size()+1]; //todo + 1 (atrás)
 			names[0] = "Atrás " + onData.get(onData.size() - 2)[1];
 			for(Element el : elem){
 			    names[i] = el.text();
 			    i++;
 			}
 		}
 		Log.d("asigseToArray", String.valueOf(elem.size()));
 	}
 	
 	public void urlsToArray(Elements melem, Boolean isHome, Boolean comun) {
 		Elements elem;
 		int i = 1;
 		if(isHome) {
 			elem = melem.select("td[headers=contents_name] a, td[headers=folders_name] a").not("[href*=/clubs/]"); //Nombre Asignaturas String !"Comunuidades"
 			urls = new String[(elem.size())+1];
 			urls[0] = "/dotlrn/?page_num="+panel;
 			for(Element el : elem){
 			    urls[i] = el.select("a").attr("href");
 			    i++;
 			}
 		} else if(comun) {
 			elem = melem.select("td[headers=contents_name] a[href*=/clubs/], td[headers=folders_name] a[href*=/clubs/]"); //Nombre Asignaturas String "Comunuidades"
 			urls = new String[elem.size()+1];
 			urls[0] = onData.get(onData.size() - 2)[0];
 			for(Element el : elem){
 			    urls[i] = el.select("a").attr("href");
 			    i++;
 			}
 		} else {
 			elem = melem.select("td[headers=contents_name] a[href], td[headers=folders_name] a[href]"); //Nombre Asignaturas String 
 			urls = new String[elem.size()+1];
 			urls[0] = onData.get(onData.size() - 2)[0];
 			for(Element el : elem){
 			    urls[i] = el.select("a").attr("href");
 			    i++;
 			}
 		}
 		Log.d("urlsToArray", String.valueOf(urls.length));
 	}
 	
 	public void typeToArray(Elements melem, Boolean isHome, Boolean comun, int size) {
 		Elements elem = melem.select("td[headers=folders_type], td[headers=contents_type]"); //Nombre Asignaturas String
 		String[] mtypes = {"carpeta", "Carpeta", "PDF", "Microsoft Excel", "Microsoft PowerPoint", "Microsoft Word"};
 		int i = 1;
 		if(isHome) {
 			types = new String[size];
 			types[0] = "6";	
 			while(i<size) {
 				types[i] = "1";
 				i++;
 			}
 		} else if(comun && onData.size()<3) {
 			types = new String[size];
 			types[0] = "0";
 			while(i<size) {
 				types[i] = "6";
 				i++;
 			}
 		} else {
 			types = new String[elem.size()+1];
 			types[0] = "0";
 			for(Element el : elem){
 				String the_types = el.text().trim();
 				types[i] = "7"; // Defecto al menos que...:
 				if(mtypes[0].equals(the_types.toString()) || mtypes[1].equals(the_types.toString())) types[i] = "1"; // 1 = Carpeta
 				if(mtypes[2].equals(the_types.toString())) types[i] = "2"; // 2 = PDF 
 				if(mtypes[3].equals(the_types.toString())) types[i] = "3"; // 3 = Excel
 				if(mtypes[4].equals(the_types.toString())) types[i] = "4"; // 4 = Power Point
 				if(mtypes[5].equals(the_types.toString())) types[i] = "5"; // 5 = Word
 				i++;
 			}
 		}
 	}
 	
 	/* TASKS */
 	
 	private class docDownload extends AsyncTask<Void, Integer, Integer> {
 		String url_back;
 		protected docDownload(String url_back) {
 			this.url_back = url_back;
 		}
 		
     	protected Integer doInBackground(Void... params) {
         	Integer id = 0;
     		if(cookies != null) {
         		Log.d("Document", "getDoc AHORA");
         		try {
         			id = getDoc(mycontext, url_back);
             	}catch(SocketTimeoutException e)
             	{
             		task_status = false;
             		id = 6;
             	}catch(IOException e)
             	{
             		task_status = false;
             		id = 5;
             	}
         		Log.d("Cookie", "HAY COOKIE!");
         	} else {
         		try {
         			setData();
             	}catch(SocketTimeoutException e)
             	{
             		task_status = false;
             		id = 6;
             	}catch(IOException e)
             	{
             		task_status = false;
             		id = 5;
             	}
         	}
             return id;
         }
     	
         protected void onProgressUpdate(Integer... progress) {
             /** Log.d("Scrapping",
                     String.valueOf(progress[0]) + "% scrapped");
             Toast.makeText(getBaseContext(),
                 String.valueOf(progress[0]) + "% scrapped",
                 Toast.LENGTH_LONG).show(); **/
         }
 
 		protected void onPreExecute() {
         	// ProgressDialog (salta para mostrar el proceso del archivo descarg�ndose)
     		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     		dialog.setMessage(getString(R.string.process));
             dialog.setCancelable(true);
             dialog.setMax(100);
             dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel_button),
             		new DialogInterface.OnClickListener() {
             		public void onClick(DialogInterface dialog,
             		int whichButton)
             		{
             		task_status = false;
             		}
             		});
 	        dialog.show();
 	        setRestrictedOrientation();
         }
 		
 		protected void onPostExecute(Integer id) {
 			Log.d("ID", String.valueOf(id));
 			process(task_status, id);
 		}
     }
     
     private class urlConnect extends AsyncTask<Void, Integer, Response> {
     	protected urlConnect() {}
     	
     	protected Response doInBackground(Void... params) {
         	//Mirar si hay datos en cache, si los hay, cogerlos y hacer el get()
         	if(cookies != null) { //si hay cookie, hacemos el GET
         		Log.d("Cookie", "HAY COOKIE!");
         		try {
             		connectGet();
             	}catch(SocketTimeoutException e)
             	{
             		res = null;
             	}catch(IOException e)
             	{
             		res = null;
             	}
         	} else { //Si no hay cookie, hacemos el POST
         		Log.d("Cookie", "NO HAY COOKIE!");
             	try {
             		setData();
             	}catch(SocketTimeoutException e)
             	{
             		res = null;
             	}catch(IOException e)
             	{
             		res = null;
             	}
         	}
             return res;
         }
 
         protected void onProgressUpdate(Integer... progress) {
         }
 
         protected void onPostExecute(Response response) {
         	if(res != null) {
 	        	if(res.hasCookie("ad_user_login")) { // El usuario y la contrase�a son correctas
 	            	Log.d("Cookie", String.valueOf(a));
 	            	if(a==2) {
 	            		a = 1;
 	            		Log.d("Cookie", "Recargada Cookie, hacemos peticion GET");
 	            		new urlConnect().execute(); //REejecutamos la tarea (GET)
 	            	}
 	            } else if(res.hasCookie("fs_block_id")) { // No tiene "ad_user_login" pero si "fs_block_id" --> Cookie NO vencida
 	            	Log.d("Cookie", "No vencida: GET hecho");
 	            	String out = "";  
 	                try {
 	        			out = response.parse().body().toString();
 	        		} catch (IOException e) {
 	        			// TODO Auto-generated catch block
 	        			e.printStackTrace();
 	        		}
 	                process(out);
 	            } else if(res.hasCookie("ad_session_id")) { // Usuario y contrase�a incorrectos. No tiene ni "ad_user_login" ni "fs_block_id"
 	            	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
 	            	Editor editor = prefs.edit();
 		            editor.remove("cookies");
 	                editor.commit();
 	                cookies = null; //eliminamos la cookie
 	                a = 2; // Aumentamos el contador
 	                Log.d("Cookie", "COOKIE VENCIDA");
 	                new urlConnect().execute(); //REejecutamos la tarea (POST)
 
 	            } else if(res.hasCookie("tupi_style") || res.hasCookie("zen_style")) { // Cookie correcta, sesi�n POST ya habilitada. GET correcto. Procede.
 	            	String out = "";  
 	                try {
 	        			out = response.parse().body().toString();
 	        		} catch (IOException e) {
 	        			// TODO Auto-generated catch block
 	        			e.printStackTrace();
 	        		}
 	                process(out);
 	            }
 	        } else {
 	        	Log.d("Exception", "Timeout2");
 	        	process(false, 6);
 	        }
         }
     }
     
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
 	
 	public void connectGet() throws IOException, SocketTimeoutException {
 		Response resp = Jsoup.connect("http://aulavirtual.uv.es"+ url).cookies(cookies).method(Method.GET).timeout(10*1000).execute();
 		res = resp;
 		Log.d("Connect", "Conectando con cookies");
 		Log.d("URL", "http://aulavirtual.uv.es"+url);
 		Log.d("COOKIE GET", resp.cookies().toString());
 	}
 	
 	/**
 	 * Codifica la URL del archivo, lo descarga (si no existe) y lo guarda en Almacenamiento externo (SDCARD)//Android/data/com.jp.miaulavirtual/files
 	 * @return String - Tama�o del archivo en formato del SI
 	 * @throws IOException
 	 */
     public int getDoc(Context mycontext, String url_back) throws IOException, SocketTimeoutException {
     	URI uri;
     	int id = 0;
     	Log.d("Document", url);
     	String request = null;
 		// Codificamos la URL del archivo
     	try {
 			uri = new URI(
 				    "http", 
 				    "aulavirtual.uv.es", 
 				    url,
 				    null);
 			request = uri.toASCIIString();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	Log.d("Document", request);
     	
     	// Recheck cookie isn't expire
     	Response resp = Jsoup.connect("http://aulavirtual.uv.es"+ url_back).cookies(cookies).method(Method.GET).timeout(10*1000).execute();
     	res = resp;
     	Log.d("Document", "Respuesta2");
     	Log.d("Cookie", res.cookies().toString());
     	// Action in response of cookie checking
     	if(res.hasCookie("ad_user_login")) { // El usuario y la contraseña son correctas al renovar la COOKIE (NO PUEDEN SER INCORRECTOS, YA ESTABA LOGUEADO)
         	Log.d("Cookie", String.valueOf(a));
         	if(a==2) {
         		a = 1;
         		new docDownload(url_back).execute(); //REejecutamos la tarea docDownload
         	}
     	} else if(res.hasCookie("fs_block_id")) {
     		id = downloadFile(request);  
         } else if(res.hasCookie("ad_session_id")) { // Cookie Vencida
         	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mycontext);
         	Editor editor = prefs.edit();
         	// Cookie Vencida
             editor.remove("cookies");
             editor.commit();
             cookies = null; // eliminamos la cookie
             a = a+1; // Aumentamos el contador
             Log.d("Cookie", "COOKIE VENCIDA");
             new docDownload(url_back).execute(); //REejecutamos la tarea (POST)
         } else if(res.hasCookie("tupi_style") || res.hasCookie("zen_style")) { // Cookie correcta, sesi�n POST ya habilitada. GET correcto. Procede.
         	id = downloadFile(request);
         }
     	Log.d("ID", String.valueOf(id));
     	return id;
     }
     
     public int downloadFile(String request) {
     	URL url2;
 	    URLConnection conn;
 	    int lastSlash;
 	    Long fileSize = null;
 	    BufferedInputStream inStream;
 	    BufferedOutputStream outStream;
 	    FileOutputStream fileStream;
 	    String cookies = cookieFormat(scookie); // format cookie for URL setRequestProperty
 	    final int BUFFER_SIZE = 23 * 1024;
 	    int id = 1;
 	    
 	    File file = null;
 		
 		Log.d("Document", "2ª respueesta");
 	    try
 	    {
     		// Just resources
     		lastSlash = url.toString().lastIndexOf('/');
     		
     		// Directory creation
     		String root = Environment.getExternalStorageDirectory().toString();
     		Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
     		// check if is there external storage
     		if(!isSDPresent) {
     			task_status = false;
     			id = 9;
     		} else {
 	    		String folder;
 	    		if(comunidades) {
 	    			folder = onData.get(2)[1];
 	    		} else {
 	    			folder = onData.get(1)[1];
 	    		}
 	    		folder = folder.replaceAll( "\\d{4}-\\d{4}\\s|\\d{4}-\\d{2}\\s|Documentos\\sde\\s?|Gr\\..+?\\s|\\(.+?\\)", "" );
 	    		folder = folder.toString().trim();
 	    		Log.d("Folder", folder);
 	    	    File myDir = new File(root + "/Android/data/com.jp.miaulavirtual/files/"+folder);
 	    	    myDir.mkdirs();
 	    	    
 	    	    // Document creation
 	    	    String name = url.toString().substring(lastSlash + 1);
 	    	    file = new File (myDir, name);
 	    	    Log.d("Document", name);
 	    	    fileSize = (long) file.length();
 	    	    
 	    	    // Check if we have already downloaded the whole file
 	    	    if (file.exists ()) {
 	    	    	dialog.setProgress(100); // full progress if file already donwloaded
 	    	    } else {
 			        // Start the connection with COOKIES (we already verified that the cookies aren't expired and we can use them)
 		    	    url2 = new URL(request);
 			        conn = url2.openConnection();
 			        conn.setUseCaches(false);
 			        conn.setRequestProperty("Cookie", cookies);
 			        conn.setConnectTimeout(10*1000);
 			        conn.setReadTimeout(20*1000);
 			        fileSize = (long) conn.getContentLength();
 			        
 			        // Check if we have necesary space
 			        if(fileSize >= myDir.getUsableSpace()) { task_status = false; id = 2;
 			        } else {
 			        	
 				        // Start downloading
 				        inStream = new BufferedInputStream(conn.getInputStream());
 				        fileStream = new FileOutputStream(file);
 				        outStream = new BufferedOutputStream(fileStream, BUFFER_SIZE);
 				        byte[] data = new byte[BUFFER_SIZE];
 				        int bytesRead = 0;
 				        int setMax = (conn.getContentLength()/1024);
 				        dialog.setMax(setMax);
 			
 				        while(task_status && (bytesRead = inStream.read(data, 0, data.length)) >= 0)
 				        {
 				            outStream.write(data, 0, bytesRead);
 				            // update progress bar
 				            dialog.incrementProgressBy((int)(bytesRead/1024));
 				        }
 				        
 				        // Close stream
 				        outStream.close();
 				        fileStream.close();
 				        inStream.close();
 				        
 				        // Delete file if Cancel button
 				        if(!task_status) {
 				        	file.delete(); 
 				        	if(myDir.listFiles().length<=0) myDir.delete(); 
 				        	id=0;
 				        }  
 				    }
 			    }
 	    	    Log.d("Status", String.valueOf(task_status));
 	    	    // Open file
 		        if(task_status) {
 		        	Log.d("Type", "Hola2");
 		        	dialog.dismiss();
 	                Intent intent = new Intent();
 	                intent.setAction(android.content.Intent.ACTION_VIEW);
 	              
 	                MimeTypeMap mime = MimeTypeMap.getSingleton();
 	                // Get extension file
 	                String file_s = file.toString();
 	                String extension = "";
 	
 	        		int i = file_s.lastIndexOf('.');
 	        		int p = Math.max(file_s.lastIndexOf('/'), file_s.lastIndexOf('\\'));
 	
 	        		if (i > p) {
 	        		    extension = file_s.substring(i+1);
 	        		}
 	        		
 	        		// Get extension reference
 	                String doc_type = mime.getMimeTypeFromExtension(extension);
 	                
 	                Log.d("Type", extension);
 	             
 	                intent.setDataAndType(Uri.fromFile(file),doc_type);
 	                startActivity(intent);
 		        }
     		}
 	    }
 	    catch(MalformedURLException e) // Invalid URL
 	    {
 	    	task_status = false;
 	    	if(file.exists ()) { file.delete(); }
 	    	id = 3;
 	    }
 	    catch(FileNotFoundException e) // FIle not found
 	    {
 	    	task_status = false;
 	    	if(file.exists ()) { file.delete(); }
 	    	id = 4;
 	    }
 	    catch(SocketTimeoutException e) // time out
 	    {
 	    	Log.d("Timeout", "Timeout");
 	    	task_status = false;
 	    	if(file.exists ()) { file.delete(); }
 	    	id = 7;
 	    }
 	    catch(Exception e) // General error
 	    {
 	    	task_status = false;
 	    	if(file.exists ()) { file.delete(); }
 	    	id = 8;
 	    }
 	    Log.d("Type", String.valueOf(id));
 	    Log.d("StartOk3", "¿Como he llegado hasta aquí?");
 	    // notify completion
 	    Log.d("ID", String.valueOf(id));
 	    return id;
 
     }	
 	
     /**
      * Pasa cookies en formato String {cookies} a Map que es el formato utilizado poara insertar cookies por JSoup
      * @param scookie la cookie que queremos pasar a Map
      * @return Map<String, String> cookie
      */
 	public Map<String, String> toMap(String scookie) {
 		Map<String, String> cookies = new LinkedHashMap<String, String>();
 		String[] split = scookie.split(" *[=,^{}$] *");
 		int i = 1;
 		while(i<split.length-1) {
 			//System.out.println(i);
 			cookies.put(split[i], split[i+1]);
 			//i++;
 			i=i+2;
 		}
 		return cookies;
 	}
 	
 	public String cookieFormat(String scookie) {
 		String [] arrayc = scookie.split(" *[=,^{}$] *");
 		String cookies ="";
 		for(int a = 1; a<(int)arrayc.length; a++) {
 			cookies += arrayc[a]+"="+arrayc[a+1]+"; ";
 			a = a+1;
 		}
 		cookies = cookies.trim();
 		return cookies;
 	}
 	
 	/**
 	 * Pasa Bytes a formate legible del SI (si = true) o del Sistema Binario
 	 * @param bytes
 	 * @param si
 	 * @return String
 	 */
 	public static String humanReadableByteCount(long bytes, boolean si) {
 	    int unit = si ? 1000 : 1024;
 	    if (bytes < unit) return bytes + " B";
 	    int exp = (int) (Math.log(bytes) / Math.log(unit));
 	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
 	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void setRestrictedOrientation() {
 		/* We don't want change screen orientation */
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
 	}
 }
