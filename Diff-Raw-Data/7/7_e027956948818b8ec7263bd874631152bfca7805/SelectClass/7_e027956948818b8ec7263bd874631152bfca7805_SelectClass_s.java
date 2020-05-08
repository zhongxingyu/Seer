 
 package com.wemakestuff.d3builder;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.pate.diablo.R;
import com.pate.diablo.string.Replacer;
import com.pate.diablo.string.Vars;
 import com.viewpagerindicator.PageIndicator;
 import com.viewpagerindicator.TitlePageIndicator;
 import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
 import com.wemakestuff.d3builder.model.ClassBuild;
 import com.wemakestuff.d3builder.model.D3Application;
 import com.wemakestuff.d3builder.model.DataModel;
 
 public class SelectClass extends SherlockFragmentActivity
 {
 
     private ClassFragmentAdapter mAdapter;
     private ViewPager            mPager;
     private PageIndicator        mIndicator;
     // private SpinnerAdapter mSpinnerAdapter;
     private static int           maxLevel      = 60;
     private String               loadFromUrl;
     private boolean              loadedFromUrl = false;
     private String               value;
     private Dialog               dialog;
     private ClassBuildAdapter    aAdapter;
     private TextView             requiredLevel;
     private LinearLayout         requiredLevelWrapper;
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
 
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     protected void onResume()
     {
 
         super.onResume();
         Log.i("SelectClass", "OnResume");
         Uri data = getIntent().getData();
 
         if (data != null)
         {
             loadFromUrl = data.toString();
             Log.i("loadFromUrl", loadFromUrl == null ? "Null" : loadFromUrl);
         }
     }
 
     public void loadClassFromUrl(String url)
     {
 
         Pattern p = Pattern.compile("^http://.*/calculator/(.*)#.*$");
         Matcher m = p.matcher(url);
 
         ClassListFragment frag = null;
 
         while (m.find())
         {
             if (m.groupCount() >= 1)
             {
                 int position = mAdapter.getItemPosition(m.group(1).replace("-", " "));
                 Log.i("LoadClassFromUrl Position", "" + position);
                 frag = (ClassListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + position);
 
                 Log.i("loadClassFromUrl", url);
                 if (frag != null)
                 {
                     mAdapter.setOnLoadFragmentsCompleteListener(null);
                     frag.delinkifyClassBuild(url);
                     mPager.setCurrentItem(position);
                     setRequiredLevel(frag.getMaxLevel());
                 }
             }
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 
         if (item.getItemId() == R.id.share)
         {
             ClassListFragment frag = (ClassListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
             Log.i("linkTest", frag.linkifyClassBuild());
             Intent intent = new Intent(Intent.ACTION_SEND);
             intent.setType("text/plain");
             intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this D3 build");
             intent.putExtra(android.content.Intent.EXTRA_TEXT, frag.linkifyClassBuild());
             startActivity(Intent.createChooser(intent, "Share using"));
         }
         else if (item.getItemId() == R.id.load)
         {
             // loadClassFromUrl("http://us.battle.net/d3/en/calculator/monk#aZYdfT!aZb!aaaaaa");
             // SharedPreferences keyVals =
             // this.getApplicationContext().getSharedPreferences("saved_build_list",
             // MODE_PRIVATE);
             // loadClassFromUrl(keyVals.getString("test",
             // "http://us.battle.net/d3/en/calculator/barbarian#......!...!......"));
 
             builds = new ArrayList<ClassBuild>();
 
             SharedPreferences valVals = getSharedPreferences("saved_build_value", MODE_PRIVATE);
             SharedPreferences clssVals = getSharedPreferences("saved_build_class", MODE_PRIVATE);
 
             Map<String, String> urls = (Map<String, String>) valVals.getAll();
             Map<String, String> classes = (Map<String, String>) clssVals.getAll();
 
             for (Map.Entry<String, String> pairs : urls.entrySet())
             {
                 if (classes.containsKey(pairs.getKey()))
                 {
                     builds.add(new ClassBuild(pairs.getKey(), classes.get(pairs.getKey()), pairs.getValue()));
                     Log.i("loadBuilds-list", "N: " + pairs.getKey() + " C: " + classes.get(pairs.getKey()) + " U: " + pairs.getValue());
                 }
             }
 
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle("Load Saved Build");
 
             final ListView buildList = new ListView(this);
 
             OnLoadBuildClickListener loadBuildListener = new OnLoadBuildClickListener()
             {
 
                 @Override
                 public void onLoadBuildClick(ClassBuild build)
                 {
 
                     loadClassFromUrl(build.getUrl());
                 }
                 
                 @Override
                 public void onLoadBuildDismiss()
                 {
                     dialog.dismiss();
                 }
                 
                 @Override
                 public void onDeleteBuild(ClassBuild build)
                 {
                     builds.remove(build);
                     aAdapter.notifyDataSetChanged();
                 }
             };
 
             aAdapter = new ClassBuildAdapter(builds, this, loadBuildListener);
             buildList.setAdapter(aAdapter);
             builder.setView(buildList);
 
             dialog = builder.create();
 
             dialog.show();
 
         }
         else if (item.getItemId() == R.id.save)
         {
             final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
             alert.setTitle("Save Current Build As...");
             alert.setMessage("Please Enter a Name to use to Save the Current Build.");
 
             // Set an EditText view to get user input
             final EditText input = new EditText(this);
             alert.setView(input);
 
             alert.setPositiveButton("Save", new DialogInterface.OnClickListener()
             {
 
                 public void onClick(DialogInterface dialog, int whichButton)
                 {
 
                     value = input.getText().toString();
                 }
             });
 
             alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
             {
 
                 public void onClick(DialogInterface dialog, int whichButton)
                 {
 
                     value = "";
                 }
             });
 
             alert.show();
 
             SharedPreferences valVals = getSharedPreferences("saved_build_value", MODE_PRIVATE);
             SharedPreferences.Editor valEdit = valVals.edit();
             SharedPreferences clssVals = getSharedPreferences("saved_build_class", MODE_PRIVATE);
             SharedPreferences.Editor clssEdit = clssVals.edit();
 
             while (valVals.contains(value) || clssVals.contains(value))
             {
                 Toast toast = Toast.makeText(this, "A Build is Already Saved as: " + value + ". Enter a New Name.", Toast.LENGTH_SHORT);
                 toast.show();
             }
 
             if (value != null && !(value.length() == 0))
             {
                 ClassListFragment frag = (ClassListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                 valEdit.putString(value, frag.linkifyClassBuild());
                 clssEdit.putString(value, frag.getSelectedClass());
 
                 valEdit.commit();
                 clssEdit.commit();
             }
         }
         else if (item.getItemId() == R.id.clear)
         {
             DialogFragment newFragment = MyAlertDialogFragment.newInstance(R.string.alert_dialog_title);
             newFragment.show(getSupportFragmentManager(), "dialog");
         }
         return super.onOptionsItemSelected(item);
     }
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 
         super.onCreate(savedInstanceState);
 
         Uri data = getIntent().getData();
         if (data != null)
         {
             loadFromUrl = data.toString();
             Log.i("OnCreate loadFromUrl", loadFromUrl == null ? "Null" : loadFromUrl);
         }
 
         if (D3Application.dataModel == null)
         {
             Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
             BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.classes)));
 
             String fakeJson = "";
             String line;
             try
             {
                 line = reader.readLine();
                 while (line != null)
                 {
                     fakeJson = fakeJson + line;
                     line = reader.readLine();
                 }
             }
             catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
             D3Application.setDataModel(gson.fromJson(fakeJson, DataModel.class));
         }
 
         setContentView(R.layout.select_skill);
         
         requiredLevelWrapper = (LinearLayout) findViewById(R.id.required_level_wrapper);
         requiredLevel = (TextView) findViewById(R.id.required_level);
         setRequiredLevel(1);
         
         if (savedInstanceState != null)
         {
             // if (savedInstanceState.containsKey("MaxLevel"))
             // {
             // maxLevel = savedInstanceState.getInt("MaxLevel");
             // }
         }
         else
         {
             // maxLevel = 60;
         }
 
         AdView adView = (AdView) this.findViewById(R.id.adView);
         AdRequest newAd = new AdRequest();
         newAd.addTestDevice("BDD7A55C1502190E502F14CBFDF9ABC7");
         newAd.addTestDevice("E85A995C749AE015AA4EE195878C0982");
         newAd.addTestDevice("E9BD79A28E313B2BDFA0CB0AED6C9697");
         adView.loadAd(newAd);
 
         // mSpinnerAdapter = ArrayAdapter.createFromResource(this,
         // R.array.levels, android.R.layout.simple_dropdown_item_1line);
         //
         // ActionBar actionBar = getSupportActionBar();
         //
         // actionBar.setDisplayShowTitleEnabled(false);
         // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
         //
         // ActionBar.OnNavigationListener mNavigationCallback = new
         // ActionBar.OnNavigationListener()
         // {
         //
         // @Override
         // public boolean onNavigationItemSelected(int itemPosition, long
         // itemId)
         // {
         //
         // maxLevel = 60 - itemPosition;
         // Log.i("MaxLevel", "" + maxLevel);
         // return true;
         // }
         // };
         //
         // actionBar.setListNavigationCallbacks(mSpinnerAdapter,
         // mNavigationCallback);
         // actionBar.setSelectedNavigationItem(60 - maxLevel);
 
         OnLoadFragmentsCompleteListener listener = new OnLoadFragmentsCompleteListener()
         {
 
             public void OnLoadFragmentsComplete(String className)
             {
 
                 if (!loadedFromUrl && loadFromUrl != null)
                 {
                     Log.i("SelectClass OnCreate", "Loading from url: " + loadFromUrl);
 
                     Pattern p = Pattern.compile("^http://.*/calculator/(.*)#.*$");
                     Matcher m = p.matcher(loadFromUrl);
 
                     while (m.find())
                     {
                         if (m.groupCount() >= 1)
                         {
                             int position = mAdapter.getItemPosition(m.group(1).replace("-", " "));
                             if (position > 0 && className.equalsIgnoreCase(m.group(1).replace("-", " ")))
                             {
                                 loadedFromUrl = true;
                                 loadClassFromUrl(loadFromUrl);
                             }
                             else if (position == 0 && !className.equalsIgnoreCase(m.group(1).replace("-", " ")))
                             {
                                 loadedFromUrl = true;
                                 loadClassFromUrl(loadFromUrl);
                             }
                         }
                     }
                 }
                 else
                 {
                     Log.i("SelectClass OnCreate", "Load from URL was null");
                 }
             }
         };
 
         mAdapter = new ClassFragmentAdapter(getSupportFragmentManager(), SelectClass.this, listener);
 
         mPager = (ViewPager) findViewById(R.id.pager);
         mPager.setOffscreenPageLimit(5);
         mPager.setAdapter(mAdapter);
 
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.select_skill_indicator);
         indicator.setViewPager(mPager);
         indicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
         
         indicator.setOnPageChangeListener(new OnPageChangeListener() {
             
             @Override
             public void onPageSelected(int position) {
                 ClassListFragment frag = (ClassListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
                 setRequiredLevel(frag.getMaxLevel());
             }
             
             @Override
             public void onPageScrolled(int arg0, float arg1, int arg2) {
                 // TODO Auto-generated method stub
                 
             }
             
             @Override
             public void onPageScrollStateChanged(int arg0) {
                 // TODO Auto-generated method stub
                 
             }
         });
         
         mIndicator = indicator;
 
     }
     
     public void setRequiredLevel(int level)
     {
         requiredLevel.setText(Replacer.replace("Required Level: " + level, "\\d+", Vars.DIABLO_GREEN));
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState)
     {
 
         // outState.putInt("MaxLevel", maxLevel);
         super.onSaveInstanceState(outState);
     }
 
     public void doPositiveClick()
     {
 
         ClassListFragment frag = (ClassListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + mPager.getCurrentItem());
         frag.clear();
     }
 
     public void doNegativeClick()
     {
 
         Log.i("FragmentAlertDialog", "Negative click!");
     }
 
     public static class MyAlertDialogFragment extends DialogFragment
     {
 
         public static MyAlertDialogFragment newInstance(int title)
         {
 
             MyAlertDialogFragment frag = new MyAlertDialogFragment();
             Bundle args = new Bundle();
             args.putInt("title", title);
             frag.setArguments(args);
             return frag;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState)
         {
 
             int title = getArguments().getInt("title");
 
             return new AlertDialog.Builder(getActivity()).setTitle(title).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
             {
 
                 public void onClick(DialogInterface dialog, int whichButton)
                 {
 
                     ((SelectClass) getActivity()).doPositiveClick();
                 }
             }).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener()
             {
 
                 public void onClick(DialogInterface dialog, int whichButton)
                 {
 
                     ((SelectClass) getActivity()).doNegativeClick();
                 }
             }).create();
         }
     }
 
 }
