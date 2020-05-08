 package com.imaginea.android.sugarcrm;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.method.LinkMovementMethod;
 import android.text.method.MovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.Util;
 import com.imaginea.android.sugarcrm.util.ViewUtil;
 
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * ModuleDetailsActivity is used to show details for all modules. Note: Ideally we would have to
  * rename the file, but to preserve CVS history we have chosen not to rename the file. Cannot use
  * CVS rename command as we are still using an old CVS server.
  * 
  * @author vasavi
  */
 public class ModuleDetailsActivity extends Activity {
 
     private String mRowId;
 
     private String mSugarBeanId;
 
     private String mModuleName;
 
     private Cursor mCursor;
 
     private String[] mSelectFields;
 
     private ViewGroup mDetailsTable;
 
     private String[] mRelationshipModules;
 
     private DatabaseHelper mDbHelper;
 
     private LoadContentTask mTask;
 
     private static final int HEADER = 1;
 
     private static final int STATIC_ROW = 2;
 
     private static final int DYNAMIC_ROW = 3;
 
     private ProgressDialog mProgressDialog;
 
     private static final String LOG_TAG = ModuleDetailsActivity.class.getSimpleName();
 
     /**
      * {@inheritDoc}
      * 
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.account_details);
 
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         mRowId = (String) intent.getStringExtra(Util.ROW_ID);
         mSugarBeanId = (String) intent.getStringExtra(RestUtilConstants.BEAN_ID);
         mModuleName = "Contacts";
         if (extras != null)
             mModuleName = extras.getString(RestUtilConstants.MODULE_NAME);
 
         mDbHelper = new DatabaseHelper(getBaseContext());
         if (intent.getData() == null) {
             intent.setData(Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId));
         }
         mSelectFields = mDbHelper.getModuleProjections(mModuleName);
         // mCursor = getContentResolver().query(getIntent().getData(), mSelectFields, null, null,
         // mDbHelper.getModuleSortOrder(mModuleName));
         // startManagingCursor(mCursor);
         // setContents();
 
         mRelationshipModules = mDbHelper.getModuleRelationshipItems(mModuleName);
 
         // ListView listView = (ListView) findViewById(android.R.id.list);
         // listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         // @Override
         // public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
         // if (Log.isLoggable(LOG_TAG, Log.INFO)) {
         // Log.i(LOG_TAG, "clicked on " + mRelationshipModules[position]);
         // }
         // openListScreen(mRelationshipModules[position]);
         // }
         // });
 
         /*
          * RelationshipAdapter adapter = new RelationshipAdapter(this);
          * adapter.setRelationshipArray(mRelationshipModules); listView.setAdapter(adapter);
          */
         mTask = new LoadContentTask();
         mTask.execute(null, null, null);
     }
 
     /**
      * <p>
      * openListScreen
      * </p>
      * 
      * @param moduleName
      *            a {@link java.lang.String} object.
      */
     protected void openListScreen(String moduleName) {
         // if (mModuleName.equals("Accounts")) {
         Intent detailIntent = new Intent(ModuleDetailsActivity.this, ModuleListActivity.class);
         if (mDbHelper == null)
             mDbHelper = new DatabaseHelper(getBaseContext());
         Uri uri = Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId);
         uri = Uri.withAppendedPath(uri, moduleName);
         detailIntent.setData(uri);
         detailIntent.putExtra(RestUtilConstants.MODULE_NAME, moduleName);
         detailIntent.putExtra(RestUtilConstants.BEAN_ID, mSugarBeanId);
         startActivity(detailIntent);
         // } else {
         // Toast.makeText(this, "Not yet supported!", Toast.LENGTH_SHORT).show();
         // }
     }
 
     /** {@inheritDoc} */
     @Override
     protected void onPause() {
         super.onPause();
         if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
             mTask.cancel(true);
         }
     }   
 
     /** {@inheritDoc} */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the currently selected menu XML resource.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.details_activity_menu, menu);
 
         SubMenu relationshipMenu = menu.addSubMenu(1, R.string.related, 0, getString(R.string.related));
         relationshipMenu.setIcon(R.drawable.menu_related);
         if (mRelationshipModules.length > 0) {
             for (int i = 0; i < mRelationshipModules.length; i++) {
                 relationshipMenu.add(0, Menu.FIRST + i, 0, mRelationshipModules[i]);
             }
         } else {
             menu.setGroupEnabled(1, false);
         }
 
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.home:
             Intent myIntent = new Intent(ModuleDetailsActivity.this, DashboardActivity.class);
             ModuleDetailsActivity.this.startActivity(myIntent);
             return true;
         case R.string.related:
             return true;
         default:
             if (Log.isLoggable(LOG_TAG, Log.INFO)) {
                 Log.i(LOG_TAG, "item id : " + item.getItemId());
             }           
             openListScreen(mRelationshipModules[item.getItemId() - 1]);           
             return true;
         }      
     }
 
     class LoadContentTask extends AsyncTask<Object, Object, Object> {
 
         int staticRowsCount;
 
         LoadContentTask() {
             mDetailsTable = (ViewGroup) findViewById(R.id.accountDetalsTable);
 
             staticRowsCount = mDetailsTable.getChildCount();
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             TextView tv = (TextView) findViewById(R.id.headerText);
             tv.setText(String.format(getString(R.string.detailsHeader), mModuleName));
 
             mProgressDialog = ViewUtil.getProgressDialog(ModuleDetailsActivity.this, getString(R.string.loading), true);
             mProgressDialog.show();
         }
 
         @Override
         protected void onProgressUpdate(Object... values) {
             super.onProgressUpdate(values);
 
             switch ((Integer) values[0]) {
 
             case HEADER:
                 TextView titleView = (TextView) values[2];
                 titleView.setText((String) values[3]);
                 break;
 
             case STATIC_ROW:
                 ViewGroup detailRow = (ViewGroup) values[2];
                 detailRow.setVisibility(View.VISIBLE);
 
                 TextView labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 TextView valueView = (TextView) values[5];
                 final String value = (String) values[6];
                 valueView.setText(value);
 
                 // handle the map
                 String fieldName = (String) values[1];
                 if (ModuleFields.SHIPPING_ADDRESS_COUNTRY.equals(fieldName)
                                                 || ModuleFields.BILLING_ADDRESS_COUNTRY.equals(fieldName)) {
                     if (!TextUtils.isEmpty(value)) {
                         valueView.setLinksClickable(true);
                         valueView.setClickable(true);
 
                         SpannableString spannableString = new SpannableString(value);
                         spannableString.setSpan(new InternalURLSpan(new OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 Log.i(LOG_TAG, "trying to locate - " + value);
                                 Uri uri = Uri.parse("geo:0,0?q=" + URLEncoder.encode(value));
                                 Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                 intent.setData(uri);
                                 startActivity(intent);
                             }
                         }), 0, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
                         valueView.setText(spannableString);
 
                         // for trackball movement
                         MovementMethod m = valueView.getMovementMethod();
                         if ((m == null) || !(m instanceof LinkMovementMethod)) {
                             if (valueView.getLinksClickable()) {
                                 valueView.setMovementMethod(LinkMovementMethod.getInstance());
                             }
                         }
                     }
                 }
 
                 break;
 
             case DYNAMIC_ROW:
                 detailRow = (ViewGroup) values[2];
                 detailRow.setVisibility(View.VISIBLE);
 
                 labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 valueView = (TextView) values[5];
                 final String value2 = (String) values[6];
                 valueView.setText(value2);
 
                 // handle the map
                 fieldName = (String) values[1];
                 if (ModuleFields.SHIPPING_ADDRESS_COUNTRY.equals(fieldName)
                                                 || ModuleFields.BILLING_ADDRESS_COUNTRY.equals(fieldName)) {
                     if (!TextUtils.isEmpty(value2)) {
                         valueView.setLinksClickable(true);
                         valueView.setClickable(true);
 
                         SpannableString spannableString = new SpannableString(value2);
                         spannableString.setSpan(new InternalURLSpan(new OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 Log.i(LOG_TAG, "trying to locate - " + value2);
                                 Uri uri = Uri.parse("geo:0,0?q=" + URLEncoder.encode(value2));
                                 Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                 intent.setData(uri);
                                 startActivity(intent);
                             }
                         }), 0, value2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
                         valueView.setText(spannableString);
 
                         // for trackball movement
                         MovementMethod m = valueView.getMovementMethod();
                         if ((m == null) || !(m instanceof LinkMovementMethod)) {
                             if (valueView.getLinksClickable()) {
                                 valueView.setMovementMethod(LinkMovementMethod.getInstance());
                             }
                         }
                     }
                 }
 
                 mDetailsTable.addView(detailRow);
                 break;
             }
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             try {
                mCursor = getContentResolver().query(getIntent().getData(), mSelectFields, null, null, mDbHelper.getModuleSortOrder(mModuleName));                
             } catch (Exception e) {
                 Log.e(LOG_TAG, e.getMessage(), e);
                 return Util.FETCH_FAILED;
             }
 
             return Util.FETCH_SUCCESS;
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
 
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
 
            setContents();
             // close the cursor irrespective of the result
             if (mCursor != null && !mCursor.isClosed())
                 mCursor.close();
 
             if (isCancelled())
                 return;
             int retVal = (Integer) result;
             switch (retVal) {
             case Util.FETCH_FAILED:
                 break;
             case Util.FETCH_SUCCESS:
                 break;
             default:
 
             }
 
             mProgressDialog.cancel();
         }
 
         private void setContents() {
 
             String[] detailsProjection = mSelectFields;
 
             if (mDbHelper == null)
                 mDbHelper = new DatabaseHelper(getBaseContext());
 
             TextView textViewForTitle = (TextView) findViewById(R.id.accountName);
             String title = "";
             List<String> titleFields = Arrays.asList(mDbHelper.getModuleListSelections(mModuleName));
 
             if (!isCancelled())
                 mCursor.moveToFirst();
 
             LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             List<String> billingAddressGroup = mDbHelper.getBillingAddressGroup();
             List<String> shippingAddressGroup = mDbHelper.getShippingAddressGroup();
             List<String> durationGroup = mDbHelper.getDurationGroup();
 
             String value = "";
             Map<String, ModuleField> fieldNameVsModuleField = mDbHelper.getModuleFields(mModuleName);
             Map<String, String> fieldsExcludedForDetails = mDbHelper.getFieldsExcludedForDetails();
 
             // LinearLayout tableRow = (LinearLayout)inflater.inflate(R.layout.table_row, null);
 
             int rowsCount = 0;
             for (int i = 0; i < detailsProjection.length; i++) {
                 // if the task gets cancelled
                 if (isCancelled())
                     break;
 
                 String fieldName = detailsProjection[i];
 
                 // if the field name is excluded in details screen, skip it
                 if (fieldsExcludedForDetails.containsKey(fieldName)) {
                     continue;
                 }
 
                 int columnIndex = mCursor.getColumnIndex(fieldName);
                 if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                     Log.d(LOG_TAG, "Col:" + columnIndex + " moduleName : " + mModuleName
                                                     + " fieldName : " + fieldName);
                 }
 
                 String tempValue = mCursor.getString(columnIndex);
 
                 // get the attributes of the moduleField
                 ModuleField moduleField = fieldNameVsModuleField.get(fieldName);
 
                 ViewGroup tableRow;
                 TextView textViewForLabel;
                 TextView textViewForValue;
                 // first two columns in the detail projection are ROW_ID and BEAN_ID
                 if (staticRowsCount > rowsCount) {
                     tableRow = (ViewGroup) mDetailsTable.getChildAt(rowsCount);
                     textViewForLabel = (TextView) tableRow.getChildAt(0);
                     textViewForValue = (TextView) tableRow.getChildAt(1);
                 } else {
                     tableRow = (ViewGroup) inflater.inflate(R.layout.table_row, null);
                     textViewForLabel = (TextView) tableRow.getChildAt(0);
                     textViewForValue = (TextView) tableRow.getChildAt(1);
                 }
 
                 // set the title
                 if (titleFields.contains(fieldName)) {
                     title = title + tempValue + " ";
                     publishProgress(HEADER, fieldName, textViewForTitle, title);
                     continue;
                 }
 
                 String label = moduleField.getLabel();
 
                 // check for the billing and shipping address groups only if the module is
                 // 'Accounts'
                 if (Util.ACCOUNTS.equals(mModuleName)) {
                     if (billingAddressGroup.contains(fieldName)) {
                         if (fieldName.equals(ModuleFields.BILLING_ADDRESS_STREET)) {
                             // First field in the group
                             value = (!TextUtils.isEmpty(tempValue)) ? tempValue + ", " : "";
                             continue;
                         } else if (fieldName.equals(ModuleFields.BILLING_ADDRESS_COUNTRY)) {
                             // last field in the group
 
                             value = value + (!TextUtils.isEmpty(tempValue) ? tempValue : "");
                             label = getBaseContext().getString(R.string.billing_address);
                         } else {
                             value = value + (!TextUtils.isEmpty(tempValue) ? tempValue + ", " : "");
                             continue;
                         }
                     } else if (shippingAddressGroup.contains(fieldName)) {
                         if (fieldName.equals(ModuleFields.SHIPPING_ADDRESS_STREET)) {
                             // First field in the group
                             value = (!TextUtils.isEmpty(tempValue)) ? tempValue + ", " : "";
                             continue;
                         } else if (fieldName.equals(ModuleFields.SHIPPING_ADDRESS_COUNTRY)) {
                             // Last field in the group
 
                             value = value + (!TextUtils.isEmpty(tempValue) ? tempValue : "");
                             label = getBaseContext().getString(R.string.shipping_address);
                         } else {
 
                             value = value + (!TextUtils.isEmpty(tempValue) ? tempValue + ", " : "");
                             continue;
                         }
                     } else {
                         value = tempValue;
                     }
                 } else if (durationGroup.contains(fieldName)) {
                     if (fieldName.equals(ModuleFields.DURATION_HOURS)) {
                         // First field in the group
                         value = (!TextUtils.isEmpty(tempValue)) ? tempValue + "hr " : "";
                         continue;
                     } else if (fieldName.equals(ModuleFields.DURATION_MINUTES)) {
                         // Last field in the group
                         value = value + (!TextUtils.isEmpty(tempValue) ? tempValue + "mins " : "");
                         label = getBaseContext().getString(R.string.duration);
                     }
                 } else {
                     value = tempValue;
                 }
 
                 if (moduleField.getType().equals("phone"))
                     textViewForValue.setAutoLinkMask(Linkify.PHONE_NUMBERS);
 
                 int command = staticRowsCount < rowsCount ? DYNAMIC_ROW : STATIC_ROW;
 
                 if (!TextUtils.isEmpty(value)) {
                     publishProgress(command, fieldName, tableRow, textViewForLabel, label, textViewForValue, value);
                 } else {
                     publishProgress(command, fieldName, tableRow, textViewForLabel, label, textViewForValue, getString(R.string.notAvailable));
                 }
 
                 rowsCount++;
 
             }
         }
     }
 
     static class InternalURLSpan extends ClickableSpan {
         OnClickListener mListener;
 
         public InternalURLSpan(OnClickListener listener) {
             super();
             mListener = listener;
         }
 
         @Override
         public void onClick(View widget) {
             Log.i("ModuleDetailsActivity", "InternalURLSpan onClick");
             mListener.onClick(widget);
         }
     }
 
     /*
      * private class RelationshipAdapter extends BaseAdapter {
      * 
      * private Context mContext;
      * 
      * private String[] relationships;
      * 
      * private LayoutInflater mInflater;
      * 
      * public RelationshipAdapter(Context context) { mContext = context; mInflater =
      * (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); }
      * 
      * public void setRelationshipArray(String[] relationshipArray) { relationships =
      * relationshipArray; }
      * 
      * @Override public int getCount() { return relationships.length; }
      * 
      * @Override public Object getItem(int position) { return relationships[position]; }
      * 
      * @Override public long getItemId(int position) { return position; }
      * 
      * @Override public View getView(int position, View convertView, ViewGroup parent) { View layout
      * = mInflater.inflate(R.layout.contact_listitem, null); TextView tv = ((TextView)
      * layout.findViewById(android.R.id.text1)); tv.setText(relationships[position]); // TODO:
      * either set the correct images or remove the image
      * tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.contacts),
      * null, null, null); return layout; }
      * 
      * }
      */
 }
