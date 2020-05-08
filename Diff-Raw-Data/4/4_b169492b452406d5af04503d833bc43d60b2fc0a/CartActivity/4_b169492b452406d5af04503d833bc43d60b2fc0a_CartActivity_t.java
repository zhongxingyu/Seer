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
 import android.view.*;
 import android.widget.*;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 public class CartActivity extends Activity {
 
     private static TextView moneyView;
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
 
         SharedPreferences appSharedPrefs = getSharedPreferences("PolyMeal",MODE_PRIVATE);
         cartAdapter = new CartItemAdapter(this, Cart.getCart(), Cart.getCartMoney());
 
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
                 onListClick.setMessage("Would you like to remove " + Cart.getCart().get(pos).replace("@#$", "") + " to your cart? \nPrice: " + "$" +  Cart.getCartMoney().get(pos));
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
        //cartAdapter.cart.remove(position);
        //cartAdapter.cartMoney.remove(position);
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
         totalAmount = MoneyTime.calcTotalMoney();
         setSubtitleColor();
         mActionBar.setSubtitle("$" + totalAmount + " Remaining");
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
         //mDrawerToggle.onConfigurationChanged(newConfig);
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
                 cartAdapter.clearCartMoney();
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
         private ArrayList<String> cart;
         private ArrayList<String> cartMoney;
 
         public CartItemAdapter(Context context, ArrayList<String> cart, ArrayList<String> cartMoney)
         {
             this.context = context;
             this.cart = cart;
             this.cartMoney = cartMoney;
         }
 
         public void clearCart()
         {
             cart.clear();
         }
 
         public void clearCartMoney()
         {
             cartMoney.clear();
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
             tvName.setText(cart.get(position).replace("@#$",""));
 
             TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
             tvPrice.setText("$" + cartMoney.get(position));
 
 
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
             cartMoney.remove(entry);
             updateBalance();
             isCartEmpty();
             notifyDataSetChanged();
         }
     }
     
 }
