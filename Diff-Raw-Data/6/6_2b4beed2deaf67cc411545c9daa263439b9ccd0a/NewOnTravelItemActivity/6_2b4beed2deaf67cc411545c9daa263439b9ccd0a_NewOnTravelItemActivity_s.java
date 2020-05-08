 package ch.hearc.devmobile.travelnotebook;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.larswerkman.holocolorpicker.ColorPicker;
 
 import ch.hearc.devmobile.travelnotebook.database.DatabaseHelper;
 import ch.hearc.devmobile.travelnotebook.database.TravelItem;
 import ch.hearc.devmobile.travelnotebook.database.Voyage;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.Editable;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class NewOnTravelItemActivity extends Activity {
 
 	/********************
 	 * Private members
 	 ********************/
 	private DatabaseHelper databaseHelper = null;
 	public static final int RESULT_FAIL = 500;
 	public static final int RESULT_SQL_FAIL = 501;
 	public static final String ITEM_ID_KEY = "itemId";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Hide application title
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// Hide status bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setContentView(R.layout.activity_new_on_travel_item);
 
 		databaseHelper = OpenHelperManager
 				.getHelper(this, DatabaseHelper.class);
 
 		// Cancel button
 		Button btnCancel = (Button) findViewById(R.id.btn_cancel);
 		btnCancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				NewOnTravelItemActivity.this.setResult(RESULT_CANCELED);
 				NewOnTravelItemActivity.this.finish();
 			}
 		});
 
 		// Save button
 		Button btnSave = (Button) findViewById(R.id.btn_save);
 		btnSave.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				try {
 					int id = NewOnTravelItemActivity.this.createItem();
 
 					Intent intent = new Intent();
 					intent.putExtra(ITEM_ID_KEY, id);
 
 					NewOnTravelItemActivity.this.setResult(RESULT_OK, intent);
 					NewOnTravelItemActivity.this.finish();
 
 				} catch (SQLException e) {
 					NewOnTravelItemActivity.this.setResult(RESULT_SQL_FAIL);
 					e.printStackTrace();
 					NewOnTravelItemActivity.this.finish();
 
 				} catch (Exception e) {
 					Toast.makeText(getApplicationContext(), e.getMessage(),
 							Toast.LENGTH_SHORT).show();
 				}
 
 			}
 		});
 	}
 
 	protected int createItem() throws Exception {
 		EditText etTitle = (EditText) findViewById(R.id.item_title);
 		String title = etTitle.getText().toString();		
 		if (title.length() == 0)
 			throw new Exception("Invalide name");
 		
 		EditText etDescription = (EditText) findViewById(R.id.item_description);
 		String description = etDescription.getText().toString();
 		
 		EditText etStartDate = (EditText) findViewById(R.id.item_start_date);
 		String strStartDate = etStartDate.getText().toString();
 				
 		
 	    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 		Date startDate = formatter.parse(strStartDate);
 		
 		Date endDate = null;
 		String startLocation = null;
 		String endLocation = null;
 		Voyage voyage = null;
 
 		Dao<TravelItem, Integer> itemDao = databaseHelper.getTravelItemDao();
		TravelItem item = new TravelItem(title, description, startDate, endDate, startLocation, endLocation, voyage);
 		itemDao.create(item);
 		
 		return item.getId();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.new_on_travel_item, menu);
 		return true;
 	}
 
 }
