 package cz.cvut.localtrade;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import cz.cvut.localtrade.dao.ItemsDAO;
 import cz.cvut.localtrade.dao.ItemsDAO.CreateResponse;
 import cz.cvut.localtrade.helper.LoginUtil;
 import cz.cvut.localtrade.helper.MapUtils;
 import cz.cvut.localtrade.model.Item;
 import cz.cvut.localtrade.model.Item.State;
 
 public class AddNewItemActivity extends BaseActivity implements
 		OnItemSelectedListener, CreateResponse {
 
 	Item item;
 
 	EditText titleText;
 	EditText descriptionText;
 	EditText priceText;
 	Spinner spinner;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.add_new_item_activity_layout);
 		super.onCreate(savedInstanceState);
 
 		item = new Item();
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle(getString(R.string.add_new_item));
 
 		titleText = (EditText) findViewById(R.id.item_title);
 		descriptionText = (EditText) findViewById(R.id.item_description);
 		priceText = (EditText) findViewById(R.id.price_of_item);
 
 		spinner = (Spinner) findViewById(R.id.state_spinner);
 		// Create an ArrayAdapter using the string array and a default spinner
 		// layout
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.states_array,
 				android.R.layout.simple_spinner_item);
 		// Specify the layout to use when the list of choices appears
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		// Apply the adapter to the spinner
 		spinner.setAdapter(adapter);
 		spinner.setOnItemSelectedListener(this);
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, MyItemsActivity.class);
 			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.add_new_item_activity_menu, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		switch (pos) {
 		case 0:
 			item.setState(State.NEW);
 			break;
 		case 1:
 			item.setState(State.USED);
 			break;
 		case 2:
 			item.setState(State.DYSFUNCTIONAL);
 			break;
 		case 3:
 			item.setState(State.BROKEN);
 			break;
 		default:
 			item.setState(State.NEW);
 			break;
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 
 	}
 
 	public void addItem(MenuItem menu) {
 
 		ItemsDAO dao = new ItemsDAO();
 
 		try {
 			String title = titleText.getText().toString();
 			String description = descriptionText.getText().toString();
 			double price = Double.parseDouble(priceText.getText().toString());
 
 			int lat = MapUtils.getUserGeoPoint().getLatitudeE6();
 			int lon = MapUtils.getUserGeoPoint().getLongitudeE6();
 
 			showLoadingDialog();
 			dao.createItem(this, title, this.item.getState(), description,
 					price, lat, lon, LoginUtil.getUserId(this));
 		} catch (NumberFormatException e) {
 			showToast("Enter price of the item");
 		}
 	}
 
 	public void cancelAdd(MenuItem item) {
 		Intent intent = new Intent(this, MyItemsActivity.class);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onItemCreate(Item item) {
 		hideLoadingDialog();
 		Intent intent = new Intent(this, MyItemDetailActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("item", item);
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onItemCreateFail() {
 		showToast(getString(R.string.connection_error));
 	}
 
 }
