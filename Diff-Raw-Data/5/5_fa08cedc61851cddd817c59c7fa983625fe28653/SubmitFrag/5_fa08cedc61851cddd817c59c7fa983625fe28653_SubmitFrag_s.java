 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 
 
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceFragment;
 
 public class SubmitFrag extends PreferenceFragment implements OnPreferenceChangeListener {
 	
 	//UI References
 	public static ListPreference TypeListPref, CatListPref;
 	
 	//public static Item.Category cat = Item.Category.MISC;
 	
 	public void syncTypePref(Item.Type value) {
     	Submit activity = (Submit)getActivity();
 		TypeListPref.setValue(((Integer)value.ordinal()).toString());
	TypeListPref.setTitle(R.string.pref_type + " - " + value.getLocalizedValue(activity));
     	TypeListPref.setSummary(value.getLocalizedDescription(activity));
 	}
 	
 	public void syncCatPref(Item.Category value) {
 	Submit activity = (Submit)getActivity();
 	CatListPref.setValue(((Integer)value.ordinal()).toString());
		CatListPref.setTitle(R.string.cat_type + " - " + value.getLocalizedValue(activity));
 		CatListPref.setSummary(value.getLocalizedDescription(activity));
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.pref_type);
         
         //Gets type from ListPreference
         TypeListPref = (ListPreference) findPreference("type_pref");
         CatListPref = (ListPreference) findPreference("cat_pref");
         
         syncTypePref(((Submit)getActivity()).getItemType());
         
         //TODO: Create one listener class for all Preferences. Maybe.
         TypeListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
 		Item.Type newType = Item.Type.forInt(Integer.parseInt((String)newValue));
 		syncTypePref(newType);
 		((Submit)getActivity()).setItemType(newType);
                 return true;
             }
         });
         
         CatListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
 		Item.Category newCategory = Item.Category.forInt(Integer.parseInt((String)newValue));
 		syncCatPref(newCategory);
 		((Submit)getActivity()).setItemCategory(newCategory);
             	return true;
                 }
             });
 	}
 	
 	
 
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 }
