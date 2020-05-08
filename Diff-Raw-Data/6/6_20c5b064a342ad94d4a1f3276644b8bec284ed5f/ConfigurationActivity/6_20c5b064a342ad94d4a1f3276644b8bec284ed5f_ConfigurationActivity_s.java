 
 package com.smilo.bullpen.activities;
 
 import com.smilo.bullpen.Constants;
 import com.smilo.bullpen.ExtraItems;
 import com.smilo.bullpen.Utils;
 import com.smilo.bullpen.WidgetProvider;
 import com.smilo.bullpen.R;
 
 import android.app.Activity;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class ConfigurationActivity extends Activity {
 
     private static final String TAG = "ConfigurationActivity";
     private static final boolean DEBUG = Constants.DEBUG_MODE;
     public static final String CONFIGURATION_ACTIVITY_CLASS_NAME = Constants.Specific.PACKAGE_NAME + ".activities." + TAG;
     
     private static ExtraItems mItem = null;
     
     private boolean mIsExecutedBySettingButton = false;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_configuration);
         
         // Set the result to CANCELED.  This will cause the widget host to cancel
         // out of the widget placement if they press the back button.
         setResult(RESULT_CANCELED);
         
         // Set title.
         this.setTitle(R.string.title_activity_configuration);
 
         // Get ExtraItems.
         Intent intent = getIntent();
         mItem = Utils.createExtraItemsFromIntent(intent);
         if (mItem.getAppWidgetId() == AppWidgetManager.INVALID_APPWIDGET_ID) {
             if (DEBUG) Log.e(TAG, "Invalid appWidget Id[" + mItem.getAppWidgetId() + "]");
             finish();
         }
 
         Bundle extras = intent.getExtras();
         if (extras != null &&
             extras.containsKey(Constants.EXTRA_PERMIT_MOBILE_CONNECTION_TYPE) &&
             extras.containsKey(Constants.EXTRA_REFRESH_TIME_TYPE) &&
             extras.containsKey(Constants.EXTRA_BOARD_TYPE)) {
             mIsExecutedBySettingButton = true;
         }
         
         int boardType, refreshTimeType;
         boolean isPermitMobileConnection;
         String blackList, blockedWords;
         
         if (mIsExecutedBySettingButton) {
             boardType = mItem.getBoardType();
             refreshTimeType = mItem.getRefreshTimeType();
             isPermitMobileConnection = mItem.getPermitMobileConnectionType();
             blackList = mItem.getBlackList();
             blockedWords = mItem.getBlockedWords();
             
         } else {
             // Load configuration info.
             SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
             
             boardType = pref.getInt(Constants.KEY_BOARD_TYPE, Constants.DEFAULT_BOARD_TYPE);
             refreshTimeType = pref.getInt(Constants.KEY_REFRESH_TIME_TYPE, Constants.DEFAULT_REFRESH_TIME_TYPE);
             isPermitMobileConnection = pref.getBoolean(Constants.KEY_PERMIT_MOBILE_CONNECTION_TYPE, Constants.DEFAULT_PERMIT_MOBILE_CONNECTION_TYPE);
             blackList = pref.getString(Constants.KEY_BLACK_LIST, Constants.DEFAULT_BLACK_LIST);
             blockedWords = pref.getString(Constants.KEY_BLOCKED_WORDS, Constants.DEFAULT_BLOCKED_WORDS);
             mItem.setScrapList(pref.getString(Constants.KEY_SCRAP_LIST, Constants.DEFAULT_SCRAP_LIST));
         }
 
         // Initialize layout components.
         initializeRadioButton(isPermitMobileConnection);
         initializeSpinners(refreshTimeType, boardType);
         initializeEditText(blackList, blockedWords);
         initializeButtons();
     }
 
     private void initializeRadioButton(boolean isPermitMobileConnection) {
         CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
         cb.setChecked(isPermitMobileConnection);
     }
     
     private void initializeSpinners(int refreshTypeType, int boardType) {
         Spinner spinRefreshTime = (Spinner)findViewById(R.id.spinRefreshTime);
         ArrayAdapter<CharSequence> adapterRefreshTime = ArrayAdapter.createFromResource(this, R.array.refreshTime, android.R.layout.simple_spinner_item);
         adapterRefreshTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinRefreshTime.setAdapter(adapterRefreshTime);
         spinRefreshTime.setOnItemSelectedListener(mSpinRefreshTimeSelectedListener);
         spinRefreshTime.setSelection(refreshTypeType);
 
         Spinner spinBoard = (Spinner)findViewById(R.id.spinBoard);
         ArrayAdapter<CharSequence> adapterBoard = ArrayAdapter.createFromResource(this, R.array.board, android.R.layout.simple_spinner_item);
         adapterBoard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinBoard.setAdapter(adapterBoard);
         spinBoard.setOnItemSelectedListener(mSpinBoardSelectedListener);
         spinBoard.setSelection(boardType);
     }
 
     private void initializeEditText(String blackList, String blockedWords) {
         EditText etBlackList = (EditText)findViewById(R.id.editBlackList);
         if (blackList == null)
             etBlackList.setText("");
         else
             etBlackList.setText(blackList);
         
         EditText etBlockedWords = (EditText)findViewById(R.id.editBlockedWords);
         if (blockedWords == null)
             etBlockedWords.setText("");
         else
             etBlockedWords.setText(blockedWords);
     }
 
     private void initializeButtons() {
         findViewById(R.id.btnConfigurationOk).setOnClickListener(mBtnOkOnClickListener);
         findViewById(R.id.btnConfigurationCancel).setOnClickListener(mBtnCancelOnClickListener);
     }
 
     View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {
         public void onClick(View v) {
             if (DEBUG) Log.i(TAG, "Button OK clicked");
 
             final Context context = ConfigurationActivity.this;
             
             // Get mobileConnection checkbox's value.
             CheckBox cb = (CheckBox)findViewById(R.id.cbMobileConnection);
             mItem.setPermitMobileConnectionType(cb.isChecked());
             
             // Get black list's value. If empty, set null.
             EditText etBlackList = (EditText)findViewById(R.id.editBlackList);
             String blackList = etBlackList.getText().toString();
             if (blackList != null && blackList.length() == 0)
                 mItem.setBlackList(null);
             else
                 mItem.setBlackList(blackList);
             
             // Get blocked words's value. If empty, set null.
             EditText etBlockedWords = (EditText)findViewById(R.id.editBlockedWords);
             String blockedWords = etBlockedWords.getText().toString();
             if (blockedWords != null && blockedWords.length() == 0)
                 mItem.setBlockedWords(null);
             else
                 mItem.setBlockedWords(blockedWords);
 
            // Set mItem to default page number.
             mItem.setPageNum(Constants.DEFAULT_PAGE_NUM);
             
             // Create intent and broadcast it!
             Intent intent = Utils.createIntentFromExtraItems(
                     context, WidgetProvider.WIDGET_PROVIDER_CLASS_NAME, Constants.ACTION_INIT_LIST, mItem, false);
             context.sendBroadcast(intent);
 
             // set return intent.
             Intent resultValue = new Intent();
             resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mItem.getAppWidgetId());
             setResult(RESULT_OK, resultValue);
             finish();
         }
     };
     
     View.OnClickListener mBtnCancelOnClickListener = new View.OnClickListener() {
         public void onClick(View v) {
             if (DEBUG) Log.i(TAG, "Button Cancel clicked");
             final Context context = ConfigurationActivity.this;
 
             if (mIsExecutedBySettingButton == false) {
                 WidgetProvider.removeWidget(context, mItem.getAppWidgetId());
             }
             finish();
         }
     };
     
     Spinner.OnItemSelectedListener mSpinRefreshTimeSelectedListener = new AdapterView.OnItemSelectedListener() {
         public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
             mItem.setRefreshTimeType(arg2);
         }
 
         public void onNothingSelected(AdapterView<?> arg0) {
             // Do nothing
         }
     };
     
     Spinner.OnItemSelectedListener mSpinBoardSelectedListener = new AdapterView.OnItemSelectedListener() {
         public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
             mItem.setBoardType(arg2);
         }
 
         public void onNothingSelected(AdapterView<?> arg0) {
             // Do nothing
         }
     };
 }
