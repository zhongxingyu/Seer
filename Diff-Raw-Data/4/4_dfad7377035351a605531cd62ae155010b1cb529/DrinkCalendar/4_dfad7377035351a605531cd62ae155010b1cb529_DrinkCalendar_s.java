 package cornell.drinkingapp;
 
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import cornell.drinkingapp.R;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.TextView;
 
 public class DrinkCalendar extends Activity implements OnClickListener {
 
 	int selectedMonth, selectedYear;
 	Calendar calendar = Calendar.getInstance();
 	GridView drinkCalendar;
 	TextView monthDisplay, yearDisplay, bottomDisplay, infoDisplay;
 	Button back, next;
 	ArrayList<Button> drinkBacButtons = new ArrayList<Button>();
 	ArrayList<String> numbers = new ArrayList<String>();
 	
 	private ArrayList<Integer> drinkingDays;
 	private ArrayList<Double> maxBac;
 	private ArrayList<Integer> bacColors;
 	private DatabaseHandler db;
 	private ArrayList<DatabaseStore> day_colors;
 	private ArrayList<DatabaseStore> day_values;
 
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
 		back = (Button) findViewById(R.id.bPreviousMonth);
 		next = (Button) findViewById(R.id.bNextMonth);
 		back.setOnClickListener(this);
 		next.setOnClickListener(this);
 
 		drinkingDays = new ArrayList<Integer>();
 		maxBac = new ArrayList<Double>();
 		bacColors = new ArrayList<Integer>();
 		
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
 		
 		DatabaseStore max_day=null;
 		DatabaseStore max_color=null;
 		for(int i=0; i< values.size(); i++){
 			DatabaseStore s = values.get(i);
 			if(max_day == null){
 				max_day = s;
 				max_color = colors.get(i);
 			}else{
 				if(max_day.day < s.day){
 					day_colors.add(max_color);
 					day_values.add(max_day);
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
 	}
 	private void calculateValues(Date date){
 		maxBac.clear();
 		bacColors.clear();
 		drinkingDays.clear();
 		ArrayList<DatabaseStore> bac_colors = (ArrayList<DatabaseStore>) db
 				.getVarValuesForMonth("bac_color", date);
 		ArrayList<DatabaseStore> bac_vals = (ArrayList<DatabaseStore>)db.getVarValuesForMonth("bac", date);
 		if(bac_colors != null && bac_vals != null){
 			bac_colors = DatabaseStore.sortByTime(bac_colors);
 			bac_vals = DatabaseStore.sortByTime(bac_vals);
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
 
 	public void changeBottomDisplay(String entry, double bac) {
 		//bottomDisplay.setText(entry);
 		int info_color = 0;
 		String info_txt="";
 		if (bac == 0){
 			info_color = 0xFF0099CC;
 			info_txt = "Click on a colored date for more information.";
 		}else{
 		
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
				info_color += 0X88A30000;
				info_txt ="-At risk for blackout.\n-Nausea.\n-Risk of stumbling and falling.\n";
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
 	}
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		finish();
 	}
 
 
 }
