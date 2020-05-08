 package com.swiftly.android;
 
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.view.Menu;
 
 import java.text.NumberFormat;
 import java.util.Locale;
 
 public class CartActivity extends BaseActivity {
     private ItemsDbAdapter mDbHelper;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.cart);
 
         mDbHelper = ((MyApplication) getApplication()).getDatabaseAdapter();
 
         populateCartView();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         boolean result = super.onCreateOptionsMenu(menu);
         menu.removeItem(mCartItemId);
         return result;
     }
 
     private String getAmountStr(double amount) {
         NumberFormat form = NumberFormat.getCurrencyInstance(Locale.US);
         return form.format(amount);
     }
 
     private void setTotal(Cursor cartCursor) {
         double total = 0;
        for (cartCursor.moveToFirst(); cartCursor != null; cartCursor.moveToNext()) {
          total += cartCursor.getFloat(cartCursor.getColumnIndex(ItemsDbAdapter.KEY_ITEM_PRICE));
         }
 
         ((TextView) findViewById(R.id.total)).setText(getAmountStr(total));
     }
 
     private Cursor getCart() {
         Cursor cursor = mDbHelper.getCart();
         setTotal(cursor);
         return cursor;
     }
 
     private void populateCartView() {
         Cursor cursor = getCart();
 
         // The desired columns to be bound
         String[] columns = new String[] {
                 ItemsDbAdapter.KEY_ITEM_NAME,
                 ItemsDbAdapter.KEY_ITEM_PRICE,
                 ItemsDbAdapter.KEY_ITEM_LOCAL_IMG_PATH
         };
 
         // the XML defined views which the data will be bound to
         int[] to = new int[] {
                 R.id.name,
                 R.id.price,
                 R.id.item_thumbnail
         };
 
         final ListView listView = (ListView) findViewById(R.id.listView1);
 
         // create the adapter using the cursor pointing to the desired data
         //as well as the layout information
         SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(
                 this, R.layout.cart_item, cursor, columns, to, 0) {
             @Override
             public View getView(int position, View convertView, ViewGroup parent) {
                 View v = super.getView(position, convertView, parent);
 
                 final int pos = position;
                 v.findViewById(R.id.delete_item).setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Item item = mDbHelper.getItemFromCursor((Cursor) listView.getItemAtPosition(pos));
                         item.cartQuantity = 0;
                         mDbHelper.updateItem(item);
                         changeCursor(getCart());
                     }
                 });
                 return v;
             }
         };
 
         // more at http://www.mysamplecode.com/2012/07/android-listview-cursoradapter-sqlite.html
 
         // Assign adapter to ListView
         listView.setAdapter(dataAdapter);
     }
 }
