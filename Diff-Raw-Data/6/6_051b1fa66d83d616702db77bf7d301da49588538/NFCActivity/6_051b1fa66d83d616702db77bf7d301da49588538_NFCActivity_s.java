 package com.siteduzero.android.nfc;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.siteduzero.android.R;
 
 public class NFCActivity extends Activity {
 	private NfcAdapter mNfcAdapter;
 	private TextView mTextView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_nfc);
 		this.mTextView = (TextView) findViewById(R.id.textView1);
 		this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 
 		if (mNfcAdapter == null || !mNfcAdapter.isEnabled()) {
 			Toast.makeText(this, R.string.text_no_nfc, Toast.LENGTH_SHORT)
 					.show();
 			finish();
 			return;
 		}
 
 		if (getIntent() != null) {
 			resolveIntent(getIntent());
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Intent intent = new Intent(this, this.getClass())
 				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
 		IntentFilter[] filters = null;
 		String[][] techListArray = null;
 		mNfcAdapter.enableForegroundDispatch(this, pIntent, filters,
 				techListArray);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mNfcAdapter.disableForegroundDispatch(this);
 	}
 
 	private void resolveIntent(Intent intent) {
 		String action = intent.getAction();
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
 			Parcelable[] rawMsgs = intent
 					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 			if (rawMsgs != null) {
 				NdefMessage[] messages = new NdefMessage[rawMsgs.length];
 				for (int i = 0; i < rawMsgs.length; i++) {
 					messages[i] = (NdefMessage) rawMsgs[i];
 				}
 				String str = new String(
 						messages[0].getRecords()[0].getPayload());
 				mTextView.setText(str);
 			}
 		}
 	}
 }
