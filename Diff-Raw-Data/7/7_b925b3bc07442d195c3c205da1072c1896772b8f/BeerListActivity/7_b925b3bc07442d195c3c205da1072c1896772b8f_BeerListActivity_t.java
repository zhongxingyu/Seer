 package ch.hsr.bieridee.android.activities;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.ListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.adapters.BeerListAdapter;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 import ch.hsr.bieridee.android.utils.ErrorHelper;
 
 /**
  * Activity that shows a list of all beers in our database.
  */
 public class BeerListActivity extends ListActivity implements ListView.OnScrollListener {
 
 	private final static String LOG_TAG = BeerListActivity.class.getName();
 	private BeerListAdapter adapter;
 	private ProgressDialog progressDialog;
 	private HttpHelper httpHelper;
 
 	private int currentPage = 0;
 	private static final int PAGESIZE = 12;
 	private static final int VISIBLECOUNT = 7;
 	private View loadingFooter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.beerlist);
 
 		this.httpHelper = AuthJsonHttp.create();
 
 		final ListView list = (ListView) this.getListView();
 		this.loadingFooter = getLayoutInflater().inflate(R.layout.loading_item, list, false);
 		list.addFooterView(this.loadingFooter, null, false);
 
 		this.adapter = new BeerListAdapter(this);
 		setListAdapter(this.adapter);
 		this.addOnClickListeners();
 		this.registerForContextMenu(this.getListView());
 
 		getListView().setOnScrollListener(this);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		new GetBeerData().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		final MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.refresh_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id_refreshmenu.refresh:
 				new GetBeerData().execute();
				this.currentPage++;
 				break;
 		}
 		return true;
 	}
 
 	/**
 	 * Add onClick listeners to UI elements.
 	 */
 	private void addOnClickListeners() {
 		this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				final Intent intent = new Intent(view.getContext(), BeerDetailActivity.class);
 				intent.putExtra(BeerDetailActivity.EXTRA_BEER_ID, id);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle(BeerListActivity.this.getString(R.string.beerlistContextTitle));
 		// Contextmenu delete entry.
 		// menu.add(0, v.getId(), 0, BeerListActivity.this.getString(R.string.beerlistContextDelete));
 		menu.add(0, v.getId(), 0, BeerListActivity.this.getString(R.string.beerlistContextEdit));
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		// Deletion of a beer.
 		// if (item.getTitle() == BeerListActivity.this.getString(R.string.beerlistContextDelete)) {
 		// new DeleteBeer().execute(info.id);
 		// } else
 		if (item.getTitle() == BeerListActivity.this.getString(R.string.beerlistContextEdit)) {
 			final Intent editBeerIntent = new Intent(BeerListActivity.this.getBaseContext(), BeerCreateActivity.class);
 			editBeerIntent.putExtra("beerToUpdate", info.id);
 			startActivity(editBeerIntent);
 		}
 		return true;
 	}
 
 	/**
 	 * Async task to get beers from server and update UI.
 	 */
 	private class GetBeerData extends AsyncTask<Void, Void, JSONArray> {
 
 		private boolean showDialog = true;
 
 		public GetBeerData() {
 			this.showDialog = true;
 		}
 
 		public GetBeerData(boolean showDialog) {
 			this.showDialog = showDialog;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			BeerListActivity.this.loadingFooter.setVisibility(View.VISIBLE);
 			if (this.showDialog) {
 				BeerListActivity.this.progressDialog = ProgressDialog.show(BeerListActivity.this, getString(R.string.pleaseWait), getString(R.string.loadingData), true);
 			}
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			final Map<String, String> params = new HashMap<String, String>();
 			params.put("items", PAGESIZE + "");
 			params.put("page", BeerListActivity.this.currentPage + "");
 			final String resourceUri = Res.getURIwithGETParams(Res.BEER_COLLECTION, params);
 
 			HttpResponse response = null;
 			try {
 				response = BeerListActivity.this.httpHelper.get(resourceUri);
 			} catch (IOException e) {
 				Log.d(LOG_TAG, e.getMessage(), e);
 				ErrorHelper.onError(getString(R.string.connectionError), BeerListActivity.this);
 			}
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONArray(responseText);
 					} catch (IOException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(getString(R.string.malformedData), BeerListActivity.this);
 					} catch (JSONException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(getString(R.string.malformedData), BeerListActivity.this);
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONArray result) {
 			if (result != null) {
 				BeerListActivity.this.adapter.updateData(result);
 				BeerListActivity.this.adapter.notifyDataSetChanged();
 			}
 			if (this.showDialog) {
 				BeerListActivity.this.progressDialog.dismiss();
 			}
 			BeerListActivity.this.loadingFooter.setVisibility(View.GONE);
 		}
 	}
 
 	// SUPPRESS CHECKSTYLE: Method not used but needed by interface
 	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
 		// nuffin (muffin|bluffing)
 	}
 
 	/**
 	 * Triggers on scroll state changed.
 	 * 
 	 * @param view
 	 *            The list view
 	 * @param scrollState
 	 *            The scrollstate
 	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
 	 */
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		final int last = view.getLastVisiblePosition();
 		switch (scrollState) {
 			case OnScrollListener.SCROLL_STATE_IDLE:
 				if (last >= (this.currentPage + 1) * VISIBLECOUNT) {
 					new GetBeerData(false).execute();
 					this.currentPage++;
 				}
 				break;
 			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
 				if (last >= (this.currentPage + 1) * VISIBLECOUNT) {
 					new GetBeerData(false).execute();
 					this.currentPage++;
 				}
 				break;
 			case OnScrollListener.SCROLL_STATE_FLING:
 				break;
 		}
 	}
 
 	/**
 	 * Async task to delete beer from server and update UI. --> Currently not supported, thus commented out.
 	 */
 	// private class DeleteBeer extends AsyncTask<Long, Void, Void> {
 	// @Override
 	// protected void onPreExecute() {
 	// BeerListActivity.this.progressDialog = ProgressDialog.show(BeerListActivity.this, getString(R.string.pleaseWait),
 	// getString(R.string.loadingData), true);
 	// }
 	//
 	// @Override
 	// protected Void doInBackground(Long... ids) {
 	// final String uri = Res.getURI(Res.BEER_DOCUMENT, ids[0].toString());
 	// final HttpResponse response = BeerListActivity.this.httpHelper.delete(uri);
 	//
 	// if (response != null) {
 	// final int statusCode = response.getStatusLine().getStatusCode();
 	// if (statusCode == HttpStatus.SC_OK) {
 	// BeerListActivity.this.adapter.remove(ids[0]);
 	// }
 	// }
 	// return null;
 	// }
 	//
 	// @Override
 	// protected void onPostExecute(Void result) {
 	// BeerListActivity.this.adapter.notifyDataSetChanged();
 	// BeerListActivity.this.progressDialog.dismiss();
 	// }
 	// }
 
 }
