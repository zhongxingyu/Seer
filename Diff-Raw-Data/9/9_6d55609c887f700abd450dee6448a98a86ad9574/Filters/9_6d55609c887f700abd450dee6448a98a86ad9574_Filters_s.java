 package eu.fiveminutes.cipele46.app;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import eu.fiveminutes.cipele46.R;
 import eu.fiveminutes.cipele46.model.AdType;
 import eu.fiveminutes.cipele46.model.Category;
 import eu.fiveminutes.cipele46.model.District;
 
 public class Filters {
 
 	public static void firstTimeInitIfNeeded(Context c) {
 		
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		String adTypeFilter = sp.getString(c.getString(R.string.preference_adtype), null);
 		if (adTypeFilter != null) {
 			return;
 		}
 		
 		Editor e = sp.edit();
 		e.putString(c.getString(R.string.preference_adtype), AdType.DEMAND.name());
 		e.putLong(c.getString(R.string.preference_category_id), -1);
 		e.putLong(c.getString(R.string.preference_district_id), -1);
 		e.putString(c.getString(R.string.preference_category_name), "Sve kategorije");
		e.putString(c.getString(R.string.preference_district_name), "Sve upanije");
 		e.commit();		
 	}
 	
 	public static void setAdTypeFilter(Context c, AdType adType) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		
 		Editor e = sp.edit();
 		e.putString(c.getString(R.string.preference_adtype), adType.name());
 		e.commit();		
 	}
 	
 	public static void setCategoryFilter(Context c, Category category) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		
 		Editor e = sp.edit();
 		e.putLong(c.getString(R.string.preference_category_id), category.getId());
 		e.putString(c.getString(R.string.preference_category_name), category.getName());
 		e.commit();
 	}
 	
 	public static void setDistrictFilter(Context c, District district) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		
 		Editor e = sp.edit();
 		e.putLong(c.getString(R.string.preference_district_id), district.getId());
 		e.putString(c.getString(R.string.preference_district_name), district.getName());
 		e.commit();		
 	}
 	
 	public static AdType getAdTypeFilter(Context c) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		return AdType.valueOf(sp.getString(c.getString(R.string.preference_adtype), null));
 	}
 	
 	// Returns category ID
 	public static Long getCategoryFilter(Context c) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		return sp.getLong(c.getString(R.string.preference_category_id), -1);
 	}
 	
 	// Returns district ID
 	public static Long getDistrictFilter(Context c) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		return sp.getLong(c.getString(R.string.preference_district_id), -1);
 	}
 	
 	public static String getCategoryFilterName(Context c) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		return sp.getString(c.getString(R.string.preference_category_name), "");
 	}
 	
 	public static String getDistrictFilterName(Context c) {
 		SharedPreferences sp = c.getSharedPreferences(c.getString(R.string.shared_preferences_name), 0);
 		return sp.getString(c.getString(R.string.preference_district_name), "");
 	}
 }
