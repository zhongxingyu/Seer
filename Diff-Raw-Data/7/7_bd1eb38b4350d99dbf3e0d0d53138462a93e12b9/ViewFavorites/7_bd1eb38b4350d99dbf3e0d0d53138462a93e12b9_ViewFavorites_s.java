 package edu.rit.se.agile.randominsultapp;
 
 import edu.rit.se.agile.data.WordsTemplate;
 import android.os.Bundle;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class ViewFavorites extends GenericActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_favorites);
 		
 		ListView list =  (ListView) findViewById(R.id.favorites_list);
 		list.setAdapter(
 				new SimpleCursorAdapter(this, 
 						R.id.favorites_list, 
 						favoritesDAO.getAllFavoritesCursor(), //FIX THIS 
						new String[]{ WordsTemplate.COLUMN_CATEGORY }, 
 						new int[]{ R.id.category_list_entry }, 
 						SimpleCursorAdapter.FLAG_AUTO_REQUERY ));
 		}
 
 }
