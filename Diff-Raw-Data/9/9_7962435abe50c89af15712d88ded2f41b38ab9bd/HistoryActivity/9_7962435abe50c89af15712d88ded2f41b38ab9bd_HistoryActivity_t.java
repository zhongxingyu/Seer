 package de.winterberg.android.money;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static de.winterberg.android.money.Constants.*;
 
 /**
  * Activity for showing all history entries of a category.
  *
  * @author Benjamin Winterberg
  */
 public class HistoryActivity extends ListActivity implements AmountDaoAware {
     private String category;
     private SimpleDateFormat simpleDateFormat;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.history);
         category = getIntent().getStringExtra(MoneyActivity.KEY_CATEGORY);
         simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         refreshData();
     }
 
     private void refreshData() {
         Cursor cursor = getAmountDao().findAll(category);
         startManagingCursor(cursor);
         SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.history_item, cursor,
                 new String[]{
                         TIME,
                         ACTION,
                         VALUE,
                         AMOUNT
                 },
                 new int[]{
                         R.id.history_time,
                         R.id.history_action,
                         R.id.history_diff,
                         R.id.history_amount
                 });
 
         simpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
             public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                 if (columnIndex != 1)
                     return false;
 
                long timeInMillis = cursor.getLong(columnIndex);
                 String formattedString = simpleDateFormat.format(new Date(timeInMillis));
                 TextView textView = (TextView) view;
                 textView.setText(formattedString);
                 return true;
             }
         });
 
         setListAdapter(simpleCursorAdapter);
     }
 
     public AmountDao getAmountDao() {
         MoneyApplication application = (MoneyApplication) getApplication();
         return application.getAmountDao();
     }
 }
