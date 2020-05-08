 package android.support.compatibility;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.support.GApp;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Pair;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
import com.compatibility.R;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.util.*;
 
 /**
  * A class that implements the action bar pattern for pre-Honeycomb devices.
  */
 public class ActionBarHelperBase extends ActionBarHelper {
     private static final String MENU_RES_NAMESPACE = "http://schemas.android.com/apk/res/android";
     private static final String MENU_ATTR_ID = "id";
     private static final String MENU_ATTR_TITLE = "title";
     private static final String MENU_ATTR_ICON = "icon";
     private static final String MENU_ATTR_SHOW_AS_ACTION = "showAsAction";
 
     protected Set<Integer> mActionItemIds = new HashSet<Integer>();
     protected Map<Integer, List<SubMenuItemInfo>> mSubMenuItems = new LinkedHashMap<Integer, List<SubMenuItemInfo>>();
     protected List<Pair<String, Fragment>> mTabFragments = new ArrayList<Pair<String, Fragment>>();
 
     protected ActionBarMenu mMenu;
     protected int mCurrentTab;
     protected boolean mTabsSupport;
 
     protected ActionBarHelperBase(Activity activity) {
         super(activity);
     }
 
     /**{@inheritDoc}*/
     @Override
     public void onCreate(Bundle savedInstanceState) {
         mActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
     }
 
     /**{@inheritDoc}*/
     @Override
     public void onPostCreate(Bundle savedInstanceState) {
         mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                 R.layout.actionbar_compat);
         setupActionBar();
 
         mMenu = new ActionBarMenu(mActivity);
         mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, mMenu);
         mActivity.onPrepareOptionsMenu(mMenu);
         for (int i = 0; i < mMenu.size(); i++) {
             MenuItem item = mMenu.getItem(i);
             if (mActionItemIds.contains(item.getItemId())) {
                 addActionItemCompatFromMenuItem(item);
             }
         }
 
         final ViewGroup tabBarCompat = getTabBarCompat();
         if (tabBarCompat == null) return;
         if (mTabsSupport) {
             tabBarCompat.setVisibility(View.VISIBLE);
 
             // define weights
             LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabBarCompat.getLayoutParams();
             params.weight = 0.6f;
             tabBarCompat.setLayoutParams(params);
 
             ViewGroup actionBar = getActionBarCompat();
             params = (LinearLayout.LayoutParams) actionBar.getLayoutParams();
             params.weight = 0.4f;
             actionBar.setLayoutParams(params);
         }
 
         final FragmentManager fragmentManager = ((FragmentActivity) mActivity).getSupportFragmentManager();
         int position = 0;
         for (final Pair<String, Fragment> fragmentPair : mTabFragments) {
             final TextView actionButton = new TextView(mActivity, null, R.attr.actionbarCompatItemStyle);
             LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
             params.setMargins(2, 2, 2, 2);
             actionButton.setLayoutParams(params);
 
             actionButton.setText(fragmentPair.first);
             actionButton.setBackgroundResource(R.drawable.tabbar_compat_item);
 
             final int item = position++;
             actionButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (mCurrentTab == item) return;
 
                     FragmentTransaction ft = fragmentManager.beginTransaction();
                     ft.replace(android.R.id.content, fragmentPair.second);
                     ft.commit();
 
                     tabBarCompat.getChildAt(mCurrentTab).setBackgroundResource(R.drawable.tabbar_compat_item);
                     actionButton.setBackgroundResource(R.drawable.tabbar_compat_item_selected);
                     mCurrentTab = item;
 
                 }
             });
 
             tabBarCompat.addView(actionButton);
         }
 
         try {
             FragmentTransaction ft = fragmentManager.beginTransaction();
             ft.add(android.R.id.content, mTabFragments.get(0).second).commit();
             tabBarCompat.getChildAt(0).setBackgroundResource(R.drawable.tabbar_compat_item_selected);
             mCurrentTab = 0;
         } catch (Exception e) {
             // empty tabs
         }
     }
 
     /**
      * Sets up the compatibility action bar with the given title.
      */
     private void setupActionBar() {
         final ViewGroup actionBarCompat = getActionBarCompat();
         if (actionBarCompat == null) {
             return;
         }
 
         LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(
                 0, ViewGroup.LayoutParams.FILL_PARENT);
         springLayoutParams.weight = 1;
 
         ActionBarMenu tempMenu = new ActionBarMenu(mActivity);
 
         // Add Home button
         ActionBarMenuItem homeItem = new ActionBarMenuItem(
                 tempMenu, android.R.id.home, 0, mActivity.getString(R.string.app_name));
         homeItem.setIcon(R.drawable.ic_home);
         addActionItemCompatFromMenuItem(homeItem);
 
         // Add title text
         TextView titleText = new TextView(mActivity, null, R.attr.actionbarCompatTitleStyle);
         titleText.setLayoutParams(springLayoutParams);
         titleText.setText(mActivity.getTitle());
         actionBarCompat.addView(titleText);
     }
 
     /**{@inheritDoc}*/
     @Override
     public void setRefreshActionItemState(boolean refreshing) {
         View refreshButton = mActivity.findViewById(R.id.actionbar_compat_item_refresh);
         View refreshIndicator = mActivity.findViewById(
                 R.id.actionbar_compat_item_refresh_progress);
 
         if (refreshButton != null) {
             refreshButton.setVisibility(refreshing ? View.GONE : View.VISIBLE);
         }
         if (refreshIndicator != null) {
             refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
         }
     }
 
     /**
      * Action bar helper code to be run in {@link android.app.Activity#onCreateOptionsMenu(android.view.Menu)}.
      *
      * NOTE: This code will mark on-screen menu items as invisible.
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Hides on-screen action items from the options menu.
         for (Integer id : mActionItemIds) {
             MenuItem menuItem = menu.findItem(id);
             if (menuItem != null) {
                 menuItem.setVisible(false);
             }
         }
 
         return true;
     }
 
     /**{@inheritDoc}*/
     @Override
     public void onTitleChanged(CharSequence title, int color) {
         TextView titleView = (TextView) mActivity.findViewById(R.id.actionbar_compat_title);
         if (titleView != null) {
             titleView.setText(title);
         }
     }
 
     /**
      * Returns a {@link android.view.MenuInflater} that can read action bar metadata on
      * pre-Honeycomb devices.
      */
     public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
         return new WrappedMenuInflater(mActivity, superMenuInflater);
     }
 
     @Override
     public void addActionBarTab(String label, Fragment fragment) {
         if (!mTabsSupport) {
             mActivity.setTheme(R.style.TabActivity);
             mTabsSupport = true;
         }
         mTabFragments.add(new Pair<String, Fragment>(label, fragment));
     }
 
     @Override
     public int getSelectedTab() {
         return mCurrentTab;
     }
 
     @Override
     public void createSearchMenuItem(final SearchViewQueryListener listener) {
         MenuItem item = mMenu.add(GApp.sInstance.getResources().getString(R.string.menu_search));
         item.setIcon(android.R.drawable.ic_menu_search);
 
         View actionButton = addActionItemCompatFromMenuItem(item);
         actionButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 final View actionBar = mActivity.findViewById(R.id.actionbar_compat);
                 final View searchBar = mActivity.findViewById(R.id.actionbar_search);
 
                 final View closeButton = mActivity.findViewById(R.id.actionbar_search_close);
                 final EditText filter = (EditText) mActivity.findViewById(R.id.actionbar_search_filter);
 
                 searchBar.getLayoutParams().height = actionBar.getHeight();
 
                 actionBar.setVisibility(View.GONE);
                 searchBar.setVisibility(View.VISIBLE);
                 Animation animation = AnimationUtils.loadAnimation(GApp.sInstance.getApplicationContext(),
                         R.anim.slide_left_in);
                 animation.reset();
                 searchBar.startAnimation(animation);
 
 
                 filter.addTextChangedListener(new TextWatcher() {
                     @Override
                     public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                     }
 
                     @Override
                     public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                     }
 
                     @Override
                     public void afterTextChanged(Editable editable) {
                         listener.onQueryTextChange(filter.getText().toString());
                     }
                 });
 
                 closeButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         searchBar.setVisibility(View.GONE);
 
                         actionBar.setVisibility(View.VISIBLE);
                         Animation animation = AnimationUtils.loadAnimation(GApp.sInstance.getApplicationContext(),
                                 R.anim.slide_right_in);
                         animation.reset();
                         actionBar.startAnimation(animation);
 
                         closeButton.setOnClickListener(null);
                     }
                 });
             }
         });
     }
 
     /**
      * Returns the {@link android.view.ViewGroup} for the action bar on phones (compatibility action
      * bar). Can return null, and will return null on Honeycomb.
      */
     private ViewGroup getActionBarCompat() {
         return (ViewGroup) mActivity.findViewById(R.id.actionbar_compat);
     }
 
     /**
      * Returns the {@link android.view.ViewGroup} for the tab bar on phones (compatibility action
      * bar). Can return null, and will return null on Honeycomb.
      */
     private ViewGroup getTabBarCompat() {
         return (ViewGroup) mActivity.findViewById(R.id.tabbar_compat);
     }
 
     /**
      * Adds an action button to the compatibility action bar, using menu information from a {@link
      * android.view.MenuItem}. If the menu item ID is <code>menu_refresh</code>, the menu item's
      * state can be changed to show a loading spinner using
      */
     private View addActionItemCompatFromMenuItem(final MenuItem item) {
         final int itemId = item.getItemId();
 
         final ViewGroup actionBar = getActionBarCompat();
         if (actionBar == null) {
             return null;
         }
 
         // Create the button
         ImageButton actionButton = new ImageButton(mActivity, null,
                 itemId == android.R.id.home
                         ? R.attr.actionbarCompatItemHomeStyle
                         : R.attr.actionbarCompatItemStyle);
         int width = (int) mActivity.getResources().getDimension(
                 itemId == android.R.id.home
                         ? R.dimen.actionbar_compat_button_home_width
                         : R.dimen.actionbar_compat_button_width);
 
         actionButton.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.FILL_PARENT));
         if (itemId == R.id.menu_refresh) {
             actionButton.setId(R.id.actionbar_compat_item_refresh);
         }
         actionButton.setImageDrawable(item.getIcon());
         actionButton.setScaleType(ImageView.ScaleType.CENTER);
         actionButton.setContentDescription(item.getTitle());
         actionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
 
                 List<SubMenuItemInfo> subItems = mSubMenuItems.get(itemId);
                 if (subItems != null) {
                     int size = subItems.size();
                     final String[] subMenuItems = new String[size];
                     final int[] ids = new int[size];
                     int i = 0;
 
                     for (SubMenuItemInfo info : subItems) {
                         subMenuItems[i] = info.getmTitle();
                         ids[i++] = info.getmId();
                     }
 
                     AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                     builder.setTitle(item.getTitle());
                     builder.setItems(subMenuItems, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int item) {
                             mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL,
                                     new ActionBarMenuItem(mMenu, ids[item], 0, subMenuItems[item]));
                         }
                     });
                     AlertDialog alert = builder.create();
                     alert.show();
                 }
 
             }
         });
 
         actionBar.addView(actionButton);
 
         if (item.getItemId() == R.id.menu_refresh) {
             // Refresh buttons should be stateful, and allow for indeterminate progress indicators,
             // so add those.
             ProgressBar indicator = new ProgressBar(mActivity, null,
                     R.attr.actionbarCompatProgressIndicatorStyle);
 
             final int buttonWidth = mActivity.getResources().getDimensionPixelSize(
                     R.dimen.actionbar_compat_button_width);
             final int buttonHeight = mActivity.getResources().getDimensionPixelSize(
                      R.dimen.actionbar_compat_button_width);
             final int progressIndicatorWidth = buttonWidth / 2;
 
             LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(
                     progressIndicatorWidth, progressIndicatorWidth);
             indicatorLayoutParams.setMargins(
                     (buttonWidth - progressIndicatorWidth) / 2,
                     (buttonHeight - progressIndicatorWidth) / 2,
                     (buttonWidth - progressIndicatorWidth) / 2,
                     0);
             indicator.setLayoutParams(indicatorLayoutParams);
             indicator.setVisibility(View.GONE);
             indicator.setId(R.id.actionbar_compat_item_refresh_progress);
             actionBar.addView(indicator);
         }
 
         return actionButton;
     }
 
     /**
      * A {@link android.view.MenuInflater} that reads action bar metadata.
      */
     private class WrappedMenuInflater extends MenuInflater {
         MenuInflater mInflater;
 
         public WrappedMenuInflater(Context context, MenuInflater inflater) {
             super(context);
             mInflater = inflater;
         }
 
         @Override
         public void inflate(int menuRes, Menu menu) {
             loadActionBarMetadata(menuRes);
             mInflater.inflate(menuRes, menu);
         }
 
         /**
          * Loads action bar metadata from a menu resource, storing a list of menu item IDs that
          * should be shown on-screen (i.e. those with showAsAction set to always or ifRoom).
          * @param menuResId
          */
         private void loadActionBarMetadata(int menuResId) {
             XmlResourceParser parser = null;
             try {
                 parser = mActivity.getResources().getXml(menuResId);
 
                 int eventType = parser.getEventType();
                 int itemId = 0;
                 String title = "";
                 int icon = 0;
                 int parentItemId = 0;
                 int showAsAction;
 
                 boolean eof = false;
                 while (!eof) {
                     switch (eventType) {
                         case XmlPullParser.START_TAG:
                             if (parser.getName().equals("menu") && itemId != 0) {
                                 mSubMenuItems.put(itemId, new LinkedList<SubMenuItemInfo>());
                                 parentItemId = itemId;
                             }
 
                             if (!parser.getName().equals("item")) {
                                 break;
                             }
 
                             itemId = parser.getAttributeResourceValue(MENU_RES_NAMESPACE,
                                     MENU_ATTR_ID, 0);
 
                             try {
                                 title = parser.getAttributeValue(MENU_RES_NAMESPACE, MENU_ATTR_TITLE);
                                 title = GApp.sInstance.getApplicationContext().getString(Integer.parseInt(title.substring(1)));
                             } catch (Exception ignore) {}
 
                             icon = parser.getAttributeResourceValue(MENU_RES_NAMESPACE,
                                     MENU_ATTR_ICON, 0);
 
                             if (itemId == 0) {
                                 break;
                             }
 
                             showAsAction = parser.getAttributeIntValue(MENU_RES_NAMESPACE,
                                     MENU_ATTR_SHOW_AS_ACTION, -1);
 
                             if (parentItemId != 0 && !title.equals("")) {
                                 mSubMenuItems.get(parentItemId).add(new SubMenuItemInfo(itemId, icon, title));
                                 title = "";
                             }
 
                             if (showAsAction == MenuItem.SHOW_AS_ACTION_ALWAYS ||
                                     showAsAction == MenuItem.SHOW_AS_ACTION_IF_ROOM) {
                                 mActionItemIds.add(itemId);
                             }
                             break;
 
                         case XmlPullParser.END_TAG:
                             if (parser.getName().equals("menu")) {
                                 parentItemId = 0;
                             }
                             break;
 
                         case XmlPullParser.END_DOCUMENT:
                             eof = true;
                             break;
                     }
 
                     eventType = parser.next();
                 }
             } catch (XmlPullParserException e) {
                 throw new InflateException("Error inflating menu XML", e);
             } catch (IOException e) {
                 throw new InflateException("Error inflating menu XML", e);
             } finally {
                 if (parser != null) {
                     parser.close();
                 }
             }
 
             for (List<SubMenuItemInfo> infos : mSubMenuItems.values()) {
                 List<SubMenuItemInfo> reverse = new LinkedList<SubMenuItemInfo>();
                 for (ListIterator iterator = infos.listIterator(infos.size()); iterator.hasPrevious();) {
                     SubMenuItemInfo info = (SubMenuItemInfo) iterator.previous();
                     reverse.add(info);
                 }
 
                 infos = reverse;
             }
         }
 
     }
 }
