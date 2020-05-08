 package com.t2.vas.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart;
 import org.achartengine.chart.LineChart;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.chart.XYChart;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Adapter;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.ViewAnimator;
 import android.widget.ViewSwitcher;
 import android.view.GestureDetector;
 
 import com.t2.vas.ArraysExtra;
 import com.t2.vas.Global;
 import com.t2.vas.MathExtra;
 import com.t2.vas.R;
 import com.t2.vas.SharedPref;
 import com.t2.vas.VASAnalytics;
 import com.t2.vas.data.DataProvider;
 import com.t2.vas.db.DBAdapter;
 import com.t2.vas.db.tables.Group;
 import com.t2.vas.db.tables.Note;
 import com.t2.vas.db.tables.Result;
 import com.t2.vas.db.tables.Scale;
 import com.t2.vas.view.OffsetGraphicalChartView;
 import com.t2.vas.view.SimpleCursorDateSectionAdapter;
 import com.t2.vas.view.ToggledButton;
 
 public abstract class ABSResultsActivity extends ABSNavigationActivity implements OnClickListener, OnItemClickListener, GestureDetector.OnGestureListener {
 	public static final String EXTRA_TIME_START = "timeStart";
 	public static final String EXTRA_CALENDAR_FIELD = "calendarField";
 	
 	private static final String KEY_NAME = "results_visible_ids_";
 	private static final String TAG = "ABSResultsActivity";
 	
 	private static final int DIRECTION_PREVIOUS = -1;
 	private static final int DIRECTION_NONE = 0;
 	private static final int DIRECTION_NEXT = 1;
 
 	private ListView keysList;
 	private ListView notesList;
 
 	private ArrayList<KeyItem> keyItems;
 	
 	private KeyItemAdapter keysAdapter;
 	private SimpleCursorDateSectionAdapter notesAdapter;
 
 	private ToggledButton keysTabButton;
 	private ToggledButton notesTabButton;
 	//private ViewGroup chartsContainer;
 
 	protected Calendar startCal;
 	protected Calendar endCal;
 	protected int calendarField;
 	private TextView monthNameTextView;
 	
 	SimpleDateFormat monthNameFormatter = new SimpleDateFormat("MMMM, yyyy");
 	//private ViewGroup backgroundChartContainer;
 	//private ViewGroup foregroundChartContainer;
 	private ViewSwitcher chartSwitcher;
 	private Animation slideInFromLeftAnimation;
 	private Animation slideOutToRightAnimation;
 	private Animation slideInFromRightAnimation;
 	private Animation slideOutToLeftAnimation;
 	private Animation fadeInAnimation;
 	private Animation fadeOutAnimation;
 	private Animation fadeNoneAnimation;
 	private GestureDetector gestureDetector;
 	private DataProvider dataProvider;
 	private Cursor notesCursor;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		dataProvider = this.getDataProvider();
 		gestureDetector = new GestureDetector(this, this);
 		
 		long startTime = this.getIntent().getLongExtra(EXTRA_TIME_START, 0);
 		if(startTime == 0) {
 			startTime = Calendar.getInstance().getTimeInMillis();
 		}
 		
 		calendarField = this.getIntent().getIntExtra(EXTRA_CALENDAR_FIELD, Calendar.DAY_OF_MONTH);
 		
 		// Set the time ranges.
 		startCal = Calendar.getInstance();
 		startCal.setTimeInMillis(MathExtra.roundTime(startTime, calendarField));
 		startCal.set(calendarField, startCal.getMinimum(calendarField));
 		
 		endCal = Calendar.getInstance();
 		endCal.setTimeInMillis(startCal.getTimeInMillis());
 		endCal.add(Calendar.MONTH, 1);
 		
 		
 		// Set the content view.
 		this.setContentView(R.layout.abs_results_activity);
 		this.monthNameTextView = (TextView) this.findViewById(R.id.monthName);
 		//this.chartsContainer = (ViewGroup) this.findViewById(R.id.chartContainer);
 		//this.backgroundChartContainer = (ViewGroup) this.findViewById(R.id.backgroundChartContainer);
 		//this.foregroundChartContainer = (ViewGroup) this.findViewById(R.id.foregroundChartContainer);
 		this.chartSwitcher = (ViewSwitcher) this.findViewById(R.id.chartSwitcher);
 		
 		this.slideInFromLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
 		this.slideOutToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
 		this.slideInFromRightAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
 		this.slideOutToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
 		this.fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
 		this.fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
 		this.fadeNoneAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_none);
 		
 		// Set the month name.
 		this.monthNameTextView.setText(monthNameFormatter.format(startCal.getTime()));
 		
 		// Prepare the notes adapter.
 		notesCursor = new Note(dbAdapter).queryForNotes(-1, -1, "timestamp DESC");
 		this.startManagingCursor(notesCursor);
 		notesAdapter = SimpleCursorDateSectionAdapter.buildNotesAdapter(
 				this, 
 				notesCursor,
 				new SimpleDateFormat(Global.NOTES_LONG_DATE_FORMAT),
 				new SimpleDateFormat(Global.NOTES_SECTION_DATE_FORMAT)
 		);
 		
 		// Prepare the keys adapter.
 		keyItems = getKeyItems();
 		
 		// set the color and visibility for each key item.
 		ArrayList<Long> visibleIds = getVisibleIds(getSettingSuffix());
 		int keyCount = keyItems.size();
 		for(int i = 0; i < keyCount; ++i) {
 			KeyItem item = keyItems.get(i);
 			
 			item.color = getKeyColor(i, keyCount);
 			item.visible = visibleIds.contains(item.id);
 		}
 		
 		keysAdapter = new KeyItemAdapter(this, getKeyItemViewType(), keyItems);
 		
 		
 		keysList = (ListView) this.findViewById(R.id.keysList);
 		keysList.setAdapter(keysAdapter);
 		if(isKeyItemsClickable()) {
 			//Log.v(TAG, "Clickable");
 			keysList.setOnItemClickListener(this);
 		}
 		
 		notesList = (ListView) this.findViewById(R.id.notesList);
 		notesList.setAdapter(notesAdapter);
 		notesList.setOnItemClickListener(this);
		notesList.setFastScrollEnabled(true);
 		
 		keysTabButton = (ToggledButton)this.findViewById(R.id.keysTabButton);
 		keysTabButton.setOnClickListener(this);
 		
 		notesTabButton = (ToggledButton)this.findViewById(R.id.notesTabButton);
 		notesTabButton.setOnClickListener(this);
 		
 		this.findViewById(R.id.monthMinusButton).setOnClickListener(this);
 		this.findViewById(R.id.monthPlusButton).setOnClickListener(this);
 		
 		generateChart(DIRECTION_NEXT);
 		showKeysTab();
 	}
 	
 	
 	
 	protected abstract String getSettingSuffix();
 	
 	protected abstract ArrayList<KeyItem> getKeyItems();
 	
 	protected abstract int getKeyItemViewType();
 	
 	protected abstract DataProvider getDataProvider();
 	
 	private ArrayList<DataPoint> loadData(KeyItem item, long startTime, long endTime, int calendarGroupByField) {
 		ArrayList<Long> xValues = getDataPoints(startTime, endTime, calendarGroupByField);
 		LinkedHashMap<Long,DataPoint> points = new LinkedHashMap<Long, DataPoint>();
 		ArrayList<DataPoint> outPoints = new ArrayList<DataPoint>();
 		
 		for(int i = 0; i < xValues.size(); ++i) {
 			DataPoint dp = new DataPoint(xValues.get(i), 50);
 			points.put(xValues.get(i), dp);
 			outPoints.add(dp);
 		}
 		
 		HashMap<Long,Double> data = dataProvider.getData(item.id, startTime, endTime);
 		for(Long time: data.keySet()) {
 			long roundedTime = MathExtra.roundTime(time, calendarGroupByField);
 			DataPoint dp = points.get(roundedTime);
 			if(dp != null) {
 				dp.addValue(data.get(time));
 			}
 		}
 		
 		return outPoints;
 	}
 	
 	private ArrayList<Long> getDataPoints(long startTime, long endTime, int calendarGroupByField) {
 		Calendar startCal = Calendar.getInstance();
 		Calendar endCal = Calendar.getInstance();
 		startCal.setTimeInMillis(startTime);
 		endCal.setTimeInMillis(endTime);
 		
 		startCal.setTimeInMillis(MathExtra.roundTime(startCal.getTimeInMillis(), calendarGroupByField));
 		endCal.setTimeInMillis(MathExtra.roundTime(endCal.getTimeInMillis(), calendarGroupByField));
 		
 		ArrayList<Long> dataPoints = new ArrayList<Long>();
 		Calendar runningCal = Calendar.getInstance();
 		runningCal.setTimeInMillis(startCal.getTimeInMillis());
 		while(true) {
 			if(runningCal.getTimeInMillis() >= endTime) {
 				break;
 			}
 			
 			switch(calendarGroupByField) {
 			case Calendar.MONTH:
 				dataPoints.add(runningCal.getTimeInMillis());
 				runningCal.add(Calendar.MONTH, 1);
 				break;
 			case Calendar.DAY_OF_MONTH:
 				dataPoints.add(runningCal.getTimeInMillis());
 				runningCal.add(Calendar.DAY_OF_MONTH, 1);
 				break;
 			}
 		}
 		
 		return dataPoints;
 	}
 	
 	private class DataPoint {
 		public final long time;
 		private double valueSum = 0.00;
 		private int count = 0;
 		public double minValue = 0.00;
 		public double maxValue = 0.00;
 		private ArrayList<Double> values = new ArrayList<Double>();
 		private double defaultValue = 0.00;
 		
 		public DataPoint(long time, double defaultValue) {
 			this.time = time;
 			this.defaultValue = defaultValue;
 		}
 		
 		public void addValue(double val) {
 			values.add(val);
 			valueSum += val;
 			++count;
 			
 			if(val > maxValue || count == 1) {
 				maxValue = val;
 			}
 			
 			if(val < minValue || count == 1) {
 				minValue = val;
 			}
 		}
 		
 		public double getAverageValue() {
 			if(valueSum == 0 && count == 0) {
 				return defaultValue;
 			}
 			return valueSum / count;
 		}
 		
 		public double[] getValues() {
 			double[] out = new double[values.size()];
 			for(int i = 0; i < values.size(); ++i) {
 				out[i] = values.get(i);
 			}
 			return out;
 		}
 	}
 	
 	protected boolean isKeyItemsClickable() {
 		return true;
 	}
 
 	protected int getKeyColor(int currentIndex, int totalCount) {
 		float hue = currentIndex / (1.00f * totalCount) * 360.00f;
 		
 		return Color.HSVToColor(
     			255,
     			new float[]{
     				hue,
     				1.0f,
     				1.0f
     			}
     	);
 	}
 	
 	protected final void showKeysTab() {
 		keysTabButton.setChecked(true);
 		notesTabButton.setChecked(false);
 		
 		keysList.setVisibility(View.VISIBLE);
 		notesList.setVisibility(View.INVISIBLE);
 	}
 	
 	protected final void showNotesTab() {
 		keysTabButton.setChecked(false);
 		notesTabButton.setChecked(true);
 		
 		keysList.setVisibility(View.INVISIBLE);
 		notesList.setVisibility(View.VISIBLE);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if(arg0 == keysList) {
 			onKeysItemClicked(keyItems.get(arg2), arg1, arg2, arg3);
 			
 		} else if(arg0 == notesList) {
 			onNotesItemClicked(arg1, arg2, arg3);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 			case R.id.monthMinusButton:
 				monthMinusButtonPressed();
 				break;
 				
 			case R.id.monthPlusButton:
 				monthPlusButtonPressed();
 				break;
 				
 			case R.id.keysTabButton:
 				showKeysTab();
 				break;
 				
 			case R.id.notesTabButton:
 				showNotesTab();
 				break;
 		}
 	}
 	
 	protected void monthMinusButtonPressed() {
 		startCal.add(Calendar.MONTH, -1);
 		endCal.add(Calendar.MONTH, -1);
 		this.monthNameTextView.setText(monthNameFormatter.format(startCal.getTime()));
 		generateChart(DIRECTION_PREVIOUS);
 		
 		notesList.setSelection(notesAdapter.getPositionForTimestamp(endCal.getTimeInMillis()));
 	}
 	
 	protected void monthPlusButtonPressed() {
 		startCal.add(Calendar.MONTH, 1);
 		endCal.add(Calendar.MONTH, 1);
 		this.monthNameTextView.setText(monthNameFormatter.format(startCal.getTime()));
 		generateChart(DIRECTION_NEXT);
 		
 		notesList.setSelection(notesAdapter.getPositionForTimestamp(endCal.getTimeInMillis()));
 	}
 	
 	protected abstract void onKeysItemClicked(KeyItem keyItem, View view, int pos, long id);
 	
 	private void onNotesItemClicked(View view, int pos, long id) {
 		Note note = new Note(dbAdapter);
 		note._id = id;
 		note.load();
 		
 		// Show a dialog with the full note.
 		new AlertDialog.Builder(this)
 			.setTitle(getString(R.string.note_details_title))
 			.setMessage(note.note)
 			.setCancelable(true)
 			.setPositiveButton(R.string.close, new DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			})
 			.create()
 			.show();
 	}
 	
 	private void onKeyToggleButtonCheckedChanged() {
 		saveVisibleKeyIds();
 		generateChart(DIRECTION_NONE);
 	}
 	
 	private void generateChart(int direction) {
 		XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		LineChart chart = new LineChart(dataSet, renderer);
 		
 		//Log.v(TAG, "StartCal:"+startCal.getTime() +" "+ startCal.get(Calendar.MILLISECOND));
 		//Log.v(TAG, "EndCal:"+endCal.getTime() +" "+ endCal.get(Calendar.MILLISECOND));
 		
 		ArrayList<DataPoint> dataPoints = null;
 		long startTime = startCal.getTimeInMillis();
 		long endTime = endCal.getTimeInMillis();
 		for(int i = 0; i < keyItems.size(); ++i) {
 			KeyItem item = keyItems.get(i);
 			
 			if(!item.visible) {
 				continue;
 			}
 			
 			XYSeries series = new XYSeries(item.title1);
 			
 			dataPoints = loadData(keyItems.get(i), startTime, endTime, calendarField);
 			//Log.v(TAG, "DataPoints:"+dataPoints.size());
 			for(int j = 0; j < dataPoints.size(); ++j) {
 				DataPoint dp = dataPoints.get(j);
 				Calendar cal = Calendar.getInstance();
 				cal.setTimeInMillis(dp.time);
 				
 				if(dp.getValues().length > 0) {
 					//Log.v(TAG, "V:"+cal.get(Calendar.DAY_OF_MONTH)+","+dp.getAverageValue());
 					series.add(cal.get(calendarField), dp.getAverageValue());
 				}
 			}
 			
 			XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
 			seriesRenderer.setColor(item.color);
 			seriesRenderer.setPointStyle(PointStyle.CIRCLE);
 			seriesRenderer.setFillPoints(true);
 			
 			renderer.addSeriesRenderer(seriesRenderer);
 			dataSet.addSeries(series);
 		}
 		
 		// Disable the switch animation.
 		chartSwitcher.setInAnimation(null);
 		chartSwitcher.setOutAnimation(null);
 		
 		if(dataSet.getSeriesCount() > 0) {
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(dataPoints.get(0).time);
 			
 			Calendar weekCal = Calendar.getInstance();
 			weekCal.setTimeInMillis(startCal.getTimeInMillis());
 			int dow = weekCal.get(Calendar.DAY_OF_WEEK);
 			weekCal.add(Calendar.DAY_OF_MONTH, 7 - dow + 2);
 			
 			int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
 			int firstMondayOfMonth = weekCal.get(Calendar.DAY_OF_MONTH);
 			
 			renderer.setShowGrid(false);
 			renderer.setAxesColor(Color.WHITE);
 			renderer.setLabelsColor(Color.WHITE);
 			renderer.setAntialiasing(true);
 			renderer.setShowLegend(false);
 			renderer.setYLabels(0);
 			renderer.setXLabels(15);
 			renderer.setYAxisMax(100.00);
 			renderer.setYAxisMin(0.00);
 			renderer.setXAxisMin(1.00);
 			renderer.setXAxisMax(lastDayOfMonth);
 			
 			/*// Mark mondays in the X-axis
 			for(int i = 0; i < dataPoints.size(); ++i) {
 				Calendar c = Calendar.getInstance();
 				c.setTimeInMillis(dataPoints.get(i).time);
 				
 				if(c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
 					renderer.addTextLabel(c.get(calendarField), "M");
 				}
 			}*/
 			
 			// Add the weekend backgrounds.
 			for(int i = firstMondayOfMonth-18; i < lastDayOfMonth; i+=7) {
 				int xStart = i;
 				int xEnd = i+2;
 				int y = 100;
 				
 				if(xStart < 1) {
 					xStart = 1;
 				}
 				
 				if(xEnd <= 1) {
 					continue;
 				}
 				
 				XYSeries weekSeries = new XYSeries("week "+i);
 				weekSeries.add(xStart, y);
 				weekSeries.add(xEnd, y);
 				
 				XYSeriesRenderer weekSeriesRenderer = new XYSeriesRenderer();
 				weekSeriesRenderer.setColor(Color.TRANSPARENT);
 				weekSeriesRenderer.setLineWidth(0.0f);
 				weekSeriesRenderer.setFillBelowLine(true);
 				weekSeriesRenderer.setFillBelowLineColor(Color.argb(10, 0, 0, 0));
 				
 				renderer.addSeriesRenderer(weekSeriesRenderer);
 				dataSet.addSeries(weekSeries);
 			}
 			
 			OffsetGraphicalChartView chartView = new OffsetGraphicalChartView(this, chart);
 			if(chartSwitcher.getChildCount() > 1) {
 				chartSwitcher.removeViewAt(0);
 			}
 			
 			// Make the switcher appear.
 			chartSwitcher.addView(chartView, LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
 			if(chartSwitcher.getVisibility() == View.INVISIBLE) {
 				chartSwitcher.setVisibility(View.VISIBLE);
 				chartSwitcher.startAnimation(fadeInAnimation);
 			}
 			
 			// Set the chart transition animation.
 			switch(direction) {
 			case DIRECTION_NONE:
 				chartSwitcher.setInAnimation(null);
 				chartSwitcher.setOutAnimation(null);
 				break;
 				
 			case DIRECTION_NEXT:
 				chartSwitcher.setInAnimation(slideInFromRightAnimation);
 				chartSwitcher.setOutAnimation(slideOutToLeftAnimation);
 				break;
 				
 			case DIRECTION_PREVIOUS:
 				chartSwitcher.setInAnimation(slideInFromLeftAnimation);
 				chartSwitcher.setOutAnimation(slideOutToRightAnimation);
 				break;
 			}
 			
 			chartSwitcher.showNext();
 		} else {
 			chartSwitcher.startAnimation(fadeOutAnimation);
 			chartSwitcher.setVisibility(View.INVISIBLE);
 			chartSwitcher.removeAllViews();
 		}
 	}
 	
 	
 	private void saveVisibleKeyIds() {
 		String keySuffix = getSettingSuffix();
 		ArrayList<Long> toggledIds = new ArrayList<Long>();
 		for(int i = 0; i < keyItems.size(); ++i) {
 			KeyItem item = keyItems.get(i);
 			if(item.visible) {
 				toggledIds.add(item.id);
 			}
 		}
 		setVisibleIds(keySuffix, toggledIds);
 	}
 	
 	private ArrayList<Long> getVisibleIds(String keySuffix) {
 		String[] idsStrArr = SharedPref.getValues(
 				sharedPref, 
 				KEY_NAME+keySuffix, 
 				",",
 				new String[0]
 		);
 		
 		return new ArrayList<Long>(
 				Arrays.asList(
 						ArraysExtra.toLongArray(idsStrArr)
 				)
 		);
 	}
 	
 	private void setVisibleIds(String keySuffix, ArrayList<Long> ids) {
 		SharedPref.setValues(
 				sharedPref, 
 				KEY_NAME+keySuffix, 
 				",", 
 				ArraysExtra.toStringArray(ids.toArray(new Long[ids.size()]))
 		);
 	}
 	
 	@Override
     public boolean onTouchEvent(MotionEvent event) {
             return gestureDetector.onTouchEvent(event);
     }
 	
 	@Override
 	public boolean onDown(MotionEvent e) {
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		//Log.v(TAG, "v:"+ velocityX +","+ velocityY);
 		if(Math.abs(velocityY) < 50 || Math.abs(velocityX) < 200) {
 			return false;
 		}
 		
 		if(velocityX > 200) {
 			monthMinusButtonPressed();
 			return true;
 			
 		} else if(velocityX < -200) {
 			monthPlusButtonPressed();
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		return false;
 	}
 	
 	
 	class KeyItem {
 		public long id;
 		public String title1;
 		public String title2;
 		public int color;
 		public boolean visible;
 		
 		public KeyItem(long id, String title1, String title2) {
 			this.id = id;
 			this.title1 = title1;
 			this.title2 = title2;
 		}
 		
 		
 		public HashMap<String,Object> toHashMap() {
 			HashMap<String,Object> data = new HashMap<String,Object>();
 			data.put("id", id);
 			data.put("title1", title1);
 			data.put("title2", title2);
 			data.put("color", color);
 			data.put("visible", visible);
 			return data;
 		}
 	}
 	
 	class KeyItemAdapter extends ArrayAdapter<KeyItem> {
 		public static final int VIEW_TYPE_ONE_LINE = 1;
 		public static final int VIEW_TYPE_TWO_LINE = 2;
 		
 		private LayoutInflater layoutInflater;
 		private int layoutId;
 
 		public KeyItemAdapter(Context context, int viewType,
 				List<KeyItem> objects) {
 			super(context, viewType, objects);
 			
 			layoutInflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
 			if(viewType == VIEW_TYPE_TWO_LINE) {
 				layoutId = R.layout.list_item_result_key_2;
 			} else {
 				layoutId = R.layout.list_item_result_key_1;
 			}
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if(convertView == null) {
 				convertView = layoutInflater.inflate(layoutId, null);
 			}
 
 			final KeyItem item = this.getItem(position);
 			TextView tv1 = (TextView)convertView.findViewById(R.id.text1);
 			TextView tv2 = (TextView)convertView.findViewById(R.id.text2);
 			ToggleButton tb = (ToggleButton)convertView.findViewById(R.id.showKeyToggleButton);
 			View keyBox = convertView.findViewById(R.id.keyBox);
 			
 			if(tv1 != null) {
 				tv1.setText(item.title1);
 			}
 			if(tv2 != null) {
 				tv2.setText(item.title2);
 			}
 			
 			if(tb != null) {
 				if(isKeyItemsClickable()) {
 					tb.setFocusable(false);
 				}
 				tb.setOnCheckedChangeListener(null);
 				tb.setChecked(item.visible);
 				tb.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 					@Override
 					public void onCheckedChanged(
 							CompoundButton buttonView, boolean isChecked) {
 						item.visible = isChecked;
 						onKeyToggleButtonCheckedChanged();
 					}
 				});
 			}
 			
 			if(keyBox != null) {
 				keyBox.setBackgroundColor(item.color);
 			}
 			
 			return convertView;
 		}
 	}
 }
