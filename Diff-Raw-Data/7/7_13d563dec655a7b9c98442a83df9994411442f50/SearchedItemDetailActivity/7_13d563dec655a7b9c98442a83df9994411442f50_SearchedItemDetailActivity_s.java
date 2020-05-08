 package cz.cvut.localtrade;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import cz.cvut.localtrade.dao.ItemsDAO;
 import cz.cvut.localtrade.dao.ItemsDAO.FindResponse;
 import cz.cvut.localtrade.helper.MapUtils;
 import cz.cvut.localtrade.model.Item;
 
 public class SearchedItemDetailActivity extends BaseActivity implements FindResponse {
 
 	private Item item;
 
 	private ItemsDAO itemDao;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		itemDao = new ItemsDAO();
 		itemDao.open();
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle(getString(R.string.found_items_detail));
 
 		int itemId = getIntent().getExtras().getInt("itemId");
 
 		itemDao.find(this, itemId);
 		showLoadingDialog();
 	}
 
 	@Override
 	protected void onResume() {
 		itemDao.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		itemDao.close();
 		super.onPause();
 	}
 
 	public void fillContent() {
 		TextView title = (TextView) findViewById(R.id.item_title);
 		TextView price = (TextView) findViewById(R.id.item_price);
 		TextView state = (TextView) findViewById(R.id.item_state);
 		TextView distance = (TextView) findViewById(R.id.item_distance);
 		TextView description = (TextView) findViewById(R.id.item_description);
 
 		title.setText(item.getTitle());
 		price.setText(item.getPrice() + " " + getString(R.string.currency));
 		state.setText(item.getState().toString());
 		distance.setText(String.format(
 				"%.2f",
 				MapUtils.distanceBetween(MapUtils.getUserGeoPoint(),
 						item.getLocation()))
 				+ " " + getString(R.string.distance_unit));
 		description.setText(item.getDescription());
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.searched_item_detail_activity_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, SearchedItemsActivity.class);
 			intent.putExtra("items", this.getIntent().getSerializableExtra("items"));
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void showOnMap(MenuItem menuItem) {
 		Intent intent = new Intent(SearchedItemDetailActivity.this,
 				ShowItemOnMapActivity.class);
 		intent.putExtra("itemId", item.getId());
 		intent.putExtra("items", this.getIntent().getSerializableExtra("items"));
 		startActivity(intent);
 	}
 
 	public void sendMessage(MenuItem item) {
 
 	}
 
 	@Override
 	public void onFound(Item item) {
 		hideLoadingDialog();
 		setContentView(R.layout.searched_item_detail_activity_layout);
 		this.item = item;
 		fillContent();
 	}
 
 	@Override
 	public void onFindFail() {
 		showToast(getString(R.string.connection_error));
 	}
 
 }
