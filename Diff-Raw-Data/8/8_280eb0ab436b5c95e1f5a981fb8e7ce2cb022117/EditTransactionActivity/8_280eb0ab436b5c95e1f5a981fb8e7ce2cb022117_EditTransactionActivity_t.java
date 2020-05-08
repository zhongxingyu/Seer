 package no.kantega.android.afp;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.*;
 import no.kantega.android.afp.controllers.Transactions;
 import no.kantega.android.afp.models.Transaction;
 import no.kantega.android.afp.models.TransactionTag;
 import no.kantega.android.afp.utils.FmtUtil;
 import no.kantega.android.afp.utils.GsonUtil;
 import no.kantega.android.afp.utils.HttpUtil;
 import no.kantega.android.afp.utils.Prefs;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This activity handles editign of an existing transaction
  */
 public class EditTransactionActivity extends Activity {
 
     private static final String DATE_FORMAT = "yyyy-MM-dd";
     private static final int DATE_DIALOG_ID = 0;
     private static final int PROGRESS_DIALOG_ID = 1;
     private static final int ALERT_DIALOG_ID = 2;
     private Transactions db;
     private ArrayAdapter<TransactionTag> adapter;
     private Transaction t;
     private TransactionTag selectedTag;
     private int pickYear;
     private int pickMonth;
     private int pickDay;
     private EditText text;
     private Button date;
     private EditText amount;
     private Spinner category;
     private TextView suggestedTag;
     private String suggestUrl;
     private TransactionTag untagged;
     private ProgressDialog progressDialog;
     private List<Transaction> matchingTransactions;
     private List<Transaction> similarTransactions;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edittransaction);
         this.t = (Transaction) getIntent().getExtras().getSerializable("transaction");
         this.db = new Transactions(getApplicationContext());
         this.suggestUrl = Prefs.getProperties(getApplication()).get("suggestTag").toString();
         this.untagged = new TransactionTag(getResources().getString(R.string.not_tagged));
         this.selectedTag = t.getTag();
         findViewById(R.id.edittransaction_button_edittransaction).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         matchingTransactions = db.getByText(t.getText(), t.get_id());
                         similarTransactions = db.getSimilarByText(String.format("%s %%",
                                FmtUtil.firstWord(t.getText())), t.getText(), t.getId());
                         if (!selectedTag.equals(untagged) && !matchingTransactions.isEmpty()) {
                             showDialog(ALERT_DIALOG_ID);
                         } else {
                             saveTransaction(false);
                         }
                     }
                 });
         setupViews();
         updateSpinnerPosition(selectedTag);
         updateDisplay();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         new SuggestionsTask().execute(suggestUrl,
                 FmtUtil.trimTransactionText(t.getText()));
     }
 
     /**
      * Update spinner position according to the selected tag
      *
      * @param tag The selected tag
      */
     private void updateSpinnerPosition(TransactionTag tag) {
         if (tag == null) {
             category.setSelection(adapter.getPosition(untagged));
         } else {
             category.setSelection(adapter.getPosition(tag));
         }
     }
 
     /**
      * Setup views
      */
     private void setupViews() {
         text = (EditText) findViewById(R.id.edittransaction_edittext_text);
         date = (Button) findViewById(R.id.edittransaction_button_pickDate);
         amount = (EditText) findViewById(R.id.edittransaction_edittext_amount);
         if (!t.isInternal()) {
             text.setEnabled(false);
             date.setEnabled(false);
             amount.setEnabled(false);
         }
         category = (Spinner) findViewById(R.id.edittransaction_spinner_category);
         suggestedTag = (TextView) findViewById(R.id.suggested_tag);
         text.setText(FmtUtil.trimTransactionText(t.getText()));
         date.setText(FmtUtil.dateToString(DATE_FORMAT, t.getDate()));
         amount.setText(String.valueOf(t.getAmount()));
         date.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
         final Calendar calendar = Calendar.getInstance();
         calendar.setTime(t.getDate());
         pickYear = calendar.get(Calendar.YEAR);
         pickMonth = calendar.get(Calendar.MONTH) + 1; // Starts at 0, wtf!
         pickDay = calendar.get(Calendar.DAY_OF_MONTH);
         adapter = new ArrayAdapter<TransactionTag>(this, android.R.layout.simple_spinner_item, db.getTags());
         adapter.add(untagged);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         category.setAdapter(adapter);
         category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 selectedTag = (TransactionTag) adapterView.getItemAtPosition(i);
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
             }
         });
     }
 
     /**
      * This task handles batch updates of tags
      */
     private class UpdateTask extends AsyncTask<TransactionTag, Integer, Boolean> {
 
         @Override
         protected void onPreExecute() {
             showDialog(PROGRESS_DIALOG_ID);
         }
 
         @Override
         protected Boolean doInBackground(TransactionTag... tags) {
             int i = 0;
             progressDialog.setMax(matchingTransactions.size());
             for (Transaction matching : matchingTransactions) {
                 matching.setTag(tags[0]);
                 matching.setDirty(true);
                 db.update(matching);
                 publishProgress(++i);
             }
             return false;
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             final Message msg = progressHandler.obtainMessage();
             msg.arg1 = values[0];
             progressHandler.sendMessage(msg);
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             dismissDialog(PROGRESS_DIALOG_ID);
             finish();
         }
     }
 
     /**
      * Save transaction
      *
      * @param autoTag Set to true if other transactions with equal text should be tagged
      */
     private void saveTransaction(final boolean autoTag) {
         TransactionTag tag = null;
         if (!selectedTag.equals(untagged)) {
             tag = selectedTag;
             if (autoTag) {
                 new UpdateTask().execute(tag);
             }
         }
         if (t.isInternal()) {
             boolean editTransactionOk = true;
             Date d = FmtUtil.stringToDate(DATE_FORMAT, String.format("%s-%s-%s",
                     pickYear, pickMonth, pickDay));
             if (FmtUtil.isNumber(amount.getText().toString())) {
                 t.setAmount(Double.parseDouble(amount.getText().toString()));
             } else {
                 Toast.makeText(getApplicationContext(), R.string.invalid_amount,
                         Toast.LENGTH_LONG).show();
                 editTransactionOk = false;
             }
             if (editTransactionOk) {
                 t.setText(text.getText().toString());
                 t.setTag(tag);
                 t.setDate(d);
                 t.setDirty(true);
                 db.update(t);
             }
         } else {
             t.setTag(tag);
             t.setDirty(true);
             db.update(t);
         }
         if (!autoTag) {
             finish();
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID: {
                 return new DatePickerDialog(this, dateSetListener, pickYear, pickMonth,
                         pickDay);
             }
             case ALERT_DIALOG_ID: {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage("")
                         .setCancelable(false)
                         .setPositiveButton(R.string.tag_exact, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog,
                                                 int id) {
                                 saveTransaction(true);
                                 dialog.dismiss();
                             }
                         })
                         .setNegativeButton(R.string.tag_single, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog,
                                                 int id) {
                                 saveTransaction(false);
                                 dialog.dismiss();
                             }
                         })
                         .setNeutralButton(R.string.tag_all, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int i) {
                                 // XXX: Find similar transactions and start activity
                                saveTransaction(true);
                                 Intent intent = new Intent(getApplicationContext(), SimilarTransactionsActivity.class);
                                 intent.putExtra("text", t.getText());
                                 intent.putExtra("excludeId", t.get_id());
                                 startActivity(intent);
                                 dialog.dismiss();
                             }
                         });
                 return builder.create();
             }
             case PROGRESS_DIALOG_ID: {
                 progressDialog = new ProgressDialog(this);
                 progressDialog.setMessage(getResources().getString(
                         R.string.please_wait));
                 progressDialog.setCancelable(false);
                 progressDialog.setProgressStyle(ProgressDialog.
                         STYLE_HORIZONTAL);
                 return progressDialog;
             }
             default: {
                 return null;
             }
         }
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch (id) {
             case PROGRESS_DIALOG_ID: {
                 progressDialog.setProgress(0);
                 break;
             }
             case ALERT_DIALOG_ID: {
                 ((AlertDialog) dialog).setMessage(String.format(getResources().getString(R.string.auto_tag),
                        matchingTransactions.size(), similarTransactions.size()));
                 break;
             }
         }
     }
 
     private final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                               int dayOfMonth) {
             pickYear = year;
             pickMonth = monthOfYear + 1;
             pickDay = dayOfMonth;
             updateDisplay();
         }
     };
 
     /**
      * Update selected date
      */
     private void updateDisplay() {
         date.setText(FmtUtil.dateToString(DATE_FORMAT, FmtUtil.stringToDate(DATE_FORMAT, String.format("%s-%s-%s",
                 pickYear, pickMonth, pickDay))));
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.close();
     }
 
     private final Handler progressHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             progressDialog.setProgress(msg.arg1);
         }
     };
 
     /**
      * This task handles retrieval of tag suggestions
      */
     private class SuggestionsTask extends AsyncTask<String, Integer, String> {
 
         @Override
         protected String doInBackground(String... params) {
             return HttpUtil.post(params[0], params[1], "text/plain");
         }
 
         @Override
         protected void onPostExecute(String s) {
             if (s != null) {
                 final List<Map<String, String>> result = GsonUtil.parseMap(s);
                 if (result != null && !result.isEmpty()) {
                     final String tag = result.get(0).get("tag");
                     suggestedTag.setText(tag);
                     updateSpinnerPosition(new TransactionTag(tag));
                 }
             }
         }
     }
 }
