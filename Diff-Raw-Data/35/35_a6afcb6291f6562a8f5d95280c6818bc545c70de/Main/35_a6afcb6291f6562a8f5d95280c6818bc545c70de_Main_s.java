 package edu.vanderbilt.vuphone.android.dining;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Random;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import edu.vanderbilt.vuphone.android.map.AllLocations;
 import edu.vanderbilt.vuphone.android.objects.Range;
 import edu.vanderbilt.vuphone.android.objects.RestaurantAdapter;
 import edu.vanderbilt.vuphone.android.objects.RestaurantHours;
 import edu.vanderbilt.vuphone.android.objects.Time;
 import edu.vanderbilt.vuphone.android.storage.Restaurant;
 
 public class Main extends ListActivity {
 	
 	
 	public static Context applicationContext;
 	/**The first case in the menu*/
 	private static final int MENU_ITEM_VIEW_MAP = 0;
 	/** The second case in the menu */
 	private static final int MENU_ITEM_MARK_FAVS = 1;
 	/** The case in menu to change sorting method */
 	private static final int MENU_ITEM_CHOOSE_SORTING = 2;
 	/** The custom adapter used to create the list */
 	private RestaurantAdapter ra;
 	/**
 	 * This value saves the users sort preference while using the program and
 	 * and defaults to showing favorites on top followed by open and then closed
 	 * locations
 	 */
 	private int currentSortMethod = RestaurantAdapter.FAVORITE_OPEN_CLOSED;
 	
 	private static final int NORMAL = 0;
 	private static final int MARK_FAVS = 1;
 	private int mode;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// clunky mechanic with gross file dependency,
 		// but DBWrapper needs Main.mainContext for its
 		// calls to DBAdapter
 		if (applicationContext == null)
			applicationContext = getApplicationContext();
 
		deleteAllRestaurants();
		addRandomRestaurantsToDB(20);		
 		
 		setContentView(R.layout.main);
 		mode = NORMAL;
 		
		Log.i("Main", "creating ra");
 		ra = new RestaurantAdapter(this, currentSortMethod);
		Log.i("Main", "done creating ra, now setting it");
 		setListAdapter(ra);
 		getListView().setTextFilterEnabled(true);
 	}
 
 	/** Resorts the list when it is resumed */
 	public void onResume() {
 		super.onResume();
 		ra.setSort(currentSortMethod);
 		setListAdapter(ra);
 	}
 
 	/** Sets a new sort method to be used and resorts the list */
 	protected void reSortList(int sortMethod) {
 		currentSortMethod = sortMethod;
 		ra.setSort(currentSortMethod);
 		setListAdapter(ra);
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		if (id<0)   // if user clicked on a partition
 			return;
 		Log.i("dining", "position " + position + " was clicked");
 		switch (mode) {
 		case NORMAL:
 			// starts restaurant details page and sends index of restaurant
 			Intent toDetails = new Intent(this, RestaurantDetails.class);
 			toDetails.putExtra(RestaurantDetails.RESTAURANT_ID, id);
 			
 			startActivity(toDetails);
 			break;
 		case MARK_FAVS:
 			Restaurant.setFavorite(id, !Restaurant.favorite(id));
 			getListView().recomputeViewAttributes(v);
 			setListAdapter(ra);
 		}
 	}
 
 	/**This opens the dialog that allows the user to choose a new sorting option*/
 	protected Dialog onCreateDialog(int id) {
 		Dialog dialog;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final CharSequence[] items = { "open/closed", "near/far",
 				"favorites/open/closed", "favorite", "alphabetical" };
 		final ArrayList<Integer> itemsIDs = new ArrayList<Integer>();
 		itemsIDs.add(RestaurantAdapter.OPEN_CLOSED);
 		itemsIDs.add(RestaurantAdapter.NEAR_FAR);
 		itemsIDs.add(RestaurantAdapter.FAVORITE_OPEN_CLOSED);
 		itemsIDs.add(RestaurantAdapter.FAVORITE);
 		itemsIDs.add(RestaurantAdapter.ALPHABETICAL);
 
 		builder.setSingleChoiceItems(items,
 				itemsIDs.indexOf(currentSortMethod),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int item) {
 
 						reSortList(itemsIDs.get(item));
 
 					}
 				});
 
 		builder.setNeutralButton("Done", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				dialog.cancel();
 			}
 		});
 		builder.setTitle("Choose sort method");
 		dialog = builder.create();
 
 		return dialog;
 	}
 
 	/** Creates list of actions for user when the menu button is clicked */
 	public boolean onCreateOptionsMenu(Menu menu) { 
 		menu.add(Menu.NONE, MENU_ITEM_VIEW_MAP, Menu.NONE, "View\nMap");
 		menu.add(Menu.NONE, MENU_ITEM_MARK_FAVS, Menu.NONE, "Mark\nFavorites");
 		menu.add(Menu.NONE, MENU_ITEM_CHOOSE_SORTING, Menu.NONE, "Sort");
 		return true;
 	}
 
 	/** Handles what happens when each menu item is clicked */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case MENU_ITEM_VIEW_MAP:
 			Intent toMapView = new Intent(this, AllLocations.class);
 			startActivity(toMapView);
 			return true;
 		case MENU_ITEM_MARK_FAVS:
 			setContentView(R.layout.mark_favs);
 			mode = MARK_FAVS;
 			ra.setShowFavIcon(true);
 			setListAdapter(ra);
 			
 			((Button) findViewById(R.mark_favs.done))
 					.setOnClickListener(doneListener);
 			((Button) findViewById(R.mark_favs.cancel))
 					.setOnClickListener(cancelListener);
 			
 //			Intent toMarkFavs = new Intent(this, MarkFavs.class);
 //			long[] order = new long[ra.getSortOrder().size()];
 //			for (int x = 0; x < ra.getSortOrder().size(); ++x) {
 //				order[x] = ra.getSortOrder().get(x);
 //			}
 //			toMarkFavs.putExtra(MarkFavs.ADAPTER, order);
 //			startActivity(toMarkFavs);
 			return true;
 		case MENU_ITEM_CHOOSE_SORTING:
 			showDialog(1);
 			return true;
 		default:
 			return false;
 		}
 	}	
 	
 	private OnClickListener doneListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			Restaurant.commit();
 			mode = NORMAL;
 			setContentView(R.layout.main);
 			ra.forceSetSort(currentSortMethod);
 			setListAdapter(ra);
 		}
 	};
 
 	/** ends activity when cancel button is clicked */
 	private OnClickListener cancelListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			Restaurant.revert();
 			mode = NORMAL;
 			setContentView(R.layout.main);
 			ra.setSort(currentSortMethod);
 			setListAdapter(ra);
 		}
 	};
 
 	
 	// placeholder/temporary methods below
 	
 	// MAY CONFUSE DBWrapper IF USED AFTER ANY OTHER DB FUNCTIONS
 	private void deleteAllRestaurants() {/*
 		Log.i("test", "opening writable database.");
 		DBAdapter adapt = new DBAdapter(this);
 		adapt.openWritable();
 		ArrayList<Long> ids = Restaurant.getIDs();
 		Log.i("test", "deleting database contents");
 		for (int i = 0; i<ids.size(); i++)
 			adapt.deleteRestaurant(ids.get(i));
 		adapt.close();*/
 		ArrayList<Long> ids = Restaurant.getIDs();
 		for (int i = 0; i < ids.size(); i++)
 			Restaurant.delete(ids.get(i));
 	}
 
 	private void addRandomRestaurantsToDB(int numRest) {
 		String []letters = {"a", "b", "c", "d", "e", "f", "g", "h", 
 				"i","k", "l", "m", "n","o","p","q","r",
 				"s","t","u","v","w","x","y","z"};
 		int maxRanges = 2;
 		Log.i("test", "loading database with valid random data");
 		Random r = new Random();
 		for (int i = 1; i <= numRest; i++) {
 			RestaurantHours rh = new RestaurantHours();
 			for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
 				int ranges = r.nextInt(maxRanges) + 1;
 				for (int k = 0; k < ranges; k++) {
 					Time start = new Time(r.nextInt(12 / ranges) + k * 24/ranges, r
 							.nextInt(59));
 					Time stop = new Time(
 							(r.nextInt(12) + 12) / ranges + k * 24/ranges, r
 									.nextInt(59));
 					rh.addRange(day, new Range(start, stop));
 				}
 			}
 			String name = new String();
 			for (int j = 0; j<7; j++)
 				name = name + letters[r.nextInt(letters.length)];
 			Restaurant restaurant = new Restaurant(name + " " + i, rh, r.nextBoolean() && r.nextBoolean(), r.nextInt(), r
 					.nextInt(), "cafe", null, 
 					"Known for its fine cuisine, this is the restaurant Restaurant " + name + " " + i, R.drawable.dining, true,
 					 false, "(615) 555-1234", "http://example.com");
 			restaurant.create();
 		}
 	}
 
 }
