 package me.toddpickell.baristalog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class DeviceState extends Activity {
 	// needs to extend activity to have access to getSharedPreferences()
 	
 	
 	private String device_type;
 	private List<String> deviceNames;
 	private List<String> subTitles;
 	private List<Integer> subTimes;
 	private List<Integer> defaultTimes;
 	private SharedPreferences device;
 	private String aero = "aeropress";
 	private String chem = "chemex";
 	private String clev = "clever";
 	private String espr = "espresso";
 	private String fren = "french press";
 	private String pour = "pour over";
 	private Integer default_pre, default_bloom, default_brew;
 	private Integer pre, bloom, brew;
 	private Integer total;
 	private String firstSub, secondSub, thirdSub;
 	private Boolean countdown = true;
 	private Context context;
 	
 	
 	
 	
 	public DeviceState(Context context, String device_type) {
 		super();
 		this.context = context;
 		this.device_type = device_type;
 		defaultTimes = new ArrayList<Integer>();
 		subTimes = new ArrayList<Integer>();
 		subTitles = new ArrayList<String>();
 		deviceNames = new ArrayList<String>();
 		deviceNames.add(aero);
 		deviceNames.add(chem);
 		deviceNames.add(clev);
 		deviceNames.add(fren);
 		deviceNames.add(pour);
 		initDeviceWithName();
 	}
 	
 	private void initDeviceWithName() {
 		if (deviceNames.contains(device_type)) {
 			// then device name is in list for shared preferences
 			
 			device = context.getSharedPreferences(device_type, 0); // getting null pointer from here not sure why yet
 			
 			if (device.contains("pre") && device.contains("default_pre")) {
 				// then file is already in place
 				getDeviceSettingsFromFile();
 				
 			} else {
 				// file is not setup with data
 				setupNewDevice();
 				
 			}
 			// now load settings into Lists
 			addDeviceSettingsToLists();
 			
 		} else if (device_type.equals(espr)) {
 			// espresso device doesn't have changeable settings 
 			// so nothing is saved to shared preferences
 			setupForEspressoDevice();
 		}
 	}
 	
 	private void addDeviceSettingsToLists() {
 		defaultTimes.clear();
 		subTimes.clear();
 		subTitles.clear();
 		
 		if (pre != null) {
 			subTimes.add(pre);
 		}
 		if (bloom != null) {
 			subTimes.add(bloom);
 		}
 		if (brew != null) {
 			subTimes.add(brew);
 		}
 		if (firstSub != null) {
 			subTitles.add(firstSub);
 		}
 		if (secondSub != null) {
 			subTitles.add(secondSub);
 		}
 		if (thirdSub != null) {
 			subTitles.add(thirdSub);
 		}
 		if (default_pre != null) {
 			defaultTimes.add(default_pre);
 		}
 		if (default_bloom != null) {
 			defaultTimes.add(default_bloom);
 		}
 		if (default_brew != null) {
 			defaultTimes.add(default_brew);
 		}
 	}
 
 	private void getDeviceSettingsFromFile() {
 	
 		Log.d("PKL", "Loading settings from device");
 		
 		if (device.contains("pre")) {
 			pre = device.getInt("pre", 0);
 		}
 		if (device.contains("bloom")) {
 			bloom = device.getInt("bloom", 0);
 		}
 		if (device.contains("brew")) {
 			brew = device.getInt("brew", 0);
 		}
 		if (device.contains("total")) {
 			total = device.getInt("total", 0);
 		}
 		if (device.contains("firstSub")) {
 			firstSub = device.getString("firstSub", "");
 		}
 		if (device.contains("secondSub")) {
 			secondSub = device.getString("secondSub", "");
 		}
 		if (device.contains("thirdSub")) {
 			thirdSub = device.getString("thirdSub", "");
 		}
 		if (device.contains("default_pre")) {
 			default_pre = device.getInt("default_pre", 0);
 		}
 		if (device.contains("default_bloom")) {
 			default_bloom = device.getInt("default_bloom", 0);
 		}
 		if (device.contains("default_brew")) {
 			default_bloom = device.getInt("default_bloom", 0);
 		}
 		countdown = true;
 	}
 
 	private void setupNewDevice() {
 		SharedPreferences.Editor editor = device.edit();		
 		
 		if (device_type.equals(chem) || device_type.equals(clev)) {
 			setupForChemexCleverDevice();
 			
 		} else if (device_type.equals(fren)) {
 			setupForFrenchPressDevice();
 			
 		} else if (device_type.equals(pour)) {
 			setupForPourOverDevice();
 			
 		} else if (device_type.equals(aero)) {
 			setupForAeroDevice();
 			
 		} else {
 			setupForEspressoDevice();
 		}
 		
 		if (!pre.equals(0)) {
 			editor.putInt("pre", pre);
 		}
 		if (!bloom.equals(0)) {
 			editor.putInt("bloom", bloom);
 		}
 		if (!brew.equals(0)) {
 			editor.putInt("brew", brew);
 		}
 		if (!total.equals(0)) {
 			editor.putInt("total", total);
 		}
 		if (!firstSub.equals("")) {
 			editor.putString("firstSub", firstSub);
 		}
 		if (!secondSub.equals("")) {
 			editor.putString("secondSub", secondSub);
 		}
 		if (!thirdSub.equals("")) {
 			editor.putString("thirdSub", thirdSub);
 		}
 		if (!default_pre.equals(0)) {
 			editor.putInt("default_pre", default_pre);
 		}
 		if (!default_bloom.equals(0)) {
 			editor.putInt("default_bloom", default_bloom);
 		}
 		if (!default_brew.equals(0)) {
 			editor.putInt("default_brew", default_brew);
 		}
 		
 		editor.commit();
 	}
 
 	private void setupForChemexCleverDevice() {
 		//setup object for chemex or clever
 		pre = 30;
 		bloom = 30;
 		brew = 180;
 		total = 240;
 		default_pre = 30;
 		default_bloom = 30;
 		default_brew = 180;
 		firstSub = "Pre";
 		secondSub = "Bloom";
 		thirdSub = "Brew";
 		countdown = true;
 	}
 
 	private void setupForFrenchPressDevice() {
 		//setup object for french press
 		pre = 30;
 		bloom = 180;
 		brew = 30;
 		total = 240;
 		default_pre = 30;
 		default_bloom = 180;
 		default_brew = 30;
 		firstSub = "Bloom";
 		secondSub = "Brew";
 		thirdSub = "Plunge";
 		countdown = true;
 	}
 
 	private void setupForPourOverDevice() {
 		//setup object for pour over
 		pre = 30;
 		bloom = 30;
 		brew = 90;
 		total = 150;
 		default_pre = 30;
 		default_bloom = 30;
 		default_brew = 90;
 		firstSub = "Pre";
 		secondSub = "Bloom";
 		thirdSub = "Brew";
 		countdown = true;
 	}
 
 	private void setupForAeroDevice() {
 		//setup for aeropress
 		pre = 10;
 		bloom = 20;
 		brew = 0;
 		total = 30;
 		default_pre = 10;
 		default_bloom = 20;
 		default_brew = 0;
 		firstSub = "Steep";
 		secondSub = "Plunge";
 		thirdSub = "";
 		countdown = true;
 	}
 
 	private void setupForEspressoDevice() {
 		//setup for espresso
 		pre = 0;
 		bloom = 0;
 		brew = 0;
 		total = 0;
 		default_pre = 0;
 		default_bloom = 0;
 		default_brew = 0;
 		firstSub = "";
 		secondSub = "";
 		thirdSub = "";
 		countdown = false;
 	}
 	
 	public void resetDeviceToDefaults() {
 		editDeviceTimes(defaultTimes);
 	}
 	
 	public List<String> getSubTitles() {
 		return subTitles;
 	}
 
 	public void setSubTitles(List<String> subTitles) {
 		this.subTitles = subTitles;
 	}
 
 	public List<Integer> getSubTimes() {
 		return subTimes;
 	}
 
 	public void setSubTimes(List<Integer> subTimes) {
 		this.subTimes = subTimes;
 	}
 
 	private List<Integer> getDefaultTimes() {
 		return defaultTimes;
 	}
 
 	public Integer getTotal() {
 		return total;
 	}
 
 	public Boolean getCountdown() {
 		return countdown;
 	}
 
 	public String popSubTitle() {
 		String temp = "";
 		if (!subTitles.isEmpty()) {
 			temp = subTitles.get(0);
 			subTitles.remove(0);
 		} 
 		return temp;
 	}
 
 	public Integer popSubTime() {
 		Integer temp = 0;
 		if (!subTimes.isEmpty()) {
 			temp = subTimes.get(0);
 			subTimes.remove(0);
 		}
 		return temp;		
 	}
 
 	public void editDeviceTimes(List<Integer> times) {
 		SharedPreferences.Editor editor = device.edit();
 		
 		editor.putInt("pre", times.get(0));
 		editor.putInt("bloom", times.get(1));
		editor.putInt("brew", times.get(2));
		editor.putInt("total", (times.get(0) + times.get(1) + times.get(2)));
 
 		editor.commit();
 		
 		getDeviceSettingsFromFile();
 		addDeviceSettingsToLists();
 	}
 }
 
 
 
 
 
 
 
 
