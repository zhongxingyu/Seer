 package net.solutinno.tutorial.multiselect;
 
 import android.database.MatrixCursor;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 public class MainActivity extends ActionBarActivity {
 
     private static final String COLUMN_ID = "_id";
     private static final String COLUMN_TITLE = "title";
     private static final String COLUMN_SUBTITLE = "subtitle";
 
     ListView mListView;
     SimpleCursorAdapter mAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         String[] columns = {COLUMN_TITLE, COLUMN_SUBTITLE};
         int[] fields = {R.id.textTitle, R.id.textSubtitle};
         mAdapter = new SimpleCursorAdapter(this, R.layout.listview_item, getData(), columns, fields, 0);
 
         mListView = (ListView) findViewById(R.id.listView);
         mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
         mListView.setItemsCanFocus(true);
         mListView.setOnItemClickListener(mOnItemClickListener);
         mListView.setOnItemLongClickListener(mOnItemLongClickListener);
         mListView.setAdapter(mAdapter);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (mListView.getCheckedItemIds().length < 2) mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
         }
     };
     private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
         @Override
         public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
             mListView.setItemChecked(i, !mListView.isItemChecked(i));
            if (mListView.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
             return true;
         }
     };
 
     private MatrixCursor getData() {
         String title = getResources().getString(R.string.test_text_title);
         String subtitle = getResources().getString(R.string.test_text_subtitle);
         MatrixCursor result = new MatrixCursor(new String[] { COLUMN_ID, COLUMN_TITLE, COLUMN_SUBTITLE });
         for (int i = 0; i < 100; ++i) {
             result.newRow().add(i).add(title + String.valueOf(i)).add(subtitle + String.valueOf(i));
         }
         return result;
     }
 }
