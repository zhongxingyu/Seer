 package com.capstonecontrol;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import com.capstonecontrol.client.ModulesRequestFactory;
 import com.capstonecontrol.client.ModulesRequestFactory.PowerDataFetchService;
 import com.capstonecontrol.shared.PowerDataProxy;
 import com.google.web.bindery.requestfactory.shared.Receiver;
 import com.google.web.bindery.requestfactory.shared.ServerFailure;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView.GraphViewSeries;
 import com.jjoe64.graphview.LineGraphView;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.os.StrictMode.ThreadPolicy;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 public class PowerManagementActivity extends BarListActivity {
 
 	private Context mContext = this;
 	private static final String TAG = "LogsActivity";
 	public static List<PowerData> powerDataArray = new ArrayList<PowerData>();
 	public static List<PowerData> matchingPowerDataArray = new ArrayList<PowerData>();
 	public static List<ModuleEvent> moduleEventsSuggested = new ArrayList<ModuleEvent>();
 	public static ArrayList<String> moduleEventsList = new ArrayList<String>();
 	public static List<ScheduledModuleEvent> scheduledModuleEvents = new ArrayList<ScheduledModuleEvent>();
 	private Button oneButton, sevenButton;
 	private ListView lv;
 	Spinner dateSpinner, moduleSpinner, typeSpinner;
 	public static boolean oneDay;
 	private float averageWatts, usedkWHr;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.power_management);
 		// @TODO REMOVE BELOW AND PUT AND POST STATEMENT IN ITS OWN THREAD
 		ThreadPolicy tp = ThreadPolicy.LAX;
 		StrictMode.setThreadPolicy(tp);
 		// enable bar
 		enableBar();
 		// enable POST capabilities
 		enablePOST();
 		// handle spinners
 		// set up submit button
 		setUpSubmitButtons();
 
 	}
 
 	private void setUpSubmitButtons() {
 		this.oneButton = (Button) this.findViewById(R.id.oneDayButton);
 		this.oneButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				// first disable the button
 				oneDay = true;
 				oneButton.setEnabled(false);
 				sevenButton.setEnabled(false);
 				// clear previous request
 				powerDataArray.clear();
 				scheduledModuleEvents.clear();
 				// now get the new info
 				if (powerDataArray.isEmpty()) {
 					getPowerDataInfo(true, 1);
 				}
 				displayMessageList("Downloading data, may take a minute or two...");
 			}
 		});
 		this.sevenButton = (Button) this.findViewById(R.id.sevenDayButton);
 		this.sevenButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				oneDay = false;
 				// first disable the button
 				oneButton.setEnabled(false);
 				sevenButton.setEnabled(false);
 				// clear previous request
 				powerDataArray.clear();
 				scheduledModuleEvents.clear();
 				// now get the new info
 				if (powerDataArray.isEmpty()) {
 					getPowerDataInfo(true, 7);
 				}
 				displayMessageList("Downloading data, may take a minute or two...");
 			}
 		});
 	}
 
 	private void displayMessageList(String message) {
 		moduleEventsList.clear();
 		moduleEventsList.add(message);
 		// create list
 		lv = getListView();
 		ArrayAdapter<String> arrayAdapter =
 		// new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
 		// BarActivity.alertsList);
 		new ArrayAdapter<String>(this, R.layout.list_text_style2,
 				moduleEventsList);
 		lv.setAdapter(arrayAdapter);
 	}
 
 	private void showGraph() {
 		// create graphview
 		GraphViewData[] wattData = new GraphView.GraphViewData[matchingPowerDataArray
 				.size()];
 		for (int i = 0; i < matchingPowerDataArray.size(); i++) {
 			wattData[i] = new GraphView.GraphViewData(i,
 					Double.parseDouble(matchingPowerDataArray.get(i).getData()));
 		}
 		GraphViewSeries wattDataSeries = new GraphViewSeries(wattData);
 
 		GraphView graphView = new LineGraphView(this // context
 				, "Watts vs Time" // heading
 		);
 		graphView.addSeries(wattDataSeries); // data
 		if (oneDay) {
 			graphView.setHorizontalLabels(new String[] { "24 hrs ago",
 					"16 hrs ago", "8 hrs ago", "now" });
 		} else {
 			graphView.setHorizontalLabels(new String[] { "7 days ago",
 					"6", "5", "4", "3",
 					"2", "1 day ago", "now" });
 		}
 		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutGraph);
 		layout.removeAllViews();
 		layout.addView(graphView);
 	}
 
 	private void getPowerDataInfo(final boolean display, final int days) {
 		new AsyncTask<Void, Void, List<PowerData>>() {
 
 			@Override
 			protected List<PowerData> doInBackground(Void... arg0) {
 				ModulesRequestFactory requestFactory = Util.getRequestFactory(
 						mContext, ModulesRequestFactory.class);
 				final PowerDataFetchService request = (PowerDataFetchService) requestFactory
 						.powerDataFetchRequest();
 				Log.i(TAG, "Sending request to server");
 				request.getPowerData().fire(
 						new Receiver<List<PowerDataProxy>>() {
 							@Override
 							public void onFailure(ServerFailure error) {
 								// do nothing, no modules found
 								int failurestop = 4;
 							}
 
 							@Override
 							public void onSuccess(List<PowerDataProxy> arg0) {
 								for (int i = 0; i < arg0.size(); i++) {
 									// create temporary module infro
 									// proxy, tmi
 									PowerDataProxy tmi = arg0.get(i);
 									powerDataArray.add(new PowerData(tmi
 											.getModuleName(), tmi.getData(),
 											tmi.getUser(), tmi.getDate()));
 								}
 							}
 						});
 				return powerDataArray;
 			}
 
 			protected void onPostExecute(List<PowerData> result) {
 				pullOutMatchingByTime();
 				calculateAverageWatts();
 				calculatekWHrUsage();
 				displayCalculations();
 				showGraph();
 				oneButton.setEnabled(true);
 				sevenButton.setEnabled(true);
 			}
 
 		}.execute();
 	}
 
 	protected void displayCalculations() {
 		moduleEventsList.clear();
 		moduleEventsList.add("Average Watt Reading: " + averageWatts);
 		moduleEventsList.add("Total kWHr used: " + usedkWHr);
 		// create list
 		lv = getListView();
 		ArrayAdapter<String> arrayAdapter =
 		// new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
 		// BarActivity.alertsList);
 		new ArrayAdapter<String>(this, R.layout.list_text_style2,
 				moduleEventsList);
 		lv.setAdapter(arrayAdapter);
 
 	}
 
 	protected void pullOutMatchingByTime() {
 		int secondsToCompare = 0;
 		matchingPowerDataArray.clear();
 		PowerData myPowerData;
 		Date currentDate = new Date();
 		if (oneDay == true) {
 			// only save data with in one day
 			secondsToCompare = 86400;
 		} else {
 			// save data up to seven days ago
 			secondsToCompare = 604800;
 		}
 		// pull out data
 		for (int i = 0; i < PowerManagementActivity.powerDataArray.size(); i++) {
 			myPowerData = PowerManagementActivity.powerDataArray.get(i);
 			int test = (int) ((currentDate.getTime() - myPowerData.getDate()
 					.getTime()) / 1000);
 			if (test < secondsToCompare) {
 				matchingPowerDataArray.add(myPowerData);
 			}
 		}
 
 	}
 
 	protected void calculatekWHrUsage() {
 		usedkWHr = 0;
 		usedkWHr = (float) ((averageWatts / 1000) * (2.2 * matchingPowerDataArray
 				.size() / 60));
 
 	}
 
 	protected void calculateAverageWatts() {
 		averageWatts = 0;
 		int totalWatts = 0;
 		for (int i = 0; i < matchingPowerDataArray.size(); i++) {
 			totalWatts = totalWatts
 					+ Integer.parseInt(matchingPowerDataArray.get(i).getData());
 		}
 		averageWatts = totalWatts / matchingPowerDataArray.size();
 	}
 
 }
