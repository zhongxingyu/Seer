 package pl.tabhero;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
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
  
 public class SearchActivity extends Activity {
 	
 	private ListView searchListView;
 	private EditText editPerformer;
 	private Button btnSearch;
 	private ArrayAdapter<String> listAdapter;
 	private ProgressDialog progressDialog;
 	private List<String[]> artists = new ArrayList<String[]>();
 	private ArrayList<String> artistNames = new ArrayList<String>();
 	private ArrayList<String> artistUrl = new ArrayList<String>();
 	//private boolean enter; 
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.search);
         
         btnSearch = (Button) findViewById(R.id.searchBtn);
 		editPerformer = (EditText) findViewById(R.id.editPerformer);
 		searchListView = (ListView) findViewById(R.id.searchListView);
 		
 		editPerformer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 		            btnSearch.performClick();
 		            return true;
 		        }
 		        return false;
 		    }
 		});
 		
     }
     public void searchView(View v) {
     	
     	artists.clear();
     	artistNames.clear();
     	artistUrl.clear();
     	String performer = new String();
     	performer = editPerformer.getText().toString().toLowerCase();
 		hideKeyboard();
 		//if(enter == true)
 			//Log.d("ENTER_TURE", Integer.toString(performer.length()));
 		//else if(enter == false)
 			//Log.d("ENTER_False", Integer.toString(performer.length()));
 		
 		//Log.d("CONNECTION", String.valueOf(checkInternetConnection()));
 		if(!(performer.length() > 0)) 
 			Toast.makeText(getApplicationContext(), R.string.hintEmpty, Toast.LENGTH_LONG).show();
 		else if (performer.charAt(0) == ' ')
 			Toast.makeText(getApplicationContext(), R.string.hintSpace, Toast.LENGTH_LONG).show();
 		else if(!(checkInternetConnection()))
 			Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
 		else {
 				new connect().execute(performer);
 		}
     }
     
     public class connect extends AsyncTask<String, Void, List<String[]>>{
     	
     	@Override
     	 protected void onPreExecute() {
     		startProgressBar();
     	 }
     	
     	@Override
     	 protected void onPostExecute(List<String[]> chosenPerformers) {
 			for(String[] art : chosenPerformers) {
 				artistNames.add(art[1]);
 				artistUrl.add(art[0]);
 			}
 			//Log.d("2222", "2222");
 			listAdapter = new ArrayAdapter<String>(SearchActivity.this, R.layout.artistsnet, artistNames);
 			searchListView.setAdapter(listAdapter);
 			searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	            	Intent i = new Intent(SearchActivity.this, SearchTitleActivity.class);
 	            	Bundle bun = new Bundle();
 	            	bun.putString("performerName", artistNames.get(position));
 	    			bun.putString("performerUrl", artistUrl.get(position));
 	    			i.putExtras(bun);
 	    			startActivityForResult(i, 500);
 	    			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
 	            }
 	        } );
 			//Log.d("3333", "3333");
     		closeProgressBar();
     	 }
     	
 		@Override
 		protected List<String[]> doInBackground(String... params) {
 			String performer = params[0];
 			String url = "http://www.chords.pl/wykonawcy/";
 	    	List<String[]> chosenPerformers = new ArrayList<String[]>();
 	    	Document doc = preperAndConnect(performer, url);
 	    	String codeFind0 = doc.select("tr.v0").toString();
 	    	String codeFind1 = doc.select("tr.v1").toString();
 	    	String codeFind = codeFind0 + codeFind1;
 	    	Document docFind = Jsoup.parse(codeFind);
 	    	Elements performers = docFind.select("a[href]");
 	    	String[][] array = new String[performers.size()][2];
 	    	boolean checkContains;
 	    	//Log.d("1111", "1111");
 	    	for(int i = 0; i < performers.size(); i++) {
 	    		array[i][0] = performers.get(i).attr("href");
 	    		array[i][1] = performers.get(i).toString();
 	    		array[i][1] = Jsoup.parse(array[i][1]).select("a").first().ownText();
 	    		array[i][1] = array[i][1].replace("\\", "");
 	    		String p = array[i][1].toLowerCase();
 	    		checkContains = p.contains(performer);
 	    		if(checkContains == true) {
 	    			chosenPerformers.add(array[i]);
 	    		}
 	    	}
 			return chosenPerformers;
 		} 
     }
     
     private Document preperAndConnect(String performer, String url) {
 		Document doc = null;
 		if(Character.isDigit(performer.charAt(0))) {
     		url = url + "1";
     		doc = connect(url);
     	}
     	else {
     		String temp = performer;
     		if(performer.charAt(0) == 'ą')
     			temp = performer.replaceAll("^ą", "a");
     		else if(performer.charAt(0) == 'ć')
     			temp = performer.replaceAll("^ć", "c");
     		else if(performer.charAt(0) == 'ę')
     			temp = performer.replaceAll("^ę", "e");
     		if(performer.charAt(0) == 'ł')
     			temp = performer.replaceAll("^ł", "l");
     		if(performer.charAt(0) == 'ń')
     			temp = performer.replaceAll("^ń", "n");
     		if(performer.charAt(0) == 'ó')
     			temp = performer.replaceAll("^ó", "o");
     		if(performer.charAt(0) == 'ś')
     			temp = performer.replaceAll("^ś", "s");
     		if(performer.charAt(0) == 'ź')
     			temp = performer.replaceAll("^ź", "z");
     		if(performer.charAt(0) == 'ż')
     			temp = performer.replaceAll("^ż", "z");
     		url = url + temp;
     		doc = connect(url);
     	}
 		return doc;
 	}
     
     private Document connect(String url) {
     	Document doc = null;
     	try {
 			doc = Jsoup.connect(url).get();
 		} catch (MalformedURLException ep) {
 			Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(), R.string.errorInInternetConnection, Toast.LENGTH_LONG).show();
 		}
     	return doc;
     }
     
     private void hideKeyboard() {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(editPerformer.getWindowToken(), 0);
 	}
     
     private void startProgressBar() {
     	setProgressBarIndeterminateVisibility(true);
         progressDialog = ProgressDialog.show(SearchActivity.this, getString(R.string.srchPerf), getString(R.string.wait));
     }
     
     private void closeProgressBar() {
     	setProgressBarIndeterminateVisibility(false);
 		progressDialog.dismiss();
     }
     
     /*private void setVisibilityOf(View v, boolean visible) {
 		int visibility = visible ? View.VISIBLE : View.GONE;
 		v.setVisibility(visibility);
 	}*/
     
     public boolean checkInternetConnection() {
         ConnectivityManager cm = (ConnectivityManager) SearchActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
         if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
             return true;
         } else {
             return false;
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
     }
 }
