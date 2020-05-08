 package ch.unibe.ese.shopnote.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.adapters.ItemListAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.core.Utility;
 import ch.unibe.ese.shopnote.share.SyncManager;
 import ch.unibe.ese.shopnote.share.requests.listchange.ItemRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.RecipeDescriptionRequest;
 
 /**
  *	Displays a single recipe including the items
  */
 public class ViewRecipeActivity extends BaseActivity {
 	private ListManager listManager;
 	private SyncManager syncManager;
 	private Recipe recipe;
 	private List<Item> itemsOfRecipe;
 	private ArrayAdapter<Item> itemAdapter;
 	ArrayAdapter<Item> autocompleteAdapter;
 	private long recipeIndex;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_recipe);
         
 		//set chosen color theme
 		RelativeLayout lo = (RelativeLayout) findViewById(R.id.RelativeLayoutViewRecipe);
 		RelativeLayout rlDrawer = (RelativeLayout) findViewById(R.id.drawer_Linearlayout);
 		updateTheme(lo, getActionBar(), rlDrawer);
 		LinearLayout lladdItem = (LinearLayout) findViewById(R.id.addItem);
 		updateThemeTextBox(lladdItem);
 		
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		// Create drawer menu
 		createDrawerMenu();
 		createDrawerToggle(); //to change the title
 		drawerToggle.setDrawerIndicatorEnabled(false);
 		
 		// get managers
 		syncManager = getSyncManager();
 		listManager = getListManager();
 		listManager.updateRecipe();
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			recipeIndex = extras.getLong(EXTRAS_RECIPE_ID);
 			recipe = listManager.getRecipeAt(recipeIndex);
 			itemsOfRecipe = recipe.getItemList();
 			setTitle(this.getString(R.string.view_recipe_title) + " " + recipe.toString());
 		} 
 		else
 			itemsOfRecipe = new ArrayList<Item>();
 		
 		//create or update recipe list
 		updateRecipeList();
 		
 		//add autocomplete items
 		createAutocomplete();
 		
 		// Done button
 		EditText addItem = (EditText) findViewById(R.id.editTextName);
 		addItem.setOnEditorActionListener(new OnEditorActionListener() {
 		    @Override
 		    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 		        if (actionId == EditorInfo.IME_ACTION_DONE) {
 		        	addItem(view);
 		            return true;
 		        }
 		        return false;
 		    }
 		});
 		
 		// load notes
 		setTextViewText(R.id.editTextNotes, recipe.getNotes());
 	}
 
 	private void updateRecipeList() {
 		// Get listOfRecipes and put it in the listview	
 		recipe = listManager.getRecipeAt(recipeIndex);
 		itemsOfRecipe = recipe.getItemList();
 		itemAdapter = new ItemListAdapter(this,
 				R.layout.shopping_list_item, itemsOfRecipe);
 		ListView listView = (ListView) findViewById(R.id.ItemView);
 		listView.setAdapter(itemAdapter);
 		updateThemeListView(listView);
 		
 		toggleDescription();
 		
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int position, long arg3) {
 				Item selectedItem = itemAdapter.getItem(position);
 				ViewRecipeActivity.this.startActionMode(new RecipeListActionMode(
 						ViewRecipeActivity.this.listManager, recipe, selectedItem,
 						ViewRecipeActivity.this.itemAdapter,
 						ViewRecipeActivity.this));
 				return true;
 			}
 		});
 		
 		// Bugfix, allows to put a ListView in a ScrollView with other objects
 		Utility.setListViewHeightBasedOnChildren(listView);
 	}
 	
 	private AutoCompleteTextView createAutocomplete() {
 		AutoCompleteTextView addItems = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		autocompleteAdapter = new ArrayAdapter<Item>(this,
 				android.R.layout.simple_list_item_1, listManager.getAllItems());
 		addItems.setAdapter(autocompleteAdapter);
 		updateThemeTextBox(addItems);
 		
 		addItems.setOnItemClickListener(new OnItemClickListener() {
 
 	        @Override
 	        public void onItemClick(AdapterView<?> parent, View arg1, int position,
 	                long id) {
 	        	Item item = (Item) autocompleteAdapter.getItem(position);
 	        	if(!itemsOfRecipe.contains(item))
 	        		itemsOfRecipe.add(item);
 	        	updateRecipeList();
 	        	
 	        	EditText addItem = (EditText) findViewById(R.id.editTextName);
 	        	addItem.setText("");
 	        }
 	    });
 		
 		return addItems;
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 	
 	/** Called when the user touches the add item button */
 	public void addItemEdit(View view) {
 		Intent intent = new Intent(this, CreateItemActivity.class);
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		String name = textName.getText().toString();
 		intent.putExtra(EXTRAS_ITEM_NAME, name);
 		intent.putExtra(EXTRAS_RECIPE_ID, recipe.getId());
 		this.startActivityForResult(intent, 1);
 		
 		// remove text from field
 		textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		textName.setText("");
 	}
 
 	/** Called when the user touches the ok button */
 	public void addItem(View view) {
 		EditText textName = (EditText) findViewById(R.id.editTextName);
 		String name = textName.getText().toString();
 		if (name.trim().isEmpty()) {
 			Toast.makeText(this, this.getString(R.string.error_name), Toast.LENGTH_SHORT).show();
 			return;				
 		} 
 		else {
 			Item item = new Item(name);
 			listManager.save(item);
 			recipe.addItem(item);
 			addItemRequestIfShared(item);
 			updateRecipeList();
 		}
 
 		// remove text from field
 		textName = (AutoCompleteTextView) findViewById(R.id.editTextName);
 		textName.setText("");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_recipe, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	long recipeIndex = recipe.getId();
     	
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			finish();
 			return true;
 			
 		// Handle presses on the action bar items
 		case R.id.action_notes:
 			// toggle notes visibility state
 			recipe.setNotesVisible(!recipe.isNotesVisible());
 			listManager.saveRecipe(recipe);
 			toggleDescription();
 			return true;
 		case R.id.action_share:
 			Intent intentShare = new Intent(this, ShareActivity.class);
 			intentShare.putExtra(EXTRAS_IS_RECIPE, true);
 			intentShare.putExtra(EXTRAS_RECIPE_ID, recipeIndex);
 			this.startActivity(intentShare);
 			return true;
 			
 		// Handle presses on overflow menu items
 		case R.id.action_edit_recipe:
         	Intent intentEdit = new Intent(this, CreateRecipeActivity.class);
         	intentEdit.putExtra(EXTRAS_RECIPE_ID, recipeIndex);
             this.startActivity(intentEdit);
 			return true;
 		case R.id.action_delete:
 			listManager.removeRecipe(recipe);
 			finish();
 			return true;
 		case R.id.action_refresh:
 			syncManager.synchronise(this);
 			Toast.makeText(this, this.getString(R.string.synchronizing),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 *	Hide the description text if no notes/items
 	 */
 	private void toggleDescription() {
 		TextView textItems = (TextView) findViewById(R.id.textItems);
 		TextView textNotes = (TextView) findViewById(R.id.textNotes);
 		EditText editTextNotes = (EditText) findViewById(R.id.editTextNotes);
 		
 		// toggle ingredients description text
 		if (!itemAdapter.isEmpty())
 			textItems.setVisibility(View.VISIBLE);
 		else
 			textItems.setVisibility(View.GONE);
 		
 		// toggle notes description and field
 		if (recipe.isNotesVisible()) {
 			textNotes.setVisibility(View.VISIBLE);
 			editTextNotes.setVisibility(View.VISIBLE);
 		}
 		else {
 			textNotes.setVisibility(View.GONE);
 			editTextNotes.setVisibility(View.GONE);
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		// save notes
 		boolean hasChanged = !getTextViewText(R.id.editTextNotes).equals(recipe.getNotes());
 		recipe.setNotes(getTextViewText(R.id.editTextNotes));
 		if(recipe.isShared() && hasChanged) {
 			syncManager.addRequest(new RecipeDescriptionRequest(getMyPhoneNumber(), recipe.getId(), recipe.getNotes()));
 			syncManager.synchronise(this);
 		}
 		listManager.saveRecipe(recipe);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		// load notes
 		setTextViewText(R.id.editTextNotes, recipe.getNotes());
 		updateRecipeList();
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		listManager.updateRecipe();
 		this.recipe = listManager.getRecipeAt(recipe.getId());
 		this.updateRecipeList();
 	}
 	
 	private void addItemRequestIfShared(Item item) {
 		if (recipe.isShared()){
 			ItemRequest irequest = new ItemRequest(getMyPhoneNumber(), recipe.getId(), item.copy());
 			irequest.isRecipe(true);
 			syncManager.addRequest(irequest);
 			syncManager.synchronise(this);
 		}
 	}
 	
 	public void refresh() {
 		setTextViewText(R.id.editTextNotes, recipe.getNotes());
 		updateRecipeList();
 	}
 }
