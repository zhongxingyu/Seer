 /**
  * @version $Id: NFCReaderWriterDemo.java 168 2012-08-09 09:14:39Z mroland $
  *
  * @author Michael Roland <mi.roland@gmail.com>
  *
  * Copyright (c) 2012 Michael Roland
  *
  * ALL RIGHTS RESERVED.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name Michael Roland nor the names of any contributors
  *       may be used to endorse or promote products derived from this software
  *       without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL MICHAEL ROLAND BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package androface.nfc.android.apps.nfcreaderwriter;
 
 import java.nio.charset.Charset;
 import java.util.Arrays;
 
 import androface.nfc.ndef.UriRecordHelper;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcManager;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 /**
  * Utilities for URI record encoding.
  */
 public class NFCReaderWriterDemo extends Activity {
 	private static final String ACTION_USB_PERMISSION = 
 			"androface.nfc.android.apps.nfcreaderwriter.USB_PERMISSION";
 	// USB Ǿ  ̺Ʈ .
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (ACTION_USB_PERMISSION.equals(action)) {
 				    // ڿ Android Accessory Protocol   Ǹ
 				    //    ̾α׿     ޴´.
 					synchronized (this) {
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 					if (intent.getBooleanExtra(
 							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
 						//  
 						showMessage("receiver : USB Host .");
 					} else {
 						Log.d(NFCReaderWriterDemo.class.getName(), 
 								"permission denied for accessory "
 								+ accessory);
 					}
 					
 					openAccessory(accessory);
 					//    ޾ ǥ
 					mPermissionRequestPending = false;
 				}
 			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				// Android Accessory Protocol    Ǿ 
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				//  ϰ ִ    Ȯ
 				if (accessory != null && accessory.equals(mAccessory)) {
 					showMessage("USB Host  .");
 					closeAccessory();
 					
 					
 				}
 			}
 		}
 	};
 	
 	
 // TODO: Part 1/Step 4: Creating our Activity
     private static final int DIALOG_WRITE_URL = 1;
     private EditText mMyUrl;
     private Button mMyWriteUrlButton;
     private boolean mWriteUrl = false;
     
 // TODO: Part 1/Step 6: Foreground Dispatch (Detect Tags)
     private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
     private NfcAdapter mNfcAdapter;
 
 // TODO: Part 2/Step 1: A Simple Dialog Box: Detected a Tag
     private static final int DIALOG_NEW_TAG = 3;
     private static final String ARG_MESSAGE = "message";
 
     //ADKs
 	private TextView txtMsg;
 	private UsbManager mUsbManager;
 	private UsbAccessory mAccessory;
 	private PendingIntent mPermissionIntent;
 	private boolean mPermissionRequestPending;
 	private ToggleButton btnLed;
     private AdkHandler handler;
     private boolean isChecked = false;
     private static final int AdkCommandStr = 4;
     
     // get specific string from string.xml
     //private final String keyCode = getResources().getString(R.string.keyCode);
     private String keyCode = "Project AndroFace | Yunsu Choi";
     private TextView keyCodeView;
     
     //Sound.
     SoundPool pool;
 	    int wheatley_hello;
 	    //int wheatley_disappointing;
 	    int turret_hello;
 	    int turret_dispense;
 	    int turret_disable;
     
     /**
      * Called when the activity is first created.
      */
     
     private Handler mHandler = new Handler() {
     	@Override
     	public void handleMessage(Message msg) {
     		if(msg.what==0) {
 	    		if(handler != null && handler.isConnected()){
 	        		btnLed.setChecked(true);
 	        		//handler.write((byte)0x1, (byte)0x0, (int) 1);
 	        		long starttime = System.currentTimeMillis(), curtime = 0;
 	        		
 	        		while(true) {
 	        			curtime = System.currentTimeMillis();
 	        			if(curtime-starttime>5000) {
 	        				btnLed.setChecked(false);
 	        				break;
 	        			}
 	        		}
 	        		
 	    		}		
     		} else if(msg.what==1) {
             	if(handler !=null && handler.isConnected()){
             		btnLed.setChecked(false);
 //            		handler.write((byte)0x1, (byte)0x0, (int) 0);
             		
             	}
     		}
     		
     	}
     };
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         txtMsg = (TextView)this.findViewById(R.id.txtMsg);
         btnLed = (ToggleButton)this.findViewById(R.id.btnLed); // ư
         keyCodeView =(TextView)this.findViewById(R.id.keyCodeView);
         
         // Testing Sound.
         pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
 	        wheatley_hello = pool.load(this, R.raw.wheatley_hello, 1);
 	        turret_hello = pool.load(this, R.raw.turret_autosearch_1 ,1);
 	        turret_dispense = pool.load(this, R.raw.turret_dispensing ,1);
 	        turret_disable = pool.load(this, R.raw.turret_disabled_2 ,1);
 	        
         //Android Accessory Protocol   ῡ  εĳƮ ù 
       	IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
       	filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
       	registerReceiver(mUsbReceiver, filter);
       	
       	mUsbManager = UsbManager.getInstance(this);
 		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
 				ACTION_USB_PERMISSION), 0);
 		
         btnLed.setOnCheckedChangeListener(new OnCheckedChangeListener(){
         	@Override
             public void onCheckedChanged(CompoundButton buttonView,
                                      boolean isChecked) {
 				if(handler != null && handler.isConnected()){  
                       handler.write((byte)0x1, (byte)0x0, isChecked ? 1 : 0);
                       showMessage("AndroFace " + (isChecked ? "On" : "Off"));
                  }
             }});
         
 // TODO: Part 1/Step 4: Creating our Activity
         mMyUrl = (EditText) findViewById(R.id.myUrl);
         mMyWriteUrlButton = (Button) findViewById(R.id.myWriteUrlButton);
 
         // Set action for "Write URL to tag..." button:
         mMyWriteUrlButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mWriteUrl = true;
                 NFCReaderWriterDemo.this.showDialog(DIALOG_WRITE_URL);
             }
         });
 
 // TODO: Part 3/Step 2: Handle NDEF_DISCOVERED Intent
         // Resolve the intent that started us:
         resolveIntent(this.getIntent(), false);
     }
 
     /**
      * Called when the activity gets focus.
      */
     @Override
     public void onResume() {
         super.onResume();
      //  ȭ鿡   ȵ̵  Android Accessory Protocol 
      		//  USB Host Ǿ ִ Ȯ
      		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
      		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
      		if (accessory != null) { // Android Accessory Protocol   ã 
      			if (mUsbManager.hasPermission(accessory)) {
      				showMessage("onresume : USB Host .");
      				openAccessory(accessory);
      				
      				//sound
      				//pool.play(wheatley_hello, 1, 1, 0, 0, 1);
      				
      			} else {
      				synchronized (mUsbReceiver) {
      					if (!mPermissionRequestPending) {
      						mUsbManager.requestPermission(accessory,
      								mPermissionIntent); // USB    ص Ǵ ڿ 
      						mPermissionRequestPending = true; //   ڵ带  ǥ
      					}
      				}
      			}
      		} else {
      			Log.d(NFCReaderWriterDemo.class.getName(), "mAccessory is null");
      		}
      		
 // TODO: Part 1/Step 6: Foreground Dispatch (Detect Tags)
         // Retrieve an instance of the NfcAdapter:
         NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
         mNfcAdapter = nfcManager.getDefaultAdapter();
 
         // Create a PendingIntent to handle discovery of Ndef and NdefFormatable tags:
         PendingIntent pi = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
 
         // Enable foreground dispatch for Ndef and NdefFormatable tags:
         mNfcAdapter.enableForegroundDispatch(
                 this,
                 pi,
                 new IntentFilter[]{ new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) },
                 new String[][]{
                     new String[]{"android.nfc.tech.NdefFormatable"},
                     new String[]{"android.nfc.tech.Ndef"}
                 });
     }
 
     /**
      * Called when the activity loses focus.
      */
     @Override
     public void onPause() {
         super.onPause();
         closeAccessory();
 
 // TODO: Part 1/Step 7: Cleanup Foreground Dispatch
         // Disable foreground dispatch:
         mNfcAdapter.disableForegroundDispatch(this);
     }
 
 // TODO: Part 3/Step 2: Handle NDEF_DISCOVERED Intent
     /**
      * Called when activity receives a new intent.
      */
     @Override
     public void onNewIntent(Intent data) {
         resolveIntent(data, false);
     }
 
 // TODO: Part 1/Step 8: Receive Foreground Dispatch Intent
     /**
      * Called when a pending intent returns (e.g. our foreground dispatch).
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case PENDING_INTENT_TECH_DISCOVERED:
                 resolveIntent(data, true);
                 break;
         }
     }
 
 // TODO: Part 1/Step 8: Receive Foreground Dispatch Intent
     /**
      * Method to handle any intents that triggered our activity.
      * @param data                The intent we received and want to process.
      * @param foregroundDispatch  True if this intent was received through foreground dispatch.
      */
     private void resolveIntent(Intent data, boolean foregroundDispatch) {
         String action = data.getAction();
 
 // TODO: Part 3/Step 2: Handle NDEF_DISCOVERED Intent
         // We were started from the recent applications history: just show our main activity
         if ((data.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) { return; }
         
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                 || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
     
 // TODO: Part 1/Step 8: Receive Foreground Dispatch Intent
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
             Tag tag = data.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 
             if (foregroundDispatch && mWriteUrl) {
                 // This is a foreground dispatch and we want to write our URL
                 mWriteUrl = false;
 
 // TODO: Part 1/Step 9: Prepare our URI NDEF Message
                 // Get URL from text box:
                 String urlStr = mMyUrl.getText().toString();
 
                 // Convert to URL to byte array (UTF-8 encoded):
                 byte[] urlBytes = urlStr.getBytes(Charset.forName("UTF-8"));
 
                 //// Use UriRecordHelper to determine an efficient encoding:
                 //byte[] urlPayload = new byte[urlBytes.length + 1];
                 //urlPayload[0] = 0;  // no prefix reduction
                 //System.arraycopy(urlBytes, 0, urlPayload, 1, urlBytes.length);
 
                 // Better: Use UriRecordHelper to determine an efficient encoding:
                 byte[] urlPayload = UriRecordHelper.encodeUriRecordPayload(urlStr);
 
                 // Create a NDEF URI record (NFC Forum well-known type "urn:nfc:wkt:U")
                 NdefRecord urlRecord = new NdefRecord(
                         NdefRecord.TNF_WELL_KNOWN, /* TNF: NFC Forum well-known type */
                         NdefRecord.RTD_URI,        /* Type: urn:nfc:wkt:U */
                         new byte[0],               /* no ID */
                         urlPayload);
 
                 // Create NDEF message from URI record:
                 NdefMessage msg = new NdefMessage(new NdefRecord[]{urlRecord});
 
 // TODO: Part 1/Step 10: Write NDEF to Preformatted Tag
                 // Write NDEF message to tag:
 
                 Ndef ndefTag = Ndef.get(tag);
                 if (ndefTag != null) {
                     // Our tag is already formatted, we just need to write our message
 
                     try {
                         // Connect to tag:
                         ndefTag.connect();
 
                         // Write NDEF message:
                         ndefTag.writeNdefMessage(msg);
                     } catch (Exception e) {
                     } finally {
                         // Close connection:
                         try { ndefTag.close(); } catch (Exception e) { }
                     }
                 } else {
 
 // TODO: Part 1/Step 11: Write NDEF to Unformatted Tag
                     NdefFormatable ndefFormatableTag = NdefFormatable.get(tag);
                     if (ndefFormatableTag != null) {
                         // Our tag is not yet formatted, we need to format it with our message
 
                         try {
                             // Connect to tag:
                             ndefFormatableTag.connect();
 
                             // Format with NDEF message:
                             ndefFormatableTag.format(msg);
                         } catch (Exception e) {
                         } finally {
                             // Close connection:
                             try { ndefFormatableTag.close(); } catch (Exception e) { }
                         }
                     }
 
 // TODO: Part 1/Step 10: Write NDEF to Preformatted Tag
                 }
 
 // TODO: Part 1/Step 8: Receive Foreground Dispatch Intent
                 dismissDialog(DIALOG_WRITE_URL);
 
 // TODO: Part 2/Step 2: Detect the Tag
             } else {
                 // Let's read the tag whenever we are not in write mode
               
                 // Retrieve information from tag and display it
                 StringBuilder tagInfo = new StringBuilder();
             	String tagUriData;
             	
                 // Get tag's UID:
                 byte[] uid = tag.getId();
                 //*tagInfo.append("UID: ").append(StringUtils.convertByteArrayToHexString(uid)).append("\n\n");
 
 // TODO: Part 2/Step 3: Read NDEF Message from Tag
                 // Get tag's NDEF messages:
                 Parcelable[] ndefRaw = data.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                 NdefMessage[] ndefMsgs = null;
                 if (ndefRaw != null) {
                     ndefMsgs = new NdefMessage[ndefRaw.length];
                     for (int i = 0; i < ndefMsgs.length; ++i) {
                         ndefMsgs[i] = (NdefMessage) ndefRaw[i];
                     }
                 }
 
 // TODO: Part 2/Step 4: Find URI Record in NDEF Messages
                 // Find URI records:
 
                 if (ndefMsgs != null) {
                     // Iterate through all NDEF messages on the tag:
                     for (int i = 0; i < ndefMsgs.length; ++i) {
                         // Get NDEF message's records:
                         NdefRecord[] records = ndefMsgs[i].getRecords();
 
                         if (records != null) {
                             // Iterate through all NDEF records:
                             for (int j = 0; j < records.length; ++j) {
                                 // Test if this record is a URI record:
                                 if ((records[j].getTnf() == NdefRecord.TNF_WELL_KNOWN)
                                         && Arrays.equals(records[j].getType(), NdefRecord.RTD_URI)) {
                                     byte[] payload = records[j].getPayload();
                                     
                                     //// Drop prefix identifier byte and convert remaining URL to string (UTF-8):
                                     //String uri = new String(Arrays.copyOfRange(payload, 1, payload.length), Charset.forName("UTF-8"));
 
                                     // Better: Use UriRecordHelper to decode URI record payload:
                                     String uri = UriRecordHelper.decodeUriRecordPayload(payload);
                                     
                                     //*tagInfo.append("URI: ").append(uri).append("\n");
                                     tagInfo.append(uri);
                                 }
                             }
                             
                         }
                     }
                 }
 
 // TODO: Part 2/Step 5: Display Tag Data in a Dialog Box
                 Bundle args = new Bundle();
                 args.putString(ARG_MESSAGE, tagInfo.toString());
                 dataMatched(AdkCommandStr, args);
                 //showDialog(DIALOG_NEW_TAG, args);
                
 // TODO: Part 1/Step 8: Receive Foreground Dispatch Intent
             }
         }
     }
 }
 	public void dataMatched(int id, Bundle args){
 		switch (id){
 			case AdkCommandStr:
 				if (args != null) {
                     String message = args.getString(ARG_MESSAGE);
                     if (message.equals(keyCode)) {
                     	showMessage("keyCode Matched!!!");
                         keyMessage(message);
                         
                        pool.play(turret_dispense, 1, 1, 0, 0, 1);
                         
                         mHandler.post(new Runnable() {
 							@Override
 							public void run() {
 								mHandler.sendEmptyMessage(0);
 							}
                         });
                         
                     }
                     else{
                     	showMessage("Wrong KeyCode...");
                     	keyMessage(message);
                     	
                    	pool.play(turret_disable, 1,1,0,0,1);
                     	
                     	mHandler.post(new Runnable() {
 							@Override
 							public void run() {
 								mHandler.sendEmptyMessage(1);
 							}
                         });
                         
                     }
 				}
 				break;
 		}
 	}
 
 	/**
      * Called when a dialog is created.
      */
     @Override
     protected Dialog onCreateDialog(int id, Bundle args) {
         switch (id) {
 // TODO: Part 1/Step 5: A Dialog Box: Ready to Write to Tag
             case DIALOG_WRITE_URL:
                 return new AlertDialog.Builder(this)
                         .setTitle("Write URL to tag...")
                         .setMessage("Touch tag to start writing.")
                         .setCancelable(true)
                         .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface d, int arg) {
                                 d.cancel();
                             }
                         })
                         .setOnCancelListener(new DialogInterface.OnCancelListener() {
                             public void onCancel(DialogInterface d) {
                                 mWriteUrl = false;
                             }
                         }).create();
 // TODO: Part 2/Step 1: A Simple Dialog Box: Detected a Tag
             case DIALOG_NEW_TAG:
                 return new AlertDialog.Builder(this)
                         .setTitle("Tag detected")
                         .setMessage("")
                         .setCancelable(true)
                         .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface d, int arg) {
                                 d.dismiss();
                             }
                         })
                         .create();
         }
         return null;
     }
 
 // TODO: Part 2/Step 1: A Simple Dialog Box: Detected a Tag
     /**
      * Called before a dialog is shown to the user.
      */
     @Override
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
         switch (id) {
             case DIALOG_NEW_TAG:
                 if (args != null) {
                     String message = args.getString(ARG_MESSAGE);
                     if (message != null) { ((AlertDialog) dialog).setMessage(message);}
                 }
                 break;
         }
     }
   	@Override
   	protected void onDestroy() { // ƼƼ Ҹ  ȣ
   		// εĳƮ ù 
   		unregisterReceiver(mUsbReceiver);
   		super.onDestroy();
   	}
   	
     private void openAccessory(UsbAccessory accessory){
 		mAccessory = accessory;
         if(handler == null)
             handler = new AdkHandler();
 
        handler.open(mUsbManager, mAccessory);
 	}
 	
 	private void closeAccessory(){
         if(handler != null && handler.isConnected())
             handler.close();
         mAccessory = null;
     }
 	
 	private void showMessage(String msg){
 		txtMsg.setText(msg);
 	}
 	
 	private void keyMessage(String msg){
 		keyCodeView.setText(msg);
 	}
 	
 	
 
 	}
 
