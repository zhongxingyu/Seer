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
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.*;
 import android.widget.*;
 import com.mustangexchange.polymeal.Sorting.ItemNameComparator;
 import com.mustangexchange.polymeal.Sorting.ItemPriceComparator;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class CartActivity extends Activity {
 
     private ListView lv;
     private CartItemAdapter cartAdapter;
     private static BigDecimal totalAmount;
     private static ActionBar mActionBar;
 
     private static Context mContext;
     public static Activity activity;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cart);
 
         cartAdapter = new CartItemAdapter(this, updateSettings());
         mContext = this;
         activity = this;
 
         lv = (ListView)findViewById(R.id.listView);
         lv.setAdapter(cartAdapter);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
                 final int fPos = pos;
                 final AlertDialog.Builder onListClick= new AlertDialog.Builder(activity);
                 onListClick.setCancelable(false);
                 onListClick.setTitle("Remove to Cart?");
                 onListClick.setMessage("Would you like to remove " + Cart.getCart().get(pos).getName() + " to your cart? \nPrice: " +  Cart.getCart().get(pos).getPriceString());
                 onListClick.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                         removeFromCart(fPos);
                     }
                 });
                 onListClick.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int button) {
                     }
                 });
                 onListClick.show();
             }
         });
 
         mActionBar = getActionBar();
         updateBalance();
 
         isCartEmpty();
     }
 
     public void isCartEmpty()
     {
         if(lv.getAdapter().getCount() > 0) {
         }
         else
         {
             setContentView(R.layout.empty_cart);
         }
     }
 
     public void removeFromCart(int position) {
         Cart.remove(position);
         updateBalance();
         isCartEmpty();
         cartAdapter.notifyDataSetChanged();
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
 
     public ArrayList<Item> updateSettings() {
         ArrayList<Item> cart = new ArrayList<Item>();
         try
         {
             cart = Cart.getCart();
             SharedPreferences defaultSp = PreferenceManager.getDefaultSharedPreferences(this);
             int sortMode;
 
             sortMode = Integer.valueOf(defaultSp.getString("sortMode", "0"));
 
             if(sortMode == 0) {
                 Collections.sort(cart, new ItemNameComparator());
             }
             else if(sortMode == 1)
             {
                 Collections.sort(cart, new ItemNameComparator());
                 Collections.reverse(cart);
             } else if(sortMode == 2)
             {
                 Collections.sort(cart, new ItemPriceComparator());
             }
             else
             {
                 Collections.sort(cart, new ItemPriceComparator());
                 Collections.reverse(cart);
             }
             return cart;
             }
         catch (NullPointerException e)
         {
             Intent intentHome = new Intent(mContext, MainActivity.class);
             intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
             mContext.startActivity(intentHome);
         }
         return cart;
     }
 
     public void onResume()
     {
         super.onResume();
        cartAdapter.updateCart();
         updateBalance();
         updateSettings();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.cart, menu);
         return super.onCreateOptionsMenu(menu);
     }
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Pass any configuration change to the drawer toggles
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case R.id.menuCom:
                 Intent intent = new Intent(this, CompleteorActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.clrCart:
                 Cart.clear();
                 cartAdapter.clearCart();
                 cartAdapter.notifyDataSetChanged();
                 isCartEmpty();
                 updateBalance();
                 Toast.makeText(this, "Cart Cleared!", Toast.LENGTH_SHORT).show();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public class CartItemAdapter extends BaseAdapter implements View.OnClickListener {
         private Context context;
         private ArrayList<Item> cart;
 
         public CartItemAdapter(Context context, ArrayList<Item> cart)
         {
             this.context = context;
             this.cart = cart;
         }
 
        public void updateCart()
        {
            notifyDataSetChanged();
        }

         public void clearCart()
         {
             cart.clear();
         }
 
         public int getCount()
         {
             return cart.size();
         }
 
         public Object getItem(int position)
         {
             return cart.get(position);
         }
 
         public long getItemId(int position)
         {
             return position;
         }
 
         public View getView(int position, View convertView, ViewGroup viewGroup)
         {
             Integer entry = position;
             if (convertView == null) {
                 LayoutInflater inflater = (LayoutInflater) context
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 convertView = inflater.inflate(R.layout.row_item_cart, null);
             }
             TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
             tvName.setText(cart.get(position).getName().replace("@#$",""));
 
             TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
             tvPrice.setText("$" + cart.get(position).getPrice());
 
 
             //Set the onClick Listener on this button
             ImageButton btnRemove = (ImageButton) convertView.findViewById(R.id.btn_rmv);
             btnRemove.setFocusableInTouchMode(false);
             btnRemove.setFocusable(false);
             btnRemove.setOnClickListener(this);
             btnRemove.setTag(entry);
 
             return convertView;
         }
 
         @Override
         public void onClick(View view)
         {
             Integer entry = (Integer) view.getTag();
             Cart.remove(entry);
             cart.remove(entry);
             updateBalance();
             isCartEmpty();
             notifyDataSetChanged();
         }
     }
     
 }
