 package com.guille.loteria;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     private static final String ESTADO = "?s=1";
     private static final String NUMERO = "?n=";
 	private static final String HTTP_API_ELPAIS_COM_WS_LOTERIA_NAVIDAD_PREMIADOS = "http://api.elpais.com/ws/LoteriaNavidadPremiados";
 	private String datos = "";
 	private int i;
 	private String numeroSeleccionado;
 	private ArrayList<String> listaNumeros = new ArrayList<String>();
 
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void actualizar(View v){
     	TableLayout tablaResultados = (TableLayout)findViewById(R.id.tablaPremios);
 		tablaResultados.removeAllViews();
     	for(String num : listaNumeros){
     		new RequestTask().execute(HTTP_API_ELPAIS_COM_WS_LOTERIA_NAVIDAD_PREMIADOS, NUMERO, num);
     	}
     }
     
     public void addNumber(View v){
     	final TableLayout tablaResultados = (TableLayout)findViewById(R.id.tablaPremios);
     	View.OnClickListener eliminarListener = new View.OnClickListener() {
 	        public void onClick(View v) {
 	        	Button b = ((Button)v);
 	        	TableRow row = (TableRow)findViewById(b.getId());
 	        	tablaResultados.removeView(row);
 	        	
	        	for(int t=0;t<listaNumeros.size();t++){
 	        		if(listaNumeros.get(t).equals(String.valueOf(b.getId()))){
 	        			listaNumeros.remove(t);
 	        		}
 	        	}
 	        	
 	        }
 	      };
     	
     	EditText status = (EditText)findViewById(R.id.numeroLoteria);
     	if(status.getText().length() > 0){
     	listaNumeros.add(status.getText().toString());
     	
     	
     	TableRow fila = new TableRow(getApplicationContext());
 		fila.setLayoutParams(new LayoutParams(
                 LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 		TextView textoFila = new TextView(getBaseContext());
 		textoFila.setTextSize(20.0f);
 		textoFila.setText(status.getText().toString());
 		
 		Button botonEliminar = new Button(getBaseContext());
 		botonEliminar.setOnClickListener(eliminarListener);
 		botonEliminar.setId(Integer.parseInt(status.getText().toString()));
 		botonEliminar.setText("Borrar");
 		botonEliminar.setGravity(Gravity.LEFT);
 		botonEliminar.setBackgroundColor(Color.GRAY);
 		fila.setId(Integer.parseInt(status.getText().toString()));
 		
 		fila.addView(botonEliminar);
 		fila.addView(textoFila);
 		fila.setBackgroundColor(0xFF3d46ff);
 		tablaResultados.addView(fila);
 		status.setText("");
     	}
 		
     }
     
     
     class RequestTask extends AsyncTask<String, String, String>{
 
         @Override
         protected String doInBackground(String... uri) {
         	String line="";
         	try{
         	HttpClient client = new DefaultHttpClient();
     		HttpGet request = new HttpGet(uri[0] + uri[1] + uri[2]);
     		
     		HttpResponse response = client.execute(request);
     		
     		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
     		
     		
     		while ((line = rd.readLine()) != null) {
     		  System.out.println(line);
     		  datos = line;
 //    		 JSONObject jsonObj = new JSONObject(line.toString().replace("info=", ""));
     		  JSONObject jsonObj = new JSONObject(line.toString().replace("busqueda=", ""));
     		  numeroSeleccionado = jsonObj.getString("numero");
     		 i = jsonObj.getInt("premio");
     		 
     		}
         }catch(Exception e){
         	e.printStackTrace();
         }
         	return "OK";
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             
             final TableLayout tablaResultados = (TableLayout)findViewById(R.id.tablaPremios);
 //        	final TextView lista = (TextView)findViewById(R.id.listaNumeros);
         	View.OnClickListener eliminarListener = new View.OnClickListener() {
     	        public void onClick(View v) {
     	        	Button b = ((Button)v);
     	        	TableRow row = (TableRow)findViewById(b.getId());
     	        	tablaResultados.removeView(row);
     	        	
    	        	for(int t=0;t<listaNumeros.size();t++){
     	        		if(listaNumeros.get(t).equals(String.valueOf(b.getId()))){
     	        			listaNumeros.remove(t);
     	        		}
     	        	}
     	        	
 //    	        	lista.setText(listaNumeros.toString());
     	        }
     	      };
             
     		TableRow fila = new TableRow(getApplicationContext());
     		fila.setLayoutParams(new LayoutParams(
                     LayoutParams.WRAP_CONTENT));
     		TextView textoFila = new TextView(getBaseContext());
     		textoFila.setText(numeroSeleccionado);
     		textoFila.setTextSize(20.0f);
     		TextView premioNumero = new TextView(getBaseContext());
     		premioNumero.setText(String.valueOf(i) + " €");
     		premioNumero.setTextSize(20.0f);
     		
     		Button botonEliminar = new Button(getBaseContext());
     		botonEliminar.setOnClickListener(eliminarListener);
     		botonEliminar.setId(Integer.parseInt(numeroSeleccionado));
     		botonEliminar.setText("Borrar");
     		botonEliminar.setGravity(Gravity.LEFT);
     		botonEliminar.setBackgroundColor(Color.GRAY);
     		fila.setId(Integer.parseInt(numeroSeleccionado));
     		
     		fila.addView(botonEliminar);
     		
     		fila.addView(textoFila);
     		fila.addView(premioNumero);
     		fila.setBackgroundColor(0xFF3d46ff);
     		tablaResultados.addView(fila);
     		
    	     	//estado.setText(estado.getText() + datos.toString());
         }
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()){
     		case R.id.menu_settings:
     			Intent settingsAbout = new Intent(getApplicationContext(), AboutActivity.class);
     			startActivity(settingsAbout);
     			break;
     	}
     	return true;
     }
     
     
   }
