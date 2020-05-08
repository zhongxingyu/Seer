 package edu.ucsb.cs290.touch.to.text;
 
 import java.io.Serializable;
 
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.MenuItem;
 import edu.ucsb.cs290.touch.to.text.remote.Helpers;
 
 public abstract class AbstractNFCExchangeActivity extends KeyActivity {
 	private static final String RECEIVED = "received";
 	private IntentFilter[] intentFiltersArray;
 	private PendingIntent pendingIntent;
 	private static final String[][] mTechLists = new String[][] { new String[] { Ndef.class.getName() } };
 	private static final String SENT = "sent";
 	private NfcAdapter mAdapter;
 	
 	private byte[] toSend;
 	private byte[] received;
 	private NdefMessage message;
 	private boolean sent = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		parseIntent(getIntent());
 		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
 				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
 		try {
 			ndef.addDataType("*/*");
 		} catch (MalformedMimeTypeException e) {
 			throw new RuntimeException("fail", e);
 		}
 		if(savedInstanceState != null) {
 			if(savedInstanceState.containsKey(RECEIVED)) {
 				received = savedInstanceState.getByteArray(RECEIVED);
 			}
 			toSend = savedInstanceState.getByteArray(SENT);
 		}
 		setResult(RESULT_CANCELED);
 		intentFiltersArray = new IntentFilter[] { ndef, };
 		mAdapter = NfcAdapter.getDefaultAdapter(this);
 		setContentView(R.layout.activity_nfc_exchange);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		if(received != null) {
 			state.putByteArray(RECEIVED, received);
 		}
 		state.putByteArray(SENT, toSend);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void checkDone() throws Exception {
 		if (sent && (received != null)) {
 			recieveObject(Helpers.deserialize(received));
 			done();
 		}
 	}
 
 	@Override
	public void onNewIntent(Intent intent) {
 		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 		NdefMessage m = Ndef.get(tagFromIntent).getCachedNdefMessage();
 		received = m.getRecords()[0].getPayload();
 		try {
 			checkDone();
 		} catch (Exception e) {
 			Log.w("touch-to-text","Exception in key exchange!", e);
 			setResult(RESULT_CANCELED, new Intent());
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mAdapter.disableForegroundDispatch(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume(); 
 		mAdapter.enableForegroundDispatch(this, pendingIntent,
 				intentFiltersArray, mTechLists);
 	}
 
 	protected abstract void done();
 	
 	protected byte[] send() {
 		try {
 			return Helpers.serialize(sendObject());
 		} catch (Exception e) {
 			Log.wtf("wtf", "Error serializing object to be sent", e);
 		}
 		return null;
 	}
 	
 	protected abstract Serializable sendObject() throws Exception;
 	
 	protected abstract void recieveObject(Object o) throws Exception;
 	
 	protected abstract void parseIntent(Intent i);
 	
 	protected void onServiceConnected() {
 		Log.i("nfc", "Callbacks and NDEF Push set");
 		if(toSend == null) {
 			toSend = send();
 		}
 		message = new NdefMessage(new NdefRecord[] { new NdefRecord(
 				NdefRecord.TNF_UNKNOWN, new byte[0], new byte[0],
 				toSend)});
 		mAdapter.setNdefPushMessage(message, this);
 		mAdapter.setOnNdefPushCompleteCallback(new NfcAdapter.OnNdefPushCompleteCallback() {
 			
 			@Override
 			public void onNdefPushComplete(NfcEvent event) {
 				sent = true;
 				try {
 					checkDone();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}				
 			}
 		}, this);
 	}
 }
