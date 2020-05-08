 package edu.mit.rerun.view;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import edu.mit.rerun.R;
 import edu.mit.rerun.model.Filter;
 import edu.mit.rerun.utils.DatabaseAdapter;
 import edu.mit.rerun.utils.EditFilterListAdapter;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditFilterActivity extends ListActivity {
     public static final String TAG = "EditFilterActivity";
     private Context mContext = this;
     private DatabaseAdapter mDbAdapter;
     private String oldFilterName;
     private boolean newFilter = true;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_filter);
         Bundle extras = getIntent().getExtras();
         mDbAdapter = new DatabaseAdapter(this);
         TextView header = (TextView) findViewById(R.id.header);
         final EditText filterName = (EditText) findViewById(R.id.new_filter_name);
 
         // editing existing filter
         if (extras != null) {
             oldFilterName = extras.getString("filterName");
             if (oldFilterName != null) {
                 header.setText("Filter: " + oldFilterName);
                 filterName.setText(oldFilterName);
                 newFilter = false;
             }
         }
 
         final ListView lv = getListView();
         LayoutInflater inflater = getLayoutInflater();
         View footer = inflater.inflate(R.layout.edit_filter_list_footer, null,
                 false);
         lv.addFooterView(footer);
 
         ImageButton addKeyword = (ImageButton) footer
                 .findViewById(R.id.add_keyword);
         Button cancel = (Button) footer.findViewById(R.id.cancel_btn);
         Button save = (Button) footer.findViewById(R.id.save_filter_btn);
 
         // setListAdapter
         final ArrayList<String> rows = new ArrayList<String>();
         if (!newFilter) {
             mDbAdapter.open();
             Filter filter = mDbAdapter.getFilter(oldFilterName);
             for (String keyword : filter.getKeyWords()) {
                rows.add(keyword);
             }
             mDbAdapter.close();
         }
 
         EditFilterListAdapter adapter = new EditFilterListAdapter(this, rows,
                 getParent());
         setListAdapter(adapter);
 
         Log.i(TAG, "after setting list adapter");
         // sample filter, to be retrieved from db later
         addKeyword.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 final Dialog dialog = new Dialog(v.getContext());
                 dialog.setContentView(R.layout.dialog_keyword_input);
                 dialog.setTitle("Add Keyword");
                 dialog.setCancelable(false);
 
                 Button addBtn = (Button) dialog.findViewById(R.id.save_btn);
                 Button cancelBtn = (Button) dialog
                         .findViewById(R.id.cancel_btn);
                 final EditText input = (EditText) dialog
                         .findViewById(R.id.keyword_input);
                 addBtn.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                         if (checkKeywordInput(input.getText().toString().trim())) {
                             rows.add(input.getText().toString().trim());
                             setListAdapter(new EditFilterListAdapter(mContext,
                                     rows, getParent()));
                             dialog.dismiss();
                         } else {
                             Toast.makeText(mContext,
                                     "please enter a valid keyword",
                                     Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
                 cancelBtn.setOnClickListener(new View.OnClickListener() {
 
                     public void onClick(View v) {
                         dialog.dismiss();
                     }
                 });
 
                 dialog.show();
             }
         });
 
         cancel.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
                 mDbAdapter.close();
                 finish();
             }
         });
 
         save.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View v) {
              // validates filter name as alphanumeric
                 String filterNameStr = filterName.getText().toString();
 
                 if (!filterNameStr.matches("[\\p{Alnum}[\\-]]*")) {
                     //error- non-alphanumerics found
                     Toast.makeText(v.getContext(), "Filter name contains invalid characters, please use alphanumeric characters only", Toast.LENGTH_SHORT).show();
                 }
                 else {
 
                     Filter filter = new Filter(filterNameStr,true, new HashSet<String>(rows));
                     mDbAdapter.open();
 
                     // removes old filter
                     if (!newFilter && oldFilterName != null) {
                         mDbAdapter.removeFilter(oldFilterName);
                     }
                     if (!mDbAdapter.filterExist(filter.getFiltername())) {
                         mDbAdapter.addFilter(filter);
                         mDbAdapter.close();
 
                         setResult(ItemListActivity.ADD_FILTER_RESULT);
                         finish();
                     } else {
                         Toast.makeText(v.getContext(), "Filter already exists, please enter another filter name",
                                 Toast.LENGTH_SHORT).show();
                     }
                 }
             }
         });
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == ItemListActivity.ADD_FILTER_RESULT) {
             Intent intent = new Intent((Context) this,
                     FilterSettingsActivity.class);
             startActivity(intent);
 
         }
     }
 
 
 	// TODO, check to see that keyword is unique
 	private boolean checkKeywordInput(String input) {
 		boolean valid = true;
 		
 		// validates keyword name as alphanumeric
 		if (!input.matches("[\\p{Alnum}[\\-]]*")) {
 			valid = false;
 		}
 		if (input == null || input.length() == 0) {
 			valid = false;
 		}
 		return valid;
 	}
 }
