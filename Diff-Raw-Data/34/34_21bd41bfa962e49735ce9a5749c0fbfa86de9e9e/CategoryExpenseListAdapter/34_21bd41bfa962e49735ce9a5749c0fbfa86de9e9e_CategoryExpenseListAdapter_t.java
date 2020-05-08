 package edu.mit.moneyManager.listUtils;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.model.DatabaseAdapter;
 import edu.mit.moneyManager.model.Expense;
 import edu.mit.moneyManager.view.HomeActivity;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.text.InputType;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CategoryExpenseListAdapter extends ArrayAdapter<Expense> {
     public static final String TAG = "CATEGORY EXPENSE LIST";
     private Context context;
     private final Context parentContext;
     private ArrayList<Expense> expenses;
     private LayoutInflater inflator;
     private DatabaseAdapter mDBAdapter;
     private List<String> categories;
     private Map<String, Integer> mCategoryMap;
     // pointer to remainingView;
     private TextView remainingView;
     // pointer to settings
     private SharedPreferences settings;
     private String categoryName;
 
     public CategoryExpenseListAdapter(Context context,
             ArrayList<Expense> expenses, Context parent, DatabaseAdapter dba,
             String categoryName, TextView view, SharedPreferences settings) {
         super(context, 0, expenses);
         this.expenses = expenses;
         this.context = context;
         this.parentContext = parent;
         this.categoryName = categoryName;
         this.remainingView = view;
         this.settings = settings;
         mDBAdapter = dba;
         mDBAdapter.open();
         categories = mDBAdapter.getCategoryNames();
         mDBAdapter.close();
         mCategoryMap = getCategoryMap(categories);
 
         inflator = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public View getView(final int position, View convertView, ViewGroup parent) {
         Log.i(TAG, "getView()");
 
         View view = convertView;
         // if (position < expenses.size() - 1) {
         final Expense expense = expenses.get(position);
         Log.i(TAG, new Boolean(expense.getCategory().equals(categoryName))
                 .toString());
         if (expense != null && expense.getCategory().equals(categoryName)) {
             Log.i(TAG, "inflating convertView");
             view = inflator.inflate(R.layout.list_entry_expense_edit, null);
 
             view.setOnClickListener(null);
             view.setOnLongClickListener(null);
             view.setLongClickable(false);
 
             final TextView date = (TextView) view
                     .findViewById(R.id.expense_date);
             final TextView amount = (TextView) view
                     .findViewById(R.id.expense_amount);
 
             date.setText(expense.getDate());
            amount.setText("$" + String.format("%.02f", expense.getAmount()));

 
             ImageView edit = (ImageView) view
                     .findViewById(R.id.edit_expense_btn);
             // Log.i("CategoryExpenseList adapter", (String)edit.getText());
             if (settings.getBoolean(HomeActivity.VIEWING_OTHER_BUDGET, false)) {
                 edit.setClickable(false);
                 edit.setVisibility(View.INVISIBLE);
             }
             else {
                 edit.setClickable(true);
                 edit.setVisibility(View.VISIBLE);
             }
             
             edit.setOnClickListener(new View.OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     final Dialog dialog = new Dialog(parentContext);
                     Log.i("CategoryExpenseListAdapter", dialog.toString());
                     dialog.setContentView(R.layout.dialog_edit_expense);
                     dialog.setTitle("Editing Expense");
                     dialog.setCancelable(false);
 
                     final EditText newAmount = (EditText) dialog
                             .findViewById(R.id.new_amount);
                     newAmount.setInputType(InputType.TYPE_CLASS_NUMBER
                             | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                     newAmount.setFocusable(true);
                    newAmount.setText(String.format("%.02f", expense.getAmount()));
 
                     final Spinner spinner = (Spinner) dialog
                             .findViewById(R.id.category_spinner);
                     ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                             context, android.R.layout.simple_spinner_item,
                             categories);
                     spinner.setAdapter(adapter);
                     spinner.setSelection(mCategoryMap.get(expense.getCategory()));
                     final Button editDateBtn = (Button) dialog
                             .findViewById(R.id.edit_date_btn);
                     editDateBtn.setText(expense.getDate());
                     editDateBtn.setOnClickListener(new View.OnClickListener() {
 
                         @Override
                         public void onClick(View v) {
                             Calendar c = Calendar.getInstance();
                             DatePickerDialog dialog = new DatePickerDialog(
                                     context,
                                     new DatePickerDialog.OnDateSetListener() {
 
                                         @Override
                                         public void onDateSet(DatePicker view,
                                                 int year, int monthOfYear,
                                                 int dayOfMonth) {
 
                                             expense.setDate((new StringBuilder()
                                                     .append(monthOfYear + 1)
                                                     .append("/")
                                                     .append(dayOfMonth)
                                                     .append("/").append(year)
                                                     .append(" ").toString()));
                                             editDateBtn.setText(expense
                                                     .getDate());
                                         }
                                     }, c.get(Calendar.YEAR), c
                                             .get(Calendar.MONTH), c
                                             .get(Calendar.DAY_OF_MONTH));
                             dialog.show();
                         }
                     });
 
                     Button deleteBtn = (Button) dialog
                             .findViewById(R.id.delete_expense_btn);
                     Button saveBtn = (Button) dialog
                             .findViewById(R.id.save_expense_btn);
                     Button cancelBtn = (Button) dialog
                             .findViewById(R.id.cancel_btn);
 
                     saveBtn.setOnClickListener(new View.OnClickListener() {
 
                         @Override
                         public void onClick(View v) {
                             expense.setCategory((String) spinner
                                     .getSelectedItem());
                             expense.setAmount(Double.parseDouble(newAmount
                                     .getText().toString()));
                             expense.setDate(editDateBtn.getText().toString());
                             mDBAdapter.open();
                             mDBAdapter.updateExpense(expense);
 
                             // remove item from list view if category changed
                             // bad hack gah
                             if (!expense.getCategory().equals(categoryName)) {
                                 expenses.remove(position);
                             }
                             // update remaining view
                            remainingView.setText(String.format("%.02f", mDBAdapter
                                    .getCategory(categoryName).getRemaining()));
                             mDBAdapter.close();
 
                             Log.i(TAG, "notifyDataSetChanged()");
 
                             notifyDataSetChanged();
                             Toast.makeText(context, "changes saved",
                                     Toast.LENGTH_SHORT).show();
                             dialog.dismiss();
 
                         }
                     });
                     cancelBtn.setOnClickListener(new View.OnClickListener() {
 
                         @Override
                         public void onClick(View v) {
                             dialog.dismiss();
                         }
                     });
 
                     deleteBtn.setOnClickListener(new View.OnClickListener() {
 
                         @Override
                         public void onClick(View v) {
                             mDBAdapter.open();
                             if (mDBAdapter.removeExpense(expense)) {
                                remainingView.setText(String.format("%.02f", mDBAdapter
                                         .getCategory(categoryName)
                                        .getRemaining()));
                             } else {
                                 Toast.makeText(context,
                                         "Error Deleting Expense",
                                         Toast.LENGTH_SHORT).show();
                             }
 
                             mDBAdapter.close();
                             expenses.remove(position);
                             notifyDataSetChanged();
                             dialog.dismiss();
                         }
                     });
 
                     dialog.show();
                 }
             });
 
         }
         // }
         return view;
     }
 
     // for persisting spinner selection
     private Map<String, Integer> getCategoryMap(List<String> categories) {
         Map<String, Integer> map = new HashMap<String, Integer>();
         for (int i = 0; i < categories.size(); i++) {
             map.put(categories.get(i), i);
         }
         return map;
     }
 }
