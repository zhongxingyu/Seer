 package android.support.compatibility;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.support.GApp;
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.SearchView;
import com.compatibility.R;
 import android.support.fragments.TabListener;
 
 /**
  * An extension of {@link ActionBarHelper} that provides Android 3.0-specific functionality for
  * Honeycomb tablets. It thus requires API level 11.
  */
 public class ActionBarHelperHoneycomb extends ActionBarHelper {
     private Menu mOptionsMenu;
     private View mRefreshIndeterminateProgressView = null;
 
     protected ActionBarHelperHoneycomb(Activity activity) {
         super(activity);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         mOptionsMenu = menu;
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public void setRefreshActionItemState(boolean refreshing) {
         // On Honeycomb, we can set the state of the refresh button by giving it a custom
         // action view.
         if (mOptionsMenu == null) {
             return;
         }
 
         final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
         if (refreshItem != null) {
             if (refreshing) {
                 if (mRefreshIndeterminateProgressView == null) {
                     LayoutInflater inflater = (LayoutInflater)
                             getActionBarThemedContext().getSystemService(
                                     Context.LAYOUT_INFLATER_SERVICE);
                     mRefreshIndeterminateProgressView = inflater.inflate(
                             R.layout.actionbar_indeterminate_progress, null);
                 }
 
                 refreshItem.setActionView(mRefreshIndeterminateProgressView);
             } else {
                 refreshItem.setActionView(null);
             }
         }
     }
 
     @Override
     public void addActionBarTab(String label, Fragment fragment) {
         ActionBar actionBar = mActivity.getActionBar();
         if (actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
             actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
         }
 
         actionBar.addTab(actionBar.newTab()
                 .setText(label)
                 .setTabListener(new TabListener(((FragmentActivity) mActivity).getSupportFragmentManager(), fragment)));
     }
 
     @Override
     public int getSelectedTab() {
         return mActivity.getActionBar().getSelectedTab().getPosition();
     }
 
     @Override
     public void createSearchMenuItem(final SearchViewQueryListener listener) {
         MenuItem item = mOptionsMenu.add(GApp.sInstance.getResources().getString(R.string.menu_search));
         item.setIcon(android.R.drawable.ic_menu_search);
         item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         SearchView sv = new SearchView(mActivity);
         sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextChange(String newText) {
                 return listener.onQueryTextChange(newText);
             }
 
             @Override
             public boolean onQueryTextSubmit(String query) {
                 // Don't care about this.
                 return true;
             }
         });
         item.setActionView(sv);
     }
 
     /**
      * Returns a {@link android.content.Context} suitable for inflating layouts for the action bar. The
      * implementation for this method in {@link android.support.compatibility.ActionBarHelperICS} asks the action bar for a
      * themed context.
      */
     protected Context getActionBarThemedContext() {
         return mActivity;
     }
 }
