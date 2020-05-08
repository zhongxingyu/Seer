 package net.anei.cadpage.preferences;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 
 import net.anei.cadpage.ManageParsers;
 import net.anei.cadpage.ManagePreferences;
 
 /**
  * Class coordinates everything that needs to be done about the location setting
  */
 public class LocationManager {
   
   private Preference descPreference;
   
   private List<String> locationList = new ArrayList<String>();
   private List<String> nameList = new ArrayList<String>();
   
   private OnPreferenceChangeListener listener = null;
   
   private static final Map<String,String> STATE_MAP = new HashMap<String,String>();
   static {
     STATE_MAP.put("AL", "ALABAMA");
     STATE_MAP.put("AK", "ALASKA");
     
     STATE_MAP.put("AZ", "ARIZONA");
     STATE_MAP.put("AR", "ARKANSAW");
     
     STATE_MAP.put("ID", "IDAHO");
     STATE_MAP.put("IL", "ILLINOIS");
     STATE_MAP.put("IN", "INDIANA");
     STATE_MAP.put("IA", "IOWA");
     
     STATE_MAP.put("ME", "MAINE");
     STATE_MAP.put("MD", "MARYLAND");
     STATE_MAP.put("MA", "MASSACHUSETTS");
     STATE_MAP.put("MI", "MICHIGAN");
     STATE_MAP.put("MN", "MINNESOTA");
     STATE_MAP.put("MS", "MISSISSIPPI");
     STATE_MAP.put("MO", "MISSOURI");
     STATE_MAP.put("MT", "MONTANA");
     
     STATE_MAP.put("NE", "NEBRASKA");
     STATE_MAP.put("NV", "NEVADA");
     STATE_MAP.put("NH", "NEW HAMPSHIRE");
     STATE_MAP.put("NJ", "NEW JERSY");
     STATE_MAP.put("NY", "NEW YORK");
     STATE_MAP.put("NC", "NORTH CAROLINA");
     STATE_MAP.put("ND", "NORTH DAKTA");
     
     STATE_MAP.put("VT", "VERMONT");
     STATE_MAP.put("VA", "VIRGINIA");
     
     STATE_MAP.put("WV", "WEST VIRGINIA");
     STATE_MAP.put("WI", "WISCONSIN");
   }
   
   // Special location code comparator that makes adjustements for the
   // difference between actual state order and state abbreviation order
   private static final Comparator<String> LOC_COMPARE = new Comparator<String>(){
     @Override
     public int compare(String str1, String str2) {
       str1 = convert(str1);
       str2 = convert(str2);
       return str1.compareTo(str2);
     }
 
     private String convert(String str) {
       if (str.equals("General")) return "";
       String pfx = str.substring(0,2);
       String state = STATE_MAP.get(pfx);
       if (state != null) str = state + str.substring(2);
       return str;
     }};
   
   public LocationManager(Context ctx, Preference descPreference) {
     
     this.descPreference = descPreference;
     
     for (String loc : ManagePreferences.location().split(",")) {
       locationList.add(loc);
       nameList.add(ManageParsers.getInstance().getLocName(loc));
     }
   }
   
   public void setOnPreferenceChangeListener(OnPreferenceChangeListener listener) {
     this.listener = listener;
   }
   
   /**
    * Called when the location is set with a item in the normal location tree
    * @param location location code
    * @param name readable location name
    */
   public void setNewLocation(String location, String name) {
     locationList.clear();
     locationList.add(location);
     nameList.clear();
     nameList.add(name);
     refresh();
   }
   
   
   /**
    * Called when the location is modified by checking or unchecking an item
    * in the multiple location code tree
    * @param checked true if item has been checked, false if unchecked
    * @param location location code 
    * @param name readable location name
    */
   public void adjustLocation(boolean checked, String location, String name) {
     
     // Look through the location list to identify where this location should be
     int ndx = 0;
     boolean found = false;
     for (; ndx<locationList.size(); ndx++) {
       int cmp = LOC_COMPARE.compare(locationList.get(ndx), location);
       found = (cmp == 0);
       if (cmp >= 0) break;
     }
     
     // If found status matches requested status, we have nothing to do.
     if (checked == found) return;
     
     // Otherwise either add or remove this location and name as requested
     if (checked) {
       locationList.add(ndx, location);
       nameList.add(ndx, name);
     } else {
       locationList.remove(ndx);
       nameList.remove(ndx);
     }
     
     // And update everything
     refresh();
   }
   
   // Return single location setting
   public String getLocSetting() {
     return (locationList.size() == 1 ? locationList.get(0) : "");
   }
 
   /**
    * Determine if location checkbox should be checked
    * @param location requested location
    * @param name readable location name
    * @return if this location is active
    */
   public boolean isSet(String location) {
     for (int ndx = 0; ndx < locationList.size(); ndx++) {
       if (location.equals(locationList.get(ndx))) return true;
     }
     return false;
   }
 
   // Update everything when location setting(s) change
   private void refresh() {
     
     // First rebuild the persistent preference setting
    if (locationList.isEmpty()) {
      locationList.add("General");
      nameList.add("Generic Location");
    }
     String newLocation;
     if (locationList.size() == 1) {
       newLocation = locationList.get(0);
     } else {
       StringBuilder sb = new StringBuilder();
       for (String loc : locationList) {
         if (sb.length() > 0) sb.append(",");
         sb.append(loc);
       }
       newLocation = sb.toString();
     }
     
     // Set preference and
     // call notification listener if there is one
     ManagePreferences.setLocation(newLocation);
     if (listener != null) {
       listener.onPreferenceChange(null, newLocation);
     }
     
     // Set the summary display on both location preference screens
     updateDisplay();
   }
 
   /**
    * Update summary display for all main location preference screens
    */
   public void updateDisplay() {
     StringBuilder sb = new StringBuilder();
     for (String name : nameList) {
       if (sb.length() > 0) sb.append('\n');
       sb.append(name);
     }
     
     descPreference.setSummary(sb.toString());
   }
 }
