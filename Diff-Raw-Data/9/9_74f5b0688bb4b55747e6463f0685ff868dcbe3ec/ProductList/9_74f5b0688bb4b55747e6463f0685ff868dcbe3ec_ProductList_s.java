 package com.tedebold.buylist;
 import android.net.Uri;
 import android.text.Html;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import com.google.ads.*;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import com.tedebold.provider.ProductProvider;
 import com.tedebold.provider.helpers.ProductHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ProductList extends ListActivity {
   private AdView adView;
   ProductProvider provider;
   Cursor productsList = null;
   ArrayAdapter<String> adapter;
   List<String> checkedProducts;
   private static final String MY_AD_UNIT_ID = "a14f6b426ebd4fc";
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.buylist);
     addAds();
     checkedProducts = new ArrayList<String>();
     fillData();
     Button finishButton = (Button) this.findViewById(R.id.finish);
     finishButton.setOnClickListener(new OnClickListener() {
       public void onClick(View v) {
         for (String id : checkedProducts) {
           String where = ProductHelper.Products._ID + "=" + id;
           ContentValues values = new ContentValues();
           values.put(ProductHelper.Products.IN_LIST.toString(), "false");
           getContentResolver().update(ProductHelper.Products.CONTENT_URI, values, where, null);
         }
         fillData();
       }
     });
 
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     fillData();
   }
 
   public void onListItemClick(ListView parent, View v, int position, long id) {
     CheckedTextView textview = (CheckedTextView) v;
     if (textview.isChecked()) {
       checkedProducts.remove(Long.toString(id));
     } else {
       checkedProducts.add(Long.toString(id));
     }
     textview.setChecked(!textview.isChecked());
   }
 
   public void addToList(View v) {
     Intent newIntent = new Intent(v.getContext(), Save.class);
     startActivity(newIntent);
   }
 
   private void fillData() {
     String[] displayFields = new String[]{
         ProductHelper.Products.NAME,
         ProductHelper.Products._ID,
         ProductHelper.Products.IN_LIST
     };
     productsList = getProductsList(displayFields);
 
     int[] displayViews = new int[]{
         android.R.id.text1
     };
     this.setListAdapter(new SimpleCursorAdapter(this,
         android.R.layout.simple_list_item_checked, productsList,
         displayFields, displayViews));
 
     ListView listview = getListView();
     //listview.setItemChecked(0, true);
   }
 
   private void addAds(){
     // Create the adView
     adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);
     LinearLayout layout = (LinearLayout)findViewById(R.id.adsList);
     // Add the adView to it
     layout.addView(adView);
     // Initiate a generic request to load it with an ad
     adView.loadAd(new AdRequest());
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     String productsMessage = "";
    productsList.moveToFirst();
     do {
       productsMessage += productsList.getString(0) + ", ";
     } while (productsList.moveToNext());
 
     switch (item.getItemId()) {
       case R.id.message: {
         Intent sendIntent = new Intent(Intent.ACTION_VIEW);
         sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", productsMessage.substring(0,productsMessage.length() - 2));
         startActivity(sendIntent);
         break;
       }
       case R.id.email: {
         Intent emailIntent = new Intent(Intent.ACTION_SEND);
         emailIntent.setType("text/html");
         emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Buylist");
         emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(productsMessage));
         startActivity(Intent.createChooser(emailIntent,  getString(R.string.emailSelector)));
         break;
       }
     }
     return true;
   }
 
   private Cursor getProductsList(String [] displayFields) {
     String where = ProductHelper.Products.IN_LIST + "='true'";
     return managedQuery(ProductHelper.Products.CONTENT_URI, displayFields, where, null, null);
   }
 }
