 /* Copyright (c) 2012 Pierre LEVY androidsoft.org
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.androidsoft.opendata.remarkabletrees.ui.activity;
 
 import android.content.Intent;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TextView;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.ViewById;
 import org.androidsoft.opendata.remarkabletrees.Constants;
 import org.androidsoft.opendata.remarkabletrees.R;
 import org.androidsoft.opendata.remarkabletrees.model.RemarkableTree;
 import org.androidsoft.opendata.remarkabletrees.service.TreesService;
 import org.androidsoft.opendata.remarkabletrees.ui.adapter.TabsAdapter;
 import org.androidsoft.opendata.remarkabletrees.ui.fragment.TreeDataFragment;
 import org.androidsoft.opendata.remarkabletrees.ui.fragment.TreeDescriptionFragment;
 
 /**
  * Tree Activity
  *
  * @author Pierre LEVY
  */
 @EActivity(R.layout.tree_activity)
 public class TreeActivity extends FragmentActivity
 {
 
     @ViewById(android.R.id.tabhost)
     TabHost mTabHost;
     @ViewById(R.id.title)
     TextView mTitle;
     @ViewById(R.id.address)
     TextView mAddress;
     @ViewById(R.id.button_show_on_map)
     View mButtonShowOnMap;
     @Extra(Constants.TREE_ID)
     int mTreeId;
     ViewPager mViewPager;
     private TabsAdapter mTabsAdapter;
 
     @AfterViews
     void initUI()
     {
         if (mTabHost != null)
         {
             mTabHost.setup();
 
             mViewPager = (ViewPager) findViewById(R.id.pager);
             mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
 
            mTabsAdapter.addTab(mTabHost.newTabSpec("data").setIndicator("Caractéristiques"),
                     TreeDataFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("description").setIndicator("Description"),
                     TreeDescriptionFragment.class, null);
         }
 
         RemarkableTree arbre = TreesService.instance().getTree(this, mTreeId);
 
         mTitle.setText(arbre.getNomCommun());
         mAddress.setText(arbre.getEspaceVert());
     }
 
     public int getTreeId()
     {
         return mTreeId;
     }
 
     @Click(R.id.button_show_on_map)
     public void onShowOnMap()
     {
         RemarkableTree tree = TreesService.instance().getTree(this, mTreeId);
         Intent intent = new Intent(Constants.ACTION_DISPLAY_POI_MAP);
         intent.putExtra(TreesMapActivity.EXTRA_POINT_LAT, tree.getLatitude());
         intent.putExtra(TreesMapActivity.EXTRA_POINT_LON, tree.getLongitude());
         startActivity(intent);
     }
 
     @OptionsItem(android.R.id.home)
     public void onHome()
     {
         Intent intent = new Intent( Constants.ACTION_DASHBOARD );
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
     }
 }
