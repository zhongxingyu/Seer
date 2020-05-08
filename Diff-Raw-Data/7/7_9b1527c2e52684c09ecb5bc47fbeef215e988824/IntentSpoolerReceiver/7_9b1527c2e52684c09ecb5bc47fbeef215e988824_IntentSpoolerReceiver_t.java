 package jp.peo3.android.intentspooler;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import jp.peo3.android.intentspooler.database.IntentDatabaseAdapter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class IntentSpoolerReceiver extends Activity {
 	private LayoutInflater inflater;
 	private TableLayout tableLayout;
 	private Intent intent;
 	private IntentDatabaseAdapter dbHelper;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.receiver);
         
         inflater = getLayoutInflater();
         tableLayout = (TableLayout) findViewById(R.id.intentTable);
         intent = getIntent();
         dbHelper = new IntentDatabaseAdapter(this);
 
         fillIntentTable(tableLayout, intent);    	
     }
 	
 	private void addRow(String key, String value) {
 		if (value == null)
 			return;
 		
 		// Must create a rowView for each row
 		View rowView = inflater.inflate(R.layout.receiver_table_row, tableLayout, false);
 		
 		TextView textKey = (TextView) rowView.findViewById(R.id.textViewKey);
 		textKey.setText(key);
 		TextView textValue = (TextView) rowView.findViewById(R.id.textViewValue);
 		textValue.setText(value);
 		
 		tableLayout.addView(rowView);
 	}
 
 	private void fillIntentTable(TableLayout tableLayout, Intent intent) {
     	addRow("action", intent.getAction());    	
     	addRow("pkg", intent.getPackage());
     	
     	Set<String> categories = intent.getCategories();
     	if (categories != null) {
     		Iterator<String> it = categories.iterator();
     		while (it.hasNext()) {
     			addRow("category", it.next());
     		}
     	}
     	
     	Bundle extras = intent.getExtras();
     	if (extras != null) {
     		Iterator<String> it = extras.keySet().iterator();
     		int i = 1;
     		while (it.hasNext()) {
     			String key = it.next();
     			addRow("extraKey" + Integer.toString(i), key);
    			Object val = extras.get(key);
    			if (val == null) {
    				addRow("extraValue" + Integer.toString(i), "(null)");
    			} else {
    				addRow("extraValue" + Integer.toString(i), val.toString());
    			}
     			i++;
     		}
     	}
     	
     	addRow("data", intent.getDataString());
     	addRow("flags", String.valueOf(intent.getFlags()));
    		addRow("scheme", intent.getScheme());
    		addRow("type", intent.getType());
 	}
 	
 	public boolean onClickOk(View view) {
 		dbHelper.open();
 		dbHelper.createIntentEntry(intent);
 		dbHelper.close();
 
 		finish();
 		return true;
 	}
 
 	public boolean onClickCancel(View view) {
 		finish();
 		return true;
 	}
 
     public boolean onClickItem(View view) {
     	TextView textview = (TextView) view;
     	String copy = textview.getText().toString();
 
     	ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 
     	clipboard.setText(copy);
     	Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
     	return true;
    }
 }
