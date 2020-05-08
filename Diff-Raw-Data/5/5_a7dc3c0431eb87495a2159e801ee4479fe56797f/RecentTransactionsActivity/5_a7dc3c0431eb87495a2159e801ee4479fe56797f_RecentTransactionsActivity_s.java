 package no.kantega.android.afp;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import no.kantega.android.afp.controllers.Transactions;
 import no.kantega.android.afp.models.Transaction;
 import no.kantega.android.afp.utils.FmtUtil;
 
 import java.util.Date;
 
 public class RecentTransactionsActivity extends ListActivity {
 
     private static final String TAG = OverviewActivity.class.getSimpleName();
     private Transactions db;
     private RecentTransactionsAdapter adapter;
     private Cursor cursor;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.transactions);
         this.db = new Transactions(getApplicationContext());
         this.cursor = db.getCursor();
         this.adapter = new RecentTransactionsAdapter(this, cursor);
         setListAdapter(adapter);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         new Thread(new Runnable() {
             @Override
             public void run() {
                 // Retrieve a new cursor in a thread, then do the actual swap on the UiThread
                 cursor = db.getCursor();
                 runOnUiThread(handler);
             }
         }).start();
     }
 
     private Runnable handler = new Runnable() {
         @Override
         public void run() {
             // Change to a fresh cursor, the old one will be automatically closed
             adapter.changeCursor(cursor);
         }
     };
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Object o = l.getItemAtPosition(position);
         if (o instanceof Cursor) {
             Cursor cursor = (Cursor) o;
             int transaction_id = cursor.getInt(cursor.getColumnIndex("_id"));
             Transaction t = db.getById(transaction_id);
             Intent intent;
             intent = new Intent(getApplicationContext(), EditTransactionActivity.class);
             intent.putExtra("transaction", t);
             startActivity(intent);
         }
     }
 
     private class RecentTransactionsAdapter extends CursorAdapter {
 
         public RecentTransactionsAdapter(Context context, Cursor c) {
             super(context, c);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             final LayoutInflater inflater = LayoutInflater.from(context);
             final View view = inflater.inflate(R.layout.transactionrow, parent, false);
             populateView(view, getCursor());
             return view;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             populateView(view, getCursor());
         }
 
         private void populateView(View view, Cursor cursor) {
             String date = cursor.getString(cursor.getColumnIndex("accountingDate"));
             String text = cursor.getString(cursor.getColumnIndex("text"));
             String tag = cursor.getString(cursor.getColumnIndex("tag"));
             String amount = cursor.getString(cursor.getColumnIndex("amountOut"));
             ImageView image = (ImageView) view.findViewById(R.id.tag_icon);
             TextView tv_date = (TextView) view.findViewById(R.id.trow_tv_date);
             TextView tv_text = (TextView) view.findViewById(R.id.trow_tv_text);
             TextView tv_tag = (TextView) view.findViewById(R.id.trow_tv_category);
             TextView tv_amount = (TextView) view.findViewById(R.id.trow_tv_amount);
             tv_date.setText(null);
             tv_text.setText(null);
             tv_tag.setText(null);
             image.setImageDrawable(null);
             tv_amount.setText(null);
             if (tv_date != null) {
                 Date d = FmtUtil.stringToDate("yyyy-MM-dd HH:mm:ss", date);
                 tv_date.setText(FmtUtil.dateToString("yyyy-MM-dd", d));
             }
             if (text != null) {
                 tv_text.setText(FmtUtil.trimTransactionText(text));
             }
             if (tag != null) {
                 tv_tag.setText(tag);
                 image.setImageDrawable(getImageIdByTag(tag));
             }
             if (amount != null) {
                 tv_amount.setText(amount);
             }
         }
     }
 
     private Drawable getImageIdByTag(String tag) {
         if ("Ferie".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_suitcase);
         } else if ("Klær".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_tshirt);
         } else if ("Restaurant".equals(tag)) {
            return getResources().getDrawable(R.drawable.tag_forkknife);
         } else if ("Dagligvarer".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_chicken);
         } else if ("Bil".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_fuel);
         } else if ("Vin".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_winebottle);
         } else if ("Datautstyr".equals(tag)) {
             return getResources().getDrawable(R.drawable.tag_imac);
         } else if ("Overtidsmiddag".equals(tag)) {
            return getResources().getDrawable(R.drawable.tag_forkknife);
         } else {
             return getResources().getDrawable(R.drawable.tag_user);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         db.close();
     }
 }
