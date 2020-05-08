 package jp.knct.di.c6t.ui.route;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.BasicClient;
 import jp.knct.di.c6t.communication.Client;
 import jp.knct.di.c6t.communication.DebugSharedPreferencesClient;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.util.ActivityUtil;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 public class SearchRouteActivity extends ListActivity implements OnClickListener {
 	private static final String[] OPTION_SCOPE_LABELS = new String[] {
 			"[g",
 			"쐬[U",
 			"",
 	};
 	private static final String[] OPTION_SCOPE_VALUES = new String[] {
 			BasicClient.SearchRouteParams.SCOPE_TITLE,
 			BasicClient.SearchRouteParams.SCOPE_USER_NAME,
 			BasicClient.SearchRouteParams.SCOPE_DESCRIPTION,
 	};
 
 	private static final String[] OPTION_SORT_LABELS = new String[] {
 			"쐬̐V/Â",
 			"V񐔂̑/Ȃ",
 			"B񐔂̑/Ȃ",
 	};
 	private static final String[] OPTION_SORT_VALUES = new String[] {
 			BasicClient.SearchRouteParams.SORT_NEW,
 			BasicClient.SearchRouteParams.SORT_PLAYED_COUNT,
 			BasicClient.SearchRouteParams.SORT_ACHIEVEMENT_COUNT,
 	};
 
 	private List<Route> mRoutes;
 	private Map<String, String> mScopeOptionMap;
 	private Map<String, String> mSortOptionMap;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_route);
 
 		setupSearchScopeSpinner();
 		setupSearchSortSpinner();
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.search_route_search,
 		});
 	}
 
 	private void setupSearchScopeSpinner() {
 		SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, OPTION_SCOPE_LABELS);
 		mScopeOptionMap = new HashMap<String, String>();
 		for (int i = 0; i < OPTION_SCOPE_LABELS.length; i++) {
 			mScopeOptionMap.put(OPTION_SCOPE_LABELS[i], OPTION_SCOPE_VALUES[i]);
 		}
 
 		Spinner spinner = (Spinner) findViewById(R.id.search_route_scope);
 		spinner.setAdapter(adapter);
 	}
 
 	private void setupSearchSortSpinner() {
 		SpinnerAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, OPTION_SORT_LABELS);
 		mSortOptionMap = new HashMap<String, String>();
 		for (int i = 0; i < OPTION_SORT_LABELS.length; i++) {
 			mSortOptionMap.put(OPTION_SORT_LABELS[i], OPTION_SORT_VALUES[i]);
 		}
 
		Spinner spinner = (Spinner) findViewById(R.id.search_route_sort);
 		spinner.setAdapter(adapter);
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Route targetRoute = mRoutes.get(position);
 		Intent intent = new Intent(this, RouteActivity.class)
 				.putExtra(IntentData.EXTRA_KEY_ROUTE, targetRoute);
 		startActivity(intent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.search_route_search:
 			// TODO: use httpclient
 			mRoutes = fetchRoutes();
 			setRoutes(mRoutes);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	// TODO
 	private List<Route> fetchRoutes() {
 		Toast.makeText(this, getSelectedScopeValue(), Toast.LENGTH_SHORT).show();
 		Toast.makeText(this, getSelectedSortValue(), Toast.LENGTH_SHORT).show();
 		Toast.makeText(this, getSelectedOrderValue(), Toast.LENGTH_SHORT).show();
 
 		String searchText = ActivityUtil.getText(this, R.id.search_route_name);
 		Client client = new DebugSharedPreferencesClient(this);
 		return client.getRoutes(searchText);
 	}
 
 	private String getSelectedScopeValue() {
 		String selectedSpinnerLabel = (String) ((Spinner) findViewById(R.id.search_route_scope)).getSelectedItem();
 		return mScopeOptionMap.get(selectedSpinnerLabel);
 	}
 
 	private CharSequence getSelectedSortValue() {
		String selectedSpinnerLabel = (String) ((Spinner) findViewById(R.id.search_route_sort)).getSelectedItem();
 		return mSortOptionMap.get(selectedSpinnerLabel);
 	}
 
 	private String getSelectedOrderValue() {
 		int selectedOrderId = ((RadioGroup) findViewById(R.id.search_route_order)).getCheckedRadioButtonId();
 		switch (selectedOrderId) {
 		case R.id.search_route_order_desc:
 			return BasicClient.SearchRouteParams.ORDER_DESC;
 
 		case R.id.search_route_order_asc:
 			return BasicClient.SearchRouteParams.ORDER_ASC;
 
 		default:
 			throw new AssertionError("Unknown ID");
 		}
 	}
 
 	private void setRoutes(List<Route> routes) {
 		RoutesAdapter adapter = new RoutesAdapter(this, routes);
 		setListAdapter(adapter);
 	}
 
 }
