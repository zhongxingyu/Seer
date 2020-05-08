 package pl.tabhero;
  
 import java.util.ArrayList;
 import java.util.List;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
  
 public class FavoritesActivity extends Activity {
 	
 	DBAdapter db = new DBAdapter(this); 
 	
 	private ListView searchListView;
 	private EditText editFavPerformer;
 	private ArrayList<String> listOfFavPerfs;
 	private ArrayList<String> listOfChosenPerfsFromBase;
 	private ArrayAdapter<String> listAdapter;
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.favorites);
         
         editFavPerformer = (EditText) findViewById(R.id.editFavPerformer);
         searchListView = (ListView) findViewById(R.id.searchFavListView);
         
         
         //listOfFavPerfs = addPerfFromBase();
         listOfChosenPerfsFromBase = addPerfFromBase();
 
         listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfChosenPerfsFromBase);
         searchListView.setAdapter(listAdapter);
         hideKeyboard();
         searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	Intent i = new Intent(FavoritesActivity.this, FavoritesTitleActivity.class);
             	Bundle bun = new Bundle();
             	bun.putString("performerName", listOfChosenPerfsFromBase.get(position));
     			i.putExtras(bun);
     			startActivity(i);	
            }
         } );
         
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.faveeditcheckbox, menu);
 	    return true;
 	}
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.delFromFavWithCheckBox:
 	        startEditActivity();
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
     
     private void startEditActivity() {
     	Log.d("1111", "1111");
     	ArrayList<String> listToEdit = new ArrayList<String>();
     	//if(listOfChosenPerfsFromBase.size() > 0) {
     		Log.d("2222", "2222");
     		listToEdit = listOfChosenPerfsFromBase;
     	//} else {
     		//Log.d("3333", "3333;");
     		//listToEdit = listOfFavPerfs;
     	//}
     	Intent i = new Intent(FavoritesActivity.this, EditFavPerfs.class);
 		Bundle bun = new Bundle();
 		Log.d("4444", "4444");
 		bun.putStringArrayList("listOfPerformers", listToEdit);
 		i.putExtras(bun);
 		Log.d("5555", "5555");
 		startActivity(i);	
     }
     
     public void searchView(View v) {
     	Log.d("START_BTN", "START_BTN");
     	hideKeyboard();
     	listOfChosenPerfsFromBase.clear();
     	//Log.d("1111","1111");
     	listOfChosenPerfsFromBase = addPerfFromBase();
     	//Log.d("2222", "2222");
     	//listOfFavPerfs.clear();
     	//Log.d("3333", "3333");
     	listOfFavPerfs = addPerfFromBase();
     	//Log.d("4444", "4444");
     	String performer = new String();
     	//final List<String> listOfChosenPerfsFromBase = new ArrayList<String>();
     	Log.d("1111","1111");
     	performer = editFavPerformer.getText().toString().toLowerCase();
     	
     	if(performer.length() > 0) {
     		if(performer.charAt(0) == ' ')
     			Toast.makeText(getApplicationContext(), R.string.hintSpace, Toast.LENGTH_LONG).show();
     		else {
     			boolean checkContains;
     			listOfChosenPerfsFromBase.clear();
     			for(String p : listOfFavPerfs) {
     				checkContains = p.toLowerCase().contains(performer);
     				if(checkContains == true)
     					listOfChosenPerfsFromBase.add(p);
     			}
     			listOfFavPerfs.clear();
     			listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfChosenPerfsFromBase);
     			searchListView.setAdapter(listAdapter);
     			searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     					Intent i = new Intent(FavoritesActivity.this, FavoritesTitleActivity.class);
     					Bundle bun = new Bundle();
     					bun.putString("performerName", listOfChosenPerfsFromBase.get(position));
     					i.putExtras(bun);
     					startActivity(i);	
     				}
     			} );
     		}
     	} else {
 			listAdapter = new ArrayAdapter<String>(this, R.layout.artists, listOfChosenPerfsFromBase);
 			searchListView.setAdapter(listAdapter);
 			searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					Intent i = new Intent(FavoritesActivity.this, FavoritesTitleActivity.class);
 					Bundle bun = new Bundle();
 					bun.putString("performerName", listOfChosenPerfsFromBase.get(position));
 					i.putExtras(bun);
 					startActivity(i);	
 				}
 			} );
     	}
     }
     
     public ArrayList<String> addPerfFromBase() {
     	ArrayList<String> list = new ArrayList<String>();
     	db.open();
         Cursor c = db.getAllRecords();
         if (c.moveToFirst())
         {
             do {
             	if(!list.contains(c.getString(1)))
             		list.add(c.getString(1));
             } while (c.moveToNext());
         }
         db.close();
         return list;      
     } 
     
     private void hideKeyboard() {
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(editFavPerformer.getWindowToken(), 0);
 	}
     
     protected void onResume() {
 		super.onResume();
 		Button btn = (Button) findViewById(R.id.searchFavBtn);
 		btn.performClick();
 	}
     
     @Override
     public void onBackPressed() {
     	Intent intent = new Intent(this, MainActivity.class);
    	//finish();
     	startActivity(intent);
     }
 }
