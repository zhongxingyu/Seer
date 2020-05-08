 package edu.klemen.rekreAsist.android;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 public class glavniasist extends MapActivity implements android.view.View.OnClickListener {
 	Button startstop,pavza,novKrog;
 	TextView cas,pot,krog,hitrost,maxHitrost;
 	TextView tvTemperatura;
 	Chronometer stoparca;
 	
 	ApplicationExample podatki;//baza
 	
 	ArrayList<Vadba> poljeRekreacij=new ArrayList<Vadba>();
 	
 	MapController mapController;
 	MyPositionOverlay positionOverlay;
 	ArrayList<Location> locations;
 	List<Float> speed;
 	public float dolzina=0;
 	public int trenKrog=0;
 	public boolean pause=false;
 	public boolean flag=true;
 	public long start = 0;//SystemClock.elapsedRealtime();
 	public long end = 0;
 	float povSp=0;
 	double kalorije=0.0;
 	boolean stoparcaFlag=false;
 	boolean tflag=true;
 	public float maxSpeed=0;
 	LocationManager locationManager;
 	public Calendar koledar;
 	
 	public int oznacbaPoti=0;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.glavni);
 		
 		
 		this.setRequestedOrientation(1);
 		podatki=(ApplicationExample)getApplication(); //baza
 		
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_1);
 		tvTemperatura = (TextView) findViewById(R.id.tv_TitleTemperatura);
 		tvTemperatura.setTextColor(Color.WHITE);
 		
 		
 		locations=new ArrayList<Location>();
 		speed=new ArrayList<Float>();
 		
 		
 		
 		startstop=(Button) findViewById(R.id.btnStartStop);
 		pavza=(Button) findViewById(R.id.btnPavza);
 		cas=(TextView) findViewById(R.id.cas);
 		pot=(TextView) findViewById(R.id.pot);
 		krog=(TextView) findViewById(R.id.krog);
 		hitrost=(TextView) findViewById(R.id.tw_Hitrost);
 		novKrog=(Button) findViewById(R.id.btnNovKrog);
 		maxHitrost=(TextView) findViewById(R.id.maxHitrost);
 		
 		startstop.setOnClickListener(this);
 		pavza.setOnClickListener(this);
 		novKrog.setOnClickListener(this);
 		
 		stoparca=(Chronometer) findViewById(R.id.stoparca);
 		
 		krog.setText("Krog: "+trenKrog);
 		
 		
 		stoparca.setFormat(null);
 		
 
 		Asinhrono task= new Asinhrono();//temperatura
 		
 		task.execute();
 
 		
 		
 		try{
 		//-----------------------------------------maps--
 		MapView myMapView1 = (MapView)findViewById(R.id.myMapView);
 		mapController = myMapView1.getController();
 		
 	//	myMapView1.setSatellite(true);
 	//	myMapView1.setStreetView(true);
 		myMapView1.displayZoomControls(false);
 
 		mapController.setZoom(17);
 
 
 		
 		String context = Context.LOCATION_SERVICE;
 		locationManager = (LocationManager)getSystemService(context);
 		// Add the MyPositionOverlay
 		positionOverlay = new MyPositionOverlay();
 		if(podatki.FLAG_IZBRANA_POT==true) positionOverlay.setIzbranaPot(podatki.getIzbranaPot());//dodam izbrano pot
 		List<Overlay> overlays = myMapView1.getOverlays();
 		overlays.add(positionOverlay);
 
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 		String provider = locationManager.getBestProvider(criteria, true);
 
 //		Location location = locationManager.getLastKnownLocation(provider);
 //		my_updateWithNewLocation(location);
 		
 		locationManager.requestLocationUpdates(provider, 35, 10, locationListener);
 		
 		//-----------------------------------------maps/-
 		}catch(Exception f){
 			Toast.makeText(this, "Prosim preverite ali imate vklopljen internet in GPS", Toast.LENGTH_LONG).show();
 		}
 		
 	}
 	
 	private final LocationListener locationListener = new LocationListener() {
 		public void onLocationChanged(Location location) {
 			my_updateWithNewLocation(location);
 		}
 
 		public void onProviderDisabled(String provider){
 			my_updateWithNewLocation(null);
 		}
 
 		public void onProviderEnabled(String provider){ }
 		public void onStatusChanged(String provider, int status, 
 				Bundle extras){ }
 	};
 
 	private void my_updateWithNewLocation(Location location) {
 		//String latLongString;
 		dolzina=0;
 		//TextView myLocationText;
 		//myLocationText = (TextView)findViewById(R.id.myLocationText);
 		
 		if (location != null) {
 			positionOverlay.setLocation(location,oznacbaPoti);
 
 			Double geoLat = location.getLatitude()*1E6;
 			Double geoLng = location.getLongitude()*1E6;
 			
 			podatki.addDBPot(new PodatkiZaPoti(geoLat, geoLng));//dodam tocko za pot
 			
 			GeoPoint point = new GeoPoint(geoLat.intValue(), 
 					geoLng.intValue());
 
 			mapController.animateTo(point);
 
 			double lat = location.getLatitude();
 			double lng = location.getLongitude();
 			podatki.listaPoti.add(new PodatkiZaPoti(lat, lng));
 			//latLongString = "Lat:" + lat + "\nLong:" + lng;
 			locations.add(location);
 			double sp=location.getSpeed()*3.6;//pretvorba iz m/s v km/h
 			DecimalFormat te=new DecimalFormat("#.##");
 			sp=Double.valueOf(te.format(sp));
 			hitrost.setText("Hitrost: "+sp+"km/h "+oznacbaPoti);
 			if((sp>=0)&&(sp<10)) oznacbaPoti=0;
 			if((sp>=10)&&(sp<25)) oznacbaPoti=1;
 			if((sp>=25)) oznacbaPoti=2;
 			//-------------------------------------------------------------------||||||||||||||||||\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 			speed.add((float)sp);
 			if(maxHitrost((float)sp)==true){
 				maxSpeed=(float)sp;
 				maxHitrost.setText("Max hitrost: "+maxSpeed+"km/h");
 			}
 			if((locations.size()>=2)){
 	    		
 	    		for(int i=0;i<locations.size()-1;i++){
 	    			Location loc1=locations.get(i);
 	    			Location loc2=locations.get(i+1);
 	    			dolzina+=loc1.distanceTo(loc2);   
 	    		//	DecimalFormat test=new DecimalFormat("#.##");
 	    		//	dolzina=float.valueOf(test.format(dolzina));
 	    			//loc1.gett
 	    		}
 	    		pot.setText("Pot: "+dolzina+"m");
 	    		
 			}
 			//myLocationText.setText("Trenutni polo�aj je:" + latLongString);
 		}
 	}
 	public boolean maxHitrost(float s){
 		for(int i=0;i<speed.size();i++){
 			if(s<speed.get(i)) return false;
 		}
 		return true;
 	}
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		
 		switch (v.getId()) {
 		case R.id.btnStartStop:
 			Toast test;
 			if(pause==false){
 				if(flag==false){
 					//start=SystemClock.elapsedRealtime();
 					stoparca.setBase(SystemClock.elapsedRealtime()-end);
 					stoparca.start();
 					stoparcaFlag=true;
 					
 					pause=true;
 					tflag=false;
 				}else{
 					locations.clear();
 					start=SystemClock.elapsedRealtime();
 					stoparca.setBase(SystemClock.elapsedRealtime());//prvi�
 					stoparca.start();
 					stoparcaFlag=true;
 					pause=true;
 					flag=false;
 					tflag=false;
 				}
 			}
 			else{// zamejaj koledar
 				if(tflag==false){
 					Calendar kol;
 					kol=Calendar.getInstance();
 
 					
 					int dan=kol.get(Calendar.DATE);
 					int mesec= kol.get(Calendar.MONTH)+1;
 					int leto= kol.get(Calendar.YEAR);
 //					test=Toast.makeText(this, dan+"  "+mesec+"  "+leto, Toast.LENGTH_SHORT);
 //					test.show();
 					povSp=povprecnaHitrost(speed);
 					kalorije=priblkalorije(povSp, stoparca.getText().toString());//izračun kalorij
 					
 					podatki.dodajPodatke(new podatkiZaBazo(dolzina, povSp, trenKrog, maxSpeed, stoparca.getText().toString(),dan+":"+mesec+":"+leto,kalorije));
 					
 			//		poljeRekreacij.add(new Vadba(locations, speed, trenKrog, maxSpeed,kol.getTime().getDate()+"."+kol.getTime().getMonth()+"."+kol.getTime().getYear()));
 					locations.clear();
 					speed.clear();
 					trenKrog=0;
 					maxSpeed=0;
 					stoparca.stop();
 					stoparca.setBase(SystemClock.elapsedRealtime());
 					flag=true;
 					pause=false;
 					
 					hitrost.setText("Hitrost: "+0.0+"km/h");
 					cas.setText("Cas: ");
 					maxHitrost.setText("Max Hitrost: "+maxSpeed+"km/h");
 					krog.setText("Krog: "+trenKrog);
 					pot.setText("Pot: "+0+"m");
 					
 //					test=Toast.makeText(this, "stvrstic:"+po, Toast.LENGTH_SHORT);
 //					test.show();
 				}
 			}
 			break;
 
 		case R.id.btnPavza:
 			stoparca.stop();
 			if(tflag==false){
 				end=SystemClock.elapsedRealtime() - stoparca.getBase();
 				tflag=true;
 			}
 			pause=false;
 			break;
 		case R.id.btnNovKrog:
 			trenKrog++;
 			krog.setText("Krog: "+trenKrog);
 			stoparca.setBase(SystemClock.elapsedRealtime());
 			break;
 
 		default:
 			break;
 		}
 		
 	}
 	private int rang(float h){
 		if((h>4)&&(h<6)) return 90;
 		if((h>6)&&(h<10)) return 230;
 		if((h>10)&&(h<15)) return 410;
 		if(h>15) return 520;
 		return 0;
 	}
 	private float priblkalorije(float povhitr, String cas){
 		int min=0,h=0;
 		String[] ca;
 		Toast.makeText(this, "kalorije "+cas, Toast.LENGTH_SHORT).show();
 		ca=cas.split(":");
 		if(ca.length==3){
 			h= Integer.parseInt(ca[0]);
 			min = Integer.parseInt(ca[1]);
 		}
 		if(ca.length==2){
 			min = Integer.parseInt(ca[0]);
 		}
 		
 		float cal=0;
 		
 		if(h!=0){
 			cal=cal+rang(povhitr);
 		}
 		cal=cal+((min*rang(povhitr))/60);
 		Toast.makeText(this, "kalorije "+cal, Toast.LENGTH_SHORT).show();
 
 		return cal;
 	}
 	private float povprecnaHitrost(List<Float> s){
 		float v=0;
 		for(int i=0;i<s.size();i++) v=v+s.get(i);
 		return (v/s.size());
 	}
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public String executeHttpGet() throws Exception 
 	{
 		String page;
 	    BufferedReader in = null;
 	    try {
 	        HttpClient client = new DefaultHttpClient();
 	        HttpGet request = new HttpGet();
 	        request.setURI(new URI("http://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observationAms_MARIBOR_TABOR_latest.xml"));
 	        HttpResponse response = client.execute(request);
 	        in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	        StringBuffer sb = new StringBuffer("");
 	        String line = "";
 	        String NL = System.getProperty("line.separator");
 	        
 	        
 	        while ((line = in.readLine()) != null) sb.append(line + NL);	        
 	        
 	        in.close();
 	        page = sb.toString();
 	      //  System.out.println(page);
 	        
 	        
 	        } 
 	    finally 
 		    {
 			        if (in != null) 
 			        {
 			            try {
 			                	in.close();
 			                } 
 			            	catch (IOException e) 
 			            	{
 			            		e.printStackTrace();
 			                }
 			        }
 		     }
 	    
 	    return page;
 	}
 	public String setTemperatura(){
 		String stran = new String();
 		
 		int znacka=0;
 		String tempC;
 		//String relVlaznost,povpTemp,link;
 		try
 		{
 			stran= executeHttpGet();
 		}
 		catch (Exception e) {}
 		znacka = stran.indexOf("<t>");
 		tempC = stran.substring(znacka+3, stran.indexOf("</t>"));
 		return tempC;
 	/*	relVlaznost=stran.substring(stran.indexOf("<rh>")+4,stran.indexOf("</rh>"));
 		povpTemp=stran.substring(stran.indexOf("<tavg>")+6,stran.indexOf("<tavg>"));
 		link=stran.substring(stran.indexOf("<docs_url>")+10,stran.indexOf("<docs_url>"));
 	*/		
 		//Toast.makeText(glavniasist.this, "V temp ", Toast.LENGTH_LONG).show();
 		
 	}
 	
 	
 	private class Asinhrono extends AsyncTask<Void,Void,String> {
 
 
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String tempPrebran="";
 			try{
 				
 				tempPrebran=setTemperatura();
 			}catch(Exception f){
 				//tvTemperatura.setText("Error");
 				return "ni internetne povezave";
 			}
 			return tempPrebran+"°C";
 		}
 		protected void onPostExecute(String tretji){
 			//tekst.setText("dsa");
 			tvTemperatura.setText(tretji);
 		//	Toast.makeText(asinhroniTask.this, "Vsota: "+tretji, Toast.LENGTH_LONG).show();
 			
 		}
 	}
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
		podatki.FLAG_IZBRANA_POT=false;
		podatki.izbranaPotLokacije.clear();
 		super.onStop();
 	}
 
 }
 
