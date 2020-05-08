 package com.ISU.shoppingsidekick;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.Database.API.Account;
 import com.Database.API.CalendarItem;
 import com.Database.API.DatabaseAPI;
 import com.Database.API.Food;
 import com.Database.API.Recipe;
 
 public class RecipesActivity extends Activity {
 
 	ListView resultsListView;
 	ArrayList<String> listItems;
 	ArrayAdapter<String> adapter;
 	ArrayList<Recipe> recipeList;
 	List<CalendarItem> calendarItemList;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_recipes);
 		listItems = new ArrayList<String>();
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
 		recipeList = new ArrayList<Recipe>();
 		resultsListView = (ListView) findViewById(R.id.listView1);
 		resultsListView.setAdapter(adapter);
 		resultsListView.setOnItemClickListener(new OnItemClickListener(){
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Recipe r = recipeList.get(position);
 				String url = r.getLink();
 				if (!url.startsWith("http://") && !url.startsWith("https://"))
 					   url = "http://" + url;
 				final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(browserIntent);
 			}
 		});
 		setRecipeList();
 	}
 
 	public void setRecipeList(){
 		final Account a = (Account) getIntent().getExtras().get("account");
 		ExecutorService pool = Executors.newFixedThreadPool(3);
         Callable task = new Callable(){
 			@Override
 			public Object call() throws Exception {
 				DatabaseAPI d = new DatabaseAPI();
 				return d.getUsersItems(a.getUserID());
 			}
         };
         Future<List<CalendarItem>> future = pool.submit(task);
         try {
 			calendarItemList = future.get();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
         
         if(calendarItemList.size() == 0){
        	listItems.add("No search results found");
 	        adapter.notifyDataSetChanged();
         }
         else if(calendarItemList.size() > 0){
         	for(int i = 0; i < calendarItemList.size(); i++){
         		final Food f = calendarItemList.get(i).getFood();
         		ExecutorService pool2 = Executors.newFixedThreadPool(3);
         		task = new Callable(){
         			@Override
         			public Object call() throws Exception {
         				DatabaseAPI d = new DatabaseAPI();
         				return d.getRecipesByFoodID(f.getID()); //returns null right now
         			}
                 };
                 Future<ArrayList<Recipe>> future2 = pool2.submit(task);
                 try {
        			recipeList.addAll(future2.get());
         		} catch (InterruptedException e) {
         			e.printStackTrace();
         		} catch (ExecutionException e) {
         			e.printStackTrace();
         		}
                 
                 for(int j = 0; i < recipeList.size(); i++){
                	listItems.add(recipeList.get(i).getName());
                 }
 		        adapter.notifyDataSetChanged();
 
         	}
         	
         }
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.recipes, menu);
 		return true;
 	}
 
 }
