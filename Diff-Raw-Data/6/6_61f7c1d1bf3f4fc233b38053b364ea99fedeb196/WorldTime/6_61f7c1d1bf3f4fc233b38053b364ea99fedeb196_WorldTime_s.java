 package com.matiaspelenur.worldtime;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.ExpandableListView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Sets;
 
 public class WorldTime extends Activity {
 
   private BroadcastReceiver timeTickReceiver;
   private ListView mainListView;
   private ExpandableListView timeZonesListView;
   private ViewSwitcher switcher;
   
   private Set<String> enabledCities = Sets.newHashSet();
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     
     mainListView = new ListView(this);
     timeZonesListView = new ExpandableListView(this);
     timeZonesListView.setTextFilterEnabled(true);
     
     switcher = new ViewSwitcher(this);
     switcher.addView(mainListView);
     switcher.addView(timeZonesListView);
     
     setContentView(switcher);
     
     SharedPreferences prefs = getPreferences(MODE_PRIVATE);
     String storedEnabledCities = prefs.getString("enabledCities", "");
     if (storedEnabledCities.length() > 0) {
       enabledCities.addAll(Arrays.asList(storedEnabledCities.split(",")));
     }
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.options_menu, menu);
     return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     if (timeZonesListView.getAdapter() == null) {
       timeZonesListView.setAdapter(new RegionCityListAdapter());
     }
     switcher.showNext();
     updateTimes();
     return true;
   }
 
   public class RegionCityListAdapter extends BaseExpandableListAdapter {
 
     // TODO(matias): let the user enable or disable regions
     private final List<String> enabledRegions = ImmutableList.of(
         "America", "Europe", "Africa", "Indian", "Pacific");
 
     private ListMultimap<String,String> regionCityMap;
     
     public RegionCityListAdapter() {
       populateRegionCityMap();
     }
     
     private void populateRegionCityMap() {
       regionCityMap = ArrayListMultimap.create();
       Pattern tzPattern = Pattern.compile("(\\w+)/(\\w+)");
       for (String tz : TimeZone.getAvailableIDs()) {
         Matcher match = tzPattern.matcher(tz);
         if (match.matches()) {
           String region = match.group(1);
           if (enabledRegions.contains(region)) {
             String city = match.group(2);
             regionCityMap.put(region, city);
           }
         }
       }
     }
     
     @Override
     public Object getChild(int groupPosition, int childPosition) {
       String region = enabledRegions.get(groupPosition);
       String city = regionCityMap.get(region).get(childPosition);
       return city;
     }
 
     @Override
     public long getChildId(int groupPosition, int childPosition) {
       return childPosition;
     }
 
     @Override
     public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
         View convertView, ViewGroup parent) {
       String region = enabledRegions.get(groupPosition);
       String city = regionCityMap.get(region).get(childPosition);
       final String fullName = region + "/" + city;
       
       LinearLayout layout = new LinearLayout(WorldTime.this);
       TextView textView = new TextView(WorldTime.this);
       textView.setText(getChild(groupPosition, childPosition).toString());
       
       CheckBox checkbox = new CheckBox(WorldTime.this);
       checkbox.setChecked(enabledCities.contains(fullName));
       checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           if (isChecked) {
             enabledCities.add(fullName);
           } else {
             enabledCities.remove(fullName);
           }
         }
       });
       layout.addView(checkbox);
       layout.addView(textView);
       return layout;
     }
 
     @Override
     public int getChildrenCount(int groupPosition) {
      return regionCityMap.size();
     }
 
     @Override
     public Object getGroup(int groupPosition) {
       return enabledRegions.get(groupPosition);
     }
 
     @Override
     public int getGroupCount() {
       return enabledRegions.size();
     }
 
     @Override
     public long getGroupId(int groupPosition) {
       return groupPosition;
     }
 
     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
         ViewGroup parent) {
       TextView textView = new TextView(WorldTime.this);
       textView.setText(getGroup(groupPosition).toString());
       textView.setPadding(36, 10, 0, 10);
       return textView;
     }
 
     @Override
     public boolean hasStableIds() {
       return true;
     }
 
     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
       return false;
    }
    
   }
 
   @Override
   protected void onResume() {
     super.onResume();
     timeTickReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
         updateTimes();
       }
     };
     this.registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
     updateTimes();
   }
 
   @Override
   protected void onPause() {
     SharedPreferences prefs = getPreferences(MODE_PRIVATE);
     prefs.edit().putString("enabledCities", Joiner.on(",").join(enabledCities));
     prefs.edit().commit();
     
     this.unregisterReceiver(timeTickReceiver);
     super.onPause();
   }
 
   private void updateTimes() {
     mainListView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, getTimes()));
   }
 
   private List<String> getTimes() {
     Date now = new Date();
     Pattern tzPattern = Pattern.compile("/(\\w+)");
     List<String> times = new ArrayList<String>();
     for (String tzName : enabledCities) {
       TimeZone tz = TimeZone.getTimeZone(tzName);
       if (tz != null) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
         dateFormat.setTimeZone(tz);
         Matcher matcher = tzPattern.matcher(tzName);
         matcher.find();
         times.add(dateFormat.format(now) + " in " + matcher.group(1).replace("_", " "));
       }
     }
     return times;
   }
 }
