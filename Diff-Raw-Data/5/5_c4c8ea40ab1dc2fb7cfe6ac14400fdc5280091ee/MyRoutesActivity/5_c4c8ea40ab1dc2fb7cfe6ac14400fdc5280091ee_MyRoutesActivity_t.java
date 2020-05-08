 package jp.knct.di.c6t.ui.route;
 
 import java.util.List;
 
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.Client;
 import jp.knct.di.c6t.communication.DebugSharedPreferencesClient;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.model.User;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 public class MyRoutesActivity extends ListActivity {
 
 	private List<Route> mRoutes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_routes);
 
		Client client = new DebugSharedPreferencesClient(this);
 
		mRoutes = client.getRoutes(client.getMyUserData()); // TODO
 		RoutesAdapter adapter = new RoutesAdapter(this, mRoutes);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Route targetRoute = mRoutes.get(position);
 		Intent intent = new Intent(this, RouteActivity.class)
 				.putExtra(IntentData.EXTRA_KEY_ROUTE, targetRoute);
 		startActivity(intent);
 		finish();
 	}
 }
