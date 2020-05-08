 package com.digitalnatura.dialogmanager;
 
 //import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 
 import com.digitalnatura.R;
 import com.digitalnatura.helpers.servGenerarPrevParcial;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class DialogosActivity extends FragmentActivity {
 	private static Context mContext;
	private SharedPreferences settings;
	private SharedPreferences asdf;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mContext = this;
 		
 		setContentView(R.layout.main_seq);
 
		 settings = getSharedPreferences("titulo", 0);
 		String tituloGuion = settings.getString("titulo", "oquesexa").replace(
 				"_", " ");
 
 		// SharedPreferences settingas = getSharedPreferences("titulo", 0);
 		// String no_sec = settingas.getString("no_sec","oquesexa");
 
		 asdf = getSharedPreferences("no_sec", 0);
 		String no_sec = asdf.getString("no_sec", "oquesexa");
 
 		this.setTitle(" " + getString(R.string.secuencia_abrev) + " " + no_sec
 				+ "\n " + getString(R.string.of) + " \"" + tituloGuion + "\"");
 		
 		
 		AdView adView = (AdView)this.findViewById(R.id.adView);
 		
 	    AdRequest gromenaguer = new AdRequest();
 	    gromenaguer.addTestDevice(AdRequest.TEST_EMULATOR);
 	    gromenaguer.addTestDevice("A209D6829AFBF722F355D5303D9E26C6");
 	    gromenaguer.addTestDevice("B265510A142C0694394C92E2DA52FC65");
 		adView.loadAd(gromenaguer );
 	    
 	    
 		
 	    
 	    
 
 		ViewPager pager = (ViewPager) findViewById(R.id.pager);
 		pager.setAdapter(new TestTitleFragmentAdapter(
 				getSupportFragmentManager()));
 
 		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
 		indicator.setViewPager(pager, 1);
 
 		Intent serviceDom = new Intent(getContext(),
 				servGenerarPrevParcial.class);
 		getContext().startService(serviceDom);
 
 		indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				//
 				if (position == 0) {
 					Preview.recargar();
 
 				}
 				// else
 				// Toast.makeText(Secuencias.this, "Changed to page " +
 				// position, Toast.LENGTH_SHORT).show();
 
 			}
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 	}
 
 	public static Context getContext() {
 		return mContext;
 	}
 }
