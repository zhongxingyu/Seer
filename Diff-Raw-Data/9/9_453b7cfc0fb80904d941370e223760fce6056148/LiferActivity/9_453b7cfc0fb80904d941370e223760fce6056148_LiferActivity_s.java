 package dude.morrildl.lifer;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMRegistrar;
 
 import dude.morrildl.lifer.gcm.GCMIntentService;
 import dude.morrildl.lifer.providence.EventHistoryFragment;
 import dude.morrildl.lifer.providence.LatestEventFragment;
 
 public class LiferActivity extends Activity implements
 		ActionBar.OnNavigationListener {
 	private LatestEventFragment latestEventFragment = null;
 	private EventHistoryFragment eventHistoryFragment = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction ft = fm.beginTransaction();
 		latestEventFragment = new LatestEventFragment();
 		eventHistoryFragment = new EventHistoryFragment();
 		ft.add(R.id.main_container, latestEventFragment);
 		ft.add(R.id.main_container, eventHistoryFragment);
 		ft.show(latestEventFragment).hide(eventHistoryFragment);
 		ft.commit();
 
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId == null || "".equals(regId)) {
 			GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
 		}
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this,
 				R.array.providence_action_list,
 				android.R.layout.simple_spinner_dropdown_item);
 		actionBar.setListNavigationCallbacks(spinnerAdapter, this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.providence, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_test_1:
 			Toast.makeText(this, R.string.menu_test_1, Toast.LENGTH_SHORT)
 					.show();
 			return true;
 		case R.id.menu_test_2:
 			Toast.makeText(this, R.string.menu_test_2, Toast.LENGTH_SHORT)
 					.show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		String regId = GCMRegistrar.getRegistrationId(this);
 		if (regId != null && !"".equals(regId)) {
 			if (!GCMRegistrar.isRegisteredOnServer(this)) {
 				new ServerRegisterTask(this).execute(regId);
 			}
 		}
 	}
 
 	static class ServerRegisterTask extends AsyncTask<String, Integer, String> {
		private static final String SERVER_URL = "http://compatriot:4280/regid";
 		private Context context;
 
 		@SuppressWarnings("unused")
 		private ServerRegisterTask() {
 		}
 
 		public ServerRegisterTask(Context context) {
 			this.context = context;
 		}
 
 		@Override
 		protected String doInBackground(String... regIds) {
 			String regId = regIds[0];
 			URL url;
 			try {
 				url = new URL(SERVER_URL);
 			} catch (MalformedURLException e) {
 				Log.e("doInBackground", "URL error", e);
 				return null;
 			}
 			HttpURLConnection cxn = null;
 			try {
 				cxn = (HttpURLConnection) url.openConnection();
 				cxn.setDoInput(true);
 				cxn.setRequestMethod("POST");
 				OutputStream os = cxn.getOutputStream();
 				os.write(regId.getBytes());
 				os.close();
 				cxn.getInputStream().close();
 			} catch (IOException e) {
 				Log.e("doInBackground", "transmission error", e);
 				return null;
 			} finally {
 				if (cxn != null)
 					cxn.disconnect();
 			}
 			return regId;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			Log.e("onPostExecute", "null result :(");
 			if (result != null)
 				GCMRegistrar.setRegisteredOnServer(context, true);
 		}
 	}
 
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		FragmentManager fm = getFragmentManager();
 		switch (itemPosition) {
 		case 0:
 			fm.beginTransaction().hide(eventHistoryFragment)
 					.show(latestEventFragment).commit();
 			break;
 		case 1:
 			fm.beginTransaction().hide(latestEventFragment)
 					.show(eventHistoryFragment).commit();
 			break;
 		default:
 			// uh oh
 			Log.e("onNavigationItemSelection",
 					"called with unknown itemPosition");
 			fm.beginTransaction().hide(eventHistoryFragment)
 					.show(latestEventFragment).commit();
 		}
 
 		return false;
 	}
 }
