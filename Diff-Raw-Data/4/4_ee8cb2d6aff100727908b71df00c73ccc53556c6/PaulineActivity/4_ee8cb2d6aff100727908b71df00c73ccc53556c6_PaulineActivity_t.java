 package fr.utc.assos.payutc;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import fr.utc.assos.payutc.soap.PBuy;
 
 /**
  * Demande au seller de badger
  * @author thomas
  *
  */
 public class PaulineActivity extends BaseActivity {
 	public static final String LOG_TAG			= "PaulineActivity";
 	
 	public static final int ASKSELLERPASSWORD	= 0;
 	
 	public final static int ID_POI				= 2;
 	public final static int ID_FUNDATION		= 2;
 	public static final int MEAN_OF_LOGIN		= 5; 
 	
 	public static final String ID_TRECOUVR			= "5B1BF88B";
 	
 	public static final PBuy PBUY = new PBuy();
 	
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(LOG_TAG, "onCreate PaulineActivity");
         setContentView(R.layout.main);
         // @todo virer ce vieux hack
     	//startAskSellerPasswordActivity(ID_TRECOUVR);
     	
         if (!identifierIsAvailable()) {
         	startAskSellerPasswordActivity(ID_TRECOUVR);
         	//startHomeActivity();
         	/*
         	PBUY.loadSeller("trecouvr", 1, "", PaulineActivity.ID_POI);
         	PBUY.loadBuyer("trecouvr", 1, "");
         	Intent intent = new Intent(this, fr.utc.assos.payutc.ShowArticleActivity.class);
         	Bundle b = new Bundle();
         	intent.putExtras(b);
         	startActivity(intent);//*/
         }
     }
 
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(LOG_TAG, "requestCode:"+requestCode+" ,resultCode:"+resultCode);
 		switch (requestCode) {
 		case ASKSELLERPASSWORD:
 			if (resultCode == RESULT_OK) {
				startHomeActivity();
 			}
 		}
     }
     
     @Override
     protected void onIdentification(String id) {
     	Log.d(LOG_TAG, "onIdentification");
         startAskSellerPasswordActivity(id);
     }
 
     
     private void startAskSellerPasswordActivity(String id) {
     	Log.d(LOG_TAG,"startAskSellerPassword");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.AskSellerPasswordActivity.class);
     	Bundle b = new Bundle();
     	b.putString("id", id); //Your id
     	intent.putExtras(b); //Put your id to your next Intent
     	startActivityForResult(intent, ASKSELLERPASSWORD);
     }
     
     public void startHomeActivity() {
     	Log.d(LOG_TAG,"startHomeActivity");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.HomeActivity.class);
     	startActivity(intent);
     }
     
 
 }
