 /*
  * WriterActivity.java (BT Tag Writer)
  *
  * https://github.com/alump/BtTagWriter
  *
  * Copyright 2011-2013 Sami Viitanen <sami.viitanen@gmail.com>
  * All rights reserved.
  */
 
 package fi.siika.bttagwriter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothClass;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 import fi.siika.bttagwriter.data.TagInformation;
 import fi.siika.bttagwriter.data.TagType;
 import fi.siika.bttagwriter.managers.BluetoothManager;
 import fi.siika.bttagwriter.managers.NfcManager;
 import fi.siika.bttagwriter.ui.BluetoothRow;
 import fi.siika.bttagwriter.ui.BluetoothRowAdapter;
 import fi.siika.bttagwriter.ui.Pages;
 import fi.siika.bttagwriter.writers.TagWriter;
 import fi.siika.bttagwriter.writers.WriteError;
 
 /**
  * Main activity of BtTagWriter application
  *
  * @author Sami Viitanen <sami.viitanen@gmail.com>
  */
 public class WriterActivity extends Activity implements
         BluetoothManager.DiscoveryListener {
 
     private final static String TAG = "WriterActivity";
     private final static String PREFS_NAME = "WriterPrefs";
     private final static String PREF_FILTER = "filter-devices";
     private final static String PREF_HANDOVER = "handover";
 
     private TagWriter mTagWriter;
     //private Handler mTagWriterHandler;
     private final TagInformation mTagInfo = new TagInformation();
     private BluetoothManager mBtMgr;
     private NfcManager mNfcMgr;
     private SharedPreferences mSettings;
 
     private void setCurrentPage(Pages page) {
         setCurrentPage(page.toInt());
     }
 
     private void setCurrentPage(int page) {
         int pageWas = getCurrentPage();
         if (showFlipChild(page)) {
             if (Pages.BT_SELECT.equal(page)) {
                 startBluetoothDiscovery();
             } else if (Pages.TAG.equal(page)) {
                 mNfcMgr.enableTechDiscovered();
             }
             invalidateOptionsMenu();
         }
     }
 
     private int getCurrentPage() {
         ViewFlipper flip = (ViewFlipper) findViewById(R.id.mainFlipper);
         return flip.getDisplayedChild();
     }
 
     /*
      * Start Bluetooth discovery (if not active)
      */
     private void startBluetoothDiscovery() {
 
         if (!mBtMgr.startDiscovery(this)) {
             showActionDialog(R.string.action_dialog_bluetooth_failed_str,
                     new DialogInterface.OnClickListener() {
 
                         public void onClick(DialogInterface dialog, int which) {
                             setCurrentPage(Pages.START);
                         }
 
                     }, false, null);
         }
     }
 
     /**
      * Function that will handle things that should be done after user moves
      * away from different child views of flip. Called by showFlipChild.
      *
      * @param index Index of child that was replaced with something else
      */
     private void flipChildHidden(int index) {
         if (Pages.BT_SELECT.equal(index)) {
             mBtMgr.stopDiscovery();
         } else if (Pages.TAG.equal(index)) {
             mNfcMgr.disableForegroundDispatch();
         }
     }
 
     /**
      * Call this when changing the shown child of flip page. Will selected
      * the correct animation depending on current and selected child.
      *
      * @param index Index of child shown next
      * @return true if shown child was changed
      */
     private boolean showFlipChild(int index) {
         ViewFlipper flip = (ViewFlipper) findViewById(R.id.mainFlipper);
         int current = flip.getDisplayedChild();
 
         if (current == index) {
             return false;
         } else if (Pages.ABOUT.equal(current)) {
             flip.setInAnimation(this, R.animator.fade_in_anim);
             flip.setOutAnimation(this, R.animator.out_jump_anim);
         } else if (Pages.ABOUT.equal(index)) {
             flip.setInAnimation(this, R.animator.in_jump_anim);
             flip.setOutAnimation(this, R.animator.fade_out_anim);
         } else if (current < index) {
             flip.setInAnimation(this, R.animator.in_left_anim);
             flip.setOutAnimation(this, R.animator.out_right_anim);
         } else {
             flip.setInAnimation(this, R.animator.in_right_anim);
             flip.setOutAnimation(this, R.animator.out_left_anim);
         }
 
         flipChildHidden(current);
         flip.setDisplayedChild(index);
         return true;
     }
 
     private final OnClickListener mStartButtonListener = new OnClickListener() {
         public void onClick(View v) {
 
             mBtListAdapter.clear();
             setCurrentPage(Pages.BT_SELECT);
         }
     };
 
     private final OnClickListener mRescanButtonListener = new OnClickListener() {
         public void onClick(View v) {
 
             mBtMgr.startDiscovery(WriterActivity.this);
         }
     };
 
     private final OnClickListener mExtraoptsReadyButtonListener =
             new OnClickListener() {
 
                 public void onClick(View v) {
 
                     //Read extra options
 
                     CheckBox cbox = (CheckBox) findViewById(R.id.readOnlycheckBox);
                     if (cbox != null) {
                         mTagInfo.setReadOnly(cbox.isChecked());
                     } else {
                         mTagInfo.setReadOnly(false);
                     }
 
                     cbox = (CheckBox) findViewById(R.id.extraoptsCompatibilityCheckBox);
                     if (cbox != null) {
                         mSettings.edit().putBoolean(PREF_HANDOVER, cbox.isChecked()).commit();
                         mTagInfo.setType(cbox.isChecked() ? TagType.HANDOVER : TagType.SIMPLIFIED);
                     } else {
                         mTagInfo.setType(TagType.SIMPLIFIED);
                     }
 
                     EditText editText = (EditText) findViewById(R.id.extraoptsPinEdit);
                     if (editText != null) {
                         mTagInfo.pin = editText.getText().toString();
                     } else {
                         mTagInfo.pin = "";
                     }
 
                     setCurrentPage(Pages.TAG);
                 }
             };
 
     private final OnClickListener mExitButtonListener = new OnClickListener() {
         public void onClick(View v) {
 
             finish();
         }
     };
 
     private BluetoothRowAdapter mBtListAdapter = null;
 
     protected void connectSignals() {
         Button button = (Button) findViewById(R.id.startButton);
         button.setOnClickListener(mStartButtonListener);
 
         button = (Button) findViewById(R.id.restartButton);
         button.setOnClickListener(mExtraoptsReadyButtonListener);
 
         button = (Button) findViewById(R.id.extraoptsReadyButton);
         button.setOnClickListener(mExtraoptsReadyButtonListener);
 
         button = (Button) findViewById(R.id.exitButton);
         button.setOnClickListener(mExitButtonListener);
 
         ImageButton ib = (ImageButton) findViewById(R.id.btRescanButton);
         ib.setOnClickListener(mRescanButtonListener);
     }
 
     private final DialogInterface.OnClickListener mWriteFailedDialogListener =
             new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                 }
 
             };
 
     @Override
     public void onResume() {
         super.onResume();
 
         mSettings = getSharedPreferences(PREFS_NAME, 0);
 
         CheckBox compCB = (CheckBox) findViewById(R.id.extraoptsCompatibilityCheckBox);
        compCB.setChecked(mSettings.getBoolean(PREF_HANDOVER, false));
 
         mTagWriter = new TagWriter(this, tagWriterListener);
     }
 
     protected TagWriter.TagWriterListener tagWriterListener = new TagWriter.TagWriterListener() {
 
         @Override
         public void onSuccess() {
             setCurrentPage(Pages.SUCCESS);
         }
 
         @Override
         public void onFailure(WriteError error) {
             if (error == WriteError.TOO_SMALL) {
                 showActionDialog(R.string.tag_is_too_small_str,
                         mWriteFailedDialogListener, false, null);
             } else if (error != WriteError.CANCELLED) {
                 Log.w(TAG, "Write failure received: " + error.toString());
                 showActionDialog(R.string.tag_write_failed_str,
                         mWriteFailedDialogListener, false, null);
             }
         }
     };
 
     @Override
     public void onPause() {
         mBtMgr.releaseAdapter();
         super.onPause();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mBtMgr = new BluetoothManager(this);
         mNfcMgr = new NfcManager(this);
 
         if (mBtListAdapter == null) {
             mBtListAdapter = new BluetoothRowAdapter(this);
         }
         ListView list = (ListView) findViewById(R.id.btDevicesList);
         list.setAdapter(mBtListAdapter);
 
         list.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
 
                 BluetoothRow row = mBtListAdapter.getRow(position);
                 mTagInfo.name = row.getName();
                 mTagInfo.address = row.getAddress();
 
                 TextView tview = (TextView) findViewById(
                         R.id.extraoptsSelectedDeviceValue);
                 if (tview != null) {
                     String text = mBtListAdapter.getRow(position).getName() +
                         " (" + mBtListAdapter.getRow(position).getAddress() + ")";
                     tview.setText(text);
                 }
 
                 setCurrentPage(Pages.EXTRA_OPTIONS);
             }
         });
 
         //Setting HTML to text views has to be done in code. Do it here.
         TextView appTV = (TextView) findViewById(R.id.appDescriptionTextView);
         appTV.setText(Html.fromHtml(getString(
                 R.string.about_info_str)));
 
         TextView limitTV = (TextView) findViewById(R.id.limitationsTextView);
        limitTV.setText(Html.fromHtml(getString(
                R.string.about_issues_str)));
 
         TextView credsTV = (TextView) findViewById(R.id.creditsTextView);
         credsTV.setText(Html.fromHtml(getString(
                 R.string.about_credits_str)));
 
         TextView linksTV = (TextView) findViewById(R.id.linksTextView);
         linksTV.setText(Html.fromHtml(getString(
                 R.string.about_links_str)));
 
         connectSignals();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 
         if ((keyCode == KeyEvent.KEYCODE_BACK)) {
             if (toPrevPage()) {
                 return true;
             }
         }
 
         return super.onKeyDown(keyCode, event);
     }
 
     private boolean toPrevPage() {
 
         int curPage = getCurrentPage();
         boolean ret = true;
 
         if (Pages.ABOUT.equal(curPage)) {
             setCurrentPage(Pages.START);
         } else if (Pages.BT_SELECT.equal(curPage)) {
             setCurrentPage(Pages.START);
         } else if (Pages.EXTRA_OPTIONS.equal(curPage)) {
             setCurrentPage(Pages.BT_SELECT);
         } else if (Pages.TAG.equal(curPage)) {
             setCurrentPage(Pages.EXTRA_OPTIONS);
         } else if (Pages.SUCCESS.equal(curPage)) {
             setCurrentPage(Pages.TAG);
         } else {
             ret = false;
         }
 
         return ret;
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         setIntent(intent);
         String action = intent.getAction();
 
 
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
             Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             if (!mTagWriter.writeToTag(tag, mTagInfo)) {
                 showActionDialog(R.string.tag_unsupported_str,
                         mWriteFailedDialogListener, false, null);
             }
 
         } else {
             Toast toast = Toast.makeText(this, action,
                     Toast.LENGTH_SHORT);
             toast.show();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
 
         menu.findItem(R.id.filterSearchitem).setChecked(mSettings.getBoolean(PREF_FILTER, true));
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         int page = getCurrentPage();
         menu.findItem(R.id.aboutItem).setVisible(!Pages.ABOUT.equal(page));
         menu.findItem(R.id.filterSearchitem).setVisible(Pages.BT_SELECT.equal(page));
         menu.findItem(R.id.filterSearchitem).setChecked(getFilterDevices());
         return super.onPrepareOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.aboutItem:
                 setCurrentPage(Pages.ABOUT);
                 return true;
             case R.id.filterSearchitem:
                 item.setChecked(!item.isChecked());
                 setFilterDevicesEnabled(item.isChecked());
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void setFilterDevicesEnabled(boolean enabled) {
         if (getFilterDevices() != enabled) {
 
             Editor editor = mSettings.edit();
             editor.putBoolean(PREF_FILTER, enabled);
             editor.commit();
 
             if (enabled) {
                 mBtListAdapter.clearNonAudio();
             }
 
             if (Pages.BT_SELECT.equal(this.getCurrentPage())) {
                 startBluetoothDiscovery();
             }
         }
     }
 
     private void showActionDialog(int textResId,
                                   DialogInterface.OnClickListener clickListener,
                                   boolean cancelable,
                                   DialogInterface.OnCancelListener cancelListener) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(textResId);
 
         AlertDialog dialog = builder.create();
 
         if (clickListener != null) {
             dialog.setCancelable(cancelable);
             dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                     getResources().getText(R.string.action_dialog_ok),
                     clickListener);
 
         } else {
             dialog.setCancelable(true);
         }
 
         if (cancelListener != null) {
             dialog.setOnCancelListener(cancelListener);
         }
 
         dialog.show();
 
     }
 
     /* (non-Javadoc)
      * @see fi.siika.bttagwriter.BluetoothManager.Listener#bluetoothDeviceFound(android.bluetooth.BluetoothDevice)
      */
     public void bluetoothDeviceFound(BluetoothDevice device, boolean fromPaired) {
 
         if (getFilterDevices()) {
             if (!device.getBluetoothClass().hasService(BluetoothClass.Service.AUDIO)) {
                 return;
             }
         }
 
         mBtListAdapter.addDeviceIfNotPresent(device, !fromPaired);
 
     }
 
     /* (non-Javadoc)
      * @see fi.siika.bttagwriter.BluetoothManager.Listener#bluetoothDiscoveryStateChanged(boolean)
      */
     public void bluetoothDiscoveryStateChanged(boolean active) {
         ProgressBar pb = (ProgressBar) findViewById(R.id.btScanProgressBar);
         pb.setIndeterminate(active);
 
         ViewFlipper flip = (ViewFlipper) findViewById(
                 R.id.btScanActionsFlipper);
 
         if (active) {
             flip.setDisplayedChild(0);
             pb.setVisibility(View.VISIBLE);
 
             Toast toast = Toast.makeText(this, R.string.btscan_started_str,
                     Toast.LENGTH_LONG);
             toast.show();
 
             TextView nodevText = (TextView) findViewById(
                     R.id.btScanNoDevicesFoundText);
             if (nodevText != null) {
                 nodevText.setVisibility(View.GONE);
             }
 
         } else {
             flip.setDisplayedChild(1);
             pb.setVisibility(View.INVISIBLE);
 
             if (this.mBtListAdapter.getCount() == 0) {
                 TextView nodevText = (TextView) findViewById(
                         R.id.btScanNoDevicesFoundText);
                 if (nodevText != null) {
                     nodevText.setVisibility(View.VISIBLE);
                 }
             }
 
         }
 
     }
 
     /**
      * Get option if devices should be filtered
      *
      * @return true to filter, false to not filter
      */
     private boolean getFilterDevices() {
         return mSettings.getBoolean(PREF_FILTER, false);
     }
 
 
 }
