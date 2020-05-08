 package com.dirtyunicorns.updater;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @SuppressWarnings("unused")
 public class MainActivity extends Activity {
 
 	private TextView deviceModel, deviceVers, deviceBuildNum, serverVers, serverBuildNum, serverBuildDate, UpdateAvail;
 	private ImageButton btnDLatest;
 	private ListView list;
 	private String devM, devV, devBM, servV, servBM, servL, servD;
     private Intent intent;
 	private SharedPreferences settings;
     private String model;
     private ServerComm sc;
     private String data;
     private String[] all;
     private NotificationManager notificationManager;
     private Notification notification;
     private Intent notificationIntent;
     private double dV;
     private String error = "";
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
        
         //Create dialog mode for widgets
         /*Boolean fDialogMode = getIntent().hasExtra("dialog_mode");
         if (!fDialogMode) {
         	super.setTheme(android.R.style.Theme_Holo);
         }*/
         
         //Start the View
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         //Initialize Variables
         settings = getSharedPreferences("UpdateInfo",0);
         deviceModel = (TextView) findViewById(R.id.DeviceModel);
         deviceVers = (TextView) findViewById(R.id.DeviceVers);
         deviceBuildNum = (TextView) findViewById(R.id.DeviceBuildnum);
         serverVers = (TextView) findViewById(R.id.ServerVers);
         serverBuildNum = (TextView) findViewById(R.id.ServerBuildNum);
         serverBuildDate = (TextView) findViewById(R.id.ServerBuildDate);
         UpdateAvail = (TextView) findViewById(R.id.UpdateAvail);
         btnDLatest = (ImageButton) findViewById(R.id.btnDownloadLatest);
         list = (ListView) findViewById(android.R.id.list);
     	btnDLatest.setVisibility(View.GONE);
        model = GetProduct();
         
         System.out.println(model);
         
         sc = new ServerComm(model);
         sc.start();
         
         //Set Text Fields
         devBM = GetBuildNum();
     	devV = GetVersNum();
     	dV = Double.valueOf(devV);
     	deviceBuildNum.setText("Device Build Number: " + devBM);
     	deviceModel.setText("Device: " + model);
         deviceVers.setText("Device Version: " +  devV);
         
         
         if (IsOnline()) {
             		try{
             			
                     	data = sc.getLatest();
                     	System.out.println(data);
                     	if (data != "Device not found in the database")
 	                    {
 	                    	all = sc.getAll();
 	                    	ListAdapter adapter = new ListAdapter(getApplicationContext(),R.layout.list_row, all);
 	                        list.setAdapter(adapter);
 	                        startService(new Intent(getApplicationContext() ,ManifestService.class));
 	                        SplitStrings();
 	                		GetInfo();
                     	}
                     	else
                     	{
                     		Toast.makeText(this, "This is not an officially supported device", Toast.LENGTH_LONG).show();

                    		UpdateAvail.setText("This is not an officially supported device");
                    		UpdateAvail.setTextColor(Color.RED);
                     	}
                     }
                     catch (Exception e) {
                     	e.printStackTrace();
                     }
             		
             	}
                 else
                 {
                 	btnDLatest.setVisibility(View.GONE);
                 	error = "offline";
                 }
         	
         //Check if online
         if (error == "offline") {
         	Toast.makeText(this, "Not online, Please try again later",Toast.LENGTH_LONG).show();
         }
         
         btnDLatest.setOnClickListener(new OnClickListener() {
         	@Override
         	public void onClick(View v){
         		StartDownload();
         	}
         });
     }
     
     public void StartDownload() {
     	new DownloadFile().execute();
     }
     
     public void SplitStrings(){
     	String[] splitJSON = data.split(",");
     	servV = splitJSON[1];
     	servBM = splitJSON[2];
     	servL = splitJSON[3];
     	servD = splitJSON[4];
     }
     
     public void GetInfo()
     {
     	
     	int dB, sB;
     	double sV;
     	sV = Double.valueOf(servV);
     	dB = Integer.valueOf(devBM);
     	sB = Integer.valueOf(servBM);
     	if (sV > dV)
     	{
     		UpdateAvail.setText("An Update is Available");
     		UpdateAvail.setTextColor(Color.GREEN);
     	}
     	else
     	{
     		if (sB > dB){
         		UpdateAvail.setText("An Update is Available");
         		UpdateAvail.setTextColor(Color.GREEN);
     		}
     		else
     		{
        		UpdateAvail.setText("You are on the most current version");
         		UpdateAvail.setTextColor(Color.RED);
     		}
     	}
     	
     	if (UpdateAvail.getText() == "An Update is Available") {
 
         	btnDLatest.setVisibility(View.VISIBLE);
     	}
     	
         serverVers.setText("Server Version: " +  servV);
         serverBuildNum.setText("Server Build Number: " + servBM);
         serverBuildDate.setText("Latest Build Date: " + servD);
         
     }
     
     public String GetBuildNum() {
 		String line = "";
 		try {
 			Process ifc = Runtime.getRuntime().exec("getprop ro.du.buildnum");
 			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
 			line = bis.readLine();
 			ifc.destroy();
 		} catch (java.io.IOException e) {
 			line = e.getMessage();
 		}
 		
 		return line;
 	}
     
     public String GetVersNum() {
 		String line = "";
 		try {
 			Process ifc = Runtime.getRuntime().exec("getprop ro.romstats.version");
 			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
 			line = bis.readLine();
 			ifc.destroy();
 		} catch (java.io.IOException e) {
 			line = e.getMessage();
 		}
 		
 		return line;
 	}
     
    public String GetProduct() {
    	String line = "";
		try {
			Process ifc = Runtime.getRuntime().exec("getprop ro.build.product");
			BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
			line = bis.readLine();
			ifc.destroy();
		} catch (java.io.IOException e) {
			line = e.getMessage();
		}
		
		return line;
    }
    
     public boolean IsOnline()
 	{
 		boolean online = false;
 		
 		ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo[] activeNet = conMgr.getAllNetworkInfo();
 		for (NetworkInfo adapter : activeNet)
 		{
 			if (adapter.isConnected()){
 				Log.v("DU", "Device is online, getting info");
 				online = true;
 				break;
 			}
 		}
 		
 		return online;
 	}
 
 
     public void OpenSettings(MenuItem item) {
     	Log.i("DU MENU", "Settings Clicked");
 		Intent i = new Intent(this, SettingsActivity.class);
 		startActivity(i);
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		
 		return true;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public class DownloadFile extends AsyncTask<String, String, String> {
 	    CharSequence contentText;
 	    Context context;
 	    CharSequence contentTitle;
 	    PendingIntent contentIntent;
 	    int HELLO_ID = 1;
 	    long time;
 	    int icon;
 	    CharSequence tickerText;
 	    File file;
 
 	    
 		public void downloadNotification(){
 	        try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 	    	String ns = Context.NOTIFICATION_SERVICE;
 	        notificationManager = (NotificationManager) getSystemService(ns);
 
 	        icon = R.drawable.ic_launcher;
 	        //the text that appears first on the status bar
 	        tickerText = "Downloading...";
 	        time = System.currentTimeMillis();
 
 	        notification = new Notification(icon, tickerText, time);
 
 	        context = getApplicationContext();
 	        //the bold font
 	        contentTitle = "DU downloading...";
 	        //the text that needs to change
 	        contentText = "0% complete";
 	        notificationIntent = new Intent(Intent.ACTION_VIEW);
 	        contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
 	        
 	        
 	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 	        notificationManager.notify(HELLO_ID, notification);
 
 	    }
 
 
 	    @Override
 	    protected void onPreExecute() {
 	        //execute the status bar notification
 	        downloadNotification();
 
 	        super.onPreExecute();
 	    }
 
 	    @Override
 	    protected String doInBackground(String... url) {
 	        
 	        
 	        	Thread t = new Thread() {
 	        		public void run() {
 	        			try {
 	        				int count;
 		        			URL url2 = new URL("http://fs1.d-h.st/download/00073/1ne/multirom-20132709-v1-m7ul.zip");
 		    	            HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
 		    	            connection.setRequestMethod("GET");
 		    	            //connection.setDoOutput(true);
 		    	            connection.connect();
 	
 		    	            int lengthOfFile = connection.getContentLength();
 	
 		    	            //make the stop drop rave folder
 		    	            File sdrFolder = new File(Environment.getExternalStorageDirectory() + "/DirtyUnicorns");
 		    	            boolean success = false;
 	
 		    	            if(!sdrFolder.exists()){
 		    	                success = sdrFolder.mkdir();
 		    	            }      
 		    	            if (!success) { 
 		    	                String PATH = Environment.getExternalStorageDirectory()
 		    	                        + "/DirtyUnicorns/";
 		    	                file = new File(PATH);
 		    	                file.mkdirs();
 	
 		    	            }
 		    	            else 
 		    	            {
 		    	                String PATH = Environment.getExternalStorageDirectory()
 		    	                        + "/DirtyUnicorns/";
 		    	                file = new File(PATH);
 		    	                file.mkdirs();
 		    	            }
 	
 		    	            String[] path = url2.getPath().split("/");
 		    	            String mp3 = path[path.length - 1];
 		    	            String mp31 = mp3.replace("%20", " ");
 		    	            String sdrMp3 = mp31.replace("%28", "(");
 		    	            String sdrMp31 = sdrMp3.replace("%29", ")");
 		    	            String sdrMp32 = sdrMp31.replace("%27", "'");
 	
 		    	            File outputFile = new File(file, sdrMp32);
 		    	            FileOutputStream fos = new FileOutputStream(outputFile);
 	
 		    	            InputStream input = connection.getInputStream();
 	
 		    	            byte[] data = new byte[1024];
 		    	            long total = 0;
 		    	            while ((count = input.read(data)) != -1) {
 		    	                total += count;
 		    	                publishProgress(""+(int) (total * 100 / lengthOfFile));
 		    	                fos.write(data, 0, count);
 		    	            }
 		    	            fos.close();
 		    	            input.close();
 
 	    	        } catch (IllegalArgumentException e) 
 	    	        { e.printStackTrace();
 	    	        }catch (IllegalStateException e) { 
 	    	            e.printStackTrace();
 	    	        }catch (IOException e) {
 	    	        e.printStackTrace();
 	    	        }
 	        		}
 	        	};
 	        	t.start();
 	            
 
 	        return null;
 	    }
 
 	    @Override
 	    public void onProgressUpdate(String... progress) {
 	        if (Integer.parseInt(progress[0]) == 100) {
 	        	notification.flags = Notification.FLAG_AUTO_CANCEL;
 	        	notificationIntent.setType("application/zip");
 	        }
 	        else if (Integer.parseInt(progress[0]) == 0) {
 	        	notification.flags = Notification.FLAG_AUTO_CANCEL;
 	        }
 	        else {
 	        	notification.flags |= Notification.FLAG_ONGOING_EVENT;
 	        }
 	    	contentText =  Integer.parseInt(progress[0]) + "% complete";
 	        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 	        notificationManager.notify(HELLO_ID, notification);
 	        super.onProgressUpdate(progress);
 	    }
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		GetInfo();
 	}
     
 }
