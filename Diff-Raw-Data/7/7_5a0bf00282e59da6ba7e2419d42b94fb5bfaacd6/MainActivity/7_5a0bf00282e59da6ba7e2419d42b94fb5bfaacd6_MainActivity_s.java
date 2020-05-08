 package com.karthikb351.vitacad;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.TelephonyManager;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.crittercism.app.Crittercism;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 import com.helpshift.Helpshift;
 import com.karthikb351.vitinfo2dev.R;
 
 public class MainActivity extends SherlockActivity {
 	
 	LoadAttendanceTask currentTask;
 	final Helpshift hs = new Helpshift();
 	static Tracker mTracker;
 	static GoogleAnalytics mInstance;
 	SharedPreferences settings;
 	SharedPreferences.Editor editor;
 	boolean attenCancelled=false;
 	boolean captchaSubmitCancelled=false;
 	boolean captchaLoadCancelled=false;
 	boolean isMainRunning;
 	boolean newuser=false, statusExplicit=false;
 	String DOB, REGNO, whoisnext=null;
 	ListView listViewSub;
 	TextView tv;
     View view;
     String regno="",dob="";
     AlertDialog.Builder builder;
     AlertDialog captchadialog;
     ImageView imCaptcha;
     Button refresh;
     int app_version;
 	DownloadImageTask currentLCTask;
 	SubmitCaptchaTask currentSCTask;
 	EditText captcha_edittext;
 	String captcha="";
 	
 	
 	//TESTING MARKS!
 	
 	static String js = "[[[\"1\", \"2203\", \"ECE201\", \"Probability Theory and Random Process\", \"Theory Only\", \"\", \"\", \"\", \"\", \"Present\", \"2.00\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"2\", \"2221\", \"ECE202\", \"Transmission Lines and fields\", \"Theory Only\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"3\", \"2232\", \"ECE203\", \"Modulation Techniques\", \"Embedded Theory\", \"\", \"\", \"\", \"\", \"Present\", \"4.25\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"4\", \"3609\", \"ECE203\", \"Modulation Techniques\", \"Embedded Lab\", \"N/A\", \"\", \"\"], [\"5\", \"3668\", \"ECE204\", \"Analog Circuit Design\", \"Embedded Lab\", \"N/A\", \"\", \"\"], [\"6\", \"2341\", \"ECE205\", \"Electrical and Electronic Measurements\", \"Theory Only\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"7\", \"2312\", \"ECE304\", \"Microcontroller and Applications\", \"Embedded Theory\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"8\", \"3540\", \"ECE304\", \"Microcontroller and Applications\", \"Embedded Lab\", \"N/A\", \"\", \"\"], [\"9\", \"1832\", \"ENG102\", \"English for Engineers II\", \"Embedded Theory\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"N/A\"], [\"10\", \"3368\", \"ENG102\", \"English for Engineers II\", \"Embedded Lab\", \"N/A\", \"\", \"\"], [\"11\", \"3534\", \"HUM121\", \"Ethics and Values\", \"Embedded Lab\", \"N/A\", \"\", \"\"]], [[\"1\", \"2252\", \"ECE204\", \"Analog Circuit Design\", \"Embedded Theory\", \"CLASS TEST 1\", \"MID TERM EXAM\", \"CLASS TEST 2\", \"PROJECT\", \"N/A\", \"10\", \"50\", \"10\", \"20\", \"N/A\", \"5\", \"20\", \"5\", \"20\", \"N/A\", \"\", \"\", \"\", \"\", \"N/A\", \"\", \"\", \"\", \"\", \"N/A\", \"\", \"\", \"\", \"\", \"N/A\", \"\", \"\", \"\", \"\", \"N/A\"], [\"2\", \"1386\", \"HUM121\", \"Ethics and Values\", \"Embedded Theory\", \"CASE ANALYSIS 1\", \"MID TERM TEST 1\", \"CASE ANALYSIS 2\", \"MID TERM TEST 2\", \"MINI PROJECT\", \"5\", \"15\", \"5\", \"15\", \"10\", \"5\", \"15\", \"5\", \"15\", \"10\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\", \"\"]]]";
 	
 	void trySave(){
 		DataHandler dt = new DataHandler(getApplicationContext());
 		
 		//SAVE THE JSON STRING
 		dt.saveMarks(js);
 		
 		//LOAD THE MARKS USING CLASS NBR
 		Mark mk = dt.loadMarks("2203");
 		
 		Log.v("TESTTEESTT", mk.quiz[0].marks);
 		
 	}
 	//TEST ENDS WORKS :)
 	
 	void extrasInit(String reg, String uid)
 	{
 		try {
 			
 			app_version=getPackageManager().getPackageInfo(MainActivity.this.getPackageName(),0).versionCode;
 			
 		} catch (NameNotFoundException e) {
 			app_version=99;
 			e.printStackTrace();
 		}
     	JSONObject crittercismConfig = new JSONObject();
     	mInstance = GoogleAnalytics.getInstance(this);
     	mTracker = mInstance.getTracker("UA-38195928-1");
     	GAServiceManager.getInstance().setDispatchPeriod(30);
     	try
     	{
     	    crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
     	    crittercismConfig.put("customVersionName", this.getApplicationInfo().packageName);
     	}
     	catch (JSONException je){}
 
     	//Crittercism.init(getApplicationContext(), "50e22966f71696783c000012", crittercismConfig);
     	
     	
     	hs.install(MainActivity.this,
     			"91ff50eded9d62de7020a839c1e2292e",
     			"vitinfo-android.helpshift.com",
     			"vitinfo-android_platform_20130101001453620-203e6cbb7463f6f");
 
     	Crittercism.setUsername(reg);
     	hs.setDeviceIdentifier (uid);
     	hs.setUsername (reg);
     	mTracker.setCustomDimension(1, reg);
 	}
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
     	TelephonyManager tManager = (TelephonyManager)MainActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
     	String uid = tManager.getDeviceId(), reg=settings.getString("regno", "USERNAME");
     	extrasInit(reg, uid);
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
     	mTracker.sendView("/MainActivity");
     	tv=(TextView)findViewById(R.id.updateOn);
     	listViewSub=(ListView)findViewById(R.id.list);
     	isMainRunning=true;
     	trySave();
     	startUp();
     	checkStatus();
     }
     
     private void checkStatus() {
 		
     	new CheckStatusTask().execute();
 		
 	}
 	@Override
     protected void onPause() {
     	super.onPause();
     	isMainRunning=false;
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	isMainRunning=true;
     
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	isMainRunning=false;
     }
     
     @Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 	    getSupportMenuInflater().inflate(R.menu.menu, menu);
 	    return true;
 	}
 	@Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
     	
         switch (item.getItemId()) {
         	case R.id.about:
         		mTracker.sendEvent("ui_action", "button_press", "about", 0l);
         		mTracker.sendView("/MainActivity/About");
             	hs.showSupport(MainActivity.this);
         		return true;
         	
         	case R.id.refreshAtt:
         		mTracker.sendEvent("ui_action", "button_press", "refresh", 0l);
         		if(!settings.getBoolean("credentials", false))
 					loginDialog();
 				else
 					startLoadAttendance();
         		return true;
         	case R.id.details:
         		loginDialog();
 				return true;
         	case R.id.share:
         		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         		sharingIntent.setType("text/plain");
         		String shareBody = "Check your VIT Attendance on your android phone! http://play.google.com/store/apps/details?id=com.karthikb351.vitinfo2";
         		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
         		startActivity(Intent.createChooser(sharingIntent, "Make us famous via"));
         		mTracker.sendEvent("ui_action", "button_press", "share_button", 0l);
         		return true;
         	default:
         		return super.onOptionsItemSelected(item);
         }
     }
 	
     void loadSubjects()
     {
     	List listSub= new ArrayList();
     	DataHandler dat=new DataHandler(MainActivity.this);
     	int length=dat.getSubLength();
     	for(int i=0;i<length;i++)
     	{
     		listSub.add(dat.loadSubject(i));
     	}
     	listViewSub.setAdapter(new SubjectAdapter(MainActivity.this, R.layout.single_item_sub, listSub));
     	listViewSub.setOnItemClickListener(otcl);
     	long time=settings.getLong("updateOn", -1);
     	Time now=new Time();
     	now.setToNow();
     	String text=DateUtils.getRelativeTimeSpanString(time, now.toMillis(true), 
                 DateUtils.MINUTE_IN_MILLIS).toString();
     	
     	if(time!=-1)
     	{
     		tv.setText("Last refreshed "+text);
     	}
     	
     }
     void saveAttendance(String json)
     {
     	DataHandler dat=new DataHandler(MainActivity.this);
     	dat.saveAttendance(json);
     	editor=settings.edit();
     	editor.putBoolean("newuser", false);
     	editor.commit();
     	loadSubjects();
     }
     void startUp()
     {
     	if(settings.getBoolean("newuser", true))
     	{
     		Log.i("status","newguy");
     		if(settings.getBoolean("credentials", false))
     		{
     			startLoadAttendance();
     		}
     		else
     		{
     			Toast.makeText(MainActivity.this, "Please enter your credentials", Toast.LENGTH_SHORT).show();
     			loginDialog();
     		}
     	}
     	else
     	{
     		loadSubjects();
     	}
     	
     	
     }
      OnItemClickListener otcl=new OnItemClickListener() {
 	   
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
 		{
 			Intent a=new Intent(MainActivity.this, SubjectDetails.class);
 			a.putExtra("index", position);
 			startActivity(a);
 		}
 	};
     
 	
 	void loginDialog()
     {
 		Log.i("status", "inside logindialog()");
     	LayoutInflater inflator=getLayoutInflater();
         View popup=inflator.inflate(R.layout.details_popup, null);
         editor=settings.edit();
         final String loadregno=settings.getString("regno", " ");
         final int loaddiagyy=settings.getInt("dobyy", 1993);
         final int loaddiagmm=settings.getInt("dobmm", 2);
         final int loaddiagdd=settings.getInt("dobdd", 14);
         final EditText edit=(EditText)popup.findViewById(R.id.diagRegno);
     	final DatePicker dp=(DatePicker)popup.findViewById(R.id.datePicker);
     	if(!loadregno.equals(" "))
     		edit.setText(loadregno);
     	dp.updateDate(loaddiagyy, loaddiagmm, loaddiagdd);
     	AlertDialog.Builder builder;
         builder=new AlertDialog.Builder(this);
         builder.setCancelable(false);
         builder.setTitle("Login").setView(popup);
         builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	
             	int d,m,y;
             	REGNO=edit.getText().toString();
             	d=dp.getDayOfMonth();
             	m=dp.getMonth()+1;
             	y=dp.getYear();
             	DOB=dateFormat(d, m, y);
             	editor.putInt("dobdd", d).putInt("dobmm", m-1).putInt("dobyy", y);
             	if(loaddiagdd!=d||loaddiagmm!=m-1||loaddiagyy!=y||!loadregno.equals(REGNO))
             	{
             		editor.putBoolean("newuser", true);
             		mTracker.sendEvent("user_changed", loadregno, REGNO, 0l);
             	}
             	editor.commit();
             	diagLogin();
             }
         })
         .setNegativeButton("Cancel",null).show();
         
     }
 	boolean checkcredentials()
     {
     	boolean flag=false;
    	if(REGNO.length()==9)
    		if(REGNO.matches("(\\d)+(\\d)+([A-Z])+([A-Z])+([A-Z])+(\\d)+(\\d)+(\\d)")||REGNO.matches("(\\d)+(\\d)+([A-Z])+([A-Z])+([A-Z])+(\\d)+(\\d)+(\\d)+(\\d)"))
    			flag=true;
     	return flag;
     	
     }
 	void diagLogin()
     {
     	if(!checkcredentials())
     	{
     		Toast.makeText(this, "Invalid Registration Number!", Toast.LENGTH_SHORT).show();
     	}
     	else
     	{
     		editor.putString("regno", REGNO);
     		editor.putString("dob", DOB);
     		editor.putBoolean("credentials", true);
     		Log.i("staus","Valid credentials");
     		editor.commit();
     		if(settings.getBoolean("newuser", true))
     				startUp();
     	}
     }
 	
     String dateFormat(int d, int m, int y)
     {
     	String dd="",mm="",yy="";
     	if(d<10)
     		dd="0";
     	if(m<10)
     		mm="0";
     	dd+=String.valueOf(d);
     	mm+=String.valueOf(m);
     	yy=String.valueOf(y);
     	return dd+mm+yy;
     }
     void startSubmitCaptcha()
 	{
 		ArrayList<String> details = new ArrayList<String>();
 		details.add(0,regno);
 		details.add(1,dob);
 		details.add(2,captcha);
 		currentSCTask = new SubmitCaptchaTask();
 		currentSCTask.execute(details);
 		
 	}
     void startLoadAttendance()
 	{
 			regno=settings.getString("regno", " ");
 			dob=settings.getString("dob", " ");
 			currentTask=new LoadAttendanceTask();
 			ArrayList<String> details = new ArrayList<String>();
 			details.add(0,regno);
 			details.add(1,dob);
 			currentTask.execute(details);
 	}
 	void startCaptcha()
 	{
 		String regno=settings.getString("regno", " ");
 		String dob=settings.getString("dob", " ");
 		view= getLayoutInflater().inflate(R.layout.captcha_dialog, null);
 		builder= new AlertDialog.Builder(MainActivity.this);
 		imCaptcha=(ImageView)view.findViewById(R.id.captcha_img);
 		refresh = (Button)view.findViewById(R.id.captcha_refresh);
     	refresh.setOnClickListener(ocl);
 		builder.setView(view).setCancelable(false).setPositiveButton("Enter", diagocl).setNegativeButton("Cancel", diagocl).setTitle("Enter Captcha");
 		captchadialog= builder.create();
     	captchadialog.show();
     	currentLCTask=new DownloadImageTask(imCaptcha);
     	currentLCTask.execute(regno);
 	}
 android.content.DialogInterface.OnClickListener diagocl = new android.content.DialogInterface.OnClickListener() {
 		
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			
 			
 			switch(which)
 			{
 				case DialogInterface.BUTTON_POSITIVE:
 					captcha_edittext=(EditText)(view.findViewById(R.id.captcha_edittext));
 					captcha=captcha_edittext.getText().toString();
 					startSubmitCaptcha();
 					break;
 				case DialogInterface.BUTTON_NEGATIVE:
 					break;
 			
 			}
 			
 			
 		}
 	};
 	OnClickListener ocl=new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 		
 			if(currentLCTask.getStatus()==AsyncTask.Status.RUNNING)
 				Toast.makeText(MainActivity.this, "Still Loading", Toast.LENGTH_SHORT).show();
 			else
 			{
 				currentLCTask=new DownloadImageTask(imCaptcha);
 				currentLCTask.execute(regno);
 			}
 		}
 	};
 	
 	
 	private class CheckStatusTask extends AsyncTask<Void, Void, String>
 	{
 		ProgressDialog pdia;
 		Time start,stop;
 		protected void onPreExecute() {
 			
 			start=new Time();
 			start.setToNow();
 			if(statusExplicit)
 			{
 			  	pdia = new ProgressDialog(MainActivity.this);
 		        pdia.setMessage("Check our systems");
 		        pdia.setCancelable(false);
 		        pdia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		        pdia.show();
 			}
 	  }
 		@Override
 		protected String doInBackground(Void... params) {
 			String res="";
 			String url = "http://vitacademicsrel.appspot.com/status";
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(url);
 				HttpResponse response;
 				response = client.execute(request);
 				res=EntityUtils.toString(response.getEntity());
 				}
 			
 			catch (Exception e) {
 				res="error";
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 				}
 			return res;
 		}
 		@Override
 		protected void onPostExecute(String result) {
 			
 			stop=new Time();
 			stop.setToNow();
 			String msg_content=" ",msg_no=null,msg_title=" ", changelog=" ", appv=" ";
 			boolean latest=true,error=false;
 			if(result.equals("error"))
 			{
 				mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Update Check", "error");
 				error=true;
 			}
 			if(pdia!=null)	
 			{
 				if(pdia.isShowing())
 				{
 					pdia.dismiss();
 				}		
 			}
 				try {
 					JSONObject j=new JSONObject(result);
 					appv=j.get("version").toString();
 					changelog=j.get("changelog").toString();
 					msg_no=j.get("msg_no").toString();
 					msg_title=j.get("msg_title").toString();
 					msg_content=j.get("msg_content").toString();
 					if(Integer.parseInt(appv)>app_version)
 						latest=false;
 					} catch (JSONException e) {
 					error=true;
 					e.printStackTrace();
 				}
 			if(!error)
 			{
 				mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Update Check", "valid");
 				if(isMainRunning)
 				{
 					if(!latest)
 					{
 						AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
 						build.setTitle("Update Available");
 						build.setMessage("Please update to the latest version of the app.");
 						android.content.DialogInterface.OnClickListener ocl = new android.content.DialogInterface.OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								
 								switch(which)
 								{
 									case DialogInterface.BUTTON_POSITIVE:
 										Intent intent = new Intent(Intent.ACTION_VIEW);
 										intent.setData(Uri.parse("market://details?id=com.karthikb351.vitinfo2"));
 										startActivity(intent);
 										dialog.dismiss();
 										break;
 									case DialogInterface.BUTTON_NEGATIVE:
 										
 										android.content.DialogInterface.OnClickListener ocl1 = new android.content.DialogInterface.OnClickListener() {
 											
 											@Override
 											public void onClick(DialogInterface dialog, int which) {
 												
 												switch(which)
 												{
 													case DialogInterface.BUTTON_POSITIVE:
 														break;
 													case DialogInterface.BUTTON_NEGATIVE:
 														Intent intent = new Intent(Intent.ACTION_VIEW);
 														intent.setData(Uri.parse("market://details?id=com.karthikb351.vitinfo2"));
 														startActivity(intent);
 														dialog.dismiss();
 													default:
 														break;
 												}
 												
 											}
 										};
 										new AlertDialog.Builder(MainActivity.this)
 										.setMessage("We cannot guarantee the proper functioning of older versions of the app. Please update to ensure compatability with our servers")
 										.setPositiveButton("I Understand",ocl1)
 										.setNegativeButton("Update", ocl1)
 										.create().show();
 										break;
 									default:
 										break;
 								}
 								
 							}
 						};
 						build.setPositiveButton("Update", ocl);
 						build.setNegativeButton("Cancel", ocl);
 						build.create().show();
 					}
 					DataHandler dat = new DataHandler(MainActivity.this);
 					if(dat.checkIfNewMsg(msg_no))
 					{
 						AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
 						build.setTitle(msg_title);
 						build.setMessage(msg_content);
 						build.setCancelable(false);
 						build.setPositiveButton("Okay", null);
 						build.create().show();
 						dat.saveMsg(msg_no, msg_content);
 					}
 				}
 			}
 				
 		}
 	}
     
 	private class LoadAttendanceTask extends AsyncTask<ArrayList <String>, Void, String>
 	{
 		ProgressDialog pdia;
 		Time start, stop;
 		protected void onPreExecute() {
 			start=new Time();
 			start.setToNow();
 			attenCancelled=false;
 		  	pdia = new ProgressDialog(MainActivity.this);
 	        pdia.setMessage("Loading Attendance");
 	        pdia.setCancelable(true);
 	        pdia.setCanceledOnTouchOutside(false);
 	        pdia.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					attenCancelled=true;
 				}
 			});
 	        pdia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        pdia.show();
 	  }
 		@Override
 		protected String doInBackground(ArrayList <String>... params) {
 			String res="";
 			ArrayList <String> details=params[0];
 			String url = "http://vitacademicsrel.appspot.com/attj/"+details.get(0)+"/"+details.get(1);
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(url);
 				HttpResponse response;
 				response = client.execute(request);
 				res=EntityUtils.toString(response.getEntity());
 				}
 			
 			catch (Exception e) {
 				res="error";
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 				}
 			return res;
 		}
 		@Override
 		protected void onPostExecute(String result) {
 			
 			stop=new Time();
 			stop.setToNow();
 			if(attenCancelled)
 			{
 				Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
 				whoisnext=null;
 			}
 			else
 			{
 				
 				if(result.contains("timedout"))
 				{
 					Log.i("timedout",result);
 					mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Attendance", "timedout");
 					whoisnext="attendance";
 					startCaptcha();
 				}
 				else if(result.contains("valid"))
 				{
 					mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Attendance", "valid");
 					saveAttendance(result);
 					whoisnext=null;
 				}
 				else
 				{
 					mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Attendance", "error");
 					Log.e("error",result);
 					Toast.makeText(MainActivity.this, "Error fetching attendance", Toast.LENGTH_SHORT).show();
 				}
 			}
 			if (pdia.isShowing()) {
 				   pdia.dismiss();
 				}
 				
 		}
 	}
 
 
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 	  ImageView bmImage;
 	  ProgressDialog pdia;
 	  Time start, stop;
 	  public DownloadImageTask(ImageView bmImage) {
 	      this.bmImage = bmImage;
 	  }
 	
 	  protected void onPreExecute() {
 		  	start=new Time();
 		  	start.setToNow();
 		  	pdia = new ProgressDialog(MainActivity.this);
 	        pdia.setMessage("Fetching Captcha");
 	        pdia.setCancelable(true);
 	        pdia.setCanceledOnTouchOutside(false);
 	        pdia.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					captchaLoadCancelled=true;
 				}
 			});
 	        pdia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        pdia.show();
 	        captchaLoadCancelled=false;
 	  }
 	  protected Bitmap doInBackground(String... urls) {
 	      String urldisplay = "http://vitacademicsrel.appspot.com/captcha/"+urls[0];
 	      Bitmap mIcon11 = null;
 	      try {
 	    	  HttpClient client = new DefaultHttpClient();
 	    	  HttpGet request = new HttpGet(urldisplay);
 	    	  HttpResponse response;
 	    	  response = client.execute(request);
 	          HttpEntity entity=response.getEntity();
 	    	  byte [] content = convertInputStreamToByteArray(entity.getContent());
 	    	  mIcon11 = BitmapFactory.decodeByteArray(content, 0, content.length);
 	      } catch (Exception e) {
 	    	  mIcon11=null;
 	          Log.e("Error", e.getMessage());
 	          e.printStackTrace();
 	      }
 	      return mIcon11;
 	  }
 	
 	
 	  protected void onPostExecute(Bitmap result) {
 		  stop=new Time();
 		  stop.setToNow();
 		  if(!captchaLoadCancelled)
 		  {
 			  Display display = getWindowManager().getDefaultDisplay();
 			  @SuppressWarnings("deprecation")
 			  int width=(int)(display.getWidth()*0.6);
 			  int height=(int)(width*25/130);
 			  LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(width,height);
 			  parms.setMargins(10, 10, 10, 10);
 			  bmImage.setLayoutParams(parms);
 			  if(result==null)
 			  {
 				  mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Captcha Fetch", "error");
 				  Toast.makeText(MainActivity.this, "Error fetching Captcha. Try again.", Toast.LENGTH_SHORT).show();
 				  bmImage.setImageResource(R.drawable.ic_captcha_error);
 			  }
 			  else
 			  {
 				  mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Captcha Fetch", "valid");
 			      bmImage.setImageBitmap(result);
 			  }
 		  }
 		  else
 		  {
 		  	Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
 		  }
 		  if (pdia.isShowing()) {
 			   pdia.cancel();
 			}
 	  }
 	}
 
 
 	private class SubmitCaptchaTask extends AsyncTask<ArrayList <String>, Void, String>
 	{
 		ProgressDialog pdia;
 		Time start,stop;
 		protected void onPreExecute() {
 			start=new Time();
 			start.setToNow();
 		  	pdia = new ProgressDialog(MainActivity.this);
 	        pdia.setMessage("Submitting Captcha");
 	        pdia.setCancelable(true);
 	        pdia.setCanceledOnTouchOutside(false);
 	        pdia.setOnCancelListener(new DialogInterface.OnCancelListener() {
 				
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					captchaLoadCancelled=false;
 				}
 			});
 	        pdia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 	        pdia.show();
 	  }
 		@Override
 		protected String doInBackground(ArrayList <String>... params) {
 			String res="";
 			ArrayList <String> details=params[0];
 			String urldisplay = "http://vitacademicsrel.appspot.com/captchasub/"+details.get(0)+"/"+details.get(1)+"/"+details.get(2);
 			try {
 				HttpClient client = new DefaultHttpClient();
 				HttpGet request = new HttpGet(urldisplay);
 				HttpResponse response;
 				response = client.execute(request);
 				res=EntityUtils.toString(response.getEntity());
 				
 				}
 			
 			catch (Exception e) {
 				res="error";
 				Log.e("Error", e.getMessage());
 				e.printStackTrace();
 			}
 			return res;
 		}
 		
 		protected void onPostExecute(String result)
 		{
 			stop=new Time();
 			stop.setToNow();
 			boolean restart=false;
 			boolean attendance=false;
 			if(result.equals("error"))
 			{
 				mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Captcha Submit", "error");
 				Toast.makeText(MainActivity.this,"Error submitting captcha",Toast.LENGTH_SHORT).show();
 			}
 			else if(result.contains("timedout"))
 			{
 				restart=true;
 			}
 			else if(captchaSubmitCancelled)
 			{
 				Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
 			}
 			else if(result.contains("success"))
 			{
 				mTracker.sendTiming("Timings", (stop.toMillis(true)-start.toMillis(true)), "Captcha Submit", "valid");
 				if(whoisnext!=null)
 					if(whoisnext.equals("attendance"))
 						attendance=true;
 			}
 			else
 			{
 				Toast.makeText(MainActivity.this,"Oops. Something went wrong. Maybe your credentials are incorrect?",Toast.LENGTH_SHORT).show();
 			}
 			if(pdia.isShowing())
 				pdia.dismiss();
 			
 			if(restart)
 			{
 				currentLCTask=new DownloadImageTask(imCaptcha);
 				currentLCTask.execute(regno);
 			}
 			
 			if(attendance)
 			{
 				captchadialog.dismiss();
 				startLoadAttendance();
 			}
 				
 		}
 	}
 
 
 	public static byte[] convertInputStreamToByteArray(InputStream is) throws IOException
 	{
 		BufferedInputStream bis = new BufferedInputStream(is);
 		ByteArrayOutputStream buf = new ByteArrayOutputStream();
 		int result = bis.read();
 		while(result !=-1)
 		{
 				byte b = (byte)result;
 					buf.write(b);
 			result = bis.read();
 		}
 		return buf.toByteArray();
 	}
 }
