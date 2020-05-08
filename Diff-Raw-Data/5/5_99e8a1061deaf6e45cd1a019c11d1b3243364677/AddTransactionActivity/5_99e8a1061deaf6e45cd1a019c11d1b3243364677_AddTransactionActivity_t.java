 package no.kantega.android.afp;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import no.kantega.android.afp.controllers.Transactions;
 import no.kantega.android.afp.models.Transaction;
 import no.kantega.android.afp.models.TransactionTag;
 import no.kantega.android.afp.utils.FmtUtil;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * This activity handles adding of a new transaction
  */
 public class AddTransactionActivity extends Activity {
 
     private static final String DATE_FORMAT = "yyyy-MM-dd";
     private static final int DATE_DIALOG_ID = 0;
     private Transactions db;
     private Button pickDate;
     private int pickYear;
     private int pickMonth;
     private int pickDay;
     private TransactionTag selectedTag;
     private TransactionTag untagged;
     private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                               int dayOfMonth) {
             pickYear = year;
            pickMonth = monthOfYear;
             pickDay = dayOfMonth;
             updateDisplay();
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.addtransaction);
         this.db = new Transactions(getApplicationContext());
         this.untagged = new TransactionTag(getResources().getString(R.string.not_tagged));
         findViewById(R.id.button_add_transaction).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 boolean newTransactionOk = true;
                 Transaction t = new Transaction();
                 TransactionTag tag = null;
                 if (!selectedTag.equals(untagged)) {
                     tag = selectedTag;
                 }
                 Date d = FmtUtil.stringToDate(DATE_FORMAT,
                         String.format("%s-%s-%s", pickYear, pickMonth, pickDay));
                 EditText etamount = (EditText) findViewById(R.id.edittext_amount);
                 EditText ettext = (EditText) findViewById(R.id.edittext_text);
                 if (FmtUtil.isNumber(etamount.getText().toString())) {
                     t.setAmount(Double.parseDouble(etamount.getText().toString()));
                 } else {
                     Toast.makeText(getApplicationContext(), R.string.invalid_amount, Toast.LENGTH_LONG).show();
                     newTransactionOk = false;
                 }
                 if (newTransactionOk) {
                     t.setText(ettext.getText().toString());
                     t.setTag(tag);
                     t.setDate(d);
                     t.setTimestamp(new Date().getTime());
                     t.setInternal(true);
                     t.setDirty(true);
                     db.add(t);
                     Toast.makeText(getApplicationContext(), R.string.transaction_added, Toast.LENGTH_LONG).show();
                     finish();
                 }
             }
         });
         pickDate = (Button) findViewById(R.id.pickDate);
         pickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
         pickYear = Calendar.getInstance().get(Calendar.YEAR);
         pickMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // Month starts at 0
         pickDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
         updateDisplay();
         Spinner spinner = (Spinner) findViewById(R.id.spinner_category);
         ArrayAdapter<TransactionTag> adapter = new ArrayAdapter<TransactionTag>(this,
                 android.R.layout.simple_spinner_item, db.getTags());
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         adapter.add(untagged);
         spinner.setAdapter(adapter);
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 selectedTag = (TransactionTag) adapterView.getItemAtPosition(i);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
             }
         });
         spinner.setSelection(adapter.getPosition(untagged));
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID:
                return new DatePickerDialog(this, dateSetListener, pickYear, pickMonth - 1,
                         pickDay);
         }
         return null;
     }
 
     /**
      * Update picked date
      */
     private void updateDisplay() {
         pickDate.setText(FmtUtil.dateToString(DATE_FORMAT, FmtUtil.stringToDate(DATE_FORMAT, String.format("%s-%s-%s",
                 pickYear, pickMonth, pickDay))));
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.close();
     }
 }
