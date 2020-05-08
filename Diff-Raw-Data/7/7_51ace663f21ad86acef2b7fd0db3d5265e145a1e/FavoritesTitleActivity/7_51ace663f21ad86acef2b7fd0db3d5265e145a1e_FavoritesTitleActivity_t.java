 package pl.tabhero;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FavoritesTitleActivity extends Activity {
 	
 	DBAdapter db = new DBAdapter(this);
 	
 	private TextView chosenFavPerf;
 	private EditText editFavTitle;
 	private ListView searchFavTitleListView;
 	private ArrayAdapter<String> listAdapter;
 	private List<String> listOfFavTitle;
 	String performerName;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.favoritestitle);
         
         chosenFavPerf = (TextView) findViewById(R.id.chosenFavPerformer);
         editFavTitle = (EditText) findViewById(R.id.editFavTitle);
         searchFavTitleListView = (ListView) findViewById(R.id.searchFavTitleListView);
         
         Intent i = getIntent();
         Bundle extras = i.getExtras();
         performerName = extras.getString("performerName");
         chosenFavPerf.setText(performerName);
         
         List<List<String>> listOfLists = addTitleFromBase(performerName);
         List<String> listTitle = listOfLists.get(0);
         final List<String> listTab = listOfLists.get(1);
         final List<String> listUrl = listOfLists.get(2);
         listOfFavTitle = listTitle;
         
         listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfFavTitle);
         searchFavTitleListView.setAdapter(listAdapter);
         hideKeyboard();
         searchFavTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	Intent i = new Intent(FavoritesTitleActivity.this, FavTabViewActivity.class);
             	Bundle bun = new Bundle();
             	bun.putString("performerName", performerName);
             	bun.putString("songTitle", listOfFavTitle.get(position));
             	bun.putString("songTab", listTab.get(position));
             	bun.putString("songUrl", listUrl.get(position));
     			i.putExtras(bun);
     			startActivity(i);	
            }
         } );
         
         
 	}
 	
 	public void searchTitleView(View v) {
 		hideKeyboard();
 		listOfFavTitle.clear();
 		List<List<String>> listOfLists = addTitleFromBase(performerName);
         List<String> listTitle = listOfLists.get(0);
         final List<String> listTab = listOfLists.get(1);
         final List<String> listUrl = listOfLists.get(2);
 		listOfFavTitle = listTitle;
 		String title = new String();
 		final List<String> listOfChosenTitleFromBase = new ArrayList<String>();
 		final List<String> listOfChosenTabFromBase = new ArrayList<String>();
 		final List<String> listOfChosenUrlFromBase = new ArrayList<String>();
     	title = editFavTitle.getText().toString().toLowerCase();
     	if(title.length() > 0) {
     		if(title.charAt(0) == ' ')
     			Toast.makeText(getApplicationContext(), R.string.hintSpace, Toast.LENGTH_LONG).show();
     		else {
     			boolean checkContains;
     			listOfChosenTitleFromBase.clear();
     			//for(String p : listOfFavTitle) {
     			for(int i = 0; i < listOfLists.get(0).size(); i++) {
     				//checkContains = p.toLowerCase().contains(title);
     				checkContains = listOfLists.get(0).get(i).toLowerCase().contains(title);
     				if(checkContains == true) {
     					//listOfChosenTitleFromBase.add(p);
     					listOfChosenTitleFromBase.add(listOfLists.get(0).get(i));
     					listOfChosenTabFromBase.add(listOfLists.get(1).get(i));
     					listOfChosenUrlFromBase.add(listOfLists.get(2).get(i));
     				}
     			}
     			listOfFavTitle.clear();
     			listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfChosenTitleFromBase);
     			searchFavTitleListView.setAdapter(listAdapter);
     			searchFavTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     					Intent i = new Intent(FavoritesTitleActivity.this, FavTabViewActivity.class);
     					Bundle bun = new Bundle();
     					bun.putString("performerName", performerName);
     					bun.putString("songTitle", listOfChosenTitleFromBase.get(position));
     					bun.putString("songTab", listOfChosenTabFromBase.get(position));
     					bun.putString("songUrl", listOfChosenUrlFromBase.get(position));
     					i.putExtras(bun);
     					startActivity(i);	
     				}
     			} );
     		}
     		
     	} else {
     		listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfFavTitle);
 			searchFavTitleListView.setAdapter(listAdapter);
 			searchFavTitleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					Intent i = new Intent(FavoritesTitleActivity.this, FavTabViewActivity.class);
 					Bundle bun = new Bundle();
 					bun.putString("performerName", performerName);
 					bun.putString("songTitle", listOfFavTitle.get(position));
 					bun.putString("songTab", listTab.get(position));
 					bun.putString("songUrl", listUrl.get(position));
 					i.putExtras(bun);
 					startActivity(i);	
 				}
 			} );
     	}
 		
 	}
 	
 	public List<List<String>> addTitleFromBase(String perfName) {
     	List<String> listTitles = new ArrayList<String>();
     	List<String> listTabs = new ArrayList<String>();
     	List<String> listUrl = new ArrayList<String>();
     	List<List<String>> listOfList = new ArrayList<List<String>>();
     	db.open();
         Cursor c = db.getRecordPerf(perfName);
     	//Cursor c = db.getAllRecords();
         if (c.moveToFirst())
         {
             do {
             	//if(performerName.equals(c.getString(1))) {
             		listTitles.add(c.getString(2));
             		listTabs.add(c.getString(3));
             		listUrl.add(c.getString(4));
             		//Log.d("TITLE", c.getString(2));
             		//Log.d("TITLE", c.getString(0));
             		//Log.d("AAA","AAA");
             	//}
             } while (c.moveToNext());
         }
         db.close();
         listOfList.add(listTitles);
         listOfList.add(listTabs);
         listOfList.add(listUrl);
         return listOfList;      
     }
 	
 	private void hideKeyboard() {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(editFavTitle.getWindowToken(), 0);
 	}
 	
 	//protected void onPause() {
 	    // TODO Auto-generated method stub
 	//    super.onPause();
 	//    finish();
 	//}
 	
 	protected void onResume() {
 		super.onResume();
 		Button btn = (Button) findViewById(R.id.searchFavTitleBtn);
 		btn.performClick();
 	}
 	
 	@Override
     public void onBackPressed() {
     	Intent intent = new Intent(this, FavoritesActivity.class);
    	//finish();
     	startActivity(intent);
     }
 
 }
