 package edu.jhu.isi.hms.visavis;
 
 import java.util.Date;
 import com.google.gson.*;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 
 import org.jivesoftware.smack.*;
 import org.jivesoftware.smack.packet.Message;
 
 public class VisavisActivity extends Activity {
 	 VerifierTask vt;
 	 public static  final String data_tag = "StdTestResult";
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Resources res = getResources();
 
 	    final StdTestResult t = new StdTestResult();
 	    t.tests.add(new StdTest("AIDS", new Date(), true));
 	    t.tests.add(new StdTest("Baby", new Date(), true));
 	    
 	    Gson gson = new Gson();
 	    String json = gson.toJson(t);
 	    Log.v("asd", json);
 	    TextView v = (TextView)this.findViewById(R.id.t);
 	    Toast.makeText(this.getApplicationContext(), json, Toast.LENGTH_SHORT);
 	    if(v != null){
 	    	v.setText(json);
 	    }
 	    //((TextView) this.findViewById(R.id.t)).setText(json);
 		vt = new VerifierTask("ian",res.getString(R.string.username),res.
 				getString(R.string.password), 
 				res.getString(R.string.XMPPServer),
 				this);
 		
 		setContentView(R.layout.main);
 		final Button share = (Button) findViewById(R.id.share);
 		share.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Log.v("STD", "changing intents");
 				Intent i = new Intent(v.getContext(), StatusActivity.class);
 				Bundle b = new Bundle();
				b.putSerializable(data_tag, t);
				i.putExtras(b);
 				startActivity(i);
 			}
 
 		});
 		Log.v("STD", "adding button event");
 		final Button send = (Button) findViewById(R.id.notify);
 		send.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
				if(!vt.getStatus().equals(AsyncTask.Status.PENDING)){
					Log.v("STD", "background task already running");
					return;
				}
 				Log.v("STD", "starting background task");
 				vt.execute();
 			}
 
 		});
 	}
 }
