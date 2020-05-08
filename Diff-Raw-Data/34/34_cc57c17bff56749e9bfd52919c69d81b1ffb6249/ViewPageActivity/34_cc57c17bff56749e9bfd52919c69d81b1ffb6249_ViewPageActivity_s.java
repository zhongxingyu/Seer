 package c301.AdventureBook;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Option;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 public class ViewPageActivity extends Activity {
 
 	//private Story currentStory;
 	//private List<Page> pages;
 	private List<Option> options;
 	ListView optionsListView;
 	
 	public StoryManager sManager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.viewpage);
 		
 		sManager = StoryManager.getInstance();
 		sManager.initContext(this);
 		populatePage();
 		
 		// When option is clicked, go to the next page.
 		optionsListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int position,
 					long arg3) {
 				Option chosenOption = (Option) optionsListView.getItemAtPosition(position);
 				sManager.setCurrentOption(chosenOption);
 				Page goToPage = chosenOption.getGoToPage();
 				sManager.setCurrentPage(goToPage);
 				Intent intent = new Intent(getApplicationContext(), ViewPageActivity.class);
 				startActivity(intent);
 				
 				/*
 				 * Allow the user to go back? Implementing finish() will
 				 * inhibit the user from going back to the previous page
 				 */
 				finish();
 			}
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_story, menu);
 		return true;
 	}
 	
 	
 	/**
 	 * Loads the page data including the title, description, and options that
 	 * are available on that page.
 	 */
 	private void populatePage() {
 		Page page = sManager.getPage();
 		options = page.getOptions();
 		
 		// Set the page title
 		String pageTitle = page.getTitle();
 		TextView pageTitleTV = (TextView) findViewById(R.id.pageTitleTV);
 		pageTitleTV.setText(pageTitle);
 		
 		// Set the page description
 		String pageDescription = page.getPageDescription();
 		TextView pageDescriptionTV = (TextView) findViewById(R.id.pageDescriptionTV);
 		pageDescriptionTV.setText(pageDescription);
 		
 		
 		// Set the page options
 		optionsListView = (ListView) findViewById(R.id.list_options);
 		ArrayAdapter<Option> optionAdapter = new ArrayAdapter<Option>(this, R.layout.list_row, options);
 		optionsListView.setAdapter(optionAdapter);
 		
 	}
 	
 	public void launchAnnotationsActivity(View v){
 		Intent i  = new Intent(this, AnnotationActivity.class);
 		startActivity(i);
 	}
 	
 
 }
