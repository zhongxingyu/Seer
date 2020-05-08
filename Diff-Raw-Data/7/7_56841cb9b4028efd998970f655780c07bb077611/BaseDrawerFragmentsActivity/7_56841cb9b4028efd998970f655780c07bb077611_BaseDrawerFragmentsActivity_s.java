 package com.framework.leopardus.activities;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import net.simonvt.menudrawer.MenuDrawer;
 import net.simonvt.menudrawer.MenuDrawer.OnDrawerStateChangeListener;
 import net.simonvt.menudrawer.Position;
 import android.annotation.SuppressLint;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Pair;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 import com.framework.leopardus.R;
 import com.framework.leopardus.adapters.ApplicationMenuItem;
 import com.framework.leopardus.adapters.ItemsAdapter;
 import com.framework.leopardus.enums.Ubications;
 import com.framework.leopardus.fragments.BaseFragment;
 import com.framework.leopardus.interfaces.ActivityMethodInterface;
 import com.framework.leopardus.interfaces.InjectableActionBarItem;
 import com.framework.leopardus.interfaces.InjectableMenuItems;
 import com.framework.leopardus.interfaces.MenuItemEvent;
 import com.framework.leopardus.interfaces.injection.InjectActionBarItem;
 import com.framework.leopardus.interfaces.injection.InjectMenuItem;
 import com.framework.leopardus.utils.Injector;
 import com.framework.leopardus.utils.InterfacesHelper;
 
 public abstract class BaseDrawerFragmentsActivity extends
 		SherlockFragmentActivity implements InjectableMenuItems,
 		InjectableActionBarItem {
 	protected MenuDrawer mMenuDrawer;
 	protected ItemsAdapter adapter;
 	private boolean enableProgressFeatures = false;
 	private ListView mList;
 	SparseArray<Pair<InjectMenuItem, MenuItemEvent>> menuItems = new SparseArray<Pair<InjectMenuItem, MenuItemEvent>>(
 			0);
 	SparseArray<Pair<InjectActionBarItem, Runnable>> abData = new SparseArray<Pair<InjectActionBarItem, Runnable>>(
 			0);
 	private boolean autoExit = false;
 	private boolean enableHomeBtn = true;
 	@SuppressWarnings("unused")
 	private int mActivePosition;
 	ActivityMethodInterface closeCallback = InterfacesHelper.getCloseMethod();
	private static Stack<Fragment> fragments = new Stack<Fragment>();
 	private static final String STATE_ACTIVE_POSITION = "net.simonvt.menudrawer.samples.LeftDrawerSample.activePosition";
 	private static final String STATE_CURRENT_FRAGMENT = "net.simonvt.menudrawer.samples.FragmentSample";
 
 	private FragmentManager mFragmentManager;
 	private FragmentTransaction mFragmentTransaction;
 	@SuppressWarnings("unused")
 	private String mCurrentFragmentTag;
 	private int autoExitId = 8802;
 	private int res_drawer = R.drawable.ic_drawer;
 
 	protected void commitTransactions() {
 		if (mFragmentTransaction != null && !mFragmentTransaction.isEmpty()) {
 			mFragmentTransaction.commit();
 			mFragmentTransaction = null;
 		}
 	}
 
 	protected FragmentTransaction ensureTransaction() {
 		if (mFragmentTransaction == null) {
 			mFragmentTransaction = mFragmentManager.beginTransaction();
 			mFragmentTransaction
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
 		}
 
 		return mFragmentTransaction;
 	}
 
 	private Fragment getFragment(String tag) {
 		Fragment f = mFragmentManager.findFragmentByTag(tag);
 
 		if (f == null) {
 			f = new Fragment();
 		}
 		return f;
 	}
 
 	protected void attachFragment(int layout, Fragment f, String tag) {
 		if (f != null) {
 			if (f.isDetached()) {
 				ensureTransaction();
 				mFragmentTransaction.attach(f);
 			} else if (!f.isAdded()) {
 				ensureTransaction();
 				mFragmentTransaction.add(layout, f, tag);
 			}
 		}
 	}
 
 	protected void detachFragment(Fragment f) {
 		if (f != null && !f.isDetached()) {
 			ensureTransaction();
 			mFragmentTransaction.detach(f);
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle inState) {
 		super.onCreate(inState);
 		if (inState != null) {
 			mActivePosition = inState.getInt(STATE_ACTIVE_POSITION);
 		}
 		adapter = new ItemsAdapter(this);
 		Injector i = new Injector(this);
 		i.injectViews(this);
 		i.injectMenuItems(this);
 		i.injectActionBarItems(this);
 		i.injectMethodsIntoViews(this);
 
 		if (enableProgressFeatures) {
 			requestWindowFeature(Window.FEATURE_PROGRESS);
 			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		}
 
 		if (enableProgressFeatures) {
 			setSupportProgressBarIndeterminateVisibility(false);
 			setSupportProgressBarVisibility(false);
 		}
 
 		// setContentView(R.layout.menu_frame);
 		mList = new ListView(this);
 		establishItemsAdapter();
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			getActionBar().setDisplayHomeAsUpEnabled(enableHomeBtn);
 		}
 
 		makeMenuDrawer();
 
 		// ======================================================
 
 		mFragmentManager = getSupportFragmentManager();
 		if (inState != null) {
 			mCurrentFragmentTag = inState.getString(STATE_CURRENT_FRAGMENT);
 		} else {
 			if (adapter.getCount() > 0) {
 				mCurrentFragmentTag = getResources().getString(
 						adapter.getItem(0).textRes);
 				setActualFragment(getFragment("xxx"), false);
 			}
 		}
 
 		autosetActionBarTitle();
 	}
 
 	public void makeMenuDrawer() {
 		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.Type.OVERLAY,
 				getDrawerPosition(), getDragMode());
 		mMenuDrawer.setMenuView(mList);
 		mMenuDrawer.setContentView(R.layout.activity_drawer);
 		// TODO:
 		// mMenuDrawer.setTouchMode(MenuDrawer.TOUCH_MODE_FULLSCREEN);
 		mMenuDrawer.setSlideDrawable(this.res_drawer);
 		mMenuDrawer.setDrawerIndicatorEnabled(true);
 		mMenuDrawer
 				.setOnDrawerStateChangeListener(new OnDrawerStateChangeListener() {
 
 					@Override
 					public void onDrawerStateChange(int oldState, int newState) {
 						autosetActionBarTitle();
 					}
 
 					@Override
 					public void onDrawerSlide(float openRatio, int offsetPixels) {
 
 					}
 				});
 	}
 
 	/**
 	 * Use dark bars in menu drawer
 	 */
 	public void enableDarkDrawer() {
 		this.res_drawer = R.drawable.ic_drawer_dark;
 		changeDrawer();
 	}
 
 	/**
 	 * Use white bars in menu drawer
 	 */
 	public void enableLightDrawer() {
 		this.res_drawer = R.drawable.ic_drawer_light;
 		changeDrawer();
 	}
 
 	public void setEnableProgressFeatures(boolean enableProgressFeatures) {
 		this.enableProgressFeatures = enableProgressFeatures;
 	}
 
 	private void changeDrawer() {
 		try {
 			mMenuDrawer.setSlideDrawable(this.res_drawer);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 
 	private Fragment topFragment;
 
 	/**
 	 * Get the actual fragment
 	 * 
 	 * @param frgmnt
 	 */
 	public Fragment getActualFragment() {
 		return topFragment;
 	}
 
 	/**
 	 * Set the actual fragment
 	 * 
 	 * @param frgmnt
 	 */
 	public void setActualFragment(Fragment frgmnt) {
 		setActualFragment(frgmnt, true);
 	}
 
 	public void setActualFragment(Fragment frgmnt, boolean addToStack) {
 		if (frgmnt == null) {
 			return;
 		}
 		if (addToStack) {
 			if (fragments.size() > 0) {
 				Fragment top = fragments.pop();
 				if (top.equals(frgmnt)) {
 					fragments.push(top);
 					// Nothing to do...
 					toggleMenu();
 					return;
 				} else {
 					if (!fragments.contains(frgmnt) && !frgmnt.equals(top)) {
 						fragments.push(top);
 						fragments.push(frgmnt);
 					} else {
 						fragments.push(top);
 						if (fragments.contains(frgmnt)) {
 							top = null;
 							while (!frgmnt.equals(top)) {
 								top = fragments.pop();
 							}
 							fragments.push(frgmnt);
 						}
 					}
 				}
 			} else if (fragments.size() == 0) {
 				fragments.push(frgmnt);
 			}
 		}
 		FragmentTransaction transaction = getSupportFragmentManager()
 				.beginTransaction();
 		transaction.setCustomAnimations(android.R.anim.slide_in_left,
 				android.R.anim.slide_out_right);
 		transaction.replace(mMenuDrawer.getContentContainer().getId(), frgmnt,
 				frgmnt.getTag());
 		transaction.commit();
 		topFragment = frgmnt;
 		autosetActionBarTitle();
 		mMenuDrawer.closeMenu();
 	}
 
 	public void enableHomeAsButton() {
 		enableHomeBtn = true;
 		try {
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 				getActionBar().setDisplayHomeAsUpEnabled(enableHomeBtn);
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 
 	public void disableHomeAsButton() {
 		enableHomeBtn = false;
 		try {
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 				getActionBar().setDisplayHomeAsUpEnabled(enableHomeBtn);
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 
 	protected int getDragMode() {
 		return MenuDrawer.MENU_DRAG_CONTENT;
 	}
 
 	protected Position getDrawerPosition() {
 		return Position.LEFT;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 			if (fragments.size() <= 1) {
 				closeCallback.Method(this);
 				return false;// super.onKeyDown(keyCode, event);
 			} else {
 				fragments.pop();
 				setActualFragment(fragments.pop());
 				return false;
 			}
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void close() {
 		closeCallback.Method(this);
 	}
 
 	public void setOnCloseEvent(ActivityMethodInterface ami) {
 		closeCallback = ami;
 	}
 
 	class InjectMenuItemComparator implements Comparator<InjectMenuItem> {
 
 		@Override
 		public int compare(InjectMenuItem o1, InjectMenuItem o2) {
 			if (o1.position() < o2.position()) {
 				return -1;
 			} else if (o1.position() > o2.position()) {
 				return 1;
 			}
 			return 0;
 		}
 	}
 
 	@SuppressLint("UseSparseArrays")
 	protected void establishItemsAdapter() {
 		List<InjectMenuItem> localMenuItems = new ArrayList<InjectMenuItem>(0);
 		Map<InjectMenuItem, Integer> keys = new HashMap<InjectMenuItem, Integer>();
 		int size = menuItems.size();
 		for (int i = 0; i < size; i++) {
 			int key = menuItems.keyAt(i);
 			keys.put(menuItems.get(key).first, key);
 			localMenuItems.add(menuItems.get(key).first);
 		}
 		Collections.sort(localMenuItems, new InjectMenuItemComparator());
 		adapter.clear();
 		for (int i = 0; i < size; i++) {
 			InjectMenuItem item = localMenuItems.get(i);
 			ApplicationMenuItem mi = new ApplicationMenuItem(item.stringId(),
 					item.iconId());
 			mi.setMenuKey(keys.get(item));
 			adapter.add(mi);
 		}
 		if (autoExit) {// TODO:
 			ApplicationMenuItem quit = new ApplicationMenuItem(R.string.quit,
 					R.drawable.ico_dark_quit);
 			quit.setMenuKey(this.autoExitId);
 			adapter.add(quit);
 		}
 		mList.setAdapter(adapter);
 		mList.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				mActivePosition = position;
 				mMenuDrawer.setActiveView(view, position);
 				adapter.setActivePosition(position);
 				ApplicationMenuItem item = (ApplicationMenuItem) adapter
 						.getItem(position);
 				if (item.getMenuKey() != autoExitId) {
 					Pair<InjectMenuItem, MenuItemEvent> m_item = menuItems
 							.get(item.getMenuKey());
 					if (m_item != null) {
 						MenuItemEvent x = m_item.second;
 						x.onListItemClick(null, view, position);
 						mMenuDrawer.setActiveView(view, position);
 					}
 				} else {
 					close();
 				}
 			}
 
 		});
 	}
 
 	public void enableAutoExit() {
 		this.autoExit = true;
 	}
 
 	public void disableAutoExit() {
 		this.autoExit = false;
 	}
 
 	public boolean isAutoExitEnabled() {
 		return autoExit;
 	}
 
 	@Override
 	public int addNewActionBarItem(InjectActionBarItem i) {
 		int id = abData.size() + 1;
 		abData.put(id, new Pair<InjectActionBarItem, Runnable>(i, null));
 		return id;
 	}
 
 	@Override
 	public void addNewActionBarItem(int menuId, Runnable runnable) {
 		InjectActionBarItem first = abData.get(menuId).first;
 		abData.put(menuId, new Pair<InjectActionBarItem, Runnable>(first,
 				runnable));
 	}
 
 	@Override
 	public int addNewItem(InjectMenuItem i) {
 		int id = menuItems.size() + 1;
 		menuItems.put(id, new Pair<InjectMenuItem, MenuItemEvent>(i, null));
 		return id;
 	}
 
 	public int addNewItem(final int stringId, final int position, final int icon) {
 		InjectMenuItem i = new InjectMenuItem() {
 
 			@Override
 			public Class<? extends Annotation> annotationType() {
 				// TODO Auto-generated method stub
 				return InjectMenuItem.class;
 			}
 
 			@Override
 			public Ubications ubication() {
 				return null;
 			}
 
 			@Override
 			public int stringId() {
 				return stringId;
 			}
 
 			@Override
 			public int position() {
 				return position;
 			}
 
 			@Override
 			public int iconId() {
 				return icon;
 			}
 		};
 		int id = menuItems.size() + 1;
 		menuItems.put(id, new Pair<InjectMenuItem, MenuItemEvent>(i, null));
 		return id;
 	}
 
 	@Override
 	public void addNewEvent(int menuId, InjectMenuItem i,
 			MenuItemEvent menuItemEvent) {
 		InjectMenuItem first = menuItems.get(menuId).first;
 		menuItems.put(menuId, new Pair<InjectMenuItem, MenuItemEvent>(first,
 				menuItemEvent));
 	}
 
 	public void addNewEvent(int menuId, MenuItemEvent menuItemEvent) {
 		InjectMenuItem first = menuItems.get(menuId).first;
 		menuItems.put(menuId, new Pair<InjectMenuItem, MenuItemEvent>(first,
 				menuItemEvent));
 	}
 
 	private static final String STATE_CONTENT_TEXT = "com.framework.leopardus";
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(STATE_CONTENT_TEXT, "...");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			toggleMenu();
 			return true;
 		default:
 			try {
 				abData.get(item.getItemId()).second.run();
 			} catch (Exception e) {
 
 			}
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void toggleMenu() {// TODO:
 		if (mMenuDrawer != null) {
 			mMenuDrawer.toggleMenu();
 			autosetActionBarTitle();
 		}
 	}
 
 	private void autosetActionBarTitle() {
 		if (mMenuDrawer != null) {
 			if (!mMenuDrawer.isMenuVisible()) {
 				if (getActualFragment() instanceof BaseFragment) {
 					getSupportActionBar().setTitle(
 							((BaseFragment) getActualFragment())
 									.getFragmentName());
 				} else {
 					String s = String.valueOf(getResources().getString(R.string.app_name));
 					getSupportActionBar().setTitle(s);
 				}
 			} else {
 				getSupportActionBar().setTitle(
 						getResources().getString(R.string.app_name));
 			}
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		final int drawerState = mMenuDrawer.getDrawerState();
 		if (drawerState == MenuDrawer.STATE_OPEN
 				|| drawerState == MenuDrawer.STATE_OPENING) {
 			mMenuDrawer.closeMenu();
 			return;
 		}
 		super.onBackPressed();
 	}
 
 	protected Map<Integer,MenuItem> abItems = new HashMap<Integer, MenuItem>();
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		for (int j = 0; j < abData.size(); j++) {
 			int key = abData.keyAt(j);
 			Pair<InjectActionBarItem, Runnable> it = abData.get(key);
 			InjectActionBarItem i = it.first;
 			MenuItem mi = menu.add(Menu.NONE, key, i.itemPosition(),
 					i.stringId());
 			mi.setIcon(i.iconId());
 			
 			mi.setVisible( i.isVisible() );
 			
 			if (i.ItemAsIcon()) {
 				mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 			} else {
 				mi.setShowAsAction(i.showAs());
 			}
 			abItems.put(i.ItemId(), mi);
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 }
