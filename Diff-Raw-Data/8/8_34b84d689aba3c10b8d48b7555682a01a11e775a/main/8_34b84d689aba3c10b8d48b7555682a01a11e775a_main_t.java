 
 package ru.openitr.exinformer;
 
 import android.app.*;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.*;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.DatePicker;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 import android.widget.TextView;
 import com.mobeta.android.dslv.DragSortListView;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.LinkedList;
 
 public class main extends ListActivity {
     boolean OldAPIVersion;
     static Calendar onDate;
 //    Calendar calendar = Calendar.getInstance();
 
      ValFromDbAdapter valFromDbAdapter;
     public static final boolean DEBUG = true;
     public static final String LOG_TAG = "CBInfo";
     public static final int STATUS_BEGIN_REFRESH = 10;
     public static final int FIN_STATUS_OK = 20;
     public static final int FIN_STATUS_NOT_RESPOND = 40;
     public static final int FIN_STATUS_NO_DATA = 50;
     public static final int FINS_STATUS_NETWORK_DISABLE = 30;
     public static final int FINS_STATUS_DATA_UPDATED = 23;
     public static final int FINS_STATUS_DATA_NOT_UPDATED = 23;
 
 
     public static final String PARAM_STATUS = "status";
     public static final String PARAM_DATE = "date";
     public static final String PARAM_ONLY_SET_ALARM = "only_set";
     public static final String PARAM_FROM = "from";
 
     static final String INFO_REFRESH_INTENT = "ru.openitr.exinformer.INFO_UPDATE";
 
     static final private int DATA_DIALOG = 1;
     static final private int NETSETTINGS_DIALOG = 2;
     static final private int PROGRESS_DIALOG = 3;
     static final private int ILLEGAL_DATA_DIALOD = 4;
     static final private int NOT_RESPOND_DIALOG = 5;
 
     private static final int SHOW_PREFERENCES = 1;
 
     static final Uri CURRENCYS_URI = Uri.parse("content://ru.openitr.exinformer.currency/currencys");
     static final Uri CURRENCY_URI = Uri.parse("content://ru.openitr.exinformer.currency/");
     private Cursor mCursor;
     Intent refreshServiceIntent;
     BroadcastReceiver br;
 
     private DragSortListView.DropListener onDrop =
             new DragSortListView.DropListener() {
                 @Override
                 public void drop(int from, int to) {
                     Log.d(LOG_TAG,"Drop from: "+Integer.toString(from)+", to: " +Integer.toString(to));
                     if (from != to) {
                         moveItem(from, to);
                         //String item = (String) valFromDbAdapter.getItem(from);
                         //adapter.remove(item);
                         //adapter.insert(item, to);
                         //list.moveCheckState(from, to);
                     }
                 }
             };
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         onDate = Calendar.getInstance();
         OldAPIVersion = Build.VERSION.SDK_INT >= 11 ? false : requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         setContentView(R.layout.main);
         Log.d(LOG_TAG, "onCreate");
         refreshServiceIntent = new Intent(this, InfoRefreshService.class);
         mCursor = managedQuery(CURRENCYS_URI, CurrencyDbAdapter.ALL_COLUMNS, null, null, CurrencyDbAdapter.KEY_ORDER + " ASC");
         startManagingCursor(mCursor);
         br = new MainActivityBroadcastReceiever();
         try {
             final String[] from = CurrencyDbAdapter.ALL_VISIBLE_COLUMNS;
             final int [] to = {R.id.drag_handle, R.id.vChСodeView, R.id.vCursView, R.id.vNameView};
             //Адаптер к листу
             valFromDbAdapter = new ValFromDbAdapter(this,R.layout.currencylayuot, mCursor, from, to);
             setListAdapter(valFromDbAdapter);
             DragSortListView listView = getListView();
             listView.setDropListener(onDrop);
             listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//            getInfo(0);
             //Титл бар
             customTitleBar(getText(R.string.app_name).toString());
             setInfoDateToTitle();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public DragSortListView getListView(){
         return (DragSortListView) super.getListView();
     }
 
 
     @Override
     protected void onResume() {
         super.onResume();
         IntentFilter intF = new IntentFilter(INFO_REFRESH_INTENT);
         registerReceiver(br, intF);
 
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(br);
     }
 
     @Override
     public void onDestroy(){
         super.onDestroy();
         if (DEBUG) Log.d(LOG_TAG, "onDestroy");
     }
 
     private OnDateSetListener cDateSetListener = new OnDateSetListener() {
         public void onDateSet(DatePicker datePicker, int year, int month, int day) {
             Calendar newDate = Calendar.getInstance();
             newDate.set(Calendar.YEAR,year);
             newDate.set(Calendar.MONTH,month);
             newDate.set(Calendar.DAY_OF_MONTH, day);
             if (!newDate.equals(onDate)){
                 onDate = newDate;
                 getInfo(newDate);
             }
         }
 
     };
 
 
 
     @Override
     public Dialog onCreateDialog (int id){
         switch (id){
 
             case (DATA_DIALOG) :
                 DatePickerDialog dpd;
                 dpd = new DatePickerDialog (this, cDateSetListener, onDate.get(Calendar.YEAR),
                         onDate.get(Calendar.MONTH), onDate.get(Calendar.DATE));
                 return  dpd;
 
             case (NETSETTINGS_DIALOG) :
                 AlertDialog.Builder netSettingsDialog = new AlertDialog.Builder(this);
                 netSettingsDialog.setTitle(R.string.app_name);
                 netSettingsDialog.setMessage(R.string.netSettingsDlgMessage);
                 netSettingsDialog.setCancelable(false);
                 netSettingsDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i){
                         removeDialog(PROGRESS_DIALOG);
                         dialogInterface.cancel();
                     }
                 });
                 netSettingsDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         goToNetsettings();
                     }
                 });
                 return netSettingsDialog.create();
 
             case (PROGRESS_DIALOG) :
                 ProgressDialog progressDialog = new ProgressDialog(this);
                 progressDialog.setMessage(getText(R.string.loading));
                 return progressDialog;
 
             case (ILLEGAL_DATA_DIALOD):
                 AlertDialog.Builder msgDlg = new AlertDialog.Builder(this);
                 msgDlg.setTitle(R.string.futureTitle);
                 msgDlg.setMessage(R.string.futureMsg);
                 msgDlg.setCancelable(false);
                 msgDlg.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         onDate.setTimeInMillis(System.currentTimeMillis());
 
                         dialogInterface.dismiss();
                     }
                 });
                 return msgDlg.create();
             case (NOT_RESPOND_DIALOG):
                 AlertDialog.Builder notRespondDlg = new AlertDialog.Builder(this);
                 notRespondDlg.setTitle(R.string.notRespondDlgTitle);
                 notRespondDlg.setMessage(R.string.notRespondDlgMsg);
                 notRespondDlg.setCancelable(true);
                 notRespondDlg.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int which) {
                         dialogInterface.dismiss();
                     }
                 }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         getInfo(onDate);
                     }
                 });
                 return notRespondDlg.create();
         }
         return null; //super.onCreateDialog(id);
     }
 
 
     /**
        Title bar для вывода даты курса.
      */
     private void customTitleBar(String left) {
         if (OldAPIVersion) {
              getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                     R.layout.apptitle);
             TextView titleLeft = (TextView) findViewById(R.id.titleLeft);
             titleLeft.setText(left);
 //            setDateOnTitle(onDate);
         }
     }
 
     private void setDateOnTitle(Calendar onDate){
         SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
         String stringDate = sdf.format(onDate.getTime());
         if (OldAPIVersion) {
             TextView titleTvRight = (TextView) findViewById(R.id.titleRight);
             titleTvRight.setText(getText(R.string.appTitleDatePrefix).toString() + ": "+stringDate);
         }
         else {
             ActionBar bar = getActionBar();
             bar.setSubtitle(getString(R.string.appTitleDatePrefix)+ ": " + stringDate);
         }
 
     }
 
     public void setInfoDateToTitle(){
         Cursor cursor = (getContentResolver().query(CURRENCYS_URI, new String[]{CurrencyDbAdapter.KEY_DATE}, null, null, null));
         if (cursor.moveToFirst()){
             onDate.setTimeInMillis(cursor.getLong(0));
             setDateOnTitle(onDate);
         }
     }
 
     private void goToNetsettings(){
         Intent netSettings = new Intent("android.settings.WIRELESS_SETTINGS");
         startActivity(netSettings);
         getInfo(onDate);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
 
         if (OldAPIVersion) {
             getMenuInflater().inflate(R.menu.main_menu, menu);
             return true;
         }
         else{
             getMenuInflater().inflate(R.menu.root_menu, menu);
             return true;
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         super.onOptionsItemSelected(item);
         switch (item.getItemId()){
                 case (R.id.setDataItem):
                     showDialog(DATA_DIALOG);
                     return true;
                 case (R.id.settingsItem):
                     Intent i = new Intent(this, BasePreferencesActivity.class);
                     startActivityForResult(i, SHOW_PREFERENCES);
                     return true;
                 case (R.id.root_menu):
                     showMenu(findViewById(R.id.root_menu));
                     return true;
         }
         return false;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == SHOW_PREFERENCES)
             if (resultCode == Activity.RESULT_OK) {
                 refreshPreferences();
             }
     }
 
 
     public void showMenu(View view){
         PopupMenu popupMenu = new PopupMenu(this, view);
         popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 onOptionsItemSelected(item);
                 return false;
             }
         });
         popupMenu.inflate(R.menu.main_menu);
         popupMenu.show();
 
     }
 
     private void getInfo(Calendar newDate){
         onDate = newDate;
         refreshServiceIntent.putExtra(PARAM_DATE,newDate.getTimeInMillis());
         startService(refreshServiceIntent);
     }
 
     private void getInfo(long timeInMillis) {
         refreshServiceIntent.putExtra(PARAM_DATE,timeInMillis);
         startService(refreshServiceIntent);
 
     }
 
     private void refreshPreferences(){
         refreshServiceIntent.putExtra(PARAM_ONLY_SET_ALARM, true);
         startService(refreshServiceIntent);
     }
 
 
     private void moveItem (int from, int to){
 
          ContentResolver cr = getContentResolver();
          ContentValues cv = new ContentValues();
          LinkedList<String> items = new LinkedList<String>();
          Cursor itemsCursor = cr.query(CURRENCYS_URI,CurrencyDbAdapter.ALL_COLUMNS,null,null,CurrencyDbAdapter.KEY_ORDER);
          itemsCursor.moveToFirst();
          do {
              items.add(itemsCursor.getString(CurrencyDbAdapter.VALCHARCODE_COLUMN));
          }
          while (itemsCursor.moveToNext());
          String item = items.get(from);
          items.remove(from);
          items.add(to, item);
          for (String itemCode: items){
              int index = items.indexOf(itemCode);
              cv.put(CurrencyDbAdapter.KEY_ORDER, index);
              cr.update(Uri.parse(CURRENCYS_URI.toString() + "/" + itemCode), cv, null, null);
          }
          //getContentResolver().notifyChange(CURRENCYS_URI, null);
          mCursor.requery();
      }
 
      private class MainActivityBroadcastReceiever extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(INFO_REFRESH_INTENT)){
                 int status = intent.getIntExtra(PARAM_STATUS,0);
                 if (DEBUG) LogSystem.logInFile(LOG_TAG, "main: (OnRecieve) Result of service run status: " + status);
                 switch (status){
                     case STATUS_BEGIN_REFRESH:
                         Log.d(LOG_TAG,"main: Begin updating info.");
                         showDialog(PROGRESS_DIALOG);
                         break;
                     case FIN_STATUS_NO_DATA:
                         removeDialog(PROGRESS_DIALOG);
                         Log.d(LOG_TAG, "main: No data receive.");
 
                         break;
                     case FIN_STATUS_NOT_RESPOND:
                         Log.d(LOG_TAG, "main: Server not respond.");
                         removeDialog(PROGRESS_DIALOG);
                         removeDialog(NOT_RESPOND_DIALOG);
                         showDialog(NOT_RESPOND_DIALOG);
                         break;
                     case FINS_STATUS_NETWORK_DISABLE:
                         Log.d(LOG_TAG, "main: Network disabled.");
                         showDialog(NETSETTINGS_DIALOG);
                         break;
                     default:
                         Log.d(LOG_TAG, "main: Refreshing OK.");
                         mCursor.requery();
                         setInfoDateToTitle();
                         removeDialog(PROGRESS_DIALOG);
                         break;
                 }
             }
 
         }
     }
 
 }
