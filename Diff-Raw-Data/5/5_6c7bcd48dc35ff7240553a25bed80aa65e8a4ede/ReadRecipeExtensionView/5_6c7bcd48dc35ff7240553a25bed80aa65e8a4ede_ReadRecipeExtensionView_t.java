 package com.cmput301w13t09.cmput301project.activities;
 
 import java.io.File;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.content.ContentResolver;
import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.cmput301w13t09.cmput301project.R;
 import com.cmput301w13t09.cmput301project.RecipeController;
 import com.cmput301w13t09.cmput301project.RecipeViewAssistant;
 
 /**
  * @author Kyle, Marcus, and Landre Class:
  */
 public class ReadRecipeExtensionView extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	SectionsPagerAdapter mSectionsPagerAdapter;
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 	private RecipeController rController;
 	private RecipeViewAssistant rAssitant;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.recipe_view);
 		rAssitant = new RecipeViewAssistant(this);
 		rController = new RecipeController(this);
 		
 		Uri data = getIntent().getData();
 		rAssitant.loadFromFile(data);
 		rAssitant.updateRecipe();
 		rAssitant.saveToFile();
 		
 		
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						actionBar.setSelectedNavigationItem(position);
 					}
 				});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 	}
 
 	public static File getContentName(ContentResolver resolver, Uri uri) {
 		Cursor cursor = resolver.query(uri,
 				new String[] { MediaStore.MediaColumns.DISPLAY_NAME }, null,
 				null, null);
 		cursor.moveToFirst();
 		int nameIndex = cursor.getColumnIndex(cursor.getColumnNames()[0]);
 		if (nameIndex >= 0) {
 			return new File(cursor.getString(nameIndex));
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.read_recipe_extension_view_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.ReadRecipeExtensionViewDownload:
 			rController.addRecipe(rAssitant.getRecipe());
 			rController.saveToFile();
 			finish();
 			return super.onOptionsItemSelected(item);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	protected void onResume() {
 		super.onResume();
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			switch (position) {
 			case 0:
 				Fragment descriptionFragment = new RecipeViewDescriptionSectionFragment();
 				return descriptionFragment;
 			case 1:
 				Fragment ingredientFragment = new RecipeViewIngredientSectionFragment();
 				return ingredientFragment;
 			case 2:
 				Fragment instructionFragment = new RecipeViewInstructionSectionFragment();
 				return instructionFragment;
 			}
 			return new Fragment();
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return getString(R.string.createNewRecipe_title_section1);
 			case 1:
 				return getString(R.string.createNewRecipe_title_section2);
 			case 2:
 				return getString(R.string.createNewRecipe_title_section3);
 			}
 			return null;
 		}
 	}
 }
