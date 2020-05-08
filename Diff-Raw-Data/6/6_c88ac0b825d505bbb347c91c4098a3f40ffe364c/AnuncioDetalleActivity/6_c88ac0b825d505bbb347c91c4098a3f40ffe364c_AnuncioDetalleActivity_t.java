 package com.udb.mad.shinmen.benja.guana.anuncios;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.NavUtils;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.MenuItem;
 
 import com.udb.mad.shinmen.benja.guana.anuncios.fragment.AnuncioDetalleFragment;
 
 public class AnuncioDetalleActivity extends ActionBarActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_empty_layout);
 		if(savedInstanceState == null) {
 			AnuncioDetalleFragment f = new AnuncioDetalleFragment();
 			f.setArguments(getIntent().getExtras());
 			FragmentTransaction ft = 
 					getSupportFragmentManager().beginTransaction();
 			ft.add(
					android.R.id.empty, f);
 			ft.commit();
 			setupActionBar();
 		}
 	}
 	
 	private void setupActionBar() {
 	    ActionBar actionBar = getSupportActionBar();
 	    actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/*@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.anuncio_detalle, menu);
 		return true;
 	}*/
 
 }
