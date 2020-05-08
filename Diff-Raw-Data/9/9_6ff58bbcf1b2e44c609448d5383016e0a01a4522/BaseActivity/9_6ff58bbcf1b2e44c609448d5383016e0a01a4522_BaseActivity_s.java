 package ch.unibe.ese.shopnote.core;
 
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.telephony.TelephonyManager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.unibe.ese.shopnote.R;
 import ch.unibe.ese.shopnote.activities.VerifyNumberActivity;
 import ch.unibe.ese.shopnote.adapters.ShoppingListAdapter;
 import ch.unibe.ese.shopnote.core.sqlite.SQLitePersistenceManager;
 import ch.unibe.ese.shopnote.drawer.NavigationDrawer;
 import ch.unibe.ese.shopnote.share.SyncManager;
 
 /**
  * Extension of {@link Activity} that allows easy access to the global managers
  * (like {@link ListManager}, {@link FriendsManager} or {@link SyncManager}.
  */
 public class BaseActivity extends Activity {
 	
 	public static final String EXTRAS_LIST_ID = "listId";
 	public static final String EXTRAS_ITEM_ID = "itemId";
 	public static final String EXTRAS_ITEM_NAME = "itemName";
 	public static final String EXTRAS_ITEM_EDIT= "itemEdit";
 	public static final String EXTRAS_IS_RECIPE = "isRecipe";
 	public static final String EXTRAS_RECIPE_ID = "recipeId";
 	public static final String EXTRAS_FRIEND_ID = "friendId";
 	public static final String EXTRAS_FRIEND_NAME= "friendName";
 	protected DrawerLayout drawMenu;
 	protected ActionBarDrawerToggle drawerToggle;
 	private boolean drawerToggleCreated;
 	private String title;
 	
 	//color variables
 	private int titleBarColor;
 	private int backgroundColor;
 	private int navigationDrawerColor;
 	private int createTextBoxColor;
 	private int textColor;
 	private int listViewColor;
 	private int listViewDividerColor;
 	public boolean colorUpdated = false;
 	
 	/**
 	 * Returns the singleton ListManager which is responsible for all the shopping lists
 	 * @return ListManager
 	 */
 	public ListManager getListManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		ListManager manager = app.getListManager();
 		if (manager == null) {
 			manager = new ListManager(new SQLitePersistenceManager(
 					getApplicationContext()));
 			app.setListManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Returns the singleton FriendsManager which is responsible for the friends
 	 * @return FriendsManager
 	 */
 	public FriendsManager getFriendsManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		FriendsManager manager = app.getFriendsManager();
 		if (manager == null) {
 			manager = new FriendsManager(new SQLitePersistenceManager(
 					getApplicationContext()), getSyncManager(), this);
 			app.setFriendsManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Returns the singleton SyncManager which is responsible for the communication between app and server
 	 * @return
 	 */
 	public SyncManager getSyncManager() {
 		BaseApplication app = (BaseApplication) this.getApplication();
 		SyncManager manager = app.getSyncManager();
 		if (manager == null) {
 			manager = new SyncManager();
 			app.setSyncManager(manager);
 		}
 		return manager;
 	}
 
 	/**
 	 * Shortcut to display a toast on an activity
 	 * @param text
 	 */
 	public void showToast(String text) {
 		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Sets a text to a TextView.
 	 * <p>
 	 * If the TextView with the given id does not exist, a
 	 * {@link NullPointerException} is thrown.
 	 * 
 	 * @param id
 	 *            id of the TextView.
 	 * @param value
 	 *            The value to set.
 	 */
 	public void setTextViewText(int id, String value) {
 		TextView textView = (TextView) findViewById(id);
 		textView.setText(value);
		updateThemeTextBox(textView);
 	}
 
 	/**
 	 * Gets the text from the TextView with the given id.
 	 * <p>
 	 * If the TextView with the given id does not exist, a
 	 * {@link NullPointerException} is thrown.
 	 * 
 	 * @param id
 	 *            id of the TextView.
 	 * @return The text in the view.
 	 */
 	public String getTextViewText(int id) {
 		TextView textView = (TextView) findViewById(id);
 		return textView.getText().toString().trim();
 	}
 	
 	/**
 	 * Checks if the android device has a connection to the internet
 	 * 
 	 * @return true if it is online
 	 */
 	public boolean isOnline() {
 	    ConnectivityManager cm =
 	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 	        return true;
 	    }
 	    return false;
 	}
 	
 	/**
 	 * Shortcut to get the phonenumber of the current device
 	 * @return Phonenumber as String (Or empty String, if it is not supported by your phone)
 	 */
 	public String getMyPhoneNumber() {
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean phoneNumberApproved = settings.getBoolean("phonenumberapproved", false);
 		if ("sdk".equals( Build.PRODUCT )) {
 			TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
 			return tMgr.getLine1Number();
 		}
 		else if(!phoneNumberApproved) {
 			Intent intent = new Intent(this, VerifyNumberActivity.class);
 			startActivity(intent);
 		}
 		String phoneNumber = settings.getString("phonenumber", "-1");
 		return phoneNumber;
 	}
 	
 	/**
 	 * Needs to be implemented by every activity
 	 * Refreshes all the content it has
 	 */
 	public void refresh() {};
 	    
 	 /**
 	 * Force opens the soft keyboard
 	 */
 	public void openKeyboard() {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
 	}
 	
 	/**
 	 * Force closes the soft keyboard
 	 */
 	public void closeKeyboard() {
         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
 	}
 	
 	/**
 	 * Closes the keyboard and finishes the current activity
 	 */
 	@Override
 	public void finish() {
 		closeKeyboard();
 		super.finish();
 	}
 	
 	/**
 	 * Calculates the number of bought items and the total number of items in a shopping list
 	 */
 	public void calculateItemCount(List<ShoppingList> lists, ShoppingListAdapter adapter) {
 		for (ShoppingList list: lists) {
 			List<Item> items = getListManager().getItemsFor(list);
 			
 			int boughtItems = 0;
 			int totalItems = 0;
 			
 			if (items != null) {
 				for (Item item: items) {
 					if (item.isBought())
 						boughtItems++;			
 				}
 				totalItems = items.size();
 			}
 			
 			adapter.setCount(boughtItems, totalItems);
 		}
 	}
 	
 	protected void createDrawerMenu() {
 		NavigationDrawer nDrawer = new NavigationDrawer();
 		drawMenu = nDrawer.constructNavigationDrawer(drawMenu, this);
 	}
 	
 	protected void createDrawerToggle() {
 		drawerToggle = new ActionBarDrawerToggle(
                 this,                  	/* host Activity */
                 drawMenu,         		/* DrawerLayout object */
                 R.drawable.ic_drawer,  	/* nav drawer icon to replace 'Up' caret */
                 R.string.app_name,  	/* "open drawer" description */
                 R.string.app_name  		/* "close drawer" description */
                 ) {
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(title);
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
             	title = (String) getActionBar().getTitle();
                 getActionBar().setTitle(R.string.app_name);
             }
         };
         
         // Set the drawer toggle as the DrawerListener
         drawMenu.setDrawerListener(drawerToggle);
         drawerToggleCreated = true;
 	}
 	
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         if (drawerToggleCreated)
         	drawerToggle.syncState();
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         if (drawerToggleCreated)
         	drawerToggle.onConfigurationChanged(newConfig);
     }
     
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (drawMenu != null)
 			drawMenu.closeDrawers();
 	}
 	
 	//Popup window for recruiting new friends
 	protected void FriendPopup(BaseActivity activity) {
 		final PopupWindow popUp = new PopupWindow(this);
         RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.RelativeLayoutFriendInviteScreen);
         LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View layout = layoutInflater.inflate(R.layout.friend_invite_screen, (ViewGroup) viewGroup);
         
 		popUp.setContentView(layout);
 		popUp.setFocusable(true);
 		popUp.setWidth(550);
 		popUp.setHeight(300);
 		
 		popUp.showAtLocation(layout, Gravity.CENTER, 10, 10);
 		popUp.setBackgroundDrawable(new ColorDrawable(titleBarColor));
 		
 		Button close = (Button) layout.findViewById(R.id.close);
 		close.setOnClickListener(new OnClickListener() {
 			 @Override
 		     public void onClick(View v) {
 		       popUp.dismiss();
 		     }
 		});
 		
 		Button send = (Button) layout.findViewById(R.id.send);
 		final BaseActivity finalActivity = activity;
 		send.setOnClickListener(new OnClickListener() {
 			 @Override
 		     public void onClick(View v) {
 		       popUp.dismiss();
 		       InvitePopup(finalActivity);
 		     }
 		});
 		
 	}
 	
 	protected void InvitePopup(BaseActivity activity) {
 		final PopupWindow popUp = new PopupWindow(this);
         RelativeLayout viewGroup = (RelativeLayout) findViewById(R.id.RelativeLayoutSendInvitationScreen);
         LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View layout = layoutInflater.inflate(R.layout.send_invitation_screen, (ViewGroup) viewGroup);
         
 		popUp.setContentView(layout);
 		popUp.setFocusable(true);
 		popUp.setWidth(550);
 		popUp.setHeight(300);
 		
 		popUp.showAtLocation(layout, Gravity.CENTER, 10, 10);
 		popUp.setBackgroundDrawable(new ColorDrawable(titleBarColor));
 		
 		Button close = (Button) layout.findViewById(R.id.close);
 		close.setOnClickListener(new OnClickListener() {
 			 @Override
 		     public void onClick(View v) {
 		       popUp.dismiss();
 		     }
 		});
 		
 		Button send = (Button) layout.findViewById(R.id.send);
 		send.setOnClickListener(new OnClickListener() {
 			 @Override
 		     public void onClick(View v) {
 		       popUp.dismiss();
 		       
 		     }
 		});
 	}
 		   
 	
 	
 	
 	//Color functions to paint the program
 	protected void updateThemeListView(ListView lv) {
 		if(!colorUpdated)
 			getColorSettings();
 		lv.setBackgroundColor(listViewColor);
 		lv.setDivider(new ColorDrawable(listViewDividerColor));
 		lv.setDividerHeight(2);
 	}
 	
 	protected void updateThemeTextBox(View view) {
 		if(!colorUpdated)
 			getColorSettings();
 		view.setBackgroundColor(createTextBoxColor);
 	}
 	
 	protected void updateTheme(View layout, ActionBar actionBar, View layoutDrawer) {
 		updateTheme(layout, actionBar);
 		layoutDrawer.setBackgroundColor(navigationDrawerColor);		
 	}
 	
 	protected void updateTheme(View layout, ActionBar actionBar) {
 		if(!colorUpdated)
 			getColorSettings();
 		actionBar.setBackgroundDrawable(new ColorDrawable(titleBarColor));
 		layout.setBackgroundColor(backgroundColor);
 	}
 	
 	private void getColorSettings() {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		String colorString = sharedPref.getString("color_setting", "white");
 		String colorChoosable[] = getResources().getStringArray(R.array.default_color_choice_names);
 		String colors[] = getResources().getStringArray(R.array.default_color_choice_white);
 
 		if(colorString.equals(colorChoosable[1]))
 			colors = getResources().getStringArray(R.array.default_color_choice_dark);
 		else if(colorString.equals(colorChoosable[2]))
 			colors = getResources().getStringArray(R.array.default_color_choice_chocolate);
 		else if(colorString.equals(colorChoosable[3]))
 			colors = getResources().getStringArray(R.array.default_color_choice_barbie);
 		if(colors == null) throw new IllegalStateException();
 		
 		this.backgroundColor = Color.parseColor(colors[0]);
 		this.navigationDrawerColor = Color.parseColor(colors[1]);
 		this.titleBarColor = Color.parseColor(colors[2]);
 		this.textColor = Color.parseColor(colors[3]);
 		this.createTextBoxColor = Color.parseColor(colors[4]);
 		this.listViewColor = Color.parseColor(colors[5]);
 		this.listViewDividerColor = Color.parseColor(colors[6]);
 		this.colorUpdated = true;
 	}
 	
 	/**
 	 * Handler class to allow threads to update UI thread
 	 */
 	protected class SynchHandler extends Handler{
 		private BaseActivity activity;
 		
 		public SynchHandler(BaseActivity activity) {
 			super();
 			this.activity = activity;
 		}
 		
 		public void handleMessage(Message msg) {
       	  super.handleMessage(msg);  
       	  switch(msg.what) {
       	  		case 0:
       	  		 	refresh();
       	  		 	break;
       	  		case 1:
       	  			FriendPopup(activity);
       	  			break;
       	  }
       	 
 		}
 	}
 }
