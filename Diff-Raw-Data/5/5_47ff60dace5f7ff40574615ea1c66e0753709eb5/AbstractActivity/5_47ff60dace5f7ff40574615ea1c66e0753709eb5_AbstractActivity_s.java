 package fr.ybo.ybotv.android;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import fr.ybo.ybotv.android.util.ArraysUtil;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class AbstractActivity extends SherlockActivity implements ActionBar.OnNavigationListener {
 
     private static final String TAG = "ybo-tv-android";
 
     private final static Map<Integer, Class<? extends AbstractActivity>> mapOfActivity = new HashMap<Integer, Class<? extends AbstractActivity>>(){{
         put(R.id.menu_now, NowActivity.class);
         put(R.id.menu_cesoir, CeSoirActivity.class);
         put(R.id.menu_parchaine, ParChaineActivity.class);
     }};
 
     private int[] menuIds;
 
     public void createMenu() {
 
         menuIds = ArraysUtil.getIdsArray(this, R.array.menu_principal_ids);
 
         Context context = getSupportActionBar().getThemedContext();
         ArrayAdapter<CharSequence> listMenu = ArrayAdapter.createFromResource(context, R.array.menu_principal_chaines, R.layout.sherlock_spinner_item);
         listMenu.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
         getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         getSupportActionBar().setListNavigationCallbacks(listMenu, this);
         getSupportActionBar().setSelectedNavigationItem(getItemPositionForCurrentClass());
         getSupportActionBar().setDisplayShowTitleEnabled(false);
     }
 
 
     @Override
     public boolean onNavigationItemSelected(int itemPosition, long itemId) {
         startActivityIfNotAlreadyIn(mapOfActivity.get(menuIds[itemPosition]));
         return true;
     }
 
     protected abstract int getMenuIdOfClass();
 
     protected int getItemPositionForCurrentClass() {
         for (int index = 0; index < menuIds.length; index++) {
             if (menuIds[index] == getMenuIdOfClass()) {
                 return index;
             }
         }
         return -1;
     }
 
     protected void startActivityIfNotAlreadyIn(Class<? extends AbstractActivity> activity) {
         if (this.getClass() != activity) {
             startActivity(new Intent(this, activity));
         }
     }
 
 
 }
