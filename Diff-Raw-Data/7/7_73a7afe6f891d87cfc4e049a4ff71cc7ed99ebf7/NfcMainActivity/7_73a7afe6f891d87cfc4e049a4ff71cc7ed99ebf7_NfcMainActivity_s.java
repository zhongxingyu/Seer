 package sk.xeito.nfc;
 
 import java.nio.charset.Charset;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.nfc.NfcEvent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class NfcMainActivity extends Activity implements CreateNdefMessageCallback {
 
	private static final String NFC_AAR = "sk.xeito.nfc.beam";
 	private static final String NFC_MIME_TYPE = "application/vnd.sk.xeito.nfc.beam";
 
 	private NfcAdapter nfcAdapter;
 	private TextView label;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		label = (TextView) findViewById(R.id.label);
 
 		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
 		if (nfcAdapter == null) {
 			label.setText("NFC not supported");
 		}
 		else {
 			label.setText("NFC is supported");
 			nfcAdapter.setNdefPushMessageCallback(this, this);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public NdefMessage createNdefMessage(NfcEvent event) {
 		String text = "Hello from Bratislava";
 
 		NdefMessage nfcMsg = new NdefMessage(
 			new NdefRecord[] {
 				createMime(NFC_MIME_TYPE, text.getBytes()),
//				NdefRecord.createApplicationRecord(NFC_AAR),
 			}
 		);
 
 		return nfcMsg;
 	}
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Intent intent = getIntent();
 
 		// Check to see that the Activity started due to an Android Beam
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
 			processIntent(intent);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		// onResume gets called after this to handle the intent
 		setIntent(intent);
 	}
 
 	/**
 	 * Parses the NDEF Message from the intent and prints to the TextView
 	 */
 	void processIntent(Intent intent) {
 		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		// only one message sent during the beam
 		NdefMessage msg = (NdefMessage) rawMsgs[0];
 		// record 0 contains the MIME type, record 1 is the AAR, if present
 		label.setText(new String(msg.getRecords()[0].getPayload()));
 	}
 
 	private static final Charset US_ASCII = Charset.forName("US-ASCII");
 	private static NdefRecord createMime(String mimeType, byte [] mimeData) {
 		if (mimeType == null) throw new NullPointerException("mimeType is null");
 
 		// We only do basic MIME type validation: trying to follow the
 		// RFCs strictly only ends in tears, since there are lots of MIME
 		// types in common use that are not strictly valid as per RFC rules
 		mimeType = Intent.normalizeMimeType(mimeType);
 		if (mimeType.length() == 0) throw new IllegalArgumentException("mimeType is empty");
 		int slashIndex = mimeType.indexOf('/');
 		if (slashIndex == 0) throw new IllegalArgumentException("mimeType must have major type");
 		if (slashIndex == mimeType.length() - 1) {
 			throw new IllegalArgumentException("mimeType must have minor type");
 		}
 		// missing '/' is allowed
 
 		// MIME RFCs suggest ASCII encoding for content-type
 		byte [] typeBytes = mimeType.getBytes(US_ASCII);
 		return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, null, mimeData);
 	}
 }
