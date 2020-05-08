 package com.brunocascio.expensis;
 
 import java.util.Calendar;
 import java.util.List;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.DialogInterface;
import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class ListExpensisActivity extends Activity implements OnItemSelectedListener, OnClickListener, OnDateSetListener{
 	private ExpensiDataSource datasource;
 	private ListView listView;
 	private List<Expensi> L;
 	private EditText edittext;
 	private Spinner spinnerMonths;
 	private ArrayAdapter<CharSequence> adapterMonths;
 	private ArrayAdapter<Expensi> adapter;
 	private AlertDialog dialog;
 	private int positionDelete;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list_expensis);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// Database pipe
 		datasource = new ExpensiDataSource(this);
 	    datasource.openReadable();
 	    
 	    L = datasource.getAllExpensis(); 
 	    
 	    edittext = (EditText) findViewById(R.id.fechaOrder);
 	    edittext.setOnClickListener(this);
 	    
 	    // ======================= Dialogs ===============================
 		    
 	    // ======================= FIn Dialogs ===========================
 	    	
 	    
 	    // =============== Lista de gastos ==============================
 	    listView = (ListView) findViewById(R.id.listExp);
 	    
 	    adapter = new ArrayAdapter<Expensi>(
 	    		this
 	    		,android.R.layout.simple_list_item_activated_1
 	    		, L //Pasamos todos los gastos
 	    );
 	    listView.setAdapter(adapter);
 	    listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 	        public boolean onItemLongClick(AdapterView<?> parent, View arg1, int pos, long id) {
 	        	positionDelete = pos;
 	        	// 1. Instantiate an AlertDialog.Builder with its constructor
 			    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		
 			    // 2. Chain together various setter methods to set the dialog characteristics
 			    builder.setMessage(R.string.dialog_message)
 			           .setTitle(R.string.dialog_title);
 			    
 			    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int id) {
 	                	Expensi item = adapter.getItem(positionDelete);
 	    	        	datasource.deleteExpensi(item.getId());
 	    	        	adapter.remove(item);
 	    	        	adapter.notifyDataSetChanged();
 	    	        	Toast.makeText(getApplicationContext(), "Eliminado "+item.getDescription(), Toast.LENGTH_SHORT).show();
 	    	        	
 	                }
 	            });
 			    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 			                public void onClick(DialogInterface dialog, int id) {
 			                    // User cancelled the dialog
 			                }
 			     });
 		
 			    // 3. Get the AlertDialog from create()
 			    dialog = builder.create();
 			    
 	        	dialog.show();
 	        	
 	            return true;
 	        }
         });
 	    
 	    // ===================== MESES ===============================
 	    spinnerMonths = (Spinner) findViewById(R.id.spinner_month);
 	    adapterMonths = ArrayAdapter.createFromResource(
 	            this, R.array.months_array, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinnerMonths.setAdapter(adapterMonths);
 	    int mesActual = Calendar.getInstance().get(Calendar.MONTH);
 	    spinnerMonths.setSelection(mesActual);
 	    spinnerMonths.setOnItemSelectedListener(this);
         
 	}
 	
 	private Activity getActivity(){
 		return this;
 	}
 
 	@Override
     protected void onResume() {
       datasource.openReadable();
       super.onResume();
     }
 
     @Override
     protected void onPause() {
       datasource.close();
       super.onPause();
     }
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.list_expensis, menu);
 		return true;
	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onDateSet(DatePicker view, int year, int month, int day) {
 		String fecha = day+"-"+(month+1)+"-"+year;
 		this.edittext.setText(fecha);
 		// Get results of Database for this Date
 		List<Expensi> L = datasource.getExpensisFromDate(edittext.getText().toString());
 		
 		ArrayAdapter<Expensi> adapter = new ArrayAdapter<Expensi>(
 	    		this
 	    		,android.R.layout.simple_list_item_activated_1
 	    		, L
 	    );
 		listView.setAdapter(adapter);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.fechaOrder){
 			DatePickerDialog dialog = new DatePickerDialog(
 					  this
 					, this
 					, Calendar.getInstance().get(Calendar.YEAR)
 					, Calendar.getInstance().get(Calendar.MONTH)
 					, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
 		       dialog.show();
 		}	
 	}
 	
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         // An item was selected. You can retrieve the selected item using
         // parent.getItemAtPosition(pos)
 		//System.out.println("Seleccionado -> "+pos+" con id: "+id);
 		//System.out.println("Seleccionado -> "+pos+" con id: "+id);
 		edittext.setText("");
 		List<Expensi> L = datasource.getExpensisFromMonth(pos+1);
 		adapter.clear();
 		adapter.addAll(L);
 		adapter.notifyDataSetChanged();
 	    listView.setAdapter(adapter);
 		
     }
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {};
 
 }
