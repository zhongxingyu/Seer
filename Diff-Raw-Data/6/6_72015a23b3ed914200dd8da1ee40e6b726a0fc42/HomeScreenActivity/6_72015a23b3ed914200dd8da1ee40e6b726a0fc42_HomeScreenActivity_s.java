 /*
  * Copyright (c) 2013. Arnav Gupta
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License, version 3, as
  *     published by
  *     the Free Software Foundation.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package in.ac.iiitd.esya;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import in.ac.iiitd.esya.fragments.EventDialog;
 import in.ac.iiitd.esya.tabfragments.EsyaTabFragment;
 import in.ac.iiitd.esya.tabfragments.ScheduleTabFragment;
 import in.ac.iiitd.esya.utils.PagerAdapter;
 
 
 /**
  * The <code>TabsViewPagerFragmentActivity</code> class implements the Fragment activity
  * that maintains a TabHost using a ViewPager.
  */
 public class HomeScreenActivity extends FragmentActivity implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
 
     public static String PACKAGE_NAME;
     private TabHost mTabHost;
     private ViewPager mViewPager;
     private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, HomeScreenActivity.TabInfo>();
     private PagerAdapter mPagerAdapter;
     /**
      *
      * Maintains extrinsic info of a tab's construct
      */
     private class TabInfo {
         private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
         TabInfo(String tag, Class<?> clazz, Bundle args) {
             this.tag = tag;
             this.clss = clazz;
             this.args = args;
         }
 
     }
     /**
      * A simple factory that returns dummy views to the Tabhost
      * @author mwho
      */
     class TabFactory implements TabContentFactory {
 
         private final Context mContext;
 
         /**
          * @param context
          */
         public TabFactory(Context context) {
             mContext = context;
         }
 
         /** (non-Javadoc)
          * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
          */
         public View createTabContent(String tag) {
             View v = new View(mContext);
             v.setMinimumWidth(0);
             v.setMinimumHeight(0);
             return v;
         }
 
     }
     /** (non-Javadoc)
      * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
      */
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Inflate the layout
         setContentView(R.layout.activity_homescreen);
         // Initialise the TabHost
         this.initialiseTabHost(savedInstanceState);
         if (savedInstanceState != null) {
             mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
         }
         // Intialise ViewPager
         this.intialiseViewPager();
         PACKAGE_NAME = getApplicationContext().getPackageName();
     }
 
     /** (non-Javadoc)
      * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
      */
     protected void onSaveInstanceState(Bundle outState) {
         outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
         super.onSaveInstanceState(outState);
     }
 
     /**
      * Initialise ViewPager
      */
     private void intialiseViewPager() {
 
         List<Fragment> fragments = new Vector<Fragment>();
         fragments.add(Fragment.instantiate(this, EsyaTabFragment.class.getName()));
         fragments.add(Fragment.instantiate(this, ScheduleTabFragment.class.getName()));
         this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
         //
         this.mViewPager = (ViewPager)super.findViewById(R.id.viewpager);
         this.mViewPager.setAdapter(this.mPagerAdapter);
         this.mViewPager.setOnPageChangeListener(this);
     }
 
     /**
      * Initialise the Tab Host
      */
     private void initialiseTabHost(Bundle args) {
         mTabHost = (TabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup();
         TabInfo tabInfo = null;
         HomeScreenActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab1").setIndicator("Esya"), (tabInfo = new TabInfo("Tab1", EsyaTabFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         HomeScreenActivity.AddTab(this, this.mTabHost, this.mTabHost.newTabSpec("Tab2").setIndicator("Schedule"), (tabInfo = new TabInfo("Tab2", ScheduleTabFragment.class, args)));
         this.mapTabInfo.put(tabInfo.tag, tabInfo);
         // Default to first tab
         //this.onTabChanged("Tab1");
         //
         mTabHost.setOnTabChangedListener(this);
     }
 
     private static void AddTab(HomeScreenActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
         // Attach a Tab view factory to the spec
         tabSpec.setContent(activity.new TabFactory(activity));
         tabHost.addTab(tabSpec);
     }
 
     /** (non-Javadoc)
      * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
      */
     public void onTabChanged(String tag) {
         //TabInfo newTab = this.mapTabInfo.get(tag);
         int pos = this.mTabHost.getCurrentTab();
        this.mViewPager.setCurrentItem(pos);
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
      */
     @Override
     public void onPageScrolled(int position, float positionOffset,
                                int positionOffsetPixels) {
         // TODO Auto-generated method stub
 
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
      */
     @Override
     public void onPageSelected(int position) {
         // TODO Auto-generated method stub
         this.mTabHost.setCurrentTab(position);
     }
 
     /* (non-Javadoc)
      * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
      */
     @Override
     public void onPageScrollStateChanged(int state) {
         // TODO Auto-generated method stub
 
     }
 
     //Helper Method to launch event Dialogs//
     public void eventDialog( String eventName ) {
         eventName = "event_dialog_" + eventName;
         int layoutId = getResources().getIdentifier(eventName, "layout", HomeScreenActivity.PACKAGE_NAME);
         FragmentManager fm = getSupportFragmentManager();
         DialogFragment newFragment = new EventDialog(layoutId);
         newFragment.show(fm, "EventDialog");
     }
 
     /* All the event dialog launchers */
     public void bigbang (View v) { eventDialog("bigbang"); }
     public void blooddonate (View v) { eventDialog("blooddonate"); }
     public void brainfuzz (View v) { eventDialog("brainfuzz"); }
     public void chakravyuha (View v) { eventDialog("chakravyuha"); }
     public void codeinless (View v) { eventDialog("codeinless"); }
     public void crossword (View v) { eventDialog("crossword"); }
     public void foqs (View v) { eventDialog("foqs"); }
     public void huntit (View v) { eventDialog("huntit"); }
     public void metromix (View v) { eventDialog("metromix"); }
     public void eventnames (View v) { eventDialog("eventnames"); }
     public void overnighackthon (View v) { eventDialog("overnighackthon"); }
     public void pool (View v) { eventDialog("pool"); }
     public void prayatna (View v) { eventDialog("prayatna"); }
     public void proconjunior (View v) { eventDialog("proconjunior"); }
     public void procon (View v) { eventDialog("procon"); }
     public void pwned (View v) { eventDialog("pwned"); }
     public void rebuttal (View v) { eventDialog("rebuttal"); }
     public void robocon (View v) { eventDialog("robocon"); }
     public void segfault (View v) { eventDialog("segfault"); }
     public void seminarseries (View v) { eventDialog("seminarseries"); }
     public void sudoku (View v) { eventDialog("sudoku"); }
     public void systemskills (View v) { eventDialog("systemskills"); }
     public void techathlon (View v) { eventDialog("techathlon"); }
     public void technicaltambola (View v) { eventDialog("technicaltambola"); }
     public void toasttocode (View v) { eventDialog("toasttocode"); }
     public void videodubbing (View v) { eventDialog("videodubbing"); }
     public void wordtussle (View v) { eventDialog("wordtussle"); }
     public void xquizit (View v) { eventDialog("xquizit"); }
 }
