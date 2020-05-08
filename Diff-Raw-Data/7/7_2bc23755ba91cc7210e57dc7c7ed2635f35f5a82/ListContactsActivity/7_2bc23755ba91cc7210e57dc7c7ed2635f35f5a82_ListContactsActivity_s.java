 package android.tdd.contacts;
 
 import java.util.ArrayList;
 
 import org.springframework.web.client.RestTemplate;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 public class ListContactsActivity extends Activity {
 
     private static String TAG = "contacts";
 
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after
      * previously being shut down then this Bundle contains the data it most
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(TAG, "onCreate");
         setContentView(R.layout.main);
         
         ProgressDialog.show(this, null, getString(R.string.loading_contacts));
         
         loadContacts();
     }
 
 	private void loadContacts() {
 		new AsyncTask<Void, Void, ArrayList>() {
 			@Override
 			protected ArrayList doInBackground(Void... arg0) {
 				String contactsUrl = "http://m.concretesolutions.com.br/extranet/contact/list";
 				return new RestTemplate().getForObject(contactsUrl, ArrayList.class);
 			}
 		}.execute();
 	}
 }
 
