 package edu.mines.broomthompsondotadictionary;
 
 import java.net.URL;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author Alexander Broom
  * @author Austin Thompson
  * 
  * Class: HeroFragment 
  * 
  * Description: The fragment containing detailed information
  * about each hero: Will eventually contain a picture and other miscellaneous
  * information.
  * 
  */
 public class HeroFragment extends Fragment {
 	// Indicator of the name of the value passed in a savedInstance
 	final static String ARG_ID = "id";
 	// The name of the hero being displayed
 	String mCurrentName = "";
 	
 	/** 
 	 * onCreate: Creates the fragment
 	 * 
 	 *  @param savedInstanceState: the Bundle that can be used to recreate the fragment
 	 *  instead of recreate.
 	 */ 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	/** 
 	 * onCreateView: Creates the view of the fragment 
 	 */ 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if (savedInstanceState != null) {
 			mCurrentName = savedInstanceState.getString(ARG_ID);
 		}
 		
 		return inflater.inflate(R.layout.hero_view, container, false);
 	}
 
 	/** 
 	 * onStart: Handles start up of the app 
 	 */ 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Bundle args = getArguments();
 		
 		// Create fragment if there are args, otherwise use previous instance of fragment
 		if (args != null) {
 			updateHeroView(args.getString(ARG_ID));
 		} else if (mCurrentName != null) {
 			updateHeroView(mCurrentName);
 		}
 	}
 
 	/** 
 	 * updateHeroView:  Sets data in fragment for each hero
 	 */ 
 	public void updateHeroView(String name) {
 		// Get hero
 		final Hero hero = MainActivity.source.getHeroByName(name);
 		// Variable for image to be loaded
 		Bitmap img;
 	
 		// Executor Service to create new thread to download image from url
 		ExecutorService es = Executors.newSingleThreadExecutor();
 		Future<Bitmap> result = es.submit(new Callable<Bitmap>() {
 			public Bitmap call() throws Exception {
 				URL url = new URL(hero.getPicture());
 				Bitmap image = BitmapFactory.decodeStream(url.openConnection()
 						.getInputStream());
 				return image;
 			}
 		});
 
 		// Try to download image
 		try {
 			img = result.get();
 			ImageView portrait = (ImageView) getActivity().findViewById(R.id.portrait);
 			portrait.setImageBitmap(img);
 			
 			// Get the width of the screen
 			WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
 			Display display = wm.getDefaultDisplay();
 			Point size = new Point();
 			display.getSize(size);
 			int intendedWidth = size.x;
 			
 			// Get the downloaded image dimensions
 			int originalWidth = img.getWidth();
 			int originalHeight = img.getHeight();
 
 			// Calculate the new dimensions
 			float scale = (float) intendedWidth / originalWidth;
 			int newHeight = (int) Math.round(originalHeight * scale);
 			
 			// Resize the portrait
 			portrait.getLayoutParams().width = intendedWidth;
 			portrait.getLayoutParams().height = newHeight;			
 		} catch (Exception e) {
 			// If exception, make log statement and print stack
 			Log.e(getString(R.string.app_name), "Failed to load portrait");
 			e.printStackTrace();
 			// Continue to create rest of fragment, whether image loads or not
 		}
 
 		// Set the name text view
 		TextView name_view = (TextView) getActivity().findViewById(R.id.name);
 		if(hero.getName().length() > 13) {
 			name_view.setTextSize(24);
 		}
 		name_view.setText(hero.getName());
 		
 		// Determine which icon to display depending on hero's focus
 		ImageView focus_view = (ImageView) getActivity().findViewById(R.id.focus);
 		String focus = hero.getFocus();
 		if(focus.equals("Intelligence")) {
 			focus_view.setImageResource(R.drawable.icon_intelligence);
 		} else if (focus.equals("Agility")) {
 			focus_view.setImageResource(R.drawable.icon_agility);
 		} else {
 			focus_view.setImageResource(R.drawable.icon_strength);
 		}
 		
 		// Determine which icon to display depending on hero's attack type
 		ImageView attack_view = (ImageView) getActivity().findViewById(R.id.attack);
 		if(hero.getAttack().equals("Melee")) {
 			attack_view.setImageResource(R.drawable.icon_melee);
 		} else {
 			attack_view.setImageResource(R.drawable.icon_ranged);
 		}
 		
 		// Display hero's ease of use and role
 		TextView use_view = (TextView) getActivity().findViewById(R.id.role_use);
 		use_view.setText(hero.getUse() + " " + hero.getRole());
 		mCurrentName = name;
 	}
 
 	
 	/** 
 	 * onCreateOptionsMenu: creation of options menu, used because refresh needs to be
 	 * removed when the hero fragment is brought up. 
 	 */ 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		// Get rid of all other items in the menu
 		menu.clear();
 		inflater.inflate(R.menu.names_menu, menu);
 	}
 	
 	/** 
 	 * onOptionsItemSelected: Handles what to do when menu item selected
 	 */ 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/** 
 	 * onSaveInstanceState: saves the current article selection in case we need to recreate the fragment
 	 */ 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(ARG_ID, mCurrentName);
 	}
 }
