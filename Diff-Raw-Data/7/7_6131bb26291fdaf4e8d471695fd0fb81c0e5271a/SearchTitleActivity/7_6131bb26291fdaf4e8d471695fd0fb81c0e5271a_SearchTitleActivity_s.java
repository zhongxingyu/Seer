 package pl.tabhero.net;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 import org.jsoup.nodes.Document;
 import pl.tabhero.R;
 import pl.tabhero.core.MenuFunctions;
 import pl.tabhero.core.Songs;
 import pl.tabhero.core.Tablature;
 import pl.tabhero.utils.FileUtils;
 import pl.tabhero.utils.InternetUtils;
 import pl.tabhero.utils.MenuUtils;
 import pl.tabhero.utils.MyEditorKeyActions;
 import pl.tabhero.utils.MyFilter;
 import pl.tabhero.utils.MyGestureDetector;
 import pl.tabhero.utils.MyOnTouchListener;
 import pl.tabhero.utils.MyTelephonyManager;
 import pl.tabhero.utils.WifiConnection;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchTitleActivity extends Activity {
 	
 	private ListView searchListView;
 	private EditText editTitle;
 	private ImageButton btnTitleSearch;
 	private ArrayAdapter<String> listAdapter;
 	private MyProgressDialogs progressDialog = new MyProgressDialogs(this);
 	private static final int MENUWIFI = Menu.FIRST;
 	private String chordsUrl = "http://www.chords.pl";
 	private GestureDetector gestureDetector;
 	private MyTelephonyManager device = new MyTelephonyManager(this);
 	private Connect connect = new Connect();
 	private InternetUtils myWifi = new InternetUtils(this);
 	
 	@SuppressWarnings("deprecation")
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.searchtitle);
 
         device.setHomeButtonEnabledForICS();
         
         FileUtils fileUtils = new FileUtils(this);
         fileUtils.checkIfMax();
 
         Intent i = getIntent();
 		Bundle extras = i.getExtras();
 
 		final String performerName = extras.getString("performerName");
         TextView chosenPerformer = (TextView) findViewById(R.id.chosenPerformer);
         chosenPerformer.setText(performerName);
 
         editTitle = (EditText) findViewById(R.id.editTitle);
 		btnTitleSearch = (ImageButton) findViewById(R.id.searchTitleBtn);
 		searchListView = (ListView) findViewById(R.id.searchTitleListView);
 
 		InputFilter filter = new MyFilter();
 		editTitle.setFilters(new InputFilter[]{filter});
 
 		TextView.OnEditorActionListener myEditorKeyActions = new MyEditorKeyActions(btnTitleSearch);
 		editTitle.setOnEditorActionListener(myEditorKeyActions);
 
         gestureDetector = new GestureDetector(new MyGestureDetector(this));
 
         OnTouchListener myOnTouchListener = new MyOnTouchListener(gestureDetector);
         searchListView.setOnTouchListener(myOnTouchListener);
     }
 
 	public void searchTitleView(View v) {
 		String typedTitle = editTitle.getText().toString().toLowerCase();
 		device.hideKeyboard(editTitle);
 
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		String performerUrl = extras.getString("performerUrl");
 
 		Songs song = new Songs(typedTitle, performerUrl);
 		if(!(myWifi.checkInternetConnection())) {
 			Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 		} else {
 			AsyncTask<Void, Void, Boolean> checkConnection = new CheckConnection(this).execute();
 			try {
 				if(checkConnection.get()) {
 					new ConnectToTitles().execute(song);
 				} else {
 					Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public class ConnectToTitles extends AsyncTask<Songs, Void, Songs> {
 		
 		@Override
    	 	protected void onPreExecute() {
 			progressDialog.start(getString(R.string.srchSong));
 		}
    	
 		@Override
 		protected void onPostExecute(final Songs song) {
 			progressDialog.close();
 			if(!connect.errorConnection) {
 				song.setListOfTitles();
 				song.setListOfUrls();
 				listAdapter = new ArrayAdapter<String>(SearchTitleActivity.this, R.layout.titlesnet, song.listOfTitles);
 				searchListView.setAdapter(listAdapter);
 				searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 						String posUrl = song.listOfSongUrls.get(position);
 						String posTitle = song.listOfTitles.get(position);
 						Tablature tablature = new Tablature(posTitle, posUrl);
 						if(!(myWifi.checkInternetConnection())) {
 							Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 						} else {
 							AsyncTask<Void, Void, Boolean> checkConnection2 = new CheckConnection(SearchTitleActivity.this).execute();
 							try {
 								if(checkConnection2.get()) {
 									new getTablature().execute(tablature);
 								} else {
 									Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 								}
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							} catch (ExecutionException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				});
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.websiteConnectionError, Toast.LENGTH_LONG).show();
 			}
    	 	}
 
 		@Override
 		protected Songs doInBackground(Songs... params) {
 			Songs song = params[0];
 	    	Document doc = connect.tryEnable(chordsUrl + song.performerUrl);
 	    	if(!connect.errorConnection) {
 	    		song.setMapOfChosenTitles(doc);
 			}
 	    	return song;
 		}
 	}
 	
 	public class getTablature extends AsyncTask<Tablature, Void, Tablature>{
 		
 		
 		@Override
    	 	protected void onPreExecute() {
 			progressDialog.start(getString(R.string.srchTab));
    	 	}
    	
 		@Override
 		protected void onPostExecute(Tablature tablature) {
 			progressDialog.close();
 			if(!connect.errorConnection) {
 				Intent i = getIntent();
 				Bundle extras = i.getExtras();
 				final String performerName = extras.getString("performerName");
 				Intent intent = new Intent(SearchTitleActivity.this, TabViewActivity.class);
 				Bundle bun = new Bundle();
 				bun.putString("performerName", performerName);
 				bun.putString("songTitle", tablature.songTitle);
 				bun.putString("songUrl", tablature.songUrl);
 				bun.putString("tab", tablature.songTablature);
 				intent.putExtras(bun);
 				startActivity(intent);
 				overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
 			} else {
 				Toast.makeText(getApplicationContext(), R.string.websiteConnectionError, Toast.LENGTH_LONG).show();
 			}
    	 	}
 
 		@Override
 		protected Tablature doInBackground(Tablature... params) {
 			Tablature tablature = params[0];
 			Document tablatureDocument = connect.tryEnable(tablature.songUrl);
 			if(!connect.errorConnection) {
 				tablature.setSongTablature(tablatureDocument);
 			}
 			return tablature;
 		}
 	}
 	
 	@Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 		MenuUtils myMenuWifiUtils = new MenuUtils(this, menu);
 		myMenuWifiUtils.setMyWifiMenu();
     	return super.onPrepareOptionsMenu(menu);
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		closeOptionsMenu();
 		MenuFunctions menuFunc = new MenuFunctions(this);
 	    switch (item.getItemId()) {
 	    case android.R.id.home:
 	    	device.goHomeScreen();
 	    	return true;
 	    case MENUWIFI:
 	    	new WifiConnection(this).execute();
 	    	return true;
 	    case R.id.minmax:
 	    	try {
 				menuFunc.minMax();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	    	return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
     }
 	
 	@Override
     public void onBackPressed() {
 		SearchTitleActivity.this.finish();
     	super.onBackPressed();
     	overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
     }
 	
 	@Override
     public void onResume() {
     	FileUtils fileUtils = new FileUtils(this);
         fileUtils.checkIfMax();
     	super.onResume();
     }
 }
