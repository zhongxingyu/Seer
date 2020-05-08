 package fh.itb.avalanchewarning;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Array;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StrictMode;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	File storageDir;
 	LinkedList<File> photoList;
 	Socket socket;
 	String timeStamp;
 	LocationManager locationManager;
 	List<String> regionNameList;
 	TempData tempdata;
 	String[] warnings;
 	ListenThread lt;
 
 	//sendet einen GPS-string an den Server
 	public void sendGPS(String GPSData) {
 		PrintWriter out;
 		try {
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.write("GPS");
 			out.flush();
 			out.write(GPSData);
 			out.flush();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	//sendet das Codewort "Info" an den Server um aktuelle Informationen zu erhalten
 	public void sendInfo() {
 		PrintWriter out;
 		try {
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.write("Info");
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// Sendet ein einzelnes foto
 	public boolean sendPhoto(File photo) {
 		PrintWriter out;
 		try {
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.write("Bild");
 			out.flush();
 			//out.close();
 
 			File myFile = new File(photo.getAbsolutePath());
 			byte[] mybytearray = new byte[(int) myFile.length()];
 			FileInputStream fis = new FileInputStream(myFile);
 			BufferedInputStream bis = new BufferedInputStream(fis);
 			bis.read(mybytearray, 0, mybytearray.length);
 			OutputStream os = socket.getOutputStream();
 			System.out.println("Sending...");
 			os.write(mybytearray, 0, mybytearray.length);
 			os.flush();
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 
 	/**
 	 * Diese Methode aktualisiert die auf der Aktiviti ersichtlichen Daten.
 	 * Hierfr werden die aktuellen Informationen aus einem TempDataobjekt
 	 * geholt und eingefgt.
 	 */
 	public void refreshData() {
 		lt.getForecast();
 		if (!tempdata.getData().equals("")) {
 			String[] splitMessage = tempdata.getData().split("--");
 			// 0 = wetter morgen | 1= wetter bermorgen
 			String[] wetter = splitMessage[0].split("##");
 			// 0 = region1 __ wetterkondition | 1= region2 usw...
 			String[] locations = splitMessage[1].split("##");
 
 			// Wettertexte setzen
 			((TextView) findViewById(R.id.txtForecast1)).setText(wetter[0]);
 			((TextView) findViewById(R.id.txtForecast2)).setText(wetter[1]);
 
 			// daten von den locations vorbereiten
 			warnings = new String[locations.length];
 			regionNameList = new ArrayList<String>();
 			String[] temp;
 			for (int i = 0; i < locations.length; i++) {
 				temp = locations[i].split("__");
 				regionNameList.add(temp[0]);
 				warnings[i] = temp[1];
 			}
 			((TextView) findViewById(R.id.txtAvalancheProbability))
 			.setText(warnings[0]);
 		}
 
 		// locations in Spinner eintragen
 		Spinner spinner = (Spinner) findViewById(R.id.spn_CurrentRegion);
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, regionNameList);
 		dataAdapter
 		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(dataAdapter);
 	}
 
 	//initialisiert das GPS-modul
 	private void initGPS() {
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 
 		LocationListener locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 			}
 
 			public void onStatusChanged(String provider, int status,
 					Bundle extras) {
 			}
 
 			public void onProviderEnabled(String provider) {
 			}
 
 			public void onProviderDisabled(String provider) {
 			}
 		};
 
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
 				0, locationListener);
 	}
 
 	//initialisiert den Forecast mit den richtigen Tagnamen
 	private void initForecast() {
 		Date date = new Date();
 
 		GregorianCalendar calendar = new GregorianCalendar(date.getYear(),
 				date.getMonth(), date.getDay());
 		int i = calendar.get(Calendar.DAY_OF_WEEK);
 
 		TextView date1 = (TextView) findViewById(R.id.txtDate1);
 		TextView date2 = (TextView) findViewById(R.id.txtDate2);
 
 		if (i == 2) {
 			date1.setText(R.string.main_forecast_Monday);
 			date2.setText(R.string.main_forecast_Tuesday);
 		} else if (i == 3) {
 			date1.setText(R.string.main_forecast_Tuesday);
 			date2.setText(R.string.main_forecast_Wednesday);
 		} else if (i == 4) {
 			date1.setText(R.string.main_forecast_Wednesday);
 			date2.setText(R.string.main_forecast_Thursday);
 		} else if (i == 5) {
 			date1.setText(R.string.main_forecast_Thursday);
 			date2.setText(R.string.main_forecast_Friday);
 		} else if (i == 6) {
 			date1.setText(R.string.main_forecast_Friday);
 			date2.setText(R.string.main_forecast_Saturday);
 		} else if (i == 7) {
 			date1.setText(R.string.main_forecast_Saturday);
 			date2.setText(R.string.main_forecast_Sunday);
 		} else if (i == 1) {
 			date1.setText(R.string.main_forecast_Sunday);
 			date2.setText(R.string.main_forecast_Monday);
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		tempdata = new TempData();
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		lt = new ListenThread(tempdata);
 
 		/*try {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 			.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 			this.socket = new Socket("10.0.2.2", 4711);
 		} catch (UnknownHostException e) {
 			System.out.println("UNKNOWN HOST");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}*/
 
		initGPS();
		initForecast();
 
 		storageDir = new File(
 				Environment
 				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
 				"AvalanchePhotos");
 
 		photoList = new LinkedList<File>();
 
 		// refreshPhotoList();
 
 		Spinner spinner = (Spinner) findViewById(R.id.spn_CurrentRegion);
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parentView,
 					View selectedItemView, int position, long id) {
 				((TextView) findViewById(R.id.txtAvalancheProbability))
 				.setText(warnings[position]);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parentView) {
 				// your code here
 			}
 
 		});
 		
 		Button btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
 		btnTakePhoto.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				 timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 				  
 				 Intent takePictureIntent = new Intent(
 				 MediaStore.ACTION_IMAGE_CAPTURE); Uri savedImage =
 				 Uri.fromFile(new File("/sdcard/IMG_" + timeStamp + ".png"));
 				 takePictureIntent.putExtra("output", savedImage);
 				 startActivityForResult(takePictureIntent, 1);
 				  
 				 galleryAddPic();
 				
 			}
 			
 		});
 		
 		Button btnPhoto = (Button) findViewById(R.id.btnPhoto);
 		btnPhoto.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				sendPhotos();
 			}
 		});
 
 		Button btnGPS = (Button) findViewById(R.id.btnSendGPS);
 
 		btnGPS.setOnClickListener(new View.OnClickListener() {
 
 			@Override 
 			public void onClick(View v) { 
 				Location location =
 						locationManager .getLastKnownLocation(LocationManager.GPS_PROVIDER);
 				String longLat = "Longitude: " + location.getLongitude() + ", Latitude: " +
 						location.getLatitude(); 
 				
 				File gps = new File("/sdcard/" + longLat + ".gps");
 				boolean success = lt.sendGPS(longLat); 
 				if (success == true){
 					gps.delete();
 				}
 				lt = new ListenThread(tempdata);
 			} 
 		});
 
 		Button btnConnect = (Button) findViewById(R.id.btnConnect);
 
 		btnConnect.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				initForecast();
 				lt = new ListenThread(tempdata);
 				lt.getForecast();
 				refreshData();
 				sendAllGPS();
 				sendPhotos();
 			}
 		});
		refreshData();
 	}
 	
 	//versucht alle GPS-aufzeichnungen zu verschicken
 	private void sendAllGPS(){
 		File folder = new File("/sdcard/");
 		for (File f : folder.listFiles()){
 			if (f.getName().endsWith(".gps")){
 				boolean success = lt.sendGPS(f.getName().split(".gps")[0]);
 				
 				if (success == true){
 					f.delete();
 				}
 			}
 		}
 	}
 
 	//fgt erstellte bilder der Gallery hinzu
 	private void galleryAddPic() {
 		Intent mediaScanIntent = new Intent(
 				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
 		File folder = new File("/sdcard/");
 		if (timeStamp != null) {
 			File f = new File("/sdcard/IMG_" + timeStamp + ".png");
 			Uri contentUri = Uri.fromFile(f);
 			mediaScanIntent.setData(contentUri);
 			this.sendBroadcast(mediaScanIntent);
 		}
 	}
 
 	// versucht alle erstellten Fotos zu verschicken
 	// falls ein Foto erfolgreich verschickt wurde wird es gelscht
 	private void sendPhotos() {
 		File folder = new File("/sdcard/");
 
 		boolean successful = false;
 		for (File f : folder.listFiles()) {
 
 			if (f.getName().endsWith(".png")) {
 				successful = lt.sendPhoto(f);
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (successful == true) {
 					f.delete();
 				}
 				lt = new ListenThread(tempdata);
 			}
 		}
 	}
 
 	private void refreshPhotoList() {
 		photoList = new LinkedList<File>();
 
 		for (File file : storageDir.listFiles()) {
 			photoList.add(file);
 		}
 	}
 
 }
