 package edu.mit.moneyManager.listUtils;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.model.DatabaseAdapter;
 import edu.mit.moneyManager.model.Expense;
 import edu.mit.moneyManager.view.ExpenseActivity;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.content.Context;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 public class MainExpenseListAdapter extends ArrayAdapter<Expense> {
     public static final String TAG = "MAIN EXPENSE LIST ADAPTER";
     private final Context context;
     private ArrayList<Expense> expenses;
     private LayoutInflater inflator;
     // pointer to dba
     private DatabaseAdapter mDBAdapter;
     private Map<String, Integer> mCategoryMap;
     //pointer to list of categories
     private List<String> categories;
 
     public MainExpenseListAdapter(Context context, ArrayList<Expense> expenses,
             DatabaseAdapter dba) {
         super(context, 0, expenses);
         this.expenses = expenses;
         this.context = context;
         this.mDBAdapter = dba;
         mDBAdapter.open();
         categories = mDBAdapter.getCategoryNames();
         mCategoryMap = getCategoryMap(categories);
         mDBAdapter.close();
         inflator = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         mDBAdapter.open();
         categories = mDBAdapter.getCategoryNames();
         mCategoryMap = getCategoryMap(categories);
         mDBAdapter.close();
         View view = convertView;
         Log.i(TAG, "getView() position: " + position);
         final Expense expense = expenses.get(position);
 
         if (expense != null) {
             // if (categories != null && categories.size() > 0) {
             // expense.setCategory(categories.get(0));
             // }
             view = inflator.inflate(R.layout.list_entry_expense_add, null);
 
             EditText newAmount = (EditText) view.findViewById(R.id.new_amount);
             newAmount.setInputType(InputType.TYPE_CLASS_NUMBER
                     | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 
             newAmount.addTextChangedListener(new TextWatcher() {
 
                 @Override
                 public void afterTextChanged(Editable s) {
                     try {
                         expense.setAmount(Double.parseDouble(s.toString()));
                     } catch (NumberFormatException e) {
                         expense.setAmount(0);
                     }
                 }
 
                 @Override
                 public void beforeTextChanged(CharSequence s, int start,
                         int count, int after) {
                 }
 
                 @Override
                 public void onTextChanged(CharSequence s, int start,
                         int before, int count) {
                 }
 
             });
 
            Log.i(TAG, "new amount: " + String.format("%.02f", expense.getAmount()));
            newAmount.setText(String.format("%.02f", expense.getAmount()));
             final Spinner spinner = (Spinner) view
                     .findViewById(R.id.category_spinner);
 
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                     android.R.layout.simple_spinner_item, categories);
             spinner.setAdapter(adapter);
             if (mCategoryMap.get(expense.getCategory()) != null) {
                 Log.i(TAG,
                         "SETTING SPINNER SELECTION: " + expense.getCategory()
                                 + " " + mCategoryMap.get(expense.getCategory()));
                 spinner.setSelection(mCategoryMap.get(expense.getCategory()));
             } else {
                 Log.i(TAG, "category not in map");
                 if (categories.size() > 0) {
                     spinner.setSelection(0);
                 }
             }
 
             spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
                 @Override
                 public void onItemSelected(AdapterView<?> parent, View view,
                         int position, long id) {
                
                     expense.setCategory(categories.get(position));
                    
                     spinner.setSelection(position);
 
                 }
 
                 @Override
                 public void onNothingSelected(AdapterView<?> arg0) {
                     // TODO Auto-generated method stub
                     Log.i(TAG, "ON NOTHING SELECTING");
                 }
             });
 
             final Button datePicker = (Button) view
                     .findViewById(R.id.edit_date_btn);
             datePicker.setText(expense.getDate());
             datePicker.setOnClickListener(new View.OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     Calendar c = Calendar.getInstance();
                     DatePickerDialog dialog = new DatePickerDialog(context,
                             new DatePickerDialog.OnDateSetListener() {
 
                                 @Override
                                 public void onDateSet(DatePicker view,
                                         int year, int monthOfYear,
                                         int dayOfMonth) {
                                     
                                     expense.setDate((new StringBuilder()
                                             .append(monthOfYear + 1)
                                             .append("/").append(dayOfMonth)
                                             .append("/").append(year)
                                             .append(" ").toString()));
                                     notifyDataSetChanged();
 
                                 }
                             }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                                     .get(Calendar.DAY_OF_MONTH));
                     dialog.show();
                 }
             });
 
             ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_expense);
             deleteBtn.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
                     expenses.remove(position);
                     notifyDataSetChanged();
                 }
             });
             
             view.setOnClickListener(null);
             view.setOnLongClickListener(null);
             view.setLongClickable(false);
 
         }
 
         return view;
     }
 
     /*
     private class SpinnerArrayAdapter extends ArrayAdapter<String> {
         private ArrayList<String> categories;
 
         public SpinnerArrayAdapter(ArrayList<String> categories) {
             super(context, 0, categories);
             this.categories = categories;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View view = convertView;
             String categoryName = categories.get(position);
             if (categoryName != null) {
                 view = inflator.inflate(android.R.layout.simple_spinner_item,
                         null);
 
             }
             return view;
         }
     }
     */
     // for persisting spinner selection
     private Map<String, Integer> getCategoryMap(List<String> categories) {
         Map<String, Integer> map = new HashMap<String, Integer>();
         for (int i = 0; i < categories.size(); i++) {
             map.put(categories.get(i), i);
         }
         return map;
     }
 }
