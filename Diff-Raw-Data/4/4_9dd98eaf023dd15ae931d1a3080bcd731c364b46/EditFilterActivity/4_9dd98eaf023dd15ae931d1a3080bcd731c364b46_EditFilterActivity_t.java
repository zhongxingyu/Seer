 package edu.mit.rerun.view;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import edu.mit.rerun.R;
 import edu.mit.rerun.model.Filter;
 import edu.mit.rerun.utils.DatabaseAdapter;
 import edu.mit.rerun.utils.EditFilterListAdapter;
 import android.app.Activity;
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
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class EditFilterActivity extends ListActivity {
     public static final String TAG = "EditFilterActivity";
     private Context mContext = this;
     private DatabaseAdapter mDbAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_filter);
         mDbAdapter = new DatabaseAdapter(this);
 
         final ListView lv = getListView();
         LayoutInflater inflater = getLayoutInflater();
         View footer = inflater.inflate(R.layout.edit_filter_list_footer, null, false);
         lv.addFooterView(footer);
         
        ImageButton addKeyword = (ImageButton) footer.findViewById(R.id.add_keyword);
         Button cancel = (Button) footer.findViewById(R.id.cancel_btn);
         Button save = (Button) footer.findViewById(R.id.save_filter_btn);
         final EditText filterName = (EditText) findViewById(R.id.new_filter_name);
 
         
         //setListAdapter
         final ArrayList<String> rows = new ArrayList<String>();
 //        rows.add("0");
 //        rows.add("1");
 //        rows.add("2");
 
         EditFilterListAdapter adapter = new EditFilterListAdapter(this, rows, getParent());
         setListAdapter(adapter);
         
         Log.i(TAG, "after setting list adapter");
         //sample filter, to be retrieved from db later
         addKeyword.setOnClickListener(new View.OnClickListener() {
             
             public void onClick(View v) {
                 // TODO Auto-generated method stub
 
                 final Dialog dialog = new Dialog(v.getContext());
                 dialog.setContentView(R.layout.dialog_keyword_input);
                 dialog.setTitle("Add Keyword");
                 dialog.setCancelable(false);
                 
                 Log.i(TAG, "after creating dialog");
                 Button addBtn = (Button) dialog.findViewById(R.id.save_btn);
                 Button cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);
                 final EditText input = (EditText) dialog.findViewById(R.id.keyword_input);
                 addBtn.setOnClickListener(new View.OnClickListener() {
                     
                     public void onClick(View v) {
                         if (checkInput(input.getText().toString().trim())) {
                             rows.add(input.getText().toString().trim());
                             setListAdapter(new EditFilterListAdapter(mContext, rows, getParent()));
                             
                         }
                         dialog.dismiss();
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
                 finish();
             }
         });
         
         save.setOnClickListener(new View.OnClickListener() {
             
             public void onClick(View v) {
                 Toast.makeText(v.getContext(), "saving filter", Toast.LENGTH_SHORT).show();
                 Filter filter = new Filter(filterName.getText().toString(), true, new HashSet<String>(rows));
                 mDbAdapter.open();
                 //TODO validate filter name and keywords
                 mDbAdapter.addFilter(filter);
                 mDbAdapter.close();
                 Intent intent = new Intent(v.getContext(), FilterSettingsActivity.class);
                 startActivity(intent);
 
             }
         });
 
     }
     //TODO
     private boolean checkInput(String input) {
         return true;
     }
 }
