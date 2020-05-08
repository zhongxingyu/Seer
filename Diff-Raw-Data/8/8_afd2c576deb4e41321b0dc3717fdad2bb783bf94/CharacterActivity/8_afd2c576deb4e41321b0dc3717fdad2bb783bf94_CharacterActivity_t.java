 package net.mindsoup.pathfindercharactersheet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mindsoup.pathfindercharactersheet.fragments.AttributesFragment;
 import net.mindsoup.pathfindercharactersheet.fragments.CharacterFragment;
 import net.mindsoup.pathfindercharactersheet.fragments.CharacterInfoFragment;
 import net.mindsoup.pathfindercharactersheet.fragments.OverviewFragment;
 import net.mindsoup.pathfindercharactersheet.fragments.SetAttributesFragment;
 import net.mindsoup.pathfindercharactersheet.fragments.SkillsFragment;
 import net.mindsoup.pathfindercharactersheet.pf.PfCharacter;
 import net.mindsoup.pathfindercharactersheet.util.DatabaseHelper;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.support.v4.widget.DrawerLayout;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class CharacterActivity extends SherlockFragmentActivity {
 	
 	private PfCharacter character;
 	
 	private ListView drawerList;
 	private DrawerLayout drawerLayout;
 	private CharacterPagerAdapter pagerAdapter;
 	private ViewPager pager;
 	private TypedArray icons;
 	List<SherlockFragment> fragments;
 	SetAttributesFragment createChar = null;
 	
 	private final String CREATE_CHAR_TAG = "fragment_create_char";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		System.out.println("****************************");
 		System.out.println("creating activity");
 		System.out.println("****************************");
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = getIntent();
 		character = (PfCharacter)intent.getParcelableExtra("CHAR");
 		
 		setContentView(R.layout.activity_character);
 	}
 	
 	private void initialisePaging() {
 		System.out.println("****************************");
 		System.out.println("creating paging");
 		System.out.println("****************************");
 		fragments = new ArrayList<SherlockFragment>();
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, OverviewFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, AttributesFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, SkillsFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, CharacterInfoFragment.class.getName()));
 		this.pagerAdapter = new CharacterPagerAdapter(getSupportFragmentManager(), fragments);
 		
 		pager = (ViewPager)findViewById(R.id.viewpager);
 		pager.setAdapter(pagerAdapter);
 		
 		OnPageChangeListener listener = new OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int position) {
 				getSupportActionBar().setSelectedNavigationItem(position);
 				
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {}
 		};
 		
 		pager.setOnPageChangeListener(listener);
 	}
 	
 	private void initialiseNavDrawer() {
 		// create navigation drawer
 		String[] drawerItems = getResources().getStringArray(R.array.drawer_items_strings);
 		icons = getResources().obtainTypedArray(R.array.drawer_items_icons);
 		
 		drawerList = (ListView)findViewById(R.id.left_drawer);
 		drawerList.setAdapter(new NavDrawerAdapter(this, R.layout.drawer_list_item, drawerItems, icons));		
 		drawerList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				toggleDrawer();
 				changeFragment(position);
 			}
 		});
 		
 		drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
 	}
 	
 	private void initialiseActionBar() {
 		// set up action bar
 		getSupportActionBar().setHomeButtonEnabled(true);
 		getSupportActionBar().setIcon(R.drawable.ic_drawer);
 		getSupportActionBar().setTitle(character.getName());
 		
 		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
 			
 			@Override
 			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
 			
 			@Override
 			public void onTabSelected(Tab tab, FragmentTransaction ft) {
 				pager.setCurrentItem(tab.getPosition());
 				
 			}
 			
 			@Override
 			public void onTabReselected(Tab tab, FragmentTransaction ft) {}
 		};
 		
 		String[] drawerItems = getResources().getStringArray(R.array.drawer_items_strings);
 		for(String s : drawerItems) {
 			Tab tab = getSupportActionBar().newTab();
 			tab.setText(s);
 			tab.setTabListener(tabListener);
 			getSupportActionBar().addTab(tab);
 		}
 		
 		// set up tabs
 		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		
 	}
 	
 	private void changeFragment(int fragment) {
 		pager.setCurrentItem(fragment);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		
 		// create navigation drawer
 		initialiseNavDrawer();
 		
 		// set up fragment paging
 		initialisePaging();
 		
 		// set up action bar
 		initialiseActionBar();
 		
 		// if we can see that our character's stats aren't set yet...
 		if(character.getConstitution().sum() < 1 || character.getCharisma().sum() < 1 || character.getIntelligence().sum() < 1) {
 			changeFragment(1);
 			
 			if(this.getSupportFragmentManager().findFragmentByTag(CREATE_CHAR_TAG) == null) {
 				FragmentManager fm = this.getSupportFragmentManager();
 				createChar = new SetAttributesFragment();
 		        createChar.show(fm, CREATE_CHAR_TAG);
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.character, menu);
 		   
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	        case android.R.id.home:
 	        	toggleDrawer();
 	            return true;
 	        case R.id.home_button:
 	        	homeButtonClicked();
 	        	return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private void homeButtonClicked() {
 		super.onBackPressed();
 	}
 	
 	private void toggleDrawer() {
 		// if our home icon is pressed
     	// toggle the navigation drawer
     	if(drawerLayout.isDrawerOpen(drawerList)) {
     		drawerLayout.closeDrawer(drawerList);
     	}
     	else {
     		drawerLayout.openDrawer(drawerList);
     	}
 	}
 	
 	public PfCharacter getCharacter() {
 		return character;
 	}
 	
 	@Override
     public void onBackPressed() {
         if (pager.getCurrentItem() == 0) {
             // If the user is currently looking at the first step, allow the system to handle the
             // Back button. This calls finish() on this activity and pops the back stack.
             super.onBackPressed();
         } else {
             // Otherwise, select the previous step.
         	pager.setCurrentItem(pager.getCurrentItem() - 1);
         }
     }
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		icons.recycle();
 	}
 	
 	public void updateCharacter() {
 		this.updateCharacter(true);
 	}
 	
 	public void updateCharacter(boolean triggerDbUpdate) {
 		// TODO: change code structure to allow for an OnChange listener on character?
 		if(triggerDbUpdate) {
 			DatabaseHelper db = new DatabaseHelper(this);
 			db.updateCharacterAttributes(character);
 		}
 		
 		for(SherlockFragment f : pagerAdapter.getItems()) 
 			((CharacterFragment)f).refresh();
 	}
 }
