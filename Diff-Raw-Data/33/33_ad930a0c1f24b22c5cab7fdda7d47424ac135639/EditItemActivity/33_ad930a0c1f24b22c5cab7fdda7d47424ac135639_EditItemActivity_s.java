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
 import cz.cvut.localtrade.dao.ItemsDAO.EditResponse;
 import cz.cvut.localtrade.helper.LoginUtil;
 import cz.cvut.localtrade.model.Item;
 import cz.cvut.localtrade.model.Item.State;
 
 public class EditItemActivity extends BaseActivity implements
 		OnItemSelectedListener, EditResponse {
 
 	Item item;
 	ItemsDAO itemDao;
 
 	EditText titleText;
 	EditText descriptionText;
 	EditText priceText;
 	Spinner spinner;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.add_new_item_activity_layout);
 		super.onCreate(savedInstanceState);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle(getString(R.string.edit_item));
 
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
 
 		itemDao = new ItemsDAO();
 		item = (Item) getIntent().getExtras().get("item");
 		fillContent();
 	}
 
 	public void fillContent() {
 		titleText.setText(item.getTitle());
 		descriptionText.setText(item.getDescription());
 		priceText.setText(item.getPrice() + "");
 		spinner.setSelection(item.getState().ordinal());
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
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.edit_item_activity_menu, menu);
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
 		// TODO Auto-generated method stub
 
 	}
 
 	public void editItem(MenuItem menu) {
 		try {
 			item.setTitle(titleText.getText().toString());
 			item.setDescription(descriptionText.getText().toString());
 			item.setPrice(Double.parseDouble(priceText.getText().toString()));
 		} catch (Exception e) {
 			showToast("Enter price of the item");
 			return;
 		}
 
 		showLoadingDialog();
 		itemDao.editItem(this, item, LoginUtil.getUserId(this));
 	}
 
 	public void cancelEdit(MenuItem item) {
		Intent intent = new Intent(this, MyItemsActivity.class);
		startActivity(intent);
 	}
 
 	@Override
 	public void onItemEdit(Item item) {
 		hideLoadingDialog();
 		Intent intent = new Intent(this, MyItemDetailActivity.class);
 		Bundle bundle = new Bundle();
 		bundle.putSerializable("item", item);
 		intent.putExtras(bundle);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onItemEditFail() {
 		showToast(getString(R.string.connection_error));
 		hideLoadingDialog();
 	}
 }
