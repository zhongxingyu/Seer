 package eu.isweb.animeplayer;
 
 import java.util.ArrayList;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AnimeEpizodesActivity extends ListActivity implements
 	SearchView.OnQueryTextListener {
 
 	Activity instance;
 	AnimeDatabaseManager db;
 	Menu menu;
 	ListView mListView;
 	ArrayAdapter<Epizode> mAdapter;
 	TextView mText;
 	SearchView mSearch;
 	ArrayList<Epizode> epizodeList = new ArrayList<Epizode>();
 	Anime anime;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_anime);
 
 		ActionBar actionBar = getActionBar();
 	    actionBar.setDisplayHomeAsUpEnabled(true);
 		
 		instance = this;
 		db = ((AnimeApp) getApplication()).getDB();
 
 		mText = (TextView) findViewById(android.R.id.empty);
 		mSearch = (SearchView) findViewById(R.id.search_view);
 		mListView = (ListView) findViewById(android.R.id.list);
 		mListView.setTextFilterEnabled(true);
 		mListView.setAdapter(mAdapter = new ArrayAdapter<Epizode>(this,
 				R.layout.listview_item, epizodeList){
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View v = super.getView(position, convertView, parent);
 				TextView t = (TextView) v.findViewById(android.R.id.text1);
 				t.setTextColor(epizodeList.get(position).isLastWatched() ? Color.parseColor("#FF6C00") : Color.BLACK);
 				return v;
 			}
 		});
 
 		if (extras != null) {
 			anime = (Anime) extras.getSerializable("anime");
 			downloadEpizodeList(anime.URL);
 			this.setTitle(anime.name);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		super.onResume();
 	}
 	
 	private void updateMenuFavorites() {
 		MenuItem favorites = menu.findItem(R.id.menu_favorites);
 		favorites.setIcon(db.isFavorites(anime.URL) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
 		AnimeFavoritesFragment.refreshFavorites();
 	}
 
 	public void downloadEpizodeList(String url) {
 		mText.setText(getString(R.string.downloading_data));
 
 		new AnimeDownloader<Epizode>(this) {
 			@Override
 			protected void onPostExecuteAction(ArrayList<Epizode> result) {
 
 				epizodeList.clear();
 				epizodeList.addAll(result);
 
 				mAdapter.notifyDataSetChanged();
 
 				if (!epizodeList.isEmpty()) {
 					mSearch.setVisibility(View.VISIBLE);
 					setupSearchView();
 					goToLastWatched();
 				} else {
 					mSearch.setVisibility(View.GONE);
 					mText.setText(getString(R.string.no_results_found));
 				}
 			};
 
 			@Override
 			protected ArrayList<Epizode> doInBackgroundAction(Document doc) 
 					throws Exception {
 				ArrayList<Epizode> result = new ArrayList<Epizode>();
				Elements elements = doc.select("a");
 				
 				if( elements.size() == 0) {
 					throw new Exception("try to retry");
 				}
 				
 				for (Element element : elements) {
 					if(element.text().contains("Odcinek") || element.text().contains("Ova"))
 						result.add(new Epizode( element.text(), element.attr("href") ));
 				}
 				return result;
 			};
 		}.execute(url);
 	}
 
 	protected void goToLastWatched() {
 		String lastURL = db.getLastEpizodeURL(anime.URL);
 		int position = -1;
 
 		for(int i=0; i< epizodeList.size(); i++) {
 			if(epizodeList.get(i).URL.equals(lastURL)) {
 				position = i;
 				break;
 			}
 		}
 		
 		if(position == -1) return;
 		
 		mListView.setSelection(position);
 
 		for(int i=0; i< epizodeList.size(); i++) {
 			epizodeList.get(i).lastWatched = false;
 		}
 		epizodeList.get(position).lastWatched = true;
 		mAdapter.notifyDataSetChanged();		
 	}
 
 	private void saveCurrentListState(String epizodeName, String URL) {
 		db.insertHistory(new History(anime.URL, anime.name, epizodeName, URL));
 		AnimeHistoryFragment.refreshHistory();
 		goToLastWatched();
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Epizode selectedEpizode = (Epizode) getListView().getItemAtPosition(position);
 
 		saveCurrentListState(selectedEpizode.name, selectedEpizode.URL);
 		Intent intent = new Intent(this, AnimeVideosActivity.class);
 		intent.putExtra("epizode", selectedEpizode);
 		intent.putExtra("anime", anime);
 		
 		startActivity(intent);
 		super.onListItemClick(l, v, position, id);
 	}
 
 	private void setupSearchView() {
 		mSearch.setIconifiedByDefault(false);
 		mSearch.setOnQueryTextListener(this);
 		mSearch.setSubmitButtonEnabled(false);
 		mSearch.setQueryHint(getString(R.string.enter_epizode_name));
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText) {
 		if (TextUtils.isEmpty(newText)) {
 			mListView.clearTextFilter();
 		} else {
 			mListView.setFilterText(newText.toString());
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query) {
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.menu = menu;
 		getMenuInflater().inflate(R.menu.activity_epizodes, menu);
 		updateMenuFavorites();
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_favorites:
 			db.toogleFavorites(anime);
 			updateMenuFavorites();
 			return true;
 			
 		case R.id.menu_about_anime_myaniemlist:
 			aboutAnimeLauncher(anime, "myanimelist.net");
 	    	return true;
 	    	
 		case R.id.menu_about_anime_tanuki:
 			aboutAnimeLauncher(anime, "tanuki.pl");
 	    	return true;
 	    	
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	
 	private void aboutAnimeLauncher(Anime a, String t) {
 		Intent intent = new Intent(instance, AboutAnimeActivity.class);
     	intent.putExtra("anime", a);
     	intent.putExtra("type", t);
     	instance.startActivity(intent);
 	}
 }
