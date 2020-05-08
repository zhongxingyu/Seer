 package net.mindsoup.charactersoup;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mindsoup.charactersoup.adapters.CharacterPagerAdapter;
 import net.mindsoup.charactersoup.adapters.NavDrawerAdapter;
 import net.mindsoup.charactersoup.fragments.AttributesFragment;
 import net.mindsoup.charactersoup.fragments.CharacterFragment;
 import net.mindsoup.charactersoup.fragments.CharacterInfoFragment;
 import net.mindsoup.charactersoup.fragments.FeatsFragment;
 import net.mindsoup.charactersoup.fragments.InventoryFragment;
 import net.mindsoup.charactersoup.fragments.LevelUpFragment;
 import net.mindsoup.charactersoup.fragments.OverviewFragment;
 import net.mindsoup.charactersoup.fragments.SetAttributesFragment;
 import net.mindsoup.charactersoup.fragments.SkillsFragment;
 import net.mindsoup.charactersoup.pf.PfCharacter;
 import net.mindsoup.charactersoup.pf.feats.PfFeats;
 import net.mindsoup.charactersoup.pf.items.Item;
 import net.mindsoup.charactersoup.util.DatabaseHelper;
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
 	
 	private final String CREATE_CHAR_TAG = "fragment_create_char";
 	private final String LEVELUP_TAG = "fragment_levelup";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = getIntent();
 		character = (PfCharacter)intent.getParcelableExtra("CHAR");
 		
 		setContentView(R.layout.activity_character);
 			
 		// set up fragment paging
 		initialisePaging();
 			
 		// set up action bar
 		initialiseActionBar();
 		
 		if(savedInstanceState != null) {
 			pager.setCurrentItem(savedInstanceState.getInt("currentItem"));
 		}
 		
 		/*System.out.println("++++++++++++++++++++++++++++++");
 		System.out.println("MADE ALL FRAGMENTS IN " + this.toString());
 		System.out.println("pagerAdapter " + pagerAdapter.toString());
 		System.out.println("viewPager " + pager.toString() + " childcount " + pager.getChildCount());
 		System.out.println("items in fragments " + fragments.toString());
 		System.out.println("pagerAdapter items " + pagerAdapter.getItems().toString());
 		System.out.println("++++++++++++++++++++++++++++++");
 		*/
 	}
 	
 	private void initialisePaging() {
 		fragments = new ArrayList<SherlockFragment>();
 		
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, OverviewFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, AttributesFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, SkillsFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, FeatsFragment.class.getName()));
 		//fragments.add((SherlockFragment)SherlockFragment.instantiate(this, EquipmentFragment.class.getName()));
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, InventoryFragment.class.getName()));	
 		fragments.add((SherlockFragment)SherlockFragment.instantiate(this, CharacterInfoFragment.class.getName()));
 		for(String name : character.getPfClass().getFragments().values())
 			fragments.add((SherlockFragment)SherlockFragment.instantiate(this, name));
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
 	
 	private String[] getFragmentNames() {
 		// create navigation drawer
 		String[] drawerItems = getResources().getStringArray(R.array.drawer_items_strings);
 		
 		// add character class specific fragment names
 		String[] classFragments = character.getPfClass().getFragments().keySet().toArray(new String[0]);
 		String[] fragmentNames = new String[drawerItems.length + classFragments.length];
 		
 	
 		for(int i = 0; i < drawerItems.length; i++)
 			fragmentNames[i] = drawerItems[i];
 		
 		for(int i = 0; i < classFragments.length; i++)
 			fragmentNames[drawerItems.length + i] = classFragments[i];
 			
 		return fragmentNames;
 	}
 	
 	private void initialiseNavDrawer() {
 		// create navigation drawer
 		String[] drawerItems = getFragmentNames();
 	
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
 		
 		String[] drawerItems = getFragmentNames();
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
 		
 		// if we can see that our character's stats aren't set yet...
 		if(character.getConstitution().sum() < 1 || character.getCharisma().sum() < 1 || character.getIntelligence().sum() < 1) {
 			changeFragment(1);
 			
 			if(this.getSupportFragmentManager().findFragmentByTag(CREATE_CHAR_TAG) == null) {
 				FragmentManager fm = this.getSupportFragmentManager();
 				SetAttributesFragment createChar = new SetAttributesFragment();
 		        createChar.show(fm, CREATE_CHAR_TAG);
 			}
 		}
 		
 
 		showLevelUpDialog();
 
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
 		if(triggerDbUpdate) {
 			DatabaseHelper db = new DatabaseHelper(this);
 			db.updateCharacterAttributes(character);
 		}
 		
 		/*System.out.println("******************************");
 		System.out.println("UPDATING ALL FRAGMENTS IN " + this.toString());
 		System.out.println("pagerAdapter " + pagerAdapter.toString());
 		System.out.println("viewPager " + pager.toString());
 		System.out.println("fragments " + fragments.toString());
 		System.out.println("pager items " + pagerAdapter.getItems().toString());
 		System.out.println("******************************");*/
 		for(SherlockFragment f : pagerAdapter.getItems()) {			
 			((CharacterFragment)f).refresh();
 		}
 		
 		
 		showLevelUpDialog();
 		
 	}
 	
 	public void showLevelUpDialog() {
 		if(character.getNewLevels() > 0) {
 			int level = character.getLevel() - character.getNewLevels() + 1;
 			
 			FragmentManager fm = this.getSupportFragmentManager();
 			LevelUpFragment levelup = new LevelUpFragment();
 			Bundle b = new Bundle();
 			b.putInt(LevelUpFragment.LEVEL, level);
 			levelup.setArguments(b);
 			levelup.show(fm, LEVELUP_TAG);
 			
 		}
 	}
 	
 	public void removeFeat(PfFeats feat) {
 		this.character.removeFeat(feat);
 		DatabaseHelper db = new DatabaseHelper(this);
 		db.deleteFeat(this.character, feat);
 		updateCharacter();
 	}
 	
 	public void addFeat(PfFeats feat) {
 		if(this.character.gainFeat(feat)) {
 			DatabaseHelper db = new DatabaseHelper(this);
 			db.addFeat(this.character, feat);
 			updateCharacter();
 		}
 	}
 	
 	public void addItem(Item item) {
 		int stackSize = this.character.addItem(item);
 		DatabaseHelper db = new DatabaseHelper(this);
 		item.setStackSize(stackSize);
 		db.addItem(this.character, item);
 		updateCharacter();
 	}
 	
 	public void removeItem(Item item, int amount) {
 		int oldStackSize = item.getStackSize();
 		
 		if(this.character.removeItem(item, amount)) {
 			DatabaseHelper db = new DatabaseHelper(this);
 			db.removeItem(this.character, item, oldStackSize - amount);
 			updateCharacter();
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(final Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt("currentItem", pager.getCurrentItem());
 	}
 
 	public void equipItem(Item item) {
 		character.equipItem(item);
 		
 		DatabaseHelper db = new DatabaseHelper(this);
 		db.equipItem(this.character, item);
 		this.updateCharacter(false);
 		
 	}
 }
