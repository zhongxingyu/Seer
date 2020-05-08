 package edu.android.podcast_listener;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import edu.android.podcast_listener.adapters.CategoryAdapter;
 import edu.android.podcast_listener.db.Category;
 import edu.android.podcast_listener.db.CategoryDAO;
 
 public class CategoryManagementActivity extends Activity {
 	CategoryDAO categoryDb;
 	ListView listView;
 	EditText categoryText;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_category_management);
 		categoryDb = new CategoryDAO(this);
 		setCategoryListAdapter();
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
 				Category cat = (Category) av.getItemAtPosition(pos);
 				categoryDb.open();
 				categoryDb.deleteCategory(cat.getId());
 				categoryDb.close();
 				setCategoryListAdapter();
				return true;
 			}
 			
 		});
 	}
 	
 	private void setCategoryListAdapter() {
 		if (listView == null) {
 			listView = (ListView) findViewById(R.id.current_categories);
 		}
 		categoryDb.open();
 		List<Category> cats = categoryDb.getAllCategories();
 		listView.setAdapter(new CategoryAdapter(getApplicationContext(), R.layout.category_list_item, cats));
 		categoryDb.close();
 	}
 	
 	public void addCategory(View view) {
 		categoryText = (EditText) findViewById(R.id.add_new_cat_field);
		if (categoryText != null && !categoryText.getText().toString().equals("")) {
 			categoryDb.open();
 			categoryDb.createCategory(categoryText.getText().toString());
 			categoryDb.close();
 			setCategoryListAdapter();
 			categoryText.setText("");
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_category_management, menu);
 		return true;
 	}
 
 }
