 package cornell.eickleapp;
 
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class DrinkCalendar extends Activity implements OnClickListener {
 
 	int selectedMonth, selectedYear;
 	Calendar calendar = Calendar.getInstance();
 	GridView drinkCalendar;
 	TextView monthDisplay, yearDisplay, bottomDisplay, infoDisplay, drinkCount, drinkEst, dogCount;
 	RelativeLayout drink_img, dog_img;
 	ImageButton back, next;
 	ArrayList<Button> drinkBacButtons = new ArrayList<Button>();
 	ArrayList<String> numbers = new ArrayList<String>();
 	
 	private ArrayList<Integer> drinkingDays;
 	private ArrayList<Double> maxBac;
 	private ArrayList<Integer> bacColors;
 	private DatabaseHandler db;
 	private ArrayList<DatabaseStore> day_colors;
 	private ArrayList<DatabaseStore> day_values;
 	private ArrayList<DatabaseStore> day_guess;
 	private ArrayList<Integer> day_counts;
 	private ArrayList<DatabaseStore> hotdogs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.drinkcalendar);
 
 		db = new DatabaseHandler(this);
 		drinkCalendar = (GridView) findViewById(R.id.gvDrinkCalendar);
 		monthDisplay = (TextView) findViewById(R.id.tvMonth);
 		yearDisplay = (TextView) findViewById(R.id.tvYear);
 		bottomDisplay = (TextView) findViewById(R.id.tvCalendarBottomDisplay);
 		infoDisplay = (TextView) findViewById(R.id.tvInfoDisplay);
 		drinkCount = (TextView) findViewById(R.id.drink_count);
 		//drinkEst = (TextView) findViewById(R.id.drink_est);
 		back = (ImageButton) findViewById(R.id.bPreviousMonth);
 		next = (ImageButton) findViewById(R.id.bNextMonth);
 		drink_img = (RelativeLayout) findViewById(R.id.drink_img);
 		dog_img = (RelativeLayout)findViewById(R.id.hot_dog_img);
 		dogCount = (TextView) findViewById(R.id.hot_dog_count);
 		back.setOnClickListener(this);
 		next.setOnClickListener(this);
 
 		drinkingDays = new ArrayList<Integer>();
 		maxBac = new ArrayList<Double>();
 		bacColors = new ArrayList<Integer>();
 
 		day_guess = new ArrayList<DatabaseStore>(); 
 		hotdogs = new ArrayList<DatabaseStore>();
 		
 		//Get the values from the DB
 		Date date = new Date();
 		calculateValues(date);
 
 		selectedMonth = calendar.get(Calendar.MONTH);
 		selectedYear = calendar.get(Calendar.YEAR);
 		setMonthFromInt(selectedMonth);
 		yearDisplay.setText(Integer.toString(selectedYear));
 		ColorAdapter adapter = new ColorAdapter(this, selectedMonth,
 				selectedYear, drinkingDays, maxBac, bacColors);
 		drinkCalendar.setAdapter(adapter);
 		drinkBacButtons = adapter.getButtonView();
 		SharedPreferences getPrefs = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		Boolean checkSurveyed = getPrefs.getBoolean("hints", true);
 		if (checkSurveyed) {
 			Intent openHint = new Intent(this, DrinkCalendarTutorial.class);
 			startActivity(openHint);
 		}
 		
 	}
 
 	/*
 	 * Construct necessary Lists for the DB
 	 */
 	private void convertToLists(ArrayList<DatabaseStore> color, ArrayList<DatabaseStore> values){
 		for(int i=0; i<values.size(); i++){
 			DatabaseStore ds = values.get(i);
 			drinkingDays.add(ds.day);
 			maxBac.add(Double.valueOf(ds.value));
 			bacColors.add(Integer.parseInt(color.get(i).value));
 		}
 	}
 	
 	/*
 	 * Must sort both color and values by time before calling.
 	 */
 	private void getMaxForDays(ArrayList<DatabaseStore> colors, ArrayList<DatabaseStore> values){
 		assert(colors.size() == values.size());
 		
 		day_colors = new ArrayList<DatabaseStore>();
 		day_values = new ArrayList<DatabaseStore>();
 		day_counts = new ArrayList<Integer>();
 		
 		DatabaseStore max_day=null;
 		DatabaseStore max_color=null;
 		if(values!=null){
 			int cnt = 0;
 			for(int i=0; i< values.size(); i++){
 				cnt+=1;
 				DatabaseStore s = values.get(i);
 				if(max_day == null){
 					max_day = s;
 					max_color = colors.get(i);
 				}else{
 					if(max_day.day < s.day){
 						day_colors.add(max_color);
 						day_values.add(max_day);
 						day_counts.add(cnt);
 						cnt=0;
 						max_day = s;
 						max_color = colors.get(i);
 					} else if(Double.valueOf(max_day.value)< Double.valueOf(s.value)){
 						max_day = s;
 						max_color = colors.get(i);
 					}
 				}
 			}
 			//add final values
 			day_values.add(max_day);
 			day_colors.add(max_color);
 			day_counts.add(cnt);
 		}
 	}
 	private void calculateValues(Date date){
 		maxBac.clear();
 		bacColors.clear();
 		drinkingDays.clear();
 		
 		ArrayList<DatabaseStore> bac_colors = (ArrayList<DatabaseStore>) db
 				.getVarValuesForMonth("bac_color", date);
 		ArrayList<DatabaseStore> bac_vals = (ArrayList<DatabaseStore>)db.getVarValuesForMonth("bac", date);
 		day_guess = (ArrayList<DatabaseStore>)db.getVarValuesForMonth("drink_guess", date);
 		hotdogs = (ArrayList<DatabaseStore>)db.getVarValuesForMonth("hot_dogs",date);
 		
 		if(bac_colors != null && bac_vals != null && hotdogs != null){
 			bac_colors = DatabaseStore.sortByTime(bac_colors);
 			bac_vals = DatabaseStore.sortByTime(bac_vals);
 			hotdogs = DatabaseStore.sortByTime(hotdogs);
 			if(day_guess!=null){
 				day_guess =DatabaseStore.sortByTime(day_guess);
 			}
 			getMaxForDays(bac_colors, bac_vals);
 			convertToLists(day_colors, day_values);
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 
 		GregorianCalendar gc = new GregorianCalendar(selectedYear, selectedMonth, 1);
 		Date date = new Date();
 		switch (v.getId()) {
 		case R.id.bNextMonth:		
 			
 			gc.add(Calendar.MONTH, 1);
 			date = gc.getTime();
 			
 			if (selectedMonth + 1 > 11) {
 				selectedMonth = 0;
 				selectedYear++;
 				yearDisplay.setText(Integer.toString(selectedYear));
 			} else
 				selectedMonth++;
 			setMonthFromInt(selectedMonth);
 			break;
 		case R.id.bPreviousMonth:
 			
 			gc.add(Calendar.MONTH, -1);
 			date = gc.getTime();
 			
 			if (selectedMonth - 1 < 0) {
 				selectedMonth = 11;
 				selectedYear--;
 				yearDisplay.setText(Integer.toString(selectedYear));
 			} else
 				selectedMonth--;
 			setMonthFromInt(selectedMonth);
 			break;
 		}
 		calculateValues(date);
 		ColorAdapter adapter = new ColorAdapter(this, selectedMonth,
 				selectedYear, drinkingDays, maxBac, bacColors);
 		drinkCalendar.setAdapter(adapter);
 		drinkBacButtons = adapter.getButtonView();
 	}
 
 	// inputs the int value of month and outputs its corresponding name
 	private void setMonthFromInt(int num) {
 		String month = "wrong";
 		DateFormatSymbols dfs = new DateFormatSymbols();
 		String[] months = dfs.getMonths();
 		month = months[num];
 		monthDisplay.setText(month);
 	}
 	
 	public void changeBottomDisplay(String entry, double bac, int index) {
 		//bottomDisplay.setText(entry);
 		int info_color = 0;
 		String info_txt="";
 		String est = "";
 		String cnt = "";
 		String dogs = "";
 		if (bac == 0){
 			info_color = 0xFF0099CC;
 			info_txt = "Click on a colored date for more information.";
 			est = "";
 			cnt="";
 			dogs ="";
 		}else{
 			if(index != -1){
 				if(day_counts.get(index)==1){
 					cnt = String.valueOf(day_counts.get(index)) + " Drink Tracked.";
 				}else{
 					cnt = String.valueOf(day_counts.get(index)) + " Drinks Tracked.";
 				}
 				if(day_guess!=null){
 					est = day_guess.get(index).value;
 				}
 				int num_dogs = Integer.parseInt(hotdogs.get(index).value);
 				if(num_dogs ==1){
 					dogs = "Which is calorically equivalent to 1 hot dog.";
 				}else{
 					dogs = "Which is calorically equivalent to " + num_dogs + " hot dogs."; 
 				}
 			}
 			info_txt ="BAC: " + entry+ "\n\n";
 			if (bac < 0.06) {
 				info_color = 0x884D944D;
 				if(bac < 0.02){
 					info_txt += "-Begin to feel relaxed.\n-Reaction time slows.\n";
 				} else {
 					info_txt += "-Euphoria, \"the buzz\"\n-Sociability.\n-Decrease in judgement and reasoning.\n";
 				}
 			} else if (bac < 0.15) {
 				info_color = 0X88E68A2E;
 				if(bac <=0.08) {
 					info_txt +="-Legally Intoxicated.\n-Balance and Coordination impaired.\n-Less self-control.";
 				}else{
 					info_txt += "-Clear deterioration of cognitive judgement and motor coordination.\n-Speech may be slurred.\n";
 				}
 			} else if (bac < 0.24) {
 				info_color = 0X88A30000;
 				info_txt +="-At risk for blackout.\n-Nausea.\n-Risk of stumbling and falling.\n";
 			} else {
 				if(bac < .35){ 
 					info_txt += "-May be unable to walk.\n-May pass out or lose conciousness.\n-Seek medical attention.\n";
 				}else{
 					info_txt += "-High risk for coma or death.\n";
 				}
 				info_color = 0XCC000000;
 			}
 		}
 		infoDisplay.setText(info_txt);
 		infoDisplay.setBackgroundColor(info_color);
 		drinkCount.setText(cnt);
 		dogCount.setText(dogs);
 		
 		int count = 0;
 		if(index!=-1){
 			int num_dogs = Integer.parseInt(hotdogs.get(index).value);
 			for(int i=0; i<=num_dogs; i++){
 				count+=1;
 				ImageView iv = new ImageView(this);
 				iv.setBackgroundResource(R.drawable.hot_dog);
 				iv.setId(i);
 				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(80,80);
 				if(i>0){
 					ImageView last = (ImageView)dog_img.findViewById(i-1);
 					if(count >10){
 						p.addRule(RelativeLayout.BELOW, i-10);
 						p.addRule(RelativeLayout.ALIGN_START, i-10);
 					}else{
 						p.addRule(RelativeLayout.RIGHT_OF, last.getId());	
 					}
 				}
 			
 				iv.setLayoutParams(p);
 				dog_img.addView(iv, p);
 			}
 			count = 0;
 			for(int i=0; i<=day_counts.get(index); i++){
 				count+=1;
 				ImageView iv = new ImageView(this);
 				iv.setBackgroundResource(R.drawable.beer_icon);
 				iv.setId(i);
 				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(60,60);
 				if(i>0){
 					ImageView last = (ImageView)drink_img.findViewById(i-1);
 					if(count >15){
 						p.addRule(RelativeLayout.BELOW, i-15);
 						p.addRule(RelativeLayout.ALIGN_START, i-15);
 					}else{
 						p.addRule(RelativeLayout.RIGHT_OF, last.getId());	
 					}
 				}
 			
 				iv.setLayoutParams(p);
 				drink_img.addView(iv, p);
 			}
 		}else if(index==-1){
 			drink_img.removeAllViews();
 			dog_img.removeAllViews();
 		}
 	}
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setDisplayShowTitleEnabled(false);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		Intent openPage;
 		switch (item.getItemId()) {
 
 		case R.id.tracking_menu:
 			openPage = new Intent(this, DrinkCounter.class);
 			startActivity(openPage);
 			break;
 		case R.id.assess_menu:
 			openPage = new Intent(this, Assessment.class);
 			startActivity(openPage);
 			break;
 		case R.id.visualize_menu:
 			openPage = new Intent(this, VisualizeMenu.class);
 			startActivity(openPage);
 			break;
 		case R.id.setting_menu:
 			openPage = new Intent(this, Settings.class);
 			startActivity(openPage);
 			break;
 		case android.R.id.home:
 			openPage = new Intent(this, MainMenu.class);
 			startActivity(openPage);
 			break;
 
 		}
 		return true;
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		finish();
 	}
 
 
 }
