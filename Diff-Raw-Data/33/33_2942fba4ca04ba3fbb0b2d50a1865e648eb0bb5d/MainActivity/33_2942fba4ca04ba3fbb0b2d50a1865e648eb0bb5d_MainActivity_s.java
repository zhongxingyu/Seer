 package ru.inventos.yum;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.slidingmenu.lib.SlidingMenu;
 
 public class MainActivity extends Activity implements Updatable, SlidingMenu.OnOpenListener, 
 		SlidingMenu.OnCloseListener, OnItemClickListener, ServerStatusReceiver {
 	private final static int STATUS_UPDATE_PERIOD = 8000;
 	private TextView mOrderCount;
 	private ImageButton mOrderBtn;
 	private FrameLayout mOrderFrame;
 	private TextView mTitle;
 	private ImageView mStatus;
 	private SlidingMenu mMenu;
 	private ListView mMenuList;
 	private ImageButton mMenuBtn;
 	private Cart mCart;
 	private MainListAdapter mLunchListAdapter;
 	private NetStorage mNetStorage;
 	private boolean mServerActive;
 	private Timer mTimer; 
 	private Handler mTimerHandler;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);		
 		findViewsByIds();
 		ListView bottomMenu = (ListView) findViewById(R.id.main_menu_menulist2);
 		bottomMenu.setOnItemClickListener(this);
 		mMenu.setOnOpenListener(this);
 		mMenu.setOnCloseListener(this);
 		createMenu();
 		createMenu2();	
 		mMenu.showMenu();
 		mCart = new Cart();
 		mNetStorage = new NetStorage(this);
 		mLunchListAdapter = new MainListAdapter(this, mCart, mNetStorage);
 		ListView lunchList = (ListView) findViewById(R.id.main_list);
 		lunchList.setAdapter(mLunchListAdapter);
 		selectFirstCategory();		
 		registerListeners();
 		update();
 		handleIntent(getIntent());
 		logon(true);
 		mServerActive = false;
 		mTimer = new Timer();
 		mTimerHandler = new Handler();
 		mTimer.scheduleAtFixedRate(new TimerBody(), 0, STATUS_UPDATE_PERIOD);
 	}
 
 	private void logon(boolean isFirstLogin) {
 		Intent intent = new Intent(this, Login.class);
 		intent.putExtra(Consts.LOGIN_IS_FIRST_LOGIN, isFirstLogin);
 		startActivityForResult(intent, Consts.LOGIN_REQUEST);		
 	}
 	
 	@Override
 	public boolean onSearchRequested() {
 		return super.onSearchRequested();
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	private void handleIntent(Intent intent) {
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			if (mLunchListAdapter.findAndShow(query)) {
 				mMenuList.setTag(Integer.valueOf(-1));
 				mMenu.toggle();
 			}
 			else {
 				Toast toast = Toast.makeText(this, R.string.main_lunch_not_found, Consts.TOASTS_SHOW_DURATION);
 				toast.show();
 			}
 	    }
 	}
 	
 	@Override
 	public void update() {
 		updateOrderBtn();
 	}
 	
 	private void updateOrderBtn() {
 		int count = mCart.getCount();
 		if (count == 0) {
 			mOrderCount.setText("0");
 		}
 		else {
 			mOrderCount.setText(Integer.toString(count));
 		}
 		if (count == 0 || !mServerActive) {
 			mOrderBtn.setImageResource(R.drawable.main_actionbar_order_btn);
 			mOrderBtn.setClickable(false);
 		}
 		else {
 			mOrderBtn.setImageResource(R.drawable.main_actionbar_order_btn_full);
 			mOrderBtn.setClickable(true);
 		}
 	}
 	
 	private void findViewsByIds() {
 		mOrderCount = (TextView) findViewById(R.id.main_actionbar_order_count);
 		mOrderBtn = (ImageButton) findViewById(R.id.main_actionbar_order_btn); 
 		mOrderFrame = (FrameLayout) findViewById(R.id.main_actionbar_order);
 		mTitle = (TextView) findViewById(R.id.main_actionbar_title);
 		mStatus = (ImageView) findViewById(R.id.main_actionbar_status);
 		mMenu = (SlidingMenu) findViewById(R.id.slidingmenulayout);
 		mMenuList = (ListView) findViewById(R.id.main_menu_menulist);
 		mMenuBtn = (ImageButton) findViewById(R.id.main_actionbar_menu_btn);
 	}
 	
 	private void createMenu() {
 		MainMenuAdapter adapter = new MainMenuAdapter(this, 
         		R.array.menu_item_names, R.array.menu_item_pic_names);
         mMenuList.setAdapter(adapter);
         mMenuList.setOnItemClickListener(this);
 	}
 	
 	private void createMenu2() {
 		ListView menuList2 = (ListView) findViewById(R.id.main_menu_menulist2);
 		MainMenuAdapter adapter = new MainMenuAdapter(this, 
         		R.array.menu_item_names2, R.array.menu_item_pic_names2);
         menuList2.setAdapter(adapter);		
 	}
 	
 	private void selectFirstCategory() {
 		View view = (View) mMenuList.getAdapter().getItem(0);
 		String category = (String) view.getTag();
 		mLunchListAdapter.setCategory(category);
 		mMenuList.setTag(Integer.valueOf(0));				
 	}
 	
 	@Override 
 	public void onOpen() {
 		mOrderFrame.setVisibility(View.INVISIBLE);
 		mStatus.setVisibility(View.VISIBLE);
 		mTitle.setText(R.string.lunch_in_office);
 		Resources res = this.getResources();
 		mTitle.setTextColor(res.getColor(R.color.actionbar_title_open));
 		mMenuBtn.setImageResource(R.drawable.button_main_active);		
 	}
 	
 	@Override 
 	public void onClose() {
 		mOrderFrame.setVisibility(View.VISIBLE);
 		mStatus.setVisibility(View.INVISIBLE);
 		mTitle.setText(R.string.choise_of_dishes);
 		Resources res = this.getResources();
 		mTitle.setTextColor(res.getColor(R.color.actionbar_title_close));
 		mMenuBtn.setImageResource(R.drawable.button_main_normal);
 	}
 	
 	public void onMenuBtnClick(View view) {
 		mMenu.toggle();		
 	}
 	
 	public void onOrderBtnClick(View view) {
 		Intent intent = new Intent(this, Order.class);
 		startActivity(intent);
 	}
 	
 	public void onSearchBtnClick(View view) {
 		onSearchRequested();		
 	}
 	
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		if (parent.getId() == R.id.main_menu_menulist) {
 			String name = (String) view.getTag();
 			mLunchListAdapter.setCategory(name);	
 			parent.setTag(Integer.valueOf(position));
 		}
 		else {
 			Intent intent = null;
 			switch (position) {
 			case 0:	
 				intent = new Intent(this, MyOrders.class);
 				startActivity(intent);
 				break;
 			case 1: 
 				intent = new Intent(this, FeedbackActivity.class);
 				startActivity(intent);
 				break;
 			}
 		}
 	}
 	
 	private void registerListeners() {
 		mCart.registerDataListener(mLunchListAdapter);
 		mCart.registerDataListener(this);
 	}
 	
 	private void unregisterListeners() {
 		mCart.unregisterDataListener(mLunchListAdapter);
 		mCart.unregisterDataListener(this);
 	}
 	
 	@Override
 	protected void onDestroy() {
 		mTimer.cancel();
 		super.onDestroy();
 		unregisterListeners();				
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == Consts.LOGIN_REQUEST) {
 			if (data != null && !data.getBooleanExtra(Consts.LOGIN_STATUS, false)) {
 				finish();
 			}
 		}
 	}
 	
 	public void onLogoutBtnClick(View v) {
 		mNetStorage.terminateSession();
 		mCart.clear();
 		logon(false);
 	}
 	
 	@Override 
 	public void receiveServerStatus(boolean status) {
 		mServerActive = status;
 		updateOrderBtn();
 		if (status) {
 			mStatus.setImageResource(R.drawable.lamp_on);
 		}
 		else {
 			mStatus.setImageResource(R.drawable.lamp_off);
 		}
 	}
 	
 	private ServerStatusReceiver getServerStatusReceiver() {
 		return this;		
 	}
 	
 	private class TimerBody extends TimerTask {
 		@Override
 	    public void run() {
 			mTimerHandler.post(new Runnable() {
 				@Override
 	            public void run() {
 					mNetStorage.getServerStatus(getServerStatusReceiver());
 	            }
 	        });			
 	    }
 	}
 }
