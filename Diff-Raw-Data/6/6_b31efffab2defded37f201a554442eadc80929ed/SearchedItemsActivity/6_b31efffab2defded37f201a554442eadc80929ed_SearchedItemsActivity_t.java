 package cz.cvut.localtrade;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import cz.cvut.localtrade.dao.ItemsDAO;
 import cz.cvut.localtrade.helper.Filter;
 import cz.cvut.localtrade.helper.MapUtils;
 import cz.cvut.localtrade.model.Item;
 
 public class SearchedItemsActivity extends FragmentActivity {
 
 	ListView listView;
 
 	private ItemsDAO itemDao;
 
 	private List<Item> items;
 
 	private ArrayList<Integer> filteredIds;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.searched_items_activity_layout);
 		super.onCreate(savedInstanceState);
 
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setTitle(getString(R.string.found_items));
 
 		// itemDao = new ItemsDAO();
 		// itemDao.open();
 		// items = itemDao.findAll(this, query)();
 		items = (ArrayList<Item>) getIntent().getExtras().getSerializable(
 				"items");
 		listView = (ListView) findViewById(R.id.searched_items_listview);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Intent intent = new Intent(SearchedItemsActivity.this,
 						SearchedItemDetailActivity.class);
 				int itemId = filteredIds.get(position);
 				Bundle bundle = new Bundle();
 				bundle.putInt("itemId", itemId);
 				bundle.putSerializable("items", (ArrayList<Item>) items);
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		fillListView();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent intent = new Intent(this, ShowMapActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void fillListView() {
 		List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
 		filteredIds = new ArrayList<Integer>();
 
 		double minItemPrice = Double.MAX_VALUE;
 		double maxItemPrice = Double.MIN_VALUE;
 		double minDistance = Double.MAX_VALUE;
 		double maxDistance = Double.MIN_VALUE;
 
 		for (Item item : items) {
 			if (item.getPrice() < minItemPrice) {
 				minItemPrice = item.getPrice();
 			}
 			if (item.getPrice() > maxItemPrice) {
 				maxItemPrice = item.getPrice();
 			}
 			float distance = MapUtils.distanceBetween(MapUtils.actualLocation,
 					item.getLocation());
 			if (distance > maxDistance) {
 				maxDistance = distance;
 			}
 			if (distance < minDistance) {
 				minDistance = distance;
 			}
 
			if (Filter.currentFilter != null
					&& Filter.currentFilter.filter(item)) {
 				continue;
 			}
 
 			HashMap<String, String> hm = new HashMap<String, String>();
 			hm.put("tit", item.getTitle());
			hm.put("sta", item.getState().toString());
 			hm.put("dis", String.format("%.2f", distance) + " "
 					+ getString(R.string.distance_unit));
 			hm.put("pri", getString(R.string.price) + ": " + item.getPrice()
 					+ " " + getString(R.string.currency));
 			hm.put("image", Integer.toString(R.drawable.no_image));
 			aList.add(hm);
 			filteredIds.add(item.getId());
 		}
 
 		if (Filter.currentFilter == null) {
 			Filter.currentFilter = new Filter();
 			Filter.currentFilter.priceLowBound = minItemPrice;
 			Filter.currentFilter.priceHighBound = maxItemPrice;
 			Filter.currentFilter.distanceHighBound = maxDistance;
 			Filter.currentFilter.distanceLowBound = minDistance;
 		}
 
 		String[] from = { "tit", "sta", "dis", "pri", "image" };
 		int[] to = { R.id.item_title, R.id.item_state, R.id.item_distance,
 				R.id.item_price, R.id.item_image };
 		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList,
 				R.layout.searched_items_list_item_layout, from, to);
 		listView.setAdapter(adapter);
 	}
 
 	public void showPopupFilter(MenuItem item) {
 		FilterDialogFragment f = new FilterDialogFragment();
 		f.setFilter(Filter.currentFilter);
 		f.show(getFragmentManager(), "FilterDialogFragment");
 	}
 
 	public void onFilterOk(Filter filter) {
 		Filter.currentFilter = filter;
 		fillListView();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.searched_items_activity_menu, menu);
 		return true;
 	}
 
 }
