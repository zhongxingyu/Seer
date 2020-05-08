 package com.pate.diablo;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.pate.diablo.model.D3Application;
 import com.pate.diablo.model.DataModel;
 import com.pate.diablo.sectionlist.EntrySkillAdapter;
 import com.pate.diablo.sectionlist.Item;
 import com.viewpagerindicator.PageIndicator;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
 
 public class Main extends SherlockFragmentActivity {
     SpinnerAdapter  mSpinnerAdapter;
     ArrayList<Item> items = new ArrayList<Item>();
     EntrySkillAdapter listAdapter;
     String selectedClass;
     Spinner maxLevelSpinner;
     int maxLevel;
     ClassFragmentAdapter mAdapter;
     ViewPager mPager;
     PageIndicator mIndicator;
     int index;
     int GET_SKILL = 0;
     int REPLACE_SKILL = 1;
     int GET_RUNE = 2;
     int REPLACE_RUNE = 3;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
         BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.classes)));
 
         String fakeJson = "";
         String line;
         try {
             line = reader.readLine();
             while (line != null) {
                 fakeJson = fakeJson + line;
                 line = reader.readLine();
             }
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         D3Application.setDataModel(gson.fromJson(fakeJson, DataModel.class));
         
         if (savedInstanceState != null)
         {
             this.selectedClass = savedInstanceState.getString("selectedClass");
             this.maxLevel = savedInstanceState.getInt("maxLevel");
         }
         else
         {
             selectedClass = D3Application.dataModel.getClasses().get(0).getName();
             maxLevel = 60;
         }
 
         mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.levels, android.R.layout.simple_dropdown_item_1line);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         
         ActionBar.OnNavigationListener mNavigationCallback = new ActionBar.OnNavigationListener() {
             
             @Override
             public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                Log.i("ItemPosition", "" + itemPosition);
                 maxLevel = getResources().getIntArray(R.array.levels)[itemPosition];
                 Log.i("MaxLevel", "" + maxLevel);
                 mAdapter = new ClassFragmentAdapter(getSupportFragmentManager(), Main.this, selectedClass, maxLevel);
                 mPager.setAdapter(mAdapter);                    
                 return false;
             }
         };
         
         actionBar.setListNavigationCallbacks(mSpinnerAdapter, mNavigationCallback);
         
         mAdapter = new ClassFragmentAdapter(getSupportFragmentManager(), Main.this, selectedClass, maxLevel);
         
         mPager = (ViewPager) findViewById(R.id.main_pager);
         mPager.setAdapter(mAdapter);
         
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.main_indicator);
         indicator.setViewPager(mPager);
         indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
         mIndicator = indicator;
 
     }
     
     
 
     
 }
