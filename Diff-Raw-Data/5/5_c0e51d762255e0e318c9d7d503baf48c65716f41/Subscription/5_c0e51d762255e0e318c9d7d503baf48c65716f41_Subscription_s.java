 package edu.gatech.oftentimes2000;
 
 import edu.gatech.oftentimes2000.gcm.GCMManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 
 public class Subscription extends Activity implements OnClickListener
 {
 	private final String TAG = "Subscription";
 	private CheckBox cbAdvertisement;
 	private CheckBox cbPSA;
 	private CheckBox cbEvent;
 	private ServerPinger pinger;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.subscription);
 		
 		// Init view
 		this.cbAdvertisement = (CheckBox) findViewById(R.id.cbSubscribeAdvertisement);
 		this.cbAdvertisement.setOnClickListener(this);
 		
 		this.cbPSA = (CheckBox) findViewById(R.id.cbSubscribePSA);
 		this.cbPSA.setOnClickListener(this);
 		
 		this.cbEvent = (CheckBox) findViewById(R.id.cbSubscribeEvent);
 		this.cbEvent.setOnClickListener(this);
 		
 		// Load settings
 		Context appContext = this.getApplicationContext();
 		SharedPreferences settings = appContext.getSharedPreferences(Settings.SETTING_PREFERENCE, Context.MODE_PRIVATE);
 		boolean advertisement = settings.getBoolean("sub_advertisement", false);
 		boolean psa = settings.getBoolean("sub_psa", false);
 		boolean event = settings.getBoolean("sub_event", false);
 		
 		this.cbAdvertisement.setChecked(advertisement);
 		this.cbPSA.setChecked(psa);
 		this.cbEvent.setChecked(event);
 	}
 
 	@Override
 	public void onClick(View view) 
 	{
 		Context appContext = this.getApplicationContext();
 		SharedPreferences settings = appContext.getSharedPreferences(Settings.SETTING_PREFERENCE, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		String subscription = "";
 		boolean delete = false;
 		switch (view.getId())
 		{
 			case R.id.cbSubscribeAdvertisement:
 				editor.putBoolean("sub_advertisement", this.cbAdvertisement.isChecked());
 				subscription = "advertisement";
 				if (!this.cbAdvertisement.isChecked())
 					delete = true;
 				break;
 			case R.id.cbSubscribePSA:
 				editor.putBoolean("sub_psa", this.cbPSA.isChecked());
 				subscription = "psa";
				if (!this.cbAdvertisement.isChecked())
 					delete = true;
 				break;
 			case R.id.cbSubscribeEvent:
 				editor.putBoolean("sub_event", this.cbEvent.isChecked());
 				subscription = "event";
				if (!this.cbAdvertisement.isChecked())
 					delete = true;
 				break;
 		}
 		
 		editor.commit();
 		
 		// Notify the server
 		try
 		{
 			if (this.pinger == null || this.pinger.getStatus() == AsyncTask.Status.FINISHED)
 			{
 				this.pinger = this.new ServerPinger();
 				
 				String q = "add";
 				if (delete)
 					q = "delete";
 				this.pinger.execute(new String[]{q, subscription});
 			}
 			else
 			{
 				Log.d(TAG, "An existing server pinger is running!");
 			}
 		}
 		catch (Exception e)
 		{
 			Log.e(TAG, e.getMessage());
 		}
 	}
 	
 	public class ServerPinger extends AsyncTask<String, Void, Void>
 	{
 		@Override
 		protected Void doInBackground(String... params) 
 		{
 			String q = params[0];
 			
 			if (q.equals("add"))
 				GCMManager.updateSubscription(Subscription.this, params[1]);
 			else if (q.equals("delete"))
 				GCMManager.deleteSubscription(Subscription.this, params[1]);
 			return null;
 		}
 	}
 }
