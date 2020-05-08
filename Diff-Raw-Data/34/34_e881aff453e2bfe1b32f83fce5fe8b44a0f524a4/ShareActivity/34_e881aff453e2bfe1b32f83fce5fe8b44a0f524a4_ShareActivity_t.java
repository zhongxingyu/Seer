 package com.teamarks.android;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.preference.PreferenceManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ShareActivity extends Activity {
 	
 	private String title;
 	private String url;
 	private String text;
 
 	private EditText etShareTitle;
 	private EditText etShareLink;
 	private EditText etShareText;
 	private TextView tvShareTitle;
 	private TextView tvShareLink;
 	private TextView tvShareText;
 	private Button	btShare;
 	
 	public SharedPreferences setting;
 	public String username;
 	public String apikey;
 	
 	ArrayList<NameValuePair> postParameters;
 	
	public static String endpoint ="http://api.teamarks.com/v1/share.xml?";
 	
 	
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_share);
         createWidget();
         String strVer=this.GetSystemVersion();
         strVer=strVer.substring(0,3).trim();
         float fv=Float.valueOf(strVer);
         if(fv>2.3)
         {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
         .detectAll() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
         .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
         .detectLeakedSqlLiteObjects() //探测SQLite数据库操作
         .penaltyDeath()
         .build()); 
         }
         Intent shareIntent = getIntent();
         url =ShareUtils.getBody(shareIntent);
         title = ShareUtils.getSubject(shareIntent);
 
 
         etShareTitle.setText(title);
         etShareLink.setText(url);
         
     }
 
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_share, menu);
         return true;
     }
     
 
     public void createWidget(){
     	
     		
     		etShareTitle = (EditText)findViewById(R.id.et_share_title);
     		etShareLink = (EditText)findViewById(R.id.et_share_link);    	
     		etShareText = (EditText)findViewById(R.id.et_share_text);
//    		etShareText.setHint(getText(R.string.texthint));
     		
    		etShareText.setText(getText(R.string.defaulttext));
     		tvShareTitle = (TextView)findViewById(R.id.tv_share_title);
     		tvShareTitle = (TextView)findViewById(R.id.tv_share_link);
     		tvShareTitle = (TextView)findViewById(R.id.tv_share_text);
     		
     		btShare = (Button)findViewById(R.id.bt_share);
     		setting = PreferenceManager.getDefaultSharedPreferences(this);
     		username = setting.getString("username", null);
     		apikey = setting.getString("apikey", null);
     		btShare.setOnClickListener(new OnClickListener() 
     		{
 				
 				private String queryEncode;
 
 				public void onClick(View v) {
 					
 
					text = etShareText.getText().toString();
 
 				    postParameters = new ArrayList<NameValuePair>();
				    postParameters.add(new BasicNameValuePair("userid", username));
 				    postParameters.add(new BasicNameValuePair("apikey", apikey));							
 				    postParameters.add(new BasicNameValuePair("url", url));		
 				    postParameters.add(new BasicNameValuePair("title", title));		
 				    postParameters.add(new BasicNameValuePair("text", text));		
 
 //					Toast.makeText(getApplicationContext(), url.toString(), Toast.LENGTH_LONG).show();		
 //					TeamarksService.shareUrl(url.toString());
 
 					TeamarksService ts = new TeamarksService();
 					try {
 						ts.shareUrl(endpoint,postParameters,"UTF-8");
 					} catch (UnsupportedEncodingException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
     }
     public static String GetSystemVersion()
     {
     return android.os.Build.VERSION.RELEASE;
     }
 }
