 package pl.tabhero.net;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 import org.jsoup.nodes.Document;
 import pl.tabhero.R;
 import pl.tabhero.core.MenuFunctions;
 import pl.tabhero.core.Performers;
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
  
 public class SearchActivity extends Activity {
 	
 	private ListView searchListView;
 	private EditText editPerformer;
 	private ImageButton btnSearch;
 	private ArrayAdapter<String> listAdapter;
 	private MyProgressDialogs progressDialog = new MyProgressDialogs(this);
 	private static final int MENUWIFI = Menu.FIRST;
 	private String chordsUrl = "http://www.chords.pl/wykonawcy/";
 	private GestureDetector gestureDetector;
 	private MyTelephonyManager device = new MyTelephonyManager(this);
 	private Connect connect = new Connect();
 	
     @SuppressWarnings("deprecation")
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search);
         
         FileUtils fileUtils = new FileUtils(this);
         fileUtils.checkIfMax();
         
         device.setHomeButtonEnabledForICS();
         
         btnSearch = (ImageButton) findViewById(R.id.searchBtn);
 		editPerformer = (EditText) findViewById(R.id.editPerformer);
 		searchListView = (ListView) findViewById(R.id.searchListView);
 		
 		InputFilter filter = new MyFilter();
 		editPerformer.setFilters(new InputFilter[]{filter}); 
 		
 		TextView.OnEditorActionListener myEditorKeyActions = new MyEditorKeyActions(btnSearch);
 		editPerformer.setOnEditorActionListener(myEditorKeyActions);
 		
 		gestureDetector = new GestureDetector(new MyGestureDetector(this));
         
 		OnTouchListener myOnTouchListener = new MyOnTouchListener(gestureDetector);
         searchListView.setOnTouchListener(myOnTouchListener);
     }
     
     public void searchView(View v) {
     	String typedPerformer = editPerformer.getText().toString().toLowerCase();
     	Performers performer = new Performers(typedPerformer);
 		device.hideKeyboard(editPerformer);
 		
 		InternetUtils myWifi = new InternetUtils(this);
 		
 		if(!(performer.typedName.length() > 0)) 
 			Toast.makeText(getApplicationContext(), R.string.hintEmpty, Toast.LENGTH_LONG).show();
 		else if ((performer.typedName.charAt(0) == ' ') || (performer.typedName.charAt(0) == '.'))
 			Toast.makeText(getApplicationContext(), R.string.hintSpace, Toast.LENGTH_LONG).show();
 		else if(!(myWifi.checkInternetConnection()))
 			Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 		else {
 			AsyncTask<Void, Void, Boolean> checkConnection = new CheckConnection(this).execute();
 			try {
 				if(checkConnection.get()) {
 					new ConnectToPerformers().execute(performer);
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
     
     public class ConnectToPerformers extends AsyncTask<Performers, Void, Performers>{
     	
     	@Override
     	 protected void onPreExecute() {
     		progressDialog.start(getString(R.string.srchPerf));
     	 }
     	
     	@Override
     	 protected void onPostExecute(final Performers performer) {
     		if(!connect.errorConnection) {
     			performer.setListOfNames();
     			performer.setListOfUrls();
     		
     			listAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.artistsnet, performer.listOfNames);
     			searchListView.setAdapter(listAdapter);
     			searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     					Intent i = new Intent(SearchActivity.this, SearchTitleActivity.class);
     					Bundle bun = new Bundle();
     					bun.putString("performerName", performer.listOfNames.get(position));
     					bun.putString("performerUrl", performer.listOfUrls.get(position));
     					i.putExtras(bun);
     					startActivity(i);
     					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
     				}
     			});
     		} else {
 				Toast.makeText(getApplicationContext(), R.string.websiteConnectionError, Toast.LENGTH_LONG).show();
 			}
     		progressDialog.close();
     	 }
     	
 		@Override
 		protected Performers doInBackground(Performers... params) {
 			Performers performer = params[0];
 			String url = chordsUrl;
 			Document doc = prepareAndConnect(performer.typedName, url);
 			if(!connect.errorConnection) {
 				performer.setMapOfChosenPerformers(doc);
 			}
 			return performer;
 		} 
     }
     
     private Document prepareAndConnect(String performer, String url) {
 		Document doc = null;
 		if(Character.isDigit(performer.charAt(0))) {
     		url = url + "1";
     		doc = connect.tryEnable(url);
     	}
     	else {
     		String temp = performer;
     		switch(performer.charAt(0)) {
     			case 'ą':
     				temp = performer.replaceAll("^ą", "a");
     				break;
     			case 'ć':
     				temp = performer.replaceAll("^ć", "c");
     				break;
     			case 'ę':
     				temp = performer.replaceAll("^ę", "e");
     				break;
     			case 'ł':
     				temp = performer.replaceAll("^ł", "l");
     				break;
     			case 'ń':
     				temp = performer.replaceAll("^ń", "n");
     				break;
     			case 'ó':
     				temp = performer.replaceAll("^ó", "o");
     				break;
     			case 'ś':
     				temp = performer.replaceAll("^ś", "s");
     				break;
     			case 'ź':
     				temp = performer.replaceAll("^ź", "z");
     				break;
     			case 'ż':
     				temp = performer.replaceAll("^ż", "z");
     				break;
     		}
     		url = url + temp;
     		doc = connect.tryEnable(url);
     	}
 		return doc;
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
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
     }
     
     @Override
     public void onBackPressed() {
     	SearchActivity.this.finish();
     	super.onBackPressed();
     	overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
     }
     
     @Override
     public void onResume() {
     	FileUtils fileUtils = new FileUtils(this);
         fileUtils.checkIfMax();
        openOptionsMenu();
     	super.onResume();
     }
 }
