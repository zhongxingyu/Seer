 package ch.novapp.rfidpos;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	TextView resultView;
 	TextView idView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		resultView = (TextView) findViewById(R.id.trxResult);
 		idView = (TextView) findViewById(R.id.scannedId);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	public void onResume() {
 		super.onResume();
 		handleIntent(getIntent());
 		setGreedyTagDetection(true);
 	}
 
 	public void onPause() {
 		/** disable foreground events **/
 		setGreedyTagDetection(false);
 		super.onPause();
 	}
 
 	public void scanTag(View view) {
 		/* disable then enable */
 		setGreedyTagDetection(false);
 		setGreedyTagDetection(true);
 		idView.setText("");
 		resultView.setText("scan next tag now"); 
 	}
 	
 	public void showResult(String result) {
 		resultView.setText(result);
 	}
 
 	public void onNewIntent(Intent intent) {
		// avoid calling handleIntent for the same intent that started the activity
		// this can happen if onResume is called while we are requesting foreground
		// intent delivery. Not sure why this happens (it shouldn't) but it does.
		if(!intent.equals(getIntent()))
			handleIntent(intent);
 	}
 	
 	private void handleIntent(Intent intent) {
 		/** see if there was an tag detected to get us started **/
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
 				|| NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
 				|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
 			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 			handleTag(tag);
 		}
 	}
 
 	private void setGreedyTagDetection(boolean enabled) {
 		NfcAdapter adapter = NfcAdapter.getDefaultAdapter(getBaseContext());
 
 		if (enabled) {
 			PendingIntent intent = PendingIntent.getActivity(this, 0,
 					new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 			adapter.enableForegroundDispatch(this, intent, null, null);
 
 		} else {
 			adapter.disableForegroundDispatch(this);
 		}
 	}
 
 	private void handleTag(Tag tag) {
 		/** extract the id **/
 		StringBuilder id = new StringBuilder();
 		for (byte b : tag.getId()) {
 			id.append(String.format("%02x", b));
 		}
 
 		/** display the id **/
 		idView.setText(id);
 		resultView.setText("...");
 
 		/** create a transaction in VendHQ **/
 		CardIdentifiedReporter reporter = new CardIdentifiedReporter();
 		reporter.execute(id.toString(), this);
 	}
 }
