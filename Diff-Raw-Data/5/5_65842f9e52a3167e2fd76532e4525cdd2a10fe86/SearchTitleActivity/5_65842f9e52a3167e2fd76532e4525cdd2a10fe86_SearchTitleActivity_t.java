 package pl.tabhero;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchTitleActivity extends Activity {
 	
 	private ListView searchListView;
 	private EditText editTitle;
 	private Button btnTitleSearch;
 	private ArrayAdapter<String> listAdapter;
 	private List<String[]> songs = new ArrayList<String[]>();
 	private ArrayList<String> songTitle = new ArrayList<String>();
 	private ArrayList<String> songUrl = new ArrayList<String>();
 	private ProgressDialog progressDialog;
 	//private ProgressDialog progressDialogTab;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.searchtitle);
         
         Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		final String performerName = extras.getString("performerName");
         TextView chosenPerformer = (TextView) findViewById(R.id.chosenPerformer);
         chosenPerformer.setText(performerName);
         
         editTitle = (EditText) findViewById(R.id.editTitle);
 		btnTitleSearch = (Button) findViewById(R.id.searchTitleBtn);
 		searchListView = (ListView) findViewById(R.id.searchTitleListView);
         
         editTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
		                actionId == EditorInfo.IME_ACTION_DONE ||
		                event.getAction() == KeyEvent.ACTION_DOWN &&
		                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 		            btnTitleSearch.performClick();
 		            return true;
 		        }
 		        return false;
 		    }
 		});
     }
 	
 	@SuppressWarnings("unchecked")
 	public void searchTitleView(View v) {
 		
 		songs.clear();
 		songTitle.clear();
 		songUrl.clear();
 		
 		String title = new String();
 		title = editTitle.getText().toString().toLowerCase();
 		hideKeyboard();
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		String performerUrl = extras.getString("performerUrl");
 
 		ArrayList<String> passing = new ArrayList<String>();
 		passing.add(performerUrl);
 		passing.add(title);
 		if(!(checkInternetConnection()))
 			Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 		else 
 			new connect().execute(passing);
 	}
 	
 	public class connect extends AsyncTask<ArrayList<String>, Void, List<String[]>> {
 		
 		@Override
    	 	protected void onPreExecute() {
 			startProgressBar(getString(R.string.srchSong));
 		}
    	
 		@Override
 		protected void onPostExecute(List<String[]> chosenTitles) {
 			for(String[] sng : chosenTitles) {
 				Log.d("ART1", sng[1]);
 				songTitle.add(sng[1]);
 				Log.d("ART0", sng[0]);
 				songUrl.add(sng[0]);
 			}
 			listAdapter = new ArrayAdapter<String>(SearchTitleActivity.this, R.layout.titlesnet, songTitle);
 			searchListView.setAdapter(listAdapter);
 			closeProgressBar();
 			searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 	            @SuppressWarnings("unchecked")
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	            	String posUrl = songUrl.get(position);
 	            	String posTitle = songTitle.get(position);
 	            	ArrayList<String> passing = new ArrayList<String>();
 	            	passing.add(posUrl);
 	            	passing.add(posTitle);
 	            	if(!(checkInternetConnection()))
 	            		Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 	            	else
 	            		new getTablature().execute(passing);      		
 	            }
 			} );
    	 	}
 
 		@Override
 		protected List<String[]> doInBackground(ArrayList<String>... params) {
 			ArrayList<String> passing = params[0];
 			String urlPerformerSongs = passing.get(0);
 			String title = passing.get(1);
 			String url = "http://www.chords.pl";
 	    	List<String[]> chosenTitles = new ArrayList<String[]>();
 	    	Document doc = connectUrl(url + urlPerformerSongs);
 	    	String codeSongs = doc.select("table.piosenki").toString();
 	    	Document songs = Jsoup.parse(codeSongs);
 	    	Elements chosenLineSong = songs.select("a[href]");
 	    	String[][] array = new String[chosenLineSong.size()][2];
 	    	boolean checkContains;
 	    	for(int i = 0; i < chosenLineSong.size(); i++) {
 	    		array[i][0] = chosenLineSong.get(i).attr("href");
 	    		array[i][0] = url + array[i][0];
 	    		array[i][1] = chosenLineSong.get(i).toString();
 	    		array[i][1] = Jsoup.parse(array[i][1]).select("a").first().ownText();
 	    		array[i][1] = array[i][1].replace("\\", "");
 	    		String p = array[i][1].toLowerCase();
 	    		checkContains = p.contains(title);
 	    		if(checkContains == true) {
 	    			chosenTitles.add(array[i]);
 	    		}
 	    	}
 			return chosenTitles;
 		}
 	}
     
 	public class getTablature extends AsyncTask<ArrayList<String>, Void, ArrayList<String>>{
 		
 		@Override
    	 	protected void onPreExecute() {
 			startProgressBar(getString(R.string.srchTab));
    	 	}
    	
 		@Override
 		protected void onPostExecute(ArrayList<String> passing) {
 			closeProgressBar();
 			String tablature = passing.get(0);
 			String songTitle = passing.get(1);
 			String songUrl = passing.get(2);
 			Intent i = getIntent();
 			Bundle extras = i.getExtras();
 			final String performerName = extras.getString("performerName");
 			Intent intent = new Intent(SearchTitleActivity.this, TabViewActivity.class);
 			Bundle bun = new Bundle();
 			bun.putString("performerName", performerName);
 			bun.putString("songTitle", songTitle);
 			bun.putString("songUrl", songUrl);
 			bun.putString("tab", tablature);
 			intent.putExtras(bun);
 			startActivityForResult(intent, 500);
 			overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
    	 	}
 
 		@Override
 		protected ArrayList<String> doInBackground(ArrayList<String>... params) {
 			ArrayList<String> passing = params[0];
 			String url = passing.get(0);
 			String title = passing.get(1);
 			Document doc = connectUrl(url);
 	    	Element elements = doc.select("pre").first();
 	    	String tab = elements.text();
 	    	String[] table = tab.split("\n");
 	    	tab = "";
 	    	for (int i = 3; i < table.length; i++)
 	    		tab += table[i] + "\n";
 	    	ArrayList<String> passing2 = new ArrayList<String>();
 	    	passing2.add(tab);
 	    	passing2.add(title);
 	    	passing2.add(url);
 			return passing2;
 		}
 	}
 	
 	private Document connectUrl(String url) {
 		Document doc = null;
 		try {
 			doc = Jsoup.connect(url).get();
 		}  catch (MalformedURLException ep) {
 			Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 		}
 		return doc;
 	}
 	
 	private void startProgressBar(String title) {
 		setProgressBarIndeterminateVisibility(true);
 		progressDialog = ProgressDialog.show(SearchTitleActivity.this, title, getString(R.string.wait));
 	}
 	
 	private void closeProgressBar() {
 		setProgressBarIndeterminateVisibility(false);
 		progressDialog.dismiss();
 	}
 	
 	private void hideKeyboard() {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(editTitle.getWindowToken(), 0);
 	}
 	
 	public boolean checkInternetConnection() {
         ConnectivityManager cm = (ConnectivityManager) SearchTitleActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
         if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
             return true;
         } else {
             return false;
         }
     }
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
     }
 }
