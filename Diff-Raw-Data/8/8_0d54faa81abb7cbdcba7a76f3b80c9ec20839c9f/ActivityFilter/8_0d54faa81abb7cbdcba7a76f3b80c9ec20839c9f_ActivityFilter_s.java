 package put.medicallocator.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import put.medicallocator.R;
 import put.medicallocator.utils.FilterManager;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 
 public class ActivityFilter extends ListActivity {
 	private static final String TAG = ActivityFilter.class.getName();
 
 	/** 
 	 * Indicates the default distance in kilometers around which facilities query will be performed 
 	 */
 	public static final int DEFAULT_DISTANCE_IN_KILOMETERS = 5;
 
 	/** 
 	 * Indicates the maximum distance in kilometers around which facilities query will be performed 
 	 */
 	public static final int MAX_DISTANCE_IN_KILOMETERS = 10;
 	
 	/* Identifiers of the inputs/outputs for this activity */
 	public static final int FILTER_REQUEST_CODE = 0x0;
 	public static final String RESULT_FILTER_ARRAY = "ActivityFilter.RESULT_FILTER_ARRAY";
 	public static final String RESULT_DISTANCE = "ActivityFilter.RESULT_DISTANCE";
 	public static final String INPUT_FILTER_ARRAY = "ActivityFilter.INPUT_FILTER_ARRAY";
 	public static final String INPUT_DISTANCE = "ActivityFilter.INPUT_DISTANCE";
 
 	/* UI-related components */
 	private SeekBar distanceSeekBar;
 	private TextView distanceTextView;
 	
 	/* Data related with this activity */
 	private String[] receivedFilters;
 	private int currentDistance;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_filter);
         
         /* Read the input data */
         receivedFilters = getIntent().getStringArrayExtra(INPUT_FILTER_ARRAY);
         currentDistance = getIntent().getIntExtra(INPUT_DISTANCE, DEFAULT_DISTANCE_IN_KILOMETERS);
 
         /* Set the ListView related attributes */
         setListAdapter(new ArrayAdapter<String>(
         		this, android.R.layout.simple_list_item_multiple_choice, 
         		FilterManager.availableFilters));
         getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         checkInputFilters();   
         
         /* Retrieve the UI components */
         distanceSeekBar = (SeekBar) findViewById(R.id.distance_seekbar);
         distanceTextView = (TextView) findViewById(R.id.distance_textview);
         
         /* Set the UI attributes */
        distanceSeekBar.setMax(MAX_DISTANCE_IN_KILOMETERS);
         distanceSeekBar.setOnSeekBarChangeListener(listener);
         updateUI();
     }
 
 	@Override
     public void finish() {
     	/* 
     	 * TODO: It may be a dirty hack. However, it seems better for me to override finish()
     	 * than hook into the back key. 
     	 */
     	setActivityResult();
     	super.finish();
     }
 
 	public void onResetToDefaultsClick(View v) {
 		currentDistance = DEFAULT_DISTANCE_IN_KILOMETERS;
 		clearFiltersSelection();
 		updateUI();
 	}
 
 	private void updateUI() {
		distanceSeekBar.setProgress(currentDistance);
 		distanceTextView.setText(currentDistance + " km");
 	}
 	
     private void clearFiltersSelection() {
     	for (int i=0; i<getListView().getCount(); i++) {
     		getListView().setItemChecked(i, false);
     	}
 	}
 
 	private void checkInputFilters() {
     	if (receivedFilters != null) {
     		for (int i=0; i<getListView().getCount(); i++) {
     			final String currentItem = (String) getListAdapter().getItem(i);
     			for (int j=0; j<receivedFilters.length; j++) {
     				if (currentItem.equals(receivedFilters[j])) {
     					getListView().setItemChecked(i, true);
     					break;
     				}
     			}
     		}
     	}
 	}
 	
     private void setActivityResult() {
     	final Intent intent = new Intent();
     	final ListView listView = getListView();
     	
     	ArrayList<String> filters = new ArrayList<String>();
     	for (int i=0; i<listView.getCount(); i++) {
     		if (listView.isItemChecked(i)) {
     			filters.add(FilterManager.availableFilters[i]);
     		}
     	}
     	final String[] resultArray = filters.toArray(new String[filters.size()]);
     	Log.d(TAG, "Following filters were chosen: " + Arrays.toString(resultArray));
     	
     	intent.putExtra(RESULT_FILTER_ARRAY, resultArray);
     	intent.putExtra(RESULT_DISTANCE, currentDistance);
     	
     	if (getParent() == null) {
     	    setResult(RESULT_OK, intent);
     	} else {
     	    getParent().setResult(RESULT_OK, intent);
     	}
     }
     
     private OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {
 		
 		public void onStopTrackingTouch(SeekBar seekBar) {
 		}
 		
 		public void onStartTrackingTouch(SeekBar seekBar) {
 		}
 		
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
			currentDistance = seekBar.getProgress();
 			updateUI();
 		}
 	};
     
 }
