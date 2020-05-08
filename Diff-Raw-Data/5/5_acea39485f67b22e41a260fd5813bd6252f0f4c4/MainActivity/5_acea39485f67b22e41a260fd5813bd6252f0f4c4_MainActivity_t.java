 package com.pennapps.apbro;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Locale;
 
 import android.R;
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.util.LogPrinter;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.TextView;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.net.URL;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 
     /**
      * The {@link android.support.v4.view.PagerAdapter} that will provide
      * fragments for each of the sections. We use a
      * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
      * will keep every loaded fragment in memory. If this becomes too memory
      * intensive, it may be best to switch to a
      * {@link android.support.v4.app.FragmentStatePagerAdapter}.
      */
     SectionsPagerAdapter mSectionsPagerAdapter;
 
     /**
      * The {@link ViewPager} that will host the section contents.
      */
     ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar.
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Create the adapter that will return a fragment for each of the three
         // primary sections of the app.
         mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
         // Set up the ViewPager with the sections adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mSectionsPagerAdapter);
 
         // When swiping between different sections, select the corresponding
         // tab. We can also use ActionBar.Tab#select() to do this if we have
         // a reference to the Tab.
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         // For each of the sections in the app, add a tab to the action bar.
         for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by
             // the adapter. Also specify this Activity object, which implements
             // the TabListener interface, as the callback (listener) for when
             // this tab is selected.
 
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mSectionsPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
         // TODO: fix this:
         File f = downloadData();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         // When the given tab is selected, switch to the corresponding page in
         // the ViewPager.
         mViewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     /**
      * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
      * one of the sections/tabs/pages.
      */
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             // getItem is called to instantiate the fragment for the given page.
             // Return a DummySectionFragment (defined as a static inner class
             // below) with the page number as its lone argument.
             Fragment fragment = new DummySectionFragment();
             Bundle args = new Bundle();
             args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             // Show 3 total pages.
             return 3;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             Locale l = Locale.getDefault();
             switch (position) {
                 case 0:
                     return getString(R.string.title_section1).toUpperCase(l);
                 case 1:
                     return getString(R.string.title_section2).toUpperCase(l);
                 case 2:
                     return getString(R.string.title_section3).toUpperCase(l);
                 default:
                 	return null;
             }
         }
     }
 
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     public static class DummySectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public DummySectionFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
             int section = getArguments().getInt(ARG_SECTION_NUMBER);
 
             switch (section){
                 case 1:
                     View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
                     TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
                     // TODO: implement
                     dummyTextView.setText("stats go here");
                     return rootView;
 
                 case 2:
                     View calView = inflater.inflate(R.layout.fragment_calendar, container, false);
                     WebView cal = (WebView) calView.findViewById(R.id.my_webview);
                     cal.loadUrl("https://dl.dropboxusercontent.com/u/59394702/APO%20html/PREZCAL.HTML");
                     return calView;
 
                 case 3:
                     View rootView3 = inflater.inflate(R.layout.fragment_main_dummy, container, false);
                     TextView dummyTextView3 = (TextView) rootView3.findViewById(R.id.section_label);
                     // TODO: implement
                     dummyTextView3.setText("facebook group goes here");
                     return rootView3;
                 default:
                 	return inflater.inflate(R.layout.fragment_main_dummy, container, false);
             }
         }
     }
 
     public File downloadData() {
         try {
             // Retrieve data via HTML GET
             DefaultHttpClient httpClient = new DefaultHttpClient();
             HttpResponse response =
             	httpClient.execute(new HttpGet(getString(R.string.main_database_URL)));
             BufferedHttpEntity buf = new BufferedHttpEntity(response.getEntity());
             // Create reader for HTML data
            InputStreamReader inputStreamReader = new InputStreamReader(buf.getContent());
             BufferedReader reader = new BufferedReader(inputStreamReader);
             // Create writer for file output
             FileOutputStream fileOutputStream =
             		openFileOutput(getString(R.string.temp_database_filename), MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
             
             // Read HTML post and write to file.
             String line;
             while ((line = reader.readLine()) != null) {
                 writer.write(line + "\n");
             }
             
             // Close all buffers and streams.
             reader.close();
             inputStreamReader.close();
             writer.close();
             fileOutputStream.close();
             
             // We're done. Return the new file.
             return new File(getString(R.string.temp_database_filename));
         }
         catch (IOException ioe){
             ioe.printStackTrace();
         }
         return null;
     }
 }
