 package fr.upsilon.inventirap;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class RequestActivity extends Activity {
 	private final int QRCODE_RESULT = 0;
 	
 	private Context context;
 	
 	private Runnable runnable;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.request_result);
         
         context = this;
         String decoded_value_internal = getString(R.string.DECODED_VALUE);
         final int decoded_value = getIntent().getExtras().getInt(decoded_value_internal);
         
         // get server ip
         String name = getResources().getString(R.string.app_name);
         SharedPreferences prefs = context.getSharedPreferences(name, MODE_PRIVATE);
         final String server_ip = prefs.getString(getString(R.string.SERVER_IP), "");
         
         runnable = new Runnable(){
             public void run() {
                 Log.d(context.getClass().getName(), "requesting to "+server_ip);
                /* String result = WebServicesTools.getXML(context, server_ip, decoded_value);*/
                 String result = "<root><name>element name</name>"+
                 				"<id>123</id>"+
                 				"<centrePayeur>UPS</centrePayeur>"+
                 				"</root>";
                 
                 if (!WebServicesTools.XMLFromString(result)) {
                 	Toast t = Toast.makeText(context, R.string.xml_error, Toast.LENGTH_LONG);
                 }
                 Log.d(context.getClass().getName(), "XML received and decoded !!");
 
                 Intent intent = new Intent(context, DisplayResultActivity.class);
                 startActivity(intent);
                 
                finish();
                
             }
         };
         Thread thread =  new Thread(null, runnable, "InventirapServerRequest");
         thread.start();
 
     }
 }
