 package com.example.campomagnetico;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class Ayuda extends Activity {
 	protected Intent intent;
 	protected TabHost tabs;
 	protected ViewGroup vista;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_ayuda);
 		
 		//Tabs
 		Resources res = getResources();
 		tabs = (TabHost)findViewById(android.R.id.tabhost);
 		tabs.setup();
 		
 		TabHost.TabSpec spec = tabs.newTabSpec("TabAyudaInicio");
 		spec.setContent(R.id.AyudaInicio);
 		spec.setIndicator(getString(R.string.tabAyuda),res.getDrawable(android.R.drawable.ic_menu_today));
 		tabs.addTab(spec);
 		
 		spec = tabs.newTabSpec("TabAyudaGuion");
 		spec.setContent(R.id.AyudaGuion);
 		spec.setIndicator(getString(R.string.bGuionTeoricoSort),res.getDrawable(android.R.drawable.ic_dialog_map));
 		tabs.addTab(spec);
 		
 		spec = tabs.newTabSpec("TabAyudaComponentes");
 		spec.setContent(R.id.AyudaComponentes);
 		spec.setIndicator(getString(R.string.bComponetesSort),res.getDrawable(android.R.drawable.ic_dialog_info));
 		tabs.addTab(spec);
 		
 		spec = tabs.newTabSpec("TabAyudaSimulacion");
 		spec.setContent(R.id.AyudaSimulacion);
 		spec.setIndicator(getString(R.string.bSimulacionSort),res.getDrawable(android.R.drawable.ic_lock_idle_charging));
 		tabs.addTab(spec);
 		
 		spec = tabs.newTabSpec("TabAyudaMenu");
 		spec.setContent(R.id.AyudaMenu);
 		spec.setIndicator(getString(R.string.bMenuDespleg),res.getDrawable(android.R.drawable.ic_menu_add));
 		tabs.addTab(spec);
 
 		vista = tabs;
 		
		Log.e("llega", "llega");
 //		int activo = getIntent().getIntExtra("tab", 0);
 //		if(activo <= 4){
 //			tabs.setCurrentTab(activo);
 //		}else{
 			tabs.setCurrentTab(0);}
 //	}
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.ayuda, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 	    switch (item.getItemId()) {
 	        case R.id.action_inicio:
 	        	onBackPressed();
 	            return true;
 	        case R.id.action_acercaDe:
 	        	intent = new Intent("acerca_de");
 	        	startActivity(intent);
 	        	return true;
 	        case R.id.action_aumentar:
 	        	recorrerTextView(vista, 0);
 	        	return true;
 	        	
 	        case R.id.action_disminuir:
 	        	recorrerTextView(vista, 1);
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	protected void recorrerTextView (ViewGroup parent, int opcion){
 		float tamano = 0;
 		TextView tvGuion;
 		
 		for (int i = parent.getChildCount() - 1; i >= 0; i--) {
             final View child = parent.getChildAt(i);
             if (child instanceof ViewGroup) {
                 recorrerTextView((ViewGroup) child, opcion);
             } else {
                 if (child != null) {
                 	if (child instanceof TextView){
                 		tvGuion = (TextView)child;
                 		if (!(tvGuion.getText().toString() == (getString(R.string.bComponetesSort))) 
                 				&& !(tvGuion.getText().toString() ==(getString(R.string.bGuionTeoricoSort)))
 	                			&& !(tvGuion.getText().toString() ==(getString(R.string.bMenuDespleg)))
 								&& !(tvGuion.getText().toString() ==(getString(R.string.bMontajeSort)))
 								&& !(tvGuion.getText().toString() ==(getString(R.string.bSimulacionSort)))
 								&& !(tvGuion.getText().toString() ==(getString(R.string.tabAyuda)))){
                 			tamano = tvGuion.getTextSize();
                     		if (opcion == 0){
                     			tamano+=5;
                     		}else {
                     			tamano-=5;
                     		}
                 		tvGuion.setTextSize(TypedValue.COMPLEX_UNIT_PX, tamano);
                 		}
                 	}
                 }
             }
         }
 	}
 
 }
