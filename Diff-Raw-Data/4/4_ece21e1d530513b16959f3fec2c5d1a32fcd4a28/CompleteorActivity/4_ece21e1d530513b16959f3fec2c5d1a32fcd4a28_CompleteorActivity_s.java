 package com.mustangexchange.polymeal;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.*;
 import android.widget.*;
 import com.mustangexchange.polymeal.Sorting.ItemNameComparator;
 import com.mustangexchange.polymeal.Sorting.ItemPriceComparator;
 
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class CompleteorActivity extends Activity {
 
     private ItemSet possibleItems;
     private ItemSet dummyItems;
 
     private ActionBar mActionBar;
     private static BigDecimal totalAmount;
     private static Context mContext;
     private static Activity activity;
     public ListView lv;
     public CompleteorItemAdapter dummyAdapter;
     public CompleteorItemAdapter lvAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_completeor);
 
         mContext = this;
         activity = this;
         mActionBar = getActionBar();
         mActionBar.setDisplayHomeAsUpEnabled(true);
         mActionBar.setHomeButtonEnabled(true);
 
         //possibleItems = new ItemSet("Completeor",new ArrayList<String>(),new ArrayList<String>());
         //dummyItems = new ItemSet("Completeor",new ArrayList<String>(),new ArrayList<String>());
         possibleItems = new ItemSet();
         dummyItems = new ItemSet();
         dummyAdapter = new CompleteorItemAdapter(this, dummyItems);
         lvAdapter = new CompleteorItemAdapter(this, possibleItems);
 
         lv = (ListView) findViewById(R.id.listView);
         lv.setAdapter(dummyAdapter);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
                 final int fPos = pos;
                 final AlertDialog.Builder onListClick= new AlertDialog.Builder(activity);
                 onListClick.setCancelable(false);
                 onListClick.setTitle("Add to Cart?");
                 onListClick.setMessage("Would you like to add " + possibleItems.getItem(pos).getName() + " to your cart? Price: " + possibleItems.getItem(pos).getPriceString());
                 onListClick.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         //money = boundPrices.get(tempIndex);
                         if (possibleItems.getItem(fPos).getOunces()) {
                             AlertDialog.Builder onYes = new AlertDialog.Builder(activity);
                             onYes.setTitle("How much?");
                             onYes.setMessage("Estimated Number of Ounces: ");
                             LayoutInflater inflater = activity.getLayoutInflater();
                             View DialogView = inflater.inflate(R.layout.number_picker, null);
                             final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                             np.setMinValue(1);
                             np.setMaxValue(50);
                             np.setWrapSelectorWheel(false);
                             np.setValue(1);
                             onYes.setView(DialogView);
                             onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
                                     possibleItems.getItem(fPos).setPrice(new BigDecimal(new BigDecimal(np.getValue())+"").multiply(possibleItems.getItem(fPos).getPrice()));
                                     Cart.add(possibleItems.getItem(fPos));
                                     updateBalance();
                                     updateList();
                                 }
                             });
                             onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
                                 }
                             });
                             onYes.show();
                         } else {
                             Cart.add(possibleItems.getItem(fPos));
                             updateBalance();
                             updateList();
                         }
                     }
                 });
                 onListClick.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                 });
                 onListClick.setNeutralButton("Description", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         AlertDialog.Builder onDialogClick = new AlertDialog.Builder(activity);
                         onDialogClick.setTitle("Description");
                         onDialogClick.setMessage(possibleItems.getItem(fPos).getDescription());
                         onDialogClick.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
 
                             }
                         });
                         onDialogClick.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int button) {
                                 onListClick.show();
                             }
                         });
                         onDialogClick.show();
                     }
                 });
                 onListClick.show();
             }
         });
 
         new calcCompleteor().execute("");
 
         updateBalance();
     }
 
     public void checkLayout() {
         if((lv.getAdapter().getCount() == 0) && (lv.getAdapter() == lvAdapter)) {
             setContentView(R.layout.empty_completeor);
         }
     }
 
     public void onResume()
     {
         super.onResume();
         updateBalance();
        updateSettings();
     }
 
     public void updateList() {
         new calcCompleteor().execute("");
     }
 
     public void setSubtitleColor() {
         int titleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
         TextView yourTextView = (TextView)findViewById(titleId);
         if(totalAmount.compareTo(BigDecimal.ZERO) < 0)
         {
             yourTextView.setTextColor(Color.RED);
         }
         else
         {
             yourTextView.setTextColor(Color.WHITE);
         }
     }
 
     public void updateBalance() {
         try
         {
             totalAmount = MoneyTime.calcTotalMoney();
             setSubtitleColor();
             mActionBar.setSubtitle("$" + totalAmount + " Remaining");
         }
         catch (NullPointerException e)
         {
             Intent intentHome = new Intent(mContext, MainActivity.class);
             intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             mContext.startActivity(intentHome);
         }
     }
 
     public void updateSettings()
     {
         try
         {
             int sortMode;
             SharedPreferences defaultSP = PreferenceManager.getDefaultSharedPreferences(this);
 
             sortMode = Integer.valueOf(defaultSP.getString("sortMode", "1"));
             if(sortMode == 0)
             {
                 Collections.sort(possibleItems.getItems(), new ItemPriceComparator());
                 Collections.reverse(possibleItems.getItems());
             }
             else
             {
                 Collections.sort(possibleItems.getItems(), new ItemNameComparator());
             }
         }
         catch (NullPointerException e)
         {
             Intent intentHome = new Intent(mContext, MainActivity.class);
             intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             mContext.startActivity(intentHome);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.completeor, menu);
         return super.onCreateOptionsMenu(menu);
     }
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggles
         //mDrawerToggle.onConfigurationChanged(newConfig);
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case android.R.id.home:
                 if(MainActivity.vgOrSand == 1)
                 {
                     Intent intent = new Intent(this, VistaActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     return true;
                 }
                 else
                 {
                     Intent intent = new Intent(this, SandwichActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(intent);
                     return true;
                 }
             case R.id.cart:
                 Intent intent = new Intent(this, CartActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public class CompleteorItemAdapter extends BaseAdapter implements View.OnClickListener {
         private Context context;
         private ItemSet possibleItems;
 
         public CompleteorItemAdapter(Context context, ItemSet possibleItems) {
             this.context = context;
             this.possibleItems = possibleItems;
         }
 
         public ItemSet getPossibleItems() {
             return possibleItems;
         }
 
         public int getCount() {
             return possibleItems.size();
         }
 
         public Object getItem(int position) {
             return possibleItems.getItem(position);
         }
 
         public long getItemId(int position) {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup viewGroup) {
             //ItemSet entry = setList.get(position);
             if (convertView == null) {
                 LayoutInflater inflater = (LayoutInflater) context
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = inflater.inflate(R.layout.row_item, null);
             }
             TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
             tvName.setText(possibleItems.getItem(position).getName());
 
             TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
             tvPrice.setText(possibleItems.getItem(position).getPriceString());
 
             //Set the onClick Listener on this button
             ImageButton btnAdd = (ImageButton) convertView.findViewById(R.id.btn_add);
             btnAdd.setFocusableInTouchMode(false);
             btnAdd.setFocusable(false);
             btnAdd.setOnClickListener(this);
             btnAdd.setTag(new Integer(position));
 
             return convertView;
         }
 
         @Override
         public void onClick(View view) {
             final int position = (Integer) view.getTag();
             if (possibleItems.getItem(position).getOunces()) {
                 AlertDialog.Builder onYes = new AlertDialog.Builder(activity);
                 onYes.setTitle("How much?");
                 onYes.setMessage("Estimated Number of Ounces: ");
                 LayoutInflater inflater = (LayoutInflater) activity.getLayoutInflater();
                 View DialogView = inflater.inflate(R.layout.number_picker, null);
                 final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                 np.setMinValue(1);
                 np.setMaxValue(50);
                 np.setWrapSelectorWheel(false);
                 np.setValue(1);
                 onYes.setView(DialogView);
                 onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         possibleItems.getItem(position).setPrice(new BigDecimal(new BigDecimal(np.getValue())+"").multiply(possibleItems.getItem(position).getPrice()));
                         Cart.add(possibleItems.getItem(position));
                         Toast.makeText(activity, possibleItems.getItem(position).getName(), Toast.LENGTH_SHORT).show();
                         updateBalance();
                         updateList();
                     }
                 });
                 onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                 });
                 onYes.show();
             }
             else
             {
                 Cart.add(possibleItems.getItem(position));
                 updateBalance();
                 Toast.makeText(activity, possibleItems.getItem(position).getName() + " added to Cart!", Toast.LENGTH_SHORT).show();
                 updateList();
             }
         }
     }
 
     private class calcCompleteor extends AsyncTask<String, Void, String> {
 
         @Override
         protected String doInBackground(String... params) {
             if(MainActivity.vgOrSand==1)
             {
                 System.out.println(ItemSetContainer.vgItems.get(0).getTitle());
                 for(int i = 0;i<ItemSetContainer.vgItems.size();i++)
                 {
                     for(int j = 0;j<ItemSetContainer.vgItems.get(i).size();j++)
                     {
                         if(MoneyTime.calcTotalMoney().compareTo(ItemSetContainer.vgItems.get(i).getItem(j).getPrice())>=0 && !ItemSetContainer.vgItems.get(i).getItem(j).getPriceString().equals(""))
                         {
                             /*possibleItems.getNames().add(ItemSetContainer.vgItems.get(i).getNames().get(j));
                             possibleItems.getPrices().add(ItemSetContainer.vgItems.get(i).getPrices().get(j));
                             possibleItems.getDesc().add(ItemSetContainer.vgItems.get(i).getDesc().get(j));*/
                             possibleItems.add(ItemSetContainer.vgItems.get(i).getItem(j));
                         }
                     }
                 }
             }
             else if(MainActivity.vgOrSand==2)
             {
                 for(int i = 0;i<ItemSetContainer.sandItems.size();i++)
                 {
                     for(int j = 0;j<ItemSetContainer.sandItems.get(i).size();j++)
                     {
                         System.out.println(i + " ," + j);
                         if(MoneyTime.calcTotalMoney().compareTo(ItemSetContainer.sandItems.get(i).getItem(j).getPrice())>=0 && !ItemSetContainer.sandItems.get(i).getItem(j).getPriceString().equals(""))
                         {
                             /*possibleItems.getNames().add(ItemSetContainer.sandItems.get(i).getNames().get(j));
                             possibleItems.getPrices().add(ItemSetContainer.sandItems.get(i).getPrices().get(j));
                             possibleItems.getDesc().add(ItemSetContainer.sandItems.get(i).getDesc().get(j));*/
                             possibleItems.add(ItemSetContainer.sandItems.get(i).getItem(j));
                         }
                     }
                 }
             }
             return "Executed";
         }
 
         @Override
         protected void onPostExecute(String result) {
             updateSettings();
             lvAdapter.notifyDataSetChanged();
             lv.setAdapter(lvAdapter);
             checkLayout();
         }
 
         @Override
         protected void onPreExecute() {
             lv.setAdapter(dummyAdapter);
             possibleItems.clear();
             lvAdapter.getPossibleItems().clear();
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
         }
     }
 }
